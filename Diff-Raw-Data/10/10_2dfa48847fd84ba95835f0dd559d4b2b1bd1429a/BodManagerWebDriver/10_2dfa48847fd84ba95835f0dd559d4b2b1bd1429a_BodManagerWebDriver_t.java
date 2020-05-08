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
 
 import static junit.framework.Assert.fail;
 import static nl.surfnet.bod.support.BodWebDriver.URL_UNDER_TEST;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import nl.surfnet.bod.pages.manager.*;
 import nl.surfnet.bod.pages.noc.ListPhysicalResourceGroupPage;
 import nl.surfnet.bod.pages.noc.NocOverviewPage;
 
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.remote.RemoteWebDriver;
 
 public class BodManagerWebDriver {
 
   private final RemoteWebDriver driver;
 
   public BodManagerWebDriver(RemoteWebDriver driver) {
     this.driver = driver;
   }
 
   public void verifyManagerLabelChanged(String nmsPortId, String managerLabel) {
     ListPhysicalPortsPage listPage = ListPhysicalPortsPage.get(driver);
 
     listPage.findRow(nmsPortId, managerLabel);
   }
 
   public void verifyPhysicalPortSelected(String managerLabel) {
     NewVirtualPortPage page = NewVirtualPortPage.get(driver);
 
     String portName = page.getSelectedPhysicalPort();
 
     assertThat(portName, is(managerLabel));
   }
 
   public void changeManagerLabelOfPhyiscalPort(String nmsPortId, String managerLabel) {
     ListPhysicalPortsPage page = ListPhysicalPortsPage.get(driver, URL_UNDER_TEST);
 
     EditPhysicalPortPage editPage = page.edit(nmsPortId);
 
     editPage.sendMagerLabel(managerLabel);
     editPage.save();
   }
 
   public void verifyOnEditPhysicalResourceGroupPage(String expectedMailAdress) {
     EditPhysicalResourceGroupPage page = EditPhysicalResourceGroupPage.get(driver);
 
     String email = page.getEmailValue();
     assertThat(email, is(expectedMailAdress));
 
     assertThat(page.getInfoMessages(), hasSize(1));
     assertThat(page.getInfoMessages().get(0), containsString("Your institute is not activated"));
   }
 
   public void verifyVirtualPortExists(String... fields) {
     ListVirtualPortPage page = ListVirtualPortPage.get(driver, URL_UNDER_TEST);
 
     page.findRow(fields);
   }
 
   public void verifyVirtualResourceGroupsEmpty() {
     ListVirtualPortPage page = ListVirtualPortPage.get(driver, URL_UNDER_TEST);
 
     assertThat(page.isTableEmpty(), is(true));
   }
 
   public void deleteVirtualPort(String name) {
     ListVirtualPortPage page = ListVirtualPortPage.get(driver, URL_UNDER_TEST);
 
     page.delete(name);
   }
 
   public void deleteVirtualPortAndVerifyAlertText(String name, String alertText) {
     ListVirtualPortPage page = ListVirtualPortPage.get(driver, URL_UNDER_TEST);
 
     page.deleteAndVerifyAlert(alertText, name);
   }
 
   public void verifyVirtualPortWasDeleted(String name) {
     ListVirtualPortPage page = ListVirtualPortPage.get(driver);
 
     try {
       page.findRow(name);
       fail(String.format("Virtual port with name %s was not deleted", name));
     }
     catch (NoSuchElementException e) {
       // fine
     }
   }
 
   public void deleteVirtualResourceGroup(String vrgName) {
     ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver, URL_UNDER_TEST);
 
     page.delete(vrgName);
   }
 
   public void verifyVirtualResourceGroupExists(String... fields) {
     ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver, URL_UNDER_TEST);
     page.findRow(fields);
   }
 
   public void verifyVirtualResourceGroupWasDeleted(String vrgName) {
     ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver);
     try {
       page.findRow(vrgName);
       fail(String.format("Virtual Resource group with vrgName %s was not deleted", vrgName));
     }
     catch (NoSuchElementException e) {
       // fine
     }
   }
 
   public void verifyNewVirtualPortHasProperties(String instituteName, String userLabel, Integer bandwidth) {
     NewVirtualPortPage page = NewVirtualPortPage.get(driver);
 
    assertThat(page.getUserLabel(), is(userLabel));
    assertThat(page.getSelectedPhysicalResourceGroup(), is(instituteName));
    assertThat(page.getBandwidth(), is(bandwidth));
   }
 
   public void editVirtualPort(String orignalName, String newName, int bandwidth, String vlanId) {
     ListVirtualPortPage listPage = ListVirtualPortPage.get(driver, URL_UNDER_TEST);
 
     EditVirtualPortPage editPage = listPage.edit(orignalName);
 
     editPage.sendName(newName);
     editPage.sendMaxBandwidth(bandwidth);
     editPage.sendVlanId(vlanId);
 
     editPage.save();
   }
 
   public void verifyReservationIsCancellable(String reservationLabel, LocalDate startDate, LocalDate endDate,
       LocalTime startTime, LocalTime endTime) {
 
     ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);
 
     page.verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);
   }
 
   public void verifyStatistics() {
     ManagerOverviewPage page = ManagerOverviewPage.get(driver, URL_UNDER_TEST);
 
     page.findRow("Physical ports", "2");
     page.findRow("Virtual ports", "2");
     page.findRow("Reservations past", "0");
     page.findRow("Active reservations", "0");
     page.findRow("Reservations in", "1");
   }
 
   public void createVirtualPort(String name) {
     createVirtualPort(name, null);
   }
 
   public void createVirtualPort(String name, String vpUserLabel) {
     NewVirtualPortPage page = NewVirtualPortPage.get(driver);
     page.sendName(name);
     page.sendUserLabel(vpUserLabel);
     page.sendVlanId("23");
 
     page.save();
   }
 
   public void acceptVirtualPort(String label) {
     NewVirtualPortPage page = NewVirtualPortPage.get(driver);
 
     page.sendName(label);
     page.sendVlanId("23");
     page.accept();
     page.save();
   }
 
   public void declineVirtualPort(String message) {
     NewVirtualPortPage page = NewVirtualPortPage.get(driver);
 
     page.decline();
     page.sendDeclineMessage(message);
 
     page.save();
   }
 
   public void verifyPhysicalResourceGroupExists(String... fields) {
     ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
 
     page.findRow(fields);
   }
 
   public void switchToNoc() {
     switchTo("NOC Engineer");
   }
 
   public void switchToUser() {
     switchTo("User");
   }
 
   public void switchToManager(String manager) {
     switchTo("BoD Administrator", manager);
   }
 
   private void switchTo(String... role) {
     NocOverviewPage page = NocOverviewPage.get(driver, URL_UNDER_TEST);
 
     page.clickSwitchRole(role);
   }
 
   public void verifyReservationWasCreated(String reservationLabel, LocalDate startDate, LocalDate endDate,
       LocalTime startTime, LocalTime endTime) {
 
     ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);
 
     page.verifyReservationExists(reservationLabel, startDate, endDate, startTime, endTime);
   }
 
   public void verifyReservationIsNotCancellable(String reservationLabel, LocalDate startDate, LocalDate endDate,
       LocalTime startTime, LocalTime endTime) {
 
     ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);
 
     page.verifyReservationIsNotCancellable(reservationLabel, startDate, endDate, startTime, endTime, "state cannot");
   }
 
   public void verifyPhysicalPortHasEnabledUnallocateIcon(String nmsPortId, String nocLabel) {
     ListPhysicalPortsPage page = ListPhysicalPortsPage.get(driver, URL_UNDER_TEST);
 
     page.verifyPhysicalPortHasEnabledUnallocateIcon(nmsPortId, nocLabel);
   }
 
   public void verifyPhysicalPortHasDisabeldUnallocateIcon(String nmsPortId, String nocLabel, String toolTipText) {
     ListPhysicalPortsPage page = ListPhysicalPortsPage.get(driver, URL_UNDER_TEST);
 
     page.verifyPhysicalPortHasDisabledUnallocateIcon(nmsPortId, nocLabel, toolTipText);
   }
 }
