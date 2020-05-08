 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 public class IdentificationSteps extends AbstractPageObject {
 
     
 	public IdentificationSteps(WebDriver driver) {
 		super(driver);
 	}
 	
 	public void setLocationAndChooseRegisterTask() {
 		driver.findElement(By.cssSelector("td.taskListItem")).click();
 	}
 
 	@Override
 	public void initialize() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 }
