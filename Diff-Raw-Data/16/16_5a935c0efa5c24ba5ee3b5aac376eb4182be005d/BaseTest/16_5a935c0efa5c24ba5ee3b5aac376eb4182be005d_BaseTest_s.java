 package be.cegeka.rsvz.ui;
 
 import be.cegeka.rsvz.webdriver.GlassFishServer;
 import org.junit.After;
 import org.junit.Before;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
 
 import java.io.File;
 import java.io.IOException;
 
 public class BaseTest {
     public static final String BASE_URL = "http://localhost:" + GlassFishServer.PORT + "/" + GlassFishServer.CONTEXT;
     private WebDriver driver;
 
     @Before
     public void init() throws Exception {
         GlassFishServer.run();
        driver = new InternetExplorerDriver();
     }
 
     @After
     public void cleanup() {
         driver.quit();
     }
 
     private String getLocalPath() throws IOException {
         String canonicalPath = new File(".").getCanonicalPath();
         if (canonicalPath.indexOf("richfaces") > 0) {
             return canonicalPath;
         } else {
             return canonicalPath + "/richfaces";
         }
     }
 
     private FirefoxProfile createFirefoxProfile() throws Exception {
         FirefoxProfile profile = new FirefoxProfile();
         profile.addExtension(new File(getLocalPath() + "/src/test/resources/firebug-1.8.4.xpi"));
         profile.setPreference("network.http.phishy-userpass-length", 255);
         profile.setPreference("extensions.firebug.currentVersion", "1.8.4");
         return profile;
     }
 
     public WebDriver getDriver() {
         return driver;
     }
 }
