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
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeUtils;
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.Errors;
 
 public class ReservationValidatorTest {
 
   private ReservationValidator subject;
   private final DateTime tomorrowNoon = DateTime.now().plusDays(1).withHourOfDay(12).withMinuteOfHour(0)
       .withSecondOfMinute(0);
 
   @Before
   public void setUp() {
     subject = new ReservationValidator();
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
     DateTime now = DateTime.now();
     Reservation reservation = new ReservationFactory().setStartDateTime(now).setEndDateTime(now.minusDays(1)).create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertTrue(errors.hasErrors());
     assertTrue(errors.hasFieldErrors("endDate"));
   }
 
   @Test
   public void endDateMayBeOnStartDate() {
 
     Reservation reservation = new ReservationFactory().setStartDateTime(tomorrowNoon)
        .setEndDateTime(tomorrowNoon.plusMinutes(16)).create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(
         reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertFalse(errors.hasErrors());
   }
 
   @Test
   public void whenEndAndStartDateAreOnTheSameDayStartTimeShouldBeBeforeEndTime() {
     DateTime now = DateTime.now();
     Reservation reservation = new ReservationFactory().setStartDateTime(now).setEndDateTime(now.minusMinutes(1))
         .create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(
         reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertTrue(errors.hasErrors());
     assertTrue(errors.hasFieldErrors("endTime"));
   }
 
   @Test
   public void whenEndAndStartDateAreOnTheSameDayStartTimeShouldBeBeforeEndTime2() {
     Reservation reservation = new ReservationFactory().setStartDateTime(tomorrowNoon)
         .setEndDateTime(tomorrowNoon.plusHours(1)).create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(
         reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertFalse(errors.hasErrors());
   }
 
   @Test
   public void reservationShouldHaveDifferentPorts() {
     VirtualPort port = new VirtualPortFactory().create();
     Reservation reservation = new ReservationFactory().setSourcePort(port).setDestinationPort(port).create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(
         reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertTrue(errors.hasErrors());
     assertThat(errors.getFieldErrors("destinationPort").get(0).getCode(),
         containsString("validation.reservation.sameports"));
   }
 
   @Test
   public void aReservationShouldNotBeInThePastCheckingDatePart() {
     DateTime yesterday = DateTime.now().minusDays(1);
 
     Reservation reservation = new ReservationFactory().setStartDateTime(yesterday).create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(
         reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertTrue(errors.hasFieldErrors("startDate"));
   }
 
   @Test
   public void aReservationShouldNotBeInThePastCheckingTimePart() {
     DateTime someHoursBeforeMidnight = LocalDate.now().toDateTimeAtStartOfDay().minusHours(2);
     DateTime startDateInThePast = someHoursBeforeMidnight.minusMinutes(5);
 
     Reservation reservation = new ReservationFactory().setStartDateTime(startDateInThePast.toDateTime()).create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(
         reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     Errors errors = createErrorObject(reservation);
 
     try {
       DateTimeUtils.setCurrentMillisFixed(someHoursBeforeMidnight.getMillis());
       subject.validate(reservation, errors);
     }
     finally {
       DateTimeUtils.setCurrentMillisSystem();
     }
 
     assertTrue(errors.hasFieldErrors("startTime"));
   }
 
   @Test
   public void aReservationShouldNotBeFurtherInTheFuterThanOneYear() {
     Reservation reservation = new ReservationFactory().setStartDateTime(DateTime.now().plusYears(1).plusDays(1))
         .setEndDateTime(DateTime.now().plusYears(1).plusDays(10)).create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(
         reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertTrue(errors.hasFieldErrors("startDate"));
   }
 
   @Test
   public void reservationShouldBeLongerThan5Minutes() {
     Reservation reservation = new ReservationFactory().setStartDateTime(tomorrowNoon)
         .setEndDateTime(tomorrowNoon.plusMinutes(3)).create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(
         reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.getGlobalErrors(), hasSize(1));
     assertThat(errors.getGlobalError().getCode(), containsString("validation.reservation.duration.tooShort"));
   }
 
   @Test
   public void reservationShouldBeLongerThan5Minutes2() {
     DateTime startDateTime = DateTime.now().withMonthOfYear(12).withDayOfMonth(31).withHourOfDay(23)
         .withMinuteOfHour(58);
     DateTime endDateTime = startDateTime.plusDays(1).withHourOfDay(0).withMinuteOfHour(1);
 
     Reservation reservation = new ReservationFactory().setStartDateTime(startDateTime).setEndDateTime(endDateTime)
         .create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(
         reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.getGlobalErrors(), hasSize(1));
     assertThat(errors.getGlobalError().getCode(), containsString("validation.reservation.duration.tooShort"));
   }
 
   @Test
   public void aReservationShouldNotBeLongerThanOneYear() {
     DateTime startDateTime = DateTime.now().plusDays(5);
     DateTime endDateTime = startDateTime.plusYears(1).plusDays(1);
 
     Reservation reservation = new ReservationFactory().setStartDateTime(startDateTime).setEndDateTime(endDateTime)
         .create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(
         reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.getGlobalErrors(), hasSize(1));
     assertThat(errors.getGlobalError().getCode(), containsString("validation.reservation.duration.tooLong"));
   }
 
   @Test
   public void userIsNotAMemberOfTheSurfConextGroup() {
     VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setAdminGroup("urn:wronggroup").create();
     VirtualPort sourcePort = new VirtualPortFactory().setVirtualResourceGroup(vrg).create();
     VirtualPort destPort = new VirtualPortFactory().setVirtualResourceGroup(vrg).create();
 
     Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort).create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup("urn:wrong:group").create());
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.getGlobalError().getCode(), containsString("validation.reservation.security"));
   }
 
   @Test
   public void bandwidthShouldNotExceedMax() {
     VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();
     VirtualPort sourcePort = new VirtualPortFactory().setVirtualResourceGroup(vrg).setMaxBandwidth(2000L).create();
     VirtualPort destPort = new VirtualPortFactory().setVirtualResourceGroup(vrg).setMaxBandwidth(3000L).create();
 
     Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort)
         .setBandwidth(2500L).create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(
         reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.hasFieldErrors("bandwidth"), is(true));
   }
 
   @Test
   public void bandwidthShouldBeGreaterThanZero() {
     VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();
     VirtualPort sourcePort = new VirtualPortFactory().setVirtualResourceGroup(vrg).setMaxBandwidth(2000L).create();
     VirtualPort destPort = new VirtualPortFactory().setVirtualResourceGroup(vrg).setMaxBandwidth(3000L).create();
 
     Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort)
         .setBandwidth(0L).create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(
         reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.hasFieldErrors("bandwidth"), is(true));
   }
 
   @Test
   public void noStartDateAndNoEdnDateShouldBeAllowed() {
     Reservation reservation = new ReservationFactory().create();
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(
         reservation.getVirtualResourceGroup().get().getAdminGroup()).create());
     reservation.setStartDateTime(null);
     reservation.setEndDateTime(null);
 
     Errors errors = createErrorObject(reservation);
 
     subject.validate(reservation, errors);
 
     assertThat(errors.hasErrors(), is(false));
   }
 
   private Errors createErrorObject(Reservation reservation) {
     return new BeanPropertyBindingResult(reservation, "reservation");
   }
 }
