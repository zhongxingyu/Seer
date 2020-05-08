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
 * $Id: IDMCommon.java,v 1.2 2007-09-04 21:46:12 bt199000 Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.common;
 
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.idm.IdConstants;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdSearchControl;
 import com.sun.identity.idm.IdSearchResults;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.sm.OrganizationConfigManager;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 /**
  * This class contains helper methods related to identity objects
  */
 public class IDMCommon extends TestCommon {
     
     /**
      * Empty Constructor
      */
     public IDMCommon() {
         super("IDMCommon");
     }
     
     public IDMCommon(String componentName) {
         super(componentName);
     }
     
     /**
      * Creates a new Identity
      */
     public AMIdentity createIdentity(
             SSOToken ssoToken,
             String parentRealm,
             IdType idType,
             String entityName,
             Map values)
             throws Exception {
         AMIdentityRepository repo = new AMIdentityRepository(
                 ssoToken, parentRealm);
         AMIdentity amid = repo.createIdentity(idType, entityName, values);
         assert (amid.getName().equals(entityName));
         return amid;
     }
     
     /**
      * Gets realm identity
      */
     public AMIdentity getRealmIdentity(
             SSOToken ssoToken,
             String parentRealm)
             throws Exception {
         AMIdentityRepository repo = new AMIdentityRepository(
                 ssoToken, parentRealm);
         return (repo.getRealmIdentity());
     }
     
     /**
      * Returns the value of specified attribute for an Identity
      */
     public Set getIdentityAttribute(
             SSOToken ssoToken,
             String serviceName,
             String attributeName)
             throws Exception {
         AMIdentity amid = new AMIdentity(ssoToken);
         Map attrValues = amid.getServiceAttributes(serviceName);
         log(logLevel, "getIdentityAttribute", "Attributes List" + attrValues);
         return (Set)attrValues.get(attributeName);
     }
     
     /**
      * Modifies specified identity attributes
      */
     public void modifyIdentity(AMIdentity amid, Map values)
     throws Exception {
         amid.setAttributes(values);
         amid.store();
     }
     
     /**
      * Modifies specified identity attributes for a realm
      */
     public void modifyRealmIdentity(SSOToken token, String realm, Map values)
     throws Exception {
         AMIdentity amid = getRealmIdentity(token, realm);
         amid.setAttributes(values);
         amid.store();
     }
     
     /**
      * Deletes an identity based on specified ssotoken, realm, id type and
      * entity name
      */
     public void deleteIdentity(
             SSOToken ssoToken,
             String parentRealm,
             IdType idType,
             String entityName)
             throws Exception {
         AMIdentityRepository repo = new AMIdentityRepository(
                 ssoToken, parentRealm);
         repo.deleteIdentities(getAMIdentity(
                 ssoToken, entityName, idType, parentRealm));
     }
 
     /**
      * Deletes multiple identities based on specified ssotoken, realm, id type, 
      * and a list of entity names
      */
     public void deleteIdentity(
             SSOToken ssoToken,
             String parentRealm,
             List<IdType> idType,
             List entityName)
             throws Exception {
         AMIdentityRepository repo = new AMIdentityRepository(
                 ssoToken, parentRealm);
         Iterator iterNameSet = entityName.iterator();
         Iterator iterTypeSet = idType.iterator();
         Set amid = new HashSet<AMIdentity>();
         while (iterNameSet.hasNext()) {
             amid.add(getFirstAMIdentity(ssoToken, (String)iterNameSet.next(), 
                     (IdType)iterTypeSet.next(), parentRealm));
         }
         repo.deleteIdentities(amid);
     }
     
     /**
      * Returns AMIdentity based in ssotoken, id name , id type and  sepcified
      * realm
      */
     public Set<AMIdentity> getAMIdentity(
             SSOToken ssoToken,
             String name,
             IdType idType,
             String realm)
             throws Exception {
         Set<AMIdentity> set = new HashSet<AMIdentity>();
         set.add(new AMIdentity(ssoToken, name, idType, realm, null));
         return set;
     }
     
     /**
      * Returns First AMIdentity based in ssotoken, id name , id type and
      * sepcified realm
      */
     public AMIdentity getFirstAMIdentity(
             SSOToken ssoToken,
             String name,
             IdType idType,
             String realm)
             throws Exception {
         Set<AMIdentity> set = getAMIdentity(ssoToken, name, idType, realm);
         AMIdentity amid = null;
         for (Iterator itr = set.iterator(); itr.hasNext();) {
             amid = (AMIdentity)itr.next();
         }
         return (amid);
     }
     
     /**
      * Creates a dummy user in the specified realm.
      * (a) The user name (sn and cn) are equal to  sn or cn plus the supplied
      *     suffix string
      * (b) The user password is concatanation of entityName and suffix
      * (c) User status is set to Active
      */
     public AMIdentity createDummyUser(
             SSOToken ssoToken,
             String parentRealm,
             String entityName,
             String suffix)
             throws Exception {
         Map<String, Set<String>> map = new HashMap<String, Set<String>>();
         putSetIntoMap("sn", map, "sn" + suffix);
         putSetIntoMap("cn", map, "cn" + suffix);
         putSetIntoMap("userpassword", map, entityName + suffix);
         putSetIntoMap("inetuserstatus", map, "Active");
         return createIdentity(ssoToken, parentRealm, IdType.USER, entityName +
                 suffix, map);
     }
     
     /**
      * Returns the parent realm
      */
     public String getParentRealm(String realm)
     throws Exception {
         int idx = realm.lastIndexOf("/");
         if (idx == -1) {
             throw new RuntimeException("Incorrect Realm, " + realm);
         }
         return (idx == 0) ? "/" : realm.substring(0, idx);
     }
     
     /**
      * Creates a new realm under the specified realm
      */
     public void createSubRealm(SSOToken ssoToken, String realm)
     throws Exception {
         if ((realm != null) && !realm.equals("/")) {
             String parentRealm = getParentRealm(realm);
             createSubRealm(ssoToken, parentRealm);
             OrganizationConfigManager orgMgr = new OrganizationConfigManager(
                     ssoToken, parentRealm);
             int idx = realm.lastIndexOf("/");
             orgMgr.createSubOrganization(realm.substring(idx+1), null);
             assert (orgMgr.getSubOrganizationNames().contains(realm));
         }
     }
 
     /**
      * Deletes a realm
      */
     public void deleteRealm(SSOToken ssoToken, String realm)
     throws Exception {
         if ((realm != null) && !realm.equals("/")) {
             String parentRealm = getParentRealm(realm);
             OrganizationConfigManager orgMgr = new
                     OrganizationConfigManager(ssoToken, parentRealm);
             int idx = realm.lastIndexOf("/");
             orgMgr.deleteSubOrganization(realm.substring(idx+1), true);
             deleteRealm(ssoToken, parentRealm);
         }
     }
     
     /**
     * Adds a User Identity to a Group or a Role Identity
      */
     public void addUserMember(SSOToken ssotoken, String userName,
             String memberName, IdType memberType)
             throws Exception {
        Set setUser = getAMIdentity(ssotoken, userName, IdType.USER, realm);
        Set setMember = getAMIdentity(ssotoken, memberName, memberType, realm);
         AMIdentity amidUser = null;
         AMIdentity amidMember = null;
         Iterator itr;
         for (itr = setUser.iterator(); itr.hasNext();) {
             amidUser = (AMIdentity)itr.next();
         }
         for (itr = setMember.iterator(); itr.hasNext();) {
             amidMember = (AMIdentity)itr.next();
         }
         amidMember.addMember(amidUser);
     }
     
     /**
      * This method searches and retrieves a list of realm
      * @param ssotoken SSO token object
      * @param pattern realm name or pattern
      * @return a set of realm name
      */
     public Set searchRealms(SSOToken ssotoken, String pattern)
     throws Exception  {
         entering("searchRealms", null);
        Set realmNames = searchIdentities(ssotoken, pattern, IdType.REALM);
         exiting("searchRealms");
         return realmNames;
     }
     
     /**
      * This method searches and retrieves a list of identity
      * @param ssotoken SSO token object
      * @param pattern realm name or pattern
      * @type  identity type - user, role, filtered role, group, agent
      * @return a set of identity name
      */
    public Set searchIdentities(SSOToken ssotoken, String pattern, IdType type)
     throws Exception  {
         entering("searchIdentities", null);
         AMIdentityRepository repo = new AMIdentityRepository(
                ssotoken, realm);
         IdSearchControl searchControl = new IdSearchControl();
         IdSearchResults results = repo.searchIdentities(type, pattern,
                 searchControl);
         log(Level.FINE, "searchIdentities", "Searching for " + type.getName() +
                 " " + pattern + "...");
         Set idNames = results.getSearchResults();
         if ((idNames != null) && (!idNames.isEmpty())) {
             Iterator iter = idNames.iterator();
             AMIdentity amIdentity;
             while (iter.hasNext()) {
                 amIdentity = (AMIdentity) iter.next();
                 log(Level.FINEST, "searchIdentities", "Id name: " +
                         amIdentity.getName() +
                         " & UID = "  + amIdentity.getUniversalId());
             }
         } else {
             log(Level.FINE, "searchIdentities",
                     "Could not find identity name " + pattern);
         }
         exiting("searchIdentities");
         return idNames;
     }
     
     /**
      * This method retrieves the configuration key and values by the prefix 
      * string and store them in a map.
      * @param prefixName key prefix string
      * @param cfgFileName properties config file name
      * @return map of configuration
      */
     public Map getDataFromCfgFile(String prefixName, String cfgFileName)
     throws Exception {
         Map cfgMapTemp = new HashMap();
         Map cfgMapNew = new HashMap();
         cfgMapTemp = getMapFromResourceBundle(cfgFileName);
         Set keys = cfgMapTemp.keySet();
         Iterator keyIter = keys.iterator();
         String key;
         String value;
         while (keyIter.hasNext()) {
             key = keyIter.next().toString();
             value = cfgMapTemp.get(key).toString();
             if (key.substring(0, prefixName.length()).equals(prefixName))
                 cfgMapNew.put(key,value);
         }
         log(Level.FINEST, "getDataFromCfgFile", cfgMapNew.toString());
         if (cfgMapNew.isEmpty()) {
             log(Level.SEVERE, "getDataFromCfgFile",
                     "Config data map is empty");
             assert false;
         }
         return cfgMapNew;
     }
     
     /**
      * This method checks and compare IDM exception error message and error 
      * code with expected error message and code. 
      * @param IdRepoException idm exception
      * @param eMessage expected error message
      * @param eCode expected error code
      * @return true if match
      */
     public boolean checkIDMExpectedErrorMessageCode(IdRepoException e, 
             String eMessage, String eCode)
     throws Exception {
         boolean isMatch = false;
         String errorCode = e.getErrorCode();
         String errorMessage = e.getMessage();
         log(Level.FINEST, "checkExpectedMessageErrorCode", "Error message: " +
                 e.getMessage() + " error code: " + e.getErrorCode());
         log(Level.FINEST, "checkExpectedMessageErrorCode", 
                 "Expected message: " + eMessage + " expected error code: " +
                 eCode);
         if (errorCode.equals(eCode) && errorMessage.indexOf(eMessage) >= 0) {
             log(Level.FINE, "checkExpectedMessageErrorCode",
                     "Error code and message match");
             isMatch = true;
         }  
         return isMatch;
     }
 }
