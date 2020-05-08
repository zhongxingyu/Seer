 package de.enwida.web.service.implementation;
 
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.sql.Date;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Service;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 import de.enwida.web.dao.interfaces.IGroupDao;
 import de.enwida.web.dao.interfaces.IRightDao;
 import de.enwida.web.dao.interfaces.IRoleDao;
 import de.enwida.web.dao.interfaces.IUserDao;
 import de.enwida.web.model.Group;
 import de.enwida.web.model.Right;
 import de.enwida.web.model.Role;
 import de.enwida.web.model.User;
 import de.enwida.web.service.interfaces.IUserService;
 import de.enwida.web.utils.Constants;
 import de.enwida.web.utils.EnwidaUtils;
 
 @Service("userService")
 @TransactionConfiguration(transactionManager = "jpaTransactionManager", defaultRollback = true)
 @Transactional(rollbackFor = Exception.class)
 public class UserServiceImpl implements IUserService {
     
     //This variable is used for tests
     static String lastActivationLink="";
 
 	/**
 	 * User Data Access Object
 	 */
     @Autowired
     private IUserDao userDao;
     
     /**
      * Group Data Access Object
      */
     @Autowired
     private IGroupDao groupDao;
     
     /**
      * Role Data Access Object
      */   
     @Autowired
     private IRoleDao roleDao;
     
     /**
      * Rights Data Access Object
      */
     @Autowired
     private IRightDao rightDao;
        
     @Autowired
     private MessageSource messageSource;
     
     /**
      * Mailing Service to send activation link or password
      */
     @Autowired
     private MailServiceImpl mailService;
     
     /**
      * Log4j static class
      */
 	private Logger logger = Logger.getLogger(getClass());
     
 	/**
 	 * Gets the User from UserID
 	 * 
 	 * @throws Exception
 	 */
     @Override
     public User fetchUser(Long id) {
         return userDao.fetchById(id);
     }
 
 	/**
 	 * Gets the Group from Group Name
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public Group findGroup(Group group) {
 		return groupDao.fetchByName(group.getGroupName());
 	}
 
 	/**
 	 * Gets the role from Role Name
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public Role findRole(Role role) {
 		return roleDao.fetchByName(role.getRoleName());
 	}
 
 	/**
 	 * Gets the role from Role Name
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public Right findRight(Right right) {
 		return rightDao.fetchById(right.getRightID());
 	}
 
     /**
      * Gets all the user from database
      * @throws Exception 
      */
     @Override
     public List<User> fetchAllUsers() throws Exception {
         return userDao.fetchAll();
     }
     
     /**
      * Saves user into Database
      * @throws Exception 
      */
     @Override
     public boolean saveUser(User user, String activationHost, Locale locale,boolean sendEmail) throws Exception 
     {
     	// FIXME: what is the return value?
         Date date = new Date(Calendar.getInstance().getTimeInMillis());
         user.setJoiningDate(date);
         user.setEnabled(false);
         
         // Generating activation Id for User
         EnwidaUtils activationIdGenerator = new EnwidaUtils();
         user.setActivationKey(activationIdGenerator.getActivationId());
         
         // Password Encryption
         user.setPassword(EnwidaUtils.md5(user.getPassword()));
         
         // Saving user in the user table
         long userId;
         try {
             //check if we dont have this user
             if(userDao.fetchByName(user.getUsername())==null)
             {
                 userDao.create(user);
                 userId=user.getUserId();
             }else{
                 throw new Exception("This user is already in database");
             }
         } catch (Exception e) {
             logger.info(e.getMessage());
             return false;
         }
         if(userId != -1)
         {        
         	// Getting domain name from email
         	String domain = this.getDomainFromEmail(user.getEmail());
         	
         	// Fetching the same group and assigning that group to user
             Group group = this.fetchGroupByDomainName(domain);            
             if(group != null && group.isAutoPass())
             {
                 Group newGroup = groupDao.fetchById(group.getGroupID());
                 this.assignGroupToUser(userId, newGroup.getGroupID());
             }
         
             // saving in default group (Anonymous)
 			Group anonymousGroup = groupDao.fetchByName(Constants.ANONYMOUS_GROUP);
             if(anonymousGroup == null)
             {
                 anonymousGroup = new Group();
 				anonymousGroup.setGroupName(Constants.ANONYMOUS_GROUP);
                 anonymousGroup.setAutoPass(true);                    
             }
             anonymousGroup = groupDao.addGroup(anonymousGroup);
             this.assignGroupToUser(userId, anonymousGroup.getGroupID());
             
             //creating individual group to see uploaded data for the user
             Group userGroup = new Group(Constants.USER_UPLOAD_PREFIX+user.getUserName()); 
             userGroup=groupDao.addGroup(userGroup);
             //assign user to this group
             this.assignGroupToUser(userId, userGroup.getGroupID());
             
             //saving individual role for the group
             Role userRole = new Role(Constants.USER_UPLOAD_PREFIX+user.getUserName()); 
             userRole=roleDao.addRole(userRole); 
             //assign group to this role
             this.assignRoleToGroup(userRole.getRoleID(),userGroup.getGroupID());
             
             if(sendEmail){          
                 sendUserActivationEmail(user, locale);
             }
             return true;
         }
         else
         {
             return false;
        }        }
 
 	/**
 	 * Gets user Password from the mail
 	 */
     @Override
     public String getPassword(String email)throws Exception {
         return userDao.fetchByName(email).getPassword();
     }
 
     /**
      * Gets all the groups
      */
     @Override
     public List<Group> fetchAllGroups() {
         return groupDao.fetchAll();
     }
 
     /**
 	 * Adds new group
 	 * 
 	 * @throws Exception
 	 */
     @Override
 	public Group saveGroup(Group newGroup) throws Exception {
 		return groupDao.addGroup(newGroup);
     }
 
     /**
      * Adds new role to the DB
      */
     @Override
     public void saveRole(Role role) throws Exception  {
         roleDao.addRole(role);
     }
     
     @Override
     public void saveRight(Right right) throws Exception {
     	rightDao.addRight(right);
     }
     
     /**
      * Gets all Roles
      */
     @Override
     public List<Role> fetchAllRoles()throws Exception  {
         return roleDao.fetchAll();
     }
     
     @Override
     public List<Right> fetchAllRights() throws Exception {
     	return rightDao.fetchAll();
     }
     
     /**
      * Updates the user
      */
     @Override
     public void updateUser(User user) throws Exception {
 		userDao.update(user, true);
     }
     /**
      * Gets the user based on userName
      */
     @Override
     public User fetchUser(String userName)  {
         return userDao.fetchByName(userName);
     }
     /**
      * Resets user Password and send an email link
      */
     @Override
     public void resetPassword(long userID,Locale locale)throws Exception  {
         SecureRandom random = new SecureRandom();
         String newPassword=new BigInteger(30, random).toString(32);
         User user=userDao.fetchById(userID);
         try {
             mailService.SendEmail(user.getEmail(),messageSource.getMessage("de.enwida.userManagement.error.newPassword", null, locale),messageSource.getMessage("de.enwida.userManagement.error.newPassword", null, locale)+":"+newPassword);
             user.setPassword(newPassword);
             userDao.update(user);
         } catch (Exception e) {
             throw new Exception("Invalid Email.Please contact info@enwida.de");
         }       
     }
     /**
      * Deletes the user
      */
     @Override
     public void deleteUser(User user) throws Exception {
         userDao.deleteById(user.getUserId());
     }
     
     @Override
     public void deleteUser(long userId) throws Exception {
     	userDao.deleteById(userId);
     }
     
 	/**
 	 * Caution: user and group parameters should be persisted and in clean state!
 	 * Dirty attributes might be applied (i.e. committed to database, eventually).
 	 * @return the updated and managed group object
 	 * @throws Exception 
 	 */
 	@Override
 	public Group assignGroupToUser(User user, Group group) throws Exception {
 		if (user.getUserId() == null) {
 			throw new IllegalArgumentException("user object is not persisted");
 		}
 		if (group.getGroupID() == null) {
 			throw new IllegalArgumentException("group object is not persisted");
 		}
 
 		// Temporarily remove assigned users
 		// This is necessary to avoid having stale user objects in the group's object tree
 		final Set<User> assignedUsers = group.getAssignedUsers();
 		group.setAssignedUsers(null);
 		
 		// Modify user's set of groups
  		final Set<Group> groups = new HashSet<Group>(user.getGroups());
 		groups.add(group);
 		user.setGroups(groups);
 		userDao.update(user, true); // with flush
 		
 		// Reassign users
 		group.setAssignedUsers(assignedUsers);
 
 		// Refresh the group in order to reflect the changes
 		final Group result = fetchGroupById(group.getGroupID());
 		groupDao.refresh(result);
 		return result;
 	}	
 
 	@Override
 	public void assignGroupToUser(long userId, Long groupID) throws Exception {
 		User user = userDao.fetchById(userId);
 		Group group = groupDao.fetchById(groupID);
 		assignGroupToUser(user, group);
 	}
 
 	/**
 	 * Caution: user and group parameters should be persisted and in clean state!
 	 * Dirty attributes might be applied (i.e. committed to database, eventually).
 	 * @return the updated and managed group object
 	 * @throws Exception 
 	 */
     @Override
     public Group revokeUserFromGroup(User user, Group group) throws Exception {
 		if (user.getUserId() == null) {
 			throw new IllegalArgumentException("user object is not persisted");
 		}
 		if (group.getGroupID() == null) {
 			throw new IllegalArgumentException("group object is not persisted");
 		}
 		// Modify user's set of groups
  		final Set<Group> groups = new HashSet<Group>(user.getGroups());
 		groups.remove(group);
 		user.setGroups(groups);
 		userDao.update(user, true); // with flush
 
 		// Refresh the group in order to reflect the changes
 		final Group result = fetchGroupById(group.getGroupID());
 		groupDao.refresh(result);
 		return result;
 	}
 
     @Override
     public void assignRoleToGroup(long roleID, long groupID) throws Exception {
     	final Group group = groupDao.fetchById(groupID);
     	final Role role = roleDao.fetchById(roleID);
     	assignRoleToGroup(role, group);
     }
 
 	/**
 	 * Caution: group and role parameters should be persisted and in clean state!
 	 * Dirty attributes might be applied (i.e. committed to database, eventually).
 	 * @return the updated and managed role object
 	 * @throws Exception 
 	 */
 	@Override
 	@Transactional
 	public Role assignRoleToGroup(Role role, Group group) throws Exception {
 		if (group.getGroupID() == null) {
 			throw new IllegalArgumentException("group object is not persisted");
 		}
 		if (role.getRoleID() == null) {
 			throw new IllegalArgumentException("role object is not persisted");
 		}
 		// Temporarily remove assigned groups
 		// This is necessary to avoid having stale group objects in the role's object tree
 		final Set<Group> assignedGroups = role.getAssignedGroups();
 		group.setAssignedUsers(null);
 		
 		// Modify group's set of roles
  		final Set<Role> roles = new HashSet<Role>(group.getAssignedRoles());
  		roles.add(role);
 		group.setAssignedRoles(roles);
 		groupDao.update(group, true); // with flush
 		
 		// Reassign groups
 		role.setAssignedGroups(assignedGroups);
 
 		// Refresh the role in order to reflect the changes
 		final Role result = fetchRoleById(role.getRoleID());
 		roleDao.refresh(result);
 		return result;
 	}
 
     @Override
     public void revokeRoleFromGroup(long roleID, long groupID) throws Exception{
     	final Group group = groupDao.fetchById(groupID);
     	final Role role = roleDao.fetchById(roleID);
     	revokeRoleFromGroup(role, group);
     }
 
 	/**
 	 * Caution: group and role parameters should be persisted and in clean state!
 	 * Dirty attributes might be applied (i.e. committed to database, eventually).
 	 * @return the updated and managed role object
 	 * @throws Exception 
 	 */
 	@Override
 	public Role revokeRoleFromGroup(Role role, Group group) throws Exception {
 		if (group.getGroupID() == null) {
 			throw new IllegalArgumentException("group object is not persisted");
 		}
 		if (role.getRoleID() == null) {
 			throw new IllegalArgumentException("role object is not persisted");
 		}
 		// Modify group's set of roles
  		final Set<Role> roles = new HashSet<Role>(group.getAssignedRoles());
  		roles.remove(role);
 		group.setAssignedRoles(roles);
 		groupDao.update(group, true); // with flush
 
 		// Refresh the role in order to reflect the changes
 		final Role result = fetchRoleById(role.getRoleID());
 		roleDao.refresh(result);
 		return result;
 	}
     
     @Override
     public void revokeUserFromGroup(long userID, long groupID) {
         User user=userDao.fetchById(userID);
         Group group=groupDao.fetchById(groupID);
         if (group.getAssignedUsers().contains(user)){
             group.getAssignedUsers().remove(user);
         }
         if(group!=null  || user!=null)
 			try {
 				groupDao.save(group);
 			} catch (Exception e) {
 				logger.error("Unable to save group", e);
 			}
     }
 
 
     /**
      * Enables or Disables the user
      */
     @Override
     public void enableDisableUser(int userID, boolean enabled)throws Exception  {
         userDao.enableDisableUser(userID,enabled);
     }
     /**
      * Removes the group
      */
     @Override
     public void deleteGroup(long groupID) throws Exception {
         groupDao.deleteById(groupID);
     }
     
     @Override
     public void deleteRole(long roleID) throws Exception {
     	roleDao.deleteById(roleID);
     }
     
     @Override
     public void deleteRight(long rightID) throws Exception {
     	rightDao.deleteById(rightID);
     }
     
     /**
      * Checks usernameAvailability
      */
     @Override
     public boolean userNameAvailability(String username) throws Exception {
         return userDao.usernameAvailablility(username);
     }
     /**
      * Enables or disables the aspect based on rightID
      */
     @Override
     public void enableDisableAspect(int rightID, boolean enabled)throws Exception  {
         rightDao.enableDisableAspect(rightID,enabled);
     }
     /**
      * Activates the user
      */
     @Override
     public boolean activateUser(String username, String activationCode) throws Exception 
     {
         if(userDao.checkUserActivationId(username, activationCode))
         {
             userDao.activateUser(username);
             return true;
         }
         return false;
     }
     
     @Override
     public Long getNextSequence(String schema, String sequenceName) {
         Long value = null;
         try {
             value = userDao.getNextSequence(schema, sequenceName);
         } catch (Exception e) {
             logger.error("Do nothing");
         }
         return value;
     }    
     
     /**
      * Gets the current User
      */
     @Override
     public User getCurrentUser() throws Exception  {
         String userName = SecurityContextHolder.getContext().getAuthentication().getName();
         User user=this.fetchUser(userName);
         //If user is not found return anonymous user;
         if (user==null){
         	user = fetchUser(Constants.ANONYMOUS_USER);
         	
         	if (user == null) {
 				user = new User("anon@enwida.de", Constants.ANONYMOUS_USER, "secret", "Anonymous", "User", true);
 				user.setCompanyName("enwida.de");
 				saveUser(user,false);
 				
 				final Group anonymousGroup = fetchGroup(Constants.ANONYMOUS_GROUP);
 				assignGroupToUser(user, anonymousGroup);
         	}
         }
         return user;
     }
 
     /**
      * Saves the user
      * @throws Exception 
      */
     @Override
     public boolean saveUser(User user,boolean sendEmail) throws Exception {
         return saveUser(user,null, null,sendEmail);
     }
     
     @Override
     public Group fetchGroup(String groupName) throws Exception {
     	return groupDao.fetchByName(groupName);
     }
     
     @Override
     public Role fetchRole(String roleName) {
     	return roleDao.fetchByName(roleName);
     }
     
     @Override
     public Right fetchRight(Long rightId) {
     	return rightDao.fetchById(rightId);
     }
     
     @Override
     public Group fetchGroupByCompanyName(final String companyName)
     {
         for (Group group : groupDao.fetchAll()) {
             for (User user : group.getAssignedUsers()) {
                 if(user.getCompanyName().equals(companyName))
                     return group;
             }
         }
         return null;
     }
     
     @Override
     public User syncUser(User user) throws Exception {
     	user = fetchUser(user.getUsername());
     	userDao.refresh(user);
     	return user;
     }
     
 	@Override
 	public boolean emailAvailability(String email) throws Exception {
         for (User user : userDao.fetchAll()) {
             if(email.equalsIgnoreCase(user.getEmail())){
                 return true;
             }
         }
 		return false;		
 	}
 	
 	@Override
     public Group fetchGroupById(long groupId) {
         return groupDao.fetchById(groupId);
     }
 
     @Override
     public Role fetchRoleById(long roleId) {
         return roleDao.fetchById(roleId);
     }
 	
 	private void sendUserActivationEmail(User user, Locale locale) throws Exception {
 	    try{
 		String activationLink = Constants.ACTIVATION_URL+"username=" + user.getUserName() + "&actId=" + user.getActivationKey();
 		String emailText = messageSource.getMessage("de.enwida.activation.email.message", null, locale) + 
 				activationLink +" \n"+ messageSource.getMessage("de.enwida.activation.email.signature", null, locale);	
 
 		mailService.SendEmail(user.getEmail(), messageSource.getMessage("de.enwida.activation.email.subject", null, locale), emailText );
 	    }catch(Exception ex){
 	        logger.error(ex);
 	        throw new Exception("Mailing Error occured");
 	    }
 	}
 
     @Override
     public void enableDisableAutoPass(Long groupID, boolean enabled) throws Exception {
         Group group=groupDao.fetchById(groupID);
         group.setAutoPass(enabled);
         groupDao.save(group);
     }
 
 	@Override
 	public Long getNextSequence(String schema, String sequenceName,
 			boolean reset) {
 		Long value = null;
 		try {
 			value = userDao.getNextSequence(schema, sequenceName, reset);
 		} catch (Exception e) {
 			logger.error("Do nothing");
 		}
 		return value;
 	}
 
 	public String getLastActivationLink() {
         return lastActivationLink;
     }
     
     private String getDomainFromEmail(String email){
     	String company = email.substring(email.indexOf('@') + 1, email.length());
     	return company;
     }
     
     @Override
     public Group fetchGroupByDomainName(String domainName) {
         for (Group group : groupDao.fetchAll()) {
             if (group.getDomainAutoPass() != null
                     && group.getDomainAutoPass().equalsIgnoreCase(domainName))
                 return group;
         }
         return null;
     }
 	
 	@Override
     public void updateDomainAutoPass(Long groupID, String domainAutoPass) throws Exception {
         Group group=groupDao.fetchById(groupID);
         group.setDomainAutoPass(domainAutoPass);
         groupDao.save(group);
     }
 
     @Override
     @Transactional
     public Role enableDisableAspectForRole(Right right, Role role,boolean enabled) throws Exception {
         
         if (role.getRoleID() == null) {
             throw new IllegalArgumentException("role object is not persisted");
         }
         if (right.getRightID() == null) {
             throw new IllegalArgumentException("right object is not persisted");
         }
         
         final Set<Right> rights=new HashSet<Right>(role.getRights());
         if(enabled)
             rights.add(right);
         else
             rights.remove(right);
         role.setRights(rights);
         roleDao.update(role, true);
        
         final Right result =rightDao.fetchById(right.getRightID());
         rightDao.refresh(result);
         
         return  role;
     }
 
 }
