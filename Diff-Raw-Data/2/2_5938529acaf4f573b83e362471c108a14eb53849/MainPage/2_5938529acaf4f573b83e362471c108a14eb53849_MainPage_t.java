 package com.euroit.militaryshop;
 
 import java.util.concurrent.TimeUnit;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.interactions.Actions;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.FluentWait;
 import org.openqa.selenium.support.ui.Wait;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 /**
  * @author EuroITConsulting
  */
 public class MainPage extends BasePage {
     private static final String MAIN_PAGE_TITLE = "Armee Online Shop - Militax";
 
 	public MainPage(WebDriver driver) {
         super(driver);
         driver.manage().window().maximize();
         Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                 .withTimeout(30, TimeUnit.SECONDS)
                 .pollingEvery(2, TimeUnit.SECONDS);
         wait.until(ExpectedConditions.titleIs(MAIN_PAGE_TITLE));
     }
 
 	public void putFirstProductInTrolleyAndCheck() {
 		WebElement firstProductThumb = driver.findElement(By.className("thumbnail"));
 		
 		Actions builder = new Actions(driver);
 		builder.moveToElement(firstProductThumb).perform();
         final WebElement toTrooleyButton = driver.findElement(By.cssSelector(".product-overlay-bottom .btn"));
         builder.moveToElement(toTrooleyButton)
                 .perform();
        ((JavascriptExecutor)driver).executeScript("$('.product-overlay-bottom .btn')[0].click();");
 		
 		Wait<WebDriver> wait = new FluentWait<>(driver)
                 .withTimeout(30, TimeUnit.SECONDS)
                 .pollingEvery(2, TimeUnit.SECONDS);
         wait.until(ExpectedConditions.textToBePresentInElement(By.id("trolley-items-count"), "1 Artikel"));
 	}
 
 	public String getNumberOfItemsInTrolley() {
 		return driver.findElement(By.id("trolley-items-count")).getText();
 	}
 
 	public ProductCardPage openFirstProductCard() {
 		WebElement firstProductThumb = driver.findElement(By.className("thumbnail"));
 		
 		Actions builder = new Actions(driver);
 		builder.moveToElement(firstProductThumb).build().perform();
 		driver.findElement(By.cssSelector(".product-overlay a.productFullName")).click();
 		
 		return new ProductCardPage(driver);
 	}
 }
