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
 
import static nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderFunctions.NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT;

 import java.util.Collection;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.xml.ws.Holder;
 
 import nl.surfnet.bod.domain.Connection;
 import nl.surfnet.bod.domain.NsiRequestDetails;
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.nsi.ws.ConnectionServiceProviderErrorCodes;
 import nl.surfnet.bod.repo.ConnectionRepo;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 
 import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2011._10.connection.types.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.scheduling.annotation.Async;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.StringUtils;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 
 @Service
 public class ConnectionServiceProviderService {
 
   private final Logger log = LoggerFactory.getLogger(ConnectionServiceProviderService.class);
 
   @Resource
   private ConnectionRepo connectionRepo;
 
   @Resource
   private ReservationService reservationService;
 
   @Resource
   private VirtualPortService virtualPortService;
 
   @Transactional(propagation = Propagation.REQUIRES_NEW)
   public void reserve(Connection connection, NsiRequestDetails requestDetails, boolean autoProvision,
       RichUserDetails userDetails) {
     connection.setCurrentState(ConnectionStateType.RESERVING);
     connection = connectionRepo.saveAndFlush(connection);
 
     final VirtualPort sourcePort = virtualPortService.findByNsiStpId(connection.getSourceStpId());
     final VirtualPort destinationPort = virtualPortService.findByNsiStpId(connection.getDestinationStpId());
 
     Reservation reservation = new Reservation();
     reservation.setConnection(connection);
     reservation.setName(connection.getDescription());
     reservation.setStartDateTime(connection.getStartTime().orNull());
     reservation.setEndDateTime(connection.getEndTime().orNull());
     reservation.setSourcePort(sourcePort);
     reservation.setDestinationPort(destinationPort);
     reservation.setVirtualResourceGroup(sourcePort.getVirtualResourceGroup());
     reservation.setBandwidth(connection.getDesiredBandwidth());
     reservation.setUserCreated(connection.getRequesterNsa());
     reservation.setUserCreated(userDetails.getNameId());
     reservation.setConnection(connection);
     connection.setReservation(reservation);
     reservationService.create(reservation, autoProvision, Optional.of(requestDetails));
   }
 
   @Transactional(propagation = Propagation.REQUIRES_NEW)
   public void provision(Long connectionId, NsiRequestDetails requestDetails) {
     // TODO [AvD] check if connection is in correct state to receive a provision
     // request..
     // for now we always go to auto provision but this is only correct if the
     // state is reserved.
     // in case it is scheduled we should start the reservation (go to
     // provisioning) But this is not supported
     // by OpenDRAC right now??
     // If we are already in the provisioned state send back a confirm and we are
     // done..
     // Any other state we have to send back a provision failed...
     Connection connection = updateConnectionState(connectionId, ConnectionStateType.AUTO_PROVISION);
     connection.setProvisionRequestDetails(requestDetails);
     connection = connectionRepo.saveAndFlush(connection);
 
     reservationService.provision(connection.getReservation(), Optional.of(requestDetails));
   }
 
   @Transactional(propagation = Propagation.REQUIRES_NEW)
   public void terminate(final Long connectionId, final String requesterNsa, final NsiRequestDetails requestDetails) {
     Connection connection = updateConnectionState(connectionId, ConnectionStateType.TERMINATING);
    connectionRepo.saveAndFlush(connection);
 
     reservationService.cancelWithReason(connection.getReservation(), "Terminate request by NSI", Security
         .getUserDetails(), Optional.of(requestDetails));
   }
 
   public Connection updateConnectionState(final Long connectionId, final ConnectionStateType state) {
     Connection connection = connectionRepo.findOne(connectionId);
     connection.setCurrentState(state);
     return connection;
   }
 
   @Async
   public void asyncQueryConnections(NsiRequestDetails requestDetails, Collection<String> connectionIds,
       Collection<String> globalReservationIds, QueryOperationType operation) {
     Preconditions.checkArgument(!connectionIds.isEmpty());
 
     QueryConfirmedType confirmedType = new QueryConfirmedType();
 
     for (String connectionId : connectionIds) {
       addQueryResult(confirmedType, findByConnectionId(connectionId), operation);
     }
 
     for (String globalReservationId : globalReservationIds) {
       addQueryResult(confirmedType, findByGlobalReservationId(globalReservationId), operation);
     }
 
     sendQueryResult(requestDetails, confirmedType);
   }
 
   @Async
   public void asyncQueryAllForRequesterNsa(NsiRequestDetails requestDetails, String requesterNsa,
       QueryOperationType operation) {
     QueryConfirmedType confirmedType = new QueryConfirmedType();
 
     for (Connection connection : findByRequesterNsa(requesterNsa)) {
       addQueryResult(confirmedType, connection, operation);
     }
 
     sendQueryResult(requestDetails, confirmedType);
   }
 
   @VisibleForTesting
   Connection findByGlobalReservationId(String globalReservationId) {
     return connectionRepo.findByGlobalReservationId(globalReservationId);
   }
 
   @VisibleForTesting
   Connection findByConnectionId(String connectionId) {
     return connectionRepo.findByConnectionId(connectionId);
   }
 
   @VisibleForTesting
   List<Connection> findByRequesterNsa(String requesterNsa) {
     return connectionRepo.findByRequesterNsa(requesterNsa);
   }
 
   private void sendQueryResult(NsiRequestDetails requestDetails, QueryConfirmedType queryResult) {
     ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
     try {
       port.queryConfirmed(new Holder<>(requestDetails.getCorrelationId()), queryResult);
     }
     catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
       log.error("Error: ", e);
       QueryFailedType failedType = new QueryFailedType();
 
       final ServiceExceptionType error = new ServiceExceptionType();
       error.setErrorId(ConnectionServiceProviderErrorCodes.CONNECTION.CONNECTION_ERROR.getId());
       failedType.setServiceException(error);
       sendQueryFailed(requestDetails.getCorrelationId(), failedType, port);
     }
   }
 
   private void addQueryResult(QueryConfirmedType confirmedType, Connection connection, QueryOperationType operation) {
     if (connection == null) {
       return;
     }
 
     if (operation.equals(QueryOperationType.DETAILS)) {
       addQueryDetailsResult(confirmedType, connection);
     }
     else {
       addQuerySummaryResult(confirmedType, connection);
     }
   }
 
   private void addQueryDetailsResult(QueryConfirmedType query, Connection connection) {
     for (QueryDetailsResultType result : query.getReservationDetails()) {
       if (result.getGlobalReservationId().equals(connection.getGlobalReservationId())) {
         return;
       }
     }
 
     QueryDetailsResultType result = new QueryDetailsResultType();
     result.setConnectionId(connection.getConnectionId());
     result.setDescription(connection.getDescription());
     result.setServiceParameters(connection.getServiceParameters());
 
     DetailedPathType path = new DetailedPathType();
     path.setConnectionState(connection.getCurrentState());
     path.setConnectionId(connection.getConnectionId());
     path.setProviderNSA(connection.getProviderNsa());
     result.setDetailedPath(path);
 
     if (StringUtils.hasText(connection.getGlobalReservationId())) {
       result.setGlobalReservationId(connection.getGlobalReservationId());
     }
 
     query.getReservationDetails().add(result);
   }
 
   private void addQuerySummaryResult(QueryConfirmedType query, Connection connection) {
     QuerySummaryResultType result = new QuerySummaryResultType();
     result.setConnectionId(connection.getConnectionId());
     result.setConnectionState(connection.getCurrentState());
     result.setDescription(connection.getDescription());
 
     if (StringUtils.hasText(connection.getGlobalReservationId())) {
       result.setGlobalReservationId(connection.getGlobalReservationId());
     }
 
     query.getReservationSummary().add(result);
   }
 
   private void sendQueryFailed(final String correlationId, QueryFailedType failedType, ConnectionRequesterPort port) {
     try {
       port.queryFailed(new Holder<>(correlationId), failedType);
     }
     catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
       log.error("Error: ", e);
     }
   }
 
 }
