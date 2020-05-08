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
 
 import static com.google.common.base.Preconditions.*;
 import static nl.surfnet.bod.nsi.ws.ConnectionServiceProviderErrorCodes.PAYLOAD.*;
 import static nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderFunctions.*;
 import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.jws.WebService;
 import javax.xml.ws.Holder;
 import javax.xml.ws.WebServiceContext;
 
 import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
 import oasis.names.tc.saml._2_0.assertion.AttributeType;
 
 import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.ProvisionRequestType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.ReleaseRequestType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.TerminateRequestType;
 import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
 import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
 import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
 import org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType;
 import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ObjectFactory;
 import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
 import org.ogf.schemas.nsi._2011._10.connection.types.QueryDetailsResultType;
 import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ReserveConfirmedType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ServiceExceptionType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
 import org.springframework.util.StringUtils;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.sun.xml.ws.developer.SchemaValidation;
 
 import nl.surfnet.bod.domain.Connection;
 import nl.surfnet.bod.domain.NsiRequestDetails;
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.nsi.ws.ConnectionServiceProvider;
 import nl.surfnet.bod.repo.ConnectionRepo;
 import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
 import nl.surfnet.bod.service.ConnectionServiceProviderService;
 import nl.surfnet.bod.service.VirtualPortService;
 
 @Service("connectionServiceProviderWs_v1sc")
 @WebService(serviceName = "ConnectionServiceProvider",
     portName = "ConnectionServiceProviderPort",
     endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort",
     targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/provider")
 @SchemaValidation
 public class ConnectionServiceProviderWs implements ConnectionServiceProvider {
 
   private final Logger log = LoggerFactory.getLogger(ConnectionServiceProviderWs.class);
 
   @Resource
   private ConnectionRepo connectionRepo;
 
   @Resource
   private VirtualPortService virtualPortService;
 
   @Resource
   private VirtualResourceGroupRepo virtualResourceGroupRepo;
 
   @Resource
   private WebServiceContext webServiceContext;
 
   @Resource
   private ConnectionServiceProviderService connectionServiceProviderService;
 
   @Resource(name = "nsaProviderUrns")
   private List<String> nsaProviderUrns = new ArrayList<>();
 
   private ServiceException getInvalidParameterServiceException(final String attributeName) {
     final ServiceExceptionType serviceExceptionType = new ServiceExceptionType();
     serviceExceptionType.setErrorId("SVC0001");
     serviceExceptionType.setText("Invalid or missing parameter");
 
     final AttributeType attribute = new AttributeType();
     attribute.setName(attributeName);
     attribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
 
     final AttributeStatementType attributeStatement = new AttributeStatementType();
     attributeStatement.getAttributeOrEncryptedAttribute().add(attribute);
 
     serviceExceptionType.setVariables(attributeStatement);
 
     return new ServiceException("SVC0001", serviceExceptionType);
   }
 
   /**
    * The reservation method processes an NSI reservation request for
    * inter-domain bandwidth. Those parameters required for the request to
    * proceed to a processing actor will be validated, however, all other
    * parameters will be validated in the processing actor.
    * 
    * @param parameters
    *          The un-marshaled JAXB object holding the NSI reservation request.
    * @return The GenericAcknowledgmentType object returning the correlationId
    *         sent in the reservation request. We are acknowledging that we have
    *         received the request.
    * @throws ServiceException
    *           if we can determine there is processing error before digging into
    *           the request.
    */
   @Override
   public GenericAcknowledgmentType reserve(final ReserveRequestType reservationRequest) throws ServiceException {
     checkNotNull(reservationRequest);
 
     Connection connection = RESERVE_REQUEST_TO_CONNECTION.apply(reservationRequest);
     connection = connectionRepo.save(connection);
 
     final NsiRequestDetails requestDetails = new NsiRequestDetails(reservationRequest.getReplyTo(),
         reservationRequest.getCorrelationId());
 
     reserve(connection, requestDetails);
 
     return createGenericAcknowledgment(requestDetails.getCorrelationId());
   }
 
   protected void reserve(Connection connection, NsiRequestDetails request) throws ServiceException {
     log.debug("Received reservation request connectionId: {}", connection.getConnectionId());
 
     validateConnection(connection);
     final Reservation reservation = connectionServiceProviderService.createReservation(connection, request, false);
 
     connection.setCurrentState(RESERVING);
     connection.setReservation(reservation);
     connectionRepo.save(connection);
   }
 
   private void validateConnection(Connection connection) throws ServiceException {
     // TODO Validate if connection id is unique (gives db constraint exception
     // now)
 
     try {
       validateProviderNsa(connection.getProviderNsa());
       validateConnectionId(connection.getConnectionId());
       validatePortExists(connection.getSourceStpId(), "sourceSTP");
       validatePortExists(connection.getDestinationStpId(), "destSTP");
     }
     catch (ServiceException e) {
       connection.setCurrentState(CLEANING);
       connectionRepo.save(connection);
       throw e;
     }
   }
 
   private void validateProviderNsa(String providerNsa) throws ServiceException {
     if (nsaProviderUrns.contains(providerNsa)) {
       return;
     }
     throw getInvalidParameterServiceException("providerNSA");
   }
 
   private void validateConnectionId(String connectionId) throws ServiceException {
     if (StringUtils.hasText(connectionId)) {
       return;
     }
     throw getInvalidParameterServiceException("connectionId");
   }
 
   private void validatePortExists(String stpId, String attribute) throws ServiceException {
     final VirtualPort port = virtualPortService.findByNsiStpId(stpId);
     if (port == null) {
       throw getInvalidParameterServiceException(attribute);
     }
   }
 
   @Override
   public void reserveConfirmed(final Connection connection, final NsiRequestDetails requestDetails) {
     log.debug("Sending a reserveConfirmed on endpoint: {} with id: {}", requestDetails.getReplyTo(),
         connection.getGlobalReservationId());
 
     connection.setCurrentState(ConnectionStateType.RESERVED);
     connectionRepo.save(connection);
 
     final ReservationInfoType reservationInfoType = new ObjectFactory().createReservationInfoType();
     reservationInfoType.setConnectionId(connection.getConnectionId());
     reservationInfoType.setDescription(connection.getDescription());
     reservationInfoType.setGlobalReservationId(connection.getGlobalReservationId());
 
     reservationInfoType.setPath(connection.getPath());
     reservationInfoType.setServiceParameters(connection.getServiceParameters());
 
     final ReserveConfirmedType reserveConfirmedType = new ObjectFactory().createReserveConfirmedType();
     reserveConfirmedType.setRequesterNSA(connection.getRequesterNsa());
     reserveConfirmedType.setProviderNSA(connection.getProviderNsa());
     reserveConfirmedType.setReservation(reservationInfoType);
 
     try {
       ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
       port.reserveConfirmed(new Holder<>(requestDetails.getCorrelationId()), reserveConfirmedType);
     }
     catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
       log.error("Error: ", e);
     }
   }
 
   @Override
   public void reserveFailed(final Connection connection, final NsiRequestDetails requestDetails) {
     log.debug("Sending a reserveFailed on endpoint: {} with id: {}", requestDetails.getReplyTo(),
         connection.getGlobalReservationId());
 
     // skipping the states cleaning and terminating...
     // TODO [AvD] Or do we need to send a Terminate Confirmed?
     connection.setCurrentState(ConnectionStateType.TERMINATED);
     connectionRepo.save(connection);
 
     // FIXME What to put into the service exception
     final ServiceExceptionType serviceException = new ServiceExceptionType();
     serviceException.setErrorId("ERROR_ID");
     serviceException.setText("Some text");
     AttributeStatementType values = new AttributeStatementType();
     serviceException.setVariables(values);
 
     GenericFailedType reservationFailed = CONNECTION_TO_GENERIC_FAILED.apply(connection);
     reservationFailed.setServiceException(serviceException);
 
     try {
       final ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
       port.reserveFailed(new Holder<>(requestDetails.getCorrelationId()), reservationFailed);
     }
     catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
       log.error("Error: ", e);
     }
   }
 
   @Override
   public GenericAcknowledgmentType provision(ProvisionRequestType parameters) throws ServiceException {
     final String connectionId = parameters.getProvision().getConnectionId();
 
     log.debug("Received provision request with id: {}", connectionId);
 
     final Connection connection = getConnectionOrFail(connectionId);
     validateProviderNsa(parameters.getProvision().getProviderNSA());
 
     final NsiRequestDetails requestDetails = new NsiRequestDetails(parameters.getReplyTo(),
         parameters.getCorrelationId());
     connectionServiceProviderService.provision(connection, requestDetails);
     return createGenericAcknowledgment(requestDetails.getCorrelationId());
   }
 
   @Override
   public void provisionFailed(Connection connection, NsiRequestDetails requestDetails) {
     log.debug("Calling sendReserveConfirmed on endpoint: {} with id: {}", requestDetails.getReplyTo(),
         connection.getGlobalReservationId());
 
     connection.setCurrentState(ConnectionStateType.SCHEDULED);
     connectionRepo.save(connection);
 
     final GenericFailedType generic = new GenericFailedType();
 
     generic.setProviderNSA(connection.getProviderNsa());
     generic.setRequesterNSA(connection.getRequesterNsa());
     generic.setConnectionId(connection.getConnectionId());
     generic.setGlobalReservationId(connection.getGlobalReservationId());
 
     try {
       final ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
       port.provisionFailed(new Holder<>(requestDetails.getCorrelationId()), generic);
     }
     catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
       log.error("Error: ", e);
     }
 
   }
 
   @Override
   public void provisionConfirmed(final Connection connection, NsiRequestDetails requestDetails) {
     log.debug("Calling sendReserveConfirmed on endpoint: {} with id: {}", requestDetails.getReplyTo(),
         connection.getGlobalReservationId());
 
     connection.setCurrentState(ConnectionStateType.PROVISIONED);
     connectionRepo.save(connection);
 
     final GenericConfirmedType genericConfirm = CONNECTION_TO_GENERIC_CONFIRMED.apply(connection);
 
     try {
       final ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
       port.provisionConfirmed(new Holder<>(requestDetails.getCorrelationId()), genericConfirm);
     }
     catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
       log.error("Error: ", e);
     }
 
   }
 
   @Override
   public GenericAcknowledgmentType release(ReleaseRequestType parameters) throws ServiceException {
     final ServiceExceptionType exceptionType = new ServiceExceptionType();
     exceptionType.setErrorId(NOT_IMPLEMENTED.getId());
     exceptionType.setText(NOT_IMPLEMENTED.getText());
     throw new ServiceException("Not supported", exceptionType);
   }
 
   @Override
   public GenericAcknowledgmentType terminate(TerminateRequestType parameters) throws ServiceException {
     final Connection connection = getConnectionOrFail(parameters.getTerminate().getConnectionId());
     validateProviderNsa(parameters.getTerminate().getProviderNSA());
 
     final NsiRequestDetails requestDetails = new NsiRequestDetails(parameters.getReplyTo(),
         parameters.getCorrelationId());
     connectionServiceProviderService.terminate(connection, parameters.getTerminate().getRequesterNSA(), requestDetails);
     return createGenericAcknowledgment(requestDetails.getCorrelationId());
   }
 
   @Override
   public void terminateConfirmed(Connection connection, NsiRequestDetails requestDetails) {
     connection.setCurrentState(ConnectionStateType.TERMINATED);
     connectionRepo.save(connection);
 
     final GenericConfirmedType genericConfirmed = CONNECTION_TO_GENERIC_CONFIRMED.apply(connection);
 
     try {
       final ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
       port.terminateConfirmed(new Holder<>(requestDetails.getCorrelationId()), genericConfirmed);
     }
     catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
       log.error("Error: ", e);
     }
   }
 
   @Override
   public void terminateFailed(Connection connection, NsiRequestDetails requestDetails) {
     connection.setCurrentState(ConnectionStateType.TERMINATED);
     connectionRepo.save(connection);
 
     final GenericFailedType genericFailed = CONNECTION_TO_GENERIC_FAILED.apply(connection);
 
     try {
       final ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
       port.terminateFailed(new Holder<>(requestDetails.getCorrelationId()), genericFailed);
     }
     catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
       log.error("Error: ", e);
     }
   }
 
   @Override
   public GenericAcknowledgmentType query(QueryRequestType parameters) throws ServiceException {
 
     final String correlationId = parameters.getCorrelationId();
     final String replyTo = parameters.getReplyTo();
     final List<String> connectionIds = parameters.getQuery().getQueryFilter().getConnectionId();
     final List<String> globalReservationIds = parameters.getQuery().getQueryFilter().getGlobalReservationId();
     final QueryConfirmedType confirmedType = new QueryConfirmedType();
 
     // no criteria, return all connections related to this requester nsa
    if (CollectionUtils.isEmpty(connectionIds) && CollectionUtils.isEmpty(globalReservationIds)) {
       for (final Connection connection : connectionRepo.findByRequesterNsa(parameters.getQuery().getRequesterNSA())) {
         addQueryResult(confirmedType, connection);
       }
     }
    else {
      if (!CollectionUtils.isEmpty(connectionIds)) {
        for (final String connectionId : connectionIds) {
          addQueryResult(confirmedType, connectionRepo.findByConnectionId(connectionId));
        }
       }
 
      if (!CollectionUtils.isEmpty(globalReservationIds)) {
        for (final String globalReservationId : globalReservationIds) {
          addQueryResult(confirmedType, connectionRepo.findByGlobalReservationId(globalReservationId));
        }
       }
     }
 
     connectionServiceProviderService.sendQueryConfirmed(correlationId, confirmedType,
         NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(new NsiRequestDetails(replyTo, correlationId)), parameters
             .getQuery().getProviderNSA(), parameters.getQuery().getRequesterNSA());
 
     return createGenericAcknowledgment(correlationId);
   }
 
   /**
    * @param confirmedType
    * @param connection
    */
   private void addQueryResult(final QueryConfirmedType confirmedType, final Connection connection) {
     if (connection == null) {
       return;
     }
     for (final QueryDetailsResultType query : confirmedType.getReservationDetails()) {
       if (query.getGlobalReservationId().equals(connection.getGlobalReservationId())) {
         return;
       }
     }
     final QueryDetailsResultType queryResult = CONNECTION_TO_QUERY_RESULT.apply(connection);
     confirmedType.getReservationDetails().add(queryResult);
   }
 
   @Override
   public void queryConfirmed(Holder<String> correlationId, QueryConfirmedType queryConfirmed) throws ServiceException {
     throw new UnsupportedOperationException("Not implemented yet.");
   }
 
   @Override
   public void queryFailed(Holder<String> correlationId, QueryFailedType queryFailed) throws ServiceException {
     throw new UnsupportedOperationException("Not implemented yet.");
   }
 
   @VisibleForTesting
   protected void addNsaProvider(String provider) {
     this.nsaProviderUrns.add(provider);
   }
 
   private Connection getConnectionOrFail(String connectionId) throws ServiceException {
     final Connection connection = connectionRepo.findByConnectionId(connectionId);
     if (connection == null) {
       throw getInvalidParameterServiceException("connectionId");
     }
     return connection;
   }
 
   private GenericAcknowledgmentType createGenericAcknowledgment(final String correlationId) {
     final GenericAcknowledgmentType genericAcknowledgmentType = new GenericAcknowledgmentType();
     genericAcknowledgmentType.setCorrelationId(correlationId);
     return genericAcknowledgmentType;
   }
 
   static {
     // Don't show full stack trace in soap result if an exception occurs
     System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
     System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
     System.setProperty("com.sun.xml.ws.util.pipe.StandaloneTubeAssembler.dump", "true");
     System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
   }
 }
