 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import java.util.List;
 
 import static org.openqa.selenium.Keys.ARROW_DOWN;
 import static org.openqa.selenium.Keys.RETURN;
 
 public class NewCheckIn extends AbstractPageObject {
 	
 	private static final String CONFIRM_TEXT = "Konfime";
 	
 	public NewCheckIn(WebDriver driver) {
 		super(driver);
 	}
 
 	public void checkInPatientFillingTheFormTwice(String patientIdentifier) throws Exception {
 		findPatient(patientIdentifier);
 		confirmRightPatient();
         selectFirstOptionFor("typeOfVisit");
         selectSecondOptionFor("paymentAmount");
         clickOnNoButton();
         selectSecondOptionFor("typeOfVisit");
         selectThirdOptionFor("paymentAmount");
 		confirmData();
 		confirmPopup();
 	}
 
     public void checkInpatientFillingWithScheduledAppointment(String patientIdentifier) throws Exception {
         findPatient(patientIdentifier);
         confirmRightPatient();
         selectSheduledAppointment("typeOfVisit");
         selectSecondOptionFor("service");
         selectAppointmentDate("apptDate");
         confirmDataForScheduleAppointment();
         confirmPopup();
     }
 	
 	private void findPatient(String patientIdentifier) throws Exception {
 		super.findPatientById(patientIdentifier, "patient-search-field-search");
 	}
 
 	private void confirmRightPatient() {
 		clickOn(By.className("icon-arrow-right"));
 	}
 
 	private void confirmData() {
 		clickOn(By.cssSelector("#confirmationQuestion .confirm"));
 	}
 
     private void confirmDataForScheduleAppointment() {
         clickOn(By.cssSelector("#confirmationQuestion .confirm"));
     }
 	
 	private void clickOnNoButton() {
 		clickOnConfirmationTab();
 		clickOn(By.cssSelector("#confirmationQuestion input.cancel"));
 	}
 	
 	private void clickOnConfirmationTab() {
 		List<WebElement> elements = driver.findElements(By.cssSelector("#formBreadcrumb span"));
 		for (WebElement element : elements) {
 	        if(element.getText().contains(CONFIRM_TEXT)) {
 	        	element.click();
 	        }
 	    }
 	}
 	
 	private void confirmPopup() {
         new WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.id("create-dossier-number-dialog")));
         if(driver.findElement(By.id("create-dossier-number-dialog")).isDisplayed()){
             clickOn(By.cssSelector("#create-dossier-number-dialog button"));
             return;
         }
         new WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.id("request-paper-record-dialog")));
         clickOn(By.cssSelector("#request-paper-record-dialog button"));
 	}
 
     private void selectFirstOptionFor(String spanId) {
         findSelectInsideSpan(spanId).sendKeys(ARROW_DOWN, RETURN);
     }
 
     private void selectSecondOptionFor(String spanId) {
         findSelectInsideSpan(spanId).sendKeys(ARROW_DOWN, ARROW_DOWN, RETURN);
     }
 
     private void selectThirdOptionFor(String spanId) {
         findSelectInsideSpan(spanId).sendKeys(ARROW_DOWN, ARROW_DOWN, ARROW_DOWN, RETURN);
     }
 
     private void selectAppointmentDate(String spandId){
       WebElement dataField =  findInputInsideSpan(spandId);
       dataField.click();
       dataField.sendKeys(RETURN, RETURN);
     }
 
 
     private WebElement findSelectInsideSpan(String spanId) {
         return driver.findElement(By.id(spanId)).findElement(By.tagName("select"));
     }
 
     private WebElement findInputInsideSpan(String spanId) {
         return driver.findElement(By.cssSelector("#" + spanId + " input"));
 
     }
 
 	public boolean isPatientSearchDisplayed() {
 		return driver.findElement(By.id("patient-search-field-search")).isDisplayed();
 	}
     private void selectSheduledAppointment(String spanId) {
         findSelectInsideSpan(spanId).sendKeys(ARROW_DOWN,ARROW_DOWN,ARROW_DOWN,ARROW_DOWN,ARROW_DOWN,ARROW_DOWN,RETURN);
     }
 	
 }	
