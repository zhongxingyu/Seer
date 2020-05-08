 package com.roosterpark.rptime.selenium.page;
 
 import com.roosterpark.rptime.selenium.BasicPageObject;
 import com.roosterpark.rptime.selenium.control.Button;
 import com.roosterpark.rptime.selenium.control.complex.timesheet.TimeSheetForm;
 import com.roosterpark.rptime.selenium.exception.NotDirectlyOpenableException;
 import com.roosterpark.rptime.selenium.page.popup.TimeSheetPopup;
 import com.roosterpark.rptime.selenium.timer.WaitForVisible;
 import org.openqa.selenium.WebDriver;
 
 /**
  * User: John
  * Date: 11/21/13
  * Time: 8:44 AM
  */
 public class UserTimeSheetPage extends BasicPageObject {
 
     private TimeSheetForm timeSheetForm;
 
     public UserTimeSheetPage(WebDriver driver) {
         super(driver);
     }
 
     @Override
     public void openPage() {
         throw new NotDirectlyOpenableException("User Time Sheet page should not be opened directly.");
     }
 
     public void initializeTimeSheetForm() {
         timeSheetForm = new TimeSheetForm(getWebDriver());
         timeSheetForm.initialize();
     }
 
     public TimeSheetForm getTimeSheetForm() {
         return timeSheetForm;
     }
 
     public void clickCreateButton() {
         CreateButton createButton = new CreateButton(getWebDriver());
         createButton.click();
     }
 
     public TimeSheetPopup clickDropDownButton() {
         DropDownButton dropDownButton = new DropDownButton(getWebDriver());
         return dropDownButton.click();
     }
 
     private class CreateButton extends Button<Void> {
 
         private static final String ID = "new";
 
         public CreateButton(WebDriver driver) {
             super(driver, ID);
         }
 
         @Override
         public Void click() {
             WaitForVisible waitForVisible = new WaitForVisible(getElement());
             waitForVisible.defaultWaitForVisible();
             getElement().click();
             return null;
         }
     }
 
     private class DropDownButton extends Button<TimeSheetPopup> {
 
        private static final String ID = "toggle-create-dropdown";
 
         public DropDownButton(WebDriver driver) {
            super(driver, ID) ;
         }
 
         @Override
         public TimeSheetPopup click() {
             WaitForVisible waitForVisible = new WaitForVisible(getElement());
             waitForVisible.defaultWaitForVisible();
             getElement().click();
            return new TimeSheetPopup(getDriver());
         }
     }
 
 }
