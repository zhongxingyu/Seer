 package com.roosterpark.rptime.selenium.control.complex.form;
 
 import com.roosterpark.rptime.selenium.control.CheckBox;
 import com.roosterpark.rptime.selenium.control.TextField;
 import com.roosterpark.rptime.selenium.timer.WaitForVisible;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 /**
  * User: John
  * Date: 10/28/13
  * Time: 2:30 PM
  */
 public class CreateWorkerForm {
 
     private static final String FIRST_NAME = "firstName";
     private static final String LAST_NAME = "lastName";
     private static final String EMAIL = "email";
     private static final String START_DATE = "start";
     private static final String HOURLY = "hourly";
 
     private WebDriver driver;
 
     private TextField firstNameField;
     private TextField lastNameField;
     private TextField emailField;
     private TextField startDateField;
     private CheckBox hourlyCheckBox;
     private SaveButton saveButton;
     private CancelButton cancelButton;
 
     public CreateWorkerForm(WebDriver driver) {
         this.driver = driver;
         firstNameField = new TextField(driver, FIRST_NAME);
         lastNameField = new TextField(driver, LAST_NAME);
         emailField = new TextField(driver, EMAIL);
         startDateField = new TextField(driver, START_DATE);
         hourlyCheckBox = new CheckBox(driver, HOURLY);
         saveButton = new SaveButton(driver);
         cancelButton = new CancelButton(driver);
     }
 
     public WebDriver getDriver() {
         return driver;
     }
 
     public void enterFirstName(String firstName) {
         firstNameField.enterText(firstName);
     }
 
     public void clearFirstName() {
         firstNameField.clear();
     }
 
     public void enterLastName(String lastName) {
         lastNameField.enterText(lastName);
     }
 
     public void clearLastName() {
         lastNameField.clear();
     }
 
     public void enterEmail(String email) {
         emailField.enterText(email);
     }
 
     public void clearEmail() {
         emailField.clear();
     }
 
     public void enterStartDate(String startDate) {
         startDateField.enterText(startDate);
     }
 
     public void clearDate() {
         startDateField.clear();
     }
 
     public void clickSave() {
         saveButton.click();
     }
 
     public void clickCancel() {
         cancelButton.click();
     }
 
     public void checkHourly() {
         hourlyCheckBox.check();
     }
 
     public boolean isHourlyChecked() {
         return hourlyCheckBox.isChecked();
     }
 
     public void waitForRedraw() {
         WebElement createWorkerElement = driver.findElement(By.id("createWorker"));
         WaitForVisible waitForVisible = new WaitForVisible(createWorkerElement);
         waitForVisible.defaultWaitForVisible();
     }
 
     public boolean doesEmailFieldDefaultToEmail(String email) {
         String emailFieldText = emailField.getText().trim();
        return email.equals(email);
     }
 
 }
