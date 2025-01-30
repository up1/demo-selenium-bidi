package com.example.demo2;

import com.google.common.net.MediaType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.module.Network;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.remote.http.Contents;
import org.openqa.selenium.remote.http.Filter;
import org.openqa.selenium.remote.http.HttpResponse;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SeleniumWithNetworkInterceptorTest {

    static WebDriver driver;

    @BeforeAll
    public static void initial() {
        System.setProperty("webdriver.chrome.driver", "./chromedriver-mac-arm64/chromedriver");
        var options = new ChromeOptions();
        // Enable BiDi
        options.setCapability("webSocketUrl", true);
        driver = new ChromeDriver(options);
    }

    @AfterAll
    public static void cleanAll() {
        driver.quit();
    }

    @Test
    @DisplayName("ทำการจำลองข้อมูลการเรียกใช้งาน API ด้วย Selenium WebDriver BiDi")
    public void interceptRequests() throws InterruptedException {
        AtomicBoolean completed = new AtomicBoolean(false);

        try (NetworkInterceptor ignored = new NetworkInterceptor(driver, (Filter) next -> req -> {
            var res = next.execute(req);
            if (req.getUri().contains("https://demo-backend-nodejs.vercel.app/")) {
                res = new HttpResponse().setStatus(200)
                        // Allow CORS
                        .addHeader("Access-Control-Allow-Origin", "*")
                        .addHeader("Content-Type", MediaType.JSON_UTF_8.toString())
                        .setContent(Contents.utf8String("{\"message\":\"Mock Hello World!\"}"));
            }
            completed.set(true);
            return res;
        })) {

            driver.get("https://demo-frontend-reactjs.vercel.app/");
            Thread.sleep(2000);
        }

        assertTrue(completed.get());
        assertEquals("Mock Hello World!", driver.findElement(By.xpath("//*[@data-testid=\"message_box\"]")).getText());

    }

}
