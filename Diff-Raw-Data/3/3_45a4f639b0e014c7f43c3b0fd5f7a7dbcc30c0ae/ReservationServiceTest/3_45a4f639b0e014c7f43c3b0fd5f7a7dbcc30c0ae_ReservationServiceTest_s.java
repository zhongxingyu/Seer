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
 package nl.surfnet.bod.service;
 
 import static nl.surfnet.bod.matchers.DateMatchers.isAfterNow;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.verifyZeroInteractions;
 import static org.mockito.Mockito.when;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import nl.surfnet.bod.domain.*;
 import nl.surfnet.bod.nbi.NbiClient;
 import nl.surfnet.bod.repo.ReservationRepo;
 import nl.surfnet.bod.support.ReservationFactory;
 import nl.surfnet.bod.support.RichUserDetailsFactory;
 import nl.surfnet.bod.support.VirtualPortFactory;
 import nl.surfnet.bod.support.VirtualResourceGroupFactory;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 
 import org.joda.time.LocalDateTime;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.data.domain.PageImpl;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.jpa.domain.Specification;
 
 import com.google.common.collect.Lists;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ReservationServiceTest {
 
   @InjectMocks
   private ReservationService subject;
 
   @Mock
   private ReservationRepo reservationRepoMock;
 
   @Mock private ReservationToNbi reservationToNbiMock;
 
   @Mock
   private NbiClient nbiClientMock;
 
   @Test
   public void whenTheUserHasNoGroupsTheReservationsShouldBeEmpty() {
     Security.setUserDetails(new RichUserDetailsFactory().create());
 
     List<Reservation> reservations = subject.findEntries(0, 20, new Sort("id"));
 
     assertThat(reservations, hasSize(0));
   }
 
   @SuppressWarnings("unchecked")
   @Test
   public void findEntriesShouldFilterOnUserGroups() {
     RichUserDetails richUserDetailsWithGroups = new RichUserDetailsFactory().addUserGroup("urn:mygroup").create();
     Security.setUserDetails(richUserDetailsWithGroups);
 
     PageImpl<Reservation> pageResult = new PageImpl<Reservation>(Lists.newArrayList(new ReservationFactory().create()));
     when(reservationRepoMock.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pageResult);
 
     List<Reservation> reservations = subject.findEntries(0, 20, new Sort("id"));
 
     assertThat(reservations, hasSize(1));
   }
 
   @Test
   public void whenTheUserHasNoGroupsCountShouldBeZero() {
     RichUserDetails richUserDetailsWithoutGroups = new RichUserDetailsFactory().create();
     Security.setUserDetails(richUserDetailsWithoutGroups);
 
     long count = subject.countForUser(richUserDetailsWithoutGroups);
 
     assertThat(count, is(0L));
   }
 
   @Test
   public void countShouldFilterOnUserGroups() {
     RichUserDetails richUserDetailsWithGroups = new RichUserDetailsFactory().addUserGroup("urn:mygroup").create();
     Security.setUserDetails(richUserDetailsWithGroups);
 
     when(reservationRepoMock.count(any(Specification.class))).thenReturn(5L);
 
     long count = subject.countForUser(richUserDetailsWithGroups);
 
     assertThat(count, is(5L));
   }
 
   @Test(expected = IllegalStateException.class)
   public void reserveDifferentVirtualResrouceGroupsShouldGiveAnIllegalStateException() {
     VirtualResourceGroup vrg1 = new VirtualResourceGroupFactory().create();
     VirtualResourceGroup vrg2 = new VirtualResourceGroupFactory().create();
     VirtualPort source = new VirtualPortFactory().setVirtualResourceGroup(vrg1).create();
     VirtualPort destination = new VirtualPortFactory().setVirtualResourceGroup(vrg2).create();
 
     Reservation reservation = new ReservationFactory().setSourcePort(source)
         .setDestinationPort(destination).create();
 
     subject.create(reservation);
   }
 
   @Test
   public void reserveSameVirtualResourceGroupsShouldBeFine() throws InterruptedException, ExecutionException {
     final Reservation reservation = new ReservationFactory().create();
 
     subject.create(reservation);
 
     verify(reservationRepoMock).save(reservation);
     verify(reservationToNbiMock).submitNewReservation(reservation.getId(), true);
   }
 
   @Test
   public void reserveShouldFillStartTime() {
     final Reservation reservation = new ReservationFactory().setStartDateTime(null).create();
 
     subject.create(reservation);
 
     assertThat(reservation.getStartDateTime(), notNullValue());
     assertThat(reservation.getStartDateTime(), isAfterNow());
   }
 
 
   @Test(expected = IllegalStateException.class)
   public void updatingDifferentVirtualResrouceGroupsShouldGiveAnIllegalStateException() {
     VirtualResourceGroup vrg1 = new VirtualResourceGroupFactory().create();
     VirtualResourceGroup vrg2 = new VirtualResourceGroupFactory().create();
     VirtualPort source = new VirtualPortFactory().setVirtualResourceGroup(vrg1).create();
     VirtualPort destination = new VirtualPortFactory().setVirtualResourceGroup(vrg2).create();
 
     Reservation reservation = new ReservationFactory().setSourcePort(source)
         .setDestinationPort(destination).create();
 
     subject.update(reservation);
   }
 
   @Test
   public void udpateShouldSave() {
     Reservation reservation = new ReservationFactory().create();
 
     subject.update(reservation);
 
     verify(reservationRepoMock).save(reservation);
   }
 
   @Test
   public void cancelAReservationAsAUserInGroupShouldChangeItsStatus() {
     Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.SCHEDULED).create();
     RichUserDetails richUserDetails = new RichUserDetailsFactory().addUserRole()
         .addUserGroup(reservation.getVirtualResourceGroup().getSurfconextGroupId())
         .setDisplayname("Piet Puk").create();
     Security.setUserDetails(richUserDetails);
 
     boolean result = subject.cancel(reservation, richUserDetails);
 
     assertThat(result, is(true));
     assertThat(reservation.getStatus(), is(ReservationStatus.CANCELLED));
     assertThat(reservation.getCancelReason(), containsString(richUserDetails.getDisplayName()));
 
     verify(reservationRepoMock).save(reservation);
     verify(nbiClientMock).cancelReservation(reservation.getReservationId());
   }
 
   @Test
   public void cancelAReservationAsAUserNotInGroupShouldNotChangeItsStatus() {
     Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.SCHEDULED).create();
     RichUserDetails richUserDetails = new RichUserDetailsFactory().addUserRole().create();
     Security.setUserDetails(richUserDetails);
 
     Assert.assertFalse(subject.cancel(reservation, richUserDetails));
     assertThat(reservation.getStatus(), is(ReservationStatus.SCHEDULED));
     verifyZeroInteractions(reservationRepoMock);
   }
 
   @Test
   public void cancelAReservationAsAManagerShouldNotInPrgShouldNotChangeItsStatus() {
     RichUserDetails richUserDetails = new RichUserDetailsFactory().addManagerRole().create();
 
     Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.SCHEDULED).create();
 
     boolean result = subject.cancel(reservation, richUserDetails);
 
     assertThat(result, is(false));
     assertThat(reservation.getStatus(), is(ReservationStatus.SCHEDULED));
 
     verify(reservationRepoMock, never()).save(reservation);
     verify(nbiClientMock, never()).cancelReservation(any(String.class));
   }
 
   @Test
   public void cancelAReservationAsAManagerInSourcePortPrgShouldChangeItsStatus() {
     Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.SCHEDULED).create();
 
     RichUserDetails richUserDetails = new RichUserDetailsFactory()
       .addManagerRole(reservation.getSourcePort().getPhysicalPort().getPhysicalResourceGroup())
       .create();
     Security.setUserDetails(richUserDetails);
 
     boolean cancelled = subject.cancel(reservation, richUserDetails);
 
     assertThat(cancelled, is(true));
     assertThat(reservation.getStatus(), is(ReservationStatus.CANCELLED));
 
     verify(reservationRepoMock).save(reservation);
   }
 
   @Test
   public void cancelAReservationAsAManagerInDestinationPortPrgShouldChangeItsStatus() {
     Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.SCHEDULED).create();
     reservation.getDestinationPort().getPhysicalPort().getPhysicalResourceGroup().setAdminGroup("urn:different");
 
     RichUserDetails richUserDetails = new RichUserDetailsFactory()
         .addManagerRole(reservation.getDestinationPort().getPhysicalPort().getPhysicalResourceGroup())
         .create();
     Security.setUserDetails(richUserDetails);
 
     boolean cancelled = subject.cancel(reservation, richUserDetails);
 
     assertThat(cancelled, is(true));
     assertThat(reservation.getStatus(), is(ReservationStatus.CANCELLED));
 
     verify(reservationRepoMock).save(reservation);
   }
 
   @Test
   public void cancelAReservationAsANocShouldChangeItsStatus() {
     RichUserDetails richUserDetails = new RichUserDetailsFactory().addNocRole().create();
 
     Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.SCHEDULED).create();
 
     boolean cancelled = subject.cancel(reservation, richUserDetails);
 
     assertThat(cancelled, is(true));
     assertThat(reservation.getStatus(), is(ReservationStatus.CANCELLED));
 
     verify(reservationRepoMock).save(reservation);
   }
 
   @Test
   public void cancelAReservationWithStatusFAILEDShouldNotChangeItsStatus() {
     RichUserDetails richUserDetails = new RichUserDetailsFactory().create();
 
     Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.FAILED).create();
 
     boolean cancelled = subject.cancel(reservation, richUserDetails);
 
     assertThat(cancelled, is(false));
     assertThat(reservation.getStatus(), is(ReservationStatus.FAILED));
 
     verify(reservationRepoMock, never()).save(reservation);
   }
 
   @Test
   public void cancelAFailedReservationShouldNotChangeItsStatus() {
     Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.FAILED).create();
     subject.cancel(reservation, Security.getUserDetails());
     assertThat(reservation.getStatus(), is(ReservationStatus.FAILED));
     verifyZeroInteractions(reservationRepoMock);
   }
 
   @Test
   public void startAndEndShouldBeInWholeMinutes() {
     LocalDateTime startDateTime = LocalDateTime.now().withSecondOfMinute(1);
     LocalDateTime endDateTime = LocalDateTime.now().withSecondOfMinute(1);
 
     Reservation reservation = new ReservationFactory().setStartDateTime(startDateTime).setEndDateTime(endDateTime)
         .create();
 
     when(reservationRepoMock.save(reservation)).thenReturn(reservation);
 
     subject.create(reservation);
 
     assertThat(reservation.getStartDateTime(), is(startDateTime.withSecondOfMinute(0).withMillisOfSecond(0)));
     assertThat(reservation.getEndDateTime(), is(endDateTime.withSecondOfMinute(0).withMillisOfSecond(0)));
   }
 
   @Test
   public void transformToFlattenedReservations() {
     List<Reservation> reservations = Lists.newArrayList();
     for (int i = 0; i < 10; i++) {
       reservations.add(new ReservationFactory().create());
     }
 
     final Collection<ReservationArchive> flattenedReservations = subject
         .transformToReservationArchives(reservations);
 
     assertThat(flattenedReservations, hasSize(10));
   }
 
 }
