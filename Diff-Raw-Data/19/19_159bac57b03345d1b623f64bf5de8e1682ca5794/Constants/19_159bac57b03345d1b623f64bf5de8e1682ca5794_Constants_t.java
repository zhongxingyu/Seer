 /*
  * Copyright 2004-2009 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dolteng.eclipse;
 
 import org.eclipse.core.runtime.QualifiedName;
 
 /**
  * @author taichi
  * 
  */
 public final class Constants {
 
     public static final String ID_PLUGIN = "org.seasar.dolteng.eclipse";
 
     public static final String ID_NATURE = ID_PLUGIN + ".nature";
 
     public static final String ID_NATURE_FLEX = ID_PLUGIN + ".nature.flex";
 
     public static final String ID_BUILDER = ID_PLUGIN + ".builder";
 
     public static final String ID_DATABASE_VIEW = ID_PLUGIN + ".databaseView";
 
     public static final String ID_PAGE_MAPPER = ID_PLUGIN + ".pageMapper";
 
     public static final String ID_HTML_MAPPER = ID_PLUGIN + ".htmlMapper";
 
     public static final String ID_DI_MAPPER = ID_PLUGIN + ".diMapper";
 
     public static final String ID_SQL_MAPPER = ID_PLUGIN + ".sqlMapper";
 
     public static final String ID_SQL_ERROR = ID_PLUGIN + ".sqlErrorMapper";
 
     public static final String ID_KUINA_ERROR = ID_PLUGIN + ".kuinaError";
 
     /* ------------------------------------------------------------------ */
 
     public static final String EXTENSION_POINT_CHURA_CONTENT_FINDER = "churaContentFinder";
 
     /* ------------------------------------------------------------------ */
 
     public static final String ID_TOMCAT_PLUGIN = "com.sysdeo.eclipse.tomcat";
 
     public static final String ID_TOMCAT_NATURE = ID_TOMCAT_PLUGIN
             + ".tomcatnature";
 
     public static final String ID_WTP_VALIDATION_PLUGIN = "org.eclipse.wst.validation";
 
     public static final String ID_WTP_VALIDATION_BUILDER = ID_WTP_VALIDATION_PLUGIN
             + ".validationbuilder";
 
     public static final String ID_WTP_FACET_PLUGIN = "org.eclipse.wst.common.project.facet.core";
 
     public static final String ID_WTP_FACET_NATURE = ID_WTP_FACET_PLUGIN
             + ".nature";
 
     public static final String ID_WTP_FACET_BUILDER = ID_WTP_FACET_PLUGIN
             + ".builder";
 
     public static final String ID_WTP_MODULE_PLUGIN = "org.eclipse.wst.common.modulecore";
 
     public static final String ID_WTP_MODULE_NATURE = ID_WTP_MODULE_PLUGIN
             + ".ModuleCoreNature";
 
     public static final String ID_JEM_PLUGIN = "org.eclipse.jem.workbench";
 
     public static final String ID_JEM_NATURE = ID_JEM_PLUGIN + ".JavaEMFNature";
 
     public static final String ID_DIIGU_PLUGIN = "org.seasar.diigu.eclipse";
 
     public static final String ID_DIIGU_NATURE = ID_DIIGU_PLUGIN
             + ".diiguNature";
 
     public static final String ID_DB_LAUNCHER_PLUGIN = "org.seasar.dblauncher";
 
     public static final String ID_DB_LAUNCHER_NATURE = ID_DB_LAUNCHER_PLUGIN
             + ".nature";
 
     public static final String ID_DB_LAUNCHER_START_SERVER_CMD = "org.seasar.dblauncher.command.start";
 
     public static final String ID_DB_LAUNCHER_EXECUTE_QUERY_CMD = "org.seasar.dblauncher.command.stop";
 
     public static final String ID_FLEX_BUILDER_PLUGIN = "com.adobe.flexbuilder.project";
 
     public static final String ID_FLEX_BUILDER_FLEXNATURE = ID_FLEX_BUILDER_PLUGIN
             + ".flexnature";
 
     public static final String ID_FLEX_BUILDER_ACTIONSCRIPTNATURE = ID_FLEX_BUILDER_PLUGIN
             + ".actionscriptnature";
 
     /* ------------------------------------------------------------------ */
 
     public static final String PREF_WEBCONTENTS_ROOT = "WebContentsRoot";
 
     public static final String PREF_SERVLET_PATH = "ServletPath";
 
     public static final String PREF_WEB_SERVER = "WebServer";
 
     public static final String PREF_NECESSARYDICONS = "NecessaryDicons";
 
     public static final String PREF_VIEW_TYPE = "ViewType";
 
     public static final String PREF_DAO_TYPE = "DaoType";
 
     public static final String PREF_DEFAULT_ROOT_PACKAGE = "DefaultRootPackage";
 
     public static final String PREF_USE_PAGE_MARKER = "UsePageMarker";
 
     public static final String PREF_USE_DI_MARKER = "UseDIMarker";
 
     public static final String PREF_USE_SQL_MARKER = "UseSqlMarker";
 
     public static final String PREF_ORM_XML_OUTPUT_PATH = "OrmXmlOutputPath";
 
     public static final String PREF_DEFAULT_SRC_PATH = "DefaultSrcPath";
 
     public static final String PREF_DEFAULT_RESOURCE_PATH = "DefaultResourcePath";
 
     public static final String PREF_FLEX_SRC_PATH = "flexSrcPath";
 
     public static final String PREF_IS_HELP_REMOTE = "isHelpRemote";
 
     public static final String PREF_IS_USE_PUBLIC_FIELD = "isUsePublicField";
 
     public static final QualifiedName PROP_USE_DI_MARKER = new QualifiedName(
             ID_PLUGIN, PREF_USE_DI_MARKER);
 
     public static final QualifiedName PROP_FLEX_PAGE_DTO_PATH = new QualifiedName(
             ID_PLUGIN, "flexPageDto");
 
     public static final QualifiedName PROP_VIEW_TYPE = new QualifiedName(
             ID_PLUGIN, PREF_VIEW_TYPE);
 
     public static final QualifiedName PROP_DAO_TYPE = new QualifiedName(
             ID_PLUGIN, PREF_DAO_TYPE);
 
     /* ------------------------------------------------------------------ */
 
     public static final String PREF_CONNECTION_NAME = "ConnectionName";
 
     public static final String PREF_DRIVER_PATH = "DriverPath";
 
     public static final String PREF_DRIVER_CLASS = "DriverClass";
 
     public static final String PREF_CONNECTION_URL = "ConnectionUrl";
 
     public static final String PREF_USER = "User";
 
     public static final String PREF_PASS = "Pass";
 
     public static final String PREF_CHARSET = "Charset";
 
     /* ------------------------------------------------------------------ */
 
     public static final String PREF_DOWNLOAD_ONLINE = "downloadOnline";
 
     public static final String PREF_MAVEN_REPOS_PATH = "mavenReposPath";
 
     public static final String PREF_DEFAULT_MAVEN_REPOS_PATH = "";
 
     /* ------------------------------------------------------------------ */
 
     public static final String MARKER_ATTR_MAPPING_TYPE_NAME = ID_PAGE_MAPPER
             + ".mappingType";
 
     public static final String MARKER_ATTR_MAPPING_FIELD_NAME = ID_PAGE_MAPPER
             + ".mappingField";
 
     public static final String MARKER_ATTR_MAPPING_HTML_PATH = ID_HTML_MAPPER
             + ".htmlpath";
 
     public static final String MARKER_ATTR_MAPPING_HTML_ID = ID_HTML_MAPPER
             + ".id";
 
     public static final String MARKER_ATTR_MAPPING_ELEMENT = ID_HTML_MAPPER
             + ".mappingElem";
 
     public static final String MARKER_ATTR_ERROR_TYPE_KUINA = ID_KUINA_ERROR
             + ".errorType";
 
     public static final String MARKER_ATTR_METHOD_NAME = ID_KUINA_ERROR
             + ".methodName";
 
     public static final String MARKER_ATTR_PARAMETER_NAME = ID_KUINA_ERROR
             + ".paramName";
 
     public static final String MARKER_ATTR_MAPPING_SQL_PATH = ID_SQL_MAPPER
             + ".htmlpath";
 
     /* ------------------------------------------------------------------ */
 
     public static final String VIEW_TYPE_TEEDA = "Teeda";
 
     public static final String VIEW_TYPE_FLEX2 = "Flex2";
     
     public static final String VIEW_TYPE_NONE = "None";
     
     public static final String VIEW_TYPE_SASTRUTS = "SA-Struts";
 
     public static final String VIEW_TYPE_SASTRUTSMAYAA = "SA-Struts+Mayaa";
 
     public static final String DAO_TYPE_KUINADAO = "Kuina-Dao";
 
     public static final String DAO_TYPE_S2DAO = "S2Dao";
 
     public static final String DAO_TYPE_S2JDBC = "S2JDBC";
 
     public static final String ERROR_TYPE_KUINA_NAME = "name";
 
     public static final String ERROR_TYPE_KUINA_TYPE = "type";
 
     /* ------------------------------------------------------------------ */
     public static final String CTX_PROJECT_NAME = "projectName";
 
     public static final String CTX_PACKAGE_NAME = "packageName";
 
     public static final String CTX_PACKAGE_PATH = "packagePath";
 
     public static final String CTX_JRE_CONTAINER = "jreContainer";
 
     public static final String CTX_JAVA_VERSION_NUMBER = "javaVersion";
 
     public static final String CTX_JAVA_VERSION_NUMBER2 = "javaVersion2";
 
     public static final String CTX_JAVA_VM_NAME = "javaVmName";
 
     public static final String CTX_APP_TYPE_PACKAGING = "appTypePackaging";
     
     public static final String CTX_LIB_PATH = "libPath";
 
     public static final String CTX_LIB_SRC_PATH = "libSrcPath";
 
     public static final String CTX_TEST_LIB_PATH = "testLibPath";
 
     public static final String CTX_TEST_LIB_SRC_PATH = "testLibSrcPath";
 
     public static final String CTX_MAIN_JAVA_PATH = "mainJavaPath";
 
     public static final String CTX_MAIN_RESOURCE_PATH = "mainResourcePath";
 
     public static final String CTX_MAIN_OUT_PATH = "mainOutPath";
 
     public static final String CTX_WEBAPP_ROOT = "webAppRoot";
 
     public static final String CTX_TEST_JAVA_PATH = "testJavaPath";
 
     public static final String CTX_TEST_RESOURCE_PATH = "testResourcePath";
 
     public static final String CTX_TEST_OUT_PATH = "testOutPath";
 }
