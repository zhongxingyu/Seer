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
 
 import static com.google.common.base.Optional.fromNullable;
 import static com.google.common.base.Strings.emptyToNull;
 import static com.google.common.collect.Lists.transform;
 import static nl.surfnet.bod.nsi.v2.ConnectionsV2.toQuerySummaryResultType;
 import static nl.surfnet.bod.util.XmlUtils.calendarToDateTime;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.xml.ws.Holder;
 
 import nl.surfnet.bod.domain.ConnectionV2;
 import nl.surfnet.bod.domain.NsiRequestDetails;
 import nl.surfnet.bod.domain.ProtectionType;
 import nl.surfnet.bod.domain.oauth.NsiScope;
 import nl.surfnet.bod.nsi.NsiHelper;
 import nl.surfnet.bod.repo.ConnectionV2Repo;
 import nl.surfnet.bod.service.ConnectionServiceV2;
 import nl.surfnet.bod.service.ConnectionServiceV2.ValidationException;
 import nl.surfnet.bod.util.Environment;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 
 import org.joda.time.DateTime;
 import org.ogf.schemas.nsi._2013._04.connection.provider.ConnectionProviderPort;
 import org.ogf.schemas.nsi._2013._04.connection.provider.QuerySummarySyncFailed;
 import org.ogf.schemas.nsi._2013._04.connection.provider.ServiceException;
 import org.ogf.schemas.nsi._2013._04.connection.types.LifecycleStateEnumType;
 import org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType;
 import org.ogf.schemas.nsi._2013._04.connection.types.QueryFailedType;
 import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryResultType;
 import org.ogf.schemas.nsi._2013._04.connection.types.ReservationRequestCriteriaType;
 import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;
 import org.ogf.schemas.nsi._2013._04.connection.types.StpType;
 import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
 import org.ogf.schemas.nsi._2013._04.framework.types.ServiceExceptionType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
 
 import com.google.common.base.Optional;
 import com.sun.xml.ws.developer.SchemaValidation;
 
 @Service("connectionServiceProviderWs_v2")
 @WebService(serviceName = "ConnectionServiceProvider",
   portName = "ConnectionServiceProviderPort",
   endpointInterface = "org.ogf.schemas.nsi._2013._04.connection.provider.ConnectionProviderPort",
   targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/provider")
 @SchemaValidation
 public class ConnectionServiceProviderV2Ws implements ConnectionProviderPort {
 
   private final Logger log = LoggerFactory.getLogger(ConnectionServiceProviderV2Ws.class);
 
   @Resource private ConnectionServiceV2 connectionService;
   @Resource private ConnectionV2Repo connectionRepo;
   @Resource private Environment bodEnvironment;
 
   @Override
   public void reserve(
       @WebParam(name = "connectionId", targetNamespace = "", mode = WebParam.Mode.INOUT) Holder<String> connectionId,
       @WebParam(name = "globalReservationId", targetNamespace = "") String globalReservationId,
       @WebParam(name = "description", targetNamespace = "") String description,
       @WebParam(name = "criteria", targetNamespace = "") ReservationRequestCriteriaType criteria,
       @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> header)
       throws ServiceException {
 
     checkOAuthScope(NsiScope.RESERVE);
 
     log.info("Received a NSI v2 reserve request");
 
     NsiRequestDetails requestDetails = createRequestDetails(header.value);
 
     ConnectionV2 connection = createConnection(
         Optional.fromNullable(emptyToNull(globalReservationId)),
         Optional.fromNullable(emptyToNull(description)),
         requestDetails,
         header.value.getProviderNSA(),
         header.value.getRequesterNSA(),
         criteria);
 
     connectionId.value = connection.getConnectionId();
 
     reserve(connection, requestDetails, Security.getUserDetails());
   }
 
   protected void reserve(ConnectionV2 connection, NsiRequestDetails requestDetails, RichUserDetails richUserDetails) throws ServiceException {
     try {
       connectionService.reserve(connection, requestDetails, false, richUserDetails);
     } catch (ValidationException e) {
       ServiceExceptionType faultInfo = new ServiceExceptionType()
         .withErrorId(e.getErrorCode())
         .withNsaId(bodEnvironment.getNsiProviderNsa())
         .withText(e.getMessage());
 
       throw new ServiceException(e.getMessage(), faultInfo, e);
     }
   }
 
   private ConnectionV2 createConnection(Optional<String> globalReservationId, Optional<String> description, NsiRequestDetails requestDetails, String providerNsa, String requesterNsa, ReservationRequestCriteriaType criteria) {
     Optional<DateTime> startTime = fromNullable(criteria.getSchedule().getStartTime()).transform(calendarToDateTime);
     Optional<DateTime> endTime = fromNullable(criteria.getSchedule().getEndTime()).transform(calendarToDateTime);
 
     ConnectionV2 connection = new ConnectionV2();
     connection.setReservationState(ReservationStateEnumType.RESERVE_START);
     connection.setLifecycleState(LifecycleStateEnumType.CREATED);
    connection.setProvisionState(ProvisionStateEnumType.UNKNOWN);
     connection.setConnectionId(NsiHelper.generateConnectionId());
     connection.setGlobalReservationId(globalReservationId.or(NsiHelper.generateGlobalReservationId()));
     connection.setDescription(description.orNull());
     connection.setStartTime(startTime.orNull());
     connection.setEndTime(endTime.orNull());
     connection.setDesiredBandwidth(criteria.getBandwidth());
     connection.setProtectionType(ProtectionType.PROTECTED.name());
     connection.setSourceStpId(stpTypeToStpId(criteria.getPath().getSourceSTP()));
     connection.setDestinationStpId(stpTypeToStpId(criteria.getPath().getDestSTP()));
     connection.setProviderNsa(providerNsa);
     connection.setRequesterNsa(requesterNsa);
     connection.setProvisionRequestDetails(requestDetails);
 
     return connection;
   }
 
   private String stpTypeToStpId(StpType type) {
     return type.getNetworkId() + ":" + type.getLocalId();
   }
 
   @Override
   public void reserveCommit(
       @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
       @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> header)
       throws ServiceException {
 
     // Checking for the reserve scope for now..
     checkOAuthScope(NsiScope.RESERVE);
 
     log.info("Received reserve commit request for connection: {}", connectionId);
 
     ConnectionV2 connection = getConnectionOrFail(connectionId);
 
     connectionService.asyncReserveCommit(connection, createRequestDetails(header.value));
   }
 
   @Override
   public void reserveAbort(
       @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
       @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> header)
       throws ServiceException {
 
     // using TERMINATE scope for now
     checkOAuthScope(NsiScope.TERMINATE);
 
     log.info("Received Reserve Abort for connection: {}", connectionId);
 
     ConnectionV2 connection = getConnectionOrFail(connectionId);
 
     connectionService.asyncReserveAbort(connection, createRequestDetails(header.value), Security.getUserDetails());
   }
 
   @Override
   public void provision(
       @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
       @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> header)
       throws ServiceException {
 
     checkOAuthScope(NsiScope.PROVISION);
 
     log.info("Received a Provision for connection: {}", connectionId);
 
     ConnectionV2 connection = getConnectionOrFail(connectionId);
 
     checkProvisionAllowed(connection);
 
     connectionService.asyncProvision(connection, createRequestDetails(header.value));
   }
 
   private void checkProvisionAllowed(ConnectionV2 connection) throws ServiceException {
     if (connection.getProvisionState() != ProvisionStateEnumType.RELEASED) {
       notApplicable();
     }
   }
 
   @Override
   public void release(
       @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
       @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> header)
       throws ServiceException {
 
     notSupporedOperation();
   }
 
   @Override
   public void terminate(
       @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
       @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> header)
       throws ServiceException {
 
     checkOAuthScope(NsiScope.TERMINATE);
 
     log.info("Received a Terminate for connection: {}", connectionId);
 
     ConnectionV2 connection = getConnectionOrFail(connectionId);
 
     connectionService.asyncTerminate(connection, createRequestDetails(header.value), Security.getUserDetails());
   }
 
   @Override
   public void querySummary(
       @WebParam(name = "connectionId", targetNamespace = "") List<String> connectionIds,
       @WebParam(name = "globalReservationId", targetNamespace = "") List<String> globalReservationIds,
       @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> header)
       throws ServiceException {
 
     checkOAuthScope(NsiScope.QUERY);
 
     log.info("Received a Query Summary");
 
     NsiRequestDetails requestDetails = createRequestDetails(header.value);
 
     connectionService.asyncQuerySummary(connectionIds, globalReservationIds, requestDetails, header.value.getRequesterNSA());
   }
 
   @Override
   public void queryRecursive(
       @WebParam(name = "connectionId", targetNamespace = "") List<String> connectionId,
       @WebParam(name = "globalReservationId", targetNamespace = "") List<String> globalReservationId,
       @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> header)
       throws ServiceException {
 
     notSupporedOperation();
   }
 
   @Override
   public List<QuerySummaryResultType> querySummarySync(
       @WebParam(name = "connectionId", targetNamespace = "") List<String> connectionIds,
       @WebParam(name = "globalReservationId", targetNamespace = "") List<String> globalReservationIds,
       @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> header)
       throws QuerySummarySyncFailed {
 
     try {
       checkOAuthScope(NsiScope.QUERY);
     } catch (ServiceException e) {
       throw toQuerySummarySyncFailed(e);
     }
 
     log.info("Received a Query Summary Sync");
 
     NsiRequestDetails requestDetails = createRequestDetails(header.value);
 
     List<ConnectionV2> connections = connectionService.querySummarySync(connectionIds, globalReservationIds, requestDetails, header.value.getRequesterNSA());
 
     return transform(connections, toQuerySummaryResultType);
   }
 
   private QuerySummarySyncFailed toQuerySummarySyncFailed(ServiceException e) {
     return new QuerySummarySyncFailed(e.getMessage(), new QueryFailedType().withServiceException(e.getFaultInfo()));
   }
 
   private ConnectionV2 getConnectionOrFail(String connectionId) throws ServiceException {
     ConnectionV2 connection = connectionRepo.findByConnectionId(connectionId);
     if (connection == null) {
       throw connectionNotFoundServiceException();
     }
 
     return connection;
   }
 
   private void checkOAuthScope(NsiScope scope) throws ServiceException {
     if (!Security.hasOauthScope(scope)) {
       throw unAuthorizedServiceException();
     }
   }
 
   private NsiRequestDetails createRequestDetails(CommonHeaderType header) {
     return new NsiRequestDetails(header.getReplyTo(), header.getCorrelationId());
   }
 
   private void notApplicable() throws ServiceException {
     throw new ServiceException("This operation is not applicable", createServiceExceptionType("Not Applicable"));
   }
 
   private void notSupporedOperation() throws ServiceException {
     throw new ServiceException("This operation is not supported by this provider", createServiceExceptionType("Not Supported"));
   }
 
   private ServiceException connectionNotFoundServiceException() {
     return new ServiceException("Could not find connection", createServiceExceptionType("Not found"));
   }
 
   private ServiceException unAuthorizedServiceException() {
     return new ServiceException("Unauthorized", createServiceExceptionType("Unauthorized"));
   }
 
   private ServiceExceptionType createServiceExceptionType(String message) {
     return new ServiceExceptionType()
       .withNsaId(bodEnvironment.getNsiProviderNsa())
       .withErrorId("0100")
       .withText(message);
   }
 
 }
