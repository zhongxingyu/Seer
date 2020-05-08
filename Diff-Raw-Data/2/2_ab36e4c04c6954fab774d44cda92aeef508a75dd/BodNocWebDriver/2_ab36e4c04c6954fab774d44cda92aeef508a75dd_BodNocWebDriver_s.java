 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.support;
 
 import static nl.surfnet.bod.support.BodWebDriver.URL_UNDER_TEST;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import nl.surfnet.bod.pages.noc.AddPhysicalPortPage;
 import nl.surfnet.bod.pages.noc.EditPhysicalPortPage;
 import nl.surfnet.bod.pages.noc.EditPhysicalResourceGroupPage;
 import nl.surfnet.bod.pages.noc.ListAllocatedPortsPage;
 import nl.surfnet.bod.pages.noc.ListPhysicalResourceGroupPage;
 import nl.surfnet.bod.pages.noc.ListUnallocatedPortsPage;
 import nl.surfnet.bod.pages.noc.NewPhysicalResourceGroupPage;
 import nl.surfnet.bod.pages.noc.NocOverviewPage;
 
 import org.openqa.selenium.remote.RemoteWebDriver;
 
 public class BodNocWebDriver {
 
   private final RemoteWebDriver driver;
 
   public BodNocWebDriver(RemoteWebDriver driver) {
     this.driver = driver;
   }
 
   /* **************************************** */
   /* Physical Resource Group */
   /* **************************************** */
 
   public void createNewPhysicalResourceGroup(String institute, String adminGroup, String email) {
     NewPhysicalResourceGroupPage page = NewPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
     page.sendInstitute(institute);
     page.sendAdminGroup(adminGroup);
     page.sendEmail(email);
 
     page.save();
   }
 
   public void deletePhysicalResourceGroup(String institute) {
     ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
 
     page.delete(institute);
   }
 
   public void editPhysicalResourceGroup(String institute, String finalEmail) {
     ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver);
 
     EditPhysicalResourceGroupPage editPage = page.edit(institute);
     editPage.sendEmail(finalEmail);
     editPage.save();
   }
 
   public void verifyGroupWasCreated(String institute, String email) {
     verifyGroupExists(institute, email, false);
   }
 
   public void verifyGroupExists(String institute, String email, boolean active) {
     ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, BodWebDriver.URL_UNDER_TEST);
 
     // TODO check for icon icon-ban-circle
     page.findRow(institute, email);
   }
 
   public void verifyPhysicalResourceGroupIsActive(String institute, String email) {
     verifyGroupExists(institute, email, true);
   }
 
   /* ******************************************** */
   /* Physical ports */
   /* ******************************************** */
 
   public void linkPhysicalPort(String networkElementPk, String nocLabel, String physicalResourceGroup) {
     linkPhysicalPort(networkElementPk, nocLabel, "", physicalResourceGroup);
   }
 
   public void linkPhysicalPort(String networkElementPk, String nocLabel, String managerLabel,
       String physicalResourceGroup) {
     ListUnallocatedPortsPage listPage = ListUnallocatedPortsPage.get(driver, BodWebDriver.URL_UNDER_TEST);
 
     EditPhysicalPortPage editPage = listPage.edit(networkElementPk);
     editPage.sendNocLabel(nocLabel);
     editPage.sendManagerLabel(managerLabel);
     editPage.selectPhysicalResourceGroup(physicalResourceGroup);
     editPage.save();
   }
 
   public void verifyPhysicalPortWasAllocated(String networkElementPk, String nocLabel) {
     ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
 
     page.findRow(networkElementPk, nocLabel);
   }
 
   public void unlinkPhysicalPort(String networkElementPk) {
     ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
 
     page.unlinkPhysicalPort(networkElementPk);
   }
 
   public void gotoEditPhysicalPortAndVerifyManagerLabel(String networkElementPk, String managerLabel) {
     ListAllocatedPortsPage listPage = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
 
     EditPhysicalPortPage editPage = listPage.edit(networkElementPk);
 
     assertThat(editPage.getManagerLabel(), is(managerLabel));
   }
 
   public void switchToManager() {
     switchTo("BoD Administrator");
   }
 
   public void switchToUser() {
     switchTo("BoD User");
   }
 
   private void switchTo(String role) {
     NocOverviewPage page = NocOverviewPage.get(driver, URL_UNDER_TEST);
 
     page.clickSwitchRole(role);
   }
 
   public void addPhysicalPortToInstitute(String groupName, String nocLabel, final String portLabel) {
     ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
 
     AddPhysicalPortPage addPage = page.addPhysicalPort(groupName);
     addPage.selectPort(portLabel);
     addPage.sendNocLabel(nocLabel);
 
     addPage.save();
   }
 
   public void verifyStatistics() {
     NocOverviewPage page = NocOverviewPage.get(driver, URL_UNDER_TEST);
 
    page.findRow("Physical ports", "2");
     page.findRow("Elapsed reservations", "0");
     page.findRow("Active reservations", "0");
     page.findRow("Coming reservations", "1");
   }
 }
