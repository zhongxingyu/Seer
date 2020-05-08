 package sele.pot.testsuit;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.Test;
 import org.openqa.selenium.WebDriver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sele.pot.pages.Page;
 import sele.pot.pages.homeRelated.ContactPage;
 import sele.util.CfgLoader;
 import sele.util.DriverUtil;
 import sele.util.ReportUtil;
 import sele.util.ScreenShotRule;
 
 
 
 
 
 public class ContactTest {
 	private final static Logger logger = LoggerFactory.getLogger(HomeTest.class);
 	private static WebDriver driver = DriverUtil.getDriver(CfgLoader.browserType);
 	private static String hostAndPortAndContext = "https://cloudbus.tibco.com/index.php/contact";
 
 	@AfterClass
 	public static void afterAllIsSaidAndDone() {
 //		driver.quit();
 
 	}
 
 	@After
 	public void tearDown() {
 	}
 
 	@Before
 	public void setUp() {
 		driver.get(hostAndPortAndContext);
 	}
 
 	@BeforeClass
 	public static void beforeAllIsSaidAndDone() {
 		//driver.manage().deleteAllCookies();
 	}
 
 	@Test
 	public void SubmitContactMsgWithInvalidEmail() throws IOException {
 		String actual = "";
 		try {
 
 			ContactPage contactPage = Page.getPage(ContactPage.class);
 			assertTrue(contactPage.isPresent());
 			logger.info("open contact page :" + driver.getTitle());
 			contactPage.inputEmailAndMsg("test.com", "test test test test");
 			logger.info("input invalid data");
 			contactPage.Submit();
 			logger.info("click submit");
 			actual = contactPage.getEmailErrorNotic();
 			assertEquals("What is your email address?", actual);
 			DriverUtil.savePassScreenshot("CB-28_2");
			boolean result = "Contact Cloud Bus".equals(actual);
 			ReportUtil
 					.insertReportLine(
 							"CB-28_2", // caseID
 							"contact", // CaseName
 							"open Contact link on home page, contact page should be opened", // CaseDescription
 							"There is a red message showing up", // expected
 							result == true ? "pass" : "fail", // test result
 							"none"); // comment assertTrue(result);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			ReportUtil.insertReportLine("CB-28_2", // caseID
 					"contact", // CaseName
 					"Input invalid email and message then click submit", // CaseDescription
 					"There is a red message showing up ", // expected
 					"error", // test result
 					"none"); // comment
 
 		}
 
 	}
 
 	@Test
 	public void SubmitContactMsgThenCheck() throws IOException {
 		String actual = "";
 		try {
 
 			ContactPage contactPage = Page.getPage(ContactPage.class);
 			assertTrue(contactPage.isPresent());
 			logger.info("contact page :" + driver.getTitle());
 			contactPage
 					.inputEmailAndMsg("test@test.com", "test test test test");
 			contactPage.Submit();
 			actual = contactPage.getThankUAftersubmit();
 			assertEquals("Your form has been submitted.", actual);
 			DriverUtil.savePassScreenshot("CB-28_3");
			boolean result = "Contact Cloud Bus".equals(actual);
 			ReportUtil
 					.insertReportLine(
 							"CB-28_3", // caseID_step
 							"contact", // CaseName
 							"Open Contact pape, input valid msg and email ,then submit.", // CaseDescription
 							"There will be a \"Thank you \" msg shows up after the message send out. ", // expected
 							result == true ? "pass" : "fail", // test result
 							"none"); // comment assertTrue(result);
 		} catch (Exception e) {
 			e.printStackTrace();
 			ReportUtil
 					.insertReportLine(
 							"CB-28_3", // caseID_step
 							"contact", // CaseName
 							"Open Contact pape, input valid msg and email ,then submit.", // CaseDescription
 							"There will be a \"Thank you \" msg shows up after the message send out. ", // expected
 							"error", // test result
 							"none"); // comment
 
 		}
 
 	}
 
 	 @Test
 	public void ClickResetThenCheck() throws IOException {
 		boolean actual = false;
 		try {
 
 			ContactPage contactPage = Page.getPage(ContactPage.class);
 			assertTrue(contactPage.isPresent());
 			contactPage
 					.inputEmailAndMsg("test@test.com", "test test test test");
 			contactPage.clickReset();
 			actual = contactPage.isInputEmpty();
 			assertTrue(actual);
 			DriverUtil.savePassScreenshot("CB-28_4");
			boolean result = "Contact Cloud Bus".equals(actual);
 			ReportUtil
 					.insertReportLine(
 							"CB-28_4", // caseID_step
 							"contact", // CaseName
 							" OpenContact page ,input valid msg and email adress,then click \"reset\".", // CaseDescription
 							" Email address input and Message input should be empty now.", // expected
							result == true ? "pass" : "fail", // test result
 							"none"); // comment assertTrue(result);
 		} catch (Exception e) {
 			e.printStackTrace();
 			ReportUtil
 					.insertReportLine(
 							"CB-28_4", // caseID_step
 							"contact", // CaseName
 							" OpenContact page ,input valid msg and email adress,then click \"reset\".", // CaseDescription
 							" Email address input and Message input should be empty now.", // expected
 							"error", // test result
 							"none"); // comment
 
 		}
 
 	}
 	
 	@Rule
 	public ScreenShotRule screenshotTestRule = new ScreenShotRule(driver);
 }
