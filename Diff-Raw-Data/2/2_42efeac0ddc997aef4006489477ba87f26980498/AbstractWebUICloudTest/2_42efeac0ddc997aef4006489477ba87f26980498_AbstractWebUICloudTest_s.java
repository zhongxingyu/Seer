 package test.webui.cloud;
 
 import java.util.Arrays;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebDriverBackedSelenium;
 import org.openqa.selenium.WebDriverException;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 
 import com.thoughtworks.selenium.Selenium;
 
 import test.cli.cloudify.cloud.AbstractCloudTest;
 import test.cli.cloudify.cloud.CloudService;
 import test.webui.objects.LoginPage;
 import test.webui.resources.WebConstants;
 import framework.tools.SGTestHelper;
 import framework.utils.LogUtils;
 
 public class AbstractWebUICloudTest extends AbstractCloudTest {
 	
 	private WebDriver driver;
 	private Selenium selenium;
 	
 	public void launchWebui() throws InterruptedException {
 		
 		CloudService service = getService();
		String webuiURL = service.getWebuiUrl();
 		startWebBrowser(webuiURL);
 		
 	}
 	
     private void startWebBrowser(String uRL) throws InterruptedException {
     	LogUtils.log("Launching browser...");
     	String browser = System.getProperty("selenium.browser");
     	LogUtils.log("Current browser is " + browser);
     	for (int i = 0 ; i < 3 ; i++) {
     		try {
     			if (browser == null) {
     				driver = new FirefoxDriver();
     			}
     			else {
     				if (browser.equals("Firefox")) {
     					driver = new FirefoxDriver();
 
     				}
     				else {
     					if (browser.equals("IE")) {
     						DesiredCapabilities desired = DesiredCapabilities.internetExplorer();
     						desired.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
     						driver = new InternetExplorerDriver(desired);
     					}
     					else {
     						System.setProperty("webdriver.chrome.driver", SGTestHelper.getSGTestRootDir() + "/src/test/webui/resources/chromedriver.exe");
     						DesiredCapabilities desired = DesiredCapabilities.chrome();
     						desired.setCapability("chrome.switches", Arrays.asList("--start-maximized"));
     						driver = new ChromeDriver(desired);
     					}
     				}
     			}
     			break;
 
     		}
     		catch (WebDriverException e) {
     			LogUtils.log("Failed to lanch browser, retyring...Attempt number " + (i + 1));
     		}
     	}
     	if (driver == null) {
     		LogUtils.log("unable to lauch browser, test will fail on NPE");
     	}
     	int seconds = 0;
     	if (driver != null) {
         	driver.get(uRL);
         	selenium = new WebDriverBackedSelenium(driver, uRL);
         	Thread.sleep(3000);
         	while (seconds < 30) {
         		try {
         			driver.findElement(By.xpath(WebConstants.Xpath.loginButton));
         			LogUtils.log("Web server connection established");
         			break;
         		}
         		catch (NoSuchElementException e) {
         			LogUtils.log("Unable to connect to Web server, retrying...Attempt number " + (seconds + 1));
         			driver.navigate().refresh();
         			Thread.sleep(1000);
         			seconds++;
         		}
         	}
         	if (seconds == 10) {
         		LogUtils.log("Could not establish a connection to webui server, Test will fail");
         	}
     	}
     }
 
 
 	public void shutdownWebui() throws InterruptedException {
 		driver.quit();
 	}
 	
 	public LoginPage getLoginPage() {
 		return new LoginPage(selenium,driver);
 	}
 }
