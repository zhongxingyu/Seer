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
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 
 import java.util.List;
 
 import com.google.common.base.Optional;
 
 import nl.surfnet.bod.domain.NsiVersion;
 import nl.surfnet.bod.domain.ReservationStatus;
 import nl.surfnet.bod.pages.user.AdvancedPage;
 import nl.surfnet.bod.pages.user.DashboardPage;
 import nl.surfnet.bod.pages.user.EditVirtualPortPage;
 import nl.surfnet.bod.pages.user.ListLogEventsPage;
 import nl.surfnet.bod.pages.user.ListReservationPage;
 import nl.surfnet.bod.pages.user.ListVirtualPortPage;
 import nl.surfnet.bod.pages.user.NewReservationPage;
 import nl.surfnet.bod.pages.user.OauthAuthorizePage;
 import nl.surfnet.bod.pages.user.OauthTokenRequestFormPage;
 import nl.surfnet.bod.pages.user.OauthTokensPage;
 import nl.surfnet.bod.pages.user.RequestNewVirtualPortRequestPage;
 import nl.surfnet.bod.pages.user.RequestNewVirtualPortSelectInstitutePage;
 import nl.surfnet.bod.pages.user.RequestNewVirtualPortSelectTeamPage;
 
 import org.apache.commons.lang.StringUtils;
 import org.hamcrest.Matchers;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 import org.joda.time.LocalTime;
 import org.junit.Assert;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 public class BodUserWebDriver extends AbstractBoDWebDriver<DashboardPage> {
 
   private final FirefoxDriver driver;
 
   public BodUserWebDriver(FirefoxDriver driver) {
     this.driver = driver;
   }
 
   public void requestVirtualPort(String team) {
     DashboardPage page = DashboardPage.get(driver, URL_UNDER_TEST);
 
     page.selectInstitute(team);
   }
 
   public void verifyReservationStartDateHasError(String string) {
     NewReservationPage page = NewReservationPage.get(driver);
     String error = page.getStartDateError();
 
     assertThat(error, containsString(string));
   }
 
   public void cancelReservation(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
     ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);
 
     page.deleteByDates(startDate, endDate, startTime, endTime);
   }
 
   public void verifyReservationIsCanceled(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
     ListReservationPage page = ListReservationPage.get(driver);
 
     page.reservationShouldBe(startDate, endDate, startTime, endTime, ReservationStatus.CANCELLED);
   }
 
   public void verifyAndWaitForReservationIsAutoStart(String label) {
     ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);
 
     page.reservationShouldBe(label, ReservationStatus.AUTO_START);
   }
 
 
   public void verifyAndWaitForReservationIsAutoStartOrRunning(String label) {
     ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);
 
     page.reservationShouldBe(label, ReservationStatus.AUTO_START, ReservationStatus.RUNNING);
   }
 
   public void selectTeamInstituteAndRequest(String team, String institute, String userLabel, Integer bandwidth,
       String message) {
     ListVirtualPortPage page = ListVirtualPortPage.get(driver, URL_UNDER_TEST);
 
     RequestNewVirtualPortSelectTeamPage selectTeamPage = page.requestVirtualPort();
     selectTeamPage.selectTeam(team);
 
     selectInstituteAndRequest(institute, userLabel, bandwidth, message);
   }
 
   public void selectInstituteAndRequest(String institute, String userLabel, Integer bandwidth, String message) {
     RequestNewVirtualPortSelectInstitutePage page = RequestNewVirtualPortSelectInstitutePage.get(driver);
 
     RequestNewVirtualPortRequestPage requestPage = page.selectInstitute(institute);
 
     requestPage.sendUserLabel(userLabel);
     requestPage.sendBandwidth("" + bandwidth);
     requestPage.sendMessage(message);
     requestPage.sentRequest();
   }
 
   public void selectInstituteAndRequest(String institute, Integer bandwidth, String message) {
     selectInstituteAndRequest(institute, "", bandwidth, message);
   }
 
   public void verifyRequestVirtualPortInstituteInactive(String instituteName) {
     RequestNewVirtualPortSelectInstitutePage page = RequestNewVirtualPortSelectInstitutePage.get(driver);
 
     try {
       page.selectInstitute(instituteName);
       Assert.fail("Found a link for institute " + instituteName);
     }
     catch (org.openqa.selenium.NoSuchElementException e) {
       // expected
     }
   }
 
   public void createNewReservation(String label, LocalDateTime startDateTime, LocalDateTime endDateTime) {
     createNewReservation(label, startDateTime.toLocalDate(), endDateTime.toLocalDate(), startDateTime.toLocalTime(),
         endDateTime.toLocalTime());
   }
 
   public void createNewReservation(String label, LocalDate startDate, LocalDate endDate, LocalTime startTime,
       LocalTime endTime) {
     NewReservationPage page = NewReservationPage.get(driver, URL_UNDER_TEST);
 
     page.sendLabel(label);
     page.sendStartDate(startDate);
     page.sendStartTime(startTime);
     page.sendEndDate(endDate);
     page.sendEndTime(endTime);
     page.sendBandwidth("500");
 
    Screenshotter.takeScreenshot(driver, "before_saving_" + label);
     page.save();
    Screenshotter.takeScreenshot(driver, "after_saving_" + label);
   }
 
   public void createNewReservation(String label) {
     NewReservationPage page = NewReservationPage.get(driver, URL_UNDER_TEST);
 
     page.sendLabel(label);
     page.clickStartNow();
     page.clickForever();
 
     page.save();
 
   }
 
   public void editVirtualPort(String oldLabel, String newLabel) {
     ListVirtualPortPage listPage = ListVirtualPortPage.get(driver, URL_UNDER_TEST);
 
     EditVirtualPortPage editPage = listPage.edit(oldLabel);
     editPage.sendUserLabel(newLabel);
     editPage.save();
   }
 
   public void verifyVirtualPortExists(String... fields) {
     ListVirtualPortPage listPage = ListVirtualPortPage.get(driver, URL_UNDER_TEST);
 
     listPage.findRow(fields);
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
 
   public void verifyReservationIsNotCancellable(String reservationLabel, LocalDate startDate, LocalDate endDate,
       LocalTime startTime, LocalTime endTime) {
     ListReservationPage page = ListReservationPage.get(driver);
 
     page.verifyReservationIsNotCancellable(reservationLabel, startDate, endDate, startTime, endTime);
   }
 
   public void verifyReservationIsCancellable(String reservationLabel, LocalDate startDate, LocalDate endDate,
       LocalTime startTime, LocalTime endTime) {
 
     ListReservationPage page = ListReservationPage.get(driver);
 
     page.verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);
   }
 
   public void verifyReservationWasCreated(String reservationLabel, LocalDate startDate, LocalDate endDate,
       LocalTime startTime, LocalTime endTime) {
     ListReservationPage page = ListReservationPage.get(driver);
 
     page.verifyReservationExists(reservationLabel, startDate, endDate, startTime, endTime);
   }
 
   public void verifyReservationWasCreated(String label) {
     ListReservationPage page = ListReservationPage.get(driver);
 
     assertThat(page.getInfoMessages(), Matchers.<String> hasItem(containsString(label)));
 
     page.verifyRowWithLabelExists(label);
   }
 
   public void verifyNotMemberOf(String teamName) {
     DashboardPage page = DashboardPage.get(driver, URL_UNDER_TEST);
 
     assertThat(page.getTeams(), not(hasItem(teamName)));
   }
 
   public void verifyMemberOf(String teamName) {
     DashboardPage page = DashboardPage.get(driver, URL_UNDER_TEST);
 
     assertThat(page.getTeams(), hasItem(teamName));
   }
 
   public void verifyReservationByFilterAndSearch(final String filterValue, final String searchString, final String... reservationLabels) {
     Poller.assertEventually(new Probe() {
       private boolean satisfied = false;
 
       @Override
       public void sample() {
         ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);
 
         page.filterReservations(filterValue);
         page.search(searchString);
 
         satisfied = page.getNumberOfRows() == reservationLabels.length;
 
         for (String label : reservationLabels) {
           try {
             page.verifyRowWithLabelExists(label);
           } catch (NoSuchElementException e) {
             satisfied = false;
           }
         }
       }
 
       @Override
       public boolean isSatisfied() {
         return satisfied;
       }
 
       @Override
       public String message() {
         return "expected rows with labels <" + StringUtils.join(reservationLabels, "; ") + ">";
       }
     });
   }
 
   public void verifyDashboardToComingReservationsLink(String team) {
     DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
 
     int numberOfItems = dashboardPage.getNumberFromRowWithLinkAndClick(
         team,
         formatLink("reservations/filter/coming/search?search=team:%s", team),
         "Show");
 
     ListReservationPage reservationPage = ListReservationPage.get(driver, URL_UNDER_TEST);
     reservationPage.filterReservations(ReservationFilterViewFactory.COMING);
     reservationPage.verifyIsCurrentPage();
     assertThat(numberOfItems, is(reservationPage.getNumberOfRows()));
   }
 
   public void verifyDashboardToElapsedReservationsLink(String team) {
     DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
 
     int numberOfItems = dashboardPage.getNumberFromRowWithLinkAndClick(
         team,
         formatLink("reservations/filter/elapsed/search?search=team:%s", team),
         "Show");
 
     ListReservationPage reservationPage = ListReservationPage.get(driver, URL_UNDER_TEST);
     reservationPage.filterReservations(ReservationFilterViewFactory.ELAPSED);
     reservationPage.verifyIsCurrentPage();
     assertThat(numberOfItems, is(reservationPage.getNumberOfRows()));
 
   }
 
   public void verifyDashboardToVirtualPortsLink(String team) {
     DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
 
     int numberOfItems = dashboardPage.getNumberFromRowWithLinkAndClick(
         team,
         formatLink("virtualports/search?search=team:%s", team),
         "Show");
 
     ListVirtualPortPage vpPage = ListVirtualPortPage.get(driver, URL_UNDER_TEST);
     vpPage.verifyIsCurrentPage();
     assertThat(numberOfItems, is(vpPage.getNumberOfRows()));
   }
 
   private String formatLink(String link, String team) {
     return String.format(link, "%22"+team.replace(" ", "%20")+"%22");
   }
 
   public void verifyMenu() {
     DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
     dashboardPage.verifyNumberOfMenuItems();
     dashboardPage.verifyMenuOverview();
     dashboardPage.verifyMenuReservations();
     dashboardPage.verifyMenuVirtualPorts();
     dashboardPage.verifyMenuLogEvents();
     dashboardPage.verifyMenuAdvanced();
     dashboardPage.verifyMenuReport();
   }
 
   @Override
   protected DashboardPage getDashboardPage() {
     return DashboardPage.get(driver, URL_UNDER_TEST);
   }
 
   public String requestOauthToken() {
     DashboardPage dashboardPage = getDashboardPage();
 
     AdvancedPage advancedPage = dashboardPage.clickAdvancedMenuLink();
     advancedPage.clickTokenLink();
 
     OauthTokenRequestFormPage oauthTokenRequestFormPage = OauthTokenRequestFormPage.get(driver);
 
     OauthTokensPage tokensPage = oauthTokenRequestFormPage.fillAndSubmit(TestExternalSupport.SELENIUM_USER_URN, "my_irrelevant_password");
 
     Optional<String> token = tokensPage.getToken();
 
     if (token.isPresent()){
       return token.get();
     }
 
     OauthAuthorizePage oauthAuthorizePage = tokensPage.clickRequestButton();
     tokensPage = oauthAuthorizePage.clickGrantButton();
 
     return tokensPage.getToken().get();
   }
 
   public List<String> getVirtualPortIds(final NsiVersion nsiVersion){
     DashboardPage dashboardPage = getDashboardPage();
     ListVirtualPortPage listVirtualPortPage = dashboardPage.clickVirtualPortsLink();
 
     return listVirtualPortPage.getAllNsiStpIds(nsiVersion);
   }
 
 }
