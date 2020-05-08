 package com.forum.web.page.tests;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import java.util.List;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class ActivityWallTest {
     private WebDriver driver;
 
     @Before
     public void initializeWebDriver() {
         driver = new FirefoxDriver();
        driver.get("http://localhost:8080/app");
     }
 
     @After
     public void closeBrowser() {
         driver.close();
     }
 
     @Test
     public void shouldLoadMoreQuestions() {
        WebElement loadMore = driver.findElement(By.id("loadMoreQuestions"));
 
        WebElement activityWall = driver.findElement(By.className("activityWall"));
        List<WebElement> questions = activityWall.findElements(By.id("questionTitle"));
         assertThat(questions.size(), is(10));
 
         loadMore.click();
 
        questions = activityWall.findElements(By.id("questionTitle"));
         assertThat(questions.size(), is(20));
     }
 }
