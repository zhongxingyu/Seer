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
 	String PHRESCO_HYPEN = "phresco-";	
 	String INFO_XML = "-info.xml";
 	/*String FILE_SEPARATOR = "/";
 	String BUILD_INFO_FILE_NAME = "build.info";
 	String DO_NOT_CHECKIN_DIR = "do_not_checkin";
 	String BUILD_DIR = DO_NOT_CHECKIN_DIR + File.separator + "build";*/    
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
 	String NODE_CONFIG_JSON = "nodeconfig.json";
 	String HUB_CONFIG_JSON = "hubconfig.json";
 	String NODE_LOG = "node.log";
 	String HUB_LOG = "hub.log";
 	String POM_NAME = "pom.xml";
 	String DO_NOT_CHECKIN_DIRY = "do_not_checkin";
 	String LOG_DIRECTORY = "log";
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
     String SLASH = "/";
 
     //Action constants
     String TEST_FUNCTIONAL = "Functional";
 	String SITE_SQL = "site.sql";
 	String JSON_PATH = "/.phresco/sqlfile.json";
 	String PHRESCO_PLUGIN_INFO_XML = "phresco-plugin-info.xml";
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
 	
 	/**
 	 * Test and test report directory constants
 	 */
 	
 	String POM_PROP_KEY_UNITTEST_DIR = "phresco.unitTest.dir";
 	String POM_PROP_KEY_UNITTEST_RPT_DIR = "phresco.unitTest.report.dir";
 	String POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH = "phresco.unitTest.testsuite.xpath";
 	String POM_PROP_KEY_UNITTEST_TESTCASE_PATH = "phresco.unitTest.testcase.path";
 	String POM_PROP_KEY_LOADTEST_DIR = "phresco.loadTest.dir";
	String POM_PROP_KEY_LOADTEST_RPT_DIR = "phresco.loadTest.report.dir";
 	String POM_PROP_KEY_FUNCTEST_SELENIUM_TOOL = "phresco.functionalTest.selenium.tool";
     String POM_PROP_KEY_FUNCTEST_RPT_DIR = "phresco.functionalTest.report.dir";
     String POM_PROP_KEY_FUNCTEST_TESTSUITE_XPATH = "phresco.functionalTest.testsuite.xpath";
     String POM_PROP_KEY_FUNCTEST_TESTCASE_PATH = "phresco.functionalTest.testcase.path";
 	String POM_PROP_KEY_FUNCTEST_DIR = "phresco.functionalTest.dir";
 	String POM_PROP_KEY_PERFORMANCETEST_DIR = "phresco.performanceTest.dir";
 	String POM_PROP_KEY_PERFORMANCETEST_RPT_DIR = "phresco.performanceTest.report.dir";
 	String PHRESCO_FUNCTIONAL_TEST_ADAPT_DIR = "phresco.functionalTest.adapt.config";
 	String POM_PROP_KEY_SQL_FILE_DIR = "phresco.sql.path";
 	String POM_PROP_KEY_MODULE_SOURCE_DIR = "phresco.module.source.dir";
 	String POM_PROP_KEY_JSLIBS_SOURCE_DIR = "phresco.jslibs.source.dir";
 	String POM_PROP_KEY_COMPONENTS_SOURCE_DIR = "phresco.components.source.dir";
 	String POM_PROP_KEY_VALIDATE_REPORT = "phresco.code.validate.report";
 	/**
 	 * MVN Goal Constants
 	 */
 	String PHASE_PACKAGE = "package";
     String PHASE_DEPLOY = "deploy";
     String PHASE_VALIDATE_CODE = "validate-code";
     String PHASE_UNIT_TEST = "unit-test";
     String PHASE_FUNCTIONAL_TEST = "functional-test";
     String PHASE_PERFORMANCE_TEST = "performance-test";
     String PHASE_LOAD_TEST = "load-test";
     String PHASE_START_HUB = "start-hub";
     String PHASE_START_NODE = "start-node";
     String PHASE_STOP_HUB = "stop-hub";
     String PHASE_STOP_NODE = "stop-node";
     String PHASE_RUNGAINST_SRC_START = "start";
     String PHASE_RUNGAINST_SRC_STOP = "stop";
     String PHASE_PDF_REPORT = "pdf-report";
     String PHASE_CI = "ci";
     
     
     String JAVA_UNIX_PROCESS_KILL_CMD	= "kill -9 ";
 	 String WINDOWS_PLATFORM			= "Windows";
     /* Drupal Version */
 	String DRUPAL_VERSION = "drupal.version";
 	String WORDPRESS_VERSION = "wordpress.version";
 	String SONAR_EXCLUSION = "sonar.exclusions";
 	String COMMA = ",";
 	String SONAR_PHPPDEPEND_ARGUMENTLINE = "sonar.phpDepend.argumentLine";
 	String SONAR_PHPPMD_ARGUMENTLINE  = "sonar.phpPmd.argumentLine";
 	String IGNORE = "--ignore=";
 	String EXCLUDE = "--exclude ";
 	
 	/**
      *  Constants for Windows
      */
     String SOURCE_DIR 	= "source";
     String PROJECT_ROOT = "Metro.UI";
     String CSPROJ_FILE 	= ".csproj";
     String PROJECT 		= "Project";
     String ITEMGROUP 	= "ItemGroup";
     String REFERENCE 	= "Reference";
     String SRC_DIR		= "src";
     String INCLUDE = "Include";
     String DLL = ".dll";
     String DOUBLE_DOT  = "..";
     String HINTPATH = "HintPath";
     String COMMON = "\\Common";
 
     /**
      * 
      *  Phresco-plugin-info files Directory constants
      */
     String PACKAGE_INFO_FILE			= ".phresco/phresco-package-info.xml";
     String DEPLOY_INFO_FILE				= ".phresco/phresco-deploy-info.xml";
     String VALIDATE_CODE_INFO_FILE		= ".phresco/phresco-validate-code-info.xml";
     String UNIT_TEST_INFO_FILE			= ".phresco/phresco-unit-test-info.xml";
     String FUNCTIONAL_TEST_INFO_FILE	= ".phresco/phresco-functional-test-info.xml";
     String PERFORMENCE_TEST_INFO_FILE	= ".phresco/phresco-performence-test-info.xml";
     String LOAD_TEST_INFO_FILE			= ".phresco/phresco-load-test-info.xml";
     String CI_INFO_FILE					= ".phresco/phresco-ci-info.xml";
     String PDF_REPORT_INFO_FILE 		= ".phresco/phresco-pdf-report-info.xml";
     String REPORT_INFO_FILE 			= ".phresco/phresco-report-info.xml";
     String START_INFO_FILE			    = ".phresco/phresco-start-info.xml";
     String START_HUB_INFO_FILE 			= ".phresco/phresco-start-hub-info.xml";
     String START_NODE_INFO_FILE 		= ".phresco/phresco-start-node-info.xml";
     String STOP_INFO_FILE 				= ".phresco/phresco-stop-info.xml";
     String SONAR_INFO_FILE 				= ".phresco/phresco-sonar-info.xml";
 
     
 	/**
 	 * Feature Write in pom Constants
 	 */
     
     String GROUP_ID = "groupId" ;
     String ARTIFACT_ID = "artifactId";
     String VERSION = "version";
     String TYPE = "type";
     String ZIP = "zip";
     String OVER_WRITE = "overWrite";
     String OUTPUT_DIR = "outputDirectory";
     String OVER_WIRTE_VALUE = "false";
     String DEPENDENCY_PLUGIN_GROUPID = "org.apache.maven.plugins";
     String DEPENDENCY_PLUGIN_ARTIFACTID = "maven-dependency-plugin";
     String EXECUTION_ID = "unpack-module";
     String PHASE = "validate";
     String GOAL = "unpack";
     
 
 }
