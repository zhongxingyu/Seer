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
 package nl.surfnet.bod.nbi;
 
 import static nl.surfnet.bod.domain.ReservationStatus.CANCELLED;
 import static nl.surfnet.bod.domain.ReservationStatus.FAILED;
 import static nl.surfnet.bod.domain.ReservationStatus.PREPARING;
 import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
 import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
 import static nl.surfnet.bod.domain.ReservationStatus.SUCCEEDED;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.ConcurrentMap;
 
 import javax.annotation.PostConstruct;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.ReservationStatus;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.nbi.generated.NetworkMonitoringServiceFault;
 import nl.surfnet.bod.nbi.generated.NetworkMonitoringService_v30Stub;
 import nl.surfnet.bod.nbi.generated.ResourceAllocationAndSchedulingServiceFault;
 import nl.surfnet.bod.nbi.generated.ResourceAllocationAndSchedulingService_v30Stub;
 
 import org.joda.time.LocalDateTime;
 import org.joda.time.Minutes;
 import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.Security;
 import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.SecurityDocument;
 import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.UsernameToken;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.EndpointT;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointRequestDocument;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointRequestDocument.QueryEndpointRequest;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointResponseDocument;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsRequestDocument;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsRequestDocument.QueryEndpointsRequest;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsResponseDocument;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.ValidEndpointsQueryTypeT;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CancelReservationScheduleRequestDocument;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CancelReservationScheduleRequestDocument.CancelReservationScheduleRequest;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleRequestDocument;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleRequestDocument.CreateReservationScheduleRequest;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleResponseDocument;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.PathRequestT;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryReservationScheduleRequestDocument;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryReservationScheduleResponseDocument;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationOccurrenceInfoT;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationScheduleRequestT;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationScheduleT;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.UserInfoT;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidProtectionTypeT;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleCreationResultT;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleStatusT;
 import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleTypeT;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.CompletionResponseDocument;
 import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.ValidLayerT;
 
 /**
  * A bridge to OpenDRAC's web services. Everything is contained in this one
  * class so that only this class is linked to OpenDRAC related classes.
  *
  * @author robert
  *
  */
 class NbiOpenDracWsClient implements NbiClient {
 
   public static final String ROUTING_ALGORITHM = "VCAT";
   public static final String DEFAULT_VID = "Untagged";
   public static final ValidProtectionTypeT.Enum DEFAULT_PROTECTIONTYPE = ValidProtectionTypeT.X_1_PLUS_1_PATH;
 
   private final Logger log = LoggerFactory.getLogger(getClass());
 
   private NetworkMonitoringService_v30Stub networkingService;
   private ResourceAllocationAndSchedulingService_v30Stub schedulingService;
   private SecurityDocument securityDocument;
 
   private final ConcurrentMap<String, String> idToTnaCache = Maps.newConcurrentMap();
 
   @Value("${nbi.billing.group.name}")
   private String billingGroupName;
 
   @Value("${nbi.password}")
   private String password;
 
   @Value("${nbi.group.name}")
   private String groupName;
 
   @Value("${nbi.resource.group.name}")
   private String resourceGroupName;
 
   @Value("${nbi.user}")
   private String username;
 
   @Value("${nbi.service.inventory}")
   private String inventoryServiceUrl;
 
   @Value("${nbi.service.scheduling}")
   private String schedulingServiceUrl;
 
   @SuppressWarnings("unused")
   @PostConstruct
   private void init() {
     try {
       schedulingService = new ResourceAllocationAndSchedulingService_v30Stub(schedulingServiceUrl);
       networkingService = new NetworkMonitoringService_v30Stub(inventoryServiceUrl);
       getSecurityDocument();
     }
     catch (IOException e) {
       log.error("Error: ", e);
     }
   }
 
   @Override
   public void cancelReservation(final String reservationId) {
     final CancelReservationScheduleRequestDocument requestDocument = CancelReservationScheduleRequestDocument.Factory
         .newInstance();
     final CancelReservationScheduleRequest request = requestDocument.addNewCancelReservationScheduleRequest();
     request.setReservationScheduleId(reservationId);
     try {
       final CompletionResponseDocument response = schedulingService.cancelReservationSchedule(requestDocument,
           getSecurityDocument());
       log.info("Status: {}", response.getCompletionResponse().getResult());
     }
     catch (Exception e) {
       log.error("Error: ", e);
     }
   }
 
   @Override
   public Reservation createReservation(final Reservation reservation) {
     try {
       CreateReservationScheduleResponseDocument responseDocument = schedulingService.createReservationSchedule(
           createSchedule(reservation), getSecurityDocument());
 
       log.debug("Create reservation response: {}", responseDocument.getCreateReservationScheduleResponse());
 
       String reservationId = responseDocument.getCreateReservationScheduleResponse().getReservationScheduleId();
       ReservationStatus status = OpenDracStatusTranslator.translate(responseDocument
           .getCreateReservationScheduleResponse().getResult());
 
       if (status == FAILED) {
         List<String> reasons = Lists.newArrayList();
         for (final ReservationOccurrenceInfoT occurenceInfo : responseDocument.getCreateReservationScheduleResponse()
             .getOccurrenceInfoArray()) {
           reasons.add(occurenceInfo.getReason());
         }
 
         String failedMessage = Joiner.on(", ").join(reasons);
         reservation.setFailedMessage(failedMessage);
 
         log.info("Create reservation ({}) failed with '{}'", reservationId, failedMessage);
       }
 
       reservation.setReservationId(reservationId);
       reservation.setStatus(status);
     }
     catch (ResourceAllocationAndSchedulingServiceFault e) {
       log.warn("Creating a reservation failed", e);
       reservation.setStatus(FAILED);
     }
     catch (Exception e) {
       log.error("Unexpected Exception while request reservation to openDRAC", e);
       reservation.setStatus(FAILED);
     }
 
     return reservation;
   }
 
   @Override
   public List<PhysicalPort> findAllPhysicalPorts() {
     try {
       final List<PhysicalPort> ports = Lists.newArrayList();
 
       for (final EndpointT endpoint : findAllEndPoints()) {
         ports.add(getPhysicalPort(endpoint));
       }
 
       return ports;
     }
     catch (NetworkMonitoringServiceFault e) {
       log.warn("Could not query OpenDrac for all endpoints", e);
       throw new RuntimeException(e);
     }
   }
 
   @Override
   public PhysicalPort findPhysicalPortByNetworkElementId(final String networkElementId) {
     try {
       EndpointT endpoint = findEndPointById(networkElementId);
       return getPhysicalPort(endpoint);
     }
     catch (NetworkMonitoringServiceFault e) {
       log.warn("Could not query OpenDrac for end point by id '{}'", networkElementId);
       throw new RuntimeException(e);
     }
   }
 
   /*
    * Find by id is a little more complex, because the webservice only supports a
    * lookup by tna and not by id. In BoD the tna is locally stored as the
    * nocLabel field. This field can be edited. The tna can also be edited in
    * OpenDRAC. So the only save thing todo is use the id. The service keeps a
    * chache of id to tna.
    */
   private EndpointT findEndPointById(String id) throws NetworkMonitoringServiceFault {
     String tna = idToTnaCache.get(id);
 
     if (tna == null) {
       List<EndpointT> endPoints = findAllEndPoints();
       for (EndpointT endPoint : endPoints) {
         if (endPoint.getId().equals(id)) {
           return endPoint;
         }
       }
 
       throw new IllegalStateException("Could not find endPoint for id " + id);
     }
     else {
       EndpointT endPoint;
       endPoint = findEndpointByTna(tna);
 
       if (endPoint.getId().equals(id)) {
         return endPoint;
       }
       else {
         idToTnaCache.remove(id);
         return findEndPointById(id);
       }
     }
   }
 
   @Override
   public long getPhysicalPortsCount() {
     return findAllPhysicalPorts().size();
   }
 
   @Override
   public ReservationStatus getReservationStatus(final String reservationId) {
     final QueryReservationScheduleRequestDocument requestDocument = QueryReservationScheduleRequestDocument.Factory
         .newInstance();
     requestDocument.addNewQueryReservationScheduleRequest();
     requestDocument.getQueryReservationScheduleRequest().setReservationScheduleId(reservationId);
 
     QueryReservationScheduleResponseDocument responseDocument = null;
     try {
       responseDocument = schedulingService.queryReservationSchedule(requestDocument, getSecurityDocument());
     }
     catch (Exception e) {
       log.error("Error: ", e);
     }
 
     if (responseDocument != null && responseDocument.getQueryReservationScheduleResponse().getIsFound()) {
       final ReservationScheduleT schedule = responseDocument.getQueryReservationScheduleResponse()
           .getReservationSchedule();
       final ValidReservationScheduleStatusT.Enum status = schedule.getStatus();
 
       return OpenDracStatusTranslator.translate(status);
     }
     else {
       log.info("No reservation found for reservation id: {}, returning FAILED", reservationId);
       return FAILED;
     }
 
   }
 
   private static final class OpenDracStatusTranslator {
     private static Logger logger = LoggerFactory.getLogger(OpenDracStatusTranslator.class);
 
     private OpenDracStatusTranslator() {
     }
 
     public static ReservationStatus translate(ValidReservationScheduleCreationResultT.Enum status) {
       if (status == ValidReservationScheduleCreationResultT.FAILED) {
         return FAILED;
       }
       else if (status == ValidReservationScheduleCreationResultT.SUCCEEDED) {
         return SCHEDULED;
       }
       else if (status == ValidReservationScheduleCreationResultT.SUCCEEDED_PARTIALLY) {
         return SCHEDULED;
       }
       else if (status == ValidReservationScheduleCreationResultT.UNKNOWN) {
         return FAILED;
       }
       else {
         logger.error("Could not translate status: " + status);
         throw new RuntimeException("Could not translate status: " + status);
       }
     }
 
     public static ReservationStatus translate(ValidReservationScheduleStatusT.Enum status) {
       if (status == ValidReservationScheduleStatusT.CONFIRMATION_PENDING) {
         return PREPARING;
       }
       else if (status == ValidReservationScheduleStatusT.CONFIRMATION_TIMED_OUT) {
         return FAILED;
       }
       else if (status == ValidReservationScheduleStatusT.CONFIRMATION_CANCELLED) {
         return CANCELLED;
       }
       else if (status == ValidReservationScheduleStatusT.EXECUTION_PENDING) {
         return SCHEDULED;
       }
       else if (status == ValidReservationScheduleStatusT.EXECUTION_IN_PROGRESS) {
         return RUNNING;
       }
       else if (status == ValidReservationScheduleStatusT.EXECUTION_SUCCEEDED) {
         return SUCCEEDED;
       }
       else if (status == ValidReservationScheduleStatusT.EXECUTION_PARTIALLY_SUCCEEDED) {
         // An OpenDRAC service can be partially successful, a single schedule
         // not (thats a reservation in BoD context).
         return FAILED;
       }
       else if (status == ValidReservationScheduleStatusT.EXECUTION_TIMED_OUT) {
         return FAILED;
       }
       else if (status == ValidReservationScheduleStatusT.EXECUTION_FAILED) {
         return FAILED;
       }
       else if (status == ValidReservationScheduleStatusT.EXECUTION_PARTIALLY_CANCELLED) {
         return CANCELLED;
       }
       else if (status == ValidReservationScheduleStatusT.EXECUTION_CANCELLED) {
         return CANCELLED;
       }
       else {
         logger.error("Could not translate status: " + status);
         throw new RuntimeException("Could not translate status: " + status);
       }
     }
   }
 
   CreateReservationScheduleRequestDocument createSchedule(final Reservation reservation)
       throws NetworkMonitoringServiceFault {
     final CreateReservationScheduleRequestDocument requestDocument = CreateReservationScheduleRequestDocument.Factory
         .newInstance();
 
     final CreateReservationScheduleRequest request = requestDocument.addNewCreateReservationScheduleRequest();
     final ReservationScheduleRequestT schedule = request.addNewReservationSchedule();
 
     schedule.setName(reservation.getUserCreated() + "-" + System.currentTimeMillis());
     schedule.setType(ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_AUTOMATIC);
 
     final Calendar start = Calendar.getInstance();
     if (reservation.getStartDateTime() != null) {
       start.setTime(reservation.getStartDateTime().toDate());
     }
     else {
      log.info("No startTime specified, using now: {0}", start);
       //Update reservation
       reservation.setStartDateTime(LocalDateTime.fromCalendarFields(start));
     }
     schedule.setStartTime(start);
 
     schedule.setReservationOccurrenceDuration(Minutes.minutesBetween(
         LocalDateTime.fromCalendarFields(schedule.getStartTime()), reservation.getEndDateTime()).getMinutes());
 
     schedule.setIsRecurring(false);
     schedule.setPath(createPath(reservation));
     schedule.setUserInfo(createUser(reservation));
 
     return requestDocument;
   }
 
   private UserInfoT createUser(final Reservation reservation) {
     final UserInfoT userInfo = UserInfoT.Factory.newInstance();
     userInfo.setBillingGroup(billingGroupName);
     userInfo.setSourceEndpointResourceGroup(resourceGroupName);
     userInfo.setSourceEndpointUserGroup(groupName);
     userInfo.setTargetEndpointResourceGroup(resourceGroupName);
     userInfo.setTargetEndpointUserGroup(groupName);
     return userInfo;
   }
 
   private PathRequestT createPath(final Reservation reservation) throws NetworkMonitoringServiceFault {
     final PathRequestT pathRequest = PathRequestT.Factory.newInstance();
 
     final VirtualPort virtualSourcePort = reservation.getSourcePort();
     final VirtualPort virtualDestinationPort = reservation.getDestinationPort();
 
     EndpointT sourceEndPoint = findEndPointById(virtualSourcePort.getPhysicalPort().getNetworkElementPk());
     EndpointT destinationEndPoint = findEndPointById(virtualDestinationPort.getPhysicalPort().getNetworkElementPk());
 
     pathRequest.setSourceTna(sourceEndPoint.getTna());
     pathRequest.setTargetTna(destinationEndPoint.getTna());
     pathRequest.setRate(reservation.getBandwidth());
 
     pathRequest.setSourceVlanId(translateVlanId(virtualSourcePort));
     pathRequest.setTargetVlanId(translateVlanId(virtualDestinationPort));
 
     pathRequest.setRoutingAlgorithm(ROUTING_ALGORITHM);
     pathRequest.setProtectionType(DEFAULT_PROTECTIONTYPE);
 
     return pathRequest;
   }
 
   private String translateVlanId(VirtualPort virtualPort) {
     return virtualPort.getVlanId() == null ? DEFAULT_VID : virtualPort.getVlanId().toString();
   }
 
   private List<EndpointT> findAllEndPoints() throws NetworkMonitoringServiceFault {
     final QueryEndpointsRequestDocument requestDocument = QueryEndpointsRequestDocument.Factory.newInstance();
     final QueryEndpointsRequest request = requestDocument.addNewQueryEndpointsRequest();
     request.setUserGroup(groupName);
     request.setLayer(ValidLayerT.LAYER_2);
     request.setType(ValidEndpointsQueryTypeT.QUERY_ENDPOINTS_BY_LAYER_AND_USER_GROUP_T);
 
     try {
       final QueryEndpointsResponseDocument response = networkingService.queryEndpoints(requestDocument,
           getSecurityDocument());
 
       log.debug("Find all endpoints response: {}", response);
 
       final List<EndpointT> endPoints = Lists.newArrayList();
       for (final String tna : response.getQueryEndpointsResponse().getTnaArray()) {
         endPoints.add(findEndpointByTna(tna));
       }
 
       return endPoints;
     }
     catch (RemoteException e) {
       log.warn("Could not query openDRAC for end points", e);
       throw new RuntimeException(e);
     }
   }
 
   private EndpointT findEndpointByTna(final String tna) throws NetworkMonitoringServiceFault {
     final QueryEndpointRequestDocument requestDocument = QueryEndpointRequestDocument.Factory.newInstance();
     final QueryEndpointRequest request = requestDocument.addNewQueryEndpointRequest();
     request.setTna(tna);
 
     try {
       QueryEndpointResponseDocument response = networkingService.queryEndpoint(requestDocument, getSecurityDocument());
       final EndpointT endpointFound = response.getQueryEndpointResponse().getEndpoint();
 
       idToTnaCache.put(endpointFound.getId(), endpointFound.getTna());
 
       return endpointFound;
     }
     catch (RemoteException e) {
       log.warn("Can query openDrac for end point by tna", e);
       throw new RuntimeException(e);
     }
   }
 
   private PhysicalPort getPhysicalPort(final EndpointT endpoint) {
     final PhysicalPort port = new PhysicalPort();
     port.setNocLabel(endpoint.getTna());
     port.setManagerLabel(endpoint.getUserLabel());
     port.setNetworkElementPk(endpoint.getId());
 
     return port;
   }
 
   private SecurityDocument getSecurityDocument() {
     if (securityDocument == null) {
       securityDocument = SecurityDocument.Factory.newInstance();
 
       Security security = securityDocument.addNewSecurity();
       UsernameToken token = security.addNewUsernameToken();
       token.setUsername(username);
       token.setPassword(password);
     }
     return securityDocument;
   }
 
   void setPassword(String password) {
     this.password = password;
   }
 
 }
