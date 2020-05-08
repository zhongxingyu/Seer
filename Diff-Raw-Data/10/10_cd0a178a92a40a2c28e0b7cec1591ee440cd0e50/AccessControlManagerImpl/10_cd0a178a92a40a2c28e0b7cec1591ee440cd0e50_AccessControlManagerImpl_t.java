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
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.lang.StringUtils;
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
 import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalTokenResolver;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalValidatorResolver;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
 import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
 import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
 import org.sakaiproject.nakamura.api.lite.authorizable.Group;
 import org.sakaiproject.nakamura.api.lite.authorizable.User;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.lite.CachingManager;
 import org.sakaiproject.nakamura.lite.storage.StorageClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 public class AccessControlManagerImpl extends CachingManager implements AccessControlManager {
 
     private static final String _SECRET_KEY = "_secretKey";
     private static final String _PATH = "_aclPath";
     private static final String _OBJECT_TYPE = "_aclType";
     public static final String _KEY = "_aclKey";
     private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlManagerImpl.class);
     private static final Set<String> PROTECTED_PROPERTIES = ImmutableSet.of(_SECRET_KEY);
     private static final Set<String> READ_ONLY_PROPERTIES = ImmutableSet.of(_SECRET_KEY, _PATH, _OBJECT_TYPE, _KEY);
     private User user;
     private String keySpace;
     private String aclColumnFamily;
     private Map<String, int[]> cache = new ConcurrentHashMap<String, int[]>();
     private boolean closed;
     private StoreListener storeListener;
     private PrincipalTokenValidator principalTokenValidator;
     private PrincipalTokenResolver principalTokenResolver;
     private SecureRandom secureRandom;
     private AuthorizableManager authorizableManager;
     private Map<String, String[]> principalCache = new ConcurrentHashMap<String, String[]>();
     private ThreadLocal<String> principalRecursionLock = new ThreadLocal<String>();
     private ThreadBoundStackReferenceCounter compilingPermissions = new ThreadBoundStackReferenceCounter();
 
     public AccessControlManagerImpl(StorageClient client, User currentUser, Configuration config,
             Map<String, CacheHolder> sharedCache, StoreListener storeListener, PrincipalValidatorResolver principalValidatorResolver) throws StorageClientException {
         super(client, sharedCache);
         this.user = currentUser;
         this.aclColumnFamily = config.getAclColumnFamily();
         this.keySpace = config.getKeySpace();
         closed = false;
         this.storeListener = storeListener;
         principalTokenValidator = new PrincipalTokenValidator(principalValidatorResolver);
         secureRandom = new SecureRandom();
     }
 
     public Map<String, Object> getAcl(String objectType, String objectPath)
             throws StorageClientException, AccessDeniedException {
         checkOpen();
         check(objectType, objectPath, Permissions.CAN_READ_ACL);
 
         String key = this.getAclKey(objectType, objectPath);
         return StorageClientUtils.getFilterMap(getCached(keySpace, aclColumnFamily, key), null, null, PROTECTED_PROPERTIES, false);
     }
 
     /**
      * Property principals are stored with keys of the form
      * _pp_<principal>@<property>@<g|d> where principal is a principal. For the
      * acl to be selected for the used of this session, they must have that
      * principal. property is the name of the property. g or d is grant or deny.
      * The value of the ACE is the bitmap for the ACE. All ACEs are selected and
      * processed to form the ACE for the user, returned as a PropertyAcl.
      */
     public PropertyAcl getPropertyAcl(String objectType, String objectPath) throws AccessDeniedException, StorageClientException {
         checkOpen();
         compilingPermissions.inc();
         try {
             String key = this.getAclKey(objectType, objectPath);
             Map<String, Object> objectAcl = getCached(keySpace, aclColumnFamily, key);
             Set<String> orderedPrincipals = Sets.newLinkedHashSet();
             {
                 String principal = user.getId();
                 if ( principal.startsWith("_") ) {
                     throw new StorageClientException("Princials may not start with _ ");
                 }
                 orderedPrincipals.add(principal);
             }
             for (String principal : getPrincipals(user) ) {
                 if ( principal.startsWith("_") ) {
                     throw new StorageClientException("Princials may not start with _ ");
                 }
                 orderedPrincipals.add(principal);
             }
             // Everyone must be the last principal to be applied
             if (!User.ANON_USER.equals(user.getId())) {
                 orderedPrincipals.add(Group.EVERYONE);
             }
             // go through each principal
             Map<String,Integer> grants = Maps.newHashMap();
             Map<String,Integer> denies = Maps.newHashMap();
             for ( String principal : orderedPrincipals) {
                 // got through each property
                 String ppk = PROPERTY_PRINCIPAL_STEM+principal;
                 for(Entry<String,Object> e : objectAcl.entrySet()) {
                     String k = e.getKey();
                     if ( k.startsWith(ppk)) {
                         String[] parts = StringUtils.split(k.substring(PROPERTY_PRINCIPAL_STEM.length()),"@");
                         String propertyName = parts[1];
                         if ( AclModification.isDeny(k)) {
                             int td = toInt(e.getValue());
                             denies.put(propertyName, toInt(denies.get(propertyName)) | td);
                         } else if ( AclModification.isGrant(k)) {
                             int tg = toInt(e.getValue());
                             grants.put(propertyName, toInt(grants.get(propertyName)) | tg);
                         }
                     }
                 }
             }
             // if the property has been granted, then that should remove the deny
             for ( Entry<String, Integer> g : grants.entrySet()) {
                 String k = g.getKey();
                 if ( denies.containsKey(k)) {
                     denies.put(k,  toInt(denies.get(k)) & ~g.getValue());
                 }
             }
             return new PropertyAcl(denies);
         } finally {
             compilingPermissions.dec();
         }
 
     }
 
 
     public Map<String, Object> getEffectiveAcl(String objectType, String objectPath)
             throws StorageClientException, AccessDeniedException {
         throw new UnsupportedOperationException("Nag someone to implement this");
     }
 
     // to sign a token we need setAcl permissions on the delegate path
     /**
      * Content Tokens activate ACEs for a user that holds the content token. The
      * token is signed by the secret key associated with the target Object/acl
      * and the token is token content item is then returned for the caller to
      * save.
      */
     public void signContentToken(Content token, String securityZone, String objectPath) throws StorageClientException, AccessDeniedException {
         checkOpen();
         check(Security.ZONE_CONTENT, objectPath, Permissions.CAN_WRITE_ACL);
         check(Security.ZONE_CONTENT, objectPath, Permissions.CAN_READ_ACL);
         String key = this.getAclKey(securityZone, objectPath);
         Map<String, Object> currentAcl = getCached(keySpace, aclColumnFamily, key);
         String secretKey = (String) currentAcl.get(_SECRET_KEY);
         principalTokenValidator.signToken(token, secretKey);
         // the caller must save the target.
     }
 
     public void setAcl(String objectType, String objectPath, AclModification[] aclModifications)
             throws StorageClientException, AccessDeniedException {
         checkOpen();
         check(objectType, objectPath, Permissions.CAN_WRITE_ACL);
         check(objectType, objectPath, Permissions.CAN_READ_ACL);
         String key = this.getAclKey(objectType, objectPath);
         Map<String, Object> currentAcl = getCached(keySpace, aclColumnFamily, key);
         if ( currentAcl == null ) {
             currentAcl = Maps.newHashMap();
         }
         // every ACL gets a secret key, which avoids doing it later with a special call
         Map<String, Object> modifications = Maps.newLinkedHashMap();
         if ( !currentAcl.containsKey(_SECRET_KEY)) {
             byte[] secretKeySeed = new byte[20];
             secureRandom.nextBytes(secretKeySeed);
             MessageDigest md;
             try {
                 md = MessageDigest.getInstance("SHA1");
                 modifications.put(_SECRET_KEY, Base64.encodeBase64URLSafeString(md.digest(secretKeySeed)));
             } catch (NoSuchAlgorithmException e) {
                 LOGGER.error(e.getMessage(),e);
             }
         }
         if ( !currentAcl.containsKey(_KEY)) {
             modifications.put(_KEY, key);
             modifications.put(_OBJECT_TYPE, objectType); // this is here to make data migration possible in the future 
             modifications.put(_PATH, objectPath); // same
         }
         for (AclModification m : aclModifications) {
             String name = m.getAceKey();
             if ( READ_ONLY_PROPERTIES.contains(name)) {
                 continue;
             }
             if (m.isRemove()) {
                 modifications.put(name, null);
             } else {
 
                 int originalbitmap = getBitMap(name, modifications, currentAcl);
                 int modifiedbitmap = m.modify(originalbitmap);
                 LOGGER.debug("Adding Modification {} {} ",name, modifiedbitmap);
                 modifications.put(name, modifiedbitmap);
                 
                 // KERN-1515
                 // We need to modify the opposite key to apply the
                 // reverse of the change we just made. Otherwise,
                 // you can end up with ACLs with contradictions, like:
                 // anonymous@g=1, anonymous@d=1
                 if (containsKey(inverseKeyOf(name), modifications, currentAcl)) {
                   // XOR gives us a mask of only the bits that changed
                   int difference = originalbitmap ^ modifiedbitmap;
                   int otherbitmap = toInt(getBitMap(inverseKeyOf(name), modifications, currentAcl));
 
                   // Zero out the bits that have been modified
                   //
                   // KERN-1887: This was originally toggling the modified bits
                   // using: "otherbitmap ^ difference", but this would
                   // incorrectly grant permissions in some cases (see JIRA
                   // issue).  To avoid inconsistencies between grant and deny
                   // lists, setting a bit in one list should unset the
                   // corresponding bit in the other.
                   int modifiedotherbitmap = otherbitmap & ~difference;
 
                   if (otherbitmap != modifiedotherbitmap) {
                       // We made a change.  Record our modification.
                       modifications.put(inverseKeyOf(name), modifiedotherbitmap);
                   }
                 }
             }
         }
         LOGGER.debug("Updating ACL {} {} ", key, modifications);
         putCached(keySpace, aclColumnFamily, key, modifications, (currentAcl == null || currentAcl.size() == 0));
         storeListener.onUpdate(objectType, objectPath,  getCurrentUserId(), "type:acl", false, null, "op:acl");
        // clear the compiled cache for this session.
        List<String> keys = Lists.newArrayList();
        for ( Entry<String,int[]> e : cache.entrySet()) {
            if (e.getKey().startsWith(key)) {
                keys.add(e.getKey());
            }
        }
        for ( String k : keys ) {
            cache.remove(k);
        }
     }
     
     private boolean containsKey(String name, Map<String, Object> map1,
             Map<String, Object> map2) {
         return map1.containsKey(name) || map2.containsKey(name);
     }
 
     private int getBitMap(String name, Map<String, Object> modifications,
             Map<String, Object> currentAcl) {
         int bm = 0;
         if ( modifications.containsKey(name)) {
             bm = toInt(modifications.get(name));
         } else {
             bm = toInt(currentAcl.get(name));
         }
         return bm;
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
         if ( compilingPermissions.isSet() ) {
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
         return objectType + ";" + objectPath;
     }
 
     public void setRequestPrincipalResolver(PrincipalTokenResolver principalTokenResolver ) {
         this.principalTokenResolver = principalTokenResolver;
     }
     public void clearRequestPrincipalResolver() {
         principalTokenResolver = null;
     }
 
     private int[] compilePermission(Authorizable authorizable, String objectType,
             String objectPath, int recursion) throws StorageClientException {
         String key = getAclKey(objectType, objectPath);
         if (user.getId().equals(authorizable.getId()) && cache.containsKey(key)) {
             return cache.get(key);
         } else {
             LOGGER.debug("Cache Miss {} [{}] ", cache, key);
         }
         try {
             // we need to allow the permissions compile to bypass access control as it needs to see everything.
             compilingPermissions.inc();
             Map<String, Object> acl = getCached(keySpace, aclColumnFamily, key);
             LOGGER.debug("ACL on {} is {} ", key, acl);
     
             int grants = 0;
             int denies = 0;
             if (acl != null) {
     
                 {
                     String principal = authorizable.getId();
                     if ( principal.startsWith("_") ) {
                         throw new StorageClientException("Princials may not start with _ ");
                     }
                     int tg = toInt(acl.get(principal
                             + AclModification.GRANTED_MARKER));
                     int td = toInt(acl
                             .get(principal + AclModification.DENIED_MARKER));
                     grants = grants | tg;
                     denies = denies | td;
                     LOGGER.debug("Added Permissions for {} g{} d{} => g{} d{}",new
                     Object[]{principal,tg,td,grants,denies});
     
                 }
                 /*
                  * Deal with any proxy principals, these override groups 
                  */
                 if (principalTokenResolver != null) {
                     Set<String> inspected = Sets.newHashSet();
                     if ( acl.containsKey(_SECRET_KEY)) {
                         String secretKey = (String) acl.get(_SECRET_KEY);
                         if ( secretKey != null ) {
                             for (Entry<String, Object> ace : acl.entrySet()) {
                                 String k = ace.getKey();
                                 LOGGER.debug("Checking {} ",k);
                                 if (k.startsWith(DYNAMIC_PRINCIPAL_STEM)) {
                                     String proxyPrincipal = AclModification.getPrincipal(k).substring(DYNAMIC_PRINCIPAL_STEM.length());
                                     if ( !inspected.contains(proxyPrincipal)) {
                                         inspected.add(proxyPrincipal);
                                         LOGGER.debug("Is Dynamic {}, checking ",k);
                                         try {
                                             // principalTokenValidators are not safe code, hence we must re-enable full access control.
                                             compilingPermissions.suspend();
                                             List<Content> proxyPrincipalTokens = principalTokenResolver.resolveTokens(proxyPrincipal);
                                             for ( Content proxyPrincipalToken : proxyPrincipalTokens ) {
                                                 if ( principalTokenValidator.validatePrincipal(proxyPrincipalToken, secretKey)) {
                                                     String pname = DYNAMIC_PRINCIPAL_STEM+proxyPrincipal;
                                                     LOGGER.debug("Has this principal {} ", proxyPrincipal);
                                                     int tg = toInt(acl.get(pname
                                                             + AclModification.GRANTED_MARKER));
                                                     int td = toInt(acl.get(pname
                                                             + AclModification.DENIED_MARKER));
                                                     grants = grants | tg;
                                                     denies = denies | td;
                                                     LOGGER.debug("Added Permissions for {} g{} d{} => g{} d{}",new
                                                             Object[]{pname, tg,td,grants,denies});
                                                     break;
                                                 }
                                             }
                                         } finally {
                                             // when done, we must resume compiling permissions where we were. 
                                             // NB, the code is re-entrant.
                                             compilingPermissions.resume();
                                         }
                                     }
                                 }
                             }
                         } else {
                             LOGGER.debug("Secret Key is null");
                         }
                     } else {
                         LOGGER.debug("No Secret Key Key ");
                     }
                 } else {
                     LOGGER.debug("No principalToken Resolver");
                 }
                 // then deal with static principals
                 for (String principal : getPrincipals(authorizable) ) {
                     if ( principal.startsWith("_") ) {
                         throw new StorageClientException("Princials may not start with _ ");
                     }
                     int tg = toInt(acl.get(principal
                             + AclModification.GRANTED_MARKER));
                     int td = toInt(acl
                             .get(principal + AclModification.DENIED_MARKER));
                     grants = grants | tg;
                     denies = denies | td;
                     LOGGER.debug("Added Permissions for {} g{} d{} => g{} d{}",new
                       Object[]{principal,tg,td,grants,denies});
                 }
     
                 // Everyone must be the last principal to be applied
                 if (!User.ANON_USER.equals(authorizable.getId())) {
                     // all users except anon are in the group everyone, by default
                     // but only if not already denied or granted by a more specific
                     // permission.
                     int tg = (toInt(acl.get(Group.EVERYONE
                             + AclModification.GRANTED_MARKER)) & ~denies);
                     int td = (toInt(acl.get(Group.EVERYONE
                             + AclModification.DENIED_MARKER)) & ~grants);
                     grants = grants | tg;
                     denies = denies | td;
                     LOGGER.debug("Added Permissions for {} g{} d{} => g{} d{}",new
                             Object[]{Group.EVERYONE,tg,td,grants,denies});
     
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
                     LOGGER.debug("Default Read Permission set {} {} ",key,denied);
                 } else {
                     LOGGER.debug("Default Read has been denied {} {} ",key,
                      denied);
                 }
                 LOGGER.debug("Permissions on {} for {} is {} {} ",new
                    Object[]{key,user.getId(),granted,denied});
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
         } finally {
             // decrement the counter from here.
             compilingPermissions.dec();
         }
     }
 
 
     private String[] getPrincipals(final Authorizable authorizable) {
         String k = authorizable.getId();
         if (principalCache.containsKey(k)) {
             return principalCache.get(k);
         }
         Set<String> memberOfSet = Sets.newHashSet(authorizable.getPrincipals());
         if ( authorizableManager != null ) {
             // membership resolution is possible, but we had better turn off recursion
             if ( principalRecursionLock.get() == null ) {
                 principalRecursionLock.set("l");
                 try {
                     for ( Iterator<Group> gi = authorizable.memberOf(authorizableManager); gi.hasNext(); ) {
                         memberOfSet.add(gi.next().getId());
                     }
                 } finally {
                     principalRecursionLock.set(null);
                 }
             }
         }
         memberOfSet.remove(Group.EVERYONE);
         String[] m = memberOfSet.toArray(new String[memberOfSet.size()]);
         principalCache.put(k, m);
         return m;
     }
 
 
     private int toInt(Object object) {
         if ( object instanceof Integer ) {
             return ((Integer) object).intValue();
         }
         LOGGER.debug("Bitmap Not Present");
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
         if ( compilingPermissions.isSet() ) {
             return true;
         }
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
 
     public void setAuthorizableManager(AuthorizableManager authorizableManager) {
         this.authorizableManager = authorizableManager;
     }
 
     
 
 
 }
