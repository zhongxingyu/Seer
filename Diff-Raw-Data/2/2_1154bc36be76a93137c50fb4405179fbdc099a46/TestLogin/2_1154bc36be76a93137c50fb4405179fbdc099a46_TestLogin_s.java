 package gmail.Login;
 
 
 import gmail.Base;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 import static org.junit.Assert.*;
 import static utils.WebElements.Presence.elementIsPresent;
 
 public class TestLogin extends Base {
 
     // Should be valid credentials
     private final String validUsername = "myEmail@gmail.com";
     private final String validPass = "secret";
 
     // Should be invalid credentials
     private final String invalidUsername = "randomEmail@gmail.com";
     private final String invalidPass = "wrongSecret";
 
     private void submitForm(String username, String password) {
 
         // make sure the username field is cleared before submitting
         // We don't do this
         WebElement emailField = driver.findElement(By.id("Email"));
         if(emailField.getAttribute("value") != null) {
             emailField.clear();
         }
 
         fillTextInput(driver, "id", "Email", username);
         fillTextInput(driver, "id", "Passwd", password);
         pressButton(driver, "id", "signIn");
     }
 
    private Boolean stillAtLogin() {
         String current  = driver.getCurrentUrl();
         String loginUrl = "https://accounts.google.com/ServiceLoginAuth";
 
         if(current.equals(loginUrl)) {
             return true;
         }
         return false;
     }
 
     private void checkLoginFails(String username, String password) {
         submitForm(username, password);
         assertTrue(stillAtLogin());
     }
 
     @Test
     public void testEmptyFormFails() {
         checkLoginFails("", "");
     }
 
     @Test
     public void testBlankUsernameFails() {
         checkLoginFails("", validPass);
     }
 
     @Test
     public void testBlankPasswordFails() {
         checkLoginFails(validUsername, "");
     }
 
     @Test
     public void testInvalidUsernameFails() {
         checkLoginFails(invalidUsername, validPass);
     }
 
     @Test
     public void testInvalidPasswordFails() {
         checkLoginFails(validUsername, invalidPass);
     }
 
     @Test
     public void testValidCredentialsPass() {
 
         // This is the top level id of an element wrapping the Gmail inbox which
         // may change as Google updates Gmail.
         By mailBox = By.id(":4");
 
         submitForm(validUsername, validPass);
         assertTrue(elementIsPresent(driver, mailBox, 15));
     }
 }
