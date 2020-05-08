 /*
  * ###
  * Service Web Archive
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
 package com.photon.phresco.service.admin.commons;
 
 public interface ServiceUIConstants {
 
     /*****************************
      * Common Constants
      *****************************/ 
     String SQUARE_CLOSE = "]";
     String COMMA = ",";
     String DOT = ".";
     String LBL_HDR = "lbl.hdr.";
     String LBL_BTN = "lbl.btn";
     String LBL_URL = "lbl.pageurl.";
     String LBL_PROG_TXT = "lbl.prog.txt.";
     String SQUARE_OPEN = "[";
     String ADD = "add";
     String REQ_FROM_PAGE = "fromPage";
     String EDIT = "edit";
     String REQ_USER_INFO = "userInfo";
     String SESSION_USER_INFO = "userInfo";
     String SUCCESS_FALSE = "{\"success\": false}";
 	String SUCCESS_TRUE = "{\"success\": true}";
 	String MAVEN_JAR_FALSE = "{\"mavenJar\": false,\"success\": true}";
 	String INVALID_ARCHETYPE_JAR = "{\"isJarTypeValid\": false,\"errorMsg\": \"Not a valid application jar\"}";
 	String INVALID_PLUGIN_JAR = "{\"isJarTypeValid\": false,\"errorMsg\": \"Not a valid plugin jar\"}";
 	String CSV_PATTERN = "\\s*,\\s*";
     
 	String VIDEOS = "video";
 	String ARCHETYPES = "archetype";
 	String CONFIG_TEMPLATES = "configtemp";
 	String PILOT_PROJECTS = "pilot";
 	String DOWNLOADS = "download";
     
 	/*****************************
      * Login Request Constants
      * String REQ_LOGIN_XXX
      *****************************/	
     String REQ_LOGIN_ERROR = "loginError";
     
     
     /*****************************
      * AppType Request Constants
      * String REQ_APP_XXX
      *****************************/
     String REQ_APP_TYPE = "appType";
     String REQ_APP_TYPES = "appTypes";  
     String REQ_APP_TYPEID = "apptypeId";
     
     
     /*****************************
      * Archetype Request Constants
      * String REQ_ARCHE_XXX
      *****************************/
     String REQ_ARCHE_TYPES = "technologies";
     String REQ_ARCHE_TYPE = "technology";
     String REQ_ARCHE_TECHID = "techId";
     String REQ_JAR_TYPE = "type";
     String REQ_PLUGIN_JAR = "pluginJar";
     String REQ_UPLOADED_JAR = "uploadedJar";
     String REQ_JAR_FILE_EXTENSION = ".jar";
     String REQ_ZIP_FILE_EXTENSION  = ".zip";
     String REQ_TAR_GZ_FILE_EXTENSION = ".gz";
     String REQ_IMAGE_JPG_EXTENSION = "jpg";
     String REQ_IMAGE_JPEG_EXTENSION = "jpeg";
     String REQ_IMAGE_PNG_EXTENSION = "png";
     String REQ_JAR_FILE = "jar";
     
     
     /*****************************
      * Archetype Constants
      *****************************/
     String FILE_FOR_APPTYPE = "appType";
 	String FILE_FOR_PLUGIN = "plugin";
     String X_FILE_NAME = "X-File-Name";
 
     
     /*****************************
      * Customer Request Constants
      * String REQ_CUST_XXX
      *****************************/ 
     String REQ_CUST_CUSTOMER = "customer";
     String REQ_CUST_CUSTOMERS = "customers";
     String REQ_CUST_CUSTOMER_ID = "customerId";
     
     /*****************************
      * Users Request Constants
      * String REQ_USER_XXX
      *****************************/ 
     String REQ_USER_LIST = "userList";
     String REQ_USER_SYNCLIST = "userSyncList";
     
     
     /*****************************
      * ConfigTemp Request Constants
      * String REQ_CONFIG_XXX
      *****************************/ 
     String REQ_CONFIG_TEMP = "configTemp";
     String REQ_CONFIG_TEMPLATES = "configTemplates";
     String REQ_CONFIG_ID = "configId";
     
     String REQ_CONFIG_KEY = "propTempKey";
     String REQ_CONFIG_NAME = "_propTempName";
 	String REQ_CONFIG_PSBL_VAL = "_psblVal";
 	String REQ_CONFIG_MULTIPLE = "_propMul";
 	String REQ_CONFIG_MANDATORY = "_propMand";
 	String REQ_CONFIG_HELP_TEXT = "_helpText";
 	String REQ_CONFIG_TYPE = "_type";
     
     /*****************************
      * Download Request Constants
      * String REQ_DOWNLOAD_XXX
      *****************************/ 
     String REQ_DOWNLOAD_INFO = "downloadInfo";
     String REQ_DOWNLOAD_ID = "downloadId";
     String REQ_DOWNLOAD_UPLOAD_FILE = "uploadFile";
     String REQ_DOWNLOAD_PLATFORMS = "platforms";
     
     
     /*****************************
      * PilotProject Request Constants
      * String REQ_PILOT_XXX
      *****************************/ 
     String REQ_PILOT_PROJECTS = "pilotProjects";
     String REQ_PILOT_PROINFO = "pilotProjectInfo";
     String REQ_PILOT_PROJ_ID = "projectId";
     
     
     /*****************************
      * Features Request Constants
      * String REQ_FEATURES_XXX
      *****************************/ 
     String REQ_FEATURES_MOD_GRP = "moduleGroup";
     String REQ_FEATURES_TYPE = "type";
     String REQ_FEATURES_TYPE_JS = "JAVASCRIPT";
     String REQ_FEATURES_TYPE_MODULE = "FEATURE";
     String REQ_FEATURES_TYPE_COMPONENT = "COMPONENT";
     String REQ_FEATURES_SELECTED_MODULEID = "selectedModuleId";
     
     
     /*****************************
      * Features Common Constants
      * String FEATURES_XXX
      *****************************/
     String FEATURES_CORE = "core";
     String FEATURES_SELECTED_TECHNOLOGY = "selectedTechnology";
     
     
     /*****************************
      * Features Session Constants
      * String SESSION_COMP_XXX
      *****************************/
     String SESSION_FEATURES_DEPENDENT_MOD_IDS = "dependentModuleIds";
     
     
     /*****************************
      * Component Session Constants
      * String SESSION_COMP_XXX
      *****************************/
     String SESSION_COMP_DEPENDENT_MOD_IDS = "compDependentModuleIds";
     
     /*****************************
      * Permissions Request Constants
      * String REQ_PERMISSIONS_XXX
      *****************************/ 
     String REQ_PERMISSIONS_LIST = "permissions";
     String REQ_PERMISSIONS_ID = "permissionId";
     
     
     /*****************************
      * Role Request Constants
      * String REQ_VIDEO_XXX
      *****************************/ 
     String REQ_VIDEO_FILE_TYPE = "type";
     String REQ_VIDEO_UPLOAD = "videoFile";
     String REQ_VIDEO_INFO = "videoInfo";
     
     String REQ_VIDEO_SAVE = "videoSave";
     String REQ_VIDEO_UPDATE = "videoUpdate";
     /*****************************
      * GlobalUrl Request Constants
      * String REQ_GLOBURL_XXX
      *****************************/ 
     String REQ_GLOBURL_URL = "globalUrl";
     String REQ_GLOBURL_ID = "globalurlId";
     
     
     /*****************************
      * Role Request Constants
      * String REQ_ROLE_XXX
      *****************************/ 
     String REQ_ROLE_ROLE = "role";
     String REQ_ROLE_LIST = "roleList";
     String REQ_ROLE_ID = "roleId";
     
     
     /*****************************
      * Error Report
      * String REQ_LOG_REPORT
      *****************************/ 
     String REQ_LOG_REPORT = "logReport";
     String LOG_ERROR = "logError";
     
     String EXCEPTION_APPTYPES_LIST = "Application types list";
     String EXCEPTION_APPTYPES_EDIT = "Application types edit";
     String EXCEPTION_APPTYPES_SAVE = "Application types save";
     String EXCEPTION_APPTYPES_UPDATE = "Application types update";
     String EXCEPTION_APPTYPES_DELETE = "Application types delete";
     
     String EXCEPTION_LOGIN = "Login Authenticate";
     String EXCEPTION_CUSTOMERS_DELETE = "Customers delete";
 	String EXCEPTION_CUSTOMERS_UPDATE = "Customers update";
 	String EXCEPTION_CUSTOMERS_SAVE = "Customers save";
 	String EXCEPTION_CUSTOMERS_ADD = "Customers add";
 	String EXCEPTION_CUSTOMERS_LIST = "Customers list";
 	String EXCEPTION_CUSTOMERS_VALIDATE = "Customers validateForm";
 	
 	String EXCEPTION_USERS_LIST = "User list";
     
 	String EXCEPTION_ROLE_DELETE = "Role delete";
 	String EXCEPTION_ROLE_UPDATE = "Role update";
 	String EXCEPTION_ROLE_SAVE = "Role save";
 	String EXCEPTION_ROLE_EDIT = "Role edit";
 	String EXCEPTION_ROLE_LIST = "Role list";
 	
 	String EXCEPTION_PERMISSION_LIST = "Permission list";
 	String EXCEPTION_PERMISSION_DELETE = "Permission delete";
 	
 	String EXCEPTION_VIDEO_DELETE = "Video delete";
 	String EXCEPTION_VIDEO_UPDATE = "Video update";
 	String EXCEPTION_VIDEO_SAVE = "Video save";
 	String EXCEPTION_VIDEO_EDIT = "Video edit";
 	String EXCEPTION_VIDEO_LIST = "Video list";
 	
 	String EXCEPTION_GLOBAL_URL_DELETE = "GlobalUrl delete";
 	String EXCEPTION_GLOBAL_URL_UPDATE = "GlobalUrl update";
 	String EXCEPTION_GLOBAL_URL_SAVE = "GlobalUrl save";
 	String EXCEPTION_GLOBAL_URL_EDIT = "GlobalUrl edit";
 	String EXCEPTION_GLOBAL_URL_LIST = "GlobalUrl list";
 		
 	String EXCEPTION_FEATURE_DELETE = "Feature delete";
 	String EXCEPTION_FEATURE_UPDATE = "Feature update";
 	String EXCEPTION_FEATURE_SAVE = "Feature save";
 	String EXCEPTION_FEATURE_EDIT = "Feature edit";
 	String EXCEPTION_FEATURE_LIST = "Feature list";
 	String EXCEPTION_FEATURE_ADD = "Feature add";
 	String EXCEPTION_FEATURE_VALIDATE = "Feature validate";
 	
 	String EXCEPTION_COMPONENT_DELETE = "Component delete";
 	String EXCEPTION_COMPONENT_UPDATE = "Component update";
 	String EXCEPTION_COMPONENT_SAVE = "Component save";
 	String EXCEPTION_COMPONENT_ADD = "Component add";
 	String EXCEPTION_COMPONENT_EDIT = "Component edit";
 	String EXCEPTION_COMPONENT_LIST = "Component list";
 	String EXCEPTION_COMPONENT_LIST_DEPENDENCY = "Component listConponentsDependency";
 	String EXCEPTION_COMPONENT_UPLOAD_FILE = "Component uploadFile";
 	
 	String EXCEPTION_ARCHETYPE_DELETE = "Archetype delete";
 	String EXCEPTION_ARCHETYPE_UPDATE = "Archetype update";
 	String EXCEPTION_ARCHETYPE_SAVE = "Archetype save";
 	String EXCEPTION_ARCHETYPE_EDIT = "Archetype edit";
 	String EXCEPTION_ARCHETYPE_ADD = "Archetype add";
 	String EXCEPTION_ARCHETYPE_LIST = "Archetype list";
 	
 	String EXCEPTION_CONFIG_TEMP_DELETE = "ConfigTemplate delete";
 	String EXCEPTION_CONFIG_TEMP_UPDATE = "ConfigTemplate update";
 	String EXCEPTION_CONFIG_TEMP_SAVE = "ConfigTemplate save";
 	String EXCEPTION_CONFIG_TEMP_EDIT = "ConfigTemplate edit";
 	String EXCEPTION_CONFIG_TEMP_ADD = "ConfigTemplate add";
 	String EXCEPTION_CONFIG_TEMP_LIST = "ConfigTemplate list";
 	
 	String EXCEPTION_PILOT_PROJECTS_DELETE = "PilotProjects delete";
 	String EXCEPTION_PILOT_PROJECTS_UPDATE = "PilotProjects update";
 	String EXCEPTION_PILOT_PROJECTS_SAVE = "PilotProjects save";
 	String EXCEPTION_PILOT_PROJECTS_EDIT = "PilotProjects edit";
 	String EXCEPTION_PILOT_PROJECTS_ADD = "PilotProjects add";
	String EXCEPTION_PILOT_PROJECTS_LIST = "Exception in PilotProjects List";
 	
 	String EXCEPTION_DOWNLOADS_DELETE = "Downloads delete";
 	String EXCEPTION_DOWNLOADS_UPDATE = "Downloads update";
 	String EXCEPTION_DOWNLOADS_SAVE = "Downloads save";
 	String EXCEPTION_DOWNLOADS_EDIT = "Downloads edit";
 	String EXCEPTION_DOWNLOADS_ADD = "Downloads add";
 	String EXCEPTION_DOWNLOADS_LIST = "Downloads list";
 	
 	/*****************************
      * I18N Keys Constants
      * String KEY_I18N_XXX_YYY
      *****************************/	
     String KEY_I18N_ERROR_LOGIN = "err.login.invalid.cred";
     String KEY_I18N_ERROR_LOGIN_ACCESS_DENIED = "err.login.access.denied";
     String KEY_I18N_LOGIN_INVALID_CRED = "err.login.invalid.cred";
     String KEY_I18N_LOGIN_EMPTY_CRED = "err.login.empty.cred";
     String KEY_I18N_LOGIN_ACCESS_DENIED	= "err.login.invalid.cred";
     String KEY_I18N_SESSION_EXPIRED = "err.login.session.expired";
     String KEY_I18N_SUCCESS_LOGOUT = "succ.logout";
 
     String KEY_I18N_ERR_NAME_EMPTY = "err.msg.name.empty";
     String KEY_I18N_ERR_GROUPID_EMPTY = "err.msg.groupid.empty";
     String KEY_I18N_ERR_ARTIFACTID_EMPTY = "err.msg.artifactid.empty";
     String KEY_I18N_ERR_NAME_ALREADY_EXIST = "err.msg.name.exists";
     String KEY_I18N_ERR_NAME_ALREADY_EXIST_TECH = "err.msg.name.tech.exists";
     String KEY_I18N_ERR_NAME_ALREADY_EXIST_APPTYPE = "err.msg.name.apptype.exists";
     String KEY_I18N_ERR_DESC_EMPTY = "err.msg.desc.empty";
     String KEY_I18N_ERR_VER_EMPTY = "err.msg.ver.empty";
     String KEY_I18N_ERR_TECHVER_EMPTY = "err.msg.techver.empty";
     String KEY_I18N_ERR_GID_EMPTY = "err.msg.gId.empty";
     String KEY_I18N_ERR_ARFID_EMPTY = "err.msg.aId.empty";
     String KEY_I18N_ERR_VER_ALREADY_EXISTS = "err.msg.ver.exists";
     String KEY_I18N_ERR_FILE_EMPTY	= "err.msg.file.empty";
     
     String KEY_I18N_ERR_APPTYPE_EMPTY = "err.msg.apptye.empty";
     String KEY_I18N_ERR_APPLIES_EMPTY = "err.msg.applies.empty";
     String KEY_I18N_ERR_APPLNJAR_EMPTY = "err.msg.applnjar.empty";
     String KEY_I18N_ERR_PLTPROJ_EMPTY = "err.msg.pltproj.empty";
     String KEY_I18N_ERR_PLUGINJAR_INVALID = "err.msg.plugin.invalid";
     
     String KEY_I18N_ERR_EMAIL_EMPTY = "err.msg.email.empty";
     String KEY_I18N_ERR_ADDRS_EMPTY	= "err.msg.addrs.empty";
     String KEY_I18N_ERR_ZIPCODE_EMPTY = "err.msg.zip.empty";
     String KEY_I18N_ERR_CONTNUM_EMPTY = "err.msg.contnum.empty";
     String KEY_I18N_ERR_FAXNUM_EMPTY = "err.msg.faxnum.empty";
     String KEY_I18N_ERR_COUN_EMPTY = "err.msg.country.empty";
     String KEY_I18N_ERR_LICEN_EMPTY = "err.msg.licence.empty";
     String KEY_I18N_ERR_URL_EMPTY = "err.msg.url.empty";
     String KEY_I18N_ERR_APPLNPLTF_EMPTY = "err.msg.applnpltf.empty";
     String KEY_I18N_ERR_GROUP_EMPTY = "err.msg.group.empty";
     String KEY_I18N_ERR_TECH_EMPTY = "err.msg.tech.empty";
     
     String KEY_I18N_ERR_VIDEO_EMPTY = "err.msg.vdeo.empty";
     String KEY_I18N_ERR_IMAGE_EMPTY = "err.msg.img.empty";
     String KEY_I18N_VIDEO_UPDATING = "lbl.prog.vdeo.update";
     String KEY_I18N_VIDEO_CREATING = "lbl.prog.vdeo.save";
     
     String KEY_I18N_BUTTON_SAVE = "lbl.hdr.comp.save";
     String KEY_I18N_BUTTON_UPDATE = "lbl.hdr.comp.update";
     String KEY_I18N_VIDEO_TITLE_ADD = "lbl.hdr.adm.vdeoadd";
     String KEY_I18N_VIDEO_TITLE_EDIT = "lbl.hdr.adm.vdoedit";
     
     
     /*****************************
      * I18N Keys Constants
      * String KEY_I18N_FEATUR_XXX
      *****************************/	
     String FEATURE_ADDED = "succ.feature.add";
     String FEATURE_UPDATED = "succ.feature.update";
     String FEATURE_DELETED = "succ.feature.delete";
     
     String COMPONENT_ADDED = "succ.component.add";
     String COMPONENT_NOT_ADDED = "fail.component.add";
     String COMPONENT_DELETED = "succ.component.delete";
     String COMPONENT_NOT_DELETED = "fail.component.delete" ;
     
     String APPTYPES_ADDED = "succ.appType.add";
     String APPTYPES_UPDATED = "succ.appType.update";
     String APPTYPES_DELETED = "succ.appType.delete";
     
     String CONFIGTEMPLATE_ADDED = "succ.configtemplate.add";
     String CONFIGTEMPLATE_UPDATED = "succ.configtemplate.update";
     String CONFIGTEMPLATE_DELETED = "succ.configtemplate.delete";
     
     String ARCHETYPE_ADDED = "succ.archetype.add";
     String ARCHETYPE_NOT_ADDED = "fail.archetype.add";
     String ARCHETYPE_UPDATED = "succ.archetype.update";
     String ARCHETYPE_NOT_UPDATED = "fail.archetype.update";
     String ARCHETYPE_DELETED = "succ.archetype.delete";
     String ARCHETYPE_NOT_DELETED = "fail.archetype.delete" ;
     
     String PLTPROJ_ADDED = "succ.pltproj.add";
     String PLTPROJ_UPDATED = "succ.pltproj.updated";
     String PLTPROJ_DELETED = "succ.pltproj.delete";
     
     String CUSTOMER_ADDED = "succ.customer.add";
     String CUSTOMER_UPDATED = "succ.customer.update";
     String CUSTOMER_DELETED = "succ.customer.delete";
     String CUSTOMER_NOT_ADDED = "fail.customer.add";
     String CUSTOMER_NOT_UPDATED = "fail.customer.update";
     String CUSTOMER_NOT_DELETED = "fail.customer.delete";
     
     String PERMISSION_DELETED = "succ.permission.delete";
     String PERMISSION_NOT_DELETED = "fail.permission.delete";
     
     String ROLE_ADDED = "succ.role.add";
     String ROLE_NOT_ADDED = "fail.role.add";
     String ROLE_DELETED = "succ.role.delete";
     String ROLE_NOT_DELETED = "fail.role.delete" ;
     
     String VIDEO_ADDED = "succ.vdeo.add";
     String VIDEO_DELETED = "succ.vdeo.delete";
     
     String URL_ADDED = "succ.url.add";
     String URL_NOT_ADDED = "fail.url.add";
     String URL_DELETED = "succ.globurl.delete";
     String URL_NOT_DELETED = "fail.globurl.delete";
     
     
     String DOWNLOAD_ADDED = "succ.download.add";
     String DOWNLOAD_UPDATED = "succ.download.update";
     String DOWNLOAD_DELETED = "succ.download.delete";
     
     //Email validation
     String INVALID_EMAIL = "err.msg.invalid.email";
 }
