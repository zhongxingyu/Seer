 
 package com.myfitnesspal.qa.test.account;
 
 import org.openqa.selenium.support.PageFactory;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import com.myfitnesspal.qa.foundation.BasicTestCase;
 import com.myfitnesspal.qa.pages.user.LoginPage;
 import com.myfitnesspal.qa.utils.rerunner.RetryAnalyzer;
 
 public class PasswordInsensitivityTest extends BasicTestCase
 {
 
 	private LoginPage loginPage;
 
 	@Test(groups = { "ui_regression" } , retryAnalyzer = RetryAnalyzer.class)
 	public void testPasswordInsensitivity()
 	{
 		loginPage = PageFactory.initElements(driver, LoginPage.class);
 		loginPage.open();
 		
 		loginPage.login(mfpUser.getLogin(), "TARANTINO");
 		Assert.assertTrue(loginPage.linkLogout.isDisplayed(), "Logout link doesn't present");
 		click("linkLogout", loginPage.linkLogout);
 		
 		loginPage.login(mfpUser.getLogin(), "tarantino");
 		Assert.assertTrue(loginPage.linkLogout.isDisplayed(), "Logout link doesn't present");
 		click("linkLogout", loginPage.linkLogout);
 	}	
 
 }
