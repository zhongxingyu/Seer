 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.lite.authorizable;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.sakaiproject.nakamura.api.lite.CacheHolder;
 import org.sakaiproject.nakamura.api.lite.Configuration;
 import org.sakaiproject.nakamura.api.lite.Session;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.StoreListener;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Authenticator;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
 import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
 import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
 import org.sakaiproject.nakamura.api.lite.authorizable.Group;
 import org.sakaiproject.nakamura.api.lite.authorizable.User;
 import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
 import org.sakaiproject.nakamura.lite.CachingManagerImpl;
 import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
 import org.sakaiproject.nakamura.lite.accesscontrol.AuthenticatorImpl;
 import org.sakaiproject.nakamura.lite.storage.spi.DisposableIterator;
 import org.sakaiproject.nakamura.lite.storage.spi.SparseRow;
 import org.sakaiproject.nakamura.lite.storage.spi.StorageClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 /**
  * An Authourizable Manager bound to a user, on creation the user ID specified
  * by the caller is trusted.
  *
  * @author ieb
  *
  */
 public class AuthorizableManagerImpl extends CachingManagerImpl implements AuthorizableManager {
 
     private static final String DISABLED_PASSWORD_HASH = "--disabled--";
     private static final Set<String> FILTER_ON_UPDATE = ImmutableSet.of(Authorizable.ID_FIELD,
             Authorizable.PASSWORD_FIELD, Authorizable.LOGIN_ENABLED_PERIOD_FIELD);
     private static final Set<String> FILTER_ON_CREATE = ImmutableSet.of(Authorizable.ID_FIELD,
             Authorizable.PASSWORD_FIELD, Authorizable.LOGIN_ENABLED_PERIOD_FIELD);
     private static final Set<String> ADMIN_FILTER_ON_UPDATE = ImmutableSet.of(Authorizable.ID_FIELD,
             Authorizable.PASSWORD_FIELD);
     private static final Set<String> ADMIN_FILTER_ON_CREATE = ImmutableSet.of(Authorizable.ID_FIELD,
             Authorizable.PASSWORD_FIELD);
     private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizableManagerImpl.class);
     private String currentUserId;
     private StorageClient client;
     private AccessControlManager accessControlManager;
     private String keySpace;
     private String authorizableColumnFamily;
     private User thisUser;
     private boolean closed;
     private Authenticator authenticator;
     private StoreListener storeListener;
     private Session session;
     private Set<String> filterOnUpdate;
     private Set<String> filterOnCreate;
 
     public AuthorizableManagerImpl(User currentUser, Session session, StorageClient client,
             Configuration configuration, AccessControlManagerImpl accessControlManager,
             Map<String, CacheHolder> sharedCache, StoreListener storeListener) throws StorageClientException,
             AccessDeniedException {
         super(client, sharedCache);
         this.currentUserId = currentUser.getId();
         if (currentUserId == null) {
             throw new RuntimeException("Current User ID shoud not be null");
         }
         this.thisUser = currentUser;
         if ( thisUser.isAdmin() ) {
             filterOnUpdate = ADMIN_FILTER_ON_UPDATE;
             filterOnCreate = ADMIN_FILTER_ON_CREATE;
         } else {
             filterOnUpdate = FILTER_ON_UPDATE;
             filterOnCreate = FILTER_ON_CREATE;
         }
         this.session = session;
         this.client = client;
         this.accessControlManager = accessControlManager;
         this.keySpace = configuration.getKeySpace();
         this.authorizableColumnFamily = configuration.getAuthorizableColumnFamily();
         this.authenticator = new AuthenticatorImpl(client, configuration, sharedCache);
         this.closed = false;
         this.storeListener = storeListener;
         accessControlManager.setAuthorizableManager(this);
     }
 
     public User getUser() {
         return thisUser;
     }
 
     public Authorizable findAuthorizable(final String authorizableId) throws AccessDeniedException,
             StorageClientException {
         checkOpen();
         if ( Group.EVERYONE.equals(authorizableId)) {
             return Group.EVERYONE_GROUP;
         }
         if (!this.currentUserId.equals(authorizableId)) {
             accessControlManager.check(Security.ZONE_AUTHORIZABLES, authorizableId,
                     Permissions.CAN_READ);
         }
 
         Map<String, Object> authorizableMap = getCached(keySpace, authorizableColumnFamily,
                 authorizableId);
         if (authorizableMap == null || authorizableMap.isEmpty()) {
             return null;
         }
         if (isAUser(authorizableMap)) {
             return new UserInternal(authorizableMap, session, false);
         } else if (isAGroup(authorizableMap)) {
             return new GroupInternal(authorizableMap, session, false);
         }
         return null;
     }
 
     public void updateAuthorizable(Authorizable authorizable) throws AccessDeniedException,
             StorageClientException {
         updateAuthorizable(authorizable, true);
     }
 
     public void updateAuthorizable(Authorizable authorizable, boolean withTouch) throws AccessDeniedException,
             StorageClientException {
         checkOpen();
         if ( !withTouch && !thisUser.isAdmin() ) {
             throw new StorageClientException("Only admin users can update without touching the user");           
         }
         String id = authorizable.getId();
         if ( authorizable.isImmutable() ) {
             throw new StorageClientException("You cant update an immutable authorizable:"+id);
         }
         if ( authorizable.isReadOnly() ) {
             return;
         }
         if ( authorizable.isNew() ) {
             throw new StorageClientException("You must create an authorizable if its new, you cant update an new authorizable");
         }
         accessControlManager.check(Security.ZONE_AUTHORIZABLES, id, Permissions.CAN_WRITE);
         if ( !authorizable.isModified() ) {
             return;
             // only perform the update and send the event if we see the authorizable as modified. It will be modified ig group membership was changed.
         }
 
         /*
          * Update the principal records for members. The list of members that
          * have been added and removed is converted into a list of Authorzables.
          * If the Authorizable does not exist, its removed from the list of
          * members added, but ignored if it was removed from the list of
          * members. For Authorizables that do exit, the ID of this group is
          * added to the list of principals. All Authorizables that require
          * modification are then modified in the store. Write permissions to
          * modify principals is granted as a result of being able to read the
          * authorizable and being able to add the member. FIXME: possibly we
          * might want to consider using a "can add this user to a group"
          * permission at some point in the future.
          */
         String type = "type:user";
         List<String> attributes = Lists.newArrayList();
         String[] membersAdded = null;
         String[] membersRemoved = null;
 
         if (authorizable instanceof Group) {
             type = "type:group";
             Group group = (Group) authorizable;
             membersAdded = group.getMembersAdded();
             Authorizable[] newMembers = new Authorizable[membersAdded.length];
             int i = 0;
             for (String newMember : membersAdded) {
                 try {
                     newMembers[i] = findAuthorizable(newMember);
                     // members that dont exist or cant be read must be removed.
                     if (newMembers[i] == null) {
                         LOGGER.warn("===================== Added member {} does not exist, and had been removed from the list to be added",newMember );
                         group.removeMember(newMember);
                     } else if (isCyclicMembership(id, newMembers[i])) {
                         LOGGER.warn("Member {} would create circular group membership and has been removed from the list to be added", newMember);
                         newMembers[i] = null;
                         group.removeMember(newMember);
                     }
                 } catch (AccessDeniedException e) {
                     group.removeMember(newMember);
                     LOGGER.warn("Cant read member {} ", newMember);
                 } catch (StorageClientException e) {
                     group.removeMember(newMember);
                     LOGGER.warn("Cant read member {} ", newMember);
                 }
                 i++;
             }
             i = 0;
             membersRemoved = group.getMembersRemoved();
             Authorizable[] retiredMembers = new Authorizable[membersRemoved.length];
             for (String retiredMember : membersRemoved) {
                 try {
                     // members that dont exist require no action
                     retiredMembers[i] = findAuthorizable(retiredMember);
                 } catch (AccessDeniedException e) {
                     LOGGER.warn("Cant read member {} wont be retrired ", retiredMember);
                 } catch (StorageClientException e) {
                     LOGGER.warn("Cant read member {} wont be retired", retiredMember);
                 }
                 i++;
 
             }
 
             String membersAddedCsv = StringUtils.join(membersAdded, ',');
             String membersRemovedCsv = StringUtils.join(membersRemoved, ',');
             LOGGER.debug("Membership Change added [{}] removed [{}] ", membersAddedCsv, membersRemovedCsv);
             int changes = 0;
             // there is now a sparse list of authorizables, that need changing
             for (Authorizable newMember : newMembers) {
                 if (newMember != null) {
                     newMember.addPrincipal(group.getId());
                     if (newMember.isModified()) {
                         Map<String, Object> encodedProperties = StorageClientUtils
                                 .getFilteredAndEcodedMap(newMember.getPropertiesForUpdate(),
                                         filterOnUpdate);
                         encodedProperties.put(Authorizable.ID_FIELD, newMember.getId());
                         putCached(keySpace, authorizableColumnFamily, newMember.getId(),
                                 encodedProperties, newMember.isNew());
                         LOGGER.debug("Updated {} with principal {} {} ",new Object[]{newMember.getId(), group.getId(), encodedProperties});
                         findAuthorizable(newMember.getId());
                         changes++;
                     } else {
                         LOGGER.debug("New Member {} already had group principal {} ",
                                 newMember.getId(), authorizable.getId());
                     }
                 }
             }
             for (Authorizable retiredMember : retiredMembers) {
                 if (retiredMember != null) {
                     retiredMember.removePrincipal(group.getId());
                     if (retiredMember.isModified()) {
                         Map<String, Object> encodedProperties = StorageClientUtils
                                 .getFilteredAndEcodedMap(retiredMember.getPropertiesForUpdate(),
                                         filterOnUpdate);
                         encodedProperties.put(Authorizable.ID_FIELD, retiredMember.getId());
                         putCached(keySpace, authorizableColumnFamily, retiredMember.getId(),
                                 encodedProperties, retiredMember.isNew());
                         changes++;
                         LOGGER.debug("Update {} and removed principal {} ",retiredMember.getId(), group.getId());
                     } else {
                         LOGGER.debug("Retired Member {} didnt have group principal {} ",
                                 retiredMember.getId(), authorizable.getId());
                     }
                 }
             }
             LOGGER.debug(" Finished Updating other principals, made {} changes, Saving Changes to {} ", changes, id);
 
             // if there were added or removed members, send them out as event properties for
             // external integration
             if (membersAdded.length > 0) {
               attributes.add("added:" +  membersAddedCsv);
             }
             if (membersRemoved.length > 0) {
               attributes.add("removed:" +  membersRemovedCsv);
             }
         }
         attributes.add(type);
         boolean wasNew = authorizable.isNew();
         Map<String, Object> beforeUpdateProperties = authorizable.getOriginalProperties();
 
         Map<String, Object> encodedProperties = StorageClientUtils.getFilteredAndEcodedMap(
                 authorizable.getPropertiesForUpdate(), filterOnUpdate);
         if (withTouch) {
             encodedProperties.put(Authorizable.LASTMODIFIED_FIELD, System.currentTimeMillis());
             encodedProperties.put(Authorizable.LASTMODIFIED_BY_FIELD,
                     accessControlManager.getCurrentUserId());
         }
         encodedProperties.put(Authorizable.ID_FIELD, id); // make certain the ID is always there.
         putCached(keySpace, authorizableColumnFamily, id, encodedProperties, authorizable.isNew());
 
         authorizable.reset(getCached(keySpace, authorizableColumnFamily, id));
 
         String[] attrs = attributes.toArray(new String[attributes.size()]);
         storeListener.onUpdate(Security.ZONE_AUTHORIZABLES, id, type, accessControlManager.getCurrentUserId(), wasNew, beforeUpdateProperties, attrs);
 
         // for each added or removed member, send an UPDATE event so indexing can properly
         // record the groups each member is a member of.\
        
         // when we add members we dont emit an event with resource type in it.
         if (membersAdded != null) {
             for (String added : membersAdded) {
                 storeListener.onUpdate(Security.ZONE_AUTHORIZABLES, added, accessControlManager.getCurrentUserId(), null, false, null);
             }
         }
         if (membersRemoved != null) {
             for (String removed : membersRemoved) {
                 storeListener.onUpdate(Security.ZONE_AUTHORIZABLES, removed, accessControlManager.getCurrentUserId(), null, false, null);
             }
         }
     }
 
 
     public boolean createAuthorizable(String authorizableId, String authorizableName,
             String password, Map<String, Object> properties) throws AccessDeniedException,
             StorageClientException {
         checkId(authorizableId);
         if (properties == null) {
           properties = Maps.newHashMap();
         }
         checkOpen();
         if (isAUser(properties)) {
             accessControlManager.check(Security.ZONE_ADMIN, Security.ADMIN_USERS,
                     Permissions.CAN_WRITE);
         } else if (isAGroup(properties)) {
             accessControlManager.check(Security.ZONE_ADMIN, Security.ADMIN_GROUPS,
                     Permissions.CAN_WRITE);
         } else {
             throw new AccessDeniedException(Security.ZONE_ADMIN, Security.ADMIN_AUTHORIZABLES,
                     "denied create on unidentified authorizable",
                     accessControlManager.getCurrentUserId());
         }
         Authorizable a = findAuthorizable(authorizableId);
         if (a != null) {
             return false;
         }
         Map<String, Object> encodedProperties = StorageClientUtils.getFilteredAndEcodedMap(
                 properties, filterOnCreate);
         encodedProperties.put(Authorizable.ID_FIELD, authorizableId);
         encodedProperties
                 .put(Authorizable.NAME_FIELD, authorizableName);
         if (password != null) {
             encodedProperties.put(Authorizable.PASSWORD_FIELD,
                     StorageClientUtils.secureHash(password));
         } else {
             encodedProperties.put(Authorizable.PASSWORD_FIELD,
                     Authorizable.NO_PASSWORD);
         }
         encodedProperties.put(Authorizable.CREATED_FIELD,
                 System.currentTimeMillis());
         encodedProperties.put(Authorizable.CREATED_BY_FIELD,
                 accessControlManager.getCurrentUserId());
         putCached(keySpace, authorizableColumnFamily, authorizableId, encodedProperties, true);
         return true;
     }
 
 
     private void checkId(String authorizableId) throws StorageClientException {
         if ( authorizableId.charAt(0) == '_') {
             throw new StorageClientException("Authorizables may not start with _  :"+authorizableId);
         }
         for ( int i = 0; i < authorizableId.length(); i++) {
             int cp = authorizableId.codePointAt(i);
             if ( Character.isWhitespace(cp) ||
             Character.isISOControl(cp) ||
             Character.isMirrored(cp) ) {
                 throw new StorageClientException("Authorizables may not contain :"+authorizableId.charAt(i));
             }
         }
     }
 
     public boolean createUser(String authorizableId, String authorizableName, String password,
             Map<String, Object> properties) throws AccessDeniedException, StorageClientException {
         if (properties == null) {
             properties = Maps.newHashMap();
         }
         checkOpen();
         if (!isAUser(properties)) {
             Map<String, Object> m = Maps.newHashMap(properties);
             m.put(Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.USER_VALUE);
             properties = m;
         }
         return createAuthorizable(authorizableId, authorizableName, password, properties);
     }
 
     public boolean createGroup(String authorizableId, String authorizableName,
             Map<String, Object> properties) throws AccessDeniedException, StorageClientException {
         if (properties == null) {
             properties = Maps.newHashMap();
         }
         checkOpen();
         if (!isAGroup(properties)) {
             Map<String, Object> m = Maps.newHashMap(properties);
             m.put(Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.GROUP_VALUE);
             properties = m;
         }
         return createAuthorizable(authorizableId, authorizableName, null, properties);
     }
 
     public void delete(String authorizableId) throws AccessDeniedException, StorageClientException {
         checkOpen();
         accessControlManager.check(Security.ZONE_ADMIN, authorizableId, Permissions.CAN_DELETE);
         Authorizable authorizable = findAuthorizable(authorizableId);
         if (authorizable != null){
             removeCached(keySpace, authorizableColumnFamily, authorizableId);
             storeListener.onDelete(Security.ZONE_AUTHORIZABLES, authorizableId, accessControlManager.getCurrentUserId(), getType(authorizable), authorizable.getOriginalProperties());
         }
     }
 
     private String getType(Authorizable authorizable) {
         if ( authorizable != null ) {
             if ( authorizable.hasProperty(Authorizable.AUTHORIZABLE_TYPE_FIELD)) {
                 return (String) authorizable.getProperty(Authorizable.AUTHORIZABLE_TYPE_FIELD);
             } else if ( authorizable instanceof Group) {
                 return Authorizable.GROUP_VALUE;
             } else if ( authorizable instanceof User) {
                 // this was an object.
                 return String.valueOf(Authorizable.USER_VALUE);
             }
             
         }
         return null;
     }
 
     private String getType(Map<String, Object> props) {
         if ( props != null ) {
             if ( props.containsKey(Authorizable.AUTHORIZABLE_TYPE_FIELD)) {
                 return (String) props.get(Authorizable.AUTHORIZABLE_TYPE_FIELD);
             }
         }
         return null;
     }
 
     
 
     public void close() {
         closed = true;
     }
 
     private void checkOpen() throws StorageClientException {
         if (closed) {
             throw new StorageClientException("Authorizable Manager is closed");
         }
     }
 
     // TODO: Unit test
     public void changePassword(Authorizable authorizable, String password, String oldPassword)
             throws StorageClientException, AccessDeniedException {
         String id = authorizable.getId();
 
         if (thisUser.isAdmin() || currentUserId.equals(id)) {
             if (!thisUser.isAdmin()) {
                 User u = authenticator.authenticate(id, oldPassword);
                 if (u == null) {
                    throw new StorageClientException(
                             "Unable to change passwords, old password does not match");
                 }
             }
             putCached(keySpace, authorizableColumnFamily, id, ImmutableMap.of(
                     Authorizable.LASTMODIFIED_FIELD,
                     (Object)System.currentTimeMillis(),
                     Authorizable.ID_FIELD,
                     id,
                     Authorizable.LASTMODIFIED_BY_FIELD,
                     accessControlManager.getCurrentUserId(),
                     Authorizable.PASSWORD_FIELD,
                     StorageClientUtils.secureHash(password)), false);
 
             storeListener.onUpdate(Security.ZONE_AUTHORIZABLES, id, currentUserId, getType(authorizable), false, null, "op:change-password");
 
         } else {
             throw new AccessDeniedException(Security.ZONE_ADMIN, id,
                     "Not allowed to change the password, must be the user or an admin user",
                     currentUserId);
         }
 
     }
 
     public DisposableIterator<Authorizable> findAuthorizable(String propertyName, String value,
             Class<? extends Authorizable> authorizableType) throws StorageClientException {
         Builder<String, Object> builder = ImmutableMap.builder();
         if (value != null) {
             builder.put(propertyName, value);
         }
         if (authorizableType.equals(User.class)) {
             builder.put(Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.USER_VALUE);
         } else if (authorizableType.equals(Group.class)) {
             builder.put(Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.GROUP_VALUE);
         }
         final DisposableIterator<Map<String, Object>> authMaps = client.find(keySpace,
                 authorizableColumnFamily, builder.build(), this);
 
         return new PreemptiveIterator<Authorizable>() {
 
             private Authorizable authorizable;
 
             @Override
             protected boolean internalHasNext() {
                 while (authMaps.hasNext()) {
                     Map<String, Object> authMap = authMaps.next();
                     if (authMap != null) {
                         try {
                             // filter any authorizables from the list that user
                             // cant
                             // see, this is not the way we want to do it as it
                             // will generate a sparse search problem.
                             // FIXME: put this in the query.
                             accessControlManager
                                     .check(Security.ZONE_AUTHORIZABLES, (String) authMap.get(Authorizable.ID_FIELD),
                                             Permissions.CAN_READ);
                             if (isAUser(authMap)) {
                                 authorizable = new UserInternal(authMap, session, false);
                                 return true;
                             } else if (isAGroup(authMap)) {
                                 authorizable = new GroupInternal(authMap, session, false);
                                 return true;
                             }
                         } catch (AccessDeniedException e) {
                             LOGGER.debug("Search result filtered ", e.getMessage());
                         } catch (StorageClientException e) {
                             LOGGER.error("Failed to check ACLs ", e.getMessage());
                             close();
                             return false;
                         }
 
                     }
                 }
 
                 authorizable = null;
                 close();
                 return false;
             }
 
             @Override
             protected Authorizable internalNext() {
                 return authorizable;
             }
             @Override
             public void close() {
                 authMaps.close();
                 super.close();
             }
 
         };
     }
 
 
     private boolean isAGroup(Map<String, Object> authProperties) {
         return (authProperties != null)
                 && Authorizable.GROUP_VALUE.equals(authProperties
                         .get(Authorizable.AUTHORIZABLE_TYPE_FIELD));
     }
 
     private boolean isAUser(Map<String, Object> authProperties) {
         return (authProperties != null)
                 && Authorizable.USER_VALUE.equals(authProperties
                         .get(Authorizable.AUTHORIZABLE_TYPE_FIELD));
     }
 
     private boolean isCyclicMembership(String groupId, Authorizable newMember) {
         if (newMember.isGroup()) {
             Group newGroupMember = (Group) newMember;
             for (String memberOfNewMember : newGroupMember.getMembers()) {
                 if (groupId.equals(memberOfNewMember)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     @Override
     protected Logger getLogger() {
         return LOGGER;
     }
 
     public void disablePassword(Authorizable authorizable) throws StorageClientException,
             AccessDeniedException {
         String id = authorizable.getId();
 
         if (thisUser.isAdmin()) {
             putCached(keySpace, authorizableColumnFamily, id, ImmutableMap.of(
                     Authorizable.LASTMODIFIED_FIELD,
                     (Object)System.currentTimeMillis(),
                     Authorizable.ID_FIELD,
                     id,
                     Authorizable.LASTMODIFIED_BY_FIELD,
                     accessControlManager.getCurrentUserId(),
                     Authorizable.PASSWORD_FIELD,
                     DISABLED_PASSWORD_HASH), false);
 
             storeListener.onUpdate(Security.ZONE_AUTHORIZABLES, id, currentUserId, getType(authorizable), false, null, "op:disable-password");
 
         } else {
             throw new AccessDeniedException(Security.ZONE_ADMIN, id,
                     "Not allowed to disable the password, must be an admin user",
                     currentUserId);
         }
     }
 
 
     public void triggerRefresh(String id) throws StorageClientException, AccessDeniedException {
         Authorizable c = findAuthorizable(id);
         if ( c != null ) {
             String type = getType(c);
             storeListener.onUpdate(Security.ZONE_AUTHORIZABLES, id,
                     accessControlManager.getCurrentUserId(),  type, false, null,
                     new String[] { type });
         }
     }
     
     public void triggerRefreshAll() throws StorageClientException {
         if (User.ADMIN_USER.equals(accessControlManager.getCurrentUserId()) ) {
             DisposableIterator<SparseRow> all = client.listAll(keySpace, authorizableColumnFamily);
             try {
                 while(all.hasNext()) {
                     Map<String, Object> c = all.next().getProperties();
                     if ( c.containsKey(Authorizable.ID_FIELD) ) {
                         storeListener.onUpdate(Security.ZONE_AUTHORIZABLES, (String)c.get(Authorizable.ID_FIELD), User.ADMIN_USER, getType(c), false, null, (String[]) null);
                     }
                 }
             } finally {
                 all.close(); // not necessary if the wile completes, but if there is an error it might be.
             }
         }
     }
 
 }
