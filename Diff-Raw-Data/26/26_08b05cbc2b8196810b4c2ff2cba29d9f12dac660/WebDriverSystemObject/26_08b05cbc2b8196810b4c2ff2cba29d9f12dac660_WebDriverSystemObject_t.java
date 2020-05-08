 package org.jsystem.webdriver_so;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLDecoder;
 
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import jsystem.framework.JSystemProperties;
 import jsystem.framework.report.Reporter;
 import jsystem.framework.system.SystemObjectImpl;
 import jsystem.runner.loader.ClassPathBuilder;
 import jsystem.runner.loader.ExtendsTestCaseClassLoader;
 import jsystem.runner.loader.LoadersManager;
 import jsystem.utils.FileUtils;
 import jsystem.utils.StringUtils;
 
 import org.jsystem.webdriver_so.CurrentPageKeeper.AbstractPageObjectResolver;
 import org.jsystem.webdriver_so.eventlistener.WebDriverReportEventHandler;
 import org.jsystem.webdriver_so.eventlistener.WebDriverScreenshotEventHandler;
 import org.jsystem.webdriver_so.generators.AndroidWebDriverGenerator;
 import org.jsystem.webdriver_so.generators.ChromeWebDriverGenerator;
 import org.jsystem.webdriver_so.generators.FirefoxWebDriverGenerator;
 import org.jsystem.webdriver_so.generators.HtmlUnitWebDriverGenerator;
 import org.jsystem.webdriver_so.generators.InternetExplorerWebDriverGenerator;
 import org.jsystem.webdriver_so.generators.OperaWebDriverGenerator;
 import org.jsystem.webdriver_so.generators.SafariWebDriverGenerator;
 import org.jsystem.webdriver_so.generators.WebDriverConfiguration;
 import org.jsystem.webdriver_so.generators.WebDriverConfigurationImpl;
 import org.jsystem.webdriver_so.generators.WebDriverGenerator;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.android.AndroidDriver;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.chrome.ChromeOptions;
 import org.openqa.selenium.firefox.FirefoxBinary;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.firefox.FirefoxProfile;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 import org.openqa.selenium.iphone.IPhoneDriver;
 import org.openqa.selenium.remote.CapabilityType;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.safari.SafariDriver;
 import org.openqa.selenium.support.events.WebDriverEventListener;
 
 //import com.tools.CurrentPageKeeper;
 
 /**
  * This class implements the Selenium system object. This class is responsible
  * for communication between the client and the remote control of the Selenium
  * platform. copy to SUT : com.jsystem.webdriver.WebDriverSystemObject
  * 
  * @author Liel Ran ,Create Date - 22.12.11
  */
 public class WebDriverSystemObject extends SystemObjectImpl implements HasWebDriver {
 
 	// can be init from the SUT file.
 	// the path should be full path with "chromedriver.exe" at the end of the
 	// string
 	protected String chromeDriverPath = null;
 
 	/**
 	 * the type of the browser to be open. e.g. IE/Chrome//FF
 	 */
 	protected WebDriverType webDriver;
 	/**
 	 * in case of true will not open the browser when init the system object.
 	 */
 	protected boolean lazyInit = false;
 
 	protected WebDriverWrapper driver;
 	protected final String CHROME_DRIVER_EXE_NAME = "chromedriver.exe";
 	protected String browserPath;// browser path ,e.g.// c:/...../Firefox.exe
 	protected String chromeFlags = null;
 	protected boolean windowMaximize = true;
 	protected String chromeProfile = null;
 	protected String chromeExtension = null;
 	protected String firefoxExtension = null;
 	protected String firefoxProfile = null;
 	protected String screenShotPath;
 	protected String screenShotFolderName = "Screenshots";
 	protected String domain = "";
 	protected String seleniumTimeOut = "30000";
 	protected boolean ignoreCertificateErrors = false;
 
 	/**
 	 * User and system web driver event handler classes Init from the SUT -
 	 * classes that will be loaded by Reflection API (class loader) this String
 	 * should contains the full class name of the class implementation if there
 	 * is more than one class the string should be this ':' char as separator
 	 * (like reporter in jsystem.propreties)
 	 * 
 	 * 
 	 * @see WebDriverScreenshotEventHandler
 	 * @see WebDriverReportEventHandler
 	 * @see WebDriverEventListener
 	 */
 	protected String webDriverEventListenerClasses = "";
 
 	protected WebDriverScreenshotEventHandler screenshot;
 
 	private boolean browserIsOpened = false;
 
 	private boolean clearCookiesBeforeOpen = false;
 
 	protected Map<String /* driver type */ , WebDriverGenerator> generators;
 	public WebDriverConfigurationImpl configuration;
 
 	public void init() throws Exception {
 		super.init();
		generators = new HashMap<String, WebDriverGenerator>();
 		generators.put(WebDriverType.FIREFOX_DRIVER.getBorwserType(), new FirefoxWebDriverGenerator());
 		
 		InternetExplorerWebDriverGenerator internetExplorerGenerator = new InternetExplorerWebDriverGenerator();
 		generators.put("ie" , internetExplorerGenerator);
 		generators.put("internetexplorer" , internetExplorerGenerator);
 		generators.put(WebDriverType.INTERNET_EXPLORER_DRIVER.getBorwserType(), internetExplorerGenerator);
 		
 		generators.put(WebDriverType.CHROME_DRIVER.getBorwserType(), new ChromeWebDriverGenerator());		
 		generators.put(WebDriverType.ANDROID_DRIVER.getBorwserType(), new AndroidWebDriverGenerator());
 		generators.put(WebDriverType.HTML_UNIT_DRIVER.getBorwserType(), new HtmlUnitWebDriverGenerator());
 		generators.put(WebDriverType.SAFARI_DRIVER.getBorwserType(), new SafariWebDriverGenerator());
 		generators.put(WebDriverType.OPERA_DRIVER.getBorwserType(), new OperaWebDriverGenerator());
 		
 		if (lazyInit == false) {
 			openBrowser();
 		}
 
 	}
 	
 	
 	protected WebDriverWrapper webDriverFactory(String type) throws Exception {
 		WebDriverGenerator generator = generators.get(type);
		if (generator == null){
			throw new Exception("WebDriver type " + type + " is either unrecognized or not supported");
		}
 		return new WebDriverWrapper(generator.getWebDriver(configuration));
 	}
 	
 
 	/**
 	 * Open the browser:
 	 * <ol>
 	 * <li>creates Selenium client using the SUT parameters</li>
 	 * <li>Sets up it's timeout</li>
 	 * <li>Loads the page (set by {@link #setDomain(String)})</li>
 	 * <li>Maximizes the browser window</li>
 	 * <li>If the changeMousePosition set to true - moves the mouse to the
 	 * bottom left position</li>
 	 * 
 	 * <ol>
 	 * 
 	 * @throws Exception
 	 *             when the SUT doesn't configures the driver type
 	 */
 	public void openBrowser() {
 
 		if (!browserIsOpened) {
 
 			WebDriverWrapper driverWrapper = getBrowserInstance();
 			browserIsOpened = true;
 			initWebDriverEventListener(driverWrapper);
 
 			if (seleniumTimeOut != null && webDriver != null) {
 				setImplicitlyWait(driverWrapper, seleniumTimeOut);
 			}
 
 			driver = driverWrapper;
 
 			if (isWindowMaximize()) {
 				driver.manage().window().maximize();
 			}
 			if (isClearCookiesBeforeOpen()) {
 				driver.manage().deleteAllCookies();
 			}
 			driver.get(getDomain());
 
 		}
 	}
 
 	/**
 	 * Restarts the browser:
 	 * <ol>
 	 * <li>
 	 * Closes the browser window, but not removing the profile.</li>
 	 * <li>Open the browser again</li>
 	 * 
 	 * @see #openBrowser
 	 */
 	public void restartBrwoser() {
 		closeBrowser();
 		openBrowser();
 	}
 
 	public void clearCookies() {
 		driver.manage().deleteAllCookies();
 	}
 
 	private void initWebDriverEventListener(WebDriverWrapper driverWrapper) {
 		if (webDriverEventListenerClasses != null && !webDriverEventListenerClasses.isEmpty()) {
 			String[] listeners = webDriverEventListenerClasses.split(";");
 
 			ClassLoader loader;
 			if (JSystemProperties.getInstance().isJsystemRunner()) {
 				loader = new ExtendsTestCaseClassLoader(ClassPathBuilder.getClassPath(), getClass().getClassLoader());
 			}
 			else {
 				loader = this.getClass().getClassLoader();
 			}
 
 			for (String eventListenerClass: listeners) {
 				try {
 					
 					if(eventListenerClass !=null && !eventListenerClass.isEmpty()){
 						// Trim the class string
 						eventListenerClass = eventListenerClass.trim();
 
 						// load the class by the class loader
 						Class<?> loadClass = loader.loadClass(eventListenerClass);
 
 						// create new instance of the class
 						WebDriverEventListener webDriverEventListener = (WebDriverEventListener) loadClass.newInstance();
 
 						// register the class as a web driver event handler.
 						driverWrapper.register(webDriverEventListener);
 					}
 			
 
 				} catch (Throwable e) {
 					report.report(eventListenerClass + " cannot be loaded", StringUtils.getStackTrace(e),Reporter.PASS);
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Creates Selenium client using SUT parameters
 	 * 
 	 * @return SeleniumClient
 	 * @throws Exception
 	 */
 	protected WebDriverWrapper getBrowserInstance() {
 		WebDriverWrapper webDriverInstance = null;
 
 		if (getWebDriver() != null) {
 			try {
				webDriverInstance = webDriverFactory(getWebDriver().getBorwserType());
 			} catch (Exception e) {
 				report("SUT file is using deprecated methodology. please refer to " + this.getClass() + " documentation" , Reporter.WARNING );
				webDriverInstance = webDriverFactory(getWebDriver());
 				//throw new RuntimeException(e);
 			}
 		}
 		else {
 
 			String error = "Browser instance init Exception, the webDriver type(e.g. CHROME_DRIVER) must not be null";
 			error += "\n" + WebDriverType.getAllSupportedWebDriverTypesAsString();
 
 			throw new RuntimeException(error);
 		}
 		return webDriverInstance;
 	}
 
 	/**
 	 * will return the Registered Web Driver Event Listeners as a list
 	 * 
 	 * @return list of all the Registered Web Driver Event Listeners
 	 */
 	public List<WebDriverEventListener> getAllRegisteredWebDriverEventListeners() {
 		List<WebDriverEventListener> registeredWebDriverEventListeners;
 		try {
 			registeredWebDriverEventListeners = driver.getAllRegisteredWebDriverEventListeners();
 		} catch (Exception e) {
 			registeredWebDriverEventListeners = new ArrayList<WebDriverEventListener>();
 		}
 
 		return registeredWebDriverEventListeners;
 	}
 
 
 	@Deprecated
 	protected WebDriverWrapper webDriverFactory(WebDriverType type) {
 
 		WebDriverWrapper driver = null;
 		WebDriver webDriver = null;
 
 		try {
 
 			if (type == null) {
 				throw new IllegalArgumentException(
 						"the input browser type is null(change the value of the 'webDriver' Type in the SUT");
 			}
 
 			switch (type) {
 			case CHROME_DRIVER:
 				webDriver = getChromeDriver();
 			break;
 
 			case FIREFOX_DRIVER:
 				webDriver = getFirefoxWebDriver();
 			break;
 
 			case INTERNET_EXPLORER_DRIVER:
 				webDriver = getInternetExplorerWebDriver();
 			break;
 
 			case HTML_UNIT_DRIVER:
 				webDriver = getHtmlUnitDriver();
 			break;
 
 			case OPERA_DRIVER:
 				webDriver = getOperaDriver();
 			break;
 
 			case ANDROID_DRIVER:
 				webDriver = getAndroidDriver();
 			break;
 
 			case IPHONE_DRIVER:
 				webDriver = getIphoneDriver();
 			break;
 
 			case SAFARI_DRIVER:
 				webDriver = getSafariDriver();
 			break;
 
 			default:
 				throw new IllegalArgumentException("WebDriver type is unsupported");
 
 			}
 		} catch (IllegalArgumentException e) {
 			report.report(e.getMessage() + WebDriverType.getAllSupportedWebDriverTypesAsString(), false);
 			e.printStackTrace();
 			throw e;
 		}
 
 		if (webDriver == null) {
 			report.report("Failed to init the web driver", false);
 			throw new IllegalArgumentException("Failed to init the web driver-" + type);
 		}
 		else {
 			driver = new WebDriverWrapper(webDriver);
 		}
 		return driver;
 	}
 
 	@Deprecated
 	private WebDriver getSafariDriver() {
 		WebDriver driver = null;
 		try {
 			driver = new SafariDriver();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return driver;
 	}
 
 	@Deprecated
 	protected WebDriver getIphoneDriver() {
 		WebDriver webDriver = null;
 		try {
 			webDriver = new IPhoneDriver();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return webDriver;
 	}
 
 	@Deprecated
 	protected WebDriver getAndroidDriver() {
 		return new AndroidDriver();
 	}
 
 	protected WebDriver getOperaDriver() {
 		// return new OperaDriver();
 		return null;// Opeara is not supported
 	}
 
 	@Deprecated
 	protected WebDriver getHtmlUnitDriver() {
 		return new HtmlUnitDriver();
 	}
 
 	private void copyResource(String sourcePath, File destination) {
 		ClassLoader loader = LoadersManager.getInstance().getLoader();
 		InputStream is = loader.getResourceAsStream(sourcePath);
 		try {
 			FileUtils.saveInputStreamToFile(is, destination);
 		} catch (Exception e) {
 			report.report("Failed reasource" + sourcePath + " to File " + destination, Reporter.WARNING);
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				report.report("failed closing input stream for " + sourcePath, Reporter.WARNING);
 			}
 		}
 	}
 	
 	@Deprecated
 	protected WebDriver getInternetExplorerWebDriver() {
 		WebDriver webDriver = null;
 		try {
 
 			File IEDriverServerExe = File.createTempFile("IEDriverServer", ".exe");
 			copyResource("IEDriverServer.exe", IEDriverServerExe);
 			System.setProperty("webdriver.ie.driver", IEDriverServerExe.getAbsolutePath());
 			IEDriverServerExe.deleteOnExit();
 
 			DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
 			ieCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
 			if (ignoreCertificateErrors) {
 				ieCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
 			}
 			webDriver = new InternetExplorerDriver(ieCapabilities);
 		} catch (Exception e) {
 			webDriver = new InternetExplorerDriver();
 		}
 		return webDriver;
 	}
 
 	private FirefoxProfile ffProfile = null;
 	private FirefoxBinary ffBinary = null;
 	
 	@Deprecated
 	protected WebDriver getFirefoxWebDriver() {
 		WebDriver webDriver = null;
 		try {
 			if (ffProfile == null) {
 				ffProfile = getFirefoxConfigurationProfile();
 			}
 
 			if (browserPath == null || browserPath.isEmpty()) {
 				webDriver = new FirefoxDriver(ffProfile);
 			}
 			else {
 				if (ffBinary == null) {
 					System.setProperty("webdriver.firefox.bin", browserPath);
 					// Build  the Default path of Firefox driver in runtime
 					report.report("set Firefox bin Property :" + browserPath);
 					File pathToFirefoxBinary = new File(browserPath);
 					ffBinary = new FirefoxBinary(pathToFirefoxBinary);
 					report.report("the Property was set to :" + System.getProperty("webdriver.firefox.bin"));
 				}
 				webDriver = new FirefoxDriver(ffBinary, ffProfile);
 			}
 		} catch (Exception e) {
 			report.report("failed to set browser path =" + browserPath + ". " + e.getMessage(), e);
 		}
 		return webDriver;
 	}
 	@Deprecated
 	protected WebDriver getChromeDriver() {
 
 		WebDriver driver = null;
 		String chromeDriverPath;
 		ChromeOptions options = new ChromeOptions();
 		List<String> switches = new ArrayList<String>();
 
 		try {
 			chromeDriverPath = getChromeDriverExePath();
 
 			report.report("the Chrome Driver path is =" + chromeDriverPath);
 			System.setProperty("webdriver.chrome.driver", chromeDriverPath);
 
 			if (chromeProfile != null) {
 				report.report("open webDriver chrome with the profile(" + chromeProfile + ").");
 				switches.add("--user-data-dir=" + chromeProfile);
 			}
 
 			if (chromeExtension != null) {
 				report.report("open webDriver chrome with the chromeExtension(" + chromeExtension + ").");
 				switches.add("--load-extension=" + chromeExtension);
 			}
 
 			if (windowMaximize == true) {
 				report.report("open webDriver chrome with the flag of Maximized.");
 				switches.add("--start-maximized");
 			}
 
 			if (ignoreCertificateErrors == true) {
 				report.report("open webDriver chrome with the flag of ignoreCertificateErrors.");
 				switches.add("--ignore-certificate-errors");
 			}
 			
 			if(chromeFlags!=null){
 				String[] flags = chromeFlags.split(",");
 				for (String flag : flags) {
 					if(flag.startsWith("--")==false){
 						flag="--"+flag;
 					}
 					switches.add(flag);
 				}
 			}
 
 			if (switches.size() > 0) {
 				options.addArguments(switches);
 				driver = new ChromeDriver(options);
 			}
 			else {
 				driver = new ChromeDriver();
 			}
 		} catch (Throwable e) {
 
 			report.report("Error in init the chorme driver", e);
 			e.printStackTrace();
 		}
 		return driver;
 	}
 
 	/**
 	 * Build the Default path of Chrome driver in runtime Note: the
 	 * 'chromedriver.exe' must be under lib folder on the local project(the
 	 * tests/src project).
 	 * 
 	 * @throws URISyntaxException
 	 */
 	private String getChromeDriverExePath() throws IOException, URISyntaxException {
 		String path = "";
 
 		try {
 			if (chromeDriverPath == null) {
 
 				// final File testSrcFolder = new
 				// File(JSystemProperties.getInstance().getPreference(
 				// FrameworkOptions.TESTS_SOURCE_FOLDER));
 				// final File libFolder = new File(testSrcFolder.getParent()
 				// +File.separator+".."+File.separator+".."+File.separator,
 				// "lib");
 				//
 
 				URL location = WebDriverSystemObject.class.getProtectionDomain().getCodeSource().getLocation();
 
 				String file = location.getFile();
 
 				String decode = URLDecoder.decode(file);
 
 				File classesDir = new File(decode);
 				File target = new File(classesDir.getParent());
 				File rootDir = new File(target.getParent());
 				File chromeExe = new File(rootDir.getAbsoluteFile() + File.separator + "src" + File.separator + "main"
 						+ File.separator + "resources" + File.separator + CHROME_DRIVER_EXE_NAME);
 
 				if (chromeExe.exists()) {
 					report.report("found the chrome driver exe(default folder -" + chromeExe + ").");
 					path = chromeExe.getAbsolutePath();
 				}
 				else {
 					report.report("didn't find the chromedriver.exe under the default folder -" + chromeExe);
 				}
 
 				// if (!libFolder.exists()) {
 				// throw new IOException("Failed to find lib folder " +
 				// libFolder.getAbsolutePath()
 				// + ", chrome driver init failed");
 				// }
 				// path = libFolder.getAbsolutePath() + File.separator +
 				// CHROME_DRIVER_EXE_NAME;
 			}
 			else {
 				path = chromeDriverPath;
 			}
 
 		} catch (Exception e) {
 			report.report("Failed to get the chrome driver path",e.getMessage(),Reporter.WARNING);
 		}
 
 		return path;
 	}
 
 	private void setImplicitlyWait(WebDriverWrapper webDriverWrapper, String seleniumTimeOut) {
 		try {
 			long miliSecs = Long.valueOf(seleniumTimeOut);
 			report.report("Set WebDriver implicitlyWait seleniumTimeOut to " + seleniumTimeOut + "(Milliseconds)");
 			webDriverWrapper.getDriver().manage().timeouts().implicitlyWait(miliSecs, TimeUnit.MILLISECONDS);
 		} catch (Exception e) {
 			report.report("unable to set WebDriver implicitlyWait seleniumTimeOut,please check the SUT seleniumTimeOut parameter(Milliseconds  as String)");
 		}
 
 	}
 
 	protected FirefoxProfile getFirefoxConfigurationProfile() {
 		File extention = null;
 		FirefoxProfile profile = null;
 
 		if (firefoxProfile != null) {
 			report.report("open webDriver firefox with the profile(" + firefoxProfile + ").");
 			File profileLib = new File(firefoxProfile);
 			if (profileLib != null && !profileLib.exists() || profileLib == null) {
 				throw new IllegalArgumentException("Failed to find firefox profile. is the path : " + firefoxProfile
 						+ " contains the profile?");
 			}
 			profile = new FirefoxProfile(profileLib);
 		}
 
 		if (firefoxExtension != null) {
 			report.report("open webDriver firefox with Extension(" + firefoxExtension + ").");
 			if (null == profile) {
 				profile = new FirefoxProfile();
 			}
 			extention = new File(firefoxExtension);
 			if (!extention.exists()) {
 				throw new IllegalArgumentException("Failed to find firefox Extension. is the path : "
 						+ firefoxExtension + " contains the .xpi extantion?");
 			}
 			try {
 
 				profile.addExtension(extention);
 			} catch (IOException e) {
 				report.report("faild to add Extension to the firefox profile", false);
 				throw new IllegalArgumentException("Failed to add firefox c to profile. is the path : "
 						+ firefoxExtension + " contains the firefoxExtension?");
 			}
 		}
 		if (this.ignoreCertificateErrors) {
 			if (null == profile) {
 				profile = new FirefoxProfile();
 			}
 			report.report("set accept Untrusted Certificates");
 			profile.setAcceptUntrustedCertificates(ignoreCertificateErrors);
 		}
 
 		try {
 			if (profile != null) {
 				profile.setEnableNativeEvents(true);
 			}
 		} catch (Exception e) {
 			report.report("faild to enable native event on the firefox profile", Reporter.WARNING);
 		}
 		return profile;
 	}
 
 	/**
 	 * Closes WebDriver Selenium client driver instance<br/>
 	 * This function will also delete the temp profile.
 	 * 
 	 * @throws Exception
 	 *             when {@code WebDriver.quit()} throws an exception
 	 * @see org.openqa.selenium.WebDriver#quit()
 	 */
 	public void closeBrowserInstance() {
 
 		report.report("Closing selenium client");
 		if (driver != null) {
 			driver.quit();
 			browserIsOpened = false;
 		}
 
 	}
 
 	/**
 	 * Closes the browser window, but not removing the profile.
 	 * 
 	 * @see org.openqa.selenium.WebDriver#close()
 	 */
 	public void closeBrowser() {
 		if (browserIsOpened) {
 			driver.close();
 			pageKeeper = null;
 		}
 		browserIsOpened = false;
 	}
 
 	@Override
 	public void close() {
 		report.report("Closing WebDriver System Object");
 		try {
 			closeBrowserInstance();
 		} catch (Exception e) {
 			report.report("error in closing WebDriver System Object,Error=" + e.getMessage());
 		} finally {
 			pageKeeper = null;
 		}
 	}
 
 	/*
 	 * feature added by Aharon @ 01/08/2012 - PageKeeper
 	 */
 	private CurrentPageKeeper pageKeeper = new CurrentPageKeeper();
 
 	/**
 	 * @return the pageKeeper
 	 */
 	public CurrentPageKeeper getPageKeeper() {
 		if (pageKeeper == null) {
 			pageKeeper = new CurrentPageKeeper();
 		}
 
 		if (pageKeeper.getDriver() != driver) {
 			pageKeeper.setDriver(driver);
 		}
 		if (this instanceof AbstractPageObjectResolver) {
 			pageKeeper.registerPageObjectResolver((AbstractPageObjectResolver) this);
 		}
 
 		return pageKeeper;
 	}
 
 	/**
 	 * Setters/Getters
 	 */
 	public String getDomain() {
 		return domain;
 	}
 
 	/**
 	 * will return the current instance of the WebDriver if exist.
 	 * 
 	 * @return WebDriver
 	 */
 	public WebDriver getDriver() {
 		return this.driver;
 	}
 
 	/**
 	 * Sets the startup domain for the browser.
 	 */
 	public void setDomain(String domain) {
 		this.domain = domain;
 	}
 
 	public String getChromeDriverPath() {
 		return chromeDriverPath;
 	}
 
 	public void setChromeDriverPath(String chromeServerDriverPath) {
 		this.chromeDriverPath = chromeServerDriverPath;
 	}
 
 	public String getScreenShotPath() {
 		return screenShotPath;
 	}
 
 	public void setScreenShotPath(String screenShotPath) {
 		this.screenShotPath = screenShotPath;
 	}
 
 	public String getSeleniumTimeOut() {
 		return seleniumTimeOut;
 	}
 
 	public void setSeleniumTimeOut(String timeout) {
 		this.seleniumTimeOut = timeout;
 	}
 
 	public String getScreenShotFolderName() {
 		return screenShotFolderName;
 	}
 
 	public void setScreenShotFolderName(String screenShotFolderName) {
 		this.screenShotFolderName = screenShotFolderName;
 	}
 
 	public String getBrowserPath() {
 		return browserPath;
 	}
 
 	public void setBrowserPath(String browserPath) {
 		this.browserPath = browserPath;
 	}
 
 	public String getChromeFlags() {
 		return chromeFlags;
 	}
 
 	public void setChromeFlags(String chromeFlags) {
 		this.chromeFlags = chromeFlags;
 	}
 
 	public boolean isWindowMaximize() {
 		return windowMaximize;
 	}
 
 	public void setWindowMaximize(boolean windowMaximize) {
 		this.windowMaximize = windowMaximize;
 	}
 
 	public String getChromeProfile() {
 		return chromeProfile;
 	}
 
 	public void setChromeProfile(String chromeProfile) {
 		this.chromeProfile = chromeProfile;
 	}
 
 	public String getChromeExtension() {
 		return chromeExtension;
 	}
 
 	public void setChromeExtension(String chromeExtension) {
 		this.chromeExtension = chromeExtension;
 	}
 
 	public String getFirefoxExtension() {
 		return firefoxExtension;
 	}
 
 	public void setFirefoxExtension(String firefoxExtension) {
 		this.firefoxExtension = firefoxExtension;
 	}
 
 	public String getFirefoxProfile() {
 		return firefoxProfile;
 	}
 
 	public void setFirefoxProfile(String firefoxProfile) {
 		this.firefoxProfile = firefoxProfile;
 	}
 
 	public boolean isIgnoreCertificateErrors() {
 		return ignoreCertificateErrors;
 	}
 
 	public void setIgnoreCertificateErrors(boolean ignoreCertificateErrors) {
 		this.ignoreCertificateErrors = ignoreCertificateErrors;
 	}
 
 	public String getWebDriverEventListenerClasses() {
 		return webDriverEventListenerClasses;
 	}
 
 	public void setWebDriverEventListenerClasses(String webDriverEventListenerClasses) {
 		this.webDriverEventListenerClasses = webDriverEventListenerClasses;
 	}
 
 	public WebDriverType getWebDriver() {
 		return webDriver;
 	}
 
 	public void setWebDriver(WebDriverType webDriver) {
 		this.webDriver = webDriver;
 	}
 
 	public boolean isLazyInit() {
 		return lazyInit;
 	}
 
 	public void setLazyInit(boolean lazyInit) {
 		this.lazyInit = lazyInit;
 	}
 
 	/**
 	 * @return True if and only if the driver set to clear cookies inside
 	 *         {@link #openBrowser()} method
 	 */
 	public boolean isClearCookiesBeforeOpen() {
 		return clearCookiesBeforeOpen;
 	}
 
 	/**
 	 * The WebDriverSystemObject can delete all cookies before navigating to the
 	 * domain (in {@link #openBrowser()} and {@link #init()} methods)
 	 * 
 	 * @param clearCookiesBeforeOpen
 	 * 
 	 */
 	public void setClearCookiesBeforeOpen(boolean clearCookiesBeforeOpen) {
 		this.clearCookiesBeforeOpen = clearCookiesBeforeOpen;
 	}
 }
