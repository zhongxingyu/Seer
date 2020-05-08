 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.Keys;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
 
 public class ArchivesRoomApp extends AbstractPageObject {
 
     private WebDriverWait wait120Seconds = new WebDriverWait(driver, 120);
     private By pullRequestTabs = By.id("tab-selector-pull");
 
     public ArchivesRoomApp(WebDriver driver) {
 		super(driver);
 	}
 	
 	public boolean isPatientInList(String patientIdentifier, String list) {
 		try {
 			findPatientInTheList(patientIdentifier, list);
 		    return true;
 		} catch (NoSuchElementException e) {
 			return false;
 		}
 	}
 	
 	public WebElement findPatientInTheList(String patientIdentifier, String list) {
         wait120Seconds.until(visibilityOfElementLocated(By.cssSelector("#" + list + " ." + patientIdentifier)));
         return driver.findElement(By.id(list)).findElement(By.className(patientIdentifier));
     }
 
     public void sendDossier(String dossieNumber) {
 		driver.findElement(By.name("mark-as-pulled-identifier")).sendKeys(dossieNumber);
 		driver.findElement(By.name("mark-as-pulled-identifier")).sendKeys(Keys.RETURN);
 	}
 
 	public void returnRecord(String dossieNumber) {
 		driver.findElement(By.id("tab-selector-return")).click();
 		driver.findElement(By.name("mark-as-returned-identifier")).sendKeys(dossieNumber);
 		driver.findElement(By.name("mark-as-returned-identifier")).sendKeys(Keys.RETURN);
 	}
 
     public void goToPullTab() {
         wait5seconds.until(visibilityOfElementLocated(pullRequestTabs));
         driver.findElement(pullRequestTabs).click();
     }
 
 	public String createRecord(String patientIdentifier) throws Exception {
 		findPatientInTheList(patientIdentifier, "create_requests_table").click();
 		clickOnAssign();
         wait5seconds.until(visibilityOfElementLocated(By.cssSelector("#assigned_create_requests_table ." + patientIdentifier)));
         return getDossierNumber(patientIdentifier);
 	}
 
     private void clickOnAssign() {
 		driver.findElement(By.id("assign-to-create-button")).click();
 	}
 
 	private String getDossierNumber(String patientIdentifier) throws Exception {
        return driver.findElement(By.className(patientIdentifier)).findElement(By.cssSelector("td:nth-child(2)")).getText();
 	}
 }
