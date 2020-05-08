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
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.support.ReservationFactory;
 import nl.surfnet.bod.support.RichUserDetailsFactory;
 import nl.surfnet.bod.support.VirtualPortFactory;
 import nl.surfnet.bod.support.VirtualResourceGroupFactory;
 import nl.surfnet.bod.web.security.Security;
 
 import org.joda.time.DateTimeUtils;
 import org.joda.time.LocalDateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.Errors;
 
 public class ReservationValidatorTest {
 
   private static final String GROUP_NAME = "urn:bandwidth-on-demand";
   private ReservationValidator subject;
   private LocalDateTime tomorrowNoon = LocalDateTime.now().plusDays(1).withHourOfDay(12).withMinuteOfHour(0)
       .withSecondOfMinute(0);
 
   @Before
   public void setUp() {
     subject = new ReservationValidator();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(GROUP_NAME).create());
   }
 
   @Test
   public void shouldSupportReservationClass() {
     assertTrue(subject.supports(Reservation.class));
   }
 
   @Test
   public void shouldNotSupportObjectClass() {
     assertFalse(subject.supports(Object.class));
   }
 
   @Test
   public void endDateShouldNotBeBeforeStartDate() {
     LocalDateTime now = LocalDateTime.now();
     Reservation reservation = new ReservationFactory().setStartDateTime(now).setEndDateTime(now.minusDays(1)).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertTrue(errors.hasErrors());
     assertTrue(errors.hasFieldErrors("endDate"));
   }
 
   @Test
   public void endDateMayBeOnStartDate() {
 
     Reservation reservation = new ReservationFactory().setStartDateTime(tomorrowNoon)
         .setEndDateTime(tomorrowNoon.plusMinutes(10)).setSurfconextGroupId(GROUP_NAME).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertFalse(errors.hasErrors());
   }
 
   @Test
   public void whenEndAndStartDateAreOnTheSameDayStartTimeShouldBeBeforeEndTime() {
     LocalDateTime now = LocalDateTime.now();
     Reservation reservation = new ReservationFactory().setStartDateTime(now).setEndDateTime(now.minusMinutes(1))
         .create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertTrue(errors.hasErrors());
     assertTrue(errors.hasFieldErrors("endTime"));
   }
 
   @Test
   public void whenEndAndStartDateAreOnTheSameDayStartTimeShouldBeBeforeEndTime2() {
     Reservation reservation = new ReservationFactory().setStartDateTime(tomorrowNoon)
         .setEndDateTime(tomorrowNoon.plusHours(1)).setSurfconextGroupId(GROUP_NAME).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertFalse(errors.hasErrors());
   }
 
   @Test
   public void reservationShouldHaveDifferentPorts() {
     VirtualPort port = new VirtualPortFactory().create();
     Reservation reservation = new ReservationFactory().setSourcePort(port).setDestinationPort(port).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertTrue(errors.hasErrors());
    assertThat(errors.getGlobalError().getCode(), containsString("validation.reservation.sameports"));
   }
 
   @Test
   public void aReservationShouldNotBeInThePast() {
     LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
 
     Reservation reservation = new ReservationFactory().setStartDateTime(yesterday).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertTrue(errors.hasFieldErrors("startDate"));
   }
 
   @Test
   public void aReservationShouldNotBeInThePast2() {
     LocalDateTime THIRTY_PAST_MIDNIGHT = LocalDateTime.now().plusMinutes(30);
     DateTimeUtils.setCurrentMillisFixed(THIRTY_PAST_MIDNIGHT.toDate().getTime());
 
     LocalDateTime fewMinutesAgo = THIRTY_PAST_MIDNIGHT.minusMinutes(20);
 
     Reservation reservation = new ReservationFactory().setStartDateTime(fewMinutesAgo).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     DateTimeUtils.setCurrentMillisSystem();
 
     assertTrue(errors.hasFieldErrors("startTime"));
   }
 
   @Test
   public void aReservationShouldNotBeFurtherInTheFuterThanOneYear() {
     Reservation reservation = new ReservationFactory().setStartDateTime(LocalDateTime.now().plusYears(1).plusDays(1))
         .setEndDateTime(LocalDateTime.now().plusYears(1).plusDays(10)).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertTrue(errors.hasFieldErrors("startDate"));
   }
 
   @Test
   public void reservationShouldBeLongerThan5Minutes() {
     Reservation reservation = new ReservationFactory().setStartDateTime(tomorrowNoon)
         .setEndDateTime(tomorrowNoon.plusMinutes(3)).setSurfconextGroupId(GROUP_NAME).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.getGlobalErrors(), hasSize(1));
     assertThat(errors.getGlobalError().getCode(), containsString("validation.reservation.duration.tooShort"));
   }
 
   @Test
   public void reservationShouldBeLongerThan5Minutes2() {
     LocalDateTime startDateTime = LocalDateTime.now().withMonthOfYear(12).withDayOfMonth(31).withHourOfDay(23)
         .withMinuteOfHour(58);
     LocalDateTime endDateTime = startDateTime.plusDays(1).withHourOfDay(0).withMinuteOfHour(1);
 
     Reservation reservation = new ReservationFactory().setStartDateTime(startDateTime).setEndDateTime(endDateTime)
         .setSurfconextGroupId(GROUP_NAME).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.getGlobalErrors(), hasSize(1));
     assertThat(errors.getGlobalError().getCode(), containsString("validation.reservation.duration.tooShort"));
   }
 
   @Test
   public void aReservationShouldNotBeLongerThanOneYear() {
     LocalDateTime startDateTime = LocalDateTime.now().plusDays(5);
     LocalDateTime endDateTime = startDateTime.plusYears(1).plusDays(1);
 
     Reservation reservation = new ReservationFactory().setStartDateTime(startDateTime).setEndDateTime(endDateTime)
         .setSurfconextGroupId(GROUP_NAME).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.getGlobalErrors(), hasSize(1));
     assertThat(errors.getGlobalError().getCode(), containsString("validation.reservation.duration.tooLong"));
   }
 
   @Test
   public void vritualGroupNameShouldBeSameAsOfThePorts() {
     VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setSurfconextGroupId("urn:wronggroup").create();
     VirtualPort source = new VirtualPortFactory().setVirtualResourceGroup(vrg).create();
 
     Reservation reservation = new ReservationFactory().setSourcePort(source).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.getGlobalError().getCode(), containsString("validation.reservation.security"));
   }
 
   @Test
   public void userIsNotAMemberOfTheSurfConextGroup() {
     VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setSurfconextGroupId("urn:wronggroup").create();
     VirtualPort sourcePort = new VirtualPortFactory().setVirtualResourceGroup(vrg).create();
     VirtualPort destPort = new VirtualPortFactory().setVirtualResourceGroup(vrg).create();
 
     Reservation reservation = new ReservationFactory().setVirtualResourceGroup(vrg).setSourcePort(sourcePort)
         .setDestinationPort(destPort).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.getGlobalError().getCode(), containsString("validation.reservation.security"));
   }
 
   @Test
   public void bandwidthShouldNotExceedMax() {
     VirtualPort sourcePort = new VirtualPortFactory().setMaxBandwidth(2000).create();
     VirtualPort destPort = new VirtualPortFactory().setMaxBandwidth(3000).create();
 
     Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort)
         .setBandwidth(2500).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.hasFieldErrors("bandwidth"), is(true));
   }
 
   @Test
   public void bandwidthShouldBeGreaterThanZero() {
     VirtualPort sourcePort = new VirtualPortFactory().setMaxBandwidth(2000).create();
     VirtualPort destPort = new VirtualPortFactory().setMaxBandwidth(3000).create();
 
     Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort)
         .setBandwidth(0).create();
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.hasFieldErrors("bandwidth"), is(true));
   }
 
   @Test
   public void noStartDateShouldBeAllowed() {
     Reservation reservation = new ReservationFactory().create();
     reservation.setStartDateTime(null);
     reservation.setEndDateTime(null);
 
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.hasErrors(), is(true));
   }
 
   private Errors createErrorObject(Reservation reservation) {
     return new BeanPropertyBindingResult(reservation, "reservation");
   }
 }
