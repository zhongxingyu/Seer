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
 * $Id: AuthContextLogout.java,v 1.1 2008-08-20 22:09:58 cmwesley Exp $
  *
  * Copyright 2008 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.authentication;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOTokenManager;
 import com.sun.identity.authentication.AuthContext;
 import com.sun.identity.authentication.spi.AuthLoginException;
 import com.sun.identity.qatest.common.FederationManager;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.authentication.AuthTestConfigUtil;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import org.testng.Reporter;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 
 /**
  * Automates the authentication test case Core_19.
  */
 
 /**
  * The class AuthContextLogout is used to test whether 
  * <code>AuthContext.logout</code> destroys the underlying session for a 
  * successfully authenticated user.
  */
 public class AuthContextLogout extends TestCommon {
     private ResourceBundle testResources;
     private String url;
     private String moduleName;
     private String configrbName = "authenticationConfigData";
     private WebClient webClient;
     private List moduleConfigData;
     private String moduleServiceName;
     private String moduleSubConfig;
     private String moduleSubConfigId;
     private String userName;
     private String password;
     private String logoutURL;
     private String absoluteRealm;
     private FederationManager fm;
     private List<String> testUserList = new ArrayList<String>();
     private AuthTestConfigUtil moduleConfig;
     private String testDescription;
     
     /**
      * Default Constructor
      */
     public AuthContextLogout() {
         super("AuthContextLogout");
         url = getLoginURL("/");
         logoutURL = protocol + ":" + "//" + host + ":" + port + uri 
                 + "/UI/Logout";
         String famadmURL = protocol + ":" + "//" + host + ":" + port + uri;
         fm = new FederationManager(famadmURL);
         testResources = ResourceBundle.getBundle("authentication" + 
                 fileseparator + "AuthContextLogout");
         moduleConfig = new AuthTestConfigUtil(configrbName);
     }
     
     /**
      * Set up the system for testing.  Create an authentication instance,
      * a user, and a realm before the logout test.
      * 
      */
     @Parameters({"testRealm"}) 
     @BeforeClass(groups={"ds_ds", "ds_ds_sec"})
     public void setup(String testRealm) 
     throws Exception {
         Object[] params = {testRealm};
         entering("setup", params);
         SSOToken idToken = getToken(adminUser, adminPassword, realm);
         webClient = new WebClient();
         try {
             moduleName = testResources.getString("am-auth-logout-module");
             userName = testResources.getString("am-auth-logout-user");
             password = testResources.getString("am-auth-logout-password");
             testDescription = testResources.getString(
                     "am-auth-logout-test-description");
 
             Map modMap = moduleConfig.getModuleData(moduleName);
             moduleServiceName = moduleConfig.getModuleServiceName();
             moduleSubConfig = moduleConfig.getModuleSubConfigName();
             moduleSubConfigId = moduleConfig.getModuleSubConfigId();
             moduleConfigData = moduleConfig.getListFromMap(modMap, moduleName);
             
             absoluteRealm = testRealm;
             if (!testRealm.equals("/")) {
                 if (testRealm.indexOf("/") != 0) {
                     absoluteRealm = "/" + testRealm;
                 }
                 log(Level.FINE, "setup", "Creating the sub-realm " + testRealm);
                 moduleConfig.createRealms(absoluteRealm);
             }
             
             log(Level.FINEST, "setup", "moduleName = " + moduleName);
             log(Level.FINEST, "setup", "Realm = " + testRealm);
             log(Level.FINEST, "setup", "User = " + userName);
             log(Level.FINEST, "setup", "Password = " + password);
             
             Reporter.log("ModuleName: " + moduleName);
             Reporter.log("Realm: " + testRealm);
             Reporter.log("User: " + userName);
             Reporter.log("Password: " + password);
 
             log(Level.FINEST, "setup", "ModuleServiceName: " +
                     moduleServiceName);
             log(Level.FINEST, "setup", "ModuleSubConfig: " +
                     moduleSubConfig);
             log(Level.FINEST, "setup", "ModuleSubConfigId: " +
                     moduleSubConfigId);
             log(Level.FINE, "setup", "Creating the authentication module " + 
                     moduleSubConfigId);            
             moduleConfig.createModuleInstances(testRealm, moduleServiceName,
                     moduleSubConfig, moduleConfigData, moduleSubConfigId);
 
             log(Level.FINE, "setup", "Creating user " + userName + "...");
             List<String> userList = new ArrayList<String>();
             userList.add("sn=" + userName);
             userList.add("cn=" + userName);
             userList.add("userpassword=" + password);
             userList.add("inetuserstatus=Active");            
             moduleConfig.setTestConfigRealm(testRealm);
             moduleConfig.createUser(userList, userName);
         } catch(Exception e) {
             cleanup(testRealm);
             log(Level.SEVERE, "setup", e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             if (idToken != null) {
                 destroyToken(idToken);
             }
             consoleLogout(webClient, logoutURL);
         }
         exiting("setup");
     }
     
     /**
      * Tests <code>AuthContext.logout</code> by authenticating a user via remote
      * module-based authentication, retrieving the user's <code>SSOToken</code>,
      * invoking <code>AuthContext.logout</code> on the token, and verifying that
      * the token is no longer valid.
      * testRealm - the realm in which the logout test will take place
      */
     @Parameters({"testRealm"}) 
     @Test(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void testAuthContextLogout(String testRealm)
     throws Exception {
         Object[] params = {testRealm};
         entering("testAuthContextLogout", params);  
         log(Level.FINE, "testAuthContextLogout", "Test Description: " + 
                 testDescription);
         Reporter.log("Test Description: " + testDescription);
         AuthContext lc = null;
         Callback[] callbacks = null;
         SSOToken userToken = null;
         boolean sessionValid = false;
 
         try {
             lc = new AuthContext(testRealm);
             log(Level.FINE, "testAuthContextLogout", 
                     "Calling AuthContext.login with the " + moduleSubConfig + 
                     " module.");
             lc.login(AuthContext.IndexType.MODULE_INSTANCE, moduleSubConfig);
         } catch (AuthLoginException le) {
             log(Level.SEVERE, "testAuthContextLogout", 
                     "An exception occurred during AuthContext.login()");
             log(Level.SEVERE, "testAuthContextLogout", le.getMessage());
             le.printStackTrace();
             assert false;
         }
         
         while (lc.hasMoreRequirements()) {
             callbacks = lc.getRequirements();
             if (callbacks != null) {
                 try {
                     for (int i = 0; i < callbacks.length; i++) {
                         if (callbacks[i] instanceof NameCallback) {
                             NameCallback namecallback =
                                     (NameCallback)callbacks[i];
                            namecallback.setName(moduleName);
                         }
                         if (callbacks[i] instanceof PasswordCallback) {
                             PasswordCallback passwordcallback =
                                     (PasswordCallback)callbacks[i];
                             passwordcallback.setPassword(
                                     password.toCharArray());
                         }
                     }
                     lc.submitRequirements(callbacks);
                 } catch (Exception e) {
                     log(Level.SEVERE, "testAuthContextLogout", e.getMessage());
                     e.printStackTrace();
                     return;
                 }
             }
         }
         if (lc.getStatus() == AuthContext.Status.SUCCESS) {
             try {
                 userToken = lc.getSSOToken();                
                 log(Level.FINE, "testAuthContextLogout", 
                         "Verifying expected behavior that the user token is " +
                        "valid after successful authentication ...");
                 sessionValid = 
                         SSOTokenManager.getInstance().isValidToken(userToken);
                 if (!sessionValid) {
                     log(Level.SEVERE, "testAuthContextLogout", 
                             "Token for user " + userName + " is not valid");
                     assert false;
                 }
 
                 log(Level.FINE, "testAuthContextLogout", 
                         "Calling AuthContext.logout for user " + userName);
                 lc.logout();
 
                 log(Level.FINE, "testAuthContextLogout", 
                         "Verifying expected behavior that the user token is " +
                         "no longer valid after logout");
                sessionValid = 
                        SSOTokenManager.getInstance().isValidToken(userToken);
                 if (sessionValid) {
                     log(Level.SEVERE, "testAuthContextLogout", 
                             "Token for user " + userName + 
                             " is still valid after logout");
                 }
                 assert !sessionValid;        
             } catch (Exception ex) {
                 log(Level.SEVERE, "testAuthContextLogout", ex.getMessage());
                 ex.printStackTrace();
                 throw ex;
             } finally {
                 if (sessionValid) {
                     destroyToken(userToken);
                 }
             }
             exiting("testAuthContextLogout");
         } else {
             log(Level.SEVERE, "testAuthContextLogout", "The user " + userName + 
                     " did not authenticate successfully.");
             assert false;
         }
     }
     
     /**
      * Performs cleanup after tests are done.
      * Deletes the authentication instances, users, and realms created by this 
      * test scenario.
      * testRealm - the realm which will be deleted 
      */
     @Parameters({"testRealm"})
     @AfterClass(groups={"ds_ds", "ds_ds_sec"})
     public void cleanup(String testRealm)
     throws Exception {
         Object[] params = {testRealm};
         entering("cleanup", params);
         SSOToken idToken = getToken(adminUser, adminPassword, realm);
 
         if (webClient == null) {
             webClient = new WebClient();
         }
         try {
             log(Level.FINEST, "cleanup", url);
             List<String> listModInstance = new ArrayList<String>();
             listModInstance.add(moduleSubConfig);
             consoleLogin(webClient, url, adminUser, adminPassword);
 
             log(Level.FINE, "cleanup", "Deleting auth instance(s) " + 
                     listModInstance + " ...");
             if (FederationManager.getExitCode(fm.deleteAuthInstances(
                     webClient, testRealm, listModInstance)) != 0) {
                 log(Level.SEVERE, "cleanup", 
                         "deleteAuthInstances famadm command failed");
             }
 
             testUserList.add(userName);
             log(Level.FINE, "cleanup", "Deleting user(s) " + testUserList + 
                         "...");                                
             if (FederationManager.getExitCode(fm.deleteIdentities(webClient, 
                     testRealm, testUserList, "User")) != 0) {
                 log(Level.SEVERE, "cleanup", 
                             "deleteIdentities famadm command failed");
             }
             absoluteRealm = testRealm;
             if (!absoluteRealm.equals("/")) {
                 if (absoluteRealm.indexOf("/") != 0) {
                     absoluteRealm = "/" + testRealm;
                 }             
                 log(Level.FINE, "cleanup", "Deleting the sub-realm " + 
                         absoluteRealm);
                 if (FederationManager.getExitCode(fm.deleteRealm(webClient, 
                         absoluteRealm, true)) != 0) {
                     log(Level.SEVERE, "cleanup", 
                             "deleteRealm famadm command failed");
                 }
             }
         } catch(Exception e) {
             log(Level.SEVERE, "cleanup", e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             destroyToken(idToken);
             consoleLogout(webClient, logoutURL);
         }
         exiting("cleanup");
     }
     
 }
