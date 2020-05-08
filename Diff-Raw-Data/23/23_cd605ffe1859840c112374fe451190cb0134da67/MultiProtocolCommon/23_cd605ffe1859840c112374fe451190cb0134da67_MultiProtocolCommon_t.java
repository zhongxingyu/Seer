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
 * $Id: MultiProtocolCommon.java,v 1.2 2007-08-10 19:55:13 mrudulahg Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.common;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * This class contains common helper methods for samlv2, IDFF tests
  */
 public class MultiProtocolCommon extends TestCommon {
     
     /** Creates a new instance of MultiProtocolCommon */
     public MultiProtocolCommon() {
         super("MultiProtocolCommon");
     }
     
     /**
      * This method creates the hosted SP metadata template & loads it.
      * It returns the uploaded standard & extended metadata.
      * Null is returned in case of failure.
      * @param WebClient object after admin login is successful.
      * @param Map consisting of SP data
      * @param boolean signed metadata should contain signature true or false
      */
     public static String[] configureSP(WebClient webClient, Map m, String spec,
             boolean signed) {
         String[] arrMetadata= {"", ""};
         try {
             String spurl = m.get(TestConstants.KEY_SP_PROTOCOL) + "://" +
                     m.get(TestConstants.KEY_SP_HOST) + ":" +
                     m.get(TestConstants.KEY_SP_PORT)
                     + m.get(TestConstants.KEY_SP_DEPLOYMENT_URI);
             
             //get sp & idp extended metadata
             FederationManager spfm = new FederationManager(spurl);
             HtmlPage spmetaPage;
             if (signed) {
                 spmetaPage = spfm.createMetadataTemplate(webClient,
                         (String)m.get(TestConstants.KEY_SP_ENTITY_NAME), true,
                         true, (String)m.get(TestConstants.KEY_SP_METAALIAS),
                         null, null, null,
                         (String)m.get(TestConstants.KEY_SP_CERTALIAS), null,
                         null, null,
                         (String)m.get(TestConstants.KEY_SP_CERTALIAS), null,
                         null, null, spec);
             } else {
                 spmetaPage = spfm.createMetadataTemplate(webClient,
                         (String)m.get(TestConstants.KEY_SP_ENTITY_NAME), true,
                         true, (String)m.get(TestConstants.KEY_SP_METAALIAS),
                         null, null, null, null, null, null, null, null, null,
                         null, null, spec);
             }
             
             String spPage = spmetaPage.getWebResponse().getContentAsString();
             if (spPage.indexOf("EntityDescriptor") != -1) {
                 arrMetadata[0] = spPage.substring(
                         spPage.indexOf("EntityDescriptor") - 4,
                         spPage.lastIndexOf("EntityDescriptor") + 17);
                 arrMetadata[1] = spPage.substring(
                         spPage.indexOf("EntityConfig") - 4,
                         spPage.lastIndexOf("EntityConfig") + 13);
             } else {
                 System.out.println(spPage);
                 arrMetadata[0] = null;
                 arrMetadata[1] = null;
                 assert false;
             }
             if ((arrMetadata[0].equals(null)) || (arrMetadata[1].equals(null))) {
                 assert(false);
             } else {
                 arrMetadata[0] = arrMetadata[0].replaceAll("&lt;", "<");
                 arrMetadata[0] = arrMetadata[0].replaceAll("&gt;", ">");
                 arrMetadata[1] = arrMetadata[1].replaceAll("&lt;", "<");
                 arrMetadata[1] = arrMetadata[1].replaceAll("&gt;", ">");
                 HtmlPage importMeta = spfm.importEntity(webClient,
                         (String)m.get(TestConstants.KEY_SP_REALM), 
                         arrMetadata[0], arrMetadata[1], 
                         (String)m.get(TestConstants.KEY_SP_COT), spec);
                 if (!importMeta.getWebResponse().getContentAsString().
                         contains("Import file, web.")) {
                     assert(false);
                     arrMetadata[0] = null;
                     arrMetadata[1] = null;
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return arrMetadata;
     }
     
     /**
      * This method creates the hosted IDP metadata template & loads it.
      * @param WebClient object after admin login is successful.
      * @param Map consisting of IDP data
      * @param String spec describing "samlv2", "idff"
      * @param boolean signed metadata should contain signature true or false
      */
     public static String[] configureIDP(WebClient webClient, Map m, String spec,
             boolean signed) {
         String[] arrMetadata={"",""};
         try {
             String idpurl = m.get(TestConstants.KEY_IDP_PROTOCOL) + "://" +
                     m.get(TestConstants.KEY_IDP_HOST) + ":"
                     + m.get(TestConstants.KEY_IDP_PORT)
                     + m.get(TestConstants.KEY_IDP_DEPLOYMENT_URI);
             
             //get sp & idp extended metadata
             FederationManager idpfm = new FederationManager(idpurl);
             HtmlPage idpmetaPage;
             if (signed) {
                 idpmetaPage = idpfm.createMetadataTemplate(webClient,
                         (String)m.get(TestConstants.KEY_IDP_ENTITY_NAME), true,
                         true, null, (String)m.get(
                         TestConstants.KEY_IDP_METAALIAS),
                         null, null,null,
                         (String)m.get(TestConstants.KEY_IDP_CERTALIAS), null,
                         null, null, (String)m.get(
                         TestConstants.KEY_IDP_CERTALIAS),
                         null, null, spec);
             } else {
                 idpmetaPage = idpfm.createMetadataTemplate(webClient,
                         (String)m.get(TestConstants.KEY_IDP_ENTITY_NAME), true,
                         true, null,
                         (String)m.get(TestConstants.KEY_IDP_METAALIAS), null,
                         null, null, null, null, null, null, null, null, null,
                         spec);
             }
             String idpPage = idpmetaPage.getWebResponse().getContentAsString();
             if (idpPage.indexOf("EntityDescriptor") != -1) {
                 arrMetadata[0] = idpPage.substring(
                         idpPage.indexOf("EntityDescriptor") - 4,
                         idpPage.lastIndexOf("EntityDescriptor") + 17);
                 arrMetadata[1] = idpPage.substring(
                         idpPage.indexOf("EntityConfig") - 4,
                         idpPage.lastIndexOf("EntityConfig") + 13);
             } else {
                 arrMetadata[0] = null;
                 arrMetadata[1] = null;
                 assert false;
             }
             if ((arrMetadata[0].equals(null)) || (arrMetadata[1].equals(null))) {
                 assert(false);
             } else {
                 arrMetadata[0] = arrMetadata[0].replaceAll("&lt;", "<");
                 arrMetadata[0] = arrMetadata[0].replaceAll("&gt;", ">");
                 arrMetadata[1] = arrMetadata[1].replaceAll("&lt;", "<");
                 arrMetadata[1] = arrMetadata[1].replaceAll("&gt;", ">");
                 HtmlPage importMeta = idpfm.importEntity(webClient,
                         (String)m.get(TestConstants.KEY_IDP_REALM),
                         arrMetadata[0], arrMetadata[1],
                         (String)m.get(TestConstants.KEY_IDP_COT), spec);
                 if (!importMeta.getWebResponse().getContentAsString().
                         contains("Import file, web.")) {
                     assert(false);
                     arrMetadata[0] = null;
                     arrMetadata[1] = null;
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return arrMetadata;
     }
     /**
      * This method fills map with SP configuration data which is needed by
      * TestCommon.configureProduct method.
      */
     public static Map getSPConfigurationMap(Map confMap)
     throws Exception {
         Map spMap = new HashMap<String, String>();
         try {
             spMap.put("serverurl", confMap.get(TestConstants.KEY_SP_PROTOCOL)
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
         } catch (Exception e) {
             e.printStackTrace();
             throw e;
         } finally {
             return spMap;
         }
     }
         
     /**
      * This method fills map with IDP configuration data which is needed by
      * TestCommon.configureProduct method.
      */
     public static Map getIDPConfigurationMap(Map confMap)
     throws Exception {
         Map idpMap = new HashMap<String, String>();
         try {
             idpMap.put("serverurl", confMap.get(TestConstants.KEY_IDP_PROTOCOL)
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
         } catch (Exception e) {
             e.printStackTrace();
             throw e;
         } finally {
             return idpMap;
         }
     }
     
     /**
      * This method grep Metadata from the htmlpage & returns as the string.
      * @param HtmlPage page which contains metadata
      */
     public static String getMetadataFromPage(HtmlPage page) {
         String metadata = "";
         String metaPage = page.getWebResponse().getContentAsString();
         if (!(metaPage.indexOf("EntityConfig") == -1)) {
             metadata = metaPage.substring(metaPage.
                     indexOf("EntityDescriptor") - 4,
                     metaPage.lastIndexOf("EntityDescriptor") + 17);
             metadata = metadata.replaceAll("&lt;", "<");
             metadata = metadata.replaceAll("&gt;", ">");
         }
         return metadata;
     }
     
     /**
      * This method grep ExtendedMetadata from the htmlpage & returns the string
      * @param HtmlPage page which contains extended metadata
      */
     public static String getExtMetadataFromPage(HtmlPage page) {
         String metadata = "";
         String metaPage = page.getWebResponse().getContentAsString();
         if (!(metaPage.indexOf("EntityConfig") == -1)) {
             metadata = metaPage.substring(metaPage.
                     indexOf("EntityConfig") - 4,
                     metaPage.lastIndexOf("EntityConfig") + 13);
             metadata = metadata.replaceAll("&lt;", "<");
             metadata = metadata.replaceAll("&gt;", ">");
         }
         return metadata;
     }
 }
 
