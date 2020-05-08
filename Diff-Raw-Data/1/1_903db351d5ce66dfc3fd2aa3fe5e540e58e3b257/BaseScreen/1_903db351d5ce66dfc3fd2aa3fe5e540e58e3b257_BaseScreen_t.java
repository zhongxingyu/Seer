 package com.photon.phresco.Screens;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Assert;
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
 
 import com.google.common.base.Function;
 import com.photon.phresco.selenium.util.Constants;
 import com.photon.phresco.selenium.util.GetCurrentDir;
 import com.photon.phresco.selenium.util.ScreenActionFailedException;
 import com.photon.phresco.selenium.util.ScreenException;
 import com.photon.phresco.uiconstants.JQueryWidgetData;
 import com.photon.phresco.uiconstants.PhrescoUiConstants;
 import com.photon.phresco.uiconstants.UIConstants;
 
 
 
 
 public class BaseScreen {
 
 	private  WebDriver driver;
 	private ChromeDriverService chromeService;
 	private Log log = LogFactory.getLog("BaseScreen");
 	private WebElement element;	
 	private JQueryWidgetData jQueryWidgetData;
 	private UIConstants uiConstants;
 	private  PhrescoUiConstants phrsc;
 	DesiredCapabilities capabilities;
 
 	// private Log log = LogFactory.getLog(getClass());
 
 	public BaseScreen() {
 
 	}
 
 	public BaseScreen(String selectedBrowser,String selectedPlatform, String applicationURL,
 			String applicationContext, 
 			JQueryWidgetData jQueryWidgetData, UIConstants uiConstants)
 					throws AWTException, IOException, ScreenActionFailedException {
 	
 		this.jQueryWidgetData = jQueryWidgetData;
 		this.uiConstants = uiConstants;
 		try {
 			instantiateBrowser(selectedBrowser, selectedPlatform, applicationURL, applicationContext);
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
 		
 			// break;
 			// capabilities.setPlatform(selectedPlatform);
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
		windowResize();
 		driver.get(applicationURL + applicationContext);
 		 //driver.manage().window().maximize();
 		// driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
 
 	}
 	/*
 	 * public static void windowMaximizeFirefox() {
 	 * driver.manage().window().setPosition(new Point(0, 0)); java.awt.Dimension
 	 * screenSize = java.awt.Toolkit.getDefaultToolkit() .getScreenSize();
 	 * Dimension dim = new Dimension((int) screenSize.getWidth(), (int)
 	 * screenSize.getHeight()); driver.manage().window().setSize(dim); }
 	 */
 	
 	public  void windowResize()
 	{
 		phrsc = new PhrescoUiConstants();
 		String resolution = phrsc.RESOLUTION;		
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
 	
 	public void closeBrowser() {
 		log.info("-------------***BROWSER CLOSING***--------------");
 		if (driver != null) {
 			driver.quit();
 			if (chromeService != null) {				
 				chromeService.stop();
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
 			WebDriverWait wait = new WebDriverWait(driver, 15);
 			log.info("Waiting:--------One second----------");
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
 				log.info("Entering:*********presenceOfElementLocated()******End");
 				return driver.findElement(locator);
 
 			}
 
 		};
 
 	}
 
 	public void Registration(String methodName)throws Exception {
     	if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1].getMethodName();;
 		}
     	
     	waitForElementPresent(uiConstants.SIGNUP_LINK,methodName);
     	getXpathWebElement(uiConstants.SIGNUP_LINK);
     	element.click();
   
     	waitForElementPresent(uiConstants.REG_FIRSTNAME,methodName);
     	getXpathWebElement(uiConstants.REG_FIRSTNAME);
     	element.sendKeys(this.jQueryWidgetData.REG_FIRSTNAME_VALUE);
     	
     	waitForElementPresent(uiConstants.REG_LASTNAME,methodName);
     	getXpathWebElement(uiConstants.REG_LASTNAME);
     	element.sendKeys(this.jQueryWidgetData.REG_LASTNAME_VALUE);
     
     	waitForElementPresent(uiConstants.REG_EMAIL,methodName);
     	getXpathWebElement(uiConstants.REG_EMAIL);
     	element.sendKeys(this.jQueryWidgetData.REG_EMAIL_VALUE);
     	
     	waitForElementPresent(uiConstants.REG_PASSWORD,methodName);
     	getXpathWebElement(uiConstants.REG_PASSWORD);
     	element.sendKeys(this.jQueryWidgetData.REG_PASSWORD_VALUE);
     	
     	
     	waitForElementPresent(uiConstants.REG_PHONENUMBER,methodName);
     	getXpathWebElement(uiConstants.REG_PHONENUMBER);
     	element.sendKeys(this.jQueryWidgetData.REG_PHONENUMBER);
     
     	
     	waitForElementPresent(uiConstants.REG_SUBMIT_BUTTON,methodName);
     	getXpathWebElement(uiConstants.REG_SUBMIT_BUTTON);
     	element.click();
     	Thread.sleep(2000);
     	isTextPresent(jQueryWidgetData.REG_SUCCESS_MSG);
     	
 }
     
 	
 	public void billingInfo(String methodName)
 			throws Exception {
 
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(this.uiConstants.EMAIL, methodName);
 		getXpathWebElement(this.uiConstants.EMAIL);
 		System.out.println("----element ---------->1" + element);		
 		
 		sendKeys(jQueryWidgetData.EMAIL_VALUE);
 		getIdWebElement(this.uiConstants.FIRSTNAME);
 		System.out.println("----element-------------> 2" + element);
 		sendKeys(jQueryWidgetData.FIRSTNAME_VALUE);
 		getIdWebElement(this.uiConstants.LASTNAME);
 		sendKeys(jQueryWidgetData.LASTNAME_VALUE);
 		getIdWebElement(this.uiConstants.COMPANY);
 		sendKeys(this.uiConstants.COMPANY);
 		getIdWebElement(this.uiConstants.ADDRESS1);
 		sendKeys(jQueryWidgetData.ADDRESS1_VALUE);
 		getIdWebElement(this.uiConstants.ADDRESS2);
 		sendKeys(jQueryWidgetData.ADDRESS2_VALUE);
 		getIdWebElement(this.uiConstants.CITY);
 		sendKeys(jQueryWidgetData.CITY_VALUE);
 		getIdWebElement(this.uiConstants.STATE);
 		sendKeys(jQueryWidgetData.STATE_VALUE);
 		getIdWebElement(this.uiConstants.POSTALCODE);
 		sendKeys(jQueryWidgetData.POSTALCODE_VALUE);
 		getIdWebElement(this.uiConstants.PHONENUMBER);
 		sendKeys(jQueryWidgetData.PHONENUMBER_VALUE);
 		getIdWebElement(this.uiConstants.CARDNUMBER);
 		sendKeys(jQueryWidgetData.CARDNUMBER_VALUE);
 		getIdWebElement(this.uiConstants.SECURITYNUMBER);
 		sendKeys(jQueryWidgetData.SECURITYNUMBER_VALUE);
 		getIdWebElement(this.uiConstants.NAMEONCARD);
 		sendKeys(jQueryWidgetData.NAMEONCARD_VALUE);
 		waitForElementPresent(this.uiConstants.REVIEWORDER, methodName);
 		getXpathWebElement(this.uiConstants.REVIEWORDER);
 		click();
 		waitForElementPresent(this.uiConstants.SUBMITORDER, methodName);
 		getXpathWebElement(this.uiConstants.SUBMITORDER);
 		click();
 
 	}
 
 	public void Television(String methodName) throws Exception {
 
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		log.info("Entering :***************Television()***********Start:");
 		waitForElementPresent(this.uiConstants.TELEVISION, methodName);
 		getXpathWebElement(this.uiConstants.TELEVISION);
 		click();
 		waitForElementPresent(this.uiConstants.PROD1_DETAILS, methodName);
 		getXpathWebElement(this.uiConstants.PROD1_DETAILS);
 		click();
 		waitForElementPresent(this.uiConstants.DET_ADDTOCART, methodName);
 		getXpathWebElement(this.uiConstants.DET_ADDTOCART);
 		click();
 		waitForElementPresent(this.uiConstants.CHECKOUT, methodName);
 		getXpathWebElement(this.uiConstants.CHECKOUT);
 		click();
 	}
 
 	public void Computers(String methodName) throws Exception {
 
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(this.uiConstants.COMPUTERS, methodName);
 		getXpathWebElement(this.uiConstants.COMPUTERS);
 		click();
 		waitForElementPresent(this.uiConstants.PROD1_DETAILS, methodName);
 		getXpathWebElement(this.uiConstants.PROD1_DETAILS);
 		click();
 		waitForElementPresent(this.uiConstants.DET_ADDTOCART, methodName);
 		getXpathWebElement(this.uiConstants.DET_ADDTOCART);
 		click();
 		waitForElementPresent(this.uiConstants.CHECKOUT, methodName);
 		getXpathWebElement(this.uiConstants.CHECKOUT);
 		click();
 
 	}
 
 	public void MobilePhones(String methodName) throws Exception {
 
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(this.uiConstants.MOBILE, methodName);
 		getXpathWebElement(this.uiConstants.MOBILE);
 		element.click();
 		waitForElementPresent(this.uiConstants.PROD1_DETAILS, methodName);
 		getXpathWebElement(this.uiConstants.PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(this.uiConstants.DET_ADDTOCART, methodName);
 		getXpathWebElement(this.uiConstants.DET_ADDTOCART);
 		element.click();
 		waitForElementPresent(this.uiConstants.CHECKOUT, methodName);
 		getXpathWebElement(this.uiConstants.CHECKOUT);
 		element.click();
 
 	}
 
 	public void AudioDevices(String methodName) throws Exception {
 
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(this.uiConstants.AUDIO_DEVICES, methodName);
 		getXpathWebElement(this.uiConstants.AUDIO_DEVICES);
 		element.click();
 		waitForElementPresent(this.uiConstants.PROD1_DETAILS, methodName);
 		getXpathWebElement(this.uiConstants.PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(this.uiConstants.DET_ADDTOCART, methodName);
 		getXpathWebElement(this.uiConstants.DET_ADDTOCART);
 		element.click();
 		waitForElementPresent(this.uiConstants.CHECKOUT, methodName);
 		getXpathWebElement(this.uiConstants.CHECKOUT);
 		element.click();
 
 	}
 
 	public void Cameras(String methodName) throws Exception {
 
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(this.uiConstants.CAMERAS, methodName);
 		getXpathWebElement(this.uiConstants.CAMERAS);
 		element.click();
 		waitForElementPresent(this.uiConstants.PROD1_DETAILS, methodName);
 		getXpathWebElement(this.uiConstants.PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(this.uiConstants.DET_ADDTOCART, methodName);
 		getXpathWebElement(this.uiConstants.DET_ADDTOCART);
 		element.click();
 		waitForElementPresent(this.uiConstants.CHECKOUT, methodName);
 		getXpathWebElement(this.uiConstants.CHECKOUT);
 		element.click();
 
 	}
 
 	public void Tablets(String methodName) throws Exception {
 
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(this.uiConstants.TABLETS, methodName);
 		getXpathWebElement(this.uiConstants.TABLETS);
 		element.click();
 		waitForElementPresent(this.uiConstants.PROD1_DETAILS, methodName);
 		getXpathWebElement(this.uiConstants.PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(this.uiConstants.DET_ADDTOCART, methodName);
 		getXpathWebElement(this.uiConstants.DET_ADDTOCART);
 		element.click();
 		waitForElementPresent(this.uiConstants.CHECKOUT, methodName);
 		getXpathWebElement(this.uiConstants.CHECKOUT);
 		element.click();
 
 	}
 
 	public void MoviesnMusic(String methodName) throws Exception {
 
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(this.uiConstants.MOVIESnMUSIC, methodName);
 		getXpathWebElement(this.uiConstants.MOVIESnMUSIC);
 		element.click();
 		waitForElementPresent(this.uiConstants.PROD1_DETAILS, methodName);
 		getXpathWebElement(this.uiConstants.PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(this.uiConstants.DET_ADDTOCART, methodName);
 		getXpathWebElement(this.uiConstants.DET_ADDTOCART);
 		element.click();
 		waitForElementPresent(this.uiConstants.CHECKOUT, methodName);
 		getXpathWebElement(this.uiConstants.CHECKOUT);
 		element.click();
 
 	}
 
 	public void VideoGames(String methodName) throws Exception {
 
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(this.uiConstants.VIDEOGAMES, methodName);
 		getXpathWebElement(this.uiConstants.VIDEOGAMES);
 		element.click();
 		waitForElementPresent(this.uiConstants.PROD1_DETAILS, methodName);
 		getXpathWebElement(this.uiConstants.PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(this.uiConstants.DET_ADDTOCART, methodName);
 		getXpathWebElement(this.uiConstants.DET_ADDTOCART);
 		element.click();
 		waitForElementPresent(this.uiConstants.CHECKOUT, methodName);
 		getXpathWebElement(this.uiConstants.CHECKOUT);
 		element.click();
 	}
 
 	public void MP3Players(String methodName) throws Exception {
 
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		waitForElementPresent(this.uiConstants.MP3PLAYERS, methodName);
 		getXpathWebElement(this.uiConstants.MP3PLAYERS);
 		element.click();
 		waitForElementPresent(this.uiConstants.PROD1_DETAILS, methodName);
 		getXpathWebElement(this.uiConstants.PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(this.uiConstants.DET_ADDTOCART, methodName);
 		getXpathWebElement(this.uiConstants.DET_ADDTOCART);
 		element.click();
 		waitForElementPresent(this.uiConstants.CHECKOUT, methodName);
 		getXpathWebElement(this.uiConstants.CHECKOUT);
 		element.click();
 	}
 
 	public void Accessories(String methodName) throws Exception {
 
 		if (StringUtils.isEmpty(methodName)) {
 			methodName = Thread.currentThread().getStackTrace()[1]
 					.getMethodName();
 			;
 		}
 		/*waitForElementPresent(this.uiConstants.MORE, methodName);
 		getXpathWebElement(this.uiConstants.MORE);
 		element.click();*/
 		waitForElementPresent(this.uiConstants.ACCESSORIES, methodName);
 		getXpathWebElement(this.uiConstants.ACCESSORIES);
 		element.click();
 		waitForElementPresent(this.uiConstants.PROD1_DETAILS, methodName);
 		getXpathWebElement(this.uiConstants.PROD1_DETAILS);
 		element.click();
 		waitForElementPresent(this.uiConstants.DET_ADDTOCART, methodName);
 		getXpathWebElement(this.uiConstants.DET_ADDTOCART);
 		element.click();
 		waitForElementPresent(this.uiConstants.CHECKOUT, methodName);
 		getXpathWebElement(this.uiConstants.CHECKOUT);
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
 			t.printStackTrace();
 		}
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
