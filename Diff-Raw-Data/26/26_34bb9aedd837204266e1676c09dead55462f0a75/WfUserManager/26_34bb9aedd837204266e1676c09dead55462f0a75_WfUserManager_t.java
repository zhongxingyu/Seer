 package velo.ejb.workflow.api;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.application.FacesMessage;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.jboss.seam.annotations.AutoCreate;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.log.Log;
 
 import velo.ejb.interfaces.IdentityAttributeManagerLocal;
 import velo.ejb.interfaces.UserManagerLocal;
 import velo.entity.IdentityAttribute;
 import velo.entity.User;
 
 @Name("wfUserManager")
 @AutoCreate
 public class WfUserManager {
 	@In
 	EntityManager entityManager;
 	
 	@Logger
 	Log log;
 	
 	@In(create=true)
 	IdentityAttributeManagerLocal identityAttributeManager;
 
 	@In(create=true)
 	UserManagerLocal userManager;	
 	
 	String manager; 
 	String identifier;
 	
 	/**
 	 * Get manager of a certain level.
 	 * @param userName - The username to find the user entity that represents the manager.
 	 * @param level - The level of the manager to find in the hierarchy
 	 * @return The user entity of the manager, null is returned if not found.
 	 */
 	public User getManagerForLevel(String userName, int level) {
 		log.debug("START: getting manager in level '#0' for user name '#1'",level,userName);
 		
 		User user = findUser(userName);
 		if (user == null) {
 			log.debug("Could not manager for user '#0' as user does not exist in repository",userName);
 			return null;
 		} 
 		
 		User currManagerInLevel = null;
 		String getManagerForUser = userName;
 		for (int i=1;i<=level;i++) {
 			log.trace("(Iteration) Getting manager for user name '#0' in level '#1'",userName,i);
 			currManagerInLevel = getManagerForUser(getManagerForUser);
 			
 			if (currManagerInLevel == null) {
 				log.debug("Could not get manager for level #0, returning null.",i);
 				return null;
 			} else {
 				getManagerForUser = currManagerInLevel.getName();
 			}
 		}
 		
 		log.debug("Found manager(user: '#0') in level #1, returning manager.",currManagerInLevel.getName(),level);
 		return currManagerInLevel;
 	}
 	
 	/**
 	 * Determines whether a user name is a manager of a certain user name. 
 	 * @param userName - 
 	 * @param expectedManagerUserName
 	 * @param level
 	 * @return
 	 */
 	public boolean isManager(String userName, String expectedManagerUserName,int level) {
 		log.debug("Determining whether username '#0' is a manager in level '#1' of user '#2'",expectedManagerUserName,userName,level);
 		User manager = getManagerForLevel(userName,level);
 		
 		if (manager == null) {
 			log.debug("Could not find manager for user name '#0' in level '#1', returning false.",expectedManagerUserName,level);
 			return false;
 		} else {
 			log.debug("Found manager('#0') in level '#1' for user name '#2' (determining whether the user is equal to the expected manager name '#3')", manager.getName(), level,userName,expectedManagerUserName);
 			if (manager.getName().equals(expectedManagerUserName)) {
 				log.debug("Found manager('#0') in level '#1' for user name '#2' is equal to the expected manager name!, returning true.",manager.getName(), level,userName);
 				return true;
 			} else {
 				log.debug("Found manager('#0') in level '#1' for user name '#2' is NOT equal to the expected manager name! (expected manager user name was: '#3'), returning false.",manager.getName(), level,userName,expectedManagerUserName);
 				return false;
 			}
 		}
 	}
 	
 	/**
 	 * Determines the level of the specified manager in the manager's hirarchy
 	 * @param userName The username to look the manager for
 	 * @param expectedManagerUserName The expected manager name
 	 * @param levelsDrill how many levels to drill for seeking the expected manager's level
 	 * @return the level of the epxected manager (0 means the expected manager is not found on any level, 1-x shows the level where 1 is the direct manager.)
 	 */
 	public int getManagerLevel(String userName, String expectedManagerUserName,int levelsDrill) {
 		log.debug("START: getting manager's level for user '#0', expected manage name '#1', levels to drill '#2'",userName,expectedManagerUserName, levelsDrill);
 		User user = findUser(userName);
 		User expectedManager = findUser(expectedManagerUserName);
 		
 		if (user == null) {
 			log.debug("Could not find user '#0' in repository to seek manager name '#0' for..., returning 0.", userName, expectedManager);
 			return 0;
 		} 
 		if (expectedManager == null) {
 			log.debug("Could not find expected manager user name '#0' in repository, returning 0.",expectedManager);
 			return 0;
 		}
 		
 		
 		User loadedRealManager = null;
 		String getManagerForUser = userName;
 		for (int i=1;i<=levelsDrill;i++) {
 			loadedRealManager = getManagerForUser(getManagerForUser);
 			
 			if (loadedRealManager == null) {
 				return 0;
 			} else {
 				if (loadedRealManager.equals(expectedManager)) {
 					log.debug("Found expected manager name '#0' in level '#1'",expectedManagerUserName,i);
 					return i;
 				} else {
 					getManagerForUser = loadedRealManager.getName();
 				}
 			}
 		}
 		
 		
 		log.debug("Could not find manager user name '#0' in any of levels up to '#1' for user name '#2'",expectedManagerUserName,levelsDrill,userName);
 		log.debug("END: getting manager's level for user '#0', expected manage name '#1', levels to drill '#2'",userName,expectedManagerUserName, levelsDrill);
 		return 0;
 	}
 	
 	
 	
 	public boolean isThereManagerForUser(String userName, int level) {
 		log.debug("Getting manager for userName '#0' in level '#1'",userName,level);
 		User manager = getManagerForLevel(userName,level);
 		
 		if (manager == null) {
 			log.debug("Could not get manager for userName '#0' in level '#1' as manager user was NOT found.", userName,level);
 			return false;
 		} else {
 			log.debug("Found manager('#0') for user '#1' in level '#2', returning true", manager.getName(), userName, level);
 			return true;
 		}
 	}
 	
 	
 	//TODO: Somewhen should be moved to a generic velo method
 	public boolean isThereManagerForUser(String userName) {
 		log.debug("Determining whether user '#0' has a manager or not.",userName);
 		
 		User manager = getManagerForUser(userName);
 		if (manager != null) {
 			log.debug("Found manager ('#0') for user name '#1'", manager.getName(),userName);
 			return true;
 		} else {
 			log.debug("Could not find manager for user name '#1'", userName);
 			return false;
 		}
 	}
 		
 	public User getManagerForUser(String userName) {
 		log.debug("Getting manager for user '#0'",userName);
 		User user = findUser(userName);
 
 		if (user == null) {
 			log.debug("Cannot get manager for user name '#0' since user does not exist.");
 			return null;
 		}
 		
 		if (getManagerIAName() == null) {
 			log.debug("The IdentityAttribute that represents a manager was not set.");
 			return null;
 		}
 		log.debug("Found IdentityAttribute that represents a 'Manager' named '#0'",getManagerIAName());
 		
 		if (getIdentifierIAName() == null) {
 			log.debug("The IdentityAttribute that represents an identifier was not set.");
 			return null;
 		}
 		log.debug("Found IdentityAttribute that represents an 'Identitifer' named '#0'",getManagerIAName());
 		
 		
 		
 		if (user.isUserAttributeExistsWithFirstValue(getManagerIAName())) {
 			String managerDN = user.getUserIdentityAttribute(getManagerIAName()).getFirstValueAsString();
 			log.debug("Manager's IA was found with value: '#0'", managerDN);
 			
 			//now we have the manager's DN, lets get its user
			//User managerUser = findUser(getIdentifierIAName(), managerDN);
			
			Map<String,String> ias = new HashMap<String, String>();
			ias.put(getIdentifierIAName(), managerDN);
			List<User> managers = userManager.findUsers(ias, false);
			log.error("!!!!!!!! SIZE OF LIST: #0", managers.size());
			log.error("!!!!!!!! VALUES: #0", managers);
			
			User managerUser = managers.get(0);
 			
 			//manager's was not found!
 			if (managerUser == null) {
 				log.debug("Manager's user was NOT found for DN '#0'!",  managerDN);
 				return null;
 			} else {
 				log.debug("Manager's user was found with name '#0' for DN '#1'", managerUser.getName(),managerDN);
 				return managerUser;
 			}
 		}
 		
 		
 		return null;
 	}
 	
 	
 	// Returns the manager of the user 'userName' or his manager's delegator if exists
 	public User getManagerOrDelegatorForUser(String userName) {
 		
 		log.debug("Getting manager or delegator for the user #0", userName);
 		
 		User manager = getManagerForUser(userName);
 		
 		if (manager == null)
 			return null;
 		
 		if (manager.getDelegator() == null) {
 			log.debug("Manager was found with DN #0, delegator was not found", manager.getName());
 			return manager;
 		}
 		
 		log.debug("Manager's delegator was found with DN #0", manager.getDelegator().getName());
 		
 		return manager.getDelegator();
 		
 	}
 	
 	public User getManagerOrDelegatorForLevel(String userName, int level) {
 		
 		log.debug("Getting manager or delegator level #0 for the user #1", level, userName);
 		
 		User manager = getManagerForLevel(userName, level);
 		
 		if (manager == null)
 			return null;
 		
 		if (manager.getDelegator() == null) {
 			log.debug("Manager for level #0 was found with DN #1, delegator was not found", level, manager.getName());
 			return manager;
 		}
 		
 		log.debug("Manager's delegator for level #0 was found with DN #1", level, manager.getDelegator().getName());
 		
 		return manager.getDelegator();	
 		
 	}
 	
 	public List<User> getUsersManagedBy(User user) {
 		
 		String identifierIA = getIdentifierIAName();
 		String managerIA = getManagerIAName();
 		
 		if (!(user.isUserAttributeExists(identifierIA))) {
 			log.error("The identifier IA #0 for the user #1 doesn't exist, returning null", identifierIA, user.getName());
 			return null;
 		}
 		
 		if (!(user.getUserIdentityAttribute(identifierIA).isFirstValueIsNotNullAndNotEmpty())) {
 			log.error("The identifier IA #0 for the user #1 doesn't exist, returning null", identifierIA, user.getName());
 			return null;
 		}
 
 		List<User> users = new ArrayList<User>();
 		
 		// Define a map for the search
 		Map<String,String> userAttr = new HashMap<String,String>();
 		
 		// Fill the map with the values to search, the identity attribute to search in and it's value
 		userAttr.put(managerIA, user.getUserIdentityAttribute(identifierIA).getFirstValueAsString());
 
 		// Run the search
 		users = userManager.findUsers(userAttr, false);
 
 		return users;
 	}
 	
 	// Returns a list of all users (With a drilldown by recursion) managed by a certain manager
 	public List<User> getAllUsersManagedBy(User user, int level) {
 
 		if (level == 0) return null;
 		
 		List<User> tempUsers;
 		List<User> tmpUsers = new ArrayList<User>();
 		List<User> etmpUsers = new ArrayList<User>();
 
 		// First get all users that are directly managed by the manager and with a 'Status' IA equals to '0'
 		tempUsers = getUsersManagedBy(user);
 
 		if (tempUsers == null || tempUsers.isEmpty()) {
 			return null;
 		}	
 
 		// Recursively iterate on every user and add his managed users to the list
 		for (User u : tempUsers) {
 			tmpUsers = getAllUsersManagedBy(u, level-1);
 			if (tmpUsers != null)
 				etmpUsers.addAll(tmpUsers);
 		}
 
 		tempUsers.addAll(etmpUsers);
 		
 		return tempUsers;
 	}	
 	
 	// Gets a manager which identity attribute is equal to a certain value (recursively)
 	public User getManagerByIA(String user, String iA, String value) {
 		List<String> values = new ArrayList<String>();
 		values.add(value);
 		return getManagerByIA(user, iA, values);
 	}
 	
 	// Gets a manager which identity attribute is equal to a certain values (recursively)
 	public User getManagerByIA(String user, String iA, List<String> values) {
 
 		log.info("Getting the manager of #0 by the IA '#1' with the value #2.", user, iA, values);
 		
 		if (values == null || values.isEmpty())
 			return null;
 		
 		User manager = getManagerForUser(user);
 		while (manager != null) {
 			if (manager.isUserAttributeExists(iA) &&
 				manager.getUserIdentityAttribute(iA).isFirstValueIsNotNullAndNotEmpty())
 					for (String value : values)
 						if (manager.getUserIdentityAttribute(iA).getFirstValueAsString().equals(value)) {
 							log.info("The manager found is #0.", manager.getName());
 							return manager;
 						}
 		
 			manager = getManagerForUser(manager.getName());
 		}
 
 		log.info("Manager was not found, returning null.");
 		return null;
 	}	
 	
 	
 	
 	//FIXME: DUPLICATED FROM USERBEAN -> 06/jul/09 - corrected, suspicious, why it was duplicated originally?
  		//Probably becauser userManager was not supporting Seam's entityManager delegation in the past
 	public User findUser(String userName) {
 		log.debug("Finding User in repository with name '" + userName + "'");
 
 		//user name might get out of a request which keeps the user name as string, thus, make sure the user name is always as uppercase
 		userName = userName.toUpperCase();
 		
 		
 		userManager.setEntityManager(entityManager);
 		return userManager.findUser(userName);
 		
 		/*
 		try {
 			Query q = entityManager.createNamedQuery("user.findByName").setParameter("name",userName);
 			return (User) q.getSingleResult();
 		}
 		catch (javax.persistence.NoResultException e) {
 			log.debug("Found user did not result any user for name '" + userName + "', returning null.");
 			return null;
 		}
 		*/
 	}
 	
 	
 	
 	
 	//FIXME: DUPLICATED FROM USERBEAN -> 06/jul/09 - corrected, suspicious, why it was duplicated originally?
 		//Probably becauser userManager was not supporting Seam's entityManager delegation in the past
 	public User findUser(String identityAttributeUniqueName, String value) {
 		userManager.setEntityManager(entityManager);
 		return userManager.findUser(identityAttributeUniqueName, value);
 		
 		/*
 		String query = "SELECT DISTINCT vl_user.* FROM VL_USER vl_user,VL_IDENTITY_ATTRIBUTE ia, VL_USER_IDENTITY_ATTRIBUTE uia,"
 			+ "VL_USER_IDENTITY_ATTR_VALUE uiav WHERE"
 			+ " uia.IDENTITY_ATTRIBUTE_ID = ia.IDENTITY_ATTRIBUTE_ID AND uia.USER_ID = vl_user.USER_ID"
 			+ " AND uiav.USER_IDENTITY_ATTRIBUTE_ID = uia.USER_IDENTITY_ATTRIBUTE_ID"
 			+ " AND (ia.UNIQUE_NAME=:identityAttributeUniqueName)"
 			+ " AND ( (UPPER(uiav.VALUE_STRING) like :uiaValue) )"; 
 		
 		try {
 			Query q = entityManager.createNativeQuery(query, User.class).setParameter("identityAttributeUniqueName",identityAttributeUniqueName).setParameter("uiaValue", value.toUpperCase());
 			return (User) q.getSingleResult();
 		}
 		catch (javax.persistence.NoResultException e) {
 			log.debug("Could not find any User for IdentityAttribute name '" + identityAttributeUniqueName + "', with value '" + value + "'");
 			return null;
 		}
 		catch (javax.persistence.NonUniqueResultException e) {
 			log.warn("Cannot retrieve user for IA '" + identityAttributeUniqueName + "', with value '" + value + "': " + e.getMessage());
 			return null;
 		}
 		*/
 	}
 	
 	
 	
 	
 	
 	
 	private String getManagerIAName() {
 		if (manager == null) {
 			IdentityAttribute ia = identityAttributeManager.getManagerIdentityAttribute();
 			
 			if (ia != null) {
 				manager = identityAttributeManager.getManagerIdentityAttribute().getUniqueName();
 			}
 		}
 		
 		return manager;
 	}
 	
 	private String getIdentifierIAName() {
 		if (identifier == null) {
 			IdentityAttribute ia = identityAttributeManager.getIdentifierIdentityAttribute();
 			
 			if (ia != null) {
 				identifier = identityAttributeManager.getIdentifierIdentityAttribute().getUniqueName();
 			}
 		}
 		
 		return identifier;
 	}
 }
