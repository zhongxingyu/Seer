 package org.openmrs.module.mirebalais.smoke;
 
import java.net.URL;
import java.util.concurrent.TimeUnit;

 import org.apache.commons.lang.SystemUtils;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.openmrs.module.mirebalais.smoke.pageobjects.SmokeTestProperties;
 import org.openqa.selenium.chrome.ChromeDriver;
 
 public abstract class BasicMirebalaisSmokeTest {
 
     protected SmokeTestProperties properties = new SmokeTestProperties();
 
 	protected static ChromeDriver driver;
 
     @BeforeClass
     public static void startWebDriver() {
         setupChromeDriver();
     	driver = new ChromeDriver();
         driver.manage().timeouts().implicitlyWait(2000, TimeUnit.MILLISECONDS);
     	driver.get(new SmokeTestProperties().getWebAppUrl());
     }
 
 	@AfterClass
     public static void stopWebDriver() {
         driver.quit();
     }
 	
 	private static void setupChromeDriver() {
         URL resource = null;
         ClassLoader classLoader = BasicMirebalaisSmokeTest.class.getClassLoader();
 
         if(SystemUtils.IS_OS_MAC_OSX) {
             resource = classLoader.getResource("chromedriver/mac/chromedriver");
         } else if(SystemUtils.IS_OS_LINUX) {
             resource = classLoader.getResource("chromedriver/linux/chromedriver");
         } else if(SystemUtils.IS_OS_WINDOWS) {
             resource = classLoader.getResource("chromedriver/windows/chromedriver.exe");
         }
         System.setProperty("webdriver.chrome.driver", resource.getPath());
 
     }
 
 }
