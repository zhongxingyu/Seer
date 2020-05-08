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
 package org.sakaiproject.nakamura.lite.accesscontrol;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import org.sakaiproject.nakamura.api.lite.CacheHolder;
 import org.sakaiproject.nakamura.api.lite.Configuration;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.StoreListener;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AclModification;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Permission;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
 import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
 import org.sakaiproject.nakamura.api.lite.authorizable.Group;
 import org.sakaiproject.nakamura.api.lite.authorizable.User;
 import org.sakaiproject.nakamura.lite.CachingManager;
 import org.sakaiproject.nakamura.lite.storage.StorageClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class AccessControlManagerImpl extends CachingManager implements AccessControlManager {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlManagerImpl.class);
     private User user;
     private String keySpace;
     private String aclColumnFamily;
     private Map<String, int[]> cache = new ConcurrentHashMap<String, int[]>();
     private boolean closed;
     private StoreListener storeListener;
 
     public AccessControlManagerImpl(StorageClient client, User currentUser, Configuration config,
             Map<String, CacheHolder> sharedCache, StoreListener storeListener) {
         super(client, sharedCache);
         this.user = currentUser;
         this.aclColumnFamily = config.getAclColumnFamily();
         this.keySpace = config.getKeySpace();
         closed = false;
         this.storeListener = storeListener;
     }
 
     public Map<String, Object> getAcl(String objectType, String objectPath)
             throws StorageClientException, AccessDeniedException {
         checkOpen();
         check(objectType, objectPath, Permissions.CAN_READ_ACL);
 
         String key = this.getAclKey(objectType, objectPath);
         return ImmutableMap.copyOf(getCached(keySpace, aclColumnFamily, key));
     }
     
     public Map<String, Object> getEffectiveAcl(String objectType, String objectPath)
             throws StorageClientException, AccessDeniedException {
         throw new UnsupportedOperationException("Nag someone to implement this");
     }
 
     public void setAcl(String objectType, String objectPath, AclModification[] aclModifications)
             throws StorageClientException, AccessDeniedException {
         checkOpen();
         check(objectType, objectPath, Permissions.CAN_WRITE_ACL);
         String key = this.getAclKey(objectType, objectPath);
         Map<String, Object> currentAcl = getAcl(objectType, objectPath);
         Map<String, Object> modifications = Maps.newLinkedHashMap();
         for (AclModification m : aclModifications) {
             String name = m.getAceKey();
             if (m.isRemove()) {
                 modifications.put(name, null);
             } else {
 
                 int originalbitmap = toInt(currentAcl.get(name));
                 int modifiedbitmap = m.modify(originalbitmap);
                 modifications.put(name, modifiedbitmap);
                 
                 // KERN-1515
                 // We need to modify the opposite key to apply the
                 // reverse of the change we just made. Otherwise,
                 // you can end up with ACLs with contradictions, like:
                 // anonymous@g=1, anonymous@d=1
                 if (currentAcl.containsKey(inverseKeyOf(name))) {
                   // XOR gives us a mask of only the bits that changed
                   int difference = originalbitmap ^ modifiedbitmap;
                   int otherbitmap = toInt(currentAcl.get(inverseKeyOf(name)));
                   // toggle the bits that have been modified
                   int modifiedotherbitmap = otherbitmap ^ difference;
                   modifications.put(inverseKeyOf(name), modifiedotherbitmap);
                 }
             }
         }
         LOGGER.debug("Updating ACL {} {} ", key, modifications);
         putCached(keySpace, aclColumnFamily, key, modifications, (currentAcl == null || currentAcl.size() == 0));
         storeListener.onUpdate(objectType, objectPath,  getCurrentUserId(), false, "op:acl");
     }
     
     private String inverseKeyOf(String key) {
       if (key == null) {
         return null;
       }
       if (AclModification.isGrant(key)) {
         return AclModification.getPrincipal(key) + AclModification.DENIED_MARKER;
       } else if (AclModification.isDeny(key)) {
         return AclModification.getPrincipal(key) + AclModification.GRANTED_MARKER;
       } else {
         return key;
       }
     }
 
     public void check(String objectType, String objectPath, Permission permission)
             throws AccessDeniedException, StorageClientException {
         if (user.isAdmin()) {
             return;
         }
         // users can always operate on their own user object.
         if (Security.ZONE_AUTHORIZABLES.equals(objectType) && user.getId().equals(objectPath)) {
             return;
         }
         int[] privileges = compilePermission(user, objectType, objectPath, 0);
         if (!((permission.getPermission() & privileges[0]) == permission.getPermission())) {
             throw new AccessDeniedException(objectType, objectPath, permission.getName(),
                     user.getId());
         }
     }
 
     private String getAclKey(String objectType, String objectPath) {
        if (objectPath.startsWith("/")) {
             return objectType + objectPath;
         }
         return objectType + "/" + objectPath;
     }
 
     private int[] compilePermission(Authorizable authorizable, String objectType,
             String objectPath, int recursion) throws StorageClientException {
         String key = getAclKey(objectType, objectPath);
         if (user.getId().equals(authorizable.getId()) && cache.containsKey(key)) {
             return cache.get(key);
         } else {
             LOGGER.debug("Cache Miss {} [{}] ", cache, key);
         }
 
         Map<String, Object> acl = getCached(keySpace, aclColumnFamily, key);
         LOGGER.debug("ACL on {} is {} ", key, acl);
 
         int grants = 0;
         int denies = 0;
         if (acl != null) {
 
             {
                 String principal = authorizable.getId();
                 int tg = toInt(acl.get(principal
                         + AclModification.GRANTED_MARKER));
                 int td = toInt(acl
                         .get(principal + AclModification.DENIED_MARKER));
                 grants = grants | tg;
                 denies = denies | td;
                 // LOGGER.info("Added Permissions for {} {}   result {} {}",new
                 // Object[]{tg,td,grants,denies});
 
             }
             for (String principal : authorizable.getPrincipals()) {
                 int tg = toInt(acl.get(principal
                         + AclModification.GRANTED_MARKER));
                 int td = toInt(acl
                         .get(principal + AclModification.DENIED_MARKER));
                 grants = grants | tg;
                 denies = denies | td;
                 // LOGGER.info("Added Permissions for {} {}   result {} {}",new
                 // Object[]{tg,td,grants,denies});
             }
             if (!User.ANON_USER.equals(authorizable.getId())) {
                 // all users except anon are in the group everyone, by default
                 // but only if not already denied or granted by a more specific
                 // permission.
                 int tg = (toInt(acl.get(Group.EVERYONE
                         + AclModification.GRANTED_MARKER)) & ~denies);
                 int td = (toInt(acl.get(Group.EVERYONE
                         + AclModification.DENIED_MARKER)) & ~grants);
                 // LOGGER.info("Adding Permissions for Everyone {} {} ",tg,td);
                 grants = grants | tg;
                 denies = denies | td;
 
             }
             /*
              * grants contains the granted permissions in a bitmap denies
              * contains the denied permissions in a bitmap
              */
             int granted = grants;
             int denied = denies;
 
             /*
              * Only look to parent objects if this is not the root object and
              * everything is not granted and denied
              */
             if (recursion < 20 && !StorageClientUtils.isRoot(objectPath)
                     && (granted != 0xffff || denied != 0xffff)) {
                 recursion++;
                 int[] parentPriv = compilePermission(authorizable, objectType,
                         StorageClientUtils.getParentObjectPath(objectPath), recursion);
                 if (parentPriv != null) {
                     /*
                      * Grant permission not denied at this level parentPriv[0]
                      * is permissions granted by the parent ~denies is
                      * permissions not denied here parentPriv[0] & ~denies is
                      * permissions granted by the parent that have not been
                      * denied here. we need to add those to things granted here.
                      * ie |
                      */
                     granted = grants | (parentPriv[0] & ~denies);
                     /*
                      * Deny permissions not granted at this level
                      */
                     denied = denies | (parentPriv[1] & ~grants);
                 }
             }
             // If not denied all users and groups can read other users and
             // groups and all content can be read
             if (((denied & Permissions.CAN_READ.getPermission()) == 0)
                     && (Security.ZONE_AUTHORIZABLES.equals(objectType) || Security.ZONE_CONTENT
                             .equals(objectType))) {
                 granted = granted | Permissions.CAN_READ.getPermission();
                 // LOGGER.info("Default Read Permission set {} {} ",key,denied);
             } else {
                 // LOGGER.info("Default Read has been denied {} {} ",key,
                 // denied);
             }
             // LOGGER.info("Permissions on {} for {} is {} {} ",new
             // Object[]{key,user.getId(),granted,denied});
             /*
              * Keep a cached copy
              */
             if (user.getId().equals(authorizable.getId())) {
                 cache.put(key, new int[] { granted, denied });
             }
             return new int[] { granted, denied };
 
         }
         if (Security.ZONE_AUTHORIZABLES.equals(objectType)
                 || Security.ZONE_CONTENT.equals(objectType)) {
             // unless explicitly denied all users can read other users.
             return new int[] { Permissions.CAN_READ.getPermission(), 0 };
         }
         return new int[] { 0, 0 };
     }
 
     private int toInt(Object object) {
         if ( object instanceof Integer ) {
             return ((Integer) object).intValue();
         }
         return 0;
     }
 
     public String getCurrentUserId() {
         return user.getId();
     }
 
     public void close() {
         closed = true;
     }
 
     private void checkOpen() throws StorageClientException {
         if (closed) {
             throw new StorageClientException("Access Control Manager is closed");
         }
     }
 
     public boolean can(Authorizable authorizable, String objectType, String objectPath,
             Permission permission) {
         if (authorizable instanceof User && ((User) authorizable).isAdmin()) {
             return true;
         }
         // users can always operate on their own user object.
         if (Security.ZONE_AUTHORIZABLES.equals(objectType)
                 && authorizable.getId().equals(objectPath)) {
             return true;
         }
         try {
             int[] privileges = compilePermission(authorizable, objectType, objectPath, 0);
             if (!((permission.getPermission() & privileges[0]) == permission.getPermission())) {
                 return false;
             }
         } catch (StorageClientException e) {
             LOGGER.warn(e.getMessage(), e);
             return false;
         }
         return true;
     }
 
     public Permission[] getPermissions(String objectType, String path) throws StorageClientException {
         int[] perms = compilePermission(this.user, objectType, path, 0);
         List<Permission> permissions = Lists.newArrayList();
         for (Permission p : Permissions.PRIMARY_PERMISSIONS) {
             if ((perms[0] & p.getPermission()) == p.getPermission()) {
                 permissions.add(p);
             }
         }
         return permissions.toArray(new Permission[permissions.size()]);
     }
 
     public String[] findPrincipals(String objectType, String objectPath, int permission, boolean granted) throws StorageClientException {
         Map<String, int[]> principalMap = internalCompilePrincipals(objectType, objectPath, 0);
         LOGGER.debug("Got Principals {} ",principalMap);
         List<String> principals = Lists.newArrayList();
         for (Entry<String, int[]> perm : principalMap.entrySet()) {
             int[] p = perm.getValue();
             if ( granted && (p[0] & permission) == permission ) {
                 principals.add(perm.getKey());
                 LOGGER.debug("Included {} {} {} ",new Object[]{perm.getKey(), perm.getValue(), permission});
             } else if ( !granted && (p[1] & permission) == permission) {
                 principals.add(perm.getKey());
                 LOGGER.debug("Included {} {} {} ",new Object[]{perm.getKey(), perm.getValue(), permission});
             } else {
                 LOGGER.debug("Filtered {} {} {} ",new Object[]{perm.getKey(), perm.getValue(), permission});
             }
         }
         LOGGER.debug(" Found Principals {} ",principals);
         return principals.toArray(new String[principals.size()]);
     }
     
 
 
     private Map<String, int[]> internalCompilePrincipals(String objectType, String objectPath, int recursion) throws StorageClientException {
         Map<String, int[]> compiledPermissions = Maps.newHashMap();
         String key = getAclKey(objectType, objectPath);
 
         Map<String, Object> acl = getCached(keySpace, aclColumnFamily, key);
 
         if (acl != null) {
             LOGGER.debug("Checking {} {} ",key,acl);
             for (Entry<String, Object> ace : acl.entrySet()) {
                 String aceKey = ace.getKey();
                 String principal = aceKey.substring(0, aceKey.length() - 2);
                 
                 if (!compiledPermissions.containsKey(principal)) {
                     int tg = toInt(acl.get(principal
                             + AclModification.GRANTED_MARKER));
                     int td = toInt(acl.get(principal
                             + AclModification.DENIED_MARKER));
                     compiledPermissions.put(principal, new int[] { tg, td });
                     LOGGER.debug("added {} ",principal);
                 }
 
             }
         }
         /*
          * grants contains the granted permissions in a bitmap denies contains
          * the denied permissions in a bitmap
          */
 
         /*
          * Only look to parent objects if this is not the root object and
          * everything is not granted and denied
          */
         if (recursion < 20 && !StorageClientUtils.isRoot(objectPath)) {
             recursion++;
             Map<String, int[]> parentPermissions = internalCompilePrincipals(objectType,
                     StorageClientUtils.getParentObjectPath(objectPath), recursion);
             // add the parernt privileges in
             for (Entry<String, int[]> parentPermission : parentPermissions.entrySet()) {
                 int[] thisPriv = new int[2];
                 String principal = parentPermission.getKey();
                 if (compiledPermissions.containsKey(principal)) {
                     thisPriv = compiledPermissions.get(principal);
                     LOGGER.debug("modified {} ",principal);
                 } else {
                     LOGGER.debug("creating {} ",principal);
                 }
                 int[] parentPriv = parentPermission.getValue();
 
                 /*
                  * Grant permission not denied at this level parentPriv[0] is
                  * permissions granted by the parent ~denies is permissions not
                  * denied here parentPriv[0] & ~denies is permissions granted by
                  * the parent that have not been denied here. we need to add
                  * those to things granted here. ie |
                  */
                 int granted = thisPriv[0] | (parentPriv[0] & ~thisPriv[1]);
                 /*
                  * Deny permissions not granted at this level
                  */
                 int denied = thisPriv[1] | (parentPriv[1] & ~thisPriv[0]);
 
                 compiledPermissions.put(principal, new int[] { granted, denied });
 
             }
         }
 
         //
         // If not denied all users and groups can read other users and
         // groups and all content can be read
         for (String principal : new String[] { Group.EVERYONE, User.ANON_USER }) {
             int[] perm = new int[2];
             if (compiledPermissions.containsKey(principal)) {
                 perm = compiledPermissions.get(principal);
             }
             if (((perm[1] & Permissions.CAN_READ.getPermission()) == 0)
                     && (Security.ZONE_AUTHORIZABLES.equals(objectType) || Security.ZONE_CONTENT
                             .equals(objectType))) {
                 perm[0] = perm[0] | Permissions.CAN_READ.getPermission();
                 LOGGER.debug("added Default {} ",principal);
                 compiledPermissions.put(principal, perm);
             }
         }
         compiledPermissions.put(User.ADMIN_USER, new int[] { 0xffff, 0x0000});
         return compiledPermissions;
         // only store those permissions the match the requested set.]
         
 
     }
 
     @Override
     protected Logger getLogger() {
         return LOGGER;
     }
 
 }
