 package com.roosterpark.rptime.selenium.control;
 
 import com.roosterpark.rptime.selenium.timer.WaitForVisible;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 /**
  * User: John
  * Date: 10/23/13
  * Time: 10:26 AM
  */
 public class TextField implements Control {
 
     private WebDriver driver;
     private WebElement element;
     private By by;
 
     public TextField(WebDriver driver, String elementId) {
         this.driver = driver;
         this.element = this.driver.findElement(By.id(elementId));
         this.by = By.id(elementId);
     }
 
     public void clear() {
         WaitForVisible waitForVisible = new WaitForVisible(element);
         waitForVisible.defaultWaitForVisible();
         element.clear();
     }
 
     public void enterText(String text) {
         WaitForVisible waitForVisible = new WaitForVisible(element);
         waitForVisible.defaultWaitForVisible();
         element.sendKeys(text);
     }
 
     public String getText() {
        return element.getAttribute("value");
     }
 
     @Override
     public WebDriver getDriver() {
         return driver;
     }
 
     @Override
     public WebElement getElement() {
         return element;
     }
 
     @Override
     public By getBy() {
         return by;
     }
 
     public boolean isValid() {
         return element.getAttribute("class").contains("ng-valid");
     }
 
 }
