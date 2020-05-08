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
 
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.annotation.Resource;
 import javax.jws.WebService;
 import javax.xml.ws.Holder;
 import javax.xml.ws.WebServiceContext;
 
 import nl.surfnet.bod.service.ReservationService;
 import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
 import oasis.names.tc.saml._2_0.assertion.AttributeType;
 
 import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.ProvisionRequestType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.ReleaseRequestType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.TerminateRequestType;
 import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
 import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
 import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ServiceExceptionType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service("nsiConnectionServiceProvider")
 @WebService(serviceName = "ConnectionServiceProvider",
     portName = "ConnectionServiceProviderPort",
     endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort",
     targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/provider",
     wsdlLocation = "/WEB-INF/wsdl/nsi/ogf_nsi_connection_provider_v1_0.wsdl")
 public class NsiConnectionServiceProvider {
 
   private final Logger log = LoggerFactory.getLogger(getClass());
 
   @Resource(name = "nsaProviderUrns")
   private List<String> nsaProviderUrns;
 
   /*
    * This holds the web service request context which includes all the original
    * HTTP information, including the JAAS authentication and authorisation
    * information.
    */
   @Resource
   private WebServiceContext webServiceContext;
 
   @Autowired
   private ReservationService reservationService;
 
   @PostConstruct
   @SuppressWarnings("unused")
   private void init() {
     log.debug("webServiceContext: " + webServiceContext);
     log.debug("reservationService: " + reservationService);
   }
 
   @PreDestroy
   @SuppressWarnings("unused")
   private void destroy() {
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
      throw new ServiceException("Invalid reservationRequest received (null)", null);
    }
    
    if (webServiceContext != null) {
      log.debug("message context: {}", webServiceContext.getMessageContext());
    }

     final ReservationInfoType reservation = reservationRequest.getReserve().getReservation();
     final String correlationId = reservationRequest.getCorrelationId();
 
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
     log.info("Requester endpoint: {}", requesterEndpoint);
 
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
      throw new ServiceException("SVC0001", createProviderNotSupportedError());
     }
    
     /*
      * Get the connectionId from the reservation as we will use this to
      * serialize related requests.
      */
     final String connectionId = reservation.getConnectionId();
     log.debug("connectionId {}", connectionId);
 
     // Route this message to the appropriate actor for processing.
 
     /*
      * We successfully sent the message for processing so acknowledge it back to
      * the requesting NSA. We hope this returns before the confirmation makes it
      * back to the requesting NSA.
      */
     final GenericAcknowledgmentType genericAcknowledgment = new GenericAcknowledgmentType();
     genericAcknowledgment.setCorrelationId(correlationId);
     return genericAcknowledgment;
   }
 
   /**
    * @return
    */
  private ServiceExceptionType createProviderNotSupportedError() {
     final ServiceExceptionType faultInfo = new ServiceExceptionType();
     faultInfo.setErrorId("SVC0001");
     faultInfo.setText("Invalid or missing parameter");
     final AttributeType attributeType = new AttributeType();
     attributeType.setName("providerNSA");
     attributeType.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
     final AttributeStatementType attributeStatementType = new AttributeStatementType();
     attributeStatementType.getAttributeOrEncryptedAttribute().add(attributeType);
     faultInfo.setVariables(attributeStatementType);
     return faultInfo;
   }
 
   /**
    * @param reserveRequest
    * @throws ServiceException
    */
  private boolean isValidProviderNsa(final ReserveRequestType reserveRequest) {
     return nsaProviderUrns == null ? true : nsaProviderUrns.contains(reserveRequest.getReserve().getProviderNSA());
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
 
   static {
     System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
   }
 
 }
