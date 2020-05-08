 package org.motechproject.functional.pages;
 
 import org.motechproject.functional.base.WebDriverProvider;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 @Component
 public class LoginPage {
     public static String LOGIN_PATH = "/ghana-national-web/login.jsp";
 
     @Value("#{functionalTestProperties['host']}")
     private String host;
 
     @Value("#{functionalTestProperties['port']}")
     private String port;
 
     private WebDriver driver;
 
     @Autowired
     public LoginPage(WebDriverProvider driverProvider) {
         this.driver = driverProvider.getWebDriver();
     }
 
     public boolean LoginAs(String UserName, String Password) {
        driver.get("http://" + host + ":" + port + LOGIN_PATH);
 
         WebElement uName = driver.findElement(By.name("j_username"));
         uName.sendKeys(UserName);
         WebElement uPass = driver.findElement(By.name("j_password"));
         uPass.sendKeys(Password);
         WebElement BtnLogin = driver.findElement(By.xpath("//input[3]"));
 
         BtnLogin.click();
         try {
             Thread.sleep(500);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         String src = driver.getPageSource();
         if (src.contains("j_spring_security_check"))
             return false;
         else
             return true;
     }
 
 }
