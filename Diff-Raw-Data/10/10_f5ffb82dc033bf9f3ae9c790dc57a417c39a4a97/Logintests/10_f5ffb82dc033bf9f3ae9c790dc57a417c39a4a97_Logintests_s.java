 package com.educo.tests.TestCases;
 
 
 import com.educo.tests.Helpers.Staticprovider;
 import com.educo.tests.PageOBjects.LoginPage.IndialoginPageObjects;
 import com.educo.tests.PageOBjects.LoginPage.UsaLoginPageObjects;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.PageFactory;
 import org.testng.annotations.*;
 
 import java.net.MalformedURLException;
 
 
 public class Logintests {
 
 
     public WebDriver driver;
 
     @Parameters({"browser"})
     @BeforeClass
     public void propsetup(@Optional("firefox") String browser) {
         com.educo.tests.Common.Properties.Properties.setProperties();
     }
 
     @BeforeTest
     public void b4test() {
         System.out.println("@BeforeTest");
     }
 
     @Parameters({"browser"})
     @BeforeMethod
     public void setup(@Optional("chrome") String browser) throws MalformedURLException, InterruptedException {
         com.educo.tests.Common.Properties.Properties.setProperties();
         DesiredCapabilities capability = null;
 
         if (browser.equalsIgnoreCase("chrome")) {
             System.out.println("chrome");
            System.setProperty("webdriver.chrome.driver", "D://Selnium//chromedriver.exe");
             capability = DesiredCapabilities.chrome();
             capability.setBrowserName("chrome");
             capability.setPlatform(org.openqa.selenium.Platform.ANY);
             //capability.setVersion("");
            driver = new RemoteWebDriver(capability);
 
         }
 
 
         if (browser.equalsIgnoreCase("Firefox")) {
             System.out.println("Firefox");
             capability = DesiredCapabilities.firefox();
             capability.setBrowserName("firefox");
             capability.setPlatform(org.openqa.selenium.Platform.WINDOWS);
             driver = new FirefoxDriver();
             System.out.println(driver);
         }
         System.setProperty("webdriver.chrome.driver", "D://Selnium//chromedriver.exe");
         // driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capability);
 
 
     }
 
     @AfterTest
     public void afterclass() {
        driver.quit();
 
     }
 
 
     @Test(dataProviderClass = Staticprovider.class, dataProvider = "UsaLogin")
     public void Login_As_Instructor_USA(String EmailInsUsa, String Password, String Profname) throws Exception {
 
 
         UsaLoginPageObjects usapage = PageFactory.initElements(driver, UsaLoginPageObjects.class);
         //Thread.sleep(1000);
         usapage.loginAndVerifyUSA(EmailInsUsa, Password, Profname);
         usapage.logout();
 
     }
 
     @Test(dataProviderClass = Staticprovider.class, dataProvider = "IndiaLogin")
     public void Login_As_Instructor_India(String EmailInsIndia, String Password, String Profname) throws Exception {
 
         IndialoginPageObjects LoginPagePageobj1 = PageFactory.initElements(driver, IndialoginPageObjects.class);
         LoginPagePageobj1.loginAndVerifyIndia(EmailInsIndia, Password, Profname);
         LoginPagePageobj1.logout();
 
     }
 
     @Test(groups = {"pain"}, dataProviderClass = Staticprovider.class, dataProvider = "InvalidLogin")
     public void InvalidLoginPromptTest(String uName, String Password) throws Exception {
 
         UsaLoginPageObjects usapage1 = PageFactory.initElements(driver, UsaLoginPageObjects.class);
         usapage1.invalidlogin("ed@test.com", "Password");
 
 
     }
 
 
 }
 
 
 
 
 
