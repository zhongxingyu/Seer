 package fr.cg95.cvq.service.request.external;
 
 import java.util.List;
 import java.util.Set;
 
 import fr.cg95.cvq.business.request.external.RequestExternalAction;
 import fr.cg95.cvq.service.request.annotation.IsRequest;
 import fr.cg95.cvq.util.Critere;
 
 public interface IRequestExternalActionService {
 
     Long addTrace(@IsRequest RequestExternalAction trace);
 
    List<RequestExternalAction> getTraces(Set<Critere> criteriaSet, String sort,
         String dir, int count, int offset);
 
    Long getTracesCount(Set<Critere> criteriaSet);
 
    List<RequestExternalAction> getLastTraces(Set<Critere> criteriaSet, String sort,
             String dir, int count, int offset);
 
    Long getLastTracesCount(Set<Critere> criteriaSet);
 
     List<Long> getRequestsWithoutExternalAction(Long requestTypeId, String externalServiceLabel);
 
     List<String> getKeys(Set<Critere> criterias);
 }
