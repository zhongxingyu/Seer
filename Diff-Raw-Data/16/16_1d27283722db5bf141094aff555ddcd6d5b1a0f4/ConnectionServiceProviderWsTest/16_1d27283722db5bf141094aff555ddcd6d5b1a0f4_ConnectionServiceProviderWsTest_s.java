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
 package nl.surfnet.bod.nsi.ws.v1sc;
 
 import java.util.EnumSet;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import junit.framework.Assert;
 import nl.surfnet.bod.domain.Connection;
 import nl.surfnet.bod.domain.NsiRequestDetails;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.domain.oauth.NsiScope;
 import nl.surfnet.bod.nsi.ws.ConnectionServiceProviderErrorCodes;
 import nl.surfnet.bod.repo.ConnectionRepo;
 import nl.surfnet.bod.service.ConnectionServiceProviderService;
 import nl.surfnet.bod.service.ReservationService;
 import nl.surfnet.bod.service.VirtualPortService;
 import nl.surfnet.bod.support.ConnectionFactory;
 import nl.surfnet.bod.support.RichUserDetailsFactory;
 import nl.surfnet.bod.support.VirtualPortFactory;
 import nl.surfnet.bod.support.VirtualResourceGroupFactory;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 
 import org.hamcrest.text.IsEmptyString;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
 import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
 import org.ogf.schemas.nsi._2011._10.connection.types.BandwidthType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
 import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ReserveType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ScheduleType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Throwables;
 
 import static junit.framework.Assert.fail;
 
 import static nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderFunctions.RESERVE_REQUEST_TO_CONNECTION;
