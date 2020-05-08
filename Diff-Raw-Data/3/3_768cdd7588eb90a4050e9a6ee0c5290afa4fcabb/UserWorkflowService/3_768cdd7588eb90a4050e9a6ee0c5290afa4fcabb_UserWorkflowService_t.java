 package fr.cg95.cvq.service.users.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationEventPublisher;
 import org.springframework.context.ApplicationEventPublisherAware;
 import org.springframework.scheduling.annotation.Async;
 
 import au.com.bytecode.opencsv.CSVWriter;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonPrimitive;
 
 import fr.cg95.cvq.authentication.IAuthenticationService;
 import fr.cg95.cvq.business.QoS;
 import fr.cg95.cvq.business.authority.LocalAuthorityResource;
 import fr.cg95.cvq.business.users.Address;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.FamilyStatusType;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.IndividualRole;
 import fr.cg95.cvq.business.users.RoleType;
 import fr.cg95.cvq.business.users.TitleType;
 import fr.cg95.cvq.business.users.UserAction;
 import fr.cg95.cvq.business.users.UserEvent;
 import fr.cg95.cvq.business.users.UserState;
 import fr.cg95.cvq.business.users.UserWorkflow;
 import fr.cg95.cvq.business.users.external.HomeFolderMapping;
 import fr.cg95.cvq.business.users.external.IndividualMapping;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.jpa.IGenericDAO;
 import fr.cg95.cvq.dao.users.IHomeFolderDAO;
 import fr.cg95.cvq.dao.users.IIndividualDAO;
 import fr.cg95.cvq.exception.CvqAuthenticationFailedException;
 import fr.cg95.cvq.exception.CvqBadPasswordException;
 import fr.cg95.cvq.exception.CvqDisabledAccountException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqValidationException;
 import fr.cg95.cvq.schema.ximport.HomeFolderImportDocument;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.security.annotation.IsUser;
 import fr.cg95.cvq.service.authority.ILocalAuthorityLifecycleAware;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.authority.impl.LocalAuthorityRegistry;
 import fr.cg95.cvq.service.users.IUserNotificationService;
 import fr.cg95.cvq.service.users.IUserSearchService;
 import fr.cg95.cvq.service.users.IUserService;
 import fr.cg95.cvq.service.users.IUserWorkflowService;
 import fr.cg95.cvq.util.JSONUtils;
 import fr.cg95.cvq.util.UserUtils;
 import fr.cg95.cvq.util.development.BusinessObjectsFactory;
 import fr.cg95.cvq.util.mail.IMailService;
 import fr.cg95.cvq.util.translation.ITranslationService;
 import fr.cg95.cvq.xml.common.AddressType;
 import fr.cg95.cvq.xml.common.AdultType;
 import fr.cg95.cvq.xml.common.ChildType;
 import fr.cg95.cvq.xml.common.HomeFolderType;
 import fr.cg95.cvq.xml.common.IndividualType;
 
 public class UserWorkflowService implements IUserWorkflowService, ApplicationEventPublisherAware,
     ILocalAuthorityLifecycleAware {
 
     private static Logger logger = Logger.getLogger(UserWorkflowService.class);
 
     private ApplicationEventPublisher applicationEventPublisher;
 
     private ILocalAuthorityRegistry localAuthorityRegistry;
 
     private IAuthenticationService authenticationService;
 
     private IMailService mailService;
 
     private ITranslationService translationService;
 
     private IUserService userService;
 
     private IUserNotificationService userNotificationService;
 
     private IUserSearchService userSearchService;
     
     private IHomeFolderDAO homeFolderDAO;
 
     private IIndividualDAO individualDAO;
 
     private IGenericDAO genericDAO;
 
     private Map<String, UserWorkflow> workflows = new HashMap<String, UserWorkflow>();
 
     @Override
     public void addLocalAuthority(String localAuthorityName) {
         try {
             if (LocalAuthorityRegistry.DEVELOPMENT_LOCAL_AUTHORITY.equals(localAuthorityName)
                 && userSearchService.getByLogin("jean.dupont") == null) {
                 Address address = BusinessObjectsFactory.gimmeAddress(
                     "12", "Rue d'Aligre", "Paris", "75012");
                 Adult homeFolderResponsible =
                     BusinessObjectsFactory.gimmeAdult(TitleType.MISTER, "Dupont", "Jean",
                         address, FamilyStatusType.SINGLE);
                 homeFolderResponsible.setPassword("aaaaaaaa");
                 HomeFolder homeFolder = create(homeFolderResponsible, false);
                 Adult other = BusinessObjectsFactory.gimmeAdult(TitleType.MISTER, "Durand",
                     "Jacques", address, FamilyStatusType.SINGLE);
                 add(homeFolder, other, false);
                 Child child = BusinessObjectsFactory.gimmeChild("Moreau", "Émilie");
                 add(homeFolder, child);
                 link(homeFolderResponsible, child, Collections.singleton(RoleType.CLR_FATHER));
             }
         } catch (Exception e) {
             e.printStackTrace();
             logger.error("addLocalAuthority() Unable to create test home folder");
         }
     }
 
     @Override
     public void removeLocalAuthority(String localAuthorityName) {
     }
 
     @Override
     public UserState[] getPossibleTransitions(UserState state) {
         return getWorkflow().getPossibleTransitions(state);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public UserState[] getPossibleTransitions(Individual user) {
         UserState[] allStates = getPossibleTransitions(user.getState());
         List<UserState> result = new ArrayList<UserState>(allStates.length);
         for (UserState state : allStates) result.add(state);
         if (userSearchService.getHomeFolderResponsible(user.getHomeFolder().getId()).getId()
             .equals(user.getId())) {
             for (Individual i : user.getHomeFolder().getIndividuals()) {
                 if (!i.getId().equals(user.getId()) && !UserState.ARCHIVED.equals(i.getState())) {
                     result.remove(UserState.ARCHIVED);
                     break;
                 }
             }
         }
         return result.toArray(new UserState[result.size()]);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public UserState[] getPossibleTransitions(HomeFolder user) {
         return getPossibleTransitions(user.getState());
     }
 
     @Override
     public boolean isValidTransition(UserState from, UserState to) {
         return getWorkflow().isValidTransition(from, to);
     }
 
     @Override
     public UserState[] getStatesBefore(UserState state) {
         return getWorkflow().getStatesBefore(state);
     }
 
     @Override
     public UserState[] getStatesWithProperty(String propertyName) {
         return getWorkflow().getStatesWithProperty(propertyName);
     }
 
     private UserWorkflow getWorkflow() {
         String name = SecurityContext.getCurrentSite().getName();
         UserWorkflow workflow = workflows.get(name);
         if (workflow == null) {
             File file = localAuthorityRegistry.getLocalAuthorityResourceFileForLocalAuthority(name,
                 LocalAuthorityResource.Type.XML, "userWorkflow", false);
             if (file.exists()) {
                 workflow = UserWorkflow.load(file);
                 workflows.put(name, workflow);
             } else {
                 workflow = workflows.get("default");
                 if (workflow == null) {
                     file = localAuthorityRegistry.getReferentialResource(
                         LocalAuthorityResource.Type.XML, "userWorkflow");
                     workflow = UserWorkflow.load(file);
                     workflows.put("default", workflow);
                 }
             }
         }
         return workflow;
     }
 
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public HomeFolder create(Adult adult, boolean temporary)
         throws CvqException {
         HomeFolder homeFolder = new HomeFolder();
         homeFolder.setAddress(adult.getAddress());
         homeFolder.setEnabled(Boolean.TRUE);
         homeFolder.setState(SecurityContext.isFrontOfficeContext() ? UserState.NEW : UserState.VALID);
         homeFolder.setTemporary(temporary);
         homeFolderDAO.create(homeFolder);
         if (SecurityContext.isFrontOfficeContext()) {
             // FIXME hack to avoid CredentialBean's explosion in setCurrentEcitizen()
             adult.setHomeFolder(homeFolder);
             SecurityContext.setCurrentEcitizen(adult);
         }
         add(homeFolder, adult, !temporary);
         UserAction action = new UserAction(UserAction.Type.CREATION, homeFolder.getId());
         action = (UserAction) genericDAO.create(action);
         homeFolder.getActions().add(action);
         if (SecurityContext.isFrontOfficeContext()) {
             // FIXME attribute all previous actions to the newly created responsible which had no ID
             Gson gson = new Gson();
             for (UserAction tempAction : homeFolder.getActions()) {
                 tempAction.setUserId(adult.getId());
                 JsonObject payload = JSONUtils.deserialize(tempAction.getData());
                 JsonObject user = payload.getAsJsonObject("user");
                 user.addProperty("id", adult.getId());
                 user.addProperty("name", UserUtils.getDisplayName(adult.getId()));
                 tempAction.setData(gson.toJson(payload));
             }
         }
         link(adult, homeFolder, Collections.singleton(RoleType.HOME_FOLDER_RESPONSIBLE));
         logger.debug("create() successfully created home folder " + homeFolder.getId());
         homeFolderDAO.update(homeFolder);
         return homeFolder;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public Long add(HomeFolder homeFolder, Adult adult, boolean assignLogin)
         throws CvqException {
         if (assignLogin) {
             adult.setLogin(authenticationService.generateLogin(adult));
         }
         if (adult.getPassword() != null)
             adult.setPassword(authenticationService.encryptPassword(adult.getPassword()));
         return add(homeFolder, adult);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public Long add(HomeFolder homeFolder, Child child) throws CvqModelException, CvqInvalidTransitionException {
         return add(homeFolder, (Individual)child);
     }
 
     private Long add(HomeFolder homeFolder, Individual individual) throws CvqModelException, CvqInvalidTransitionException {
         homeFolder.getIndividuals().add(individual);
         individual.setHomeFolder(homeFolder);
         individual.setState(SecurityContext.isFrontOfficeContext() ? UserState.NEW : UserState.VALID);
         individual.setCreationDate(new Date());
         individual.setQoS(SecurityContext.isFrontOfficeContext() ? QoS.GOOD : null);
         individual.setLastModificationDate(new Date());
         if (individual.getAddress() == null) individual.setAddress(homeFolder.getAddress());
         Long id = individualDAO.create(individual).getId();
         UserAction action = new UserAction(UserAction.Type.CREATION, id);
         action = (UserAction) genericDAO.create(action);
         individual.getHomeFolder().getActions().add(action);
         if (SecurityContext.isFrontOfficeContext()
             && !UserState.NEW.equals(individual.getHomeFolder().getState())
             && !UserState.MODIFIED.equals(individual.getHomeFolder().getState())) {
             changeState(homeFolder, UserState.MODIFIED);
         }
         homeFolderDAO.update(homeFolder);
         applicationEventPublisher.publishEvent(new UserEvent(this, action));
         return id;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void modify(HomeFolder homeFolder) {
         if (SecurityContext.isFrontOfficeContext() && !UserState.NEW.equals(homeFolder.getState())) {
             homeFolder.setState(UserState.MODIFIED);
         }
         homeFolderDAO.update(homeFolder);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void modify(Individual individual, JsonObject atom)
         throws CvqException {
 
         if (individual == null)
             throw new CvqException("No adult object provided");
         else if (individual.getId() == null)
             throw new CvqException("Cannot modify a transient individual");
         if (SecurityContext.isFrontOfficeContext()) {
             if (!UserState.NEW.equals(individual.getState())) {
                 individual.setState(UserState.MODIFIED);
                 individual.setLastModificationDate(new Date());
                 individual.setQoS(QoS.GOOD);
             }
             if (!UserState.NEW.equals(individual.getHomeFolder().getState()))
                 individual.getHomeFolder().setState(UserState.MODIFIED);
         }
         JsonObject payload = new JsonObject();
         payload.add("atom", atom);
         UserAction action = new UserAction(UserAction.Type.MODIFICATION, individual.getId(), payload);
         action = (UserAction) genericDAO.create(action);
         // FIXME hack for specific business when changing a user's first or last name
         if ("identity".equals(atom.get("name").getAsString())) {
             JsonObject fields = atom.get("fields").getAsJsonObject();
             if (fields.has("firstName") || fields.has("lastName")) {
                 String firstName = fields.has("firstName") ?
                     fields.get("firstName").getAsJsonObject().get("from").getAsString()
                     : individual.getFirstName();
                 String lastName = fields.has("lastName") ?
                         fields.get("lastName").getAsJsonObject().get("from").getAsString()
                         : individual.getLastName();
                 Gson gson = new Gson();
                 payload = JSONUtils.deserialize(action.getData());
                 payload.get("target").getAsJsonObject()
                     .addProperty("name", firstName + ' ' + lastName);
                 if (individual.getId().equals(payload.get("user").getAsJsonObject().get("id").getAsLong())) {
                     payload.get("user").getAsJsonObject()
                         .addProperty("name", firstName + ' ' + lastName);
                 }
                 if (individual instanceof Adult) {
                     Adult adult = (Adult)individual;
                     if (!StringUtils.isEmpty(adult.getLogin())) {
                         JsonObject login = new JsonObject();
                         login.addProperty("from", adult.getLogin());
                         adult.setLogin(authenticationService.generateLogin(adult));
                         login.addProperty("to", adult.getLogin());
                         payload.get("atom").getAsJsonObject().get("fields").getAsJsonObject()
                             .add("login", login);
                         // hack to refresh security context
                         HibernateUtil.getSession().flush();
                     }
                 }
                 action.setData(gson.toJson(payload));
             }
         }
         individual.getHomeFolder().getActions().add(action);
         homeFolderDAO.update(individual.getHomeFolder());
         applicationEventPublisher.publishEvent(new UserEvent(this, action));
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public void modifyPassword(Adult adult, String oldPassword, String newPassword)
         throws CvqException, CvqBadPasswordException {
         try {
             authenticationService.authenticate(adult.getLogin(), oldPassword);
         } catch (CvqAuthenticationFailedException cafe) {
             String warning = "modifyPassword() old password does not match for user " + adult.getLogin();
             logger.warn(warning);
             throw new CvqBadPasswordException(warning);
         } catch (CvqDisabledAccountException cdae) {
             logger.info("modifyPassword() account is disabled, still authorizing password change");
         }
         authenticationService.resetAdultPassword(adult, newPassword);
     }
 
     @Override
     public String resetPassword(Adult adult)
         throws CvqException {
         String password = authenticationService.generatePassword();
         authenticationService.resetAdultPassword(adult, password);
         String message;
         if (!StringUtils.isBlank(adult.getEmail())) {
             userNotificationService.notifyByEmail(
                 SecurityContext.getCurrentSite().getAdminEmail(),
                 adult.getEmail(),
                 translationService.translate("account.notification.passwordReset.adult.subject"),
                 translationService.translate("account.notification.passwordReset.adult.body",
                     new String[]{password}),
                 null, null);
             message = translationService.translate("account.message.passwordResetSuccessAdultEmail",
                 new String[]{adult.getEmail()});
         } else if (!StringUtils.isBlank(SecurityContext.getCurrentSite().getAdminEmail())) {
             mailService.send(
                 SecurityContext.getCurrentSite().getAdminEmail(),
                 SecurityContext.getCurrentSite().getAdminEmail(),
                 null,
                 translationService.translate("account.notification.passwordReset.admin.subject",
                     new String[]{SecurityContext.getCurrentSite().getDisplayTitle()}),
                 translationService.translate("account.notification.passwordReset.admin.body",
                     new String[] {
                         translationService.translate("homeFolder.adult.title."
                             + adult.getTitle().toString().toLowerCase()),
                         adult.getLastName(), adult.getFirstName(), adult.getLogin(), password
                     }));
             message =
                 translationService.translate("account.message.passwordResetSuccessAdminEmail");
         } else {
             message = translationService.translate("account.message.passwordResetSuccessNoEmail",
                 new String[]{password});
         }
         return message;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void link(Individual owner, HomeFolder target, Collection<RoleType> types) {
         Set<RoleType> missing = new HashSet<RoleType>(types);
         for (IndividualRole role : owner.getHomeFolderRoles(target.getId())) {
             if (types.contains(role.getRole())) missing.remove(role.getRole());
             else owner.getIndividualRoles().remove(role);
         }
         for (RoleType type : missing) {
             IndividualRole newRole = new IndividualRole();
             newRole.setRole(type);
             newRole.setHomeFolderId(target.getId());
             owner.getIndividualRoles().add(newRole);
         }
         if (SecurityContext.isFrontOfficeContext() && !UserState.NEW.equals(target.getState())) {
             target.setState(UserState.MODIFIED);
         }
         JsonObject payload = new JsonObject();
         JsonObject jsonResponsible = new JsonObject();
         JsonArray jsonTypes = new JsonArray();
         for (RoleType type : types) jsonTypes.add(new JsonPrimitive(type.toString()));
         jsonResponsible.add("types", jsonTypes);
         jsonResponsible.addProperty("id", owner.getId());
         jsonResponsible.addProperty("name", UserUtils.getDisplayName(owner.getId()));
         payload.add("responsible", jsonResponsible);
         UserAction action = new UserAction(UserAction.Type.MODIFICATION, target.getId(), payload);
         action = (UserAction) genericDAO.create(action);
         owner.getHomeFolder().getActions().add(action);
         homeFolderDAO.update(owner.getHomeFolder());
         applicationEventPublisher.publishEvent(new UserEvent(this, action));
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void unlink(Individual owner, HomeFolder target) {
         Set<IndividualRole> roles = owner.getHomeFolderRoles(target.getId());
         if (roles.isEmpty()) return;
         Set<RoleType> deleted = new HashSet<RoleType>();
         for (IndividualRole role : roles) {
             owner.getIndividualRoles().remove(role);
             deleted.add(role.getRole());
         }
         if (SecurityContext.isFrontOfficeContext() && !UserState.NEW.equals(target.getState())) {
             target.setState(UserState.MODIFIED);
         }
         JsonObject payload = new JsonObject();
         JsonObject jsonResponsible = new JsonObject();
         JsonArray jsonTypes = new JsonArray();
         for (RoleType type : deleted) jsonTypes.add(new JsonPrimitive(type.toString()));
         jsonResponsible.add("deleted", jsonTypes);
         jsonResponsible.addProperty("id", owner.getId());
         jsonResponsible.addProperty("name", UserUtils.getDisplayName(owner.getId()));
         payload.add("responsible", jsonResponsible);
         UserAction action = new UserAction(UserAction.Type.MODIFICATION, target.getId(), payload);
         action = (UserAction) genericDAO.create(action);
         owner.getHomeFolder().getActions().add(action);
         homeFolderDAO.update(owner.getHomeFolder());
         applicationEventPublisher.publishEvent(new UserEvent(this, action));
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void link(Individual owner, Individual target, Collection<RoleType> types) {
         Set<RoleType> missing = new HashSet<RoleType>(types);
         for (IndividualRole role : owner.getIndividualRoles(target.getId())) {
             if (types.contains(role.getRole())) missing.remove(role.getRole());
             else owner.getIndividualRoles().remove(role);
         }
         if (missing.isEmpty()) return;
         for (RoleType type : missing) {
             IndividualRole newRole = new IndividualRole();
             newRole.setRole(type);
             newRole.setIndividualId(target.getId());
             owner.getIndividualRoles().add(newRole);
         }
         if (SecurityContext.isFrontOfficeContext()) {
             if (!UserState.NEW.equals(target.getState())) {
                 target.setState(UserState.MODIFIED);
                 target.setLastModificationDate(new Date());
                 target.setQoS(QoS.GOOD);
             }
             if (!UserState.NEW.equals(target.getHomeFolder().getState()))
                 target.getHomeFolder().setState(UserState.MODIFIED);
         }
         JsonObject payload = new JsonObject();
         JsonObject jsonResponsible = new JsonObject();
         JsonArray jsonTypes = new JsonArray();
         for (RoleType type : types) jsonTypes.add(new JsonPrimitive(type.toString()));
         jsonResponsible.add("types", jsonTypes);
         jsonResponsible.addProperty("id", owner.getId());
         jsonResponsible.addProperty("name", UserUtils.getDisplayName(owner.getId()));
         payload.add("responsible", jsonResponsible);
         UserAction action = new UserAction(UserAction.Type.MODIFICATION, target.getId(), payload);
         action = (UserAction) genericDAO.create(action);
         owner.getHomeFolder().getActions().add(action);
         homeFolderDAO.update(owner.getHomeFolder());
         applicationEventPublisher.publishEvent(new UserEvent(this, action));
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void unlink( Individual owner, Individual target) {
         Set<IndividualRole> roles = owner.getIndividualRoles(target.getId());
         if (roles.isEmpty()) return;
         Set<RoleType> deleted = new HashSet<RoleType>();
         for (IndividualRole role : roles) {
             owner.getIndividualRoles().remove(role);
             deleted.add(role.getRole());
         }
         if (SecurityContext.isFrontOfficeContext()) {
             if (!UserState.NEW.equals(target.getState())) {
                 target.setState(UserState.MODIFIED);
                 target.setLastModificationDate(new Date());
                 target.setQoS(QoS.GOOD);
             }
             if (!UserState.NEW.equals(target.getHomeFolder().getState()))
                 target.getHomeFolder().setState(UserState.MODIFIED);
         }
         JsonObject payload = new JsonObject();
         JsonObject jsonResponsible = new JsonObject();
         JsonArray jsonTypes = new JsonArray();
         for (RoleType type : deleted) jsonTypes.add(new JsonPrimitive(type.toString()));
         jsonResponsible.add("deleted", jsonTypes);
         jsonResponsible.addProperty("id", owner.getId());
         jsonResponsible.addProperty("name", UserUtils.getDisplayName(owner.getId()));
         payload.add("responsible", jsonResponsible);
         UserAction action = new UserAction(UserAction.Type.MODIFICATION, target.getId(), payload);
         action = (UserAction) genericDAO.create(action);
         owner.getHomeFolder().getActions().add(action);
         homeFolderDAO.update(owner.getHomeFolder());
         applicationEventPublisher.publishEvent(new UserEvent(this, action));
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void changeState(HomeFolder homeFolder, UserState state)
         throws CvqModelException, CvqInvalidTransitionException {
         if (!isValidTransition(homeFolder.getState(), state))
             throw new CvqInvalidTransitionException(
                 translationService.translate(
                     "user.state." + homeFolder.getState().toString().toLowerCase()),
                 translationService.translate(
                     "user.state." + state.toString().toLowerCase()));
         if (UserState.VALID.equals(state)) {
             for (Individual individual : homeFolder.getIndividuals()) {
                 if (!UserState.VALID.equals(individual.getState())
                     && !UserState.ARCHIVED.equals(individual.getState()))
                     throw new CvqModelException("");
             }
         }
         homeFolder.setState(state);
         JsonObject payload = new JsonObject();
         payload.addProperty("state", state.toString());
         UserAction action = new UserAction(UserAction.Type.STATE_CHANGE, homeFolder.getId(), payload);
         action = (UserAction) genericDAO.create(action);
         homeFolder.getActions().add(action);
         homeFolderDAO.update(homeFolder);
         applicationEventPublisher.publishEvent(new UserEvent(this, action));
     }
     
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void validateHomeFolder(@IsUser HomeFolder homeFolder) throws CvqModelException, CvqInvalidTransitionException {
         // collect individuals ids to not iterate on individuals directly
         // coz' it throws ConcurrentModificationException in called methods (changeState)
         List<Long> ids=new ArrayList<Long>();
         for (Individual i : homeFolder.getIndividuals()) {
             ids.add(i.getId());
         }
         for (Long id : ids) {
             Individual individual = userSearchService.getById(id);
             if (individual.getState().equals(UserState.NEW) 
                     || individual.getState().equals(UserState.MODIFIED)) {
                 changeState(individual,UserState.VALID);
             }
         }
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void changeState(Individual individual, UserState state)
         throws CvqModelException, CvqInvalidTransitionException {
         if (!isValidTransition(individual.getState(), state))
             throw new CvqInvalidTransitionException(
                 translationService.translate(
                     "user.state." + individual.getState().toString().toLowerCase()),
                 translationService.translate(
                     "user.state." + state.toString().toLowerCase()));
 
         HomeFolder homeFolder = individual.getHomeFolder();
 
         // if trying to archive home folder responsible, check it is the last non archived individual
         // in home folder
         Long responsibleId = userSearchService.getHomeFolderResponsible(homeFolder.getId()).getId(); 
         if (UserState.ARCHIVED.equals(state) && individual.getId().equals(responsibleId)) {
             for (Individual i : homeFolder.getIndividuals()) {
                 // check that other individuals are also archived
                 if (!UserState.ARCHIVED.equals(i.getState()) && !i.getId().equals(responsibleId))
                     throw new CvqModelException("user.state.error.mustArchiveResponsibleLast");
             }
         }
 
         if (UserState.ARCHIVED.equals(state)) {
             //Forbid to delete an adult if he's the last responsible of someone.
             if (individual.getClass().equals(Adult.class)) {
                 Set<Child> children = userSearchService.havingAsOnlyResponsible((Adult)individual);
                 if (!children.isEmpty())
                     throw new CvqModelException(translationService.translate(
                             "user.state.error.cannotDeleteLastResponsible",
                             new  Object[]{
                                     UserUtils.getDisplayName(individual.getId()),
                                     UserUtils.getDisplayName(( (Child)(children.toArray()[0]) ).getId())
                             }));
             }
             //Remove in and out roles.
             List<Individual> individualsCopy = new ArrayList<Individual>(homeFolder.getIndividuals());
             for (Individual responsible : individualsCopy) {
                 unlink(responsible, individual);
                 unlink(individual, responsible);
             }
         }
 
         // update individual state and notify
         individual.setState(state);
         individual.setLastModificationDate(new Date());
         // NdBOR : why only in Back Office context ?
         if (SecurityContext.isBackOfficeContext()) {
             individual.setQoS(null);
         }
         JsonObject payload = new JsonObject();
         payload.addProperty("state", state.toString());
         UserAction action = new UserAction(UserAction.Type.STATE_CHANGE, individual.getId(), payload);
         action = (UserAction) genericDAO.create(action);
         homeFolder.getActions().add(action);
         homeFolderDAO.update(individual.getHomeFolder());
         applicationEventPublisher.publishEvent(new UserEvent(this, action));
 
         // change home folder state if needed
         if (UserState.INVALID.equals(state) && !UserState.INVALID.equals(homeFolder.getState()))
             changeState(homeFolder, UserState.INVALID);
         else if (UserState.VALID.equals(state) || UserState.ARCHIVED.equals(state)) {
             UserState homeFolderState = state;
             for (Individual i : homeFolder.getIndividuals()) {
                 if (UserState.VALID.equals(i.getState())) {
                     homeFolderState = UserState.VALID;
                 } else if (!UserState.ARCHIVED.equals(i.getState())) {
                     homeFolderState = null;
                     break;
                 }
             }
             if (homeFolderState != null && !homeFolderState.equals(homeFolder.getState()))
                 changeState(individual.getHomeFolder(), homeFolderState);
         }
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void delete(Long id) {
         delete(userSearchService.getHomeFolderById(id));
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void delete(Individual individual) {
         HomeFolder homeFolder = individual.getHomeFolder();
         for (Individual responsible : homeFolder.getIndividuals()) {
             unlink(responsible, individual);
         }
         UserAction action = new UserAction(UserAction.Type.DELETION, individual.getId());
         action = (UserAction) genericDAO.create(action);
         applicationEventPublisher.publishEvent(new UserEvent(this, action));
         homeFolder.getActions().add(action);
         homeFolder.getIndividuals().remove(individual);
         individual.setAddress(null);
         individual.setHomeFolder(null);
         individualDAO.delete(individual);
         homeFolderDAO.update(homeFolder);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void delete(HomeFolder homeFolder) {
         applicationEventPublisher.publishEvent(new UserEvent(this, new UserAction(UserAction.Type.DELETION, homeFolder.getId())));
         List<Individual> individuals = homeFolder.getIndividuals();
 
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
             delete(adult);
         }
 
         for (Child child : children) {
             delete(child);
         }
 
         homeFolderDAO.delete(homeFolder);
     }
 
     @Override
     @Async
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void importHomeFolders(HomeFolderImportDocument doc)
         throws CvqException, IOException {
         SecurityContext.setCurrentContext(SecurityContext.ADMIN_CONTEXT);
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
                 Adult responsible = null;
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
                         boolean isResponsible = false;
                         Iterator<IndividualRole> it = a.getIndividualRoles().iterator();
                         while (it.hasNext()) {
                             if (RoleType.HOME_FOLDER_RESPONSIBLE.equals(it.next().getRole())) {
                                 if (responsible != null)
                                     throw new CvqModelException("homeFolder.error.onlyOneResponsibleIsAllowed");
                                 isResponsible = true;
                                 it.remove();
                             }
                         }
                         if (isResponsible) responsible = a; else adults.add(a);
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
                 if (responsible == null)
                     throw new CvqModelException("homeFolder.error.responsibleIsRequired");
                 HomeFolder result = create(responsible, false);
                 HibernateUtil.getSession().flush();
                 for (Adult a : adults) add(result, a, false);
                 adults.add(responsible);
                 for (Child c : children) {
                     add(result, c);
                     for (Adult a : adults) {
                         List<RoleType> roles = new ArrayList<RoleType>();
                         Iterator<IndividualRole> it = a.getIndividualRoles().iterator();
                         while (it.hasNext()) {
                             IndividualRole role = it.next();
                            if (role.getIndividualName() != null
                                    && c.getFullName().toUpperCase().equals(role.getIndividualName().toUpperCase())) {
                                 roles.add(role.getRole());
                                 it.remove();
                                 genericDAO.delete(role);
                             }
                         }
                         if (!roles.isEmpty()) {
                             for (RoleType role : roles) link(a, c, Collections.singleton(role));
                         }
                     }
                 }
                 HibernateUtil.getSession().flush();
                 for (Individual i : individuals) {
                     List<String> errors = userService.validate(i);
                     if (!errors.isEmpty()) throw new CvqValidationException(errors);
                 }
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
                     // FIXME attribute all actions to the external service
                     Gson gson = new Gson();
                     for (UserAction action : result.getActions()) {
                         JsonObject payload = JSONUtils.deserialize(action.getData());
                         JsonObject user = payload.getAsJsonObject("user");
                         user.addProperty("name", homeFolderMapping.getExternalServiceLabel());
                         action.setData(gson.toJson(payload));
                     }
                     homeFolderDAO.update(result);
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
 
     @Override
     public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
         this.applicationEventPublisher = applicationEventPublisher;
     }
 
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
 
     public void setAuthenticationService(IAuthenticationService authenticationService) {
         this.authenticationService = authenticationService;
     }
 
     public void setMailService(IMailService mailService) {
         this.mailService = mailService;
     }
 
     public void setTranslationService(ITranslationService translationService) {
         this.translationService = translationService;
     }
 
     public void setUserService(IUserService userService) {
         this.userService = userService;
     }
 
     public void setUserNotificationService(IUserNotificationService userNotificationService) {
         this.userNotificationService = userNotificationService;
     }
 
     public void setUserSearchService(IUserSearchService userSearchService) {
         this.userSearchService = userSearchService;
     }
 
     public void setHomeFolderDAO(IHomeFolderDAO homeFolderDAO) {
         this.homeFolderDAO = homeFolderDAO;
     }
 
     public void setIndividualDAO(IIndividualDAO individualDAO) {
         this.individualDAO = individualDAO;
     }
 
     public IGenericDAO getGenericDAO() {
         return genericDAO;
     }
 
     public void setGenericDAO(IGenericDAO genericDAO) {
         this.genericDAO = genericDAO;
     }
 }
