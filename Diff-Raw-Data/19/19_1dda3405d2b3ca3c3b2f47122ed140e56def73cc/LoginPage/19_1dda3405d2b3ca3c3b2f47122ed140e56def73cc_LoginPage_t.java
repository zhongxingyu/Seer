 package com.roosterpark.rptime.selenium.page;
 
 import com.roosterpark.rptime.selenium.BasicPageObject;
 import com.roosterpark.rptime.selenium.control.Button;
import com.roosterpark.rptime.selenium.control.CheckBox;
 import com.roosterpark.rptime.selenium.control.TextField;
 import com.roosterpark.rptime.selenium.exception.NotDirectlyOpenableException;
 import org.openqa.selenium.WebDriver;
 
 /**
  * User: John
  * Date: 10/23/13
  * Time: 1:42 PM
  */
 public class LoginPage extends BasicPageObject {
 
     private static final String EMAIL_ID = "Email";
     private static final String PASSWORD_ID = "Passwd";
    private static final String SIGNED_IN_CHECK_BOX_ID = "PersistentCookie";
 
     public LoginPage(WebDriver driver) {
         super(driver);
     }
 
     public ConfirmationPage signIn(String email, String password) {
        TextField emailField = new TextField(getWebDriver(), EMAIL_ID);
        TextField passwordField = new TextField(getWebDriver(), PASSWORD_ID);
        SignInButton signInButton = new SignInButton(getWebDriver());
        CheckBox staySignedIn = new CheckBox(getWebDriver(), SIGNED_IN_CHECK_BOX_ID);
         emailField.enterText(email);
         passwordField.enterText(password);
        if (staySignedIn.isChecked()) {
            staySignedIn.check();
        }
         return signInButton.click();
     }
 
     @Override
     public void openPage() {
         throw new NotDirectlyOpenableException("Login page should not be opened directly.");
     }
 
     private class SignInButton extends Button<ConfirmationPage> {
 
         private static final String BUTTON_ID = "signIn";
 
         public SignInButton(WebDriver driver) {
             super(driver, BUTTON_ID);
         }
 
         @Override
         public ConfirmationPage click() {
             getElement().click();
             return new ConfirmationPage(getDriver());
         }
     }
 
 }
