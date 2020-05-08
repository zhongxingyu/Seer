 package com.roosterpark.rptime.selenium.control.complex.form;
 
 import com.roosterpark.rptime.selenium.control.Button;
 import com.roosterpark.rptime.selenium.control.CheckBox;
 import com.roosterpark.rptime.selenium.control.DropDownList;
 import com.roosterpark.rptime.selenium.control.TextField;
 import com.roosterpark.rptime.selenium.timer.WaitForVisible;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 /**
  * User: John
  * Date: 11/13/13
  * Time: 10:46 AM
  */
 public class CreateContractForm {
 
     private static final String CHECKBOX_ID = "onSite";
     private static final String START_DATE_ID = "startDate";
     private static final String END_DATE_ID = "endDate";
 
     private WebDriver driver;
     private SaveButton saveButton;
     private CancelButton cancelButton;
     private CheckBox onSite;
     private TextField startDate;
     private TextField endDate;
     private WorkerSelect workerSelect;
     private ClientSelect clientSelect;
 
     public CreateContractForm(WebDriver driver) {
         this.driver = driver;
         saveButton = new SaveButton(driver);
         cancelButton = new CancelButton(driver);
         onSite = new CheckBox(driver, CHECKBOX_ID);
         startDate = new TextField(driver, START_DATE_ID);
         endDate = new TextField(driver, END_DATE_ID);
         workerSelect = new WorkerSelect(driver);
         clientSelect = new ClientSelect(driver);
     }
 
     public WebDriver getDriver() {
         return driver;
     }
 
     public void clickSave() {
         saveButton.click();
     }
 
     public void clickCancel() {
         cancelButton.click();
     }
 
     public void checkOnSite() {
         onSite.check();
     }
 
     public boolean isOnSiteChecked() {
         return onSite.isChecked();
     }
 
     public void selectWorker(String name) {
         workerSelect.selectByName(name);
     }
 
     public void selectClient(String name) {
         clientSelect.selectByName(name);
     }
 
     public void enterStartDate(String date) {
         startDate.enterText(date);
     }
 
     public void clearStartDate() {
         startDate.clear();
     }
 
     public void enterEndDate(String date) {
         endDate.enterText(date);
     }
 
     public void clearEndDate() {
         endDate.clear();
     }
 
    public String getStartDate() {
        return startDate.getText();
    }

    public String getEndDate() {
        return endDate.getText();
    }

     public void waitForCreateContractRedraw() {
         WebElement createContractDiv = getDriver().findElement(By.id("createContract"));
         WaitForVisible waitForVisible = new WaitForVisible(createContractDiv);
         waitForVisible.defaultWaitForVisible();
     }
 
     private class WorkerSelect extends DropDownList {
 
         private static final String ID = "worker";
 
         public WorkerSelect(WebDriver driver) {
             super(driver, ID);
         }
 
         public void selectByName(String name) {
             getSelect().selectByVisibleText(name);
         }
 
     }
 
     private class ClientSelect extends DropDownList {
 
         private static final String ID = "client";
 
         public ClientSelect(WebDriver driver) {
             super(driver, ID);
         }
 
         public void selectByName(String name) {
             getSelect().selectByVisibleText(name);
         }
 
     }
 
 }
