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
 * $Id: ConfigureSAMLv2.java,v 1.6 2007-08-07 23:35:23 rmisra Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.samlv2;
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.sun.identity.qatest.common.FederationManager;
 import com.sun.identity.qatest.common.SAMLv2Common;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.TestConstants;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 
 /**
  * This class configures SP & IDP deployed war's if it hasn't done so.
  * Also it creates COT on both instances, loads samlv2 meta on both side with
  * one as SP & one as IDP.
  */
 public class ConfigureSAMLv2 extends TestCommon {
     private WebClient spWebClient;
     private WebClient idpWebClient;
     private Map<String, String> configMap;
     private Map<String, String> spConfigMap;
     private Map<String, String> idpConfigMap;
     public String groupName="";
     
     /** Creates a new instance of ConfigureSAMLv2 */
     public ConfigureSAMLv2() {
         super("ConfigureSAMLv2");
     }
     
     /**
      * Create the webClient which should be run before each test.
      */
     private void getWebClient() throws Exception {
         try {
             spWebClient = new WebClient(BrowserVersion.MOZILLA_1_0);
             idpWebClient = new WebClient(BrowserVersion.MOZILLA_1_0);
         } catch(Exception e) {
             log(Level.SEVERE, "getWebClient", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         }
     }
     
     /**
      * Configure sp & idp
      * @DocTest: SAML2|Configure SP & IDP by loading metadata on both sides.
      */
     @Parameters({"groupName"})
     @BeforeSuite(groups={"ds_ds","ds_ds_sec","ff_ds","ff_ds_sec"})
     public void configureSAMLv2(String strGroupName)
     throws Exception {
         Object[] params = {strGroupName};
         entering("configureSAMLv2", params);
         try {
             URL url;
             HtmlPage page;
             //Upload global properties file in configMap
             configMap = new HashMap<String, String>();
             spConfigMap = new HashMap<String, String>();
             idpConfigMap = new HashMap<String, String>();
             getWebClient();
             
             log(logLevel, "configureSAMLv2", "GroupName received from " +
                     "testng is " + strGroupName);
             SAMLv2Common.getEntriesFromResourceBundle("samlv2TestConfigData",
                     configMap);
             log(logLevel, "configureSAMLv2", "Map:" + configMap);
             
             String spurl = configMap.get(TestConstants.KEY_SP_PROTOCOL)
             + "://" + configMap.get(TestConstants.KEY_SP_HOST) + ":"
                     + configMap.get(TestConstants.KEY_SP_PORT)
                     + configMap.get(TestConstants.KEY_SP_DEPLOYMENT_URI);
             String idpurl = configMap.get(TestConstants.KEY_IDP_PROTOCOL)
             + "://" + configMap.get(TestConstants.KEY_IDP_HOST) + ":"
                     + configMap.get(TestConstants.KEY_IDP_PORT)
                     + configMap.get(TestConstants.KEY_IDP_DEPLOYMENT_URI);
             
             url = new URL(spurl);
             page = (HtmlPage)spWebClient.getPage(url);
            getSPConfigurationMap(spConfigMap, configMap);
             boolean spConfigResult = configureProduct(spConfigMap);
             if (spConfigResult) {
                 log(logLevel, "configureSAMLv2", spurl + "is configured" +
                         "Proceed with SAMLv2 SP configuration");
             } else {
                 log(logLevel, "configureSAMLv2", spurl + "is not " +
                         "configured successfully. " +
                         "Exiting the SAMLv2 configuration");
                 assert false;
             }
             
             url = new URL(idpurl);
             page = (HtmlPage)idpWebClient.getPage(url);
            getIDPConfigurationMap(idpConfigMap, configMap);
             boolean idpConfigResult = configureProduct(idpConfigMap);
             if (idpConfigResult) {
                 log(logLevel, "configureSAMLv2", idpurl + "is configured" +
                         "Proceed with SAMLv2 IDP configuration");
             } else {
                 log(logLevel, "configureSAMLv2", idpurl + "is not " +
                         "configured successfully. " +
                         "Exiting the SAMLv2 configuration");
                 assert false;
             }
             
             FederationManager spfm = new FederationManager(spurl);
             FederationManager idpfm = new FederationManager(idpurl);
             
             //on sp side create cot, load sp metadata
             consoleLogin(spWebClient, spurl,
                     (String)configMap.get(TestConstants.KEY_SP_AMADMIN_USER),
                     (String)configMap.get(
                     TestConstants.KEY_SP_AMADMIN_PASSWORD));
             
             HtmlPage spcotPage = spfm.listCircleOfTrusts(spWebClient,
                     configMap.get(TestConstants.KEY_SP_REALM));
             if (!spcotPage.getWebResponse().getContentAsString().
                     contains(configMap.get(TestConstants.KEY_SP_COT))) {
                 spcotPage = spfm.createCircleOfTrust(spWebClient,
                         configMap.get(TestConstants.KEY_SP_COT),
                         configMap.get(TestConstants.KEY_SP_REALM),
                         null, null);
                 if (!spcotPage.getWebResponse().getContentAsString().
                         contains("Circle of trust, "
                         + configMap.get(TestConstants.KEY_SP_COT)
                         + " is created.")) {
                     log(Level.SEVERE, "configureSAMLv2", "Couldn't create " +
                             "COT at SP side", null);
                     assert false;
                 }
             } else {
                 log(logLevel, "configureSAMLv2", "COT exists at SP side", null);
             }
             
             String spMetadata[]= {"",""};
             HtmlPage spEntityPage = spfm.listEntities(spWebClient,
                     configMap.get(TestConstants.KEY_SP_REALM), "saml2");
             if (!spEntityPage.getWebResponse().getContentAsString().
                     contains(configMap.get(TestConstants.KEY_SP_ENTITY_NAME))) {
                 log(logLevel, "configureSAMLv2", "sp entity doesnt exist. " +
                         "Get template & create the entity", null);
                 if (strGroupName.contains("sec")) {
                     spMetadata = SAMLv2Common.configureSP(spWebClient,
                             configMap, true);
                 } else {
                     spMetadata = SAMLv2Common.configureSP(spWebClient,
                             configMap, false);
                 }
                 if ((spMetadata[0].equals(null)) || (spMetadata[1].equals(null))) {
                     log(Level.SEVERE, "configureSAMLv2", "Couldn't configure " +
                             "SP", null);
                     assert false;
                 }
             } else {
                 //If entity exists, export to get the metadata.
                 HtmlPage spExportEntityPage = spfm.exportEntity(spWebClient,
                         configMap.get(TestConstants.KEY_SP_ENTITY_NAME),
                         configMap.get(TestConstants.KEY_SP_REALM), false, true,
                         true, "saml2");
                 spMetadata[0] = SAMLv2Common.getMetadataFromPage(
                         spExportEntityPage);
                 spMetadata[1] = SAMLv2Common.getExtMetadataFromPage(
                         spExportEntityPage);
             }
             spMetadata[1] = spMetadata[1].replaceAll(
                     configMap.get(TestConstants.KEY_SP_COT), "");
             log(logLevel, "configureSAMLv2", "sp metadata" + spMetadata[0],
                     null);
             log(logLevel, "configureSAMLv2", "sp Ext metadata" + spMetadata[1],
                     null);
             
             //idp side create cot, load idp metadata
             consoleLogin(idpWebClient, idpurl,
                     (String)configMap.get(TestConstants.KEY_IDP_AMADMIN_USER),
                     (String)configMap.get(
                     TestConstants.KEY_IDP_AMADMIN_PASSWORD));
             
             HtmlPage idpcotPage = idpfm.listCircleOfTrusts(idpWebClient,
                     configMap.get(TestConstants.KEY_IDP_REALM));
             if (idpcotPage.getWebResponse().getContentAsString().
                     contains(configMap.get(TestConstants.KEY_IDP_COT))) {
                 log(logLevel, "configureSAMLv2", "COT exists at IDP side",
                         null);
             } else {
                 idpcotPage = idpfm.createCircleOfTrust(idpWebClient,
                         configMap.get(TestConstants.KEY_IDP_COT),
                         configMap.get(TestConstants.KEY_IDP_REALM),
                         null, null);
                 if (!idpcotPage.getWebResponse().getContentAsString().
                         contains("Circle of trust, " +
                         configMap.get(TestConstants.KEY_IDP_COT)
                         + " is created.")) {
                     log(Level.SEVERE, "configureSAMLv2", "Couldn't create " +
                             "COT at IDP side", null);
                     assert false;
                 }
             }
             
             String[] idpMetadata = {"",""};
             HtmlPage idpEntityPage = idpfm.listEntities(idpWebClient,
                     configMap.get(TestConstants.KEY_IDP_REALM), "saml2");
             if (!idpEntityPage.getWebResponse().getContentAsString().
                     contains(configMap.get(TestConstants.KEY_IDP_ENTITY_NAME))) {
                 log(logLevel, "configureSAMLv2", "idp entity doesnt exist. " +
                         "Get template & create the entity", null);
                 if (strGroupName.contains("sec")) {
                     idpMetadata = SAMLv2Common.configureIDP(idpWebClient,
                             configMap, true);
                 } else {
                     idpMetadata = SAMLv2Common.configureIDP(idpWebClient,
                             configMap, false);
                 }
                 
                 log(logLevel, "configureSAMLv2", "idp metadata" +
                         idpMetadata[0], null);
                 log(logLevel, "configureSAMLv2", "idp Ext metadata" +
                         idpMetadata[1], null);
                 if ((idpMetadata[0].equals(null)) || (
                         idpMetadata[1].equals(null))) {
                     log(Level.SEVERE, "configureSAMLv2", "Couldn't configure " +
                             "IDP", null);
                     assert false;
                 }
             } else {
                 //If entity exists, export to get the metadata.
                 HtmlPage idpExportEntityPage = idpfm.exportEntity(idpWebClient,
                         configMap.get(TestConstants.KEY_IDP_ENTITY_NAME),
                         configMap.get(TestConstants.KEY_IDP_REALM), false, true,
                         true, "saml2");
                 idpMetadata[0] = SAMLv2Common.getMetadataFromPage(
                         idpExportEntityPage);
                 idpMetadata[1] = SAMLv2Common.getExtMetadataFromPage(
                         idpExportEntityPage);
             }
             idpMetadata[1] = idpMetadata[1].replaceAll(
                     configMap.get(TestConstants.KEY_IDP_COT), "");
             log(logLevel, "configureSAMLv2", "idp metadata" +
                     idpMetadata[0], null);
             log(logLevel, "configureSAMLv2", "idp Ext metadata" +
                     idpMetadata[1], null);
             
             //load spmetadata on idp
             if (idpEntityPage.getWebResponse().getContentAsString().
                     contains(configMap.get(TestConstants.KEY_SP_ENTITY_NAME))) {
                 log(logLevel, "configureSAMLv2", "sp entity exists at idp. " +
                         "Delete & load the metadata ", null);
                 HtmlPage spDeleteEntityPage = idpfm.deleteEntity(idpWebClient,
                         configMap.get(TestConstants.KEY_SP_ENTITY_NAME),
                         configMap.get(TestConstants.KEY_SP_REALM), false,
                         "saml2");
                 if (spDeleteEntityPage.getWebResponse().getContentAsString().
                         contains("Descriptor is deleted for entity, " +
                         configMap.get(TestConstants.KEY_SP_ENTITY_NAME))) {
                     log(logLevel, "configureSAMLv2", "Delete sp entity on " +
                             "IDP side", null);
                 } else {
                     log(logLevel, "configureSAMLv2", "Couldnt delete sp " +
                             "entity on IDP side", null);
                     assert false;
                 }
             }
             spMetadata[1] = spMetadata[1].replaceAll(
                     "hosted=\"true\"", "hosted=\"false\"");
             spMetadata[1] = spMetadata[1].replaceAll(
                     "hosted=\"1\"", "hosted=\"0\"");
             HtmlPage importSPMeta = idpfm.importEntity(idpWebClient,
                     configMap.get(TestConstants.KEY_IDP_REALM), spMetadata[0],
                     spMetadata[1],
                     (String)configMap.get(TestConstants.KEY_IDP_COT), "saml2");
             if (!importSPMeta.getWebResponse().getContentAsString().
                     contains("Import file, web.")) {
                 log(Level.SEVERE, "configureSAMLv2", "Couldn't import SP " +
                         "metadata on IDP side" + importSPMeta.getWebResponse().
                         getContentAsString(), null);
                 assert false;
             }
             //load idpmetadata on sp
             if (spEntityPage.getWebResponse().getContentAsString().
                     contains(configMap.get(TestConstants.KEY_IDP_ENTITY_NAME))) {
                 log(logLevel, "configureSAMLv2", "idp entity exists at sp. " +
                         "Delete & load the metadata ", null);
                 HtmlPage idpDeleteEntityPage = spfm.deleteEntity(spWebClient,
                         configMap.get(TestConstants.KEY_IDP_ENTITY_NAME),
                         configMap.get(TestConstants.KEY_IDP_REALM), false,
                         "saml2");
                 if (idpDeleteEntityPage.getWebResponse().getContentAsString().
                         contains("Descriptor is deleted for entity, " +
                         configMap.get(TestConstants.KEY_IDP_ENTITY_NAME))) {
                     log(logLevel, "configureSAMLv2", "Delete idp entity on " +
                             "SP side", null);
                 } else {
                     log(logLevel, "configureSAMLv2", "Couldnt delete idp " +
                             "entity on SP side", null);
                     assert false;
                 }
             }
             idpMetadata[1] = idpMetadata[1].replaceAll(
                     "hosted=\"true\"", "hosted=\"false\"");
             idpMetadata[1] = idpMetadata[1].replaceAll(
                     "hosted=\"1\"", "hosted=\"0\"");
             HtmlPage importIDPMeta = spfm.importEntity(spWebClient,
                     configMap.get(TestConstants.KEY_SP_REALM), idpMetadata[0],
                     idpMetadata[1],
                     (String)configMap.get(TestConstants.KEY_SP_COT), "saml2");
             if (!importIDPMeta.getWebResponse().getContentAsString().
                     contains("Import file, web.")) {
                 log(Level.SEVERE, "configureSAMLv2", "Couldn't import IDP " +
                         "metadata on SP side" + importIDPMeta.getWebResponse().
                         getContentAsString(), null);
                 assert false;
             }
             consoleLogout(spWebClient, spurl);
             consoleLogout(idpWebClient, idpurl);
         } catch (Exception e) {
             log(Level.SEVERE, "configureSAMLv2", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         }
         exiting("configureSAMLv2");
     }
     
     /**
      * This method fills map with SP configuration data which is needed by
      * TestCommon.configureProduct method.
      */
     private void getSPConfigurationMap(Map spMap, Map confMap)
     throws Exception {
         try {
             spMap.put("serverurl",confMap.get(TestConstants.KEY_SP_PROTOCOL)
             + ":" + "//" + confMap.get(TestConstants.KEY_SP_HOST) + ":"
                     + confMap.get(TestConstants.KEY_SP_PORT));
             spMap.put("serveruri",
                     confMap.get(TestConstants.KEY_SP_DEPLOYMENT_URI));
             spMap.put(TestConstants.KEY_ATT_COOKIE_DOMAIN,
                     confMap.get(TestConstants.KEY_SP_COOKIE_DOMAIN));
             spMap.put(TestConstants.KEY_ATT_CONFIG_DIR,
                     confMap.get(TestConstants.KEY_SP_CONFIG_DIR));
             spMap.put(TestConstants.KEY_ATT_AMADMIN_PASSWORD,
                     confMap.get(TestConstants.KEY_SP_AMADMIN_PASSWORD));
             spMap.put(TestConstants.KEY_ATT_CONFIG_DATASTORE,
                     confMap.get(TestConstants.KEY_SP_DATASTORE));
             spMap.put(TestConstants.KEY_ATT_AM_ENC_KEY,
                     confMap.get(TestConstants.KEY_SP_ENC_KEY));
             spMap.put(TestConstants.KEY_ATT_DIRECTORY_SERVER,
                     confMap.get(TestConstants.KEY_SP_DIRECTORY_SERVER));
             spMap.put(TestConstants.KEY_ATT_DIRECTORY_PORT,
                     confMap.get(TestConstants.KEY_SP_DIRECTORY_PORT));
             spMap.put(TestConstants.KEY_ATT_CONFIG_ROOT_SUFFIX,
                     confMap.get(TestConstants.KEY_SP_CONFIG_ROOT_SUFFIX));
             spMap.put(TestConstants.KEY_ATT_SM_ROOT_SUFFIX,
                     confMap.get(TestConstants.KEY_SP_SM_ROOT_SUFFIX));
             spMap.put(TestConstants.KEY_ATT_DS_DIRMGRDN,
                     confMap.get(TestConstants.KEY_SP_DS_DIRMGRDN));
             spMap.put(TestConstants.KEY_ATT_DS_DIRMGRPASSWD,
                     confMap.get(TestConstants.KEY_SP_DS_DIRMGRPASSWORD));
             spMap.put(TestConstants.KEY_ATT_LOAD_UMS,
                     confMap.get(TestConstants.KEY_SP_LOAD_UMS));
         } catch(Exception e) {
             log(Level.SEVERE, "getspConfigurationMap", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         }
     }
     
     /**
      * This method fills map with IDP configuration data which is needed by
      * TestCommon.configureProduct method.
      */
     private void getIDPConfigurationMap(Map idpMap, Map confMap)
     throws Exception {
         try {
             idpMap.put("serverurl",confMap.get(TestConstants.KEY_IDP_PROTOCOL)
             + ":" + "//" + confMap.get(TestConstants.KEY_IDP_HOST) + ":"
                     + confMap.get(TestConstants.KEY_IDP_PORT));
             idpMap.put("serveruri",
                     confMap.get(TestConstants.KEY_IDP_DEPLOYMENT_URI));
             idpMap.put(TestConstants.KEY_ATT_COOKIE_DOMAIN,
                     confMap.get(TestConstants.KEY_IDP_COOKIE_DOMAIN));
             idpMap.put(TestConstants.KEY_ATT_CONFIG_DIR,
                     confMap.get(TestConstants.KEY_IDP_CONFIG_DIR));
             idpMap.put(TestConstants.KEY_ATT_AMADMIN_PASSWORD,
                     confMap.get(TestConstants.KEY_IDP_AMADMIN_PASSWORD));
             idpMap.put(TestConstants.KEY_ATT_CONFIG_DATASTORE,
                     confMap.get(TestConstants.KEY_IDP_DATASTORE));
             idpMap.put(TestConstants.KEY_ATT_AM_ENC_KEY,
                 confMap.get(TestConstants.KEY_IDP_ENC_KEY));
             idpMap.put(TestConstants.KEY_ATT_DIRECTORY_SERVER,
                     confMap.get(TestConstants.KEY_IDP_DIRECTORY_SERVER));
             idpMap.put(TestConstants.KEY_ATT_DIRECTORY_PORT,
                     confMap.get(TestConstants.KEY_IDP_DIRECTORY_PORT));
             idpMap.put(TestConstants.KEY_ATT_CONFIG_ROOT_SUFFIX,
                     confMap.get(TestConstants.KEY_IDP_CONFIG_ROOT_SUFFIX));
             idpMap.put(TestConstants.KEY_ATT_SM_ROOT_SUFFIX,
                     confMap.get(TestConstants.KEY_IDP_SM_ROOT_SUFFIX));
             idpMap.put(TestConstants.KEY_ATT_DS_DIRMGRDN,
                     confMap.get(TestConstants.KEY_IDP_DS_DIRMGRDN));
             idpMap.put(TestConstants.KEY_ATT_DS_DIRMGRPASSWD,
                     confMap.get(TestConstants.KEY_IDP_DS_DIRMGRPASSWORD));
             idpMap.put(TestConstants.KEY_ATT_LOAD_UMS,
                     confMap.get(TestConstants.KEY_SP_LOAD_UMS));
         } catch(Exception e) {
             log(Level.SEVERE, "getidpConfigurationMap", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         }
     }
 }
