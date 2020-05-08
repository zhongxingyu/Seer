 /**
  * Copyright (c) 2011, SURFnet bv, The Netherlands
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *   - Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *   - Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  *   - Neither the name of the SURFnet bv, The Netherlands nor the names of
  *     its contributors may be used to endorse or promote products derived
  *     from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL SURFnet bv, The Netherlands BE LIABLE FOR
  * AND DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
  * DAMAGE.
  *
  */
 package nl.surfnet.bod.web.services;
 
 import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.annotation.Resource;
 import javax.jws.WebService;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.Holder;
 
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
 import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionServiceRequester;
 import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
 import org.ogf.schemas.nsi._2011._10.connection.types.GenericRequestType;
 import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
 import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ServiceExceptionType;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import nl.surfnet.bod.nsi.StateMachine;
 import nl.surfnet.bod.service.ReservationService;
 
 @Service("nsiProvider")
 @WebService(serviceName = "ConnectionServiceProvider",
     portName = "ConnectionServiceProviderPort",
     endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort",
     targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/provider",
     wsdlLocation = "/WEB-INF/wsdl/nsi/ogf_nsi_connection_provider_v1_0.wsdl")
 public class NsiConnectionServiceProvider extends NsiConnectionService {
 
   @Resource(name = "nsaProviderUrns")
   private List<String> nsaProviderUrns;
 
   @Resource(name = "simpelStateMachine")
   private StateMachine stateMachine;
 
   @Autowired
   private ReservationService reservationService;
 
   private final Logger log = getLog();
 
  @Resource(name = "nsaProviderUrns")
   private List<String> nsaChildren = new ArrayList<String>() {
     {
       add("urn:ogf:network:nsa:child1");
       add("urn:ogf:network:nsa:child2");
       add("urn:ogf:network:nsa:child3");
     }
   };
 
   @PostConstruct
   @SuppressWarnings("unused")
   private void init() {
     log.debug("webServiceContext: " + getWebServiceContext());
     log.debug("reservationService: " + reservationService);
   }
 
   @PreDestroy
   @SuppressWarnings("unused")
   private void destroy() {
   }
 
   /**
    * @return
    */
   private ServiceExceptionType getInvalidParameterServiceExceptionType(final String attributeName) {
     final ServiceExceptionType faultInfo = new ServiceExceptionType();
     faultInfo.setErrorId("SVC0001");
     faultInfo.setText("Invalid or missing parameter");
     final AttributeType attributeType = new AttributeType();
     attributeType.setName(attributeName);
     attributeType.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
     final AttributeStatementType attributeStatementType = new AttributeStatementType();
     attributeStatementType.getAttributeOrEncryptedAttribute().add(attributeType);
     faultInfo.setVariables(attributeStatementType);
     return faultInfo;
   }
 
   private void sendReservationFailed(final String requesterEndpoint, final String correlationId) {
     log.info("Calling reserveFailed on endpoint: {} with id: {}", requesterEndpoint, correlationId);
 
     final ConnectionServiceRequester requester = new ConnectionServiceRequester();
     final ConnectionRequesterPort connectionServiceRequesterPort = requester.getConnectionServiceRequesterPort();
     final GenericFailedType reservationFailed = new GenericFailedType();
     reservationFailed.setRequesterNSA(requesterEndpoint);
     try {
       ((BindingProvider) connectionServiceRequesterPort).getRequestContext().put(
           BindingProvider.ENDPOINT_ADDRESS_PROPERTY, requesterEndpoint);
       connectionServiceRequesterPort.reserveFailed(new Holder<String>(correlationId), reservationFailed);
     }
     catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
       log.error("Error: ", e);
     }
   }
 
   private void sendTerminateTypeToChildNsa(final String childNsa, final String requesterNsa, final String correlationId) {
     final TerminateRequestType terminator = new TerminateRequestType();
     terminator.setCorrelationId(correlationId);
     terminator.setReplyTo(requesterNsa);
     final GenericRequestType genericRequestType = new GenericRequestType();
     genericRequestType.setConnectionId(correlationId);
     genericRequestType.setProviderNSA(childNsa);
     genericRequestType.setRequesterNSA(requesterNsa);
     // TODO: Get security attribute
     genericRequestType.setSessionSecurityAttr(null);
     terminator.setTerminate(genericRequestType);
    log.debug("Sendign terminate event to: {} with id: {}", childNsa, correlationId);
   }
 
   private void sendTerminatToNrm(final String correlationId) {
     log.debug("Sendig nrm terminate event.");
   }
 
   /**
    * @param reserveRequest
    * @throws ServiceException
    */
   private boolean isValidProviderNsa(final ReserveRequestType reserveRequest) {
     return nsaProviderUrns == null ? true : nsaProviderUrns.contains(reserveRequest.getReserve().getProviderNSA());
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
   public GenericAcknowledgmentType reserve(final ReserveRequestType reservationRequest) throws ServiceException {
 
     if (reservationRequest == null) {
      stateMachine.inserOrUpdateState(reservationRequest.getCorrelationId(), CLEANING);
       throw new ServiceException("Invalid reservationRequest received (null)", null);
     }
 
     final String correlationId = reservationRequest.getCorrelationId();
     log.debug("Received reservation request with id: {}", reservationRequest.getCorrelationId());
     stateMachine.inserOrUpdateState(correlationId, INITIAL);
 
     // if (getWebServiceContext() != null) {
     // log.debug("message context: {}",
     // getWebServiceContext().getMessageContext());
     // }
 
     final ReservationInfoType reservation = reservationRequest.getReserve().getReservation();
     if (!isValidCorrelationId(correlationId)) {
       throw new ServiceException("SVC0001", getInvalidParameterServiceExceptionType("correlationId"));
     }
 
     stateMachine.inserOrUpdateState(correlationId, RESERVING);
 
     // Build an internal request for this reservation request.
 
     /*
      * Break out the attributes we need for handling. correlationId is needed
      * for any acknowledgment, confirmation, or failed message.
      */
 
     /*
      * We will send the confirmation, or failed message back to this location.
      * In the future we may remove this parameter and add a csRequesterEndpoint
      * field to NSA topology.
      */
     final String requesterEndpoint = reservationRequest.getReplyTo();
     // log.debug("Requester endpoint: {}", requesterEndpoint);
 
     /*
      * Save the calling NSA security context and pass it along for use during
      * processing of request (when implemented).
      */
 
     /*
      * Extract the reservation information for use by the actor processing
      * logic.
      */
 
     /*
      * Extract NSA fields.
      */
 
     /*
      * Verify that this message was targeting this NSA by looking at the
      * ProviderNSA field. If invalid we will throw an exception.
      */
     if (!isValidProviderNsa(reservationRequest)) {
       throw new ServiceException("SVC0001", getInvalidParameterServiceExceptionType("providerNSA"));
     }
 
     /*
      * Get the connectionId from the reservation as we will use this to
      * serialize related requests.
      */
     final String connectionId = reservation.getConnectionId();
     // log.debug("connectionId {}", connectionId);
 
     // Route this message to the appropriate actor for processing.
 
     // for now always fail the reservation
     forceFailed(correlationId, requesterEndpoint);
 
     /*
      * We successfully sent the message for processing so acknowledge it back to
      * the requesting NSA. We hope this returns before the confirmation makes it
      * back to the requesting NSA.
      */
     final GenericAcknowledgmentType genericAcknowledgment = new GenericAcknowledgmentType();
     genericAcknowledgment.setCorrelationId(correlationId);
 
     log.info("Returning GenericAcknowledgmentType with id: {}", genericAcknowledgment.getCorrelationId());
     return genericAcknowledgment;
   }
 
   /**
    * @param correlationId
    * @param requesterEndpoint
    */
   private void forceFailed(final String correlationId, final String requesterEndpoint) {
     new Timer().schedule(new TimerTask() {
       @Override
       public void run() {
         sendReservationFailed(requesterEndpoint, correlationId);
         stateMachine.inserOrUpdateState(correlationId, CLEANING);
 
        for (final String childNsa : nsaChildren) {
          sendTerminateTypeToChildNsa(childNsa, requesterEndpoint, correlationId);
         }
         sendTerminatToNrm(correlationId);
         stateMachine.deleteState(correlationId);
       }
     }, 2000L);
   }
 
   public GenericAcknowledgmentType provision(ProvisionRequestType parameters) throws ServiceException {
 
     // Build an internal request for this reservation request.
 
     /*
      * Break out the attributes we need for handling. correlationId is needed
      * for any acknowledgment, confirmation, or failed message.
      */
 
     /*
      * We will send the confirmation, or failed message back to this location.
      */
 
     /*
      * Save the calling NSA security context and pass it along for use during
      * processing of request.
      */
 
     // Extract the reservation information.
 
     // Extract NSA fields.
 
     /*
      * Verify that this message was targeting this NSA by looking at the
      * ProviderNSA field. If invalid we will throw an exception.
      */
 
     /*
      * Get the connectionId from the reservation as we will use this to
      * serialize related requests.
      */
 
     // Route this message to the appropriate actor for processing.
 
     /*
      * We successfully sent the message for processing so acknowledge it back to
      * the sending.
      */
     GenericAcknowledgmentType ack = new GenericAcknowledgmentType();
     ack.setCorrelationId(null);
     return ack;
   }
 
   public GenericAcknowledgmentType release(ReleaseRequestType parameters) throws ServiceException {
 
     // Build an internal request for this reservation request.
 
     /*
      * Break out the attributes we need for handling. correlationId is needed
      * for any acknowledgment, confirmation, or failed message.
      */
 
     /*
      * We will send the confirmation, or failed message back to this location.
      */
 
     /*
      * Save the calling NSA security context and pass it along for use during
      * processing of request.
      */
 
     // Extract the reservation information.
 
     // Extract NSA fields.
 
     /*
      * Verify that this message was targeting this NSA by looking at the
      * ProviderNSA field. If invalid we will throw an exception.
      */
 
     /**
      * Get the connectionId from the reservation as we will use this to
      * serialize related requests.
      */
 
     // Route this message to the appropriate actor for processing.
 
     /*
      * We successfully sent the message for processing so acknowledge it back to
      * the sending.
      */
     GenericAcknowledgmentType ack = new GenericAcknowledgmentType();
     ack.setCorrelationId(null);
     return ack;
   }
 
   public GenericAcknowledgmentType terminate(TerminateRequestType parameters) throws ServiceException {
 
     // Build an internal request for this reservation request.
 
     /*
      * Break out the attributes we need for handling. correlationId is needed
      * for any acknowledgment, confirmation, or failed message.
      */
 
     /*
      * We will send the confirmation, or failed message back to this location.
      */
 
     /*
      * Save the calling NSA security context and pass it along for use during
      * processing of request.
      */
 
     // Extract the reservation information.
 
     // Extract NSA fields.
 
     /*
      * Verify that this message was targeting this NSA by looking at the
      * ProviderNSA field. If invalid we will throw an exception.
      */
 
     /*
      * Get the connectionId from the reservation as we will use this to
      * serialize related requests.
      */
 
     // Route this message to the appropriate actor for processing.
 
     /*
      * We successfully sent the message for processing so acknowledge it back to
      * the sending.
      */
     GenericAcknowledgmentType ack = new GenericAcknowledgmentType();
     ack.setCorrelationId(null);
     return ack;
   }
 
   public GenericAcknowledgmentType query(QueryRequestType parameters) throws ServiceException {
 
     // Build an internal request for this reservation request.
 
     /*
      * Break out the attributes we need for handling. correlationId is needed
      * for any acknowledgment, confirmation, or failed message.
      */
 
     /*
      * We will send the confirmation, or failed message back to this location.
      */
 
     /*
      * Save the calling NSA security context and pass it along for use during
      * processing of request.
      */
 
     // Extract the query information.
 
     // We want to route to operation specific provider.
 
     // Extract NSA fields.
 
     /*
      * Verify that this message was targeting this NSA by looking at the
      * ProviderNSA field. If invalid we will throw an exception.
      */
 
     // Route this message to the appropriate actor for processing.
 
     /*
      * We successfully sent the message for processing so acknowledge it back to
      * the sending.
      */
     GenericAcknowledgmentType ack = new GenericAcknowledgmentType();
     ack.setCorrelationId(null);
     return ack;
   }
 
   public void queryConfirmed(Holder<String> correlationId, QueryConfirmedType queryConfirmed) throws ServiceException {
     throw new UnsupportedOperationException("Not implemented yet.");
   }
 
   public void queryFailed(Holder<String> correlationId, QueryFailedType queryFailed) throws ServiceException {
     throw new UnsupportedOperationException("Not implemented yet.");
   }
 
 }
