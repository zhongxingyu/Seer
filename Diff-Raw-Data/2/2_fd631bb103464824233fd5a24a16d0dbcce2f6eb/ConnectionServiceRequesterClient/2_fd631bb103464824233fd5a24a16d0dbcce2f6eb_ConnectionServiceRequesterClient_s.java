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
 package nl.surfnet.bod.nsi.v2;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URL;
 import java.util.List;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.Holder;
 
 import com.google.common.collect.ImmutableList;
 import com.sun.xml.ws.client.ClientTransportException;
 
 import org.ogf.schemas.nsi._2013._04.connection.requester.ConnectionRequesterPort;
 import org.ogf.schemas.nsi._2013._04.connection.requester.ConnectionServiceRequester;
 import org.ogf.schemas.nsi._2013._04.connection.requester.ServiceException;
 import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStatusType;
 import org.ogf.schemas.nsi._2013._04.connection.types.QueryRecursiveResultType;
 import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryResultType;
 import org.ogf.schemas.nsi._2013._04.connection.types.ReservationConfirmCriteriaType;
 import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.stereotype.Component;
 
 @Component
 public class ConnectionServiceRequesterClient {
 
   private static final String WSDL_LOCATION = "/wsdl/2.0/ogf_nsi_connection_requester_v2_0.wsdl";
 
   private final Logger log = LoggerFactory.getLogger(ConnectionServiceRequesterClient.class);
 
   public void sendReserveConfirmed(CommonHeaderType header, String connectionId, String globalReservationId,
       String description, ImmutableList<ReservationConfirmCriteriaType> criteria, URI replyTo) {
     ConnectionRequesterPort port = createPort(replyTo);
     try {
       port.reserveConfirmed(connectionId, globalReservationId, description, criteria, new Holder<>(header));
     } catch (ClientTransportException | ServiceException e) {
       log.info("Sending Reserve Confirmed failed", e);
     }
   }
 
   public void sendAbortConfirmed(CommonHeaderType header, String connectionId, URI replyTo) {
     ConnectionRequesterPort port = createPort(replyTo);
     try {
       port.reserveAbortConfirmed(connectionId, new Holder<>(header));
     } catch (ServiceException e) {
       log.info("Sending Reserve Abort Confirmed failed", e);
     }
   }
 
   public void sendTerminateConfirmed(CommonHeaderType header, String connectionId, URI replyTo) {
     ConnectionRequesterPort port = createPort(replyTo);
     try {
       port.terminateConfirmed(connectionId, new Holder<>(header));
     } catch (ClientTransportException | ServiceException e) {
       log.info("Sending Terminate Confirmed failed", e);
     }
   }
 
   public void sendReserveAbortConfirmed(CommonHeaderType header, String connectionId, URI replyTo) {
     ConnectionRequesterPort port = createPort(replyTo);
     try {
       port.reserveAbortConfirmed(connectionId, new Holder<>(header));
     } catch (ClientTransportException | ServiceException e) {
       log.info("Sending Reserve Abort Confirmed failed", e);
     }
 
   }
 
   public void sendReserveCommitConfirmed(CommonHeaderType header, String connectionId, URI replyTo) {
     ConnectionRequesterPort port = createPort(replyTo);
     try {
       port.reserveCommitConfirmed(connectionId, new Holder<>(header));
     } catch (ClientTransportException | ServiceException e) {
       log.info("Sending Reserve Commit Confirmed failed", e);
     }
   }
 
   public void sendProvisionConfirmed(CommonHeaderType header, String connectionId, URI replyTo) {
     ConnectionRequesterPort port = createPort(replyTo);
     try {
       port.provisionConfirmed(connectionId, new Holder<>(header));
     } catch (ClientTransportException | ServiceException e) {
       log.info("Sending Provision Confirmed failed", e);
     }
   }
 
   public void sendDataPlaneStatus(CommonHeaderType header, String connectionId, DataPlaneStatusType dataPlaneStatus,
       XMLGregorianCalendar timeStamp, URI replyTo) {
     ConnectionRequesterPort port = createPort(replyTo);
     try {
       port.dataPlaneStateChange(connectionId, dataPlaneStatus, timeStamp, new Holder<>(header));
     } catch (ClientTransportException | ServiceException e) {
       log.info("Failed to send Data Plane State Change");
     }
   }
 
   public void sendQuerySummaryConfirmed(CommonHeaderType header, List<QuerySummaryResultType> results, URI replyTo) {
     ConnectionRequesterPort port = createPort(replyTo);
     try {
       port.querySummaryConfirmed(results, new Holder<>(header));
     } catch (ClientTransportException | ServiceException e) {
       log.info("Failed to send query summary confirmed", e);
     }
   }
 
   public void sendQueryRecursiveConfirmed(CommonHeaderType header, List<QueryRecursiveResultType> result, URI replyTo) {
     ConnectionRequesterPort port = createPort(replyTo);
     try {
       port.queryRecursiveConfirmed(result, new Holder<>(header));
     } catch (ClientTransportException | ServiceException e) {
       log.info("Failed to send query recursive confirmed", e);
     }
   }
 
   private ConnectionRequesterPort createPort(URI endpoint) {
     ConnectionRequesterPort port = new ConnectionServiceRequester(wsdlUrl()).getConnectionServiceRequesterPort();
    ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
 
     return port;
   }
 
   private URL wsdlUrl() {
     try {
       return new ClassPathResource(WSDL_LOCATION).getURL();
     }
     catch (IOException e) {
       throw new RuntimeException("Could not find the requester wsdl", e);
     }
   }
 
 }
