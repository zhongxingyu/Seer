 package com.bulain.qunit;
 
 import java.util.concurrent.TimeUnit;
 
 import org.junit.After;
 import org.junit.Before;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 
 public abstract class BaseWebDriver {
     private static final String CHROME_DRIVER = "ChromeDriver";
     private static final String FIREFOX_DRIVER = "FirefoxDriver";
     private static final String INTERNET_EXPLORER_DRIVER = "InternetExplorerDriver";
 
     protected WebDriver driver;
 
     @Before
     public void setUp() {
         String driverName = System.getProperty("WebDriver");
         
         if (INTERNET_EXPLORER_DRIVER.equalsIgnoreCase(driverName)) {
             DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
             ieCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
             driver = new InternetExplorerDriver(ieCapabilities);
         } else if (FIREFOX_DRIVER.equalsIgnoreCase(driverName)) {
             driver = new FirefoxDriver();
         } else if (CHROME_DRIVER.equalsIgnoreCase(driverName)) {
             driver = new ChromeDriver();
         } else {
             HtmlUnitDriver htmlUnitDriver = new HtmlUnitDriver();
             htmlUnitDriver.setJavascriptEnabled(true);
             driver = htmlUnitDriver;
         }
     }
 
     protected void implicitlyWait(Long waitSeconds) {
         driver.manage().timeouts().implicitlyWait(waitSeconds, TimeUnit.SECONDS);
     }
 
     @After
     public void tearDown() {
         if (driver != null) {
             driver.quit();
         }
         driver = null;
     }
 
 }
