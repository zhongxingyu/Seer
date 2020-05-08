 package test;
 
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
 import java.util.Iterator;
 import java.util.Set;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.interactions.Actions;
 import org.openqa.selenium.remote.CapabilityType;
 import java.util.Date;
 
 /**
  *
  * @author Rami Saleem
  */
 @Listeners({SauceOnDemandTestListener.class})
 public class LoginUsingFacebookAccount implements SauceOnDemandSessionIdProvider, SauceOnDemandAuthenticationProvider {
 
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
     public void Login_Using_Facebook_Account() throws Exception {
 
 driver.get("http://markavip.com");
	driver.findElement(By.className("do_modal")).click();
 	
 	//Save Current Window Name
 	String markavipwindow= driver.getWindowHandle();
 	
 	WebDriverWait wait=new WebDriverWait(driver, 25);
 	wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("login-form")));	
 	wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div/div[2]/div[4]/div[1]/a/span[1]")));
 	driver.findElement(By.xpath("/html/body/div[2]/div/div[2]/div[4]/div[1]/a/span[1]")).click();
 	
 	//Save all the pages and popup
 	Set<String> allpages=driver.getWindowHandles();
 	Iterator<String> findit=allpages.iterator();
 	
 	//Select Facebook popup
 	while(findit.hasNext())
 	
 	{
 		String facebookpopup=findit.next().toString();
 		
 		if(!facebookpopup.contains(markavipwindow))
 			
 		{
 			driver.switchTo().window(facebookpopup);
 		}
 		
 	}
 	
 
 	driver.findElement(By.id("email")).sendKeys("ramisaleem17@gmail.com");
 	driver.findElement(By.id("pass")).sendKeys("Rami2017");
 	driver.findElement(By.id("u_0_1")).click();
 	
 	//Back to MarkaVIP page
 	
 	driver.switchTo().window(markavipwindow);
 
 	/*driver.findElement(By.id("username_link")).getText();
 	String name2="Rami Saleem";
 	Assert.assertEquals(driver.findElement(By.id("username_link")).getText(), name2);*/
 		
 
 		
 
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
