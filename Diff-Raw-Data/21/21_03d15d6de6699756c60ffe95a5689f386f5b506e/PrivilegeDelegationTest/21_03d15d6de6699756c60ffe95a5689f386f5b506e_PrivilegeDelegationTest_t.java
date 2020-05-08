 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
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
 * $Id: PrivilegeDelegationTest.java,v 1.2 2009-10-14 15:58:50 veiming Exp $
  */
 
 package com.sun.identity.entitlement;
 
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.authentication.AuthContext;
 import com.sun.identity.entitlement.opensso.OpenSSOUserSubject;
 import com.sun.identity.entitlement.opensso.SubjectUtils;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.security.AdminTokenAction;
 import java.security.AccessController;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class PrivilegeDelegationTest {
     private static final String DELEG_PRIVILEGE_NAME =
         "PrivilegeDelegationTestDelegationPrivilege";
     private static final String PRIVILEGE_NAME =
         "PrivilegeDelegationTestPrivilege";
     private static final String RESOURCE =
         "http://www.privilegedelegationtest.com";
     private static final String USER_NAME = "PrivilegeDelegationTestUser";
     private SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
         AdminTokenAction.getInstance());
     private AMIdentity user;
     private String realm = "/";
     private Privilege privilegeObject;
     private SSOToken token;
 
     @BeforeClass
     public void setup() throws Exception {
         user = createUser(USER_NAME);
         PrivilegeManager pm = PrivilegeManager.getInstance(realm,
             SubjectUtils.createSubject(adminToken));
         Map<String, Boolean> actions = new HashMap<String, Boolean>();
         actions.put("GET", Boolean.TRUE);
         Entitlement ent = new Entitlement(
             ApplicationTypeManager.URL_APPLICATION_TYPE_NAME,
             RESOURCE + "/index.html", actions);
         Privilege privilege = Privilege.getNewInstance();
         privilege.setName(PRIVILEGE_NAME);
         privilege.setEntitlement(ent);
         privilege.setSubject(new AnyUserSubject());
         pm.addPrivilege(privilege);
 
         privilegeObject = pm.getPrivilege(PRIVILEGE_NAME);
         token = authenticate(USER_NAME, USER_NAME);
     }
 
     @AfterClass
     public void cleanup() throws Exception {
         AMIdentityRepository amir = new AMIdentityRepository(
             adminToken, realm);
         Set<AMIdentity> identities = new HashSet<AMIdentity>();
         identities.add(user);
         amir.deleteIdentities(identities);
 
         PrivilegeManager pm = PrivilegeManager.getInstance(realm,
             SubjectUtils.createSubject(adminToken));
         pm.removePrivilege(PRIVILEGE_NAME);
     }
 
     @Test
     public void negativeTest() throws Exception {
         PrivilegeManager mgr = PrivilegeManager.getInstance(realm,
             SubjectUtils.createSubject(token));
         try {
             mgr.addPrivilege(privilegeObject);
             throw new Exception(
                 "PrivilegeDelegationTest.negativeTest: modify privilege should be deny");
         } catch (EntitlementException e) {
             if (e.getErrorCode() != 326) {
                 throw e;
             }
         }
         try {
             mgr.modifyPrivilege(privilegeObject);
             throw new Exception(
                 "PrivilegeDelegationTest.negativeTest: modify privilege should be deny");
         } catch (EntitlementException e) {
             if (e.getErrorCode() != 326) {
                 throw e;
             }
         }
         try {
             mgr.modifyPrivilege(privilegeObject);
             throw new Exception(
                 "PrivilegeDelegationTest.negativeTest: modify privilege should be deny");
         } catch (EntitlementException e) {
             if (e.getErrorCode() != 326) {
                 throw e;
             }
         }
         try {
             mgr.removePrivilege(PRIVILEGE_NAME);
             throw new Exception(
                 "PrivilegeDelegationTest.negativeTest: remove privilege should be deny");
         } catch (EntitlementException e) {
             if (e.getErrorCode() != 326) {
                 throw e;
             }
         }
 
     }
 
     @Test (dependsOnMethods = {"negativeTest"})
     public void positiveGetTest() throws Exception {
         ApplicationPrivilegeManager apm = createApplicationPrivilege(
            ApplicationPrivilege.PossibleAction.READ);
         PrivilegeManager mgr = PrivilegeManager.getInstance(realm,
             SubjectUtils.createSubject(token));
         mgr.getPrivilege(PRIVILEGE_NAME);
         try {
             mgr.modifyPrivilege(privilegeObject);
             throw new Exception(
                 "PrivilegeDelegationTest.positiveGetTest: modify privilege should be deny");
         } catch (EntitlementException e) {
             if (e.getErrorCode() != 326) {
                 throw e;
             }
         }
         apm.removePrivilege(DELEG_PRIVILEGE_NAME);
     }
 
     @Test (dependsOnMethods = {"positiveGetTest"})
     public void positiveModifyTest() throws Exception {
         ApplicationPrivilegeManager apm = createApplicationPrivilege(
            ApplicationPrivilege.PossibleAction.READ_MODIFY);
         PrivilegeManager mgr = PrivilegeManager.getInstance(realm,
             SubjectUtils.createSubject(token));
         mgr.getPrivilege(PRIVILEGE_NAME);
         mgr.modifyPrivilege(privilegeObject);
         apm.removePrivilege(DELEG_PRIVILEGE_NAME);
     }
 
     private ApplicationPrivilegeManager createApplicationPrivilege(
        ApplicationPrivilege.PossibleAction actions) 
         throws EntitlementException {
         ApplicationPrivilegeManager apm =
             ApplicationPrivilegeManager.getInstance(
             realm, SubjectUtils.createSubject(adminToken));
         ApplicationPrivilege ap = new ApplicationPrivilege(
             DELEG_PRIVILEGE_NAME);
         OpenSSOUserSubject sbj = new OpenSSOUserSubject();
         sbj.setID(user.getUniversalId());
         ap.setSubject(sbj);
 
         Set<String> res = new HashSet<String>();
         Map<String, Set<String>> appRes = new HashMap<String, Set<String>>();
         appRes.put(ApplicationTypeManager.URL_APPLICATION_TYPE_NAME, res);
         res.add(RESOURCE + "/*");
         ap.setApplicationResources(appRes);
         ap.setActionValues(actions);
         apm.addPrivilege(ap);
         return apm;
     }
 
     private AMIdentity createUser(String name)
         throws SSOException, IdRepoException {
         AMIdentityRepository amir = new AMIdentityRepository(
             adminToken, realm);
         Map<String, Set<String>> attrValues =new HashMap<String, Set<String>>();
         Set<String> set = new HashSet<String>();
         set.add(name);
         attrValues.put("givenname", set);
         attrValues.put("sn", set);
         attrValues.put("cn", set);
         attrValues.put("userpassword", set);
         return amir.createIdentity(IdType.USER, name, attrValues);
     }
 
     private SSOToken authenticate(
         String userName,
         String password
     ) throws Exception {
         AuthContext lc = new AuthContext(realm);
         lc.login();
         while (lc.hasMoreRequirements()) {
             Callback[] callbacks = lc.getRequirements();
             for (int i = 0; i < callbacks.length; i++) {
                 if (callbacks[i] instanceof NameCallback) {
                     NameCallback nc = (NameCallback) callbacks[i];
                     nc.setName(userName);
                 } else if (callbacks[i] instanceof PasswordCallback) {
                     PasswordCallback pc = (PasswordCallback) callbacks[i];
                     pc.setPassword(password.toCharArray());
                 } else {
                     throw new Exception("No callback");
                 }
             }
             lc.submitRequirements(callbacks);
         }
 
         return (lc.getStatus() != AuthContext.Status.SUCCESS) ? null : lc.
             getSSOToken();
     }
 
 }
