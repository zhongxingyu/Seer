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
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import nl.surfnet.bod.domain.*;
 import nl.surfnet.bod.nbi.generated.NetworkMonitoringServiceFault;
 import nl.surfnet.bod.nbi.generated.NetworkMonitoringService_v30Stub;
 import nl.surfnet.bod.nbi.generated.ResourceAllocationAndSchedulingService_v30Stub;
 import nl.surfnet.bod.support.PhysicalPortFactory;
 import nl.surfnet.bod.support.ReservationFactory;
 import nl.surfnet.bod.support.VirtualPortFactory;
 import nl.surfnet.bod.support.VirtualResourceGroupFactory;
 
 import org.apache.xmlbeans.XmlException;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.SecurityDocument;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointRequestDocument;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointResponseDocument;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsRequestDocument;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsResponseDocument;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleRequestDocument;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleResponseDocument;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.PathRequestT;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidProtectionTypeT;
 
 @RunWith(MockitoJUnitRunner.class)
 public class NbiOpenDracWsClientTest {
 
   private NbiOpenDracWsClient subject;
 
   @Mock
   private NetworkMonitoringService_v30Stub networkingServiceMock;
   @Mock
   private ResourceAllocationAndSchedulingService_v30Stub schedulingServiceMock;
 
   private QueryEndpointsResponseDocument endpointsResponse;
   private QueryEndpointResponseDocument endpointResponse;
   private VirtualPort sourcePort;
   private VirtualPort destPort;
 
   @Before
   public void init() throws Exception {
     subject = spy(new NbiOpenDracWsClient());
     when(subject.getNetworkMonitoringService()).thenReturn(networkingServiceMock);
    when(subject.getResourceAllocationAndSchedulingService()).thenReturn(schedulingServiceMock);
 
     endpointsResponse = QueryEndpointsResponseDocument.Factory.parse(new File(
         "src/test/resources/opendrac/queryEndpointsResponse.xml"));
     endpointResponse = QueryEndpointResponseDocument.Factory.parse(new File(
         "src/test/resources/opendrac/queryEndpointResponse.xml"));
 
     VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();
     sourcePort = new VirtualPortFactory()
         .setPhysicalPort(new PhysicalPortFactory().setNmsPortId("00-21-E1-D9-CC-70_ETH-1-36-4").create())
         .setVirtualResourceGroup(vrg).create();
     destPort = new VirtualPortFactory()
         .setPhysicalPort(new PhysicalPortFactory().setNmsPortId("00-21-E1-D9-CC-70_ETH-1-36-4").create())
         .setVirtualResourceGroup(vrg).create();
 
     when(networkingServiceMock.queryEndpoints(any(QueryEndpointsRequestDocument.class), any(SecurityDocument.class)))
         .thenReturn(endpointsResponse);
 
     when(networkingServiceMock.queryEndpoint(any(QueryEndpointRequestDocument.class), any(SecurityDocument.class)))
         .thenReturn(endpointResponse);
   }
 
   @Test
   public void findAllPhysicalPorts() throws XmlException, NetworkMonitoringServiceFault, IOException {
     when(networkingServiceMock.queryEndpoints(any(QueryEndpointsRequestDocument.class), any(SecurityDocument.class)))
         .thenReturn(endpointsResponse);
     when(networkingServiceMock.queryEndpoint(any(QueryEndpointRequestDocument.class), any(SecurityDocument.class)))
         .thenReturn(endpointResponse);
 
     List<PhysicalPort> ports = subject.findAllPhysicalPorts();
 
     assertThat(ports, hasSize(1));
   }
 
   @Test
   public void shouldCreateReservationWithGivenStartTime() throws Exception {
     Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort).create();
 
     CreateReservationScheduleRequestDocument schedule = subject.createReservationScheduleRequest(reservation, true);
 
     assertThat(schedule.getCreateReservationScheduleRequest().getReservationSchedule().getStartTime().getTime(),
         is(reservation.getStartDateTime().toDate()));
   }
 
   @Test
   public void createReservationShouldFailWithMessage() throws Exception {
     Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort).create();
     CreateReservationScheduleResponseDocument responseDocument = CreateReservationScheduleResponseDocument.Factory
         .parse(new File("src/test/resources/opendrac/createReservationScheduleResponse.xml"));
 
     when(
         schedulingServiceMock.createReservationSchedule(any(CreateReservationScheduleRequestDocument.class),
             any(SecurityDocument.class))).thenReturn(responseDocument);
 
     Reservation scheduledReservation = subject.createReservation(reservation, true);
 
     assertThat(scheduledReservation.getStatus(), is(ReservationStatus.NOT_ACCEPTED));
     assertThat(scheduledReservation.getFailedReason(), is("No available bandwidth on source port"));
   }
 
   @Test
   public void createReservationWithoutEndTime() throws Exception {
     Reservation foreverReservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort)
         .setEndDateTime(null).create();
 
     CreateReservationScheduleRequestDocument schedule = subject.createReservationScheduleRequest(foreverReservation, true);
 
     assertThat(schedule.getCreateReservationScheduleRequest().getReservationSchedule()
         .getReservationOccurrenceDuration(), is(Integer.MAX_VALUE));
   }
 
   @Test
   public void aProtectedResevationShouldCreateAProtectedPath() throws NetworkMonitoringServiceFault {
     Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort)
         .withProtection().create();
 
     PathRequestT path = subject.createPath(reservation);
 
     assertThat(path.getProtectionType(), is(ValidProtectionTypeT.X_1_PLUS_1_PATH));
   }
 
   @Test
   public void aUnProtectedReservationShouldCreateAUnProtectedPath() throws NetworkMonitoringServiceFault {
     Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort)
         .withoutProtection().create();
 
     PathRequestT path = subject.createPath(reservation);
 
     assertThat(path.getProtectionType(), is(ValidProtectionTypeT.UNPROTECTED));
   }
 
   @Test
   public void shouldNotRequireVlan() {
     assertThat(subject.isVlanRequired("pre" + NbiClient.VLAN_REQUIRED_SELECTOR + "post"), is(false));
   }
 
   @Test
   public void shouldRequireVlan() {
     assertThat(subject.isVlanRequired("pre post"), is(true));
   }
 
   @Test
   public void shouldNotRequireVlanWhenNull() {
     assertThat(subject.isVlanRequired(null), is(false));
   }
 
   @Test
   public void shouldRequireVlanWhenEmpty() {
     assertThat(subject.isVlanRequired(""), is(true));
   }
 
 }
