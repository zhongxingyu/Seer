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
 
 package org.openmrs.module.mirebalais.smoke.pageobjects.forms;
 
 import org.openmrs.module.mirebalais.smoke.pageobjects.AbstractPageObject;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 public class ConsultNoteForm extends AbstractPageObject {
 
 	private static final String PRIMARY_DIAGNOSIS = "IGU";
 	private static final String DISPOSITION = "Discharge";
 	
 	public ConsultNoteForm(WebDriver driver) {
 		super(driver);
 	}
 
 	public void fillForm() throws Exception {
 		choosePrimaryDiagnosis();
 		chooseDisposition();
 		clickOn(By.cssSelector("#buttons .confirm"));
 	}
 
 	private void choosePrimaryDiagnosis() {
 		setClearTextToField("diagnosis-search", PRIMARY_DIAGNOSIS);
 		driver.findElement(By.cssSelector("strong.matched-name")).click();
 	}
 	
 	private void chooseDisposition() throws Exception {
		clickOnOptionLookingForText(DISPOSITION, By.cssSelector("#dispositions option"));	
 	}
 	
 }
