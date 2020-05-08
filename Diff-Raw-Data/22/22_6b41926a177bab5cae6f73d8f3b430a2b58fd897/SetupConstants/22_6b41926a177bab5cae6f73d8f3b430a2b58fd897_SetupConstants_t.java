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
 * $Id: SetupConstants.java,v 1.6 2007-01-25 00:42:48 pawand Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.setup;
 
 /**
  * This defines the constants used in setup package.
  */
 public interface SetupConstants {
     /**
      * Setup Debug name.
      */
     String DEBUG_NAME = "amSetupServlet";
     
     /**
      * Default Platform Locale.
      */
     String DEFAULT_PLATFORM_LOCALE = "en_US";
 
     /**
      * Flag to overwrite <code>AMConfig.properties</code>
      */
     String AMC_OVERRIDE_PROPERTY = "com.sun.identity.overrideAMC";
 
     /**
      * Encryption property in configuration file.
      */
     String ENC_PWD_PROPERTY = "am.encryption.pwd";
 
     /**
      * Bootstrap directory prefix.
      */
     String BOOTSTRAP_FILE_PREFIX = "com.sun.identity.bootClassPath";
 
     /**
      * Configurator plugins properties file name.
      */
     String PROPERTY_CONFIGURATOR_PLUGINS = "configuratorPlugins";
 
     /**
      * Configurator plugins class name.
      */
     String KEY_CONFIGURATOR_PLUGINS = "configurator.plugins";
     
     /**
      * <code>AMConfig.properties</code> file name.
      */
     String AMCONFIG_PROPERTIES = "AMConfig.properties";
 
     /**
      * Encrypted Admin password.
      */
     String ENCRYPTED_ADMIN_PWD = "ENCADMINPASSWD";
 
     /**
      * Hash Admin password.
      */
     String HASH_ADMIN_PWD = "HASHADMINPASSWD";
 
     /**
      * LDAP user password.
      */
     String LDAP_USER_PWD = "AMLDAPUSERPASSWD";
 
     /**
      * Encrypted LDAP user password.
      */
     String ENCRYPTED_LDAP_USER_PWD = "ENCLDAPUSERPASSWD";
 
     /**
      * Hash LDAP user password.
      */
     String HASH_LDAP_USER_PWD = "HASHLDAPUSERPASSWD";
 
     /**
      * Encrypted directory Admin password.
      */
     String ENCRYPTED_AD_ADMIN_PWD = "ENCADADMINPASSWD";
 
     /**
      * Database name in directory server.
      */
     String DB_NAME = "DB_NAME";
     
     /**
      * Properties file name that contains the names of all services that need
      * to be registered by the configurator.
      */
     String PROPERTY_FILENAME = "serviceNames";
     
     /**
      * Property key in <code>PROPERTY_FILENAME</code> file that has all
      * services that need to be registered by the configurator.
      */
     String SERVICE_NAMES = "serviceNames";
 
     /**
      * Properties file name that contains the names of service schema 
      * files be loaded by the configurator.
      */
     String SCHEMA_PROPERTY_FILENAME = "schemaNames";
 
     /**
      * Property keys in <code>SCHEMA_PROPERTY_FILENAME</code> file that has 
      * schema that needs to be loaded by the configurator.
      */
     String SMS_PROPERTY_FILENAME = "smsSchema";
     String SDK_PROPERTY_FILENAME = "sdkSchema";
 
     /**
      * Type of Data store used for storing the configuration files.
      */
     String SMS_DS_DATASTORE = "dirServer";
     String SMS_FF_DATASTORE = "flatfile";
 
     /**
      * Variable for org root suffix.
      */
     String ORG_ROOT_SUFFIX = "ORG_ROOT_SUFFIX";
 
     /**
      * Variable for relative distinguish name.
      */
     String RS_RDN = "RS_RDN";
 
     /**
      * Variable for default organization.
      */
     String DEFAULT_ORG = "DEFAULT_ORG";
 
     /**
      * Variable for default organization base suffix.
      */
     String ORG_BASE = "ORG_BASE";
 
     /**
      * Variable for normalized organization base suffix.
      */
     String NORMALIZED_ORG_BASE = "NORMALIZED_ORGBASE";
 
     /**
      * Variable for normalized relative distinguish name.
      */
     String NORMALIZED_RS = "NORMALIZED_RS";
 
     /**
      * Variable for normalized root suffix.
      */
     String NORMALIZED_ROOT_SUFFIX = "NM_ORG_ROOT_SUFFIX";
 
     /**
      * Flag to indicate if DIT is loaded in Directory Server.
      */
     String DIT_LOADED = "DIT_LOADED";
     
     /**
      * Configuration Variable for product name.
      */
     String CONFIG_VAR_PRODUCT_NAME = "IS_PRODNAME";
     
     /**
      * Configuration Variable for lagency console deployment URI.
      */
     String CONFIG_VAR_OLD_CONSOLE_URI  = "OLDCON_DEPLOY_URI";
 
     /**
      * Configuration variable for Platform Locale.
      */
     String CONFIG_VAR_PLATFORM_LOCALE = "PLATFORM_LOCALE";
 
     /**
      * Configuration Variable for console deployment URI.
      */
     String CONFIG_VAR_CONSOLE_URI  = "CONSOLE_URI";
     
     /**
      * Configuration Variable for server protocol.
      */
     String CONFIG_VAR_SERVER_PROTO = "SERVER_PROTO";
     
     /**
      * Configuration Variable for server host.
      */
     String CONFIG_VAR_SERVER_HOST = "SERVER_HOST";
     
     /**
      * Configuration Variable for server port.
      */
     String CONFIG_VAR_SERVER_PORT = "SERVER_PORT";
     
     /**
      * Configuration Variable for server deployment URI.
      */
     String CONFIG_VAR_SERVER_URI = "SERVER_URI";
 
     /**
      * Configuration Variable for server URL.
      */
     String CONFIG_VAR_SERVER_URL = "SERVER_URL";
 
     /**
      * Configuration Variable for encryption key.
      */
     String CONFIG_VAR_ENCRYPTION_KEY = "AM_ENC_KEY";
 
     /**
      * Configuration Variable for directory server administrator password.
      */
     String CONFIG_VAR_DS_MGR_PWD = "DS_DIRMGRPASSWD";
 
     /**
      * Configuration Variable for directory server administrator DN.
      */
     String CONFIG_VAR_DS_MGR_DN = "DS_DIRMGRDN";
 
     /**
      * Configuration Variable for directory server host.
      */
     String CONFIG_VAR_DIRECTORY_SERVER_HOST = "DIRECTORY_SERVER";
 
     /**
      * Configuration Variable for directory server port.
      */
     String CONFIG_VAR_DIRECTORY_SERVER_PORT = "DIRECTORY_PORT";
 
     /**
      * Configuration Variable for confirm directory server 
      * administrator password.
      */
     String CONFIG_VAR_CONFIRM_DS_MGR_PWD = "DS_CONFIRM_PWD";
 
     /**
      * Configuration Variable for administrator password.
      */
     String CONFIG_VAR_ADMIN_PWD = "ADMIN_PWD";
 
     /**
      * Configuration Variable for confirm administrator password.
      */
     String CONFIG_VAR_CONFIRM_ADMIN_PWD  = "ADMIN_CONFIRM_PWD";
 
     /**
      * Configuration Variable for server cookie domain.
      */
     String CONFIG_VAR_COOKIE_DOMAIN = "COOKIE_DOMAIN";
     
     /**
      * Configuration Variable for installation base directory.
      */
     String CONFIG_VAR_BASE_DIR  = "BASE_DIR";
     
     /**
      * Configuration Variable for root suffix.
      */
     String CONFIG_VAR_ROOT_SUFFIX = "ROOT_SUFFIX";
     
     /**
      * Configuration Variable for root suffix with carat suffix.
      */
     String CONFIG_VAR_ROOT_SUFFIX_HAT = "ROOT_SUFFIX_HAT";
 
 
     /**
      * Configuration Variable for bootstarp file base directory.
      */
     String CONFIG_VAR_BOOTSTRAP_BASE_DIR = "AccessManager";
 
     /**
      * Configuration Variable for bootstarp file base prefix.
      */
     String CONFIG_VAR_BOOTSTRAP_BASE_PREFIX = "AMConfig";
 
 
     /**
      * Configuration Variable for the type of data store used.
      */
     String CONFIG_VAR_DATA_STORE = "DATA_STORE";
 
     /**
      * Configuration Variable to indicate if User Management 
      * schema needs to be loaded. 
      */
     String CONFIG_VAR_DS_UM_SCHEMA = "DS_UM_SCHEMA";
 
     /**
      * Configuration Variable for Directory Server config store property.
      */
     String CONFIG_VAR_DS_DATASTORE_CLASS = "DS_OBJECT_CLASS";
 
     /**
      * Configuration Variable for Flat-file config store property.
      */
     String CONFIG_VAR_SMS_DATASTORE_CLASS = "SMS_OBJECT_CLASS";
 
     /**
      * Configuration Variable for default shared secret key.
      */
     String CONFIG_VAR_DEFAULT_SHARED_KEY = "KmhUnWR1MYWDYW4xuqdF5nbm+CXIyOVt";
 
     /**
      * Configuration Variable for service management root suffix with carat 
      * suffix.
      */
     String SM_ROOT_SUFFIX_HAT = "SM_ROOT_SUFFIX_HAT";
 
     /**
      * Variable for default organization base suffix for service management 
      * node.
      */
     String SM_CONFIG_BASEDN = "SM_CONFIG_BASEDN";
 
     /**
      * Configuration Variable for service management root suffix.
      */
     String SM_CONFIG_ROOT_SUFFIX = "SM_CONFIG_ROOT_SUFFIX";
     
 
 }
