 package org.sakaiproject.nakamura.lite.authorizable;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.sakaiproject.nakamura.api.lite.Configuration;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Authenticator;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
 import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
 import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
 import org.sakaiproject.nakamura.api.lite.authorizable.Group;
 import org.sakaiproject.nakamura.api.lite.authorizable.User;
 import org.sakaiproject.nakamura.lite.Security;
 import org.sakaiproject.nakamura.lite.accesscontrol.AuthenticatorImpl;
 import org.sakaiproject.nakamura.lite.storage.StorageClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Maps;
 
 /**
  * An Authourizable Manager bound to a user, on creation the user ID specified
  * by the caller is trusted.
  * 
  * @author ieb
  * 
  */
 public class AuthorizableManagerImpl implements AuthorizableManager {
 
     private static final Set<String> FILTER_ON_UPDATE = ImmutableSet.of(Authorizable.ID_FIELD,
             Authorizable.PASSWORD_FIELD);
     private static final Set<String> FILTER_ON_CREATE = ImmutableSet.of(Authorizable.ID_FIELD,
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
 
     public AuthorizableManagerImpl(User currentUser, StorageClient client,
             Configuration configuration, AccessControlManager accessControlManager)
             throws StorageClientException, AccessDeniedException {
         this.currentUserId = currentUser.getId();
         if (currentUserId == null) {
             throw new RuntimeException("Current User ID shoud not be null");
         }
         this.thisUser = currentUser;
         this.client = client;
         this.accessControlManager = accessControlManager;
         this.keySpace = configuration.getKeySpace();
         this.authorizableColumnFamily = configuration.getAuthorizableColumnFamily();
         this.authenticator = new AuthenticatorImpl(client, configuration);
         this.closed = false;
     }
 
     public User getUser() {
         return thisUser;
     }
 
     public Authorizable findAuthorizable(String authorizableId) throws AccessDeniedException,
             StorageClientException {
         checkOpen();
         if (!this.currentUserId.equals(authorizableId)) {
             accessControlManager.check(Security.ZONE_AUTHORIZABLES, authorizableId,
                     Permissions.CAN_READ);
         }
         Map<String, Object> authorizableMap = client.get(keySpace, authorizableColumnFamily,
                 authorizableId);
         if (authorizableMap == null || authorizableMap.isEmpty()) {
             return null;
         }
         LOGGER.info("Found Map {} ", authorizableMap);
         if (Authorizable.isAUser(authorizableMap)) {
             return new User(authorizableMap);
         } else if (Authorizable.isAGroup(authorizableMap)) {
             return new Group(authorizableMap);
         }
         return null;
     }
 
     public void updateAuthorizable(Authorizable authorizable) throws AccessDeniedException,
             StorageClientException {
         checkOpen();
         String id = authorizable.getId();
         accessControlManager.check(Security.ZONE_AUTHORIZABLES, id, Permissions.CAN_WRITE);
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
         if (authorizable instanceof Group) {
             Group group = (Group) authorizable;
             String[] membersAdded = group.getMembersAdded();
             Authorizable[] newMembers = new Authorizable[membersAdded.length];
            Authorizable[] retiredMembers = new Authorizable[membersAdded.length];
             int i = 0;
             for (String newMember : membersAdded) {
                 try {
                     newMembers[i] = findAuthorizable(newMember);
                     // members that dont exist or cant be read must be removed.
                     if (newMembers[i] == null) {
                         group.removeMember(newMember);
                     }
                 } catch (AccessDeniedException e) {
                     group.removeMember(newMember);
                     LOGGER.debug("Cant read member {} ", newMember);
                 } catch (StorageClientException e) {
                     group.removeMember(newMember);
                     LOGGER.debug("Cant read member {} ", newMember);
                 }
                 i++;
             }
             i = 0;
             String[] membersRemoved = group.getMembersRemoved();
             for (String retiredMember : membersRemoved) {
                 try {
                     // members that dont exist require no action
                     retiredMembers[i] = findAuthorizable(retiredMember);
                 } catch (AccessDeniedException e) {
                     LOGGER.debug("Cant read member {} ", retiredMember);
                 } catch (StorageClientException e) {
                     LOGGER.debug("Cant read member {} ", retiredMember);
                 }
                 i++;
 
             }
 
             LOGGER.info("Membership Change added [{}] removed [{}] ", newMembers, retiredMembers);
             // there is now a sparse list of authorizables, that need changing
             for (Authorizable newMember : newMembers) {
                 if (newMember != null) {
                     newMember.addPrincipal(group.getId());
                     if (newMember.isModified()) {
                         Map<String, Object> encodedProperties = StorageClientUtils
                                 .getFilteredAndEcodedMap(newMember.getPropertiesForUpdate(),
                                         FILTER_ON_UPDATE);
                         client.insert(keySpace, authorizableColumnFamily, newMember.getId(),
                                 encodedProperties);
                     } else {
                         LOGGER.info("New Member {} already had group principal {} ",
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
                                         FILTER_ON_UPDATE);
                         client.insert(keySpace, authorizableColumnFamily, retiredMember.getId(),
                                 encodedProperties);
                     } else {
                         LOGGER.info("Retired Member {} didnt have group principal {} ",
                                 retiredMember.getId(), authorizable.getId());
                     }
                 }
             }
         }
         Map<String, Object> encodedProperties = StorageClientUtils.getFilteredAndEcodedMap(
                 authorizable.getPropertiesForUpdate(), FILTER_ON_UPDATE);
         encodedProperties.put(Authorizable.LASTMODIFIED,
                 StorageClientUtils.toStore(System.currentTimeMillis()));
         encodedProperties.put(Authorizable.LASTMODIFIED_BY,
                 StorageClientUtils.toStore(accessControlManager.getCurrentUserId()));
         client.insert(keySpace, authorizableColumnFamily, id, encodedProperties);
         authorizable.reset();
 
     }
 
     public boolean createAuthorizable(String authorizableId, String authorizableName,
             String password, Map<String, Object> properties) throws AccessDeniedException,
             StorageClientException {
         checkOpen();
         if (Authorizable.isAUser(properties)) {
             accessControlManager.check(Security.ZONE_ADMIN, Security.ADMIN_USERS,
                     Permissions.CAN_WRITE);
         } else if (Authorizable.isAGroup(properties)) {
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
                 properties, FILTER_ON_CREATE);
         encodedProperties.put(Authorizable.ID_FIELD, StorageClientUtils.toStore(authorizableId));
         encodedProperties
                 .put(Authorizable.NAME_FIELD, StorageClientUtils.toStore(authorizableName));
         if (password != null) {
             encodedProperties.put(Authorizable.PASSWORD_FIELD,
                     StorageClientUtils.toStore(StorageClientUtils.secureHash(password)));
         } else {
             encodedProperties.put(Authorizable.PASSWORD_FIELD,
                     StorageClientUtils.toStore(Authorizable.NO_PASSWORD));
         }
         encodedProperties.put(Authorizable.CREATED,
                 StorageClientUtils.toStore(System.currentTimeMillis()));
         encodedProperties.put(Authorizable.CREATED_BY,
                 StorageClientUtils.toStore(accessControlManager.getCurrentUserId()));
         client.insert(keySpace, authorizableColumnFamily, authorizableId, encodedProperties);
         return true;
     }
 
     public boolean createUser(String authorizableId, String authorizableName, String password,
             Map<String, Object> properties) throws AccessDeniedException, StorageClientException {
         checkOpen();
         if (!Authorizable.isAUser(properties)) {
             Map<String, Object> m = Maps.newHashMap(properties);
             m.put(Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.USER_VALUE);
             properties = m;
         }
         return createAuthorizable(authorizableId, authorizableName, password, properties);
     }
 
     public boolean createGroup(String authorizableId, String authorizableName,
             Map<String, Object> properties) throws AccessDeniedException, StorageClientException {
         checkOpen();
         if (!Authorizable.isAGroup(properties)) {
             Map<String, Object> m = Maps.newHashMap(properties);
             m.put(Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.GROUP_VALUE);
             properties = m;
         }
         return createAuthorizable(authorizableId, authorizableName, null, properties);
     }
 
     public void delete(String authorizableId) throws AccessDeniedException, StorageClientException {
         checkOpen();
         accessControlManager.check(Security.ZONE_ADMIN, authorizableId, Permissions.CAN_DELETE);
         client.remove(keySpace, authorizableColumnFamily, authorizableId);
 
     }
 
     public void close() {
         closed = true;
     }
 
     private void checkOpen() throws StorageClientException {
         if (closed) {
             throw new StorageClientException("Authorizable Manager is closed");
         }
     }
 
     @Override
     public void changePassword(Authorizable authorizable, String password, String oldPassword)
             throws StorageClientException, AccessDeniedException {
         String id = authorizable.getId();
         
         if (thisUser.isAdmin() || currentUserId.equals(id)) {
             if ( !thisUser.isAdmin() ) {
                 User u = authenticator.authenticate(id, oldPassword);
                 if ( u == null ) {
                     throw new StorageClientException("Unable to change passwords, old password does not match");
                 }
             }
             client.insert(keySpace, authorizableColumnFamily, id, ImmutableMap.of(
                     Authorizable.LASTMODIFIED,
                     StorageClientUtils.toStore(System.currentTimeMillis()),
                     Authorizable.LASTMODIFIED_BY,
                     StorageClientUtils.toStore(accessControlManager.getCurrentUserId()),
                     Authorizable.PASSWORD_FIELD,
                     StorageClientUtils.toStore(StorageClientUtils.secureHash(password))));
         } else {
             throw new AccessDeniedException(Security.ZONE_ADMIN, id,
                     "Not allowed to change the password, must be the user or an admin user",
                     currentUserId);
         }
     }
 
     @Override
     public Iterator<Authorizable> findAuthorizable(String propertyName, String value,
             Class<? extends Authorizable> authorizableType) throws StorageClientException {
         Builder<String, Object> builder = ImmutableMap.builder();
         if ( value != null ) {
             builder.put(propertyName, StorageClientUtils.toStore(value));
         }
         if (authorizableType.equals(User.class)) {
             builder.put(Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.USER_VALUE);
         } else if (authorizableType.equals(Group.class)) {
             builder.put(Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.GROUP_VALUE);
         }
         final Iterator<Map<String, Object>> authMaps = client.find(keySpace,
                 authorizableColumnFamily, builder.build());
 
         return  new Iterator<Authorizable>() {
 
             private Authorizable authorizable;
 
             @Override
             public boolean hasNext() {
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
                                     .check(Security.ZONE_AUTHORIZABLES, StorageClientUtils
                                             .toString(authMap.get(Authorizable.ID_FIELD)),
                                             Permissions.CAN_READ);
                             if (Authorizable.isAUser(authMap)) {
                                 authorizable = new User(authMap);
                                 return true;
                             } else if (Authorizable.isAGroup(authMap))
                                 authorizable = new Group(authMap);
                             return true;
                         } catch (AccessDeniedException e) {
                             LOGGER.debug("Search result filtered ", e.getMessage());
                         } catch (StorageClientException e) {
                             LOGGER.error("Failed to check ACLs ", e.getMessage());
                             return false;
                         }
 
                     }
                 }
 
                 authorizable = null;
                 return false;
             }
 
             @Override
             public Authorizable next() {
                 return authorizable;
             }
 
             @Override
             public void remove() {
                 throw new UnsupportedOperationException();
             }
 
         };
     }
 
 }
