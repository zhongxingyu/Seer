 package br.com.camiloporto.cloudfinance.ui.mobile;
 
 import org.openqa.selenium.support.PageFactory;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import br.com.camiloporto.cloudfinance.ui.mobile.page.MobileHomePage;
 import br.com.camiloporto.cloudfinance.ui.mobile.page.RootAccountHomePage;
 
 public class LoginUserMobileWUITest extends AbstractWUITest {
 
 
 	@Test
 	public void shouldLoginExistentUser() {
 		MobileHomePage mhp = PageFactory.initElements(driver,
 				MobileHomePage.class);
 		mhp.login(NEWUSER_GMAIL_COM, NEWUSER_PASS);
 		RootAccountHomePage rootAccountPage = PageFactory.initElements(driver,
 				RootAccountHomePage.class);
 		rootAccountPage.checkRootAccountsArePresent(NEWUSER_GMAIL_COM);
 
 	}
 	
 	@Test
 	public void shouldLogoffExistentLoggedUser() {
 		MobileHomePage mhp = PageFactory.initElements(driver,
 				MobileHomePage.class);
 		mhp.login(NEWUSER_GMAIL_COM, NEWUSER_PASS);
 		RootAccountHomePage rootAccountPage = PageFactory.initElements(driver,
 				RootAccountHomePage.class);
 		rootAccountPage.checkRootAccountsArePresent(NEWUSER_GMAIL_COM);
 		
 		rootAccountPage.logoff();
 		String currentLocation = driver.getCurrentUrl();
 		
 		Assert.assertTrue(currentLocation.endsWith("/mobile"), "current location [" + currentLocation + "] did not match expected end path ");
 
 	}
 
 	@Test
 	public void shouldStayOnLoginPageAndShowErrorMessageOnAuthenticationFailed() {
 		MobileHomePage mhp = PageFactory.initElements(driver,
 				MobileHomePage.class);
 		mhp.login(NEWUSER_GMAIL_COM, "wrong_pass");
 
 		mhp = PageFactory.initElements(driver, MobileHomePage.class);
 		mhp.assertLoginStatusMessageIs("Login failed");
 
 	}
 	
 	@Test
 	public void shouldGoDirectoToRootAccountPageIfHasALoggedUser() {
 		MobileHomePage mhp = PageFactory.initElements(driver,
 				MobileHomePage.class);
 		//successful login
 		mhp.login(NEWUSER_GMAIL_COM, NEWUSER_PASS);
 		RootAccountHomePage rootAccountPage = PageFactory.initElements(driver,
 				RootAccountHomePage.class);
 		rootAccountPage.checkRootAccountsArePresent(NEWUSER_GMAIL_COM);
 		
 		//goto home page again
		goToPath("/mobile");
 		
 		//should redirect to root accounts page
 		rootAccountPage = PageFactory.initElements(driver,
 				RootAccountHomePage.class);
 		rootAccountPage.checkRootAccountsArePresent(NEWUSER_GMAIL_COM);
 		
 	}
 
 }
