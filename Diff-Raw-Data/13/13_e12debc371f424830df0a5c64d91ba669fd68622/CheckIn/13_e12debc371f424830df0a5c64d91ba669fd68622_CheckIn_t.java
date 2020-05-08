 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.Wait;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 public class CheckIn extends AbstractPageObject {
 
 	private Wait<WebDriver> wait;
 	
 	public CheckIn(WebDriver driver) {
 		super(driver);
 		wait = new WebDriverWait(driver,2);
 	}
 
 	@Override
 	public void initialize() {
 		// TODO Auto-generated method stub
 	}
 	
 	public void setLocationAndChooseCheckInTask(String identifier, final String patientName) {
 		driver.get("http://bamboo.pih-emr.org:8080/mirebalais");
 		driver.findElement(By.linkText("Patient Registration")).click();
 		driver.findElement(By.xpath("//*[@id='taskDiv']/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr[2]/td[1]")).click();
 		
 		driver.findElement(By.id("patientIdentifier")).sendKeys(identifier);
 		clickNext();
 		
 		wait.until(new ExpectedCondition<Boolean>() {
 			public Boolean apply(WebDriver webDriver) {
 				return 	webDriver.findElement(By.id("okBtn")).isDisplayed() &&
 						webDriver.findElement(By.className("confirmExistingPatientModalList")).getText().contains(patientName) ;
 			}
 		});
 		driver.findElement(By.id("okBtn")).click();
 
 		
 		clickNext();
 		
 		clickNext();
 		chooseVisitReason(MEDICAL_CERTIFICATE);
 		clickNext();
 		choosePaymentAmount(PAYMENT_50);
 		enterReceiptNumber("123456789");
 		clickNext();
 
 		clickYellowCheckMark();
 		
 		wait.until(new ExpectedCondition<Boolean>() {
 			public Boolean apply(WebDriver webDriver) {
 				return webDriver.findElement(By.id("okDialog")).isDisplayed();
 			}
 		});
 		driver.findElement(By.id("okDialog")).click();
 	}
 	
 	public void enterReceiptNumber(String receiptNumber) {
         driver.findElement(By.id("receiptInput")).sendKeys(receiptNumber);
     }
 
     public void choosePaymentAmount(String paymentAmountElementId) {
         driver.findElement(By.id(paymentAmountElementId)).click();
     }
 
     public void chooseVisitReason(String visitReasonElementId) {
         driver.findElement(By.id(visitReasonElementId)).click();
     }
     
     public static final String MEDICAL_CERTIFICATE = "visitReasonStatus121";
     public static final String STANDART_DENTAL_VISIT = "visitReasonStatus243";
     public static final String MARRIAGE_CERTIFICATE = "visitReasonStatus135";
     public static final String STANDART_OUTPATIENT = "visitReasonStatus242";
     
     public static final String PAYMENT_0 = "paymentAmountStatus0";
     public static final String PAYMENT_50 = "paymentAmountStatus50";
     public static final String PAYMENT_100 = "paymentAmountStatus100";
     
 }
