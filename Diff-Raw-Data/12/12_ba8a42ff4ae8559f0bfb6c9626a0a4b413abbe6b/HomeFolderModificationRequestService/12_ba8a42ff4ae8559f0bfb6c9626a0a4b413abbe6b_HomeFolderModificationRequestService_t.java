 package fr.cg95.cvq.service.request.ecitizen.impl;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestSeason;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.ecitizen.HomeFolderModificationRequest;
 import fr.cg95.cvq.business.users.ActorState;
 import fr.cg95.cvq.business.users.Address;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.CreationBean;
 import fr.cg95.cvq.business.users.HistoryEntry;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.IndividualRole;
 import fr.cg95.cvq.business.users.RoleType;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.hibernate.HistoryInterceptor;
 import fr.cg95.cvq.dao.users.IHistoryEntryDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.request.ecitizen.IHomeFolderModificationRequestService;
 import fr.cg95.cvq.service.request.impl.RequestService;
 
 /**
  * Implementation of the home folder modification request service.
  *
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public class HomeFolderModificationRequestService
     extends RequestService implements IHomeFolderModificationRequestService {
 
     static Logger logger =
         Logger.getLogger(HomeFolderModificationRequestService.class);
 
     protected IHistoryEntryDAO historyEntryDAO;
     protected HistoryInterceptor historyInterceptor;
 
     @Override
     public HomeFolderModificationRequest create(final Long homeFolderId, final Long requesterId)
         throws CvqException, CvqObjectNotFoundException {
 
         // load home folder first to check for the existence of another
         // similar request in progress
         HomeFolder homeFolder = homeFolderService.getById(homeFolderId);
         checkIsAuthorized(homeFolder);
         
         HomeFolderModificationRequest hfmr = new HomeFolderModificationRequest();
         hfmr.setHomeFolderId(homeFolderId);
         hfmr.setRequesterId(requesterId);
         hfmr.setRequestType(requestTypeService.getRequestTypeByLabel(getLabel()));
         performBusinessChecks(hfmr);
 
         setAdministrativeInformation(hfmr);
         requestDAO.create(hfmr);
         return hfmr;
     }
 
     private boolean hasModificationRequestInProgress(final HomeFolder homeFolder)
         throws CvqException {
 
         List<Request> otherRequests = 
             getByHomeFolderIdAndRequestLabel(homeFolder.getId(), getLabel());
         if (otherRequests != null) {
             for (Request request : otherRequests) {
                 if (request.getState().equals(RequestState.PENDING)
                     || request.getState().equals(RequestState.COMPLETE)
                     || request.getState().equals(RequestState.UNCOMPLETE)) {
                     logger.warn("create() Home folder " + homeFolder.getId() 
                             + " already has an home folder modification request in progress");
                     return true;
                 }
             }
         }
 
         return false;
     }
     
     public void checkIsAuthorized(final HomeFolder homeFolder) throws CvqException {
         if (hasModificationRequestInProgress(homeFolder))
             throw new CvqModelException("homeFolder.error.alreadyAccountModifcationInProgess");
 
         if (!homeFolder.getState().equals(ActorState.VALID))
             throw new CvqModelException("homeFolder.error.accountModifcationPossibleForValidatedAccount");
     }
     
     @Override
     public Map<Long, Set<RequestSeason>> getAuthorizedSubjects(final Long homeFolderId)
         throws CvqException, CvqObjectNotFoundException {
 
         HomeFolder homeFolder = homeFolderService.getById(homeFolderId);
         try { 
             checkIsAuthorized(homeFolder);
         } catch (CvqModelException cme) {
             logger.debug("getAuthorizedSubjects() not authorized, returning null");
             return null;
         }
         
         Map<Long, Set<RequestSeason>> result =
             new HashMap<Long, Set<RequestSeason>>();
         result.put(homeFolder.getId(), new HashSet<RequestSeason>());
         return result;
     }
 
     @Override
     public CreationBean modify(final HomeFolderModificationRequest hfmr,
             final List<Adult> newAdults, final List<Child> newChildren, Address adress)
         throws CvqException {
         
         // Merge new homeFolder object if reuired
         for (int i = 0; i < newAdults.size(); i++) {
             if (newAdults.get(i).getId() != null) {
                 Adult mergeAdult = (Adult)HibernateUtil.getSession().merge(newAdults.get(i));
                 newAdults.set(i, mergeAdult);
             }
         }
         for (int i = 0; i < newChildren.size(); i++) {
             if (newChildren.get(i).getId() != null) {
                 Child mergeChild = (Child)HibernateUtil.getSession().merge(newChildren.get(i));
                 newChildren.set(i, mergeChild);
             }
         }
         if (adress.getId() != null)
             adress = (Address)HibernateUtil.getSession().merge(adress);
         
         historyInterceptor.setCurrentRequest(hfmr);
         historyInterceptor.setCurrentUser(SecurityContext.getCurrentUserLogin());
         historyInterceptor.setCurrentSession(HibernateUtil.getSession());
         
         CreationBean cb = null;
         
         HomeFolder oldHomeFolder = homeFolderService.getById(hfmr.getHomeFolderId());
         
         // TODO REFACTORING
         // home folder will have to be validated again
         oldHomeFolder.setState(ActorState.PENDING);
 
         // take a snapshot of the "old" home folder
         // (ie as it was before issuing this modification request)
         
         List<Child> oldChildren = new ArrayList<Child>();
         List<Adult> oldAdults = new ArrayList<Adult>();
         for (Individual tempInd : oldHomeFolder.getIndividuals()) {
             if (tempInd instanceof Adult) {
                 oldAdults.add((Adult) tempInd);
             } else {
                 oldChildren.add((Child) tempInd);
             }
         }
 
         // first, deal with modifications related to children
         for (Child child : oldChildren) {
             logger.debug("modify() looking at old child : " + child);
             if (!containsIndividual(newChildren, child)) {
                 logger.debug("modify() child removed from home folder : " + child);
                 // just unlink from its home folder, don't remove it yet from DB
                 // because request can be refused
                 // if the request is validated, the child will be removed then
                 child.setHomeFolder(null);
                 
                 // TODO INDEXED LISTS TO TEST MORE
                 oldHomeFolder.getIndividuals().remove(child);
 
                 individualService.modify(child);
             }
         }
         for (Child newChild : newChildren) {
             logger.debug("modify() looking at new child : " + newChild);
             if (!containsIndividual(oldChildren, newChild)) {
                 logger.debug("modify() child added to home folder : " + newChild);
 
                 individualService.create(newChild, oldHomeFolder, adress, false);
                 oldChildren.add(newChild);
             }
         }
 
         // then, deal with modifications related to home folder adults
         boolean loggedInUserChange = false;
         for (Adult adult : oldAdults) {
             logger.debug("modify() looking at old adult : " + adult);
             if (!containsIndividual(newAdults, adult)) {
                 logger.debug("modify() adult removed from home folder : " + adult);
 
                 // check if adult was the currently logged in user
                 // if so, prepare for a hot swap
                 if (SecurityContext.getCurrentEcitizen().getLogin().equals(adult.getLogin())) {
                     logger.info("modify() currently logged in user is going to be removed from its home folder !");
                     loggedInUserChange = true;
                 }
 
                 // just unlink from its home folder, don't remove it yet from DB
                 // because request can be refused
                 // if the request is validated, the adult will be removed then
                 adult.setHomeFolder(null);
 
                 // TODO INDEXED LISTS TO TEST MORE
                 oldHomeFolder.getIndividuals().remove(adult);
 
                 individualService.modify(adult);
             } else {
                 if (!adult.getLogin().startsWith(adult.getFirstName().toLowerCase() + "." 
                         + adult.getLastName().toLowerCase())) {
                     logger.debug("modify() adult changed of first or last name");
                     logger.debug("modify() adult login : " + adult.getLogin());
                     logger.debug("modify() adult names : " + adult.getFirstName() + " " + adult.getLastName());
                 }
             }
         }
 
         for (Adult adult : newAdults) {
             logger.debug("modify() looking at new adult : " + adult);
 
             // new passwords generation handling
             //     -> a new adult which is home folder responsible
             //     -> an home folder responsible change inside home folder
             if (adult.getPassword() != null && !adult.getPassword().startsWith("{SHA}")) {
                 adult.setPassword(individualService.encryptPassword(adult.getPassword()));
             }
 
             if (!containsIndividual(oldAdults, adult)) {
                 logger.debug("modify() adult added to home folder : " + adult);
                 individualService.create(adult, oldHomeFolder, adult.getAdress(), true);
             }
 
             // currently logged in user has been removed from the home folder
             // set the new home folder responsible as the new logged in user
             if (loggedInUserChange) {
 
                 for (IndividualRole individualRole : adult.getIndividualRoles()) {
                     if (individualRole.getRole().equals(RoleType.HOME_FOLDER_RESPONSIBLE)) {
                         logger.debug("modify() Got the new logged in user with login : "
                                 + adult.getLogin());
                         cb = new CreationBean();
                         cb.setLogin(adult.getLogin());
                         cb.setRequestId(hfmr.getId());
                         // and set it as the request's requester, to pass security checks
                         hfmr.setRequesterId(adult.getId());
                         SecurityContext.setCurrentEcitizen(adult);
                     }
                 }
             }
         }
         
         homeFolderService.checkAndFinalizeRoles(hfmr.getHomeFolderId(), newAdults, newChildren);
 
         requestDAO.update(hfmr);
 
         // TODO REFACTORING : branch into common treatments
         logger.debug("modify() Gonna generate a pdf of the request");
         byte[] pdfData =
             certificateService.generateRequestCertificate(hfmr);
         requestActionService.addCreationAction(hfmr.getId(), new Date(), pdfData);
 
         super.notifyRequestCreation(hfmr, pdfData);
         
         // inform history interceptor that it could stop intercepting after the next postFlush()
         historyInterceptor.releaseInterceptor();
         
         return cb;
     }
     
     public CreationBean modify(final HomeFolderModificationRequest hfmr,
             final List<Adult> adults, final List<Child> children, List<Adult> foreignRoleOwners, 
             final Address adress, List<Document> documents)
         throws CvqException {
         
         CreationBean cb = modify(hfmr, adults, children, adress);
         
         addDocuments(hfmr, documents);
         
        if (foreignRoleOwners != null) {
            for (int i = 0; i < foreignRoleOwners.size(); i++) {
                if (foreignRoleOwners.get(i).getId() != null) {
                    Adult mergeRoleOwner = (Adult)HibernateUtil.getSession().merge(foreignRoleOwners.get(i));
                    foreignRoleOwners.set(i, mergeRoleOwner);
                }
             }
         }
         homeFolderService.saveForeignRoleOwners(hfmr.getHomeFolderId(), adults, children, 
                 foreignRoleOwners);
         
         return cb;
     }
 
     @Override
     public Set<HistoryEntry> getHistoryEntries(final Long hfmrId)
         throws CvqException {
 
         List<HistoryEntry> historyEntriesList = historyEntryDAO.listByRequestId(hfmrId);
         return new LinkedHashSet<HistoryEntry>(historyEntriesList);
     }
 
     @Override
     public void onRequestValidated(final Request request)
         throws CvqException {
 
         HomeFolderModificationRequest hfmr = (HomeFolderModificationRequest) request;
 
         // navigate through all the history entries and persist previous changes
         // (ie clear history entries and delete removed elements from home folder)
         //////////////////////////////////////////////////////////////////////////
 
         Set<HistoryEntry> historiesSet = getHistoryEntries(hfmr.getId());
         Set<Object> objectsToUpdate = new HashSet<Object>();
         Set<Object> objectsToRemove = new HashSet<Object>();
 
         for (HistoryEntry he : historiesSet) {
 
             if (he.getOperation().equals("update")) {
 
                 // "simple" field value change
                 if (!he.getProperty().equals("homeFolder")) {
                     logger.debug("validate() simple field value change (" + he.getProperty() + ")");
                     Object object = 
                         genericDAO.findById(getClassFromHistoryEntry(he.getClazz()),
                             he.getObjectId());
                     if (!objectsToRemove.contains(object))
                         objectsToUpdate.add(object);
                 }
 
                 // TODO REFACTORING : nothing to do for individualRoles ??
 
                 // an individual has been removed
                 // (only individuals have an homeFolder property)
                 if (he.getProperty().equals("homeFolder")) {
 
                     Individual individual = individualService.getById(he.getObjectId());
 
                     // unlink from (eventually) common home folder adress
                     // FIXME : what if alone with this adress ?
                     individual.setAdress(null);
 
                     // FIXME : can newValue be anything else ??
                     if (he.getNewValue() == null) {
                         logger.debug("validate() an individual has been removed : " + individual);
 
                         if (individual instanceof Adult) {
                             Adult adult = (Adult) individual;
 
                             List<Request> requests = getByRequesterId(adult.getId());
                             for (Request tempRequest : requests) {
                                 // don't remove requesterLastName 'coz it can be useful
                                 // for history purposes
                                 tempRequest.setRequesterId(null);
                                 requestDAO.update(tempRequest);
                             }
                         }
 
                         String oldValue = he.getOldValue();
                         String oldHomeFolderId =
                             oldValue.substring(oldValue.indexOf("id=") + 3,
                             oldValue.lastIndexOf(']'));
                         homeFolderService.removeRolesOnSubject(Long.valueOf(oldHomeFolderId),
                             individual.getId());
 
                         // update requests whose individual is the subject
                         List<Request> requestsAsSubject = getBySubjectId(individual.getId());
                         for (Request requestAsSubject : requestsAsSubject) {
                             // don't remove subjectLastName 'coz it can be useful
                             // for history purposes
                             requestAsSubject.setSubjectId(null);
                             requestDAO.update(requestAsSubject);
                         }
 
                         if (objectsToUpdate.contains(individual)) {
                             logger.debug("validate() was in update list, removing");
                             objectsToUpdate.remove(individual);
                         }
 
                         objectsToRemove.add(individual);
                     } 
                 }
             } else if (he.getOperation().equals("created")) {
                 Object object = 
                     genericDAO.findById(getClassFromHistoryEntry(he.getClazz()), he.getObjectId());
                 logger.debug("validate() an object has been created : " + object);
                 objectsToUpdate.add(object);
             }
         }
 
         // clear history entries and persist changes in DB
         //////////////////////////////////////////////////
 
         updateObjects(objectsToUpdate, objectsToRemove);
 
         int deletedEntries = historyEntryDAO.deleteEntries(hfmr.getId());
         logger.debug("validate() deleted " + deletedEntries + " history entries");
     }
 
     /**
      * Do the real work of making the changes (update or delete) persistent.
      */
     private void updateObjects(final Set<Object> objectsToUpdate, 
             final Set<Object> objectsToRemove)
         throws CvqException {
 
         for (Object object : objectsToUpdate) {
             logger.debug("updateObjects() Updating " + object);
 
             // only for children because new adults are written when calling
             // modify method (because we need their login and pwd)
             // FIXME REFACTORING : validate this 
             if (object instanceof Child) {
                 Child child = (Child) object;
                 individualService.assignLogin(child);
             }
         }
 
         for (Object object : objectsToRemove) {
             logger.debug("updateObjects() Removing " + object);
             genericDAO.delete(object);
         }
     }
 
     /**
      * Transform a field name into a setXXX method name
      */
     private String propertyToMethodName(final String property, final String modifier) {
 
         String charToUpper = property.substring(0,1);
         String methodName = property.replaceFirst(charToUpper, charToUpper.toUpperCase());
         methodName = modifier + methodName;
         return methodName;
     }
 
     /**
      * Load the class represented by this clazz string.
      */
     private Class<?> getClassFromHistoryEntry(final String clazz) {
         try {
             return Class.forName(clazz);
         } catch (ClassNotFoundException cnfe) {
             // I know this can't happen
         }
         
         return null;
     }
     
     /**
      * Restore the original property value on an object using information
      * stored in the {@link fr.cg95.cvq.business.users.HistoryEntry HistoryEntry}.
      * If not specified, assume the property to be of type {@link String String}.
      *
      * @return the object pointed by the history entry with the given property
      *         reset to its original state.
      */
     private Object restoreOldProperty(final HistoryEntry he, final Class<?> oldPropertyClass,
             final Object oldPropertyValue)
         throws CvqException {
 
         Object object = genericDAO.findById(getClassFromHistoryEntry(he.getClazz()),
             he.getObjectId());
         Method meth = null;
         String methodName = propertyToMethodName(he.getProperty(), "set");
 
         // retrieve and invoke the method to use for comparisons
         try {
             Class<?>[] paramsTypes = new Class[1];
             // to avoid a parameter type nightmare, each historizable
             // object has a setXXX(String) method for non-association fields that,
             // if needed, call the right setXXX method
             if (oldPropertyClass != null)
                 paramsTypes[0] = oldPropertyClass;
             else
                 paramsTypes[0] = String.class;
             meth = object.getClass().getMethod(methodName, paramsTypes);
             Object[] params = new Object[1];
             // check on oldPropertyClass because we want to be able to set a null value
             if (oldPropertyClass != null)
                 params[0] = oldPropertyValue;
             else
                 params[0] = he.getOldValue();
             meth.invoke(object, params);
         } catch (Exception e) {
             e.printStackTrace();
             logger.error("restoreOldProperty() Error while restoring property " 
                     + he.getProperty() + " on object " + object);
             throw new CvqException("Error while restoring property " 
                     + he.getProperty() + " on object " + object);
         }
 
         return object;
     }
 
     /**
      * Restore the original home folder as it was before the given request was issued.
      */
     private void restoreOriginalHomeFolder(final HomeFolderModificationRequest request)
         throws CvqException {
 
         Set<Object> objectsToUpdate = new HashSet<Object>();
         Set<Object> objectsToRemove = new HashSet<Object>();
 
         // navigate through all the history entries and cancel previous changes
 
         Set<HistoryEntry> historiesSet = getHistoryEntries(request.getId());
         for (HistoryEntry he : historiesSet) {
             logger.debug("restoreOriginalHomeFolder() history entry : " + he.getId());
             
             if (he.getOperation().equals("update")) {
                 logger.debug("restoreOriginalHomeFolder() update operation");
                 logger.debug("restoreOriginalHomeFolder() dealing with property : " 
                         + he.getProperty() + " of class " + he.getClazz());
                 
                 // simple field value change
                 if (!he.getProperty().startsWith("password")
                     && !he.getProperty().equals("homeFolder")
                     && !he.getProperty().equals("adress")
                     && !he.getProperty().equals("individualRoles")
                     && !he.getProperty().equals("individuals")) {
                     logger.debug("restoreOriginalHomeFolder() simple field value change (" 
                             + he.getProperty() + ")");
                     Object object = restoreOldProperty(he, null, null);
 
                     if (!objectsToRemove.contains(object))
                         objectsToUpdate.add(object);
                 }
 
                 // a individual has been planed for removal, re-attach it to home folder
                 else if (he.getProperty().equals("homeFolder")) {
 
                     if (he.getNewValue() == null) {
                         logger.debug("restoreOriginalHomeFolder() an object was planned for removal, re-attach it");
 
                         String oldValue = he.getOldValue();
                         String oldHomeFolderId =
                             oldValue.substring(oldValue.indexOf("id=") + 3, 
                                     oldValue.lastIndexOf(']'));
                         HomeFolder oldHomeFolder = 
                             homeFolderService.getById(Long.valueOf(oldHomeFolderId));
                         Object object = restoreOldProperty(he, HomeFolder.class, oldHomeFolder);
 
                         if (!objectsToRemove.contains(object))
                             objectsToUpdate.add(object);
                     } 
                 }
 
                 // an adress change was planned, go back to the original one
                 else if (he.getProperty().equals("adress")) {
                     logger.debug("restoreOriginalHomeFolder() an adress change was planned, "
                             + "go back");
                     String oldValue = he.getOldValue();
                     String oldAdressId =
                         oldValue.substring(oldValue.indexOf("id=") + 3,
                                 oldValue.lastIndexOf(']'));
                     Address oldAdress = 
                         (Address) genericDAO.findById(Address.class, Long.valueOf(oldAdressId));
                     Object object = restoreOldProperty(he, Address.class, oldAdress);
 
                     if (!objectsToRemove.contains(object))
                         objectsToUpdate.add(object);
                 }
 
                 else if (he.getProperty().equals("individualRoles")) {
                    logger.debug("restoreOriginalHomeFolder() a role change was planned, go back");
                    
                    // load the owner to restore its original roles
                    Individual individual = 
                        (Individual) genericDAO.findById(Individual.class, he.getObjectId());
                    Set<IndividualRole> oldIndividualRoles = new HashSet<IndividualRole>();
                    
                    // take old values and add them back to the set
                    if (he.getOldValue() != null) {
                        String[] oldValues =
                            he.getOldValue().substring(1, he.getOldValue().length() - 1).split(",");
                        for (int i=0; i < oldValues.length; i++) {
                            String oldValue = oldValues[i];
                            String oldIndividualRoleId =
                                oldValue.substring(oldValue.indexOf("id=") + 3,
                                        oldValue.lastIndexOf(']'));
                            logger.debug("restoreOriginalHomeFolder() got old role id : " 
                                    + oldIndividualRoleId);
                            IndividualRole individualRole = 
                                (IndividualRole) genericDAO.findById(IndividualRole.class, 
                                        Long.valueOf(oldIndividualRoleId));
                            oldIndividualRoles.add(individualRole);
                        }
                    }
                    
                    if (!oldIndividualRoles.isEmpty()) {
                        logger.debug("restoreOriginalHomeFolder() restoring roles (" 
                                + oldIndividualRoles.size() + ") for individual "
                                + individual.getId());
                        individual.setIndividualRoles(oldIndividualRoles);
                        
                        if (!objectsToRemove.contains(individual))
                            objectsToUpdate.add(individual);
                    }
                }
             } else if (he.getOperation().equals("created")) {
                 logger.debug("restoreOriginalHomeFolder() creation operation"); 
 
                 Object object = 
                     genericDAO.findById(getClassFromHistoryEntry(he.getClazz()), he.getObjectId());
                 logger.debug("restoreOriginalHomeFolder() object " + object 
                         + " was planned for addition, unlink it");
                 
                 if (object instanceof Individual) {
                     if (!objectsToRemove.contains(object)) {
                         Individual individual = (Individual) object;
                         homeFolderService.deleteIndividual(request.getHomeFolderId(), 
                                 individual.getId());
                     }
                 } else if (object instanceof IndividualRole) {
                     IndividualRole individualRole = (IndividualRole) object;
                     List<Individual> individuals = 
                         homeFolderService.getIndividuals(request.getHomeFolderId());
                     for (Individual individual : individuals) {
                         if (individual.getIndividualRoles() == null)
                             continue;
                         if (individual.getIndividualRoles().remove(individualRole)) {
                             logger.debug("restoreOriginalHomeFolder() removed role "
                                     + individualRole.getRole() + " from "
                                     + individual.getId() + " on "
                                     + individualRole.getIndividualId());
                             break;
                         }
                     }
                 }
 
                 if (objectsToUpdate.contains(object)) {
                     logger.debug("restoreOriginalHomeFolder() was in update list, removing");
                     objectsToUpdate.remove(object);
                 }
                 objectsToRemove.add(object);
             }
         }
 
         // clear history entries and persist changes in DB
 
         updateObjects(objectsToUpdate, objectsToRemove);
 
         historyEntryDAO.deleteEntries(request.getId());
     }
 
     @Override
     public void onRequestCancelled(final Request request)
         throws CvqException, CvqInvalidTransitionException,
                CvqObjectNotFoundException {
 
         // modification request has been cancelled, restore original home folder state
         restoreOriginalHomeFolder((HomeFolderModificationRequest) request);
     }
 
     @Override
     public void onRequestRejected(final Request request)
         throws CvqException, CvqInvalidTransitionException,
                CvqObjectNotFoundException {
 
         // modification request has been rejected, restore original home folder state
         restoreOriginalHomeFolder((HomeFolderModificationRequest) request);
     }
 
     private boolean containsIndividual(final List<? extends Individual> setToSearchIn, 
             final Individual individualToLookFor) {
     
         if (setToSearchIn == null || setToSearchIn.isEmpty())
             return false;
         
         for (Individual individual : setToSearchIn) {
             if (individual.getId() == null)
                 continue;
             if (individual.getId().equals(individualToLookFor.getId()))
                 return true;
         }
         
         return false;
     }
     
     @Override
     public boolean accept(Request request) {
         return request instanceof HomeFolderModificationRequest;
     }
 
     @Override
     public Request getSkeletonRequest() throws CvqException {
         return new HomeFolderModificationRequest();
     }
 
     public void setHistoryEntryDAO(IHistoryEntryDAO iDAO) {
         this.historyEntryDAO = iDAO;
     }
 
     public void setHistoryInterceptor(HistoryInterceptor historyInterceptor) {
         this.historyInterceptor = historyInterceptor;
     }
 }
