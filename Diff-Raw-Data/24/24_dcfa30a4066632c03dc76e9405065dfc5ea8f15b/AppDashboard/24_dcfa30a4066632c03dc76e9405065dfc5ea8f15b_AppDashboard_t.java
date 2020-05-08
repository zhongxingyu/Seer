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
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 public class AppDashboard extends AbstractPageObject {
 
    public static final String PATIENT_REGISTRATION_AND_CHECK_IN = "Patient Registration and Check-In";
    public static final String ARCHIVES = "Archives";

    public void openActiveVisitsApp() {
 		driver.get(properties.getWebAppUrl());
 		driver.findElement(By.linkText("Active Visits")).click();
 	}
 	
 	public AppDashboard(WebDriver driver) {
 		super(driver);
 	}
 
 	public void openArchivesRoomApp() {
 		driver.get(properties.getWebAppUrl());
		driver.findElement(By.linkText(ARCHIVES)).click();
 	}
 
 	public void openPatientRegistrationApp() {
 		driver.get(properties.getWebAppUrl());
		driver.findElement(By.linkText(PATIENT_REGISTRATION_AND_CHECK_IN)).click();
 	}
 
 	public void openSysAdminApp() {
 		driver.get(properties.getWebAppUrl());
 		driver.findElement(By.linkText("System Administration")).click();
 	}
 	
 	public boolean isPatientRegistrationAppPresented() {
 		return driver.findElement(By.id("apps")).getText().contains("Patient Registration");
 	}
 	
 	public boolean isArchivesRoomAppPresented() {
		return driver.findElement(By.id("apps")).getText().contains(ARCHIVES);
 	}
 	
 	public boolean isSystemAdministrationAppPresented() {
 		return driver.findElement(By.id("apps")).getText().contains("System Administration");
 	}
 	
 	public boolean isFindAPatientAppPresented() {
 		return driver.findElement(By.id("apps")).getText().contains("Find a Patient");
 	}
 	
 	public boolean isActiveVisitsAppPresented() {
 		return driver.findElement(By.id("apps")).getText().contains("Active Visits");
 	}
 }
