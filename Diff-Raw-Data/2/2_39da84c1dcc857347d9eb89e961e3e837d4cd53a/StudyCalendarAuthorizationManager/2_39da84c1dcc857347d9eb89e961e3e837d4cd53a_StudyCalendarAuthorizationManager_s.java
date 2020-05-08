 package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;
 
 import static java.util.Arrays.asList;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
 import gov.nih.nci.security.UserProvisioningManager;
 import gov.nih.nci.security.authorization.domainobjects.Group;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroupRoleContext;
 import gov.nih.nci.security.authorization.domainobjects.Role;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.dao.*;
 import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
 import gov.nih.nci.security.exceptions.CSTransactionException;
 import gov.nih.nci.security.util.ObjectSetUtil;
 import gov.nih.nci.cabig.ctms.domain.DomainObject;
 
 import java.util.*;
 
 //import org.apache.commons.logging.Log;
 //import org.apache.commons.logging.LogFactory;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Padmaja Vedula
  * @author Rhett Sutphin
  */
 
 // TODO: None of these methods should throw checked exceptions
 
 public class StudyCalendarAuthorizationManager {
 	public static final String APPLICATION_CONTEXT_NAME = "study_calendar";
 	public static final String BASE_SITE_PG = "BaseSitePG";
     public static final String ASSIGNED_USERS = "ASSIGNED_USERS";
     public static final String AVAILABLE_USERS = "AVAILABLE_USERS";
     public static final String ASSIGNED_PGS = "ASSIGNED_PGS";
     public static final String AVAILABLE_PGS = "AVAILABLE_PGS";
     public static final String ASSIGNED_PES = "ASSIGNED_PES";
     public static final String AVAILABLE_PES = "AVAILABLE_PES";
     public static final String PARTICIPANT_COORDINATOR_GROUP = "PARTICIPANT_COORDINATOR";
     public static final String SITE_COORDINATOR_GROUP = "SITE_COORDINATOR";
     
 //    private static Log log = LogFactory.getLog(StudyCalendarAuthorizationManager.class);
     private static Logger log = LoggerFactory.getLogger(StudyCalendarAuthorizationManager.class);
 
     private UserProvisioningManager userProvisioningManager;
     private SiteDao siteDao;
 
     public void assignProtectionElementsToUsers(List<String> userIds, String protectionElementObjectId) throws Exception
 	{
     	boolean protectionElementPresent = false;
     	ProtectionElement pElement = new ProtectionElement();
 			
 		try { 
 			pElement = userProvisioningManager.getProtectionElement(protectionElementObjectId);
 			protectionElementPresent = true;
 		} catch (CSObjectNotFoundException ex){
 			ProtectionElement newProtectionElement = new ProtectionElement();
 			newProtectionElement.setObjectId(protectionElementObjectId);
 			newProtectionElement.setProtectionElementName(protectionElementObjectId);
 			userProvisioningManager.createProtectionElement(newProtectionElement);
 			pElement = userProvisioningManager.getProtectionElement(protectionElementObjectId);
 			//userProvisioningManager.setOwnerForProtectionElement(protectionElementObjectId, userIds.toArray(new String[0]));
 			//userProvisioningManager.assignOwners(protectionElementObjectId, userIds.toArray(new String[0]));
 		}
 		userProvisioningManager.assignOwners(pElement.getProtectionElementId().toString(), userIds.toArray(new String[0]));
 		/*if (protectionElementPresent)
 		{
 			if (log.isDebugEnabled()) {
 				log.debug(" The given Protection Element: " + userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is present in Database");
 			}
 			for (String userId : userIds)
 			{
 				String userName = getUserObject(userId).getLoginName();
 				if (!(userProvisioningManager.checkOwnership((String)userName, protectionElementObjectId)))
 				{
 					if (log.isDebugEnabled()) {
 						log.debug(" Given Protection Element: " + userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is not owned by " + userName);
 					}
 					userProvisioningManager.setOwnerForProtectionElement((String)userName, protectionElementObjectId, userProvisioningManager.getProtectionElement(protectionElementObjectId).getAttribute());
 				} else {
 					if (log.isDebugEnabled()) {
 						log.debug(" Given Protection Element: " + userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is owned by " + userName);
 					}
 				}
 			
 			}
 		}*/
 	}
     	
     public void assignMultipleProtectionElements(String userId, List<String> protectionElementObjectIds) throws Exception
 	{
     	boolean protectionElementPresent = false;
     	String userName = getUserObject(userId).getLoginName();
 		for (String protectionElementObjectId : protectionElementObjectIds)	{
 			try { 
 				userProvisioningManager.getProtectionElement(protectionElementObjectId);
 				protectionElementPresent = true;
 			} catch (CSObjectNotFoundException ex){
 				ProtectionElement newProtectionElement = new ProtectionElement();
 				newProtectionElement.setObjectId(protectionElementObjectId);
 				newProtectionElement.setProtectionElementName(protectionElementObjectId);
 				userProvisioningManager.createProtectionElement(newProtectionElement);
 				//protection element attribute name is set to be the same as protection element object id
 				userProvisioningManager.setOwnerForProtectionElement(userName, protectionElementObjectId, protectionElementObjectId);
 			}
 		
 			if (protectionElementPresent)
 			{
 				if (log.isDebugEnabled()) {
 					log.debug(" The given Protection Element: " + userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is present in Database");
 				}
 				if (!(userProvisioningManager.checkOwnership(userName, protectionElementObjectId)))
 				{
 					if (log.isDebugEnabled()) {
 						log.debug(" Given Protection Element: " + userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is not owned by " + userName);
 					}
 					userProvisioningManager.setOwnerForProtectionElement(userName, protectionElementObjectId, userProvisioningManager.getProtectionElement(protectionElementObjectId).getAttribute());
 				} else {
 					if (log.isDebugEnabled()) 
 						log.debug(" Given Protection Element: " + userProvisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is owned by " + userName);
 				}
 			}
 		}
 	}
     
     //get users of a group, associated with a protection element, and also those not associated
 	public Map getUsers(String groupName, String pgId, String pgName) throws Exception {
 		HashMap<String, List> usersMap = new HashMap<String, List>();
 		List<User> usersForRequiredGroup = getUsersForGroup(groupName);
         usersMap = (HashMap) getUserListsForProtectionElement(usersForRequiredGroup, pgId, pgName);
 		
         
 		return usersMap;
 	}
     
 	
 	private Map getUserListsForProtectionElement(List<User> users, String pgpeId, String pgName) throws Exception {
 		HashMap<String, List> userHashMap = new HashMap<String, List>();
 		List<User> assignedUsers = new ArrayList<User>();
 		List<User> availableUsers = new ArrayList<User>();
 		ProtectionGroup pGroup = getPGByName(pgName);
 		List<User> usersForSite = new ArrayList<User>();
 		for (User user : users) {
 			Set<ProtectionGroupRoleContext> pgRoleContext = getProtectionGroupRoleContexts(user);
             for (ProtectionGroupRoleContext pgrc : pgRoleContext) {
                 if (pgrc.getProtectionGroup().getProtectionGroupName().equals(pgName)) {
                     usersForSite.add(user);
                 }
             }
 		}
 		for (User userSite : usersForSite) {
 			Set<ProtectionGroupRoleContext> userSiteRoleContext = getProtectionGroupRoleContexts(userSite);
             for (ProtectionGroupRoleContext pgrcSite : userSiteRoleContext) {
                 if (pgrcSite.getProtectionGroup().getProtectionGroupName().equals(pgpeId)) {
                     assignedUsers.add(userSite);
                 }
             }
 		}
 		availableUsers  = (List) ObjectSetUtil.minus(usersForSite, assignedUsers);
 		
 		userHashMap.put(ASSIGNED_USERS, assignedUsers);
 		userHashMap.put(AVAILABLE_USERS, availableUsers);
 		return userHashMap;
 	}
 	
     public User getUserObject(String id) throws Exception {
     	User user = null;
       	user = userProvisioningManager.getUserById(id);
       	return user;
     }
 
     public void createProtectionGroup(String newProtectionGroup) throws Exception {
         ProtectionGroup requiredProtectionGroup = new ProtectionGroup();
         requiredProtectionGroup.setProtectionGroupName(newProtectionGroup);
         userProvisioningManager.createProtectionGroup(requiredProtectionGroup);
         if (log.isDebugEnabled()) {
             log.debug("new protection group created " + newProtectionGroup);
         }
     }
     
     /**
      * Method to retrieve all site protection groups
      * 
      */
     
     public List getSites() throws Exception {
     	List<ProtectionGroup> siteList = new ArrayList<ProtectionGroup>() ;
 		ProtectionGroup protectionGroup = new ProtectionGroup();
         SearchCriteria pgSearchCriteria = new ProtectionGroupSearchCriteria(protectionGroup);
 		List<ProtectionGroup> pgList = userProvisioningManager.getObjects(pgSearchCriteria);
 			
 		if (pgList.size() > 0) {
 			for (ProtectionGroup requiredProtectionGroup : pgList) {
			   if ((requiredProtectionGroup.getParentProtectionGroup()!=null) && (isSitePG(requiredProtectionGroup))) {
 				   siteList.add(requiredProtectionGroup);
 			   }
 			}
 		}
 
 		return siteList;
     }
     
     /**
      * Method to retrieve a site protection group
      * @param name
      * @return null or site Protection Group
      * 
      */
     
     public ProtectionGroup getPGByName(String name) throws Exception {
     	ProtectionGroup requiredProtectionGroup = null;
     	
 		ProtectionGroup protectionGroupSearch = new ProtectionGroup();
 		protectionGroupSearch.setProtectionGroupName(name);
 	    SearchCriteria protectionGroupSearchCriteria = new ProtectionGroupSearchCriteria(protectionGroupSearch);
 		List<ProtectionGroup> protectionGroupList = userProvisioningManager.getObjects(protectionGroupSearchCriteria);
 			
 		if (protectionGroupList.size() > 0) {
 			requiredProtectionGroup = protectionGroupList.get(0);
 			
 		}
 		return requiredProtectionGroup;
     }
     
     public List<User> getUsersForGroup(String groupName) {
 		List<User> usersForRequiredGroup = new ArrayList<User>(); 
 		User user = new User();
         SearchCriteria userSearchCriteria = new UserSearchCriteria(user);
 		List<User> userList = userProvisioningManager.getObjects(userSearchCriteria);
         for (User requiredUser : userList) {
             Set<Group> userGroups = getGroups(requiredUser);
             for (Group userGroup : userGroups) {
                 if (userGroup.getGroupName().equals(groupName)) {
                     usersForRequiredGroup.add(requiredUser);
                     break;
                 }
             }
         }
 		return usersForRequiredGroup;
     }
     
     /**
      * Method to retrieve users who have the given protection group assigned to them.
      * (can be used for retrieving site coordinators for site protection groups)
      * @param group
      * @param protectionGroupName
      * @return
      * @throws Exception
      */
     public Map getUserPGLists(String group, String protectionGroupName) throws Exception {
 		List<User> usersForRequiredGroup = getUsersForGroup(group);
 		return getUserListsForProtectionGroup(usersForRequiredGroup, protectionGroupName);
     }
     
     /**
      * 
      * @param users
      * @param protectionGroupName
      * @return
      * @throws Exception
      */
     
     private Map getUserListsForProtectionGroup(List<User> users, String protectionGroupName) throws Exception {
 		HashMap<String, List> userHashMap = new HashMap<String, List>();
 		List<User> assignedUsers = new ArrayList<User>();
 		List<User> availableUsers = new ArrayList<User>();
 		for (User user : users)
 		{
 			boolean isAssigned = false;
             Set<ProtectionGroupRoleContext> pgRoleContext = getProtectionGroupRoleContexts(user);
 			List<ProtectionGroupRoleContext> pgRoleContextList = new ArrayList(pgRoleContext);
 			if (pgRoleContextList.size() != 0) {
 				for (ProtectionGroupRoleContext pgrc : pgRoleContextList) {
 					if (pgrc.getProtectionGroup().getProtectionGroupName().equals(protectionGroupName)) {
 						assignedUsers.add(user);
 						isAssigned = true;
 						break;
 					} 
 				}
 				if (!isAssigned) {
 					availableUsers.add(user);
 				}
 			} else { 
 				availableUsers.add(user);
 			}
 		}
 		userHashMap.put(ASSIGNED_USERS, assignedUsers);
 		userHashMap.put(AVAILABLE_USERS, availableUsers);
 		return userHashMap;
 	}
 
     public void assignProtectionGroupsToUsers(String userId, ProtectionGroup protectionGroup, String roleName) throws Exception {
         assignProtectionGroupsToUsers(asList(userId), protectionGroup, roleName);
     }
 
     public void assignProtectionGroupsToUsers(List<String> userIds, ProtectionGroup protectionGroup, String roleName) throws Exception
 	{
         if (protectionGroup == null) return;
         
         Role role = new Role();
 		role.setName(roleName);
 		SearchCriteria roleSearchCriteria = new RoleSearchCriteria(role);
 		List roleList = userProvisioningManager.getObjects(roleSearchCriteria);
 		if (roleList.size() > 0) {
 			Role accessRole = (Role) roleList.get(0);
 			String[] roleIds = new String[] {accessRole.getId().toString()};
 
 			for (String userId : userIds)
 			{
 				userProvisioningManager.assignUserRoleToProtectionGroup(userId, roleIds, protectionGroup.getProtectionGroupId().toString());
 			}
 		}
 	}
 
     public void assignProtectionGroupsToUsers(List<String> userIds, ProtectionGroup protectionGroup, String[] roleNames) throws Exception
 	{
         if (protectionGroup == null) return;
 
         List<Role> roleList = new ArrayList<Role>();
         for (String roleStr : roleNames ) {
             Role searchRole = new Role();
 		    searchRole.setName(roleStr);
             SearchCriteria roleSearchCriteria = new RoleSearchCriteria(searchRole);
 		    roleList.addAll(userProvisioningManager.getObjects(roleSearchCriteria));
         }
 
 
 		if (roleList.size() > 0) {
             String[] roleIds = new String[roleList.size()];
             Iterator<Role> role = roleList.iterator();
             for (int i = 0; i < roleIds.length; i++) {
                 roleIds[i] = role.next().getId().toString();
             }
 
 			for (String userId : userIds)
 			{
 				userProvisioningManager.assignUserRoleToProtectionGroup(userId, roleIds, protectionGroup.getProtectionGroupId().toString());
 			}
 		}
 	}
     
     public void removeProtectionGroupUsers(List<String> userIds, ProtectionGroup protectionGroup) throws Exception
     {
         if (protectionGroup == null) return;
 
         if (!((userIds.size() == 1) && (userIds.get(0).equals("")))) {
     		for (String userId : userIds)
     		{
     			userProvisioningManager.removeUserFromProtectionGroup(protectionGroup.getProtectionGroupId().toString(), userId);
     		}
     	}
     }
 
     public void registerUrl(String url, List<String> protectionGroups) {
         if (log.isDebugEnabled()) log.debug("Attempting to register PE for " + url + " in " + protectionGroups);
 
         ProtectionElement element = getOrCreateProtectionElement(url);
 
         syncProtectionGroups(element, protectionGroups);
     }
 
     private ProtectionElement getOrCreateProtectionElement(String objectId) {
         ProtectionElement element = null;
         try {
             element = userProvisioningManager.getProtectionElement(objectId);
             log.debug("PE for " + objectId + " found");
         } catch (CSObjectNotFoundException e) {
             log.debug("PE for " + objectId + " not found");
             // continue
         }
         if (element == null) {
             element = new ProtectionElement();
             element.setObjectId(objectId);
             element.setProtectionElementName(objectId);
             element.setProtectionElementDescription("Autogenerated PE for " + objectId);
             try {
                 userProvisioningManager.createProtectionElement(element);
             } catch (CSTransactionException e) {
                 throw new StudyCalendarSystemException("Creating PE for " + objectId + " failed", e);
             }
             try {
                 element = userProvisioningManager.getProtectionElement(element.getObjectId());
             } catch (CSObjectNotFoundException e) {
                 throw new StudyCalendarSystemException("Reloading just-created PE for " + element.getObjectId() + " failed", e);
             }
         }
         return element;
     }
 
     private void syncProtectionGroups(ProtectionElement element, List<String> desiredProtectionGroups) {
         Set<ProtectionGroup> existingGroups;
         try {
             existingGroups = userProvisioningManager.getProtectionGroups(element.getProtectionElementId().toString());
         } catch (CSObjectNotFoundException e) {
             throw new StudyCalendarError("Could not find groups for just-created/loaded PE", e);
         }
         // if they're all the same, we don't need to do anything
         if (existingGroups.size() == desiredProtectionGroups.size()) {
             List<String> existingNames = new ArrayList<String>(existingGroups.size());
             for (ProtectionGroup existingGroup : existingGroups) existingNames.add(existingGroup.getProtectionGroupName());
             if (log.isDebugEnabled()) log.debug(element.getObjectId() + " currently in " + desiredProtectionGroups);
             if (existingNames.containsAll(desiredProtectionGroups)) {
                 log.debug("Sync requires no changes");
                 return;
             }
         }
 
         if (log.isDebugEnabled()) log.debug("Setting groups for " + element.getObjectId() + " to " + desiredProtectionGroups);
         // accumulate IDs from names
         // Seriously -- there's no way to look them up by name
         List<ProtectionGroup> allGroups = userProvisioningManager.getProtectionGroups();
         List<String> desiredGroupIds = new ArrayList<String>(desiredProtectionGroups.size());
         for (ProtectionGroup group : allGroups) {
             if (desiredProtectionGroups.contains(group.getProtectionGroupName())) {
                 desiredGroupIds.add(group.getProtectionGroupId().toString());
             }
         }
         // warn about missing groups, if any
         if (desiredGroupIds.size() != desiredProtectionGroups.size()) {
             List<String> missingGroups = new LinkedList<String>(desiredProtectionGroups);
             for (ProtectionGroup group : allGroups) {
                 String name = group.getProtectionGroupName();
                 if (missingGroups.contains(name)) missingGroups.remove(name);
             }
             log.warn("Requested protection groups included one or more that don't exist:  " + missingGroups + ".  These groups were skipped.");
         }
 
         try {
             userProvisioningManager.assignToProtectionGroups(
                 element.getProtectionElementId().toString(), desiredGroupIds.toArray(new String[0]));
         } catch (CSTransactionException e) {
             throw new StudyCalendarSystemException("Assigning PE " + element.getProtectionElementName() + " to groups " + desiredProtectionGroups + " failed", e);
         }
     }
 
     public Map getPEForUserProtectionGroup(String pgName, String userId) throws Exception {
     	HashMap<String, List> peHashMap = new HashMap<String, List>();
 		List<ProtectionElement> assignedPEs = new ArrayList<ProtectionElement>();
 		List<ProtectionElement> availablePEs = new ArrayList<ProtectionElement>();
 		
 		Set<ProtectionElement> allAssignedPEsForPGs = userProvisioningManager.getProtectionElements(getPGByName(pgName).getProtectionGroupId().toString());
 		
 		for (ProtectionElement userPE : allAssignedPEsForPGs) {
 			String userName = getUserObject(userId).getLoginName();
 			if (userProvisioningManager.checkOwnership(userName, userPE.getObjectId()))
 			{
 				assignedPEs.add(userPE);
 			}
 		}
 		
 		availablePEs = (List) ObjectSetUtil.minus(allAssignedPEsForPGs, assignedPEs);
     	peHashMap.put(ASSIGNED_PES, assignedPEs);
     	peHashMap.put(AVAILABLE_PES, availablePEs);
 		return peHashMap;
 		
     }
     
     public List<ProtectionGroup> getSitePGsForUser(String userName) {
         return getSitePGs(userProvisioningManager.getUser(userName));
     }
 
     public List<ProtectionGroup> getStudySitePGsForUser(String userName) {
         User user = userProvisioningManager.getUser(userName);
         List<ProtectionGroup> studySitePGs = new ArrayList<ProtectionGroup>();
 
         for (Group group : getGroups(user)) {
             if (group.getGroupName().equals(PARTICIPANT_COORDINATOR_GROUP)) {
                 Set<ProtectionGroupRoleContext> pgRoleContexts = getProtectionGroupRoleContexts(user);
                 for (ProtectionGroupRoleContext pgrc : pgRoleContexts) {
                     if (isStudySitePG(pgrc.getProtectionGroup())) {
                         studySitePGs.add(pgrc.getProtectionGroup());
                     }
                 }
            }
         }
         return studySitePGs;
     }
 
     public List<Study> checkOwnership(String userName, List<Study> studies) {
         Set<Study> assignedStudies = new LinkedHashSet<Study>();
         User userTemplate = new User();
         userTemplate.setLoginName(userName);
         SearchCriteria userSearchCriteria = new UserSearchCriteria(userTemplate);
         List<User> userList = userProvisioningManager.getObjects(userSearchCriteria);
         if (userList.size() > 0) {
             User user = userList.get(0);
             Set<Group> userGroups = getGroups(user);
             if (userGroups.size() > 0) {
                    Group requiredGroup = userGroups.iterator().next();
                    if (requiredGroup.getGroupName().equals(PARTICIPANT_COORDINATOR_GROUP)) {
                        for (Study study : studies) {
                            if (checkParticipantCoordinatorOwnership(user, study)) {
                                assignedStudies.add(study);
                            }
                        }
                    } else if (requiredGroup.getGroupName().equals(SITE_COORDINATOR_GROUP)) {
                        List<ProtectionGroup> sites = getSitePGs(user);
                        for (Study study : studies) {
                            if (checkSiteCoordinatorOwnership(study, sites)) {
                                assignedStudies.add(study);
                            }
                        }
                    } else {
                        assignedStudies.addAll(studies);
                    }
             }
         }
 
         return new ArrayList<Study>(assignedStudies);
     }
 
     public void removeProtectionGroup(String protectionGroupName) throws Exception {
 		ProtectionGroup pg = new ProtectionGroup();
 		pg.setProtectionGroupName(protectionGroupName);
         SearchCriteria pgSearchCriteria = new ProtectionGroupSearchCriteria(pg);
         List<ProtectionGroup> pgList = userProvisioningManager.getObjects(pgSearchCriteria);
         if (pgList.size() > 0) {
         	userProvisioningManager.removeProtectionGroup(pgList.get(0).getProtectionGroupId().toString());
         }
         
     }
     
     public void createAndAssignPGToUser(List<String> userIds, String protectionGroupName, String roleName) throws Exception {
     	ProtectionGroup pg = new ProtectionGroup();
 		pg.setProtectionGroupName(protectionGroupName);
         SearchCriteria pgSearchCriteria = new ProtectionGroupSearchCriteria(pg);
         List<ProtectionGroup> pgList = userProvisioningManager.getObjects(pgSearchCriteria);
         if (pgList.size() <= 0) {
         	ProtectionGroup requiredProtectionGroup = new ProtectionGroup();
 			requiredProtectionGroup.setProtectionGroupName(protectionGroupName);
 			userProvisioningManager.createProtectionGroup(requiredProtectionGroup);
         } 
         Role role = new Role();
 		role.setName(roleName);
 		SearchCriteria roleSearchCriteria = new RoleSearchCriteria(role);
 		List roleList = userProvisioningManager.getObjects(roleSearchCriteria);
 		if (roleList.size() > 0) {
 			Role accessRole = (Role) roleList.get(0);
 			String[] roleIds = new String[] {accessRole.getId().toString()};
             if (!((userIds.size() == 1) && (userIds.get(0).equals("")))) {
             	for (String userId : userIds)
             	{
             		userProvisioningManager.assignUserRoleToProtectionGroup(userId, roleIds, getPGByName(protectionGroupName).getProtectionGroupId().toString());
             	}
             }
 		}
     }
 
     public boolean isUserPGAssigned(String pgName, String userId) throws Exception {
         Set<ProtectionGroupRoleContext> pgRoleContext = userProvisioningManager.getProtectionGroupRoleContextForUser(userId);
 		   List<ProtectionGroupRoleContext> pgRoleContextList = new ArrayList<ProtectionGroupRoleContext> (pgRoleContext);
 		   if (pgRoleContextList.size() != 0) {
 			   for (ProtectionGroupRoleContext pgrc : pgRoleContextList) {
 				    if (pgrc.getProtectionGroup().getProtectionGroupName().equals(pgName)) {
 						return true;
 					}
 			   }
 		   }
     	return false;
     }
     
     public User getUserForLogin(String userName) {
     	return userProvisioningManager.getUser(userName);
     }
 
 
 
     public void assignCsmGroups(edu.northwestern.bioinformatics.studycalendar.domain.User user, Set<UserRole> userRoles) throws Exception {
         List<String> csmRoles = rolesToCsmGroups(userRoles);
         String[] strCsmRoles = csmRoles.toArray(new String[csmRoles.size()]);
         userProvisioningManager.assignGroupsToUser(user.getCsmUserId().toString(), strCsmRoles);
     }
 
     private List<String> rolesToCsmGroups(Set<UserRole> userRoles) throws Exception{
         List csmGroupsForUser = new ArrayList<String>();
         if(userRoles != null) {
             List<Group> allCsmGroups = getAllCsmGroups();
 
             for(UserRole userRole: userRoles) {
                 for(Group group: allCsmGroups) {
                     if(isGroupEqualToRole(group, userRole.getRole())) {
                         csmGroupsForUser.add(group.getGroupId().toString());
                     }
                 }
             }
         }
         return csmGroupsForUser;
     }
 
 
     ////// INTERNAL HELPERS
 
     protected boolean isGroupEqualToRole(Group group, edu.northwestern.bioinformatics.studycalendar.domain.Role role) {
         return group.getGroupName().equals(role.getCode());
     }
 
     @SuppressWarnings("unchecked")
     private List<Group> getAllCsmGroups() throws Exception {
         SearchCriteria searchCriteria = new GroupSearchCriteria(new Group());
         List<Group> groups = userProvisioningManager.getObjects(searchCriteria);
         if(groups == null) {
             throw new StudyCalendarSystemException("Get Csm Groups is null");
         }
         return groups;
     }
     
     private List<ProtectionGroup> getSitePGs(User user) {
         List<ProtectionGroup> sites = new ArrayList<ProtectionGroup>();
         Set<ProtectionGroupRoleContext> pgRoleContext = getProtectionGroupRoleContexts(user);
         if (pgRoleContext.size() != 0) {
             for (ProtectionGroupRoleContext pgrc : pgRoleContext) {
                  if (isSitePG(pgrc.getProtectionGroup())) {
                      sites.add(pgrc.getProtectionGroup());
                  }
             }
         }
         return sites;
     }
 
     private boolean checkSiteCoordinatorOwnership(Study study, List<ProtectionGroup> sites) {
         List<StudySite> studySites = study.getStudySites();
         for (StudySite studySite : studySites) {
             for (ProtectionGroup site : sites) {
                if (studySite.getSite().equals(DomainObjectTools.loadFromExternalObjectId(site.getProtectionGroupName(), siteDao))) {
                    return true;
                }
             }
         }
         return false;
     }
 
     private boolean checkParticipantCoordinatorOwnership(User user, Study study) {
         String userName = user.getLoginName();
         List<StudySite> studySites = study.getStudySites();
         for (StudySite studySite : studySites) {
             String protectionGroupName = DomainObjectTools.createExternalObjectId(studySite);
             Set<ProtectionGroupRoleContext> pgRoleContext = getProtectionGroupRoleContexts(user);
             if (pgRoleContext.size() != 0) {
                 for (ProtectionGroupRoleContext pgrc : pgRoleContext) {
                      if (pgrc.getProtectionGroup().getProtectionGroupName().equals(protectionGroupName)) {
                          return true;
                      }
                 }
             }
         }
         return userProvisioningManager.checkOwnership(userName, DomainObjectTools.createExternalObjectId(study));
     }
 
     private Set<Group> getGroups(User user) {
         try {
             return userProvisioningManager.getGroups(user.getUserId().toString());
         } catch (CSObjectNotFoundException e) {
             throw new StudyCalendarSystemException("Could not get groups for " + user.getLoginName(), e);
         }
     }
 
     private Set<ProtectionGroupRoleContext> getProtectionGroupRoleContexts(User user) {
         try {
             return userProvisioningManager.getProtectionGroupRoleContextForUser(user.getUserId().toString());
         } catch (CSObjectNotFoundException e) {
             throw new StudyCalendarSystemException("Could not find PGRCs for " + user.getLoginName(), e);
         }
     }
 
     private boolean isSitePG(ProtectionGroup protectionGroup) {
         return protectionGroup.getProtectionGroupName().startsWith(Site.class.getName());
     }
 
     private boolean isStudySitePG(ProtectionGroup protectionGroup) {
         return protectionGroup.getProtectionGroupName().startsWith(StudySite.class.getName());
     }
 
     ////// CONFIGURATION
     
     public void setUserProvisioningManager(UserProvisioningManager userProvisioningManager) {
         this.userProvisioningManager = userProvisioningManager;
     }
 
     public void setSiteDao(SiteDao siteDao) {
         this.siteDao = siteDao;
     }
 }
 
 
