 package org.motechproject.functional.base;
 
 import com.gargoylesoftware.htmlunit.ElementNotFoundException;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.WebDriverWait;
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
         driver = webDrivers.get(System.getProperty(BROWSER, FIREFOX)).getDriver();
     }
 
     public WebDriver getWebDriver() {
         return driver;
     }
 
 
     public boolean WaitForElement_ID(final String elementid) {
 
         try {
 
             (new WebDriverWait(driver, 20)).until(new ExpectedCondition<Boolean>() {
                 public Boolean apply(WebDriver driver) {
                     return driver.findElement(By.id(elementid)).isDisplayed();
                 }
             });
        } catch (ElementNotFoundException ignored) {
         }
         return true;
     }
 }
