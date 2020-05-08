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
 package nl.surfnet.bod;
 
 import nl.surfnet.bod.support.TestExternalSupport;
 
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ReservationTestSelenium extends TestExternalSupport {
 
   private static final String INSTITUTE_NAME = "SURFnet Netwerk";
 
   @Before
   public void setup() {
     getNocDriver().createNewPhysicalResourceGroup(INSTITUTE_NAME, ICT_MANAGERS_GROUP, "test@example.com");
     getNocDriver().linkPhysicalPort(NETWORK_ELEMENT_PK, "First port", INSTITUTE_NAME);
     getNocDriver().linkPhysicalPort(NETWORK_ELEMENT_PK_2, "Second port", INSTITUTE_NAME);
 
     getWebDriver().clickLinkInLastEmail();
 
     getUserDriver().requestVirtualPort("selenium-users");
     getUserDriver().selectInstituteAndRequest(INSTITUTE_NAME, 1200, "port 1");
     getWebDriver().clickLinkInLastEmail();
     getManagerDriver().createVirtualPort("First port");
 
     getUserDriver().requestVirtualPort("selenium-users");
     getUserDriver().selectInstituteAndRequest(INSTITUTE_NAME, 1200, "port 2");
     getWebDriver().clickLinkInLastEmail();
     getManagerDriver().createVirtualPort("Second port");
   }
 
   @Test
   public void createAndDeleteAReservation() {
     final LocalDate startDate = LocalDate.now().plusDays(3);
     final LocalDate endDate = LocalDate.now().plusDays(5);
     final LocalTime startTime = LocalTime.now().plusHours(1);
     final LocalTime endTime = LocalTime.now();
     final String reservationLabel = "Selenium Reservation";
 
     getManagerDriver().switchToUser();
     getUserDriver().createNewReservation(reservationLabel, startDate, endDate, startTime, endTime);
     getUserDriver().verifyReservationWasCreated(reservationLabel, startDate, endDate, startTime, endTime);
 
     getUserDriver().switchToManager(INSTITUTE_NAME);
     getManagerDriver().verifyReservationWasCreated(reservationLabel, startDate, endDate, startTime, endTime);
 
     // Verify statistics for manager
     getManagerDriver().verifyStatistics();
 
     // Verify statistics for noc
     getManagerDriver().switchToNoc();
     getNocDriver().verifyStatistics();
 
     getManagerDriver().switchToUser();
 
     getUserDriver().cancelReservation(startDate, endDate, startTime, endTime);
 
     getUserDriver().verifyReservationWasCanceled(startDate, endDate, startTime, endTime);
   }
 
   @Test
   public void createReservationWithNowAndForever() {
     getManagerDriver().switchToUser();
 
     getUserDriver().createNewReservation("Starts now and forever");
 
     getUserDriver().verifyReservationWasCreated("Starts now and forever");
   }
 
   @Test
   public void cancelReservation() {
     final LocalDate startDate = LocalDate.now().plusDays(3);
     final LocalDate endDate = LocalDate.now().plusDays(5);
     final LocalTime startTime = LocalTime.now().plusHours(1);
     final LocalTime endTime = LocalTime.now();
     final String reservationLabel = "Selenium Reservation";
 
     // User, create reservation
     getManagerDriver().switchToUser();
     getUserDriver().createNewReservation(reservationLabel, startDate, endDate, startTime, endTime);
     getUserDriver().verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);
 
     // Manager should also be able to cancel the reservation
     getUserDriver().switchToManager(INSTITUTE_NAME);
    getManagerDriver().verifyReservationIsNotCancellable(reservationLabel, startDate, endDate, startTime, endTime);
 
     // Noc should NOT be able to cancel the reservation
     getManagerDriver().switchToNoc();
     getNocDriver().verifyReservationIsNotCancellable(reservationLabel, startDate, endDate, startTime, endTime);
 
     getManagerDriver().switchToUser();
 
     getUserDriver().cancelReservation(startDate, endDate, startTime, endTime);
     getUserDriver().verifyReservationWasCanceled(startDate, endDate, startTime, endTime);
   }
 
 }
