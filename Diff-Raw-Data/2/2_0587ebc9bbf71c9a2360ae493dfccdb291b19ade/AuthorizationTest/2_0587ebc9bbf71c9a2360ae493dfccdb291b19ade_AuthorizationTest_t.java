 package tests.appiumTests.ios;
 
 import helpers.GenerateRandomString;
 
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import com.ios.AppiumDriver;
 import com.mobile.driver.wait.Sleeper;
 
 import tests.constants.ErrorMessages;
 import tests.page.CallPage;
 import tests.page.SettingsPage;
 
 public class AuthorizationTest extends NonAutorizationBaseTest {
 
 	private String VALUE_INPUT = "1234567890";
 
 	private String CHARACTERS_INPUT = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 
 	@Test(priority = 1)
 	public void checkLoginFieldDigits() {
 		Sleeper.SYSTEM_SLEEPER.sleep(10000);
 		main.checkPage();
 		main.inputLoginTextfield(VALUE_INPUT);
 		Assert.assertEquals(main.getLoginFieldText(), VALUE_INPUT);
 	}
 
 	@Test(priority = 2)
 	public void checkLoginFieldLetters() {
 		main.checkPage();
 		main.inputLoginTextfield(CHARACTERS_INPUT);
 		Assert.assertEquals(main.getLoginFieldText(), CHARACTERS_INPUT);
 		main.inputLoginTextfield(CHARACTERS_INPUT.toLowerCase());
 		Assert.assertEquals(main.getLoginFieldText(),
 				CHARACTERS_INPUT.toLowerCase());
 	}
 
 	 @Test(priority=3, enabled = false)
 	 public void checkLoginWithIncorrectCredentionals() {
 	 main.checkPage();
 	 String password = GenerateRandomString.generateString();
 	 main.inputLoginTextfield(INCORRECT_USER_NAME);
 	 main.inputPasswordTextfield(password);
 	 main.clickLogin();
 	 Assert.assertTrue(main.isErrorMessageAppears());
 	 }
 	
 	@Test(priority = 4)
 	public void simpleLogin() {
 		main.checkPage();
 		call = main.simpleLogin(USER_NAME, USER_PASSWORD, false, false);
 		call.checkPage();
 		Assert.assertTrue(call.isStatusAvailable(), "CallPage doesn't open");
 	}
 
 	@Test(priority = 5, description = "Check save password functionality")
 	public void loginWithSavePasswordFlag() throws Exception {
 		AppiumDriver.class.cast(driver).quit();
 		initPages();
 		main.checkPage();
 		call = main.simpleLogin(USER_NAME, USER_PASSWORD, true, false);
 		call.checkPage();
 		AppiumDriver.class.cast(driver).quit();
 		initPages();
 		Sleeper.SYSTEM_SLEEPER.sleep(10000);
 		main.checkPage();
 		Assert.assertTrue(main.isSavePasswordCorrect(), "Sava password flad doesn't work correctly.Login or Password filed are empty");
 	}
 
 	@Test(priority = 6, description = "Check auto login functionality")
 	public void autoLogin() throws Exception {
 		AppiumDriver.class.cast(driver).quit();
 		initPages();
 		main.checkPage();
		call = main.simpleLogin(USER_NAME, USER_PASSWORD, true, true);
 		call.checkPage();
 		AppiumDriver.class.cast(driver).quit();
 		initPages();
 		Sleeper.SYSTEM_SLEEPER.sleep(10000);
 		Assert.assertTrue(call.isStatusAvailable(), "");
 		((SettingsPage) call.navigateToSettingsTab()).setAutoLogin(false);
 	}
 
 }
