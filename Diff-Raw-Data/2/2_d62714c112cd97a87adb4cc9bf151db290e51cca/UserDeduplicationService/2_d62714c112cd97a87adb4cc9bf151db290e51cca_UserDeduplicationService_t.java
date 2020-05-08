 package fr.cg95.cvq.service.users.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationEventPublisher;
 import org.springframework.context.ApplicationEventPublisherAware;
 import org.springframework.context.ApplicationListener;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.IndividualRole;
 import fr.cg95.cvq.business.users.UserAction;
 import fr.cg95.cvq.business.users.UserEvent;
 import fr.cg95.cvq.business.users.UserState;
 import fr.cg95.cvq.dao.users.IAdultDAO;
 import fr.cg95.cvq.dao.users.IChildDAO;
 import fr.cg95.cvq.dao.users.IHomeFolderDAO;
 import fr.cg95.cvq.dao.users.IIndividualDAO;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.users.IUserDeduplicationService;
 import fr.cg95.cvq.service.users.IUserSearchService;
 import fr.cg95.cvq.service.users.IUserWorkflowService;
 import fr.cg95.cvq.util.JSONUtils;
 
 public class UserDeduplicationService implements ApplicationListener<UserEvent>, ApplicationEventPublisherAware, IUserDeduplicationService{
 
     private static Logger logger = Logger.getLogger(UserDeduplicationService.class);
     
     private ApplicationEventPublisher applicationEventPublisher;
 
     private IAdultDAO adultDAO;
     private IChildDAO childDAO;
     private IIndividualDAO individualDAO;
     private IHomeFolderDAO homeFolderDAO;
     private IUserSearchService userSearchService;
     private IUserWorkflowService userWorkflowService;
     
     @Override
     public void onApplicationEvent(UserEvent userEvent) {
         if (userEvent.getAction().getType().equals(UserAction.Type.CREATION)) {
             Individual individual =  userSearchService.getById(userEvent.getAction().getTargetId());
             if (individual == null) {
                 logger.debug("onApplicationEvent() ignoring creation event on home folder");
                 return;
             } else if (individual.getHomeFolder().isTemporary()) {
                 logger.debug("onApplicationEvent() ignoring creation event on temporary home folder");
                 return;
             }
             
             if (individual instanceof Adult) {
                 findAdultDuplicates((Adult) individual);
             } else {
                 Adult homeFolderResponsible = userSearchService.getHomeFolderResponsible(individual.getHomeFolder().getId());
                 for (Long homeFolderId : getResponsibleDuplicatedHomeFolders(homeFolderResponsible.getId()))
                     findChildDuplicates((Child) individual, homeFolderId);
             }
         }
     }
 
     private void findChildDuplicates(Child child, Long targetHomeFolderId) {
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("lastName", child.getLastName());
         parameters.put("firstName", child.getFirstName());
         parameters.put("birthDate", child.getBirthDate());
         parameters.put("homeFolderId", targetHomeFolderId);
         List<Child> duplicates = childDAO.findDuplicates(parameters);
         if (duplicates != null && !duplicates.isEmpty()) {
             Map<Long, Map<String, String>> duplicatesMap =
                 new HashMap<Long, Map<String, String>>();
             for (Child duplicate : duplicates) {
                 Map<String, String> homeFolderEntry = new HashMap<String, String>();
                 homeFolderEntry.put("id", duplicate.getId().toString());
                 int rank = 0;
                 if (duplicate.getFirstName().equals(child.getFirstName())) {
                     rank++;
                     homeFolderEntry.put("firstName", duplicate.getFirstName());
                 }
                 if (duplicate.getLastName().equals(child.getLastName())) {
                     rank++;
                     homeFolderEntry.put("lastName", duplicate.getLastName());
                 }
                 if (duplicate.getBirthDate().getTime() == child.getBirthDate().getTime()) {
                     rank++;
                     homeFolderEntry.put("birthDate", duplicate.getBirthDate().toString());
                 }
                 homeFolderEntry.put("rank", String.valueOf(rank));
 
                 duplicatesMap.put(duplicate.getHomeFolder().getId(), homeFolderEntry);
             }
 
             Gson gson = new Gson();
             child.setDuplicateAlert(true);
             child.setDuplicateData(gson.toJson(duplicatesMap));
             childDAO.update(child);
         }
     }
 
     private void findAdultDuplicates(Adult adult) {
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("lastName", adult.getLastName());
         parameters.put("firstName", adult.getFirstName());
         parameters.put("email", adult.getEmail());
         parameters.put("address", adult.getAddress().getStreetName());
         List<Adult> duplicates = adultDAO.findDuplicates(parameters);
         if (duplicates != null && !duplicates.isEmpty()) {
             Map<Long, Map<String, String>> duplicatesMap =
                 new HashMap<Long, Map<String, String>>();
             for (Adult duplicate : duplicates) {
                 if (duplicate.getId().equals(adult.getId()))
                     continue;
                 duplicatesMap.put(duplicate.getHomeFolder().getId(), new HashMap<String, String>());
                 Map<String, String> homeFolderEntry = duplicatesMap.get(duplicate.getHomeFolder().getId());
                 homeFolderEntry.put("id", duplicate.getId().toString());
                 int rank = 0;
                 if (duplicate.getLastName().equals(adult.getLastName())) {
                     rank++;
                     homeFolderEntry.put("lastName", duplicate.getLastName());
                 }
                 if (duplicate.getFirstName().equals(adult.getFirstName())) {
                     rank++;
                     homeFolderEntry.put("firstName", duplicate.getFirstName());
                 }
                 if (duplicate.getEmail().equals(adult.getEmail())) {
                     rank++;
                     homeFolderEntry.put("email", duplicate.getEmail());
                 }
                 if (duplicate.getAddress().getStreetName().equals(adult.getAddress().getStreetName())) {
                     rank++;
                     homeFolderEntry.put("streetName", duplicate.getAddress().getStreetName());
                 }
                 homeFolderEntry.put("rank", String.valueOf(rank));
             }
 
             if (!duplicatesMap.isEmpty()) {
                 Gson gson = new Gson();
                 adult.setDuplicateAlert(true);
                 adult.setDuplicateData(gson.toJson(duplicatesMap));
                 adultDAO.update(adult);
             }
         }
     }
 
     private List<Long> getResponsibleDuplicatedHomeFolders(Long homeFolderResponsibleId) {
         Adult adult = userSearchService.getAdultById(homeFolderResponsibleId);
         if (adult.getDuplicateData() == null)
             return Collections.emptyList();
         Map<Long, Map<String, String>> data = JSONUtils.deserializeAsArray(adult.getDuplicateData());
         List<Long> homeFolderIds = new ArrayList<Long>();
         for (Long homeFolderId : data.keySet()) {
             homeFolderIds.add(homeFolderId);
         }
         
         return homeFolderIds;
     }
 
     @Override
     @Context(types={ContextType.AGENT}, privilege=ContextPrivilege.WRITE)
     public Map<String, Long> countHomeFolderDuplicates(Long homeFolderId,
             Long duplicateHomeFolderId) {
         Map<String, Long> resultsByIndividualType = new HashMap<String, Long>();
         resultsByIndividualType.put("adults", new Long(0));
         resultsByIndividualType.put("children", new Long(0));
         for (Individual individual : userSearchService.getIndividuals(homeFolderId)) {
             if (individual.getDuplicateData() == null)
                 continue;
             Map<Long, Map<String, String>> data = JSONUtils.deserializeAsArray(individual.getDuplicateData());
             for (Long tempHomeFolderId : data.keySet()) {
                 if (tempHomeFolderId.equals(duplicateHomeFolderId)) {
                     if (individual instanceof Adult)
                         resultsByIndividualType.put("adults", resultsByIndividualType.get("adults") + 1);
                     else if (individual instanceof Child)
                         resultsByIndividualType.put("children", resultsByIndividualType.get("children") + 1);
                 }
             }
         }
 
         return resultsByIndividualType;
     }
 
     @Override
    @Context(types={ContextType.AGENT}, privilege=ContextPrivilege.READ)
     public List<String> getHomeFolderDuplicates(Long homeFolderId,
             Long duplicateHomeFolderId) {
         List<String> resultsByIndividualType = new ArrayList<String>();
         for (Individual individual : userSearchService.getIndividuals(homeFolderId)) {
             if (individual.getDuplicateData() == null)
                 continue;
             Map<Long, Map<String, String>> data = JSONUtils.deserializeAsArray(individual.getDuplicateData());
             for (Long tempHomeFolderId : data.keySet()) {
                 if (tempHomeFolderId.equals(duplicateHomeFolderId)) {
                     resultsByIndividualType.add(individual.getFullName());
                 }
             }
         }
 
         return resultsByIndividualType;
     }
 
     @Override
     @Context(types={ContextType.AGENT}, privilege=ContextPrivilege.WRITE)
     public void removeDuplicate(Long homeFolderId, Long targetHomeFolderId) {
         Gson gson = new Gson();
         for (Individual individual : userSearchService.getIndividuals(homeFolderId)) {
             if (individual.getDuplicateData() == null)
                 continue;
             Map<Long, Map<String, String>> data = JSONUtils.deserializeAsArray(individual.getDuplicateData());
             if (!data.containsKey(targetHomeFolderId))
                 continue;
             data.remove(targetHomeFolderId);
             if (data.isEmpty()) {
                 individual.setDuplicateData(null);
                 individual.setDuplicateAlert(false);
             } else {
                 individual.setDuplicateData(gson.toJson(data));
             }
             adultDAO.update(individual);
         }
         
     }
 
     @Override
     public void mergeDuplicate(Long homeFolderId, Long targetHomeFolderId)
         throws CvqModelException, CvqInvalidTransitionException {
 
         HomeFolder homeFolder = userSearchService.getHomeFolderById(homeFolderId);
         HomeFolder targetHomeFolder = userSearchService.getHomeFolderById(targetHomeFolderId);
         Individual homeFolderResponsible = userSearchService.getHomeFolderResponsible(homeFolderId);
 
         // collect individuals ids to not iterate on individuals directly
         // coz' it throws ConcurrentModificationException in called methods (eg changeState)
         List<Long> ids=new ArrayList<Long>();
         for (Individual i : homeFolder.getIndividuals()) {
             ids.add(i.getId());
         }
 
         // make a first pass to deal with individual roles
         List<Long> moved = new ArrayList<Long>();
         Map<Long, Long> merged = new HashMap<Long, Long>();
         for (Long id : ids) {
 
             // check if it is a move or a merge
             boolean moveIndividual = false;
             Map<Long, Map<String, String>> data = 
                 JSONUtils.deserializeAsArray(userSearchService.getById(id).getDuplicateData());
             if (data == null || !data.containsKey(targetHomeFolder.getId()))
                 moveIndividual = true;
             
             if (moveIndividual) {
                 moved.add(id);
             } else {
                 merged.put(id, Long.valueOf(data.get(targetHomeFolder.getId()).get("id")));
             }
         }
         for (Individual owner : homeFolder.getIndividuals()) {
             boolean ownerMoved = false;
             boolean targetMoved = false;
             List<IndividualRole> rolesToRemove = new ArrayList<IndividualRole>();
             if (moved.contains(owner.getId()))
                 ownerMoved = true;
             for (IndividualRole role : owner.getIndividualRoles()) {
                 if (moved.contains(role.getIndividualId()))
                     targetMoved = true;
                 if (ownerMoved && !targetMoved) {
                     role.setIndividualId(merged.get(role.getIndividualId()));
                     individualDAO.update(owner);
                 } else if (!ownerMoved && targetMoved) {
                     Individual targetIndividual = userSearchService.getById(merged.get(owner.getId()));
                     IndividualRole newRole = new IndividualRole();
                     newRole.setIndividualId(role.getIndividualId());
                     newRole.setRole(role.getRole());
                     targetIndividual.getIndividualRoles().add(newRole);
                     individualDAO.update(targetIndividual);
                     rolesToRemove.add(role);
                 }
             }
             for (IndividualRole role : rolesToRemove)
                 owner.getIndividualRoles().remove(role);
         }
 
         for (Long id : ids) {
 
             // responsible must be dealt with last because we have to archive it after all other individuals
             if (id.equals(homeFolderResponsible.getId()))
                 continue;
 
             Individual individual = userSearchService.getById(id);
             moveOrMergeIndividual(individual, targetHomeFolder);
         }
 
         moveOrMergeIndividual(homeFolderResponsible, targetHomeFolder);
         
         JsonObject payload = new JsonObject();
         payload.addProperty("merge", targetHomeFolderId);
         Gson gson = new Gson();
         payload.addProperty("mergedIndividuals", gson.toJson(merged));
         payload.addProperty("movedIndividuals", gson.toJson(moved));
         UserAction action = new UserAction(UserAction.Type.MERGE, homeFolderId, payload);
         homeFolder.getActions().add(action);
         homeFolderDAO.update(homeFolder);
         applicationEventPublisher.publishEvent(new UserEvent(this, action));
         
         // eventually change state of target home folder according state of moved individuals
         boolean hasNewOrModifiedIndividual = false;
         boolean hasInvalidIndividual = false;
         for (Long id : moved) {
             Individual individual = userSearchService.getById(id);
             if (individual.getState().name().equals(UserState.NEW.name())
                     || individual.getState().name().equals(UserState.MODIFIED.name()))
                 hasNewOrModifiedIndividual = true;
             else if (individual.getState().name().equals(UserState.INVALID.name()))
                 hasInvalidIndividual = true;
         }
         if (hasInvalidIndividual) {
             targetHomeFolder.setState(UserState.INVALID);
             homeFolderDAO.update(targetHomeFolder);
         } else if (hasNewOrModifiedIndividual) {
             targetHomeFolder.setState(UserState.MODIFIED);
             homeFolderDAO.update(targetHomeFolder);
         }
 
         userWorkflowService.changeState(homeFolder, UserState.ARCHIVED);
     }
 
     private void moveOrMergeIndividual(Individual individual, HomeFolder targetHomeFolder) 
         throws CvqModelException, CvqInvalidTransitionException {
 
         HomeFolder homeFolder = individual.getHomeFolder();
 
         // check if it is a move or a merge
         boolean moveIndividual = false;
         Map<Long, Map<String, String>> data = JSONUtils.deserializeAsArray(individual.getDuplicateData());
         if (data == null || !data.containsKey(targetHomeFolder.getId()))
             moveIndividual = true;
 
         if (moveIndividual) {
             // was not a duplicate, just change its home folder
             individual.setHomeFolder(targetHomeFolder);
             targetHomeFolder.getIndividuals().add(individual);
             homeFolder.getIndividuals().remove(individual);
 
             individualDAO.update(individual);
             homeFolderDAO.update(targetHomeFolder);
             homeFolderDAO.update(homeFolder);
 
             // send an event
             JsonObject payload = new JsonObject();
             payload.addProperty("move", targetHomeFolder.getId());
             UserAction action = new UserAction(UserAction.Type.MOVE, individual.getId(), payload);
             homeFolder.getActions().add(action);
             homeFolderDAO.update(homeFolder);
             applicationEventPublisher.publishEvent(new UserEvent(this, action));
 
             payload = new JsonObject();
             payload.addProperty("move", homeFolder.getId());
             action = new UserAction(UserAction.Type.MOVE, individual.getId(), payload);
             targetHomeFolder.getActions().add(action);
             homeFolderDAO.update(targetHomeFolder);
         } else {
             Long targetIndividualId = Long.valueOf(data.get(targetHomeFolder.getId()).get("id"));
 
             JsonObject payload = new JsonObject();
             payload.addProperty("merge", targetIndividualId);
             UserAction action = new UserAction(UserAction.Type.MERGE, individual.getId(), payload);
             homeFolder.getActions().add(action);
             homeFolderDAO.update(homeFolder);
             applicationEventPublisher.publishEvent(new UserEvent(this, action));
 
             payload = new JsonObject();
             payload.addProperty("merge", individual.getId());
             action = new UserAction(UserAction.Type.MERGE, targetIndividualId, payload);
             targetHomeFolder.getActions().add(action);
             homeFolderDAO.update(targetHomeFolder);
 
             // FIXME : c/c from UserWorkflowService.changeState to avoid those nasty
             // ConcurrentModificationException
             individual.setState(UserState.ARCHIVED);
             individual.setLastModificationDate(new Date());
             individual.setQoS(null);
             payload = new JsonObject();
             payload.addProperty("state", UserState.ARCHIVED.toString());
             action = new UserAction(UserAction.Type.STATE_CHANGE, individual.getId(), payload);
             individual.getHomeFolder().getActions().add(action);
         }
         
         individual.setDuplicateAlert(false);
         individual.setDuplicateData(null);
     }
 
     public void setAdultDAO(IAdultDAO adultDAO) {
         this.adultDAO = adultDAO;
     }
 
     public void setChildDAO(IChildDAO childDAO) {
         this.childDAO = childDAO;
     }
 
     public void setHomeFolderDAO(IHomeFolderDAO homeFolderDAO) {
         this.homeFolderDAO = homeFolderDAO;
     }
 
     public void setIndividualDAO(IIndividualDAO individualDAO) {
         this.individualDAO = individualDAO;
     }
 
     public void setUserSearchService(IUserSearchService userSearchService) {
         this.userSearchService = userSearchService;
     }
 
     public void setUserWorkflowService(IUserWorkflowService userWorkflowService) {
         this.userWorkflowService = userWorkflowService;
     }
 
     @Override
     public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
         this.applicationEventPublisher = applicationEventPublisher;
     }
 }
