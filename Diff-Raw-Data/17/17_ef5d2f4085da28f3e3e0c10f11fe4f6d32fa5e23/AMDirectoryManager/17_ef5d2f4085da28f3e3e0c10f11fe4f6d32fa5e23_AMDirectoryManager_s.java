 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: AMDirectoryManager.java,v 1.1 2005-11-01 00:29:00 arvindp Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.iplanet.am.sdk;
 
 import java.security.AccessController;
 import java.security.ProviderException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.security.auth.callback.Callback;
 
 import netscape.ldap.util.DN;
 
 import com.iplanet.am.util.Cache;
 import com.iplanet.am.util.Debug;
 import com.iplanet.am.util.OrderedSet;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.naming.WebtopNaming;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.ums.SearchControl;
 import com.sun.identity.common.CaseInsensitiveHashMap;
 import com.sun.identity.common.CaseInsensitiveHashSet;
 import com.sun.identity.delegation.DelegationEvaluator;
 import com.sun.identity.delegation.DelegationException;
 import com.sun.identity.delegation.DelegationPermission;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.IdConstants;
 import com.sun.identity.idm.IdOperation;
 import com.sun.identity.idm.IdRepo;
 import com.sun.identity.idm.IdRepoBundle;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdRepoFatalException;
 import com.sun.identity.idm.IdRepoServiceListener;
 import com.sun.identity.idm.IdRepoUnsupportedOpException;
 import com.sun.identity.idm.IdSearchControl;
 import com.sun.identity.idm.IdSearchOpModifier;
 import com.sun.identity.idm.IdSearchResults;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.idm.IdUtils;
 import com.sun.identity.idm.RepoSearchResults;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.sm.DNMapper;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.SchemaType;
 import com.sun.identity.sm.ServiceConfig;
 import com.sun.identity.sm.ServiceConfigManager;
 import com.sun.identity.sm.ServiceManager;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchemaManager;
 
 /**
  * A class which manages all the major Directory related operations. Contains
  * functionality to create, delete and manange directory entries.
  * 
  * This class should not be used directly when caching mode is on.
  * 
  */
 public class AMDirectoryManager implements AMConstants {
 
     protected static Debug debug = AMCommonUtils.debug;
 
     protected static Debug idd = Debug.getInstance("amIdm");
 
     protected static boolean isUserPluginInitialized = false; // first time
 
     // flag
 
     private final static String SDK_PACKAGE_NAME_PROPERTY = 
         "com.iplanet.am.sdk.package";
 
     private final static String DEFAULT_SDK_PACKAGE = "com.iplanet.am.sdk.ldap";
 
     private final static String SDK_REMOTE_PACKAGE = 
         "com.iplanet.am.sdk.remote";
 
     private final static String directoryMgrClass = "DirectoryManager";
 
     private final static String dcTreeClass = "DCTree";
 
     private final static String complianceClass = "Compliance";
 
     private final static String DELEGATION_ATTRS_NAME = "attributes";
 
     protected String sdkPackageName;
 
     protected DirectoryManagerInterface dMgr;
 
     protected DCTreeInterface dcTree;
 
     protected ComplianceInterface compliance;
 
     protected static Map idRepoMap = new HashMap();
 
     protected static Map namingAttrMap = new HashMap();
 
     // A handle to Singleton instance
     private static AMDirectoryManager instance = null;
 
     private static boolean remote = false;
 
     private static HashSet readAction = new HashSet(2);
 
     private static HashSet writeAction = new HashSet(2);
 
     // SMS objects
     protected static ServiceSchemaManager idRepoServiceSchemaManager;
 
     protected static ServiceSchema idRepoSubSchema;
 
     protected static Set idRepoPlugins;
 
     /**
      * Ideally this constructor should be private, since we are extending this
      * class, it needs to be protected. This constructor should not be used to
      * create an instance of this class.
      * 
      * <p>
      * Use <code>AMDirectoryWrapper.getInstance()</code> to create an
      * instance.
      */
     protected AMDirectoryManager() {
         readAction.add("READ");
         writeAction.add("MODIFY");
         try {
             sdkPackageName = SystemProperties.get(SDK_PACKAGE_NAME_PROPERTY,
                     DEFAULT_SDK_PACKAGE);
 
             Class dsClass = Class.forName(sdkPackageName + "."
                     + directoryMgrClass);
             Class dcClass = Class.forName(sdkPackageName + "." + dcTreeClass);
             Class cClass = Class
                     .forName(sdkPackageName + "." + complianceClass);
             dMgr = (DirectoryManagerInterface) dsClass.newInstance();
             dcTree = (DCTreeInterface) dcClass.newInstance();
             compliance = (ComplianceInterface) cClass.newInstance();
             if (sdkPackageName.equals(SDK_REMOTE_PACKAGE)) {
                 remote = true;
             }
         } catch (ClassNotFoundException cfe) {
             // In client SDK deployment it should be fine not to have
             // com.iplanet.am.sdk.ldap.DirectoryManager
             if (WebtopNaming.isServerMode()) {
                 debug.error("SDK Package classes not found: " + sdkPackageName,
                         cfe);
             } else if (debug.messageEnabled()) {
                 // Debug it as a message
                 debug.message("Default SDK Package not found: "
                         + sdkPackageName);
             }
         } catch (Exception e) {
             // In client SDK deployment it should be fine
             if (WebtopNaming.isServerMode()) {
                 debug.error("AMDirectoryManager: exception in "
                         + "instantiation of directory access classes", e);
             } else if (debug.messageEnabled()) {
                 debug.message("AMDirectoryManager: exception in "
                         + "instantiation of directory access classes", e);
             }
         }
         if (dMgr == null || dcTree == null || compliance == null) {
             if (debug.warningEnabled()) {
                 debug.warning("AMDirectoryManager: Configured/Default "
                         + "AMSDK DirectoryManager failed, "
                         + "trying JAXPRC or LDAP");
             }
             try {
                 if (sdkPackageName.equals(DEFAULT_SDK_PACKAGE)) {
                     Class dsClass = Class.forName(SDK_REMOTE_PACKAGE + "."
                             + directoryMgrClass);
                     Class dcClass = Class.forName(SDK_REMOTE_PACKAGE + "."
                             + dcTreeClass);
                     Class cClass = Class.forName(SDK_REMOTE_PACKAGE + "."
                             + complianceClass);
                     dMgr = (DirectoryManagerInterface) dsClass.newInstance();
                     dcTree = (DCTreeInterface) dcClass.newInstance();
                     compliance = (ComplianceInterface) cClass.newInstance();
                     remote = true;
                     if (debug.warningEnabled()) {
                         debug.warning("AMDirectoryManager: Using default "
                                 + "JAX RPC implementation");
                     }
                 } else {
                     Class dsClass = Class.forName(DEFAULT_SDK_PACKAGE + "."
                             + directoryMgrClass);
                     Class dcClass = Class.forName(DEFAULT_SDK_PACKAGE + "."
                             + dcTreeClass);
                     Class cClass = Class.forName(DEFAULT_SDK_PACKAGE + "."
                             + complianceClass);
                     dMgr = (DirectoryManagerInterface) dsClass.newInstance();
                     dcTree = (DCTreeInterface) dcClass.newInstance();
                     compliance = (ComplianceInterface) cClass.newInstance();
                     remote = false;
                     if (debug.warningEnabled()) {
                         debug.warning("AMDirectoryManager: Using default "
                                 + "LDAP implementation");
                     }
                 }
             } catch (Exception e) {
                 debug.error("Unable to initialize the data access layer", e);
                 throw new ProviderException(AMSDKBundle.getString("300"));
             }
         }
         if (!remote) {
             // On the server side, configure and set up a service
             // listener for listening to changes made to plugin
             // configurations.
             if (debug.messageEnabled()) {
                 debug.message("AMDirectoryManager: "
                         + " In server mode..setting up ServiceListener");
             }
             SSOToken stoken = (SSOToken) AccessController
                     .doPrivileged(AdminTokenAction.getInstance());
             try {
                 IdRepoServiceListener sListener = new IdRepoServiceListener();
                 ServiceConfigManager ssm = new ServiceConfigManager(stoken,
                         IdConstants.REPO_SERVICE, "1.0");
                 ssm.addListener(sListener);
 
                 // Initialize schema objects
                 idRepoServiceSchemaManager = new ServiceSchemaManager(stoken,
                         IdConstants.REPO_SERVICE, "1.0");
                 idRepoSubSchema = idRepoServiceSchemaManager
                         .getOrganizationSchema();
                 idRepoPlugins = idRepoSubSchema.getSubSchemaNames();
                 idRepoServiceSchemaManager.addListener(sListener);
 
             } catch (SMSException smse) {
                 if (debug.warningEnabled()) {
                     debug.warning("AMDirectoryManager: "
                             + "Unable to set up a service listener for IdRepo",
                             smse);
                 }
             } catch (SSOException ssoe) {
                 debug.error("AMDirectoryManager: "
                         + "Unable to set up a service listener for IdRepo",
                         ssoe);
             }
         }
     }
 
     protected static synchronized AMDirectoryManager getInstance() {
         if (instance == null) {
             debug.message("AMDirectoryManager.getInstance(): Creating a new "
                     + "Instance of AMDirectoryManager()");
             instance = new AMDirectoryManager();
         }
         return instance;
     }
 
     protected String getEntryNotFoundMsgID(int objectType) {
         switch (objectType) {
         case AMObject.ROLE:
         case AMObject.MANAGED_ROLE:
         case AMObject.FILTERED_ROLE:
             return "465";
         case AMObject.GROUP:
         case AMObject.DYNAMIC_GROUP:
         case AMObject.STATIC_GROUP:
         case AMObject.ASSIGNABLE_DYNAMIC_GROUP:
             return "466";
         case AMObject.ORGANIZATION:
             return "467";
         case AMObject.USER:
             return "468";
         case AMObject.ORGANIZATIONAL_UNIT:
             return "469";
         case AMObject.PEOPLE_CONTAINER:
             return "470";
         case AMObject.GROUP_CONTAINER:
             return "471";
         default:
             return "461";
         }
     }
 
     /**
      * <b>NOTE: </b> This method is declared in this class only for its subclass
      * This is just a Dummy declaration, so that it can be overridden in its
      * exended class. Ideally this method should be declared abstract.
      * 
      * <p>
      * This method will be called by <code>AMIdRepoListener</code> This method
      * will update the cache by removing all the entires which are affected as a
      * result of an event notification caused because of
      * changes/deletions/renaming of entries with and without aci's.
      * 
      * <p>
      * NOTE: The event could have been caused either by changes to an aci entry
      * or a costemplate or a cosdefinition or changes to a normal entry
      * 
      * @param dn
      *            name of entity being modified
      * @param eventType
      *            type of modification
      * @param cosType
      *            true if it is cos related. false otherwise
      * @param aciChange
      *            true if it is aci related. false otherwise
      * @param attrNames
      *            Set of attribute Names which should be removed from the
      *            CacheEntry in the case of COS change
      */
     protected void dirtyCache(Cache thisCache, String dn, int eventType,
             boolean cosType, boolean aciChange, Set attrNames) {
         // DO Nothing. To be implemented by cache manager
     }
 
     public synchronized void clearCache(Cache thisCache) {
         // do nothing
     }
 
     /**
      * Mechamism to register for change notificaions. This method must be
      * extended by plugins to register for event notifications.
      */
     public void addListener(SSOToken token, AMObjectListener listener)
             throws AMEventManagerException {
         // Add listner to DirectoryManagerInterface plugin
         if (dMgr != null) {
             dMgr.addListener(token, listener, null);
             if (debug.messageEnabled()) {
                 debug.message("AMDirectoryManager:addListner: Added Listeners");
             }
         } else if (debug.warningEnabled()) {
             debug.warning("AMDirectoryManager:addListner: Unable to add "
                     + "listener -- no DirectoryManagerInterface "
                     + "plugin configured");
         }
     }
 
     // *************************************************************************
     // All protected methods related to DS Operations.
     // *************************************************************************
     /**
      * Checks if the entry exists in the directory.
      * 
      * @param token
      *            a valid SSOToken
      * @param entryDN
      *            The DN of the entry that needs to be checked
      * @return true if the entryDN exists in the directory, false otherwise
      */
     public boolean doesEntryExists(SSOToken token, String entryDN) {
         return dMgr.doesEntryExists(token, entryDN);
     }
 
     /**
      * Gets the type of the object given its DN.
      * 
      * @param SSOToken
      *            token a valid SSOToken
      * @param dn
      *            DN of the object whose type is to be known.
      * 
      * @throws AMException
      *             if the data store is unavailable or if the object type is
      *             unknown
      * @throws SSOException
      *             if ssoToken is invalid or expired.
      */
     public int getObjectType(SSOToken token, String dn) throws AMException,
             SSOException {
 
         return dMgr.getObjectType(token, dn);
     }
 
     /**
      * Returns type of the object given the DN and its' cached attributes. If
      * the cached attributes have objectclasses, a directory lookup would not be
      * required.
      */
     public int getObjectType(SSOToken token, String dn, Map cachedAttributes)
             throws AMException, SSOException {
         return dMgr.getObjectType(token, dn, cachedAttributes);
     }
 
     /**
      * Gets the attributes for this entryDN from the corresponding DC Tree node.
      * The attributes are fetched only for Organization entries in DC tree mode.
      * 
      * @param token
      *            a valid SSOToken
      * @param po
      *            the PersistentObject
      * @return an AttrSet of values or null if not found
      * @throws AMException
      *             if error encountered in fetching the DC node attributes.
      */
     public Map getDCTreeAttributes(SSOToken token, String entryDN,
             Set attrNames, boolean byteValues, int objectType)
             throws AMException, SSOException {
         return dMgr.getDCTreeAttributes(token, entryDN, attrNames, byteValues,
                 objectType);
     }
 
     public Map getAttributes(SSOToken token, String entryDN, int profileType)
             throws AMException, SSOException {
         // Since realm operations could be performed by users who
         // might not have directory ACIs to read, need to check with
         // delegation and use "adminToken" for read/search operations
         if (!remote && checkRealmPermission(token, entryDN, readAction)) {
             token = (SSOToken) AccessController.doPrivileged(AdminTokenAction
                     .getInstance());
         }
         return dMgr.getAttributes(token, entryDN, profileType);
     }
 
     public Map getAttributes(SSOToken token, String entryDN, Set attrNames,
             int profileType) throws AMException, SSOException {
         // Since realm operations could be performed by users who
         // might not have directory ACIs to read, need to check with
         // delegation and use "adminToken" for read/search operations
         if (!remote && checkRealmPermission(token, entryDN, readAction)) {
             token = (SSOToken) AccessController.doPrivileged(AdminTokenAction
                     .getInstance());
         }
         return dMgr.getAttributes(token, entryDN, attrNames, profileType);
     }
 
     public Map getAttributesByteValues(SSOToken token, String entryDN,
             int profileType) throws AMException, SSOException {
         return dMgr.getAttributesByteValues(token, entryDN, profileType);
     }
 
     public Map getAttributesByteValues(SSOToken token, String entryDN,
             Set attrNames, int profileType) throws AMException, SSOException {
         return dMgr.getAttributesByteValues(token, entryDN, attrNames,
                 profileType);
     }
 
     /**
      * Gets all attributes corresponding to the entryDN. This method obtains the
      * DC Tree node attributes and also performs compliance related verification
      * checks in compliance mode. Note: In compliance mode you can skip the
      * compliance checks by setting ignoreCompliance to "false".
      * 
      * @param token
      *            a valid SSOToken
      * @param entryDN
      *            the DN of the entry whose attributes need to retrieved
      * @param ignoreCompliance
      *            a boolean value specificying if compliance related entries
      *            need to ignored or not. Ignored if true.
      * @return a Map containing attribute names as keys and Set of values
      *         corresponding to each key.
      * @throws AMException
      *             if an error is encountered in fetching the attributes
      */
     public Map getAttributes(SSOToken token, String entryDN,
             boolean ignoreCompliance, boolean byteValues, int profileType)
             throws AMException, SSOException {
         // Since realm operations could be performed by users who
         // might not have directory ACIs to read, need to check with
         // delegation and use "adminToken" for read/search operations
         if (!remote && checkRealmPermission(token, entryDN, readAction)) {
             token = (SSOToken) AccessController.doPrivileged(AdminTokenAction
                     .getInstance());
         }
         return dMgr.getAttributes(token, entryDN, ignoreCompliance, byteValues,
                 profileType);
     }
 
     /**
      * Gets the specific attributes corresponding to the entryDN. This method
      * obtains the DC Tree node attributes and also performs compliance related
      * verification checks in compliance mode. Note: In compliance mode you can
      * skip the compliance checks by setting ignoreCompliance to "false".
      * 
      * @param token
      *            a valid SSOToken
      * @param entryDN
      *            the DN of the entry whose attributes need to retrieved
      * @param attrNames
      *            a Set of names of the attributes that need to be retrieved.
      *            The attrNames should not be null.
      * @param ignoreCompliance
      *            a boolean value specificying if compliance related entries
      *            need to ignored or not. Ignored if true.
      * @return a Map containing attribute names as keys and Set of values
      *         corresponding to each key.
      * @throws AMException
      *             if an error is encountered in fetching the attributes
      */
     public Map getAttributes(SSOToken token, String entryDN, Set attrNames,
             boolean ignoreCompliance, boolean byteValues, int profileType)
             throws AMException, SSOException {
         // Since realm operations could be performed by users who
         // might not have directory ACIs to read, need to check with
         // delegation and use "adminToken" for read/search operations
         if (!remote && checkRealmPermission(token, entryDN, readAction)) {
             token = (SSOToken) AccessController.doPrivileged(AdminTokenAction
                     .getInstance());
         }
         return dMgr.getAttributes(token, entryDN, attrNames, ignoreCompliance,
                 byteValues, profileType);
     }
 
     public String getOrgSearchFilter(String entryDN) {
         // The search filter calls here should return a global search
         // filter (not default) if a search template cannot be found for this
         // entryDN. It is a hack as entryDN may not be orgDN, but right now
         // only solution.
         String orgSearchFilter = AMSearchFilterManager.getSearchFilter(
                 AMObject.ORGANIZATION, entryDN, null, true);
         String orgUnitSearchFilter = AMSearchFilterManager.getSearchFilter(
                 AMObject.ORGANIZATIONAL_UNIT, entryDN, null, true);
 
         StringBuffer sb = new StringBuffer();
         sb.append("(|").append(orgSearchFilter).append(orgUnitSearchFilter);
         sb.append(")");
         return sb.toString();
     }
 
     /**
      * Gets the Organization DN for the specified entryDN. If the entry itself
      * is an org, then same DN is returned.
      * <p>
      * <b>NOTE: </b> This method will involve serveral directory searches, hence
      * be cautious of Performance hit
      * 
      * @param token
      *            a valid SSOToken
      * @param entryDN
      *            the entry whose parent Organization is to be obtained
      * @return the DN String of the parent Organization
      * @throws AMException
      *             if an error occured while obtaining the parent Organization
      */
     public String getOrganizationDN(SSOToken token, String entryDN)
             throws AMException {
         return dMgr.getOrganizationDN(token, entryDN);
     }
 
     /**
      * Gets the Organization DN for the specified entryDN. If the entry itself
      * is an org, then same DN is returned.
      * 
      * @param token
      *            a valid SSOToken
      * @param entryDN
      *            the entry whose parent Organization is to be obtained
      * @param childDN
      *            the immediate entry whose parent Organization is to be
      *            obtained
      * @return the DN String of the parent Organization
      * @throws AMException
      *             if an error occured while obtaining the parent Organization
      */
     public String verifyAndGetOrgDN(SSOToken token, String entryDN,
             String childDN) throws AMException {
         return dMgr.verifyAndGetOrgDN(token, entryDN, childDN);
     }
 
     /**
      * Returns attributes from an external data store.
      * 
      * @param token
      *            Single sign on token of user
      * @param entryDN
      *            DN of the entry user is trying to read
      * @param attrNames
      *            Set of attributes to be read
      * @param profileType
      *            Integer determining the type of profile being read
      * @return A Map of attribute-value pairs
      * @throws AMException
      *             if an error occurs when trying to read external datastore
      */
     public Map getExternalAttributes(SSOToken token, String entryDN,
             Set attrNames, int profileType) throws AMException {
         return dMgr.getExternalAttributes(token, entryDN, attrNames,
                 profileType);
     }
 
     /**
      * Adds or remove static group DN to or from member attribute
      * 'iplanet-am-static-group-dn'
      * 
      * @param token
      *            SSOToken
      * @param members
      *            set of user DN's
      * @param staticGroupDN
      *            DN of the static group
      * @param toAdd
      *            true to add, false to remove
      * @throws AMException
      *             if there is an internal problem with AM Store.
      */
     public void updateUserAttribute(SSOToken token, Set members,
             String staticGroupDN, boolean toAdd) throws AMException {
         dMgr.updateUserAttribute(token, members, staticGroupDN, toAdd);
     }
 
     /**
      * Create an entry in the Directory
      * 
      * @param token
      *            SSOToken
      * @param entryName
      *            name of the entry (naming value), e.g. "sun.com", "manager"
      * @param objectType
      *            Profile Type, ORGANIZATION, AMObject.ROLE, AMObject.USER, etc.
      * @param parentDN
      *            the parent DN
      * @param attrSet
      *            the initial attribute set for creation
      */
     public void createEntry(SSOToken token, String entryName, int objectType,
             String parentDN, Map attributes) throws AMEntryExistsException,
             AMException {
         try {
             dMgr
                     .createEntry(token, entryName, objectType, parentDN,
                             attributes);
         } catch (AMException ame) {
             if (!remote && ame.getErrorCode().equals("460")
                     && checkRealmPermission(token, parentDN, writeAction)) {
                 // Permission denied could be because of directory ACIs
                 // Check with delegation and perform action as admin
                 SSOToken adminToken = (SSOToken) AccessController
                         .doPrivileged(AdminTokenAction.getInstance());
                 dMgr.createEntry(adminToken, entryName, objectType, parentDN,
                         attributes);
             } else {
                 throw (ame);
             }
         }
     }
 
     /**
      * Remove an entry from the directory.
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            dn of the profile to be removed
      * @param objectType
      *            profile type
      * @param recursive
      *            if true, remove all sub entries & the object
      * @param softDelete
      *            Used to let pre/post callback plugins know that this delete is
      *            either a soft delete (marked for deletion) or a purge/hard
      *            delete itself, otherwise, remove the object only
      */
     public void removeEntry(SSOToken token, String entryDN, int objectType,
             boolean recursive, boolean softDelete) throws AMException,
             SSOException {
         try {
             dMgr.removeEntry(token, entryDN, objectType, recursive, softDelete);
         } catch (AMException ame) {
             if (!remote && ame.getErrorCode().equals("460")
                     && checkRealmPermission(token, entryDN, writeAction)) {
                 // Permission denied could be because of directory ACIs
                 // Check with delegation and perform action as admin
                 SSOToken adminToken = (SSOToken) AccessController
                         .doPrivileged(AdminTokenAction.getInstance());
                 dMgr.removeEntry(adminToken, entryDN, objectType, recursive,
                         softDelete);
             } else {
                 throw (ame);
             }
         }
     }
 
     /**
      * Remove group admin role
      * 
      * @param token
      *            SSOToken of the caller
      * @param dn
      *            group DN
      * @param recursive
      *            true to delete all admin roles for all sub groups or sub
      *            people container
      */
     public void removeAdminRole(SSOToken token, String dn, boolean recursive)
             throws SSOException, AMException {
         dMgr.removeAdminRole(token, dn, recursive);
     }
 
     /**
      * Searches the Directory
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the entry to start the search with
      * @param searchFilter
      *            search filter
      * @param searchScope
      *            search scope, BASE, ONELEVEL or SUBTREE
      * @return Set set of matching DNs
      */
     public Set search(SSOToken token, String entryDN, String searchFilter,
             int searchScope) throws AMException {
         // Since realm operations could be performed by users who
         // might not have directory ACIs to read, need to check with
         // delegation and use "adminToken" for read/search operations
         if (!remote && checkRealmPermission(token, entryDN, readAction)) {
             token = (SSOToken) AccessController.doPrivileged(AdminTokenAction
                     .getInstance());
         }
         return (dMgr.search(token, entryDN, searchFilter, searchScope));
     }
 
     // RENAME from searchUsingSearchControl => search()
     /**
      * Search the Directory
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the entry to start the search with
      * @param searchFilter
      *            search filter
      * @param SearchControl
      *            search control defining the VLV indexes and search scope
      * @return Set set of matching DNs
      */
     public AMSearchResults search(SSOToken token, String entryDN,
             String searchFilter, SearchControl searchControl,
             String attrNames[]) throws AMException {
         // Since realm operations could be performed by users who
         // might not have directory ACIs to read, need to check with
         // delegation and use "adminToken" for read/search operations
         if (!remote && checkRealmPermission(token, entryDN, readAction)) {
             token = (SSOToken) AccessController.doPrivileged(AdminTokenAction
                     .getInstance());
         }
         return dMgr.search(token, entryDN, searchFilter, searchControl,
                 attrNames);
     }
 
     /**
      * Get members for roles, dynamic group or static group
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the role or group
      * @param objectType
      *            objectType of the target object, AMObject.ROLE or
      *            AMObject.GROUP
      * @return Set Member DNs
      */
     public Set getMembers(SSOToken token, String entryDN, int objectType)
             throws AMException {
         return dMgr.getMembers(token, entryDN, objectType);
     }
 
     /**
      * Renames an entry. Currently used for only user renaming
      * 
      * @param token
      *            the sso token
      * @param objectType
      *            the type of entry
      * @param entryDN
      *            the entry DN
      * @param newName
      *            the new name (i.e., if RDN is cn=John, the value passed should
      *            be "John"
      * @param deleteOldName
      *            if true the old name is deleted otherwise it is retained.
      * @return new <code>DN</code> of the renamed entry
      * @throws AMException
      *             if the operation was not successful
      */
     public String renameEntry(SSOToken token, int objectType, String entryDN,
             String newName, boolean deleteOldName) throws AMException {
         return dMgr.renameEntry(token, objectType, entryDN, newName,
                 deleteOldName);
     }
 
     /**
      * Method Set the attributes of an entry.
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the profile whose template is to be set
      * @param objectType
      *            profile type
      * @param attrSet
      *            attributes to be set
      */
     public void setAttributes(SSOToken token, String entryDN, int objectType,
             Map stringAttributes, Map byteAttributes, boolean isAdd)
             throws AMException, SSOException {
         try {
             dMgr.setAttributes(token, entryDN, objectType, stringAttributes,
                     byteAttributes, isAdd);
         } catch (AMException ame) {
             if (!remote && ame.getErrorCode().equals("460")
                     && checkRealmPermission(token, entryDN, writeAction)) {
                 // Permission denied could be because of directory ACIs
                 // Check with delegation and perform action as admin
                 SSOToken adminToken = (SSOToken) AccessController
                         .doPrivileged(AdminTokenAction.getInstance());
                 dMgr.setAttributes(adminToken, entryDN, objectType,
                         stringAttributes, byteAttributes, isAdd);
             } else {
                 throw (ame);
             }
         }
     }
 
     // ###### Group and Role APIs
     /**
      * 
      * @param token
      * @param entryDN
      * @param profileType
      * @throws AMException
      * @throws SSOException
      */
     public String[] getGroupFilterAndScope(SSOToken token, String entryDN,
             int profileType) throws AMException, SSOException {
         return dMgr.getGroupFilterAndScope(token, entryDN, profileType);
     }
 
     public void setGroupFilter(SSOToken token, String entryDN, String filter)
             throws AMException, SSOException {
         dMgr.setGroupFilter(token, entryDN, filter);
     }
 
     /**
      * Modify member ship for role or static group
      * 
      * @param token
      *            SSOToken
      * @param members
      *            Set of member DN to be operated
      * @param target
      *            DN of the target object to add the member
      * @param type
      *            type of the target object, AMObject.ROLE or AMObject.GROUP
      * @param operation
      *            type of operation, ADD_MEMBER or REMOVE_MEMBER
      * @param updateUserEntry
      *            If true then call the updatUserAttribute when modifying group
      *            membership
      */
     public void modifyMemberShip(SSOToken token, Set members, String target,
             int type, int operation) throws AMException {
         dMgr.modifyMemberShip(token, members, target, type, operation);
     }
 
     // *************************************************************************
     // Service Related Functionality
     // *************************************************************************
     /**
      * Get registered services for an organization
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the org
      * @return Set set of service names
      */
     public Set getRegisteredServiceNames(SSOToken token, String entryDN)
             throws AMException {
         // Since realm operations could be performed by users who
         // might not have directory ACIs to read, need to check with
         // delegation and use "adminToken" for read/search operations
         if (!remote && checkRealmPermission(token, entryDN, readAction)) {
             token = (SSOToken) AccessController.doPrivileged(AdminTokenAction
                     .getInstance());
         }
         return dMgr.getRegisteredServiceNames(token, entryDN);
     }
 
     /**
      * Register a service for an org or org unit policy to a profile
      * 
      * @param token
      *            token
      * @param orgDN
      *            DN of the org
      * @param serviceName
      *            Service Name
      */
     public void registerService(SSOToken token, String orgDN, 
             String serviceName) throws AMException, SSOException {
         try {
             dMgr.registerService(token, orgDN, serviceName);
         } catch (AMException ame) {
             if (!remote && ame.getErrorCode().equals("460")
                     && checkRealmPermission(token, orgDN, writeAction)) {
                 // Permission denied could be because of directory ACIs
                 // Check with delegation and perform action as admin
                 SSOToken adminToken = (SSOToken) AccessController
                         .doPrivileged(AdminTokenAction.getInstance());
                 dMgr.registerService(adminToken, orgDN, serviceName);
             } else {
                 throw (ame);
             }
         }
     }
 
     // Rename from removeService to unRegisterService
     /**
      * Un register service for a AMro profile.
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the profile whose service is to be removed
      * @param objectType
      *            profile type
      * @param serviceName
      *            Service Name
      * @param template
      *            AMTemplate
      * @param type
      *            Template type
      */
     public void unRegisterService(SSOToken token, String entryDN,
             int objectType, String serviceName, AMTemplate template, int type)
             throws AMException {
         try {
             dMgr.unRegisterService(token, entryDN, objectType, serviceName,
                     template, type);
         } catch (AMException ame) {
             if (!remote && ame.getErrorCode().equals("460")
                     && checkRealmPermission(token, entryDN, writeAction)) {
                 // Permission denied could be because of directory ACIs
                 // Check with delegation and perform action as admin
                 SSOToken adminToken = (SSOToken) AccessController
                         .doPrivileged(AdminTokenAction.getInstance());
                 dMgr.unRegisterService(adminToken, entryDN, objectType,
                         serviceName, template, type);
             } else {
                 throw (ame);
             }
         }
     }
 
     // Rename from removeService to unRegisterService
     /**
      * Un register service for a AMro profile.
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the profile whose service is to be removed
      * @param objectType
      *            profile type
      * @param serviceName
      *            Service Name
      * @param type
      *            Template type
      */
     public void unRegisterService(SSOToken token, String entryDN,
             int objectType, String serviceName, int type) throws AMException {
         try {
             dMgr.unRegisterService(token, entryDN, objectType, serviceName,
                     null, type);
         } catch (AMException ame) {
             if (!remote && ame.getErrorCode().equals("460")
                     && checkRealmPermission(token, entryDN, writeAction)) {
                 // Permission denied could be because of directory ACIs
                 // Check with delegation and perform action as admin
                 SSOToken adminToken = (SSOToken) AccessController
                         .doPrivileged(AdminTokenAction.getInstance());
                 dMgr.unRegisterService(adminToken, entryDN, objectType,
                         serviceName, null, type);
             } else {
                 throw (ame);
             }
         }
     }
 
     /**
      * Get the AMTemplate DN (COSTemplateDN)
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the profile whose template is to be set
      * @param serviceName
      *            Service Name
      * @param type
      *            the template type, AMTemplate.DYNAMIC_TEMPLATE
      * @return String DN of the AMTemplate
      */
     public String getAMTemplateDN(SSOToken token, String entryDN,
             int objectType, String serviceName, int type) throws AMException {
         return dMgr.getAMTemplateDN(token, entryDN, objectType, serviceName,
                 type);
     }
 
     /**
      * Create an AMTemplate (COSTemplate)
      * 
      * @param token
      *            token
      * @param entryDN
      *            DN of the profile whose template is to be set
      * @param serviceName
      *            Service Name
      * @param attrSet
      *            attributes to be set
      * @param priority
      *            template priority
      * @return String DN of the newly created template
      */
     public String createAMTemplate(SSOToken token, String entryDN,
             int objectType, String serviceName, Map attributes, int priority)
             throws AMException {
         return dMgr.createAMTemplate(token, entryDN, objectType, serviceName,
                 attributes, priority);
     }
 
     /**
      * 
      * @param objectclass
      * @return The set of attribute names (both required and optional) for this
      *         objectclass
      */
     public Set getAttributesForSchema(String objectclass) {
         return dMgr.getAttributesForSchema(objectclass);
     }
 
     /**
      * 
      * @param token
      * @return The top level containers this user manages based on its'
      *         administrative roles (if any)
      * @throws AMException
      *             if a datastore access fails
      * @throws SSOException
      *             if user's single sign on token is invalid.
      */
     public Set getTopLevelContainers(SSOToken token) throws AMException,
             SSOException {
         return dMgr.getTopLevelContainers(token);
     }
 
     /*
      * (non-Javadoc)
      */
     public AMIdentity create(SSOToken token, IdType type, String name,
             Map attrMap, String amOrgName) throws IdRepoException, SSOException 
     {
         IdRepoException origEx = null;
         if (remote) {
             return dMgr.create(token, type, name, attrMap, amOrgName);
         }
         // First get the list of plugins that support the create operation.
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, attrMap.keySet(),
                 IdOperation.CREATE, type);
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         String amsdkdn = null;
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.CREATE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
 
                 Map cMap = idRepo.getConfiguration(); // do stuff to map attr
                 // names.
                 attrMap = mapAttributeNames(attrMap, cMap);
                 String representation = idRepo.create(token, type, name,
                         attrMap);
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)) {
                     amsdkdn = representation;
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to create identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + ":: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("Create: Fatal Exception", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to create identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         AMIdentity id = new AMIdentity(token, name, type, amOrgName, amsdkdn);
         if (noOfSuccess == 0) {
             idd.error("Unable to create identity " + type.getName() + " :: "
                     + name + "in any of the configured data stores", origEx);
             throw origEx;
         } else {
             return id;
         }
 
     }
 
     /*
      * (non-Javadoc)
      */
     public void delete(SSOToken token, IdType type, String name,
             String orgName, String amsdkDN) throws IdRepoException,
             SSOException {
         IdRepoException origEx = null;
         // First get the list of plugins that support the create operation.
         if (remote) {
             dMgr.delete(token, type, name, orgName, amsdkDN);
             return;
         }
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, orgName, name, null, IdOperation.DELETE, type);
         Set plugIns = getIdRepoPlugins(token, orgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, orgName, plugIns,
                 IdOperation.DELETE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     idRepo.delete(token, type, amsdkDN);
                 } else {
                     idRepo.delete(token, type, name);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idd.warningEnabled()) {
                     idd.warning("Unable to delete identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("Delete: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to delete identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 if (!ide.getErrorCode().equals("220") || (origEx == null)) {
                     origEx = ide;
                 }
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to delete identity " + type.getName() + " :: "
                     + name + "in any of the configured data stores", origEx);
             throw origEx;
         }
     }
 
     /**
      * Returns <code>true</code> if the data store has successfully
      * authenticated the identity with the provided credentials. In case the
      * data store requires additional credentials, the list would be returned
      * via the <code>IdRepoException</code> exception.
      * 
      * @param orgName
      *            realm name to which the identity would be authenticated
      * @param credentials
      *            Array of callback objects containing information such as
      *            username and password.
      * 
      * @return <code>true</code> if data store authenticates the identity;
      *         else <code>false</code>
      */
     public boolean authenticate(String orgName, Callback[] credentials)
             throws IdRepoException,
             com.sun.identity.authentication.spi.AuthLoginException {
         if (idd.messageEnabled()) {
             idd.message("AMDirectoryManager::authenticate called for org: "
                     + orgName);
         }
         if (remote) {
             if (idd.messageEnabled()) {
                 idd.message("AMDirectoryManager::authenticate "
                         + "not supported for remote clients");
             }
             // Not supported for remote
             return (false);
         }
         IdRepoException firstException = null;
         // Get the list of plugins and check if they support authN
         SSOToken token = (SSOToken) AccessController
                 .doPrivileged(AdminTokenAction.getInstance());
         Set plugins = getIdRepoPlugins(token, orgName);
         Set cPlugins = getAllConfiguredPlugins(token, orgName, plugins);
         for (Iterator items = cPlugins.iterator(); items.hasNext();) {
             IdRepo idRepo = (IdRepo) items.next();
             if (idRepo.supportsAuthentication()) {
                 if (idd.messageEnabled()) {
                     idd.message("AuthN to " + idRepo.getClass().getName()
                             + " in org: " + orgName);
                 }
                 try {
                     if (idRepo.authenticate(credentials)) {
                         // Successfully authenticated
                         if (idd.messageEnabled()) {
                             idd.message("AuthN success for "
                                     + idRepo.getClass().getName());
                         }
                         return (true);
                     }
                 } catch (IdRepoException ide) {
                     // Save the exception to be thrown later if
                     // all authentication calls fail
                     if (firstException == null) {
                         firstException = ide;
                     }
                 }
             } else if (idd.messageEnabled()) {
                 idd.message("AuthN not supported by "
                         + idRepo.getClass().getName());
             }
         }
         if (firstException != null) {
             throw (firstException);
         }
         return (false);
     }
 
     /*
      * (non-Javadoc)
      */
     public Map getAttributes(SSOToken token, IdType type, String name,
             Set attrNames, String amOrgName, String amsdkDN, boolean isString)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
         if (remote) {
             return dMgr.getAttributes(token, type, name, attrNames, amOrgName,
                     amsdkDN);
         }
         // First get the list of plugins that support the create operation.
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, attrNames, IdOperation.READ,
                 type);
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         Set attrMapsSet = new HashSet();
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 Map cMap = idRepo.getConfiguration();
                 // do stuff to map attr names.
                 attrNames = mapAttributeNames(attrNames, cMap);
                 Map aMap = null;
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     if (isString) {
                         aMap = idRepo.getAttributes(token, type, amsdkDN,
                                 attrNames);
                     } else {
                         aMap = idRepo.getBinaryAttributes(token, type, name,
                                 attrNames);
                     }
                 } else {
                     if (isString) {
                         aMap = idRepo.getAttributes(token, type, name,
                                 attrNames);
                     } else {
                         aMap = idRepo.getBinaryAttributes(token, type, name,
                                 attrNames);
                     }
                 }
                 aMap = reverseMapAttributeNames(aMap, cMap);
                 attrMapsSet.add(aMap);
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idd.warningEnabled()) {
                     idd.warning("Unable to read identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("GetAttributes: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to read identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to get attributes for identity " + type.getName()
                     + "::" + name + " in any configured data store", origEx);
             throw origEx;
         } else {
             Map returnMap = combineAttrMaps(attrMapsSet, isString);
             return returnMap;
         }
     }
 
     /*
      * (non-Javadoc)
      */
     public Map getAttributes(SSOToken token, IdType type, String name,
             String amOrgName, String amsdkDN) throws IdRepoException,
             SSOException {
         IdRepoException origEx = null;
         if (remote) {
             return dMgr.getAttributes(token, type, name, amOrgName, amsdkDN);
         }
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null, IdOperation.READ, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         Set attrMapsSet = new HashSet();
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 Map cMap = idRepo.getConfiguration();
                 Map aMap = null;
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     aMap = idRepo.getAttributes(token, type, amsdkDN);
                 } else {
                     aMap = idRepo.getAttributes(token, type, name);
                 }
                 aMap = reverseMapAttributeNames(aMap, cMap);
                 attrMapsSet.add(aMap);
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to read identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("GetAttributes: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to read identity in the following "
                             + "repository" + idRepo.getClass().getName()
                             + " :: " + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to get attributes for identity " + type.getName()
                     + "::" + name + " in any configured data store", origEx);
             throw origEx;
         } else {
             Map returnMap = combineAttrMaps(attrMapsSet, true);
             return returnMap;
         }
     }
 
     /*
      * (non-Javadoc)
      */
     public Set getMembers(SSOToken token, IdType type, String name,
             String amOrgName, IdType membersType, String amsdkDN)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
         if (remote) {
             return dMgr.getMembers(token, type, name, amOrgName, membersType,
                     amsdkDN);
         }
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null, IdOperation.READ, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         Set membersSet = new HashSet();
         Set amsdkMembers = new HashSet();
         boolean amsdkIncluded = false;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 // Map cMap = idRepo.getConfiguration();
                 boolean isAMSDK = idRepo.getClass().getName().equals(
                         IdConstants.AMSDK_PLUGIN);
                 Set members = (isAMSDK && (amsdkDN != null)) ? idRepo
                         .getMembers(token, type, amsdkDN, membersType) : idRepo
                         .getMembers(token, type, name, membersType);
                 if (isAMSDK) {
                     amsdkMembers.addAll(members);
                     amsdkIncluded = true;
                 } else {
                     membersSet.add(members);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to read identity members in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("Get Members: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to read identity members in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to get members for identity " + type.getName()
                     + "::" + name + " in any configured data store", origEx);
             throw origEx;
         } else {
             Set results = combineMembers(token, membersSet, membersType,
                     amOrgName, amsdkIncluded, amsdkMembers);
             return results;
         }
     }
 
     /*
      * (non-Javadoc)
      */
     public Set getMemberships(SSOToken token, IdType type, String name,
             IdType membershipType, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
         if (remote) {
             return dMgr.getMemberships(token, type, name, membershipType,
                     amOrgName, amsdkDN);
         }
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null, IdOperation.READ, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         Set membershipsSet = new HashSet();
         Set amsdkMemberShips = new HashSet();
         boolean amsdkIncluded = false;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 // Map cMap = idRepo.getConfiguration();
                 boolean isAMSDK = idRepo.getClass().getName().equals(
                         IdConstants.AMSDK_PLUGIN);
                 Set members = (isAMSDK && (amsdkDN != null)) ? idRepo
                         .getMemberships(token, type, amsdkDN, membershipType)
                         : idRepo.getMemberships(token, type, name,
                                 membershipType);
                 if (isAMSDK) {
                     amsdkMemberShips.addAll(members);
                     amsdkIncluded = true;
                 } else {
                     membershipsSet.add(members);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to get memberships in the following "
                             + "repository" + idRepo.getClass().getName()
                             + " :: " + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("Get memberships: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to read identity in the following "
                             + "repository" + idRepo.getClass().getName(), ide);
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to get members for identity " + type.getName()
                     + "::" + name + " in any configured data store", origEx);
             throw origEx;
         } else {
             Set results = combineMembers(token, membershipsSet, membershipType,
                     amOrgName, amsdkIncluded, amsdkMemberShips);
             return results;
         }
     }
 
     /*
      * (non-Javadoc)
      */
     public boolean isExists(SSOToken token, IdType type, String name,
             String amOrgName) throws SSOException, IdRepoException {
         if (remote) {
             return dMgr.isExists(token, type, name, amOrgName);
         }
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         IdRepo idRepo;
         boolean exists = false;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             // Map cMap = idRepo.getConfiguration();
             exists = idRepo.isExists(token, type, name);
             if (exists) {
                 continue;
             }
         }
         return exists;
     }
 
     public boolean isActive(SSOToken token, IdType type, String name,
             String amOrgName, String amsdkDN) throws SSOException,
             IdRepoException {
         IdRepoException origEx = null;
 
         if (remote) {
             return dMgr.isActive(token, type, name, amOrgName, amsdkDN);
         }
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null, IdOperation.READ, type);
         // First get the list of plugins that support the create operation.
 
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         boolean active = false;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     active = idRepo.isActive(token, type, amsdkDN);
                 } else {
                     active = idRepo.isActive(token, type, name);
                 }
                 if (active) {
                     break;
                 }
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("IsActive: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to check isActive identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
 
         if (noOfSuccess == 0) {
             idd.error("Unable to check if identity is active " + type.getName()
                     + "::" + name + " in any configured data store", origEx);
             throw origEx;
         }
 
         return active;
     }
 
     /*
      * (non-Javadoc)
      */
     public void modifyMemberShip(SSOToken token, IdType type, String name,
             Set members, IdType membersType, int operation, String amOrgName)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
         if (remote) {
             dMgr.modifyMemberShip(token, type, name, members, membersType,
                     operation, amOrgName);
             return;
         }
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null, IdOperation.EDIT, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.EDIT, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 idRepo.modifyMemberShip(token, type, name, members,
                         membersType, operation);
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to modify memberships  in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("Modify membership: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.error("Unable to modify memberships in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to modify members for identity " + type.getName()
                     + "::" + name + " in any configured data store", origEx);
             throw origEx;
         }
     }
 
     /*
      * (non-Javadoc)
      */
     public void removeAttributes(SSOToken token, IdType type, String name,
             Set attrNames, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
         if (remote) {
             dMgr.removeAttributes(token, type, name, attrNames, amOrgName,
                     amsdkDN);
             return;
         }
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, attrNames, IdOperation.EDIT,
                 type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.EDIT, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 Map cMap = idRepo.getConfiguration();
                 // do stuff to map attr names.
                 attrNames = mapAttributeNames(attrNames, cMap);
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     idRepo.removeAttributes(token, type, amsdkDN, attrNames);
                 } else {
                     idRepo.removeAttributes(token, type, name, attrNames);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to modify identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("Remove attributes: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to remove attributes in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 if (!ide.getErrorCode().equals("220") || (origEx == null)) {
                     origEx = ide;
                 }
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to remove attributes  for identity "
                     + type.getName() + "::" + name
                     + " in any configured data store", origEx);
             throw origEx;
         }
     }
 
     /*
      * (non-Javadoc)
      */
     public IdSearchResults search(SSOToken token, IdType type, String pattern,
             Map avPairs, boolean recursive, int maxResults, int maxTime,
             Set returnAttrs, String amOrgName) throws IdRepoException,
             SSOException {
         IdRepoException origEx = null;
         if (remote) {
             return dMgr.search(token, type, pattern, avPairs, recursive,
                     maxResults, maxTime, returnAttrs, amOrgName);
         }
         // First get the list of plugins that support the create operation.
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, null, null, IdOperation.READ, type);
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         Object[][] amsdkResults = new Object[1][2];
         boolean amsdkIncluded = false;
         Object[][] arrayOfResult = new Object[noOfSuccess][2];
         // Set resultsSet = new HashSet();
         int iterNo = 0;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 Map cMap = idRepo.getConfiguration();
                 RepoSearchResults results = idRepo.search(token, type, pattern,
                         avPairs, recursive, maxResults, maxTime, returnAttrs);
                 // resultsSet.add(results);
 
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)) {
                     amsdkResults[0][0] = results;
                     amsdkResults[0][1] = cMap;
                     amsdkIncluded = true;
                 } else {
                     arrayOfResult[iterNo][0] = results;
                     arrayOfResult[iterNo][1] = cMap;
                     iterNo++;
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to search in the following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("Search: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to search identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to search for identity " + type.getName() + "::"
                     + pattern + " in any configured data store", origEx);
             throw origEx;
         } else {
             IdSearchResults res = combineSearchResults(token, arrayOfResult,
                     iterNo, type, amOrgName, amsdkIncluded, amsdkResults);
             return res;
         }
     }
 
     public IdSearchResults search(SSOToken token, IdType type, String pattern,
             IdSearchControl ctrl, String amOrgName) throws IdRepoException,
             SSOException {
         IdRepoException origEx = null;
         if (remote) {
             return dMgr.search(token, type, pattern, ctrl, amOrgName);
         }
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, null, null, IdOperation.READ, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         Object[][] amsdkResults = new Object[1][2];
         boolean amsdkIncluded = false;
         Object[][] arrayOfResult = new Object[noOfSuccess][2];
         // Set resultsSet = new HashSet();
         int iterNo = 0;
         int maxTime = ctrl.getTimeOut();
         int maxResults = ctrl.getMaxResults();
         Set returnAttrs = ctrl.getReturnAttributes();
         boolean returnAllAttrs = ctrl.isGetAllReturnAttributesEnabled();
         IdSearchOpModifier modifier = ctrl.getSearchModifier();
         int filterOp = IdRepo.NO_MOD;
         if (modifier.equals(IdSearchOpModifier.AND)) {
             filterOp = IdRepo.AND_MOD;
         } else if (modifier.equals(IdSearchOpModifier.OR)) {
             filterOp = IdRepo.OR_MOD;
         }
         Map avPairs = ctrl.getSearchModifierMap();
         boolean recursive = ctrl.isRecursive();
         // if (modifier.)
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 Map cMap = idRepo.getConfiguration();
                 RepoSearchResults results = idRepo.search(token, type, pattern,
                         maxTime, maxResults, returnAttrs, returnAllAttrs,
                         filterOp, avPairs, recursive);
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)) {
                     amsdkResults[0][0] = results;
                     amsdkResults[0][1] = cMap;
                     amsdkIncluded = true;
                 } else {
                     arrayOfResult[iterNo][0] = results;
                     arrayOfResult[iterNo][1] = cMap;
                     iterNo++;
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.error("Unable to search in the following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("Search: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to search identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to search for identity " + type.getName() + "::"
                     + pattern + " in any configured data store", origEx);
             throw origEx;
         } else {
             IdSearchResults res = combineSearchResults(token, arrayOfResult,
                     iterNo, type, amOrgName, amsdkIncluded, amsdkResults);
             return res;
         }
     }
 
     public IdSearchResults getSpecialIdentities(SSOToken token, IdType type,
             String orgName) throws IdRepoException, SSOException {
         Set pluginClasses = new OrderedSet();
         IdRepo thisPlugin = null;
         if (ServiceManager.isConfigMigratedTo70()
                 && ServiceManager.getBaseDN().equalsIgnoreCase(orgName)) {
             // add the "SpecialUser plugin
             String p = IdConstants.SPECIAL_PLUGIN;
             IdRepo pClass = null;
             synchronized (idRepoMap) {
                 pClass = (IdRepo) idRepoMap.get(p);
             }
             if (pClass == null) {
                 try {
 
                     Class thisClass = Class.forName(p);
                     thisPlugin = (IdRepo) thisClass.newInstance();
                     thisPlugin.initialize(new HashMap());
                     Map listenerConfig = new HashMap();
                     listenerConfig.put("realm", orgName);
                     IdRepoListener lter = new IdRepoListener();
                     lter.setConfigMap(listenerConfig);
                     thisPlugin.addListener(token, lter);
                     synchronized (idRepoMap) {
                         idRepoMap.put(p, thisPlugin);
                     }
                     Set opSet = thisPlugin.getSupportedOperations(type);
                     if (opSet != null && opSet.contains(IdOperation.READ)) {
                         pluginClasses.add(thisPlugin);
                     }
                 } catch (Exception e) {
                     idd.error("Unable to instantiate plugin: " + p, e);
                 }
             } else {
                 Set opSet = pClass.getSupportedOperations(type);
                 if (opSet != null && opSet.contains(IdOperation.READ)) {
                     pluginClasses.add(pClass);
                 }
             }
         }
         if (pluginClasses.isEmpty()) {
             return new IdSearchResults(type, orgName);
         } else {
             IdRepo specialRepo = (IdRepo) pluginClasses.iterator().next();
             RepoSearchResults res = specialRepo.search(token, type, "*", 0, 0,
                     Collections.EMPTY_SET, false, 0, Collections.EMPTY_MAP,
                     false);
             Object obj[][] = new Object[1][2];
             obj[0][0] = res;
             obj[0][1] = Collections.EMPTY_MAP;
             return combineSearchResults(token, obj, 1, type, orgName, false,
                     null);
 
         }
     }
 
     /*
      * (non-Javadoc)
      */
     public void setAttributes(SSOToken token, IdType type, String name,
             Map attributes, boolean isAdd, String amOrgName, String amsdkDN,
             boolean isString) throws IdRepoException, SSOException {
         IdRepoException origEx = null;
         if (remote) {
             dMgr.setAttributes(token, type, name, attributes, isAdd, amOrgName,
                     amsdkDN);
             return;
         }
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, attributes.keySet(),
                 IdOperation.EDIT, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         if (attributes.containsKey("objectclass")) {
             configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                     plugIns, IdOperation.SERVICE, type);
         } else {
             configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                     plugIns, IdOperation.EDIT, type);
         }
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 Map cMap = idRepo.getConfiguration();
                 // do stuff to map attr names.
                 attributes = mapAttributeNames(attributes, cMap);
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     if (isString) {
                         idRepo.setAttributes(token, type, amsdkDN, attributes,
                                 isAdd);
                     } else {
                         idRepo.setBinaryAttributes(token, type, amsdkDN,
                                 attributes, isAdd);
                     }
                 } else {
                     if (isString) {
                         idRepo.setAttributes(token, type, name, attributes,
                                 isAdd);
                     } else {
                         idRepo.setBinaryAttributes(token, type, name,
                                 attributes, isAdd);
                     }
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("AMDirectoryManager:setAttributes: "
                             + "Unable to set attributes in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("Set Attributes: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to modify identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 // 220 is entry not found. This error should have lower
                 // precedence than other error because we search thru
                 // all the ds and this entry might exist in one of the other ds.
                 if (!ide.getErrorCode().equals("220") || origEx == null) {
                     origEx = ide;
                 }
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to set attributes  for identity "
                     + type.getName() + "::" + name
                     + " in any configured data store", origEx);
             throw origEx;
 
         }
 
     }
 
     public Set getAssignedServices(SSOToken token, IdType type, String name,
             Map mapOfServiceNamesAndOCs, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
         if (remote) {
             return dMgr.getAssignedServices(token, type, name,
                     mapOfServiceNamesAndOCs, amOrgName, amsdkDN);
         }
         // First get the list of plugins that support the create operation.
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, 
                 null, IdOperation.SERVICE, type);
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.SERVICE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo = null;
         Set resultsSet = new HashSet();
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             try {
 
                 Set services = null;
                 if (repo.getClass().getName().equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     services = repo.getAssignedServices(token, type, amsdkDN,
                             mapOfServiceNamesAndOCs);
                 } else {
                     services = repo.getAssignedServices(token, type, name,
                             mapOfServiceNamesAndOCs);
                 }
                 if (services != null && !services.isEmpty()) {
                     resultsSet.addAll(services);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.error("AMDirectoryManager.getAssignedServices: "
                             + "Services not supported for repository "
                             + repo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("GetAssignedServices: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to get services for identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to get assigned services for identity "
                     + type.getName() + "::" + name
                     + " in any configured data store", origEx);
             throw origEx;
         } else {
             return resultsSet;
         }
 
     }
 
     public void assignService(SSOToken token, IdType type, String name,
             String serviceName, SchemaType stype, Map attrMap,
             String amOrgName, String amsdkDN) throws IdRepoException,
             SSOException {
         IdRepoException origEx = null;
         if (remote) {
             dMgr.assignService(token, type, name, serviceName, stype, attrMap,
                     amOrgName, amsdkDN);
             return;
         }
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, 
                 null, IdOperation.SERVICE, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.SERVICE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo = null;
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             Map cMap = repo.getConfiguration();
             try {
                 attrMap = mapAttributeNames(attrMap, cMap);
                 if (repo.getClass().getName().equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     repo.assignService(token, type, amsdkDN, serviceName,
                             stype, attrMap);
                 } else {
                     repo.assignService(token, type, name, serviceName, stype,
                             attrMap);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.error("AMDirectoryManager.assignServices: "
                             + "Assign Services not supported for repository "
                             + repo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("AssignService: FatalException ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.error("Unable to assign Service identity in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to assign service  for identity "
                     + type.getName() + "::" + name
                     + " in any configured data store", origEx);
             throw origEx;
         }
     }
 
     public void unassignService(SSOToken token, IdType type, String name,
             String serviceName, Map attrMap, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
         if (remote) {
             dMgr.unassignService(token, type, name, serviceName, attrMap,
                     amOrgName, amsdkDN);
             return;
         }
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, 
                 null, IdOperation.SERVICE, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.SERVICE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo = null;
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             Map cMap = repo.getConfiguration();
             try {
                 attrMap = mapAttributeNames(attrMap, cMap);
                 if (repo.getClass().getName().equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     repo.unassignService(token, type, amsdkDN, serviceName,
                             attrMap);
                 } else {
                     repo.unassignService(token, type, name, serviceName,
                             attrMap);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.error("AMDirectoryManager.assignServices: "
                             + "Assign Services not supported for repository "
                             + repo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("UnassignService: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to unassign service in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to unassign Service for identity "
                     + type.getName() + "::" + name
                     + " in any configured data store", origEx);
             throw origEx;
         }
 
     }
 
     public Map getServiceAttributes(SSOToken token, IdType type, String name,
             String serviceName, Set attrNames, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         if (remote) {
             return dMgr.getServiceAttributes(token, type, name, serviceName,
                     attrNames, amOrgName, amsdkDN);
         }
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, 
                 null, IdOperation.SERVICE, type);
 
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.SERVICE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo = null;
         Set resultsSet = new HashSet();
         IdRepoException origEx = null;
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             Map cMap = repo.getConfiguration();
             try {
                 Map attrs = null;
                 if (repo.getClass().getName().equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     attrs = repo.getServiceAttributes(token, type, amsdkDN,
                             serviceName, attrNames);
                 } else {
                     attrs = repo.getServiceAttributes(token, type, name,
                             serviceName, attrNames);
                 }
                 attrs = reverseMapAttributeNames(attrs, cMap);
                 resultsSet.add(attrs);
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("AMDirectoryManager.getServiceAttributes: "
                             + "Services not supported for repository "
                             + repo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("GetServiceAttributes: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("AMDirectoryManager.getServiceAttributes: "
                             + "Unable to get service attributes for "
                             + "the repository" + idRepo.getClass().getName()
                             + " :: " + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to get service attributes for identity "
                     + type.getName() + "::" + name
                     + " in any configured data store", origEx);
             throw origEx;
         } else {
             Map resultsMap = combineAttrMaps(resultsSet, true);
             return resultsMap;
         }
 
     }
 
     public void modifyService(SSOToken token, IdType type, String name,
             String serviceName, SchemaType stype, Map attrMap,
             String amOrgName, String amsdkDN) throws IdRepoException,
             SSOException {
         if (remote) {
             dMgr.modifyService(token, type, name, serviceName, stype, attrMap,
                     amOrgName, amsdkDN);
             return;
         }
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, 
                 name, null, IdOperation.SERVICE, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.SERVICE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo = null;
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             Map cMap = repo.getConfiguration();
             try {
                 attrMap = mapAttributeNames(attrMap, cMap);
                 if (repo.getClass().getName().equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     repo.modifyService(token, type, amsdkDN, serviceName,
                             stype, attrMap);
                 } else {
                     repo.modifyService(token, type, name, serviceName, stype,
                             attrMap);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.error("AMDirectoryManager.modifyServices: "
                             + "Modify Services not supported for repository "
                             + repo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 idd.error("ModifyService: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && idd.warningEnabled()) {
                     idd.warning("Unable to modify service in the "
                             + "following repository"
                             + idRepo.getClass().getName() + " :: "
                             + ide.getMessage());
                 }
                 noOfSuccess--;
             }
         }
         if (noOfSuccess == 0) {
             idd.error("Unable to modify service attributes for identity "
                     + type.getName() + "::" + name
                     + " in any configured data store");
             Object[] args = { IdOperation.SERVICE.toString() };
             throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME,
                     "302", args);
         }
     }
 
     public Set getSupportedTypes(SSOToken token, String amOrgName)
             throws IdRepoException, SSOException {
         if (remote) {
             return dMgr.getSupportedTypes(token, amOrgName);
         }
         // First get the list of plugins that support the create operation.
         Set unionSupportedTypes = new HashSet();
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = getAllConfiguredPlugins(token, amOrgName,
                 plugIns);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             Set supportedTypes = repo.getSupportedTypes();
             if (supportedTypes != null && !supportedTypes.isEmpty()) {
                 unionSupportedTypes.addAll(supportedTypes);
             }
         }
         // Check if the supportedTypes is defined as supported in
         // the global schema.
         unionSupportedTypes.retainAll(IdUtils.supportedTypes);
         return unionSupportedTypes;
     }
 
     public Set getSupportedOperations(SSOToken token, IdType type,
             String amOrgName) throws IdRepoException, SSOException {
         if (remote) {
             return dMgr.getSupportedOperations(token, type, amOrgName);
         }
         // First get the list of plugins that support the create operation.
         Set unionSupportedOps = new HashSet();
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = getAllConfiguredPlugins(token, amOrgName,
                 plugIns);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             Set supportedOps = repo.getSupportedOperations(type);
             if (supportedOps != null && !supportedOps.isEmpty()) {
                 unionSupportedOps.addAll(supportedOps);
             }
         }
         return unionSupportedOps;
     }
 
     public void cleanupIdRepoPlugins() {
         idd.message("AMDM: Cleanup IdRepo Plugins is called..."
                 + "\n Cleaning up the map.." + idRepoMap);
         Set localSet = new HashSet();
         synchronized (idRepoMap) {
             Set keys = idRepoMap.keySet();
             Iterator it = keys.iterator();
             while (it.hasNext()) {
                 String cachekey = (String) it.next();
                 Object o = idRepoMap.get(cachekey);
                 if (o instanceof IdRepo) {
                     localSet.add(idRepoMap.get(cachekey));
                 } else {
                     Map rMap = (Map) o;
                     localSet.addAll(rMap.values());
                 }
             }
             idRepoMap.clear();
         }
         Iterator it = localSet.iterator();
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             repo.removeListener();
             repo.shutdown();
         }
     }
 
     private Set getIdRepoPlugins(SSOToken token, String orgName) {
         if (idRepoPlugins == null) {
             return (Collections.EMPTY_SET);
         }
         if (ServiceManager.isCoexistenceMode()
                 && !idRepoPlugins.contains("amSDK")) {
             idRepoPlugins.add("amSDK");
         }
         return idRepoPlugins;
     }
 
     public static void idRepoServiceSchemaChanged() {
         idRepoPlugins = idRepoSubSchema.getSubSchemaNames();
     }
 
     private Set getConfiguredPlugins(SSOToken token, String orgName,
             Set plugins, IdOperation op, IdType type) {
         // Check and load what is in the cache.
         Set pluginClasses = new OrderedSet();
         if (ServiceManager.isConfigMigratedTo70()
                 && ServiceManager.getBaseDN().equalsIgnoreCase(orgName)) {
             // add the "SpecialUser plugin
             String p = IdConstants.SPECIAL_PLUGIN;
             IdRepo pClass = null;
             synchronized (idRepoMap) {
                 pClass = (IdRepo) idRepoMap.get(p);
             }
             if (pClass == null) {
                 try {
                     Class thisClass = Class.forName(p);
                     IdRepo thisPlugin = (IdRepo) thisClass.newInstance();
                     thisPlugin.initialize(new HashMap());
                     Map listenerConfig = new HashMap();
                     listenerConfig.put("realm", orgName);
                     IdRepoListener lter = new IdRepoListener();
                     lter.setConfigMap(listenerConfig);
                     thisPlugin.addListener(token, lter);
                     synchronized (idRepoMap) {
                         idRepoMap.put(p, thisPlugin);
                     }
                     Set opSet = thisPlugin.getSupportedOperations(type);
                     if (opSet != null && opSet.contains(op)) {
                         pluginClasses.add(thisPlugin);
                     }
                 } catch (Exception e) {
                     idd.error("Unable to instantiate plguin: " + p, e);
                 }
             } else {
                 Set opSet = pClass.getSupportedOperations(type);
                 if (opSet != null && opSet.contains(op)) {
                     pluginClasses.add(pClass);
                 }
             }
         }
 
         if (ServiceManager.isCoexistenceMode()) {
             String p = IdConstants.AMSDK_PLUGIN;
             String cacheKey = orgName + ":" + IdConstants.AMSDK_PLUGIN_NAME;
             IdRepo pClass = null;
             synchronized (idRepoMap) {
                pClass = (IdRepo) idRepoMap.get(cacheKey);
             }
             if (pClass == null) {
                 Map amsdkConfig = new HashMap();
                 Set vals = new HashSet();
                 vals.add(DNMapper.realmNameToAMSDKName(orgName));
                 amsdkConfig.put("amSDKOrgName", vals);
                 try {
                     Class thisClass = Class.forName(p);
                     IdRepo thisPlugin = (IdRepo) thisClass.newInstance();
                     thisPlugin.initialize(amsdkConfig);
                     // Add listener to this plugin class!
                     Map listenerConfig = new HashMap();
                     listenerConfig.put("realm", orgName);
                     listenerConfig.put("amsdk", "true");
                     IdRepoListener lter = new IdRepoListener();
                     lter.setConfigMap(listenerConfig);
                     thisPlugin.addListener(token, lter);
                     synchronized (idRepoMap) {
                         idRepoMap.put(cacheKey, thisPlugin);
                     }
                     Set opSet = thisPlugin.getSupportedOperations(type);
                     if (opSet != null && opSet.contains(op)) {
                         pluginClasses.add(thisPlugin);
                     }
                 } catch (Exception e) {
                     idd.error("Unable to instantiate plugin: " + p, e);
                 }
             } else {
                 Set opSet = pClass.getSupportedOperations(type);
                 if (opSet != null && opSet.contains(op)) {
                     pluginClasses.add(pClass);
                 }
             }
         }
 
         // Check in cache if plugins are already instantiated.
         Iterator it = plugins.iterator();
         while (it.hasNext()) {
             String p = (String) it.next();
             String cacheKey = orgName + ":" + p;
             IdRepo pClass = null;
             Map classMap = null;
             synchronized (idRepoMap) {
                 Object obj = idRepoMap.get(cacheKey);
                 if (obj instanceof Map) {
                     // set it to class map only if this is an instance of
                     // Map. It could possibly be an IdRepo impl class in some
                     // cases.
                     classMap = (Map) obj;
                 }
             }
 
             if (classMap != null) {
                 // This plugin is in cache. Check for
                 // support of operation and type and add
                 // to list of plugins to be invoked.
                 Iterator it2 = classMap.keySet().iterator();
                 while (it2.hasNext()) {
                     String pName = (String) it2.next();
                     pClass = (IdRepo) classMap.get(pName);
                     Set opSet = pClass.getSupportedOperations(type);
                     if (opSet != null && opSet.contains(op)) {
                         pluginClasses.add(pClass);
                     }
                 }
             } else {
                 // Not in cache. Invoke and initialize this class.
                 Set pNames = getConfiguredPluginNames(orgName, p);
                 if (pNames == null || pNames.isEmpty()) {
                     // Update the cache with empty HashMap
                     synchronized (idRepoMap) {
                        idRepoMap.put(cacheKey, Collections.EMPTY_MAP);
                     }
                     continue; // go to start of while
                 }
                 Iterator it2 = pNames.iterator();
                 while (it2.hasNext()) {
                     String pn = (String) it2.next();
                     Map configMap = getConfigMap(orgName, p, pn);
                     if (configMap != null && !configMap.isEmpty()) {
                         Set vals = (Set) configMap.get(IdConstants.ID_REPO);
                         if (vals != null) {
                             String className = (String) vals.iterator().next();
                             try {
                                 Class thisClass = Class.forName(className);
                                 IdRepo thisPlugin = (IdRepo) thisClass
                                         .newInstance();
                                 thisPlugin.initialize(configMap);
                                 // Add listener to this plugin class!
                                 Map listenerConfig = new HashMap();
                                 listenerConfig.put("realm", orgName);
                                 if (className.equals(
                                         IdConstants.AMSDK_PLUGIN)) {
                                     listenerConfig.put("amsdk", "true");
                                 }
                                 listenerConfig.put("plugin-name", pn);
                                 IdRepoListener lter = new IdRepoListener();
                                 lter.setConfigMap(listenerConfig);
                                 thisPlugin.addListener(token, lter);
                                 synchronized (idRepoMap) {
                                     Map tmpMap = (Map) idRepoMap.get(cacheKey);
                                     if (tmpMap == null) {
                                         tmpMap = new HashMap();
                                     }
                                     tmpMap.put(pn, thisPlugin);
                                     idRepoMap.put(cacheKey, tmpMap);
                                 }
                                 if (thisPlugin.getSupportedOperations(type)
                                         .contains(op)) {
                                     pluginClasses.add(thisPlugin);
                                 }
                             } catch (Exception e) {
                                 idd.error("Unable to instantiate plugin: "
                                         + className, e);
                             }
                         }
                     } else {
                         // Plugin not configured for this org. Do nothing.
                     }
                 } // end inner while it.hasNext()
             } // end else
         } // end while
         return pluginClasses;
     }// end getConfiguredPlugins
 
     private Set getAllConfiguredPlugins(SSOToken token, String orgName,
             Set plugins) {
         // Check and load what is in the cache.
         Set pluginClasses = new OrderedSet();
         if (ServiceManager.isConfigMigratedTo70()
                 && ServiceManager.getBaseDN().equalsIgnoreCase(orgName)) {
             // add the "SpecialUser plugin
             String p = IdConstants.SPECIAL_PLUGIN;
             IdRepo pClass = null;
             synchronized (idRepoMap) {
                 pClass = (IdRepo) idRepoMap.get(p);
             }
             if (pClass == null) {
                 try {
                     Class thisClass = Class.forName(p);
                     IdRepo thisPlugin = (IdRepo) thisClass.newInstance();
                     thisPlugin.initialize(new HashMap());
                     Map listenerConfig = new HashMap();
                     listenerConfig.put("realm", orgName);
                     IdRepoListener lter = new IdRepoListener();
                     lter.setConfigMap(listenerConfig);
                     thisPlugin.addListener(token, lter);
                     // thisPlugin.addListener(token, )
                     synchronized (idRepoMap) {
                         idRepoMap.put(p, thisPlugin);
                     }
                     pluginClasses.add(thisPlugin);
                 } catch (Exception e) {
                     idd.error("Unable to instantiate plguin: " + p, e);
                 }
             } else {
                 pluginClasses.add(pClass);
             }
         }
 
         if (ServiceManager.isCoexistenceMode()) {
             String p = IdConstants.AMSDK_PLUGIN;
             String cacheKey = orgName + ":amSDK";
             IdRepo pClass = null;
             synchronized (idRepoMap) {
                 pClass = (IdRepo) idRepoMap.get(cacheKey);
             }
             if (pClass == null) {
                 Map amsdkConfig = new HashMap();
                 Set vals = new HashSet();
                 vals.add(DNMapper.realmNameToAMSDKName(orgName));
                 amsdkConfig.put("amSDKOrgName", vals);
                 try {
                     Class thisClass = Class.forName(p);
                     IdRepo thisPlugin = (IdRepo) thisClass.newInstance();
                     thisPlugin.initialize(amsdkConfig);
                     // Add listener to this plugin class!
                     Map listenerConfig = new HashMap();
                     listenerConfig.put("realm", orgName);
                     listenerConfig.put("amsdk", "true");
                     IdRepoListener lter = new IdRepoListener();
                     lter.setConfigMap(listenerConfig);
                     thisPlugin.addListener(token, lter);
                     synchronized (idRepoMap) {
                         idRepoMap.put(cacheKey, thisPlugin);
                     }
                     pluginClasses.add(thisPlugin);
                 } catch (Exception e) {
                     idd.error("Unable to instantiate plugin: " + p, e);
                 }
             } else {
                 pluginClasses.add(pClass);
             }
         }
         // Check in cache if plugins are already instantiated.
         Iterator it = plugins.iterator();
         while (it.hasNext()) {
             String p = (String) it.next();
             String cacheKey = orgName + ":" + p;
             Map classMap = null;
             synchronized (idRepoMap) {
                 // pClass = (IdRepo) idRepoMap.get(cacheKey);
                 Object obj = idRepoMap.get(cacheKey);
                 if (obj instanceof Map) {
                     // set it to class map only if this is an instance of
                     // Map. It could possibly be an IdRepo impl class in some
                     // cases.
                     classMap = (Map) obj;
                 }
             }
 
             if (classMap != null) {
                 // This plugin is in cache.
                 Iterator it2 = classMap.keySet().iterator();
                 while (it2.hasNext()) {
                     IdRepo t = (IdRepo) classMap.get(it2.next());
                     pluginClasses.add(t);
                 }
 
             } else {
                 // Not in cache. Invoke and initialize this class.
                 Set pNames = getConfiguredPluginNames(orgName, p);
                 if (pNames == null) {
                     // Update the cache with empty HashMap
                     synchronized (idRepoMap) {
                         idRepoMap.put(cacheKey, Collections.EMPTY_MAP);
                     }
                     continue; // go to next plugin schema
                 }
                 Iterator it2 = pNames.iterator();
                 while (it2.hasNext()) {
                     String pn = (String) it2.next();
                     Map configMap = getConfigMap(orgName, p, pn);
                     if (configMap != null && !configMap.isEmpty()) {
                         Set vals = (Set) configMap.get(IdConstants.ID_REPO);
                         if (vals != null) {
                             String className = (String) vals.iterator().next();
                             try {
                                 Class thisClass = Class.forName(className);
                                 IdRepo thisPlugin = (IdRepo) thisClass
                                         .newInstance();
                                 thisPlugin.initialize(configMap);
                                 // Add listener to this plugin class!
                                 Map listenerConfig = new HashMap();
                                 listenerConfig.put("realm", orgName);
                                 if (className.equals(
                                         IdConstants.AMSDK_PLUGIN)) {
                                     listenerConfig.put("amsdk", "true");
                                 }
                                 listenerConfig.put("plugin-name", pn);
                                 IdRepoListener lter = new IdRepoListener();
                                 lter.setConfigMap(listenerConfig);
                                 thisPlugin.addListener(token, lter);
                                 synchronized (idRepoMap) {
                                     Map tmpMap = (Map) idRepoMap.get(cacheKey);
                                     if (tmpMap == null) {
                                         tmpMap = new HashMap();
                                     }
                                     tmpMap.put(pn, thisPlugin);
                                     idRepoMap.put(cacheKey, tmpMap);
                                 }
                                 pluginClasses.add(thisPlugin);
                             } catch (Exception e) {
                                 idd.error("Unable to instantiate plugin: "
                                         + className, e);
                             }
                         }
                     } else {
                         // Plugin not configured for this org. Do nothing.
                     }
                 } // end while it2
             } // end else
         } // end while
 
         return pluginClasses;
     }// end getAllConfiguredPlugins
 
     private Set getConfiguredPluginNames(String orgName, String schemaName) {
         Set cPlugins = new HashSet();
         SSOToken token = (SSOToken) AccessController
                 .doPrivileged(AdminTokenAction.getInstance());
         try {
             ServiceConfigManager scm = new ServiceConfigManager(token,
                     IdConstants.REPO_SERVICE, "1.0");
             if (scm == null) {
                 // Plugin not configured for this organization
                 // return an empty map.
                 return cPlugins;
             }
             ServiceConfig sc = scm.getOrganizationConfig(orgName, null);
             if (sc == null) {
                 // plugin not configured. Return empty Map
                 return cPlugins;
             }
 
             ServiceConfig subConfig = null;
             Set plugins = sc.getSubConfigNames();
             if (plugins != null && !plugins.isEmpty()) {
                 Iterator items = plugins.iterator();
                 while (items.hasNext()) {
                     String pName = (String) items.next();
                     subConfig = sc.getSubConfig(pName);
                     if (subConfig != null
                             && subConfig.getSchemaID().equalsIgnoreCase(
                                     schemaName)) {
                         cPlugins.add(pName);
                     }
                 }
             }
             return cPlugins;
         } catch (SMSException smse) {
             // the if condition below is added to avoid writing this message
             // in debug logs at install time. Durng installation, due
             // a call from amAuth.xml, idrepo is invoked before idrepo service
             // is loaded. The condition below returns false and hence
             // we avoid writing to debug logs at install time
             if (ServiceManager.isConfigMigratedTo70()) {
                 idd.error("SM Exception: unable to get plugin information",
                         smse);
             }
             return cPlugins;
         } catch (SSOException ssoe) {
             idd.error("SSO Exception: ", ssoe);
             return cPlugins;
         }
 
     }
 
     private Map getConfigMap(String orgName, String pluginName,
             String configName) {
         SSOToken token = (SSOToken) AccessController
                 .doPrivileged(AdminTokenAction.getInstance());
         if (ServiceManager.isCoexistenceMode() && pluginName.equals("amSDK")) {
             Map amsdkConfig = new HashMap();
             Set vals = new HashSet();
             vals.add(DNMapper.realmNameToAMSDKName(orgName));
             amsdkConfig.put("amSDKOrgName", vals);
             return amsdkConfig;
         }
         Map configMap = Collections.EMPTY_MAP;
         try {
             ServiceConfigManager scm = new ServiceConfigManager(token,
                     IdConstants.REPO_SERVICE, "1.0");
             if (scm == null) {
                 // Plugin not configured for this organization
                 // return an empty map.
                 return configMap;
             }
             ServiceConfig sc = scm.getOrganizationConfig(orgName, null);
             if (sc == null) {
                 // plugin not configured. Return empty Map
                 return configMap;
             }
 
             ServiceConfig subConfig = sc.getSubConfig(configName);
             if (subConfig != null
                     && subConfig.getSchemaID().equalsIgnoreCase(pluginName)) {
                 configMap = subConfig.getAttributes();
             }
             /*
              * Set plugins = sc.getSubConfigNames(); if (plugins != null &&
              * !plugins.isEmpty()) { Iterator items = plugins.iterator(); while
              * (items.hasNext()) { String pName = (String) items.next();
              * subConfig = sc.getSubConfig(pName); if
              * (subConfig.getSchemaID().equalsIgnoreCase(pluginName)) {
              * configMap = subConfig.getAttributes(); break; } } }
              */
 
             return configMap;
         } catch (SMSException smse) {
             idd.error("SM Exception: unable to get plugin information", smse);
             return configMap;
         } catch (SSOException ssoe) {
             idd.error("SSO Exception: ", ssoe);
             return configMap;
         }
 
     }
 
     private Map combineAttrMaps(Set setOfMaps, boolean isString) {
         // Map resultMap = new CaseInsensitiveHashMap();
         Map resultMap = new AMHashMap();
         Iterator it = setOfMaps.iterator();
         while (it.hasNext()) {
             Map currMap = (Map) it.next();
             if (currMap != null) {
                 Iterator keyset = currMap.keySet().iterator();
                 while (keyset.hasNext()) {
                     String thisAttr = (String) keyset.next();
                     if (isString) {
                         Set resultSet = (Set) resultMap.get(thisAttr);
                         Set thisSet = (Set) currMap.get(thisAttr);
                         if (resultSet != null) {
                             resultSet.addAll(thisSet);
                         } else {
                             /*
                              * create a new Set so that we do not alter the set
                              * that is referenced in setOfMaps
                              */
                             resultSet = new HashSet(
                                                 (Set) currMap.get(thisAttr));
                             resultMap.put(thisAttr, resultSet);
                         }
                     } else { // binary attributes
 
                         byte[][] resultSet = (byte[][]) resultMap.get(thisAttr);
                         byte[][] thisSet = (byte[][]) currMap.get(thisAttr);
                         int combinedSize = thisSet.length;
                         if (resultSet != null) {
                             combinedSize = resultSet.length + thisSet.length;
                             byte[][] tmpSet = new byte[combinedSize][];
                             for (int i = 0; i < resultSet.length; i++) {
                                 tmpSet[i] = resultSet[i];
                             }
                             for (int i = 0; i < thisSet.length; i++) {
                                 tmpSet[i] = thisSet[i];
                             }
                             resultSet = tmpSet;
                         } else {
                             resultSet = (byte[][]) thisSet.clone();
                         }
                         resultMap.put(thisAttr, resultSet);
 
                     }
 
                 }
             }
         }
         return resultMap;
     }
 
     private Map mapAttributeNames(Map attrMap, Map configMap) {
         if (attrMap == null || attrMap.isEmpty()) {
             return attrMap;
         }
         Map resultMap;
         Map[] mapArray = getAttributeNameMap(configMap);
         if (mapArray == null) {
             resultMap = attrMap;
         } else {
             resultMap = new CaseInsensitiveHashMap();
             Map forwardMap = mapArray[0];
             Iterator it = attrMap.keySet().iterator();
             while (it.hasNext()) {
                 String curr = (String) it.next();
                 if (forwardMap.containsKey(curr)) {
                     resultMap.put(forwardMap.get(curr), attrMap.get(curr));
                 } else {
                     resultMap.put(curr, attrMap.get(curr));
                 }
             }
         }
         return resultMap;
     }
 
     private Set mapAttributeNames(Set attrNames, Map configMap) {
         if (attrNames == null || attrNames.isEmpty()) {
             return attrNames;
         }
         Map[] mapArray = getAttributeNameMap(configMap);
         Set resultSet;
         if (mapArray == null) {
             resultSet = attrNames;
         } else {
             resultSet = new CaseInsensitiveHashSet();
             Map forwardMap = mapArray[0];
             Iterator it = attrNames.iterator();
             while (it.hasNext()) {
                 String curr = (String) it.next();
                 if (forwardMap.containsKey(curr)) {
                     resultSet.add(forwardMap.get(curr));
                 } else {
                     resultSet.add(curr);
                 }
             }
         }
         return resultSet;
     }
 
     private Map reverseMapAttributeNames(Map attrMap, Map configMap) {
         if (attrMap == null || attrMap.isEmpty()) {
             return attrMap;
         }
         Map resultMap;
         Map[] mapArray = getAttributeNameMap(configMap);
         if (mapArray == null) {
             resultMap = attrMap;
         } else {
             resultMap = new CaseInsensitiveHashMap();
             Map reverseMap = mapArray[1];
             Iterator it = attrMap.keySet().iterator();
             while (it.hasNext()) {
                 String curr = (String) it.next();
                 if (reverseMap.containsKey(curr)) {
                     resultMap.put(reverseMap.get(curr), attrMap.get(curr));
                 } else {
                     resultMap.put(curr, attrMap.get(curr));
                 }
             }
         }
         return resultMap;
     }
 
     private Set combineMembers(SSOToken token, Set membersSet, IdType type,
             String orgName, boolean amsdkIncluded, Set amsdkMemberships) {
         Set results = new HashSet();
         Map resultsMap = new CaseInsensitiveHashMap();
         if (amsdkIncluded) {
             if (amsdkMemberships != null) {
                 Iterator it = amsdkMemberships.iterator();
                 while (it.hasNext()) {
                     String m = (String) it.next();
                     String mname = m;
                     if (DN.isDN(m)) {
                         mname = (new DN(m)).explodeDN(true)[0];
                     }
                     AMIdentity id = new AMIdentity(token, mname, type, orgName,
                             m);
                     results.add(id);
                     resultsMap.put(mname, id);
                 }
             }
         }
         Iterator miter = membersSet.iterator();
         while (miter.hasNext()) {
             Set first = (Set) miter.next();
             if (first == null) {
                 continue;
             }
             Iterator it = first.iterator();
             while (it.hasNext()) {
                 String m = (String) it.next();
                 String mname = m;
                 if (DN.isDN(m)) {
                     mname = (new DN(m)).explodeDN(true)[0];
                 }
                 // add to results, if not already there!
                 if (!resultsMap.containsKey(mname)) {
                     AMIdentity id = new AMIdentity(token, mname, type, orgName,
                             null);
                     results.add(id);
                     resultsMap.put(mname, id);
                 }
             }
         }
         return results;
     }
 
     private IdSearchResults combineSearchResults(SSOToken token,
             Object[][] arrayOfResult, int sizeOfArray, IdType type,
             String orgName, boolean amsdkIncluded, Object[][] amsdkResults) {
         Map amsdkDNs = new CaseInsensitiveHashMap();
         Map resultsMap = new CaseInsensitiveHashMap();
         int errorCode = IdSearchResults.SUCCESS;
         if (amsdkIncluded) {
             RepoSearchResults amsdkRepoRes = 
                 (RepoSearchResults) amsdkResults[0][0];
             Set results = amsdkRepoRes.getSearchResults();
             Map attrResults = amsdkRepoRes.getResultAttributes();
             Iterator it = results.iterator();
             while (it.hasNext()) {
                 String dn = (String) it.next();
                 String name = (new DN(dn)).explodeDN(true)[0];
                 amsdkDNs.put(name, dn);
                 Set attrMaps = new HashSet();
                 attrMaps.add(attrResults.get(dn));
                 resultsMap.put(name, attrMaps);
             }
             errorCode = amsdkRepoRes.getErrorCode();
         }
         for (int i = 0; i < sizeOfArray; i++) {
             RepoSearchResults current = (RepoSearchResults) arrayOfResult[i][0];
             Map configMap = (Map) arrayOfResult[i][1];
             Iterator it = current.getSearchResults().iterator();
             Map allAttrMaps = current.getResultAttributes();
             while (it.hasNext()) {
                 String m = (String) it.next();
                 String mname = m;
                 Map attrMap = (Map) allAttrMaps.get(m);
                 if (DN.isDN(m)) {
                     mname = (new DN(m)).explodeDN(true)[0];
                 }
                 attrMap = reverseMapAttributeNames(attrMap, configMap);
                 Set attrMaps = (Set) resultsMap.get(mname);
                 if (attrMaps == null) {
                     attrMaps = new HashSet();
                 }
                 attrMaps.add(attrMap);
                 resultsMap.put(mname, attrMaps);
             }
         }
         IdSearchResults results = new IdSearchResults(type, orgName);
         Iterator it = resultsMap.keySet().iterator();
         while (it.hasNext()) {
             String mname = (String) it.next();
             Map combinedMap = combineAttrMaps(
                     (Set) resultsMap.get(mname), true);
             AMIdentity id = new AMIdentity(token, mname, type, orgName,
                     (String) amsdkDNs.get(mname));
             results.addResult(id, combinedMap);
         }
         results.setErrorCode(errorCode);
         return results;
     }
 
     private Map[] getAttributeNameMap(Map configMap) {
         Set attributeMap = (Set) configMap.get(IdConstants.ATTR_MAP);
 
         if (attributeMap == null || attributeMap.isEmpty()) {
             return null;
         } else {
             Map returnArray[] = new Map[2];
             int size = attributeMap.size();
             returnArray[0] = new CaseInsensitiveHashMap(size);
             returnArray[1] = new CaseInsensitiveHashMap(size);
             Iterator it = attributeMap.iterator();
             while (it.hasNext()) {
                 String mapString = (String) it.next();
                 int eqIndex = mapString.indexOf('=');
                 if (eqIndex > -1) {
                     String first = mapString.substring(0, eqIndex);
                     String second = mapString.substring(eqIndex + 1);
                     returnArray[0].put(first, second);
                     returnArray[1].put(second, first);
                 } else {
                     returnArray[0].put(mapString, mapString);
                     returnArray[1].put(mapString, mapString);
                 }
             }
             return returnArray;
         }
     }
 
     private boolean checkPermission(SSOToken token, String realm, String name,
             Set attrs, IdOperation op, IdType type) throws IdRepoException,
             SSOException {
         if (!ServiceManager.isConfigMigratedTo70()) {
             // Config not migrated to 7.0 which means this is
             // in coexistence mode. Do not perform any delegation check
             return true;
         }
         Set thisAction = null;
         if (op.equals(IdOperation.READ)) {
             // thisAction = readAction;
             // TODO This is a temporary fix where-in all users are
             // being allowed read permisions, till delegation component
             // is fixed to support "user self read" operations
             thisAction = readAction;
         } else {
             thisAction = writeAction;
         }
         try {
             DelegationEvaluator de = new DelegationEvaluator();
             String resource = type.getName();
             if (name != null) {
                 resource += "/" + name;
             }
             DelegationPermission dp = new DelegationPermission(realm,
                     IdConstants.REPO_SERVICE, "1.0", "application", resource,
                     thisAction, Collections.EMPTY_MAP);
             Map envMap = Collections.EMPTY_MAP;
             if (attrs != null) {
                 envMap = new HashMap();
                 envMap.put(DELEGATION_ATTRS_NAME, attrs);
             }
             if (!de.isAllowed(token, dp, envMap)) {
                 Object[] args = { op.getName(), 
                         token.getPrincipal().getName() };
                 throw new IdRepoException(
                         IdRepoBundle.BUNDLE_NAME, "402", args);
             }
             return true;
 
         } catch (DelegationException dex) {
             idd.error("AMDirectoryManager.checkPermission "
                     + "Got Delegation Exception: ", dex);
             Object[] args = { op.getName(), token.getPrincipal().getName() };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "402", args);
         }
     }
 
     private boolean checkRealmPermission(SSOToken token, String realm,
             Set action) {
         boolean answer = false;
         if (token != null) {
             try {
                 DelegationEvaluator de = new DelegationEvaluator();
                 DelegationPermission dp = new DelegationPermission(realm,
                         com.sun.identity.sm.SMSEntry.REALM_SERVICE, "1.0", "*",
                         "*", action, Collections.EMPTY_MAP);
                 if (de.isAllowed(token, dp, null)) {
                     answer = true;
                 }
             } catch (DelegationException dex) {
                 debug.error("AMDirectoryManager.checkRealmPermission: "
                         + "Got Delegation Exception: ", dex);
             } catch (SSOException ssoe) {
                 if (debug.messageEnabled()) {
                     debug.message("AMDirectoryManager.checkRealmPermission: "
                             + "Invalid SSOToken: ", ssoe);
                 }
             }
         }
         return (answer);
     }
 
 }
