 package org.motechproject.functional.base;
 
 import org.openqa.selenium.WebDriver;
 
 public abstract class BaseWebDriver {
    protected WebDriver driver;
 
     public abstract WebDriver getDriver();
 
     public void open(String pageurl) {
         driver.navigate().to(pageurl);
     }
 }
