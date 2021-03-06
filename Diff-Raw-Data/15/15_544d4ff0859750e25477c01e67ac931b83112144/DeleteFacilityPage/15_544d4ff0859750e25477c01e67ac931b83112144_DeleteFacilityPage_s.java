 /*
  * This program is part of the OpenLMIS logistics management information system platform software.
  * Copyright © 2013 VillageReach
  *
  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
  */
 
 package org.openlmis.pageobjects;
 
 
 import com.thoughtworks.selenium.SeleneseTestNgHelper;
 import org.openlmis.UiUtils.TestWebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.How;
 import org.openqa.selenium.support.PageFactory;
 import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
 
 import java.io.IOException;
 
 
 public class DeleteFacilityPage extends Page {
 
 
   @FindBy(how = How.ID, using = "searchFacility")
   private static WebElement searchFacilityTextField;
 
   @FindBy(how = How.XPATH, using = "//div[@class='facility-list']/ul/li/a")
   private static WebElement facilityList;
 
   @FindBy(how = How.XPATH, using = "//input[@class='btn btn-danger delete-button']")
   private static WebElement disableButton;
 
   @FindBy(how = How.LINK_TEXT, using = "OK")
   private static WebElement okButton;
 
   @FindBy(how = How.XPATH, using = "//div[@id='disableFacilityDialog']/div[@class='modal-body']/p")
   private static WebElement disableMessageOnAlert;
 
   @FindBy(how = How.XPATH, using = "//a[@ng-click='disableFacility()']")
   private static WebElement deteteButtonOnAlert;
 
   @FindBy(how = How.XPATH, using = "//div[@id='saveSuccessMsgDiv']/span")
   private static WebElement messageDiv;
 
   @FindBy(how = How.ID, using = "saveSuccessMsgDiv")
   private static WebElement successMessageDiv;
 
   @FindBy(how = How.ID, using = "saveErrorMsgDiv")
   private static WebElement saveErrorMsgDiv;
 
 
   @FindBy(how = How.XPATH, using = "//ng-switch/span")
   private static WebElement enabledFlag;
 
   @FindBy(how = How.XPATH, using = "//input[@name='isActive' and @value='false']")
   private static WebElement isActiveRadioNoOption;
 
   @FindBy(how = How.XPATH, using = "//input[@name='isActive' and @value='true']")
   private static WebElement isActiveRadioYesOption;
 
   @FindBy(how = How.XPATH, using = "//input[@class='btn btn-primary enable-button']")
   private static WebElement enableButton;
 
   @FindBy(how = How.XPATH, using = "//div[@id='enableConfirmModal']/div[@class='modal-body']/p")
   private static WebElement enableMessageOnAlert;
 
   @FindBy(how = How.LINK_TEXT, using = "OK")
   private static WebElement okLink;
 
   @FindBy(how = How.XPATH, using = " //div[@id='activeConfirmModel']/div[@class='modal-body']/p")
   private static WebElement isActiveMessageOnAlert;
 
   @FindBy(how = How.LINK_TEXT, using = "Yes")
   private static WebElement yesLink;
 
   @FindBy(how = How.ID, using = "catchment-population")
   private static WebElement catchmentPopulation;
 
   @FindBy(how = How.ID, using = "latitude")
   private static WebElement latitude;
 
   @FindBy(how = How.ID, using = "longitude")
   private static WebElement longitude;
 
   @FindBy(how = How.ID, using = "altitude")
   private static WebElement altitude;
 
 
   @FindBy(how = How.ID, using = "code")
   private static WebElement facilityCode;
 
   @FindBy(how = How.ID, using = "save-button")
   private static WebElement SaveButton;
 
   @FindBy(how = How.ID, using = "edit-facility-header")
   private static WebElement facilityHeader;
 
   @FindBy(how = How.ID, using = "programs-supported")
   public static WebElement programsSupported;
 
   @FindBy(how = How.ID, using = "supported-program-active")
   private static WebElement programsSupportedActiveFlag;
 
   @FindBy(how = How.ID, using = "supported-program-start-date")
   private static WebElement programsSupportedStartDate;
 
   @FindBy(how = How.XPATH, using = "//a[contains(text(),'25')]")
   private static WebElement startDateCalender;
 
   @FindBy(how = How.ID, using = "button_OK")
   private static WebElement startDateAlert;
 
   @FindBy(how = How.ID, using = "supported-program-add")
   private static WebElement addSupportedProgram;
 
   @FindBy(how = How.ID, using = "remove0")
   private static WebElement removeSupportedProgram;
 
 
   public DeleteFacilityPage(TestWebDriver driver) throws IOException {
     super(driver);
     PageFactory.initElements(new AjaxElementLocatorFactory(TestWebDriver.getDriver(), 10), this);
     testWebDriver.setImplicitWait(10);
 
   }
 
   public void saveFacility(){
     testWebDriver.waitForElementToAppear(SaveButton);
     SaveButton.click();
   }
 
 
   public void searchFacility(String facilityCodeValue) {
 
     testWebDriver.waitForElementToAppear(searchFacilityTextField);
     sendKeys(searchFacilityTextField, facilityCodeValue);
     testWebDriver.sleep(2000);
   }
 
   public void clickFacilityList(String date_time) {
     testWebDriver.sleep(2000);
     testWebDriver.waitForElementToAppear(testWebDriver.getElementByXpath("//a[contains(text(),'"+date_time+"')]"));
     testWebDriver.getElementByXpath("//a[contains(text(),'"+date_time+"')]").click();
     testWebDriver.waitForElementToAppear(facilityHeader);
   }
 
 
   public void disableFacility(String facilityCodeValue, String facilityNameValue) {
 
     String expectedMessageOnAlert = "''" + facilityNameValue + "'' / ''" + facilityCodeValue + "'' will be disabled in the system.";
     verifyHeader("Edit facility");
     clickDisableButtonOnFacilityScreen();
     verifyDisableAlert(expectedMessageOnAlert);
     clickOkButtonOnAlert();
 
 
   }
 
   private void clickDisableButtonOnFacilityScreen() {
     testWebDriver.waitForElementToAppear(disableButton);
     disableButton.click();
   }
 
   private void clickOkButtonOnAlert() {
     testWebDriver.sleep(1000);
     okButton.click();
   }
 
   private void verifyDisableAlert(String expectedMessageOnAlert) {
     testWebDriver.waitForElementToAppear(disableMessageOnAlert);
 
     String disableMessageOnAlertValue = disableMessageOnAlert.getText();
     SeleneseTestNgHelper.assertEquals(disableMessageOnAlertValue, expectedMessageOnAlert);
   }
 
 
   private void verifyHeader(String headerToBeVerified) {
     testWebDriver.waitForElementToAppear(facilityHeader);
     SeleneseTestNgHelper.assertEquals(facilityHeader.getText().trim(), headerToBeVerified);
   }
 
 
   public void verifyDisabledFacility(String facilityCodeValue, String facilityNameValue) {
     String expectedMessageOnFacilityScreenAfterDisable = "\"" + facilityNameValue + "\" / \"" + facilityCodeValue + "\" is now disabled";
 
     testWebDriver.waitForElementToAppear(successMessageDiv);
 
     testWebDriver.sleep(1000);
     String disableMessageOnFacilityScreenValue = successMessageDiv.getText();
     SeleneseTestNgHelper.assertEquals(disableMessageOnFacilityScreenValue, expectedMessageOnFacilityScreenAfterDisable);
 
     String enableValue = enabledFlag.getText();
     SeleneseTestNgHelper.assertEquals(enableValue.trim(), "No");
 
     SeleneseTestNgHelper.assertTrue(isActiveRadioNoOption.isSelected());
   }
 
   public void verifyEnabledFacility() {
 
     testWebDriver.sleep(1000);
     String enableValue = enabledFlag.getText();
     SeleneseTestNgHelper.assertEquals(enableValue.trim(), "Yes");
     SeleneseTestNgHelper.assertTrue(isActiveRadioYesOption.isSelected());
     verifyHeader("Edit facility");
   }
 
   public HomePage enableFacility() throws IOException {
     String expectedIsActiveMessageOnAlert = "Do you want to set facility as active?";
 
     testWebDriver.waitForElementToAppear(enableButton);
     testWebDriver.sleep(1000);
     enableButton.click();
     testWebDriver.waitForElementToAppear(enableMessageOnAlert);
 
 
     testWebDriver.sleep(1000);
     okLink.click();
     testWebDriver.sleep(1000);
     testWebDriver.waitForElementToAppear(isActiveMessageOnAlert);
     String isActiveMessageOnAlertValue = isActiveMessageOnAlert.getText();
     SeleneseTestNgHelper.assertEquals(isActiveMessageOnAlertValue, expectedIsActiveMessageOnAlert);
     testWebDriver.waitForElementToAppear(okLink);
     testWebDriver.sleep(1000);
     okLink.click();
 
     verifyEnabledFacility();
 
     return new HomePage(testWebDriver);
   }
 
   public HomePage editAndVerifyFacility(String program, String facilityNameValue) throws IOException {
     String catchmentPopulationValue = "600000";
     String latitudeValue = "955.5555";
     String longitudeValue = "644.4444";
     String altitudeValue = "6545.4545";
 
     verifyHeader("Edit facility");
 
     testWebDriver.waitForElementToAppear(disableButton);
     testWebDriver.sleep(1500);
     testWebDriver.waitForElementToAppear(facilityCode);
     catchmentPopulation.clear();
     catchmentPopulation.sendKeys(catchmentPopulationValue);
     latitude.clear();
     latitude.sendKeys(latitudeValue);
     longitude.clear();
     longitude.sendKeys(longitudeValue);
     altitude.clear();
     altitude.sendKeys(altitudeValue);
 
     testWebDriver.selectByVisibleText(programsSupported, program);
     programsSupportedActiveFlag.click();
     testWebDriver.sleep(500);
     programsSupportedStartDate.click();
     startDateCalender.click();
     testWebDriver.sleep(500);
     startDateAlert.click();
     testWebDriver.sleep(500);
     addSupportedProgram.click();
 
     verifyEditedFacility(catchmentPopulationValue, latitudeValue, longitudeValue, altitudeValue);
 
     SaveButton.click();
     verifyMessageOnFacilityScreen(facilityNameValue, "updated");
 
     return new HomePage(testWebDriver);
   }
 
   public void verifyMessageOnFacilityScreen(String facilityName, String status) {
 
     String message = null;
     testWebDriver.sleep(1000);
     testWebDriver.waitForElementsToAppear(messageDiv, saveErrorMsgDiv);
     String updateMessage = getMessage();
     SeleneseTestNgHelper.assertEquals(updateMessage, "Facility '" + facilityName + "' " + status + " successfully");
     testWebDriver.sleep(500);
   }
 
   private void verifyEditedFacility(String catchmentPopulationValue, String latitudeValue, String longitudeValue, String altitudeValue) {
     SeleneseTestNgHelper.assertEquals(testWebDriver.getAttribute(catchmentPopulation, "value"), catchmentPopulationValue);
     SeleneseTestNgHelper.assertEquals(testWebDriver.getAttribute(latitude, "value"), latitudeValue);
     SeleneseTestNgHelper.assertEquals(testWebDriver.getAttribute(longitude, "value"), longitudeValue);
     SeleneseTestNgHelper.assertEquals(testWebDriver.getAttribute(altitude, "value"), altitudeValue);
 
     SeleneseTestNgHelper.assertTrue(removeSupportedProgram.isDisplayed());
   }
 
   public HomePage verifyProgramSupported(java.util.ArrayList<String> programsSupported, String date_time) throws IOException {
     int i = 1;
     clickFacilityList(date_time);
     verifyHeader("Edit facility");
     testWebDriver.waitForElementToAppear(disableButton);
     testWebDriver.sleep(1500);
     for (String program : programsSupported) {
       WebElement programsSupportedElement = testWebDriver.getElementByXpath("//table[@class='table table-striped table-bordered']/tbody/tr[" + i + "]/td[1]");
       WebElement programsActiveElement = testWebDriver.getElementByXpath("//table[@class='table table-striped table-bordered']/tbody/tr[" + i + "]/td[2]/input");
       SeleneseTestNgHelper.assertEquals(programsSupportedElement.getText().trim(), program);
       SeleneseTestNgHelper.assertTrue("Program " + i + " should be active", programsActiveElement.isSelected());
 
       i++;
     }
     SeleneseTestNgHelper.assertTrue(removeSupportedProgram.isDisplayed());
 
     return new HomePage(testWebDriver);
   }
 
 
   public String getMessage() {
     String updateMessage;
     if (messageDiv.isDisplayed()) {
       updateMessage = messageDiv.getText();
     } else {
       updateMessage = saveErrorMsgDiv.getText();
     }
     return updateMessage;
   }
 
 
 }
