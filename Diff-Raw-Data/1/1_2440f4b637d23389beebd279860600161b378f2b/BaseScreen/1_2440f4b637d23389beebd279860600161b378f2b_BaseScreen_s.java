 package com.photon.phresco.Screens;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.concurrent.TimeUnit;
 
 
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openqa.selenium.By;
 import org.openqa.selenium.Dimension;
 import org.openqa.selenium.OutputType;
 import org.openqa.selenium.Platform;
 import org.openqa.selenium.TakesScreenshot;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.chrome.ChromeDriverService;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 import org.openqa.selenium.remote.CapabilityType;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.testng.Assert;
 
 import com.google.common.base.Function;
 import com.photon.phresco.selenium.util.Constants;
 import com.photon.phresco.selenium.util.GetCurrentDir;
 import com.photon.phresco.selenium.util.ScreenActionFailedException;
 import com.photon.phresco.selenium.util.ScreenException;
 import com.photon.phresco.uiconstants.PhrescoUiConstants;
 import com.photon.phresco.uiconstants.UIConstants;
 import com.photon.phresco.uiconstants.UserInfoConstants;
 import com.photon.phresco.uiconstants.WidgetData;
 
 public class BaseScreen {
 
 	private WebDriver driver;
 	private ChromeDriverService chromeService;
 	private Log log = LogFactory.getLog("BaseScreen");
 	private WebElement element;
 	private UserInfoConstants userInfoConstants;
 	private UIConstants uiConstants;
 	private WidgetData widgetData;
 	private PhrescoUiConstants phrescoUiConstants;
 	DesiredCapabilities capabilities;
 	
 
 	// private Log log = LogFactory.getLog(getClass());
 
 	public BaseScreen() {
 
 	}
 
 	public BaseScreen(String selectedBrowser,String selectedPlatform, String applicationURL,
 			String applicationContext, UserInfoConstants userInfoConstants,
 			UIConstants uiConstants, WidgetData widgetData,PhrescoUiConstants phrescoUiConstants)
 					throws AWTException, IOException, ScreenActionFailedException {
 
 		this.userInfoConstants = userInfoConstants;
 		this.uiConstants = uiConstants;
 		this.widgetData = widgetData;
 		this.phrescoUiConstants=phrescoUiConstants;
 		try {
 			instantiateBrowser(selectedBrowser,selectedPlatform, applicationURL, applicationContext);
 		} catch (ScreenException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void instantiateBrowser(String selectedBrowser,String selectedPlatform,
 			String applicationURL, String applicationContext)
 					 throws ScreenException,
 						MalformedURLException  {
 
 
 		URL server = new URL("http://localhost:4444/wd/hub/");
 		if (selectedBrowser.equalsIgnoreCase(Constants.BROWSER_CHROME)) {
 			log.info("-------------***LAUNCHING GOOGLECHROME***--------------");
 			try {
 
 				/*
 				 * chromeService = new ChromeDriverService.Builder()
 				 * .usingChromeDriverExecutable( new File(getChromeLocation()))
 				 * .usingAnyFreePort().build(); log.info(
 				 * "-------------***LAUNCHING GOOGLECHROME***--------------");
 				 * chromeService.start();
 				 */
 				capabilities = new DesiredCapabilities();
 				capabilities.setBrowserName("chrome");
 				/*
 				 * break; capabilities.setPlatform(Platform)
 				 * capabilities.setPlatform(selectedPlatform); driver = new
 				 * RemoteWebDriver(server, capabilities);
 				 */
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		} else if (selectedBrowser.equalsIgnoreCase(Constants.BROWSER_IE)) {
 			log.info("---------------***LAUNCHING INTERNET EXPLORE***-----------");
 			try {
 				capabilities = new DesiredCapabilities();
 				capabilities.setJavascriptEnabled(true);
 				capabilities.setBrowserName("iexplorer");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 		}
 			
 			else if (selectedBrowser.equalsIgnoreCase(Constants.BROWSER_OPERA)) {
 				log.info("-------------***LAUNCHING OPERA***--------------");
 				try {
 					
 				capabilities = new DesiredCapabilities();
 				capabilities.setBrowserName("opera");
 				capabilities.setCapability("opera.autostart ",true);
 
 				System.out.println("-----------checking the OPERA-------");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 		
 		} 
 			else if (selectedBrowser.equalsIgnoreCase(Constants.BROWSER_SAFARI)) {
 				log.info("-------------***LAUNCHING SAFARI***--------------");
 				try {
 					
 			    capabilities = new DesiredCapabilities();
 				capabilities.setBrowserName("safari");
 				capabilities.setCapability("safari.autostart ", true);
 				System.out.println("-----------checking the SAFARI-------");
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		
 		} else if (selectedBrowser.equalsIgnoreCase(Constants.BROWSER_FIREFOX)) {
 			log.info("-------------***LAUNCHING FIREFOX***--------------");
 			capabilities = new DesiredCapabilities();
 			capabilities.setBrowserName("firefox");
 			System.out.println("-----------checking the firefox-------");
 			// break;
 			// driver = new RemoteWebDriver(server, capabilities);
 
 		} else {
 			throw new ScreenException(
 					"------Only FireFox,InternetExplore and Chrome works-----------");
 		}
 
 		/**
 		 * These 3 steps common for all the browsers
 		 */
 
 		/* for(int i=0;i<platform.length;i++) */
 
 		if (selectedPlatform.equalsIgnoreCase("WINDOWS")) {
 			capabilities.setCapability(CapabilityType.PLATFORM,
 					Platform.WINDOWS);
 			// break;
 		} else if (selectedPlatform.equalsIgnoreCase("LINUX")) {
 			capabilities.setCapability(CapabilityType.PLATFORM, Platform.LINUX);
 			// break;
 		} else if (selectedPlatform.equalsIgnoreCase("MAC")) {
 			capabilities.setCapability(CapabilityType.PLATFORM, Platform.MAC);
 			// break;
 		}
 		driver = new RemoteWebDriver(server, capabilities);
 		driver.get(applicationURL + applicationContext);
 		// driver.manage().window().maximize();
 		// driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
 
 	}
 	
 	public  void windowResize()
 	{
 	
 	String resolution =phrescoUiConstants.RESOLUTION;
 	if(resolution!=null)
 	{
 			String[] tokens = resolution.split("x");
 			String resolutionX=tokens[0];
 			String resolutionY=tokens[1];
 			int x= Integer.parseInt(resolutionX);
 			int y= Integer.parseInt(resolutionY);
 			Dimension screenResolution = new Dimension(x,y);
 			driver.manage().window().setSize(screenResolution);
 	}
 	else{
 	driver.manage().window().maximize();
 	}
 	}
 
 	/*
 	 * public static void windowMaximizeFirefox() {
 	 * driver.manage().window().setPosition(new Point(0, 0)); java.awt.Dimension
 	 * screenSize = java.awt.Toolkit.getDefaultToolkit() .getScreenSize();
 	 * Dimension dim = new Dimension((int) screenSize.getWidth(), (int)
 	 * screenSize.getHeight()); driver.manage().window().setSize(dim); }
 	 */
 
 	public void closeBrowser() {
 		log.info("-------------***BROWSER CLOSING***--------------");
 		if (driver != null) {
 			driver.quit();
 			if (chromeService != null) {
 
 			}
 		}
 
 	}
 
 	public String getChromeLocation() {
 		log.info("getChromeLocation:*****CHROME TARGET LOCATION FOUND***");
 		String directory = System.getProperty("user.dir");
 		String targetDirectory = getChromeFile();
 		String location = directory + targetDirectory;
 		return location;
 	}
 
 	public String getChromeFile() {
 		if (System.getProperty("os.name").startsWith(Constants.WINDOWS_OS)) {
 			log.info("*******WINDOWS MACHINE FOUND*************");
 			// getChromeLocation("/chromedriver.exe");
 			return Constants.WINDOWS_DIRECTORY + "/chromedriver.exe";
 		} else if (System.getProperty("os.name").startsWith(Constants.LINUX_OS)) {
 			log.info("*******LINUX MACHINE FOUND*************");
 			return Constants.LINUX_DIRECTORY_64 + "/chromedriver";
 		} else if (System.getProperty("os.name").startsWith(Constants.MAC_OS)) {
 			log.info("*******MAC MACHINE FOUND*************");
 			return Constants.MAC_DIRECTORY + "/chromedriver";
 		} else {
 			throw new NullPointerException("******PLATFORM NOT FOUND********");
 		}
 
 	}
 
 	public void getXpathWebElement(String xpath) throws Exception {
 		log.info("Entering:-----getXpathWebElement-------");
 		try {
 
 			element = driver.findElement(By.xpath(xpath));
 
 		} catch (Throwable t) {
 			log.info("Entering:---------Exception in getXpathWebElement()-----------");
 			t.printStackTrace();
 
 		}
 
 	}
 
 	public void getIdWebElement(String id) throws ScreenException {
 		log.info("Entering:---getIdWebElement-----");
 		try {
 			element = driver.findElement(By.id(id));
 
 		} catch (Throwable t) {
 			log.info("Entering:---------Exception in getIdWebElement()----------");
 			t.printStackTrace();
 
 		}
 
 	}
 
 	public void getcssWebElement(String selector) throws ScreenException {
 		log.info("Entering:----------getIdWebElement----------");
 		try {
 			element = driver.findElement(By.cssSelector(selector));
 
 		} catch (Throwable t) {
 			log.info("Entering:---------Exception in getIdWebElement()--------");
 
 			t.printStackTrace();
 
 		}
 
 	}
 
 	public void waitForElementPresent(String locator, String methodName)
 			throws IOException, Exception {
 		try {
 			log.info("Entering:--------waitForElementPresent()--------");
 			By by = By.xpath(locator);
 			WebDriverWait wait = new WebDriverWait(driver, 40);
 			log.info("Waiting:--------Wait for Element----------> "+locator);
 			wait.until(presenceOfElementLocated(by));
 		}
 
 		catch (Exception e) {
 			/*File scrFile = ((TakesScreenshot) driver)
 					.getScreenshotAs(OutputType.FILE);
 			FileUtils.copyFile(scrFile,
 					new File(GetCurrentDir.getCurrentDirectory() + "\\"
 							+ methodName + ".png"));
 			throw new RuntimeException("waitForElementPresent"
 					+ super.getClass().getSimpleName() + " failed", e);*/
 			Assert.assertNull(e);
 			
 		}
 	}
 
 	Function<WebDriver, WebElement> presenceOfElementLocated(final By locator) {
 		log.info("Entering:------presenceOfElementLocated()-----Start");
 		return new Function<WebDriver, WebElement>() {
 			public WebElement apply(WebDriver driver) {
 				return driver.findElement(locator);
 
 			}
 
 		};
 
 	}
 
 	public void clickOnBrowse(String methodName) throws IOException, Exception {
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(uiConstants.BROWSE, methodName);
 		getXpathWebElement(uiConstants.BROWSE);
 		element.click();
 
 	}
 	public void clickOnBrowseTab(String methodName) throws IOException, Exception {
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(uiConstants.BROWSE_TAB, methodName);
 		getXpathWebElement(uiConstants.BROWSE_TAB);
 		element.click();
 
 	}
 
 	public void Television(String methodName) throws Exception {
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(uiConstants.TELEVISION, methodName);
 		getXpathWebElement(uiConstants.TELEVISION);
 		element.click();
 		waitForElementPresent(uiConstants.TELE_PROD1_DETAILS, methodName);
 		getXpathWebElement(uiConstants.TELE_PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(uiConstants.ADDTOCART, methodName);
 
 	}
 
 	public void Computers(String methodName) throws Exception {
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(uiConstants.COMPUTERS, methodName);
 		getXpathWebElement(uiConstants.COMPUTERS);
 		element.click();
 		waitForElementPresent(uiConstants.COMP_PROD1_DETAILS, methodName);
 		getXpathWebElement(uiConstants.COMP_PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(uiConstants.ADDTOCART, methodName);
 
 	}
 
 	public void MobilePhones(String methodName) throws Exception {
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(uiConstants.MOBILE, methodName);
 		getXpathWebElement(uiConstants.MOBILE);
 		element.click();
 		waitForElementPresent(uiConstants.MOBILE_PROD1_DETAILS, methodName);
 		getXpathWebElement(uiConstants.MOBILE_PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(uiConstants.ADDTOCART, methodName);
 
 	}
 
 	public void AudioDevices(String methodName) throws Exception {
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		System.out
 				.println("-----------------*********--------------------------");
 		waitForElementPresent(uiConstants.AUDIO_DEVICES, methodName);
 		getXpathWebElement(uiConstants.AUDIO_DEVICES);
 		element.click();
 		waitForElementPresent(uiConstants.AUDIO_PROD1_DETAILS, methodName);
 		getXpathWebElement(uiConstants.AUDIO_PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(uiConstants.ADDTOCART, methodName);
 
 	}
 
 	public void Cameras(String methodName) throws Exception {
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(uiConstants.CAMERAS, methodName);
 		getXpathWebElement(uiConstants.CAMERAS);
 		element.click();
 		waitForElementPresent(uiConstants.CAMERAS_PROD1_DETAILS, methodName);
 		getXpathWebElement(uiConstants.CAMERAS_PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(uiConstants.ADDTOCART, methodName);
 
 	}
 
 	public void Tablets(String methodName) throws Exception {
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(uiConstants.TABLETS, methodName);
 		getXpathWebElement(uiConstants.TABLETS);
 		element.click();
 		waitForElementPresent(uiConstants.TABLETS_PROD1_DETAILS, methodName);
 		getXpathWebElement(uiConstants.TABLETS_PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(uiConstants.ADDTOCART, methodName);
 
 	}
 
 	public void MoviesnMusic(String methodName) throws Exception {
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(uiConstants.MOVIESnMUSIC, methodName);
 		getXpathWebElement(uiConstants.MOVIESnMUSIC);
 		element.click();
 		waitForElementPresent(uiConstants.MnM_PROD1_DETAILS, methodName);
 		getXpathWebElement(uiConstants.MnM_PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(uiConstants.ADDTOCART, methodName);
 
 	}
 
 	public void VideoGames(String methodName) throws Exception {
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(uiConstants.VIDEOGAMES, methodName);
 		getXpathWebElement(uiConstants.VIDEOGAMES);
 		element.click();
 		waitForElementPresent(uiConstants.VIDGAMES_PROD1_DETAILS, methodName);
 		getXpathWebElement(uiConstants.VIDGAMES_PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(uiConstants.ADDTOCART, methodName);
 
 	}
 
 	public void MP3Players(String methodName) throws Exception {
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(uiConstants.MP3PLAYERS, methodName);
 		getXpathWebElement(uiConstants.MP3PLAYERS);
 		element.click();
 		waitForElementPresent(uiConstants.MP3_PROD1_DETAILS, methodName);
 		getXpathWebElement(uiConstants.MP3_PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(uiConstants.ADDTOCART, methodName);
 
 	}
 
 	public void Accessories(String methodName) throws Exception {
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(uiConstants.ACCESSORIES, methodName);
 		getXpathWebElement(uiConstants.ACCESSORIES);
 		element.click();
 		waitForElementPresent(uiConstants.ACC_PROD1_DETAILS, methodName);
 		getXpathWebElement(uiConstants.ACC_PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(uiConstants.ADDTOCART, methodName);
 
 	}
 
 	public void BillingInfo(String methodName) throws Exception {
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(uiConstants.ADDTOCART, methodName);
 		getXpathWebElement(uiConstants.ADDTOCART);
 		element.click();
 		/*waitForElementPresent(uiConstants.UPDATECART, methodName);
 		getXpathWebElement(uiConstants.UPDATECART);
 		element.click();*/
 		waitForElementPresent(uiConstants.CHECKOUT, methodName);
 		getXpathWebElement(uiConstants.CHECKOUT);
 		element.click();
 		waitForElementPresent(uiConstants.CUSTOMERINFORMATION, methodName);
 		getXpathWebElement(uiConstants.CUSTOMERINFORMATION);
 		element.click();
 		Thread.sleep(2000);
 	//	waitForElementPresent(uiConstants.EMAIL, methodName);
 		getIdWebElement(uiConstants.EMAIL);
 		element.click();
 		element.clear();		
 		element.sendKeys(this.widgetData.EMAIL_VALUE);
 		waitForElementPresent(uiConstants.DELIVERYINFO, methodName);
 		getXpathWebElement(uiConstants.DELIVERYINFO);
 		element.click();
 		getIdWebElement(uiConstants.FIRSTNAME);
 		element.sendKeys(this.widgetData.FIRSTNAME_VALUE);
 		getIdWebElement(uiConstants.LASTNAME);
 		element.sendKeys(this.widgetData.LASTNAME_VALUE);
 		getIdWebElement(uiConstants.COMPANY);
 		element.sendKeys(this.widgetData.COMPANY_VALUE);
 		getIdWebElement(uiConstants.ADDRESS1);
 		element.sendKeys(this.widgetData.ADDRESS1_VALUE);
 		getIdWebElement(uiConstants.ADDRESS2);
 		element.sendKeys(this.widgetData.ADDRESS2_VALUE);
 		getIdWebElement(uiConstants.CITY);
 		element.sendKeys(this.widgetData.CITY_VALUE);
 		getIdWebElement(uiConstants.STATE);
 		element.sendKeys(this.widgetData.STATE_VALUE);
 		getIdWebElement(uiConstants.POSTCODE);
 		element.sendKeys(this.widgetData.POSTALCODE_VALUE);
 		getIdWebElement(uiConstants.PHONENUMBER);
 		element.sendKeys(this.widgetData.PHONENUMBER_VALUE);
 		waitForElementPresent(uiConstants.BILLINGINFO, methodName);
 		getXpathWebElement(uiConstants.BILLINGINFO);
 		element.click();
 		Thread.sleep(5000);
 		waitForElementPresent(uiConstants.CHECKADDRESS, methodName);
 		getXpathWebElement(uiConstants.CHECKADDRESS);
 		element.click();
 		waitForElementPresent(uiConstants.PAYMENTMETHODS, methodName);
 		getXpathWebElement(uiConstants.PAYMENTMETHODS);
 		element.click();
 		/*waitForElementPresent(uiConstants.CASHONDELIVERY, methodName);
 		getXpathWebElement(uiConstants.CASHONDELIVERY);
 		element.click();*/
 		waitForElementPresent(uiConstants.ORDERCOMMENTS, methodName);
 		getXpathWebElement(uiConstants.ORDERCOMMENTS);
 		element.click();
 		getIdWebElement(uiConstants.GIVECOMMENTS);
 		element.sendKeys(this.widgetData.ORDERCOMMENTS_VALUE);
 		waitForElementPresent(uiConstants.REVIEWORDER, methodName);
 		getXpathWebElement(uiConstants.REVIEWORDER);
 		element.click();
 		waitForElementPresent(uiConstants.SUBMITORDER, methodName);
 		getXpathWebElement(uiConstants.SUBMITORDER);
 		Thread.sleep(2000);
 		element.click();
 	}
 
 	public void click() throws ScreenException {
 		log.info("Entering:********click operation start********");
 		try {
 			element.click();
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 		log.info("Entering:********click operation end********");
 
 	}
 
 	public void clear() throws ScreenException {
 		log.info("Entering:********clear operation start********");
 		try {
 			element.clear();
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 		log.info("Entering:********clear operation end********");
 
 	}
 	public void isTextPresent(String text) {
 		if (text!= null){
 		boolean value=driver.findElement(By.tagName("body")).getText().contains(text);	
 		Assert.assertTrue(value);   
 	    
 	    }
 		else
 		{
 			throw new RuntimeException("---- Text not existed----");
 		}
 	    
 	}	
 
 	public void sendKeys(String text) throws ScreenException {
 		log.info("Entering:********enterText operation start********");
 		try {
 			clear();
 			element.sendKeys(text);
 		} catch (Throwable t) {
 			t.printStackTrace();		}
 		log.info("Entering:********enterText operation end********");
 	}
 
 	public void submit() throws ScreenException {
 		log.info("Entering:********submit operation start********");
 		try {
 			element.submit();
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 		log.info("Entering:********submit operation end********");
 
 	}
 
 }
