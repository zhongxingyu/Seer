 package fr.cg95.cvq.service.request.impl;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.sf.oval.ConstraintViolation;
 import net.sf.oval.Validator;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.ApplicationListener;
 
 import com.google.gson.JsonObject;
 
 import fr.cg95.cvq.business.authority.LocalAuthorityResource;
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.document.DocumentState;
 import fr.cg95.cvq.business.request.DataState;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestAction;
 import fr.cg95.cvq.business.request.RequestActionType;
 import fr.cg95.cvq.business.request.RequestDocument;
 import fr.cg95.cvq.business.request.RequestEvent;
 import fr.cg95.cvq.business.request.RequestNote;
 import fr.cg95.cvq.business.request.RequestSeason;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.RequestStep;
 import fr.cg95.cvq.business.request.RequestType;
 import fr.cg95.cvq.business.request.RequestEvent.COMP_DATA;
 import fr.cg95.cvq.business.request.RequestEvent.EVENT_TYPE;
 import fr.cg95.cvq.business.request.workflow.RequestWorkflow;
 import fr.cg95.cvq.business.request.workflow.event.IWorkflowPostAction;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowArchivedEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowCancelledEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowClosedEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowCompleteEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowExtInProgressEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowInProgressEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowNotifiedEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowPendingEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowRectifiedEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowRejectedEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowUncompleteEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowValidatedEvent;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.UserAction;
 import fr.cg95.cvq.business.users.UserState;
 import fr.cg95.cvq.business.users.UserEvent;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.exception.CvqValidationException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.document.IDocumentService;
 import fr.cg95.cvq.service.request.IRequestActionService;
 import fr.cg95.cvq.service.request.IRequestDocumentService;
 import fr.cg95.cvq.service.request.IRequestPdfService;
 import fr.cg95.cvq.service.request.IRequestSearchService;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.request.IRequestServiceRegistry;
 import fr.cg95.cvq.service.request.IRequestTypeService;
 import fr.cg95.cvq.service.request.IRequestWorkflowService;
 import fr.cg95.cvq.service.request.external.IRequestExternalService;
 import fr.cg95.cvq.service.users.IUserSearchService;
 import fr.cg95.cvq.service.users.IUserWorkflowService;
 import fr.cg95.cvq.util.Critere;
 import fr.cg95.cvq.util.JSONUtils;
 import fr.cg95.cvq.util.ValidationUtils;
 
 /**
  * This services handles workflow tasks for requests. It is responsible for :
  * <ul>
  *  <li>Checking requested states changes are authorized</li>
  *  <li>Updating requests state and workflow information</li>
  *  <li>Creating and managing workflow action traces</li>
  * </ul>
  *
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public class RequestWorkflowService implements IRequestWorkflowService, ApplicationListener<UserEvent>,
     ApplicationContextAware {
 
     private static Logger logger = Logger.getLogger(RequestWorkflowService.class);
 
     private IRequestPdfService requestPdfService;
     private IDocumentService documentService;
     private IUserSearchService userSearchService;
     private IUserWorkflowService userWorkflowService;
     private IRequestServiceRegistry requestServiceRegistry;
     private IRequestActionService requestActionService;
     private IRequestExternalService requestExternalService;
     private IRequestTypeService requestTypeService;
     private IRequestDocumentService requestDocumentService;
     private IRequestSearchService requestSearchService;
 
     private IRequestDAO requestDAO;
 
     private ApplicationContext applicationContext;
 
     private ILocalAuthorityRegistry localAuthorityRegistry;
 
     private Map<String, RequestWorkflow> workflows = new HashMap<String, RequestWorkflow>();
 
     private RequestWorkflow getWorkflow() {
         String name = SecurityContext.getCurrentSite().getName();
         RequestWorkflow workflow = workflows.get(name);
         if (workflow == null) {
             File file = localAuthorityRegistry.getLocalAuthorityResourceFileForLocalAuthority(name,
                 LocalAuthorityResource.Type.XML, "requestWorkflow", false);
             if (file.exists()) {
                 workflow = RequestWorkflow.load(file);
                 workflows.put(name, workflow);
             } else {
                 workflow = workflows.get("default");
                 if (workflow == null) {
                     file = localAuthorityRegistry.getReferentialResource(
                         LocalAuthorityResource.Type.XML, "requestWorkflow");
                     workflow = RequestWorkflow.load(file);
                     workflows.put("default", workflow);
                 }
             }
         }
         return workflow;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public Long create(Request request, String note) throws CvqException {
         performBusinessChecks(request, SecurityContext.getCurrentEcitizen());
         IRequestService requestService = requestServiceRegistry.getRequestService(request);
         requestService.onRequestIssued(request);
         return finalizeAndPersist(request, note);
     }
 
     @Deprecated
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public Long create(Request request, Adult requester, String note)
         throws CvqException {
         HomeFolder homeFolder = performBusinessChecks(request, requester);
         IRequestService requestService = requestServiceRegistry.getRequestService(request);
         requestService.onRequestIssued(request);
         return finalizeAndPersist(request, homeFolder, note);
     }
 
     @Deprecated
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public Long create(Request request, Adult requester, List<Document> documents, String note)
         throws CvqException {
 
         HomeFolder homeFolder = performBusinessChecks(request, requester);
 
         HibernateUtil.getSession().flush();
         SecurityContext.setCurrentEcitizen(
                 userSearchService.getHomeFolderResponsible(homeFolder.getId()));
 
         IRequestService requestService = requestServiceRegistry.getRequestService(request);
         requestService.onRequestIssued(request);
         requestDocumentService.addDocuments(request, documents);
         return finalizeAndPersist(request, homeFolder, note);
     }
 
     private HomeFolder performBusinessChecks(Request request, Adult requester)
         throws CvqException {
 
         HomeFolder homeFolder = createOrSynchronizeHomeFolder(request, requester);
 
         if (!RequestState.DRAFT.equals(request.getState())) {
             IRequestService requestService = 
                 requestServiceRegistry.getRequestService(request);
             RequestType requestType = 
                 requestTypeService.getRequestTypeByLabel(requestService.getLabel());
             request.setRequestType(requestType);
         }
 
         if (request.getSubjectId() != null) {
             Individual individual = userSearchService.getById(request.getSubjectId());
             request.setSubjectId(individual.getId());
             request.setSubjectLastName(individual.getLastName());
             request.setSubjectFirstName(individual.getFirstName());
         }
 
         return homeFolder;
     }
 
     /**
      * Create or synchronize informations from home folder :
      * <ul>
      *   <li>Create an home folder containing the given requester if requester id is not provided 
      *         within request object (in this case, requester parameter <strong>must</strong> must
      *         be provided)</li>
      *   <li>Load and set home folder and requester informations if requester id is provided
      *         within request object (in this case, requester parameter is not used)</li>
      * </ul>
      * 
      * @return the newly created home folder or null if home folder already existed 
      */
     @Deprecated
     private HomeFolder createOrSynchronizeHomeFolder(Request request, Adult requester)
         throws CvqException, CvqModelException {
 
         // in case requester id is not filled, feed it with currently logged in ecitizen
         if (request.getRequesterId() == null && SecurityContext.getCurrentEcitizen() != null)
             request.setRequesterId(SecurityContext.getCurrentEcitizen().getId());
 
         if (request.getRequesterId() == null) {
             IRequestService requestService = requestServiceRegistry.getRequestService(request);
             if (requestService.supportUnregisteredCreation()) {
                 logger.debug("create() Gonna create implicit home folder");
                 HomeFolder homeFolder = userWorkflowService.create(requester, true);
                 request.setHomeFolderId(homeFolder.getId());
                 request.setRequesterId(requester.getId());
                 request.setRequesterLastName(requester.getLastName());
                 request.setRequesterFirstName(requester.getFirstName());
 
                 SecurityContext.setCurrentEcitizen(requester);
 
                 return homeFolder;
             } else {
                 logger.error("create() refusing creation by unregistered user");
                 throw new CvqModelException("request.error.unregisteredCreationUnauthorized");
             }
         } else {
             logger.debug("create() Adult already exists, re-synchronizing it with DB");
             // load requester in order to have names information
             Individual somebody = userSearchService.getById(request.getRequesterId());
             if (somebody instanceof Child)
                 throw new CvqModelException("request.error.requesterMustBeAdult");
             Adult adult = (Adult) somebody;
             request.setRequesterLastName(adult.getLastName());
             request.setRequesterFirstName(adult.getFirstName());
             request.setHomeFolderId(adult.getHomeFolder().getId());
         }
 
         return null;
     }
 
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN, ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void checkSubjectPolicy(final Long subjectId, Long homeFolderId, final String policy,
             final RequestType requestType) 
         throws CvqModelException {
 
         // first, check general subject policy
         if (!policy.equals(IRequestWorkflowService.SUBJECT_POLICY_NONE)) {
             if (subjectId == null)
                 throw new CvqModelException("model.request.subject_is_required");
             Individual subject = userSearchService.getById(subjectId);
             if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_INDIVIDUAL)) {
                 if (!(subject instanceof Individual)) {
                     throw new CvqModelException("model.request.wrong_subject_type");
                 }
             } else if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_ADULT)) {
                 if (!(subject instanceof Adult)) {
                     throw new CvqModelException("model.request.wrong_subject_type");
                 }
             } else if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_CHILD)) {
                 if (!(subject instanceof Child)) {
                     throw new CvqModelException("model.request.wrong_subject_type");
                 }
             }
         } else {
             if (subjectId != null)
                 throw new CvqModelException("model.request.subject_not_supported");
         }
 
         // then check that request's subject is allowed to issue this request
         // ie that it is authorized to issue it (no current one, an open season, ...)
         if (!policy.equals(IRequestWorkflowService.SUBJECT_POLICY_NONE)) {
             Individual individual = userSearchService.getById(subjectId);
             boolean isAuthorized = false;
             Map<Long, Set<RequestSeason>> authorizedSubjectsMap = 
                 getAuthorizedSubjects(requestType, homeFolderId);
             if (authorizedSubjectsMap != null) {
                 Set<Long> authorizedSubjects = authorizedSubjectsMap.keySet();
                 for (Long authorizedSubjectId : authorizedSubjects) {
                     if (authorizedSubjectId.equals(individual.getId())) {
                         isAuthorized = true;
                         break;
                     }
                 }
             }
             if (!isAuthorized)
                 throw new CvqModelException("request.error.subjectNotAuthorized");
         }
     }
 
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN, ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void checkRequestTypePolicy(RequestType requestType, HomeFolder homeFolder)
         throws CvqException {
         if (!requestType.getActive()) {
             throw new CvqModelException("requestType.message.inactive");
         }
         if (!requestTypeService.isRegistrationOpen(requestType.getId())) {
             throw new CvqModelException("requestType.message.registrationClosed");
         }
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public boolean validateSeason(RequestType requestType, RequestSeason requestSeason)
         throws CvqModelException {
         if (requestTypeService.isOfRegistrationKind(requestType.getId())) {
             Set<RequestSeason> seasons = requestTypeService.getOpenSeasons(requestType);
             return (requestSeason == null && seasons.isEmpty())
                 || (requestSeason != null && seasons.contains(requestSeason));
         }
         return requestSeason == null;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void validate(Request request, List<String> steps)
         throws ClassNotFoundException, IllegalAccessException, CvqValidationException,
             InvocationTargetException, NoSuchMethodException {
         Validator validator = new Validator();
         validator.disableAllProfiles();
         if (steps == null) {
             validator.enableAllProfiles();
             validator.disableProfile("administration");
             for (Map<String, Object> stepState : request.getStepStates().values()) {
                 stepState.put("state", "complete");
                 stepState.put("errorMsg", null);
                 stepState.put("invalidFields", new ArrayList<String>());
             }
         } else {
             validator.enableProfile("default");
             Map<String, Object> stepState;
             for (String step : steps) {
                 validator.enableProfile(step);
                 stepState = request.getStepStates().get(step);
                 stepState.put("state", "complete");
                 stepState.put("errorMsg", null);
                 stepState.put("invalidFields", new ArrayList<String>());
             }
         }
         Map<String, List<String>> invalidFields = new LinkedHashMap<String, List<String>>();
         for (ConstraintViolation violation : validator.validate(request)) {
             ValidationUtils.collectInvalidFields(violation, invalidFields, "", "");
         }
         if (invalidFields.get("") != null) {
             Iterator<String> iterator = invalidFields.get("").iterator();
             while (iterator.hasNext()) {
                 String invalidField = iterator.next();
                 if ("subjectId".equals(invalidField)) {
                     String firstStep = request.getStepStates().keySet().iterator().next();
                     if (steps == null || steps.contains(firstStep)) {
                         if (invalidFields.get(firstStep) == null)
                             invalidFields.put(firstStep, new ArrayList<String>(1));
                         invalidFields.get(firstStep).add(invalidField);
                     }
                     iterator.remove();
                 }
             }
             if (invalidFields.get("").isEmpty()) {
                 invalidFields.remove("");
             }
         }
         for (Map.Entry<String, Map<String, Object>> stepState :
             request.getStepStates().entrySet()) {
             List<String> fields = invalidFields.get(stepState.getKey());
             if (fields != null) {
                 stepState.getValue().put("state", "invalid");
                 stepState.getValue().put("invalidFields", fields);
             }
             if (!"validation".equals(stepState.getKey())
                 && ("invalid".equals(stepState.getValue().get("state"))
                     || ("uncomplete".equals(stepState.getValue().get("state"))
                         && (Boolean)stepState.getValue().get("required")))) {
                 request.getStepStates().get("validation").put("state", "unavailable");
             }
         }
         if (!invalidFields.isEmpty()) {
             throw new CvqValidationException("request.error.dataValidation");
         }
     }
 
     /**
      * Get the list of eligible subjects for the current request service. Does
      * not make any control on already existing requests.
      */
     private Set<Long> getEligibleSubjects(final Long homeFolderId, final String policy) {
 
         if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_NONE)) {
             Set<Long> result = new HashSet<Long>();
             result.add(homeFolderId);
             return result;
         } else {
             List<Individual> individualsReference = userSearchService.getIndividuals(homeFolderId);
             Set<Long> result = new HashSet<Long>();
             for (Individual individual : individualsReference) {
                 if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_INDIVIDUAL)) {
                     result.add(individual.getId());
                 } else if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_ADULT)) {
                     if (individual instanceof Adult)
                         result.add(individual.getId());
                 } else if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_CHILD)) {
                     if (individual instanceof Child)
                         result.add(individual.getId());
                 }
             }
 
             return result;
         }
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public Map<Long, Set<RequestSeason>> getAuthorizedSubjects(final RequestType requestType,
             final Long homeFolderId)
             throws CvqModelException {
 
         logger.debug("getAuthorizedSubjects() searching authorized subjects for : "
                 + requestType.getLabel());
 
         IRequestService requestService = 
             requestServiceRegistry.getRequestService(requestType.getLabel());
         if (requestService.isOfRegistrationKind() && !requestType.getSeasons().isEmpty()) {
             Set<RequestSeason> openSeasons = requestTypeService.getOpenSeasons(requestType);
             // no open seasons, no registration is possible
             if (openSeasons.isEmpty())
                 return Collections.emptyMap();
 
             Set<Long> eligibleSubjects = 
                 getEligibleSubjects(homeFolderId, requestService.getSubjectPolicy());
             Map<Long, Set<RequestSeason>> result = new HashMap<Long, Set<RequestSeason>>();
 
             // by default, add every subject to all open seasons, restrictions
             // will be made next
             for (Long subjectId : eligibleSubjects)
                 result.put(subjectId, new HashSet<RequestSeason>(openSeasons));
 
             // no restriction on the number of registrations per season
             // just return the whole map
             if (requestType.getAuthorizeMultipleRegistrationsPerSeason())
                 return result;
 
             for (RequestSeason season : openSeasons) {
                 // get all requests made for this season by the current home folder
                 Set<Critere> criterias = new HashSet<Critere>(3);
                 criterias.add(new Critere(Request.SEARCH_BY_HOME_FOLDER_ID,
                     homeFolderId, Critere.EQUALS));
                 criterias.add(new Critere(Request.SEARCH_BY_SEASON_ID,
                     season.getId(), Critere.EQUALS));
                 criterias.add(new Critere(Request.SEARCH_BY_STATE,Arrays.asList(getStatesExcludedForRunningRequests()) ,Critere.NIN));
                 List<Request> seasonRequests = 
                     requestDAO.search(criterias, null, null, 0, 0, false);
                 for (Request request : seasonRequests) {
                     Set<RequestSeason> subjectSeasons = null;
                     if (requestService.getSubjectPolicy().equals(IRequestWorkflowService.SUBJECT_POLICY_NONE))
                         subjectSeasons = result.get(request.getHomeFolderId());
                     else
                         subjectSeasons = result.get(request.getSubjectId());
                     // no current request on this season, let's continue
                     if (subjectSeasons == null)
                         continue;
                     // a request on this season and it is the last one for this
                     // subject,
                     // simply remove the subject
                     else if (subjectSeasons.size() == 1)
                         if (requestService.getSubjectPolicy().equals(IRequestWorkflowService.SUBJECT_POLICY_NONE))
                             result.remove(request.getHomeFolderId());
                         else
                             result.remove(request.getSubjectId());
                     // a request on this season and it is not the last one for
                     // this subject,
                     // drop the season from the set of possible ones
                     else
                         subjectSeasons.remove(season);
                 }
             }
             return result;
 
         } else {
             Set<Long> eligibleSubjects = 
                 getEligibleSubjects(homeFolderId, requestService.getSubjectPolicy());
             Map<Long, Set<RequestSeason>> result = new HashMap<Long, Set<RequestSeason>>();
             for (Long subjectId : eligibleSubjects)
                 result.put(subjectId, null);
             if (!requestService.getSupportMultiple()) {
                 RequestState[] excludedStates = getStatesExcludedForRunningRequests();
                 List<Long> homeFolderSubjectIds = requestDAO.listHomeFolderSubjectIds(homeFolderId,
                         requestService.getLabel(), excludedStates);
                 if (requestService.getSubjectPolicy().equals(IRequestWorkflowService.SUBJECT_POLICY_NONE)) {
                     if (!homeFolderSubjectIds.isEmpty()) {
                         return Collections.emptyMap();
                     } else {
                         return result;
                     }
                 } else {
                     for (Long subjectId : homeFolderSubjectIds)
                         result.remove(subjectId);
                     return result;
                 }
             }
             return result;
         }
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Long> getAuthorizedSubjects(final Request request)
         throws CvqModelException {
         List<Long> subjects = new ArrayList<Long>();
         if (SecurityContext.getCurrentEcitizen() != null
             && !requestTypeService.getSubjectPolicy(request.getRequestType().getId())
                 .equals(IRequestWorkflowService.SUBJECT_POLICY_NONE)) {
             Map<Long, Set<RequestSeason>> authorizedSubjects =
                 getAuthorizedSubjects(request.getRequestType(),
                     SecurityContext.getCurrentEcitizen().getHomeFolder().getId());
             if (request.getRequestSeason() == null) {
                 subjects = new ArrayList<Long>(authorizedSubjects.keySet());
             } else {
                 for (Map.Entry<Long, Set<RequestSeason>> subject : authorizedSubjects.entrySet()) {
                     if (subject.getValue().contains(request.getRequestSeason())) {
                         subjects.add(subject.getKey());
                     }
                 }
             }
             if (request.getSubjectId() != null && !subjects.contains(request.getSubjectId())) {
                 subjects.add(request.getSubjectId());
             }
         }
         return subjects;
     }
 
     // FIXME : when first entering the request creation process, stepStates is empty
     // so return null and add a generic message in view
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<String> getMissingSteps(Request request) {
         List<String> result = new ArrayList<String>();
         for (Map.Entry<String, Map<String, Object>> stepState : request.getStepStates().entrySet()) {
             if ((Boolean)stepState.getValue().get("required")
                 && !"complete".equals(stepState.getValue().get("state"))
                 && !"validation".equals(stepState.getKey())) {
                 result.add(stepState.getKey());
             }
         }
         return result;
     }
 
     private void setAdministrativeInformation(Request request) throws CvqException {
 
         IRequestService requestService = requestServiceRegistry.getRequestService(request);
         RequestType requestType = 
             requestTypeService.getRequestTypeByLabel(requestService.getLabel());
         request.setRequestType(requestType);
         if (!RequestState.DRAFT.equals(request.getState()))
             request.setState(RequestState.PENDING);
         request.setDataState(DataState.PENDING);
         request.setStep(RequestStep.INSTRUCTION);
         request.setCreationDate(new Date());
         request.setOrangeAlert(Boolean.FALSE);
         request.setRedAlert(Boolean.FALSE);
 
     }
 
     /**
      * Finalize the setting of request properties (creation date, state, ...) and persist it in BD.
      */
     private Long finalizeAndPersist(Request request, HomeFolder homeFolder, String note)
         throws CvqException {
 
         setAdministrativeInformation(request);
 
         Long requestId = requestDAO.saveOrUpdate(request).getId();
 
         if (!RequestState.DRAFT.equals(request.getState())) {
             // TODO DECOUPLING
             // To flush new individuals and retrieve them with Query.list
             HibernateUtil.getSession().flush();
             byte[] pdfData = requestPdfService.generateCertificate(request);
             
             requestActionService.addCreationAction(requestId, new Date(), pdfData, note);
 
             RequestEvent requestEvent = 
                 new RequestEvent(this, EVENT_TYPE.REQUEST_CREATED, request);
             if (pdfData != null)
                 requestEvent.addComplementaryData(COMP_DATA.PDF_FILE, pdfData);
             applicationContext.publishEvent(requestEvent);
 
         } else if (!requestActionService.hasAction(requestId, RequestActionType.CREATION)) {
             requestActionService.addDraftCreationAction(requestId, new Date());
         }
 
         return requestId;
     }
 
     /**
      * Finalize the setting of request properties (creation date, state, ...) and persist it in BD.
      */
     private Long finalizeAndPersist(final Request request, String note)
         throws CvqException {
         return finalizeAndPersist(request, null, note);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN}, privilege = ContextPrivilege.READ)
     public Map<Individual, Request> getRenewableRequests(final String label)
         throws CvqException {
         Map<Individual, Request> result = new HashMap<Individual, Request>();
         RequestState[] excludedStates = getStatesExcludedForRequestCloning();
         Map<Long, Set<RequestSeason>> subjectsMap = getAuthorizedSubjects(
             requestTypeService.getRequestTypeByLabel(label),
             SecurityContext.getCurrentEcitizen().getHomeFolder().getId());
         Set<Long> subjects = subjectsMap.keySet();
         if (subjects != null && !subjects.isEmpty()) {
             for (Long subjectId : subjects) {
                 List<Request> requests =
                     requestDAO.listBySubjectAndLabel(subjectId, label, excludedStates, false);
                 if (requests.isEmpty()) continue;
                 Request request = requests.get(0);
                 if (requests.size() > 1) {
                     for (Request requestCloned : requests) {
                         if (request.getCreationDate().compareTo(requestCloned.getCreationDate()) < 0)
                             request = requestCloned;
                     }
                 }
                 result.put(userSearchService.getById(subjectId), request);
             }
         }
         return result;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public Request getRequestClone(final Long requestId)
         throws CvqException {
         if (requestId == null) throw new CvqException("request.error.requestIdRequired");
         Request request = requestDAO.findById(requestId, true);
         if (request == null) throw new CvqException("request.error.notFound");
         Request clone = request.clone();
         applicationContext.publishEvent(new RequestEvent(this, EVENT_TYPE.REQUEST_CLONED, clone));
         return clone;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public Request getRequestClone(final Long requestId, final Long requestSeasonId)
         throws CvqException {
         return bootstrapDraft(getRequestClone(requestId), requestSeasonId);
     }
 
     @Override
     public Request getSkeletonRequest(final String requestTypeLabel) throws CvqException {
         IRequestService service = requestServiceRegistry.getRequestService(requestTypeLabel);
         Request request = service.getSkeletonRequest();
         request.setRequestType(requestTypeService.getRequestTypeByLabel(requestTypeLabel));
         if (SecurityContext.getCurrentEcitizen() != null) {
             Adult currentEcitizen = SecurityContext.getCurrentEcitizen();
             request.setRequesterId(currentEcitizen.getId());
             request.setHomeFolderId(currentEcitizen.getHomeFolder().getId());
         }
         return request;
     }
 
     @Override
     public Request getSkeletonRequest(final String requestTypeLabel, final Long requestSeasonId)
         throws CvqException {
         return bootstrapDraft(getSkeletonRequest(requestTypeLabel), requestSeasonId);
     }
 
     private Request bootstrapDraft(Request request, Long requestSeasonId)
         throws CvqException {
         RequestType requestType = request.getRequestType();
         checkRequestTypePolicy(requestType, userSearchService.getHomeFolderById(request.getHomeFolderId()));
         if (requestSeasonId != null) {
             request.setRequestSeason(
                 requestTypeService.getRequestSeason(requestType.getId(), requestSeasonId));
         }
         request.setState(RequestState.DRAFT);
         create(request, null);
         return request;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void modify(Request request)
         throws CvqException {
 
         IRequestService requestService = requestServiceRegistry.getRequestService(request);
         requestService.onRequestModified(request);
 
         updateLastModificationInformation(request, null);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void delete(Request request) {
         requestDAO.delete(request);
         if (request != null) {
             applicationContext.publishEvent(new RequestEvent(this, EVENT_TYPE.REQUEST_DELETED, request));
             HomeFolder homeFolder = userSearchService.getHomeFolderById(request.getHomeFolderId());
             if (homeFolder.isTemporary()) {
                 HibernateUtil.getSession().flush();
                 userWorkflowService.delete(homeFolder);
             }
         }
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void delete(final Long id) {
         delete(requestDAO.findById(id, true));
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void updateRequestDataState(final Long id, final DataState rs)
         throws CvqInvalidTransitionException {
 
         Request request = requestDAO.findById(id);
         if (rs.equals(DataState.VALID))
             validData(request);
         else if (rs.equals(DataState.INVALID))
             invalidData(request);
     }
 
     // TODO : must we trace as request action
     private void validData(Request request)
         throws CvqInvalidTransitionException {
 
         // if no state change asked, just return silently
         if (request.getDataState().equals(DataState.VALID))
             return;
 
         if (request.getDataState().equals(DataState.PENDING))
             request.setDataState(DataState.VALID);
         else
             throw new CvqInvalidTransitionException();
     }
 
     // TODO : must we trace as request action
     private void invalidData(Request request)
             throws CvqInvalidTransitionException {
 
         if (request.getDataState().equals(DataState.INVALID))
             return;
 
         if (request.getDataState().equals(DataState.PENDING))
             request.setDataState(DataState.INVALID);
         else
             throw new CvqInvalidTransitionException();
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.EXTERNAL_SERVICE},
             privilege = ContextPrivilege.WRITE)
     public void updateRequestState(final Long id, final RequestState rs, final String note)
             throws CvqException, CvqModelException, CvqInvalidTransitionException {
 
         Request request = requestDAO.findById(id);
         if (!getWorkflow().isValidTransition(request.getState(), rs)) {
             throw new CvqInvalidTransitionException(request.getState().toString(), rs.toString());
         }
         if (rs.equals(RequestState.PENDING))
             pending(request, note);
         else if (rs.equals(RequestState.INPROGRESS))
             inProgress(request, note);
         else if (rs.equals(RequestState.EXTINPROGRESS))
             extInProgress(request, note);
         else if (rs.equals(RequestState.COMPLETE))
             complete(request, note);
         else if (rs.equals(RequestState.UNCOMPLETE))
             uncomplete(request, note);
         else if (rs.equals(RequestState.RECTIFIED))
             rectify(request, note);
         else if (rs.equals(RequestState.REJECTED))
             reject(request, note);
         else if (rs.equals(RequestState.CANCELLED))
             cancel(request, note);
         else if (rs.equals(RequestState.VALIDATED))
             validate(request, note);
         else if (rs.equals(RequestState.NOTIFIED))
             notify(request, note);
         else if (rs.equals(RequestState.CLOSED))
             close(request, note);
         else if (rs.equals(RequestState.ARCHIVED))
             archive(request, note);
     }
 
     private void pending(Request request, final String note) throws CvqException {
         Date date = new Date();
         updateLastModificationInformation(request, date);
 
         /* Set all the states to pending */
         request.setState(RequestState.PENDING);
         request.setDataState(DataState.PENDING);
         updateDocumentsToPending(request);
 
         WorkflowPendingEvent wfEvent = new WorkflowPendingEvent(request);
         requestExternalService.publish(wfEvent);
 
         byte[] pdfData = requestPdfService.generateCertificate(request);
         requestActionService.addCreationAction(request.getId(), date, pdfData, note);
         postActionsProcess(wfEvent.getWorkflowPostActions());
     }
 
     protected void updateDocumentsToPending(Request request) throws CvqException,
             CvqObjectNotFoundException, CvqInvalidTransitionException {
         Set<RequestDocument> documentSet = request.getDocuments();
         if (documentSet == null)
             return;
 
         for (RequestDocument requestDocument : documentSet) {
             Document document = documentService.getById(requestDocument.getDocumentId());
             if (document.getState().equals(DocumentState.DRAFT))
                 documentService.pending(requestDocument.getDocumentId());
         }
     }
 
     private void inProgress(Request request, final String note) throws CvqException {
         Date date = new Date();
         updateLastModificationInformation(request, date);
         request.setState(RequestState.INPROGRESS);
 
         WorkflowInProgressEvent wfEvent = new WorkflowInProgressEvent(request);
         requestExternalService.publish(wfEvent);
 
         requestActionService.addWorfklowAction(request.getId(), note, date,
             RequestState.INPROGRESS, null);
         postActionsProcess(wfEvent.getWorkflowPostActions());
     }
 
     private void extInProgress(Request request, final String note) throws CvqException {
         Date date = new Date();
         updateLastModificationInformation(request, date);
         request.setState(RequestState.EXTINPROGRESS);
 
         WorkflowExtInProgressEvent wfEvent = new WorkflowExtInProgressEvent(request);
         requestExternalService.publish(wfEvent);
 
         requestActionService.addWorfklowAction(request.getId(), note, date,
             RequestState.EXTINPROGRESS, null);
         postActionsProcess(wfEvent.getWorkflowPostActions());
     }
 
     private void complete(Request request, final String note) throws CvqException {
         if (request.getState().equals(RequestState.COMPLETE))
             return;
 
         Date date = new Date();
         updateLastModificationInformation(request, date);
         request.setState(RequestState.COMPLETE);
 
         WorkflowCompleteEvent wfEvent = new WorkflowCompleteEvent(request);
         requestExternalService.publish(wfEvent);
 
         requestActionService.addWorfklowAction(request.getId(), note, date,
             RequestState.COMPLETE, null);
         postActionsProcess(wfEvent.getWorkflowPostActions());
     }
 
     private void uncomplete(Request request, final String note) throws CvqException {
         if (request.getState().equals(RequestState.UNCOMPLETE))
             return;
         
         Date date = new Date();
         updateLastModificationInformation(request, date);
         request.setState(RequestState.UNCOMPLETE);
 
         WorkflowUncompleteEvent wfEvent = new WorkflowUncompleteEvent(request);
         requestExternalService.publish(wfEvent);
 
         requestActionService.addWorfklowAction(request.getId(), note, date,
             RequestState.UNCOMPLETE, null);
         postActionsProcess(wfEvent.getWorkflowPostActions());
     }
 
     private void rectify(Request request, final String note) throws CvqException {
         Date date = new Date();
         updateLastModificationInformation(request, date);
         request.setState(RequestState.RECTIFIED);
 
         WorkflowRectifiedEvent wfEvent = new WorkflowRectifiedEvent(request);
         requestExternalService.publish(wfEvent);
 
         requestActionService.addWorfklowAction(request.getId(), note, date,
             RequestState.RECTIFIED, null);
         postActionsProcess(wfEvent.getWorkflowPostActions());
     }
 
     private void reject(final Request request, final String note)
         throws CvqException, CvqModelException, CvqInvalidTransitionException {
 
         IRequestService requestService = requestServiceRegistry.getRequestService(request.getId());
         requestService.onRequestRejected(request);
 
         if (request.getState().equals(RequestState.REJECTED))
             return;
 
         Date date = new Date();
         updateLastModificationInformation(request, date);
         request.setState(RequestState.REJECTED);
 
         WorkflowRejectedEvent wfEvent = new WorkflowRejectedEvent(request);
         requestExternalService.publish(wfEvent);
 
         byte[] pdfData = requestPdfService.generateCertificate(request);
         if (requestServiceRegistry.getRequestService(request).isArchiveDocuments()) {
             request.setDocumentsArchive(
                 requestPdfService.generateDocumentsArchive(request.getDocuments()));
         }
 
         requestActionService.addWorfklowAction(request.getId(), note, date,
             RequestState.REJECTED, pdfData);
         postActionsProcess(wfEvent.getWorkflowPostActions());
 
         HomeFolder homeFolder = userSearchService.getHomeFolderById(request.getHomeFolderId());
         if (homeFolder.isTemporary())
             userWorkflowService.changeState(homeFolder, UserState.INVALID);
     }
 
     protected void validateAssociatedDocuments(final Set<RequestDocument> documentSet)
         throws CvqException {
 
         if (documentSet == null)
             return;
 
         for (RequestDocument requestDocument : documentSet) {
             documentService.validate(requestDocument.getDocumentId(),
                     new Date(), "Automatic validation");
         }
     }
 
     private void cancel(final Request request, final String note)
         throws CvqException, CvqModelException, CvqInvalidTransitionException {
 
         IRequestService requestService = requestServiceRegistry.getRequestService(request.getId());
         requestService.onRequestCancelled(request);
 
         if (request.getState().equals(RequestState.CANCELLED))
              return;
 
         Date date = new Date();
         updateLastModificationInformation(request, date);
         request.setState(RequestState.CANCELLED);
 
         WorkflowCancelledEvent wfEvent = new WorkflowCancelledEvent(request);
         requestExternalService.publish(wfEvent);
 
         byte[] pdfData = requestPdfService.generateCertificate(request);
         if (requestServiceRegistry.getRequestService(request).isArchiveDocuments()) {
             request.setDocumentsArchive(
                 requestPdfService.generateDocumentsArchive(request.getDocuments()));
          }
 
         requestActionService.addWorfklowAction(request.getId(), note, date,
             RequestState.CANCELLED, pdfData);
         postActionsProcess(wfEvent.getWorkflowPostActions());
 
         HomeFolder homeFolder = userSearchService.getHomeFolderById(request.getHomeFolderId());
         if (homeFolder.isTemporary())
             userWorkflowService.changeState(homeFolder, UserState.INVALID);
      }
 
     private void validate(Request request, final String note)
         throws CvqException, CvqInvalidTransitionException, CvqModelException {
         if (request.getState().equals(RequestState.VALIDATED))
             return;
 
         IRequestService requestService = requestServiceRegistry.getRequestService(request.getId());
         requestService.onRequestValidated(request);
 
         Date date = new Date();
         request.setValidationDate(date);
         updateLastModificationInformation(request, date);
         request.setState(RequestState.VALIDATED);
         request.setDataState(DataState.VALID);
         request.setStep(RequestStep.DELIVERY);
 
         WorkflowValidatedEvent wfEvent = new WorkflowValidatedEvent(request);
         requestExternalService.publish(wfEvent);
 
         // TODO Decoupling
         byte[] pdfData = requestPdfService.generateCertificate(request);
         if (requestServiceRegistry.getRequestService(request).isArchiveDocuments()) {
             request.setDocumentsArchive(
                 requestPdfService.generateDocumentsArchive(request.getDocuments()));
         }
 
         requestActionService.addWorfklowAction(request.getId(), note, date,
             RequestState.VALIDATED, pdfData);
 
         validateAssociatedDocuments(request.getDocuments());
 
         HomeFolder homeFolder = userSearchService.getHomeFolderById(request.getHomeFolderId());
         Individual individual = userSearchService.getAdultById(request.getRequesterId());
        if (homeFolder.isTemporary())
             userWorkflowService.changeState(individual, UserState.VALID);
 
         RequestEvent requestEvent = 
             new RequestEvent(this, EVENT_TYPE.REQUEST_VALIDATED, request);
         if (pdfData != null)
             requestEvent.addComplementaryData(COMP_DATA.PDF_FILE, pdfData);
         applicationContext.publishEvent(requestEvent);
         postActionsProcess(wfEvent.getWorkflowPostActions());
     }
 
     private void notify(Request request, final String note) throws CvqException {
         if (request.getState().equals(RequestState.NOTIFIED))
             return;
 
         Date date = new Date();
         updateLastModificationInformation(request, date);
         request.setState(RequestState.NOTIFIED);
 
         WorkflowNotifiedEvent wfEvent = new WorkflowNotifiedEvent(request);
         requestExternalService.publish(wfEvent);
 
         requestActionService.addWorfklowAction(request.getId(), note, date,
             RequestState.NOTIFIED, null);
         postActionsProcess(wfEvent.getWorkflowPostActions());
     }
 
     private void close(Request request, final String note) throws CvqException {
         if (request.getState().equals(RequestState.CLOSED))
             return;
 
         Date date = new Date();
         updateLastModificationInformation(request, date);
         request.setState(RequestState.CLOSED);
 
         WorkflowClosedEvent wfEvent = new WorkflowClosedEvent(request);
         requestExternalService.publish(wfEvent);
 
         requestActionService.addWorfklowAction(request.getId(), note, date,
             RequestState.CLOSED, null);
         postActionsProcess(wfEvent.getWorkflowPostActions());
     }
 
     private void archive(Request request, final String note)
         throws CvqException, CvqInvalidTransitionException, CvqModelException {
         if (request.getState().equals(RequestState.ARCHIVED))
             return;
 
         Date date = new Date();
         updateLastModificationInformation(request, date);
         request.setState(RequestState.ARCHIVED);
 
         WorkflowArchivedEvent wfEvent = new WorkflowArchivedEvent(request);
         requestExternalService.publish(wfEvent);
 
         requestActionService.addWorfklowAction(request.getId(), note, date,
             RequestState.ARCHIVED, null);
         postActionsProcess(wfEvent.getWorkflowPostActions());
 
         HomeFolder homeFolder = userSearchService.getHomeFolderById(request.getHomeFolderId());
         Individual individual = userSearchService.getAdultById(request.getRequesterId());
         if (homeFolder.isTemporary())
             userWorkflowService.changeState(homeFolder, UserState.ARCHIVED);
     }
 
     // TODO : Review security rules
     private void postActionsProcess(List<IWorkflowPostAction> workflowPostActions) {
         for(IWorkflowPostAction workflowPostAction : workflowPostActions) {
             try {
                 // Save current context
                 String externalService = null;
                 String context = null;
                 if (SecurityContext.isExternalServiceContext())
                     externalService = SecurityContext.getCurrentExternalService();
                 else
                     context = SecurityContext.getCurrentContext();
 
                 // Switch to ES context
                 SecurityContext.setCurrentContext(SecurityContext.EXTERNAL_SERVICE_CONTEXT);
                 SecurityContext.setCurrentExternalService(workflowPostAction.getExecutor());
 
                 workflowPostAction.execute(this);
 
                 // Reset saved context
                 if (externalService != null)
                     SecurityContext.setCurrentExternalService(externalService);
                 else
                     SecurityContext.setCurrentContext(context);
             } catch (CvqException e) {
                 // Harmless error
                 logger.debug(e.getMessage());
             }
         }
     }
 
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     private void archiveHomeFolderRequests(Long homeFolderId) {
 
         List<Request> requests = requestDAO.listByHomeFolder(homeFolderId, false);
         if (requests == null || requests.isEmpty()) {
             logger.debug("archiveHomeFolderRequests() no requests associated to home folder "
                     + homeFolderId);
             return;
         }
 
         // duplicated to avoid state checks
         for (Request request : requests) {
             request.setState(RequestState.ARCHIVED);
             Date date = new Date();
             updateLastModificationInformation(request, date);
             requestActionService.addWorfklowAction(request.getId(), null, date,
                 RequestState.ARCHIVED, null);
         }
     }
 
     @Override
     public DataState[] getPossibleTransitions(DataState ds) {
         List<DataState> dataStateList = new ArrayList<DataState>();
 
         if (ds.equals(DataState.PENDING)) {
             dataStateList.add(DataState.VALID);
             dataStateList.add(DataState.INVALID);
         }
         return dataStateList.toArray(new DataState[dataStateList.size()]);
     }
 
     @Override
     public RequestState[] getPossibleTransitions(RequestState rs) {
         return getWorkflow().getPossibleTransitions(rs);
     }
 
     @Override
     public Boolean isValidTransition(RequestState from, RequestState to) {
         return getWorkflow().isValidTransition(from, to);
     }
 
     @Override
     public RequestState[] getStatesBefore(RequestState rs) {
         return getWorkflow().getStatesBefore(rs);
     }
 
     @Override
     public RequestState[] getStatesExcludedForRunningRequests() {
         return getWorkflow().getStatesWithProperty("!runnable");
     }
 
     @Override
     public RequestState[] getStatesExcludedForRequestCloning() {
         return getWorkflow().getStatesWithProperty("!cloneable");
     }
 
     @Override
     public RequestState[] getEditableStates() {
         return getWorkflow().getStatesWithProperty("editableBO");
     }
 
     @Override
     public boolean isEditable(final Long requestId) {
         Request request = requestDAO.findById(requestId);
         if (Arrays.asList(getWorkflow().getStatesWithProperty("editableFO")).contains(request.getState()))
             return true;
 
         return false;
     }
 
     @Override
     public boolean isSupportMultiple(final String requestLabel) {
         IRequestService requestService = requestServiceRegistry.getRequestService(requestLabel);
         return requestService.getSupportMultiple();
     }
 
     @Override
     public List<RequestState> getInstructionDoneStates() {
         return Arrays.asList(getWorkflow().getStatesWithProperty("done"));
     }
 
     private void updateLastModificationInformation(Request request,
         final Date date) {
 
         // update request's last modification date
         if (date != null)
             request.setLastModificationDate(date);
         else
             request.setLastModificationDate(new Date());
 
         Long userId = SecurityContext.getCurrentUserId();
         request.setLastInterveningUserId(userId);
 
         requestDAO.update(request);
     }
 
     @Override
     public void onApplicationEvent(UserEvent event) {
         logger.debug("onApplicationEvent() got a user event of type " + event.getAction().getType());
         if (UserAction.Type.STATE_CHANGE.equals(event.getAction().getType())) {
             HomeFolder homeFolder = userSearchService.getHomeFolderById(event.getAction().getTargetId());
             if (homeFolder != null) {
                 if (UserState.ARCHIVED.equals(homeFolder.getState())) {
                     logger.debug("onApplicationEvent() archiving requests for home folder "
                         + homeFolder.getId());
                     archiveHomeFolderRequests(homeFolder.getId());
                 }
             } else {
                 logger.debug("onApplicationEvent() nothing to do for individual "
                     + event.getAction().getTargetId());
             }
         } else if (UserAction.Type.DELETION.equals(event.getAction().getType())) {
             HomeFolder homeFolder = userSearchService.getHomeFolderById(event.getAction().getTargetId());
             if (homeFolder != null) {
                 logger.debug("onApplicationEvent() deleting requests for home folder "
                     + homeFolder.getId());
                 for (Request request : requestDAO.listByHomeFolder(homeFolder.getId(), false)) {
                     delete(request);
                 }
             } else {
                 logger.debug("onApplicationEvent() nothing to do for individual "
                     + event.getAction().getTargetId());
             }
         } else if (UserAction.Type.MERGE.equals(event.getAction().getType())) {
             JsonObject payload = JSONUtils.deserialize(event.getAction().getData());
             HomeFolder homeFolder = userSearchService.getHomeFolderById(event.getAction().getTargetId());
             if (homeFolder != null) {
                 logger.debug("onApplicationEvent() moving requests of home folder "
                         + event.getAction().getTargetId() + " to " + payload.get("merge").getAsLong());
                 List<Request> requests = requestSearchService.getByHomeFolderId(homeFolder.getId(), false);
                 for (Request request : requests) {
                     request.setHomeFolderId(payload.get("merge").getAsLong());
                 }
             } else {
                 logger.debug("onApplicationEvent() moving requests of individual "
                         + event.getAction().getTargetId() + " to " + payload.get("merge"));
                 Individual targetIndividual = userSearchService.getById(payload.get("merge").getAsLong());
                 Individual individual = userSearchService.getById(event.getAction().getTargetId());
                 List<Request> requests = 
                     requestSearchService.getByHomeFolderId(individual.getHomeFolder().getId(), false);
                 for (Request request : requests) {
                     if (request.getRequesterId().equals(event.getAction().getTargetId())) {
                         request.setRequesterId(targetIndividual.getId());
                     } 
                     if (request.getSubjectId() != null && request.getSubjectId().equals(event.getAction().getTargetId())) {
                         request.setSubjectId(targetIndividual.getId());
                     }
                     if (request.getLastInterveningUserId() != null && request.getLastInterveningUserId().equals(event.getAction().getTargetId())) {
                         request.setLastInterveningUserId(targetIndividual.getId());
                     }
                     for (RequestAction requestAction : request.getActions()) {
                         if (requestAction.getAgentId().equals(event.getAction().getTargetId())) {
                             requestAction.setAgentId(targetIndividual.getId());
                         }
                     }
                     for (RequestNote requestNote : request.getNotes()) {
                         if (requestNote.getUserId().equals(event.getAction().getTargetId())) {
                             requestNote.setUserId(targetIndividual.getId());
                         }
                     }
                 }
             }
         }
     }
 
     public void setRequestDAO(IRequestDAO requestDAO) {
         this.requestDAO = requestDAO;
     }
 
     public void setDocumentService(IDocumentService documentService) {
         this.documentService = documentService;
     }
 
     public void setUserSearchService(IUserSearchService userSearchService) {
         this.userSearchService = userSearchService;
     }
 
     public void setUserWorkflowService(IUserWorkflowService userWorkflowService) {
         this.userWorkflowService = userWorkflowService;
     }
 
     public void setRequestActionService(IRequestActionService requestActionService) {
         this.requestActionService = requestActionService;
     }
 
     public void setRequestExternalService(IRequestExternalService requestExternalService) {
         this.requestExternalService = requestExternalService;
     }
 
     public void setRequestServiceRegistry(IRequestServiceRegistry requestServiceRegistry) {
         this.requestServiceRegistry = requestServiceRegistry;
     }
 
     public void setRequestTypeService(IRequestTypeService requestTypeService) {
         this.requestTypeService = requestTypeService;
     }
 
     public void setRequestDocumentService(IRequestDocumentService requestDocumentService) {
         this.requestDocumentService = requestDocumentService;
     }
 
     public void setRequestPdfService(IRequestPdfService requestPdfService) {
         this.requestPdfService = requestPdfService;
     }
 
     public void setRequestSearchService(IRequestSearchService requestSearchService) {
         this.requestSearchService = requestSearchService;
     }
 
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
 
     @Override
     public void setApplicationContext(ApplicationContext arg0) throws BeansException {
         this.applicationContext = arg0;
     }
 }
