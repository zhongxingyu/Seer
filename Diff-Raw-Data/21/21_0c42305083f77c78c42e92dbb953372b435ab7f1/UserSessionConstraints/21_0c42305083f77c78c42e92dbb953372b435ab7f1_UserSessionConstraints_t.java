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
 * $Id: UserSessionConstraints.java,v 1.4 2008-05-22 00:58:22 srivenigan Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.session;
 
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.qatest.common.DelegationCommon;
 import com.sun.identity.qatest.common.IDMCommon;
 import com.sun.identity.qatest.common.SMSCommon;
 import com.sun.identity.qatest.common.TestCommon;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import org.testng.Reporter;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * This class is used to test 'Active Sessions' dynamic attribute for 
  * two users at the User level. All the tests depend on 
  * constraint - Resulting behavior if session quota exhausted 
  *                which can be DENY_ACESS/DESTROY_OLD_SESSION 
  */
 public class UserSessionConstraints extends TestCommon {
 
     private SSOToken admintoken;
     private IDMCommon idmc;
     private Iterator itr;
     private SMSCommon smsc;
     private DelegationCommon delc;
     private Map map;
     private Map quotamap;
     private Set set;
     private String testUserWithSrvc = "sessConsTestWithSrvc";
     private String testUserWithoutSrvc = "sessConsTestWithoutSrvc";
     private String quotaConst;
     private String resultBehavior;
     private String sessionsrvc = "iPlanetAMSessionService";
     private String srvcType = "Dynamic";
     private String quotaAttr = "iplanet-am-session-quota-limit";
     private boolean dynSrvcRealmAssigned = false;
     private boolean cleanedUp = false;
     
     /**
      * SessionConstraints Constructor
      * Creates admintoken.
      *
      * @throws java.lang.Exception
      */
     public UserSessionConstraints() throws Exception {
         super("UserSessionConstraints");
         admintoken = getToken(adminUser, adminPassword, basedn);
         idmc = new IDMCommon();  
         delc = new DelegationCommon("UserSessionConstraints");
         smsc = new SMSCommon(admintoken);
         quotamap = new HashMap();
     }
 
     /**
      * Initialization method. Setup:
      * (a) Creates adminuser, user Identities 
      * (b) Parameter inheritancelevel has values Global/Realm/User
      * (c) Validates that session service is available at each inheritance
      *     level, and if not assigns the service and sets value of active 
      *     number of sessions to "1"
      * 
      * @throws java.lang.Exception
      */
     @BeforeClass(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void setup()
     throws Exception {
         entering("setup", null);
         try {
             set = smsc.getAttributeValueFromSchema(sessionsrvc,
                     "iplanet-am-session-enable-session-constraint", "Global");
             Iterator itr = set.iterator();
             quotaConst = (String) itr.next();            
             set = smsc.getAttributeValueFromSchema(sessionsrvc,
                     "iplanet-am-session-constraint-resulting-behavior", 
                     "Global");
             itr = set.iterator();
             resultBehavior = (String) itr.next();     
             log(Level.FINE, "setup", "Resulting behavior if session quota " +
                     "exhausted is set to: " + resultBehavior);  
             if (quotaConst.equals("OFF")) {
                log(Level.CONFIG, "setup", "CONFIG FAILURE: Session constraints " +
                         "testcases must be run with enable session quota " +
                         "constraints attribute set to 'ON' " );
                Reporter.log("CONFIG FAILURE: Session constraints " +
                         "testcases must be run with enable session quota " +
                         "constraints attribute set to 'ON' ");
                 assert false;
             }
             idmc.createDummyUser(admintoken, realm, "", testUserWithSrvc);
             log(Level.FINE,"setup", "Created user " + 
                     testUserWithSrvc + " identity");
 
             idmc.createDummyUser(admintoken, realm, "", testUserWithoutSrvc);
             log(Level.FINE,"setup", "Created user " + 
                     testUserWithoutSrvc + " identity");
             quotamap = fillQuotaMap(quotaAttr, "1");
             delc.assignServiceToUser(admintoken, testUserWithSrvc, 
                         sessionsrvc, quotamap, realm);
             log(Level.FINEST, "setup", "Session Service Successfully " +
             		"assigned to user " + testUserWithSrvc);
         } catch(Exception e) {
             cleanup();
             log(Level.SEVERE, "setup", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("setup");
     }
   
     /**
      * Tests the following case: 
      * (a) Max session quota set to "1" for one user with session service
      * (b) Session service is not assigned for other user at user level
      * (c) Set resulting behavior if session quota exhausted to DENY_ACCESS
      * (d) Validates that only one session can be created for user with
      *     session service and new session is denied access
      * (e) Validates that more than one session can be created for user 
      *     without session service 
      * 
      * @throws java.lang.Exception
      */
     @Test(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void testQuotaDAWithSessionSrvcAtGlobal()
     throws Exception {
         entering("testQuotaDAWithSessionSrvcAtGlobal", null);
         SSOToken usrtokenWithSrvcOrig = null;
         SSOToken usrtokenWithSrvcNew = null;
         SSOToken usrtokenWithoutSrvcOrig = null;
         SSOToken usrtokenWithoutSrvcNew = null;
         log(Level.FINE, "testQuotaDAWithSessionSrvcAtGlobal",
                 "This testcase validates session quota for one user with session " +
                 "service at user level and for other user at global level when " +
                 "resulting behavior set to DENY_ACCESS");
         Reporter.log("Test Description: This testcase validates session quota " +
                 "for one user with session service at user level and for other " +
                 "user at global level when resulting behavior set to DENY_ACCESS");
         try {
             if (smsc.isServiceAssigned(sessionsrvc, realm)) {
                 smsc.unassignDynamicServiceRealm(sessionsrvc, realm);
             }
             String globalActiveSessions =
                             getGlobalSessionQuotaAttribute(sessionsrvc, 
                             quotaAttr, srvcType);
             if (globalActiveSessions.equals("1")) {
                 quotamap = fillQuotaMap(quotaAttr, "5");
                 smsc.updateGlobalServiceDynamicAttributes(sessionsrvc,
                             quotamap);
                 globalActiveSessions = getGlobalSessionQuotaAttribute(
                             sessionsrvc, quotaAttr, srvcType);
             }
             assert !globalActiveSessions.equals("1");
             if (resultBehavior.equals("DENY_ACCESS")) {
                 usrtokenWithoutSrvcOrig = getToken(testUserWithoutSrvc, 
                                         testUserWithoutSrvc, basedn);
                 usrtokenWithoutSrvcNew = getToken(testUserWithoutSrvc, 
                                         testUserWithoutSrvc, basedn);
                 usrtokenWithSrvcOrig = getToken(testUserWithSrvc, 
                                         testUserWithSrvc, basedn);
                 try {
                     usrtokenWithSrvcNew = getToken(testUserWithSrvc,
                             testUserWithSrvc, basedn);
                     log(Level.SEVERE, "testQuotaDAWithSessionSrvcAtGlobal", 
                             "ERROR: Deny access case for user failed ");
                     assert false;
                 } 
                 catch (Exception e) {
                 	log(Level.SEVERE, "testQuotaDAWithSessionSrvcAtGlobal",
                                 "Cannot create new token for user: "
                     		  + testUserWithSrvc + "\n " + e.getMessage());
                         e.printStackTrace();
                 }
                 log(Level.FINE, "testQuotaDAWithSessionSrvcAtGlobal", 
                         "Original token and new token are valid here");
                 assert validateToken(usrtokenWithoutSrvcOrig);
                 assert validateToken(usrtokenWithoutSrvcNew);
                 assert validateToken(usrtokenWithSrvcOrig);
                 assert !validateToken(usrtokenWithSrvcNew);
             } else {
                 log(Level.FINE, "testQuotaDAWithSessionSrvcAtGlobal",
                         "Resulting behaviour attribute" +
                         "do not apply for this testcase");
                 Reporter.log("Note: Resulting behaviour attribute " +
                         "do not apply for this testcase");
             }
         } catch (Exception e) {
             cleanup();
             cleanedUp = true;
             log(Level.SEVERE, "testQuotaDAWithSessionSrvcAtGlobal",
                     e.getMessage()); 
             e.printStackTrace();
         } finally {
             if (!cleanedUp) {
                 destroyToken(usrtokenWithoutSrvcOrig);
                 destroyToken(usrtokenWithoutSrvcNew);
                 destroyToken(usrtokenWithSrvcOrig);
                 destroyToken(usrtokenWithSrvcNew);     
             }
         }
         exiting("testQuotaDAWithSessionSrvcAtGlobal");
     }
 
     /**
      * Tests the following case: 
      * (a) Max session quota set to "1" for one user with session service
      * (b) Session service is not assigned for other user at user level
      * (c) Set resulting behavior if session quota exhausted to DENY_ACCESS
      * (d) Validates that only one session can be created for user with
      *     session service and new session is denied access
      * (e) Validates that more than one session can be created for user 
      *     without session service 
      * 
      * @throws java.lang.Exception
      */
     @Test(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"}, 
     dependsOnMethods = {"testQuotaDAWithSessionSrvcAtGlobal"})
     public void testQuotaDOSWithSessionSrvcAtGlobal()
     throws Exception {
         entering("testQuotaDOSWithSessionSrvcAtGlobal", null);
         SSOToken usrtokenWithSrvcOrig = null;
         SSOToken usrtokenWithSrvcNew = null;
         SSOToken usrtokenWithoutSrvcOrig = null;
         SSOToken usrtokenWithoutSrvcNew = null;
         log(Level.FINE, "testQuotaDOSWithSessionSrvcAtGlobal",
                 "This testcase validates session quota for one user with session" +
                 "service at user level and for other user at global level when " +
                 "resulting behavior set to DESTROY_OLD_SESSION");
         Reporter.log("Test Description: This testcase validates session quota " +
                 "for one user with session service at user level and for other " +
                 "user at global level when resulting behavior set to " +
                 "DESTROY_OLD_SESSION");
         try {
             if (resultBehavior.equals("DESTROY_OLD_SESSION")) {
                 usrtokenWithoutSrvcOrig = getToken(testUserWithoutSrvc, 
                                         testUserWithoutSrvc, basedn);
                 usrtokenWithoutSrvcNew = getToken(testUserWithoutSrvc, 
                                         testUserWithoutSrvc, basedn);
                 usrtokenWithSrvcOrig = getToken(testUserWithSrvc, 
                                         testUserWithSrvc, basedn);
                 usrtokenWithSrvcNew = getToken(testUserWithSrvc,
                             testUserWithSrvc, basedn);
                 log(Level.FINE, "testQuotaDOSWithSessionSrvcAtGlobal", 
                         "Original token is valid and new " +
                         "token is valid here");
                 assert validateToken(usrtokenWithoutSrvcOrig);
                 assert validateToken(usrtokenWithoutSrvcNew);
                 assert validateToken(usrtokenWithSrvcNew);
             } else {
                 log(Level.FINE, "testQuotaDOSWithSessionSrvcAtGlobal",
                         "Resulting behaviour attribute" +
                         " do not apply for this testcase");
                 Reporter.log("Note: Resulting behaviour attribute " +
                         "do not apply for this testcase");
             }
         } catch (Exception e) {
             cleanup();
             cleanedUp = true;
             log(Level.SEVERE, "testQuotaDOSWithSessionSrvcAtGlobal",
                     e.getMessage()); 
             e.printStackTrace();
         } finally {
             if (!cleanedUp) {
                 destroyToken(usrtokenWithoutSrvcOrig);
                 destroyToken(usrtokenWithoutSrvcNew);
                 destroyToken(usrtokenWithSrvcNew);     
             }
         }
         exiting("testQuotaDOSWithSessionSrvcAtGlobal");
     }
 
     /**
      * Tests the following case: 
      * (a) Max session quota set to "1" for one user with session service
      * (b) Session service is not assigned for other user at user level
      * (c) Set resulting behavior if session quota exhausted to DENY_ACCESS
      * (d) Validates that only one session can be created for user with
      *     session service and new session is denied access
      * (e) Validates that more than one session can be created for user 
      *     without session service 
      * 
      * @throws java.lang.Exception
      */
     @Test(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void testQuotaDAWithSessionSrvcAtRealm()
     throws Exception {
         entering("testQuotaDAWithSessionSrvcAtRealm", null);
         SSOToken usrtokenWithSrvcOrig = null;
         SSOToken usrtokenWithSrvcNew = null;
         SSOToken usrtokenWithoutSrvcOrig = null;
         SSOToken usrtokenWithoutSrvcNew = null;
         log(Level.FINE, "testQuotaDAWithSessionSrvcAtRealm",
                 "This testcase validates session quota for one user with session" +
                 "service at user level and for other user at realm level when " +
                 "resulting behavior set to DENY_ACCESS");
         Reporter.log("Test Description: This testcase validates session quota " +
                 "for one user with session service at user level and for other " +
                 "user at realm level when resulting behavior set to DENY_ACCESS");        
         try {
             if (smsc.isServiceAssigned(sessionsrvc, realm)) {
                 smsc.unassignDynamicServiceRealm(sessionsrvc, realm);
             }
             quotamap = fillQuotaMap(quotaAttr, "5");
             smsc.assignDynamicServiceRealm(sessionsrvc, realm, quotamap);
             dynSrvcRealmAssigned = true;
             String realmActiveSessions = getRealmSessionQuotaAttribute(
                     sessionsrvc, realm);
             assert !realmActiveSessions.equals("1");
             if (resultBehavior.equals("DENY_ACCESS")) {
                 usrtokenWithoutSrvcOrig = getToken(testUserWithoutSrvc, 
                                         testUserWithoutSrvc, basedn);
                 usrtokenWithoutSrvcNew = getToken(testUserWithoutSrvc, 
                                         testUserWithoutSrvc, basedn);
                 usrtokenWithSrvcOrig = getToken(testUserWithSrvc, 
                                         testUserWithSrvc, basedn);
                 try {
                     usrtokenWithSrvcNew = getToken(testUserWithSrvc,
                             testUserWithSrvc, basedn);
                     log(Level.SEVERE, "testQuotaDAWithSessionSrvcAtRealm",
                             "ERROR: Deny access case for user failed ");
                     assert false;
                 } 
                 catch (Exception e) {
                       log(Level.SEVERE, "testQuotaDAWithSessionSrvcAtRealm",
                               "Cannot create new token for user: "
                     		  + testUserWithSrvc + "\n " + e.getMessage());
                         e.printStackTrace();
                 }
                 log(Level.FINE, "testQuotaDAWithSessionSrvcAtRealm", 
                         "Original token is valid and new " +
                         "token is valid here");
                 assert validateToken(usrtokenWithoutSrvcOrig);
                 assert validateToken(usrtokenWithoutSrvcNew);
                 assert validateToken(usrtokenWithSrvcOrig);
                 assert !validateToken(usrtokenWithSrvcNew);
             } else {
                 log(Level.FINE, "testQuotaDAWithSessionSrvcAtRealm",
                         "Resulting behaviour attribute " +
                         "do not apply for this testcase");
                 Reporter.log("Note: Resulting behaviour attribute " +
                         "do not apply for this testcase");
             }
         } catch (Exception e) {
             cleanup();
             cleanedUp = true;
             log(Level.SEVERE, "testQuotaDAWithSessionSrvcAtRealm",
                     e.getMessage()); 
             e.printStackTrace();
         } finally {
             if (!cleanedUp) {
                 destroyToken(usrtokenWithoutSrvcOrig);
                 destroyToken(usrtokenWithoutSrvcNew);
                 destroyToken(usrtokenWithSrvcOrig);
                 destroyToken(usrtokenWithSrvcNew);
             }
         }
         exiting("testQuotaDAWithSessionSrvcAtRealm");
     }
 
     /**
      * Tests the following case: 
      * (a) Max session quota set to "1" for one user with session service
      * (b) Session service is not assigned for other user at user level
      * (c) Set resulting behavior if session quota exhausted to DENY_ACCESS
      * (d) Validates that only one session can be created for user with
      *     session service and new session is denied access
      * (e) Validates that more than one session can be created for user 
      *     without session service 
      * 
      * @throws java.lang.Exception
      */
     @Test(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"}, 
     dependsOnMethods = {"testQuotaDAWithSessionSrvcAtRealm"})
     public void testQuotaDOSWithSessionSrvcAtRealm()
     throws Exception {
         entering("testQuotaDOSWithSessionSrvcAtRealm", null);
         SSOToken usrtokenWithSrvcOrig = null;
         SSOToken usrtokenWithSrvcNew = null;
         SSOToken usrtokenWithoutSrvcOrig = null;
         SSOToken usrtokenWithoutSrvcNew = null;
         log(Level.FINE, "testQuotaDOSWithSessionSrvcAtRealm",
                 "This testcase validates session quota for one user with session" +
                 "service at user level and for other user at realm level when " +
                 "resulting behavior set to DESTROY_OLD_SESSION");
         Reporter.log("Test Description: This testcase validates session quota " +
                 "for one user with session service at user level and for other " +
                 "user at realm level when resulting behavior set to " +
                 "DESTROY_OLD_SESSION");        
         try {
             if (resultBehavior.equals("DESTROY_OLD_SESSION")) {
                 usrtokenWithoutSrvcOrig = getToken(testUserWithoutSrvc, 
                                         testUserWithoutSrvc, basedn);
                 usrtokenWithoutSrvcNew = getToken(testUserWithoutSrvc, 
                                         testUserWithoutSrvc, basedn);
                 usrtokenWithSrvcOrig = getToken(testUserWithSrvc, 
                                         testUserWithSrvc, basedn);
                 usrtokenWithSrvcNew = getToken(testUserWithSrvc,
                             testUserWithSrvc, basedn);
                 log(Level.FINE, "testQuotaDOSWithSessionSrvcAtRealm", 
                         "Original token is valid and new " +
                         "token is valid here");
                 assert validateToken(usrtokenWithoutSrvcOrig);
                 assert validateToken(usrtokenWithoutSrvcNew);
                 assert validateToken(usrtokenWithSrvcNew);
             } else {
                 log(Level.FINE, "testQuotaDOSWithSessionSrvcAtRealm",
                         "Resulting behaviour attribute" +
                         "do not apply for this testcase");
                 Reporter.log("Note: Resulting behaviour attribute" +
                         " do not apply for this testcase");
             }
         } catch (Exception e) {
             cleanup();
             cleanedUp = true;
             log(Level.SEVERE, "testQuotaDOSWithSessionSrvcAtRealm",
                     e.getMessage()); 
             e.printStackTrace();
         } finally {
             if (!cleanedUp) {
                 destroyToken(usrtokenWithoutSrvcOrig);
                 destroyToken(usrtokenWithoutSrvcNew);
                 destroyToken(usrtokenWithSrvcNew);     
             }
         }
         exiting("testQuotaDOSWithSessionSrvcAtRealm");
     }
     
     /**
      * Cleans up the testcase attributes set in setup
      * 
      * @throws java.lang.Exception
      */
     @AfterClass(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void cleanup()
     throws Exception {
         entering("cleanup", null);
         try {
             if (dynSrvcRealmAssigned) {
                 smsc.unassignDynamicServiceRealm(sessionsrvc, realm);
             } 
             log(Level.FINEST, "cleanup", "Cleaning User:" 
                     + testUserWithSrvc);
             idmc.deleteIdentity(admintoken, realm, IdType.USER, testUserWithSrvc);
             log(Level.FINEST, "cleanup", "Cleaning User:" + testUserWithoutSrvc);
             idmc.deleteIdentity(admintoken, realm, IdType.USER, 
             		testUserWithoutSrvc);
         } catch (Exception e) {
             log(Level.SEVERE, "cleanup", e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
         	destroyToken(admintoken);
         }
         exiting("cleanup");
     }   
     
     /**
      * @param serviceName  servicename as String
      * @param attributeName name of attribute in the service
      * @param attributeType type of the attribute (User/Role etc.)
      * @return String containing global active sessions
      * 
      * @throws java.lang.Exception
      */
     private String getGlobalSessionQuotaAttribute(String serviceName, 
             String attributeName, 
             String attributeType) 
     throws Exception {
         String globalActiveSessions = "0";
         set = smsc.getAttributeValueFromSchema(serviceName,
               attributeName, attributeType);
         Iterator itr = set.iterator();
         globalActiveSessions = (String)itr.next();
         return globalActiveSessions;
     }
 
     /**
      * @param serviceName  servicename as String
      * @param realm  realm name as String
      * @return String containing realm active sessions
      * 
      * @throws java.lang.Exception
      */
     private String getRealmSessionQuotaAttribute(String serviceName, 
             String realm) 
     throws Exception {
         String realmActiveSessions = "0";
         map = new HashMap();
         map = smsc.getDynamicServiceAttributeRealm(serviceName, realm);
         set = (Set)map.get(quotaAttr);
         Iterator iter = set.iterator();
         realmActiveSessions = (String)iter.next();
         return realmActiveSessions;
     }
     
     /**
      * @param adminSSOToken  ssotoken of admin
      * @param userName  username String 
      * @param serviceName servicename as String
      * @param realm realm name as String
      * @return
      * @throws java.lang.Exception
      */
     private String getUserSessionQuotaAttribute(SSOToken adminSSOToken, 
             String userName,
             String serviceName,
             String realm) 
     throws Exception {
         String userActiveSessions = "0";
         map = new HashMap();
         map = delc.getServiceAttrsOfUser(adminSSOToken, userName, serviceName,
                    realm);
         set = (Set)map.get(quotaAttr);
         Iterator iter = set.iterator();
         userActiveSessions = (String)iter.next();
         return userActiveSessions;
     }
     
     /**
      * @param quotaattr quota schema Attribute
      * @param quotavalue value to be set 
      * @return Map map with quota value set
      * 
      * @throws java.lang.Exception
      */
     private Map fillQuotaMap(String quotaattr, String quotavalue) 
     throws Exception {
         Map quotaMap = new HashMap();
         set = new HashSet();
         set.add(quotavalue);
         quotaMap.put(quotaattr, set);        
         return quotaMap;
     }   
 }
