 package fr.cg95.cvq.service.users.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.scheduling.annotation.Async;
 
 import au.com.bytecode.opencsv.CSVWriter;
 
 import fr.cg95.cvq.authentication.IAuthenticationService;
 import fr.cg95.cvq.business.users.ActorState;
 import fr.cg95.cvq.business.users.Address;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
import fr.cg95.cvq.business.users.FamilyStatusType;
 import fr.cg95.cvq.business.users.HomeFolder;
import fr.cg95.cvq.business.users.SexType;
import fr.cg95.cvq.business.users.TitleType;
 import fr.cg95.cvq.business.users.UsersEvent;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.IndividualRole;
 import fr.cg95.cvq.business.users.RoleType;
 import fr.cg95.cvq.business.users.UsersEvent.EVENT_TYPE;
 import fr.cg95.cvq.business.users.external.HomeFolderMapping;
 import fr.cg95.cvq.business.users.external.IndividualMapping;
 import fr.cg95.cvq.dao.IGenericDAO;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.hibernate.HistoryInterceptor;
 import fr.cg95.cvq.dao.users.IAdultDAO;
 import fr.cg95.cvq.dao.users.IChildDAO;
 import fr.cg95.cvq.dao.users.IHomeFolderDAO;
 import fr.cg95.cvq.dao.users.IIndividualDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.schema.ximport.HomeFolderImportDocument;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 import fr.cg95.cvq.service.users.IIndividualService;
 import fr.cg95.cvq.util.mail.IMailService;
 import fr.cg95.cvq.util.translation.ITranslationService;
 import fr.cg95.cvq.xml.common.AddressType;
 import fr.cg95.cvq.xml.common.AdultType;
 import fr.cg95.cvq.xml.common.ChildType;
 import fr.cg95.cvq.xml.common.HomeFolderType;
 import fr.cg95.cvq.xml.common.IndividualType;
 
 /**
  * Implementation of the home folder service.
  *
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public class HomeFolderService implements IHomeFolderService, ApplicationContextAware {
 
     private static Logger logger = Logger.getLogger(HomeFolderService.class);
 
     protected IGenericDAO genericDAO;
     protected IHomeFolderDAO homeFolderDAO;
     protected IIndividualDAO individualDAO;
     protected IChildDAO childDAO;
     protected IAdultDAO adultDAO;
 
     protected IIndividualService individualService;
 
     protected ILocalAuthorityRegistry localAuthorityRegistry;
     protected IMailService mailService;
 
     private ApplicationContext applicationContext;
 
     private HistoryInterceptor historyInterceptor;
 
     private IAuthenticationService authenticationService;
 
     private ITranslationService translationService;
 
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public HomeFolder create(final Adult adult) throws CvqException {
 
         List<Adult> adults = new ArrayList<Adult>();
         adults.add(adult);
         
         HomeFolder homeFolder = create(adults, null, adult.getAdress());
         homeFolder.setTemporary(true);
         return homeFolder;
     }
 
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public HomeFolder create(List<Adult> adults, List<Child> children, Address address)
         throws  CvqException, CvqModelException {
 
         if (adults == null)
             throw new CvqModelException("homefolder.error.mustContainAtLeastAnAdult");
         
         // create the home folder
         HomeFolder homeFolder = new HomeFolder();
         initializeCommonAttributes(homeFolder);
         homeFolder.setAdress(address);
         homeFolderDAO.create(homeFolder);
         genericDAO.create(address);
 
         List<Individual> allIndividuals = new ArrayList<Individual>();
         allIndividuals.addAll(adults);
         if (children != null)
             allIndividuals.addAll(children);
         
         for (Individual individual : allIndividuals) {
             if (individual instanceof Child) 
                 individualService.create(individual, homeFolder, address, false);
             else if (individual instanceof Adult)
                 individualService.create(individual, homeFolder, address, true);                
         }
         
         checkAndFinalizeRoles(homeFolder.getId(), adults, children);
         
         homeFolder.setIndividuals(allIndividuals);
         homeFolderDAO.update(homeFolder);
         
         logger.debug("create() successfully created home folder " + homeFolder.getId());
         return homeFolder;
     }
 
     private void initializeCommonAttributes(HomeFolder homeFolder) {
         homeFolder.setState(ActorState.PENDING);
         homeFolder.setEnabled(Boolean.TRUE);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public final void modify(final HomeFolder homeFolder)
         throws CvqException {
 
         if (homeFolder != null)
             homeFolderDAO.update(homeFolder);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void modify(final Long homeFolderId, final Long keyId,
             final List<Adult> newAdults, List<Child> newChildren, Address adress)
         throws CvqException {
 
         // Merge new homeFolder object if reuired
         for (int i = 0; i < newAdults.size(); i++) {
             if (newAdults.get(i).getId() != null) {
                TitleType oldTitle = newAdults.get(i).getTitle();
                FamilyStatusType oldFamilyStatus = newAdults.get(i).getFamilyStatus();
                 Adult mergeAdult = (Adult)HibernateUtil.getSession().merge(newAdults.get(i));
                mergeAdult.setTitle(oldTitle);
                mergeAdult.setFamilyStatus(oldFamilyStatus);
                 newAdults.set(i, mergeAdult);
             }
         }
         // to prevent NPE if we have a null children list
         if (newChildren == null) 
             newChildren = Collections.<Child>emptyList();
         for (int i = 0; i < newChildren.size(); i++) {
             if (newChildren.get(i).getId() != null) {
                SexType oldSex = newChildren.get(i).getSex();
                 Child mergeChild = (Child)HibernateUtil.getSession().merge(newChildren.get(i));
                mergeChild.setSex(oldSex);
                 newChildren.set(i, mergeChild);
             }
         }
         if (adress.getId() != null)
             adress = (Address)HibernateUtil.getSession().merge(adress);
         
         historyInterceptor.setCurrentRequest(keyId);
         historyInterceptor.setCurrentUser(SecurityContext.getCurrentUserLogin());
         historyInterceptor.setCurrentSession(HibernateUtil.getSession());
         
         HomeFolder oldHomeFolder = getById(homeFolderId);
         
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
 
         // Child have the same address than the homeFolder responsible
         for (Child child : getChildren(homeFolderId)) {
             child.setAdress(adress.clone());
             individualService.modify(child);
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
                         // and set it as the request's requester, to pass security checks
                         SecurityContext.setCurrentEcitizen(adult);
                     }
                 }
             }
         }
         
         checkAndFinalizeRoles(homeFolderId, newAdults, newChildren);
 
         // inform history interceptor that it could stop intercepting after the next postFlush()
         historyInterceptor.releaseInterceptor();
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
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public final void delete(final Long id)
         throws CvqException {
 
         HomeFolder homeFolder = getById(id);
         delete(homeFolder);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void deleteIndividual(final Long homeFolderId, final Long individualId) 
         throws CvqException, CvqObjectNotFoundException {
 
         Individual individual = individualService.getById(individualId);
         HomeFolder homeFolder = getById(homeFolderId);
         removeRolesOnSubject(homeFolderId, individual.getId());
         individual.setHomeFolder(null);
 
         homeFolder.getIndividuals().remove(individual);
         individualService.delete(individual);
 
         UsersEvent individualEvent = 
             new UsersEvent(this, EVENT_TYPE.INDIVIDUAL_DELETE, null, individual.getId());
         applicationContext.publishEvent(individualEvent);
     }
 
     private final void delete(final HomeFolder homeFolder)
         throws CvqException {
 
         UsersEvent homeFolderEvent = 
             new UsersEvent(this, EVENT_TYPE.HOME_FOLDER_DELETE, homeFolder.getId(), null);
         applicationContext.publishEvent(homeFolderEvent);
         
         List<Individual> individuals = homeFolder.getIndividuals();
         for (Individual individual : individuals) {
             UsersEvent individualEvent = 
                 new UsersEvent(this, EVENT_TYPE.INDIVIDUAL_DELETE, null, individual.getId());
             applicationContext.publishEvent(individualEvent);
         }
 
         // need to stack adults and children to ensure that adults are deleted before children
         // because of legal responsibles constraints
         Set<Adult> adults = new HashSet<Adult>();
         Set<Child> children = new HashSet<Child>();
         for (Individual individual : individuals) {
             if (individual instanceof Adult)
                 adults.add((Adult)individual);
             else if (individual instanceof Child)
                 children.add((Child) individual);
         }
 
         for (Adult adult : adults) {
             individualService.delete(adult);
         }
 
         for (Child child : children) {
             individualService.delete(child);
         }
 
         homeFolderDAO.delete(homeFolder);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public final List<HomeFolder> getAll(boolean filterArchived, boolean filterTemporary)
         throws CvqException {
 
         return homeFolderDAO.listAll(filterArchived, filterTemporary);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public final HomeFolder getById(final Long id)
         throws CvqException, CvqObjectNotFoundException {
 
         return (HomeFolder) homeFolderDAO.findById(HomeFolder.class, id);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public final List<Child> getChildren(final Long homeFolderId)
         throws CvqException {
 
         return childDAO.listChildrenByHomeFolder(homeFolderId);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public final List<Adult> getAdults(final Long homeFolderId)
         throws CvqException {
 
         return adultDAO.listAdultsByHomeFolder(homeFolderId);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Individual> getIndividuals(Long homeFolderId) throws CvqException {
         
         return individualDAO.listByHomeFolder(homeFolderId);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Individual> getExternalIndividuals(final Long homeFolderId)
         throws CvqException {
         
         Set<Individual> externalIndividuals = new HashSet<Individual>();
         externalIndividuals.addAll(individualDAO.listByHomeFolderRoles(homeFolderId, RoleType.allRoleTypes, true));
         for (Individual individual : getIndividuals(homeFolderId))
             externalIndividuals.addAll(individualDAO.listBySubjectRoles(individual.getId(), RoleType.allRoleTypes, true));
         
         return new ArrayList<Individual>(externalIndividuals);
     }
 
     private void addRoleToOwner(Individual owner, IndividualRole role) {        
         if (owner.getIndividualRoles() == null) {
             Set<IndividualRole> individualRoles = new HashSet<IndividualRole>();
             individualRoles.add(role);
             owner.setIndividualRoles(individualRoles);
         } else {
             owner.getIndividualRoles().add(role);
         }
     }
     
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void addHomeFolderRole(Individual owner, Long homeFolderId, RoleType role)
             throws CvqException {
  
         IndividualRole individualRole = new IndividualRole();
         individualRole.setRole(role);
         individualRole.setHomeFolderId(homeFolderId);
         addRoleToOwner(owner, individualRole);
     }
 
 
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public void addHomeFolderRole(Individual owner, RoleType role)
             throws CvqException {
 
         IndividualRole individualRole = new IndividualRole();
         individualRole.setRole(role);
         addRoleToOwner(owner, individualRole);        
     }
 
     @Override
     public void addIndividualRole(Individual owner, Individual individual, RoleType role)
             throws CvqException {
 
         IndividualRole individualRole = new IndividualRole();
         individualRole.setRole(role);
         if (individual.getId() != null)
             individualRole.setIndividualId(individual.getId());
         else
             individualRole.setIndividualName(individual.getFullName());
         addRoleToOwner(owner, individualRole);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void removeRolesOnSubject(final Long homeFolderId, final Long individualId)
         throws CvqException {
         
         for (Individual homeFolderIndividual : getById(homeFolderId).getIndividuals()) {
             if (homeFolderIndividual.getIndividualRoles() == null)
                 continue;
             Set<IndividualRole> rolesToRemove = new HashSet<IndividualRole>();
             for (IndividualRole individualRole : homeFolderIndividual.getIndividualRoles()) {
                 if (individualRole.getIndividualId() != null
                         && individualRole.getIndividualId().equals(individualId))
                     rolesToRemove.add(individualRole);
             }
             if (rolesToRemove.isEmpty())
                 continue;
             logger.debug("removeRolesOnSubject() removing " + rolesToRemove.size()
                     + " roles from " + homeFolderIndividual.getId());
             for (IndividualRole roleToRemove : rolesToRemove)
                 homeFolderIndividual.getIndividualRoles().remove(roleToRemove);
             individualDAO.update(homeFolderIndividual);
         }
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public boolean removeHomeFolderRole(Individual owner, Long homeFolderId, RoleType role)
             throws CvqException {
 
         if (owner.getIndividualRoles() == null)
             return false;
         
         IndividualRole roleToRemove = null;
         for (IndividualRole individualRole : owner.getIndividualRoles()) {
             if (individualRole.getRole().equals(role) 
                     && homeFolderId.equals(individualRole.getHomeFolderId())) {
                 roleToRemove = individualRole;
                 break;
             } 
         }
         
         if (roleToRemove != null)
             return owner.getIndividualRoles().remove(roleToRemove);
 
         return false;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public boolean removeIndividualRole(Individual owner, Individual individual, RoleType role)
             throws CvqException {
 
         if (owner.getIndividualRoles() == null)
             return false;
         
         IndividualRole roleToRemove = null;
         String individualName = individual.getLastName() + " " + individual.getFirstName();
         for (IndividualRole individualRole : owner.getIndividualRoles()) {
             if (individualRole.getRole().equals(role)) {
                 if (individualRole.getIndividualId() != null
                         && individualRole.getIndividualId().equals(individual.getId())) {
                         roleToRemove = individualRole;
                         break;
                 } else if (individualRole.getIndividualName() != null
                         && individualRole.getIndividualName().equals(individualName)) {
                         roleToRemove = individualRole;
                         break;
                 }
             }
         }
 
         if (roleToRemove != null) {
             return owner.getIndividualRoles().remove(roleToRemove);
         }
 
         return false;
     }
     
     /*
      * TODO : refactor role management 
      */
     
     @Override
     public void addRole(Individual owner, final Individual individual, final Long homeFolderId, 
             final RoleType role) throws CvqException {
         if (individual == null)
             addHomeFolderRole(owner, homeFolderId, role);
         else
             addIndividualRole(owner, individual, role);
     }
     
     @Override
     public void addRole(Individual owner, final Individual individual, final RoleType role)
             throws CvqException {
         if (individual == null)
             addHomeFolderRole(owner, role);
         else
             addIndividualRole(owner, individual, role);
     }
     
     @Override
     public boolean removeRole(Individual owner, final Individual individual, final Long homeFolderId, 
             final RoleType role) throws CvqException {
         if (individual == null)
             return removeHomeFolderRole(owner, homeFolderId, role);
         else
             return removeIndividualRole(owner, individual, role);
     }
     
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public boolean removeRole(Individual owner, final Individual individual,  final RoleType role)
             throws CvqException {
         if (owner.getIndividualRoles() == null)
             return false;
         
         IndividualRole roleToRemove = null;
         for (IndividualRole ownerRole : owner.getIndividualRoles()) {
             if (ownerRole.getRole().equals(role)) {
                 if (ownerRole.getIndividualName() != null
                         && ownerRole.getIndividualName().equals(individual.getFullName())) {
                     roleToRemove = ownerRole;
                     break;
                 } else if (ownerRole.getIndividualName() == null
                         && individual == null
                         && Arrays.asList(RoleType.homeFolderRoleTypes).contains(role)) {
                     roleToRemove = ownerRole;
                     break;
                 }
             }
         }
         if (roleToRemove != null) {
             return owner.getIndividualRoles().remove(roleToRemove);
         }
         return false;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void checkAndFinalizeRoles(Long homeFolderId, List<Adult> adults, List<Child> children)
         throws CvqException, CvqModelException {
         
         List<Individual> allIndividuals = new ArrayList<Individual>();
         allIndividuals.addAll(adults);
         if (children != null)
             allIndividuals.addAll(children);
 
         // now that all individuals are persisted, we can deal with roles
         boolean foundHomeFolderResponsible = false;
         for (Adult adult : adults) {
             if (adult.getIndividualRoles() != null) {
                 for (IndividualRole individualRole : adult.getIndividualRoles()) {
                     if (individualRole.getRole().equals(RoleType.HOME_FOLDER_RESPONSIBLE)) {
                         logger.debug("checkAndFinalizeRoles() adult " + adult.getId() 
                                 + " is home folder responsible");
                         if (foundHomeFolderResponsible)
                             throw new CvqModelException("homeFolder.error.onlyOneResponsibleIsAllowed");
                         foundHomeFolderResponsible = true;
                         individualRole.setHomeFolderId(homeFolderId);
                         individualService.modify(adult);
                     } else if (individualRole.getRole().equals(RoleType.TUTOR)) {
                         logger.debug("checkAndFinalizeRoles() adult " + adult.getId() 
                                 + " is tutor");
                         String individualName = individualRole.getIndividualName();
                         if (individualName != null) {
                             // individual name is provided, it is the tutor of another individual
                             for (Individual individual : allIndividuals) {
                                 String otherAdultName = 
                                     individual.getLastName() + " " + individual.getFirstName();
                                 if (otherAdultName.equals(individualName)) {
                                     individualRole.setIndividualId(individual.getId());
                                     break;
                                 }
                             }
                         } else {
                             // individual name is not provided, it is the tutor of the home folder
                             individualRole.setHomeFolderId(homeFolderId);
                         }
                         individualService.modify(adult);
                     } else if (individualRole.getRole().equals(RoleType.CLR_FATHER)
                             || individualRole.getRole().equals(RoleType.CLR_MOTHER)
                             || individualRole.getRole().equals(RoleType.CLR_TUTOR)) {
                         logger.debug("checkAndFinalizeRoles() adult " + adult.getId() 
                                 + " is " + individualRole.getRole() + " for "
                                 + individualRole.getIndividualName() + "("
                                 + individualRole.getIndividualId() + ")");
                         if (individualRole.getIndividualId() == null) {
                             // FIXME : CLR role can only be used on Child. Useful to test corrupted xml import
                             if (individualRole.getHomeFolderId() != null && individualRole.getIndividualName() == null)
                                 throw new CvqException("homeFolder.error.legalResponsibleMustReferenceAChild");
                             for (Child child : children) {
                                 if (individualRole.getIndividualName().equals(child.getFullName())) {
                                     individualRole.setIndividualId(child.getId());
                                     break;
                                 }
                             }
                             individualService.modify(adult);
                         }
                     }
                 }
             }
         }
         
         // check all children have at least a legal responsible
 //        RoleType[] roles = {RoleType.CLR_FATHER, RoleType.CLR_MOTHER, RoleType.CLR_TUTOR};
         if (children != null) {
             for (Child child : children) {
                 // TODO REFACTORING : there is something strange here !
                 //            List<Individual> legalResponsibles = 
                 //                individualDAO.listBySubjectRoles(child.getId(), roles);
                 List<Individual> legalResponsibles = new ArrayList<Individual>();
                 for (Adult adult : adults) {
                     if (adult.getIndividualRoles() != null) {
                         for (IndividualRole individualRole : adult.getIndividualRoles()) {
                             if ((child.getId().equals(individualRole.getIndividualId())
                                     || child.getFullName().equals(individualRole.getIndividualName()))
 
                                     && (individualRole.getRole().equals(RoleType.CLR_FATHER)
                                             || individualRole.getRole().equals(RoleType.CLR_MOTHER)
                                             || individualRole.getRole().equals(RoleType.CLR_TUTOR)))
                                 legalResponsibles.add(adult);
                         }
                     }
                 }
                 if (legalResponsibles.isEmpty())
                     throw new CvqModelException("Child " + child.getFirstName() + 
                             " (" + child.getId() + ") has no legal responsible");
                 else if (legalResponsibles.size() > 3) 
                     throw new CvqModelException("Too many legal responsibles for child : " 
                             + child.getFirstName());
             }
         }
         
         if (!foundHomeFolderResponsible)
             throw new CvqModelException("homeFolder.error.responsibleIsRequired");
     }
     
     @Override
     public void saveForeignRoleOwners(Long homeFolderId, List<Adult> adults, List<Child> children,
             List<Adult> foreignRoleOwners) throws CvqException, CvqModelException {
         
         if (foreignRoleOwners == null)
             return;
         
         for (Adult roleOwner : foreignRoleOwners) {
             // in case someone has declared a foreign adult but has not given it any role
             if (roleOwner.getIndividualRoles() != null) {
                 for (IndividualRole role : roleOwner.getIndividualRoles()) {
                     if (Arrays.asList(RoleType.homeFolderRoleTypes).contains(role.getRole()))
                         role.setHomeFolderId(homeFolderId);
                     else if (Arrays.asList(RoleType.adultRoleTypes).contains(role.getRole())) {
                         for (Adult adult : adults) {
                             if (adult.getFullName().equals(role.getIndividualName())) {
                                 role.setIndividualId(adult.getId());
                                 break;
                             }
                         }
                     }
                     else if (Arrays.asList(RoleType.childRoleTypes).contains(role.getRole())) {
                         for (Child child : children) {
                             if (child.getFullName().equals(role.getIndividualName())) {
                                 role.setIndividualId(child.getId());
                                 break;
                             }
                         }
                     }
                 }
             }
             
             if (roleOwner.getId() == null)
                 individualService.create(roleOwner, null, null, true);
             else
                 individualService.modify(roleOwner);
         }
     }
     
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public boolean hasHomeFolderRole(Long ownerId, Long homeFolderId, RoleType role)
             throws CvqException {
         
         Individual owner = individualService.getById(ownerId);
         if (owner.getIndividualRoles() == null)
             return false;
         
         for (IndividualRole individualRole : owner.getIndividualRoles()) {
             if (individualRole.getRole().equals(role)
                     && homeFolderId.equals(individualRole.getHomeFolderId()))
                 return true;
         }
         
         return false;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public boolean hasIndividualRole(Long ownerId, Individual individual, RoleType role)
             throws CvqException {
 
         Individual owner = individualService.getById(ownerId);
         if (owner.getIndividualRoles() == null)
             return false;
         
         Long individualId = individual.getId();
         String individualName = individual.getLastName() + " " + individual.getFirstName();
         for (IndividualRole individualRole : owner.getIndividualRoles()) {
             if (individualRole.getRole().equals(role)) {
                 if (individualRole.getIndividualId() != null 
                         && individualRole.getIndividualId().equals(individualId))
                     return true;
                 if (individualRole.getIndividualName() != null
                         && individualRole.getIndividualName().equals(individualName))
                     return true;
             }
         }
 
         return false;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public Adult getHomeFolderResponsible(Long homeFolderId) throws CvqException {
         
         List<Individual> individuals = 
             individualDAO.listByHomeFolderRole(homeFolderId, RoleType.HOME_FOLDER_RESPONSIBLE);
         
         // here we can make the assumption that we properly enforced the role
         return (Adult) individuals.get(0);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Individual> getByHomeFolderRole(Long homeFolderId, RoleType role) {
         return individualDAO.listByHomeFolderRole(homeFolderId, role);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Individual> listByHomeFolderRoles(Long homeFolderId, RoleType[] roles) {
         return individualDAO.listByHomeFolderRoles(homeFolderId, roles, false);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Individual> getBySubjectRole(Long subjectId, RoleType role) {
         return individualDAO.listBySubjectRole(subjectId, role);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Individual> getBySubjectRoles(Long subjectId, RoleType[] roles) {
         return individualDAO.listBySubjectRoles(subjectId, roles, false);
     }
 
     private void updateHomeFolderState(HomeFolder homeFolder, ActorState newState) 
 		throws CvqException {
 
 		logger.debug("updateHomeFolderState() Gonna update state of home folder : " 
 		        + homeFolder.getId());
 		homeFolder.setState(newState);
 		homeFolderDAO.update(homeFolder);
 
 		// retrieve individuals and validate them
 		List<Individual> homeFolderIndividuals = homeFolder.getIndividuals();
 		for (Individual individual : homeFolderIndividuals) {
 			individualService.updateIndividualState(individual, newState);
 		}
     }
     
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public final void validate(final Long id)
         throws CvqException, CvqObjectNotFoundException {
 
         HomeFolder homeFolder = getById(id);
         validate(homeFolder);
     }
 
     private void validate(HomeFolder homeFolder)
         throws CvqException, CvqObjectNotFoundException {
 
         updateHomeFolderState(homeFolder, ActorState.VALID);
     }
     
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public final void invalidate(final Long id)
         throws CvqException, CvqObjectNotFoundException {
 
         HomeFolder homeFolder = getById(id);
         invalidate(homeFolder);
     }
 
     private void invalidate(HomeFolder homeFolder) 
         throws CvqException, CvqObjectNotFoundException {
 
 		updateHomeFolderState(homeFolder, ActorState.INVALID);
 	}
 
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public final void archive(final Long id) 
         throws CvqException, CvqObjectNotFoundException {
         
         HomeFolder homeFolder = getById(id);
         archive(homeFolder);
     }
 
     private void archive(HomeFolder homeFolder) 
         throws CvqException, CvqObjectNotFoundException {
         
         updateHomeFolderState(homeFolder, ActorState.ARCHIVED);
         
         UsersEvent homeFolderEvent = 
             new UsersEvent(this, EVENT_TYPE.HOME_FOLDER_ARCHIVE, homeFolder.getId(), null);
         applicationContext.publishEvent(homeFolderEvent);
     }
 
     public PasswordResetNotificationType notifyPasswordReset(Adult adult, String password, String categoryAddress)
         throws CvqException {
         String to = null;
         String body = null;
         PasswordResetNotificationType notificationType = PasswordResetNotificationType.INLINE;
         if (adult.getEmail() != null && !adult.getEmail().trim().isEmpty()) {
             to = adult.getEmail();
             body = "Veuillez trouver ci-dessous votre nouveau mot de passe :\n\t" + password + "\n\nBien cordialement.";
             notificationType = PasswordResetNotificationType.ADULT_EMAIL;
         } else if (categoryAddress != null) {
             to = categoryAddress;
             body = "Le mot de passe ci-dessous a été attribué à " + adult.getTitle() + " " + adult.getLastName() + " " + adult.getFirstName() + " (" + adult.getLogin() + ") :\n\t" + password;
             notificationType = PasswordResetNotificationType.CATEGORY_EMAIL;
         }
         if (notificationType != PasswordResetNotificationType.INLINE) {
             mailService.send(categoryAddress, to, null, "[" + SecurityContext.getCurrentSite().getDisplayTitle() + "] " + "Votre nouveau mot de passe pour vos démarches en ligne", body);
         }
         return notificationType;
     }
 
     @Override
     @Async
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void importHomeFolders(HomeFolderImportDocument doc)
         throws CvqException, IOException {
         ByteArrayOutputStream creationsOutput = new ByteArrayOutputStream();
         CSVWriter creations = new CSVWriter(new OutputStreamWriter(creationsOutput));
         creations.writeNext(new String[] {
             translationService.translate("homeFolder.property.id"),
             translationService.translate("homeFolder.property.externalId"),
             translationService.translate("homeFolder.individual.property.firstName"),
             translationService.translate("homeFolder.individual.property.lastName"),
             translationService.translate("homeFolder.adult.property.login"),
             translationService.translate("homeFolder.adult.property.password"),
             translationService.translate("homeFolder.adult.property.email"),
             translationService.translate("homeFolder.individual.property.address")
         });
         boolean hasCreations = false;
         ByteArrayOutputStream duplicatesOutput = new ByteArrayOutputStream();
         CSVWriter duplicates = new CSVWriter(new OutputStreamWriter(duplicatesOutput));
         duplicates.writeNext(new String[] {
             translationService.translate("homeFolder.property.externalId"),
             translationService.translate("homeFolder.individual.property.firstName"),
             translationService.translate("homeFolder.individual.property.lastName"),
             translationService.translate("homeFolder.adult.property.email"),
             translationService.translate("homeFolder.adult.property.homePhone"),
             translationService.translate("homeFolder.individual.property.address")
         });
         boolean hasDuplicates = false;
         ByteArrayOutputStream failuresOutput = new ByteArrayOutputStream();
         CSVWriter failures = new CSVWriter(new OutputStreamWriter(failuresOutput));
         failures.writeNext(new String[] {
             translationService.translate("homeFolder.property.externalId"),
             translationService.translate("Error")
         });
         boolean hasFailures = false;
         String label = doc.getHomeFolderImport().getExternalServiceLabel();
         homeFolders : for (HomeFolderType homeFolder : doc.getHomeFolderImport().getHomeFolderArray()) {
             HibernateUtil.beginTransaction();
             try {
                 List<Adult> adults = new ArrayList<Adult>();
                 List<Child> children = new ArrayList<Child>();
                 List<Individual> individuals = new ArrayList<Individual>();
                 HomeFolderMapping homeFolderMapping = null;
                 if (label != null && homeFolder.getExternalId() != null) {
                     homeFolderMapping = new HomeFolderMapping(label, null, homeFolder.getExternalId());
                 }
                 Address homeFolderAddress = Address.xmlToModel(homeFolder.getAddress());
                 for (IndividualType individual : homeFolder.getIndividualsArray()) {
                     String email = null;
                     String phone = null;
                     if (individual instanceof AdultType) {
                         AdultType adult = (AdultType)individual;
                         email = adult.getEmail();
                         phone = adult.getHomePhone();
                         Adult a = Adult.xmlToModel(adult);
                         adults.add(a);
                         individuals.add(a);
                     } else {
                         Child c = Child.xmlToModel((ChildType)individual);
                         children.add(c);
                         individuals.add(c);
                     }
                     AddressType address = individual.getAddress();
                     if (individualDAO.hasSimilarIndividuals(individual.getFirstName(),
                         individual.getLastName(), email, phone, address.getStreetNumber(),
                         address.getStreetName(), address.getPostalCode(), address.getCity())) {
                         duplicates.writeNext(new String[] {
                             homeFolder.getExternalId(),
                             individual.getFirstName(),
                             individual.getLastName(),
                             email,
                             phone,
                             address.getStreetNumber() == null ?
                                 String.format("%s %s %s", address.getStreetName(),
                                     address.getPostalCode(), address.getCity()) :
                                 String.format("%s %s %s %s", address.getStreetNumber(),
                                     address.getStreetName(), address.getPostalCode(), address.getCity())
                         });
                         hasDuplicates = true;
                         continue homeFolders;
                     }
                     if (homeFolderMapping != null) {
                         homeFolderMapping.getIndividualsMappings().add(
                             new IndividualMapping(null, individual.getExternalId(), homeFolderMapping));
                     }
                 }
                 HomeFolder result = create(adults, children, homeFolderAddress);
                 updateHomeFolderState(result, ActorState.VALID);
                 HibernateUtil.getSession().flush();
                 Adult responsible = getHomeFolderResponsible(result.getId());
                 String password = authenticationService.generatePassword();
                 authenticationService.resetAdultPassword(responsible, password);
                 creations.writeNext(new String[] {
                     String.valueOf(result.getId()),
                     homeFolder.getExternalId(),
                     responsible.getFirstName(),
                     responsible.getLastName(),
                     responsible.getLogin(),
                     password,
                     responsible.getEmail(),
                     homeFolderAddress.getStreetNumber() == null ?
                             String.format("%s %s %s", homeFolderAddress.getStreetName(),
                                     homeFolderAddress.getPostalCode(), homeFolderAddress.getCity()) :
                             String.format("%s %s %s %s", homeFolderAddress.getStreetNumber(),
                                     homeFolderAddress.getStreetName(), homeFolderAddress.getPostalCode(), homeFolderAddress.getCity())
                 });
                 if (homeFolderMapping != null) {
                     homeFolderMapping.setHomeFolderId(result.getId());
                     for (int i = 0; i < individuals.size(); i++) {
                         homeFolderMapping.getIndividualsMappings().get(i).setIndividualId(
                             individuals.get(i).getId());
                     }
                     genericDAO.create(homeFolderMapping);
                 }
                 HibernateUtil.commitTransaction();
                 hasCreations = true;
             } catch (Throwable t) {
                 failures.writeNext(new String[]{homeFolder.getExternalId(), t.getMessage()});
                 HibernateUtil.rollbackTransaction();
                 hasFailures = true;
             }
         }
         creations.close();
         duplicates.close();
         failures.close();
         Map<String, byte[]> attachments = new LinkedHashMap<String, byte[]>();
         if (hasCreations) {
             attachments.put(translationService
                 .translate("homeFolder.import.notification.attachmentName.creations") + ".csv",
                 creationsOutput.toByteArray());
         }
         if (hasDuplicates) {
             attachments.put(translationService
                 .translate("homeFolder.import.notification.attachmentName.duplicates") + ".csv",
                 duplicatesOutput.toByteArray());
         }
         if (hasFailures) {
             attachments.put(translationService
                 .translate("homeFolder.import.notification.attachmentName.errors") + ".csv",
                 failuresOutput.toByteArray());
         }
         try {
             mailService.send(null, SecurityContext.getCurrentSite().getAdminEmail(), null,
                 translationService.translate("homeFolder.import.notification.subject",
                     new Object[]{ SecurityContext.getCurrentSite().getDisplayTitle() }),
                 translationService.translate("homeFolder.import.notification.body"), attachments);
         } catch (CvqException e) {
             logger.error("importHomeFolders : could not notify result", e);
         }
     }
 
 	public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
 
     public void setMailService(IMailService mailService) {
 		this.mailService = mailService;
 	}
     public void setIndividualService(final IIndividualService individualService) {
 		this.individualService = individualService;
 	}
 
     public final void setHomeFolderDAO(final IHomeFolderDAO homeFolderDAO) {
         this.homeFolderDAO = homeFolderDAO;
     }
 
     public final void setChildDAO(final IChildDAO childDAO) {
         this.childDAO = childDAO;
     }
 
     public final void setAdultDAO(final IAdultDAO adultDAO) {
         this.adultDAO = adultDAO;
     }
 
 	public void setIndividualDAO(IIndividualDAO individualDAO) {
         this.individualDAO = individualDAO;
     }
 
     public void setGenericDAO(IGenericDAO genericDAO) {
 		this.genericDAO = genericDAO;
 	}
 
     public void setHistoryInterceptor(HistoryInterceptor historyInterceptor) {
         this.historyInterceptor = historyInterceptor;
     }
 
     @Override
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         this.applicationContext = applicationContext;
     }
 
     public void setAuthenticationService(IAuthenticationService authenticationService) {
         this.authenticationService = authenticationService;
     }
 
     public void setTranslationService(ITranslationService translationService) {
         this.translationService = translationService;
     }
 }
