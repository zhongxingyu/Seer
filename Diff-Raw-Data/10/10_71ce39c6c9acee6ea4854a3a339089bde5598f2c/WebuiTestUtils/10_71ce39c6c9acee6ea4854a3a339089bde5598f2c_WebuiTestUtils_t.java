 package org.cloudifysource.quality.iTests.test.webui;
 
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.ProcessingUnitUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Arrays;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.dsl.utils.IPUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudService;
 import org.cloudifysource.utilitydomain.openspaces.OpenspacesConstants;
 import org.openqa.selenium.By;
 import org.openqa.selenium.Dimension;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.OutputType;
 import org.openqa.selenium.TakesScreenshot;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebDriverBackedSelenium;
 import org.openqa.selenium.WebDriverException;
 import org.openqa.selenium.chrome.ChromeDriverService;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.Assert;
 
import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudService;
import org.cloudifysource.utilitydomain.openspaces.OpenspacesConstants;

 import com.gigaspaces.webuitf.LoginPage;
 import com.gigaspaces.webuitf.WebConstants;
 import com.gigaspaces.webuitf.dashboard.DashboardTab;
 import com.gigaspaces.webuitf.util.AjaxUtils;
 import com.j_spaces.kernel.PlatformVersion;
 import com.j_spaces.kernel.SystemProperties;
 import com.thoughtworks.selenium.Selenium;
 
 /**
  * use this class to add webui testing capabilities to a test.
  * 
  * <p> call close() when done.
  */
 public class WebuiTestUtils{
 
 	private static final String WEBUI_DEFAULT_URL = "http://127.0.0.1:8099";
 	private static final String WEBUI_REVERSE_PROXY_URL = "http://localhost/reverse-proxy-testing/Gs_webui.html";
 
     private WebDriver driver;
 	private Selenium selenium;
 	private String defaultBrowser;
 	private ChromeDriverService chromeService;
 	private AjaxUtils helper;
 	private Admin admin;
 	private CloudService cloud;
 
 	public static String METRICS_ASSERTION_SUFFIX = " metric that is defined in the dsl is not displayed in the metrics panel";    
 
 	public final long WAITING_TIME = 30000;
 
 	/**
 	 * use this constructor when testing local cloud (on localhost).
 	 * Secured by default.
 	 * @throws UnknownHostException
 	 * @throws InterruptedException
 	 */
 	public WebuiTestUtils() throws UnknownHostException, InterruptedException {
 		this(null,null, true);
 	}
 
 	/**
 	 * use this constructor when testing local cloud.
 	 * Secured by default.
 	 * @throws UnknownHostException
 	 * @throws InterruptedException
 	 */
 	public WebuiTestUtils(Admin admin) throws UnknownHostException, InterruptedException {
 		this(null, admin, true);
 	}
 
 	/**
 	 * use this constructor when testing non-local clouds.
 	 * Secured by default.
 	 * @param cloud
 	 * @throws InterruptedException
 	 * @throws UnknownHostException
 	 */
 	public WebuiTestUtils(CloudService cloud) throws InterruptedException, UnknownHostException{
 		this( cloud, null , true);
 	}
 	
 	public WebuiTestUtils(CloudService cloud, Admin admin, boolean isSecured) throws InterruptedException, UnknownHostException{
 		startup(admin, cloud, isSecured);
 	}
 
 	private void startup(Admin admin, CloudService cloud, boolean isSecured) throws UnknownHostException, InterruptedException {
 		this.defaultBrowser = (System.getProperty("selenium.browser") != null) ? System.getProperty("selenium.browser"): "Firefox";
 		this.admin = admin;
 		this.cloud = cloud;
 
 		if(this.cloud == null){
 			setLocators();			
 		}
 
 		String webuiUrl = getWebuiUrl(isSecured);
 
         LogUtils.log("starting web browser with url " + webuiUrl);
         startWebBrowser(webuiUrl);
 	}
 
 	private void setLocators() throws UnknownHostException {
 		InetAddress localHost = InetAddress.getLocalHost();
 		String hostAddress = localHost.getHostAddress();
 		String locatorUrl = IPUtils.getSafeIpAddress(hostAddress) + ":" + String.valueOf(OpenspacesConstants.DEFAULT_LOCALCLOUD_LUS_PORT);
 		System.setProperty( SystemProperties.JINI_LUS_LOCATORS, locatorUrl );
 	}
 
 	public void startWebBrowser(String uRL) throws InterruptedException {
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
 							DesiredCapabilities desired = DesiredCapabilities.chrome();
 							desired.setCapability("chrome.switches", Arrays.asList("--start-maximized"));
 							String chromeDriverExePath = SGTestHelper.getSGTestRootDir() + "/src/main/resources/webui/chromedriver.exe";
 							chromeService = new ChromeDriverService.Builder().usingAnyFreePort().usingDriverExecutable(new File(chromeDriverExePath)).build();
 							LogUtils.log("Starting Chrome Driver Server...");
 							chromeService.start();
 							driver = new RemoteWebDriver(chromeService.getUrl(), desired);
 						}
 					}
 				}
 				break;
 
 			}
 			catch (WebDriverException e) {
 				LogUtils.log("Failed to lanch browser, retyring...Attempt number " + (i + 1));
 			} catch (IOException e) {
 				Assert.fail("Failed to lanch browser", e);
 			}
 		}
 		if (driver == null) {
 			LogUtils.log("unable to lauch browser, test will fail on NPE");
 		}
 		int seconds = 0;
 		if (driver != null) {
 			driver.get(uRL);
 			if ((browser == null) || browser.equals("Firefox")) {
 				maximize(); // this method is supported only on Firefox
 			}
 			selenium = new WebDriverBackedSelenium(driver, uRL);
 			helper = new AjaxUtils(driver);
 			Thread.sleep(3000);
 			while (seconds < 30) {
 				try {
 					helper.waitForElement(By.xpath(WebConstants.Xpath.loginButton), AjaxUtils.ajaxWaitingTime*2);
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
 			if (seconds == 30) {
 				LogUtils.log("Could not establish a connection to webui server, Test will fail");
 			}
 		}
 	}
 
 	private void maximize() {
 		driver.manage().window().setSize(new Dimension(1280, 1024)); 
 	}
 
 	public LoginPage getLoginPage() {
 		return new LoginPage(selenium, driver);
 	}
 
 	public boolean verifyAlertThrown() {
 		return selenium.isElementPresent(WebConstants.Xpath.okAlert);
 	}
 
 	public void close() throws InterruptedException {
 		LogUtils.log("Killing browser...");
 		selenium.stop();
 		selenium = null;
 		Thread.sleep(1000);
 
 		if (chromeService != null && chromeService.isRunning()) {
 			LogUtils.log("Chrome Driver Server is still running, shutting it down...");
 			chromeService.stop();
 			chromeService = null;
 		}
 	}
 
 	public DashboardTab refreshPage() throws InterruptedException {
 		driver.navigate().refresh();
 		Thread.sleep(10000);
 		return new DashboardTab(selenium, driver);
 	}
 
 	public void takeScreenShot(Class<?> cls, String testMethod, String picName) {
 
 		if (!SGTestHelper.isDevMode()) {
 
 			String suiteName = "webui-" + System.getProperty("selenium.browser");
 
 			File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
 
 			String buildDir = SGTestHelper.getSGTestRootDir() + "/deploy/local-builds/build_" + PlatformVersion.getBuildNumber();
 
 			String testLogsDir = cls.getName() + "." + testMethod + "()";
 
 			String to = buildDir + "/" + suiteName + "/" + testLogsDir + "/" + picName + ".png";
 
 			try {
 				FileUtils.copyFile(scrFile, new File(to));
 			} catch (IOException e) {
 				Assert.fail("Failed to take screenshot", e); 
 			}
 		}
 	}
 
 	public void restorePreviousBrowser() {
 		LogUtils.log("restoring browser setting to " + defaultBrowser);
 		setBrowser(defaultBrowser);
 	}
 
 	public void setBrowser(String browser) {
 		System.setProperty("selenium.browser", browser);
 	}
 
 	private String getWebuiUrl(boolean isSecured) {
 
 		String webuiUrl;
 		
 		if(cloud != null){
 			if (cloud.getRestUrls() == null) {
 				Assert.fail("Test requested the Webui URLs for the cloud, but they were not set. This may indeicate that the cloud was not bootstrapped properly");
 			}
 
 			webuiUrl = cloud.getWebuiUrls()[0];
 		}
 		
 		else if(admin != null){
 			ProcessingUnit webui = admin.getProcessingUnits().waitFor("webui");
 			ProcessingUnitUtils.waitForDeploymentStatus(webui, DeploymentStatus.INTACT);
 			AssertUtils.assertTrue(webui != null);
 			AssertUtils.assertTrue(webui.getInstances().length != 0);	
 			webuiUrl = ProcessingUnitUtils.getWebProcessingUnitURL(webui, isSecured).toString();
 		}
 		
 		else{
 
             webuiUrl = WEBUI_DEFAULT_URL;
 
             String isReverseProxy = System.getProperty("reverse.proxy");
             LogUtils.log("isReverseProxy: " + isReverseProxy);
 
             if(isReverseProxy != null && isReverseProxy.equals("true")){
                 webuiUrl = WEBUI_REVERSE_PROXY_URL;
             }
 		}
 		
 		return webuiUrl;		
 	}
 
 }
