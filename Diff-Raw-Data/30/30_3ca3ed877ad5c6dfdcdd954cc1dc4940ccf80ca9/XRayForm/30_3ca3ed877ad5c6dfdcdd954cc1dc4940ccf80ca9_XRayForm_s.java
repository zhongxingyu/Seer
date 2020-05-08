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
 
 public class XRayForm extends AbstractPageObject {
 
 	public XRayForm(WebDriver driver) {
 		super(driver);
 	}
 
	public void fillForm(String study1, String study2) {
		driver.findElement(By.name("clinicalHistory")).sendKeys("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc eu neque ut mi auctor pulvinar. Mauris in orci non sem consequat posuere.");
 		setClearTextToField("study-search", study1);
 		clickOn(By.linkText(study1));
 		
 		setClearTextToField("study-search", study2);
 		clickOn(By.linkText(study2));
 
 		clickOn(By.id("next"));
 	}
 }
