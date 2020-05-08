 package com.habitualcoder.springpetclinic.selenium.utils;
 
 import com.googlecode.yatspec.junit.SpecResultListener;
 import com.googlecode.yatspec.junit.WithCustomResultListeners;
 import com.googlecode.yatspec.state.givenwhenthen.TestState;
 import com.googlecode.yatspec.state.givenwhenthen.WithTestState;
 import com.habitualcoder.springpetclinic.selenium.home.HomePage;
 import com.habitualcoder.springpetclinic.selenium.utils.selenium.TestUrlUtil;
 import com.habitualcoder.springpetclinic.selenium.utils.yatspec.HtmlWithScreenShotResultListener;
 import com.habitualcoder.springpetclinic.selenium.utils.yatspec.ScreenShotHolder;
 import org.junit.After;
 import org.junit.Before;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebDriverBackedSelenium;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Arrays;
 
 public class SpringPetClinicWebDriverTestSupport extends TestState implements WithCustomResultListeners, WithTestState {
     protected static WebDriverBackedSelenium selenium;
     private static final Logger logger = LoggerFactory.getLogger(SpringPetClinicWebDriverTestSupport.class);
     private static final String LOCAL_APP_PORT="9966";
     private static final String LOCAL_APP_CONTEXT = "petclinic";
     private static final String LOCAL_APP_STARTING_URL = String.format("http://localhost:%s/%s/", LOCAL_APP_PORT, LOCAL_APP_CONTEXT);
 
     // useful flags to help with debugging - enabling you to actually see the pages as they are automated
     // false means using the htmlunit driver (invisible but quicker and more stable)
     private static final boolean USE_FIREFOX = true;
     private static final boolean DELAY_AFTER_TEST = false;
 
     private String screenShotName;
     protected TestUrlUtil urlUtil;
     protected HomePage homepage;
     private static int testCounter = 0;
 
     @Before
     public void setupSelenium() throws InterruptedException {
         WebDriver driver = null;
         if (USE_FIREFOX) {
             driver = new FirefoxDriver();
         } else {
             driver = new HtmlUnitDriver(true);
         }
         selenium = new WebDriverBackedSelenium(driver, LOCAL_APP_STARTING_URL);
         try {
             selenium.open(LOCAL_APP_STARTING_URL);
             selenium.waitForPageToLoad("2000");
         } catch (Exception e) {
             //For some reason the first request appears to sometimes fail...ignore it and hope the second will be successful (it often does)
             System.err.println("*** You can probably ignore the above error ***");
         }
         selenium.open(LOCAL_APP_STARTING_URL);
         selenium.waitForPageToLoad("2000");
         urlUtil = new TestUrlUtil(LOCAL_APP_PORT, LOCAL_APP_CONTEXT);
         homepage = new HomePage(selenium);
     }
 
     public void takeSnapshotOfBrowserWindow() throws Exception {
         String imageData = selenium.captureScreenshotToString();
         this.capturedInputAndOutputs.add(ScreenShotHolder.INTERESTING_GIVENS_KEY, new ScreenShotHolder(imageData));
         System.out.println("Captured screen shot");
     }
 
     @After
     public void closeSelenium() throws Exception {
         takeSnapshotOfBrowserWindow();
         if (DELAY_AFTER_TEST) {
             try {
                 Thread.sleep(2000L);
             } catch (InterruptedException e) {
             }
         }
         selenium.stop();
     }
 
     @Override
     public Iterable<SpecResultListener> getResultListeners() throws Exception {
        return Arrays.asList((SpecResultListener) new HtmlWithScreenShotResultListener());
     }
 }
