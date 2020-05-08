 package org.ghana.national.pages;
 
 import org.jbehave.web.selenium.WebDriverProvider;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 public class LoginPage extends GhanaPage {
     public LoginPage(WebDriverProvider driverProvider) {
         super(driverProvider, "spring_security_login");
     }
 
     public void login(String user, String password) {
         inputField("j_username").sendKeys(user);
         inputField("j_password").sendKeys(password);
         inputField("submit").click();
     }
 
     private WebElement inputField(String j_username) {
         return findElement(By.name(j_username));
     }
 
     public void logout() {
        go("logout");
     }
 }
