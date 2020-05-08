 package fr.cg95.cvq.service.request.impl;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlObject;
 import org.apache.xmlbeans.XmlOptions;
 import org.w3c.dom.Node;
 
 import fr.cg95.cvq.business.authority.Agent;
 import fr.cg95.cvq.business.authority.Category;
 import fr.cg95.cvq.business.authority.CategoryRoles;
 import fr.cg95.cvq.business.authority.SiteProfile;
 import fr.cg95.cvq.business.authority.SiteRoles;
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.document.DocumentType;
 import fr.cg95.cvq.business.ecitizen.VoCardRequest;
 import fr.cg95.cvq.business.request.DataState;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestAction;
 import fr.cg95.cvq.business.request.RequestForm;
 import fr.cg95.cvq.business.request.RequestFormType;
 import fr.cg95.cvq.business.request.RequestNote;
 import fr.cg95.cvq.business.request.RequestNoteType;
 import fr.cg95.cvq.business.request.RequestSeason;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.RequestStep;
 import fr.cg95.cvq.business.request.RequestType;
 import fr.cg95.cvq.business.request.Requirement;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.payment.ExternalAccountItem;
 import fr.cg95.cvq.business.users.payment.Payment;
 import fr.cg95.cvq.business.users.payment.PaymentState;
 import fr.cg95.cvq.business.users.payment.PurchaseItem;
 import fr.cg95.cvq.dao.IGenericDAO;
 import fr.cg95.cvq.dao.document.IDocumentDAO;
 import fr.cg95.cvq.dao.document.IDocumentTypeDAO;
 import fr.cg95.cvq.dao.request.IRequestActionDAO;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.dao.request.IRequestFormDAO;
 import fr.cg95.cvq.dao.request.IRequestNoteDAO;
 import fr.cg95.cvq.dao.request.IRequestTypeDAO;
 import fr.cg95.cvq.dao.users.IIndividualDAO;
 import fr.cg95.cvq.exception.CvqConfigurationException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.external.IExternalService;
 import fr.cg95.cvq.permission.CvqPermissionException;
 import fr.cg95.cvq.permission.PrivilegeDescriptor;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.authority.ICategoryService;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.authority.LocalAuthorityConfigurationBean;
 import fr.cg95.cvq.service.document.IDocumentService;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.request.IRequestServiceRegistry;
 import fr.cg95.cvq.service.users.ICertificateService;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 import fr.cg95.cvq.util.Critere;
 import fr.cg95.cvq.util.localization.ILocalizationService;
 import fr.cg95.cvq.util.mail.IMailService;
 
 
 /**
  * Partial implementation of the {@link IRequestService}, only provides functionalities
  * common to all request types.
  *
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public abstract class RequestService implements IRequestService {
 
     private static Logger logger = Logger.getLogger(RequestService.class);
 
     protected String fopConfig;
     protected String localReferentialFilename;
     protected String placeReservationFilename;
     protected String externalReferentialFilename;
     protected Boolean supportUnregisteredCreation;
     protected String subjectPolicy = SUBJECT_POLICY_NONE;
     protected String label;
     protected String xslFoFilename;
     protected Boolean isOfRegistrationKind;
     
     protected ICategoryService categoryService;
     protected IDocumentService documentService;
     protected IHomeFolderService homeFolderService;
     protected ICertificateService certificateService;
     protected IRequestServiceRegistry requestServiceRegistry;
     protected ILocalAuthorityRegistry localAuthorityRegistry;
     protected IMailService mailService;
     protected ILocalizationService localizationService;
     
     protected IGenericDAO genericDAO;
     protected IIndividualDAO individualDAO;
     protected IRequestDAO requestDAO;
     protected IRequestTypeDAO requestTypeDAO;
     protected IRequestNoteDAO requestNoteDAO;
     protected IDocumentDAO documentDAO;
     protected IDocumentTypeDAO documentTypeDAO;
     protected IRequestActionDAO requestActionDAO;
     protected IRequestFormDAO requestFormDAO;
 
     protected RequestWorkflowService requestWorkflowService;
     
     public RequestService() {
         super();
     }
 
     public void init() throws CvqConfigurationException {
         // register with the request registry
         requestServiceRegistry.registerService(this, getLabel());
 
         if (supportUnregisteredCreation == null)
             supportUnregisteredCreation = Boolean.FALSE;
     }
 
     private Critere getCurrentUserFilter() throws CvqException {
 
         Critere crit = new Critere();
         if (SecurityContext.isBackOfficeContext()) {
             Agent agent = SecurityContext.getCurrentAgent();
             Set agentCategoryRoles = agent.getCategoriesRoles();
             if (agentCategoryRoles == null || agentCategoryRoles.size() == 0)
                 return null;
             Iterator agentCategorysIt = agentCategoryRoles.iterator();
             StringBuffer sb = new StringBuffer();
             while (agentCategorysIt.hasNext()) {
                 CategoryRoles categoryRoles = (CategoryRoles) agentCategorysIt.next();
                 if (sb.length() > 0)
                     sb.append(",");
                 sb.append("'")
                     .append(categoryRoles.getCategory().getId())
                     .append("'");
             }
             crit.setAttribut("belongsToCategory");
             crit.setComparatif(Critere.EQUALS);
             crit.setValue(sb.toString());
         } else {
             Adult adult = SecurityContext.getCurrentEcitizen();
             crit.setAttribut(Request.SEARCH_BY_HOME_FOLDER_ID);
             crit.setComparatif(Critere.EQUALS);
             crit.setValue(adult.getHomeFolder().getId());
         }
     
         return crit;
     }
     
     public Set get(Set criteriaSet, final String orderedBy, final boolean onlyIds)
         throws CvqException {
 
         criteriaSet.add(getCurrentUserFilter());
         List results = null;
         results = requestDAO.search(criteriaSet, orderedBy, null, -1, 0, onlyIds);
 
         return new LinkedHashSet(results);
     }
 
     public Set extendedGet(Set<Critere> criteriaSet, final String sort, final String dir, 
             final int recordsReturned, final int startIndex)
         throws CvqException {
         
         if (criteriaSet == null)
             criteriaSet = new HashSet<Critere>();
         criteriaSet.add(getCurrentUserFilter());
         List results = null;
         results = requestDAO.search(criteriaSet, sort, dir, recordsReturned, startIndex, false);
         
         return new LinkedHashSet(results);
     }
     
    public Long getCount(Set<Critere> criteriaSet) throws CvqException {
        
        if (criteriaSet == null)
            criteriaSet = new HashSet<Critere>();
        criteriaSet.add(getCurrentUserFilter());

        return requestDAO.count(criteriaSet);
    }
    
     public Request getById(final Long id)
         throws CvqException, CvqObjectNotFoundException {
             return (Request) requestDAO.findById(Request.class, id, PrivilegeDescriptor.READ);
     }
 
     public Set<Request> getByIds(final Long[] ids)
     throws CvqException, CvqObjectNotFoundException {
         return new LinkedHashSet<Request>(requestDAO.listByIds(ids));
     }
 
     public Set getByRequesterId(final Long requesterId)
         throws CvqException {
 
         List results = null;
         results = requestDAO.listByRequester(requesterId);
         return new LinkedHashSet(results);
     }
 
     public Set getBySubjectId(final Long subjectId)
         throws CvqException, CvqObjectNotFoundException {
 
         logger.debug("getBySubjectId() searching requests for subject id " + subjectId);
 
         List results = null;
         results = requestDAO.listBySubject(subjectId);
         return new LinkedHashSet(results);
     }
 
     public Set getBySubjectIdAndRequestLabel(final Long subjectId, final String requestLabel, 
             boolean retrieveArchived)
         throws CvqException, CvqObjectNotFoundException {
 
     	if (requestLabel == null)
     		throw new CvqModelException("request.label_required");
     	
         logger.debug("getBySubjectIdAndRequestLabel() searching requests of type " 
                 + requestLabel + " for subject id " + subjectId);
 
         RequestState[] excludedStates = null;
         if (!retrieveArchived)
             excludedStates = new RequestState[] { RequestState.ARCHIVED };
         
         List results = null;
         results = requestDAO.listBySubjectAndLabel(subjectId, requestLabel, excludedStates);
         return new LinkedHashSet(results);        
     }
     
     public Set<Request> getByHomeFolderId(final Long homeFolderId) throws CvqException {
 		List<Request> results = null;
 		results = requestDAO.listByHomeFolder(homeFolderId);
 		return new LinkedHashSet<Request>(results);
 	}
 
     public Set getByHomeFolderIdAndRequestLabel(final Long homeFolderId, final String requestLabel)
         throws CvqException, CvqObjectNotFoundException {        
         List results = null;
         results = requestDAO.listByHomeFolderAndLabel(homeFolderId, requestLabel, null);
         return new LinkedHashSet(results);
     }
     
 
     public Set getByLastInterveningAgentId(final Long agentId)
         throws CvqException, CvqObjectNotFoundException {
         List results = null;
         results = requestDAO.listByLastInterveningAgentId(agentId);
         return new LinkedHashSet(results);
     }
 
     private void updateLastModificationInformation(Request request, final Date date)
         throws CvqException {
 
         // update request's last modification date
         if (date != null)
             request.setLastModificationDate(date);
         else
             request.setLastModificationDate(new Date());
 
         if (SecurityContext.isBackOfficeContext()) {
             Agent agent = SecurityContext.getCurrentAgent();
             if (agent == null)
                 throw new CvqException("No logged in agent !");
             request.setLastInterveningAgentId(agent.getId());
         } else if (SecurityContext.isAdminContext()) {
             request.setLastInterveningAgentId(Long.valueOf("-1"));
         }
 
         requestDAO.update(request);
     }
 
     public Set getNotes(final Long id)
         throws CvqException {
 
         List notesList = requestNoteDAO.listByRequest(id);
         return new LinkedHashSet(notesList);
     }
 
     public void addNote(final Long requestId, final RequestNoteType rtn,
                         final String note)
         throws CvqException, CvqObjectNotFoundException {
 
         Request request = getById(requestId);
 
         if (!SecurityContext.isBackOfficeContext())
             throw new CvqPermissionException(Request.class, request,
                                              PrivilegeDescriptor.WRITE);
         Agent agent = SecurityContext.getCurrentAgent();
         if (agent == null)
             throw new CvqPermissionException(Request.class, request,
                                              PrivilegeDescriptor.WRITE);
         Long agentId = agent.getId();
 
 	    RequestNote rn = new RequestNote();
         rn.setRequest(request);
         rn.setType(rtn);
         rn.setNote(note);
         rn.setAgentId(agentId);
 
 	    if (request.getNotes() == null) {
 	        Set notes = new HashSet();
 	        notes.add(rn);
 	        request.setNotes(notes);
     	} else {
 	        request.getNotes().add(rn);
 	    }
 
 
 	    requestNoteDAO.create(rn);
 	
         updateLastModificationInformation(request, null);
     }
 
     public void addDocuments(final Long requestId, final Set documentsId)
         throws CvqException, CvqObjectNotFoundException {
 
         // retrieve the document and the request to attach the document to
         Request request = getById(requestId);
         logger.debug("addDocuments() loaded request " + requestId);
 
         Iterator it = documentsId.iterator();
         while (it.hasNext()) {
             Long documentId = (Long) it.next();
             logger.debug("addDocuments() loading document : " + documentId);
             Document document = 
                 (Document) documentDAO.findById(Document.class, documentId, 
                         PrivilegeDescriptor.READ);
             if (request.getDocuments() == null) {
                 HashSet documentSet = new HashSet();
                 documentSet.add(document);
                 request.setDocuments(documentSet);
             } else {
                 request.getDocuments().add(document);
             }
         }
 
         updateLastModificationInformation(request, null);
     }
 
     public void addDocument(final Long requestId, final Long documentId)
         throws CvqException, CvqObjectNotFoundException {
 
         // retrieve the document and the request to attach the document to
         Request request = getById(requestId);
 
         Document document = 
             (Document) documentDAO.findById(Document.class, documentId, PrivilegeDescriptor.READ);
         if (request.getDocuments() == null) {
             Set documents = new HashSet();
             documents.add(document);
             request.setDocuments(documents);
         } else {
             request.getDocuments().add(document);
         }
 
         updateLastModificationInformation(request, null);
     }
 
     public Set getActions(final Long id)
         throws CvqException {
 
         List actionsList = requestActionDAO.listByRequest(id);
         return new LinkedHashSet(actionsList);
     }
 
     public void addAction(final Request request, final String label, final String note)
         throws CvqException {
         
         addActionTrace(label, note, new Date(), null, request, null);
     }
     
     public Set getAssociatedDocuments(final Long id)
         throws CvqException {
 
         logger.debug("getAssociatedDocuments() searching documents for request id : " + id);
 
         List documentsList = documentDAO.listByRequest(id);
         return new LinkedHashSet(documentsList);
     }
 
     public byte[] getCertificate(final Long id, final RequestState requestState)
         throws CvqException {
 
         RequestAction requestAction =
             requestActionDAO.findByRequestIdAndResultingState(id,requestState);
 
         if (requestAction != null)
             return requestAction.getFile();
         else
             return null;
     }
 
     public Set<RequestType> getAllRequestTypes()
         throws CvqException {
         List<RequestType> requestTypes = requestTypeDAO.listAll();
 
         if (SecurityContext.isAdminContext() || SecurityContext.isFrontOfficeContext())
             return new LinkedHashSet<RequestType>(requestTypes);
         
         // if agent is admin, return all categories ...
         Set<SiteRoles> agentSiteRoles = SecurityContext.getCurrentAgent().getSitesRoles();
         for (SiteRoles siteRole : agentSiteRoles) {
             if (siteRole.getProfile().equals(SiteProfile.ADMIN))
                 return new LinkedHashSet<RequestType>(requestTypes);
         }
         
         List<Category> agentCategories = categoryService.getAll();
         Set<RequestType> results = new LinkedHashSet<RequestType>();
         for (RequestType requestType : requestTypes) {
             Category rtCategory = requestType.getCategory();
             if (agentCategories.contains(rtCategory))
                 results.add(requestType);
         }
 
         return results;
     }
 
     public RequestType getRequestTypeById(final Long id)
         throws CvqException {
         return (RequestType) requestTypeDAO.findById(RequestType.class, id);
     }
 
     public void modifyRequestType(RequestType requestType)
         throws CvqException {
         requestTypeDAO.update(requestType);
     }
 
     public void modifyRequestTypeRequirements(RequestType requestType, Set requirements)
         throws CvqException {
 
         Iterator requirementsIt = requirements.iterator();
         while (requirementsIt.hasNext()) {
             Requirement requirement = (Requirement) requirementsIt.next();
             requirement.setRequestType(requestType);
         }
         requestType.setRequirements(requirements);
         requestTypeDAO.update(requestType);
     }
 
     public void addRequestTypeRequirement(Long requestTypeId, Long documentTypeId)
         throws CvqException {
         
         try {
             RequestType requestType = 
                 (RequestType) requestTypeDAO.findById(RequestType.class, requestTypeId);
             if (requestType.getRequirements() == null)
                 requestType.setRequirements(new HashSet<Requirement>());
             DocumentType documentType = 
                 (DocumentType) documentTypeDAO.findById(DocumentType.class, documentTypeId);
             Requirement requirement = new Requirement();
             requirement.setMultiplicity(Integer.valueOf("1"));
             requirement.setRequestType(requestType);
             requirement.setSpecial(false);
             requirement.setDocumentType(documentType);
             if (!requestType.getRequirements().contains(requirement)) {
                 requestType.getRequirements().add(requirement);
                 requestTypeDAO.update(requestType);
             }
         } catch (RuntimeException e) {
             e.printStackTrace();
             throw new CvqException("Could not update request type : " + e.toString());
         }        
     }
     
     public void removeRequestTypeRequirement(Long requestTypeId, Long documentTypeId)
         throws CvqException {
     
         try {
             RequestType requestType = 
                 (RequestType) requestTypeDAO.findById(RequestType.class, requestTypeId);
             if (requestType.getRequirements() == null)
                 return;
             DocumentType documentType = 
                 (DocumentType) documentTypeDAO.findById(DocumentType.class, documentTypeId);
             Requirement requirement = new Requirement();
             requirement.setRequestType(requestType);
             requirement.setDocumentType(documentType);
             if (requestType.getRequirements().remove(requirement))
                 requestTypeDAO.update(requestType);
         } catch (RuntimeException e) {
             e.printStackTrace();
             throw new CvqException("Could not update request type : " + e.toString());
         }        
     }
 
     /**
      * Get the list of seasons whose registrations are currently open.
      * 
      * @return null if no seasons defined for this request type, an empty list if no
      *  seasons with opened registrations, the list of seasons with opened registrations
      *  otherwise.
      */
     protected Set<RequestSeason> getOpenSeasons(RequestType requestType) {
         
         if (requestType.getSeasons() != null && !requestType.getSeasons().isEmpty()) {
             Date now = new Date();
             Set<RequestSeason> openSeasons = new HashSet<RequestSeason>();
             Set<RequestSeason> seasons = requestType.getSeasons();
             for (RequestSeason requestSeason : seasons) {
                 if (requestSeason.getRegistrationStart().before(now)
                         && requestSeason.getRegistrationEnd().after(now))
                     openSeasons.add(requestSeason);
             }
             
             return openSeasons;
         }
         
         return null;
     }
         
     public Set getRequestsTypesByCategory(final Long categoryId)
         throws CvqException {
         List requestTypes = null;            
         requestTypes = requestTypeDAO.listByCategory(categoryId);
         return new LinkedHashSet(requestTypes);
     }
 
     public RequestType getRequestTypeByLabel(final String requestLabel)
         throws CvqException {
         logger.debug("getRequestTypeByLabel() Searching for request label : " + requestLabel);
         return requestTypeDAO.findByName(requestLabel);
     }
 
     public Set getAllowedDocuments(final RequestType requestType)
         throws CvqException {
 
         Set requirements = requestType.getRequirements();
         if (requirements != null) {
             Map resultMap = new TreeMap();
             Set resultSet = new LinkedHashSet();
             Iterator requirementsIt = requirements.iterator();
             while (requirementsIt.hasNext()) {
                 Requirement requirement = (Requirement) requirementsIt.next();
                 resultMap.put(requirement.getDocumentType().getId(), requirement.getDocumentType());
                 resultSet.add(requirement.getDocumentType());
             }
             return new LinkedHashSet(resultMap.values());
         } else {
             return new HashSet();
         }
     }
 
     //////////////////////////////////////////////////////////
     // Season related methods
     //////////////////////////////////////////////////////////
 
     public void modifyRequestTypeSeasons(RequestType requestType, Set<RequestSeason> seasons)
         throws CvqException {
 
         IRequestService service = requestServiceRegistry.getRequestService(requestType.getLabel());
         if (!service.isOfRegistrationKind())
             throw new CvqModelException("request.season.unacceptable_request_type");
 
         // ensure we don't have more than two seasons since we don't yet know
         // how to handle more than two of them
         if (seasons.size() > 2)
             throw new CvqModelException("request.season.maximum_reached");
 
         // check validity of seasons data and check that registration dates
         // do not overlap
         Set<String> seasonsLabels = new HashSet<String>();
         Map<Date, Date> seasonsRegistrationDates = new TreeMap<Date, Date>();
         for (RequestSeason requestSeason : seasons) {
             if (requestSeason.getRegistrationStart() == null)
                 throw new CvqModelException("request.season.registration_start_required");
             if (requestSeason.getRegistrationStart().before(new Date()))
                 throw new CvqModelException("request.season.registration_start_is_over");
             if (requestSeason.getEffectStart() == null)
                 throw new CvqModelException("request.season.effect_dates_required");
             String seasonLabel = requestSeason.getLabel();
             if (seasonsLabels.contains(seasonLabel))
                 throw new CvqModelException("request.season.already_used_label");
             else
                 seasonsLabels.add(seasonLabel);
             seasonsRegistrationDates.put(requestSeason.getRegistrationStart(), 
                     requestSeason.getRegistrationEnd());
 
             requestSeason.setRequestType(requestType);
 
             if (requestSeason.getUuid() == null)
                 requestSeason.setUuid(UUID.randomUUID().toString());
         }
 
         Date lastRegistrationEndDate = null;
         boolean isFirst = true;
         for (Date startDate : seasonsRegistrationDates.keySet()) {
             if (isFirst) {
                 lastRegistrationEndDate = seasonsRegistrationDates.get(startDate);
                 isFirst = false;
             } else {
                 if (lastRegistrationEndDate == null) {
                     throw new CvqModelException("request.season.overlapping_registrations");
                 } else {
                     if (lastRegistrationEndDate.after(startDate))
                         throw new CvqModelException("request.season.overlapping_registrations");
                     else
                         lastRegistrationEndDate = seasonsRegistrationDates.get(startDate);
                 }
             }
         }
 
         // FIXME : performs checks on deleted seasons
 
         logger.debug("modifyRequestTypeSeasons() setting " + seasons.size() + " seasons");
         requestType.setSeasons(seasons);
         requestTypeDAO.update(requestType);
     }
     
     public boolean hasOpenSeasons(final Long requestTypeId) throws CvqException {
         RequestType requestType = getRequestTypeById(requestTypeId);
         Set<RequestSeason> openSeasons = getOpenSeasons(requestType);
         if (openSeasons == null || openSeasons.isEmpty())
             return false;
         else
             return true;
     }
     
     private void checkSeasonSupport(RequestType requestType) throws CvqModelException {
         IRequestService service = requestServiceRegistry.getRequestService(requestType.getLabel());
         if (!service.isOfRegistrationKind())
             throw new CvqModelException("request.season.not_supported");
     }
     
     private void checkSeasondDatesOverlapping(RequestSeason rs1, RequestSeason rs2) 
         throws CvqModelException {
         
         if (rs1.getRegistrationStart().before(rs2.getRegistrationEnd())
                 && rs1.getRegistrationEnd().after(rs2.getRegistrationEnd())
                 || rs1.getRegistrationEnd().after(rs2.getRegistrationStart())
                     && rs1.getRegistrationStart().before(rs2.getRegistrationStart()))
             throw new CvqModelException("request.season.seasons_registration_overlapped");
         
         if (rs1.getEffectStart().before(rs2.getEffectEnd())
                 && rs1.getEffectEnd().after(rs2.getEffectEnd())
                 || rs1.getEffectEnd().after(rs2.getEffectStart())
                     && rs1.getEffectStart().before(rs2.getEffectStart()))
             throw new CvqModelException("request.season.seasons_effect_overlapped");
     }
     
     private void checkSeasonValidity (Set<RequestSeason> seasons, RequestSeason requestSeason)
         throws CvqModelException {
         // Set now Date at 00h00:00 0000
         Calendar calendar = new GregorianCalendar();
         calendar.setTime(new Date());
         calendar.set(Calendar.HOUR, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         Date dayNow = calendar.getTime();
         
         // check validity of seasons data
         if (requestSeason.getRegistrationStart() == null)
             throw new CvqModelException("request.season.registration_start_required");
         if (requestSeason.getRegistrationEnd() == null)
             throw new CvqModelException("request.season.registration_end_required");
         if (requestSeason.getEffectStart() == null)
             throw new CvqModelException("request.season.effect_start_required");
         if (requestSeason.getEffectEnd() == null)
             throw new CvqModelException("request.season.effect_end_required");
 
         if (requestSeason.getUuid() == null)
             requestSeason.setUuid(UUID.randomUUID().toString());
         
         // check registrationt start
         if (requestSeason.getRegistrationStart().before(dayNow))
             throw new CvqModelException("request.season.registration_start_before_now");
         
         // check scheduling chronological respect
         if (!requestSeason.getRegistrationStart().before(requestSeason.getRegistrationEnd()))
             throw new CvqModelException("request.season.registration_start_after_registration_end");
         if (!requestSeason.getEffectStart().before(requestSeason.getEffectEnd()))
             throw new CvqModelException("request.season.effect_start_after_effect_end");
         
         // Registration and effect date overlapping policy
         if (!requestSeason.getRegistrationStart().before(requestSeason.getEffectStart()))
             throw new CvqModelException("request.season.registration_start_after_effect_start");
         if (!requestSeason.getRegistrationEnd().before(requestSeason.getEffectEnd()))
             throw new CvqModelException("request.season.registration_end_after_effect_end");
      
         // check season's registration dates do not overlap and season's effect dates to
         for(RequestSeason rs : seasons) {
             if (!requestSeason.getUuid().equals(rs.getUuid())) {
                 checkSeasondDatesOverlapping(requestSeason, rs);
             
                 // test the label uniqueness
                 if (rs.getLabel().equals(requestSeason.getLabel()))
                     throw new CvqModelException("request.season.already_used_label");
             }
             // This rules apply just for modification
             else {
                 if (rs.getEffectEnd().before(dayNow))
                     throw new CvqModelException("request.season.effect_ended");
                 if (rs.getRegistrationStart().before(dayNow) 
                         && ! rs.getRegistrationStart().equals(requestSeason.getRegistrationStart()))
                     throw new CvqModelException("request.season.registration_started");
                         
             }
         }
     }
     
     public void createRequestTypeSeasons(RequestType requestType, RequestSeason requestSeason)
             throws CvqException {
         
         checkSeasonSupport(requestType);
         Set<RequestSeason> seasons = requestType.getSeasons();
         checkSeasonValidity(seasons, requestSeason);
         
         requestSeason.setRequestType(requestType);
         seasons.add(requestSeason);
 
         try {
             requestTypeDAO.update(requestType);
         } catch (RuntimeException e) {
             throw new CvqException("Could not update request type : " + e.toString());
         } 
     }
   
     public void modifyRequestTypeSeasons(RequestType requestType, RequestSeason requestSeason)
         throws CvqException {
         
         checkSeasonSupport(requestType);
         
         IRequestService service = requestServiceRegistry.getRequestService(requestType.getLabel());
         if (!service.isOfRegistrationKind())
             throw new CvqModelException("request.season.unacceptable_request_type");
         
         Set<RequestSeason> seasons = requestType.getSeasons();
         checkSeasonValidity(seasons, requestSeason);
         
         Iterator<RequestSeason> it = seasons.iterator();
         while(it.hasNext()) {
             RequestSeason rs = it.next();
             if (rs.getUuid().equals(requestSeason.getUuid())){
                 it.remove();
             }
         }
         seasons.add(requestSeason);
 
         try {
             requestTypeDAO.update(requestType);
         } catch (RuntimeException e) {
             throw new CvqException("Could not update request type : " + e.toString());
         } 
     }
     
     public void removeRequestTypeSeasons(RequestType requestType, RequestSeason requestSeason) 
         throws CvqException {
     
         checkSeasonSupport(requestType);
         
         IRequestService service = requestServiceRegistry.getRequestService(requestType.getLabel());
         if (!service.isOfRegistrationKind())
             throw new CvqModelException("request.season.unacceptable_request_type");
         
         Set<RequestSeason> seasons = requestType.getSeasons();
         Iterator<RequestSeason> it = seasons.iterator();
         while(it.hasNext()) {
             RequestSeason rs = it.next();
             if (rs.getUuid().equals(requestSeason.getUuid()))
                 it.remove();
         }
         requestType.setSeasons(seasons);
     
         try {
             requestTypeDAO.update(requestType);
         } catch (RuntimeException e) {
             e.printStackTrace();
             throw new CvqException("Could not update request type : " + e.toString());
         }
     }
     
     public boolean isRegistrationOpen (final Long requestTypeId) throws CvqException {
         
         if (!isOfRegistrationKind())
             return true;
         
         RequestType requestType = getRequestTypeById(requestTypeId);
         Set<RequestSeason> openSeasons = getOpenSeasons(requestType);
         if (openSeasons == null)
             return true;
         if (openSeasons.isEmpty())
             return false;
         else
             return true;
     }
     
     public RequestSeason getRequestAssociatedSeason(Long requestId) throws CvqException {
        Request request = getById(requestId);
        
        RequestType requestType = request.getRequestType();
        
        if (requestType.getSeasons() != null) {
            Iterator<RequestSeason> it = requestType.getSeasons().iterator();
            while(it.hasNext()) {
                 RequestSeason rs = it.next();
                 if (rs.getUuid().equals(request.getSeasonUuid()))
                     return rs;
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
         Set<Request> requests = new HashSet<Request>();
         Map<String, List<PurchaseItem>> externalServicesToNotify = 
         	new HashMap<String, List<PurchaseItem>>();
         Iterator purchaseItemsIt = payment.getPurchaseItems().iterator();
         while (purchaseItemsIt.hasNext()) {
             PurchaseItem purchaseItem = (PurchaseItem) purchaseItemsIt.next();
             
             // if payment is validated and purchase item is managed by an external service, 
             // notify associated external service of the purchased item
             if (payment.getState().equals(PaymentState.VALIDATED)
                     && (purchaseItem instanceof ExternalAccountItem)) {
                 logger.debug("notifyPaymentResult() item managed by an external service : " 
                         + purchaseItem.getFriendlyLabel());
                 ExternalAccountItem externalAccountItem = (ExternalAccountItem) purchaseItem;
                 externalAccountItem.setSupportedBroker(payment.getBroker());
                 String externalServiceLabel = externalAccountItem.getExternalServiceLabel();
                 if (externalServicesToNotify.get(externalServiceLabel) == null) {
                     externalServicesToNotify.put(externalServiceLabel, 
                     		new ArrayList<PurchaseItem>());
                 }
                 List<PurchaseItem> externalServicesItems = 
                 	externalServicesToNotify.get(externalServiceLabel);
                 externalServicesItems.add(purchaseItem);
             }
             
             // if purchase item has a request id, notify the corresponding service
             if (purchaseItem.getRequest() != null)
                 requests.add(purchaseItem.getRequest());
         }
         
         if (!requests.isEmpty()) {
         	for (Request request : requests) {
                 IRequestService requestService = requestServiceRegistry.getRequestService(request);
                 if (payment.getState().equals(PaymentState.VALIDATED))
                     requestService.onPaymentValidated(request, payment.getBankReference());
                 else if (payment.getState().equals(PaymentState.CANCELLED))
                     requestService.onPaymentCancelled(request);
                 else if (payment.getState().equals(PaymentState.REFUSED))
                     requestService.onPaymentRefused(request);
             }
         }
         
         if (!externalServicesToNotify.isEmpty()) {
             LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
             for (String externalServiceLabel : externalServicesToNotify.keySet()) {
                 IExternalService service = 
                     (IExternalService) lacb.getExternalServiceByLabel(externalServiceLabel);
                 if (service == null) {
                     logger.error("notifyPayments() No external service with label " + 
                             externalServiceLabel + " has been found");
                     continue;
                 }
                 Collection purchaseItems = externalServicesToNotify.get(externalServiceLabel);
                 service.creditHomeFolderAccounts(purchaseItems, payment.getCvqReference(),
                         payment.getBankReference(), payment.getHomeFolder().getId(), 
                         payment.getCommitDate());
             }
         }
     }
     
     public boolean hasConsumptions(final String requestLabel)
         throws CvqException {
 
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         Set externalServices =
             lacb.getExternalServicesByRequestType(requestLabel);
         if (externalServices != null && externalServices.size() > 0) {
             logger.debug("hasConsumptions() got at least one external service for request type "
                          + requestLabel);
             return true;
         } else {
             logger.debug("hasConsumptions() no external service for request type " + requestLabel);
             return false;
         }
     }
 
     public Map getConsumptionsByRequest(final Long requestId, final Date dateFrom, 
             final Date dateTo)
         throws CvqException {
 
         logger.debug("getConsumptionsByRequest() request id " + requestId);
 
         Request request = null;
         request = getById(requestId);
 
         if (request.getState().equals(RequestState.ARCHIVED)) {
             logger.debug("getConsumptionsByRequest() Filtering archived request");
             return null;
         }
 
         Map resultMap = new LinkedHashMap();
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         Set externalServices =
             lacb.getExternalServicesByRequestType(request.getRequestType().getLabel());
         if (externalServices != null) {
             logger.debug("getConsumptionsByRequest() got " + externalServices.size() 
                     + " external services for request " + request.getRequestType().getLabel());
             Iterator externalServicesIt = externalServices.iterator();
             while (externalServicesIt.hasNext()) {
                 IExternalService service = (IExternalService) externalServicesIt.next();
                 logger.debug("getConsumptionsByRequest() calling service : " + service);
                 Map serviceConsumptionsMap =
                     service.getConsumptionsByRequest(request, dateFrom, dateTo);
                 if (serviceConsumptionsMap != null && serviceConsumptionsMap.size() > 0)
                     resultMap.putAll(serviceConsumptionsMap);
             }
         }
 
         return resultMap;
     }
 
     public String getConsumptionsField()
         throws CvqException {
         return null;
     }
 
     //////////////////////////////////////////////////////////
     // Workflow related methods
     //////////////////////////////////////////////////////////
 
     protected HomeFolder createOrSynchronizeHomeFolder(Request request) 
     		throws CvqException, CvqModelException {
     	
 		if (request.getRequester().getId() == null) {
 			if (supportUnregisteredCreation.booleanValue()) {
 				logger.debug("create() Gonna create implicit home folder");
 				HomeFolder homeFolder = homeFolderService.create(request.getRequester());
 				request.setHomeFolder(homeFolder);
 				
 	            SecurityContext.setCurrentEcitizen(request.getRequester());
 
 	            return homeFolder;
 			} else {
 				logger.error("create() refusing creation by unregistered user");
 				throw new CvqModelException("Service does not support creation by unregistered users !");
 			}
 		} else {
 			logger.debug("create() Adult already exists, re-synchronizing it with DB");
 			// resynchronize requester with our model object in order to have a fully filled adult object
 			Adult adult = (Adult) genericDAO.findById(Adult.class, request.getRequester().getId());
 			request.setRequester(adult);
 			request.setHomeFolder(adult.getHomeFolder());
 		}
 		
 		return null;
     }
 
     protected void notifyRequestCreation(Request request, byte[] pdfData) 
         throws CvqException {
         
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         if (request.getRequester().getEmail() != null
                 && !request.getRequester().getEmail().equals("")) {
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
                     mailService.send(null, request.getRequester().getEmail(), null, 
                             mailSubject.toString(), mailDataBody, pdfData, "Recu_Demande.pdf");
                 } else {
                     mailService.send(null, request.getRequester().getEmail(), null, 
                             mailSubject.toString(), mailDataBody);
                 }
             }
         }        
     }
     
     /**
      * Default implementation of the POJO-based creation method. It is suitable
      * for requests types that do not have any specific stuff to perform upon
      * creation of a new request instance. 
      */
     public Long create(Request request, Long requesterId) 
         throws CvqException, CvqObjectNotFoundException {
         
         initializeCommonAttributes(request, requesterId);
         return create(request);
     }
 
     protected Long create(final Request request)
         throws CvqException {
 
         Long requestId = requestDAO.create(request);
 
         logger.debug("create() Gonna generate a pdf of the request");
         byte[] pdfData =
             certificateService.generateRequestCertificate(request, this.fopConfig);
         addActionTrace(CREATION_ACTION, null, new Date(), RequestState.PENDING, request, pdfData);
 
         notifyRequestCreation(request, pdfData);
         
         return requestId;
     }
 
     protected void validateXmlData(XmlObject xmlObject) {
 //        logger.debug("validateXmlData() gonna validate object of type : " 
 //                + xmlObject.getClass().getName());
 //        logger.debug("validateXmlData() gonna validate data : " + xmlObject);
         ArrayList validationErrors = new ArrayList();
         XmlOptions options = new XmlOptions();
         options.setErrorListener(validationErrors);
         boolean isValid = xmlObject.validate(options);
         if (!isValid) {
             Iterator iter = validationErrors.iterator();
             while (iter.hasNext()) {
                 logger.warn("validateXmlData() Error : " + iter.next());
             }
         }
     }
     
     public Node getRequestClone(final Long subjectId, Long homeFolderId, final String requestLabel) 
         throws CvqException {
 
     	if (requestLabel == null)
     		throw new CvqModelException("request.label_required");
 //    	if (subjectId == null && homeFolderId == null)
 //    		throw new CvqModelException("request.subject_or_home_folder_required");
     	
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
             		&& tempRequestService.getSubjectPolicy() != SUBJECT_POLICY_NONE) {
             	Object subject = genericDAO.findById(Individual.class, subjectId);
             	checkSubjectPolicy(subject, tempRequestService.getSubjectPolicy());
             	request.setSubject(subject);
             }
         } else {
             // We choose the last resquest version for cloning, based on CreationDate
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
 
 //            logger.debug("getRequestClone() original request " + xmlRequest.toString());
 
             Method copyMethod = xmlRequest.getClass().getMethod("copy", parameterTypes);
             XmlObject xmlRequestCopy = (XmlObject) copyMethod.invoke(xmlRequest, arguments);
 
             String xmlRequestCopyClass = xmlRequestCopy.getClass().getSimpleName();
             String getBodyMethod = "get" + xmlRequestCopyClass.replace("DocumentImpl", "");
 
             Method xmlRequestGetBody = 
                 xmlRequestCopy.getClass().getMethod(getBodyMethod, parameterTypes);
             fr.cg95.cvq.xml.common.RequestType xmlRequestType =
                 (fr.cg95.cvq.xml.common.RequestType) xmlRequestGetBody.invoke(xmlRequestCopy, arguments);
             purgeClonedRequest(xmlRequestType);
 
 //            logger.debug("getRequestClone() clone request " + xmlRequestCopy.toString());
             
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
     
     public void modify(final Request request)
         throws CvqException {
 
         if (request != null) {
             updateLastModificationInformation(request, null);
         }
     }
 
     protected void delete(final Request request)
         throws CvqException, CvqObjectNotFoundException {
         
         requestDAO.delete(request);
 		if (request.getHomeFolder().getBoundToRequest().booleanValue()) {
 			logger.debug("delete() Home folder belongs to request, removing it from DB");
 			homeFolderService.delete(request.getHomeFolder());
 		}
     }
 
     public void delete(final Long id)
         throws CvqException, CvqObjectNotFoundException {
 
         logger.debug("delete() Gonna delete request object with id : " + id);
 
         Request request = getById(id);
         delete(request);
     }
 
     public Map<Object, Set<RequestSeason>> getAuthorizedSubjects(final Long homeFolderId)
         throws CvqException, CvqObjectNotFoundException {
         
         RequestType requestType = requestTypeDAO.findByName(getLabel());
         logger.debug("getAuthorizedSubjects() searching authorized subjects for : "
                 + requestType.getLabel());
         
         Set<RequestSeason> openSeasons = getOpenSeasons(requestType);
         if (openSeasons != null) {
             if (openSeasons.isEmpty()) {
                 return null;
             } else {
                 Set<Object> eligibleSubjects = getEligibleSubjects(homeFolderId);
                 Map<Object, Set<RequestSeason>> result =
                     new HashMap<Object, Set<RequestSeason>>();
                 for (Object object : eligibleSubjects)
                     result.put(object, openSeasons);
                 if (requestType.getAuthorizeMultipleRegistrationsPerSeason()) {
                     // no restriction on registrations per season
                     // just return the whole map
                     return result;
                 } else {
                     for (RequestSeason season : openSeasons) {
                         List<Request> seasonRequests =
                             requestDAO.listByHomeFolderAndSeason(homeFolderId, season.getUuid());
                         for (Request request : seasonRequests) {
                             Set<RequestSeason> subjectSeasons = null;
                             if (getSubjectPolicy().equals(SUBJECT_POLICY_NONE))
                                 subjectSeasons = result.get(request.getHomeFolder());
                             else
                                 subjectSeasons = result.get(request.getSubject());
                             if (subjectSeasons == null)
                                 continue;
                             else if (subjectSeasons.size() == 1)
                                 if (getSubjectPolicy().equals(SUBJECT_POLICY_NONE))
                                     result.remove(request.getHomeFolder());
                                 else
                                     result.remove(request.getSubject());
                             else
                                 subjectSeasons.remove(season);
                         }
                     }
                     return result;
                 }
             }
         } else {
             // TODO : for now, we deny concurrent requests on a given request type
             //        add this feature to request types
             Set<Object> eligibleSubjects = getEligibleSubjects(homeFolderId);
             Map<Object, Set<RequestSeason>> result =
                 new HashMap<Object, Set<RequestSeason>>();
             for (Object object : eligibleSubjects)
                 result.put(object, new HashSet<RequestSeason>());
             RequestState[] excludedStates = 
                 requestWorkflowService.getStatesExcludedForRunningRequests();
             List<Request> homeFolderRequests = 
                 requestDAO.listByHomeFolderAndLabel(homeFolderId, getLabel(), excludedStates);
             if (getSubjectPolicy().equals(SUBJECT_POLICY_NONE)) {
                 if (!homeFolderRequests.isEmpty()) {
                     return null;
                 } else {
                     return result;
                 }
             } else {
                 for (Request request : homeFolderRequests)
                     result.remove(request.getSubject());
                 return result;
             }
         }
     }
 
     //////////////////////////////////////////////////////////
     // Request Workflow related methods
     //////////////////////////////////////////////////////////
     
     
     // Request data state treatment
     /////////////////////////////////////////////////////////
     
     public void updateRequestDataState(final Long id, final DataState rs)
             throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
         if (rs.equals(DataState.VALID))
             validData(id);
         else if (rs.equals(DataState.INVALID))
             invalidData(id);
     }
     
     private void validData(final Long id)
             throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
         Request request = getById(id);
         requestWorkflowService.validData(request);
     }
     
     private void invalidData(final Long id)
             throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
         Request request = getById(id);
         requestWorkflowService.invalidData(request);
     }
     
     
     // Request state treatment
     // TODO : make workflow method private - migrate unit tests
     /////////////////////////////////////////////////////////
     
     public void updateRequestState(final Long id, final RequestState rs, final String motive)
             throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
         if (rs.equals(RequestState.COMPLETE))
             complete(id);
         else if (rs.equals(RequestState.UNCOMPLETE))
             specify(id, motive);
         else if (rs.equals(RequestState.REJECTED))
             reject(id, motive);
         else if (rs.equals(RequestState.CANCELLED))
             cancel(id);
         else if (rs.equals(RequestState.VALIDATED))
             validate(id);
         else if (rs.equals(RequestState.NOTIFIED))
             notify(id, motive);
         else if (rs.equals(RequestState.ACTIVE))
             activate(id);
         else if (rs.equals(RequestState.EXPIRED))
             expire(id);
         else if (rs.equals(RequestState.CLOSED))
             close(id);
         else if (rs.equals(RequestState.ARCHIVED))
             archive(id);
     }
     
     public void complete(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         complete(request);
     }
     
     public void complete(Request request)
         throws CvqException, CvqInvalidTransitionException {
         
         requestWorkflowService.complete(request);
     }
 
     public void specify(final Long id, final String motive)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         specify(request, motive);
     }
 
     public void specify(final Request request, final String motive)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         requestWorkflowService.specify(request, motive);
     }
     
     protected void validateAssociatedDocuments(final Set documentSet)
         throws CvqException {
 
         Iterator documentSetIt = documentSet.iterator();
         while (documentSetIt.hasNext()) {
             Document doc = (Document) documentSetIt.next();
             documentService.validate(doc.getId(), new Date(), "Automatic validation");
         }
     }
 
     /**
 	 * Do the real work of validating a request.
      * 
      * <ul>
      *   <li>change request's state and step</li>
      *   <li>generate a PDF certificate is asked for</li>
      *   <li>validate home folder if created along the request</li>
 	 *   <li>validate associated documents</li>
      *   <li>notify associated external services</li>
      *   <li>send notification email to e-citizen if notification enabled for this request</li>
      * </ul>
 	 * 
 	 * @param request the request to be validated
 	 * @param generateCertificate
 	 *            whether or not we want the request's certificate to be
 	 *            generated (some requests have to add extra information in the
 	 *            certificate and so they call directly the
 	 *            {@link ICertificateService})
 	 */
     protected void validate(final Request request, final boolean generateCertificate)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         requestWorkflowService.validate(request, generateCertificate, this.fopConfig);
         
         validateAssociatedDocuments(getAssociatedDocuments(request.getId()));
 
 		if (request.getHomeFolder().getOriginRequestId().longValue() == request.getId().longValue()) {
 			logger.debug("validate() Home folder has been created along the request, validating it");
 			homeFolderService.validate(request.getHomeFolder());
 		}
 
 		// send request data to interested external services
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         String requestTypeLabel = request.getRequestType().getLabel();
         Set<IExternalService> externalServices = 
             lacb.getExternalServicesByRequestType(requestTypeLabel);
         if (externalServices != null) {
             for (IExternalService externalService : externalServices)
                 externalService.sendRequest(request);
         }
         
         // send notification to ecitizen if enabled
         if (lacb.hasEcitizenValidationNotification(requestTypeLabel)
                 && (request.getRequester().getEmail() != null
                         && !request.getRequester().getEmail().equals(""))) {
             String mailData = lacb.getEcitizenValidationNotificationData(requestTypeLabel, 
                     "mailData");
             Boolean attachPdf = 
                 Boolean.valueOf(lacb.getEcitizenValidationNotificationData(requestTypeLabel, 
                         "attachPdf"));
             String mailDataBody = 
                 localAuthorityRegistry.getBufferedCurrentLocalAuthorityResource(
                         ILocalAuthorityRegistry.TXT_ASSETS_RESOURCE_TYPE, mailData, false);
 
             if (mailDataBody == null) {
                 logger.warn("validate() local authority has activated ecitizen notification for request type "
                         + requestTypeLabel + " but has no mail data for it !");
                 return;
             }
             
             byte[] pdfData = null;
             if (attachPdf.booleanValue()) {
                 pdfData = getCertificate(request.getId(), RequestState.VALIDATED);
                 if (pdfData == null)
                     pdfData = certificateService.generateRequestCertificate(request, this.fopConfig);
             }
             
             StringBuffer mailSubject = new StringBuffer();
             mailSubject.append("[").append(lacb.getDisplayTitle()).append("] ")
                 .append(localizationService.getRequestLabelTranslation(request.getClass().getName(), "fr", false))
                 .append(" validée");
             
             if (attachPdf.booleanValue()) {
                 mailService.send(null, request.getRequester().getEmail(), null, 
                         mailSubject.toString(), mailDataBody, pdfData, "Attestation_Demande.pdf");
             } else {
                 mailService.send(null, request.getRequester().getEmail(), null, 
                         mailSubject.toString(), mailDataBody);
             }
         }
     }
 
     public void validate(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         validate(request);
     }
 
     public void validate(final Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         validate(request, true);
         validateXmlData(request.modelToXml());
     }
 
     public void notify(final Long id, final String motive)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         notify(request, motive);
     }
     
     public void notify(Request request, final String motive)
         throws CvqException, CvqInvalidTransitionException {
     
         requestWorkflowService.notify(request, motive);
         
         // automatically switch to ACTIVE state if request type is of registration kind
         // and has activated the mechanism. 
         // this is to avoid agents one more manual state's change.
         RequestType requestType = request.getRequestType();
         if (isOfRegistrationKind() 
                 && requestType.getHasAutomaticActivation()
                 && requestType.getSeasons() == null)
             activate(request);
     }
 
     public void activate(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         activate(request);
     }
 
     public void activate(final Request request)
         throws CvqException, CvqInvalidTransitionException {
         
         requestWorkflowService.activate(request);
     }
 
     public void expire(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         expire(request);
     }
     
     public void expire(final Request request)
         throws CvqException, CvqInvalidTransitionException {
         
         requestWorkflowService.expire(request);
     }
     
     public void cancel(final Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         requestWorkflowService.cancel(request);
         
 		if (request.getHomeFolder().getOriginRequestId().longValue() == request.getId().longValue()) {
 			logger.debug("cancel() Home folder has been created along the request, invalidating it");
 			homeFolderService.invalidate(request.getHomeFolder());
 		}
     }
 
     public void cancel(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         cancel(request);
     }
 
     public void reject(final Request request, final String motive)
         throws CvqException, CvqInvalidTransitionException {
 
         requestWorkflowService.reject(request, motive);
         
 		if (request.getHomeFolder().getOriginRequestId().longValue() == request.getId().longValue()) {
 			logger.debug("reject() Home folder has been created along the request, invalidating it");
 			homeFolderService.invalidate(request.getHomeFolder());
 		}
     }
 
     public void reject(final Long id, final String motive)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         reject(request,motive);
     }
 
     public void close(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         close(request);
     }
     
     public void close(Request request)
         throws CvqException, CvqInvalidTransitionException {
     
         requestWorkflowService.close(request);
     }
 
     public void archive(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         archive(request);
     }
 
     public void archive(final Request request)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         requestWorkflowService.archive(request);
         
 		if (request.getHomeFolder().getBoundToRequest()) {
 			logger.debug("archive() Home folder belongs to request, archiving it");
 			homeFolderService.archive(request.getHomeFolder());
 		}
     }
     
     public void archiveHomeFolderRequests(HomeFolder homeFolder)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
     
         Set<Request> requests = getByHomeFolderId(homeFolder.getId());
         if (requests == null || requests.isEmpty()) {
             logger.debug("archiveHomeFolderRequests() no requests associated to home folder "
                     + homeFolder.getId());
             return;
         }
         
         for (Request request : requests) {
             request.setState(RequestState.ARCHIVED);
             Date date = new Date();
             updateLastModificationInformation(request, date);
             addActionTrace(STATE_CHANGE_ACTION, null, date, RequestState.ARCHIVED, request, null);
         }
     }
     
     public RequestState[] getPossibleTransitions(RequestState rs) {
 
         return requestWorkflowService.getPossibleTransitions(rs);
     }
     
     public DataState[] getPossibleTransitions(DataState ds) {        
         List<DataState> dataStateList = new ArrayList<DataState>();
 
         if (ds.equals(DataState.PENDING)) {
             dataStateList.add(DataState.VALID);
             dataStateList.add(DataState.INVALID);
         }
         return (DataState[]) dataStateList.toArray(new DataState[0]);
     }
 
     public Set<RequestState> getStatesBefore(RequestState rs) {
     
         return requestWorkflowService.getStatesBefore(rs);
     }
     
     protected void addActionTrace(final String label, final String note, final Date date,
             final RequestState resultingState, final Request request, final byte[] pdfData)
         throws CvqException {
 
         // retrieve user or agent id according to context
         Long userId = null;
         if (request instanceof VoCardRequest) {
             VoCardRequest vocr = (VoCardRequest) request;
             // there can't be a logged in user at VO card request creation time
             userId = vocr.getHomeFolder().getHomeFolderResponsible().getId();
         } else {
             userId = SecurityContext.getCurrentUserId();
         }
         
         RequestAction requestAction = new RequestAction();
         requestAction.setAgentId(userId);
         requestAction.setLabel(label);
         requestAction.setNote(note);
         requestAction.setDate(date);
         requestAction.setResultingState(resultingState);
         requestAction.setRequest(request);
         requestAction.setFile(pdfData);
         requestActionDAO.create(requestAction);
     }
 
     protected void addCertificateToActionTrace(final Request request,
                                                final RequestState requestState,
                                                byte[] pdfData)
         throws CvqException {
 
         RequestAction requestAction =
             requestActionDAO.findByRequestIdAndResultingState(request.getId(),
                                                               requestState);
         requestAction.setFile(pdfData);
         requestActionDAO.update(requestAction);
     }
 
     protected void initializeCommonAttributes(final Request request, final Long requesterId)
         throws CvqException, CvqObjectNotFoundException {
 
         // create the association with the requester
         Individual somebody = null;
         try {
             somebody = (Individual) individualDAO.findById(Individual.class, requesterId);
         } catch (CvqPermissionException e) {
             // can't happen
         }
         if (somebody instanceof Child)
             throw new CvqObjectNotFoundException("The provided requester id does not match an adult object !");
         Adult requester = (Adult) somebody;
         request.setRequester(requester);
 		request.setHomeFolder(requester.getHomeFolder());
 
         initializeCommonAttributes(request);
     }
 
     protected void initializeCommonAttributes(final Request request) 
         throws CvqException {
 
         logger.debug("initializeCommonAttributes() checking respect for policy "
                 + getSubjectPolicy());
         
         checkSubjectPolicy(request.getSubject(), getSubjectPolicy());
         
         // check that request's subject is allowed to issue this request
         if (!getSubjectPolicy().equals(SUBJECT_POLICY_NONE)) {
             Individual individual = (Individual) request.getSubject();
             boolean isAuthorized = false;
             Map<Object, Set<RequestSeason>> authorizedSubjectsMap =
                 getAuthorizedSubjects(request.getHomeFolder().getId());
             if (authorizedSubjectsMap != null) {
                 Set<Object> authorizedSubjects = 
                     getAuthorizedSubjects(request.getHomeFolder().getId()).keySet();
                 for (Object authorizedSubject : authorizedSubjects) {
                     Individual authorizedIndividual = (Individual) authorizedSubject;
                     if (authorizedIndividual.getId().equals(individual.getId())) {
                         isAuthorized = true;
                         break;
                     }
                 }
             }
             if (!isAuthorized)
                 throw new CvqModelException("request.subject_not_authorized");
             
             request.setSubject((Individual) genericDAO.findById(Individual.class, 
             		individual.getId()));
         }
         
         RequestType requestType = getRequestTypeByLabel(getLabel());
         Set<RequestSeason> openSeasons = getOpenSeasons(requestType);
         // FIXME : we don't yet manage overlapping seasons so take the first one
         if (openSeasons != null) {
             if (openSeasons.isEmpty())
                 throw new CvqModelException("request.seasons.no_open_registrations");
             request.setSeasonUuid(openSeasons.iterator().next().getUuid());
         }
         request.setRequestType(requestType);
         request.setState(RequestState.PENDING);
         request.setDataState(DataState.PENDING);
         request.setStep(RequestStep.INSTRUCTION);
         request.setCreationDate(new Date());
         request.setOrangeAlert(Boolean.FALSE);
         request.setRedAlert(Boolean.FALSE);
 	}
 
     /**
      * Check that a request's subject is of the good type with respect to
      * the given policy.
      * 
      * @throws CvqModelException if there's a policy violation
      */
     private void checkSubjectPolicy(final Object subject, final String policy)
     	throws CvqModelException {
     	
         if (!policy.equals(SUBJECT_POLICY_NONE)) {
             logger.debug("checkSubjectPolicy() subject is " + subject);
             if (subject == null) 
                 throw new CvqModelException("model.request.subject_is_required");
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
             if (subject != null)
                 throw new CvqModelException("model.request.subject_not_supported");
         }
     }
     
     /**
      * Get the list of eligible subjects for the current request service. Does not make
      * any control on already existing requests. 
      */
     private Set<Object> getEligibleSubjects(final Long homeFolderId) 
         throws CvqException {
         
         if (getSubjectPolicy().equals(SUBJECT_POLICY_NONE)) {
             Set<Object> result = new HashSet<Object>();
             result.add(homeFolderService.getById(homeFolderId));
             return result;
         } else {
             Set individualsReference = null;
             if (getSubjectPolicy().equals(SUBJECT_POLICY_INDIVIDUAL))
                 individualsReference = homeFolderService.getById(homeFolderId).getIndividuals();
             else if (getSubjectPolicy().equals(SUBJECT_POLICY_ADULT))
                 individualsReference = homeFolderService.getAdults(homeFolderId);
             else if (getSubjectPolicy().equals(SUBJECT_POLICY_CHILD))
                 individualsReference = homeFolderService.getChildren(homeFolderId);
             
             return individualsReference;
         }
     }
     
     //////////////////////////////////////////////////////////
     // RequestForm related Methods
     //////////////////////////////////////////////////////////
     
     /**
      * Generate requestForm asset resource name
      */
     private String generateAssetRessourceName (String requestTypeLabel, 
             String requestFormLabel, String filename) {
         String generatedName =  requestTypeLabel 
             + "_" + RequestFormType.REQUEST_MAIL_TEMPLATE.toString()
             + "_" + requestFormLabel;
             generatedName = generatedName.trim().replace(' ', '_');
         
         // get file extension
         if (filename != null) {
             String[] splitFilename = filename.split("\\.");
             if (splitFilename.length > 1)
                 generatedName = generatedName + "."  + splitFilename[splitFilename.length - 1];
         } else
             generatedName = generatedName + ".xsl-fo";
         
         return generatedName;
     }
     
     /**
      * Check requestForm's labels uniqueness for given RequesType and RequestFormType
      */
     private void checkRequestFormLabelUniqueness(String label, String shortLabel, 
             RequestFormType requestFormType, Long requestTypeId,
             Long requestFormId) throws CvqModelException {
         List<RequestForm> requestFormList = 
             requestFormDAO.findByTypeAndRequestTypeId(requestFormType, requestTypeId);
         for (RequestForm requestForm : requestFormList) {
             if (!requestForm.getId().equals(requestFormId)) {
                 if (requestForm.getLabel().equals(label))
                     throw new CvqModelException("requestForm.label_already_used");
                 if (requestForm.getShortLabel().equals(shortLabel))
                     throw new CvqModelException("requestForm.shortLabel_already_used");
             }
         }
     }
     
     public void addRequestTypeForm(final Long requestTypeId, RequestFormType requestFormType, 
             String label, String shortLabel, String filename, byte[] data)
         throws CvqException {
         checkRequestFormLabelUniqueness(
                 label, shortLabel, requestFormType, requestTypeId, new Long(-1));
         
         RequestForm requestForm = new RequestForm();
         RequestType requestType = 
             (RequestType) requestTypeDAO.findById(RequestType.class, requestTypeId);
         
         Set<RequestType> requestTypesSet = new HashSet<RequestType>();
         requestTypesSet.add(requestType);
         requestForm.setRequestTypes(requestTypesSet);
         requestType.getForms().add(requestForm);
         
         if (label != null)
             requestForm.setLabel(label);
         else
             throw new CvqModelException("requestForm.label_is_null");
         if (shortLabel != null)
             requestForm.setShortLabel(shortLabel);
         else
             throw new CvqModelException("requestForm.shortLabel_is_null");
         
         requestForm.setType(requestFormType);
         
         if (requestForm.getType().equals(RequestFormType.REQUEST_MAIL_TEMPLATE))
             requestForm.setXslFoFilename(
                     generateAssetRessourceName(requestType.getLabel(),
                             requestForm.getShortLabel(), filename));
         else
             requestForm.setXslFoFilename(filename);
       
         localAuthorityRegistry.saveLocalAuthorityResource(ILocalAuthorityRegistry.XSL_RESOURCE_TYPE, 
                 requestForm.getXslFoFilename(), data);
       
         requestFormDAO.create(requestForm);
     }
     
     public void modifyRequestTypeForm (Long requestTypeId, Long requestFormId, 
             String newLabel, String newShortLabel, String newFilename, byte[] newData) 
             throws CvqException {
         
         RequestForm requestForm = (RequestForm)genericDAO.findById(RequestForm.class, requestFormId);
         RequestType requestType = 
             (RequestType) requestTypeDAO.findById(RequestType.class, requestTypeId);
         
         checkRequestFormLabelUniqueness(
                 newLabel, newShortLabel, requestForm.getType(), requestTypeId, requestFormId);
 
         if (newLabel != null)
             requestForm.setLabel(newLabel);
         
         if (newShortLabel != null) {
             String oldFilename = requestForm.getXslFoFilename();
             
             requestForm.setShortLabel(newShortLabel);
             if (requestForm.getType().equals(RequestFormType.REQUEST_MAIL_TEMPLATE))
                 requestForm.setXslFoFilename(
                         generateAssetRessourceName(requestType.getLabel(),
                                 requestForm.getShortLabel(), newFilename));
             else if (newFilename != null)
                 requestForm.setXslFoFilename(newFilename);
             
             if (newData == null)
                 localAuthorityRegistry.renameLocalAuthorityResource(
                         ILocalAuthorityRegistry.XSL_RESOURCE_TYPE, oldFilename, 
                         requestForm.getXslFoFilename());
         }
         if (newData != null)
             localAuthorityRegistry.saveLocalAuthorityResource(
                     ILocalAuthorityRegistry.XSL_RESOURCE_TYPE, 
                     requestForm.getXslFoFilename(), newData);
         
         requestFormDAO.update(requestForm);
     }
 
     public void removeRequestTypeForm(final Long requestTypeId, final Long requestFormId)
         throws CvqException {     
         RequestType requestType = 
             (RequestType) requestTypeDAO.findById(RequestType.class, requestTypeId);
         RequestForm requestForm =
             (RequestForm) genericDAO.findById(RequestForm.class, requestFormId);
         requestType.getForms().remove(requestForm);
       
         localAuthorityRegistry.removeLocalAuthorityResource(
                 ILocalAuthorityRegistry.XSL_RESOURCE_TYPE,
                 requestForm.getXslFoFilename());
         
         requestFormDAO.delete(requestForm);
     }
 
     public Set<RequestForm> getRequestTypeForms(Long requestTypeId, 
             RequestFormType requestFormType) throws CvqException {
        
         List<RequestForm> requestFormList = 
             requestFormDAO.findByTypeAndRequestTypeId(requestFormType, requestTypeId);
         
         Set<RequestForm> requestFormSet = new HashSet<RequestForm>();
         for (RequestForm requestForm : requestFormList)
             requestFormSet.add(requestForm);
         
         return requestFormSet;
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
 
     public void setIndividualDAO(IIndividualDAO individualDAO) {
         this.individualDAO = individualDAO;
     }
 
     public void setDocumentDAO(IDocumentDAO documentDAO) {
         this.documentDAO = documentDAO;
     }
 
     public void setRequestTypeDAO(IRequestTypeDAO requestTypeDAO) {
         this.requestTypeDAO = requestTypeDAO;
     }
 
     public void setRequestActionDAO(IRequestActionDAO requestActionDAO) {
         this.requestActionDAO = requestActionDAO;
     }
 
     public void setRequestNoteDAO(IRequestNoteDAO requestNoteDAO) {
         this.requestNoteDAO = requestNoteDAO;
     }
     
     public void setRequestFormDAO(IRequestFormDAO requestFormDAO) {
         this.requestFormDAO = requestFormDAO;
     }
 
     public void setDocumentService(IDocumentService documentService) {
         this.documentService = documentService;
     }
 
     public void setHomeFolderService(IHomeFolderService homeFolderService) {
         this.homeFolderService = homeFolderService;
     }
 
     public void setCertificateService(ICertificateService certificateService) {
         this.certificateService = certificateService;
     }
 
     public void setRequestServiceRegistry(IRequestServiceRegistry requestServiceRegistry) {
         this.requestServiceRegistry = requestServiceRegistry;
     }
 
     public void setFopConfig(String fopConfig) {
         this.fopConfig = fopConfig;
     }
 
     public String getLabel() {
         if (label != null)
             return this.label;
         else
             return "";
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
 
 	public void setLocalizationService(ILocalizationService localizationService) {
 		this.localizationService = localizationService;
 	}
 
     public void setRequestWorkflowService(RequestWorkflowService requestWorkflowService) {
         this.requestWorkflowService = requestWorkflowService;
     }
 
     public void setCategoryService(ICategoryService categoryService) {
         this.categoryService = categoryService;
     }
 }
