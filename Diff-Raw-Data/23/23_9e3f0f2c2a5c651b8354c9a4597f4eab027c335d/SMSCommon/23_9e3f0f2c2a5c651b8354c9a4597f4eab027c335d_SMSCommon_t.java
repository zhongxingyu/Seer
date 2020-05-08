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
 * $Id: SMSCommon.java,v 1.10 2008-02-06 19:06:38 cmwesley Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.common;
 
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.idm.IdConstants;
 import com.sun.identity.qatest.common.IDMCommon;
 import com.sun.identity.qatest.common.LDAPCommon;
 import com.sun.identity.qatest.common.SMSConstants;
 import com.sun.identity.sm.AttributeSchema;
 import com.sun.identity.sm.OrganizationConfigManager;
 import com.sun.identity.sm.ServiceConfig;
 import com.sun.identity.sm.ServiceConfigManager;
 import com.sun.identity.sm.ServiceManager;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchemaManager;
 import com.sun.identity.sm.SMSException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 /**
  * This class has helper functions related to service management
  */
 public class SMSCommon extends TestCommon {
     private SSOToken admintoken;
     private String fileSeparator = System.getProperty("file.separator");
     private Map globalCfgMap;
     
     /**
      * Class constructor. Sets class variables.
      */
     public SMSCommon(SSOToken token)
     throws Exception{
         super("SMSCommon");
         admintoken = token;
     }
     
     /**
      * Create new instant for SMSCommon. It will read in the global properties
      * file.
      */
     public SMSCommon(SSOToken token, String globalCfgFile)
     throws Exception {
         super("SMSCommon");
         try {
             globalCfgMap = new HashMap();
             globalCfgMap = getMapFromResourceBundle(globalCfgFile);
             admintoken = token;
             if (!validateToken(admintoken)) {
                 log(Level.SEVERE, "SMSCommon", "SSO token is invalid");
                 assert false;
             }
         } catch (Exception e) {
             e.printStackTrace();
             throw e;
         }
     }
     
     /**
      * Returns attribute value as a set for an attribute in a service using
      * ServiceSchema API. Service type can be one of the following: Global,
      * Organization, Dynamic, User or Policy.
      */
     public Set getAttributeValueFromSchema(String serviceName,
             String attributeName, String type)
             throws Exception {
         ServiceManager sm = new ServiceManager(admintoken);
         ServiceSchemaManager ssm = sm.getSchemaManager(serviceName, "1.0");
         ServiceSchema ss = null;
         if (type.equals("Global"))
             ss = ssm.getGlobalSchema();
         else if (type.equals("Organization"))
             ss = ssm.getOrganizationSchema();
         else if (type.equals("Dynamic"))
             ss = ssm.getDynamicSchema();
         else if (type.equals("User"))
             ss = ssm.getUserSchema();
         else if (type.equals("Policy"))
             ss = ssm.getPolicySchema();
         Map map = ss.getAttributeDefaults();
         if (map.containsKey(attributeName))
             return (((Set)map.get(attributeName)));
         else
             return (null);
     }
     
     /**
      * Returns attribute value as a set for an attribute in a service
      * which have sub configurations. Service type can be one of the following:
      * Global or Organization.
      */
     public Set getAttributeValue(String serviceName, String attributeName,
             String type)
             throws Exception {
         ServiceConfigManager scm = new ServiceConfigManager(admintoken,
                 serviceName, "1.0");
         ServiceConfig sc = null;
         if (type.equals("Global"))
             sc = scm.getGlobalConfig(null);
         else if (type.equals("Organization"))
             sc = scm.getOrganizationConfig(realm, null);
         Map map = sc.getAttributes();
         if (map.containsKey(attributeName))
             return (((Set)map.get(attributeName)));
         else
             return (null);
     }
     
     /**
      * Method updates a given attribute in any sepcified service.
      * This is only valid for Global and Organization level attributes.
      * It does not update Dynamic attributes. This updates attribute
      * values for global and organization services which have sub
      * configurations.
      */
     public void updateSvcAttribute(String serviceName,
             String attributeName, Set set, String type)
             throws Exception {
         ServiceConfigManager scm = new ServiceConfigManager(serviceName,
                 admintoken);
         ServiceConfig sc = null;
         if (type.equals("Global"))
             sc = scm.getGlobalConfig(null);
         else if (type.equals("Organization"))
             sc = scm.getOrganizationConfig(realm, null);
         Map map = new HashMap();
         map.put(attributeName, set);
         sc.removeAttribute(attributeName);
         sc.setAttributes(map);
     }
     
     /**
      * Method updates a given attribute in any sepcified service.
      * This is only valid for Global, Organization, Dynamic, User and Policy
      * attributes. This directly updates the schema entry for these services.
      */
     public void updateSvcSchemaAttribute(String serviceName,
             String attributeName, Set set, String type)
             throws Exception {
         ServiceManager sm = new ServiceManager(admintoken);
         ServiceSchemaManager ssm = sm.getSchemaManager(serviceName, "1.0");
         ServiceSchema ss = null;
         if (type.equals("Global"))
             ss = ssm.getGlobalSchema();
         else if (type.equals("Organization"))
             ss = ssm.getOrganizationSchema();
         else if (type.equals("Dynamic"))
             ss = ssm.getDynamicSchema();
         else if (type.equals("User"))
             ss = ssm.getUserSchema();
         else if (type.equals("Policy"))
             ss = ssm.getPolicySchema();
         ss.setAttributeDefaults(attributeName, set);
     }
     
     /**
      * Method removes values for a given attribute in any sepcified service
      */
     public void removeServiceAttributeValues(String serviceName,
             String attributeName, String type)
             throws Exception {
         ServiceConfigManager scm = new ServiceConfigManager(serviceName,
                 admintoken);
         ServiceConfig sc = null;
         if (type.equals("Global"))
             sc = scm.getGlobalConfig(null);
         else if (type.equals("Organization"))
             sc = scm.getOrganizationConfig(realm, null);
         sc.removeAttribute(attributeName);
     }
     
     /**
      * Assign a service which has dynamic attributes like session or user from
      * a realm
      */
     public Map getDynamicServiceAttributeRealm(String serviceName, String realm)
     throws Exception {
         AMIdentityRepository idrepo = new AMIdentityRepository(admintoken,
                 realm);
         AMIdentity realmIdentity = idrepo.getRealmIdentity();
         Set set = realmIdentity.getAssignedServices();
         if (set.contains(serviceName))
             return (realmIdentity.getServiceAttributes(serviceName));
         else
             return (null);
     }
     
     /**
      * Assign a service which has dynamic attributes like session or user from
      * a realm
      */
     public boolean assignDynamicServiceRealm(String serviceName, String realm,
             Map map)
             throws Exception {
         AMIdentityRepository idrepo = new AMIdentityRepository(admintoken,
                 realm);
         AMIdentity realmIdentity = idrepo.getRealmIdentity();
         Set set = realmIdentity.getAssignedServices();
         if (!set.contains(serviceName))
             realmIdentity.assignService(serviceName, map);
         set = realmIdentity.getAssignedServices();
         return (set.contains(serviceName));
     }
     
     /**
      * Assign a service without dynamic attributes to a realm
      */
     public void assignServiceRealm(String serviceName, String realm, Map map)
     throws Exception {
         if (!isServiceAssigned(serviceName, realm)) {
             OrganizationConfigManager ocm =
                     new OrganizationConfigManager(admintoken, realm);
             ocm.assignService(serviceName, map);
         }
     }
     
     /**
      * Unassign a service which has dynamic attributes like session or user
      * from a realm
      */
     public boolean unassignDynamicServiceRealm(String serviceName, String realm)
     throws Exception {
         AMIdentityRepository idrepo = new AMIdentityRepository(admintoken,
                 realm);
         AMIdentity realmIdentity = idrepo.getRealmIdentity();
         Set set = realmIdentity.getAssignedServices();
         if (set.contains(serviceName))
             realmIdentity.unassignService(serviceName);
         set = realmIdentity.getAssignedServices();
         return (!set.contains(serviceName));
     }
     
     /**
      * Unassign a service without dynamic attributes from a realm
      */
     public void unassignServiceRealm(String serviceName, String realm)
     throws Exception {
         if (isServiceAssigned(serviceName, realm)) {
             OrganizationConfigManager ocm =
                     new OrganizationConfigManager(admintoken, realm);
             ocm.unassignService(serviceName);
         }
     }
     
     /**
      * Checks whether a service is assigned to a realm
      */
     public boolean isServiceAssigned(String serviceName, String realm)
     throws Exception {
         OrganizationConfigManager ocm =
                 new OrganizationConfigManager(admintoken, realm);
         Set set = ocm.getAssignedServices();
         if (set.contains(serviceName))
             return (true);
         else
             return (false);
     }
     
     /**
      * Sets dynamic attributes for a service at the global leval
      */
     public void updateGlobalServiceDynamicAttributes(String serviceName,
             Map map)
             throws Exception {
         ServiceManager sm = new ServiceManager(admintoken);
         ServiceSchemaManager ssm = sm.getSchemaManager(serviceName , "1.0");
         ServiceSchema ss = ssm.getDynamicSchema();
         ss.setAttributeDefaults(map);
     }
     
     /**
      * This method create one or multiple datastores by datastore index number
      * from configuration data specified in the properties file.
      * @param cdsIndex datastore index to be retrieved from the properties file
      * @param propertyFileName properties file name (without extenstion)
      */
     public void createDataStore(int cdsIndex, String propertyFileName)
     throws Exception {
         entering("createDataStore", null);
         createDataStore(getDataStoreConfigByIndex(cdsIndex, propertyFileName));
         exiting("createDataStore");
     }
     
     /**
      * This method create one or multiple datastores from configuration data
      * specified in a Map.
      * @param cdsMap a map contains datstore configuration data
      */
     public void createDataStore(Map cdsMap)
     throws Exception {
         entering("createDataStore", null);
         String dsCount = (String)cdsMap.get(SMSConstants.SMS_DATASTORE_COUNT);
         for (int j = 0; j < Integer.parseInt(dsCount); j++)
             createDataStoreImpl(setDataStoreConfigData(j, cdsMap));
         exiting("createDataStore");
     }
     
     /**
      * This method calls Service Management methods to a create datastore.
      */
     private void createDataStoreImpl(Map cdsiMap)
     throws Exception {
         entering("createDataStoreImpl", null);
         try {
             String realmName = (String)cdsiMap.
                     get(SMSConstants.SMS_DATASTORE_REALM);
             String dsName = (String)cdsiMap.
                     get(SMSConstants.SMS_DATASTORE_NAME);
             if (!doesDataStoreExists(realmName, dsName)) {
                 String dsType = (String)cdsiMap.
                         get(SMSConstants.SMS_DATASTORE_TYPE);
                 if (dsType.equalsIgnoreCase(
                         SMSConstants.SMS_DATASTORE_TYPE_AMDS) ||
                         dsType.equalsIgnoreCase(
                         SMSConstants.SMS_DATASTORE_TYPE_AD)) {
                     // Retrieve the LDAP server information and call the
                     // method to load the AM user schema
                     String dsHost = (String)cdsiMap.
                             get(SMSConstants.SMS_LDAPv3_LDAP_SERVER);
                     String dsPort = (String)cdsiMap.
                             get(SMSConstants.SMS_LDAPv3_LDAP_PORT);
                     String dsDirmgrdn = (String)cdsiMap.
                             get(SMSConstants.SMS_DATASTORE_ADMINID);
                     String dsDirmgrpwd = (String)cdsiMap.
                             get(SMSConstants.SMS_DATASTORE_ADMINPW);
                     String dsRootSuffix = (String)cdsiMap.
                             get(SMSConstants.SMS_LDAPv3_ORGANIZATION_NAME);
                     String sslmode = (String)cdsiMap.
                             get(SMSConstants.SMS_LDAPv3_LDAP_SSL_ENABLED);
                     String keystore = null;
                     if (sslmode.equals("true"))
                         keystore = (String)cdsiMap.
                                 get(SMSConstants.SMS_DATASTORE_KEYSTORE);
                     LDAPCommon ldc = new LDAPCommon(dsHost, dsPort,
                             dsDirmgrdn, dsDirmgrpwd, dsRootSuffix, keystore);
                     String schemaString = (String)globalCfgMap.
                             get(SMSConstants.SMS_SCHEMNA_LIST + "." + dsType);
                     String schemaAttributes = (String)globalCfgMap.
                             get(SMSConstants.SMS_SCHEMNA_ATTR + "." + dsType);
                     ldc.loadAMUserSchema(schemaString, schemaAttributes);
                 }
                 log(Level.FINE, "createDataStoreImpl", "Creating datastore " +
                         dsName +  "...");
                 ServiceConfig cfg = getServiceConfig(admintoken, realmName,
                         true);
                 cfg.addSubConfig(dsName,
                         getDataStoreType(dsType), 0,
                         setDataStoreAttributes(cdsiMap));
                 if (doesDataStoreExists(realmName, dsName))
                     log(Level.FINE, "createDataStoreImpl", "Datastore " +
                             dsName +  " is created successfully.");
                 else {
                     log(Level.SEVERE, "createDataStoreImpl",
                             "Failed to create datastore " + dsName);
                     assert false;
                 }
             } else {
                 log(Level.FINE, "createDataStoreImpl", "Datastore " +
                         dsName + " exists");
             }
         } catch (Exception e) {
             log(Level.SEVERE, "createDataStoreImpl", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("createDataStoreImpl");
     }
     
     /**
      * This method updates a datastore with datastore attribute list
      * @param udsRealm is the realm name where the datastore belongs to
      * @param udsName datastore name to be updated
      * @param updatedAttrMap a list of attributes to be updated
      */
     public void updateDataStore(String udsRealm, String udsName, Map updAttrMap)
     throws Exception {
         entering("updateDataStore", null);
         try {
             log(Level.FINE, "updateDataStore", "Updating datastore " +
                     udsName + " in realm " + udsRealm + "...");
             ServiceConfig cfg = getServiceConfig(admintoken, udsRealm);
             if (cfg != null) {
                 ServiceConfig sc = cfg.getSubConfig(udsName);
                 if (sc != null) {
                     Map currentAttr = sc.getAttributes();
                     Set keys = updAttrMap.keySet();
                     Iterator keyIter = keys.iterator();
                     String key;
                     Set curValSet;
                     Set newValSet;
                     while (keyIter.hasNext()) {
                         key = (String)keyIter.next();
                         curValSet = (Set)currentAttr.get(key);
                         newValSet = (Set)updAttrMap.get(key);
                         if (!curValSet.contains(null))
                             concatSet(newValSet, curValSet);
                         updAttrMap.put(key, newValSet);
                     }
                     sc.setAttributes(updAttrMap);
                     Map existingAttrMap = sc.getAttributesForRead();
                     log(Level.FINEST, "updateDatastore",
                             "Verifying the updated attribute(s)");
                     if (doesMapContainsKeysValues(updAttrMap,
                             existingAttrMap))
                         log(Level.FINE, "updateDataStore", "Datastore " +
                                 udsName + " is updated successfully");
                     else {
                         log(Level.SEVERE, "updateDataStore",
                                 "Failed to update datastore");
                         assert false;
                     }
                 } else {
                     log(Level.SEVERE, "updateDataStore", "Datastore not found");
                     assert false;
                 }
             } else {
                 log(Level.SEVERE, "UpdateDataStore", "Datastore not found");
                 assert false;
             }
         } catch (Exception e) {
             log(Level.SEVERE, "UpdateDataStore", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("updateDataStore");
     }
     
     /**
      * This method checks if datastore exists in a realm
      * @param idseRealm realm name where datastore belongs to
      * @param idseName datastore name to be checked
      * @return true if datastore exists
      */
     public boolean doesDataStoreExists(String idseRealm, String idseName)
     throws Exception {
         entering("doesDataStoreExists", null);
         boolean datastoreFound = false;
         log(Level.FINEST, "doesDataStoreExists", "Realm = " + idseRealm + " " +
                 "Datastore name " + idseName);
         datastoreFound = listDataStore(idseRealm).contains(idseName);
         exiting("doesDataStoreExists");
         return datastoreFound;
     }
     
     /**
      * This method lists all datastore(s) in a realm
      * @param ldsRealm realm name where datastore belongs to
      * @return a set of datastore
      */
     public Set listDataStore(String ldsRealm)
     throws Exception {
         entering("listDataStore", null);
         Set datastoreNameSet = null;
         try {
             log(Level.FINE, "listDataStore", "Listing datastore for realm " +
                     ldsRealm + "...");
             ServiceConfig cfg = getServiceConfig(admintoken, ldsRealm);
             datastoreNameSet = (cfg != null) ? cfg.getSubConfigNames() :
                 Collections.EMPTY_SET;
             if ((datastoreNameSet != null) && !datastoreNameSet.isEmpty()) {
                 for (Iterator i = datastoreNameSet.iterator(); i.hasNext();) {
                     String dsname = (String)i.next();
                     log(Level.FINEST, "listDataStore", "Datastore is " +
                             dsname);
                 }
             } else
                 log(Level.FINE, "listDataStore", "Datastore list empty");
         } catch (Exception e) {
             log(Level.SEVERE, "listDataStore", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("listDataStore");
         return datastoreNameSet;
     }
     
     /**
      * This method delete one or multiple datastore(s) that specified in a
      * properties file.
      * @param ddsIndex realm name where datastore belongs to
      * @param propertyFileName properties file with datastore configuration data
      */
     public void deleteDataStore(int ddsIndex, String propertyFileName)
     throws Exception {
         entering("deleteDataStore", null);
         deleteDataStore(getDataStoreConfigByIndex(ddsIndex,
                 propertyFileName));
         exiting("deleteDataStore");
     }
     
     /**
      * This method delete a datastore that defined in a map.
      * @param ddsMap a map with datastore configuration data
      */
     public void deleteDataStore(Map ddsMap)
     throws Exception {
         entering("deleteDataStore", null);
         Map oneCfgMap = null;
         String ddsRealm;
         String ddsName;
         String dsCount = (String)ddsMap.get(SMSConstants.SMS_DATASTORE_COUNT);
         for (int j = 0; j < Integer.parseInt(dsCount); j++) {
             oneCfgMap = setDataStoreConfigData(j, ddsMap);
             ddsRealm = (String)oneCfgMap.get(SMSConstants.SMS_DATASTORE_REALM);
             ddsName = (String)oneCfgMap.get(SMSConstants.SMS_DATASTORE_NAME);
             deleteDataStore(ddsRealm, ddsName);
         }
         exiting("deleteDataStore");
     }
     
     /**
      * This method delete a datastore.
      * @param ddsRealm realm name where datastore belongs
      * @param ddsName datastore name to be deleted
      */
     public void deleteDataStore(String ddsRealm, String ddsName)
     throws Exception {
         entering("deleteDataStore", null);
         try {
             log(Level.FINE, "deleteDataStore", "Deleting datastore " +
                     ddsName + "...");
             ServiceConfig cfg = getServiceConfig(admintoken, ddsRealm);
             if (cfg != null)
                 cfg.removeSubConfig(ddsName);
             if (!doesDataStoreExists(ddsRealm, ddsName))
                 log(Level.FINE, "deleteDataStore", "Datastore " + ddsName +
                         " was deleted successfully");
             else {
                 log(Level.SEVERE, "DeleteDataStore",
                         "Failed to delete datastore " + ddsName);
                 assert false;
             }
         } catch (Exception e) {
             log(Level.SEVERE, "deleteDataStore", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("deleteDataStore");
     }
     
     /**
      * This method retrieves datastore configuration data from a properties file
      * by index number and store them in a map
      * @param gdscbiIndex datastore index
      * @param cfgFileName properties file name
      * @return a map that contains the datastore configuration without the
      * prefix, SMSConstants.SMS_DATASTORE_PARAMS_PREFIX
      */
     public Map getDataStoreConfigByIndex(int gdscbiIndex, String cfgFileName)
     throws Exception {
         entering("getDataStoreConfigByIndex", null);
         Map ldapMap = new HashMap();
         Map fileMap = new HashMap();
         try {
             log(Level.FINE, "getDataStoreConfigByIndex",
                     "Retrieving datastore with index " + gdscbiIndex +
                     " from property file " + cfgFileName);
             fileMap = getMapFromResourceBundle(cfgFileName);
             Set keys = fileMap.keySet();
             Iterator keyIter = keys.iterator();
             String key;
             String value;
             String prefixCfgParams;
             while (keyIter.hasNext()) {
                 key = keyIter.next().toString();
                 value = fileMap.get(key).toString();
                 prefixCfgParams = SMSConstants.SMS_DATASTORE_PARAMS_PREFIX +
                         gdscbiIndex;
                 if (key.toString().startsWith(prefixCfgParams)) {
                     ldapMap.put(key.toString().
                             substring(prefixCfgParams.length() + 1), value);
                 }
             }
             log(Level.FINEST, "getDataStoreConfigByIndex",
                     "Datastore config data " + ldapMap.toString());
             if (ldapMap.isEmpty()) {
                 log(Level.SEVERE, "getDataStoreConfigByIndex",
                         "Could not retrieve datastore for index" + gdscbiIndex);
                 assert false;
             }
         } catch (Exception e) {
             e.printStackTrace();
             throw e;
         }
         exiting("getDataStoreConfigByIndex");
         return ldapMap;
     }
     
     /**
      * This method get a Service Configuration object from Service
      * Management methods
      */
     private ServiceConfig getServiceConfig(SSOToken admToken, String gscRealm)
     throws Exception {
         return (getServiceConfig(admToken, gscRealm, false));
     }
     
     /**
      * This method get a Service Configuration object from Service Management
      * methods
      */
     private ServiceConfig getServiceConfig(SSOToken admToken,
             String gscRealm, boolean createIfNull)
             throws Exception {
         ServiceConfig svcfg = null;
         IDMCommon idmObj = new IDMCommon();
         if (!gscRealm.equals(realm)) {
             log(Level.FINE, "getServiceConfig", "Checking sub realm " +
                     gscRealm + " ...");
             // Check the realm to make sure it exists before creating object
             // for ServiceConfig
             if (idmObj.searchRealms(admToken, gscRealm.
                     substring(gscRealm.lastIndexOf(realm) + 1)).isEmpty()) {
                 log(Level.SEVERE, "getServiceConfig", "Realm " + gscRealm +
                         " not found");
                 assert false;
             } else
                 log(Level.FINEST, "getServiceConfig", "Found realm " +
                         gscRealm);
         }
         ServiceConfigManager scm = new ServiceConfigManager(
                 IdConstants.REPO_SERVICE, admToken);
         svcfg = scm.getOrganizationConfig(gscRealm, null);
         if (createIfNull && svcfg == null) {
             OrganizationConfigManager orgCfgMgr = new
                     OrganizationConfigManager(admToken, gscRealm);
             Map attrValues = getDefaultAttributeValues(admToken);
             svcfg = orgCfgMgr.addServiceConfig(IdConstants.REPO_SERVICE,
                     attrValues);
         }
         return svcfg;
     }
     
     /**
      * This method returns the datastore schema type name depend on datastore
      * type.
      */
     private String getDataStoreType(String gdstType)
     throws Exception {
         if (gdstType.equals(SMSConstants.SMS_DATASTORE_TYPE_AMDS))
             return SMSConstants.SMS_DATASTORE_SCHEMA_TYPE_AMDS;
         else if (gdstType.equals(SMSConstants.SMS_DATASTORE_TYPE_AD))
             return SMSConstants.SMS_DATASTORE_SCHEMA_TYPE_AD;
         else if (gdstType.equals(SMSConstants.SMS_DATASTORE_TYPE_LDAP))
             return SMSConstants.SMS_DATASTORE_SCHEMA_TYPE_LDAP;
         else if (gdstType.equals(SMSConstants.SMS_DATASTORE_TYPE_FF))
             return SMSConstants.SMS_DATASTORE_SCHEMA_TYPE_FF;
         else if (gdstType.equals(SMSConstants.SMS_DATASTORE_TYPE_AMSDK))
             return SMSConstants.SMS_DATASTORE_SCHEMA_TYPE_AMSDK;
         else {
             log(Level.SEVERE, "getDataStoreType", "Invalid type " + gdstType +
                     " .Failed to retrieve datastore schema type name");
             assert false;
             return null;
         }
     }
     
     /**
      * This method set the datastore attributes and store them in a map
      */
     private Map setDataStoreAttributes(Map sdscfmMap)
     throws Exception {
         String newTempKey;
         Map dsAttributeMap = new HashMap<String, Set<String>>();
         String dsType = (String)sdscfmMap.get(SMSConstants.SMS_DATASTORE_TYPE);
         try {
             Set keys = sdscfmMap.keySet();
             Iterator keyIter = keys.iterator();
             String key;
             String value;
             String portNumber;
             while (keyIter.hasNext()) {
                 key = keyIter.next().toString();
                 value = sdscfmMap.get(key).toString();
                 if (!key.startsWith(SMSConstants.SMS_DATASTORE_KEY_PREFIX)) {
                     if ((dsType == null) || (dsType.equalsIgnoreCase(
                             SMSConstants.SMS_DATASTORE_TYPE_FF)) ||
                             (dsType.equalsIgnoreCase(
                             SMSConstants.SMS_DATASTORE_TYPE_AMSDK))) {
                         putSetIntoMap(key, dsAttributeMap, value, "|");
                     } else if (!key.equals(SMSConstants.SMS_LDAPv3_LDAP_PORT)) {
                         if (key.equals(SMSConstants.SMS_LDAPv3_LDAP_SERVER)) {
                             portNumber = (String)sdscfmMap.
                                     get(SMSConstants.SMS_LDAPv3_LDAP_PORT);
                             value = (portNumber == null) ? value + ":389" :
                                 value + ":" + portNumber;
                         }
                         putSetIntoMap(key, dsAttributeMap, value, "|");
                     }
                 }
             }
             log(Level.FINEST, "setDataStoreAttributes",
                     "Datastore attributes " + dsAttributeMap.toString());
         } catch (Exception e) {
             log(Level.SEVERE, "setDataStoreAttributes", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         return dsAttributeMap;
     }
     
     /**
      * This method set the datastore configuration data and store it in a map.
      * It will remove the datastore number from the key and append LDAPv3
      * attribute prefix in LDAPv3 attribute keys.
      */
     private Map setDataStoreConfigData(int sdscdIndex, Map sdscfmMap)
     throws Exception {
         Map dsMap = new HashMap();
         String dsType = (String)sdscfmMap.
                 get(SMSConstants.SMS_DATASTORE_TYPE + "." + sdscdIndex);
         if (dsType == null)
             dsType = (String)sdscfmMap.
                     get(SMSConstants.SMS_DATASTORE_TYPE);
         try {
             Set keys = sdscfmMap.keySet();
             Iterator keyIter = keys.iterator();
             String newTempKey;
             String key;
             String value;
             int posOfLastPeriodIndex = 0;
             while (keyIter.hasNext()) {
                 key = keyIter.next().toString();
                 value = sdscfmMap.get(key).toString();
                 posOfLastPeriodIndex = key.lastIndexOf("." + sdscdIndex);
                 if (posOfLastPeriodIndex >= 0) {
                     newTempKey = (posOfLastPeriodIndex >= 0) ?
                         key.substring(0, posOfLastPeriodIndex) : key;
                     dsMap.put(newTempKey, value);
                 }
                 log(Level.FINEST, "setDataStoreConfigData", dsMap.toString());
             }
             if (dsMap.isEmpty()) {
                 log(Level.SEVERE, "setDataStoreConfigData",
                         "Could not find config data for datastore " +
                         sdscdIndex);
                 assert false;
             }
         } catch (Exception e) {
             e.printStackTrace();
             throw e;
         }
         return dsMap;
     }
     
     /**
      * This method compare if keys and values in a Map are also in another Map
      */
     private boolean doesMapContainsKeysValues(Map sMap, Map lMap)
     throws Exception {
         boolean foundKeysValues = true;
         Set keys = sMap.keySet();
         String key;
         Set sValue;
         Set lValue;
         Iterator keyIter = keys.iterator();
         while (keyIter.hasNext()) {
             key = keyIter.next().toString();
             sValue = (Set)sMap.get(key);
             lValue = (Set)lMap.get(key);
             log(Level.FINEST, "doesMapContainsKeysValues", "Key = " + key);
             log(Level.FINEST, "doesMapContainsKeysValues",
                     "Small set value = " + sValue.toString());
             log(Level.FINEST, "doesMapContainsKeysValues",
                     "Large set value = " + lValue.toString());
             // if one of the value of the key does not match or empty, set the
             // flag to false.
             if (!lMap.get(key).equals(sValue)) {
                 foundKeysValues = false;
                 break;
             }
         }
         return foundKeysValues;
     }
     
     /**
      * This method get a default attribute value for CreateDataStore
      */
     private Map getDefaultAttributeValues(SSOToken adminSSOToken)
     throws SMSException, SSOException {
         log(Level.FINEST, "getDefaultAttributeValues", null);
         ServiceSchemaManager schemaMgr = new
                 ServiceSchemaManager(IdConstants.REPO_SERVICE, adminSSOToken);
         ServiceSchema orgSchema = schemaMgr.getOrganizationSchema();
         Set attrs = orgSchema.getAttributeSchemas();
         Map values = new HashMap(attrs.size() * 2);
         AttributeSchema as;
         for (Iterator iter = attrs.iterator(); iter.hasNext();) {
             as = (AttributeSchema)iter.next();
             values.put(as.getName(), as.getDefaultValues());
         }
         return values;
     }
     
     /**
      * This method checks whether a given type of datastore plugin is 
      * configured at the selected realm
      * The supported type of pluings are :
      *  - amSDK
      *  - LDAVPv3forAMDS
      *  - LDAPv3ForAD
      *  - LDAPv3
      *  - files
      * @param ssotoken SSO token
      * @param pluginName Plugin name
      * @param subRealmName subRealm name
      * @return true if Plugin type to be checked is supported to the given realm
      * 
      */
     public boolean isPluginConfigured(SSOToken ssoToken, String pluginName, 
             String subRealmName)
     throws Exception {
         entering("isPluginConfigured", null);
         ServiceConfig subConfig;
         OrganizationConfigManager orgMgr = new OrganizationConfigManager(ssoToken,
                 subRealmName);
         OrganizationConfigManager subrealm = orgMgr.getSubOrgConfigManager(subRealmName);
         ServiceConfig sc = subrealm.getServiceConfig(SMSConstants.REALM_SERVICE);
         boolean isPluginConfigured = false;
         if (sc != null) {
             try {
                 Iterator items = sc.getSubConfigNames().iterator();
                 while (items.hasNext()) {
                     subConfig = sc.getSubConfig((String) items.next());
                     if (subConfig.getSchemaID().equalsIgnoreCase(pluginName)) {
                         isPluginConfigured = true;
                         break;
                     }
                 }
             } catch (Exception e) {
                 e.printStackTrace();
                 throw e;
             }
         }
         exiting("isPluginConfigured");
         return isPluginConfigured;
     }
  
    /**
     * This method checks whether a given type of datastore plugin is 
     * configured at the selected realm using the existing admin 
     * <code>SSOToken</code>.
     * The supported type of pluings are :
     *  - amSDK
     *  - LDAVPv3forAMDS
     *  - LDAPv3ForAD
     *  - LDAPv3
     *  - files
     * @param pluginName Plugin name
     * @param subRealmName subRealm name
     * @return true if Plugin type to be checked is supported to the given realm
     * 
     */    
    public boolean isPluginConfigured(String pluginName, String subRealmName) 
    throws Exception {
        return isPluginConfigured(admintoken, pluginName, subRealmName);
    }    
 }
