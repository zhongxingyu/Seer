 /**
  * Phresco Commons
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.util;
 
 public interface ServiceConstants {
 
 	String REST_API_PROJECT =  "/project";
 	String REST_API_PROJECT_CREATE =  "/create";
 	String REST_API_PROJECT_UPDATE =  "/update";
 	String REST_API_PROJECTLIST = "/list";
 	String REST_API_PROJECT_EDIT = "/edit";
 	String REST_API_UPDATEPROJECT = "/updateproject";
 	String REST_API_UPDATE_FEATRUE = "/updateFeature";
 	String REST_UPDATE_APPLICATION = "/updateApplication";
 	String REST_API_EDIT_APPLICATION = "/editApplication";
 	String REST_API_PROJECT_DELETE = "/delete";
 	String REST_API_MODULE_DEPENDENTS = "/dependents";
 	String REST_API_GET_PERMISSION = "/getPermission";
 	String REST_API_APPINFOS = "/appinfos";
 	String REST_APP_UPDATEDOCS = "/updatedocs";
 	String REST_API_COMPONENT =  "/components";
 	String REST_API_FRAMEWORK_COMPONENT = "/framwork/components";
 	String REST_API_ADMIN =  "/admin";
 	String REST_API_CUSTOMERS = "/customers";
 	String REST_API_CUSTOMER = "/customer";
 	String REST_API_ICON = "/customers/icon";
 	String REST_CUSTOMER_PROPERTIES = "/customers/properties";
 	String REST_API_APPTYPES = "/apptypes";
 	String REST_API_PROPERTY = "/property";
 	String REST_API_CONFIG_TEMPLATES= "/configtemplates";
 	String REST_API_MODULES= "/modules";
 	String REST_API_MODULES_DESC= "/modules/desc";
 	String REST_API_PILOTS = "/pilots";
 	String REST_API_ARTIFACTINFO = "/artifactInfo";
 	String REST_API_SERVERS = "/servers";
 	String REST_API_DATABASES = "/databases";
 	String REST_API_WEBSERVICES = "/webservices";
 	String REST_API_REPORTS = "/reports";
 	String REST_API_OPTIONS = "/options";
 	String REST_API_OPTIONS_FUNCTIONAL_GRP = "/functionalframeworks";
 	String REST_API_OPTIONS_FUNCTIONAL = "/functionalframeworks/functional";
 	String REST_API_OPTIONS_CUSTOMER = "/options/customer";
 	String REST_API_TECHGROUPS = "/techgroups";
 	String REST_API_TECHNOLOGIES = "/technologies";
 	String REST_API_TECHINFO = "/techInfo";
 	String REST_API_DOWNLOADS = "/downloads";
 	String REST_API_GLOBALURL ="/globalurl";
 	String REST_API_VIDEOS = "/videos";
 	String REST_API_USERS = "/users";
 	String REST_API_USERS_IMPORT = "/users/import";
 	String REST_API_ROLES = "/roles";
 	String REST_API_VERSION = "/version";
 	String REST_API_FORUMS = "/forums";
 	String REST_API_PERMISSIONS = "/permissions";
 	String REST_API_LDAP = "/settings/ldap";
 	String REST_API_DOWNLOADIPA = "/Ipadownload";
 	String REST_API_SETTINGS = "/settings";
 	String REST_API_PREBUILT = "/prebuilt";
 	String REST_API_PLATFORMS = "/platforms";
 	String REST_API_TWEETS = "/tweets";
 	String REST_API_LICENSE = "/licenses";
 	String REST_API_JSBYID = "/modules/js";
 	String REST_API_PILOTSBYID = "/pilots/id";
 	String REST_API_LOGIN = "/login";
 	String REST_API_ENV_PATH = "/settings/env";
 	String REST_LOGIN_PATH = "/service/rest/api/login";
 	String REST_API_REPO = "/repo";
 	String REST_API_UNIT = "/unit";
 	String REST_API_TEST_SUITES = "/testsuites";
 	String REST_API_TEST_REPORTS = "/testreports";
 	String REST_API_FUNCTIONAL_FRAMEWORK = "/functionalFramework";
 	String REST_API_PERFORMANCE = "/performance";
 	String REST_API_TEST_RESULT_FILES = "/testResultFiles";
 	String REST_API_DEVICES = "/devices";
 	String REST_API_LOAD = "/load";
 	String REST_API_PERFORMANCE_RESULTS = "/performanceTestResults";
 	String REST_API_LOAD_RESULTS = "/loadTestResults";
 	String REST_API_CUSTOMER_THEME = "/theme";
 	String REST_API_FAVOURITE_ICON = "/favIcon";
 	String REST_API_LOGIN_ICON = "/loginIcon";
 	String REST_API_DASHBOARD ="/dashboard";
 	String REST_API_DASHBOARD_ID ="/{dashboardid}";
 	String REST_API_WIDGET ="/widget";
 	String REST_API_WIDGET_ID ="/widget/{widgetid}";
 	String REST_API_WIDGET_SEARCH ="/search";
 
 	
 	String REST_API_CI_CONFIG = "/ci/config";
 	String REST_API_CI_CREDENTIAL = "/ci/credentialsxml";
 	String REST_API_CI_JDK_HOME = "/ci/javahomexml";
 	String REST_API_CI_MAVEN_HOME = "/ci/mavenhomexml";
 	String REST_API_CI_MAILER_HOME = "/ci/mailxml";
 	String REST_API_CI_MAIL_PLUGIN = "/ci/emailext";
 	String REST_API_LOG = "/log";
 	String REST_API_MANUAL = "/manual";
 	String REST_API_MANUALTEMPLATE = "/manualTemplate";
 	String REST_API_UPLOADTEMPLATE = "/uploadTemplate";
 	String REST_API_TESTSUITES = "/testsuites";
 	String REST_API_TESTSUITE = "testSuiteName";
 	String REST_API_TESTSUITES_DELETE = "/deleteTestsuite";
 	String REST_API_TESTCASE_DELETE = "/deleteTestCase";
 	String REST_API_TESTCASES = "/testcases";
 	String REST_API_TESTCASE_VALIDATION = "/testcaseValidation";
 	String REST_API_LDAP_PARAM_ID = "ldap";
 	String REST_API_PATH_ID = "/{id}";
 	String REST_API_PATH_PARAM_ID = "id";
 	String REST_API_QUERY_PARAM_SESSION_ID = "sessionId";
 	String REST_API_QUERY_PARAM_LIMIT = "limit";
 	String REST_API_QUERY_PARAM_OFFSET = "offset";
 	
 	String ERROR_MSG_UNSUPPORTED_OPERATION = "{0} operation is not allowed";
 	String ERROR_MSG_NOT_FOUND = "Content Not Found";
 	String ERROR_MSG_ID_NOT_EQUAL = "Given Id Not Equal";
 	String REST_QUERY_TECHID = "techId";
 	String REST_QUERY_ISPILOT = "pilot";
 	String REST_LIMIT_VALUE = "limitValue";
 	String REST_SKIP_VALUE = "skipValue";
 	String REST_QUERY_PROJECTID = "projectId";
 	String REST_QUERY_TYPE = "type";
 	String REST_QUERY_PLATFORM = "platform";
 	String REST_QUERY_TYPE_MODULE = "module";
 	String REST_QUERY_TYPE_COMPONENT = "component";
 	String REST_QUERY_TYPE_JS = "js";
 	String REST_QUERY_ID = "id";
 	String REST_QUERY_CUSTOMERID = "customerId";
 	String REST_QUERY_APPLIESTO = "appliesTo";
 	String REST_QUERY_APPTYPEID = "appTypeId";
 	String REST_QUERY_USERID = "userId";
 	String REST_QUERY_NATURE = "nature";
 	String REST_QUERY_VERSION = "version";
 	String REST_QUERY_APPDIR_NAME = "appDirName";
 	String REST_QUERY_TESTCASE_NAME = "testCaseId";
 	String REST_QUERY_RESULT_FILE_NAME = "resultFileName";
 	String REST_QUERY_DEVICE_ID = "deviceId";
 	String REST_QUERY_SHOW_GRAPH_FOR = "showGraphFor";
 	String REST_QUERY_TEST_AGAINST = "testAgainst";
 	String REST_QUERY_OLD_APPDIR_NAME = "oldAppDirName";
 	String REST_QUERY_REPORT_FILE_NAME = "reportFileName";
 	String REST_QUERY_CUSTOMER_NAME = "customerName";
 	String REST_QUERY_BUILD_NUMBER = "buildNumber";
 	String REST_QUERY_NAME = "name";
 	String REST_QUERY_CONTINOUSNAME = "continuousName";
 	String REST_QUERY_OLDNAME = "oldname";
 	String REST_QUERY_ENV_NAME = "envName";
 	String REST_JOBNAME = "jobName";
 	String REST_QUERY_EMAIL_ADDRESS = "emailAddress";
 	String REST_QUERY_EMAIL_PASSWORD = "emailPassword";
 	String REST_QUERY_APPID = "appId";
 	String REST_QUERY_SUBMODULEIDS = "subModuleIds";
 	String REST_QUERY_APPNAME = "appName";
 	String REST_QUERY_APPCODE = "appCode";
 	String REST_QUERY_BUILD_DOWNLOAD_URL = "buildDownloadUrl";
 	String REST_QUERY_DOWNLOAD_JOB_NAME = "downloadJobName";
 	String REST_QUERY_SENDER_MAIL_ID = "senderEmailId";
 	String REST_QUERY_SENDER_MAIL_PWD = "senderEmailPassword";
 	String REST_QUERY_URL = "url";
 	String REST_QUERY_EVN_NAME = "envName";
 	String REST_QUERY_CLONE_NAME = "cloneName";
 	String REST_QUERY_ENVIRONMENT = "environment";
 	String REST_QUERY_GOAL = "goal";
 	String REST_QUERY_PHASE = "phase";
 	String REST_QUERY_KEY = "key";
 	String REST_QUERY_VALUE = "value";
 	String REST_QUERY_VALIDATE_AGAINST = "validateAgainst";
 	String REST_QUERY_FROM_PAGE = "fromPage";
 	String REST_QUERY_PILOT_ID = "pilotId";
 	String REST_QUERY_TEST_TYPE = "testType";
 	String REST_QUERY_TECH_REPORT = "techReport";
 	String REST_QUERY_MODULE_NAME = "moduleName";
 	String REST_QUERY_PROJECT_CODE = "projectCode";
 	String REST_QUERY_ROOT_MODULE_NAME = "rootModule";
 	String REST_QUERY_TEST_SUITE ="testSuite";
 	String REST_QUERY_ACTION = "action";
 	String REST_QUERY_CONTEXT = "context";
 	String REST_QUERY_FEATURENAME = "featureName";
 	String REST_QUERY_FEATURETYPE = "featureType";
 	String REST_QUERY_DATATYPE = "datatype";
 	String REST_QUERY_USER_NAME = "username";
 	String REST_QUERY_PASSWORD = "password";
 	String REST_QUERY_TFS_URL = "tfsUrl";
 	String REST_QUERY_WIDGET_QUERY = "query";
 	String REST_QUERY_WIDGET_ID = "widgetid";
 	String REST_QUERY_WIDGET_NAME = "name";
 	String REST_QUERY_WIDGET_AUTOREFRESH = "autorefresh";
 	String REST_QUERY_WIDGET_START_TIME = "starttime";
 	String REST_QUERY_WIDGET_END_TIME = "endtime";
 	String REST_QUERY_DASHBOARD_NAME = "dashboardname";
 	String REST_QUERY_DASHBOARD_ID = "dashboardid";
 	String REST_QUERY_COMMENT		= "comment";
 	String REST_QUERY_CURRENT_BRANCH_NAME = "currentbranchname";
 	String REST_QUERY_TAG_NAME		      = "tagname";
 	String REST_QUERY_BRANCH_NAME		  = "branchname";
 	String REST_QUERY_DOWNLOAD_OPTION	  = "downloadoption";
 
 
 	
 	String DEFAULT_CUSTOMER_NAME = "photon";
 	String REST_ACTION_TYPE = "actionType";
 	String PROJECT_NAME = "name";
 	String DEFAULT_REPO = "default";
 	String APPTYPE = "appType";
 	String LOGIN = "login";
 	String ADMIN_CUSTOMER = "admin/customers";
 	String CUSTOMER_IMAGE= "admin/icon";
 	String CUSTOMER_PROPERTIES= "admin/customerproperties";
 	String FEATURE_TYPE_JS = "JAVASCRIPT";
 	String JS_GROUP_ID = "jslibraries.files";
 	String ICON_EXT = "png";
 	String UPDATE_REPO_ID = "updateRepo";
 	String VIDEO_REPO_ID  = "videoRepo";
 	String REST_QUERY_FILETYPE = "fileType";
 	String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
 	String PARAMETER_RETURNED_SUCCESSFULLY = "Parameter returned successfully";
 	String DEVICES_RETURNED_SUCCESSFULLY = "Devices returned successfully";
 	String PARAMETER_NOT_FETCHED = "Parameter not fetched";
 	String NO_PARAMETER_AVAILABLE = "No Parameter Available";
 	String DEPENDENCY_NOT_FETCHED = "Dependency not fetched";
 	String DEPENDENCY_RETURNED_SUCCESSFULLY = "Dependency returned successfully";
 	String FAILURE = "failure";
 	String MAP_NOT_UPDATED = "Map not updated";
 	
 	String MAP_UPDATED_SUCCESSFULLY = "Map updated successfully";
 	String PROJECT_MODULES = "projectModules";
 	String REPORT_OPTIONS = "reportOptions";
 	String TEST_SUITES_LISTED_SUCCESSFULLY = "Test Suites listed successfully";
 	String TEST_SUITES_LISTED_FAILED = "Test Suites listed failed";
 	String TEST_RESULT_NOT_AVAILABLE = "Test Result not available";
 	String FAILED_TO_GET_FUNCTIONAL_FRAMEWORK = "Failed to get the functional test framework";
 	String FUNCTIONAL_TEST_FRAMEWORK_FETCHED_SUCCESSFULLY = "Functional test framework fetched Successfully";
 	String HUB_STATUS = "hubStatus";
 	String FUNCTIONAL_IFRAME_URL = "iframeUrl";
 	String NODE_STATUS = "nodeStatus";
 	String FUNCTIONAL_FRAMEWORK = "functionalFramework";
 	String TEST_CASE_NOT_AVAILABLE = "TestCase Not Available";
 	String TEST_CASES_LISTED_SUCCESSFULLY = "Test Cases listed successfully";
 	String TEST_CASES_LISTED_FAILED = "Test Cases listed failed";
 	String UNABLE_TO_GET_PERFORMANCE_TEST_RESULT_OPTIONS = "Unable to get performance test result options";
 	String UNABLE_TO_GET_PERFORMANCE_TEST_RESULTS = "Unable to get performance test results";
 	String DEVICES = "devices";
 	String SHOW_DEVICE = "showDevice";
 	String TEST_RESULT_FILES = "testResultFiles";
 	String RESULT_AVAILABLE = "resultAvailable";
 	String TEST_AGAINSTS = "testAgainsts";
 	String RESULT_FILE_NOT_FOUND = "Result File Not Found";
 	String UNABLE_TO_GET_LOAD_TEST_REULTS = "Unable to get load test reults";
 	String UNABLE_TO_GET_LOAD_TEST_RESULT_OPTIONS = "Unable to get load test result options";
 	String CUSTOMER_THEME_SUCCESS_STATUS = "Customer theme fetched";
 	String CUSTOMER_FAVOURITE_ICON_SUCCESS_STATUS = "Favourite Icon fetched";
 	String CUSTOMER_LOGIN_ICON_SUCCESS_STATUS = "Main Logo fetched";
 	String UNABLE_FETCH_CUTOMER_FAILURE_THEME = "Customer theme is not fetched";
 	String UNABLE_FETCH_FAVOURITE_ICON = "Favourite Icon is not fetched";
 	String UNABLE_FETCH_MAIN_LOGO_ICON = "Login Logo is not fetched";
 	
 	String PROJECT_CREATED_SUCCESSFULLY = "Project created Successfully";
 	String PROJECT_CREATED_FAILED = "Project creation failed";
 	String PROJECT_LIST_FAILED = "Project List failed";
 	String PROJECT_EDITED_SUCCESSFULLY = "Project edited Successfully";
 	String PROJECT_EDITED_FAILED = "Project edit failed";
 	String PROJECT_UPDATED_SUCCESSFULLY= "Project updated Successfully";
 	String PROJECT_UPDATED_FAILED = "Project update failed";
 	String FEATURE_UPDATE_FAILED = "update Feature failed";
 	String FEATURE_UPDATE_SUCCESS = "Features updated successfully";
 	String APPLICATION_UPDATE_FAILED = "Application update Failed";
 	String APPLICATION_UPDATED_SUCCESSFULLY = "Application updated successfully";
 	String APPLICATION_EDITED_SUCCESSFULLY = "Application edited Successfully";
 	String APPLICATION_EDITED_FAILED = "Application edit Failed";
 	String UNABLE_APPLICATION_DELETE = "Unable to delete the Application";
 	String APPLICATION_DELETED_SUCCESSFULLY = "Application deleted Successfully";
 	String PERMISSION_FOR_USER_RETURNED_SUCCESSFULLY = "Permission for user returned Successfully";
 	String PERMISSION_FOR_USER_NOT_RETURNED = "Permission for user not fetched";
 	String RESULTS_JMETER_GRAPHS = "/results/jmeter/graphs/";
 	String PLUGIN_TYPES = "pluginTypes";
 	String JMETER_MAVEN_PLUGIN = "jmeter-maven-plugin";
 	String COM_LAZERYCODE_JMETER = "com.lazerycode.jmeter";
 	String TEST_RESULT_FILES_RETURNED_FAILED = "Test Result Files returned Failed";
 	String DEVICES_RETURNED_FAILED = "Devices returned failed";
 	String TEST_NOT_YET_EXECUTED_FOR = "Test not yet executed for ";
 	String TEST_RESULT_FILES_RETURNED_SUCCESSFULLY = "Test Result Files returned successfully";
 	 /*
      * Constants for MongoDB Collections
      */
 	String CUSTOMERS_COLLECTION_NAME = "customers";
 	String FORUM_COLLECTION_NAME = "forums";
 	String VIDEOS_COLLECTION_NAME = "videos";
 	String VIDEODAO_COLLECTION_NAME = "videoDAOs";
 	String VIDEOTYPES_COLLECTION_NAME = "videotypes";
 	String VIDEOTYPESDAO_COLLECTION_NAME = "videotypeDAOs";
 	String USERS_COLLECTION_NAME = "users";
 	String LOG_COLLECTION_NAME = "adminLog";
 	String DOWNLOAD_COLLECTION_NAME = "downloads";
 	String GLOBALURL_COLLECTION_NAME="globalurl";
 	String APPTYPES_COLLECTION_NAME = "apptypes";
 	String PILOTS_COLLECTION_NAME = "pilots";
 	String SERVERS_COLLECTION_NAME = "servers";
 	String DATABASES_COLLECTION_NAME = "databases";
 	String WEBSERVICES_COLLECTION_NAME = "webservices";
 	String SETTINGS_COLLECTION_NAME = "settings";
 	String TECHNOLOGIES_COLLECTION_NAME = "technologies";
 	String USERDAO_COLLECTION_NAME = "userdaos";
 	String ROLES_COLLECTION_NAME = "roles";
 	String PERMISSION_COLLECTION_NAME = "permissions";
 	String APPTYPESDAO_COLLECTION_NAME = "apptypedao";
 	String CUSTOMERDAO_COLLECTION_NAME = "customers";
 	String CREATEDPROJECTS_COLLECTION_NAME = "createdprojects";
 	String ARCHETYPEINFO_COLLECTION_NAME = "archetypes";
 	String ARTIFACT_GROUP_COLLECTION_NAME = "artifactGroupDAOs";
 	String ARTIFACT_INFO_COLLECTION_NAME = "artifactInfos";
 	String APPLICATION_INFO_COLLECTION_NAME = "applicationInfos";
 	String PLATFORMS_COLLECTION_NAME = "platforms";
 	String REPORTS_COLLECTION_NAME = "reports";
 	String PROPERTIES_COLLECTION_NAME = "properties";
 	String OPTIONS_COLLECTION_NAME = "options";
 	String FUNCTIONAL_FRAMEWORK_COLLECTION_NAME = "functionalFrameworks";
 	String CUSTOMER_OPTIONS_COLLECTION_NAME = "customerOptions";
 	String LICENSE_COLLECTION_NAME ="Licenses";
 	String TECH_GROUP_COLLECTION_NAME = "techgroup";
 	String TECH_GROUP_ID = "techGroupIds";
 	String FRAMEWORK_THEME_COLLECTION_NAME = "frameworkTheme";
 	String REPOINFO_COLLECTION_NAME = "repoInfo";
 	String ARTIFACT_ELEMENT_COLLECTION_NAME = "artifactElement";
 	String PROJECTINFO_COLLECTION_NAME = "projectInfo";
 	String PROPERTY_TEMPLATE_COLLECTION_NAME = "propertyTemplates";
 	String FUNCTIONAL_FRAMEWORK_GRP_COLLECTION_NAME = "functionalFrameworkGroup";
 	
 	/*
 	 * DB query params
 	 */
 	String DB_COLUMN_CUSTOMERIDS = "customerIds";
     String DB_COLUMN_ARTIFACT_GROUP_ID = "artifactGroupId";
     String DB_COLUMN_ARTIFACT_GROUP_TYPE = "type";
     String DB_COLUMN_TECHID = "techId";
     String DB_COLUMN_VERSIONIDS = "versionIds";
     String DB_COLUMN_APPLIESTOTECHID = "appliesTo.techId";
 	String DB_COLUMN_CREATIONDATE = "creationDate";
 	String DB_COLUMN_VIDEOINFOID = "videoInfoId";
 	String DB_COLUMN_PLATFORM = "platformTypeIds";
 	String DB_COLUMN_PREBUILT = "preBuilt";
 	
 	/*
      * Constants for Exception Message keys
      */
 	String EX_PHEX00001 = "PHEX00001";
 	String EX_PHEX00002 = "PHEX00002";
 	String EX_PHEX00003 = "PHEX00003";
 	String EX_PHEX00004 = "PHEX00004";
 	String EX_PHEX00005 = "PHEX00005";
 	String EX_PHEX00006 = "PHEX00006";
 	String EX_PHEX00007 = "PHEX00007";
 	
 	/*
      * Constants for Operatins
      */
 	String UPDATE = "Update";
 	String INSERT = "Insert";
 	String DELETE = "Delete";
 	
 	/*
 	 * Constants for URL in ServiceBaseAction
 	 */
 	String COLON_DOUBLE_SLASH = "://";
 	String COLON = ":";
 	String SLASH_REST_SLASH_API = "/rest/api";
 	String OPEN_PHRASE = "{";
 	String CLOSE_PHRASE = "}";
 	String ALL_HEADER = "*";
 	
 	/*
      * Constants for Fields
      */
 	String REST_API_FIELD_TECH = "technologies";
 	String REST_API_FIELD_APPID = "appTypeId";
 	String REST_API_USED = "used";
 	String REST_API_NAME = "name";
 	String PASSWORD = "password";
 	String AUTHTYPE = "authType";
 	String REST_API_ARTIFACTID = "artifactId";
 	String REST_API_MODULEID = "moduleGroupId";
 	String CATEGORY = "category";
 	String SERVER = "Server";
 	String DATABASE = "Database";
 	String APPLIES_TO_TECHIDS = "appliesToTechIds";
 	String TECHINFO_VERSION = "techInfo._id";
 	/*
      * Constants for Media Type
      */
 	String MEDIATYPE_ZIP = "application/zip";
 	
 	/*
      * Constants for Response Code
      */
 	int RES_CODE_200 = 200;
 	int RES_CODE_201 = 201;
 	
 	/*
      * Constants for Media Type
      */
 	String REPOTYPE_RELEASE = "RELEASE";
 	String REPOTYPE_SNAPSHOT = "SNAPSHOT";
 	String REPOTYPE_GROUP = "group";
 	String REPO_LOCAL="/service/local/repositories/";
 	String REPO_GROUPURL = "/service/local/repo_groups/";
 	String REPO_GROUP_PATH = "/service/local/repo_groups?undefined";
 	String REPO_GROUP_CONTENT = "/content/groups/";
 	String REPO_HOSTED_PATH = "/service/local/repositories?undefined";
 	String REPO_HOSTED_CONTENT = "/content/repositories/";
 	String REPO_PROVIDER = "maven2";
 	String REPO_PROVIDER_ROLE = "org.sonatype.nexus.proxy.repository.Repository";
 	String REPO_ALLOW_WRITE = "ALLOW_WRITE";
 	int NOT_FOUND_CACHE = 1440;
 	String REPO_HOSTED = "hosted";
 	String SLASH = "\"";
 	String FORWARD_SLASH="/";
 	String DOT=".";
 	String HYPEN = "-";
 	String COMMA = ",";
 	String CONTENT="/content/";
 	String LOCAL_REPO_GROUP = "/service/local/repo_groups/";
 	String REPO_RELEASE_NAME = "release";
 	String REPO_SNAPSHOT_NAME = "snapshot";
 	String REPO_OBJECT_ID = "data";
 	String PHRESCO_REPO_NAME = "Releases";
 	String REPO_FAILURE_MSG = "Repository Creation Failed Repository Already Exists......";
 	
 	/*
      * Constants for Object Validation
      */
 	String VAL_ID_MSG = "Id Should Not Be Null";
 	String VAL_NAME_MSG = "Name Should Not Be Null";
 	String VAL_APPID_MSG = "ApptypeId Should Not Be Null";
 	String VAL_ARCHETYPE_MSG = "Archetype Should Not Be Null";
 	String VAL_TECHGRPID_MSG = "TechnologyGroupId Should Not Be Null";
 	String VAL_CUSID_MSG = "Customerids Should Not Be Null";
 	String UNAUTHORIZED_USER = "UnAuthorized User";
 	
 	/*
      * Constants for Repository Manager Impl
      */
 	
 	String REPO_MGR_IMPL_GET_MODULES = "RepositoryManagerImpl.getModules";
 	String REPO_MGR_IMPL_ADD_ARTIFACT = "RepositoryManagerImpl.addArtifact";
 	String REPO_MGR_IMPL_IS_EXIST = "RepositoryManagerImpl.isExist";
 	String REPO_MGR_IMPL_GET_ARTF_AS_STRING = "RepositoryManagerImpl.getArtifactAsString";
 	String REPO_MGR_IMPL_GET_ARTF_AS_STREAM = "RepositoryManagerImpl.getArtifactAsStream";
 	String STATUS_BAD_REQUEST = "status=\"Bad Request\"";
 	String MESSAGE_CUST_ID_EMPTY = "message=\"customerId is empty\"";
 	String CUSTOMER_ID_EQUALS = "customerId=";
 	String CUST_ID_EMPTY = "customerId is Empty";
 	
 	/*
      * Constants for Dependency Manager Impl
      */
 	String DEPENDENCY_MGR_IMPL_CONF_PROJ = "DependencyManagerImpl.configureProject";
 	String DEPENDENCY_MGR_IMPL_UPDATE_POM_WITH_PLUGIN_ARTF = "DependencyManagerImpl.updatePOMWithPluginArtifact";
 	String PATH_EQUALS_SLASH = "path=\"";
 	String STATUS_FAILURE = "status=\"Failure\"";
 	
 	/*
      * Constants for Db Manager Impl
      */
 	String CUSTOMER_ID_EQUALS_SLASH = "customerId=\"";
 	String DB_MGR_IMPL_FIND_SELCTD_ARTF = "DbManagerImpl.findSelectedArtifacts";
 	String DB_MGR_IMPL_GET_ARTF_GRP = "DbManagerImpl.getArtifactGroup";
 	String MESSAGE_EQUALS = "message=";
 	
 	/*
      * Constants for Archetype Executor Impl
      */
 	String ARCHETYPE_EXE_IMPL_UPDATE_REPO = "ArchetypeExecutorImpl.updateRepository";
 	
 	/*
      * Constants for Dependency Utils
      */
 	String DEPENDENCY_UTIL_EXTRACT_FILES = "DependencyUtils.extractFiles";
 	
 	/*
      * Constants for DbService
      */
 	String UNCHECKED = "unchecked";
 	/*
      * Constants for Numbers
      */
 	
 	int NUMBER_FIVE = 5;
 	int NUMBER_SEVEN = 7;
 }
