 package test_util;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 
 /* For Sauce Labs OnDemand. */
 public class Sauce extends Base {
 
     protected Key key = new Key();
 
     private static DesiredCapabilities getBrowser(String browser, final String browserVersion) {
         browser = browser.toLowerCase();
 
         // Never specify version for Chrome
         if (browser.contentEquals("chrome")) {
             return DesiredCapabilities.chrome();
         }
 
         if (browser.contentEquals("firefox")) {
             final DesiredCapabilities cap = DesiredCapabilities.firefox();
             cap.setCapability("version", browserVersion);
             return cap;
         }
 
         if (browser.contentEquals("ie")) {
             final DesiredCapabilities cap = DesiredCapabilities
                     .internetExplorer();
             cap.setCapability("version", browserVersion);
             return cap;
         }
         
         if (browser.contentEquals("ipad")) {
             final DesiredCapabilities cap = DesiredCapabilities
                     .ipad();
             cap.setCapability("version", browserVersion);
             return cap;
         }
         
         if (browser.contentEquals("iphone")) {
             final DesiredCapabilities cap = DesiredCapabilities
                     .iphone();
             cap.setCapability("version", browserVersion);
             return cap;
         }
         
         // Sauce currently only supports one version for Android
         if (browser.contentEquals("android")) {
             final DesiredCapabilities cap = DesiredCapabilities
                     .android();
             cap.setCapability("version", "4");
             return cap;
         }
 
         throw new RuntimeException("Unknown browser: " + browser);
     }
 
     public static String timeNow() {
         return DateFormat.getDateTimeInstance().format(new Date());
     }
 
     protected void setUpDriver(final String operatingSystem, final String browser, final String browserVersion) {
         // Don't record videos.
         // capabillities.setCapability("record-video", false);
         // Don't record screenshots.
         // capabillities.setCapability("record-screenshots", false);
 
         final DesiredCapabilities capabilities = getBrowser(browser, browserVersion);
         capabilities.setCapability("platform", operatingSystem);
         final String name = operatingSystem + "_" + browser + "_" + timeNow();
         capabilities.setCapability("name", name);
 
         try {
             driver = new RemoteWebDriver(new URL(key.url()), capabilities);
         } catch (MalformedURLException e) {
             e.printStackTrace();
         }
 
         driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
         exec = (JavascriptExecutor) driver;
     }
 }
