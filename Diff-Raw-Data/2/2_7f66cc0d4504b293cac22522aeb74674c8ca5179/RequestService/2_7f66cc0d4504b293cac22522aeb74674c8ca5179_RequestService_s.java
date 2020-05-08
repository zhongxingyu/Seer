 package fr.cg95.cvq.service.request.impl;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlObject;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.ListableBeanFactory;
 import org.w3c.dom.Node;
 
 import fr.cg95.cvq.business.request.DataState;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestAction;
 import fr.cg95.cvq.business.request.RequestDocument;
 import fr.cg95.cvq.business.request.RequestNote;
 import fr.cg95.cvq.business.request.RequestNoteType;
 import fr.cg95.cvq.business.request.RequestSeason;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.RequestStep;
 import fr.cg95.cvq.business.request.RequestType;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.payment.Payment;
 import fr.cg95.cvq.business.users.payment.PaymentState;
 import fr.cg95.cvq.business.users.payment.PurchaseItem;
 import fr.cg95.cvq.dao.IGenericDAO;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.dao.request.IRequestFormDAO;
 import fr.cg95.cvq.dao.request.IRequestNoteDAO;
 import fr.cg95.cvq.exception.CvqConfigurationException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.external.IExternalService;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.authority.LocalAuthorityConfigurationBean;
 import fr.cg95.cvq.service.document.IDocumentTypeService;
 import fr.cg95.cvq.service.request.IRequestActionService;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.request.IRequestServiceRegistry;
 import fr.cg95.cvq.service.request.IRequestTypeService;
 import fr.cg95.cvq.service.request.IRequestWorkflowService;
 import fr.cg95.cvq.service.request.annotation.RequestFilter;
 import fr.cg95.cvq.service.request.condition.EqualityChecker;
 import fr.cg95.cvq.service.request.condition.IConditionChecker;
 import fr.cg95.cvq.service.users.ICertificateService;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 import fr.cg95.cvq.service.users.IIndividualService;
 import fr.cg95.cvq.util.Critere;
 import fr.cg95.cvq.util.mail.IMailService;
 import fr.cg95.cvq.xml.common.SubjectType;
 
 
 /**
  * Partial implementation of the {@link IRequestService}, only provides functionalities
  * common to all request types.
  *
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public abstract class RequestService implements IRequestService, BeanFactoryAware {
 
     private static Logger logger = Logger.getLogger(RequestService.class);
 
     protected String localReferentialFilename;
     protected String placeReservationFilename;
     protected String externalReferentialFilename;
     protected Boolean supportUnregisteredCreation;
     protected String subjectPolicy = SUBJECT_POLICY_NONE;
     protected String label;
     protected String xslFoFilename;
     protected Boolean isOfRegistrationKind;
     protected Map<String,IConditionChecker> filledConditions;
 
     protected IDocumentTypeService documentTypeService;
     protected IHomeFolderService homeFolderService;
     protected ICertificateService certificateService;
     protected IRequestServiceRegistry requestServiceRegistry;
     protected IRequestActionService requestActionService;
     protected IRequestTypeService requestTypeService;
     protected IRequestWorkflowService requestWorkflowService;
     protected ILocalAuthorityRegistry localAuthorityRegistry;
     protected IMailService mailService;
     protected IExternalService externalService;
     protected IIndividualService individualService;
 
     protected IGenericDAO genericDAO;
     protected IRequestDAO requestDAO;
     protected IRequestNoteDAO requestNoteDAO;
     protected IRequestFormDAO requestFormDAO;
 
     private ListableBeanFactory beanFactory;
 
     public RequestService() {
         super();
     }
 
     public void init() throws CvqConfigurationException {
         // register with the request registry
         requestServiceRegistry.registerService(this, getLabel());
 
         if (supportUnregisteredCreation == null)
             supportUnregisteredCreation = Boolean.FALSE;
         
         this.homeFolderService = (IHomeFolderService)
             beanFactory.getBeansOfType(IHomeFolderService.class, false, false).values().iterator().next();
         this.externalService = (IExternalService)
             beanFactory.getBeansOfType(IExternalService.class, false, false).values().iterator().next();
         this.requestWorkflowService = (IRequestWorkflowService)
             beanFactory.getBeansOfType(IRequestWorkflowService.class, false, false).values().iterator().next();
         
         initFilledConditions();
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT, privilege=ContextPrivilege.NONE)
     @RequestFilter(privilege=ContextPrivilege.READ)
     public List<Request> get(Set<Critere> criteriaSet, final String sort, final String dir,
             final int recordsReturned, final int startIndex)
         throws CvqException {
 
         return requestDAO.search(criteriaSet, sort, dir, recordsReturned, startIndex);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT, privilege=ContextPrivilege.NONE)
     @RequestFilter(privilege=ContextPrivilege.READ)
     public Long getCount(Set<Critere> criteriaSet) throws CvqException {
 
         return requestDAO.count(criteriaSet);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Request getById(final Long id)
         throws CvqException, CvqObjectNotFoundException {
             return (Request) requestDAO.findById(Request.class, id);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Request> getByRequesterId(final Long requesterId)
         throws CvqException {
 
         return requestDAO.listByRequester(requesterId);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Request> getBySubjectId(final Long subjectId)
         throws CvqException, CvqObjectNotFoundException {
 
         return requestDAO.listBySubject(subjectId);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Request> getBySubjectIdAndRequestLabel(final Long subjectId, 
             final String requestLabel, boolean retrieveArchived)
         throws CvqException, CvqObjectNotFoundException {
 
     	if (requestLabel == null)
     		throw new CvqModelException("request.label_required");
 
         RequestState[] excludedStates = null;
         if (!retrieveArchived)
             excludedStates = new RequestState[] { RequestState.ARCHIVED };
 
         return requestDAO.listBySubjectAndLabel(subjectId, requestLabel, excludedStates);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Request> getByHomeFolderId(final Long homeFolderId) throws CvqException {
 
 		return requestDAO.listByHomeFolder(homeFolderId);
 	}
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Request> getByHomeFolderIdAndRequestLabel(final Long homeFolderId, 
             final String requestLabel)
         throws CvqException, CvqObjectNotFoundException {
 
         return requestDAO.listByHomeFolderAndLabel(homeFolderId, requestLabel, null);
     }
 
     private void updateLastModificationInformation(Request request, final Date date)
         throws CvqException {
 
         // update request's last modification date
         if (date != null)
             request.setLastModificationDate(date);
         else
             request.setLastModificationDate(new Date());
         request.setLastInterveningAgentId(SecurityContext.getCurrentUserId());
 
         requestDAO.update(request);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<RequestNote> getNotes(final Long id)
         throws CvqException {
 
         // TODO ACMF : if ecitizen, filter notes he is not authorized to see 
         // (eg instruction internal)
         return requestNoteDAO.listByRequest(id);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void addNote(final Long requestId, final RequestNoteType rtn, final String note)
         throws CvqException, CvqObjectNotFoundException {
 
 
         Long agentId = SecurityContext.getCurrentUserId();
 
 	    RequestNote requestNote = new RequestNote();
         requestNote.setType(rtn);
         requestNote.setNote(note);
         requestNote.setAgentId(agentId);
 
         Request request = getById(requestId);
 	    if (request.getNotes() == null) {
 	        Set<RequestNote> notes = new HashSet<RequestNote>();
 	        notes.add(requestNote);
 	        request.setNotes(notes);
     	} else {
 	        request.getNotes().add(requestNote);
 	    }
 
         updateLastModificationInformation(request, null);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void addDocuments(final Long requestId, final Set<Long> documentsId)
         throws CvqException, CvqObjectNotFoundException {
 
         Request request = getById(requestId);
 
         for (Long documentId : documentsId) {
             RequestDocument requestDocument = new RequestDocument();
             requestDocument.setDocumentId(documentId);
             if (request.getDocuments() == null) {
                 Set<RequestDocument> documentSet = new HashSet<RequestDocument>();
                 documentSet.add(requestDocument);
                 request.setDocuments(documentSet);
             } else {
                 request.getDocuments().add(requestDocument);
             }
         }
 
         updateLastModificationInformation(request, null);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void addDocument(final Long requestId, final Long documentId)
         throws CvqException, CvqObjectNotFoundException {
 
         Request request = getById(requestId);
 
         RequestDocument requestDocument = new RequestDocument();
         requestDocument.setDocumentId(documentId);
         if (request.getDocuments() == null) {
             Set<RequestDocument> documents = new HashSet<RequestDocument>();
             documents.add(requestDocument);
             request.setDocuments(documents);
         } else {
             request.getDocuments().add(requestDocument);
         }
 
         updateLastModificationInformation(request, null);
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void addDocument(Request request, final Long documentId)
         throws CvqException, CvqObjectNotFoundException {
         RequestDocument requestDocument = new RequestDocument();
         requestDocument.setDocumentId(documentId);
         if (request.getId() != null)
             request = getById(request.getId());
         if (request.getDocuments() == null) {
             Set<RequestDocument> documents = new HashSet<RequestDocument>();
             documents.add(requestDocument);
             request.setDocuments(documents);
         } else {
             request.getDocuments().add(requestDocument);
         }
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void removeDocument(Request request, final Long documentId)
         throws CvqException, CvqObjectNotFoundException {
         if (request.getId() != null)
             request = getById(request.getId());
         Iterator<RequestDocument> it = request.getDocuments().iterator();
         while (it.hasNext()) {
             RequestDocument rd = it.next();
             if (rd.getDocumentId().equals(documentId)){
                 it.remove();
             }
         }
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Set<RequestDocument> getAssociatedDocuments(final Long requestId)
         throws CvqException {
 
         Request request = getById(requestId);
         return request.getDocuments();
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Set<RequestDocument> getAssociatedDocuments(Request request)
         throws CvqException {
         if (request.getId() != null)
             request = getById(request.getId());
         return request.getDocuments();
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public byte[] getCertificate(final Long id, final RequestState requestState)
         throws CvqException {
 
         RequestAction requestAction =
             requestActionService.getActionByResultingState(id, requestState);
 
         return requestAction != null ? requestAction.getFile() : null;
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public RequestSeason getRequestAssociatedSeason(Long requestId) throws CvqException {
 
         Request request = getById(requestId);
         RequestType requestType = request.getRequestType();
 
         if (requestType.getSeasons() != null) {
             for (RequestSeason requestTypeSeason : requestType.getSeasons()) {
                 if (requestTypeSeason.getUuid().equals(request.getSeasonUuid()))
                     return requestTypeSeason;
             }
         }
         return null;
     }
 
     //////////////////////////////////////////////////////////
     // Payment related methods
     //////////////////////////////////////////////////////////
 
     public final void notifyPaymentResult(final Payment payment)
         throws CvqException {
 
         // for each different request found in purchased items list, notify the associated
         // service of payment result status
         Set<Long> requests = new HashSet<Long>();
         Set<PurchaseItem> purchaseItems = payment.getPurchaseItems();
         for (PurchaseItem purchaseItem : purchaseItems) {
             // if purchase item is bound to a request, notify the corresponding service
             if (purchaseItem.getRequestId() != null)
                 requests.add(purchaseItem.getRequestId());
         }
 
         if (!requests.isEmpty()) {
         	for (Long requestId : requests) {
         	    Request request = getById(requestId);
                 IRequestService requestService = 
                     requestServiceRegistry.getRequestService(getById(requestId));
                 if (payment.getState().equals(PaymentState.VALIDATED))
                     requestService.onPaymentValidated(request, payment.getBankReference());
                 else if (payment.getState().equals(PaymentState.CANCELLED))
                     requestService.onPaymentCancelled(request);
                 else if (payment.getState().equals(PaymentState.REFUSED))
                     requestService.onPaymentRefused(request);
             }
         }
 
         externalService.creditHomeFolderAccounts(payment);
     }
 
     public boolean hasMatchingExternalService(final String requestLabel)
         throws CvqException {
 
         return externalService.hasMatchingExternalService(requestLabel);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Map<Date, String> getConsumptionsByRequest(final Long requestId, final Date dateFrom,
             final Date dateTo)
         throws CvqException {
 
         Request request = getById(requestId);
         if (request.getState().equals(RequestState.ARCHIVED)) {
             logger.debug("getConsumptionsByRequest() Filtering archived request");
             return null;
         }
 
         return externalService.getConsumptionsByRequest(request, dateFrom, dateTo);
     }
 
     public String getConsumptionsField()
         throws CvqException {
         return null;
     }
 
     //////////////////////////////////////////////////////////
     // Workflow related methods
     //////////////////////////////////////////////////////////
 
     @Override
     public void prepareDraft(Request request) throws CvqException {
         request.setDraft(true);
         request.setHomeFolderId(SecurityContext.getCurrentEcitizen().getHomeFolder().getId());
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public Long processDraft(Request request) throws CvqException {
         if(request.getId() == null) {
             return this.createDraft(request);
         } else {
             this.modifyDraft(request);
             return request.getId();
         }
     }
     
     @Override
     public Long createDraft(Request request) throws CvqException {
         performBusinessChecks(request, SecurityContext.getCurrentEcitizen(), null);
         return finalizeAndPersist(request);
     }
     
     @Override
     public void modifyDraft(Request request) throws CvqException {
         if(this.isSubjectChanged(request)) {
             this.createDraft(request);
         } else {
             createOrSynchronizeHomeFolder(request, null);
             finalizeAndPersist(request);
         }
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void finalizeDraft(Request request) throws CvqException {
         request.setDraft(false);
         this.modifyDraft(request);
     }
     
     protected boolean isSubjectChanged(Request request) {
         Long subjectId = this.requestDAO.getSubjectId(request.getId());
         if(subjectId == null) {
             if(request.getSubjectId() != null) return true;
             else return false;
         }
         return !subjectId.equals(request.getSubjectId());
     }
     
     protected void notifyRequestCreation(Request request, byte[] pdfData)
         throws CvqException {
 
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         Adult requester = (Adult) individualService.getById(request.getRequesterId());
         if (requester.getEmail() != null && !requester.getEmail().equals("")) {
             Map<String, String> ecitizenCreationNotifications =
                 lacb.getEcitizenCreationNotifications();
             if (ecitizenCreationNotifications == null) {
                 logger.warn("notifyRequestCreation() ecitizen creation notifications not configured !");
                 return;
             }
             String mailData = ecitizenCreationNotifications.get("mailData");
             Boolean attachPdf =
                 Boolean.valueOf(ecitizenCreationNotifications.get("attachPdf"));
             String mailDataBody =
                 localAuthorityRegistry.getBufferedCurrentLocalAuthorityResource(
                         ILocalAuthorityRegistry.TXT_ASSETS_RESOURCE_TYPE, mailData, false);
 
             if (mailDataBody == null) {
                 logger.warn("notifyRequestCreation() no mail data for ecitizen request creation notification");
             } else {
                 StringBuffer mailSubject = new StringBuffer();
                 mailSubject.append("[").append(lacb.getDisplayTitle()).append("] ")
                     .append(ecitizenCreationNotifications.get("mailSubject"));
 
                 if (attachPdf) {
                     mailService.send(null, requester.getEmail(), null,
                             mailSubject.toString(), mailDataBody, pdfData, "Recu_Demande.pdf");
                 } else {
                     mailService.send(null, requester.getEmail(), null,
                             mailSubject.toString(), mailDataBody);
                 }
             }
         }
     }
 
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public Long create(Request request)
         throws CvqException, CvqObjectNotFoundException {
 
         performBusinessChecks(request, SecurityContext.getCurrentEcitizen(), null);
         
         return finalizeAndPersist(request);
     }
 
     @Override
     @Context(type=ContextType.UNAUTH_ECITIZEN,privilege=ContextPrivilege.WRITE)
     public Long create(Request request, Adult requester, Individual subject)
         throws CvqException {
         
         HomeFolder homeFolder = performBusinessChecks(request, requester, subject);
         
         return finalizeAndPersist(request, homeFolder);
     }
     
     protected HomeFolder performBusinessChecks(Request request,Adult requester,Individual subject)
         throws CvqException, CvqObjectNotFoundException {
         
         HomeFolder homeFolder = createOrSynchronizeHomeFolder(request, requester);
         
         if(!request.getDraft() || request.getDraft() == null)
             checkSubjectPolicy(request.getSubjectId(),request.getHomeFolderId(),getSubjectPolicy());
         
         if (request.getSubjectId() != null) {
             Individual individual = individualService.getById(request.getSubjectId());
             request.setSubjectId(individual.getId());
             request.setSubjectLastName(individual.getLastName());
             request.setSubjectFirstName(individual.getFirstName());
         }
         
         return homeFolder;
     }
     
     protected void performBusinessChecks(final Request request) throws CvqException {
         performBusinessChecks(request, null, null);
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
     protected HomeFolder createOrSynchronizeHomeFolder(Request request, Adult requester)
         throws CvqException, CvqModelException {
 
         // in case requester id is not filled, feed it with currently logged in ecitizen
         if (request.getRequesterId() == null && SecurityContext.getCurrentEcitizen() != null) 
             request.setRequesterId(SecurityContext.getCurrentEcitizen().getId());
         
         if (request.getRequesterId() == null) {
             if (supportUnregisteredCreation.booleanValue()) {
                 logger.debug("create() Gonna create implicit home folder");
                 HomeFolder homeFolder = homeFolderService.create(requester);
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
             Individual somebody = individualService.getById(request.getRequesterId());
             if (somebody instanceof Child)
                 throw new CvqModelException("request.error.requesterMustBeAdult");
             Adult adult = (Adult) somebody;
             request.setRequesterLastName(adult.getLastName());
             request.setRequesterFirstName(adult.getFirstName());
             request.setHomeFolderId(adult.getHomeFolder().getId());
         }
 
         return null;
     }
 
     /**
      * Perform checks wrt subject policies :
      * <ul>
      *   <li>Check that subject is coherent wrt the request's policy.</li>
      *   <li>Check that subject is allowed to issue a request of the given type</li>
      * </ul>
      * 
      * @throws CvqModelException if there's a policy violation
      */
     private void checkSubjectPolicy(final Long subjectId, Long homeFolderId, final String policy) 
         throws CvqException, CvqModelException {
 
         // first, check general subject policy
         if (!policy.equals(SUBJECT_POLICY_NONE)) {
             if (subjectId == null)
                 throw new CvqModelException("model.request.subject_is_required");
             Individual subject = individualService.getById(subjectId);
             if (policy.equals(SUBJECT_POLICY_INDIVIDUAL)) {
                 if (!(subject instanceof Individual)) {
                     throw new CvqModelException("model.request.wrong_subject_type");
                 }
             } else if (policy.equals(SUBJECT_POLICY_ADULT)) {
                 if (!(subject instanceof Adult)) {
                     throw new CvqModelException("model.request.wrong_subject_type");
                 }
             } else if (policy.equals(SUBJECT_POLICY_CHILD)) {
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
         if (!getSubjectPolicy().equals(SUBJECT_POLICY_NONE)) {
             Individual individual = individualService.getById(subjectId);
             boolean isAuthorized = false;
             Map<Long, Set<RequestSeason>> authorizedSubjectsMap = 
                 getAuthorizedSubjects(homeFolderId);
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
 
     /**
      * Get the list of eligible subjects for the current request service. Does
      * not make any control on already existing requests.
      */
     private Set<Long> getEligibleSubjects(final Long homeFolderId) throws CvqException {
 
         if (getSubjectPolicy().equals(SUBJECT_POLICY_NONE)) {
             Set<Long> result = new HashSet<Long>();
             result.add(homeFolderId);
             return result;
         } else {
             List<Individual> individualsReference = homeFolderService.getIndividuals(homeFolderId);
             Set<Long> result = new HashSet<Long>();
             for (Individual individual : individualsReference) {
                 if (getSubjectPolicy().equals(SUBJECT_POLICY_INDIVIDUAL)) {
                     result.add(individual.getId());
                 } else if (getSubjectPolicy().equals(SUBJECT_POLICY_ADULT)) {
                     if (individual instanceof Adult)
                         result.add(individual.getId());
                 } else if (getSubjectPolicy().equals(SUBJECT_POLICY_CHILD)) {
                     if (individual instanceof Child)
                         result.add(individual.getId());
                 }
             }
 
             return result;
         }
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Map<Long, Set<RequestSeason>> getAuthorizedSubjects(final Long homeFolderId)
             throws CvqException, CvqObjectNotFoundException {
 
         RequestType requestType = requestTypeService.getRequestTypeByLabel(getLabel());
         logger.debug("getAuthorizedSubjects() searching authorized subjects for : "
                 + requestType.getLabel());
 
         Set<RequestSeason> openSeasons = requestTypeService.getOpenSeasons(requestType);
         if (openSeasons != null) {
             // no open seasons, no registration is possible
             if (openSeasons.isEmpty())
                 return null;
 
             Set<Long> eligibleSubjects = getEligibleSubjects(homeFolderId);
             Map<Long, Set<RequestSeason>> result = new HashMap<Long, Set<RequestSeason>>();
 
             // by default, add every subject to all open seasons, restrictions
             // will be made next
             for (Long subjectId : eligibleSubjects)
                 result.put(subjectId, openSeasons);
 
             // no restriction on the number of registrations per season
             // just return the whole map
             if (requestType.getAuthorizeMultipleRegistrationsPerSeason())
                 return result;
 
             for (RequestSeason season : openSeasons) {
                 // get all requests made for this season by the current home
                 // folder
                 List<Request> seasonRequests = requestDAO.listByHomeFolderAndSeason(homeFolderId,
                         season.getUuid());
                 for (Request request : seasonRequests) {
                     Set<RequestSeason> subjectSeasons = null;
                     if (getSubjectPolicy().equals(SUBJECT_POLICY_NONE))
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
                         if (getSubjectPolicy().equals(SUBJECT_POLICY_NONE))
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
             Set<Long> eligibleSubjects = getEligibleSubjects(homeFolderId);
             Map<Long, Set<RequestSeason>> result = new HashMap<Long, Set<RequestSeason>>();
             for (Long subjectId : eligibleSubjects)
                 result.put(subjectId, null);
             RequestState[] excludedStates = requestWorkflowService
                     .getStatesExcludedForRunningRequests();
             List<Long> homeFolderSubjectIds = requestDAO.listHomeFolderSubjectIds(homeFolderId,
                     getLabel(), excludedStates);
             if (getSubjectPolicy().equals(SUBJECT_POLICY_NONE)) {
                 if (!homeFolderSubjectIds.isEmpty()) {
                     return null;
                 } else {
                     return result;
                 }
             } else {
                 for (Long subjectId : homeFolderSubjectIds)
                     result.remove(subjectId);
                 return result;
             }
         }
     }
 
     protected void setAdministrativeInformation(Request request) throws CvqException {
         
         RequestType requestType = requestTypeService.getRequestTypeByLabel(getLabel());
         request.setRequestType(requestType);
         request.setState(RequestState.PENDING);
         request.setDataState(DataState.PENDING);
         request.setStep(RequestStep.INSTRUCTION);
         request.setCreationDate(new Date());
         request.setOrangeAlert(Boolean.FALSE);
         request.setRedAlert(Boolean.FALSE);
 
     }
     
     protected Long finalizeAndPersist(Request request, HomeFolder homeFolder) 
         throws CvqException {
 
         setAdministrativeInformation(request);
 
         if (isOfRegistrationKind()) {
             RequestType requestType = requestTypeService.getRequestTypeByLabel(getLabel());
             Set<RequestSeason> openSeasons = requestTypeService.getOpenSeasons(requestType);
             if (openSeasons != null && !openSeasons.isEmpty())  
                 request.setSeasonUuid(openSeasons.iterator().next().getUuid());
         }
         
         Long requestId = (requestDAO.saveOrUpdate(request)).getId();
 
         if (homeFolder != null) {
             homeFolder.setBoundToRequest(Boolean.valueOf(true));
             homeFolder.setOriginRequestId(requestId);
             homeFolderService.modify(homeFolder);
         }
         
         if(!request.getDraft() || request.getDraft() == null) {
             // TODO DECOUPLING
             logger.debug("create() Gonna generate a pdf of the request");
             byte[] pdfData =
                 certificateService.generateRequestCertificate(request);
             requestActionService.addCreationAction(requestId, new Date(), pdfData);
     
             // TODO DECOUPLING
             notifyRequestCreation(request, pdfData);
         }
 
         return requestId;
     }
     
     /**
      * Finalize the setting of request properties (creation date, state, ...) and persist it in BD.
      */
     protected Long finalizeAndPersist(final Request request)
         throws CvqException {
         return finalizeAndPersist(request, null);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Node getRequestClone(final Long subjectId, Long homeFolderId, final String requestLabel)
         throws CvqException {
 
     	if (requestLabel == null)
     		throw new CvqModelException("request.error.labelRequired");
     	if (subjectId == null && homeFolderId == null)
     		throw new CvqModelException("request.error.subjectOrHomeFolderRequired");
 
     	RequestState[] excludedStates =
             requestWorkflowService.getStatesExcludedForRequestsCloning();
         List<Request> requests = null;
         if (subjectId != null)
         	requests = requestDAO.listBySubjectAndLabel(subjectId, requestLabel, excludedStates);
         else if (homeFolderId != null)
         	requests = requestDAO.listByHomeFolderAndLabel(homeFolderId, requestLabel,
         			excludedStates);
         Request request = null;
         if (requests == null || requests.isEmpty()) {
             IRequestService tempRequestService =
                 requestServiceRegistry.getRequestService(requestLabel);
             request = tempRequestService.getSkeletonRequest();
             if (subjectId != null
             		&& !tempRequestService.getSubjectPolicy().equals(SUBJECT_POLICY_NONE)) {
             	checkSubjectPolicy(subjectId, homeFolderId, tempRequestService.getSubjectPolicy());
             	request.setSubjectId(subjectId);
             }
         } else {
             // choose the most recent version of this request 
             request = requests.get(0);
             if (requests.size() > 1)
         	    for (Request requestCloned : requests)
                     if (request.getCreationDate().compareTo(requestCloned.getCreationDate()) < 0)
                         request = requestCloned;
         }
 
         Class[] parameterTypes = null;
         Object[] arguments = null;
         try {
             Method modelToXmlMethod = request.getClass().getMethod("modelToXml", parameterTypes);
             XmlObject xmlRequest = (XmlObject) modelToXmlMethod.invoke(request, arguments);
 
             Method copyMethod = xmlRequest.getClass().getMethod("copy", parameterTypes);
             XmlObject xmlRequestCopy = (XmlObject) copyMethod.invoke(xmlRequest, arguments);
 
             String xmlRequestCopyClass = xmlRequestCopy.getClass().getSimpleName();
             String getBodyMethod = "get" + xmlRequestCopyClass.replace("DocumentImpl", "");
 
             Method xmlRequestGetBody =
                 xmlRequestCopy.getClass().getMethod(getBodyMethod, parameterTypes);
             fr.cg95.cvq.xml.common.RequestType xmlRequestType =
                 (fr.cg95.cvq.xml.common.RequestType) xmlRequestGetBody.invoke(xmlRequestCopy, arguments);
             
             if (request.getSubjectId() != null) {
                 SubjectType subject = xmlRequestType.addNewSubject();
                 Individual requestSubject = individualService.getById(request.getSubjectId());
                 subject.setIndividual(Individual.modelToXml(requestSubject));
             }
 
             purgeClonedRequest(xmlRequestType);
 
             return xmlRequestCopy.getDomNode();
         } catch (SecurityException e) {
         	logger.error("getRequestClone() Security exception while cloning request");
         	throw new CvqException("Security exception while cloning request");
         } catch (IllegalAccessException e) {
         	logger.error("getRequestClone() Illegal access exception while cloning request");
         	throw new CvqException("Illegal access exception while cloning request");
         } catch (InvocationTargetException e) {
         	logger.error("getRequestClone() Invocation target exception while cloning request");
         	throw new CvqException("Invocation target exception while cloning request");
         } catch (NoSuchMethodException e) {
             // hey, you know what ? I know how my methods are named :-)
         }
 
         return null;
     }
 
     protected void purgeClonedRequest(fr.cg95.cvq.xml.common.RequestType requestType) {
 
     	// administrative data
         requestType.setId(0);
         requestType.setCreationDate(null);
         requestType.setDataState(null);
         requestType.setLastInterveningAgentId(0);
         requestType.setLastModificationDate(null);
         requestType.setObservations(null);
         requestType.setState(null);
         requestType.setStep(null);
         requestType.setValidationDate(null);
 
         // business data
         requestType.setRequester(null);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void modify(Request request)
         throws CvqException {
 
         if (request != null) {
             updateLastModificationInformation(request, null);
         }
     }
 
     protected void delete(final Request request)
         throws CvqException, CvqObjectNotFoundException {
 
         requestDAO.delete(request);
         homeFolderService.onRequestDeleted(request.getHomeFolderId(), request.getId());
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void delete(final Long id)
         throws CvqException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         delete(request);
     }
 
     public void onRequestValidated(Request request)
         throws CvqException {
     }
     
     public void onRequestCancelled(Request request) throws CvqException {
     }
 
     public void onRequestRejected(Request request) throws CvqException {
     }
 
     public void onPaymentValidated(Request request, String paymentReference)
         throws CvqException {
     }
 
     public void onPaymentRefused(Request request)
         throws CvqException {
     }
 
     public void onPaymentCancelled(Request request)
         throws CvqException {
     }
 
     public void onExternalServiceSendRequest(Request request, String sendRequestResult) throws CvqException {
     }
     
     //////////////////////////////////////////////////////////
     // Condition related method
     //////////////////////////////////////////////////////////
     
     protected void initFilledConditions() {
         filledConditions = new HashMap<String,IConditionChecker>();
        filledConditions.put("_activeHomeFolder", new EqualityChecker("true"));
     }
     
     public boolean isConditionFilled (Map<String, String> triggers) {
         boolean test = true;
         for (Entry<String, String> trigger : triggers.entrySet()) {
             if (filledConditions.get(trigger.getKey()) != null 
                 && filledConditions.get(trigger.getKey()).test(trigger.getValue()))
                 test = test && true;
             else
                 return false;
         }
         return test;
     }
 
     public String getLocalReferentialFilename() {
         return this.localReferentialFilename;
     }
 
     public void setLocalReferentialFilename(final String filename) {
         this.localReferentialFilename = filename;
     }
 
     public void setRequestDAO(IRequestDAO requestDAO) {
         this.requestDAO = requestDAO;
     }
 
     public void setGenericDAO(IGenericDAO genericDAO) {
         this.genericDAO = genericDAO;
     }
 
     public void setIndividualService(IIndividualService individualService) {
         this.individualService = individualService;
     }
 
     public void setRequestNoteDAO(IRequestNoteDAO requestNoteDAO) {
         this.requestNoteDAO = requestNoteDAO;
     }
 
     public void setRequestFormDAO(IRequestFormDAO requestFormDAO) {
         this.requestFormDAO = requestFormDAO;
     }
 
     public void setDocumentTypeService(IDocumentTypeService documentTypeService) {
         this.documentTypeService = documentTypeService;
     }
 
     public void setCertificateService(ICertificateService certificateService) {
         this.certificateService = certificateService;
     }
 
     public void setRequestServiceRegistry(IRequestServiceRegistry requestServiceRegistry) {
         this.requestServiceRegistry = requestServiceRegistry;
     }
 
     public String getLabel() {
         return label != null ? label : "";
     }
 
     public void setLabel(final String label) {
         this.label = label;
     }
 
     public String getXslFoFilename() {
         return this.xslFoFilename;
     }
 
     public void setXslFoFilename(String xslFoFilename) {
         this.xslFoFilename = xslFoFilename;
     }
 
     public void setSupportUnregisteredCreation(String supportUnregisteredCreation) {
         this.supportUnregisteredCreation = Boolean.valueOf(supportUnregisteredCreation);
     }
 
     public boolean supportUnregisteredCreation() {
         return supportUnregisteredCreation == null ? false : supportUnregisteredCreation;
     }
 
     public void setPlaceReservationFilename(String placeReservationFilename) {
         this.placeReservationFilename = placeReservationFilename;
     }
 
     public String getPlaceReservationFilename() {
         return placeReservationFilename;
     }
 
     public void setExternalReferentialFilename(String externalReferentialFilename) {
         this.externalReferentialFilename = externalReferentialFilename;
     }
 
     public String getExternalReferentialFilename() {
         return externalReferentialFilename;
     }
 
     public String getSubjectPolicy() {
         return subjectPolicy;
     }
 
     public void setSubjectPolicy(final String subjectPolicy) {
         this.subjectPolicy = subjectPolicy;
     }
 
     public boolean isOfRegistrationKind() {
         return isOfRegistrationKind == null ? false : isOfRegistrationKind;
     }
 
     public void setIsOfRegistrationKind(String isOfRegistrationKind) {
         this.isOfRegistrationKind = Boolean.valueOf(isOfRegistrationKind);
     }
 
 	public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
 		this.localAuthorityRegistry = localAuthorityRegistry;
 	}
 
 	public void setMailService(IMailService mailService) {
 		this.mailService = mailService;
 	}
 
     public void setRequestActionService(IRequestActionService requestActionService) {
         this.requestActionService = requestActionService;
     }
 
     public void setRequestTypeService(IRequestTypeService requestTypeService) {
         this.requestTypeService = requestTypeService;
     }
 
     public void setBeanFactory(BeanFactory arg0) throws BeansException {
         this.beanFactory = (ListableBeanFactory) arg0;
     }
 }
