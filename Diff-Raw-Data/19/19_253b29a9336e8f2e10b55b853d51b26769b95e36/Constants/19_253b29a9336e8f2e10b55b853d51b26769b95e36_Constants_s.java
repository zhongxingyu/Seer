 /*
  * ###
  * Phresco Commons
  * 
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 package com.photon.phresco.util;
 
 public interface Constants {
 
 	int STATUS_OPEN = 0;
 	int STATUS_EXPIRED = 1;
 	int STATUS_LOCKED = 2;
 	int STATUS_DELETED = 3;
 
     String SPACE = " ";
 	String REM_DELIMETER = "REM";
     String PROJECT_FOLDER = "project";
     String OSNAME  = "os.name";
     String WINDOWS = "Windows";
 
     String PHRESCO_HOME = "PHRESCO_HOME";
     String USER_HOME = "user.home";
     String JAVA_TMP_DIR = "java.io.tmpdir";
     String PROJECTS_HOME = "projects";
     String PROJECTS_WORKSPACE = "workspace";
     String PROJECTS_TEMP = "temp";
     String ARCHIVE_HOME = "workspace/archive";
     String PROJECT_INFO_FILE = "project.info";
     String TOOLS_DIR = "tools";
     String JENKINS_DIR = "jenkins";
     String PLUGIN_DIR = "plugins";
     String TEMPLATE_DIR = "template";
     String JENKINS_HOME = "JENKINS_HOME";
     String TYPE_TOMCAT	= "Apache Tomcat";
 	String TYPE_JBOSS = "JBoss";
 	String TYPE_WEBLOGIC ="WebLogic";
 	String PHRESCO = "phresco";
 	String CONFIGURATION_INFO_FILE = "phresco-env-config.xml";
 	String DOT_PHRESCO_FOLDER = ".phresco";
 	String SETTINGS_XML = "settings.xml";
	
    
     // Constants for Maven
     String MVN_COMMAND = "mvn";
     String MVN_ARCHETYPE = "archetype";
     String MVN_GOAL_GENERATE = "generate";
     String MVN_GOAL_PACKAGE = "package";
     String MVN_GOAL_DEPLOY = "deploy";
     /*String MVN_PLUGIN_PHP_ID = "php:";
     String MVN_PLUGIN_DRUPAL_ID = "drupal:";
     String MVN_PLUGIN_ANDROID_ID = "android:";
     String MVN_PLUGIN_JAVA_ID = "java:";
     String MVN_PLUGIN_NODEJS_ID = "nodejs:";
     String MVN_PLUGIN_SHAREPOINT_ID = "sharepoint:";
     String MVN_PLUGIN_DOTNET_ID = "dotnet:";
     String MVN_PLUGIN_SITECORE_ID ="sitecore:";
     String MVN_PLUGIN_IPHONE_ID = "xcode:";
     String MVN_PLUGIN_WORDPRESS_ID = "wordpress:";
     String MVN_PLUGIN_WINDOWS_PHONE_ID = "windows-phone:";
     String MVN_PLUGIN_BLACKBERRY_ID = "blackberry:"*/
 	String MVN_PLUGIN_PHRESCO_ID = "phresco:";
 
     //Constants for Authentication Token
     String AUTH_TOKEN = "auth_token";
     
     // Constants for Server
     String SETTINGS_TEMPLATE_SERVER = "Server";
     String SETTINGS_TEMPLATE_BROWSER = "Browser";
     String SERVER_PROTOCOL = "protocol";
     String SERVER_HOST = "host";
     String SERVER_PORT = "port";
     String SERVER_ADMIN_USERNAME = "admin_username";
     String SERVER_ADMIN_PASSWORD = "admin_password";
     String SERVER_DEPLOY_DIR = "deploy_dir";
     String SERVER_TYPE = "type";
     String SERVER_CONTEXT = "context";
     String SERVER_VERSION = "version";
 	String SERVER_REMOTE_DEPLOYMENT = "remoteDeployment";
 	String SITECORE_INST_PATH = "sitecoreInstPath";
     String SITE_NAME = "siteName";
     String APPLICATION_NAME = "applicationName";
 
 
     // Constants for Database
     String DB_PROTOCOL="http";
     String SETTINGS_TEMPLATE_DB = "Database";
     String DB_NAME = "dbname";
     String DB_HOST = "host";
     String DB_PORT = "port";
     String DB_USERNAME = "username";
     String DB_PASSWORD = "password";
     String DB_TYPE = "type";
     String DB_DRIVER = "driver";
     String DB_TABLE_PREFIX = "table_prefix";
     String DB_VERSION = "version";
 	String MONGO_DB = "mongodb";
     String ORACLE_DB = "oracle";
     String MYSQL_DB = "mysql";
     String HSQL_DB = "hsql";
     String DB2_DB = "db2";
     String MSSQL_DB = "mssql";
 
     // Constants for Email
     String SETTINGS_TEMPLATE_EMAIL = "Email";
     String EMAIL_HOST = "host";
     String EMAIL_PORT = "port";
     String EMAIL_USER = "username";
     String EMAIL_PASSWORD = "password";
 
     // Constants for Web Service
     String SETTINGS_TEMPLATE_WEBSERVICE = "WebService";
     String WEB_SERVICE_PROTOCOL = "protocol";
     String WEB_SERVICE_HOST = "host";
     String WEB_SERVICE_PORT = "port";
     String WEB_SERVICE_CONTEXT = "context";
     String WEB_SERVICE_CONFIG_URL="configUrl";
     String WEB_SERVICE_USERNAME = "username";
     String WEB_SERVICE_PASSWORD = "password";
 
     String BROWSER_NAME = "browser.name";
     
     //Common contants
     String STR_MINUSD = "-D";
     String STR_COLON = ":";
     String STR_EQUAL = "=";
     String STR_DOUBLE_QUOTES = "\"";
 
     //Action constants
     String TEST_FUNCTIONAL = "Functional";
 	String SITE_SQL = "site.sql";
 	String JSON_PATH = "/.phresco/sqlfile.json";
 	String DB_MYSQL   = "mysql";
 	/*
 	 * Constants for String
 	 */
 	String STR_BLANK_SPACE = " ";
 	String STR_EQUALS = "=";
 	String STR_COMMA = ",";
 	String CMD_ARG_VAR = "-D";
 	String STR_UNDER_SCORE="_";
 	/*
 	 *  Constants for System properties
 	 */
 	String USER_HOME_DIR = "user.home";
 	String OS_NAME = "os.name";
 	
 	/*
 	 * Constants for Rest
 	 */
 	String PHR_AUTH_TOKEN = "X-phr-auth-token";
 	String ARTIFACT_COUNT_RESULT = "X-Result-Count";
 	
 	//constants for server version
 	
 	String WEBLOGIC_12c = "12c(12.1.1)";
 	String WEBLOGIC_11gR1 = "11gR1(10.3.6)";
 	String WEBLOGIC_12c_PLUGIN_VERSION = "12.1.1.0";
 	String WEBLOGIC_11gr1c_PLUGIN_VERSION = "10.3.6.0";
 	
 	/*
 	 * Constants for Framework Options
 	 */
 	
 	String ENABLE_CODE_VALIDATION = "lbl.framework.options.code.validation";
 	String ENABLE_BUILD = "lbl.framework.options.build";
 	String ENABLE_DEPLOY = "lbl.framework.options.deploy";
 	String ENABLE_RUN_AGAINST_SOURCE = "lbl.framework.options.run.against.source";
 	String ENABLE_UNIT_TEST = "lbl.framework.options.unit.test";
 	String ENABLE_FUNCTIONAL_TEST = "lbl.framework.options.functional.test";
 	String ENABLE_PERFORMANCE_TEST = "lbl.framework.options.performance.test";
 	String ENABLE_LOAD_TEST = "lbl.framework.options.load.test";
 	String ENABLE_CONTINUOUS_INTEGRATION = "lbl.framework.options.continous.integration";
 	String ENABLE_JS_LIBRARIES = "lbl.framework.options.js.libraries";
 
 }
