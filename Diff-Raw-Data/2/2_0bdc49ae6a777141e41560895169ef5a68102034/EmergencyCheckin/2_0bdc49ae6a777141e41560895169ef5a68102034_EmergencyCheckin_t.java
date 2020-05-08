 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 public class EmergencyCheckin extends AbstractPageObject {
 
 	private Registration registration;
 	
     public EmergencyCheckin(WebDriver driver) {
         super(driver);
     }
 
   
 
     public void checkinMaleUnindentifiedPatient() {
     	gotoPage("/module/patientregistration/workflow/selectLocationAndService.form");
        driver.findElement(By.xpath("//*[@id='taskDiv']/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr[3]/td[1]")).click();
         registration = new Registration(driver);
     	
         driver.findElement(By.id("registerJdBtn")).click();
 
         registration.enterSexData();
     }
 
 
 }
