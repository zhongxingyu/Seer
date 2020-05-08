 package com.roosterpark.rptime.selenium.page;
 
 import com.roosterpark.rptime.selenium.BasicPageObject;
 import com.roosterpark.rptime.selenium.control.Button;
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

    private TextField emailField;
    private TextField passwordField;
    private SignInButton signInButton;
 
     public LoginPage(WebDriver driver) {
         super(driver);
     }
 
     public ConfirmationPage signIn(String email, String password) {
        emailField = new TextField(getWebDriver(), EMAIL_ID);
        passwordField = new TextField(getWebDriver(), PASSWORD_ID);
        signInButton = new SignInButton(getWebDriver());
         emailField.enterText(email);
         passwordField.enterText(password);
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
