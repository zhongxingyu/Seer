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
 
 package org.openmrs.module.mirebalais.smoke;
 
 import org.junit.Test;
 import org.openmrs.module.mirebalais.smoke.pageobjects.AppDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.CheckIn;
 import org.openmrs.module.mirebalais.smoke.pageobjects.IdentificationSteps;
 import org.openmrs.module.mirebalais.smoke.pageobjects.LoginPage;
 import org.openmrs.module.mirebalais.smoke.pageobjects.PatientDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.Registration;
 import org.openqa.selenium.By;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 public class ActiveVisitsTest extends BasicMirebalaisSmokeTest{
 
 	private CheckIn checkIn;
 	private static LoginPage loginPage;
 	private IdentificationSteps identificationSteps;
 	private Registration registration;
 	private PatientDashboard patientDashboard;
 	private AppDashboard appDashboard;
 	private String patientName;
 	private String patientIdentifier;
 	
 	
 	@Override
     public void specificSetUp() {
 		loginPage = new LoginPage(driver);
 		identificationSteps = new IdentificationSteps(driver);
 		registration = new Registration(driver);
 		patientDashboard = new PatientDashboard(driver);
 		checkIn = new CheckIn(driver);
 		appDashboard = new AppDashboard(driver);
 	}
 
 	
 	@Test
 	public void patientHasAnActiveVisiteWithoutPullingADossier() {
 		loginPage.logIn("admin", "Admin123");
 		appDashboard.openPatientRegistrationApp();
 		identificationSteps.setLocationAndChooseRegisterTask();
 		registration.goThruRegistrationProcessWithoutPrintingCard(); // TODO: transform it in a sql script
 		patientIdentifier = patientDashboard.getIdentifier();
 		patientName = patientDashboard.getName();

 		appDashboard.openActiveVisitsApp();
 		assertFalse(driver.findElement(By.id("content")).getText().contains(patientIdentifier));
     	
 		checkIn.setLocationAndChooseCheckInTask(patientIdentifier, patientName);
 		
 		appDashboard.openActiveVisitsApp();
 		assertTrue(driver.findElement(By.id("content")).getText().contains(patientName));
 		assertTrue(driver.findElement(By.id("content")).getText().contains(patientIdentifier));
 	}
 
 }
