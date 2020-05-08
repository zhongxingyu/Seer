 /**
  * Copyright (c) 2000-2007, Shakarchi Asaf
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package velo.ejb.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.ejb.EJB;
 import javax.ejb.EJBException;
 import javax.ejb.Stateless;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceUnit;
 import javax.persistence.Query;
 
 import org.apache.log4j.Logger;
 import org.jboss.ejb3.annotation.IgnoreDependency;
 import org.jboss.seam.annotations.AutoCreate;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.security.management.PasswordHash;
 
 import velo.common.EdmMessages;
 import velo.common.SysConf;
 import velo.contexts.OperationContext;
 import velo.ejb.interfaces.AccountManagerLocal;
 import velo.ejb.interfaces.CommonUtilsManagerLocal;
 import velo.ejb.interfaces.EventManagerLocal;
 import velo.ejb.interfaces.IdentityAttributeManagerLocal;
 import velo.ejb.interfaces.PasswordManagerLocal;
 import velo.ejb.interfaces.RequestManagerLocal;
 import velo.ejb.interfaces.ResourceAttributeManagerLocal;
 import velo.ejb.interfaces.ResourceManagerLocal;
 import velo.ejb.interfaces.ResourceOperationsManagerLocal;
 import velo.ejb.interfaces.RoleManagerLocal;
 import velo.ejb.interfaces.TaskManagerLocal;
 import velo.ejb.interfaces.UserManagerLocal;
 import velo.ejb.interfaces.UserManagerRemote;
 import velo.entity.Account;
 import velo.entity.AccountAttribute;
 import velo.entity.BulkTask;
 import velo.entity.Capability;
 import velo.entity.CapabilityFolder;
 import velo.entity.CreateUserRequest;
 import velo.entity.IdentityAttribute;
 import velo.entity.PasswordPolicyContainer;
 import velo.entity.Position;
 import velo.entity.RequestAttribute;
 import velo.entity.Resource;
 import velo.entity.ResourceAttribute;
 import velo.entity.Role;
 import velo.entity.SystemEvent;
 import velo.entity.User;
 import velo.entity.UserIdentityAttribute;
 import velo.entity.UserRole;
 import velo.entity.IdentityAttribute.IdentityAttributeSources;
 import velo.exceptions.AttributeSetValueException;
 import velo.exceptions.EntityAssociationException;
 import velo.exceptions.EventExecutionException;
 import velo.exceptions.ModifyAttributeFailureException;
 import velo.exceptions.NoResultFoundException;
 import velo.exceptions.NoUserIdentityAttributeFoundException;
 import velo.exceptions.OperationException;
 import velo.exceptions.PasswordValidationException;
 import velo.exceptions.PersistEntityException;
 import velo.exceptions.ScriptInvocationException;
 import velo.exceptions.UnsupportedAttributeTypeException;
 import velo.exceptions.UserAuthenticationException;
 import velo.exceptions.UserNameGenerationException;
 import velo.request.Attributes;
 import velo.scripting.ScriptFactory;
 import velo.scripting.UserPluginIdTools;
 import velo.storage.Attribute;
 import velo.utils.Stopwatch;
 
 /**
  * A Stateless EJB bean for User management
  * 
  * @author Asaf Shakarchi
  */
 @Stateless()
 @Name("userManager")
 @AutoCreate
 public class UserBean implements UserManagerLocal, UserManagerRemote {
 	private final String table_users_to_capabilities = "VL_USERS_TO_CAPABILITIES";
 	private final String table_user_role = "FROM VL_USER_ROLE";
 	
 	
 	private static Logger log = Logger.getLogger(UserBean.class.getName());
 
 	// private static Logger edmLogger =
 	// SysLogger.getLogger(UserBean.class.getName());
 
 	/**
 	 * Injected entity manager
 	 */
 	//@PersistenceContext
 	public EntityManager entityManager;
 	
 	@PersistenceUnit
 	private EntityManagerFactory factory;
 
 	/**
 	 * Inject a local ResourceAttribute ejb
 	 */
 	@EJB
 	public ResourceAttributeManagerLocal tsam;
 
 	/**
 	 * Inject a local AccountManager ejb
 	 */
 	@EJB
 	public AccountManagerLocal am;
 
 	@org.jboss.annotation.IgnoreDependency
 	@IgnoreDependency
 	@EJB
 	public RequestManagerLocal reqm;
 
 	@EJB
 	CommonUtilsManagerLocal cum;
 
 	@org.jboss.annotation.IgnoreDependency
 	@IgnoreDependency
 	@EJB
 	RoleManagerLocal rolem;
 
 	@EJB
 	TaskManagerLocal tm;
 
 	@EJB
 	PasswordManagerLocal passwordManager;
 
 	@EJB
 	IdentityAttributeManagerLocal identityAttributeManager;
 
 	@EJB
 	EventManagerLocal eventManager;
 	
 	@EJB
 	ResourceManagerLocal resourceManager;
 	
 	@org.jboss.annotation.IgnoreDependency
 	@IgnoreDependency
 	@EJB
 	ResourceOperationsManagerLocal resourceOperationsManager;
 
 	// !cleaned
 
 	
 	
 	
 	//mainly used by the gui/reconcile to determine if there is a need to update accounts when UIAs gets modified
 	//TODO: should support other ways except direct resourceAttribute->IdentityAttribute assigment 
 	public Collection<Account> findAccountsByUserIdentityAttribute(UserIdentityAttribute uia) {
 		Collection<ResourceAttribute> resourceAttributes = tsam.findResourceAttributesAttachedToIdentityAttribute(uia.getIdentityAttribute());
 
 		log.debug("Found -"+ resourceAttributes.size()
 				+ "- ResourceAttributes that are attached to the corresponding IdentityAttribute name: "
 				+ uia.getIdentityAttribute().getUniqueName());
 
 		
 		Collection<Account> accountList = am.findAccounts(uia.getUser(),resourceAttributes);
 
 		log
 				.info("Found -"
 						+ accountList.size()
 						+ "- Accounts that are attached to the corresponding ResourceAttributes for user name: "
 						+ uia.getUser().getName());
 
 		return accountList;
 	}
 	
 	
 	//Mainly used by the gui/reconcile(?) when an admin modifies user's Identity Attributes
 	public void modifyUserAttributes(Collection<UserIdentityAttribute> uiaList) throws ModifyAttributeFailureException {
 		log.debug("started User Identity Attributes modification process...");
 
 		// Initialize an indicator of the whole process
 		boolean processStatus = true;
 
 		// Initiate a message logger
 		EdmMessages ems = new EdmMessages();
 
 		
 		/*
 		//TODO Support Accounts modifications!
 		// Verify if set attribute structure fits the corresponding
 		// IdentityAttribute
 
 		// Initiailize a global account list for all accounts
 		// 'HashSet' was chosen since we do not want to have duplicated account
 		// objects in the list
 		// (If two attributes were modified and same account should get
 		// modified, we only want to have one object of the account and not two)
 		Set<Account> accountList = new HashSet<Account>();
 		 */
 		
 		for (UserIdentityAttribute currUIA : uiaList) {
 			log.debug("Updating attribute ID: "
 			+ currUIA.getUserIdentityAttributeId() + ", name: "
 			+ currUIA.getIdentityAttribute().getUniqueName()
 			+ ", with 1st value: "
 			+ currUIA.getValues().iterator().next().getAsString()
 			+ ", with values amount: '" + currUIA.getValues().size()
 			+ "'");
 
 			// Merge the entity
 			getEntityManager().merge(currUIA);
 
 			
 			/*Not yet supported
 			// Retrieve the account list for the current iterated UIA
 			Collection<Account> accountListPerUIA = findAccountsByUserIdentityAttribute(currUIA);
 
 			// Add the retrieved accounts per the current UIA to the GLOBAL
 			// account list
 			accountList.addAll(accountListPerUIA);
 			*/
 		}
 
 		// Perform an update over the global account list
 		//log.debug("Factoring Update Account Tasks for all found accounts that should be affected...");
 
 		/*
 		 * try { Collection<ResourceAccountActionInterface>
 		 * accountUpdateActionList = am .getUpdateActions(accountList);
 		 * 	
 		 * logger .info("Factored -" + accountUpdateActionList.size() + "-
 		 * UpdateActions for the following number of accounts that should get
 		 * affected: " + accountList.size());
 		 * 		
 		 * Iterator<ResourceAccountActionInterface> actionsIterator =
 		 * accountUpdateActionList .iterator(); while
 		 * (actionsIterator.hasNext()) { ResourceAccountActionInterface tsaai =
 		 * actionsIterator.next(); try { tsaai.__execute__(); } catch
 		 * (ActionFailureException afe) { logger.severe(afe.getMessage());
 		 * 	
 		 * //Indicate that there was a failure processStatus = false;
 		 * 	
 		 * //Add the failure message to the view ems.severe(afe.getMessage()); } } }
 		 */
 
 		// Iterate over the account list and prepare an UPDATE 'task' for each
 		// account
 		/*
 		for (Account currAccount : accountList) {
 			if (!am.updateAccount(currAccount)) {
 				processStatus = false;
 			}
 		}
 		*/
 		
 		/*
 		 * catch (BulkActionsFactoryFailureException baffe) {
 		 * logger.severe(baffe.getMessage());
 		 * 
 		 * //Indicate that there was a failure processStatus = false;
 		 * 
 		 * //Add the failure message to the view ems.severe(baffe.getMessage()); }
 		 */
 
 		if (!processStatus) {
 			// Log a new EventLog with the failure
 			/*cum.addEventLog("UserManagement","FAILURE","HIGH",
 				"One or more UserIdentityAttributes (or their attached accounts updates) were failed to get updated",
 				ems.toString());
 			*/
 			throw new ModifyAttributeFailureException("There was a failure while modifing one or more User Attributes, please see EventLog for details.");
 		}
 
 		log.info("ENDED UserIdentityAttributes modification");
 	}
 
 	
 	public User findUser(String name) {
 		log.debug("Finding User in repository with name '" + name + "'");
 		
 		if (name == null) {
 			log.warn("Name was set to null, returning null.");
 			return null;
 		}
 
 		//user name might get out of a request which keeps the user name as string, thus, make sure the user name is always as uppercase
 		name = name.toUpperCase();
 		
 		try {
 			Query q = getEntityManager().createNamedQuery("user.findByName").setParameter("name",name);
 			User user = (User) q.getSingleResult();
 			
 			try {
 				loadUserAttributes(user);
 				
 				return user;
 			} catch (OperationException e) {
 				log.error("Error while loading attributes of user '" + user.getName() + "': " + e.getMessage());
 				return null;
 			}
 		}
 		catch (javax.persistence.NoResultException e) {
 			log.debug("Found user did not result any user for name '" + name + "', returning null.");
 			return null;
 		}
 	}
 	
 	public void loadUserAttributes(User user) throws OperationException {
 		if (user.getLoaded()) {
 			//throw new OperationException("User '" + user.getName() + "' was already flagged as loaded.");
 			return;
 		}
 
 		//Load identity attributes of user
 		Collection<IdentityAttribute> allActiveIAs = identityAttributeManager.findAllActive();
 		for (IdentityAttribute currIA : allActiveIAs) {
 			if (currIA.getSource().equals(IdentityAttributeSources.RESOURCE_ATTRIBUTE)) {
 				//Find whether user has an account for this resource
 				Account accOnResource = user.getAccountOnResource(currIA.getResourceAttributeSource().getResource().getUniqueName());
 				
 				if (accOnResource == null) {
 					log.debug("Could not load Identity attribute '" + currIA.getUniqueName() + "' with source type 'RESOURCE_ATTRIBUTE' since user '" + user.getName() + "' has no account on resource '" + currIA.getResourceAttributeSource().getResource().getUniqueName() + "'");
 					continue;
 				}
 				
 				//Get the account attribute!
 				AccountAttribute currAA = accOnResource.getAccountAttribute(currIA.getResourceAttributeSource());
 				if (currAA == null) {
 					log.debug("Could not load Identity attribute '" + currIA.getUniqueName() + "' with source type 'RESOURCE_ATTRIBUTE' for user '" + user.getName() + "', since found account '" +accOnResource.getName() + "' has no account attribute for resource attribute '" + currIA.getResourceAttributeSource().getDisplayName() + "'");  
 					continue;
 				}
 				
 				//Argh, factory creates one value by default
 				UserIdentityAttribute uia = UserIdentityAttribute.factory(currIA, user);
 				uia.getValues().clear();
 				
 				try {
 					uia.importValues(currAA.getAsStandardAttribute().getValues());
 					user.getDynamicUserAttributes().add(uia);
 				} catch (AttributeSetValueException e) {
 					throw new OperationException("Could not load user attribute '" + currIA.getUniqueName() + "' due to: '" + e.getMessage());
 				}
 			}
 		}
 		
 		user.setLoaded(true);
 	}
 	
 	public User findUserEagerly(String name) {
 		log.debug("Finding User eaglery...");
 		
 		User user = findUser(name);
 		if (user != null) {
 			user.getAccounts().size();
 			user.getUserRoles().size();
 
 			return user;
 		} else {
 			return null;
 		}
 	}
 	
 	public User findUserById(long userId) {
 		try {
 			User loadedUser = (User) getEntityManager().find(User.class, userId);
 
 			return loadedUser;
 		} catch (NoResultException e) {
 			throw new NoResultException("No such result exception occured: Couldnt load User with ID: "+ userId);
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 	}
 	
 	
 	public boolean isUserExit(String userName) {
 		log.trace("Checking whether username: " + userName + " exist or not...");
 		Query q = getEntityManager().createNamedQuery("user.isExistsByName").setParameter("userName", userName.toUpperCase());
 		Long num = (Long) q.getSingleResult();
 
 		if (num == 0) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	/*
 	//if username is null, then a new user name will be generated if there is a user plugin ID, otherwise an exception will be raised 
 	public User factoryUser(List<UserIdentityAttribute> userIdentityAttributes, String userName) throws ObjectsConstructionException {
 		User user = User.factory(userIdentityAttributes, userName);
 		
 		if (userName == null) {
 			//then a new user id must be generated via user plugin id
 			//generate via plugin id!
 		}
 	}
 	*/
 	
 	
 	public Collection<User> findAllUsers() {
 		try {
 			return getEntityManager().createNamedQuery("user.findAll").getResultList();
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 	}
 	
 	
     public User factoryUser(String userName) {
         User user = new User();
         if (userName != null) 
         	user.setName(userName);
         user.setDisabled(false);
         user.setCreationDate(new Date());
         
         //before persisting the user, add the user to the default capability folder.
 		CapabilityFolder cf = findCapabilityFolder("STANDARD_USER");
 		
 		if (cf == null) {
 			log.error("Couldn't attach User to capability folder 'STANDARD_USER' as it does not exist.");
 		} else {
 			user.getCapabilityFolders().add(cf);
 		}
         
         return user;
     }
 	
     
     //hopefully always invoked with an object that was created via "factoryUser"
 	public void persistUserEntity(User user) throws PersistEntityException {
 		try {
 			invokeCreateUserEvent(user,true);
 			//persist user anyway, events should not cause user persistancy to fail, this is too risky
 			//what for? should be done using User.factoryUser (always should go thorugh factory!!!)
 			user.setCreationDate(new Date());
 			getEntityManager().persist(user);
 			invokeCreateUserEvent(user,false);
 		} catch (ScriptInvocationException e) {
 			//log.error(e.toString());
 			throw new PersistEntityException(e);
 		}
 	}
 	
 	public Long getCreatedUsersAmount(Date from, Date to) {
 		Query q = getEntityManager().createQuery("select count(*) from User user where user.creationDate > :fromDate and user.creationDate < :toDate").setParameter("fromDate",from).setParameter("toDate",to);
 		
 		Long num = (Long) q.getSingleResult();
 		
 		return num;
 	}
 	
 	
 	public void assignCapabilityToUser(User user, String capabilityUniqueName) throws EntityAssociationException {
 		Capability c = findCapability(capabilityUniqueName);
 		
 		if (c == null) {
 			throw new EntityAssociationException("Capabiity was not found!");
 		}
 		
 		user.getCapabilities().add(c);
 		getEntityManager().merge(user);
 	}
 	
 	
 	public Capability findCapability(String uniqueName) {
 		log.debug("Finding Capability in repository with name '" + uniqueName + "'");
 
 		try {
 			Query q = getEntityManager().createNamedQuery("capability.findByName").setParameter("name",uniqueName);
 			return (Capability) q.getSingleResult();
 		}
 		catch (javax.persistence.NoResultException e) {
 			log.debug("Found capability did not result any user for name '" + uniqueName + "', returning null.");
 			return null;
 		}
 	}
 	
 	
 	public List<User> findAllUsersAssignedToRole(Role role) {
 		return getEntityManager().createNamedQuery("user.findAssignedToRole").setParameter("role", role).getResultList();
 	}
 	
 	
 	//compare value as UPPERCASE (non-case sensitive)
 	@Deprecated
 	public User findUser(String identityAttributeUniqueName, String value) {
 		String query = "SELECT DISTINCT vl_user.* FROM VL_USER vl_user,VL_IDENTITY_ATTRIBUTE ia, VL_USER_IDENTITY_ATTRIBUTE uia,"
 			+ "VL_USER_IDENTITY_ATTR_VALUE uiav WHERE"
 			+ " uia.IDENTITY_ATTRIBUTE_ID = ia.IDENTITY_ATTRIBUTE_ID AND uia.USER_ID = vl_user.USER_ID"
 			+ " AND uiav.USER_IDENTITY_ATTRIBUTE_ID = uia.USER_IDENTITY_ATTRIBUTE_ID"
 			+ " AND (ia.UNIQUE_NAME=:identityAttributeUniqueName)"
 			+ " AND ( (UPPER(uiav.VALUE_STRING) like :uiaValue) )"; 
 		
 		try {
 			Query q = getEntityManager().createNativeQuery(query, User.class).setParameter("identityAttributeUniqueName",identityAttributeUniqueName).setParameter("uiaValue", value.toUpperCase());
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
 	}
 	
 	
 	public List<User> findUsersByFullName(String fullName, int maxResults) {
 		Map<String,String> mapAttrs = new HashMap<String,String>();
 		
 		if ( (fullName == null) || (fullName.length() < 1) ) return null;
 		
 		fullName = fullName.trim();
 		if (!fullName.contains(" ")) {
 			mapAttrs.put("FIRST_NAME", fullName + "%");
 		} else {
 			String[] arr = fullName.split(" ");
 			mapAttrs.put("FIRST_NAME", arr[0] + "%");
 			mapAttrs.put("LAST_NAME", arr[1] + "%");
 		}
 		
 		return findUsers(mapAttrs,false,true,maxResults);
 	}
 	
 	public List<User> findUsers(Map<String,String> ias, boolean caseSensitive) {
 		return findUsers(ias, caseSensitive,false,0);
 	}
 	
 	//TODO: Support case-sensitive for values
 	//TODO: Support data types (currently only STRING is supported)
 	public List<User> findUsers(Map<String,String> ias, boolean caseSensitive, boolean wildCardSearch, int maxResults) {
 		log.debug("Finding users based on Identity Attributes has started...");
 		log.debug("Amount of creteria Identity Attributes is '" + ias.size() + "'");
 		
 		if (ias.size() < 1) {
 			log.warn("Could not invoke findUsers as no IdentityAttributes creterias were specified at all!");
 			return null;
 		}
 		
 		//Load IAs entities
 		log.debug("Loading Identity Attributes entities with amount '" + ias.size() + "'");
 		List<IdentityAttribute> iaEntities = identityAttributeManager.findIdentityAttribues(ias.keySet());
 		
 		Map<String,IdentityAttribute> allIdentityAttributesEntities = new HashMap<String,IdentityAttribute>();
 		for (IdentityAttribute currIA : iaEntities) {
 			allIdentityAttributesEntities.put(currIA.getUniqueName().toUpperCase(), currIA);
 		}
 		
 		log.debug("Successfully loaded Identity Attributes entities with amount '" + iaEntities.size() + "'");
 		
 		if (iaEntities.size() != ias.size()) {
 			throw new RuntimeException("The specified identity attributes names are not equal to the amount of loaded Identity Attributes entities!");
 		}
 		
 
 		
 		//Treeset for keepting the right order.
 		Map<String,String> treeMapOfLocalIdentityAttributes = new TreeMap<String,String>();
 		//for (Map.Entry<String,String> currEntry : ias.entrySet()) {
 		for (IdentityAttribute currIA : iaEntities) {
 			if (currIA.getSource() == IdentityAttributeSources.LOCAL) {
 				treeMapOfLocalIdentityAttributes.put(currIA.getUniqueName().toUpperCase(), ias.get(currIA.getUniqueName().toUpperCase()));
 			}
 		}
 		log.debug("Constructed a Map(tree) of local identity attributes with amount '" + treeMapOfLocalIdentityAttributes.size());
 		
 		Map<String,String> treeMapOfRAIdentityAttributes = new TreeMap<String,String>();
 		for (IdentityAttribute currIA : iaEntities) {
 			if (currIA.getSource() == IdentityAttributeSources.RESOURCE_ATTRIBUTE) {
 				treeMapOfRAIdentityAttributes.put(currIA.getUniqueName().toUpperCase(), ias.get(currIA.getUniqueName().toUpperCase()));
 			}
 		}
 		log.debug("Constructed a Map(tree) of resource_attribute identity attributes with amount '" + treeMapOfRAIdentityAttributes.size());
 		
 		
 		
 		String uiaPrefix = "uia";
 		String uiaValPrefix = "uiaVal";
 		String accAttrPrefix = "accAttr";
 		String accAttrValPrefix = "accAttrVal";
 		
 		
 		StringBuilder query = new StringBuilder("select user FROM User user, Account account, IN (user.accounts) userAccount");
 		int i=0;
 		String uiaVarName;
 		String uiaValVarName;
 		for (Map.Entry<String,String> currEntry : treeMapOfLocalIdentityAttributes.entrySet()) {
 			i++;
 			uiaVarName = uiaPrefix + i;
 			uiaValVarName = uiaValPrefix + i;
 			query.append(", IN(user.localUserAttributes) " + uiaVarName + ", IN(" + uiaVarName + ".values) " + uiaValVarName);
 		}
 		
 		
 		String accAttrVarName;
 		String accAttrValVarName;
 		i=0;
 		for (Map.Entry<String,String> currEntry : treeMapOfRAIdentityAttributes.entrySet()) {
 			i++;
 			accAttrVarName = accAttrPrefix + i;
 			accAttrValVarName = accAttrValPrefix + i;
 			query.append(", IN(account.accountAttributes) " + accAttrVarName + ", IN(" + accAttrVarName + ".values) " + accAttrValVarName);
 		}
 		
 		
 		query.append(" WHERE ( ");
 		String uiaContent;
 		String uiaValueContent;
 		i=0;
 		for (Map.Entry<String,String> currEntry : treeMapOfLocalIdentityAttributes.entrySet()) {
 			i++;
 			uiaVarName = uiaPrefix + i;
 			uiaValVarName = uiaValPrefix + i;
 			uiaContent = ":"+uiaVarName+"_Content";
 			uiaValueContent = ":"+uiaValVarName+"_Content";
 			query.append("(" + uiaVarName + ".identityAttribute = " + uiaContent + " AND ");
 			query.append(caseSensitive ? "" : "UPPER(");
 			query.append(uiaValVarName + ".valueString");
 			query.append(caseSensitive ? "" : ")");
 			
 			if (wildCardSearch) {
 				query.append(" like " + uiaValueContent + ")");
 			} else {
 				query.append(" = " + uiaValueContent + ")");
 			}
 			
 			if (i < treeMapOfLocalIdentityAttributes.size()) query.append(" AND ");
 		}
 		
 		
		if (treeMapOfLocalIdentityAttributes.size()>0 && treeMapOfRAIdentityAttributes.size() > 0) {
 			query.append(" AND ");
 		}
 		
 		
 		String accAttrContent;
 		String accAttrValueContent;
 		i=0;
 		for (Map.Entry<String,String> currEntry : treeMapOfRAIdentityAttributes.entrySet()) {
 			i++;
 			accAttrVarName = accAttrPrefix + i;
 			accAttrValVarName = accAttrValPrefix + i;
 			accAttrContent = ":"+accAttrVarName+"_Content";
 			accAttrValueContent = ":"+accAttrValVarName+"_Content";
 			query.append("(" + accAttrVarName + ".resourceAttribute = " + accAttrContent + " AND ");
			
			query.append(" " + accAttrVarName + ".resourceAttribute.resource = " + "account.resource AND ");
			
			
 			query.append(caseSensitive ? "" : "UPPER(");
 			query.append(accAttrValVarName + ".valueString");
 			query.append(caseSensitive ? "" : ")");
 			
 			if (wildCardSearch) {
 				query.append(" like " + accAttrValueContent + ")");
 			} else {
 				query.append(" = " + accAttrValueContent + ")");
 			}
 			
 			if (i < treeMapOfRAIdentityAttributes.size()) query.append(" AND ");
 		}
 		
 		
		query.append(" AND userAccount = account");
 		
		query.append(" )");
 		
 		
 		
 		
 		log.debug("Generated query is: " + query.toString());
 		
 		Query q = getEntityManager().createQuery(query.toString());
 		i = 0;
 		for (Map.Entry<String,String> currEntry : treeMapOfLocalIdentityAttributes.entrySet()) {
 			i++;
 			uiaContent = uiaPrefix + i + "_Content";
 			uiaValueContent = uiaValPrefix + i + "_Content";
 			
 			log.trace("Attaching value of parameter name " + allIdentityAttributesEntities.get(currEntry.getKey()).getUniqueName().toUpperCase() + " to value: '" + currEntry.getKey() + "'");
 			log.trace("Attaching value of parameter name " + uiaValueContent + " to value: '" + currEntry.getValue() + "'");
 			
 			//q.setParameter(uiaContent, currEntry.getKey());
 			q.setParameter(uiaContent, allIdentityAttributesEntities.get(currEntry.getKey().toUpperCase()));
 			q.setParameter(uiaValueContent, caseSensitive ? currEntry.getValue() : currEntry.getValue().toUpperCase());
 		}
 		
 		
 		//SET RESOURCE ATTRIBUTE AS THE ENTITY ITSELF NOT THE UNIQUE NAME OF IT!
 		i = 0;
 		for (Map.Entry<String,String> currEntry : treeMapOfRAIdentityAttributes.entrySet()) {
 			i++;
 			accAttrContent = accAttrPrefix + i + "_Content";
 			accAttrValueContent = accAttrValPrefix + i + "_Content";
 			
 			log.trace("Attaching value of parameter name " + accAttrContent + " to value: '" + currEntry.getKey() + "'");
 			log.trace("Attaching value of parameter name " + accAttrValueContent + " to value: '" + currEntry.getValue() + "'");
 			
 			//q.setParameter(accAttrContent, currEntry.getKey());
 			q.setParameter(accAttrContent, allIdentityAttributesEntities.get(currEntry.getKey().toUpperCase()).getResourceAttributeSource());
 			q.setParameter(accAttrValueContent, caseSensitive ? currEntry.getValue() : currEntry.getValue().toUpperCase());
 		}
 		
 		
 		
 		if (maxResults != 0) {
 			q.setMaxResults(maxResults);
 		}
 		
 		
 		log.debug("Performing query against repository and getting result...");
 		
 		/*
 		catch (javax.persistence.NoResultException e) {
 			log.debug("Could not find any User for IdentityAttribute name '" + identityAttributeUniqueName + "', with value '" + value + "'");
 			return null;
 		}
 		catch (javax.persistence.NonUniqueResultException e) {
 			log.warn("Cannot retrieve user for IA '" + identityAttributeUniqueName + "', with value '" + value + "': " + e.getMessage());
 			return null;
 		}
 		*/
 		
 		//Query q = getEntityManager().createQuery("select user FROM User user, IN(user.userIdentityAttributes) uia1, IN(uia1.values) uiaVal1, IN(user.userIdentityAttributes) uia2, IN(uia2.values) uiaVal2 WHERE ( (uia1.identityAttribute.uniqueName = 'FIRST_NAME' AND uiaVal1.valueString = 'Admini') AND (uia2.identityAttribute.uniqueName = 'LAST_NAME' AND uiaVal2.valueString = 'Strator') )");
 		/*
 		Query qa = getEntityManager().createQuery("select user FROM User user, IN(user.userIdentityAttributes) uia1, IN(uia1.values) uiaVal1, IN(user.userIdentityAttributes) uia2, IN(uia2.values) uiaVal2 WHERE ( (uia1.identityAttribute.uniqueName = :uia1_Content AND uiaVal1.valueString = :uiaVal1_Content) AND (uia2.identityAttribute.uniqueName = :uia2_Content AND uiaVal2.valueString = :uiaVal2_Content) )");
 		qa.setParameter("uia1_Content", "FIRST_NAME");
 		qa.setParameter("uiaVal1_Content", "Admini");
 		qa.setParameter("uia2_Content", "LAST_NAME");
 		qa.setParameter("uiaVal2_Content", "Strator");
 		*/
 		
 		Stopwatch s = new Stopwatch();
 		s.start();
 		List<User> result = q.getResultList();
 		s.stop();
 		log.debug("The resulted users amount is '" + result.size() + "', execution time: '" + s.asSeconds() + "' seconds.");
 		
 		if (log.isTraceEnabled()) {
 			log.trace("Retrieving full name from all fetched users...");
 			int ii=0;
 			for (User currUser : result) {
 				ii++;
 				log.trace("Full name of user: " + currUser.getFullName());
 			}
 		}
 		
 		return result;
 	}
 	
 	//SEEK IN DIRECT ROLES / ROLES INHERITED FROM POSITIONS
 	public Map<String,User> findUsersAssignedToRole(String roleName) {
 		Map<String,User> map = new HashMap<String,User>();
 		List<User> usersWithDirectRole = getEntityManager().createNamedQuery("user.findAssignedDirectlyToRole").setParameter("roleName", roleName).getResultList();
 		List<User> usersWithRolesInheritedFromPos = getEntityManager().createNamedQuery("user.findAssignedToRoleByPositions").setParameter("roleName", roleName).getResultList();
 		
 		for (User currUser : usersWithDirectRole) {
 			if (!map.containsKey(currUser.getName())) {
 				map.put(currUser.getName(), currUser);
 			}
 		}
 		
 		
 		for (User currUser : usersWithRolesInheritedFromPos) {
 			if (!map.containsKey(currUser.getName())) {
 				map.put(currUser.getName(), currUser);
 			}
 		}
 		
 		
 		return map;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	//helper
 	private void invokeCreateUserEvent(User user, boolean pre) throws ScriptInvocationException {
 		OperationContext context = new OperationContext();
 		context.addVar("user", user);
 		context.addVar("userName", user.getName());
 		context.addVar("userIdAttrs", user.getUserIdentityAttributesAsMap());
 		
 		if (pre) {
 			try {
 				eventManager.raiseSystemEvent(SystemEvent.EVENT_PRE_USER_CREATION, context);
 			} catch (EventExecutionException e) {
 				log.error("Could not raise '" + SystemEvent.EVENT_PRE_USER_CREATION + "' system event: " + e.getMessage());
 			}
 		}else {
 			try {
 				eventManager.raiseSystemEvent(SystemEvent.EVENT_POST_USER_CREATION, context);
 			} catch (EventExecutionException e) {
 				log.error("Could not raise '" + SystemEvent.EVENT_POST_USER_CREATION + "' system event: " + e.getMessage());
 			}
 		}
 		
 		//eventManager.invokeEventDefinitionResponses(ed, context);
 		//eventManager.invokeEvent(ed, context);
 	}
 	
 	public Collection<User> findUsersToSync() {
 		return getEntityManager().createNamedQuery("user.findUsersToSync")
 				.getResultList();
 	}
 
 	
 	
 	public boolean authenticate(String username, String password, String ip) throws UserAuthenticationException {
 		log.debug("Authenticating user: '" + username + "', with password: '"+ "*******" + "', from IP: " + ip);
 
 		// Query database to see if user exists.
 		//try {
 			User user = findUser(username);
 	
 			if (user == null) {
 				String msg = "Could not authenticate user named: '" + username
 				+ "', user does not exist in repository!";
 				log.warn(msg);
 				// Log to DB
 				// edmLogger.warning(msg);
 				throw new UserAuthenticationException(msg);
 			}
 			
 			
 
 			if (user.isLocked()) {
 				String msg = "User named '" + username
 				+ "' is Locked, cannot login user from IP: '" + ip
 				+ "'";
 				// Log to DB
 				// edmLogger.warning(msg);
 				log.warn(msg);
 				throw new UserAuthenticationException(msg);
 			}
 
 			// Check whether user should get authenticated locally or not
 			if (user.isAuthenticatedViaLocalPassword()) {
 				log.debug("Authentication is being performed against internal repository...");
 				
 				
 				//PERFORM INTERNAL AUTHENTICATION
 				String saultedHash = PasswordHash.instance().generateSaltedHash(password, user.getName());
 				
 				if (user.getPassword().equals(saultedHash)) {
 					// Detemrine if a clean to user auth failures counter is
 					// required
 					if (user.getAuthFailureCounter() > 0) {
 						resetUserLockAndAuthFailureCounter(user);
 					}
 
 					String msg = "User '" + user.getName()+ "' successfully authenticated from IP '" + ip
 					+ "'";
 					// edmLogger.info();
 					log.info("msg");
 					return true;
 				} else {
 					userAuthFailure(user, ip);
 					return false;
 				}
 			} else {
 				// TODO: Support authentication via external target systems
 				//throw new UserAuthenticationException(
 				//		"Currently external user authentication is not supported!");
 
 
 
 				//authenticate via external resources
 				log.debug("Authentication will be performed against external resource...");
 				String resourceName = SysConf.getSysConf().getString("security.authentication.admin_interface.resource_name");
 				log.trace("External resource to authenticate through is: '" + resourceName + "'");
 				if (resourceName == null) {
 					throw new UserAuthenticationException("Resource to authenticate with in system configuration is null.");
 				}
 
 				Resource r = resourceManager.findResource(resourceName);
 
 				if (r == null) {
 					throw new UserAuthenticationException("Could not find resource name '" + resourceName + "' to authenticate with");
 				}
 
 				log.trace("Found resource in repository, authenticating!");
 
 
 				resourceOperationsManager.authenticate(r, username, password);
 
 				return true;
 
 				/*
 		ResourceOperationController roc = (ResourceOperationController)r.getResourceType().factoryResourceOperationsController();
 		roc.setResource(r);
 
 		try {
 			//return the authentication result
 			return roc.authenticate(user.getName(), password);
 		}
 		catch (AuthenticationFailureException e) {
 			log.info("Failed to authenticate user '" + user.getName() + "' against resource '" + resourceName + "': " + e.toString());
 			throw new UserAuthenticationException(e.toString());
 		}
 				 */
 			}
 	}
 	
 	
 	private void resetUserLockAndAuthFailureCounter(User user) {
 		user.setLocked(false);
 		user.setAuthFailureCounter(0);
 		updateUser(user);
 	}
 
 
 	
 	
 	
 	//TODO: Move to more suitable place.
 	public CapabilityFolder findCapabilityFolder(String uniqueName) {
 		
 		log.debug("Finding Capability Folder in repository with unique name '" + uniqueName + "'");
 
 		try {
 			Query q = getEntityManager().createNamedQuery("capabilityFolder.findByUniqueName").setParameter("uniqueName", uniqueName);
 			return (CapabilityFolder) q.getSingleResult();
 		}
 		catch (javax.persistence.NoResultException e) {
 			log.debug("Found Capability Folder did not result any capability folder for unique name '" + uniqueName + "', returning null.");
 			return null;
 		}
 	}
 	
 	
 	public void setEntityManager(EntityManager entityManager) {
 		this.entityManager = entityManager;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	// !dirty
 	@Deprecated
 	public User reloadUser(User user, boolean eagerly) {
 		User loadedUser = getEntityManager().find(User.class, user.getUserId());
 
 		if (eagerly) {
 			touchUserReferences(loadedUser);
 		}
 
 		return loadedUser;
 	}
 
 	@Deprecated
 	public User loadFreshedUserById(long userId) {
 		User loadedUser = (User) getEntityManager().createNamedQuery("findUserById")
 				.setParameter("userId", userId).getSingleResult();
 
 		return loadedUser;
 	}
 
 
 	@Deprecated
 	public Collection<User> findUsersByString(String searchString) {
 		// Break the string to two saparated strings by space
 		String[] splited = searchString.split(" ");
 
 		String str1;
 		String str2;
 		if (splited.length > 1) {
 			str1 = splited[0];
 			str2 = splited[1];
 		} else {
 			str1 = splited[0];
 			str2 = str1;
 		}
 
 		// jul-20-07: GF works with #param, with hibernate, changed from # to :
 		String query = "SELECT DISTINCT idm_user.* FROM VL_USER idm_user,VL_IDENTITY_ATTRIBUTE ia, VL_USER_IDENTITY_ATTRIBUTE uia,"
 				+ "VL_USER_IDENTITY_ATTR_VALUE uiav WHERE (ia.IDENTITY_ATTRIBUTE_ID=1 OR ia.IDENTITY_ATTRIBUTE_ID=2) "
 				+ "AND ( (UPPER(uiav.VALUE_STRING) like :searchString1) OR (UPPER(uiav.VALUE_STRING) like :searchString2) OR (UPPER(vl_user.name) like :searchString1) OR (UPPER(vl_user.name) like :searchString2) )"
 				+ "AND uia.USER_ID = vl_user.USER_ID AND ia.IDENTITY_ATTRIBUTE_ID = uia.IDENTITY_ATTRIBUTE_ID AND uia.USER_IDENTITY_ATTRIBUTE_ID = uiav.USER_IDENTITY_ATTRIBUTE_ID";
 
 		log.trace("Seeking User in repository, executing query: " + query);
 
 		Query q = getEntityManager().createNativeQuery(query, User.class).setParameter(
 				"searchString1", str1).setParameter("searchString2", str2);
 
 		if (q.getResultList().size() > 0) {
 			return q.getResultList();
 		} else {
 			return getEntityManager().createNamedQuery("user.searchUsersByString")
 					.setParameter("searchString", searchString).getResultList();
 		}
 
 		// String query = "SELECT DISTINCT idm_user.* FROM IDM_USER idm_user
 		// where UPPER(idm_user.name) like #searchString";
 		// return getEntityManager().createNativeQuery(query,
 		// User.class).setParameter("searchString",searchString.toUpperCase()).getResultList();
 	}
 
 	@Deprecated
 	public Collection<User> findUserByIdentityAttributes(
 			Collection<IdentityAttribute> identityAttributes,
 			String searchString) {
 		Collection<User> users = new HashSet<User>();
 		Collection<UserIdentityAttribute> relevantUIAs = new ArrayList<UserIdentityAttribute>();
 
 		for (IdentityAttribute ia : identityAttributes) {
 			// Load all UserIdentityAttributes for the specified IAs
 			Collection<UserIdentityAttribute> uias = getEntityManager().createNamedQuery(
 					"uia.findByIdentityAttributes").setParameter(
 					"identityAttribute", ia).getResultList();
 			relevantUIAs.addAll(uias);
 		}
 
 		for (UserIdentityAttribute uia : relevantUIAs) {
 			// System.out.println("UIAAAAAAAAAAAAAAAAAAAAAAAAAAAA IA ID: " +
 			// uia.getIdentityAttribute().getIdentityAttributeId());
 
 			try {
 				if (uia.getFirstValue().getAsString().toUpperCase().contains(
 						searchString.toUpperCase())) {
 					users.add(uia.getUser());
 				}
 			} catch (NoResultFoundException ex) {
 				continue;
 			}
 		}
 
 		return users;
 	}
 
 
 	@Deprecated
 	public Collection<UserRole> findAllUserRolesForUser(User user) {
 		return getEntityManager().createNamedQuery("userRole.findByUser").setHint(
 				"toplink.refresh", "true").setParameter("user", user)
 				.getResultList();
 	}
 
 
 	@Deprecated
 	public User loadUserByName(String userName, boolean eagerly)
 			throws NoResultFoundException {
 		try {
 			User loadedUser = (User) getEntityManager().createNamedQuery("findUserByName")
 					.setParameter("userName", userName.toUpperCase())
 					.getSingleResult();
 
 			if (eagerly) {
 				touchUserReferences(loadedUser);
 			}
 
 			return loadedUser;
 		} catch (NoResultException e) {
 			throw new NoResultFoundException(
 					"Couldnt find User for User Name: " + userName
 							+ ", detailed message is: " + e.getMessage());
 		}
 	}
 
 	@Deprecated
 	public User findUserByName(String userName) throws NoResultFoundException {
 		try {
 			return (User) getEntityManager().createNamedQuery("findUserByName").setParameter(
 					"userName", userName.toUpperCase()).getSingleResult();
 		} catch (NoResultException e) {
 			throw new NoResultFoundException(
 					"Couldnt find User for User Name: " + userName
 							+ ", detailed message is: " + e.getMessage());
 		}
 	}
 
 
 	@Deprecated
 	public void persistUserEntityPartOfReconcileUsers(User user) {
 		user.setCreationDate(new Date());
 		getEntityManager().persist(user);
 
 		/*
 		 * try { eventManager.createEventResponsesOfEventDefinition(ed,
 		 * properties); } catch (EventResponseException ex) { //TODO: Replace
 		 * with throwing an exception, events integrity is very important!
 		 * log.warn("Could not fire events due to: " + ex); }
 		 */
 	}
 
 	@Deprecated
 	public void persistUsers(Collection<User> users) {
 		//TODO: SHOULD SUPPORT BULK EVENT DEF EXECUTION?
 		/*JB
 		try {
 			// Trigger event
 			EventDefinition ed = eventManager.find(eventUserCreateSuccessName);
 			List<Map<String, Object>> allUsersProps = new ArrayList<Map<String, Object>>();
 
 			for (User currUser : users) {
 				getEntityManager().persist(currUser);
 
 				Map<String, Object> properties = new HashMap<String, Object>();
 				properties.put("user", currUser);
 				allUsersProps.add(properties);
 			}
 
 			eventManager.createEventResponsesOfEventDefinition(ed,
 					allUsersProps);
 		} catch (NoResultFoundException nrfe) {
 			log
 					.warn("Warning, could not create an event named: '"
 							+ eventUserCreateSuccessName
 							+ "' since the event does not exist by the specified name!");
 		}
 		// TODO handle event response exceptions correctly! they are important!
 		catch (EventResponseException ere) {
 			log.warn("An event response exception has occured: " + ere);
 		}
 		*/
 	}
 
 	@Deprecated
 	public void removeUser(User user) {
 		if (!user.isProtected()) {
 			getEntityManager().remove(user);
 		} else {
 			log.info("Cannot remove user named: '" + user.getName()
 					+ "', user is immune!");
 		}
 	}
 
 	@Deprecated
 	public void removeUser(long userId) {
 		try {
 			User user = getEntityManager().find(User.class, userId);
 			removeUser(user);
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 	}
 
 	@Deprecated
 	public void removeUsers(Collection<User> users) {
 		for (User currUser : users) {
 			removeUser(currUser);
 		}
 	}
 
 	@Deprecated
 	public void updateUser(User user) {
 		getEntityManager().merge(user);
 	}
 
 	@Deprecated
 	public void disableUserInRepository(User user) {
 		user.setDisabled(true);
 		updateUser(user);
 	}
 
 	@Deprecated
 	public void changeUserPassword(User user, String newPassword)
 			throws PasswordValidationException {
 		String passPolicyUniqueName = SysConf.getSysConf().getString(
 				"security.users.password-policy-unique-name");
 
 		if (passPolicyUniqueName == null) {
 			throw new PasswordValidationException(
 					"Users Password Policy was not set in system configuration!");
 		}
 //		try {
 			// Try to load the specified users password policy
 //			PasswordPolicy pp = passwordManager
 //					.findPasswordPolicyByUniqueName(passPolicyUniqueName);
 
 //			pp.validate(newPassword, user.getName());
 			// PasswordValidationException
 			user.setPassword(newPassword);
 			//user.encryptPassword();
 			updateUser(user);
 //		} catch (NoResultFoundException ex) {
 	//		throw new PasswordValidationException(ex);
 //		} /*catch (EncryptionException ex) {
 			//throw new PasswordValidationException(ex);
 		//}
 	}
 
 	@Deprecated
 	public void createUserIdentityAttribute(UserIdentityAttribute uia) {
 		getEntityManager().persist(uia);
 	}
 
 	@Deprecated
 	public Collection<User> findUsers(String wildCardAccountName) {
 		String searchPattern = wildCardAccountName.toUpperCase().replace('*',
 				'%');
 
 		log.debug("Checking whether users for wildCard string: '"
 				+ searchPattern + "' exist or not!");
 
 		Query q = getEntityManager().createNamedQuery("user.searchUsersByString").setParameter(
 				"searchString", searchPattern);
 
 		return q.getResultList();
 	}
 
 	@Deprecated
 	public UserIdentityAttribute getUserIdentityAttribute(User user,
 			String attrName) throws NoUserIdentityAttributeFoundException {
 		Query q = getEntityManager()
 				.createQuery(
 						"SELECT object(uia) FROM UserIdentityAttribute AS uia, IdentityAttribute as ia WHERE (uia.identityAttribute = ia.identityAttributeId AND ia.name = :name AND uia.user = :user)")
 				.setParameter("name", attrName).setParameter("user", user);
 		try {
 			UserIdentityAttribute uia = (UserIdentityAttribute) q
 					.getSingleResult();
 			/*
 			 * log.info("Retrieved user identity attribute for user name: " +
 			 * user.getName() + ", for attribute name: " + attrName + ", with
 			 * value: " + uia.getOldValues().iterator().next());
 			 */
 			return uia;
 		}
 		// This could happen when there is no UserIdentityAttribute entry for a
 		// user
 		// Might occure if the attribute is not a must / Synch process was not
 		// run yet after a new IdentityAttribute was added
 		catch (NoResultException nre) {
 			throw new NoUserIdentityAttributeFoundException(
 					"Couldnt get UserIdentityAttribute for user: "
 							+ user.getName() + ", for attribute name: "
 							+ attrName);
 		}
 	}
 
 	@Deprecated
 	public void createUserIdentityAttributesForUser(Attributes attributes,
 			User user) throws UnsupportedAttributeTypeException {
 		log.info("Creating UserIdentityAttributes for user name '"
 				+ user.getName() + "'");
 		for (Attribute currAttr : attributes.asCollection()) {
 			log.info("Creating UserIdentityAttribute for Attribute name '"
 					+ currAttr.getName() + "'");
 
 			// Make sure there is no such an attribute for the user already,
 			// otherwise skip it
 			try {
 				getUserIdentityAttribute(user, currAttr.getName());
 
 				// If found then continue
 				log
 						.info("Cannot create User Identity Attribute, attribute named: '"
 								+ currAttr.getName()
 								+ "' already exist for user name: '"
 								+ user.getName() + "'");
 				continue;
 			} catch (NoUserIdentityAttributeFoundException nuiafe) {
 				try {
 					IdentityAttribute ia = identityAttributeManager
 							.findIdentityAttributeByUniqueName(currAttr
 									.getName());
 					UserIdentityAttribute uia = new UserIdentityAttribute();
 					uia.setIdentityAttribute(ia);
 					uia.setUser(user);
 					// uia.setValues(currAttr.getValues());
 //					uia.setValuesByAV(currAttr.getValues());
 
 					user.getUserIdentityAttributes().add(uia);
 
 					persistUserIdentityAttributeEntity(uia);
 				} catch (NoResultFoundException nrfe) {
 					log
 							.warn("Cannot create User Identity attribute, attribute named: '"
 									+ currAttr.getName()
 									+ "' is not DEFINED in the respositor as an Identity Attribute!");
 				} /*catch (UnsupportedAttributeTypeException uate) {
 					throw new UnsupportedAttributeTypeException(
 							"Couldnt create User Identity Attribute due to: "
 									+ uate.getMessage());
 				}*/
 			}
 		}
 	}
 
 	// TODO: Add it to the interface.
 	@Deprecated
 	public void persistUserIdentityAttributeEntity(UserIdentityAttribute uia) {
 		getEntityManager().persist(uia);
 	}
 
 
 	@Deprecated
 	public boolean isRoleAssignedToUser(User user, Role role) {
 		log.debug("Checking whether user: '" + user.getName()
 				+ "' has the role '" + role.getName() + "' assigned or not...");
 
 		Query q = getEntityManager().createNamedQuery("userRole.findByRoleAndUser")
 				.setParameter("user", user).setParameter("role", role);
 
 		try {
 			q.getSingleResult();
 			return true;
 		} catch (NoResultException nre) {
 			return false;
 		}
 	}
 
 	/*
 	 * public void addRoleToUser(User user, Role role,User requester) throws
 	 * RequestExecutionException { try { Request req = new Request();
 	 * //req.initNewRequest(user,requester);
 	 * 
 	 * //TODO: This is only the supported option now
 	 * //req.setExecutedBySuperUser(true); //If it's a superuser, then set the
 	 * request as approved //req.setApproved(true);
 	 * 
 	 * //First persist request reqm.createRequestEntity(req);
 	 * 
 	 * //Then check whether the request should be executed now. /* if
 	 * (req.isApproved()) { reqm.executeRequest(req); }
 	 */
 	/*
 	 * } catch(RequestAttributeValidationException rave) { throw new
 	 * RequestExecutionException(rave.getMessage()); } }
 	 */
 	@Deprecated
 	public boolean isUserHasAccountOnResource(User user, Resource ts) {
 		// Retrieve all accounts, check per account if it's associated with the
 		// specified target system
 		/*
 		 * Iterator<Account> acctIterator = user.getAccounts().iterator();
 		 * while (acctIterator.hasNext()) { Account currAccount =
 		 * acctIterator.next(); if (currAccount.getResource().getResourceId() ==
 		 * ts .getResourceId()) { return true; } }
 		 */
 
 		Long totalAccounts = (Long) getEntityManager()
 				.createQuery(
 						"select count(account) from Account account where account.user = :user and account.resource = :resource")
 				.setParameter("user", user).setParameter("resource", ts)
 				.getSingleResult();
 
 		System.out.println("Total accounts for User: " + user.getName()
 				+ ", on Resource named: " + ts.getDisplayName() + ", is: "
 				+ totalAccounts);
 		if (totalAccounts > 0) {
 			return true;
 		} else {
 			// Not found, return false
 			return false;
 		}
 	}
 
 	/*
 	 * We have this already! public UserIdentityAttribute
 	 * getUserIdentityAttribute(User user, String attributeName) { Attribute
 	 * attr = new Attribute(); Query iaq =
 	 * getEntityManager().createNamedQuery("findIdentityAttributeByName").setParameter("attributeName",
 	 * attributeName); IdentityAttribute ai =
 	 * (IdentityAttribute)iaq.getSingleResult();
 	 * 
 	 * Query uiaq =
 	 * getEntityManager().createNamedQuery("findUserIdentityAttributeByName").setParameter("user",user).setParameter("identityAttribute",ai);
 	 * UserIdentityAttribute uia =
 	 * (UserIdentityAttribute)uiaq.getSingleResult();
 	 * 
 	 * return uia; }
 	 */
 
 	@Deprecated
 	public Collection<Account> getAccountsForResourcePerUser(Resource ts,User user) {
 		System.out
 				.println("Verifying whether there are account(s) attached to Resource name: "
 						+ ts.getDisplayName()
 						+ ", for user name: "
 						+ user.getName());
 		Query q = getEntityManager().createNamedQuery("findAccountsForUserPerResource")
 				.setParameter("user", user).setParameter("resource", ts);
 		return q.getResultList();
 	}
 
 
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	@Deprecated
 	public void modifyUserAttribute(UserIdentityAttribute uia)
 			throws ModifyAttributeFailureException {
 		log.info("STARTED UserIdentityAttribute modification for user name: "
 				+ uia.getUser().getName() + ", for UserAttribute name: "
 				+ uia.getIdentityAttribute().getUniqueName());
 
 		// Initialize an indicator of the whole process
 		boolean processStatus = true;
 
 		// Initiate a message logger
 		EdmMessages ems = new EdmMessages();
 
 		// Verify if set attribute structure fits the corresponding
 		// IdentityAttribute
 
 		// Merge the entity
 		getEntityManager().merge(uia);
 
 		// 12/08/06
 		// There was a problem when more than one IdentityAttribute per user
 		// (through view) were changed
 		// Groovy action were created per attribute, but for some reason, in
 		// groovy action, the 2nd attribute modification
 		// Did not notified that the first attribute were changed, and
 		// re-modified the 1st attribute to its original value, dunno why
 		// anyway flushing the entity manager did not help.
 		// Synchronize the persistence context to the underlying database.
 		// getEntityManager().flush();
 
 		UserIdentityAttribute loadedOne = getEntityManager().find(UserIdentityAttribute.class,
 				uia.getUserIdentityAttributeId());
 		// System.out.println("*********LOADED ONE !!!!!!!!!********: " +
 		// loadedOne.getName() + ", value: " + loadedOne.getValue());
 
 		// Get a list of ResourceAttributes
 		/*
 		 * Moved to 'findAccountsByUserIdentityAttribute' method Collection<ResourceAttribute>
 		 * tsaList = tsam .findResourceAttributesAttachedToIdentityAttribute(uia
 		 * .getIdentityAttribute());
 		 */
 
 		/*
 		 * Moved to 'findAccountsByUserIdentityAttribute' Collection<Account>
 		 * accountList = am .findAccountsAttachedToTargetAttributesForUser(uia
 		 * .getUser(), tsaList);
 		 */
 
 	/*
 		Collection<Account> accountList = findAccountsByUserIdentityAttribute(uia);
 
 		log
 				.info("Factoring UpdateActions for all found accounts that should be affected...");
 		try {
 			Collection<ResourceAccountActionInterface> accountUpdateActionList = am
 					.getUpdateActions(accountList);
 
 			log
 					.info("Factored -"
 							+ accountUpdateActionList.size()
 							+ "- UpdateActions for the following number of accounts that should get affected: "
 							+ accountList.size());
 
 			Iterator<ResourceAccountActionInterface> actionsIterator = accountUpdateActionList
 					.iterator();
 
 			while (actionsIterator.hasNext()) {
 				ResourceAccountActionInterface tsaai = actionsIterator.next();
 				try {
 					tsaai.__execute__();
 				} catch (ActionFailureException afe) {
 					log.warn(afe.getMessage());
 
 					processStatus = false;
 
 					ems.warning(afe.getMessage());
 					// throw new
 					// ModifyAttributeFailureException(afe.getMessage());
 				}
 			}
 
 			log.info("ENDED UserIdentityAttribute modification for user name: "
 					+ uia.getUser().getName() + ", for UserAttribute name: "
 					+ uia.getIdentityAttribute().getUniqueName());
 		} catch (BulkActionsFactoryFailureException baffe) {
 			log.warn(baffe.getMessage());
 
 			processStatus = false;
 
 			ems.warning(baffe.getMessage());
 		}
 
 		if (!processStatus) {
 			// Log a new EventLog with the failure
 			String messageSummary = "UserIdentityAttribute name: "
 					+ uia.getIdentityAttribute().getUniqueName()
 					+ ", for user: " + uia.getUser().getName()
 					+ " was failed to get modified";
 			/*cum.addEventLog("UserManagement", "FAILURE", "HIGH",messageSummary, ems.toString());*/
 /*
 			throw new ModifyAttributeFailureException(
 					"There was a failure while modifying User Attribute, please see EventLog for details.");
 		}
 	}
 	*/
 
 /*	
 	@Deprecated
 	public void modifyUserAttribute(UserIdentityAttribute uia,
 			Attribute newAttribute) throws ModifyAttributeFailureException {
 
 		// SHITtry {
 		// SIHTTTTTTTTTTuia.getIdentityAttribute().validateAttribute(newAttribute);
 
 		log
 				.info("Succesfully validated that the new attribute value fits the UserIdentityAttribute structure");
 
 		// Modify the value within the entity!
 		// SHITTTTTTTTuia.setValueAsAttr(newAttribute);
 
 		modifyUserAttribute(uia);
 		/*
 		 * SHIT } catch (AttributeValidationException ave) { logger .warning("An
 		 * error occured while trying to verify the specified attribute,
 		 * message: " + ave.getMessage()); }
 		 */
 
 	//}
 
 	@Deprecated
 	public BulkTask deleteUserBulkTask(User user) throws OperationException {
 		log.info("Deleting user '" + user.getName());
 
 		if (user.isProtected()) {
 			throw new OperationException("Cannot delete user name: '"
 					+ user.getName() + "' since user is protected!");
 		}
 
 		BulkTask bulkTask = BulkTask.factory("Deleting user name: "
 				+ user.getName());
 
 		for (Account currAccount : user.getAccounts()) {
 			//JB!!! try {
 				//JB!!! bulkTask.addTask(am.deleteAccountTask(currAccount, bulkTask,null, null));
 				/*
 				 * log.warn("Couldnt delete account name: " +
 				 * currAccount.getName() + ", skipping account delete
 				 * operation...");
 				 */
 				continue;
 			/*} catch (TaskCreationException tce) {
 				// If one task was failed to be created, then delete the
 				// bulkTask and throw an exception
 				tm.deleteBulkTask(bulkTask);
 				throw new OperationException(
 						"Could not delete accounts associated with user '"
 								+ user.getName()
 								+ "' since one or more creation tasks operation were failed, message failure: "
 								+ tce.getMessage());
 			}*/
 		}
 
 		// Persist the bulk task
 		tm.persistBulkTask(bulkTask);
 		removeUser(user);
 		return bulkTask;
 	}
 
 	@Deprecated
 	public Long deleteUser(User user) throws OperationException {
 		log.info("Disabling accounts associated with user '" + user.getName()
 				+ "'");
 
 		BulkTask bt = deleteUserBulkTask(user);
 
 		// Persist the bulk task
 		tm.persistBulkTask(bt);
 
 		// TODO: Specify some other better places constants such as table names
 		// Remove MANY2MANY associations!
 		getEntityManager().createNativeQuery(
 				"DELETE FROM " + table_users_to_capabilities + " where USER_ID = "
 						+ user.getUserId()).executeUpdate();
 		getEntityManager()
 				.createNativeQuery(
 						"DELETE " + table_user_role + " where USER_ID = "
 								+ user.getUserId()).executeUpdate();
 
 		return bt.getBulkTaskId();
 	}
 
 	/*
 	@Deprecated
 	public BulkTask disableUserBulkTask(User user) throws OperationException {
 		BulkTask bulkTask = BulkTask.factory("Disabling user name: "
 				+ user.getName() + " and its associated accounts.");
 
 		for (Account currAccount : user.getAccounts()) {
 			try {
 				bulkTask.addTask(am.disableAccountTask(currAccount, bulkTask,
 						null, null));
 				/*
 				 * log.warn("Couldnt disable account name: " +
 				 * currAccount.getName() + ", skipping account disable
 				 * operation...");
 				 */
 			/*} catch (TaskCreationException tce) {
 				// If one task was failed to be created, then delete the
 				// bulkTask and throw an exception
 				tm.deleteBulkTask(bulkTask);
 				throw new OperationException(
 						"Could not disable accounts associated with user '"
 								+ user.getName()
 								+ "' since one or more creation tasks operation were failed, message failure: "
 								+ tce.getMessage());
 			}
 
 		}
 
 		return bulkTask;
 	}
 */
 	
 	
 	/*
 	@Deprecated
 	public Long disableUser(User user) throws OperationException {
 		log.info("Disabling accounts associated with user '" + user.getName()
 				+ "'");
 
 		user.setDisabled(true);
 		updateUser(user);
 
 		BulkTask bt = disableUserBulkTask(user);
 		// Persist the bulk task
 		tm.persistBulkTask(bt);
 
 		return bt.getBulkTaskId();
 	}
 	*/
 
 	/*
 	@Deprecated
 	public BulkTask enableUserBulkTask(User user) throws OperationException {
 		BulkTask bulkTask = BulkTask.factory("Enabling user name: "
 				+ user.getName() + " and its associated accounts.");
 
 		for (Account currAccount : user.getAccounts()) {
 			try {
 				bulkTask.addTask(am.enableAccountTask(currAccount, bulkTask,
 						null));
 				/*
 				 * log.warn("Couldnt enable account name: " +
 				 * currAccount.getName() + ", skipping account enable
 				 * operation...");
 				 *//*
 			} catch (TaskCreationException tce) {
 				throw new OperationException(
 						"Could not enable accounts associated with user '"
 								+ user.getName()
 								+ "' since one or more creation tasks operation were failed, message failure: "
 								+ tce.getMessage());
 			}
 		}
 
 		return bulkTask;
 	}*/
 
 	/*
 	@Deprecated
 	public Long enableUser(User user) throws OperationException {
 		log.info("Enabling accounts associated with user '" + user.getName()
 				+ "'");
 
 		user.setDisabled(false);
 		updateUser(user);
 
 		BulkTask bt = enableUserBulkTask(user);
 
 		// Persist the bulk task
 		tm.persistBulkTask(bt);
 		return bt.getBulkTaskId();
 	}
 	*/
 
 	/*
 	@Deprecated
 	public BulkTask userAccountsResetPasswordBulkTask(User user, String password)
 			throws OperationException, PasswordValidationException {
 		BulkTask bulkTask = BulkTask
 				.factory("Reset passwords for accounts associated to user named: "
 						+ user.getName());
 
 		for (Account currAccount : user.getAccounts()) {
 			try {
 				bulkTask.addTask(am.accountResetPasswordTask(currAccount,
 						password));
 			} catch (TaskCreationException tce) {
 				// If one task was failed to be created, then delete the
 				// bulkTask and throw an exception
 				throw new OperationException(
 						"Could not reset passwords for accounts associated with user '"
 								+ user.getName()
 								+ "' since one or more creation tasks operation were failed, message failure: "
 								+ tce.getMessage());
 			}
 		}
 
 		return bulkTask;
 	}
 	*/
 
 	/*
 	@Deprecated
 	public Long userAccountsResetPassword(User user, String password)
 			throws OperationException, PasswordValidationException {
 		log.info("Reseting accounts password associated with user: user '"
 				+ user.getName() + "'");
 
 		BulkTask bt = userAccountsResetPasswordBulkTask(user, password);
 
 		// Persist the bulk task
 		tm.persistBulkTask(bt);
 		return bt.getBulkTaskId();
 	}*/
 
 	
 	/*
 	@Deprecated
 	public BulkTask accountsResetPasswordForPasswordPolicyContainerBulkTask(
 			User user, PasswordPolicyContainer ppc, List<Account> accounts,
 			String password) throws OperationException,
 			PasswordValidationException {
 		BulkTask bt = BulkTask
 				.factory("Password Reset for selected accounts for User: '"
 						+ user.getName() + "', over Policy Container name: '"
 						+ ppc.getDisplayName() + "'");
 
 		// Make sure all set accounts are associated to the specified User
 		// entity / All account's target's poicy container is the same as the
 		// specified policy container
 		for (Account currAccount : accounts) {
 			if (currAccount.getUser().getUserId() != user.getUserId()) {
 				throw new OperationException("Account name: '"
 						+ currAccount.getName() + "', on target system name: '"
 						+ currAccount.getResource().getDisplayName()
 						+ "' is not associated with the specified USER name: '"
 						+ user.getName() + "'");
 			}
 
 			if (!currAccount.getResource().getPasswordPolicyContainer().equals(
 					ppc)) {
 				throw new OperationException(
 						"Account name: '"
 								+ currAccount.getName()
 								+ "', on target system name: '"
 								+ currAccount.getResource().getDisplayName()
 								+ "' is not associated with the specified Password Policy Container name: '"
 								+ ppc.getDisplayName() + "'");
 			}
 
 			try {
 				bt.addTask(am.accountResetPasswordTask(currAccount, password));
 			} catch (TaskCreationException tce) {
 				// If one task was failed to be created, then delete the
 				// bulkTask and throw an exception
 				throw new OperationException(
 						"Could not reset passwords, a task creation failure has occured while trying to factory a reset password for Account name: '"
 								+ currAccount.getName()
 								+ "', on target system name: '"
 								+ currAccount.getResource().getDisplayName()
 								+ "'  associated with user '"
 								+ user.getName()
 								+ "', message failure: " + tce.getMessage());
 			}
 		}
 
 		return bt;
 	}
 	*/
 
 	/*
 	@Deprecated
 	public Long accountsResetPasswordForPasswordPolicyContainer(User user,
 			PasswordPolicyContainer ppc, List<Account> accounts, String password)
 			throws OperationException, PasswordValidationException {
 		BulkTask bt = accountsResetPasswordForPasswordPolicyContainerBulkTask(
 				user, ppc, accounts, password);
 		getEntityManager().persist(bt);
 
 		getEntityManager().flush();
 
 		return bt.getBulkTaskId();
 	}*/
 
 
 	@Deprecated
 	public void modifyUserPositions(User user, Set<Position> positionsToRemove,
 			Set<Position> positionsToAdd) {
 		for (Position currUserPosition : user.getPositions()) {
 
 		}
 	}
 
 	@Deprecated
 	public String generateUserNameFromRequest(CreateUserRequest request)
 			throws UserNameGenerationException {
 		boolean isPluginScriptExist = false;
 		log.info("Generating User name for request ID: "
 				+ request.getRequestId());
 
 		if (request.getSuggestedUserName() != null) {
 			log
 					.info("Suggested user ID was set in request, accepting suggested user ID of: '"
 							+ request.getSuggestedUserName() + "'");
 			return request.getSuggestedUserName();
 		}
 
 		if (SysConf.getSysConf()
 				.getString("system.files.user_plugin_id_script") != null) {
 			isPluginScriptExist = true;
 		}
 
 		if (isPluginScriptExist) {
 			String scriptFileName = SysConf.getSysConf().getString(
 					"system.directory.user_workspace_dir")
 					+ "/"
 					+ SysConf.getSysConf().getString(
 							"system.directory.general_scripts")
 					+ "/"
 					+ SysConf.getSysConf().getString(
 							"system.files.user_plugin_id_script");
 
 			log.info("Constructing UserName from RequestID: "
 					+ request.getRequestId()
 					+ ", plugin file name to generate User ID is: "
 					+ scriptFileName);
 
 			try {
 				ScriptFactory sf = new ScriptFactory();
 				Object scriptObj = sf
 						.factoryScriptableObjectByResourceName(scriptFileName);
 				UserPluginIdTools pluginObj = (UserPluginIdTools) scriptObj;
 				System.out
 						.println("************: BEFORE PLUGIN request attrs: "
 								+ request.getAttributes().size());
 				//pluginObj.setRequest(request);
 
 				//String userName = pluginObj.generate();
 				System.out.println("************: AFTER PLUGIN request attrs: "
 						+ request.getAttributes().size());
 
 				/*JB!
 				if (userName != null) {
 					return userName;
 				} else {
 					if (request.getSuggestedUserName() != null) {
 						return request.getSuggestedUserName();
 					} else {
 						String errMsg = "Could not generate UserName for Request ID: '"
 								+ request.getRequestId()
 								+ "', script to generate user name was not specified and Request's suggested user was not set.!";
 						log.warn(errMsg);
 						throw new UserNameGenerationException(errMsg);
 					}
 				}
 				*/
 				
 				return null;
 			} catch (Exception e) {
 				String errMsg = "An exception was occured while trying to generate a user ID, failure message is: "
 						+ e.getMessage();
 				log.warn(errMsg);
 				throw new UserNameGenerationException(errMsg);
 			}
 		} else {
 			// No plugin script to generate user name was specified, expecting
 			// the name to be as a part of the request
 			if (request.getSuggestedUserName() != null) {
 				return request.getSuggestedUserName();
 			} else {
 				String errMsg = "Could not generate UserName for Request ID: '"
 						+ request.getRequestId()
 						+ "', script to generate user name was not specified and Request's suggested user was not set.!";
 				log.warn(errMsg);
 				throw new UserNameGenerationException(errMsg);
 			}
 
 		}
 	}
 
 	@Deprecated
 	public User createUserFromRequest(CreateUserRequest request)
 			throws OperationException {
 		log.info("Starting Add User from Request method for Request ID: "
 				+ request.getRequestId());
 
 		// We expect the request to be validated already by the RequestManager
 		// before the request was persisted.
 
 		try {
 			User newUser = new User();
 			newUser.setCreatedByRequest(true);
 			String userName = generateUserNameFromRequest(request);
 
 			log.info("Successfully generated user named: '" + userName
 					+ "' for Request ID: '" + request.getRequestId() + "'");
 			newUser.setName(userName);
 
 			// Before persisting the user, make sure that the there is no user
 			// exists already with the generated user name
 			if (isUserExit(userName)) {
 				throw new OperationException(
 						"Cannot create user from request, generated user name: '"
 								+ userName
 								+ "' alreayd exists in the users repository!");
 			}
 
 			// Add the attributes to the user
 			addIdentityAttributesToUser(request.getAttributes(), newUser);
 			// TODO: HANDLE THIS. WRONG. BAH. no static IAs should be added, or
 			// special threatment for USERNAME IA should be done!
 			// Add statically USERNAME IA to user
 			/*
 			 * try { IdentityAttribute ia =
 			 * identityAttributeManager.findIdentityAttributeByName("USERNAME");
 			 * UserIdentityAttribute userNameIA = new UserIdentityAttribute();
 			 * userNameIA.setIdentityAttribute(ia); userNameIA.setUser(newUser);
 			 * UserIdentityAttributeValueString uiavs = new
 			 * UserIdentityAttributeValueString(); uiavs.setValue(userName);
 			 * userNameIA.getValues().add(uiavs);
 			 * newUser.getUserIdentityAttributes().add(userNameIA); } catch
 			 * (NoResultFoundException nrfe) { nrfe.printStackTrace(); }
 			 */
 
 			log
 					.info("Successfully finished adding attributes to user, user currently has: '"
 							+ newUser.getUserIdentityAttributes().size()
 							+ "' Identity Attributes.");
 
 			// Persist the user
 			//JB! persistUserEntity(newUser);
 			getEntityManager().flush();
 			log.info("Successfully persisted User named: '" + newUser.getName()
 					+ "' into the repository");
 
 			return newUser;
 
 		} catch (UserNameGenerationException unge) {
 			throw new OperationException(
 					"Could not add user from Request, failure message is: "
 							+ unge.getMessage());
 		}
 	}
 
 	@Deprecated
 	public void addIdentityAttributesToUser(List<RequestAttribute> attrs,
 			User user) {
 		log.info("Adding Identity Attributes with size: '" + attrs.size()
 				+ "' to User name: '" + user.getName() + "'");
 		for (RequestAttribute currAttr : attrs) {
 			try {
 				// Make sure that the user does not have the IA already
 				if (!user.isUserAttributeExists(currAttr.getUniqueName())) {
 					log
 							.debug("User does not already has an attribute with name: '"
 									+ currAttr.getUniqueName()
 									+ "', adding one...");
 					// Find the corresponding IA
 					IdentityAttribute ia = identityAttributeManager
 							.findIdentityAttributeByUniqueName(currAttr
 									.getUniqueName());
 					UserIdentityAttribute uia = new UserIdentityAttribute();
 					uia.setIdentityAttribute(ia);
 					uia.setUser(user);
 					// uia.setValues(currAttr.getValues());
 					uia.setValuesByRequestValues(currAttr.getRequestAttributeValues());
 
 					user.getUserIdentityAttributes().add(uia);
 				} else {
 					log.info("User already has attribute with name: '"
 							+ currAttr.getUniqueName()
 							+ "', skipping attribute...");
 				}
 			} catch (NoResultFoundException nrfe) {
 				log
 						.warn("Skipping attriubte named : '"
 								+ currAttr.getUniqueName()
 								+ "' to user: '"
 								+ user.getName()
 								+ "', no IdentityAttribute was found for the specified name, detailed failure message: "
 								+ nrfe.getMessage());
 			} catch (UnsupportedAttributeTypeException uate) {
 				log
 						.warn("Skipping attriubte named : '"
 								+ currAttr.getUniqueName()
 								+ "' to user: '"
 								+ user.getName()
 								+ "', An Unsupported attribute Type Exception occured while trying to set its value, detailed failure message: "
 								+ uate.getMessage());
 			}
 		}
 
 		try {
 			for (UserIdentityAttribute currUIA : user
 					.getUserIdentityAttributes()) {
 				log.info("values #: '" + currUIA.getValues().size()
 						+ "' First value obj: "
 						+ currUIA.getFirstValue().getClass().getName());
 				log.info("VALUE OF ATTRIBUTE: '"
 						+ currUIA.getIdentityAttribute().getUniqueName()
 						+ "' as string is: '"
 						+ currUIA.getFirstValue().getAsString());
 			}
 		} catch (NoResultFoundException nrfe) {
 			// TODO Handle this exception.
 		}
 	}
 
 	@Deprecated
 	public Collection<PasswordPolicyContainer> loadAccountsInPasswordPolicyContainers(
 			Collection<PasswordPolicyContainer> passwordPolicyContainers,
 			User user) {
 		boolean isUserHasAccountOnCurrentIteratedTarget = false;
 
 		log
 				.info("Starting to iterate over the specified Password Policy Containers, loading accounts into containers for user name: '"
 						+ user.getName() + "'");
 		for (PasswordPolicyContainer currPPC : passwordPolicyContainers) {
 			log.info("Current iterated password policy container name is: '"
 					+ currPPC.getDisplayName() + "'");
 			// Per Container, Find the corresponding accounts of the specified
 			// user which associated with targets associated with the container
 			for (Resource currTargetInCurrContainer : currPPC.getResources()) {
 				isUserHasAccountOnCurrentIteratedTarget = isUserHasAccountOnResource(
 						user, currTargetInCurrContainer);
 				log.info("Is user has account on target system name: '"
 						+ currTargetInCurrContainer.getDisplayName() + "?: "
 						+ isUserHasAccountOnCurrentIteratedTarget);
 
 				if (isUserHasAccountOnCurrentIteratedTarget) {
 					Account foundAccount = am.findAccount(currTargetInCurrContainer, user);
 					currPPC.getLoadedAccounts().add(foundAccount);
 						
 					if (foundAccount == null) {
 						log.warn("Cannot add account in PolicyContainer since account was not found");
 						continue;
 					}
 				}
 			}
 		}
 
 		log
 				.info("END of Loadding Accounts into Password Policy Containers for user name: '"
 						+ user.getName() + "'");
 
 		return passwordPolicyContainers;
 	}
 
 	@Deprecated
 	public void associateUserToRole(String userName, String roleName,
 			boolean isInherited) throws OperationException {
 		try {
 			if (!isUserExit(userName)) {
 				throw new velo.exceptions.OperationException(
 						"Cannot associate user named \'"
 								+ userName
 								+ "\' to Role named \'"
 								+ roleName
 								+ "\' since the specified User does not exists in repository!");
 			}
 			if (!rolem.isRoleExit(roleName)) {
 				throw new velo.exceptions.OperationException(
 						"Cannot associate user named \'"
 								+ userName
 								+ "\' to Role named \'"
 								+ roleName
 								+ "\' since the specified Role does not exists in repository!");
 			}
 
 			velo.entity.User loadedUser = findUserByName(userName);
 			velo.entity.Role loadedRole = rolem.findRole(roleName);
 
 			// Make sure that the user does not have the specified role already
 			if (isRoleAssignedToUser(loadedUser, loadedRole)) {
 				throw new OperationException(
 						"Cannot associate user named \'"
 								+ userName
 								+ "\' to Role named \'"
 								+ roleName
 								+ "\' since the specified Role already associated to user!");
 			}
 
 			// Perform the association
 			UserRole userRole = new UserRole();
 			userRole.setRole(loadedRole);
 			userRole.setUser(loadedUser);
 			userRole.setInherited(isInherited);
 			loadedUser.getUserRoles().add(userRole);
 
 			// Should not get here due to validations
 		} catch (NoResultFoundException ex) {
 			throw new OperationException(ex.getMessage());
 		}
 	}
 
 	public void updateUserIdentityAttributes(
 			Collection<UserIdentityAttribute> userIdentityAttributes) {
 		for (UserIdentityAttribute uia : userIdentityAttributes) {
 			getEntityManager().merge(uia);
 		}
 	}
 
 	public void persistUserIdentityAttributes(
 			Collection<UserIdentityAttribute> userIdentityAttributes) {
 		for (UserIdentityAttribute uia : userIdentityAttributes) {
 			getEntityManager().merge(uia);
 		}
 	}
 
 	// Helper methods
 	private void userAuthFailure(User user, String ip) {
 		String failureMsg = "User authentication failure for user name '"
 				+ user.getName() + "' from IP '" + ip + "'";
 		log.trace(failureMsg);
 		// edmLogger.warning(failureMsg);
 		int max = SysConf.getSysConf()
 				.getInt("security.users.max-auth-failure");
 		log.trace("Max allowed failures before lock: '" + max + "'");
 		if (!user.isLocked()) {
 			log
 					.trace("User is not locked, determining if user has passed the maximum allowed authentication failures before lock");
 			if (user.getAuthFailureCounter() + 1 >= max) {
 				log.trace("User passed max auth failures!, locking user!");
 				user.setLocked(true);
 				updateUser(user);
 			} else {
 				log
 						.trace("User has not passed the maximum auth failures, incrementing user auth failures to number: '"
 								+ user.getAuthFailureCounter() + 1 + "'");
 			}
 
 			user.setAuthFailureCounter(user.getAuthFailureCounter() + 1);
 			updateUser(user);
 		}
 		// Already locked, increment failures
 		else {
 			log
 					.trace("User already locked!, incrementing user auth failures to number: '"
 							+ user.getAuthFailureCounter() + 1 + "'");
 			user.setAuthFailureCounter(user.getAuthFailureCounter() + 1);
 			updateUser(user);
 		}
 	}
 
 
 	@Deprecated
 	private User touchUserReferences(User user) {
 		System.out.println("----DEBUG---------");
 		user.getAccounts().size();
 		user.getCapabilityFolders().size();
 		user.getUserIdentityAttributes().size();
 		user.getUserRoles().size();
 
 		return user;
 	}
 	
 	
 	public EntityManager getEntityManager() {
 		boolean needFactory = false;
 		
 		if  ( (entityManager == null) ) {
 			needFactory = true;
 		} else {
 			//meaning its a Seam EM
 			if (!entityManager.getClass().getName().equals("org.jboss.ejb3.entity.TransactionScopedEntityManager")) {
 				//make sure its not closed, otherwise factory a new one as well
 				if(!entityManager.isOpen()) {
 					needFactory = true;
 				}
 			}
 		}
 		
 
 		if (needFactory) {
 			try {
 				entityManager = (EntityManager)(new InitialContext().lookup("java:/veloDataSourceEM"));
 			}catch(NamingException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 		
 		
 		return entityManager;
 	}
 	
 	
 }
