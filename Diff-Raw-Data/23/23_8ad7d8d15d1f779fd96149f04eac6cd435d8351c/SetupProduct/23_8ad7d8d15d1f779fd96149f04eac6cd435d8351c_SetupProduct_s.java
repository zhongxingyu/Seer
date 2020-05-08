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
 * $Id: SetupProduct.java,v 1.19 2008-10-16 04:04:53 nithyas Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.setup;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.qatest.common.FederationManager;
 import com.sun.identity.qatest.common.LDAPCommon;
 import com.sun.identity.qatest.common.SMSCommon;
 import com.sun.identity.qatest.common.SMSConstants;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.TestConstants;
 import com.sun.identity.sm.ServiceConfig;
 import com.sun.identity.sm.ServiceConfigManager;
 import java.util.logging.Level;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 /**
  * This class configures the product. This means calling the configurator to
  * configure the deployed war and configure the datastores. If product is 
  * is already configured, only datastores are configured, depending upon flags
  * set in resource file.
  * 
  * This class is called from xml/opensso-common.xml
  */
 public class SetupProduct extends TestCommon {
     
     private String FILE_CLIENT_PROPERTIES = "AMConfig.properties";
     private Map properties = new HashMap();
     private SSOToken admintoken;
     List list;
     
     /**
      * This method configures the deployed war and datastores for different
      * servers.     
      */
     public SetupProduct(String serverName0, String serverName1)
     throws Exception {
         super("SetupProduct");
         
         try {
             boolean bserver0 = false;
             boolean bserver1 = false;
             boolean bserver2 = false;
             boolean bserver3 = false;
 
             String namingProtocol = "";
             String namingHost = "";
             String namingPort = "";
             String namingURI = "";
 
             String serverName2 = null;
             String serverName3 = null;
 
             ResourceBundle cfg0 = null;
             ResourceBundle cfg1 = null;
             ResourceBundle cfg2 = null;
             ResourceBundle cfg3 = null;
             ResourceBundle cfgData = null;
 
             Map<String, String> umDatastoreTypes = new HashMap<String,
                     String>();
             boolean bMulti = false;
 
             ResourceBundle gblCfgData = ResourceBundle.getBundle("config" +
                     fileseparator + "default" + fileseparator +
                     "UMGlobalConfig");
             SMSCommon smscGbl = new SMSCommon("config" + fileseparator +
                     "default" + fileseparator + "UMGlobalConfig");
 
             // Checks if both SERVER_NAME1 and SERVER_NAME2 are specified while
             // executing ant command for qatest. This loop is executed only if
             // both entries are sepcified.
             if ((serverName0.indexOf("SERVER_NAME1") == -1) &&
                     (serverName1.indexOf("SERVER_NAME2") == -1)) {
 
                 if (serverName0.indexOf(".") != -1) {
                     log(Level.SEVERE, "SetupProduct", "Server configuration " +
                             "file Configurator-" + serverName0 + ".properties" +
                             " cannot have \".\" in its name");
                     assert false;
                 }
                 if (serverName1.indexOf(".") != -1) {
                     log(Level.SEVERE, "SetupProduct", "Server configuration " +
                             "file Configurator-" + serverName1 + ".properties" +
                             " cannot have \".\" in its name");
                     assert false;
                 }
 
                 Map<String, String> configMapServer0 = new HashMap<String,
                         String>();
                 configMapServer0 = getMapFromResourceBundle(
                         "config" + fileseparator + "default" + fileseparator +
                         "ConfiguratorCommon");
                 configMapServer0.putAll(getMapFromResourceBundle(
                         "Configurator-" + serverName0));
                 createFileFromMap(configMapServer0, serverName + fileseparator +
                         "built" + fileseparator + "classes" + fileseparator +
                         "Configurator-" + serverName0 +
                         "-Generated.properties");
 
                 cfg0 = ResourceBundle.getBundle("Configurator-" + serverName0 +
                         "-Generated");
                 umDatastoreTypes.put("0", serverName0);
 
                 Map<String, String> configMapServer1 = new HashMap<String,
                         String>();
                 configMapServer1 = getMapFromResourceBundle(
                         "config" + fileseparator + "default" + fileseparator +
                         "ConfiguratorCommon");
                 configMapServer1.putAll(getMapFromResourceBundle(
                         "Configurator-" + serverName1));
                 createFileFromMap(configMapServer1, serverName + fileseparator +
                         "built" + fileseparator + "classes" + fileseparator +
                         "Configurator-" + serverName1 +
                         "-Generated.properties");
 
                 cfg1 = ResourceBundle.getBundle("Configurator-" + serverName1 +
                         "-Generated");
                 umDatastoreTypes.put("1", serverName1);
                 
                 log(Level.FINE, "SetupProduct", "Multiprotocol " +
                         "flag is set to: " +
                         cfg0.getString(
                         TestConstants.KEY_ATT_MULTIPROTOCOL_ENABLED));
 
                 if (cfg0.getString(TestConstants.KEY_ATT_MULTIPROTOCOL_ENABLED).
                         equalsIgnoreCase("true")) {
                     bMulti = true;
 
                     serverName2 = cfg0.getString(TestConstants.
                             KEY_ATT_IDFF_SP);
                     serverName3 = cfg0.getString(TestConstants.
                             KEY_ATT_WSFED_SP);
 
                     if (serverName2.indexOf(".") != -1) {
                         log(Level.SEVERE, "SetupProduct", "Server" +
                                 " configuration file Configurator-" +
                                 serverName2 + ".properties cannot have" +
                                 " \".\" in its name");
                         assert false;
                     }
                     if (serverName3.indexOf(".") != -1) {
                         log(Level.SEVERE, "SetupProduct", "Server" +
                                 " configuration file Configurator-" +
                                 serverName3 + ".properties cannot have" +
                                 " \".\" in its name");
                         assert false;
                     }
 
                     Map<String, String> configMapServer2 = new HashMap<String,
                             String>();
                     configMapServer2 = getMapFromResourceBundle(
                         "config" + fileseparator + "default" + fileseparator +
                         "ConfiguratorCommon");
                     configMapServer2.putAll(getMapFromResourceBundle(
                         "Configurator-" + serverName2));
                     createFileFromMap(configMapServer2, serverName +
                             fileseparator + "built" + fileseparator +
                             "classes" + fileseparator + "Configurator-" +
                             serverName2 + "-Generated.properties");
 
                     cfg2 = ResourceBundle.getBundle("Configurator-" +
                             serverName2 + "-Generated");
                     umDatastoreTypes.put("2", serverName2);
 
                     Map<String, String> configMapServer3 = new HashMap<String,
                             String>();
                     configMapServer3 = getMapFromResourceBundle(
                         "config" + fileseparator + "default" + fileseparator +
                         "ConfiguratorCommon");
                     configMapServer3.putAll(getMapFromResourceBundle(
                         "Configurator-" + serverName3));
                     createFileFromMap(configMapServer3, serverName +
                             fileseparator + "built" + fileseparator +
                             "classes" + fileseparator + "Configurator-" +
                             serverName3 + "-Generated.properties");
 
                     cfg3 = ResourceBundle.getBundle("Configurator-" +
                             serverName3 + "-Generated");
                     umDatastoreTypes.put("3", serverName3);
                 }
                 
                 // Initiating setp for server index 0. This is with refrence to
                 // definitions in resources/config/UMGlobalDatstoreConfig 
                 // resource bundle. This is done using famadm.jsp.
                 log(Level.FINE, "SetupProduct", "Initiating setup for " +
                         serverName0);                
                 String strURL = cfg0.
                         getString(TestConstants.KEY_AMC_NAMING_URL);
                 log(Level.FINE, "SetupProduct", "Server URL: " + strURL);
                 Map map = getURLComponents(strURL);
                 log(Level.FINE, "SetupProduct", "Server URL Components: " +
                         map);
                 namingProtocol = (String)map.get("protocol");
                 namingHost = (String)map.get("host");
                 namingPort = (String)map.get("port");
                 namingURI = (String)map.get("uri");
                 
                 list = new ArrayList();
                 
                 bserver0 = configureProduct(getConfigurationMap("Configurator-"
                         + serverName0 + "-Generated", namingProtocol,
                         namingHost, namingPort, namingURI), "0");
                 if (!bserver0) {
                     log(Level.FINE, "SetupProduct",
                             "Configuration failed for " + serverName0);
                     assert false;
                 } else {
                     if (bMulti)
                         createGlobalDatastoreFile(umDatastoreTypes,
                                 SMSConstants.QATEST_EXEC_MODE_ALL);
                     else
                         createGlobalDatastoreFile(umDatastoreTypes,
                                 SMSConstants.QATEST_EXEC_MODE_DUAL);
                     
                     cfgData = ResourceBundle.getBundle("config" +
                             fileseparator +
                             SMSConstants.UM_DATASTORE_PARAMS_PREFIX +
                             "-Generated");
                     String adminUser = cfg0.getString(
                             TestConstants.KEY_ATT_AMADMIN_USER);
                     String adminPassword = cfg0.getString(
                             TestConstants.KEY_ATT_AMADMIN_PASSWORD);
 
                     String loginURL = namingProtocol + ":" + "//" + 
                             namingHost + ":" + namingPort + namingURI + 
                             "/UI/Login";
                     String logoutURL = namingProtocol + ":" + "//" + 
                             namingHost + ":" + namingPort + namingURI + 
                             "/UI/Logout";
                     String famadmURL = namingProtocol + ":" + "//" + 
                             namingHost + ":" + namingPort + namingURI;
                         
                     int dCount = new Integer(cfgData.getString(
                             SMSConstants.UM_DATASTORE_PARAMS_PREFIX + "0." +
                             SMSConstants.UM_DATASTORE_COUNT)).intValue();
                     WebClient webClient = null;
                     try {
                         FederationManager famadm =
                                     new FederationManager(famadmURL);
                             webClient = new WebClient();
                             consoleLogin(webClient, loginURL, adminUser,
                                     adminPassword);
 
                             if (createDataStoreUsingfamadm(webClient,
                                     famadm, cfgData, 0, dCount)) {
                                 HtmlPage pageDStore =
                                         famadm.listDatastores(webClient, realm);
                                 if (FederationManager.
                                         getExitCode(pageDStore) != 0) {
                                     log(Level.SEVERE, "SetupProduct",
                                             "listDatastores famadm" +
                                             " command failed");
                                     assert false;
                                 }
                                 if ((gblCfgData.getString("UMGlobalConfig." +
                                         "deleteExistingDatastores")).
                                         equals("true")) {
                                     List datastoreList = smscGbl.
                                             getDatastoreDeleteList(
                                             getListFromHtmlPage(pageDStore), 0);
                                     if (datastoreList.size() != 0) {
                                         if (FederationManager.getExitCode(
                                                 famadm.deleteDatastores(
                                                 webClient, realm,
                                                 datastoreList)) != 0) {
                                             log(Level.SEVERE, "SetupProduct",
                                                     "deleteDatastores famadm" +
                                                     " command failed");
                                             assert false;
                                         }
                                     }
                                 }
                             } else {
                                 log(Level.SEVERE, "SetupProduct", "DataStore" +
                                     " configuration didn't succeed for " +
                                     namingHost);
                             }
                         } catch (Exception e) {
                             log(Level.SEVERE, "SetupProduct", e.getMessage());
                             e.printStackTrace();
                         } finally {
                             consoleLogout(webClient, logoutURL);
                         }
                 }
                 
                 // Initiating setp for server index 1. This is with refrence to
                 // definitions in resources/config/UMGlobalDatstoreConfig 
                 // resource bundle. This is for single server tests and uses
                 // client api's to do all the configuration.
                 log(Level.FINE, "SetupProduct", "Initiating setup for " +
                         serverName1);
                 bserver1 = configureProduct(
                         getConfigurationMap("Configurator-" + serverName1 +
                         "-Generated"), "1");
                 if (!bserver1) {
                     log(Level.FINE, "SetupProduct", "Configuration failed" +
                             " for " + serverName1);
                     assert false;
                 } else {
                     cfg1 = ResourceBundle.getBundle("Configurator-" +
                             serverName1 + "-Generated");
                     String strUMDatastore1 = cfg1.getString("umdatastore");
                     log(Level.FINE, "SetupProduct", "UM Datastore for " +
                             serverName1 + " is " + strUMDatastore1);
                         admintoken = getToken(adminUser, adminPassword, basedn);
                         SMSCommon smsc = new SMSCommon(admintoken, "config" +
                                 fileseparator + "default" + fileseparator +
                                 "UMGlobalConfig");
                         smsc.createDataStore(1, "config" + fileseparator +
                                 SMSConstants.UM_DATASTORE_PARAMS_PREFIX +
                                 "-Generated");
                         if ((gblCfgData.getString("UMGlobalConfig." +
                                 "deleteExistingDatastores")).equals("true"))
                             smsc.deleteAllDataStores(realm, 1);
                         modifyPolicyService(smsc, serverName1, 1, 0);
                 }
                 
                 // Configure other two servers (index 2 and 3 in resources/
                 // config/UMGlobalDatstoreConfig only if multiprotocol is 
                 // enabled. This flag is set in the server configuration file
                 // for server index 0.
                 if (bMulti) {
                     //configure multiple sp's
                     log(Level.FINE, "SetupProduct", "Initiating setup for " +
                             "multiple SP's");
 
                     // Initiating setp for server index 2. This is with refrence
                     // to definitions in resources/config/UMGlobalDatstoreConfig 
                     // resource bundle. This is done using famadm.jsp.
                     log(Level.FINE, "SetupProduct", "Initiating setup for " +
                             serverName2);                    
                     cfg2 = ResourceBundle.
                             getBundle("Configurator-" + serverName2 +
                             "-Generated");
                     strURL = cfg2.
                             getString(TestConstants.KEY_AMC_NAMING_URL);
                     log(Level.FINE, "SetupProduct", "Server URL: " + strURL);
                     map = getURLComponents(strURL);
                     log(Level.FINE, "SetupProduct", "Server URL Components: " +
                             map);
                     namingProtocol = (String)map.get("protocol");
                     namingHost = (String)map.get("host");
                     namingPort = (String)map.get("port");
                     namingURI = (String)map.get("uri");
                     
                     bserver2 = configureProduct(
                             getConfigurationMap("Configurator-" + serverName2 +
                             "-Generated", namingProtocol, namingHost, 
                             namingPort, namingURI), "2");
                     if (!bserver2) {
                         log(Level.FINE, "SetupProduct", "Configuration failed" +
                                 " for " + serverName2);
                         assert false;
                     } else {
                         String adminUser = cfg2.getString(
                                 TestConstants.KEY_ATT_AMADMIN_USER);
                         String adminPassword = cfg2.getString(
                                 TestConstants.KEY_ATT_AMADMIN_PASSWORD);
 
                         String loginURL = namingProtocol + ":" + "//" + 
                                 namingHost + ":" + namingPort + 
                                 namingURI + "/UI/Login";
                         String logoutURL = namingProtocol + ":" + "//" + 
                                 namingHost + ":" + namingPort + 
                                 namingURI + "/UI/Logout";
                         String famadmURL = namingProtocol + ":" + "//" + 
                                 namingHost + ":" + namingPort + 
                                 namingURI;
                         int dCount = new Integer(cfgData.getString(
                                 SMSConstants.UM_DATASTORE_PARAMS_PREFIX +
                                 "2." +
                                 SMSConstants.UM_DATASTORE_COUNT)).
                                 intValue();
                         WebClient webClient = null;
                         try {
                             FederationManager famadm =
                                     new FederationManager(famadmURL);
                             webClient = new WebClient();
                             consoleLogin(webClient, loginURL, adminUser,
                                     adminPassword);
 
                             if (createDataStoreUsingfamadm(webClient,
                                     famadm, cfgData, 2, dCount)) { 
                             HtmlPage pageDStore = famadm.listDatastores(
                                     webClient, realm);
                             if (FederationManager.
                                     getExitCode(pageDStore) != 0) {
                                 log(Level.SEVERE, "SetupProduct",
                                         "listDatastores famadm command failed");
                                 assert false;
                             }
                             if ((gblCfgData.getString("UMGlobalConfig." +
                                     "deleteExistingDatastores")).
                                     equals("true")) {
                                 List datastoreList = 
                                         smscGbl.getDatastoreDeleteList(
                                         getListFromHtmlPage(pageDStore), 2);
                                 if (datastoreList.size() != 0) {
                                     if (FederationManager.getExitCode(
                                             famadm.deleteDatastores(
                                             webClient, realm, datastoreList))
                                             != 0) {
                                         log(Level.SEVERE, "SetupProduct",
                                                 "deleteDatastores famadm" +
                                                 " command failed");
                                         assert false;
                                     }
                                 }
                             }
                         } else
                             log(Level.SEVERE, "SetupProduct", "DataStore" +
                                 " configuration didn't succeed for " + 
                                 namingHost);
                         } catch (Exception e) {
                             log(Level.SEVERE, "SetupProduct", e.getMessage());
                             e.printStackTrace();
                         } finally {
                             consoleLogout(webClient, logoutURL);
                         }
                     }
                     
                     // Initiating setp for server index 3. This is with refrence
                     // to definitions in resources/config/UMGlobalDatstoreConfig 
                     // resource bundle. This is done using famadm.jsp.
                     log(Level.FINE, "SetupProduct", "Initiating setup for " +
                             serverName3);                    
                     cfg3 = ResourceBundle.getBundle("Configurator-" +
                             serverName3 + "-Generated");
                     strURL = cfg3.
                             getString(TestConstants.KEY_AMC_NAMING_URL);
                     log(Level.FINE, "SetupProduct", "Server URL: " + strURL);
                     map = getURLComponents(strURL);
                     log(Level.FINE, "SetupProduct", "Server URL Components:" +
                             " " +  map);
                     namingProtocol = (String)map.get("protocol");
                     namingHost = (String)map.get("host");
                     namingPort = (String)map.get("port");
                     namingURI = (String)map.get("uri");
                     
                     bserver3 = configureProduct(
                             getConfigurationMap("Configurator-" + serverName3 +
                             "-Generated",
                             namingProtocol, namingHost, namingPort,
                             namingURI), "3");
                     if (!bserver3) {
                         log(Level.FINE, "SetupProduct", "Configuration failed" +
                                 " for " + serverName3);
                         assert false;
                     } else {
                         String adminUser = cfg3.getString(
                                 TestConstants.KEY_ATT_AMADMIN_USER);
                         String adminPassword = cfg3.getString(
                                 TestConstants.KEY_ATT_AMADMIN_PASSWORD);
 
                         String loginURL = namingProtocol + ":" + "//" + 
                                 namingHost + ":" + namingPort + 
                                 namingURI + "/UI/Login";
                         String logoutURL = namingProtocol + ":" + "//" + 
                                 namingHost + ":" + namingPort + 
                                 namingURI + "/UI/Logout";
                         String famadmURL = namingProtocol + ":" + "//" + 
                                 namingHost + ":" + namingPort + 
                                 namingURI;
 
                         int dCount = new Integer(cfgData.getString(
                                 SMSConstants.UM_DATASTORE_PARAMS_PREFIX +
                                 "3." + SMSConstants.UM_DATASTORE_COUNT)).
                                 intValue();
                         WebClient webClient = null;
                         try {
                             FederationManager famadm =
                                     new FederationManager(famadmURL);
                             webClient = new WebClient();
                             consoleLogin(webClient, loginURL, adminUser,
                                     adminPassword);
 
                             if (createDataStoreUsingfamadm(webClient,
                                     famadm, cfgData, 3, dCount)) {
                             HtmlPage pageDStore = famadm.listDatastores(
                                     webClient, realm);
                             if (FederationManager.
                                     getExitCode(pageDStore) != 0) {
                                 log(Level.SEVERE, "SetupProduct",
                                         "listDatastores famadm command failed");
                                 assert false;
                             }
                             if ((gblCfgData.getString("UMGlobalConfig." +
                                     "deleteExistingDatastores")).
                                     equals("true")) {
                                 List datastoreList = 
                                         smscGbl.getDatastoreDeleteList(
                                         getListFromHtmlPage(pageDStore), 3);
                                 if (datastoreList.size() != 0) {
                                     if (FederationManager.getExitCode(
                                             famadm.deleteDatastores(
                                             webClient, realm, datastoreList))
                                             != 0) {
                                         log(Level.SEVERE, "SetupProduct",
                                                 "deleteDatastores famadm" +
                                                 " command failed");
                                         assert false;
                                     }
                                 }
                             }
                         } else
                             log(Level.SEVERE, "SetupProduct", "DataStore" +
                                 " configuration didn't succeed for " +
                                 namingHost);
                         } catch (Exception e) {
                             log(Level.SEVERE, "SetupProduct", e.getMessage());
                             e.printStackTrace();
                         } finally {
                             consoleLogout(webClient, logoutURL);
                         }
                     }
                 }
             // Checks if both SERVER_NAME1 and SERVER_NAME2 are specified while
             // executing ant command for qatest. This loop is executed only if
             // SERVER_NAME1 is sepcified. This setup refers to single server 
             // tests only.
             } else if ((serverName0.indexOf("SERVER_NAME1") == -1) &&
                     (serverName1.indexOf("SERVER_NAME2") != -1)) {
 
                 if (serverName0.indexOf(".") != -1) {
                     log(Level.SEVERE, "SetupProduct", "Server configuration " +
                         "file Configurator-" + serverName0 + ".properties" +
                         " cannot have \".\" in its name");
                     assert false;
                 }
 
                 Map<String, String> configMapServer1 = new HashMap<String,
                         String>();
                 configMapServer1 = getMapFromResourceBundle(
                         "config" + fileseparator + "default" + fileseparator +
                         "ConfiguratorCommon");
                 configMapServer1.putAll(getMapFromResourceBundle(
                         "Configurator-" + serverName0));
                 createFileFromMap(configMapServer1, serverName + fileseparator +
                         "built" + fileseparator + "classes" + fileseparator +
                         "Configurator-" + serverName0 +
                         "-Generated.properties");
                 
                 cfg1 = ResourceBundle.getBundle("Configurator-" +
                         serverName0 + "-Generated");
                 
                 String namingURL = cfg1.getString(KEY_AMC_NAMING_URL);
                 Map namingURLMap = getURLComponents(namingURL);
                 
                 namingProtocol = (String) namingURLMap.get("protocol");
                 namingHost = (String) namingURLMap.get("host");
                 namingPort = (String) namingURLMap.get("port");
                 namingURI = (String) namingURLMap.get("uri");
                 
                 // Initiating setp for server index 1. This is with refrence
                 // to definitions in resources/config/UMGlobalDatstoreConfig 
                 // resource bundle. This is done using client api's.
                 log(Level.FINE, "SetupProduct", "Initiating setup for " +
                         serverName0);   
                 
                 bserver1 = configureProduct(getConfigurationMap("Configurator-"
                         + serverName0 + "-Generated", namingProtocol,
                         namingHost, namingPort, namingURI), "1");
                 if (!bserver1) {
                     log(Level.FINE, "SetupProduct", "Configuration failed for" +
                             " " + serverName0);
                     setSingleServerSetupFailedFlag();
                     assert false;
                 } else {
                     umDatastoreTypes.put("1", serverName0);
                     createGlobalDatastoreFile(umDatastoreTypes,
                             SMSConstants.QATEST_EXEC_MODE_SINGLE);
                     admintoken = getToken(adminUser, adminPassword, basedn);
                     SMSCommon smsc = new SMSCommon(admintoken, "config" +
                             fileseparator + "default" + fileseparator +
                             "UMGlobalConfig");
                     smsc.createDataStore(1, "config" + fileseparator +
                             SMSConstants.UM_DATASTORE_PARAMS_PREFIX +
                             "-Generated");
                     if ((gblCfgData.getString("UMGlobalConfig." +
                             "deleteExistingDatastores")).equals("true"))
                         smsc.deleteAllDataStores(realm, 1);
                     modifyPolicyService(smsc, serverName0, 1, 0);
                 }
             // Checks if both SERVER_NAME1 and SERVER_NAME2 are specified while
             // executing ant command for qatest. If only SERVER_NAME2 is 
             // specified, its an error conditions.
             } else if ((serverName0.indexOf("SERVER_NAME1") != -1) &&
                     (serverName1.indexOf("SERVER_NAME2") == -1)) {
                 log(Level.FINE, "SetupProduct", "Unsupported configuration." +
                         " Cannot have SERVER_NAME2 specified without" +
                         " SERVER_NAME1.");
                 assert false;
             }
 
             if (distAuthEnabled) {
                 String strServiceName = "iPlanetAMAuthService";
                 ServiceConfigManager scm = new ServiceConfigManager(admintoken, 
                         strServiceName, "1.0");
                 log(Level.FINEST, "SetupProduct", "get ServiceConfig");
                 ServiceConfig sc = scm.getOrganizationConfig(realm, null);
                 Map scAttrMap = sc.getAttributes();
                 log(Level.FINEST, "SetupProduct", "Map " +
                         "returned from Org config is: " + scAttrMap);
                 Set oriAuthAttrValues = null;
                 oriAuthAttrValues = (Set) scAttrMap.get
                         ("iplanet-am-auth-login-success-url");
                 log(Level.FINEST, "SetupProduct", "Value of " +
                         "iplanet-am-auth-login-success-url: " + 
                         oriAuthAttrValues);
                 Set newAuthValues = new HashSet();
                 newAuthValues.add(namingProtocol + "://" + namingHost + ":" + 
                         namingPort + namingURI + "/console");
                 Map newAuthValuesMap = new HashMap();
                 newAuthValuesMap.put("iplanet-am-auth-login-success-url", 
                         newAuthValues);
                 log(Level.FINEST, "AuthServiceOrgModificationTest", "Set " +
                         "iplanet-am-auth-login-success-url to " + 
                         newAuthValuesMap);
                 sc.setAttributes(newAuthValuesMap);
             }                        
         } catch(Exception e) {
             log(Level.SEVERE, "SetupProduct", e.getMessage());
             e.printStackTrace();
         } finally {          
             destroyToken(admintoken);
         }
     }
 
     /**
      * In case product configuration fails, this method rewrites the client
      * side AMConfig.properties file and sets product setup flag to false.
      */
     public void setSingleServerSetupFailedFlag() {
         entering("setSingleServerSetupFailedFlag", null);
         try {
             log(Level.FINE, "setSingleServerSetupFailedFlag", "Single server" +
                     " product configuration failed.");
             Set set = new HashSet();
             set.add((String)TestConstants.KEY_ATT_PRODUCT_SETUP_RESULT);
             properties = getMapFromResourceBundle("AMConfig", null, set);
             properties.put(TestConstants.KEY_ATT_PRODUCT_SETUP_RESULT, "fail");
             createFileFromMap(properties, "resources" + fileseparator +
                     FILE_CLIENT_PROPERTIES);
         } catch(Exception e) {
             log(Level.SEVERE, "setSingleServerSetupFailedFlag", e.getMessage());
             e.printStackTrace();
         }
         exiting("setSingleServerSetupFailedFlag");
     }
 
     /**
      * This method creates the generated UMGlobalDatastoreConfig resource bundle
      * under resources/config
      * @param serverName
      * @param map
      * @param sIdx
      * @throws java.lang.Exception
      */
     private void createGlobalDatastoreFile(Map map, String sIdx)
     throws Exception {
         SMSCommon smsc = new SMSCommon("config" + fileseparator + "default" +
                 fileseparator + "UMGlobalConfig");
         smsc.createUMDatastoreGlobalMap(SMSConstants.UM_DATASTORE_PARAMS_PREFIX,
                 map, sIdx);
     }
         
     /**
      * This method creates the datastore using famadm.jsp
      */
     private boolean createDataStoreUsingfamadm(WebClient webClient,
             FederationManager famadm, ResourceBundle cfgData, int
             index, int dCount)
     throws Exception {
         String dsRealm = null;
         String dsType;
         String dsName;
         String adminId;
         String ldapServer;
         String ldapPort;
         String dsAdminPassword;
         String orgName;
         String sslEnabled;
         String authId;
         String psearchBase;
         String groupAtt;
         String roleAtt;
         String userObjClass;
         String userAtt;
         String supportedOps;
         boolean dsCreated = false;
         
         for (int i = 0; i < dCount; i++) {
             dsCreated = false;
             dsType = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_DATASTORE_TYPE + "." +
                     i);
             dsName = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_DATASTORE_NAME + "." +
                     i);
             adminId = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_DATASTORE_ADMINID +
                     "." + i);
             dsRealm = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_DATASTORE_REALM +
                     "." + i);
             ldapServer = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_LDAPv3_LDAP_SERVER +
                     "." + i);
             ldapPort = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_LDAPv3_LDAP_PORT +
                     "." + i);
             dsAdminPassword = cfgData.getString(
                     SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_DATASTORE_ADMINPW +
                     "." + i);
             orgName = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.
                     UM_LDAPv3_ORGANIZATION_NAME + "." + i);
             sslEnabled = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_LDAPv3_LDAP_SSL_ENABLED
                     + "." + i);
             authId = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_LDAPv3_AUTHID
                     + "." + i);
             psearchBase = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_LDAPv3_LDAP_PSEARCHBASE
                     + "." + i);
             groupAtt = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_LDAPv3_GROUP_ATTR
                     + "." + i);
             roleAtt = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_LDAPv3_ROLE_ATTR
                     + "." + i);
             userObjClass = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_LDAPv3_USER_OBJECT_CLASS
                     + "." + i);
             userAtt = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_LDAPv3_USER_ATTR
                     + "." + i);
             supportedOps = cfgData.getString(SMSConstants.
                     UM_DATASTORE_PARAMS_PREFIX + index + "." +
                     SMSConstants.UM_LDAPv3_SUPPORT_OPERATION
                     + "." + i);
             list.add(SMSConstants.
                     UM_LDAPv3_LDAP_SERVER + "=" +
                     ldapServer + ":" + ldapPort);
             list.add(SMSConstants.
                     UM_LDAPv3_AUTHPW + "=" +
                    dsAdminPassword);
             list.add(SMSConstants.
                     UM_LDAPv3_ORGANIZATION_NAME + "=" +
                     orgName);
             list.add(SMSConstants.
                     UM_LDAPv3_LDAP_SSL_ENABLED + "=" +
                     sslEnabled);
             list.add(SMSConstants.
                     UM_LDAPv3_AUTHID + "=" +
                     authId);
             list.add(SMSConstants.
                     UM_LDAPv3_LDAP_PSEARCHBASE + "=" +
                     psearchBase);
             if (groupAtt.indexOf("|") != 0) {
                 List locList = getAttributeList(groupAtt, "|");
                 for (int j = 0; j < locList.size(); j++) {
                     list.add(SMSConstants.
                             UM_LDAPv3_GROUP_ATTR + "=" +
                             (String)locList.get(j));
                 }
             } else {
                 list.add(SMSConstants.
                         UM_LDAPv3_GROUP_ATTR + "=" +
                         groupAtt);
             }
             if (roleAtt.indexOf("|") != 0) {
                 List locList = getAttributeList(roleAtt, "|");
                 for (int j = 0; j < locList.size(); j++) {
                     list.add(SMSConstants.
                             UM_LDAPv3_ROLE_ATTR + "=" +
                             (String)locList.get(j));
                 }
             } else {
                 list.add(SMSConstants.
                         UM_LDAPv3_ROLE_ATTR + "=" +
                         roleAtt);
             }
             if (userObjClass.indexOf("|") != 0) {
                 List locList = getAttributeList(userObjClass, "|");
                 for (int j = 0; j < locList.size(); j++) {
                     list.add(SMSConstants.
                             UM_LDAPv3_USER_OBJECT_CLASS + "=" +
                             (String)locList.get(j));
                 }
             } else {
                 list.add(SMSConstants.
                         UM_LDAPv3_USER_OBJECT_CLASS + "=" +
                         userObjClass);
             }
             if (userAtt.indexOf("|") != 0) {
                 List locList = getAttributeList(userAtt, "|");
                 for (int j = 0; j < locList.size(); j++) {
                     list.add(SMSConstants.
                             UM_LDAPv3_USER_ATTR + "=" +
                             (String)locList.get(j));
                 }
             } else {
                 list.add(SMSConstants.
                         UM_LDAPv3_USER_ATTR + "=" +
                         userAtt);
             }
             if (supportedOps.indexOf("|") != 0) {
                 List locList = getAttributeList(supportedOps, "|");
                 for (int j = 0; j < locList.size(); j++) {
                     list.add(SMSConstants.
                             UM_LDAPv3_SUPPORT_OPERATION + "=" +
                             (String)locList.get(j));
                 }
             } else {
                 list.add(SMSConstants.
                         UM_LDAPv3_SUPPORT_OPERATION + "=" +
                         supportedOps);
             }
             log(Level.FINEST, "createDataStoreUsingfamadm", "Datastore" +
                     " attributes list:" + list);
             HtmlPage page = famadm.listDatastores(webClient,
                     dsRealm);
             if (FederationManager.getExitCode(page) != 0) {
                 log(Level.SEVERE, "createDataStoreUsingfamadm",
                         "listDatastores famadm command" +
                         " failed");
                 assert false;
             }
             LDAPCommon ldc = null;
 	    ldc = new LDAPCommon(ldapServer,
                     ldapPort, adminId, dsAdminPassword,
                     orgName);
             ResourceBundle smsGblCfg = ResourceBundle.
                     getBundle("config" + fileseparator + "default" +
                     fileseparator + "UMGlobalConfig");
             String schemaString = (String)smsGblCfg.
                     getString(SMSConstants.UM_SCHEMNA_LIST
                     + "." +
                     SMSConstants.UM_DATASTORE_SCHEMA_TYPE_AMDS);
             String schemaAttributes = (String)smsGblCfg.
                     getString(SMSConstants.UM_SCHEMNA_ATTR
                     + "." +
                     SMSConstants.UM_DATASTORE_SCHEMA_TYPE_AMDS);
             ldc.loadAMUserSchema(schemaString,
                     schemaAttributes);
             ldc.disconnectDServer();
 	    Thread.sleep(5000);
 	    if (getHtmlPageStringIndex(
                     page, dsName) == -1)
                 if (FederationManager.getExitCode(famadm.createDatastore(
                 webClient, dsRealm, dsName, dsType, list)) != 0) {
                     log(Level.SEVERE, "createDataStoreUsingfamadm",
                             "createDatastore famadm" +
                             " command failed");
                     assert false;
                 }
             page = famadm.listDatastores(webClient,
                     dsRealm);
             if (FederationManager.getExitCode(page) != 0) {
                 log(Level.SEVERE, "createDataStoreUsingfamadm",
                         "listDatastores famadm command" +
                         " failed");
                 assert false;
             }
             if (getHtmlPageStringIndex(
                     page, dsName) == -1) {
                 log(Level.SEVERE, "createDataStoreUsingfamadm", "Datastore" +
                     " creation failed: " + list);
                  assert false;
             }
             dsCreated = true;
 	    list.clear();
         }
         return (dsCreated);
     }
 
     /**
      * This method modifies Policy Service using SMS API. LDAP server name,
      * port, bind bn, bind password, users base dn, roles base dn, ssl enabled
      * will be modified if User Management datastore is set to a remote
      * directory. The data values are picked from
      * resources/config/UMGlobalDatastoreConfig resource bundle.
      */
     private void modifyPolicyService(SMSCommon smsc, String serverName,
             int mIdx, int dIdx)
     throws Exception {
         entering("modifyPolicyService", null);
         String dsRealm = null;
         String ldapServer;
         String ldapPort;
         String dsAdminPassword;
         String orgName;
         String sslEnabled;
         String authId;
         String umDSType;
 
         ResourceBundle amCfgData = ResourceBundle.getBundle("Configurator-" +
                 serverName + "-Generated");
         ResourceBundle cfgData = ResourceBundle.getBundle("config" +
                 fileseparator + SMSConstants.UM_DATASTORE_PARAMS_PREFIX +
                 "-Generated");
 
         umDSType = amCfgData.getString("umdatastore");
 
         try {
             if ((umDSType).equals("dirServer")) {
                 dsRealm = cfgData.getString(SMSConstants.
                         UM_DATASTORE_PARAMS_PREFIX + mIdx + "." +
                         SMSConstants.UM_DATASTORE_REALM + "." + dIdx);
                 ldapServer = cfgData.getString(SMSConstants.
                         UM_DATASTORE_PARAMS_PREFIX + mIdx + "." +
                         SMSConstants.UM_LDAPv3_LDAP_SERVER +
                         "." + dIdx);
                 ldapPort = cfgData.getString(SMSConstants.
                         UM_DATASTORE_PARAMS_PREFIX + mIdx + "." +
                         SMSConstants.UM_LDAPv3_LDAP_PORT +
                         "." + dIdx);
                 dsAdminPassword = cfgData.getString(
                         SMSConstants.UM_DATASTORE_PARAMS_PREFIX +
                         mIdx + "." + SMSConstants.UM_DATASTORE_ADMINPW +
                         "." + dIdx);
                 orgName = cfgData.getString(SMSConstants.
                         UM_DATASTORE_PARAMS_PREFIX + mIdx + "." +
                         SMSConstants.UM_LDAPv3_ORGANIZATION_NAME + "." + dIdx);
                 sslEnabled = cfgData.getString(SMSConstants.
                         UM_DATASTORE_PARAMS_PREFIX + mIdx + "." +
                         SMSConstants.UM_LDAPv3_LDAP_SSL_ENABLED + "." + dIdx);
                 authId = cfgData.getString(SMSConstants.
                         UM_DATASTORE_PARAMS_PREFIX + mIdx + "." +
                         SMSConstants.UM_LDAPv3_AUTHID + "." + dIdx);
 
                 Map scAttrMap =
                         smsc.getAttributes("iPlanetAMPolicyConfigService",
                         dsRealm, "Organization");
                 log(Level.FINEST, "modifyPolicyService",
                         "Policy service attributes before modification: " +
                         scAttrMap);
 
                 Map newPolicyAttrValuesMap = new HashMap();
                 Set newPolicyAttrValues = new HashSet();
                 newPolicyAttrValues.add(ldapServer + ":" + ldapPort);
                 newPolicyAttrValuesMap.
                         put("iplanet-am-policy-config-ldap-server",
                         newPolicyAttrValues);
                 newPolicyAttrValues = new HashSet();
                newPolicyAttrValues.add(dsAdminPassword);
                 newPolicyAttrValuesMap.
                         put("iplanet-am-policy-config-ldap-bind-password",
                         newPolicyAttrValues);
                 newPolicyAttrValues = new HashSet();
                 newPolicyAttrValues.add(authId);
                 newPolicyAttrValuesMap.
                         put("iplanet-am-policy-config-ldap-bind-dn",
                         newPolicyAttrValues);
                 newPolicyAttrValues = new HashSet();
                 newPolicyAttrValues.add(sslEnabled);
                 newPolicyAttrValuesMap.
                         put("iplanet-am-policy-config-ldap-ssl-enabled",
                         newPolicyAttrValues);
                 newPolicyAttrValues = new HashSet();
                 newPolicyAttrValues.add(orgName);
                 newPolicyAttrValuesMap.
                         put("iplanet-am-policy-config-ldap-base-dn",
                         newPolicyAttrValues);
                 newPolicyAttrValuesMap.
                         put("iplanet-am-policy-config-is-roles-base-dn",
                         newPolicyAttrValues);
                 newPolicyAttrValuesMap.
                         put("iplanet-am-policy-config-ldap-users-base-dn",
                         newPolicyAttrValues);
 
                 log(Level.FINEST, "modifyPolicyService",
                         "Policy attributes being modified: " +
                         newPolicyAttrValuesMap);
                 smsc.updateSvcAttribute(dsRealm,
                         "iPlanetAMPolicyConfigService", newPolicyAttrValuesMap,
                         "Organization");
 
                 Map scAttrMapNew =
                         smsc.getAttributes("iPlanetAMPolicyConfigService",
                         dsRealm, "Organization");
                 log(Level.FINEST, "modifyPolicyService",
                         "Policy service attributes after modification: "
                         + scAttrMapNew);
             }
         } catch (Exception e) {
             log(Level.SEVERE, "modifyPolicyService", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("modifyPolicyService");
     }
 
     public static void main(String args[]) {
         try {
             SetupProduct cp = new SetupProduct(args[0], args[1]);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
