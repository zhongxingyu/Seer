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
 * $Id: ProfileAttributesTest.java,v 1.2 2007-08-07 23:35:20 rmisra Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.authentication;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.sun.identity.qatest.common.FederationManager;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.authentication.AuthTestConfigUtil;
 import com.sun.identity.qatest.common.authentication.AuthTestsValidator;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import org.testng.Reporter;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 
 /**
  * This class is called by the <code>ProfileAttributesTest</code>.
  * Performs the tests for User profile attributes .Test cases
  * coveres are Global Test cases
  * AMSubRealmAuth,Core_5,Core_7
  * 
  */
 public class ProfileAttributesTest extends TestCommon {
     
     private ResourceBundle testResources;
     private String testModule;
     private String locTestProfile;
     private String testAttribute;
     private String createUserProp;
     private boolean userExists;
     private String testUser;
     private String testUserpass;
     private String testPassmsg;
     private String testURL;
     private String servicename = "iPlanetAMAuthService";
     private List<String> attributevalues = new ArrayList<String>();
     private String lockoutPassmsg;
     private String warnPassmsg;
     private String failpage;
     private FederationManager fm;
     private WebClient webClient;
     private List moduleConfigData;
     private String moduleServiceName;
     private String moduleSubConfig;
     private String moduleSubConfigId;
     private String testModName;
     private String url;
     private String logoutURL;
     private String configrbName = "authenticationConfigData";
     private List<String> testUserList = new ArrayList<String>();
     
     /**
      * Default Constructor
      **/
     public ProfileAttributesTest() {
         super("ProfileAttributesTest");
         url = protocol + ":" + "//" + host + ":" + port + uri;
         logoutURL = url + "/UI/Logout";
         fm = new FederationManager(url);
     }
     
     /**
      * Reads the necessary test configuration and prepares the system
      * for user profile testing
      * - Create module instances
      * - Sets the profile attribute
      * - Create Users , If needed
      */
     @Parameters({"testProfile"})
    @BeforeClass(groups={"ds_ds","ds_ds_sec","ff_ds","ff_ds_sec"})
     public void setup(String testProfile)
     throws Exception {
         Object[] params = {testProfile};
         entering("setup", params);
         webClient = new WebClient();
         try {
             locTestProfile = testProfile;
             testAttribute = "am-auth-" + locTestProfile ;
             testResources = ResourceBundle.getBundle("ProfileAttributesTest");
             testModule = testResources.getString(testAttribute + "-test-module");
             createUserProp = testResources.getString(testAttribute
                     + "-test-createUser");
             userExists = new Boolean(createUserProp).booleanValue();
             testUser = testResources.getString(testAttribute + "-test-username");
             testUser.trim();
             testUserpass = testResources.getString(testAttribute
                     + "-test-userpassword");
             testUserpass.trim();
             testPassmsg = testResources.getString(testAttribute + "-test-passmsg");
             createModule(testModule);
             testURL = url + "?module=" + moduleSubConfig;
             String attributeVal = getProfileAttribute(locTestProfile);
             attributevalues.add(attributeVal);
             consoleLogin(webClient, url, adminUser, adminPassword);
             fm.setServiceAttributes(webClient, realm, servicename, attributevalues);
             if (!userExists) {
                 createUser(testUser, testUserpass);
             }
         } catch(Exception e) {
             log(Level.SEVERE, "setup", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
         exiting("setup");
     }
     
     /*
      * Validate the profile tests
      */
    @Test(groups={"ds_ds","ds_ds_sec","ff_ds","ff_ds_sec"})
     public void testProfile()
     throws Exception {
         entering("testProfile", null);
         Map executeMap = new HashMap();
         executeMap.put("Loginuser", testUser);
         executeMap.put("Loginpassword", testUserpass);
         executeMap.put("Passmsg", testPassmsg);
         executeMap.put("loginurl", testURL);
         executeMap.put("profileattr",locTestProfile);
         AuthTestsValidator profileTestValidator =
                 new AuthTestsValidator(executeMap);
         profileTestValidator.testProfile();
         exiting("testProfile");
     }
     
     /**
      * performs cleanup after tests are done.
      */
    @AfterClass(groups={"ds_ds","ds_ds_sec","ff_ds","ff_ds_sec"})
     public void cleanup()
     throws Exception {
         entering("cleanup", null);
         WebClient webClient = new WebClient();
         try {
             log(logLevel, "cleanup", url);
             log(logLevel, "cleanup", "Users:" + testUserList);
             List<String> listModInstance = new ArrayList<String>();
             listModInstance.add(moduleSubConfig);
             log(logLevel, "cleanup", "listModInstance" + listModInstance);
             consoleLogin(webClient, url, adminUser, adminPassword);
             HtmlPage page = (HtmlPage)fm.deleteAuthInstances(webClient,
                     realm, listModInstance);
             log(logLevel, "cleanup", "Page" + page.asXml());
             fm.deleteIdentities(webClient, realm, testUserList, "User");
         } catch(Exception e) {
             log(Level.SEVERE, "cleanup", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
         exiting("cleanup");
     }
     
     /**
      * Call Authentication Utility class to create the module instances
      * for a given module instance name
      * @param moduleName
      */
     private void createModule(String mName) {
         try {
             AuthTestConfigUtil moduleConfig =
                     new AuthTestConfigUtil(configrbName);
             Map modMap = moduleConfig.getModuleData(mName);
             moduleServiceName = moduleConfig.getModuleServiceName();
             moduleSubConfig = moduleConfig.getModuleSubConfigName();
             moduleSubConfigId = moduleConfig.getModuleSubConfigId();
             moduleConfigData = moduleConfig.getListFromMap(modMap, mName);
             log(logLevel, "createModule", "ModuleServiceName :" +
                     moduleServiceName);
             log(logLevel, "createModule", "ModuleSubConfig :" +
                     moduleSubConfig);
             log(logLevel, "createModule", "ModuleSubConfigId :" +
                     moduleSubConfigId);
             moduleConfig.createModuleInstances(moduleServiceName,
                     moduleSubConfig, moduleConfigData, moduleSubConfigId);
         } catch(Exception e) {
             log(Level.SEVERE, "createModule", e.getMessage(), null);
             e.printStackTrace();
         }
     }
     
     /**
      * Creates the required test users on the system
      * @param username
      * @param password
      **/
     private void createUser(String newUser, String userpassword) {
         List<String> userList = new ArrayList<String>();
         userList.add("sn=" + newUser);
         userList.add("cn=" + newUser);
         userList.add("userpassword=" + userpassword);
         userList.add("inetuserstatus=Active");
         log(logLevel, "createUser", "userList " + userList);
         testUserList.add(newUser);
         try {
             fm.createIdentity(webClient, realm, newUser, "User", userList);
         } catch(Exception e) {
             log(Level.SEVERE, "createUser", e.getMessage(), null);
             e.printStackTrace();
         }
     }
     
     /**
      * Returns the profile attribute based on the profile test performed
      *
      */
     private String getProfileAttribute(String profile){
         String testAttribute;
         if (profile.equals("dynamic")) {
             testAttribute = "iplanet-am-auth-dynamic-profile-creation=true";
         } else if(profile.equals("required")) {
             testAttribute = "iplanet-am-auth-dynamic-profile-creation=false";
         } else {
             testAttribute = "iplanet-am-auth-dynamic-profile-creation=ignore";
         }
         return testAttribute;
     }
 }
