 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 public class PatientRegistrationDashboard extends AbstractPageObject{
 
 	
 	public PatientRegistrationDashboard(WebDriver driver) {
 		super(driver);
 	}
 
 	public String getIdentifier() {
 		return driver.findElement(By.xpath("//*[@id='overviewTable']/tbody/tr/td[2]/table/tbody/tr[2]/td[1]")).getText();
 	}
 
 	public String getName() {
 		return driver.findElement(By.xpath("//*[@id='overviewTable']/tbody/tr/td[1]/table/tbody/tr[2]/td[1]")).getText();
 	}
 	
 	public String getGender() {
		return driver.findElement(By.xpath("//*[@id='overviewTable']/tbody/tr/td[1]/table/tbody/tr[4]/td[1]")).getText();
 	}
 
 	
 }
