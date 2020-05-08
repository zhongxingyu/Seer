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
 package nl.surfnet.bod;
 
 import com.google.common.base.Optional;
 
 import nl.surfnet.bod.service.DatabaseTestHelper;
 import nl.surfnet.bod.support.ReservationFilterViewFactory;
 import nl.surfnet.bod.support.SeleniumWithSingleSetup;
 
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.junit.After;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ReservationTestSelenium extends SeleniumWithSingleSetup {
 
   private static final Logger LOGGER = LoggerFactory.getLogger(ReservationTestSelenium.class);
 
   @Override
   public void setupInitialData() {
     getNocDriver().createNewApiBasedPhysicalResourceGroup(GROUP_SURFNET, ICT_MANAGERS_GROUP, "test@example.com");
     getNocDriver().linkUniPort(NMS_NOVLAN_PORT_ID_1, "First port", GROUP_SURFNET);
     getNocDriver().linkUniPort(NMS_PORT_ID_2, "Second port", GROUP_SURFNET);
 
     getWebDriver().clickLinkInLastEmail();
 
     getUserDriver().requestVirtualPort("Selenium users");
     getUserDriver().selectInstituteAndRequest(GROUP_SURFNET, 1200, "port 1");
     getWebDriver().clickLinkInLastEmail();
     getManagerDriver().acceptVirtualPort("First port", "First port", Optional.<String>absent(), Optional.<Integer>absent());
 
     getUserDriver().requestVirtualPort("Selenium users");
     getUserDriver().selectInstituteAndRequest(GROUP_SURFNET, 1200, "port 2");
     getWebDriver().clickLinkInLastEmail();
     getManagerDriver().acceptVirtualPort("Second port", "Second port", Optional.<String>absent(), Optional.of(23));
   }
 
   @After
   public void dropReservations() {
     DatabaseTestHelper.deleteReservationsFromSeleniumDatabase();
   }
 
   @Test @Ignore
   public void createAndCancelAReservation() {
     LocalDate startDate = LocalDate.now().plusDays(3);
     LocalDate endDate = LocalDate.now().plusDays(5);
     LocalTime startTime = LocalTime.now().plusHours(1);
     LocalTime endTime = LocalTime.now();
     String reservationLabel = "Selenium Reservation";
 
     getManagerDriver().switchToUserRole();
     getUserDriver().createNewReservation(reservationLabel, startDate, endDate, startTime, endTime);
     getUserDriver().verifyAndWaitForReservationIsAutoStart(reservationLabel);
     getUserDriver().verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);
 
     getUserDriver().switchToManagerRole(GROUP_SURFNET);
     getManagerDriver().verifyReservationWasCreated(reservationLabel, startDate, endDate, startTime, endTime);
     getManagerDriver().verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);
     getManagerDriver().verifyStatistics();
 
     getManagerDriver().switchToNocRole();
     getNocDriver().verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);
     getNocDriver().verifyStatistics();
 
     getManagerDriver().switchToUserRole();
     getUserDriver().cancelReservation(startDate, endDate, startTime, endTime);
     getUserDriver().verifyReservationIsCanceled(startDate, endDate, startTime, endTime);
   }
 
   @Test @Ignore
   public void createReservationWithStartTimeNowAndEndTimeForever() {
     String reservationLabel = "Starts now and forever";
 
     getManagerDriver().switchToUserRole();
 
     getUserDriver().createNewReservation(reservationLabel);
 
     getUserDriver().verifyReservationWasCreated(reservationLabel);
 
     getUserDriver().verifyAndWaitForReservationIsAutoStartOrRunning(reservationLabel);
   }
 
   @Test
   public void searchAndFilterReservations() {
     String even = "Even reservation";
     String odd = "Odd reservation";
     LocalDate date = LocalDate.now().plusDays(1);
     LocalTime startTime = new LocalTime(8, 0);
 
     getManagerDriver().switchToUserRole();
 
    LOGGER.info("Creating " + even);
    getUserDriver().createNewReservation(odd, date, date, startTime.plusHours(2), startTime.plusHours(2 + 1));
    LOGGER.info("Created " + even);
     LOGGER.info("Creating " + odd);
    getUserDriver().createNewReservation(even, date, date, startTime, startTime.plusHours(1));
     LOGGER.info("Created " + odd);
 
     // Filter on this year, and no search String. All should be found.
     getUserDriver().verifyReservationByFilterAndSearch(String.valueOf(date.getYear()), "", even, odd);
 
     // Search on even
     getUserDriver().verifyReservationByFilterAndSearch(ReservationFilterViewFactory.COMING, "even", even);
 
     // Search on odd
     getUserDriver().verifyReservationByFilterAndSearch(ReservationFilterViewFactory.COMING, "odd", odd);
   }
 }
