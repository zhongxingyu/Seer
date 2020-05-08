 package fr.cg95.cvq.service.users.impl;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.ListableBeanFactory;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.users.ActorState;
 import fr.cg95.cvq.business.users.Address;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.IndividualRole;
 import fr.cg95.cvq.business.users.RoleType;
 import fr.cg95.cvq.business.users.payment.ExternalAccountItem;
 import fr.cg95.cvq.business.users.payment.ExternalDepositAccountItem;
 import fr.cg95.cvq.business.users.payment.ExternalInvoiceItem;
 import fr.cg95.cvq.business.users.payment.Payment;
 import fr.cg95.cvq.dao.IGenericDAO;
 import fr.cg95.cvq.dao.users.IAdultDAO;
 import fr.cg95.cvq.dao.users.IChildDAO;
 import fr.cg95.cvq.dao.users.IHomeFolderDAO;
 import fr.cg95.cvq.dao.users.IIndividualDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.external.IExternalService;
 import fr.cg95.cvq.payment.IPaymentService;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.authority.LocalAuthorityConfigurationBean;
 import fr.cg95.cvq.service.document.IDocumentService;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.request.IRequestWorkflowService;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 import fr.cg95.cvq.service.users.IIndividualService;
 import fr.cg95.cvq.util.mail.IMailService;
 
 /**
  * Implementation of the home folder service.
  *
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public class HomeFolderService implements IHomeFolderService, BeanFactoryAware {
 
     private static Logger logger = Logger.getLogger(HomeFolderService.class);
 
     protected IGenericDAO genericDAO;
     protected IHomeFolderDAO homeFolderDAO;
     protected IIndividualDAO individualDAO;
     protected IChildDAO childDAO;
     protected IAdultDAO adultDAO;
 
     protected IIndividualService individualService;
 
     protected ILocalAuthorityRegistry localAuthorityRegistry;
     protected IMailService mailService;
     protected IRequestService requestService;
     protected IRequestWorkflowService requestWorkflowService;
     protected IDocumentService documentService;
     protected IPaymentService paymentService;
     protected IExternalService externalService;
 
     private ListableBeanFactory beanFactory;
 
     public void init() {
         Map<String, IRequestService> beans = beanFactory.getBeansOfType(IRequestService.class, false, true);
         for (String beanName : beans.keySet()) {
             if (beanName.equals("defaultRequestService")) {
                 this.requestService = beans.get(beanName);
                 break;
             }
         }
 
         this.requestWorkflowService = (IRequestWorkflowService)
             beanFactory.getBeansOfType(IRequestWorkflowService.class, false, false).values().iterator().next();
     }
     
     @Override
     @Context(type=ContextType.UNAUTH_ECITIZEN,privilege=ContextPrivilege.WRITE)
     public HomeFolder create(final Adult adult) throws CvqException {
 
         List<Adult> adults = new ArrayList<Adult>();
         adults.add(adult);
         
         return create(adults, null, adult.getAdress());
     }
 
     @Override
     @Context(type=ContextType.UNAUTH_ECITIZEN,privilege=ContextPrivilege.WRITE)
     public HomeFolder create(List<Adult> adults, List<Child> children, Address address)
         throws  CvqException, CvqModelException {
 
         if (adults == null)
             throw new CvqModelException("homefolder.error.mustContainAtLeastAnAdult");
         
         // create the home folder
         HomeFolder homeFolder = new HomeFolder();
         initializeCommonAttributes(homeFolder);
         homeFolder.setAdress(address);
         homeFolder.setBoundToRequest(Boolean.valueOf(false));
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
         
         return homeFolder;
     }
 
     private void initializeCommonAttributes(HomeFolder homeFolder) 
         throws CvqException {
 
         homeFolder.setState(ActorState.PENDING);
         homeFolder.setEnabled(Boolean.TRUE);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public final void modify(final HomeFolder homeFolder)
         throws CvqException {
 
         if (homeFolder != null)
             homeFolderDAO.update(homeFolder);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public final void delete(final Long id)
         throws CvqException {
 
         HomeFolder homeFolder = getById(id);
         delete(homeFolder);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void deleteIndividual(final Long homeFolderId, final Long individualId) 
         throws CvqException, CvqObjectNotFoundException {
 
         Individual individual = individualService.getById(individualId);
         HomeFolder homeFolder = getById(homeFolderId);
         removeRolesOnSubject(homeFolderId, individual.getId());
         individual.setAdress(null);
         individual.setHomeFolder(null);
 
         homeFolder.getIndividuals().remove(individual);
     }
 
     private final void delete(final HomeFolder homeFolder)
         throws CvqException {
 
         // payments need to be deleted first because adults are requesters for them
         if (homeFolder.getPayments() != null) {
             for (Object object : homeFolder.getPayments()) {
                 Payment payment = (Payment) object;
                 paymentService.delete(payment);
             }
         }
 
         // delete all documents related to home folder and associated individuals
 
         documentService.deleteHomeFolderDocuments(homeFolder.getId());
 
         List<Individual> individuals = homeFolder.getIndividuals();
         for (Individual individual : individuals) {
             documentService.deleteIndividualDocuments(individual.getId());
         }
 
         // need to stack adults and children to ensure that adults are deleted before children
         // because of legal responsibles constraints
         // TODO REFACTORING
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
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.READ)
     public final Set<HomeFolder> getAll()
         throws CvqException {
 
         List<HomeFolder> homeFolders = homeFolderDAO.listAll();
         return new LinkedHashSet<HomeFolder>(homeFolders);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public final HomeFolder getById(final Long id)
         throws CvqException, CvqObjectNotFoundException {
 
         return (HomeFolder) homeFolderDAO.findById(HomeFolder.class, id);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public final List<Child> getChildren(final Long homeFolderId)
         throws CvqException {
 
         return childDAO.listChildrenByHomeFolder(homeFolderId);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public final List<Adult> getAdults(final Long homeFolderId)
         throws CvqException {
 
         return adultDAO.listAdultsByHomeFolder(homeFolderId);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Individual> getIndividuals(Long homeFolderId) throws CvqException {
         
         return individualDAO.listByHomeFolder(homeFolderId);
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
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void addHomeFolderRole(Individual owner, Long homeFolderId, RoleType role)
             throws CvqException {
  
         IndividualRole individualRole = new IndividualRole();
         individualRole.setRole(role);
         individualRole.setHomeFolderId(homeFolderId);
         addRoleToOwner(owner, individualRole);
     }
 
 
     @Override
     @Context(type=ContextType.UNAUTH_ECITIZEN,privilege=ContextPrivilege.WRITE)
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
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
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
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
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
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
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
     @Context(type=ContextType.UNAUTH_ECITIZEN,privilege=ContextPrivilege.WRITE)
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
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
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
                             String childName = individualRole.getIndividualName();
                             for (Child child : children) {
                                 if (childName.equals(child.getLastName() + " " + child.getFirstName())) {
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
         RoleType[] roles = {RoleType.CLR_FATHER, RoleType.CLR_MOTHER, RoleType.CLR_TUTOR};
         if (children != null) {
             for (Child child : children) {
                 // TODO REFACTORING : there is something strange here !
                 //            List<Individual> legalResponsibles = 
                 //                individualDAO.listBySubjectRoles(child.getId(), roles);
                 List<Individual> legalResponsibles = new ArrayList<Individual>();
                 for (Adult adult : adults) {
                     if (adult.getIndividualRoles() != null) {
                         for (IndividualRole individualRole : adult.getIndividualRoles()) {
                             if (child.getId().equals(individualRole.getIndividualId())
                                     && (individualRole.getRole().equals(RoleType.CLR_FATHER)
                                             || individualRole.getRole().equals(RoleType.CLR_MOTHER)
                                             || individualRole.getRole().equals(RoleType.CLR_TUTOR)))
                                 legalResponsibles.add(adult);
                         }
                     }
                 }
                 if (legalResponsibles == null || legalResponsibles.isEmpty())
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
         
         for (Adult roleOwner : foreignRoleOwners) {
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
             
             if (roleOwner.getId() == null)
                 individualService.create(roleOwner, null, null, true);
             else
                 individualService.modify(roleOwner);
         }
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
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
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
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
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Adult getHomeFolderResponsible(Long homeFolderId) throws CvqException {
         
         List<Individual> individuals = 
             individualDAO.listByHomeFolderRole(homeFolderId, RoleType.HOME_FOLDER_RESPONSIBLE);
         
         // here we can make the assumption that we properly enforced the role
         return (Adult) individuals.get(0);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Individual> getByHomeFolderRole(Long homeFolderId, RoleType role) {
         return individualDAO.listByHomeFolderRole(homeFolderId, role);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Individual> listByHomeFolderRoles(Long homeFolderId, RoleType[] roles) {
         return individualDAO.listByHomeFolderRoles(homeFolderId, roles);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Individual> getBySubjectRole(Long subjectId, RoleType role) {
         return individualDAO.listBySubjectRole(subjectId, role);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Individual> getBySubjectRoles(Long subjectId, RoleType[] roles) {
         return individualDAO.listBySubjectRoles(subjectId, roles);
     }
 
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Set<ExternalAccountItem> getExternalAccounts(Long homeFolderId, String type) 
         throws CvqException {
         
         // FIXME : at least request optimization or even refactoring ?
         List<Request> requests = requestService.getByHomeFolderId(homeFolderId);
         Set<String> homeFolderRequestsTypes = new HashSet<String>();
         for (Request request : requests) {
             homeFolderRequestsTypes.add(request.getRequestType().getLabel());
         }
 
         return externalService.getExternalAccounts(homeFolderId, 
                 homeFolderRequestsTypes, type);
     }
 
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Map<Individual, Map<String, String>> getIndividualExternalAccountsInformation(Long homeFolderId) 
         throws CvqException {
 
         // FIXME : at least request optimization or even refactoring ?
         List<Request> requests = requestService.getByHomeFolderId(homeFolderId);
         Set<String> homeFolderRequestsTypes = new HashSet<String>();
         for (Request request : requests) {
             homeFolderRequestsTypes.add(request.getRequestType().getLabel());
         }
 
         return externalService.getIndividualAccountsInformation(homeFolderId, 
                 homeFolderRequestsTypes);
     }
 
     public void loadExternalDepositAccountDetails(ExternalDepositAccountItem edai) 
         throws CvqException {
         
         externalService.loadDepositAccountDetails(edai);
     }
 
     public void loadExternalInvoiceDetails(ExternalInvoiceItem eii) 
         throws CvqException {
 
         externalService.loadInvoiceDetails(eii);
     }
 
     private void updateHomeFolderState(HomeFolder homeFolder, ActorState newState) 
 		throws CvqException {
 
 		logger.debug("Gonna update state of home folder : " + homeFolder.getId());
 		homeFolder.setState(newState);
 		homeFolderDAO.update(homeFolder);
 
 		// retrieve individuals and validate them
 		List<Individual> homeFolderIndividuals = homeFolder.getIndividuals();
 		for (Individual individual : homeFolderIndividuals) {
 			individualService.updateIndividualState(individual, newState);
 		}
     }
     
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void onRequestArchived(Long homeFolderId, Long requestId) throws CvqException {
         HomeFolder homeFolder = getById(homeFolderId);
         if (homeFolder.getBoundToRequest() && homeFolder.getOriginRequestId().equals(requestId)) {
             archive(homeFolder);
         }
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void onRequestCancelled(Long homeFolderId, Long requestId) throws CvqException {
         HomeFolder homeFolder = getById(homeFolderId);
         if (homeFolder.getBoundToRequest() && homeFolder.getOriginRequestId().equals(requestId)) {
             invalidate(homeFolder);
         }
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void onRequestRejected(Long homeFolderId, Long requestId) throws CvqException {
         HomeFolder homeFolder = getById(homeFolderId);
         if (homeFolder.getBoundToRequest() && homeFolder.getOriginRequestId().equals(requestId)) {
             invalidate(homeFolder);
         }
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void onRequestValidated(Long homeFolderId, Long requestId) throws CvqException {
         HomeFolder homeFolder = getById(homeFolderId);
         if (homeFolder.getBoundToRequest() && homeFolder.getOriginRequestId().equals(requestId)) {
             validate(homeFolder);
         }
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void onRequestDeleted(final Long homeFolderId, final Long requestId)
         throws CvqException {
         HomeFolder homeFolder = getById(homeFolderId);
         if (homeFolder.getBoundToRequest() && homeFolder.getOriginRequestId().equals(requestId)) {
             logger.debug("onRequestDeleted() Home folder " + homeFolderId 
                     + " belongs to request " + requestId + ", removing it from DB");
             delete(homeFolder);
         }
     }
 
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public final void validate(final Long id)
         throws CvqException, CvqObjectNotFoundException {
 
         HomeFolder homeFolder = getById(id);
         validate(homeFolder);
     }
 
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public final void validate(HomeFolder homeFolder)
         throws CvqException, CvqObjectNotFoundException {
 
         updateHomeFolderState(homeFolder, ActorState.VALID);
     }
     
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public final void invalidate(final Long id)
         throws CvqException, CvqObjectNotFoundException {
 
         HomeFolder homeFolder = getById(id);
         invalidate(homeFolder);
     }
 
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public final void invalidate(HomeFolder homeFolder) 
         throws CvqException, CvqObjectNotFoundException {
 
 		updateHomeFolderState(homeFolder, ActorState.INVALID);
 	}
 
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public final void archive(final Long id) 
         throws CvqException, CvqObjectNotFoundException {
         
         HomeFolder homeFolder = getById(id);
         archive(homeFolder);
     }
 
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public final void archive(HomeFolder homeFolder) 
         throws CvqException, CvqObjectNotFoundException {
         
         updateHomeFolderState(homeFolder, ActorState.ARCHIVED);
         
         requestWorkflowService.archiveHomeFolderRequests(homeFolder.getId());
     }
 
     public void notifyPaymentByMail(Payment payment) throws CvqException {
     	
         Adult homeFolderResponsible = getHomeFolderResponsible(payment.getHomeFolder().getId());
         String mailSendTo = homeFolderResponsible.getEmail();
         if (mailSendTo == null || mailSendTo.equals("")) {
             logger.warn("notifyPaymentByMail() e-citizen has no email adress, returning");
             return;
         }
         
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         Map<String, String> mailMap = 
             lacb.getMailAsMap("hasPaymentNotification", "getPaymentNotificationData", 
                     "CommitPaymentConfirmation");
         
 		if (mailMap != null) {
 			String mailSubject = mailMap.get("mailSubject") != null ? mailMap.get("mailSubject") : "";
 			String mailBodyFilename = mailMap.get("mailData") != null ? mailMap.get("mailData") : "";
             String mailBody = 
                 localAuthorityRegistry.getBufferedCurrentLocalAuthorityResource(
                         ILocalAuthorityRegistry.TXT_ASSETS_RESOURCE_TYPE, mailBodyFilename, false);
 			
             if (mailBody == null) {
                 logger.warn("notifyPaymentByMail() did not find mail template "
                         + mailBodyFilename + " for local authority " 
                         + SecurityContext.getCurrentSite().getName());
                 return;
             }
             
 			//	Mail body variable
 			mailBody = mailBody.replace("${broker}",
 					payment.getBroker() != null ? payment.getBroker() : "" );
 			mailBody = mailBody.replace("${cvqReference}",
 					payment.getCvqReference() != null ? payment.getCvqReference() : "" );
 			mailBody = mailBody.replace("${paymentMode}",
 					payment.getPaymentMode() !=  null ? payment.getPaymentMode().toString() : "");
 			mailBody = mailBody.replace("${commitDate}",
 					payment.getCommitDate().toString());
 			
 			mailService.send(null, mailSendTo, null, mailSubject, mailBody);
 		}
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
 
     public void setPaymentService(IPaymentService paymentService) {
         this.paymentService = paymentService;
     }
 
     public void setDocumentService(IDocumentService documentService) {
         this.documentService = documentService;
     }
 
     public void setExternalService(IExternalService externalService) {
         this.externalService = externalService;
     }
 
     @Override
     public void setBeanFactory(BeanFactory arg0) throws BeansException {
         this.beanFactory = (ListableBeanFactory) arg0;
     }
 }
 
