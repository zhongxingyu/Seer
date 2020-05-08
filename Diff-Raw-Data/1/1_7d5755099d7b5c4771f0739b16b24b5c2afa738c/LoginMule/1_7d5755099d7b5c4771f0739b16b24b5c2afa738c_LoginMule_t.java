 package com.roosterpark.rptime.selenium.mule;
 
 import com.roosterpark.rptime.selenium.page.ConfirmationPage;
 import com.roosterpark.rptime.selenium.page.HomePage;
 import com.roosterpark.rptime.selenium.page.LandingPage;
 import com.roosterpark.rptime.selenium.page.LoginPage;
 import com.roosterpark.rptime.selenium.user.AdminUser;
 import com.roosterpark.rptime.selenium.user.StandardUser;
 import com.roosterpark.rptime.selenium.user.User;
 import org.openqa.selenium.WebDriver;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * User: John
  * Date: 10/25/13
  * Time: 10:21 AM
  */
 public class LoginMule {
 
     private WebDriver driver;
     private User user;
 
     private HomePage homePage;
 
     public LoginMule(WebDriver driver) {
         this.driver = driver;
     }
 
     public User getUser() {
         return user;
     }
 
     public HomePage loginAsAdmin() {
         user = new AdminUser();
         homePage = login(user);
        homePage.setAdmin(true);
         assertTrue("Not logged in as admin!", homePage.isLoggedInAsAdmin());
         return homePage;
     }
 
     public HomePage loginAsStandard() {
         user = new StandardUser();
         homePage = login(user);
         assertFalse("Logged in as admin!", homePage.isLoggedInAsAdmin());
         return homePage;
     }
 
     private HomePage login(User user) {
         LandingPage landingPage = new LandingPage(driver);
         landingPage.openPage();
         LoginPage loginPage = landingPage.clickSignInLink();
         ConfirmationPage confirmationPage = loginPage.signIn(user.getUsername(), user.getPassword());
         homePage = confirmationPage.confirm();
         return homePage;
     }
 
 }
