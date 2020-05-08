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
 * $Id: TestConstants.java,v 1.9 2007-08-17 18:44:42 bt199000 Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.common;
 
 public interface TestConstants {
 
     /**
      * Property name for AMConfig.properties file
      */
     String TEST_PROPERTY_AMCONFIG = "AMConfig";
 
     /**
      * Property name for logging level.
      */
     String KEY_ATT_LOG_LEVEL = "log_level";
 
     /**
      * Property key for <code>amadmin</code> user.
      */
     String KEY_ATT_AMADMIN_USER = "amadmin_username";
 
     /**
      * Property key for <code>amadmin</code> user password.
      */
     String KEY_ATT_AMADMIN_PASSWORD = "amadmin_password";
 
     /**
      * Property key for <code>com.iplanet.am.defaultOrg</code>.
      */
     String KEY_AMC_BASEDN = "com.iplanet.am.defaultOrg";
 
     /**
      * Property key for <code>planet.am.server.protocol</code>.
      */
     String KEY_AMC_PROTOCOL = "com.iplanet.am.server.protocol";
 
     /**
      * Property key for <code>com.iplanet.am.server.host</code>.
      */
     String KEY_AMC_HOST = "com.iplanet.am.server.host";
 
     /**
      * Property key for <code>com.iplanet.am.server.port</code>.
      */
     String KEY_AMC_PORT = "com.iplanet.am.server.port";
 
     /**
      * Property key for
      * <code>com.iplanet.am.services.deploymentDescriptor</code>.
      */
     String KEY_AMC_URI = "com.iplanet.am.services.deploymentDescriptor";
 
     /**
      * Property key for <code>com.iplanet.am.naming.url</code>.
      */
     String KEY_AMC_NAMING_URL = "com.iplanet.am.naming.url";
 
     /**
      * Property key for <code>com.iplanet.am.service.password</code>.
      */
     String KEY_AMC_SERVICE_PASSWORD = "com.iplanet.am.service.password";
 
     /**
      * Property key for <code>realm</code>.
      */
     String KEY_ATT_REALM = "realm";
 
     /**
      * Property key for <code>testservername</code>.
      */
     String KEY_ATT_SERVER_NAME = "testservername";
 
     /**
      * Property key for <code>cookiedomain</code>.
      */
     String KEY_ATT_COOKIE_DOMAIN = "cookiedomain";
 
     /**
      * Property key for <code>namingservice</code>.
      */
     String KEY_ATT_NAMING_SVC = "namingservice";
 
     /**
      * Property key for <code>config_dir</code>.
      */
     String KEY_ATT_CONFIG_DIR = "config_dir";
 
     /**
      * Property key for <code>encryption_key</code>.
      */
     String KEY_ATT_AM_ENC_KEY = "encryption_key";
 
     /**
     * Property key for <code>am.encryption.pwd</code>.
     */
    String KEY_ATT_AM_ENC_PWD = "am.encryption.pwd";
    
    /**
      * Property key for <code>datastore</code>.
      */
     String KEY_ATT_CONFIG_DATASTORE = "datastore";
 
     /**
      * Property key for <code>directory_server</code>.
      */
     String KEY_ATT_DIRECTORY_SERVER = "directory_server";
 
     /**
      * Property key for <code>directory_port</code>.
      */
     String KEY_ATT_DIRECTORY_PORT = "directory_port";
 
     /**
      * Property key for <code>config_root_suffix</code>.
      */
     String KEY_ATT_CONFIG_ROOT_SUFFIX = "config_root_suffix";
 
     /**
      * Property key for <code>sm_root_suffix</code>.
      */
     String KEY_ATT_SM_ROOT_SUFFIX = "sm_root_suffix";
 
     /**
      * Property key for <code>ds_dirmgrdn</code>.
      */
     String KEY_ATT_DS_DIRMGRDN = "ds_dirmgrdn";
 
     /**
      * Property key for <code>ds_dirmgrpasswd</code>.
      */
     String KEY_ATT_DS_DIRMGRPASSWD = "ds_dirmgrpasswd";
 
     /**
      * Property key for <code>load_ums</code>.
      */
     String KEY_ATT_LOAD_UMS = "load_ums";
 
     /**
      * Property key for <code>defaultorg</code>.
      */
     String KEY_ATT_DEFAULTORG = "defaultorg";
 
     /**
      * Property key for <code>productSetupResult</code>.
      */
     String KEY_ATT_PRODUCT_SETUP_RESULT = "productSetupResult";
 
     /**
      * Property key for <code>metaalias</code>.
      */
     String KEY_ATT_METAALIAS = "metaalias";
 
     /**
      * Property key for <code>entity_name</code>.
      */
     String KEY_ATT_ENTITY_NAME = "entity_name";
 
     /**
      * Property key for <code>cot</code>.
      */
     String KEY_ATT_COT = "cot";
 
     /**
      * Property key for <code>certalias</code>.
      */
     String KEY_ATT_CERTALIAS = "certalias";
 
     /**
      * Property key for <code>protocol</code>.
      */
     String KEY_ATT_PROTOCOL = "protocol";
 
     /**
      * Property key for <code>host</code>.
      */
     String KEY_ATT_HOST = "host";
 
     /**
      * Property key for <code>port</code>.
      */
     String KEY_ATT_PORT = "port";
 
     /**
      * Property key for <code>deployment_uri</code>.
      */
     String KEY_ATT_DEPLOYMENT_URI = "deployment_uri";
     
     /** 
      * SAMLv2, IDFF SP related constants 
      * Property key for <code>sp_host</code>
      */
     String KEY_SP_HOST = "sp_host";
  
     /** 
      * SAMLv2, IDFF Property key for <code>sp_protocol</code>
      */
     String KEY_SP_PROTOCOL = "sp_protocol";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>sp_port</code>
      */
     String KEY_SP_PORT = "sp_port";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>sp_deployment_uri</code>
      */
     String KEY_SP_DEPLOYMENT_URI = "sp_deployment_uri";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_metaalias</code>
      */
     String KEY_SP_METAALIAS = "sp_metaalias";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_entity_name</code>
      */
     String KEY_SP_ENTITY_NAME = "sp_entity_name";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_realm</code>
      */
     String KEY_SP_REALM = "sp_realm";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_cot</code>
      */
     String KEY_SP_COT = "sp_cot";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_certalias</code>
      */
     String KEY_SP_CERTALIAS = "sp_certalias";  
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_config_dir</code>
      */
     String KEY_SP_CONFIG_DIR = "sp_config_dir";      
 
     /**
      * SAMLv2, IDFF Property key for <code>sp_encryption_key</code>
      */
     String KEY_SP_ENC_KEY = "sp_encryption_key";
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_datastore</code>
      */
     String KEY_SP_DATASTORE = "sp_datastore";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_amadmin_username</code>
      */
     String KEY_SP_AMADMIN_USER = "sp_amadmin_username";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_amadmin_password</code>
      */
     String KEY_SP_AMADMIN_PASSWORD = "sp_amadmin_password";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>sp_directory_server</code>
      */
     String KEY_SP_DIRECTORY_SERVER = "sp_directory_server";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_directory_port</code>
      */
     String KEY_SP_DIRECTORY_PORT = "sp_directory_port";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_config_root_suffix</code>
      */
     String KEY_SP_CONFIG_ROOT_SUFFIX = "sp_config_root_suffix";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>sp_sm_root_suffix</code>
      */
     String KEY_SP_SM_ROOT_SUFFIX = "sp_sm_root_suffix";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_ds_dirmgrdn</code>
      */
     String KEY_SP_DS_DIRMGRDN = "sp_ds_dirmgrdn";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_ds_dirmgrpasswd</code>
      */
     String KEY_SP_DS_DIRMGRPASSWORD = "sp_ds_dirmgrpasswd";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>sp_load_ums</code>
      */
     String KEY_SP_LOAD_UMS = "sp_load_ums";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>sp_cookiedomain</code>
      */
     String KEY_SP_COOKIE_DOMAIN = "sp_cookiedomain";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>sp_user</code>
      */
     String KEY_SP_USER = "sp_user";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>sp_userpw</code>
      */
     String KEY_SP_USER_PASSWORD = "sp_userpw";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>sp_mail</code>
      */
     String KEY_SP_USER_MAIL = "sp_mail";  
 
     /** 
      * SAMLv2, IDFF IDP related constants 
      * Property key for <code>idp_host</code>
      */
     String KEY_IDP_HOST = "idp_host";
  
     /** 
      * SAMLv2, IDFF Property key for <code>idp_port</code>
      */
     String KEY_IDP_PORT = "idp_port";  
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_protocol</code>
      */
     String KEY_IDP_PROTOCOL = "idp_protocol";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>idp_deployment_uri</code>
      */
     String KEY_IDP_DEPLOYMENT_URI = "idp_deployment_uri";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_metaalias</code>
      */
     String KEY_IDP_METAALIAS = "idp_metaalias";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_entity_name</code>
      */
     String KEY_IDP_ENTITY_NAME = "idp_entity_name";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_realm</code>
      */
     String KEY_IDP_REALM = "idp_realm";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_cot</code>
      */
     String KEY_IDP_COT = "idp_cot";      
 
     /**
      * SAMLv2, IDFF Property key for <code>idp_encryption_key</code>
      */
     String KEY_IDP_ENC_KEY = "idp_encryption_key";
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_certalias</code>
      */
     String KEY_IDP_CERTALIAS = "idp_certalias";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>idp_config_dir</code>
      */
     String KEY_IDP_CONFIG_DIR = "idp_config_dir";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_datastore</code>
      */
     String KEY_IDP_DATASTORE = "idp_datastore";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_amadmin_username</code>
      */
     String KEY_IDP_AMADMIN_USER = "idp_amadmin_username";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_amadmin_password</code>
      */
     String KEY_IDP_AMADMIN_PASSWORD = "idp_amadmin_password";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>idp_directory_server</code>
      */
     String KEY_IDP_DIRECTORY_SERVER = "idp_directory_server";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_directory_port</code>
      */
     String KEY_IDP_DIRECTORY_PORT = "idp_directory_port";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_config_root_suffix</code>
      */
     String KEY_IDP_CONFIG_ROOT_SUFFIX = "idp_config_root_suffix";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>idp_sm_root_suffix</code>
      */
     String KEY_IDP_SM_ROOT_SUFFIX = "idp_sm_root_suffix";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_ds_dirmgrdn</code>
      */
     String KEY_IDP_DS_DIRMGRDN = "idp_ds_dirmgrdn";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_ds_dirmgrpasswd</code>
      */
     String KEY_IDP_DS_DIRMGRPASSWORD = "idp_ds_dirmgrpasswd";      
     
     /** 
      * SAMLv2, IDFF Property key for <code>idp_load_ums</code>
      */
     String KEY_IDP_LOAD_UMS = "idp_load_ums";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>idp_cookiedomain</code>
      */
     String KEY_IDP_COOKIE_DOMAIN = "idp_cookiedomain";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>idp_user</code>
      */
     String KEY_IDP_USER = "idp_user";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>idp_userpw</code>
      */
     String KEY_IDP_USER_PASSWORD = "idp_userpw";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>idp_mail</code>
      */
     String KEY_IDP_USER_MAIL = "idp_mail";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>ssoresult</code>
      */
     String KEY_SSO_RESULT = "ssoresult";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>ssoinitresult</code>
      */
     String KEY_SSO_INIT_RESULT = "ssoinitresult";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>spsloresult</code>
      */
     String KEY_SP_SLO_RESULT = "spsloresult";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>terminateresult</code>
      */
     String KEY_TERMINATE_RESULT = "terminateresult";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>idpsloresult</code>
      */
     String KEY_IDP_SLO_RESULT = "idpsloresult";  
 
     /** 
      * SAMLv2, IDFF Property key for <code>loginresult</code>
      */
     String KEY_LOGIN_RESULT = "loginresult";  
     
     /** 
      * IDFF Property key for <code>nameregresult</code>
      */
     String KEY_NAME_REG_RESULT = "nameregresult";  
     
     /** 
      * IDFF Property key for <code>SSO Artifact Profile</code>
      */
     String SSO_BROWSER_ARTIFACT_VALUE =
         "<Value>http://projectliberty.org/profiles/brws-art</Value>";
     
     /** 
      * IDFF Property key for <code>SSO Post Profile</code>
      */
     String SSO_BROWSER_POST_VALUE =
         "<Value>http://projectliberty.org/profiles/brws-post</Value>";
 
     /** 
      * IDFF Property key for <code>SLO HTTP Profile</code>
      */
     String SLO_HTTP_PROFILE_VALUE =
         "<SingleLogoutProtocolProfile>http://projectliberty.org/profiles/" +
             "slo-idp-http</SingleLogoutProtocolProfile>";
     
     /** 
      * IDFF Property key for <code>SLO SOAP Profile</code>
      */
     String SLO_SOAP_PROFILE_VALUE =
         "<SingleLogoutProtocolProfile>http://projectliberty.org/profiles/" +
             "slo-idp-soap</SingleLogoutProtocolProfile>";
 
     /** 
      * IDFF Property key for <code>Registration HTTP Profile</code>
      */
     String REG_HTTP_PROFILE_VALUE =
         "<RegisterNameIdentifierProtocolProfile>http://projectliberty.org/" +
             "profiles/rni-idp-http</RegisterNameIdentifierProtocolProfile>";
     
     /** 
      * IDFF Property key for <code>Registration SOAP Profile</code>
      */
     String REG_SOAP_PROFILE_VALUE =
         "<RegisterNameIdentifierProtocolProfile>http://projectliberty.org/" +
             "profiles/rni-idp-soap</RegisterNameIdentifierProtocolProfile>";
 
     /** 
      * IDFF Property key for <code>Termination HTTP Profile</code>
      */
     String TERMIATION_HTTP_PROFILE_VALUE =
         "<FederationTerminationNotificationProtocolProfile>" +
             "http://projectliberty.org/profiles/fedterm-idp-http" +
             "</FederationTerminationNotificationProtocolProfile>";
     
     /** 
      * IDFF Property key for <code>Termination SOAP Profile</code>
      */
     String TERMIATION_SOAP_PROFILE_VALUE =
         "<FederationTerminationNotificationProtocolProfile>" +
             "http://projectliberty.org/profiles/fedterm-idp-soap" +
             "</FederationTerminationNotificationProtocolProfile>";
 } 
