 
 package com.myfitnesspal.qa.test.basictest;
 
 import java.lang.reflect.Method;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.remote.UnreachableBrowserException;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.Listeners;
 
 import com.myfitnesspal.qa.data.UserData;
 import com.myfitnesspal.qa.foundation.BaseTestListener;
 import com.myfitnesspal.qa.foundation.DriverFactory;
 import com.myfitnesspal.qa.foundation.DriverHelper;
 import com.myfitnesspal.qa.foundation.DriverPool;
 import com.myfitnesspal.qa.foundation.log.TestDriverIdCollector;
 import com.myfitnesspal.qa.utils.ConfigArgs;
 import com.myfitnesspal.qa.utils.Messager;
 import com.myfitnesspal.qa.utils.R;
 import com.myfitnesspal.qa.utils.rerunner.RetryListener;
 
 @Listeners(
 { BaseTestListener.class, RetryListener.class })
 public class BasicTestCase extends DriverHelper
 {
 	protected static final Logger LOGGER = Logger.getLogger(BasicTestCase.class);
 
 	public UserData mfpUser = new UserData(R.TESTDATA.get("user.name"), R.TESTDATA.get("user.password"));
 
 	public UserData mfpUser1 = new UserData(R.TESTDATA.get("user1.name"), R.TESTDATA.get("user1.password"));
 
 	public UserData mfpUser2 = new UserData(R.TESTDATA.get("user2.name"), R.TESTDATA.get("user2.password"));
 
 	public UserData mfpUser3 = new UserData(R.TESTDATA.get("user3.name"), R.TESTDATA.get("user3.password"));
 
 	public UserData mfpUser4 = new UserData(R.TESTDATA.get("user4.name"), R.TESTDATA.get("user4.password"));
 
 	public UserData mfpUser5 = new UserData(R.TESTDATA.get("user5.name"), R.TESTDATA.get("user5.password"));
 
 	@BeforeSuite(alwaysRun = true)
 	public void beforeSuite() {
 //		System.setProperty("webdriver.chrome.driver", R.CONFIG.get("driver.chrome.path"));
 		
 		PropertyConfigurator.configure(ClassLoader.getSystemResource("log4j.properties"));
 		Messager.TEST_CONFIGURATION.info(ConfigArgs.getBrowser(), ConfigArgs.getURL(), ConfigArgs.getGridHost());
 //		editableStorage = new Properties();
 //		Screenshot.removeOldScreenshotDirs();
 		
 //		chromeDriverService = new ChromeDriverService.Builder().usingChromeDriverExecutable(new File(R.CONFIG.get("driver.chrome.path")))
 //        .usingAnyFreePort()
 //        .build();
 //		try {
 //			chromeDriverService.start();
 //		} catch (IOException e) {
 //			e.printStackTrace();
 //		}
 	}
 	
 	@BeforeMethod(alwaysRun = true)
 	public void beforeMethod(Method m)
 	{
 		try{
 		driver = initDriver(m.getName());
 		}
 		catch(UnreachableBrowserException e)
 		{
 			LOGGER.info("UnreachableBrowserException was catch. Driver will be recreated in 10 sec");
 			pause(10);
 			driver = initDriver(m.getName());
 		}
 		TestDriverIdCollector.register(m.getName(), driver);
 		initSummary(driver);
 		DriverPool.associateTestNameWithDriver(m.getName(), driver);
 	}
 
 	@AfterMethod(alwaysRun = true)
 	public void afterMethod(Method m)
 	{
 		if (driver != null)
 		{
 			driver.quit();
 			driver = null;
 		}
 	}
 	
 	@AfterSuite(alwaysRun = true)
 	public void afterSuite() throws InterruptedException {
 		
 //		chromeDriverService.stop();
 		
 //			HtmlReportGenerator.generate(Screenshot.getBaseDir().getAbsolutePath());
 //			String emailReport = EmailReportItemCollector.createReport();
 //			TestResultType result = EmailReportItemCollector.getCommonTestResult();
 //			Email.sendContent("OpenSky test results: " + result.get(), emailReport, ConfigArgs.getEmailsList());
 	}
 	
 	protected WebDriver initDriver(String testName)
 	{
 		if (driver == null)
 		{
			driver = DriverFactory.create(testName, chromeDriverService.getUrl());
 		}
 		return driver;
 	}
 }
