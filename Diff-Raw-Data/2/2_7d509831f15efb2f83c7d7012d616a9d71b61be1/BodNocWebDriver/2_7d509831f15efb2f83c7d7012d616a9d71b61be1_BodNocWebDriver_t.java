 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.support;
 
 import static nl.surfnet.bod.support.BodWebDriver.URL_UNDER_TEST;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 import nl.surfnet.bod.domain.ReservationStatus;
 import nl.surfnet.bod.pages.AbstractListPage;
 import nl.surfnet.bod.pages.noc.AddPhysicalPortPage;
 import nl.surfnet.bod.pages.noc.DashboardPage;
 import nl.surfnet.bod.pages.noc.EditPhysicalPortPage;
 import nl.surfnet.bod.pages.noc.EditPhysicalResourceGroupPage;
 import nl.surfnet.bod.pages.noc.ListAllocatedPortsPage;
 import nl.surfnet.bod.pages.noc.ListLogEventsPage;
 import nl.surfnet.bod.pages.noc.ListPhysicalResourceGroupPage;
 import nl.surfnet.bod.pages.noc.ListReservationPage;
 import nl.surfnet.bod.pages.noc.ListUnallocatedPortsPage;
 import nl.surfnet.bod.pages.noc.ListVirtualPortPage;
 import nl.surfnet.bod.pages.noc.ListVirtualResourceGroupPage;
 import nl.surfnet.bod.pages.noc.MovePhysicalPortPage;
 import nl.surfnet.bod.pages.noc.MovePhysicalPortResultPage;
 import nl.surfnet.bod.pages.noc.NewPhysicalResourceGroupPage;
 import org.apache.commons.lang.ArrayUtils;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.openqa.selenium.remote.RemoteWebDriver;
 
 public class BodNocWebDriver extends AbstractBoDWebDriver<DashboardPage> {
 
   private final RemoteWebDriver driver;
 
   public BodNocWebDriver(RemoteWebDriver driver) {
     this.driver = driver;
   }
 
   /* **************************************** */
   /* Physical Resource Group */
   /* **************************************** */
 
   public void createNewApiBasedPhysicalResourceGroup(String institute, String adminGroup, String email) {
     NewPhysicalResourceGroupPage page = NewPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
     page.selectApiMethod();
     page.sendInstitute(institute);
     page.sendAdminGroup(adminGroup);
     page.sendEmail(email);
 
     page.save();
   }
 
   public void createNewSabBasedPhysicalResourceGroup(String institute, String email) {
     NewPhysicalResourceGroupPage page = NewPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
     page.selectSabMethod();
     page.sendInstitute(institute);
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
 
   public void verifyPhysicalResourceGroupExists(String... fields) {
     ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
 
     page.findRow(fields);
   }
 
   public void verifyGroupExists(String institute, String email, boolean active) {
     ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, BodWebDriver.URL_UNDER_TEST);
 
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
 
   public void unlinkPhysicalPort(String bodPortId) {
     ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
 
     page.unlinkPhysicalPort(bodPortId);
   }
 
   public void gotoEditPhysicalPortAndVerifyManagerLabel(String nmsPortId, String managerLabel) {
     ListAllocatedPortsPage listPage = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
 
     EditPhysicalPortPage editPage = listPage.edit(nmsPortId);
 
     assertThat(editPage.getManagerLabel(), is(managerLabel));
   }
 
   public void addPhysicalPortToInstitute(String groupName, String nocLabel, String portLabel) {
     ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
 
     AddPhysicalPortPage addPage = page.addPhysicalPort(groupName);
     addPage.selectPort(portLabel);
     addPage.sendNocLabel(nocLabel);
 
     addPage.save();
   }
 
   public void verifyStatistics() {
     DashboardPage page = DashboardPage.get(driver, URL_UNDER_TEST);
 
    page.findRow("Allocated UNI ports", "2");
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
 
     page.verifyReservationIsNotCancellable(reservationLabel, startDate, endDate, startTime, endTime);
   }
 
   public void verifyReservationByFilterAndSearch(String filterValue, String searchString, String... reservationLabels) {
     ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);
 
     page.filterReservations(filterValue);
     verifyBySearch(page, searchString, reservationLabels);
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
     ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
 
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
 
     assertThat(page.getNumberOfRows(), is(i));
   }
 
   public void verifyHasLogEvents(int i) {
     ListLogEventsPage page = ListLogEventsPage.get(driver, URL_UNDER_TEST);
 
     assertThat(page.getNumberOfLogEvents(), is(i));
   }
 
   public void verifyLogEventExists(String... fields) {
     verifyLogEventExistsCreatedWithin(-1, fields);
   }
 
   public void verifyLogEventExistsCreatedWithin(int seconds, String... fields) {
     ListLogEventsPage page = ListLogEventsPage.get(driver, URL_UNDER_TEST);
 
     page.logEventShouldBe(DateTime.now(), seconds, fields);
   }
 
   public void verifyLogEventDoesNotExist(String... fields) {
     ListLogEventsPage page = ListLogEventsPage.get(driver, URL_UNDER_TEST);
     page.verifyRowWithLabelDoesNotExist(fields);
   }
 
   public void verifyVirtualResourceGroupExists(String... fields) {
     ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver, URL_UNDER_TEST);
     page.findRow(fields);
   }
 
   public void verifyUnallocatedPortsBySearch(String searchString, String... portLabels) {
     ListUnallocatedPortsPage page = ListUnallocatedPortsPage.get(driver, URL_UNDER_TEST);
     verifyBySearch(page, searchString, portLabels);
   }
 
   public void verifyAllocatedPortsBySearch(String searchString, String... portLabels) {
     ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
     verifyBySearch(page, searchString, portLabels);
   }
 
   public void verifyAllocatedPortsBySort(String sortColumn, String... expectedSequenceLabels) {
     ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
     verifyBySort(page, sortColumn, expectedSequenceLabels);
   }
 
   public void verifyAllocatedPortsBySearchAndSort(String searchString, String sortColumn,
       String... expectedSequenceLabels) {
     ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
 
     page.search(searchString);
     verifyBySort(page, sortColumn, expectedSequenceLabels);
   }
 
   private void verifyBySearch(AbstractListPage page, String searchString, String... labels) {
     page.search(searchString);
 
     int expectedAmount = labels == null ? 0 : labels.length;
     assertThat(page.getNumberOfRows(), is(expectedAmount));
 
     for (String label : labels) {
       page.verifyRowWithLabelExists(label);
     }
   }
 
   private void verifyBySort(AbstractListPage page, String sortColumn, String... expectedSequence) {
     page.verifyRowSequence(sortColumn, false, expectedSequence);
 
     ArrayUtils.reverse(expectedSequence);
     page.verifyRowSequence(sortColumn, true, expectedSequence);
   }
 
   public void verifyPhysicalResourceGroupToPhysicalPortsLink(String groupName) {
     ListPhysicalResourceGroupPage prgListPage = ListPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
 
     int numberOfItems = prgListPage.getNumberFromRowWithLinkAndClick(groupName, "noc/physicalports/uni", "Show");
 
     ListAllocatedPortsPage appListPage = ListAllocatedPortsPage.get(driver);
     appListPage.verifyIsCurrentPage();
     appListPage.verifyAmountOfRowsWithLabel(numberOfItems, groupName);
   }
 
   public void verifyTeamToVirtualPortsLink(String teamName) {
     ListVirtualResourceGroupPage vrgListPage = ListVirtualResourceGroupPage.get(driver, URL_UNDER_TEST);
 
     int numberOfItems = vrgListPage.getNumberFromRowWithLinkAndClick(teamName, "noc/virtualports", "Show");
 
     ListVirtualPortPage vpListPage = ListVirtualPortPage.get(driver);
     vpListPage.verifyIsCurrentPage();
     vpListPage.verifyAmountOfRowsWithLabel(numberOfItems, teamName);
   }
 
   public void verifyDashboardToUniPortsLink() {
     DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
 
     int expectedAmount = dashboardPage.getNumberFromRowWithLinkAndClick("Allocated UNI", "noc/physicalports/uni", "Show");
 
     ListAllocatedPortsPage listAllocatedPortsPage = ListAllocatedPortsPage.get(driver, URL_UNDER_TEST);
     listAllocatedPortsPage.verifyIsCurrentPage();
     listAllocatedPortsPage.verifyAmountOfRowsWithLabel(expectedAmount, ArrayUtils.EMPTY_STRING_ARRAY);
   }
 
   public void verifyDashboardToElapsedReservationsLink() {
     DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
 
     int expectedAmount = dashboardPage.getNumberFromRowWithLinkAndClick("past", "noc/reservations/filter/elapsed",
         "Show");
 
     ListReservationPage reservationsPage = ListReservationPage.get(driver, URL_UNDER_TEST);
     reservationsPage.verifyIsCurrentPage();
     reservationsPage.filterReservations(ReservationFilterViewFactory.ELAPSED);
     reservationsPage.verifyAmountOfRowsWithLabel(expectedAmount, ArrayUtils.EMPTY_STRING_ARRAY);
   }
 
   public void verifyDashboardToComingReservationsLink() {
     DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
 
     int expectedAmount = dashboardPage.getNumberFromRowWithLinkAndClick("in", "noc/reservations/filter/coming", "Show");
     ListReservationPage reservationsPage = ListReservationPage.get(driver, URL_UNDER_TEST);
     reservationsPage.verifyIsCurrentPage();
     reservationsPage.filterReservations(ReservationFilterViewFactory.COMING);
     reservationsPage.verifyAmountOfRowsWithLabel(expectedAmount, ArrayUtils.EMPTY_STRING_ARRAY);
   }
 
   public void verifyMenu() {
     DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
     dashboardPage.verifyNumberOfMenuItems();
     dashboardPage.verifyMenuOverview();
     dashboardPage.verifyMenuReservations();
     dashboardPage.verifyMenuTeams();
     dashboardPage.verifyMenuInstitutes();
     dashboardPage.verifyMenuVirtualPorts();
     dashboardPage.verifyMenuPhysicalPorts();
     dashboardPage.verifyMenuLogEvents();
     dashboardPage.verifyMenuReport();
   }
 
   @Override
   protected DashboardPage getDashboardPage() {
     return DashboardPage.get(driver, URL_UNDER_TEST);
   }
 
   public void verifyAndWaitForReservationIsAutoStart(String reservationLabel) {
       MovePhysicalPortResultPage page = MovePhysicalPortResultPage.get(driver);
       page.reservationShouldBe(reservationLabel, ReservationStatus.AUTO_START);
   }
 }
