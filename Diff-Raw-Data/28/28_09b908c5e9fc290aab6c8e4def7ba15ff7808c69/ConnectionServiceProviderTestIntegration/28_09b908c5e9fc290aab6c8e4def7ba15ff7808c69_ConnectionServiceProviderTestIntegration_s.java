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
 package nl.surfnet.bod.nsi.v1sc;
 
 import static nl.surfnet.bod.nsi.NsiConstants.URN_PROVIDER_NSA;
 import static nl.surfnet.bod.nsi.NsiConstants.URN_STP_V1;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.nullValue;
 import static org.junit.Assert.assertFalse;
 
 import java.util.EnumSet;
 import java.util.concurrent.Callable;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
 import javax.xml.datatype.DatatypeConfigurationException;
 
 import com.google.common.base.Optional;
 import com.jayway.awaitility.Awaitility;
 import nl.surfnet.bod.AppComponents;
 import nl.surfnet.bod.config.IntegrationDbConfiguration;
 import nl.surfnet.bod.domain.BodRole;
 import nl.surfnet.bod.domain.ConnectionV1;
 import nl.surfnet.bod.domain.Institute;
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.ReservationStatus;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.domain.oauth.NsiScope;
 import nl.surfnet.bod.nbi.opendrac.NbiOpenDracOfflineClient;
 import nl.surfnet.bod.nbi.opendrac.ReservationPoller;
 import nl.surfnet.bod.repo.ConnectionV1Repo;
 import nl.surfnet.bod.repo.InstituteRepo;
 import nl.surfnet.bod.repo.PhysicalPortRepo;
 import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
 import nl.surfnet.bod.repo.VirtualPortRepo;
 import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
 import nl.surfnet.bod.service.DatabaseTestHelper;
 import nl.surfnet.bod.service.ReservationService;
 import nl.surfnet.bod.support.ConnectionServiceProviderFactory;
 import nl.surfnet.bod.support.MockHttpServer;
 import nl.surfnet.bod.support.PhysicalPortFactory;
 import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
 import nl.surfnet.bod.support.ReserveRequestTypeFactory;
 import nl.surfnet.bod.support.RichUserDetailsFactory;
 import nl.surfnet.bod.support.VirtualPortFactory;
 import nl.surfnet.bod.support.VirtualResourceGroupFactory;
 import nl.surfnet.bod.util.TestHelper;
 import nl.surfnet.bod.util.TestHelper.TimeTraveller;
 import nl.surfnet.bod.util.XmlUtils;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 import org.jadira.usertype.dateandtime.joda.PersistentDateTime;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.ProvisionRequestType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.TerminateRequestType;
 import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
 import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
 import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ScheduleType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.test.annotation.DirtiesContext;
 import org.springframework.test.annotation.DirtiesContext.ClassMode;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { AppComponents.class, IntegrationDbConfiguration.class })
 @DirtiesContext(classMode = ClassMode.AFTER_CLASS)
 @ActiveProfiles({"opendrac-offline", "idd-offline"})
 public class ConnectionServiceProviderTestIntegration {
 
   private static MockHttpServer nsiRequester = new MockHttpServer(ConnectionServiceProviderFactory.PORT);
 
   @Resource private ConnectionServiceProviderV1Ws nsiProvider;
   @Resource private VirtualPortRepo virtualPortRepo;
   @Resource private PhysicalPortRepo physicalPortRepo;
   @Resource private VirtualResourceGroupRepo virtualResourceGroupRepo;
   @Resource private PhysicalResourceGroupRepo physicalResourceGroupRepo;
   @Resource private InstituteRepo instituteRepo;
   @Resource private ConnectionV1Repo connectionRepo;
   @Resource private ReservationService reservationService;
  @Resource private EntityManagerFactory entityManagerFactory;
   @Resource private ReservationPoller reservationPoller;
   @Resource private NbiOpenDracOfflineClient nbiOfflineClient;
 
   private static final String URN_REQUESTER_NSA = "urn:requester";
   private final String virtualResourceGroupName = "nsi:group";
   private VirtualPort sourceVirtualPort;
   private VirtualPort destinationVirtualPort;
   private final RichUserDetails userDetails = new RichUserDetailsFactory()
     .setScopes(EnumSet.allOf(NsiScope.class))
     .addBodRoles(BodRole.createUser())
     .addUserGroup("test").create();
 
   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
     DatabaseTestHelper.clearIntegrationDatabaseSkipBaseData();
     nsiRequester.addResponse("/bod/nsi/requester", new ClassPathResource("web/services/nsi/mockNsiReservationFailedResponse.xml"));
     nsiRequester.startServer();
   }
 
   @AfterClass
   public static void tearDownAfterClass() throws Exception {
     nsiRequester.stopServer();
   }
 
   @Before
   public void setup() {
     setLoggedInUser(userDetails);
 
     nbiOfflineClient.setShouldSleep(false);
 
     PhysicalResourceGroup physicalResourceGroup = createPhysicalResourceGroup();
     VirtualResourceGroup virtualResourceGroup = createVirtualResourceGroup();
 
     PhysicalPort sourcePp = createPhysicalPort(physicalResourceGroup);
     PhysicalPort destinationPp = createPhysicalPort(physicalResourceGroup);
 
     this.sourceVirtualPort = createVirtualPort(virtualResourceGroup, sourcePp);
     this.destinationVirtualPort = createVirtualPort(virtualResourceGroup, destinationPp);
   }
 
   @After
   public void teardown() {
     nsiRequester.clearRequests();
     DatabaseTestHelper.clearIntegrationDatabaseSkipBaseData();
   }
 
   @Test
   public void shouldReserveProvisionTerminate() throws Exception {
     ReserveRequestType reservationRequest = createReserveRequest(Optional.of(DateTime.now().plusDays(1)), Optional.<DateTime> absent());
     String connectionId = reservationRequest.getReserve().getReservation().getConnectionId();
 
     // reserve
     GenericAcknowledgmentType reserveAcknowledgment = nsiProvider.reserve(reservationRequest);
     assertThat(reserveAcknowledgment.getCorrelationId(), is(reservationRequest.getCorrelationId()));
 
     awaitReserveConfirmed();
     assertStates(connectionId, ReservationStatus.RESERVED, ConnectionStateType.RESERVED);
 
     // terminate
     TerminateRequestType terminateRequest = createTerminateRequest(connectionId);
     GenericAcknowledgmentType terminateAck = nsiProvider.terminate(terminateRequest);
     assertThat(terminateAck.getCorrelationId(), is(terminateRequest.getCorrelationId()));
 
     awaitTerminateConfirmed();
     assertStates(connectionId, ReservationStatus.CANCELLED, ConnectionStateType.TERMINATED);
   }
 
 
   @Test
   public void shouldSetEndDateWhenNoneIsPresentOrBeforeStart() throws Exception {
     DateTime start = DateTime.now().plusHours(1).withSecondOfMinute(0).withMillisOfSecond(0);
     ReserveRequestType reservationRequest = createReserveRequest(Optional.of(start), Optional.<DateTime> absent());
     String connectionId = reservationRequest.getReserve().getReservation().getConnectionId();
 
     GenericAcknowledgmentType reserveAcknowledgment = nsiProvider.reserve(reservationRequest);
     assertThat(reserveAcknowledgment.getCorrelationId(), is(reservationRequest.getCorrelationId()));
 
     awaitReserveConfirmed();
 
 
     ConnectionV1 connection = connectionRepo.findByConnectionId(connectionId);
    internalReservationStateIs(connection.getReservation().getId(), ReservationStatus.RESERVED);
     Reservation reservation = reservationService.find(connection.getReservation().getId());
 
     assertThat(reservation.getStartDateTime(), is(start));
     assertThat(connection.getStartTime().get(), is(start));
 
     assertThat(reservation.getEndDateTime(), nullValue());
     assertFalse(connection.getEndTime().isPresent());
   }
 
   /**
    * After reading from the database the timestamps are converted to the default
    * jvm timezone by the @See {@link PersistentDateTime} annotation on the
    * timestamp fields
    */
   @Test
   public void shouldReserveAndConvertTimeZoneToJVMDefault() throws Exception {
     int jvmOffesetInMillis = DateTimeZone.getDefault().getOffset(new DateTime().getMillis());
     int offsetInHours = -4;
     DateTime startNowPlusHour = new DateTime().plusHours(1).withSecondOfMinute(0).withMillisOfSecond(0)
       .withZoneRetainFields(DateTimeZone.forOffsetHours(offsetInHours));
 
     ReserveRequestType reservationRequest = createReserveRequest(Optional.of(startNowPlusHour), Optional.<DateTime> absent());
     String connectionId = reservationRequest.getReserve().getReservation().getConnectionId();
 
     GenericAcknowledgmentType reserveAcknowledgment = nsiProvider.reserve(reservationRequest);
     assertThat(reserveAcknowledgment.getCorrelationId(), is(reservationRequest.getCorrelationId()));
 
     awaitReserveConfirmed();
 
     ConnectionV1 connection = connectionRepo.findByConnectionId(connectionId);
    internalReservationStateIs(connection.getReservation().getId(), ReservationStatus.RESERVED);
     Reservation reservation = reservationService.find(connection.getReservation().getId());
 
     assertThat(reservation.getStartDateTime().getZone().getOffset(reservation.getStartDateTime().getMillis()), is(jvmOffesetInMillis));
     assertThat(connection.getStartTime().get().getZone().getOffset(connection.getStartTime().get().getMillis()), is(jvmOffesetInMillis));
     assertThat(reservation.getStartDateTime().withZone(DateTimeZone.UTC).getMillis(), is(startNowPlusHour.withZone(DateTimeZone.UTC).getMillis()));
     assertThat(connection.getStartTime().get().withZone(DateTimeZone.UTC).getMillis(), is(startNowPlusHour.withZone(DateTimeZone.UTC).getMillis()));
   }
 
   @Test
   public void queryShouldGiveAConfirm() throws Exception {
     ReserveRequestType reserveRequest = createReserveRequest();
     String connectionId = reserveRequest.getReserve().getReservation().getConnectionId();
 
     nsiProvider.reserve(reserveRequest);
     awaitReserveConfirmed();
 
     QueryRequestType queryRequest = createQueryRequest(connectionId);
     GenericAcknowledgmentType genericAcknowledgment = nsiProvider.query(queryRequest);
 
     assertThat(genericAcknowledgment.getCorrelationId(), is(queryRequest.getCorrelationId()));
 
     String response = awaitQueryConfirmed();
     assertThat(response, containsString(queryRequest.getCorrelationId()));
     assertThat(response, containsString("<connectionId>" + connectionId + "</connectionId"));
     assertThat(response, containsString("<providerNSA>" + URN_PROVIDER_NSA + "</providerNSA>"));
     assertThat(response, containsString("<requesterNSA>" + URN_REQUESTER_NSA + "</requesterNSA>"));
   }
 
   @Test
   public void shouldTerminateWhenCancelledByGUI() throws Exception {
     ReserveRequestType reservationRequest = createReserveRequest();
     final String connectionId = reservationRequest.getReserve().getReservation().getConnectionId();
 
     nsiProvider.reserve(reservationRequest);
 
     awaitReserveConfirmed();
 
     ConnectionV1 connection = connectionRepo.findByConnectionId(connectionId);
     reservationService.cancel(connection.getReservation(), new RichUserDetailsFactory().addNocRole().create());
 
     Awaitility.await().until(internalConnectionStateIs(connectionId, ConnectionStateType.TERMINATED));
     Awaitility.await().until(internalReservationStateIs(connection.getReservation().getId(), ReservationStatus.CANCELLED));
   }
 
 
 
   @Test
   public void terminateShouldFailWhenStateIsTerminated() throws Exception {
     ReserveRequestType reservationRequest = createReserveRequest();
     String connectionId = reservationRequest.getReserve().getReservation().getConnectionId();
 
     nsiProvider.reserve(reservationRequest);
 
     awaitReserveConfirmed();
 
     nsiProvider.terminate(createTerminateRequest(connectionId));
 
     awaitTerminateConfirmed();
 
     nsiProvider.terminate(createTerminateRequest(connectionId));
 
     awaitTerminateFailed();
   }
 
   @Test
   public void provisionShouldFailWhenStateIsTerminated() throws Exception {
     ReserveRequestType reservationRequest = createReserveRequest();
     String connectionId = reservationRequest.getReserve().getReservation().getConnectionId();
 
     nsiProvider.reserve(reservationRequest);
     awaitReserveConfirmed();
 
     nsiProvider.terminate(createTerminateRequest(connectionId));
     awaitTerminateConfirmed();
 
     internalConnectionStateIs(connectionId, ConnectionStateType.TERMINATED);
 
     nsiProvider.provision(createProvisionRequest(connectionId));
 
     awaitProvisionFailed();
 
     internalConnectionStateIs(connectionId, ConnectionStateType.TERMINATED);
   }
 
   @Test
   public void provisionShouldSucceedWhenStateIsProvisioned() throws Exception {
     final ReserveRequestType reservationRequest = createReserveRequest();
     String connectionId = reservationRequest.getReserve().getReservation().getConnectionId();
 
     TestHelper.runInPast(4, TimeUnit.MINUTES,
         new TimeTraveller<GenericAcknowledgmentType>() {
           @Override
           public GenericAcknowledgmentType apply() throws ServiceException {
             GenericAcknowledgmentType ack = nsiProvider.reserve(reservationRequest);
             awaitReserveConfirmed();
             return ack;
           }
         });
 
     ProvisionRequestType provisionRequest = createProvisionRequest(connectionId);
     nsiProvider.provision(provisionRequest);
 
     reservationPoller.pollReservationsThatAreAboutToChangeStatusOrShouldHaveChanged();

     awaitProvisionConfirmed();
 
     nsiProvider.provision(provisionRequest);
     awaitProvisionConfirmed();
   }
 
   @Test
   public void connectionWithNoProvisionShouldMoveToScheduledAfterStartTime() throws Exception {
     final ReserveRequestType reservationRequest = createReserveRequest();
     String connectionId = reservationRequest.getReserve().getReservation().getConnectionId();
 
     TestHelper.runInPast(4, TimeUnit.MINUTES,
         new TimeTraveller<GenericAcknowledgmentType>() {
 
           @Override
           public GenericAcknowledgmentType apply() throws Exception {
             GenericAcknowledgmentType ack = nsiProvider.reserve(reservationRequest);
             awaitReserveConfirmed();
             return ack;
           }
         });
 
     reservationPoller.pollReservationsThatAreAboutToChangeStatusOrShouldHaveChanged();
 
    Thread.sleep(500);
    ConnectionV1 connection = connectionRepo.findByConnectionId(connectionId);
    assertThat(connection.getCurrentState(), is(ConnectionStateType.SCHEDULED));

    //internalConnectionStateIs(connectionId, ConnectionStateType.SCHEDULED); // doing this causes postgres deadlocks
   }
 
   private ReserveRequestType createReserveRequest() throws DatatypeConfigurationException {
     return createReserveRequest(Optional.<DateTime> absent(), Optional.<DateTime> absent());
   }
 
   private ReserveRequestType createReserveRequest(Optional<DateTime> start, Optional<DateTime> end) {
     PathType path = new PathType();
 
     ServiceTerminationPointType dest = new ServiceTerminationPointType();
     dest.setStpId(URN_STP_V1 + ":" + sourceVirtualPort.getId());
     path.setDestSTP(dest);
 
     ServiceTerminationPointType source = new ServiceTerminationPointType();
     source.setStpId(URN_STP_V1 + ":" + destinationVirtualPort.getId());
     path.setSourceSTP(source);
 
     ReserveRequestType reservationRequest = new ReserveRequestTypeFactory().setProviderNsa(URN_PROVIDER_NSA).setPath(
         path).create();
 
     ScheduleType scheduleType = reservationRequest.getReserve().getReservation().getServiceParameters().getSchedule();
     scheduleType.setDuration(null);
     scheduleType.setStartTime(start.isPresent() ? XmlUtils.toGregorianCalendar(start.get()) : null);
     scheduleType.setEndTime(end.isPresent() ? XmlUtils.toGregorianCalendar(end.get()) : null);
 
     return reservationRequest;
   }
 
   private QueryRequestType createQueryRequest(String connectionId) {
     return new ConnectionServiceProviderFactory().setConnectionId(connectionId).setProviderNsa(URN_PROVIDER_NSA)
         .setRequesterNsa(URN_REQUESTER_NSA).createQueryRequest();
   }
 
   private TerminateRequestType createTerminateRequest(String connId) {
     return new ConnectionServiceProviderFactory().setConnectionId(connId).setProviderNsa(URN_PROVIDER_NSA)
         .createTerminateRequest();
   }
 
   private ProvisionRequestType createProvisionRequest(String connId) {
     return new ConnectionServiceProviderFactory().setConnectionId(connId).setProviderNsa(URN_PROVIDER_NSA)
         .createProvisionRequest();
   }
 
   private String awaitReserveConfirmed() {
     return awaitRequestFor("reserveConfirmed");
   }
 
   private String awaitTerminateConfirmed() {
     return awaitRequestFor("terminateConfirmed");
   }
 
   private String awaitTerminateFailed() {
     return awaitRequestFor("terminateFailed");
   }
 
   private String awaitQueryConfirmed() {
     return awaitRequestFor("queryConfirmed");
   }
 
   private String awaitProvisionFailed() {
     return awaitRequestFor("provisionFailed");
   }
 
   private String awaitProvisionConfirmed() {
     return awaitRequestFor("provisionConfirmed");
   }
 
   private String awaitRequestFor(String responseTag) {
     String response = nsiRequester.awaitRequest(5, TimeUnit.SECONDS);
     assertThat(response, containsString(responseTag));
 
     return response;
   }
 
   private VirtualPort createVirtualPort(VirtualResourceGroup virtualResourceGroup, PhysicalPort port) {
     VirtualPort vPort = new VirtualPortFactory().setMaxBandwidth(100).setPhysicalPort(port).setVirtualResourceGroup(virtualResourceGroup).create();
     vPort = virtualPortRepo.save(vPort);
     virtualResourceGroup.addVirtualPort(vPort);
     virtualResourceGroupRepo.save(virtualResourceGroup);
 
     return vPort;
   }
 
   private PhysicalPort createPhysicalPort(PhysicalResourceGroup physicalResourceGroup) {
     return physicalPortRepo.save(new PhysicalPortFactory().setPhysicalResourceGroup(physicalResourceGroup).create());
   }
 
   private PhysicalResourceGroup createPhysicalResourceGroup() {
     Institute institute = instituteRepo.findAll().get(0);
     PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().setInstitute(institute).create();
 
     return physicalResourceGroupRepo.save(physicalResourceGroup);
   }
 
   private VirtualResourceGroup createVirtualResourceGroup() {
     VirtualResourceGroup virtualResourceGroup = new VirtualResourceGroupFactory().setName(virtualResourceGroupName)
         .setAdminGroup(userDetails.getUserGroupIds().iterator().next()).create();
 
     return virtualResourceGroupRepo.save(virtualResourceGroup);
   }
 
   private void setLoggedInUser(RichUserDetails userDetails) {
     Security.setUserDetails(userDetails);
   }
 
  private void assertStates(final String connectionId, final ReservationStatus reservationStatus, final ConnectionStateType connectionState) {
     ConnectionV1 connection = connectionRepo.findByConnectionId(connectionId);
    internalConnectionStateIs(connectionId, connectionState);
    internalReservationStateIs(connection.getReservation().getId(), reservationStatus);
   }
 
   private Callable<Boolean> internalConnectionStateIs(final String connectionId, final ConnectionStateType connectionStateType) {
     return new Callable<Boolean>() {
       public Boolean call() throws Exception {
         ConnectionV1 connection =  connectionRepo.findByConnectionId(connectionId);
         return connectionStateType.equals(connection.getCurrentState());
       }
     };
   }
 
   private Callable<Boolean> internalReservationStateIs(final Long id, final ReservationStatus status) {
     return new Callable<Boolean>() {
       public Boolean call() throws Exception {
         Reservation reservation = reservationService.find(id);
         return status.equals(reservation.getStatus());
       }
     };
   }
 
 
 }
