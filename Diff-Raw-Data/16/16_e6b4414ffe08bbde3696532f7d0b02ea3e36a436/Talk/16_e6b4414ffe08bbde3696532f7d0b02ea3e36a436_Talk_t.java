 package com.thoughtworks.twu.utils;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
import static com.thoughtworks.twu.utils.WaitForAjax.WaitForAjax;
 import static org.hamcrest.CoreMatchers.is;
 import static org.joda.time.DateTime.now;
 import static org.junit.Assert.assertThat;
 import static org.testng.Assert.assertTrue;
 
 public class Talk {
 
     public void newTalk(WebDriver webDriver){
         WebElement myTalksButton = webDriver.findElement(By.id("my_talks_button"));
         myTalksButton.click();
        try {
            WaitForAjax(webDriver);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         assertTrue(webDriver.findElement(By.id("new_talk")).isDisplayed());
         webDriver.findElement(By.id("new_talk")).click();
        try {
            WaitForAjax(webDriver);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         assertTrue(webDriver.findElement(By.id("title")).isDisplayed());
         webDriver.findElement(By.id("title")).sendKeys(now().toString());
         webDriver.findElement(By.id("description")).sendKeys("Seven wise men");
         webDriver.findElement(By.id("venue")).sendKeys("Ajanta Ellora");
         JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
         javascriptExecutor.executeScript("$('#datepicker').val('28/09/2012')");
         javascriptExecutor.executeScript("$('#timepicker').val('11:42 AM')");
        webDriver.findElement(By.id("new_talk_submit")).click();
     }
 }
