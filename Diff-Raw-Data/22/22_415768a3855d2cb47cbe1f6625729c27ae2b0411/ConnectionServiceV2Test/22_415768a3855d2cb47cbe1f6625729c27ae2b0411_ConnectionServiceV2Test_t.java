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
 package nl.surfnet.bod.service;
 
 import static org.hamcrest.Matchers.contains;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.hasSize;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.Collections;
 import java.util.List;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 
 import nl.surfnet.bod.domain.ConnectionV2;
 import nl.surfnet.bod.domain.NsiRequestDetails;
 import nl.surfnet.bod.nsi.v2.ConnectionServiceRequesterV2;
 import nl.surfnet.bod.repo.ConnectionV2Repo;
 import nl.surfnet.bod.support.ConnectionV2Factory;
 import nl.surfnet.bod.support.NsiRequestDetailsFactory;
 import nl.surfnet.bod.support.RichUserDetailsFactory;
 import nl.surfnet.bod.util.Environment;
 import nl.surfnet.bod.web.security.RichUserDetails;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStateChangeRequestType;
 import org.ogf.schemas.nsi._2013._04.connection.types.ErrorEventType;
 import org.ogf.schemas.nsi._2013._04.connection.types.NotificationBaseType;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ConnectionServiceV2Test {
 
   @InjectMocks private ConnectionServiceV2 subject;
 
   @Mock private ConnectionV2Repo connectionRepoMock;
   @Mock private ConnectionServiceRequesterV2 connectionServiceRequesterMock;
   @Mock private Environment bodEnvironmentMock;
 
   @Test
   public void reserve_with_a_duplicate_global_reservation_id_should_give_validation_exception() throws ConnectionServiceV2.ReservationCreationException {
     String providerNsa = "nsa:surfnet.nl";
     ConnectionV2 connection = new ConnectionV2Factory().setGlobalReservationId("GlobalReservationId").setProviderNsa(providerNsa).create();
     NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
     RichUserDetails userDetails = new RichUserDetailsFactory().create();
 
     when(bodEnvironmentMock.getNsiProviderNsa()).thenReturn(providerNsa);
     when(connectionRepoMock.findByGlobalReservationId("GlobalReservationId")).thenReturn(connection);
 
     try {
       subject.reserve(connection, requestDetails, userDetails);
       fail("Expected a ValicationException");
     } catch (ConnectionServiceV2.ReservationCreationException e) {
       assertThat(e.getMessage(), containsString("GlobalReservationId"));
     }
 
     verify(connectionRepoMock, never()).save(any(ConnectionV2.class));
   }
 
   @Test
   public void querySummarySync_should_return_an_empty_list_when_no_connection_objects_where_found() {
     final String nonExistingConnectionId = "1";
 
     when(connectionRepoMock.findByConnectionId(nonExistingConnectionId)).thenReturn(null);
 
     List<ConnectionV2> connections = subject.querySummarySync(ImmutableList.of(nonExistingConnectionId), Collections.<String>emptyList(), "requesterNsa");
 
     assertThat(connections, hasSize(0));
   }
 
   @Test
   public void querySummarySync_should_query_by_connection_id() {
     ConnectionV2 connection = new ConnectionV2Factory().create();
     when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(connection);
 
     List<ConnectionV2> connections = subject.querySummarySync(ImmutableList.of("connectionId"), Collections.<String>emptyList(), "requesterNsa");
 
     assertThat(connections, contains(connection));
   }
 
   @Test
   public void querySummarySync_should_query_by_global_reservation_id() {
     ConnectionV2 connection = new ConnectionV2Factory().create();
     when(connectionRepoMock.findByGlobalReservationId("globalReservationId")).thenReturn(connection);
 
     List<ConnectionV2> connections = subject.querySummarySync(Collections.<String>emptyList(), ImmutableList.of("globalReservationId"), "requesterNsa");
 
     assertThat(connections, contains(connection));
   }
 
   @Test
   public void querySummarySync_should_query_by_requester_nsa_when_both_global_reservation_id_and_connection_id_are_empty() {
     ConnectionV2 connection = new ConnectionV2Factory().create();
     when(connectionRepoMock.findByRequesterNsa("requesterNsa")).thenReturn(ImmutableList.of(connection));
 
     List<ConnectionV2> connections = subject.querySummarySync(Collections.<String>emptyList(), Collections.<String>emptyList(), "requesterNsa");
 
     assertThat(connections, contains(connection));
   }
 
   @Test
   public void queryNotification_should_not_return_dataplane_status_changes() {
     final String connectionId = "f00f";
     final NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
     ConnectionV2 connection = new ConnectionV2Factory().create();
     connection.setConnectionId(connectionId);
 
     connection.addNotification(new ErrorEventType().withNotificationId(0));
     connection.addNotification(new DataPlaneStateChangeRequestType().withNotificationId(1));
 
     when(connectionRepoMock.findByConnectionId(connectionId)).thenReturn(connection);
     List<NotificationBaseType> notifications = subject.queryNotification(connectionId, Optional.<Integer>absent(), Optional.<Integer>absent(), requestDetails);
 
     assertTrue(notifications.size() == 1);
     assertFalse(notifications.get(0) instanceof DataPlaneStateChangeRequestType);
   }
 
   @Test
   public void queryNotification_ranges_are_optional() {
     final String connectionId = "f00f";
     final NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
     ConnectionV2 connection = new ConnectionV2Factory().create();
     connection.setConnectionId(connectionId);
 
     for (int i = 0; i < 4; i++){
       ErrorEventType notification = new ErrorEventType();
       notification.setNotificationId(i);
       connection.addNotification(notification);
     }
 
     when(connectionRepoMock.findByConnectionId(connectionId)).thenReturn(connection);
     List<NotificationBaseType> notifications = subject.queryNotification(connectionId, Optional.<Integer>absent(), Optional.<Integer>absent(), requestDetails);
 
     assertTrue(notifications.size() == 4);
   }
 
   @Test
   public void queryNotification_should_only_post_back_notifications_that_are_in_range() {
     final Optional<Integer> lowerBound = Optional.of(2);
     final Optional<Integer> upperBound = Optional.of(3);
     final String connectionId = "f00f";
     final NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
     ConnectionV2 connection = new ConnectionV2Factory().create();
     connection.setConnectionId(connectionId);
 
     for (int i = lowerBound.get() - 1; i <= upperBound.get() + 1; i++){
       ErrorEventType notification = new ErrorEventType();
       notification.setNotificationId(i);
       connection.addNotification(notification);
     }
 
     when(connectionRepoMock.findByConnectionId(connectionId)).thenReturn(connection);
     List<NotificationBaseType> notifications = subject.queryNotification(connectionId, lowerBound, upperBound, requestDetails);
 
     assertTrue(notifications.size() == 2);
   }

  @Test
  public void queryNotification_should_return_empty_list_when_connection_not_found(){
    final String connectionId = "f00f";
    final NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();

    when(connectionRepoMock.findByConnectionId(connectionId)).thenReturn(null);
    List<NotificationBaseType> notifications = subject.queryNotification(connectionId, Optional.<Integer>absent(), Optional.<Integer>absent(), requestDetails);

    assertTrue(notifications.size() == 0);
  }
 }
