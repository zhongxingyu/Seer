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
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.Select;
 
 import com.gargoylesoftware.htmlunit.ElementNotFoundException;
 
 import java.util.List;
 
 public class UserAdmin extends AbstractPageObject {
 	
 	private SysAdminPage adminPage;
 	
 	public UserAdmin(WebDriver driver) {
 		super(driver);
 		adminPage = new SysAdminPage(driver);
 	}
 
 	public boolean isAccountCreatedSuccessfully() {
		return driver.findElement(By.class("toast-item")).getText().contains("Saved account successfully");
 	}
 	
 	public void createClinicalAccount(String firstName, String lastName, String username, String password) {
 		createAccount(firstName, lastName, username, password);
 		chooseClinicalRole();
 	    clickOnSave();
 	}
 	
 	public void createRadiologyAccount(String firstName, String lastName, String username, String password) {
 		createAccount(firstName, lastName, username, password);
 		chooseRadiologyRole();
 	    clickOnSave();
 	}
 
 	public void createDataArchivesAccount(String firstName, String lastName, String username, String password) {
 		createAccount(firstName, lastName, username, password);
 		chooseDataArchivesRole();
 	    clickOnSave();
 	}
 	
 	public void createSysAdminAccount(String firstName, String lastName, String username, String password) {
 		createAccount(firstName, lastName, username, password);
 		chooseSysAdminRole();
 	    clickOnSave();
 	}
 	
 	public void createSysAdminAccount(String firstName, String lastName, String username, String password, String language) {
 		createAccount(firstName, lastName, username, password);
 		chooseSysAdminRole();
 		chooseLanguage(language);
 	    clickOnSave();
 	}
 	
 	private void chooseLanguage(String language) {
 		Select select = new Select(driver.findElement(By.name("defaultLocale")));
 		select.selectByVisibleText(language);
 	}
 
 	private void createAccount(String firstName, String lastName, String username, String password) {
 		fillBasicInfo(firstName, lastName);
 		fillUserAccountDetails(username, password);
 	}
 	
 	private void fillBasicInfo(String firstName, String lastName) {
 		adminPage.openManageAccounts();
 		driver.findElement(By.id("create-account-button")).click();
 		driver.findElement(By.name("givenName")).sendKeys(firstName);
 		driver.findElement(By.name("familyName")).sendKeys(lastName);
 	}
 	
 	private void fillUserAccountDetails(String username, String password) {
 		driver.findElement(By.id("createUserAccountButton")).click();
 		driver.findElement(By.name("username")).sendKeys(username);
 		driver.findElement(By.name("password")).sendKeys(password);
 		driver.findElement(By.name("confirmPassword")).sendKeys(password);
 
 		WebElement select = driver.findElement(By.name("privilegeLevel"));
 	    List<WebElement> options = select.findElements(By.tagName("option"));
 	    for (WebElement option : options) {
 	        if("Full".equals(option.getText()))
 	            option.click();
 	    }
 	}
 
 	private void clickOnSave() {
 		driver.findElement(By.id("save-button")).click();
 	}
 	
 	private void chooseClinicalRole() {
 		getRightRole("clinical").click();
 	}
 	
 	private void chooseDataArchivesRole() {
 		getRightRole("dataArchives").click();
 	}
 	
 	private void chooseRadiologyRole() {
 		getRightRole("radiology").click();
 	}
 	
 	private void chooseSysAdminRole() {
 		getRightRole("sysAdmin").click();
 	}
 
 	public void unlockUser(String username) {
 		adminPage.openManageAccounts();
 		this.getRightUserElement(username).click();
 		driver.findElement(By.id("unlock-button")).click();
 	}
 	
 	private WebElement getRightRole(String role) {
 		List<WebElement> options = driver.findElements(By.name("capabilities"));
 		for (WebElement element : options) {
 			if(element.getAttribute("value").contains(role)) {
 	            return element;
 	        }
 		}
 		throw new ElementNotFoundException(role, role, role);
 	}
 	
 	private WebElement getRightUserElement(String username) {
 		List<WebElement> options = driver.findElements(By.tagName("a"));
 		for (int i = options.size(); i > 0; i = i-1) {
 			WebElement option = options.get(i-1);
 	        if(option.getText().contains(username)) {
 	            return option;
 	        }
 		}
 		throw new ElementNotFoundException(username, username, username);
 	}
 
 	
 }
