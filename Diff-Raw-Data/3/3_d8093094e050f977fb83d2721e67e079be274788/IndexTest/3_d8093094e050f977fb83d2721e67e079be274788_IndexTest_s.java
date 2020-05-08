 package com.sebprunier.jobboard.web;
 
 //import com.saucelabs.common.SauceOnDemandAuthentication; 
 //import com.saucelabs.common.SauceOnDemandSessionIdProvider; 
 import static junit.framework.Assert.assertEquals;
 
 import java.net.URL;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.Platform;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 
 public class IndexTest {
 
     private WebDriver driver;
 
     @Before
     public void setUp() throws Exception {
         String remoteWebDriverUrl = System.getProperty("remoteWebDriverUrl");
 
         DesiredCapabilities capabillities = DesiredCapabilities.iphone();
         capabillities.setCapability("version", "5.0");
         capabillities.setCapability("platform", Platform.MAC);
         this.driver = new RemoteWebDriver(new URL(remoteWebDriverUrl), capabillities);
     }
 
     @Test
     public void basic() throws Exception {
         driver.get("http://www.amazon.com/");
        assertEquals("Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more",
                driver.getTitle());
     }
 
     @After
     public void tearDown() throws Exception {
         driver.quit();
     }
 
 }
