 package fr.cg95.cvq.exporter.service.endpoint;
 
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.springframework.oxm.Marshaller;
 
 import fr.capwebct.capdemat.GetRequestsRequestDocument;
 import fr.capwebct.capdemat.GetRequestsResponseDocument;
 import fr.capwebct.capdemat.GetRequestsRequestDocument.GetRequestsRequest;
 import fr.capwebct.capdemat.GetRequestsResponseDocument.GetRequestsResponse;
 import fr.cg95.cvq.business.external.ExternalServiceTrace;
 import fr.cg95.cvq.business.external.TraceStatusEnum;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestAction;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.external.IExternalService;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.util.Critere;
 import fr.cg95.cvq.util.DateUtils;
 import fr.cg95.cvq.xml.common.RequestType;
 
 public class RequestServiceEndpoint extends SecuredServiceEndpoint {
     
     private ILocalAuthorityRegistry localAuthorityRegistry;
     private IRequestService defaultRequestService;
     private IExternalService externalService;
     
     private final String noPermissions = "Access denied! No permissions granted"; 
     
     @Override
     protected Object invokeInternal(Object request) throws Exception {
         
         GetRequestsResponseDocument responseDocument = 
             GetRequestsResponseDocument.Factory.newInstance();
         GetRequestsResponse response = responseDocument.addNewGetRequestsResponse();
         
         Collection<String> requestTypesLabels =
             externalService.getRequestTypesForExternalService(SecurityContext.getCurrentExternalService());
         
         GetRequestsRequest typedRequest =
             ((GetRequestsRequestDocument)request).getGetRequestsRequest();
         Set<Critere> criterias = new HashSet<Critere>();
 
         if (typedRequest.getRequestTypeLabel() != null) {
             if (requestTypesLabels.contains(typedRequest.getRequestTypeLabel())) {
                criterias.add(new Critere(Request.SEARCH_BY_REQUEST_TYPE_LABEL,
                     typedRequest.getRequestTypeLabel(), Critere.EQUALS));
             } else {
                 response.setError(noPermissions);
                 return response;
             }
         } else {
             if (requestTypesLabels != null && !requestTypesLabels.isEmpty()) {
                criterias.add(new Critere(Request.SEARCH_BY_REQUEST_TYPE_LABEL,
                     requestTypesLabels, Critere.IN));
             } else {
                 response.setError(noPermissions);
                 return response;
             }
         }
         
         Critere criteria = new Critere();
         criteria.setAttribut("date");
         criteria.setComparatif(Critere.LTE);
         if (typedRequest.getDateTo() == null) {
             criteria
                 .setValue(DateUtils.getShiftedDate(Calendar.DAY_OF_MONTH, 1));
         } else {
             criteria.setValue(
                 DateUtils.getShiftedDate(typedRequest.getDateTo().getTime(),
                     Calendar.DAY_OF_MONTH, 1));
         }
         criterias.add(criteria);
         if (typedRequest.getId() != 0)
             criterias.add(new Critere(Request.SEARCH_BY_REQUEST_ID,
                 typedRequest.getId(), Critere.EQUALS));
         try {
             List<Request> requests =
                 defaultRequestService.get(criterias, null, null, 0, 0);
             for (Iterator<Request> it = requests.iterator(); it.hasNext();) {
                 Request r = it.next();
                 boolean keepIt = true;
                 if (typedRequest.getState() != null) {
                     keepIt = false;
                     for (RequestAction action : r.getActions()) {
                         if (action.getResultingState()
                             .equals(typedRequest.getState())) {
                             keepIt = true;
                             break;
                         }
                     }
                 }
                 if (typedRequest.getDateFrom() != null) {
                     keepIt = false;
                     for (RequestAction action : r.getActions()) {
                         if (action.getDate()
                             .after(typedRequest.getDateFrom().getTime())) {
                             keepIt = true;
                             break;
                         }
                     }
                 }
                 // do not send again requests that have already been acknowledged ...
                 // ... except if we are invoked with an explicit request id !
                 if (typedRequest.getId() == 0) {
                     keepIt = false;
                     Set<Critere> criteriaSet = new HashSet<Critere>(2);
                     criteriaSet.add(new Critere(
                         ExternalServiceTrace.SEARCH_BY_STATUS,
                         TraceStatusEnum.ACKNOWLEDGED, Critere.EQUALS));
                     criteriaSet.add(
                         new Critere(ExternalServiceTrace.SEARCH_BY_KEY,
                             String.valueOf(r.getId()), Critere.EQUALS));
                     if (externalService.getTraces(criteriaSet, null, null)
                         .isEmpty()) {
                         keepIt = true;
                     }
                 }
                 if (!keepIt) it.remove();
             }
             Set<RequestType> resultArray = new HashSet<RequestType>();
             for (Request r : requests) {
                 RequestType rt = null;
                 
                 if (localAuthorityRegistry.getRequestXmlResource(r.getId())
                     .exists()) {
                     rt = RequestType.Factory.parse(
                         localAuthorityRegistry.getRequestXmlResource(r.getId()));
                 } else {
                     rt = r.modelToXmlRequest();
                 }
                 resultArray.add(rt);
                 
                 ExternalServiceTrace trace = new ExternalServiceTrace();
                 trace.setKeyOwner("capdemat");
                 trace.setKey(String.valueOf(r.getId()));
                 trace.setStatus(TraceStatusEnum.SENT);
                 
                 externalService.addTrace(trace);
             }
             response.setRequestArray(resultArray.toArray(new RequestType[0]));
             
         } catch (CvqException e) {
             response.setError(e.getMessage());
         }
 
         return response;
     }
 
     public RequestServiceEndpoint(Marshaller marshaller) {
         super(marshaller);
     }
 
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
     
     public void setDefaultRequestService(IRequestService defaultRequestService) {
         this.defaultRequestService = defaultRequestService;
     }
 
     public void setExternalService(IExternalService externalService) {
         this.externalService = externalService;
     }
 }
