 package smartpool.functional;
 
 import org.junit.After;
 import org.junit.Before;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import smartpool.util.EnvironmentLoader;
 
 public abstract class BaseTest {
 
     protected FirefoxDriver webDriver;
 
     @Before
     public void setUp() {
         webDriver = new FirefoxDriver();
         webDriver.get(new EnvironmentLoader().getPropertyList(EnvironmentLoader.APPLICATION_PATH, EnvironmentLoader.APPLICATION_URL));
     }
 
     @After
     public void tearDown() {
         webDriver.manage().deleteAllCookies();
         webDriver.quit();
     }
 }