import static nl.surfnet.bod.web.WebUtils.not;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ConnectionServiceProviderWsTest {
 
   @InjectMocks
   private ConnectionServiceProviderWs subject;
 
   @Mock
   private ConnectionRepo connectionRepoMock;
 
   @Mock
   private VirtualPortService virtualPortServiceMock;
 
   @Mock
   private ReservationService reservationServiceMock;
 
   @Mock
   private ConnectionServiceProviderService connectionServiceProviderComponent;
 
   private final String nsaProvider = "nsa:surfnet.nl";
 
   private final RichUserDetails userDetails = new RichUserDetailsFactory().addUserGroup("test").create();
 
   private final VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setSurfconextGroupId("test").create();
 
   private final VirtualPort sourcePort = new VirtualPortFactory().setVirtualResourceGroup(vrg).create();
 
   private final VirtualPort destinationPort = new VirtualPortFactory().setVirtualResourceGroup(vrg).create();
 
   private final Connection connection = new ConnectionFactory().setSourceStpId("Source Port").setDestinationStpId(
       "Destination Port").setProviderNSA(nsaProvider).create();
 
   private final NsiRequestDetails request = new NsiRequestDetails("http://localhost", "123456");
 
   @Before
   public void setup() {
     subject.addNsaProvider(nsaProvider);
 
     when(virtualPortServiceMock.findByNsiStpId("Source Port")).thenReturn(sourcePort);
     when(virtualPortServiceMock.findByNsiStpId("Destination Port")).thenReturn(destinationPort);
   }
 
   @Test
   public void shouldComplainAboutTheProviderNsa() {
     Connection connection = new ConnectionFactory().setSourceStpId("Source Port").setDestinationStpId(
         "Destination Port").setProviderNSA("non:existingh").create();
 
     try {
       subject.reserve(connection, request, userDetails);
       Assert.fail("Exception expected");
     }
     catch (ServiceException e) {
       assertThat(e.getFaultInfo().getErrorId(), is(ConnectionServiceProviderWs.SVC0001_INVALID_PARAM));
     }
   }
 
   @Test
   public void shouldComplainAboutNonExistingPort() {
     when(virtualPortServiceMock.findByNsiStpId("Destination Port")).thenReturn(null);
 
     try {
       subject.reserve(connection, request, userDetails);
       Assert.fail("Exception expected");
     }
     catch (ServiceException e) {
       assertThat(e.getFaultInfo().getErrorId(), is(ConnectionServiceProviderWs.SVC0001_INVALID_PARAM));
     }
   }
 
   @Test
   public void shouldCreateReservation() throws ServiceException {
     subject.reserve(connection, request, userDetails);
   }
 
   @Test
   public void shouldThrowInvalidCredentialsWhileBakingAReservationPieForSourcePort() {
     sourcePort.getVirtualResourceGroup().setSurfconextGroupId("other");
 
     try {
       subject.reserve(connection, request, userDetails);
       Assert.fail("Exception expected");
     }
     catch (ServiceException e) {
       assertThat(e.getFaultInfo().getErrorId(), is(ConnectionServiceProviderWs.SVC0005_INVALID_CREDENTIALS));
     }
   }
 
   @Test
   public void shouldThrowInvalidCredentialsWhileBakingAReservationPieForDestinationPort() {
     destinationPort.getVirtualResourceGroup().setSurfconextGroupId("other");
 
     try {
       subject.reserve(connection, request, userDetails);
       Assert.fail("Exception expected");
     }
     catch (ServiceException e) {
       assertThat(e.getFaultInfo().getErrorId(), is(ConnectionServiceProviderWs.SVC0005_INVALID_CREDENTIALS));
     }
   }
 
   @Test
   public void shouldThrowAlreadyExistsWhenNonUniqueConnectionIdIsUsed() {
     when(connectionRepoMock.findByConnectionId(anyString())).thenReturn(new Connection());
 
     try {
       subject.reserve(connection, request, userDetails);
       fail("Exception expected");
     }
     catch (ServiceException e) {
       assertThat(e.getFaultInfo().getErrorId(), is(ConnectionServiceProviderWs.SVC0003_ALREADY_EXISTS));
     }
   }
 
   @Test
   public void reserveTypeWithoutGlobalReservationIdShouldGetOne() {
     ReserveRequestType reserveRequestType = createReservationRequestType(1000, Optional.<String> absent());
 
     Connection connection = RESERVE_REQUEST_TO_CONNECTION.apply(reserveRequestType);
 
     assertThat(connection.getGlobalReservationId(), not(IsEmptyString.isEmptyOrNullString()));
     assertThat(connection.getDesiredBandwidth(), is(1000));
   }
 
   @Test
   public void reserveTypeWithGlobalReservationId() {
     ReserveRequestType reserveRequestType = createReservationRequestType(1000, Optional.of("urn:surfnet.nl:12345"));
 
     Connection connection = RESERVE_REQUEST_TO_CONNECTION.apply(reserveRequestType);
 
     assertThat(connection.getGlobalReservationId(), is("urn:surfnet.nl:12345"));
   }
 
   @Test
   public void reserveWithoutReserveScopeShouldFail() throws ServiceException {
     Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.of(NsiScope.QUERY)).create());
 
     try {
       subject.reserve(createReservationRequestType(100, Optional.of("1234")));
       fail("Exception expected");
     }
     catch (ServiceException e) {
       assertThat(e.getFaultInfo().getErrorId(), is(ConnectionServiceProviderErrorCodes.SECURITY.MISSING_GRANTED_SCOPE
           .getId()));
     }
   }
 
   @Test
   public void queryWithoutReserveScopeShouldFail() throws ServiceException {
     Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.of(NsiScope.RESERVE)).create());
 
     try {
       subject.query(new QueryRequestType());
       fail("Exception expected");
     }
     catch (ServiceException e) {
       assertThat(e.getFaultInfo().getErrorId(), is(ConnectionServiceProviderErrorCodes.SECURITY.MISSING_GRANTED_SCOPE
           .getId()));
     }
   }
 
   @Test
   public void shouldTransferToScheduledWhenProvisionFailsAndStartTimeIsReached() {
     Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.of(NsiScope.PROVISION)).create());
 
     connection.setStartTime(Optional.of(DateTime.now().plusMinutes((1))));
     try {
       subject.provisionFailed(connection, request);
     }
     catch (Exception e) {
      if (not(e.getMessage().contains("refused"))) {
        fail("Other exception expected");
      }
     }
     assertThat(connection.getCurrentState(), is(ConnectionStateType.SCHEDULED));
   }
 
   @Test
   public void shouldTransferToReservedWhenProvisionFailsAndStartTimeIsNotReached() {
     Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.of(NsiScope.PROVISION)).create());
 
     connection.setStartTime(Optional.of(DateTime.now().minusMinutes(1)));
     try {
       subject.provisionFailed(connection, request);
     }
     catch (Exception e) {
      if (not(e.getMessage().contains("refused"))) {
        fail("Other exception expected");
      }
     }
     assertThat(connection.getCurrentState(), is(ConnectionStateType.RESERVED));
   }
 
   @Test
   public void shouldTransferToReservedWhenProvisionFailsAndNoStartTimeIsPresent() {
     Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.of(NsiScope.PROVISION)).create());
 
     connection.setStartTime(Optional.<DateTime> absent());
     try {
       subject.provisionFailed(connection, request);
     }
     catch (Exception e) {
      if (not(e.getMessage().contains("refused"))) {
        fail("Other exception expected");
      }
     }
     assertThat(connection.getCurrentState(), is(ConnectionStateType.RESERVED));
   }
 
   public static ReserveRequestType createReservationRequestType(int desiredBandwidth,
       Optional<String> globalReservationId) {
     ServiceTerminationPointType dest = new ServiceTerminationPointType();
     dest.setStpId("urn:stp:1");
 
     ServiceTerminationPointType source = new ServiceTerminationPointType();
     source.setStpId("urn:stp:1");
 
     PathType path = new PathType();
     path.setDestSTP(dest);
     path.setSourceSTP(source);
 
     ScheduleType schedule = new ScheduleType();
     XMLGregorianCalendar xmlGregorianCalendar;
     try {
       xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar();
       schedule.setStartTime(xmlGregorianCalendar);
       schedule.setEndTime(xmlGregorianCalendar);
     }
     catch (DatatypeConfigurationException e) {
       Throwables.propagate(e);
     }
 
     BandwidthType bandwidth = new BandwidthType();
     bandwidth.setDesired(desiredBandwidth);
 
     ServiceParametersType serviceParameters = new ServiceParametersType();
     serviceParameters.setSchedule(schedule);
     serviceParameters.setBandwidth(bandwidth);
 
     ReservationInfoType reservation = new ReservationInfoType();
     reservation.setPath(path);
     reservation.setServiceParameters(serviceParameters);
     reservation.setGlobalReservationId(globalReservationId.orNull());
 
     ReserveType reserve = new ReserveType();
     reserve.setReservation(reservation);
 
     ReserveRequestType reserveRequestType = new ReserveRequestType();
     reserveRequestType.setReserve(reserve);
 
     return reserveRequestType;
   }
 
 }
