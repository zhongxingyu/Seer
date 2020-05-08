 package tests.appiumTests.ios;
 
 import helpers.GenerateRandomString;
 import junit.framework.Assert;
 
 import org.testng.annotations.Test;
 
 import com.mobile.driver.wait.Sleeper;
 
 import tests.constants.ErrorMessages;
 
 public class AuthorizationTest extends BaseTest{
 	
 	private String VALUE_INPUT = "1234567890";
 	
 	private String CHARACTERS_INPUT = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 
 	@Test(priority=1)
 	public void  checkLoginFieldDigits() {
 		Sleeper.SYSTEM_SLEEPER.sleep(10000);
 		main.checkPage();
 		main.inputLoginTextfield(VALUE_INPUT);
 		Assert.assertEquals(main.getLoginFieldText(), VALUE_INPUT);
 	}
 	
 	@Test(priority=2)
 	public void  checkLoginFieldLetters() {
 		main.checkPage();
 		main.inputLoginTextfield(CHARACTERS_INPUT);
 		Assert.assertEquals(main.getLoginFieldText(), CHARACTERS_INPUT);
 		main.inputLoginTextfield(CHARACTERS_INPUT.toLowerCase());
 		Assert.assertEquals(main.getLoginFieldText(), CHARACTERS_INPUT.toLowerCase());
 	}
 		
 	@Test(priority=3)
 	public void checkLoginWithIncorrectCredentionals() {
 		main.checkPage();
 		String password = GenerateRandomString.generateString();
 		main.inputLoginTextfield(INCORRECT_USER_NAME);
 		main.inputPasswordTextfield(password);
 		main.clickLogin();
 		Assert.assertTrue(main.isErrorMessageAppears());
 	}
 	
 
 	@Test(priority=4)
 	public void  simpleLogin() {
 		main.checkPage();
		call = main.simpleLogin(USER_NAME, USER_PASSWORD);
 		call.checkPage();
 		Assert.assertTrue(call.isStatusAvailable());
 	}
 	
 	
 	
 }
