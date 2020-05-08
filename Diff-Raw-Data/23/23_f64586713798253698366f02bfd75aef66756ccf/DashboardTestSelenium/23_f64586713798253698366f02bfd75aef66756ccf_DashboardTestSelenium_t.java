 /**
  * Copyright (c) 2012, SURFnet BV
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
 
 import nl.surfnet.bod.support.SeleniumWithSingleSetup;
 
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.junit.Test;
 
 public class DashboardTestSelenium extends SeleniumWithSingleSetup {
 
   @Override
   public void setupInitialData() {
     LocalDate startDate = LocalDate.now();
     LocalDate endDate = startDate;
     LocalTime startTime = LocalTime.now();
     LocalTime endTime = startTime.plusHours(1);
 
     new ReservationTestSelenium().setup();
 
     getManagerDriver().switchToUser();
 
     getUserDriver().createNewReservation(
         "Res Coming", startDate.plusDays(1), endDate.plusDays(1), startTime.plusHours(1), endTime.plusHours(1));
 
     getUserDriver().createNewReservation("Res Active, which will not become active");
   }
 
   @Test
   public void verifyUserStatisticLinksFromDashboard() {
    getNocDriver().switchToUser();

     getUserDriver().verifyDashboardToVirtualPortsLink("Selenium users");
     getUserDriver().verifyDashboardToComingReservationsLink("Selenium users");
     getUserDriver().verifyDashboardToElapsedReservationsLink("Selenium users");
   }
 
   @Test
   public void verifyNocStatisticLinksFromDashboard() {
     getManagerDriver().switchToNoc();
 
     getNocDriver().verifyDashboardToAllocatedPhysicalPortsLink();
     getNocDriver().verifyDashboardToElapsedReservationsLink();
     getNocDriver().verifyDashboardToComingReservationsLink();
   }
 
   @Test
   public void verifyManagerStatisticLinksFromDashboard() {
     getNocDriver().switchToManager();
 
     getManagerDriver().verifyDashboardToPhysicalPortsLink();
     getManagerDriver().verifyDashboardToElapsedReservationsLink();
     getManagerDriver().verifyDashboardToComingReservationsLink();
   }
 
 }
