 package com.roosterpark.rptime.selenium;
 
 import com.roosterpark.rptime.selenium.mule.LoginMule;
 import com.roosterpark.rptime.selenium.mule.WorkerMule;
 import com.roosterpark.rptime.selenium.page.*;
 import com.roosterpark.rptime.selenium.user.AdminUser;
 import com.roosterpark.rptime.selenium.user.StandardUser;
 import com.roosterpark.rptime.selenium.user.User;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * User: John
  * Date: 10/22/13
  * Time: 3:14 PM
  */
 public class LoginTest extends BasicSeleniumTest {
 
     private LandingPage landingPage;
     private LoginPage loginPage;
     private ConfirmationPage confirmationPage;
     private HomePage homePage;
     private WorkerMule workerMule;
     private LoginMule loginMule;
 
     private User user;
 
     @Before
     public void setup() {
         landingPage = new LandingPage(getDriver());
         workerMule = new WorkerMule(getDriver());
         loginMule = new LoginMule(getDriver());
     }
 
     @Test
     public void standardLoginTest() {
         workerMule.setHomePage(loginMule.loginAsAdmin());
        WorkerPage workerPage = workerMule.addSalariedWorker("Test", "User", "testuser@roosterpark.com", "2014-01-01");
         workerPage.clickSignOutButton();
         user = new StandardUser();
         landingPage.openPage();
         loginPage = landingPage.clickSignInLink();
         confirmationPage = loginPage.signIn(user.getUsername(), user.getPassword());
         homePage = confirmationPage.confirm();
         assertFalse("Logged in as admin!", homePage.isAdminWarningVisible());
         homePage.close();
     }
 
     @Test
     public void adminLoginTest() {
         user = new AdminUser();
         landingPage.openPage();
         loginPage = landingPage.clickSignInLink();
         confirmationPage = loginPage.signIn(user.getUsername(), user.getPassword());
         homePage = confirmationPage.confirm();
         assertTrue("Not logged in as admin!", homePage.isAdminWarningVisible());
         homePage.close();
     }
 
 }
