 import java.io.Serializable;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 /**
  *
  * @author Julia
  */
 public class WebpageLogin implements Action, Serializable {
 
     //private String url;
     private String username;
     private String password;
 
    public WebpageLogin(String user, String pass) {
         //this.url = url;
         this.username = user;
         this.password = pass;
     }
 
     public void facebookLaunch() {
         WebDriver driver = new FirefoxDriver();
         driver.get("http://www.facebook.com");
 
         WebElement email = driver.findElement(By.name("email"));
         email.sendKeys(username);
 
         WebElement pass = driver.findElement(By.name("pass"));
         pass.sendKeys(password);
 
         WebElement submit = driver.findElement(By.id("u_0_b"));
         submit.click();
     }
     
         public void piazzaLaunch() {
         WebDriver driver = new FirefoxDriver();
         driver.get("http://www.piazza.com");
         
         WebElement login = driver.findElement(By.id("topbar_login"));
         login.click();
 
         WebElement email = driver.findElement(By.name("email"));
         email.sendKeys(username);
 
         WebElement pass = driver.findElement(By.name("password"));
         pass.sendKeys(password);
         pass.sendKeys("\n");
     }
         
     public void hophacksLaunch() {
         WebDriver driver = new FirefoxDriver();
         driver.get("http://www.hophacks.com/forums");
         driver.get("http://www.hophacks.com/forums/entry/signin?Target=discussions");
         WebElement email = driver.findElement(By.name("Form/Email"));
         email.sendKeys(username);
 
         WebElement pass = driver.findElement(By.name("Form/Password"));
         pass.sendKeys(password);
 
         WebElement submit = driver.findElement(By.id("Form/Sign_In"));
         submit.click();
     }
 }
