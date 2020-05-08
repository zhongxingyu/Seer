 package com.forum.web.page.tests;
 
 import com.forum.web.page.Browser;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 
 import java.util.List;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class QuestionDetailedTest {
     private Browser browser;
 
     @Before
     public void initializeBrowser() {
         browser = new Browser("http://localhost:8080/forum", true);
         browser.open("/");
     }
 
     @After
     public void closeBrowser() {
         browser.stop();
     }
 
     @Test
     public void shouldGoToDetailedView() {
         WebElement activityWall = browser.findElement(By.id("leftPane"));
         List<WebElement> questions = activityWall.findElements(By.tagName("a"));
         WebElement question = questions.get(0);
         String title = question.getText();
 
         question.click();
 
        WebElement detailedViewTitle = browser.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id("questionTitle")));
 
         assertThat(detailedViewTitle.getText(), is(title));
         assertTrue(browser.getCurrentUrl().contains("/question/view/"));
 
     }
 }
