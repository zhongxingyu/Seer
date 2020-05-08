 package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import gov.nih.nci.security.UserProvisioningManager;
 import gov.nih.nci.security.authorization.domainobjects.Group;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroupRoleContext;
 import gov.nih.nci.security.authorization.domainobjects.Role;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.dao.RoleSearchCriteria;
 import gov.nih.nci.security.dao.SearchCriteria;
 import gov.nih.nci.security.dao.UserSearchCriteria;
 import gov.nih.nci.security.dao.ProtectionGroupSearchCriteria;
 import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
 
 /**
  * @author Padmaja Vedula
  */
 
 public class StudyCalendarAuthorizationManager {
 	public static final String APPLICATION_CONTEXT_NAME = "study_calendar";
 	public static final String BASE_SITE_PG = "BaseSitePG";
     public static final String ASSIGNED_USERS = "ASSIGNED_USERS";
     public static final String AVAILABLE_USERS = "AVAILABLE_USERS";
     private static Log log = LogFactory.getLog(LoginCheckInterceptor.class);
     
     private UserProvisioningManager userProvisioningManager;
   
     public void assignProtectionElementsToUsers(List<String> userIds, String protectionElementObjectId) throws Exception
 	{
     	boolean protectionElementPresent = false;	
 			
 		try { 
 			userProvisioningManager.getProtectionElement(protectionElementObjectId);
 			protectionElementPresent = true;
 		} catch (CSObjectNotFoundException ex){
 			ProtectionElement newProtectionElement = new ProtectionElement();
 			newProtectionElement.setObjectId(protectionElementObjectId);
 			newProtectionElement.setProtectionElementName(protectionElementObjectId);
 			userProvisioningManager.createProtectionElement(newProtectionElement);
 			userProvisioningManager.setOwnerForProtectionElement(protectionElementObjectId, userIds.toArray(new String[0]));
 		}
 		if (protectionElementPresent)
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
 		}
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
     
 	public Map getUsers(String groupName, String protectionElementObjectId) throws Exception {
 		HashMap<String, List> usersMap = new HashMap<String, List>();
 		List<User> usersForRequiredGroup = getUsersForGroup(groupName);
         usersMap = (HashMap) getUserListsForProtectionElement(usersForRequiredGroup, protectionElementObjectId);
 				
 		return usersMap;
 	}
     
 	
 	private Map getUserListsForProtectionElement(List<User> users, String protectionElementObjectId) throws Exception {
 		HashMap<String, List> userHashMap = new HashMap<String, List>();
 		List<User> assignedUsers = new ArrayList<User>();
 		List<User> availableUsers = new ArrayList<User>();
 		
 		for (User user : users)
 		{
 			String userName = user.getLoginName();
 			if (userProvisioningManager.checkOwnership(userName, protectionElementObjectId))
 			{
 				assignedUsers.add(user);
 			} else {
 				availableUsers.add(user);
 			}
 		}
 		userHashMap.put(ASSIGNED_USERS, assignedUsers);
 		userHashMap.put(AVAILABLE_USERS, availableUsers);
 		return userHashMap;
 	}
 	
     public User getUserObject(String id) throws Exception {
     	User user = null;
       	user = userProvisioningManager.getUserById(id);
       	return user;
     }
     
     public void createProtectionGroup(String newProtectionGroup, String parentPG) throws Exception {
     	if (parentPG != null) {
     		ProtectionGroup parentGroupSearch = new ProtectionGroup();
     		parentGroupSearch.setProtectionGroupName(parentPG);
             SearchCriteria protectionGroupSearchCriteria = new ProtectionGroupSearchCriteria(parentGroupSearch);
     		List parentGroupList = userProvisioningManager.getObjects(protectionGroupSearchCriteria);
     		
     		if (parentGroupList.size() > 0) {
     			ProtectionGroup parentProtectionGroup = (ProtectionGroup) parentGroupList.get(0);
     			ProtectionGroup requiredProtectionGroup = new ProtectionGroup();
     			requiredProtectionGroup.setProtectionGroupName(newProtectionGroup);
     			requiredProtectionGroup.setParentProtectionGroup(parentProtectionGroup);
     			userProvisioningManager.createProtectionGroup(requiredProtectionGroup);
     			if (log.isDebugEnabled()) {
 					log.debug("new protection group created " + newProtectionGroup);
 				}
     		}
     	}
     }
     
     /**
      * Method to retrieve all site protection groups
      * 
      */
     
     public List getSites() throws Exception {
     	List<ProtectionGroup> siteList = new ArrayList<ProtectionGroup>() ;
     	
 		ProtectionGroup parentGroupSearch = new ProtectionGroup();
 		parentGroupSearch.setProtectionGroupName(BASE_SITE_PG);
 	    SearchCriteria protectionGroupSearchCriteria = new ProtectionGroupSearchCriteria(parentGroupSearch);
 		List parentGroupList = userProvisioningManager.getObjects(protectionGroupSearchCriteria);
 			
 		if (parentGroupList.size() > 0) {
 			ProtectionGroup parentProtectionGroup = (ProtectionGroup) parentGroupList.get(0);
 			ProtectionGroup protectionGroup = new ProtectionGroup();
 	        SearchCriteria pgSearchCriteria = new ProtectionGroupSearchCriteria(protectionGroup);
 			List<ProtectionGroup> pgList = userProvisioningManager.getObjects(pgSearchCriteria);
 			
 			if (pgList.size() > 0) {
 				for (ProtectionGroup requiredProtectionGroup : pgList) {
 					   if (requiredProtectionGroup.getParentProtectionGroup().getProtectionGroupId() == parentProtectionGroup.getProtectionGroupId()) {	
 						   siteList.add(requiredProtectionGroup);
 					   }
 				}
 			}
 		}
 		return siteList;
     }
     
     /**
      * Method to retrieve a site protection group
      * @param String site
      * @return null or site Protection Group
      * 
      */
     
     public ProtectionGroup getSite(String name) throws Exception {
     	ProtectionGroup requiredProtectionGroup = null;
     	
 		ProtectionGroup protectionGroupSearch = new ProtectionGroup();
 		protectionGroupSearch.setProtectionGroupName(name);
 	    SearchCriteria protectionGroupSearchCriteria = new ProtectionGroupSearchCriteria(protectionGroupSearch);
 		List<ProtectionGroup> protectionGroupList = userProvisioningManager.getObjects(protectionGroupSearchCriteria);
 			
 		if (protectionGroupList.size() > 0) {
 			requiredProtectionGroup = (ProtectionGroup) protectionGroupList.get(0);
 			
 		}
 		return requiredProtectionGroup;
     }
     
     public List getUsersForGroup(String groupName) throws Exception {
 		List<User> usersForRequiredGroup = new ArrayList<User>(); 
 		User user = new User();
         SearchCriteria userSearchCriteria = new UserSearchCriteria(user);
 		List<User> userList = userProvisioningManager.getObjects(userSearchCriteria);
 		if (userList.size() > 0)
 		{
 			
 		   for (User requiredUser : userList) {
 			   try {
 				   Set groups = userProvisioningManager.getGroups(requiredUser.getUserId().toString());
 				   Set<Group> userGroups = groups;
 				   if (userGroups.size() > 0) {	
 					   Group requiredGroup = (Group) userGroups.toArray()[0];
 					   if (groupName.equals(requiredGroup.getGroupName())) {
 						   usersForRequiredGroup.add(requiredUser);
 					   }
 				   }
 			   } catch (CSObjectNotFoundException cse){
 				   throw cse;
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
     	HashMap<String, List> usersMap = new HashMap<String, List>();
 		List<User> usersForRequiredGroup = getUsersForGroup(group);
         usersMap = (HashMap) getUserListsForProtectionGroup(usersForRequiredGroup, protectionGroupName);
 				
 		return usersMap;
     	
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
 			String userId = user.getUserId().toString();
 			Set<ProtectionGroupRoleContext> pgRoleContext = userProvisioningManager.getProtectionGroupRoleContextForUser(userId);
 			List<ProtectionGroupRoleContext> pgRoleContextList = new ArrayList(pgRoleContext);
 			if (pgRoleContextList.size() != 0) {
 				for (ProtectionGroupRoleContext pgrc : pgRoleContextList) {
 					if (pgrc.getProtectionGroup().getProtectionGroupName().equals(protectionGroupName)) {
 						assignedUsers.add(user);
					} else {
						availableUsers.add(user);
					}
 				}
			} else 
 				availableUsers.add(user);
 		}
 		userHashMap.put(ASSIGNED_USERS, assignedUsers);
 		userHashMap.put(AVAILABLE_USERS, availableUsers);
 		return userHashMap;
 	}
     
     public void assignProtectionGroupsToUsers(List<String> userIds, ProtectionGroup protectionGroup, String roleName) throws Exception
 	{
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
     
     public void removeProtectionGroupUsers(List<String> userIds, ProtectionGroup protectionGroup) throws Exception
     {
 	
     	for (String userId : userIds)
     	{
     		userProvisioningManager.removeUserFromProtectionGroup(protectionGroup.getProtectionGroupId().toString(), userId);
     	}
     }
     
     public void assignProtectionElementToPGs(List<String> pgIdsList, String protectionElementId) throws Exception {
     	userProvisioningManager.assignToProtectionGroups(protectionElementId, pgIdsList.toArray(new String[0]));
     }
     
     ////// CONFIGURATION
     
     public void setUserProvisioningManager(UserProvisioningManager userProvisioningManager) {
         this.userProvisioningManager = userProvisioningManager;
     }
     
 }
 
 
