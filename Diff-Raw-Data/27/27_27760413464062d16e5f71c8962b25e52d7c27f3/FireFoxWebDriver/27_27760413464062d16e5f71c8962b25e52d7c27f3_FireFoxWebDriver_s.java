 package org.motechproject.functional.base;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxBinary;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.firefox.FirefoxProfile;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import java.io.File;
 
 @Component
 public class FireFoxWebDriver extends BaseWebDriver {
 
     @Value("#{functionalTestProperties['firefox.loc']}")
     private String firefoxLocation;
 
     @Value("#{functionalTestProperties['firefox.display']}")
     private String firefoxDisplay;
 
     public WebDriver getDriver() {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setEnableNativeEvents(true);
        FirefoxBinary firefoxBinary = new FirefoxBinary(new File(firefoxLocation));
//        firefoxBinary.setEnvironmentProperty("DISPLAY", System.getProperty("functional.test.display", ":0.0"));
        firefoxBinary.setEnvironmentProperty("DISPLAY", "0.0");
        WebDriver driver = new FirefoxDriver(firefoxBinary,profile);
         return driver;
     }
 }
