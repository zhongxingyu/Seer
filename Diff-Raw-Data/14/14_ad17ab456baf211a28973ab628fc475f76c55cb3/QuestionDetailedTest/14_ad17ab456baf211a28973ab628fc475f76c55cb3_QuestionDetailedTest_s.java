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
 import static org.junit.Assert.assertTrue;
 
 public class QuestionDetailedTest {
     private WebDriver driver;
 
     @Before
     public void initializeWebDriver() {
         driver = new FirefoxDriver();
         driver.get("http://localhost:8080/forum");
     }
 
     @After
     public void closeBrowser() {
         driver.close();
     }
 
     @Test
     public void shouldGoToDetailedView() {
        WebElement activityWall = driver.findElement(By.className("activityWall"));
         List<WebElement> questions = activityWall.findElements(By.tagName("a"));
         WebElement question = questions.get(0);
         String title = question.getText();
 
         question.click();
 
         String detailedViewTitle = driver.findElement(By.tagName("h2")).getText();
         assertThat(detailedViewTitle, is(title));
         assertTrue(driver.getCurrentUrl().contains("/question/view/"));
     }
 }
