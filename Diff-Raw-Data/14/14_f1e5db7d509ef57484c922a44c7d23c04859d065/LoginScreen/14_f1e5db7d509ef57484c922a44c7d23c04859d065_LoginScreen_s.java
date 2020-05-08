 package play.modules.webtest;
 
 import org.openqa.selenium.WebDriver;
 
 public class LoginScreen extends Screen {
 
     public LoginScreen(WebDriver driver) {
         super(driver);
         open("/login");
        assertEquals("Login", driver.getTitle());
     }
 
     public Screen login(String user, String password) {
         findByCss("#username").sendKeys(user);
         findByCss("#password").sendKeys(password);
         findByCss("#signin").click();
 
         assertElementDoesNotHaveText("#login .error", "Oops, unknown username or password.");
 
         return this;
     }
 
 }
