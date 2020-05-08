 package test;
 
 import org.openqa.selenium.remote.CapabilityType;
 import com.saucelabs.common.SauceOnDemandAuthentication;
 import com.saucelabs.common.SauceOnDemandSessionIdProvider;
 import com.saucelabs.testng.SauceOnDemandAuthenticationProvider;
 import com.saucelabs.testng.SauceOnDemandTestListener;
 import org.apache.commons.lang.StringUtils;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.remote.SessionId;
 import org.testng.annotations.*;
 import java.lang.reflect.Method;
 import java.net.URL;
 import static org.testng.Assert.assertEquals;
 import org.openqa.selenium.By;
 import org.openqa.selenium.Platform;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Optional;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.interactions.Actions;
 import java.util.Date;
 
 /**
  *
  * @author Rami Saleem
  */
 @Listeners({SauceOnDemandTestListener.class})
 public class RegisterWithInvalidEmail implements SauceOnDemandSessionIdProvider, SauceOnDemandAuthenticationProvider {
 
     public SauceOnDemandAuthentication authentication;
 
     private WebDriver driver;
 
     /**
      * If the tests can rely on the username/key to be supplied by environment variables or the existence
      * of a ~/.sauce-ondemand file, then we don't need to specify them as parameters, just create a new instance
      * of {@link SauceOnDemandAuthentication} using the no-arg constructor.
      * @param username
      * @param key
      * @param os
      * @param browser
      * @param browserVersion
      * @param method
      * @throws Exception
      */
 
 @Parameters({"username", "key", "os", "browser", "browserVersion"})
     @BeforeMethod
     public void setUp(@Optional("icreativeapp") String username,
                       @Optional("8e40a4f9-07bd-4bdb-88f2-806eb88c63ab") String key,
                       @Optional("XP") String os,
                       @Optional("opera") String browser,
                       @Optional("11") String browserVersion,
                       Method method) throws Exception {
 
         if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(key)) {
            authentication = new SauceOnDemandAuthentication(username, key);
         } else {
            authentication = new SauceOnDemandAuthentication();
         }
 
  DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
         desiredCapabilities.setBrowserName(System.getenv("SELENIUM_BROWSER"));
         desiredCapabilities.setVersion(System.getenv("SELENIUM_VERSION"));
         desiredCapabilities.setCapability(CapabilityType.PLATFORM, System.getenv("SELENIUM_PLATFORM"));
         this.driver = new RemoteWebDriver(
                 new URL("http://icreativeapp:8e40a4f9-07bd-4bdb-88f2-806eb88c63ab@ondemand.saucelabs.com:80/wd/hub"),
                 desiredCapabilities);
     }
 
 
 
     /**
      * {@inheritDoc}
      * @return
      */
     @Override
     public String getSessionId() {
         SessionId sessionId = ((RemoteWebDriver)driver).getSessionId();
         return (sessionId == null) ? null : sessionId.toString();
     }
 
     @Test
     public void Register_With_Invalid_Email() throws Exception {
 
        driver.get("http://markavip.com");
 	driver.findElement(By.className("do_modal")).click();
 	WebDriverWait wait=new WebDriverWait(driver, 25);
 	wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("fb-login-section")));
 
 	//This to prevent the browser Auto selection for Registration the text-box.
 	wait.until(ExpectedConditions.elementToBeClickable(By.id("register_email")));
 	driver.findElement(By.id("register_email")).click();
 	
 	driver.findElement(By.id("register_email")).sendKeys("icreativeapp@gmail");
 	driver.findElement(By.id("register_gender_male")).click();
 	driver.findElement(By.id("reg-send")).click();
 	wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("fb-login-section")));
 	
 	String invalidemail2="Please enter a valid email address. For example johndoe@domain.com.";
 	Assert.assertEquals(driver.findElement(By.id("advice-validate-email-register_email")).getText(), invalidemail2);
 	System.out.println(driver.findElement(By.id("advice-validate-email-register_email")).getText());
     }
 
     @AfterMethod
     public void tearDown() throws Exception {
         driver.quit();
     }
 
     /**
      * {@inheritDoc}
      * @return
      */
     @Override
     public SauceOnDemandAuthentication getAuthentication() {
         return authentication;
     }
 }
