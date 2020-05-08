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
 package nl.surfnet.bod.web.view;
 
 import nl.surfnet.bod.web.noc.LogEventController;
 import nl.surfnet.bod.web.noc.ReservationController;
 
 public class NocStatisticsView {
 
   private final long elapsedReservationsAmount;
   private final long activeReservationsAmount;
   private final long comingReservationsAmount;
   private final long uniPortsAmount;
   private final long enniPortsAmount;
   private final long unalignedPhysicalPortsAmount;
 
  public NocStatisticsView(long countUniPorts, long countEnniPorts, long countElapsedReservations, long countActiveReservations,
      long countComingReservations, long countUnalignedPhyscalPorts) {
 
     this.uniPortsAmount = countUniPorts;
     this.enniPortsAmount = countEnniPorts;
     this.elapsedReservationsAmount = countElapsedReservations;
     this.activeReservationsAmount = countActiveReservations;
     this.comingReservationsAmount = countComingReservations;
     this.unalignedPhysicalPortsAmount = countUnalignedPhyscalPorts;
   }
 
   public long getElapsedReservationsAmount() {
     return elapsedReservationsAmount;
   }
 
   public long getActiveReservationsAmount() {
     return activeReservationsAmount;
   }
 
   public long getComingReservationsAmount() {
     return comingReservationsAmount;
   }
 
   public long getUniPortsAmount() {
     return uniPortsAmount;
   }
 
   public long getEnniPortsAmount() {
     return enniPortsAmount;
   }
 
   public String getElapsedReservationsUrl() {
     return ReservationController.ELAPSED_URL;
   }
 
   public String getActiveReservationsUrl() {
     return ReservationController.ACTIVE_URL;
   }
 
   public String getComingReservationsUrl() {
     return ReservationController.COMING_URL;
   }
 
   public long getUnalignedPhysicalPortsAmount() {
     return unalignedPhysicalPortsAmount;
   }
 
   public String getUnalignedPhysicalPortsUrl() {
     return "noc/physicalports/unaligned";
   }
 
   public String getUniPortsUrl() {
     return "noc/physicalports/uni";
   }
 
   public String getEnniPortsUrl() {
     return "noc/physicalports/enni";
   }
 
   public String getLogEventsUrl() {
     return "noc/" + LogEventController.PAGE_URL;
   }
 }
