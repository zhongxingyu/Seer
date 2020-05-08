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
 
 import org.openmrs.module.mirebalais.smoke.pageobjects.forms.ConsultNoteForm;
 import org.openmrs.module.mirebalais.smoke.pageobjects.forms.DispenseMedicationForm;
 import org.openmrs.module.mirebalais.smoke.pageobjects.forms.EditEncounterForm;
 import org.openmrs.module.mirebalais.smoke.pageobjects.forms.EmergencyDepartmentNoteForm;
 import org.openmrs.module.mirebalais.smoke.pageobjects.forms.RetroConsultNoteForm;
 import org.openmrs.module.mirebalais.smoke.pageobjects.forms.XRayForm;
 import org.openqa.selenium.By;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.TimeoutException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 
 import java.util.HashMap;
 import java.util.List;
 
 import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
 import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
 import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
 
 public class PatientDashboard extends AbstractPageObject {
 	
 	public static final String CHECKIN = "Tcheke";
 	
 	public static final String CONSULTATION = "Konsiltasyon";
 	
 	public static final String VITALS = "Siy Vito";
 	
 	public static final String RADIOLOGY = "Preskripsyon Radyoloji";
 	
 	public static final String ACTIVE_VISIT_MESSAGE = "Vizit aktiv";
 	
 	public static final String ADMISSION = "Admisyon";
 	
 	public static final String TRANSFER = "Transfè";
 	
 	private final By dispensingForm = By.cssSelector(".encounter-summary-container .dispensing-form");
 	
 	private final By medications = By.className("medication");
 	
 	private ConsultNoteForm consultNoteForm;
 	
 	private EmergencyDepartmentNoteForm eDNoteForm;
 	
 	private RetroConsultNoteForm retroConsultNoteForm;
 	
 	private XRayForm xRayForm;
 	
 	private HashMap<String, By> formList;
 	
 	private By deleteEncounter = By.cssSelector("i.deleteEncounterId");
 	
 	private By confirmDeleteEncounter = By.cssSelector("#delete-encounter-dialog .confirm");
 	
 	private By actions = By.cssSelector(".actions");
 	
 	private By checkIn = By.cssSelector("a i.icon-check-in");
 	
 	private By confirmStartVisit = By.cssSelector("#quick-visit-creation-dialog .confirm");
 	
 	private By firstPencilIcon = By.cssSelector("#encountersList span i:nth-child(1)");
 	
 	private By dispenseMedicationButton = By.id("dispensing.dashboardAction");
 	
 	private By firstEncounterDetails = By.className("details-action");
 	
 	private ExpectedCondition<WebElement> detailsAjaxCallReturns = visibilityOfElementLocated(dispensingForm);
 	
 	public PatientDashboard(WebDriver driver) {
 		super(driver);
 		consultNoteForm = new ConsultNoteForm(driver);
 		eDNoteForm = new EmergencyDepartmentNoteForm(driver);
 		retroConsultNoteForm = new RetroConsultNoteForm(driver);
 		xRayForm = new XRayForm(driver);
 		createFormsMap();
 	}
 	
 	public void orderXRay(String study1, String study2) throws Exception {
 		openForm(formList.get("Order X-Ray"));
 		xRayForm.fillForm(study1, study2);
 	}
 	
 	public boolean verifyIfSuccessfulMessageIsDisplayed() {
 		return (driver.findElement(By.className("icon-ok")) != null);
 	}
 	
 	public boolean hasActiveVisit() {
 		return driver.findElement(By.id("visit-details")).getText().contains(ACTIVE_VISIT_MESSAGE);
 	}
 	
 	public void deleteFirstEncounter() {
 		clickOn(deleteEncounter);
 		clickOn(confirmDeleteEncounter);
 		
 		wait5seconds.until(invisibilityOfElementLocated(By.id("delete-encounter-dialog")));
 	}
 	
 	public Integer countEncountersOfType(String encounterName) {
 
         try {
            wait5seconds.until(presenceOfElementLocated(By.id("span.encounter-name")));
         }
         catch (TimeoutException e) {
             return 0;
         }
 
 		int count = 0;
 		List<WebElement> encounters = driver.findElements(By.cssSelector("span.encounter-name"));
 		for (WebElement encounter : encounters) {
 			if (encounter.getText().equals(encounterName))
 				count++;
 		}
 		return count;
 	}
 	
 	public void startVisit() {
 		hoverOn(actions);
 		clickOn(checkIn);
 		clickOn(confirmStartVisit);
 		
 		wait5seconds.until(visibilityOfElementLocated(By.cssSelector(".visit-actions.active-visit")));
 	}
 	
 	public void addConsultNoteWithDischarge(String primaryDiagnosis) throws Exception {
 		openForm(formList.get("Consult Note"));
 		consultNoteForm.fillFormWithDischarge(primaryDiagnosis);
 	}
 	
 	public String addConsultNoteWithAdmissionToLocation(String primaryDiagnosis, int numbered) throws Exception {
 		openForm(formList.get("Consult Note"));
 		return consultNoteForm.fillFormWithAdmissionAndReturnLocation(primaryDiagnosis, numbered);
 	}
 	
 	public String addConsultNoteWithTransferToLocation(String primaryDiagnosis, int numbered) throws Exception {
 		openForm(formList.get("Consult Note"));
 		return consultNoteForm.fillFormWithTransferAndReturnLocation(primaryDiagnosis, numbered);
 	}
 	
 	public void addConsultNoteWithDeath(String primaryDiagnosis) throws Exception {
 		openForm(formList.get("Consult Note"));
 		consultNoteForm.fillFormWithDeath(primaryDiagnosis);
 	}
 	
 	public void editExistingConsultNote(String primaryDiagnosis) throws Exception {
 		openForm(By.cssSelector(".consult-encounter-template .editEncounter"));
 		consultNoteForm.editPrimaryDiagnosis(primaryDiagnosis);
 	}
 	
 	public void addRetroConsultNoteWithDischarge(String primaryDiagnosis) throws Exception {
 		openForm(formList.get("Consult Note"));
 		retroConsultNoteForm.fillFormWithDischarge(primaryDiagnosis);
 	}
 	
 	public String addRetroConsultNoteWithAdmissionToLocation(String primaryDiagnosis, int numbered) throws Exception {
 		openForm(formList.get("Consult Note"));
 		return retroConsultNoteForm.fillFormWithAdmissionAndReturnLocation(primaryDiagnosis, numbered);
 	}
 	
 	public String addRetroConsultNoteWithTransferToLocation(String primaryDiagnosis, int numbered) throws Exception {
 		openForm(formList.get("Consult Note"));
 		return retroConsultNoteForm.fillFormWithTransferAndReturnLocation(primaryDiagnosis, numbered);
 	}
 	
 	public void addEmergencyDepartmentNote(String primaryDiagnosis) throws Exception {
 		openForm(formList.get("ED Note"));
 		eDNoteForm.fillFormWithDischarge(primaryDiagnosis);
 	}
 	
 	public void editExistingEDNote(String primaryDiagnosis) throws Exception {
 		openForm(By.cssSelector(".consult-encounter-template .editEncounter"));
 		eDNoteForm.editPrimaryDiagnosis(primaryDiagnosis);
 	}
 	
 	public void openForm(By formIdentification) {
 		clickOn(formIdentification);
 	}
 	
 	public void viewConsultationDetails() {
 		clickOn(By.cssSelector(".consult-encounter-template .show-details"));
 	}
 	
 	public void requestRecord() {
 		hoverOn(By.cssSelector(".actions"));
 		clickOn(By.cssSelector(".actions i.icon-folder-open"));
 		clickOn(By.cssSelector("#request-paper-record-dialog .confirm"));
 	}
 	
 	public String getDossierNumber() {
 		List<WebElement> elements = driver.findElements(By.cssSelector(".identifiers span"));
 		return elements.get(1).getText();
 	}
 	
 	public boolean canRequestRecord() {
 		hoverOn(By.cssSelector(".actions"));
 		return driver.findElement(By.cssSelector(".actions i.icon-folder-open")).isDisplayed();
 	}
 	
 	public Boolean startVisitButtonIsVisible() {
 		try {
 			driver.findElement(By.id("noVisitShowVisitCreationDialog"));
 			return true;
 		}
 		catch (NoSuchElementException e) {
 			return false;
 		}
 	}
 	
 	public Boolean containsText(String text) {
 		return driver.getPageSource().contains(text);
 	}
 	
 	public List<WebElement> getVisits() {
 		return driver.findElements(By.cssSelector(".menu-item.viewVisitDetails"));
 	}
 	
 	public boolean isDead() {
 		try {
 			driver.findElement(By.className("death-message"));
 			return true;
 		}
 		catch (NoSuchElementException e) {
 			return false;
 		}
 	}
 	
 	public String providerForFirstEncounter() {
 		return driver.findElement(By.className("encounter-details")).findElement(By.className("provider")).getText();
 	}
 	
 	public String locationForFirstEncounter() {
 		return driver.findElement(By.className("encounter-details")).findElement(By.className("location")).getText();
 	}
 	
 	public EditEncounterForm.SelectedProviderAndLocation editFirstEncounter(int providerOption, int locationOption) {
 		EditEncounterForm editEncounterForm = editEncounter();
 		
 		editEncounterForm.selectProvider(providerOption);
 		editEncounterForm.selectLocation(locationOption);
 		
 		return editEncounterForm.selectedProviderAndLocation();
 	}
 	
 	public boolean canDispenseMedication() {
 		try {
 			driver.findElement(dispenseMedicationButton);
 			return true;
 		}
 		catch (NoSuchElementException e) {
 			return false;
 		}
 	}
 	
 	public DispenseMedicationForm goToDispenseMedicationForm() {
 		driver.findElement(dispenseMedicationButton).click();
 		return new DispenseMedicationForm(driver);
 	}
 	
 	public void clickFirstEncounterDetails() {
 		driver.findElement(firstEncounterDetails).click();
 		wait5seconds.until(detailsAjaxCallReturns);
 	}
 	
 	private EditEncounterForm editEncounter() {
 		driver.findElement(firstPencilIcon).click();
 		return new EditEncounterForm(driver);
 	}
 	
 	private void createFormsMap() {
 		formList = new HashMap<String, By>();
 		formList.put("Consult Note", By.id("mirebalais.consult"));
 		formList.put("Surgical Note", By.id("mirebalais.surgicalOperativeNote"));
 		formList.put("Order X-Ray", By.id("org.openmrs.module.radiologyapp.orderXray"));
 		formList.put("ED Note", By.id("mirebalais.edConsult"));
 	}
 	
 	public MedicationDispensed firstMedication() {
 		WebElement first = driver.findElement(dispensingForm).findElements(medications).get(0);
 		return new MedicationDispensed(first, 1);
 	}
 	
 	public class MedicationDispensed {
 		
 		private WebElement medication;
 		
 		private int order;
 		
 		public MedicationDispensed(WebElement medication, int order) {
 			this.medication = medication;
 			this.order = order;
 		}
 		
 		public String getName() {
 			return medication.findElement(name()).getText();
 		}
 		
 		public String getFrequency() {
 			return medication.findElement(frequency()).getText();
 		}
 		
 		public String getDose() {
 			return medication.findElement(dose()).getText();
 		}
 		
 		public String getDoseUnit() {
 			return medication.findElement(doseUnit()).getText();
 		}
 		
 		public String getDuration() {
 			return medication.findElement(duration()).getText();
 		}
 		
 		public String getDurationUnit() {
 			return medication.findElement(durationUnit()).getText();
 		}
 		
 		public String getAmount() {
 			return medication.findElement(amount()).getText();
 		}
 		
 		private By name() {
 			return By.cssSelector("#name" + order);
 		}
 		
 		private By frequency() {
 			return By.cssSelector("#frequency" + order);
 		}
 		
 		private By dose() {
 			return By.cssSelector("#dose" + order);
 		}
 		
 		private By doseUnit() {
 			return By.cssSelector("#doseUnit" + order);
 		}
 		
 		private By duration() {
 			return By.cssSelector("#duration" + order);
 		}
 		
 		private By durationUnit() {
 			return By.cssSelector("#durationUnit" + order);
 		}
 		
 		private By amount() {
 			return By.cssSelector("#amount" + order);
 		}
 		
 	}
 }
