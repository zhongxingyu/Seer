 package fr.cg95.cvq.external.endpoint;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlObject;
 import org.springframework.oxm.Marshaller;
 
 import fr.capwebct.capdemat.GetRequestsRequestDocument;
 import fr.capwebct.capdemat.GetRequestsResponseDocument;
 import fr.capwebct.capdemat.GetRequestsRequestDocument.GetRequestsRequest;
 import fr.capwebct.capdemat.GetRequestsResponseDocument.GetRequestsResponse;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.external.RequestExternalAction;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.request.IRequestExportService;
 import fr.cg95.cvq.service.request.IRequestSearchService;
 import fr.cg95.cvq.service.request.external.IRequestExternalActionService;
 import fr.cg95.cvq.service.request.external.IRequestExternalService;
 import fr.cg95.cvq.util.DateUtils;
 import fr.cg95.cvq.xml.common.RequestType;
 
 public class RequestServiceEndpoint extends SecuredServiceEndpoint {
 
     private static Logger logger = Logger.getLogger(RequestServiceEndpoint.class);
 
     private IRequestExportService requestExportService;
     private IRequestSearchService requestSearchService;
     private IRequestExternalService requestExternalService;
     private IRequestDAO requestDAO;
     private IRequestExternalActionService requestExternalActionService;
 
     private final String noPermissions = "Access denied! No permissions granted";
 
     @Override
     protected Object invokeInternal(Object request) throws Exception {
 
         GetRequestsResponseDocument responseDocument =
             GetRequestsResponseDocument.Factory.newInstance();
         GetRequestsResponse response = responseDocument.addNewGetRequestsResponse();
 
         Collection<String> authorizedRequestTypesLabels =
             requestExternalService.getRequestTypesForExternalService(SecurityContext.getCurrentExternalService());
 
         GetRequestsRequest typedRequest =
             ((GetRequestsRequestDocument)request).getGetRequestsRequest();
 
         // Check external service permissions wrt configured request types labels
         // and add request types labels filter if authorized
         List<String> selectedRequestTypesLabels = new ArrayList<String>();
         if (typedRequest.getRequestTypeLabel() != null) {
             if (authorizedRequestTypesLabels.contains(typedRequest.getRequestTypeLabel())) {
                 selectedRequestTypesLabels.add(typedRequest.getRequestTypeLabel());
             } else {
                 response.setError(noPermissions);
                 return response;
             }
         } else {
             if (!authorizedRequestTypesLabels.isEmpty()) {
                 selectedRequestTypesLabels.addAll(authorizedRequestTypesLabels);
             } else {
                 response.setError(noPermissions);
                 return response;
             }
         }
 
         // if a request id is specified, return the request whatever states and dates are set to
         if (typedRequest.getId() != 0) {
             Request askedRequest = requestSearchService.getById(typedRequest.getId(), true);
             if (askedRequest != null
                     && authorizedRequestTypesLabels.contains(askedRequest.getRequestType().getLabel())) {
                 List<Request> requests = new ArrayList<Request>();
                 requests.add(askedRequest);
 
                 response.setRequestArray(prepareRequestsForResponse(requests));
             } else {
                 response.setError(noPermissions);
             }
 
             return response;
         }
 
         RequestState requestedState =
             typedRequest.getState() != null ? RequestState.forString(typedRequest.getState().toString()) : RequestState.VALIDATED;
         Date requestedDateFrom = null;
         if (typedRequest.getDateFrom() != null)
             requestedDateFrom = typedRequest.getDateFrom().getTime();
         else
             requestedDateFrom = DateUtils.getShiftedDate(Calendar.DAY_OF_MONTH, -1);
         Date requestedDateTo = null;
         if (typedRequest.getDateTo() != null)
             requestedDateTo = typedRequest.getDateTo().getTime();
         else
             requestedDateTo = DateUtils.getShiftedDate(Calendar.DAY_OF_MONTH, 1);
 
         try {
             List<Request> requests =
                 requestDAO.listRequestsToExport(requestedState.toString(), requestedDateFrom,
                         requestedDateTo, selectedRequestTypesLabels);
             List<Request> selectedRequests = new ArrayList<Request>();
             for (Request eligibleRequest : requests) {
                 if (!requestExternalActionService.isAcknowledged(eligibleRequest.getId(), 
                         SecurityContext.getCurrentExternalService()))
                     selectedRequests.add(eligibleRequest);
             }
 
             response.setRequestArray(prepareRequestsForResponse(selectedRequests));
 
         } catch (Exception e) {
             e.printStackTrace();
             response.setError(e.getMessage());
         }
 
         return response;
     }
 
     private RequestType[] prepareRequestsForResponse(List<Request> requests) throws Exception {
 
         Set<RequestType> resultArray = new HashSet<RequestType>();
         for (Request r : requests) {
 
             if (r.getState().equals(RequestState.ARCHIVED))
                 continue;
 
             RequestType rt = null;
             // TODO : port this properly in 4.2
             XmlObject xmlObject = requestExportService.fillRequestXml(r);
             try {
                 rt =  (fr.cg95.cvq.xml.common.RequestType) xmlObject.getClass().getMethod("get" + xmlObject.getClass().getSimpleName().replace("DocumentImpl", "")).invoke(xmlObject);
             } catch (Exception e) {
                 logger.error("prepareRequestsForResponse() Unexpected exception while converting to XML "
                         + e.getMessage());
             }
             resultArray.add(rt);
 
             RequestExternalAction trace = new RequestExternalAction();
             trace.setKeyOwner("capdemat");
             trace.setKey(r.getId());
             trace.setStatus(RequestExternalAction.Status.SENT);
 
             requestExternalActionService.addTrace(trace);
         }
 
         logger.debug("prepareRequestsForResponse() number of returned requests " + requests.size());
         return resultArray.toArray(new RequestType[0]);
     }
 
     public RequestServiceEndpoint(Marshaller marshaller) {
         super(marshaller);
     }
 
     public void setRequestExternalService(IRequestExternalService requestExternalService) {
         this.requestExternalService = requestExternalService;
     }
 
     public void setRequestSearchService(IRequestSearchService requestSearchService) {
         this.requestSearchService = requestSearchService;
     }
 
     public void setRequestExportService(IRequestExportService requestExportService) {
         this.requestExportService = requestExportService;
     }
 
     public void setRequestDAO(IRequestDAO requestDAO) {
         this.requestDAO = requestDAO;
     }
 
     public void setRequestExternalActionService(
             IRequestExternalActionService requestExternalActionService) {
         this.requestExternalActionService = requestExternalActionService;
     }
 
 }
