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
 package nl.surfnet.bod.web.user;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.service.ReservationService;
 import nl.surfnet.bod.service.VirtualResourceGroupService;
 import nl.surfnet.bod.support.ModelStub;
 import nl.surfnet.bod.support.ReservationFactory;
 import nl.surfnet.bod.support.ReservationFilterViewFactory;
 import nl.surfnet.bod.support.RichUserDetailsFactory;
 import nl.surfnet.bod.support.VirtualPortFactory;
 import nl.surfnet.bod.support.VirtualResourceGroupFactory;
 import nl.surfnet.bod.web.base.MessageView;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 import nl.surfnet.bod.web.view.ElementActionView;
 import nl.surfnet.bod.web.view.ReservationFilterView;
 import nl.surfnet.bod.web.view.ReservationView;
 
 import org.joda.time.DurationFieldType;
 import org.joda.time.LocalDateTime;
 import org.joda.time.Period;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.context.MessageSource;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.domain.Sort.Direction;
 import org.springframework.ui.Model;
 
 import com.google.common.collect.Lists;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.hasKey;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 import static org.hamcrest.Matchers.nullValue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ReservationControllerTest {
 
   private static final String INFO_AT_LEAST_TWO_PORTS = "at least two ports";
 
   @InjectMocks
   private ReservationController subject;
 
   @Mock
   private VirtualResourceGroupService virtualResourceGroupServiceMock;
 
   @Mock
   private ReservationService reservationServiceMock;
 
   @Mock
   private MessageSource messageSource;
 
   @Mock
   private final ReservationFilterViewFactory reservationFilterViewFactoryMock =
     when(mock(ReservationFilterViewFactory.class).create(anyString())).thenCallRealMethod().getMock();
 
   private final RichUserDetails user = new RichUserDetailsFactory().create();
   private final Model model = new ModelStub();
 
   @Before
   public void onSetup() {
     Security.setUserDetails(user);
   }
 
   @Test
   public void newReservationShouldHaveDefaults() {
     VirtualResourceGroup group = new VirtualResourceGroupFactory().create();
     VirtualPort sourcePort = new VirtualPortFactory().setMaxBandwidth(8000).setVirtualResourceGroup(group).create();
     VirtualPort destPort = new VirtualPortFactory().setMaxBandwidth(4000).setVirtualResourceGroup(group).create();
 
     // Make sure source and destination have some
     group.setVirtualPorts(Lists.newArrayList(sourcePort, destPort));
 
     when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(Lists.newArrayList(group));
 
     subject.createForm(null, model);
 
     assertThat(model.asMap(), hasKey("reservation"));
     assertThat(model.asMap(), hasKey("virtualPorts"));
     assertThat(model.asMap(), hasKey("virtualResourceGroups"));
 
     Reservation reservation = (Reservation) model.asMap().get("reservation");
     assertThat(reservation.getStartDateTime(), not(nullValue()));
     assertThat(reservation.getEndDateTime(), not(nullValue()));
     assertThat(reservation.getSourcePort(), is(sourcePort));
     assertThat(reservation.getDestinationPort(), is(destPort));
     assertThat(reservation.getBandwidth(), is(2000));
   }
 
   @Test
   public void createFormWithoutAnyGroupsShouldGiveSpecialPage() {
     when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(
         Collections.<VirtualResourceGroup> emptyList());
 
     String page = subject.createForm(null, model);
 
     assertThat(page, is("message"));
     assertThat(model.asMap(), hasKey(MessageView.MODEL_KEY));
   }
 
   @Test
   public void reservationShouldHaveDefaultDuration() {
     VirtualResourceGroup group = new VirtualResourceGroupFactory().create();
     VirtualPort sourcePort = new VirtualPortFactory().setVirtualResourceGroup(group).create();
     VirtualPort destPort = new VirtualPortFactory().setVirtualResourceGroup(group).create();
 
     // Make sure source and destination have some
     group.setVirtualPorts(Lists.newArrayList(sourcePort, destPort));
     when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(Lists.newArrayList(group));
 
     subject.createForm(null, model);
 
     Reservation reservation = (Reservation) model.asMap().get(ReservationController.MODEL_KEY);
 
     Period period = new Period(reservation.getStartDateTime().toDate().getTime(), reservation.getEndDateTime().toDate()
         .getTime());
 
     assertThat(period.get(DurationFieldType.minutes()),
         is(ReservationController.DEFAULT_RESERVATON_DURATION.get(DurationFieldType.minutes())));
   }
 
   @Test
   public void listShouldSetListOnModel() {
     Reservation reservation = new ReservationFactory().setStartDateTime(LocalDateTime.now().plusDays(1)).create();
 
     when(
         reservationServiceMock.findEntriesForUserUsingFilter(any(RichUserDetails.class),
             any(ReservationFilterView.class), eq(0), eq(Integer.MAX_VALUE), any(Sort.class))).thenReturn(
         Lists.newArrayList(reservation));
 
    subject.search(0, "name", "asc", "2012", model);
 
     assertThat(model.asMap(), hasKey("list"));
     assertThat(model.asMap(), hasKey("sortProperty"));
     assertThat(model.asMap(), hasKey("sortDirection"));
   }
 
   @Test
   public void listWithNonExistingSortProperty() {
     Reservation reservation = new ReservationFactory().create();
     List<Reservation> reservations = Lists.newArrayList(reservation);
     List<ReservationView> reservationViews = Lists.newArrayList(new ReservationView(reservation, new ElementActionView(
         true)));
 
     when(
         reservationServiceMock.findEntriesForUserUsingFilter(any(RichUserDetails.class),
             any(ReservationFilterView.class), anyInt(), anyInt(), any(Sort.class))).thenReturn(
         reservations);
 
     when(reservationServiceMock.transformToView(reservations, user)).thenReturn(reservationViews);
     when(reservationServiceMock.pageList(anyInt(), anyInt(), eq(reservationViews))).thenCallRealMethod();
 
     subject.filter(1, "nonExistingProperty", "nonExistingDirection", "", "2012", model);
 
     assertThat(model.asMap(), hasKey("list"));
     assertThat(model.asMap(), hasKey("sortProperty"));
     assertThat(model.asMap(), hasKey("sortDirection"));
 
     assertThat(model.asMap().get("sortDirection"), is(Object.class.cast(Direction.ASC)));
     assertThat(model.asMap().get("sortProperty"), is(Object.class.cast("startDateTime")));
     assertThat(((List<?>) model.asMap().get("list")), hasSize(1));
   }
 
   @Test
   public void listWithNonExistingFilter() {
     when(reservationFilterViewFactoryMock.create(anyString())).thenCallRealMethod();
 
     String page = subject.filter(1, "name", "asc", "", "nonExistingFilter", model);
 
     assertThat(page, is("redirect:../"));
   }
 
   @Test
   public void lessThenTwoVirtualPortsShouldShowInfoMessage() {
     VirtualResourceGroup group = new VirtualResourceGroupFactory().addVirtualPorts(new VirtualPortFactory().create())
         .create();
 
     when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(Lists.newArrayList(group));
     when(
         messageSource.getMessage(eq("info_reservation_need_two_virtual_ports_message"), any(Object[].class),
             any(Locale.class))).thenReturn(INFO_AT_LEAST_TWO_PORTS);
 
     String view = subject.createForm(null, model);
 
     assertThat(model.asMap().containsKey(MessageView.MODEL_KEY), is(true));
     assertThat(((MessageView) model.asMap().get(MessageView.MODEL_KEY)).getMessage(),
         containsString(INFO_AT_LEAST_TWO_PORTS));
     assertThat(view, is(MessageView.PAGE_URL));
   }
 
 }
