 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import java.util.List;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 public class MergeFlow extends AbstractPageObject {
 
 	
 	public MergeFlow(WebDriver driver) {
 		super(driver);
 	}
 
 	public void setFirstPatient(String patientData) {
 		setClearTextToField("choose-first-search", patientData);
 		clickOnTheRightName("/html/body/ul[1]/li", patientData);
 	}
 	
 	public void setSecondPatient(String patientData) {
 		setClearTextToField("choose-second-search", patientData);
 		clickOnTheRightName("/html/body/ul[2]/li", patientData);
 	}
 
 	public void setPatientsToMerge(String patientDataOne, String patientDataTwo) {
 		setFirstPatient(patientDataOne);
 		setSecondPatient(patientDataTwo);
 		
 		clickOnContinueMergeButton();
 		clickOnLeftPatient();
 		clickOnContinueMergeButton(); // Yes, the button is the same, it just changes the caption!
 	}
 
 	private void clickOnTheRightName(String xpath, String patientData) {
 		List<WebElement> options = driver.findElements(By.xpath(xpath));
 		for(WebElement option: options){
 		    if(option.getText().contains(patientData)) {
 		    	option.click();
 		    }               
 		}
 	}
 
 	private void clickOnLeftPatient() {
		driver.findElement(By.className("first-patient")).click();
 	}
 
 	private void clickOnContinueMergeButton() {
 		driver.findElement(By.className("confirm")).click();
 	}
 	
 }
