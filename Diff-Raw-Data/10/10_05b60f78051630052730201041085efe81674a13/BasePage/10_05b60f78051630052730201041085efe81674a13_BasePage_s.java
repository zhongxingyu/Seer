 package org.motechproject.ghana.national.functional.pages;
 
 import org.motechproject.ghana.national.functional.util.*;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.Select;
 
 public class BasePage<T> {
     protected JavascriptExecutor javascriptExecutor = new JavascriptExecutor();
     protected PlatformSpecificExecutor platformSpecificExecutor = new PlatformSpecificExecutor();
     protected HtmlTableParser htmlTableParser = new HtmlTableParser();
     protected DateSelector dateSelector = new DateSelector();
     protected ElementPoller elementPoller = new ElementPoller();
 
     protected WebDriver driver;
 
     public BasePage(WebDriver driver) {
         this.driver = driver;
     }
 
     public WebDriver getDriver() {
         return driver;
     }
 
     protected T enter(WebElement webElement, String value) {
         webElement.clear();
         webElement.sendKeys(value);
         return (T) this;
     }
 
     protected T select(WebElement webElement, String value) {
         Select selectRegion = new Select(webElement);
         selectRegion.selectByValue(value);
         return (T) this;
     }
 
     protected WebElement find(By by) {
         return driver.findElement(by);
     }
 
     protected String attrValue(WebElement webElement, String key) {
         return webElement.getAttribute(key);
     }
 
     public void waitForSuccessfulCompletion() {
     }
 }
