 package pages;
 
 import config.Configuration;
 import org.openqa.selenium.WebDriver;
 
 public class BasePage{
 	protected WebDriver driver;
 
     protected BasePage(WebDriver driver) {
         this.driver = driver;
     }
 
     public BasePage goToGoogle() {
         driver.get("http://www.google.com");
         return new BasePage(driver);
     }
 
    public HomePage returnToKabbadi() {
         driver.get(Configuration.KABBADI_URL);
        return new HomePage(driver);
     }
 }
