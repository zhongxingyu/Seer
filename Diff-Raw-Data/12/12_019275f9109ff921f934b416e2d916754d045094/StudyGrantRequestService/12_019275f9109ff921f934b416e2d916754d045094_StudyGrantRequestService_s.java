 package fr.cg95.cvq.service.request.school.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.StringReader;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringUtils;
 import org.jaxen.dom.DOMXPath;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import com.google.gson.JsonObject;
 
 import fr.cg95.cvq.business.CapDematEvent;
 import fr.cg95.cvq.business.authority.LocalAuthorityResource;
 import fr.cg95.cvq.business.request.LocalReferentialEntry;
 import fr.cg95.cvq.business.request.LocalReferentialType;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestEvent;
 import fr.cg95.cvq.business.request.RequestEvent.EVENT_TYPE;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.external.RequestExternalAction;
 import fr.cg95.cvq.business.request.school.DistanceType;
 import fr.cg95.cvq.business.request.school.StudyGrantRequest;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.UserAction;
 import fr.cg95.cvq.business.users.UserEvent;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.dao.request.external.IRequestExternalActionDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.authority.ILocalAuthorityLifecycleAware;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.request.ILocalReferentialService;
 import fr.cg95.cvq.service.request.IRequestSearchService;
 import fr.cg95.cvq.service.request.condition.EqualityChecker;
 import fr.cg95.cvq.service.request.external.IRequestExternalActionService;
 import fr.cg95.cvq.service.request.impl.RequestService;
 import fr.cg95.cvq.service.users.IUserSearchService;
 import fr.cg95.cvq.util.Critere;
 import fr.cg95.cvq.util.JSONUtils;
 import fr.cg95.cvq.util.web.WS;
 
 /**
  * @author Jean-Sébastien Bour (jsb@zenexity.fr)
  *
  */
 public class StudyGrantRequestService extends RequestService implements ILocalAuthorityLifecycleAware {
 
     private static final String GOOGLE_MAPS = "Google Maps";
 
     private static final String SCHOOLS = "CurrentSchoolName";
 
     private static final String CITIES = "TaxHouseholdCity";
 
     private ILocalAuthorityRegistry localAuthorityRegistry;
 
     private ILocalReferentialService localReferentialService;
 
     private IRequestExternalActionService requestExternalActionService;
 
     private IRequestSearchService requestSearchService;
 
     private IUserSearchService userSearchService;
 
     private IRequestDAO requestDAO;
 
     private IRequestExternalActionDAO requestExternalActionDAO;
 
     private static Map<String, String> schoolAddresses = new HashMap<String, String>();
 
     @Override
     public void init() {
         StudyGrantRequest.conditions.put("abroadInternship", new EqualityChecker("true"));
         StudyGrantRequest.conditions.put("currentStudiesDiploma",
             new EqualityChecker("otherStudies"));
         StudyGrantRequest.conditions.put("taxHouseholdCity", new EqualityChecker("573"));
         StudyGrantRequest.conditions.put("currentSchoolName", new EqualityChecker("autre"));
         StudyGrantRequest.conditions.put("isSubjectAccountHolder", new EqualityChecker("true"));
     }
 
     public boolean accept(Request request) {
         return request instanceof StudyGrantRequest;
     }
 
     public Request getSkeletonRequest() {
         return new StudyGrantRequest();
     }
 
     @Override
     public void addLocalAuthority(String localAuthorityName) {
         try {
             LocalReferentialType ref = localReferentialService.getLocalReferentialType(label, SCHOOLS);
             for (LocalReferentialEntry school : ref.getEntries()) {
                 localReferentialService.removeLocalReferentialEntry(label, SCHOOLS, school.getKey());
             }
             File file = new File(localAuthorityRegistry.getAssetsBase()
                 + SecurityContext.getCurrentSite().getName().toLowerCase() + "/"
                 + LocalAuthorityResource.Type.LOCAL_REFERENTIAL.getFolder() + "/"
                 + SCHOOLS + ".csv");
             if (file.exists()) {
                 Map<String, String> parentKeys = new HashMap<String, String>();
                 try {
                     for (String[] line : new CSVReader(new StringReader(
                         localAuthorityRegistry.getFileContent(file))).readAll()) {
                         if (line.length == 3) {
                             String parentKey = null;
                             if (line[0] != null) {
                                 parentKey = parentKeys.get(line[0]);
                                 if (parentKey == null) {
                                     parentKey = localReferentialService.addLocalReferentialEntry(
                                         label, SCHOOLS, null, line[0], null);
                                     parentKeys.put(line[0], parentKey);
                                 }
                             }
                             schoolAddresses.put(
                                 localReferentialService.addLocalReferentialEntry(
                                     label, SCHOOLS, parentKey, line[1], null),
                                 StringUtils.defaultString(line[2]).replaceAll(",", ""));
                         }
                     }
                     localReferentialService.addLocalReferentialEntry(label, SCHOOLS, null, "autre", null);
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }
             ref = localReferentialService.getLocalReferentialType(label, CITIES);
             for (LocalReferentialEntry city : ref.getEntries()) {
                 localReferentialService.removeLocalReferentialEntry(label, CITIES, city.getKey());
             }
             file = new File(localAuthorityRegistry.getAssetsBase()
                 + SecurityContext.getCurrentSite().getName().toLowerCase() + "/"
                 + LocalAuthorityResource.Type.LOCAL_REFERENTIAL.getFolder() + "/"
                 + CITIES + ".csv");
             if (file.exists()) {
                 try {
                     for (String[] line : new CSVReader(new StringReader(
                         localAuthorityRegistry.getFileContent(file))).readAll()) {
                         if (line.length == 2) {
                             localReferentialService.addLocalReferentialEntry(
                                 label, CITIES, null, line[0], line[1], null);
                         }
                     }
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }
         } catch (CvqException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void removeLocalAuthority(String localAuthorityName) {}
 
     @Override
     public void onRequestModified(Request request)
         throws CvqException {
         computeDistance((StudyGrantRequest)request);
     }
 
     @Override
     public void onRequestValidated(Request request) throws CvqException {
         StudyGrantRequest sgr = (StudyGrantRequest) request;
         Individual subject = (Individual) genericDAO.findById(Individual.class, sgr.getSubjectId());
         subject.setBirthDate(sgr.getSubjectBirthDate());
     }
 
     @Override
     public void onApplicationEvent(CapDematEvent e) {
         if (e instanceof RequestEvent) {
             RequestEvent event = (RequestEvent)e;
             if (EVENT_TYPE.REQUEST_CLONED.equals(event.getEvent()) && accept(event.getRequest())) {
                 StudyGrantRequest request = (StudyGrantRequest) event.getRequest();
                 request.setAccountHolderEdemandeId(null);
                 request.setEdemandeId(null);
             }
         } else if (e instanceof UserEvent) {
             UserAction action = ((UserEvent)e).getAction();
             JsonObject payload = JSONUtils.deserialize(action.getData());
             if (UserAction.Type.MODIFICATION.equals(action.getType()) && payload.has("atom")
                 && "address".equals(payload.get("atom").getAsJsonObject().get("name").getAsString())) {
                 Set<Critere> criterias = new HashSet<Critere>();
                 criterias.add(new Critere(Request.SEARCH_BY_SUBJECT_ID, action.getTargetId(), Critere.EQUALS));
                 criterias.add(new Critere(Request.SEARCH_BY_REQUEST_TYPE_LABEL, label, Critere.EQUALS));
                 Set<RequestState> states = new HashSet<RequestState>();
                 states.add(RequestState.COMPLETE);
                 states.add(RequestState.VALIDATED);
                 states.add(RequestState.NOTIFIED);
                 criterias.add(new Critere(Request.SEARCH_BY_STATE, states, Critere.IN));
                 try {
                     if (requestSearchService.getCount(criterias) > 0)
                         throw new CvqModelException("sgr.error.addressChangeForbidden");
                 } catch (CvqException ex) {
                     throw new RuntimeException(ex);
                 }
                 criterias.clear();
                 criterias.add(new Critere(Request.SEARCH_BY_SUBJECT_ID, action.getTargetId(), Critere.EQUALS));
                 criterias.add(new Critere(Request.SEARCH_BY_REQUEST_TYPE_LABEL, label, Critere.EQUALS));
                 states.clear();
                 states.add(RequestState.DRAFT);
                 states.add(RequestState.PENDING);
                 states.add(RequestState.UNCOMPLETE);
                 criterias.add(new Critere(Request.SEARCH_BY_STATE, states, Critere.IN));
                 List<Request> requests;
                 try {
                     requests = requestSearchService.get(criterias, null, null, 0, 0, true);
                 } catch (CvqException ex) {
                     throw new RuntimeException(ex);
                 }
                 for (Request request : requests) {
                     computeDistance((StudyGrantRequest)request);
                 }
             }
         }
     }
 
     private void computeDistance(StudyGrantRequest request) {
         String oldSchool = null;
         String oldUser = null;
         Set<Critere> criterias = new HashSet<Critere>();
         criterias.add(new Critere(RequestExternalAction.SEARCH_BY_KEY, request.getId().toString(), Critere.EQUALS));
         criterias.add(new Critere(RequestExternalAction.SEARCH_BY_NAME, GOOGLE_MAPS, Critere.EQUALS));
        criterias.add(new Critere(RequestExternalAction.SEARCH_BY_STATUS, RequestExternalAction.Status.SENT, Critere.EQUALS));
         List<RequestExternalAction> lastCheck =
             requestExternalActionDAO.get(criterias, RequestExternalAction.SEARCH_BY_DATE, "desc", 1, 0, true);
         if (lastCheck.size() > 0) {
             oldSchool = (String)lastCheck.get(0).getComplementaryData().get("schoolAddress");
             oldUser = (String)lastCheck.get(0).getComplementaryData().get("userAddress");
         }
         String currentSchool = null;
         if (request.getCurrentSchoolName().size() > 0
             && !StudyGrantRequest.conditions.get("currentSchoolName").test(
                 request.getCurrentSchoolName().get(0).getName())) {
             currentSchool = schoolAddresses.get(request.getCurrentSchoolName().get(0).getName());
         } else if (request.getCurrentSchoolAddress() != null) {
             currentSchool = request.getCurrentSchoolAddress().format();
         }
         String currentUser = null;
         try {
             Individual subject = userSearchService.getById(request.getSubjectId());
             if (subject != null && subject.getAddress() != null)
                 currentUser = subject.getAddress().format();
         } catch (CvqObjectNotFoundException e) {
             throw new RuntimeException(e);
         }
         DistanceType distance = request.getDistance();
         if (request.getAbroadInternship()) {
             distance = DistanceType.MORE_THAN250KMS_AND_ABROAD;
             requestExternalActionService.addTrace(new RequestExternalAction(new Date(),
                 request.getId(), "capdemat", "Stage à l'étranger",
                 GOOGLE_MAPS, RequestExternalAction.Status.SENT));
         } else if (currentSchool == null || currentUser == null) {
             distance = DistanceType.UNDETERMINED;
         } else if (!currentSchool.equals(oldSchool) || !currentUser.equals(oldUser)) {
             try {
                 int km = Integer.valueOf(
                     new DOMXPath("/DistanceMatrixResponse//distance/value").stringValueOf(
                         WS.url("https://maps.googleapis.com/maps/api/distancematrix/xml")
                             .setParameter("origins", currentUser)
                             .setParameter("destinations", currentSchool)
                             .setParameter("sensor", Boolean.FALSE.toString())
                             .get().getXml())) / 1000;
                Map<String, Serializable> data = new HashMap<String, Serializable>();
                data.put("schoolAddress", currentSchool);
                data.put("userAddress", currentUser);
                 requestExternalActionService.addTrace(new RequestExternalAction(new Date(),
                     request.getId(), "capdemat", "Distance : " + km + "km",
                     GOOGLE_MAPS, RequestExternalAction.Status.SENT, data));
                 if (km > 250) distance = DistanceType.MORE_THAN250KMS_AND_ABROAD;
                 else if (km >= 30) distance = DistanceType.BETWEEN30AND250KMS;
                 else distance = DistanceType.LESS_THAN30KMS;
             } catch (Exception e) {
                 requestExternalActionService.addTrace(new RequestExternalAction(new Date(),
                     request.getId(), "capdemat", null, GOOGLE_MAPS,
                    RequestExternalAction.Status.ERROR));
                 distance = DistanceType.UNDETERMINED;
             }
         }
         request.setDistance(distance);
         if (request.getCurrentSchoolAddress() != null)
             requestDAO.saveOrUpdate(request.getCurrentSchoolAddress());
         requestDAO.saveOrUpdate(request);
     }
 
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
 
     public void setLocalReferentialService(ILocalReferentialService localReferentialService) {
         this.localReferentialService = localReferentialService;
     }
 
     public void setRequestExternalActionService(
         IRequestExternalActionService requestExternalActionService) {
         this.requestExternalActionService = requestExternalActionService;
     }
 
     public void setRequestSearchService(IRequestSearchService requestSearchService) {
         this.requestSearchService = requestSearchService;
     }
 
     public void setUserSearchService(IUserSearchService userSearchService) {
         this.userSearchService = userSearchService;
     }
 
     public void setRequestDAO(IRequestDAO requestDAO) {
         this.requestDAO = requestDAO;
     }
 
     public void setRequestExternalActionDAO(IRequestExternalActionDAO requestExternalActionDAO) {
         this.requestExternalActionDAO = requestExternalActionDAO;
     }
 }
