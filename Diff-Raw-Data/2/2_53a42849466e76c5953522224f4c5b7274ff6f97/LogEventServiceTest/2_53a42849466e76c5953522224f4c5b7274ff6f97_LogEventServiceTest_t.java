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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import nl.surfnet.bod.domain.BodRole;
 import nl.surfnet.bod.domain.Institute;
 import nl.surfnet.bod.domain.Loggable;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.event.LogEvent;
 import nl.surfnet.bod.event.LogEventType;
 import nl.surfnet.bod.repo.LogEventRepo;
 import nl.surfnet.bod.support.InstituteFactory;
 import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
 import nl.surfnet.bod.support.ReservationFactory;
 import nl.surfnet.bod.support.RichUserDetailsFactory;
 import nl.surfnet.bod.support.VirtualResourceGroupFactory;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security.RoleEnum;
 
 import org.joda.time.DateTimeUtils;
 import org.joda.time.LocalDateTime;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.slf4j.Logger;
 
 import com.google.common.collect.Lists;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.verifyZeroInteractions;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class LogEventServiceTest {
 
   private static final String GROUP_ID = "urn:groupie";
   private static final String LOG_DETAILS = "The reason why";
 
   @Mock
   private LogEventRepo repoMock;
 
   @Mock
   private Logger logMock;
 
   @Mock
   private RichUserDetails userMock;
 
   @Mock
   private PhysicalResourceGroupService physicalResourceGroupService;
 
   @InjectMocks
   private LogEventService subject;
 
   private RichUserDetails user = new RichUserDetailsFactory().addUserGroup(GROUP_ID).create();
 
   private VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();
 
   @Test
   public void shouldCreateLogEvent() {
     try {
       LocalDateTime now = LocalDateTime.now();
       DateTimeUtils.setCurrentMillisFixed(now.toDate().getTime());
 
       LogEvent logEvent = subject.createLogEvent(user, GROUP_ID, LogEventType.CREATE, vrg, LOG_DETAILS);
 
       assertThat(logEvent.getUserId(), is(user.getUsername()));
      assertThat(logEvent.getAdminGroup(), is(GROUP_ID));
       assertThat(logEvent.getEventTypeWithCorrelationId(), is("Create"));
 
       assertThat(logEvent.getClassName(), is(vrg.getClass().getSimpleName()));
       assertThat(logEvent.getDetails(), is(LOG_DETAILS));
 
       assertThat(logEvent.getSerializedObject(), is(vrg.toString()));
       assertThat(logEvent.getCreated(), is(now));
     }
     finally {
       DateTimeUtils.setCurrentMillisSystem();
     }
   }
 
   @Test
   public void shouldOnlyLogEvent() {
     LogEvent logEvent = new LogEvent(user.getUsername(), GROUP_ID, LogEventType.UPDATE, vrg);
 
     subject.handleEvent(logMock, logEvent);
 
     verify(logMock).info(anyString(), eq(logEvent));
     verifyZeroInteractions(repoMock);
   }
 
   @Test
   public void shouldPersistEventForReservation() {
     LogEvent logEvent = new LogEvent(user.getUsername(), GROUP_ID, LogEventType.UPDATE,
         new ReservationFactory().create());
 
     subject.handleEvent(logMock, logEvent);
 
     verify(logMock).info(anyString(), eq(logEvent));
     verify(repoMock).save(logEvent);
   }
 
   @Test
   public void shouldPersistEventForListOfReservation() {
     List<Reservation> reservations = Lists.newArrayList(new ReservationFactory().create(),
         new ReservationFactory().create());
 
     subject.logUpdateEvent(userMock, reservations, "details");
 
     verify(repoMock, times(2)).save(any(LogEvent.class));
   }
 
   @Test
   public void shouldNotPersistNullDomainObject() {
     LogEvent logEvent = new LogEvent(user.getUsername(), GROUP_ID, LogEventType.UPDATE, null);
 
     subject.handleEvent(logMock, logEvent);
 
     verify(logMock).info(anyString(), eq(logEvent));
     verifyZeroInteractions(repoMock);
   }
 
   @Test
   public void shouldNotPersistEmptyList() {
     LogEvent logEvent = new LogEvent(user.getUsername(), GROUP_ID, LogEventType.UPDATE, Lists.newArrayList());
 
     subject.handleEvent(logMock, logEvent);
 
     verify(logMock).info(anyString(), eq(logEvent));
     verifyZeroInteractions(repoMock);
   }
 
   @Test
   public void shouldDetermineManagerAdminGroupWithoutReservation() {
     PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();
     BodRole manager = BodRole.createManager(prg);
 
     when(userMock.isSelectedManagerRole()).thenReturn(true);
     when(userMock.getSelectedRole()).thenReturn(manager);
 
     when(physicalResourceGroupService.findByInstituteId(prg.getId())).thenReturn(prg);
     prg.getAdminGroup();
     String adminGroup = subject.determineAdminGroup(userMock, null);
 
     assertThat(adminGroup, is(prg.getAdminGroup()));
   }
 
   @Test
   public void shouldDetermineManagerAdminGroupWithReservation() {
     Reservation res = new ReservationFactory().create();
     PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();
     BodRole manager = BodRole.createManager(prg);
 
     when(userMock.isSelectedManagerRole()).thenReturn(true);
     when(userMock.getSelectedRole()).thenReturn(manager);
 
     when(physicalResourceGroupService.findByInstituteId(prg.getId())).thenReturn(prg);
     prg.getAdminGroup();
     String adminGroup = subject.determineAdminGroup(userMock, res);
 
     assertThat(adminGroup, is(res.getVirtualResourceGroup().getSurfconextGroupId()));
   }
 
   @Test
   public void shouldDetermineNocGroupWithoutReservation() {
     when(userMock.isSelectedNocRole()).thenReturn(true);
 
     String adminGroup = subject.determineAdminGroup(userMock, null);
 
     assertThat(adminGroup, is(RoleEnum.NOC_ENGINEER.name()));
   }
 
   @Test
   public void shouldDetermineNocGroupWithReservation() {
     Reservation res = new ReservationFactory().create();
     when(userMock.isSelectedNocRole()).thenReturn(true);
 
     String adminGroup = subject.determineAdminGroup(userMock, res);
 
     assertThat(adminGroup, is(res.getVirtualResourceGroup().getSurfconextGroupId()));
   }
 
   @Test
   public void shouldDetermineReservationGroupForUser() {
     Reservation reservation = new ReservationFactory().create();
     when(userMock.isSelectedUserRole()).thenReturn(true);
 
     String adminGroup = subject.determineAdminGroup(userMock, reservation);
 
     assertThat(adminGroup, is(reservation.getVirtualResourceGroup().getSurfconextGroupId()));
   }
 
   @Test(expected = IllegalStateException.class)
   public void shouldThrowForForUserWithoutReservation() {
     when(userMock.isSelectedUserRole()).thenReturn(true);
 
     subject.determineAdminGroup(userMock, null);
   }
 
   @Test
   public void shouldRelatedItemsInList() {
     List<Loggable> institutes = new ArrayList<>();
     institutes.add(new InstituteFactory().create());
     institutes.add(new InstituteFactory().create());
 
     when(userMock.isSelectedNocRole()).thenReturn(true);
     List<LogEvent> logEvents = subject
         .createLogEvents(userMock, institutes, LogEventType.UPDATE, "show my the details");
 
     assertThat(logEvents, hasSize(2));
     assertThat(logEvents.get(0).getEventTypeWithCorrelationId().toString(), is("Update 1-2"));
     assertThat(logEvents.get(1).getEventTypeWithCorrelationId().toString(), is("Update 2-2"));
   }
 }
