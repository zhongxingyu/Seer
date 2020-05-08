 package org.cagrid.cds.service.wsrf;
 
 import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata;
 import org.cagrid.cds.model.DelegatedCredentialAuditRecord;
 import org.cagrid.cds.model.DelegationDescriptor;
 import org.cagrid.cds.model.DelegationIdentifier;
 import org.cagrid.cds.model.DelegationRecord;
 import org.cagrid.cds.model.DelegationSigningRequest;
 import org.cagrid.cds.service.CredentialDelegationService;
 import org.cagrid.cds.service.exception.CDSInternalException;
 import org.cagrid.cds.service.exception.DelegationException;
 import org.cagrid.cds.service.exception.InvalidPolicyException;
 import org.cagrid.cds.service.exception.PermissionDeniedException;
 import org.cagrid.cds.wsrf.stubs.AddAdminRequest;
 import org.cagrid.cds.wsrf.stubs.AddAdminResponse;
 import org.cagrid.cds.wsrf.stubs.ApproveDelegationRequest;
 import org.cagrid.cds.wsrf.stubs.ApproveDelegationResponse;
 import org.cagrid.cds.wsrf.stubs.CDSInternalFaultFaultMessage;
 import org.cagrid.cds.wsrf.stubs.CredentialDelegationServicePortTypeImpl;
 import org.cagrid.cds.wsrf.stubs.DelegationFaultFaultMessage;
 import org.cagrid.cds.wsrf.stubs.DeleteDelegatedCredentialRequest;
 import org.cagrid.cds.wsrf.stubs.DeleteDelegatedCredentialResponse;
 import org.cagrid.cds.wsrf.stubs.FindCredentialsDelegatedToClientRequest;
 import org.cagrid.cds.wsrf.stubs.FindCredentialsDelegatedToClientResponse;
 import org.cagrid.cds.wsrf.stubs.FindDelegatedCredentialsRequest;
 import org.cagrid.cds.wsrf.stubs.FindDelegatedCredentialsResponse;
 import org.cagrid.cds.wsrf.stubs.GetAdminsRequest;
 import org.cagrid.cds.wsrf.stubs.GetAdminsResponse;
 import org.cagrid.cds.wsrf.stubs.InitiateDelegationRequest;
 import org.cagrid.cds.wsrf.stubs.InitiateDelegationResponse;
 import org.cagrid.cds.wsrf.stubs.InvalidPolicyFaultFaultMessage;
 import org.cagrid.cds.wsrf.stubs.PermissionDeniedFaultFaultMessage;
 import org.cagrid.cds.wsrf.stubs.RemoveAdminRequest;
 import org.cagrid.cds.wsrf.stubs.RemoveAdminResponse;
 import org.cagrid.cds.wsrf.stubs.SearchDelegatedCredentialAuditLogRequest;
 import org.cagrid.cds.wsrf.stubs.SearchDelegatedCredentialAuditLogResponse;
 import org.cagrid.cds.wsrf.stubs.UpdateDelegatedCredentialStatusRequest;
 import org.cagrid.cds.wsrf.stubs.UpdateDelegatedCredentialStatusResponse;
 import org.cagrid.core.common.JAXBUtils;
 import org.cagrid.core.resource.SimpleResourceKey;
 import org.cagrid.delegatedcredential.types.DelegatedCredentialReference;
 import org.cagrid.gaards.authentication.WebServiceCallerId;
 import org.cagrid.gaards.security.servicesecurity.GetServiceSecurityMetadataRequest;
 import org.cagrid.gaards.security.servicesecurity.GetServiceSecurityMetadataResponse;
 import org.cagrid.wsrf.properties.InvalidResourceKeyException;
 import org.cagrid.wsrf.properties.NoSuchResourceException;
 import org.cagrid.wsrf.properties.Resource;
 import org.cagrid.wsrf.properties.ResourceException;
 import org.cagrid.wsrf.properties.ResourceHome;
 import org.cagrid.wsrf.properties.ResourceKey;
 import org.cagrid.wsrf.properties.ResourceProperty;
 import org.cagrid.wsrf.properties.ResourcePropertySet;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.GetMultipleResourceProperties;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.GetMultipleResourcePropertiesResponse;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.GetResourcePropertyResponse;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.QueryResourceProperties;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.QueryResourcePropertiesResponse;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01_wsdl.InvalidQueryExpressionFault;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01_wsdl.InvalidResourcePropertyQNameFault;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01_wsdl.QueryEvaluationErrorFault;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01_wsdl.ResourceUnknownFault;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01_wsdl.UnknownQueryExpressionDialectFault;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xmlsoap.schemas.ws._2004._03.addressing.AttributedURI;
 import org.xmlsoap.schemas.ws._2004._03.addressing.EndpointReferenceType;
 import org.xmlsoap.schemas.ws._2004._03.addressing.ReferencePropertiesType;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.namespace.QName;
 import javax.xml.soap.SOAPBodyElement;
 import javax.xml.soap.SOAPElement;
 import javax.xml.ws.WebServiceContext;
 import javax.xml.ws.handler.MessageContext;
 
 import java.util.List;
 
 public class CDSWSRFImpl extends CredentialDelegationServicePortTypeImpl {
 
     private final Logger logger;
 
     private final CredentialDelegationService cds;
     private final ResourceHome resourceHome;
 
     @javax.annotation.Resource
     private WebServiceContext wsContext;
 
     public CDSWSRFImpl(CredentialDelegationService cds) {
         this.logger = LoggerFactory.getLogger(getClass());
         this.cds = cds;
         this.resourceHome = cds.getResourceHome();
     }
 
     @Override
     public AddAdminResponse addAdmin(AddAdminRequest parameters) throws PermissionDeniedFaultFaultMessage, CDSInternalFaultFaultMessage {
         String message = "addAdmin";
         logger.debug(message);
         try {
             cds.addAdmin(getCallerId(), parameters.getGridIdentity());
             return new AddAdminResponse();
         } catch (PermissionDeniedException e) {
             throw new PermissionDeniedFaultFaultMessage(message, e.getFault());
         } catch (CDSInternalException e) {
             throw new CDSInternalFaultFaultMessage(message, e.getFault());
         }
     }
 
     @Override
     public DeleteDelegatedCredentialResponse deleteDelegatedCredential(DeleteDelegatedCredentialRequest parameters) throws PermissionDeniedFaultFaultMessage,
             CDSInternalFaultFaultMessage {
         String message = "deleteDelegatedCredential";
         logger.debug(message);
         try {
             cds.deleteDelegatedCredential(getCallerId(), parameters.getId().getDelegationIdentifier());
             return new DeleteDelegatedCredentialResponse();
         } catch (PermissionDeniedException e) {
             throw new PermissionDeniedFaultFaultMessage(message, e.getFault());
         } catch (CDSInternalException e) {
             throw new CDSInternalFaultFaultMessage(message, e.getFault());
         }
     }
 
     @Override
     public ApproveDelegationResponse approveDelegation(ApproveDelegationRequest parameters) throws DelegationFaultFaultMessage, CDSInternalFaultFaultMessage,
             PermissionDeniedFaultFaultMessage {
         String message = "approveDelegation";
         logger.debug(message);
         try {
             DelegationIdentifier id = cds.approveDelegation(getCallerId(), parameters.getDelegationSigningResponse().getDelegationSigningResponse());
             ApproveDelegationResponse response = new ApproveDelegationResponse();
             response.setDelegatedCredentialReference(getDelegatedCredentialRefernce(id));
             return response;
         } catch (DelegationException e) {
             logger.debug(message, e);
             throw new DelegationFaultFaultMessage(message, e.getFault());
         } catch (PermissionDeniedException e) {
             logger.debug(message, e);
             throw new PermissionDeniedFaultFaultMessage(message, e.getFault());
         } catch (CDSInternalException e) {
             logger.debug(message, e);
             throw new CDSInternalFaultFaultMessage(message, e.getFault());
         }
     }
 
     @Override
     public FindDelegatedCredentialsResponse findDelegatedCredentials(FindDelegatedCredentialsRequest parameters) throws PermissionDeniedFaultFaultMessage,
             CDSInternalFaultFaultMessage {
         String message = "findDelegatedCredentials";
         logger.debug(message);
         try {
             List<DelegationRecord> records = cds.findDelegatedCredentials(getCallerId(), parameters.getFilter().getDelegationRecordFilter());
             FindDelegatedCredentialsResponse response = new FindDelegatedCredentialsResponse();
             response.getDelegationRecord().addAll(records);
             return response;
         } catch (PermissionDeniedException e) {
             throw new PermissionDeniedFaultFaultMessage(message, e.getFault());
         } catch (CDSInternalException e) {
             throw new CDSInternalFaultFaultMessage(message, e.getFault());
         }
     }
 
     @Override
     public InitiateDelegationResponse initiateDelegation(InitiateDelegationRequest parameters) throws InvalidPolicyFaultFaultMessage,
             DelegationFaultFaultMessage, CDSInternalFaultFaultMessage, PermissionDeniedFaultFaultMessage {
         String message = "initiateDelegation";
         logger.debug(message);
         try {
             DelegationSigningRequest dsr = cds.initiateDelegation(getCallerId(), parameters.getReq().getDelegationRequest());
             InitiateDelegationResponse response = new InitiateDelegationResponse();
             response.setDelegationSigningRequest(dsr);
             return response;
         } catch (DelegationException e) {
             logger.debug(message, e);
             throw new DelegationFaultFaultMessage(message, e.getFault());
         } catch (PermissionDeniedException e) {
             logger.debug(message, e);
             throw new PermissionDeniedFaultFaultMessage(message, e.getFault());
         } catch (CDSInternalException e) {
             logger.debug(message, e);
             throw new CDSInternalFaultFaultMessage(message, e.getFault());
         } catch (InvalidPolicyException e) {
             logger.debug(message, e);
             throw new InvalidPolicyFaultFaultMessage(message, e.getFault());
         }
     }
 
     @Override
     public org.cagrid.cds.wsrf.stubs.FindCredentialsDelegatedToClientResponse findCredentialsDelegatedToClient(
             FindCredentialsDelegatedToClientRequest parameters) throws PermissionDeniedFaultFaultMessage, CDSInternalFaultFaultMessage {
         String message = "findCredentialsDelegatedToClient";
         logger.debug(message);
         try {
             logger.debug("Looking for credentials delegated to client:" + getCallerId() + ", using filter:"
                     + parameters.getFilter().getClientDelegationFilter().getGridIdentity());
             List<DelegationRecord> records = cds.findCredentialsDelegatedToClient(getCallerId(), parameters.getFilter().getClientDelegationFilter());
             FindCredentialsDelegatedToClientResponse response = new FindCredentialsDelegatedToClientResponse();
             logger.debug("Found " + records.size() + " records.");
             for (DelegationRecord record : records) {
                 DelegationDescriptor descriptor = new DelegationDescriptor();
                 descriptor.setDelegatedCredentialReference(getDelegatedCredentialRefernce(record.getDelegationIdentifier()));
                 descriptor.setExpiration(record.getExpiration());
                 descriptor.setGridIdentity(record.getGridIdentity());
                 descriptor.setIssuedCredentialLifetime(record.getIssuedCredentialLifetime());
                 descriptor.setIssuedCredentialPathLength(record.getIssuedCredentialPathLength());
                 response.getDelegationDescriptor().add(descriptor);
                 logger.debug("Returning record:" + record.getDelegationIdentifier());
             }
             return response;
         } catch (PermissionDeniedException e) {
             throw new PermissionDeniedFaultFaultMessage(message, e.getFault());
         } catch (CDSInternalException e) {
             throw new CDSInternalFaultFaultMessage(message, e.getFault());
         }
     }
 
     @Override
     public UpdateDelegatedCredentialStatusResponse updateDelegatedCredentialStatus(UpdateDelegatedCredentialStatusRequest parameters)
             throws DelegationFaultFaultMessage, CDSInternalFaultFaultMessage, PermissionDeniedFaultFaultMessage {
         String message = "updateDelegatedCredentialStatus";
         logger.debug(message);
         try {
             cds.updateDelegatedCredentialStatus(getCallerId(), parameters.getId().getDelegationIdentifier(), parameters.getStatus().getDelegationStatus());
             return new UpdateDelegatedCredentialStatusResponse();
         } catch (DelegationException e) {
             throw new DelegationFaultFaultMessage(message, e.getFault());
         } catch (PermissionDeniedException e) {
             throw new PermissionDeniedFaultFaultMessage(message, e.getFault());
         } catch (CDSInternalException e) {
             throw new CDSInternalFaultFaultMessage(message, e.getFault());
         }
     }
 
     @Override
     public SearchDelegatedCredentialAuditLogResponse searchDelegatedCredentialAuditLog(SearchDelegatedCredentialAuditLogRequest parameters)
             throws DelegationFaultFaultMessage, CDSInternalFaultFaultMessage, PermissionDeniedFaultFaultMessage {
         String message = "searchDelegatedCredentialAuditLog";
         logger.debug(message);
         try {
             List<DelegatedCredentialAuditRecord> records = cds.searchDelegatedCredentialAuditLog(getCallerId(), parameters.getF()
                     .getDelegatedCredentialAuditFilter());
             SearchDelegatedCredentialAuditLogResponse response = new SearchDelegatedCredentialAuditLogResponse();
             response.getDelegatedCredentialAuditRecord().addAll(records);
             return response;
         } catch (DelegationException e) {
             throw new DelegationFaultFaultMessage(message, e.getFault());
         } catch (PermissionDeniedException e) {
             throw new PermissionDeniedFaultFaultMessage(message, e.getFault());
         } catch (CDSInternalException e) {
             throw new CDSInternalFaultFaultMessage(message, e.getFault());
         }
     }
 
     @Override
     public RemoveAdminResponse removeAdmin(RemoveAdminRequest parameters) throws PermissionDeniedFaultFaultMessage, CDSInternalFaultFaultMessage {
         String message = "removeAdmin";
         logger.debug(message);
         try {
             cds.removeAdmin(getCallerId(), parameters.getGridIdentity());
             return new RemoveAdminResponse();
         } catch (PermissionDeniedException e) {
             throw new PermissionDeniedFaultFaultMessage(message, e.getFault());
         } catch (CDSInternalException e) {
             throw new CDSInternalFaultFaultMessage(message, e.getFault());
         }
     }
 
     @Override
     public GetAdminsResponse getAdmins(GetAdminsRequest parameters) throws PermissionDeniedFaultFaultMessage, CDSInternalFaultFaultMessage {
         String message = "getAdmins";
         logger.debug(message);
 
         try {
             GetAdminsResponse response = new GetAdminsResponse();
             response.getResponse().addAll(cds.getAdmins(getCallerId()));
             return response;
         } catch (PermissionDeniedException e) {
             throw new PermissionDeniedFaultFaultMessage(message, e.getFault());
         } catch (CDSInternalException e) {
             throw new CDSInternalFaultFaultMessage(message, e.getFault());
         }
     }
 
     @Override
     public GetResourcePropertyResponse getResourceProperty(QName resourcePropertyQName) throws ResourceUnknownFault, InvalidResourcePropertyQNameFault {
         logger.debug("getResourceProperty " + resourcePropertyQName);
         Exception e = null;
         GetResourcePropertyResponse response = null;
         try {
             Resource resource = resourceHome.find(null);
             if (resource instanceof ResourcePropertySet) {
                 ResourcePropertySet resourcePropertySet = (ResourcePropertySet) resource;
                 ResourceProperty<?> resourceProperty = resourcePropertySet.get(resourcePropertyQName);
                 if (resourceProperty != null) {
                     Object resourcePropertyValue = resourceProperty.get(0);
                     logger.debug("getResourceProperty " + resourcePropertyQName + " returning " + resourcePropertyValue);
                     resourcePropertyValue = JAXBUtils.wrap(resourcePropertyValue);
                     response = new GetResourcePropertyResponse();
                     response.getAny().add(resourcePropertyValue);
                 }
             }
         } catch (NoSuchResourceException nsre) {
             e = nsre;
         } catch (InvalidResourceKeyException irke) {
             e = irke;
         } catch (ResourceException re) {
             e = re;
         }
         if ((response == null) || (e != null)) {
             throw new ResourceUnknownFault("No resource for '" + resourcePropertyQName + "'", e);
         }
         return response;
     }
 
     @Override
     public GetServiceSecurityMetadataResponse getServiceSecurityMetadata(GetServiceSecurityMetadataRequest getServiceSecurityMetadataRequest) {
         logger.debug("getServiceSecurityMetadata");
         ServiceSecurityMetadata serviceSecurityMetadata = cds.getServiceSecurityMetadata();
         GetServiceSecurityMetadataResponse response = new GetServiceSecurityMetadataResponse();
         response.setServiceSecurityMetadata(serviceSecurityMetadata);
         return response;
     }
 
     @Override
     public QueryResourcePropertiesResponse queryResourceProperties(QueryResourceProperties queryResourcePropertiesRequest) throws QueryEvaluationErrorFault,
             InvalidQueryExpressionFault, ResourceUnknownFault, InvalidResourcePropertyQNameFault, UnknownQueryExpressionDialectFault {
         // TODO
         QueryResourcePropertiesResponse response = null;
         response = new QueryResourcePropertiesResponse();
         return response;
     }
 
     @Override
     public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(GetMultipleResourceProperties getMultipleResourcePropertiesRequest)
             throws ResourceUnknownFault, InvalidResourcePropertyQNameFault {
 
         String message = "getMultipleResourceProperties";
         logger.debug(message);
 
         // TODO
         return super.getMultipleResourceProperties(getMultipleResourcePropertiesRequest);
     }
 
     private String getCallerId() {
         return WebServiceCallerId.getCallerId(wsContext);
     }
 
     private DelegatedCredentialReference getDelegatedCredentialRefernce(DelegationIdentifier id) throws CDSInternalFaultFaultMessage {
 
         try {
             MessageContext msgContext = wsContext.getMessageContext();
             HttpServletRequest request = (HttpServletRequest) msgContext.get("HTTP.REQUEST");
            StringBuffer transportURL = request.getRequestURL();
            transportURL.append("/DelegatedCredential");
 
            EndpointReferenceType epr = createEndpointReference(transportURL.toString(), getResourceKey(id));
             DelegatedCredentialReference response = new DelegatedCredentialReference();
             response.setEndpointReference(epr);
             return response;
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
             throw new CDSInternalFaultFaultMessage("Unexpected error creating EPR.", e);
         }
     }
 
     private EndpointReferenceType createEndpointReference(String address, ResourceKey key) throws Exception {
         EndpointReferenceType reference = new EndpointReferenceType();
         if (key != null) {
             ReferencePropertiesType referenceProperties = new ReferencePropertiesType();
 
             SOAPElement elem = key.toSOAPElement();
 
             setAny(referenceProperties, elem);
 
             reference.setReferenceProperties(referenceProperties);
         }
 
         AttributedURI uri = new AttributedURI();
         uri.setValue(address);
         reference.setAddress(uri);
 
         return reference;
     }
 
     private ResourceKey getResourceKey(DelegationIdentifier id) throws Exception {
         // TODO: move this elsewhere common with service bundle
         ResourceKey key = new SimpleResourceKey(new QName("http://cds.gaards.cagrid.org/CredentialDelegationService/DelegatedCredential",
                 "DelegatedCredentialKey"), id);
         return key;
     }
 
     private void setAny(ReferencePropertiesType object, SOAPElement value) {
         if (value == null || object == null) {
             return;
         }
         if (!(value instanceof SOAPBodyElement)) {
             throw new IllegalArgumentException();
         }
         object.getAny().add(value);
     }
 }
