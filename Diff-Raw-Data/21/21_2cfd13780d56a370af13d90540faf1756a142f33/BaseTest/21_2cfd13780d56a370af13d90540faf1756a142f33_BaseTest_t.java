 package com.enonic.autotests;
 
 import java.io.IOException;
 
 import org.testng.ITestContext;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 
 import com.enonic.autotests.logger.Logger;
 import com.enonic.autotests.services.IAdminConsoleService;
 import com.enonic.autotests.services.v4.AdminConsoleServiceImplV4;
 import com.enonic.autotests.utils.TestUtils;
 
 public class BaseTest {
 
 	protected  Logger logger = Logger.getLogger();
 	protected IAdminConsoleService adminConsoleServiceV4 = new AdminConsoleServiceImplV4();
 	private TestSession testSession;
 
 	@BeforeClass
 	public void readDesiredCapabilities(ITestContext context) {
 		testSession = new TestSession();
 		String browser = (String) context.getCurrentXmlTest().getParameter("browser");
		if (browser == null) {
 			throw new IllegalArgumentException("parameter browser was not specified! ");
 		}
 		String browserVersion = (String) context.getCurrentXmlTest().getParameter("browserVersion");
 		if(browserVersion == null){
 			throw new IllegalArgumentException("parameter browserVersion was not specified! ");
 		}
 		String platform = (String) context.getCurrentXmlTest().getParameter("platform");
 		if(platform == null){
 			throw new IllegalArgumentException("parameter platform was not specified! ");
 		}
 		testSession.put(TestSession.BROWSER_NAME, browser);
 		testSession.put(TestSession.BROWSER_VERSION, browserVersion);
 		testSession.put(TestSession.PLATFORM, platform);
 		String url = (String) context.getCurrentXmlTest().getParameter("base.url");
 		if(url == null){
 			throw new IllegalArgumentException("parameter base url was not specified! ");
 		}
 		testSession.put(TestSession.START_URL, url);
 		String remoteParam = context.getCurrentXmlTest().getParameter("isRemote");
 		if(remoteParam == null){
 			throw new IllegalArgumentException("parameter isRemote was not specified! ");
 		}
 		Boolean isRemote = Boolean.valueOf(remoteParam);
 		if (isRemote != null) {
 			testSession.put(TestSession.IS_REMOTE, isRemote);
 			if (isRemote) {
 				String hubUrl = (String) context.getCurrentXmlTest().getParameter("hub.url");
 				if(hubUrl == null){
 					throw new IllegalArgumentException("parameter hubUrl was not specified! ");
 				}
 				testSession.put(TestSession.HUB_URL, hubUrl);
 			}
 		}
 
 	}
 	@BeforeMethod
 	public void openBrowser() throws IOException {
 		TestUtils.getInstance().createDriverAndOpenBrowser(getTestSession());
 		
 		
 
 	}
 
 	@AfterMethod
 	public void closeBrowser() {
 		getTestSession().closeBrowser();
 	
 	}
 
 	public TestSession getTestSession() {
 		return testSession;
 	}
 
 }
