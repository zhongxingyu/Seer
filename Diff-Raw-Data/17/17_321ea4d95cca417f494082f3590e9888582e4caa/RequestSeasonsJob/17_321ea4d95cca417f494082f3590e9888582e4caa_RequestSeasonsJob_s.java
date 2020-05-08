 package fr.cg95.cvq.service.request.job;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestSeason;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.RequestType;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.request.IRequestSearchService;
 import fr.cg95.cvq.service.request.IRequestTypeService;
 import fr.cg95.cvq.service.request.IRequestWorkflowService;
 import fr.cg95.cvq.util.Critere;
 
 /**
  * A job that automatically archives notified requests which are associated
  * with a finished season.
  * 
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public class RequestSeasonsJob {
 
     private static Logger logger = Logger.getLogger(RequestSeasonsJob.class);
     
     private ILocalAuthorityRegistry localAuthorityRegistry;
     private IRequestWorkflowService requestWorkflowService;
     private IRequestTypeService requestTypeService;
     private IRequestSearchService requestSearchService;
 
     public void launchJob() {
         localAuthorityRegistry.browseAndCallback(this, "checkRequestsSeasons", null);
     }
 
     public void checkRequestsSeasons()
         throws CvqException {
     
         List<RequestType> requestTypes = requestTypeService.getAllRequestTypes();
         for (RequestType requestType : requestTypes) {
             if (requestType.getSeasons() == null || requestType.getSeasons().isEmpty()) {
                 logger.debug("checkRequestsSeasons() no seasons defined for request type " 
                         + requestType.getLabel());
                 continue;
             }
                 
             logger.debug("checkRequestsSeasons() looking at seasons for request type " 
                     + requestType.getLabel());
             Set<RequestSeason> requestSeasons = requestType.getSeasons();
             for (RequestSeason requestSeason : requestSeasons) {
                 logger.debug("checkRequestsSeasons() looking at season " 
                         + requestSeason.getLabel());
                 if (requestSeason.getEffectEnd().isBeforeNow()) {
                     Set<Critere> criterias = new HashSet<Critere>(2);
                     Critere stateCriteria =
                         new Critere(Request.SEARCH_BY_STATE,
                             RequestState.VALIDATED, Critere.EQUALS);
                     criterias.add(stateCriteria);
                     criterias.add(new Critere(Request.SEARCH_BY_SEASON_ID,
                         requestSeason.getId(), Critere.EQUALS));
                     List<Request> requests = 
                         requestSearchService.get(criterias, null, null, 0, 0, false);
                     for (Request request : requests) {
                         requestWorkflowService.updateRequestState(request.getId(),
                            RequestState.NOTIFIED, null);
                        requestWorkflowService.updateRequestState(request.getId(),
                            RequestState.CLOSED, null);
                    }
                    stateCriteria.setValue(RequestState.NOTIFIED);
                    requests = requestSearchService.get(criterias, null, null, 0, 0, false);
                    for (Request request : requests) {
                        requestWorkflowService.updateRequestState(request.getId(),
                             RequestState.CLOSED, null);
                     }
                 }
             }
         }
     }
 
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
 
     public void setRequestSearchService(IRequestSearchService requestSearchService) {
         this.requestSearchService = requestSearchService;
     }
 
     public void setRequestWorkflowService(IRequestWorkflowService requestWorkflowService) {
         this.requestWorkflowService = requestWorkflowService;
     }
 
     public void setRequestTypeService(IRequestTypeService requestTypeService) {
         this.requestTypeService = requestTypeService;
     }
 }
