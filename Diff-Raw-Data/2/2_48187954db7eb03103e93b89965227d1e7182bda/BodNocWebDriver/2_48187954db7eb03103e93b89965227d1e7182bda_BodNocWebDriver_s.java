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
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.event.LogEventType;
 import nl.surfnet.bod.pages.noc.*;
 import nl.surfnet.bod.web.InstituteController;
 
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 import org.joda.time.LocalTime;
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
 
   public void linkPhysicalPort(String nmsPortId, String nocLabel, String physicalResourceGroup) {
     linkPhysicalPort(nmsPortId, nocLabel, "", physicalResourceGroup);
   }
 
   public void linkPhysicalPort(String nmsPortId, String nocLabel, String managerLabel, String physicalResourceGroup) {
     ListUnallocatedPortsPage listPage = ListUnallocatedPortsPage.get(driver, BodWebDriver.URL_UNDER_TEST);
 
     EditPhysicalPortPage editPage = listPage.edit(nmsPortId);
     editPage.sendNocLabel(nocLabel);
     editPage.sendManagerLabel(managerLabel);
     editPage.selectPhysicalResourceGroup(physicalResourceGroup);
     editPage.save();
   }
 
   public void unlinkPhysicalPort(String nmsPortId) {
     ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
 
     page.unlinkPhysicalPort(nmsPortId);
   }
 
   public void gotoEditPhysicalPortAndVerifyManagerLabel(String nmsPortId, String managerLabel) {
     ListAllocatedPortsPage listPage = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
 
     EditPhysicalPortPage editPage = listPage.edit(nmsPortId);
 
     assertThat(editPage.getManagerLabel(), is(managerLabel));
   }
 
   public void switchToManager() {
     switchTo("BoD Administrator");
   }
 
   public void switchToUser() {
     switchTo("User");
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
 
     page.findRow("Allocated physical ports", "2");
     page.findRow("Reservations past", "0");
     page.findRow("Active reservations", "0");
     page.findRow("Reservations in", "1");
   }
 
   public void verifyReservationIsCancellable(String reservationLabel, LocalDate startDate, LocalDate endDate,
       LocalTime startTime, LocalTime endTime) {
 
     ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);
 
     page.verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);
   }
 
   public void verifyReservationIsNotCancellable(String reservationLabel, LocalDate startDate, LocalDate endDate,
       LocalTime startTime, LocalTime endTime) {
 
     ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);
 
     page.verifyReservationIsNotCancellable(reservationLabel, startDate, endDate, startTime, endTime,
         "no right to cancel");
   }
 
   public void verifyPhysicalPortHasEnabledUnallocateIcon(String nmsPortId, String label) {
     ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
 
     page.verifyPhysicalPortHasEnabledUnallocateIcon(nmsPortId, label);
   }
 
   public void verifyPhysicalPortHasDisabeldUnallocateIcon(String nmsPortId, String label, String toolTipText) {
     ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
 
     page.verifyPhysicalPortHasDisabledUnallocateIcon(nmsPortId, label, toolTipText);
   }
 
   public void verifyPhysicalPortIsNotOnUnallocatedPage(String nmsPortId, String label) {
     ListUnallocatedPortsPage page = ListUnallocatedPortsPage.get(driver, URL_UNDER_TEST);
 
     page.verifyPhysicalPortIsNotOnUnallocatedPage(nmsPortId, label);
   }
 
   public void verifyPhysicalPortWasAllocated(String nmsPortId, String label) {
    ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver);
 
     page.verifyPhysicalPortWasAllocated(nmsPortId, label);
   }
 
   public void movePhysicalPort(String name) {
     ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
 
     page.movePort(name);
   }
 
   public void movePhysicalPortChooseNewPort(String networkElementPk) {
     MovePhysicalPortPage movePage = MovePhysicalPortPage.get(driver);
     movePage.selectNewPhysicalPort(networkElementPk);
     movePage.movePort();
   }
 
   public void verifyMovePage(String networkElementPk, String instituteName, int numberOfVps, int numberOfRess,
       int numberOfActiveRess) {
     MovePhysicalPortPage movePage = MovePhysicalPortPage.get(driver);
 
     assertThat(movePage.getNmsPortId(), is(networkElementPk));
     assertThat(movePage.getInstituteName(), is(instituteName));
     assertThat(movePage.getNumberOfVirtualPorts(), is(numberOfVps));
     assertThat(movePage.getNumberOfReservations(), is(numberOfRess));
     assertThat(movePage.getNumberOfActiveReservations(), is(numberOfActiveRess));
   }
 
   public void verifyMoveResultPage(int i) {
     MovePhysicalPortResultPage page = MovePhysicalPortResultPage.get(driver);
 
     assertThat(page.getNumberOfReservations(), is(i));
   }
 
   public void verifyHasReservations(int i) {
     ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);
 
     assertThat(page.getNumberOfReservations(), is(i));
   }
 
   public void verifyHasLogEvents(int i) {
     ListLogEventsPage page = ListLogEventsPage.get(driver, URL_UNDER_TEST);
 
     assertThat(page.getNumberOfLogEvents(), is(i));
   }
 
   public void verifyLogEvents() {
     ListLogEventsPage page = ListLogEventsPage.get(driver, URL_UNDER_TEST);
 
     page.logEventShouldBe(LocalDateTime.now(), "selenium-user", LogEventType.CREATE,
         PhysicalResourceGroup.class.getSimpleName());
   }
 
   public void refreshInstitutes() {
     driver.get(URL_UNDER_TEST + InstituteController.REFRESH_URL);
   }
 
   public void verifyVirtualResourceGroupExists(String... fields) {
     ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver, URL_UNDER_TEST);
     page.findRow(fields);
   }
 }
