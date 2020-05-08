 package testingbot;
 
 import java.net.URL;
 
 import org.junit.After;
 import org.junit.Before;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.Platform;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 
 import test_util.Base;
 import test_util.Key;
 
 public class BotTest extends Base {
 	/**
 	 * Run latest Chrome on Mac. https://testingbot.com/members
 	 * 
 	 * TODO: Report test results to testingbot.
 	 * http://testingbot.com/support/getting-started/java.html
 	 **/
 	@Before
 	public void setUp() throws Exception {
 		final DesiredCapabilities capabillities = DesiredCapabilities.chrome();
 		capabillities.setCapability("platform", Platform.MAC);
		capabillities.setCapability("name", "Linux Chrome");
 		capabillities.setCapability("screenshot", "true");
 		capabillities.setCapability("screenrecorder", "true");
 
 		driver = new RemoteWebDriver(new URL("http://"
 				+ Key.readFile("testingbot.txt")
 				+ "@hub.testingbot.com:4444/wd/hub"), capabillities);
 		exec = (JavascriptExecutor) driver;
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		driver.quit();
 	}
 }
