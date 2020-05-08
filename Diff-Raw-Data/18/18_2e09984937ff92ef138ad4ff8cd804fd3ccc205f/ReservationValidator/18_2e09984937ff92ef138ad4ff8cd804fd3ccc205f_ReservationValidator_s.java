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
 package nl.surfnet.bod.domain.validator;
 
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.VirtualPort;
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
     VirtualPort sourcePort = reservation.getSourcePort();
     VirtualPort destinationPort = reservation.getDestinationPort();
 
     if (sourcePort == null || destinationPort == null || reservation.getBandwidth() == null) {
       return;
     }
 
     if (reservation.getBandwidth() <= 0) {
       errors.rejectValue("bandwidth", "validation.reservation.bandwidthZero");
       return;
     }
 
     Integer maxBandwidth = Math.min(sourcePort.getMaxBandwidth(), destinationPort.getMaxBandwidth());
     if (reservation.getBandwidth() > maxBandwidth) {
       errors.rejectValue("bandwidth", "validation.reservation.maxBandwidth", new Object[] { maxBandwidth },
           "bandwidth exceeded max");
     }
   }
 
   private void validatePorts(Errors errors, Reservation reservation) {
     VirtualPort sourcePort = reservation.getSourcePort();
     VirtualPort destinationPort = reservation.getDestinationPort();
 
     if (sourcePort == null || destinationPort == null) {
       return;
     }
 
     if (sourcePort.equals(destinationPort)) {
       errors.reject("validation.reservation.sameports", "Source and Destination port should be different");
       errors.rejectValue("sourcePort", "", "");
       errors.rejectValue("destinationPort", "", "");
     }
 
     String groupNameOfSource = sourcePort.getVirtualResourceGroup().getSurfconextGroupId();
     String groupNameOfDestination = destinationPort.getVirtualResourceGroup().getSurfconextGroupId();
     String groupName = reservation.getVirtualResourceGroup().getSurfconextGroupId();
 
     if (!groupNameOfSource.equals(groupNameOfDestination) || !groupName.equals(groupNameOfSource)) {
       errors.reject("validation.reservation.security", "Ports are not in the same virtualResourceGroup");
     }
 
     if (!Security.getUserDetails().getUserGroupIds().contains(groupNameOfSource)) {
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
 
     if (reservation.getEndDate() == null) {
       errors.rejectValue("endDate", "validation.not.empty");
     }
 
     if (reservation.getEndTime() == null) {
       errors.rejectValue("endTime", "validation.not.empty");
     }
 
    if (reservation.getStartDateTime() == null) {
       return;
     }
 
     LocalDate startDate = reservation.getStartDate();
     LocalTime startTime = reservation.getStartTime();
     LocalDate endDate = reservation.getEndDate();
     LocalTime endTime = reservation.getEndTime();
 
     LocalDate today = LocalDate.now();
     LocalDate maxFutureDate = today.plus(MAX_PERIOD_AWAY);
     LocalTime now = LocalTime.now();
 
     if (startDate.isBefore(today)) {
       errors.rejectValue("startDate", "validation.reservation.startdate.past");
     }
     if (startDate.isEqual(today) && startTime.isBefore(now)) {
       errors.rejectValue("startTime", "validation.reservation.startdate.past");
     }
     if (startDate.isAfter(maxFutureDate)) {
       errors.rejectValue("startDate", "validation.reservation.startdate.maxFuture",
           new Object[] { MAX_PERIOD_AWAY.toString(PERIOD_FORMATTER) }, "Start date to far away");
     }
 
     if (endDate.isBefore(startDate)) {
       errors.rejectValue("endDate", "validation.reservation.enddate.before.start");
     }
     if (datesAreOnSameDay(startDate, endDate) && endTime.isBefore(startTime)) {
       errors.rejectValue("endTime", "validation.reservation.endtime.before.start");
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
