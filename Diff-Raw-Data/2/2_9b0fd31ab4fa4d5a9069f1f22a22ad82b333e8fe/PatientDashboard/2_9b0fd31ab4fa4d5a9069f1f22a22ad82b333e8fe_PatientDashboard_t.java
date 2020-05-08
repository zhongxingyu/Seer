 /*
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openmrs.module.mirebalais.smoke.helper.Waiter;
 import org.openmrs.module.mirebalais.smoke.pageobjects.forms.ConsultNoteForm;
 import org.openmrs.module.mirebalais.smoke.pageobjects.forms.SurgicalPostOperativeNoteForm;
 import org.openmrs.module.mirebalais.smoke.pageobjects.forms.XRayForm;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 public class PatientDashboard extends AbstractPageObject {
 
 	public static final String CHECKIN = "Tcheke"; 
 	public static final String CONSULTATION = "Consultation";
 	public static final String VITALS = "Siy Vito";
 	public static final String RADIOLOGY = "Preskripsyon Radyoloji";
 	
 	public static final String ACTIVE_VISIT_MESSAGE = "Vizit aktiv";
 	public static final String ADMISSION = "Admisyon";
 	public static final String TRANSFER = "Transfè";
 	
 	private ConsultNoteForm consultNoteForm;
 	private SurgicalPostOperativeNoteForm surgicalPostOperativeNoteForm;
 	private XRayForm xRayForm;
 	
 	private HashMap<String, By> formList;
 	
 	public PatientDashboard(WebDriver driver) {
 		super(driver);
 		consultNoteForm = new ConsultNoteForm(driver);
 		surgicalPostOperativeNoteForm = new SurgicalPostOperativeNoteForm(driver);
 		xRayForm = new XRayForm(driver);
 		createFormsMap();
 	}
 
 	public void orderXRay(String study1, String study2) {
 		clickOn(By.className("icon-x-ray"));
 		xRayForm.fillForm(study1, study2);
 	}
 
 	public boolean hasOrder(String orderDetails) {
 		return driver.findElement(By.id("content")).getText().contains(orderDetails);
 	}
 
 	public Set<String> getIdentifiers() {
 		String temp = driver.findElement(By.className("identifiers")).getText().trim();
 		StringTokenizer st = new StringTokenizer(temp.substring(3), ",");
 		Set<String> result = new HashSet<String>();
 		while(st.hasMoreTokens()) {
 			result.add(st.nextToken());
 		}
 		return result;
 	}
 
 	public boolean verifyIfSuccessfulMessageIsDisplayed(){
         return (driver.findElement(By.className("icon-ok")) != null);
     }
     
     public boolean hasActiveVisit() {
		return driver.findElement(By.id("visit-details")).getText().contains(ACTIVE_VISIT_MESSAGE);
 	}
 
 	public void deleteEncounter(String encounterName) throws Exception {
 		String encounterId = findEncounterId(encounterName);
 		List<WebElement> encounters = driver.findElements(By.cssSelector("i.deleteEncounterId"));
 		for (WebElement encounter : encounters) {
 	        if (encounter.getAttribute("data-encounter-id").equals(encounterId))
 	        	encounter.click();
 	    }
 		
 		clickOn(By.xpath("//*[@id='delete-encounter-dialog']/div[2]/button[1]"));
 	}
 	
 	public String findEncounterId(String encounterName) throws Exception {
 		try {		
 			return getOptionBasedOnText(encounterName, By.cssSelector("span.encounter-name")).getAttribute("data-encounter-id");
 		} catch (Exception e) {
 			throw new Exception("No encounter of this type found.");
 		}
 	}
 
 	public Integer countEncouters(String encounterName) {
 		int count = 0;
 		List<WebElement> encounters = driver.findElements(By.cssSelector("span.encounter-name"));
 		for (WebElement encounter : encounters) {
 	        if (encounter.getText().equals(encounterName))
 	        	count++;
 	    }
 		return count;
 	}
 
 	public void startVisit() {
 		hoverOn(By.cssSelector(".actions"));
 		clickOn(By.cssSelector("i.icon-check-in"));
 		clickOn(By.cssSelector("#quick-visit-creation-dialog .confirm"));
 	}
 	
 	public void addConsultNoteWithDischarge() throws Exception {
 		openForm(formList.get("Consult Note"));
 		consultNoteForm.fillFormWithDischarge();
 	}
 	
 	public String addConsultNoteWithAdmission() throws Exception {
 		openForm(formList.get("Consult Note"));
 		return consultNoteForm.fillFormWithAdmissionAndReturnPlace();
 	}
 
 	public String addConsultNoteWithTransfer() throws Exception {
 		openForm(formList.get("Consult Note"));
 		return consultNoteForm.fillFormWithTransferAndReturnPlace();
 	}
 	
 	public void addConsultNoteWithDeath() throws Exception {
 		openForm(formList.get("Consult Note"));
 		consultNoteForm.fillFormWithDeath();
 	}
 	
 	public void addSurgicalNote() throws Exception {
 		openForm(formList.get("Surgical Note"));
 		surgicalPostOperativeNoteForm.fillBasicForm();
 	}
 	
 	public void openForm(By formIdentification) {
 		clickOn(formIdentification);
 	}
 
 	public void requestRecord() {
 		hoverOn(By.cssSelector(".actions"));
 		clickOn(By.cssSelector("i.icon-folder-open"));
 		clickOn(By.cssSelector("#request-paper-record-dialog .confirm"));
 	}
 
 	public String getDossieNumber() {
 		List<WebElement> elements = driver.findElements(By.cssSelector(".identifiers span"));
 		return elements.get(1).getText();
 	}
 
 	public boolean canRequestRecord() {
 		hoverOn(By.cssSelector(".actions"));
 		return driver.findElement(By.className("icon-folder-open")).isDisplayed();
 	}
 
 	private void createFormsMap() {
 		formList = new HashMap<String, By>();
 		formList.put("Consult Note", By.cssSelector("#visit-details a:nth-child(2) .icon-stethoscope"));
 		formList.put("Surgical Note", By.cssSelector("#visit-details .icon-paste"));
 	}
 
 	public Boolean showStartVisitButton() {
 		try {
         	Waiter.waitForElementToDisplay(By.cssSelector("#noVisitShowVisitCreationDialog"), 10, driver);
         	return true;
         } catch (Exception e) {
         	e.printStackTrace();
         	return false;
         }
 	}
 }
