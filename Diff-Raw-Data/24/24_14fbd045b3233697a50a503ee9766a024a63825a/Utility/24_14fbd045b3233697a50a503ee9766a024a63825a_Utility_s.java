 package smoketest;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 public class Utility{
 
     /**
      * Uses js to click on hidden element on the page by XPATH
      * 
      * @param driver
      * @param menuXPATH 
      */
     public static void navigateToSubMenu(WebDriver driver, String menuXPATH) {
         WebElement hiddenElement = driver.findElement(By.xpath(menuXPATH));
         ((JavascriptExecutor) driver).executeScript("arguments[0].click()", hiddenElement);
     }
     
     public static void optionalClickByXPATH(WebDriver driver, String path) {
         try {
             if( driver.findElement(By.linkText(path)).isDisplayed() )
             {
                 driver.findElement(By.linkText(path)).click();
             }
         }
         catch( Exception ex ) {
             // Do nothing
         }
     }
}
//driver.findElement(By.linkText("Show More Groups")).click();
