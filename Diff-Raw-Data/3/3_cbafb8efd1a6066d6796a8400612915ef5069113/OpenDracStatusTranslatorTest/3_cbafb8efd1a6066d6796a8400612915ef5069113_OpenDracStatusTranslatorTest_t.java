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
 package nl.surfnet.bod.nbi;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 import java.util.Locale;
 
 import nl.surfnet.bod.domain.ReservationStatus;
 import nl.surfnet.bod.nbi.NbiOpenDracWsClient.OpenDracStatusTranslator;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationScheduleT;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleCreationResultT;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleStatusT;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleTypeT;
 
 public class OpenDracStatusTranslatorTest {
 
   @Test
   public void failedShouldTranslateToFailed() {
     ReservationStatus status = OpenDracStatusTranslator.translate(ValidReservationScheduleCreationResultT.FAILED, true);
 
     assertThat(status, is(ReservationStatus.NOT_ACCEPTED));
   }
 
   @Test
   public void succeededPartiallyShouldTranslateTo() {
     ReservationStatus status =
         OpenDracStatusTranslator.translate(ValidReservationScheduleCreationResultT.SUCCEEDED_PARTIALLY, true);
 
     assertThat(status, is(ReservationStatus.AUTO_START));
   }
 
   @Test
   public void unknownShouldTranslateToFailed() {
     ReservationStatus status = OpenDracStatusTranslator.translate(ValidReservationScheduleCreationResultT.UNKNOWN, true);
 
     assertThat(status, is(ReservationStatus.FAILED));
   }
 
   @Test
   public void executionTimedOutShouldTranslateToFailed() {
     ReservationScheduleT reservationSchedule = ReservationScheduleT.Factory.newInstance();
     reservationSchedule.setStatus(ValidReservationScheduleStatusT.EXECUTION_TIMED_OUT);
 
     ReservationStatus status = OpenDracStatusTranslator.translate(reservationSchedule);
 
     assertThat(status, is(ReservationStatus.TIMED_OUT));
   }
 
   @Test
   public void executionSucceededShouldTranslateToSucceeded() {
     ReservationScheduleT reservationSchedule = ReservationScheduleT.Factory.newInstance();
     reservationSchedule.setStatus(ValidReservationScheduleStatusT.EXECUTION_SUCCEEDED);
 
     ReservationStatus status = OpenDracStatusTranslator.translate(reservationSchedule);
 
     assertThat(status, is(ReservationStatus.SUCCEEDED));
   }
 
   @Test
   public void executionPendingShouldTranslateToAutoStart() {
     ReservationScheduleT reservationSchedule = ReservationScheduleT.Factory.newInstance();
     reservationSchedule.setStatus(ValidReservationScheduleStatusT.EXECUTION_PENDING);
     reservationSchedule.setType(ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_AUTOMATIC);
    
    // Added because an AUTOMATIC schedule in OpenDRAC is always true / activated
    reservationSchedule.setActivated(true);
 
     ReservationStatus status = OpenDracStatusTranslator.translate(reservationSchedule);
 
     assertThat(status, is(ReservationStatus.AUTO_START));
   }
 
   @Test
   public void executionPendingShouldTranslateToReserved() {
     ReservationScheduleT reservationSchedule = ReservationScheduleT.Factory.newInstance();
     reservationSchedule.setStatus(ValidReservationScheduleStatusT.EXECUTION_PENDING);
     reservationSchedule.setType(ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_MANUAL);
     reservationSchedule.setStartTime(DateTime.now().plusDays(1).toCalendar(Locale.getDefault()));
 
     ReservationStatus status = OpenDracStatusTranslator.translate(reservationSchedule);
 
     assertThat(status, is(ReservationStatus.RESERVED));
   }
 
   @Test
   public void executionPendingShouldTranslateToScheduled() {
     ReservationScheduleT reservationSchedule = ReservationScheduleT.Factory.newInstance();
     reservationSchedule.setStatus(ValidReservationScheduleStatusT.EXECUTION_PENDING);
     reservationSchedule.setType(ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_MANUAL);
     reservationSchedule.setStartTime(DateTime.now().minusDays(1).toCalendar(Locale.getDefault()));
 
     ReservationStatus status = OpenDracStatusTranslator.translate(reservationSchedule);
 
     assertThat(status, is(ReservationStatus.SCHEDULED));
   }
 }
