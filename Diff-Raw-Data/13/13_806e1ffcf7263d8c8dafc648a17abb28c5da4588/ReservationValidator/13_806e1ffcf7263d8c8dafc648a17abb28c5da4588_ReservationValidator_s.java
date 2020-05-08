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
 package nl.surfnet.bod.domain.validator;
 
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.ReservationEndPoint;
 import nl.surfnet.bod.web.security.Security;
 
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.joda.time.Period;
 import org.joda.time.format.PeriodFormat;
 import org.joda.time.format.PeriodFormatter;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 
 public class ReservationValidator implements Validator {
 
  private static final Period MIN_PERIOD = new Period().withMinutes(5);
  private static final Period MAX_PERIOD = new Period().withYears(1);
  private static final Period MAX_PERIOD_AWAY = new Period().withYears(1);
   private static final PeriodFormatter PERIOD_FORMATTER = PeriodFormat.getDefault();
 
   @Override
   public boolean supports(Class<?> clazz) {
     return Reservation.class.isAssignableFrom(clazz);
   }
 
   @Override
   public void validate(Object objectToValidate, Errors errors) {
     Reservation reservation = (Reservation) objectToValidate;
 
     validatePorts(errors, reservation);
     validateStartAndEndDate(errors, reservation);
     validateBandwidth(errors, reservation);
   }
 
   private void validateBandwidth(Errors errors, Reservation reservation) {
     ReservationEndPoint sourcePort = reservation.getSourcePort();
     ReservationEndPoint destinationPort = reservation.getDestinationPort();
 
     if (sourcePort == null || destinationPort == null || reservation.getBandwidth() == null) {
       return;
     }
 
     if (reservation.getBandwidth() <= 0) {
       errors.rejectValue("bandwidth", "validation.reservation.bandwidthZero");
       return;
     }
 
     long maxBandwidth = Long.MAX_VALUE;
     if (sourcePort.getVirtualPort().isPresent()) {
       maxBandwidth = Math.min(maxBandwidth, sourcePort.getVirtualPort().get().getMaxBandwidth());
     }
     if (destinationPort.getVirtualPort().isPresent()) {
       maxBandwidth = Math.min(maxBandwidth, destinationPort.getVirtualPort().get().getMaxBandwidth());
     }
     if (reservation.getBandwidth() > maxBandwidth) {
       errors.rejectValue("bandwidth", "validation.reservation.maxBandwidth", new Object[] { maxBandwidth }, "bandwidth exceeded max");
     }
   }
 
   private void validatePorts(Errors errors, Reservation reservation) {
     ReservationEndPoint sourcePort = reservation.getSourcePort();
     ReservationEndPoint destinationPort = reservation.getDestinationPort();
 
     if (sourcePort == null || destinationPort == null) {
       return;
     }
 
     if (sourcePort.equals(destinationPort)) {
       errors.rejectValue("sourcePort", "", "");
       errors.rejectValue("destinationPort", "validation.reservation.sameports", "Source and Destination port should be different");
     }
 
     if (!reservation.hasConsistentVirtualResourceGroups()) {
       errors.rejectValue("sourcePort", "", "");
       errors.rejectValue("destinationPort", "validation.reservation.security", "Ports are not in the same virtualResourceGroup");
     }
 
     if (reservation.getVirtualResourceGroup().isPresent() && !Security.getUserDetails().getUserGroupIds().contains(reservation.getVirtualResourceGroup().get().getAdminGroup())) {
       errors.reject("validation.reservation.security", "You can not select this port");
     }
   }
 
   /**
    * Validates time related values. {@link Reservation#getStartDateTime()} is
    * allowed to be null, to indicate an immediate start
    *
    * @param errors
    *          {@link Errors}
    * @param reservation
    *          {@link Reservation}
    */
   private void validateStartAndEndDate(Errors errors, Reservation reservation) {
     if (reservation.getEndDateTime() == null || reservation.getStartDateTime() == null) {
       return;
     }
 
     LocalDate startDate = reservation.getStartDate();
     LocalTime startTime = reservation.getStartTime();
     LocalDate endDate = reservation.getEndDate();
     LocalTime endTime = reservation.getEndTime();
 
     LocalDate today = LocalDate.now();
     LocalDate maxFutureDate = today.plus(MAX_PERIOD_AWAY);
     LocalTime now = LocalTime.now();
 
     boolean datesValid = true;
 
     if (startDate.isBefore(today)) {
       errors.rejectValue("startDate", "validation.reservation.startdate.past");
       datesValid = false;
     }
     if (startDate.isEqual(today) && startTime.isBefore(now)) {
       errors.rejectValue("startTime", "validation.reservation.startdate.past");
       datesValid = false;
     }
     if (startDate.isAfter(maxFutureDate)) {
       errors.rejectValue("startDate", "validation.reservation.startdate.maxFuture",
           new Object[] { MAX_PERIOD_AWAY.toString(PERIOD_FORMATTER) }, "Start date to far away");
       datesValid = false;
     }
 
     if (endDate.isBefore(startDate)) {
       errors.rejectValue("endDate", "validation.reservation.enddate.before.start");
       datesValid = false;
     }
     if (datesAreOnSameDay(startDate, endDate) && endTime.isBefore(startTime)) {
       errors.rejectValue("endTime", "validation.reservation.endtime.before.start");
       datesValid = false;
     }
 
     if (!datesValid) {
       return;
     }
 
     DateTime startDateTime = startDate.toDateTime(startTime);
     DateTime endDateTime = endDate.toDateTime(endTime);
     Duration duration = new Duration(startDateTime, endDateTime);
 
     if (duration.isLongerThan(MAX_PERIOD.toDurationFrom(startDateTime))) {
       errors.reject("validation.reservation.duration.tooLong", new Object[] { MAX_PERIOD.toString(PERIOD_FORMATTER) },
           "Reservation is too long");
     }
     if (duration.isShorterThan(MIN_PERIOD.toDurationFrom(startDateTime))) {
       errors.reject("validation.reservation.duration.tooShort", new Object[] { MIN_PERIOD.toString(PERIOD_FORMATTER) },
           "Reservation is too short");
     }
   }
 
   private boolean datesAreOnSameDay(LocalDate startDate, LocalDate endDate) {
     return startDate.isEqual(endDate);
   }
 
 }
