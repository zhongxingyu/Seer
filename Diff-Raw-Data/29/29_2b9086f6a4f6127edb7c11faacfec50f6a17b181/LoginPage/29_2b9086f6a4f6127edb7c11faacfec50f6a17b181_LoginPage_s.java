 package pages;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 public class LoginPage extends BasePage{
 	public LoginPage(WebDriver driver) {
 		super(driver);
         assertThat(driver.getTitle(), containsString("Central Authentication Service"));
 	}
 
    public ListInvoicePage loginWithValidCredentials() {
         driver.findElement(By.id("username")).sendKeys("bill");
         driver.findElement(By.id("password")).sendKeys("bill");
         driver.findElement(By.name("submit")).click();
        return new ListInvoicePage(driver);
     }
 
     public LoginPage loginWithInvalidCredentials() {
         driver.findElement(By.id("username")).sendKeys("bill");
         driver.findElement(By.id("password")).sendKeys("not bill");
         driver.findElement(By.name("submit")).click();
         return new LoginPage(driver);
     }
 }
 
