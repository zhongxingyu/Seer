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
 import org.joda.time.LocalDateTime;
 import org.joda.time.LocalTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ReservationTestSelenium extends TestExternalSupport {
 
  private static final String INSTITUTE_NAME = "SURFnet netwerk";
 
   @Before
   public void setup() {
     getNocDriver().createNewPhysicalResourceGroup(INSTITUTE_NAME, ICT_MANAGERS_GROUP, "test@example.com");
     getNocDriver().linkPhysicalPort(NETWORK_ELEMENT_PK, "First port", INSTITUTE_NAME);
     getNocDriver().linkPhysicalPort(NETWORK_ELEMENT_PK_2, "Second port", INSTITUTE_NAME);
 
     getWebDriver().refreshGroups();
 
     getWebDriver().clickLinkInLastEmail();
 
     getWebDriver().requestVirtualPort("selenium-users");
     getWebDriver().selectInstituteAndRequest(INSTITUTE_NAME, 1200, "port 1");
     getWebDriver().clickLinkInLastEmail();
     getManagerDriver().createVirtualPort("First port");
 
     getWebDriver().requestVirtualPort("selenium-users");
     getWebDriver().selectInstituteAndRequest(INSTITUTE_NAME, 1200, "port 2");
     getWebDriver().clickLinkInLastEmail();
     getManagerDriver().createVirtualPort("Second port");
   }
 
   @Test
   public void createAndDeleteAReservation() {
     LocalDateTime creationDateTime = LocalDateTime.now();
     LocalDate startDate = LocalDate.now().plusDays(3);
     LocalDate endDate = LocalDate.now().plusDays(5);
     LocalTime startTime = LocalTime.now().plusHours(1);
     LocalTime endTime = LocalTime.now();
 
     getWebDriver().createNewReservation(startDate, endDate, startTime, endTime);
 
     getWebDriver().verifyReservationWasCreated(startDate, endDate, startTime, endTime, creationDateTime);
 
     getManagerDriver().verifyReservationExists(startDate, endDate, startTime, endTime, creationDateTime);
 
     getWebDriver().cancelReservation(startDate, endDate, startTime, endTime);
 
     getWebDriver().verifyReservationWasCanceled(startDate, endDate, startTime, endTime);
   }
 
   @After
   public void teardown() {
     getManagerDriver().deleteVirtualResourceGroup("selenium-users");
     getNocDriver().unlinkPhysicalPort(NETWORK_ELEMENT_PK);
     getNocDriver().unlinkPhysicalPort(NETWORK_ELEMENT_PK_2);
     getNocDriver().deletePhysicalResourceGroup(INSTITUTE_NAME);
   }
 
 }
