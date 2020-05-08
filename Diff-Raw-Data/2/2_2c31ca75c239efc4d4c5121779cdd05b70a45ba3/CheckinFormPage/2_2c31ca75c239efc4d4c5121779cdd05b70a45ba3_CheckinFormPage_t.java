 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import static org.openqa.selenium.Keys.ARROW_DOWN;
 import static org.openqa.selenium.Keys.RETURN;
 import static org.openqa.selenium.support.ui.ExpectedConditions.stalenessOf;
 
 public class CheckinFormPage extends AbstractPageObject {
     public CheckinFormPage(WebDriver driver) {
 		super(driver);
 	}
 
 	public void enterInfo() {
         selectFirstOptionFor("typeOfVisit");
         selectSecondOptionFor("paymentAmount");
 
         WebElement confirmButton = driver.findElement(By.id("confirmationQuestion")).findElement(By.className("confirm"));
         confirmButton.click();
 
         new WebDriverWait(driver, 20).until(stalenessOf(confirmButton));
     }
 
     public void enterInfoWithMultipleEnterKeystrokesOnSubmit() {
         selectFirstOptionFor("typeOfVisit");
        selectSecondOptionFor("paymentAmount");
         WebElement confirmButton = driver.findElement(By.id("confirmationQuestion")).findElement(By.className("confirm"));
         confirmButton.sendKeys(RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,
                 RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,
                 RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN);
     }
 
 
     private void selectFirstOptionFor(String spanId) {
         findSelectInsideSpan(spanId).sendKeys(ARROW_DOWN, RETURN);
     }
 
     private void selectSecondOptionFor(String spanId) {
         findSelectInsideSpan(spanId).sendKeys(ARROW_DOWN, ARROW_DOWN, RETURN);
     }
 
     private WebElement findSelectInsideSpan(String spanId) {
         return driver.findElement(By.id(spanId)).findElement(By.tagName("select"));
     }
 	
 }
