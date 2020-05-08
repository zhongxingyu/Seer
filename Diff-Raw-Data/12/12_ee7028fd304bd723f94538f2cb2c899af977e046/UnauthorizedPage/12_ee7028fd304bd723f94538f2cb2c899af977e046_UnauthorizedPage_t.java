 package com.euroit.militaryshop.order;
 
 import com.euroit.militaryshop.BasePage;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.FluentWait;
 import org.openqa.selenium.support.ui.Select;
 import org.openqa.selenium.support.ui.Wait;
 
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author EuroITConsulting
  */
 public class UnauthorizedPage extends BasePage {
 
    private static final String UNAUTHORIZED_PAGE_TITLE = "Anmelden";
 
     public UnauthorizedPage(WebDriver driver) {
         super(driver);
 
         Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                 .withTimeout(30, TimeUnit.SECONDS)
                 .pollingEvery(2, TimeUnit.SECONDS);
         wait.until(ExpectedConditions.titleIs(UNAUTHORIZED_PAGE_TITLE));
     }
 
     public SelectPaymentPage registerForOrder(String firstname, String secondname, String mainAddress, String postalCode,
                                               String city, String country, String registerUsername,
                                               String registerPassword, String registerPassword2, boolean sameDeliveryAddress,
                                               boolean agreementsConfirmed) {
         driver.findElement(By.id("firstname")).sendKeys(firstname);
         driver.findElement(By.id("secondname")).sendKeys(secondname);
         driver.findElement(By.id("mainAddress")).sendKeys(mainAddress);
         driver.findElement(By.id("postalCode")).sendKeys(postalCode);
         driver.findElement(By.id("city")).sendKeys(city);
         driver.findElement(By.id("country")).sendKeys(country);
         driver.findElement(By.id("registerUsername")).sendKeys(registerUsername);
         driver.findElement(By.id("registerPassword")).sendKeys(registerPassword);
         driver.findElement(By.id("registerPassword2")).sendKeys(registerPassword2);
 
         if (sameDeliveryAddress) {
             driver.findElement(By.id("sameDeliveryAddress")).click();
         } else {
             driver.findElement(By.id("differentDeliveryAddress")).click();
         }
 
         WebElement agreementsConfirmedCheckbox = driver.findElement(By.id("agreementsConfirmed"));
         if (agreementsConfirmed && !agreementsConfirmedCheckbox.isSelected()) {
             driver.findElement(By.id("agreementsConfirmed")).click();
         }
         //submits enclosing form
         agreementsConfirmedCheckbox.submit();
 
         return new SelectPaymentPage(driver);
     }
 }
