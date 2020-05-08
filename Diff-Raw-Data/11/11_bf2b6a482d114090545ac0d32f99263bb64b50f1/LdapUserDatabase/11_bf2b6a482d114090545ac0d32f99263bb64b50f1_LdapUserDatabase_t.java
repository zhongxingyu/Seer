 /*
 *  Adito
 *
 *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
 
 package com.adito.ldap;
 
 import com.adito.boot.ContextHolder;
 import com.adito.core.CoreEvent;
 import com.adito.core.CoreListener;
 import com.adito.core.CoreServlet;
 import com.adito.properties.Property;
 import com.adito.properties.PropertyChangeEvent;
 import com.adito.properties.impl.realms.RealmKey;
 import com.adito.realms.Realm;
 import com.adito.security.DefaultUserDatabase;
 import com.adito.security.InvalidLoginCredentialsException;
 import com.adito.security.Role;
 import com.adito.security.User;
 import com.adito.security.UserDatabaseException;
 import com.adito.security.UserNotFoundException;
 import com.adito.util.ThreadRunner;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.ldap.core.AttributesMapper;
 import org.springframework.ldap.core.ContextMapper;
 import org.springframework.ldap.core.DirContextAdapter;
 import org.springframework.ldap.core.DirContextOperations;
 import org.springframework.ldap.core.DistinguishedName;
 import org.springframework.ldap.core.LdapTemplate;
 import org.springframework.ldap.core.support.LdapContextSource;
 import org.springframework.ldap.filter.AndFilter;
 import org.springframework.ldap.filter.EqualsFilter;
 import org.springframework.ldap.filter.LikeFilter;
 import org.springframework.ldap.support.LdapUtils;
 
 import javax.management.relation.RoleNotFoundException;
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.BasicAttribute;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.ModificationItem;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * <p/>
  * Ldap implementation of
  * {@link com.adito.security.UserDatabase}.
  */
 public class LdapUserDatabase extends DefaultUserDatabase implements CoreListener {
 
     public static final String COMMON_NAME_ATTRIBUTE = "cn";
     public static final String SURNAME_ATTRIBUTE = "sn";
     public static final String MAIL_ATTRIBUTE = "mail";
     public static final String MEMBER_ATTRIBUTE = "uniqueMember";
     public static final String PASSWORD_ATTRIBUTE = "userPassword";
     public static final String UID_ATTRIBUTE = "uid";
     public static final String MODIFY_TIMESTAMP_ATTRIBUTE = "modifyTimestamp";
     public static final String OBJECT_CLASS_ATTRIBUTE = "objectclass";
 
     public static final String USERS_CLASS = "inetOrgPerson";
     public static final String GROUPS_CLASS = "groupofuniquenames";
     public static final String TOP_CLASS = "top";
 
     public static final String WILDCARD_SEARCH = "*";
 
     private static final Log logger = LogFactory.getLog(LdapUserDatabase.class);
 
     private static final String LDAP_PROTOCOL = "ldap://";
 
     private UserContainer userContainer = UserContainer.EMPTY_CACHE;
     private GroupContainer groupContainer = GroupContainer.EMPTY_CACHE;
     private ThreadRunner threadRunner;
 
     private LdapContextSource ldapContextSource;
     private String controllerHost;
     private String serviceAccountName;
     private String serviceAccountPassword;
     private String baseDn;
     private String rdnUsers;
     private String rdnGroups;
     private int userCacheSize;
     private int groupCacheSize;
     private boolean inMemoryCache;
     private int timeToLive;
     private int timeOut;
     private boolean usernamesAreCaseSensitive;
     private boolean followReferrals;
 
 
     /**
      * Constructeur
      */
     public LdapUserDatabase() {
         this(true, true);
     }
 
     /**
      * Constructeur
      *
      * @param supportsAccountCreation if the ldap database can be modify, true, else, false
      * @param supportsPasswordChange  if the password can be change, true, else, false
      */
     public LdapUserDatabase(boolean supportsAccountCreation, boolean supportsPasswordChange) {
         super("Ldap", supportsAccountCreation, supportsPasswordChange);
 
     }
 
     /**
      * @return the container with users
      */
     protected UserContainer getUserContainer() {
         return userContainer;
     }
 
     /**
      * @return the container with groups
      */
     protected GroupContainer getGroupContainer() {
         return groupContainer;
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.security.UserDatabase#logon(java.lang.String,
      *      java.lang.String)
      */
     public User logon(String username, String password) throws UserDatabaseException, InvalidLoginCredentialsException {
         LdapUser user;
         try {
             user = getAccount(username);
         } catch (Exception e) {
             logger.error("Failed to logon", e);
             throw new UserDatabaseException("Failed to logon", e);
         }
         assertValidCredentials(user, password);
         return user;
     }
 
     /**
      * Verify the validity of pair user-password
      *
      * @param user     user that we want verify his password
      * @param password password of the user
      * @throws InvalidLoginCredentialsException
      *          if the password is not valid for this user
      */
     private void assertValidCredentials(LdapUser user, String password) throws InvalidLoginCredentialsException {
         if (!areCredentialsValid(user, password)) {
             throw new InvalidLoginCredentialsException("Invalid username or password.");
         }
     }
 
     /**
      * Return if pair user - password is valid or not
      *
      * @param user     user that we want verify his password
      * @param password password of the user
      * @return true if password is valid for this user, false in other case
      */
     protected final boolean areCredentialsValid(LdapUser user, String password) {
         DirContext ctx = null;
         try {
             ctx = ldapContextSource.getContext(user.getDn(), password);
             return true;
         } catch (Exception e) {
             // Context creation failed - authentication did not succeed
             logger.error("Login failed", e);
             return false;
         } finally {
 
             LdapUtils.closeContext(ctx);
         }
     }
 
     /**
      * @return all users in the ldap directory
      */
     @SuppressWarnings("unchecked")
     public Iterable<User> allUsers() {
         Iterator<? extends User> retrievePrincipals = userContainer.retrievePrincipals();
         return (Iterable<User>) toIterable(retrievePrincipals);
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.security.UserDatabase#getAccount(java.lang.String)
      */
 
     public LdapUser getAccount(String username) throws Exception {
 
         if (logger.isDebugEnabled()) {
             logger.debug("Getting account " + username);
         }
         if (!userContainer.containsPrincipal(username)) {
             loadUsers(username, false);
         }
         if (!userContainer.containsPrincipal(username)) {
             throw new UserNotFoundException(username + " is not a valid user!");
         }
 
         return userContainer.retrievePrincipal(username);
 
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.security.UserDatabase#getRole(java.lang.String)
      */
     public Role getRole(String groupName) throws UserDatabaseException, RoleNotFoundException {
         if (logger.isDebugEnabled()) {
             logger.debug("Getting group " + groupName);
         }
 
         if (!groupContainer.containsPrincipal(groupName)) {
             loadRoles(groupName, false);
 
         }
         if (!groupContainer.containsPrincipal(groupName)) {
             throw new RoleNotFoundException(groupName + " is not a valid group!");
         }
         return groupContainer.retrievePrincipal(groupName);
     }
 
     /**
      * @return all the roles(groups) in ldap directory
      */
     @SuppressWarnings("unchecked")
     public Iterable<Role> allRoles() {
         Iterator<? extends Role> retrievePrincipals = groupContainer.retrievePrincipals();
         return (Iterable<Role>) toIterable(retrievePrincipals);
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.security.DefaultUserDatabase#createRole(java.lang.String)
      */
     public Role createRole(String rolename) throws Exception {
         if (!supportsAccountCreation()) {
             throw new UnsupportedOperationException("User database is read-only");
         }
         if (logger.isInfoEnabled()) {
             logger.info("create role " + rolename);
         }
         LdapTemplate ldapTemplate = new LdapTemplate();
         ldapTemplate.setContextSource(ldapContextSource);
 
         String rdn = COMMON_NAME_ATTRIBUTE + "=" + rolename + "," + rdnGroups;
         DirContextAdapter context = new DirContextAdapter(rdn);
         context.setUpdateMode(false);
         context.setAttributeValues(OBJECT_CLASS_ATTRIBUTE, new String[]{TOP_CLASS, GROUPS_CLASS});
         ldapTemplate.bind(context);
 
 
         String originalDn = rdn + "," + baseDn;
         LdapGroup g = new LdapGroup(rolename, originalDn, getRealm());
         groupContainer.storeGroup(g);
         return g;
 
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.security.DefaultUserDatabase#deleteRole(java.lang.String)
      */
     public void deleteRole(String rolename) throws Exception {
         if (!supportsAccountCreation()) {
             throw new UnsupportedOperationException("User database is read-only");
         }
 
         LdapTemplate ldapTemplate = new LdapTemplate();
         ldapTemplate.setContextSource(ldapContextSource);
 
         String rdn = COMMON_NAME_ATTRIBUTE + "=" + rolename + "," + rdnGroups;
 
         //take the name of user'member of this group
         NamingEnumeration e = (NamingEnumeration) ldapTemplate.lookup(rdn, new AttributesMapper() {
             public Object mapFromAttributes(Attributes attrs)
                     throws NamingException {
 
                 return attrs.get(MEMBER_ATTRIBUTE).getAll();
 
 
             }
         });
 
         //delete the group in database
         ldapTemplate.unbind(rdn);
         groupContainer.removeGroup((LdapGroup) getRole(rolename));
 
         // delete the group in Ldapuser
         for (; e.hasMore();) {
             LdapUser u = getAccountFromDN(e.next().toString());
             u.setRoles(getGroupsForUser(u.getPrincipalName()));
             userContainer.storePrincipal(u);
         }
 
 
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.security.UserDatabase#checkPassword(java.lang.String,java.lang.String)
      */
     public boolean checkPassword(String username, String password) throws UserDatabaseException, InvalidLoginCredentialsException {
         try {
             LdapUser user = getAccount(username);
             return areCredentialsValid(user, password);
         } catch (UserNotFoundException e) {
             throw new UserDatabaseException("Failed to check password", e);
         } catch (Exception e) {
             throw new UserDatabaseException("Failed to check password", e);
         }
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.security.DefaultUserDatabase#changePassword(java.lang.String, java.lang.String, java.lang.String, boolean)
      */
     public void changePassword(String username, String oldPassword, String password, boolean forcePasswordChangeAtLogon)
             throws UserDatabaseException, InvalidLoginCredentialsException {
         if (!supportsPasswordChange()) {
             throw new InvalidLoginCredentialsException("Database doesn't support password change.");
         }
 
         DirContext ctx = null;
         try {
             if (forcePasswordChangeAtLogon)
                 getAccount(username).setLastPasswordChange(null);
             else
                 getAccount(username).setLastPasswordChange(new Date());
 
             ctx = ldapContextSource.getContext(getAccount(username).getDn(), oldPassword);
             LdapTemplate ldapTemplate = new LdapTemplate();
             ldapTemplate.setContextSource(ldapContextSource);
             DirContextOperations context = ldapTemplate.lookupContext(UID_ATTRIBUTE + "=" + username + "," + rdnUsers);
             context.setAttributeValue(PASSWORD_ATTRIBUTE, password);
             ldapTemplate.modifyAttributes(context);
 
         } catch (Exception e) {
             // Context creation failed - authentication did not succeed
             logger.error("Login failed", e);
 
         } finally {
             LdapUtils.closeContext(ctx);
         }
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.security.DefaultUserDatabase#setPassword(java.lang.String, java.lang.String, boolean, com.adito.security.User, java.lang.String)
      */
     public void setPassword(String username, String password, boolean forcePasswordChangeAtLogon, User adminUser,
                             String adminPassword) throws UserDatabaseException, InvalidLoginCredentialsException {
         if (!supportsPasswordChange()) {
             throw new InvalidLoginCredentialsException("Database doesn't support password change.");
         }
 
         LdapUser user;
 
         try {
             user = getAccount(username);
         } catch (Exception e) {
             throw new UserDatabaseException(e.toString());
         }
 
         if (forcePasswordChangeAtLogon)
             user.setLastPasswordChange(null);
         else
             user.setLastPasswordChange(new Date());
 
         LdapTemplate ldapTemplate = new LdapTemplate();
         ldapTemplate.setContextSource(ldapContextSource);
         Attribute attr = new BasicAttribute(PASSWORD_ATTRIBUTE, password);
         ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);
         ldapTemplate.modifyAttributes(UID_ATTRIBUTE + "=" + username + "," + rdnUsers, new ModificationItem[]{item});
 
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.security.DefaultUserDatabase#createAccount(java.lang.String, java.lang.String, java.lang.String, java.lang.String, com.adito.security.Role[])
      */
     public User createAccount(String username, String password, String email, String fullname, Role[] roles) throws Exception {
         if (!supportsAccountCreation()) {
             throw new UnsupportedOperationException("User database is read-only");
         }
         LdapTemplate ldapTemplate = new LdapTemplate();
         ldapTemplate.setContextSource(ldapContextSource);
         DirContextAdapter context = new DirContextAdapter(UID_ATTRIBUTE + "=" + username + "," + rdnUsers);
         context.setUpdateMode(false);
         context.setAttributeValues(OBJECT_CLASS_ATTRIBUTE, new String[]{TOP_CLASS, USERS_CLASS});
         context.setAttributeValue(COMMON_NAME_ATTRIBUTE, fullname);
         context.setAttributeValue(SURNAME_ATTRIBUTE, fullname);
         context.setAttributeValue(PASSWORD_ATTRIBUTE, password);
         context.setAttributeValue(MAIL_ATTRIBUTE, email);
         context.setAttributeValue(UID_ATTRIBUTE, username);
         ldapTemplate.bind(context);
 
         String originalDn = UID_ATTRIBUTE + "=" + username + "," + rdnUsers + "," + baseDn;
 
         for (Role role : roles) {
             DirContextOperations context2 = ldapTemplate.lookupContext(COMMON_NAME_ATTRIBUTE + "=" + role.getPrincipalName() + "," + rdnGroups);
             context2.addAttributeValue(MEMBER_ATTRIBUTE, originalDn);
             ldapTemplate.modifyAttributes(context2);
         }
 
 
         LdapUser u = new LdapUser(username, originalDn, email, fullname, new Date(), getRealm());
         u.setRoles(roles);
         userContainer.storePrincipal(u);
         return u;
 
 
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.security.UserDatabase#updateAccount(com.adito.security.User, java.lang.String, java.lang.String, com.adito.security.Role[])
      */
     public void updateAccount(User user, String email, String fullname,
                               Role[] roles) throws Exception {
         if (!supportsAccountCreation()) {
             throw new UnsupportedOperationException("User database is read-only");
         }
 
         if (logger.isInfoEnabled()) {
             logger.info("update Account " + user.getPrincipalName());
         }
 
         LdapTemplate ldapTemplate = new LdapTemplate();
         ldapTemplate.setContextSource(ldapContextSource);
 
         String rdn = UID_ATTRIBUTE + "=" + user.getPrincipalName() + "," + rdnUsers;
 
         //update email and fullname
         DirContextOperations context = ldapTemplate.lookupContext(rdn);
         context.setAttributeValue(MAIL_ATTRIBUTE, email);
         context.setAttributeValue(COMMON_NAME_ATTRIBUTE, fullname);
         ldapTemplate.modifyAttributes(context);
 
         //update roles
 
         Role[] rolesOld = user.getRoles();
 
         String originalDn = rdn + "," + baseDn;
 
         /* delete old roles*/
         for (Role aRolesOld : rolesOld) {
             logger.info("delete in: " + COMMON_NAME_ATTRIBUTE + "=" + aRolesOld.getPrincipalName() + "," + rdnGroups);
             DirContextOperations context2 = ldapTemplate.lookupContext(COMMON_NAME_ATTRIBUTE + "=" + aRolesOld.getPrincipalName() + "," + rdnGroups);
             context2.removeAttributeValue(MEMBER_ATTRIBUTE, originalDn);
             ldapTemplate.modifyAttributes(context2);
             logger.info("attribut remove: " + originalDn);
 
         }
 
         /* add new roles */
         int i = 0;
         while (i < roles.length) {
             logger.info("add: " + COMMON_NAME_ATTRIBUTE + "=" + roles[i].getPrincipalName() + "," + rdnGroups);
             DirContextOperations context3 = ldapTemplate.lookupContext(COMMON_NAME_ATTRIBUTE + "=" + roles[i].getPrincipalName() + "," + rdnGroups);
             context3.addAttributeValue(MEMBER_ATTRIBUTE, originalDn);
             ldapTemplate.modifyAttributes(context3);
             i++;
         }
 
         loadUsers(user.getPrincipalName(), false);
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.security.DefaultUserDatabase#deleteAccount(com.adito.security.User)
      */
     protected void performDeleteAccount(User user) throws Exception, UserNotFoundException {
 
         LdapTemplate ldapTemplate = new LdapTemplate();
         ldapTemplate.setContextSource(ldapContextSource);
 
         //delete the name of user in groups
         Role[] roles = user.getRoles();
 
         String rdn = UID_ATTRIBUTE + "=" + user.getPrincipalName() + "," + rdnUsers;
 
         for (Role role : roles) {
             DirContextOperations context = ldapTemplate.lookupContext(COMMON_NAME_ATTRIBUTE + "=" + role.getPrincipalName() + "," + rdnGroups);
             context.removeAttributeValue(MEMBER_ATTRIBUTE, rdn + "," + baseDn);
             ldapTemplate.modifyAttributes(context);
         }
 
         //delete the user in database
         ldapTemplate.unbind(rdn);
 
         userContainer.removePrincipal(user.getPrincipalName());
 
     }
 
     /**
      * Load users in container
      *
      * @param filter               name of users that load in container
      * @param removeMissingEntries true, it's delete all user is in before loading, false, it's delete nothing
      */
     private void loadUsers(final String filter, final boolean removeMissingEntries) {
 
         if (logger.isInfoEnabled()) {
             logger.info("load users of name like " + filter);
         }
 
         final Collection<String> usernames = userContainer.retrievePrincipalNames();
         LdapTemplate ldapTemplate = new LdapTemplate();
         ldapTemplate.setContextSource(ldapContextSource);
 
         AndFilter filterS = new AndFilter();
         filterS.and(new EqualsFilter(OBJECT_CLASS_ATTRIBUTE, USERS_CLASS));
         filterS.and(new LikeFilter(UID_ATTRIBUTE, filter));
         List users = ldapTemplate.search(
                 rdnUsers, filterS.encode(),
                 new AttributesMapper() {
                     public Object mapFromAttributes(Attributes attrs)
                             throws NamingException {
 
                         if (attrs.get(MAIL_ATTRIBUTE) == null)
                             attrs.put(MAIL_ATTRIBUTE, "");
                         if (attrs.get(COMMON_NAME_ATTRIBUTE) == null)
                             attrs.put(COMMON_NAME_ATTRIBUTE, "");
 
                         String uid = attrs.get(UID_ATTRIBUTE).get().toString();
 
                         String originalDn = UID_ATTRIBUTE + "=" + uid + "," + rdnUsers + "," + baseDn;
 
                         //get the date of last modify of this user
                         DistinguishedName rdn = new DistinguishedName(UID_ATTRIBUTE + "=" + uid + "," + rdnUsers);
                         LdapTemplate tmp = new LdapTemplate();
                         tmp.setContextSource(ldapContextSource);
                         final String[] attrOp = {MODIFY_TIMESTAMP_ATTRIBUTE};
                         Object o = tmp.lookup(rdn, attrOp,
                                 new ContextMapper() {
                                     public Object mapFromContext(Object ctx) {
                                         DirContextAdapter adapter = (DirContextAdapter) ctx;
                                         return adapter.getStringAttribute(attrOp[0]);
 
                                     }
                                 }
 
                         );
 
                         Date lastPasswordChange; //the time of last change for the entry
                         if (o != null) {
                             String modifyTimestamp = o.toString();
                             lastPasswordChange = getDate(modifyTimestamp);
                         } else {
                             //if the modifyTimeStamp is null
                             lastPasswordChange = new Date();
                         }
 
                         return new LdapUser(uid, originalDn, attrs.get(MAIL_ATTRIBUTE).get().toString(), attrs.get(COMMON_NAME_ATTRIBUTE).get().toString(), lastPasswordChange, getRealm());
 
 
                     }
                 });
 
         if (removeMissingEntries) {
             userContainer.updateRemovedPrincipals(usernames);
         }
 
         for (Object user : users) {
             LdapUser u = (LdapUser) user;
             u.setRoles(getGroupsForUser(u.getPrincipalName()));
             userContainer.storePrincipal(u);
         }
 
     }
 
 
     /**
      * Load groups in container
      *
      * @param filter               name of groups that load in container
      * @param removeMissingEntries true, it's delete all groups is in before loading, false, it's delete nothing
      */
     private void loadRoles(final String filter, final boolean removeMissingEntries) {
 
         if (logger.isInfoEnabled()) {
             logger.info("load groups of name like " + filter);
         }
 
         final Collection<String> groupNames = groupContainer.retrievePrincipalNames();
 
         LdapTemplate ldapTemplate = new LdapTemplate();
         ldapTemplate.setContextSource(ldapContextSource);
 
         AndFilter filterS = new AndFilter();
         filterS.and(new EqualsFilter(OBJECT_CLASS_ATTRIBUTE, GROUPS_CLASS));
         filterS.and(new LikeFilter(COMMON_NAME_ATTRIBUTE, filter));
         List groups = ldapTemplate.search(
                 rdnGroups, filterS.encode(),
                 new AttributesMapper() {
                     public Object mapFromAttributes(Attributes attrs) throws NamingException {
                         String cn = attrs.get(COMMON_NAME_ATTRIBUTE).get().toString();
                         String orginalDn = COMMON_NAME_ATTRIBUTE + "=" + cn + "," + rdnGroups + "," + baseDn;
                         return new LdapGroup(cn, orginalDn, getRealm());
                     }
                 });
 
         if (removeMissingEntries) {
             groupContainer.updateRemovedGroups(groupNames);
         }
 
         for (final Object group : groups) {
             groupContainer.storeGroup((LdapGroup) group);
         }
 
     }
 
 
     /**
      * Get a user account given a DN. <code>null</code> will be returned if no
      * such account can be found.
      *
      * @param dn dn
      * @return user account
      * @throws UserDatabaseException on any error
      */
 
     public LdapUser getAccountFromDN(final String dn) throws UserDatabaseException {
 
         if (dn.endsWith(baseDn)) {
             // This looks like a DN so do a lookup
             if (logger.isDebugEnabled()) {
                 logger.debug("Looking up account using DN: " + dn);
             }
             LdapTemplate ldapTemplate = new LdapTemplate();
             ldapTemplate.setContextSource(ldapContextSource);
 
             int indx = dn.indexOf("," + baseDn);
             final String rdn = dn.substring(0, indx);
             Object user = ldapTemplate.lookup(rdn,
                     new AttributesMapper() {
                         public Object mapFromAttributes(Attributes attrs) throws NamingException {
 
 
                             if (attrs.get(MAIL_ATTRIBUTE) == null)
                                 attrs.put(MAIL_ATTRIBUTE, "");
                             if (attrs.get(COMMON_NAME_ATTRIBUTE) == null)
                                 attrs.put(COMMON_NAME_ATTRIBUTE, "");
 
                             String uid = attrs.get(UID_ATTRIBUTE).get().toString();
                             String orginalDn = UID_ATTRIBUTE + "=" + uid + "," + rdnUsers + "," + baseDn;
 
                             //get the date of last modify of this user
                             LdapTemplate tmp = new LdapTemplate();
                             tmp.setContextSource(ldapContextSource);
                             final String[] attrOp = {MODIFY_TIMESTAMP_ATTRIBUTE};
                             Object o = tmp.lookup(rdn, attrOp,
                                     new ContextMapper() {
                                         public Object mapFromContext(Object ctx) {
                                             DirContextAdapter adapter = (DirContextAdapter) ctx;
                                             String value = adapter.getStringAttribute(attrOp[0]);
                                             return value;
 
 
                                         }
                                     }
 
                             );
 
                             Date lastPasswordChange; //the time of last change for the entry
                             if (o != null) {
                                 String modifyTimestamp = o.toString();
                                 lastPasswordChange = getDate(modifyTimestamp);
                             } else {
                                 //if the modifyTimeStamp is null
                                 lastPasswordChange = new Date();
                             }
 
                             return new LdapUser(uid,
                                     orginalDn,
                                     attrs.get(MAIL_ATTRIBUTE).get().toString(),
                                     attrs.get(COMMON_NAME_ATTRIBUTE).get().toString(),
                                     lastPasswordChange,
                                     getRealm()
                             );
 
 
                         }
                     });
 
             if (user == null) {
                 throw new UserDatabaseException("User not found for DN " + dn);
             }
             return (LdapUser) user;
 
         }
         throw new UserDatabaseException("Certificate requires subject to be DN of Ldap user");
     }
 
     /**
      * Return all the roles(groupes) for one user
      *
      * @param username name of the user
      * @return array of user's role
      */
     private Role[] getGroupsForUser(final String username) {
 
         LdapTemplate ldapTemplate = new LdapTemplate();
         ldapTemplate.setContextSource(ldapContextSource);
 
         AndFilter filterS = new AndFilter();
         filterS.and(new EqualsFilter(OBJECT_CLASS_ATTRIBUTE, GROUPS_CLASS));
         filterS.and(new EqualsFilter(MEMBER_ATTRIBUTE, UID_ATTRIBUTE + "=" + username + "," + rdnUsers + "," + baseDn));
         List groups = ldapTemplate.search(
                 rdnGroups, filterS.encode(),
                 new AttributesMapper() {
                     public Object mapFromAttributes(Attributes attrs)
                             throws NamingException {
                         try {
                             return getRole(attrs.get(COMMON_NAME_ATTRIBUTE).get().toString());
                         }
                         catch (UserDatabaseException e) {
                             logger.error("UserDatabaseException :" + e);
                             return null;
                         }
                         catch (RoleNotFoundException e) {
                             logger.error("RoleNotFoundException :" + e);
                             return null;
                         }
                     }
                 });
         return (LdapGroup[]) groups.toArray(new LdapGroup[groups.size()]);
 
     }
 
     /*
     * (non-Javadoc)
     *
     * @see com.adito.core.CoreListener#coreEvent(com.adito.core.CoreEvent)
     */
     public void coreEvent(CoreEvent event) {
         // When in install mode, the wizard looks after re-initialising the user
         // database
         if (!ContextHolder.getContext().isSetupMode() && event instanceof PropertyChangeEvent) {
             PropertyChangeEvent changeEvent = (PropertyChangeEvent) event;
             if (changeEvent.getDefinition().getName().startsWith("ldap.")) {
                 if (logger.isInfoEnabled()) {
                     logger.info("Ldap configuration changed. Re-initialising");
                 }
                 try {
                     //initialise();
                 } catch (Exception e) {
                     logger.error("Failed to re-initialise Ldap.", e);
                 }
             }
         }
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.security.UserDatabase#open(com.adito.core.CoreServlet, com.adito.realms.Realm)
      */
     public void open(CoreServlet controllingServlet, Realm realm) throws Exception {
         try {
             super.open(controllingServlet, realm);
 
             if (logger.isInfoEnabled()) {
                 logger.info("User database is being opened...");
             }
 
             initConfiguration();
 
             userContainer = new UserContainer(userCacheSize, inMemoryCache, usernamesAreCaseSensitive, baseDn);
             groupContainer = new GroupContainer(groupCacheSize, inMemoryCache);
 
             ldapContextSource = new LdapContextSource();
             ldapContextSource.setUrl(LDAP_PROTOCOL + controllerHost);
             ldapContextSource.setBase(baseDn);
             ldapContextSource.setUserDn(serviceAccountName);
             ldapContextSource.setPassword(serviceAccountPassword);
             ldapContextSource.afterPropertiesSet();
 
 
             Map baseEnvProps = new Hashtable();
             baseEnvProps.put("com.sun.jndi.ldap.connect.timeout", timeOut);
 
             if (followReferrals) {
                 baseEnvProps.put(Context.REFERRAL, "follow");
             }
 
             ldapContextSource.setBaseEnvironmentProperties(baseEnvProps);
 
             CoreServlet.getServlet().addCoreListener(this);
             threadRunner = new ThreadRunner("CacheUpdater", getCacheUpdaterJob(), timeToLive);
             threadRunner.start();
         } catch (Exception e) {
             close();
             throw e;
         }
     }
 
     /**
      * Set all properties (option and connection)
      *
      * @throws Exception if one of propertie are not valide
      */
     private void initConfiguration() throws Exception {
         Properties propertyNames = new Properties();
 
         setControllerHost(getProperty("ldap.controllerHost", propertyNames));
        setBaseDN(getProperty("ldap.baseDN", propertyNames));
         setServiceAccountName(getProperty("ldap.serviceAccountUsername", propertyNames));
         setServiceAccountPassword(getProperty("ldap.serviceAccountPassword", propertyNames));
         setDomainUsers(getProperty("ldap.domainUsers", propertyNames));
         setDomainGroups(getProperty("ldap.domainGroups", propertyNames));
 
         setFollowReferrals(getPropertyBoolean("ldap.followReferrals", propertyNames));
         setUserCacheSize(getPropertyInt("ldap.cacheUserMaxObjects", propertyNames));
         setGroupCacheSize(getPropertyInt("ldap.cacheGroupMaxObjects", propertyNames));
         setInMemoryCache(getPropertyBoolean("ldap.cacheInMemory", propertyNames));
         setTimeToLive(getPropertyInt("ldap.userCacheTTL", propertyNames));
 
         setUsernamesAreCaseSensitive(getPropertyBoolean("ldap.usernamesAreCaseSensitive", propertyNames));
         setTimeOut(getPropertyInt("ldap.connection.timeout", propertyNames));
     }
 
 
     /**
      * Return millisecond
      *
      * @param minutes number of minut
      * @return millesecond equivalent to minutes
      */
     private static int minutesToMillis(int minutes) {
         return minutes * 60 * 1000;
     }
 
     /**
      * upadate caches
      *
      * @return proccess
      */
     private Runnable getCacheUpdaterJob() {
         return new Runnable() {
             public void run() {
                 if (logger.isInfoEnabled()) {
                     logger.info("Caching items");
                     logger.info("Caching roles");
                 }
 
 
                 loadRoles(WILDCARD_SEARCH, true);
 
 
                 if (logger.isInfoEnabled()) {
                     logger.info("Finished caching roles");
                     logger.info("Caching users");
                 }
 
                 // once we have the roles, we'll be able to find the users role
                 // assignments
 
 
                 loadUsers(WILDCARD_SEARCH, true);
 
                 if (logger.isInfoEnabled()) {
                     logger.info("Finished caching users");
                     logger.info("Finished caching items");
                 }
             }
         };
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.core.Database#close()
      */
     public void close() throws Exception {
         super.close();
         CoreServlet.getServlet().removeCoreListener(this);
         if (threadRunner != null) {
             threadRunner.stop();
         }
         userContainer.close();
         groupContainer.close();
     }
 
     /**
      * (non-Javadoc)
      *
      * @see com.adito.security.UserDatabase#logout(com.adito.security.User)
      */
     public void logout(User user) {
         //nothing special to do
     }
 
 
     /**
      * As some characters are escaped via a backslash, we need to add some more
      * slashes to get this to work.
      *
      * @param dn
      * @return String
      */
     private static String getEscapedDn(String dn) {
         String escapeDn = dn.replaceAll("\\\\", "\\\\\\\\");
         String escapeForwardSlash = escapeDn.replaceAll("/", "\\\\/");
         return escapeForwardSlash;
     }
 
 
     /** This method return the date of a timestamp of ldap
      2008123115959Z=2008,12(december),31,11:59:59
      */
     /**
      * Return a date equivalent to a timestamp ldap
      * In the case of timestamp is 2008123115959Z, the date is 2008,12(december),31,11:59:59
      *
      * @param timestamp string that reprsent the timestamp like 2008123115959Z
      * @return a date of timestamp
      */
     private static Date getDate(String timestamp) {
 
         GregorianCalendar calendar = new GregorianCalendar(
                 Integer.parseInt(timestamp.substring(0, 4)),
                 Integer.parseInt(timestamp.substring(4, 6)) - 1,
                 Integer.parseInt(timestamp.substring(6, 8)),
                 Integer.parseInt(timestamp.substring(8, 10)),
                 Integer.parseInt(timestamp.substring(10, 12)),
                 Integer.parseInt(timestamp.substring(12, 14)));
 
         return calendar.getTime();
 
     }
 
     /**
      * Set controllerHost
      *
      * @param controllerHost name of controllerHost
      * @throw Exception if name of controllerHost is empty
      */
     private void setControllerHost(String controllerHost) throws Exception {
         if (controllerHost.equals("")) {
            throw new IllegalArgumentException("No ldap controller host configured.");
         }
         this.controllerHost = controllerHost;
     }
 
     /**
      * Set the base DN (example : dc=adito, dc=com)
      *
      * @param baseDN name of domain
      * @throws Exception if name of domain is empty
      */
     private void setBaseDN(String baseDN) throws Exception {
         this.baseDn = baseDN.trim();
         if (this.baseDn.equals("")) {
             throw new IllegalArgumentException("No ldap base DN configured.");
         }
     }
 
     /**
      * Set rdnUsers
      *
      * @param rdnUsers distinguish name of users
      * @throws Exception if distinguish name is empty
      */
     private void setDomainUsers(String rdnUsers) throws Exception {
         this.rdnUsers = rdnUsers.trim();
         if (this.rdnUsers.equals("")) {
            throw new IllegalArgumentException("No ldap domain of user configured.");
         }
     }
 
     /**
      * Set rdnGroups
      *
      * @param rdnGroups distinguish name of groups
      * @throws Exception if distinguish name is empty
      */
     private void setDomainGroups(String rdnGroups) throws Exception {
         this.rdnGroups = rdnGroups.trim();
         if (this.rdnGroups.equals("")) {
            throw new IllegalArgumentException("No ldap domain of group configured.");
         }
     }
 
     /**
      * Set serviceAccountName
      *
      * @param serviceAccountName
      */
     void setServiceAccountName(String serviceAccountName) {
         this.serviceAccountName = serviceAccountName == null ? null : serviceAccountName.trim();
     }
 
     /**
      * Set serviceAccountPassword
      *
      * @param serviceAccountPassword
      */
     void setServiceAccountPassword(String serviceAccountPassword) {
         this.serviceAccountPassword = serviceAccountPassword;
     }
 
     /**
      * Set folloRefferals
      *
      * @param followReferrals
      */
     void setFollowReferrals(boolean followReferrals) {
         this.followReferrals = followReferrals;
     }
 
     /**
      * Set userCacheSize
      *
      * @param userCacheSize
      */
     void setUserCacheSize(int userCacheSize) {
         this.userCacheSize = userCacheSize;
     }
 
     /**
      * Set groupCacheSize
      *
      * @param groupCacheSize
      */
     void setGroupCacheSize(int groupCacheSize) {
         this.groupCacheSize = groupCacheSize;
     }
 
     /**
      * Set inMemoryCache
      *
      * @param inMemoryCache
      */
     void setInMemoryCache(boolean inMemoryCache) {
         this.inMemoryCache = inMemoryCache;
     }
 
     /**
      * Set timeToLive and modify it if it too small
      *
      * @param timeToLive
      */
     void setTimeToLive(int timeToLive) {
         if (timeToLive < 1) {
             logger.warn("Cache TTL is less than 1 minute. This would cause serious performance problems. The minimum value of 1 minute will now be used");
             timeToLive = 1;
         }
         this.timeToLive = minutesToMillis(timeToLive);
     }
 
 
     /**
      * Set usernameAreCaseSensitive
      *
      * @param usernamesAreCaseSensitive
      */
     private void setUsernamesAreCaseSensitive(boolean usernamesAreCaseSensitive) {
         this.usernamesAreCaseSensitive = usernamesAreCaseSensitive;
     }
 
     /**
      * Set timeOut
      *
      * @param timeOut
      */
     private void setTimeOut(int timeOut) {
         this.timeOut = timeOut * 1000;
     }
 
     /**
      * @param key
      * @param propertyNames
      * @return string that correspond to key
      */
     private String getProperty(String key, Properties propertyNames) {
         return Property.getProperty(getRealmKey(key, propertyNames));
     }
 
     /**
      * @param key
      * @param propertyNames
      * @return boolean that correspond to key
      */
     private boolean getPropertyBoolean(String key, Properties propertyNames) {
         return Property.getPropertyBoolean(getRealmKey(key, propertyNames));
     }
 
     /**
      * @param key
      * @param propertyNames
      * @return int thtat correspond to key
      */
     private int getPropertyInt(String key, Properties propertyNames) {
         return Property.getPropertyInt(getRealmKey(key, propertyNames));
     }
 
     /**
      * @param key
      * @param propertyNames
      * @return RealmKey that correspond to key
      */
     private RealmKey getRealmKey(String key, Properties propertyNames) {
         String propertyOrDefault = propertyNames.getProperty(key, key);
         logger.info("propertyOrDefault: " + propertyOrDefault);
         return new RealmKey(propertyOrDefault, realm);
     }
 
 }
