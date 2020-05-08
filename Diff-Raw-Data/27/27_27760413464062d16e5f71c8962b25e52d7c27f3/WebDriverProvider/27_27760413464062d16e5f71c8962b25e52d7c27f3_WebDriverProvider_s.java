 package org.motechproject.functional.base;
 
 import org.openqa.selenium.WebDriver;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.Map;
 
 @Component
 public class WebDriverProvider {
 
     private static final String BROWSER = "browser";
     private static final String FIREFOX = "fireFoxWebDriver";
 
     private WebDriver driver;
 
     @Autowired
     public WebDriverProvider(Map<String, BaseWebDriver> webDrivers) {
        driver = webDrivers.get(System.getProperty(BROWSER, FIREFOX)).getDriver();;
     }
 
     public WebDriver getWebDriver() {
         return driver;
     }
 }
