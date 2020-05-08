 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.common.client.servlet;
 
 /**
  * Some constants.
  *
  * @author Michael Schneider
  */
 public class Constants {
 
     /*
      * REST specific constants
      */
     public static final String PROTOCOL = "http";
 
     /*
      * Created user (which are not from ldap or shibboleth) have an default
      * password.
      */
     public static String DEFAULT_USER_PASSWORD = "PubManR2";
 
     public static final String SCHEMA_LOCATION_BASE = "http://www.escidoc.org/schemas";
 
     public static final String ESCIDOC_BASE_URI = "/";
 
     public static final String INSTITUTIONAL_REPOSITORY_URI = "ir";
 
     public static final String AA_URI = "aa";
 
     public static final String OAI_URI = "oai";
 
     public static final String ADMIN_URI = "adm";
 
     public static final String STATISTIC_URI = "statistic";
 
     public static final String FEDORAGSEARCH_URI = "fedoragsearch/rest";
 
     // *****************************************************************
     // * Base Resource Uris
     // *
 
     // public static final String ADMIN_DESCRIPTOR_BASE_URI = ESCIDOC_BASE_URI
     // + INSTITUTIONAL_REPOSITORY_URI + "/admin-descriptor";
     //
     // public static final String ADMIN_DESCRIPTORS_BASE_URI = ESCIDOC_BASE_URI
     // + INSTITUTIONAL_REPOSITORY_URI + "/admin-descriptors";
 
     public static final String FILTER = "/filter";
 
     public static final String REFS = "/refs";
 
     public static final String QUERY = "/query";
 
     public static final String SPO = "/spo";
 
     public static final String AA_BASE_URI = ESCIDOC_BASE_URI + AA_URI;
 
     public static final String LOGIN_DEFAULT_PROVIDER = "/database";
 
     public static final String LOGIN_URI = AA_BASE_URI + "/login" + LOGIN_DEFAULT_PROVIDER;
 
     public static final String CONTEXT_BASE_URI = ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/context";
 
     public static final String CONTEXTS_BASE_URI = ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/contexts";
 
     public static final String CONTAINER_BASE_URI = ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/container";
 
     public static final String XML_SCHEMA_BASE_URI = ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/xml-schema";
 
     public static final String CONTAINERS_BASE_URI = ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/containers";
 
     public static final String JHOVE_BASE_URI = ESCIDOC_BASE_URI + "tme/jhove";
 
     public static final String ITEM_BASE_URI = ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/item";
 
     public static final String INGEST_BASE_URI = ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/ingest";
 
     public static final String TOC_BASE_URI = ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/toc";
 
     public static final String CONTENT_RELATION_BASE_URI =
         ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/content-relation";
 
     public static final String CONTENT_RELATIONS_BASE_URI =
         ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/content-relations";
 
     public static final String ITEMS_BASE_URI = ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/items";
 
     public static final String SET_DEFINITIONS_BASE_URI = ESCIDOC_BASE_URI + OAI_URI + "/set-definitions";
 
     public static final String SEMANTIC_STORE_BASE_URI =
         ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/semantic-store";
 
     public static final String SET_DEFINITION_BASE_URI = ESCIDOC_BASE_URI + OAI_URI + "/set-definition";
 
     public static final String REPOSITORY_INFO_BASE_URI = ESCIDOC_BASE_URI + ADMIN_URI + "/admin/get-repository-info";
 
     public static final String INDEX_CONFIGURATION_BASE_URI =
         ESCIDOC_BASE_URI + ADMIN_URI + "/admin/get-index-configuration";
 
     public static final String DELETE_OBJECTS_BASE_URI = ESCIDOC_BASE_URI + ADMIN_URI + "/admin/deleteobjects";
 
     public static final String LOAD_EXAMPLES_BASE_URI = ESCIDOC_BASE_URI + ADMIN_URI + "/admin/load-examples";
 
     public static final String REINDEX_BASE_URI = ESCIDOC_BASE_URI + ADMIN_URI + "/admin/reindex";
 
     public static final String LICENSE_TYPE_BASE_URI =
         ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/license-type";
 
     public static final String LICENSE_TYPES_BASE_URI =
         ESCIDOC_BASE_URI + INSTITUTIONAL_REPOSITORY_URI + "/license-types";
 
     public static final String MD_SCHEMA_BASE_URI = ESCIDOC_BASE_URI + "mm/mdschema";
 
     public static final String ORGANIZATIONAL_UNIT_BASE_URI = ESCIDOC_BASE_URI + "oum/organizational-unit";
 
     public static final String ORGANIZATIONAL_UNITS_BASE_URI = ESCIDOC_BASE_URI + "oum/organizational-units";
 
     public static final String CONTENT_MODEL_BASE_URI = ESCIDOC_BASE_URI + "cmm/content-model";
 
     public static final String CONTENT_MODELS_BASE_URI = ESCIDOC_BASE_URI + "cmm/content-models";
 
     public static final String PDP_BASE_URI = AA_BASE_URI + "/pdp";
 
     public static final String ROLE_BASE_URI = AA_BASE_URI + "/role";
 
     public static final String ROLES_BASE_URI = AA_BASE_URI + "/roles";
 
     public static final String STAGING_FILE_BASE_URI = ESCIDOC_BASE_URI + "st/staging-file";
 
     public static final String USER_ACCOUNT_BASE_URI = AA_BASE_URI + "/user-account";
 
     public static final String USER_ACCOUNTS_BASE_URI = AA_BASE_URI + "/user-accounts";
 
     public static final String GRANTS_BASE_URI = AA_BASE_URI + "/grants";
 
     public static final String USER_GROUP_BASE_URI = AA_BASE_URI + "/user-group";
 
     public static final String USER_GROUPS_BASE_URI = AA_BASE_URI + "/user-groups";
 
     public static final String USER_MANAGEMENT_WRAPPER_BASE_URI = ESCIDOC_BASE_URI + "aa";
 
     public static final String SEARCH_BASE_URI = ESCIDOC_BASE_URI + "srw/search";
 
     public static final String FEDORAGSEARCH_BASE_URI = ESCIDOC_BASE_URI + FEDORAGSEARCH_URI;
 
     public static final String STATISTIC_DATA_BASE_URI = ESCIDOC_BASE_URI + STATISTIC_URI + "/statistic-data";
 
     public static final String STATISTIC_AGGREGATION_DEFINITION_BASE_URI =
         ESCIDOC_BASE_URI + STATISTIC_URI + "/aggregation-definition";
 
     public static final String STATISTIC_AGGREGATION_DEFINITIONS_BASE_URI =
         ESCIDOC_BASE_URI + STATISTIC_URI + "/aggregation-definitions";
 
     public static final String STATISTIC_REPORT_DEFINITION_BASE_URI =
         ESCIDOC_BASE_URI + STATISTIC_URI + "/report-definition";
 
     public static final String STATISTIC_REPORT_DEFINITIONS_BASE_URI =
         ESCIDOC_BASE_URI + STATISTIC_URI + "/report-definitions";
 
     public static final String STATISTIC_REPORT_BASE_URI = ESCIDOC_BASE_URI + STATISTIC_URI + "/report";
 
     public static final String STATISTIC_SCOPE_BASE_URI = ESCIDOC_BASE_URI + STATISTIC_URI + "/scope";
 
     public static final String STATISTIC_SCOPES_BASE_URI = ESCIDOC_BASE_URI + STATISTIC_URI + "/scopes";
 
     public static final String STATISTIC_PREPROCESSING_BASE_URI = ESCIDOC_BASE_URI + STATISTIC_URI + "/preprocessing";
 
     public static final String UNSECURED_ACTIONS_BASE_URI = ESCIDOC_BASE_URI + "aa/unsecured-actions";
 
     public static final String TEST_DATA_BASE_URI = ESCIDOC_BASE_URI + "testdata/testDocuments";
 
     // *****************************************************************
     // * Sub Resource names
     // *
 
     public static final String SUB_ADMINDESCRIPTORS = "admin-descriptors";
 
     public static final String SUB_ADMINDESCRIPTOR = SUB_ADMINDESCRIPTORS + "/" + "admin-descriptor";
 
     public static final String SUB_COMPONENTS = "components";
 
     public static final String SUB_COMPONENT = SUB_COMPONENTS + "/" + "component";
 
     public static final String SUB_CONTAINERS = "containers";
 
     public static final String SUB_CONTENT = "content";
 
     // public static final String SUB_GENERIC_PROPERTIES = "generic-properties";
 
     public static final String SUB_GENRE = "genre";
 
     public static final String SUB_ITEMS = Constants.SUB_RESOURCES + "/" + "items";
 
     public static final String SUB_CONTEXT_MEMBERS = Constants.SUB_RESOURCES + "/" + "members/filter";
 
     public static final String SUB_CONTEXT_MEMBER_REFS = Constants.SUB_RESOURCES + "/" + "members/filter/refs";
 
     public static final String SUB_CONTEXT_OPEN = "open";
 
     public static final String SUB_MD_RECORDS = "md-records";
 
     public static final String SUB_CONTENT_STREAMS = "content-streams";
 
     public static final String SUB_CONTENT_STREAM = SUB_CONTENT_STREAMS + "/content-stream";
 
     public static final String SUB_MD_RECORD = SUB_MD_RECORDS + "/" + "md-record";
 
     public static final String SUB_PROPERTIES = "properties";
 
     public static final String SUB_PARENTS = "parents";
 
     public static final String SUB_RESOURCES = "resources";
 
     public static final String SUB_RESOURCE = "resource";
 
     public static final String SUB_STRUCT_MAP = "struct-map";
 
     public static final String SUB_CONTAINER_MEMBERS = "members";
 
     public static final String SUB_CONTAINER_TOCS = "tocs";
 
     public static final String SUB_METS = SUB_RESOURCES + "/" + "mets";
 
     public static final String SUB_TOCS = "tocs";
 
     public static final String SUB_TOC = "toc";
 
     public static final String SUB_TOC_VIEW = SUB_RESOURCES + "/" + "toc-view";
 
     public static final String SUB_CHILD_OBJECTS = SUB_RESOURCES + "/" + "child-objects";
 
     public static final String SUB_PARENT_OBJECTS = SUB_RESOURCES + "/" + "parent-objects";
 
     public static final String SUB_VERSION_HISTORY = SUB_RESOURCES + "/" + "version-history";
 
     public static final String SUB_SUBMIT = "submit";
 
     public static final String SUB_RELATIONS = "relations";
 
     public static final String SUB_CONTENT_RELATIONS = "content-relations";
 
     public static final String SUB_ADD_CONTENT_RELATIONS = SUB_CONTENT_RELATIONS + "/add";
 
     public static final String SUB_REMOVE_CONTENT_RELATIONS = SUB_CONTENT_RELATIONS + "/remove";
 
     public static final String SUB_CLOSE = "close";
 
     public static final String SUB_OPEN = "open";
 
     public static final String SUB_RELEASE = "release";
 
     public static final String SUB_REVISE = "revise";
 
     public static final String SUB_ASSIGN_VERSION_PID = "assign-version-pid";
 
     public static final String SUB_ASSIGN_OBJECT_PID = "assign-object-pid";
 
     public static final String SUB_ASSIGN_CONTENT_PID = "assign-content-pid";
 
    public static final String SUB_RETRIEVE_REGISTERED_PREDICATES = "retrieve-registered-predicates";

     // public static final String SUB_ASSIGN_CONTENT_PID_PATH =
     // SUB_COMPONENTS + "/" + "component";
 
     public static final String SUB_WITHDRAW = "withdraw";
 
     public static final String SUB_LOCK = "lock";
 
     public static final String SUB_UNLOCK = "unlock";
 
     public static final String SUB_ADD_TOCS = "tocs/add";
 
     public static final String SUB_ADD_MEMBERS = "members/add";
 
     public static final String SUB_REMOVE_MEMBERS = "members/remove";
 
     public static final String SUB_ADD_SELECTORS = "selectors/add";
 
     public static final String SUB_REMOVE_SELECTORS = "selectors/remove";
 
     public static final String SUB_MOVE_TO_CONTEXT = "move-to-context";
 
     public static final String SUB_CREATE_ITEM = "create-item";
 
     public static final String SUB_CREATE_CONTAINER = "create-container";
 
     public static final String SUB_PATH_LIST = SUB_RESOURCES + "/" + "path-list";
 
     public static final String SUB_SUCCESSORS = SUB_RESOURCES + "/" + "successors";
 
     // *****************************************************************
     // * Sub Resource ingest
     // *
     public static final String SUB_INGEST_CONFIGURATION = "configure";
 
     public static final String SUB_INGEST_NEXT_OBJIDS = "nextobjid";
 
     public static final String SUB_INGEST_LOCKED_LIST = "locked";
 
     public static final String SUB_INGEST_ITEM = "item";
 
     public static final String SUB_INGEST_CONTAINER = "container";
 
     public static final String SUB_INGEST_COMMIT = "commit";
 
     // *****************************************************************
     // * Sub Resource user-account and user-group
     // *
     public static final String SUB_GRANT = SUB_RESOURCES + "/grants/grant";
 
     // *****************************************************************
     // * Possible methods invoked by call of HTTP Post method and the body param
     // keys.
     // *
     public static final String POST_METHOD_PUBLISH = "publish";
 
     public static final String POST_METHOD_SUBMIT = "submit";
 
     public static final String POST_METHOD_WITHDRAW = "withdraw";
 
     public static final String POST_METHOD_LOCK = "lock";
 
     public static final String POST_METHOD_UNLOCK = "unlock";
 
     public static final String POST_BODY_CONSTRAINT = "method";
 
     public static final String POST_BODY_LAST_MODIFICATION_DATE = "last-modification-date";
 
     public static final String POST_BODY_LIST_DELIMITER = ",";
 
     // *****************************************************************
     // * Some common
     // *
     public static final String HTTP_METHOD_DELETE = "DELETE";
 
     public static final String HTTP_METHOD_GET = "GET";
 
     public static final String HTTP_METHOD_POST = "POST";
 
     public static final String HTTP_METHOD_PUT = "PUT";
 
     // *****************************************************************
     // * Login specific constants
     // *
     public static final String UM_LOGIN_BASE_URI = ESCIDOC_BASE_URI + "login";
 
     public static final String UM_LOGIN_COOKIE = "escidocCookie";
 
     public static final String PARAM_UM_AUTHORIZATION = "Escidoc-Authorization";
 
     public static final String PARAM_UM_LOGIN_NAME = "j_username";
 
     public static final String PARAM_UM_LOGIN_PASSWORD = "j_password";
 
     public static final String PARAM_UM_REDIRECT_URL = "redirectUrl";
 
     public static final String CDATA_START = "<![CDATA[";
 
     public static final String CDATA_END = "]]>";
 
     // *****************************************************************
     // * Fedora specific constants
     // *
     public static final String FOXML_FORMAT = "info:fedora/fedora-system:FOXML-1.1";
 
 }
