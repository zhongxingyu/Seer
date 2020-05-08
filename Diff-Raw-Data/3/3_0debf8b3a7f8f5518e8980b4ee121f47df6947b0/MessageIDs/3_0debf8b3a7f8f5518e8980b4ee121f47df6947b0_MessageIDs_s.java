 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.tools.messagehandling;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jubula.tools.i18n.I18n;
 
 
 /**
  * @author BREDEX GmbH
  * @created 18.08.2005
  */
 public class MessageIDs {
     // ---------------------------------------------------------------------
     // ---------- Error IDs ---------------------------------------------
     // ---------------------------------------------------------------------
     // -- IO ERROR IDs -----------------------------------------------------
     /** if a file could not load / read */
     public static final Integer E_FILE_IO = new Integer(1000);
     /** if a file could not found */
     public static final Integer E_FILE_NOT_FOUND = new Integer(1001);
     /** if the default properties file could not found */
     public static final Integer E_PROPERTIES_FILE_NOT_FOUND = new Integer(1002);
     /** if the given workspace is invalid (no write access to given directory) */
     public static final Integer E_INVALID_WORKSPACE = new Integer(1003);
     /** file access is impossible due to configuration problems */
     public static final Integer E_CONFIG_ERROR = new Integer(1004);
     // -- DATABASE ERROR IDs -----------------------------------------------
     /** if no or wrong username and password */
     public static final Integer E_NO_DB_CONNECTION = new Integer(2000);
     /** if persistence load failed */
     public static final Integer E_PERSISTENCE_LOAD_FAILED = new Integer(2001);
     /** if persistence configuration problem */
     public static final Integer E_PERSISTENCE_CONFIG_PROBLEM = 
         new Integer(2002);
     /** if persistence can't close session */
     public static final Integer E_PERSISTENCE_CANT_CLOSE = new Integer(2003);
     /** if persistence can't setup */
     public static final Integer E_PERSISTENCE_CANT_SETUP = new Integer(2004);
     /** if persistence can't open session */
     public static final Integer E_PERSISTENCE_CANT_OPEN = new Integer(2005);
     /** if persistence can't read the project from database */
     public static final Integer E_CANT_READ_PROJECT = new Integer(2006);
     /** if getting database session failed */
     public static final Integer E_NO_DB_SESSION = new Integer(2007);
     /** if the transaction start failed */
     public static final Integer E_START_TRANSACTION = new Integer(2008);
     /** if database rollback doesn't work */
     public static final Integer E_ROLLBACK = new Integer(2009);
     /** if project can't attached */
     public static final Integer E_ATTACH_PROJECT = new Integer(2010);
     /** if project can't deleted */
     public static final Integer E_DELETE_PROJECT = new Integer(2011);
     /** if deleted project can't committed */
     public static final Integer E_COMMIT_PROJECT = new Integer(2012);
     /** if project can't loaded */
     public static final Integer E_LOAD_PROJECT = new Integer(2013);
     /** if project can't readed */
     public static final Integer E_READ_PROJECT = new Integer(2014);
     /** if a general O/R mapping error occurred */
     public static final Integer E_MAPPING_GENERAL = new Integer(2015);
     /** if a general database error occurred */
     public static final Integer E_DATABASE_GENERAL = new Integer(2016);
     /** if project is already deleted */
     public static final Integer E_ALREADY_DELETED_PROJECT = new Integer(2017);
     /** if database read exception */
     public static final Integer E_DB_READ = new Integer(2018);
     /** if an object was modified in database */
     public static final Integer E_MODIFIED_OBJECT = new Integer(2019);
     /** if save to db does not work */
     public static final Integer E_SAVE_TO_DB_FAILED = new Integer(2020);
     /** if refresh of master session failed */
     public static final Integer E_MASTER_REFRESH = new Integer(2021);
     /** if an object is already in use */
     public static final Integer E_OBJECT_IN_USE = new Integer(2022);
     /** if stale an object */
     public static final Integer E_STALE_OBJECT = new Integer(2023);
     /** if commit of transaction failed */
     public static final Integer E_COMMIT_FAILED = new Integer(2024);
     /** if refresh of an object failed */
     public static final Integer E_REFRESH_FAILED = new Integer(2025);
     /** if refresh of an object is necessary */
     public static final Integer E_REFRESH_REQUIRED = new Integer(2026);
     /** in case of save errors in database */
     public static final Integer E_DB_SAVE = new Integer(2027);
     /** if the database is already in use by another process */
     public static final Integer E_DB_IN_USE = new Integer(2028);
     /** attempt to start a transaction while a transaction is already started */
     public static final Integer E_TRANS_STARTED = new Integer(2029);
     /** refresh of editor failed */
     public static final Integer E_EDITOR_REFRESH = new Integer(2030);
     /** version of db doesn't fit to version of GUIdancer client */
     public static final Integer E_INVALID_DB_VERSION = new Integer(2031);
     /** missing or ambiguous version entry in db */
     public static final Integer E_NOT_CHECKABLE_DB_VERSION = new Integer(2032);
     /** any problem with db scheme */
     public static final Integer E_NO_DB_SCHEME = new Integer(2033);
     /** an editor will be closed */
     public static final Integer E_EDITOR_CLOSE = new Integer(2035);
     /** an editor will be closed */
     public static final Integer E_DELETED_TC = new Integer(2036);
     /** if import of project (xml) failed for any reason */
     public static final Integer E_IMPORT_PROJECT_XML_FAILED = new Integer(2037);
     /** if parameter of a test case cannot be changed */
     public static final Integer E_CANNOT_CHANGE_PARAMETER = new Integer(2038);
      /** if save as... of project (xml) failed for any reason */
     public static final Integer E_SAVE_AS_PROJECT_FAILED = new Integer(2039);
     /** if import of project (xml) failed because of an xml configuration conflict */
     public static final Integer E_IMPORT_PROJECT_CONFIG_CONFLICT = 
         new Integer(2040);
     /** if project can't be loaded because of an xml configuration conflict*/
     public static final Integer E_LOAD_PROJECT_CONFIG_CONFLICT = 
         new Integer(2041);
     /** if creation of a new version of a project failed */
     public static final Integer E_CREATE_NEW_VERSION_FAILED = new Integer(2042);
     /** if creation of a new project failed */
     public static final Integer E_CREATE_NEW_PROJECT_FAILED = new Integer(2043);
     /** revert of editor changes failed */
     public static final Integer E_REVERT_EDITOR_CHANGES_FAILED = 
         new Integer(2044);
     /** adding attachments to support request mail failed */
     public static final Integer E_ADD_ATTACHMENTS_TO_MAIL_FAILED = 
         new Integer(2045);
     /** opening system mail client failed */
     public static final Integer E_OPEN_MAIL_CLIENT_FAILED = new Integer(2046);
     /** the version number of a project could not be parsed */
     public static final Integer E_INVALID_PROJECT_VERSION = new Integer(2047);
     /** if schema load failed */
     public static final Integer E_SCHEMA_LOAD_FAILED = new Integer(2058);
     /** error in schema.properties */
     public static final Integer E_ERROR_IN_SCHEMA_CONFIG = 
         new Integer(2059);
     /** error in databases.properties */
     public static final Integer E_ERROR_IN_DB_CONFIG = new Integer(2060);
     /** if testresult can't deleted */
     public static final Integer E_DELETE_TESTRESULT = new Integer(2061);
     /** if testresult can't be stored */
     public static final Integer E_STORE_TESTRESULT = new Integer(2062);
     /** if sql exception occured */
     public static final Integer E_SQL_EXCEPTION = new Integer(2063);
     
     // -- GENERAL ERROR IDs ------------------------------------------------
     /** if serilization exception */
     public static final Integer E_SERILIZATION_FAILED = new Integer(3001);
     /** if language is not supported */
     public static final Integer E_UNSUPPORTED_LANGUAGE = new Integer(3002);
     /** if deprecated code was used */
     public static final Integer E_DEPRECATED = new Integer(3003);
     /** if an unexpected exception occurres */
     public static final Integer E_UNEXPECTED_EXCEPTION = new Integer(3004);
     /** if the GUI is in an unexpected state, for example a missing editor */
     public static final Integer E_INVALID_GUI_STATE = new Integer(3005);
     /** if an unhandled Persistence (JPA / EclipseLink) exception occurs */ 
     public static final Integer E_UNKNOWN_DB_ERROR = new Integer(3006);
     /** if an unknown object was clicked */ 
     public static final Integer E_UNKNOWN_OBJECT = new Integer(3007);
     /** if an object could not found */ 
     public static final Integer E_ITEM_NOT_FOUND = new Integer(3008);
     /** if an perspective could not opened */ 
     public static final Integer E_NO_PERSPECTIVE = new Integer(3009);
     /** if there is a problem while refreshing problemView */ 
     public static final Integer E_PROBLEM_VIEW_REFRESH = new Integer(3011);
     /** if an error occured while gui-synchronization */
     public static final Integer E_SYNCHRONIZATION = new Integer(3012);
     /** if the current OS is not supported by GUIdancer */
     public static final Integer E_UNSUPPORTED_OS = new Integer(3013);
     /** if an error occurs while string pasring */
     public static final Integer E_SCRIPT_PARSING = new Integer(3014);
     /** if no AutConfigDialog was found */
     public static final Integer E_NO_AUTCONFIG_DIALOG = new Integer(3015);
     /** In case of converter did not found a reused Project */
     public static final Integer E_CONVERTER_REUSED_PROJ_NOT_FOUND = 
         new Integer(3016);
     /** if GUIdancer cannot start an AUT because the AUT's toolkit is not available */
     public static final Integer E_AUT_TOOLKIT_NOT_AVAILABLE = new Integer(3017);
     // -- DATAMODEL ERROR IDs ----------------------------------------------   
     /** if a parameter does not exist */
     public static final Integer E_NO_PARAMETER = new Integer(4000);
     /** if duplicate action */
     public static final Integer E_DUPLICATE_ACTION = new Integer(4001);
     /** if an action does not exist */
     public static final Integer E_NO_ACTION = new Integer(4002);
     /** if a component is not abstract */
     public static final Integer E_ABSTRACT_COMPONENT = new Integer(4003);
     /** if a component was multiple defined */
     public static final Integer E_MULTIPLE_COMPONENT = new Integer(4004);
     /** if a component does not exist */
     public static final Integer E_NO_COMPONENT = new Integer(4005);
     /** if an abstract component has no concrete components */
     public static final Integer E_NO_ABSTRACT_COMPONENT = new Integer(4006);
     /** if a not specified compSystem error occurred */
     public static final Integer E_GENERAL_COMPONENT_ERROR = new Integer(4007);
     /** if testdata do not exist */
     public static final Integer E_NO_TESTDATA = new Integer(4008);
     /** if unsupported type of visibility is used */
     public static final Integer E_TYPE_SUPPORT = new Integer(4009);
     /** if a PO class could not found */
     public static final Integer E_PO_NOT_FOUND = new Integer(4010);
     /** if component could not found */
     public static final Integer E_COMPONENT_NOT_FOUND = new Integer(4011);
     /** if the compSystem could not built */
     public static final Integer E_NO_REFERENCE = new Integer(4013);
     /** if a not supported reentry property was used */
     public static final Integer E_UNSUPPORTED_REENTRY = new Integer(4014);
     /** if double eventTC was found for the same event. */
     public static final Integer E_DOUBLE_EVENT = new Integer(4015);
     /** if a locgical component is not managed */
     public static final Integer E_COMPONENT_NOT_MANAGED = new Integer(4016);
     /** if a genral compsystem problem occurred */
     public static final Integer E_COMPSYSTEM_PROBLEM = new Integer(4017);
     /** if no id for a component could created */
     public static final Integer E_COMPONENT_ID_CREATION = new Integer(4019);
     /** if no valid data source was found */
     public static final Integer E_NOT_SUPP_DATASOURCE = new Integer(4020);
     /** if invalid data source was found */
     public static final Integer E_DATASOURCE_CONTAIN_EMPTY_DATA = 
         new Integer(4021);
     /** parameter is missing in data source file */
     public static final Integer E_DATASOURCE_MISSING_PARAMETER = 
         new Integer(4022);
     /** category can't delete because it contains reused testcases */
     public static final Integer E_NO_CAT_DELETE = new Integer(4023);
     /** object was deleted in database */
     public static final Integer E_DELETED_OBJECT = new Integer(4024);
     /** project was deleted in database */
     public static final Integer E_CURRENT_PROJ_DEL = new Integer(4025);
     /** datasource file not readable */
     public static final Integer E_DATASOURCE_FILE_IO = new Integer(4026);
     /** parameter is missing in data source file */
     public static final Integer E_DATASOURCE_LOCALE_NOTSUPPORTED = 
         new Integer(4027);
     /** parameter is missing in data source file */
     public static final Integer E_DATASOURCE_MISSING_VALUES = new Integer(4028);
     /** if item with same name exists in tree (in OM-Editor --> Drag&Drop) */
     public static final Integer E_OM_DUPLICATE_NAME = new Integer(4029);
     /** if trying to map compName to wrong technicalName (in OM-Editor --> Drag&Drop) */
     public static final Integer E_OM_WRONG_COMP_TYPE = new Integer(4030);
     /** if an parameter error occurs */
     public static final Integer E_PARAMETER_ERROR = new Integer(4031);
     /** if a reserved comp name was entered */
     public static final Integer E_RESERVED_COMP_NAME = new Integer(4032);
     /** if sth. was not entered in properties view */
     public static final Integer E_CANNOT_SAVE_EDITOR = new Integer(4033);
     /** if sth. was not entered in (ExecTC) properties view */
     public static final Integer E_CANNOT_SAVE_EDITOR_TC_EX = new Integer(4034);
     /** if sth. was not entered in (SpecTC) properties view */
     public static final Integer E_CANNOT_SAVE_EDITOR_TC_SP = new Integer(4035);
     /** if aut cannot deleted, because it is used in a testSuite */
     public static final Integer E_CANNOT_DELETE_AUT = new Integer(4036);
     /** if autConfig cannot deleted, because it is used in a testSuite */
     public static final Integer E_CANNOT_DELETE_AUT_CONFIG = new Integer(4037);
     /** if language cannot deleted, because it used in an AUT */
     public static final Integer E_DELETE_PROJECT_LANG = new Integer(4038);
     /** if there are event handler of each type in a TC */
     public static final Integer E_ENOUGH_EVENT_HANDLER = new Integer(4039);
     /** if a projectname is already in use in db */
     public static final Integer E_PROJECTNAME_ALREADY_EXISTS = 
         new Integer(4040);
     /** if a comp name was entered that is already mapped to incompatible type */
     public static final Integer E_INCOMPATIBLE_COMP_TYPE = new Integer(4041);
     /** If a general Toolkit-Problem occurs */
     public static final Integer E_GENERAL_TOOLKIT_ERROR = new Integer(4042);
     /** if a project version is already in use in db */
     public static final Integer E_PROJECTVERSION_ALREADY_EXISTS = 
         new Integer(4043);
     /** if a name conflict exists between imported and existing projects (same guid, different name) */
     public static final Integer E_PROJ_NAME_CONFLICT = 
         new Integer(4045);
     /** if a guid conflict exists between imported and existing projects (same name, different guid) */
     public static final Integer E_PROJ_GUID_CONFLICT = 
         new Integer(4046);
     /** if a project to load has a different major version */
     public static final Integer E_LOAD_PROJECT_TOOLKIT_MAJOR_VERSION_ERROR = 
         new Integer(4047);
     /** if a project xml to load is too old for converters */
     public static final Integer E_LOAD_PROJECT_XML_VERSION_ERROR = 
         new Integer(4048);
     /** Move to external project error because of toolkit levels do not match */
     public static final Integer E_MOVE_TO_EXT_PROJ_ERROR_TOOLKITLEVEL = 
         new Integer(4049);
     /** if the import of a project would cause a circular dependency */
     public static final Integer E_PROJ_CIRC_DEPEND = new Integer(4050);
     /** if an incompatibility of component name types was found while saving */
     public static final Integer E_COMP_TYPE_INCOMPATIBLE = new Integer(4051);
     /** read error while importing excel file, probably an POI error */
     public static final Integer E_DATASOURCE_READ_ERROR = new Integer(4052);
     /** if the moving of a Test Case fails due to the existence of a Component Name */
     public static final Integer E_MOVE_TC_COMP_NAME_EXISTS = new Integer(4053);
     /** if the moving of a Test Case fails due to a Component Name type incompatibility */
     public static final Integer E_MOVE_TC_COMP_TYPE_INCOMPATIBLE = 
         new Integer(4054);
     /** unsupported toolkit version, e.g. during import of projects */
     public static final Integer E_UNSUPPORTED_TOOLKIT = new Integer(4055);
     /** if a Function is not available */
     public static final Integer E_NO_FUNCTION = new Integer(4056);
     // -- CONNECTION ERROR IDs ---------------------------------------------
     /** if problem occurres, during AUT connection initialization */
     public static final Integer E_AUT_CONNECTION_INIT = new Integer(5000);
     /** if sth. was called on an connected connection */
     public static final Integer E_CONNECTED_CONNECTION = new Integer(5001);
     /** if sth. was called on an unconnected connection */
     public static final Integer E_UNCONNECTED_CONNECTION = new Integer(5002);
     /** if a unspecified connection error occurred */
     public static final Integer E_GENERAL_CONNECTION = new Integer(5003);
     /** if try to connect to unknown host */
     public static final Integer E_UNKNOWN_HOST = new Integer(5004);
     /** if ServerConnection is not initialized */
     public static final Integer E_NO_SERVER_CONNECTION_INIT = new Integer(5005);
     /** if ServerConnection is timeout */
     public static final Integer E_TIMEOUT_CONNECTION = new Integer(5006);
     /** if a paused testExecution was interrupted */
     public static final Integer E_INTERRUPTED_CONNECTION = new Integer(5007);
     /** if a version error between AutStarter and Client occurs */
     public static final Integer E_VERSION_ERROR = new Integer(5008);
     /** if an error occured while trying to start the AUT */
     public static final Integer E_AUT_START = new Integer(5009);
     /** if an error occured while trying to start the AutStarter */
     public static final Integer E_SERVER_ERROR = new Integer(5010);
     /** 
      * if an operation was attempted that requires a connection to the 
      * AUT Server, but no such connection is established 
      */
     public static final Integer E_NO_AUT_CONNECTION_ERROR = new Integer(5011);
     // -- SERVER ERROR IDs -------------------------------------------------
     /** if message header is invalid */
     public static final Integer E_INVALID_HEADER = new Integer(6000);
     /** if component is not supported */
     public static final Integer E_COMPONENT_UNSUPPORTED = new Integer(6001);
     /** if deprecated code was used */
     public static final Integer E_INTERFACE_UNIMPLEMENTED = new Integer(6002);
     /** if linkage exception occurred */
     public static final Integer E_LINKAGE = new Integer(6003);
     /** if instantiation exception occurred */
     public static final Integer E_INSTANTIATION = new Integer(6004);
     /** if illegal access exception occurred */
     public static final Integer E_ILLEGAL_ACCESS = new Integer(6005);
     /** if security exception occurred */
     public static final Integer E_SECURITY = new Integer(6006);
     /** if class not found exception occurred */
     public static final Integer E_CLASS_NOT_FOUND = new Integer(6007);
     /** if class not found exception occurred */
     public static final Integer E_COMPONENT_NOT_INSTANTIATED = 
         new Integer(6008);
     /** if creating a command failed */
     public static final Integer E_COMMAND_NOT_CREATED = new Integer(6009);
     /** if command is not asignable */
     public static final Integer E_COMMAND_NOT_ASSIGNABLE = new Integer(6010);
     /** if a message could not send as request */
     public static final Integer E_MESSAGE_NOT_TO_REQUEST = new Integer(6011);
     /** if there is no message to send */
     public static final Integer E_NO_MESSAGE_TO_SEND = new Integer(6012);
     /** if a message could not send */
     public static final Integer E_MESSAGE_NOT_SEND = new Integer(6013);
     /** if an error occurres while sending a message */
     public static final Integer E_MESSAGE_SEND = new Integer(6014);
     /** if an error occurres while message was requested */
     public static final Integer E_MESSAGE_REQUEST = new Integer(6015);
     /** if an error occurres while communitcator connection */
     public static final Integer E_COMMUNICATOR_CONNECTION = new Integer(6016);
     /** if there is no command for receiving response */
     public static final Integer E_NO_RECEIVING_COMMAND = new Integer(6017);
     /** if creating a message failed */
     public static final Integer E_MESSAGE_NOT_CREATED = new Integer(6018);
     /** if message is not asignable */
     public static final Integer E_MESSAGE_NOT_ASSIGNABLE = new Integer(6019);
     // -- RUNTIME ERROR IDs -------------------------------------------------
     /** if an editor cannot openend */
     public static final Integer E_CANNOT_OPEN_EDITOR = new Integer(7000);
     /** if there is no opened editor */
     public static final Integer E_NO_OPENED_EDITOR = new Integer(7001);
     /** if there is an unsupported node */
     public static final Integer E_UNSUPPORTED_NODE = new Integer(7002);
     /** if a perspective cannot openend */
     public static final Integer E_CANNOT_OPEN_PERSPECTIVE = new Integer(7003);
     /** if an unlocked workversion cannot be saved */
     public static final Integer E_CANNOT_SAVE_UNLOCKED = new Integer(7004);
     /** if an inavlid workversion cannot be saved */
     public static final Integer E_CANNOT_SAVE_INVALID = new Integer(7005);
     /** if you are not allowed to get a transaction because of a null session */
     public static final Integer E_NULL_SESSION = new Integer(7006);
     /** if there is an unrecoverable error */
     public static final Integer E_NON_RECOVERABLE = new Integer(7007);
     /** if an opened session failed */
     public static final Integer E_SESSION_FAILED = new Integer(7008);
     /** if an error occured while trying to start the AutStarter */
     public static final Integer E_TEST_EXECUTION_ERROR = new Integer(7009);
     /** variable could not be resolved because no saving before */
     public static final Integer E_UNRESOLV_VAR_ERROR = new Integer(7010);
     /** syntax error in execution representation of parameter value */
     public static final Integer E_SYNTAX_ERROR = new Integer(7011);
     /** if observation of a test step fails */
     public static final Integer E_TEST_STEP_NOT_CREATED = new Integer(7012);
     /** error occurred while evaluating a Function */
     public static final Integer E_FUNCTION_EVAL_ERROR = new Integer(7013);
     // -- general RUNTIME ERROR IDs (SERVER !!!) ----------------------------
     /** if an opened session failed */
     public static final Integer E_EVENT_SUPPORT = new Integer(8000);
     /** if an opened session failed */
     public static final Integer E_ROBOT = new Integer(8001);
     /** if an opened session failed */
     public static final Integer E_STEP_EXEC = new Integer(8002);
     /** if an opened session failed */
     public static final Integer E_STEP_VERIFY = new Integer(8003);
     // -- VERSION CONTROL ERROR IDs ----------------------------------------
     /** if the script location is not valid */
     public static final Integer E_SCRIPT_NOT_FOUND = new Integer(9000);
     /** if the script location is not valid */
     public static final Integer E_PROJECT_NOT_FOUND = new Integer(9001);    
     /** if the sync location is not valid */
     public static final Integer E_REPOSITORY_NOT_FOUND = new Integer(9002);
     /** if the password is not valid */
     public static final Integer E_LOGIN_FAILED = new Integer(9003);
     /** if the command is aborted */
     public static final Integer E_COMMAND_ABORTED = new Integer(9004);
     /** if authentication failed */
     public static final Integer E_AUTHENTICATION_FAILED = new Integer(9005);    
     /** if the working directory is not found */
     public static final Integer E_WORKING_DIRECTORY_NOT_FOUND = 
         new Integer(9006);
     /** IO Exception */
     public static final Integer E_IO_EXCEPTION = new Integer(9007);
     // -- SYNTAX-, SEMANTICAL-ERRORS IN PARAMETER VALUES --------------------
     /** general error in parsing of parameter value */
     public static final Integer E_GENERAL_PARSE_ERROR = new Integer(9501);
     /** error in used name for a parameter value (e.g. for a reference or variable) */
     public static final Integer E_PARSE_NAME_ERROR = new Integer(9502);
     /** missing closing brace for a name (e.g. reference or variable) in a parameter value */
     public static final Integer E_MISSING_CLOSING_BRACE = new Integer (9503);
     /** incomplete single quotes in parameter value */
     public static final Integer E_INCOMPL_QUOTES = new Integer(9504);
     /** parameter could be invalid because it contains only one special character */
     public static final Integer E_ONE_CHAR_PARSE_ERROR = new Integer(9506);
     /** specTc must not contain references */
     public static final Integer E_NO_REF_FOR_SPEC_TC = new Integer(9507);
     /** reference not allowed because of locked interface */
     public static final Integer E_INVALID_REF = new Integer(9508);
     /** parameter could be invalid because it contains no character between braces */
     public static final Integer E_MISSING_CONTENT = new Integer(9509);
     /** reference refers to an incompatible parameter type */
     public static final Integer E_INVALID_REF_TYPE = new Integer(9510);
     /** variable name in storeValue action contains invalid characters */
     public static final Integer E_INVALID_VAR_NAME = new Integer(9511);
     /**reference error, because the test case is directly beneath the test suite.  */
     public static final Integer E_REF_IN_TS = new Integer(9512);
     /**format errror for parameter in integer textfield  */
     public static final Integer E_BAD_INT = new Integer(9513);    
     /** minus sign not allowed */
     public static final Integer E_NEG_VAL = new Integer(9514);
     /** value to big for an integer number */
     public static final Integer E_TOO_BIG_VALUE = new Integer(9515);
     /** value to small for an integer number  */
     public static final Integer E_TOO_SMALL_VALUE = new Integer(9516);
     /** value is not contained in value set of a parameter */
     public static final Integer E_NOT_SUPP_COMBO_ITEM = new Integer(9517);
     /**reference error, because references are invalid for test data cubes.  */
     public static final Integer E_REF_IN_TDC = new Integer(9518);
     /** missing name for a Function in a parameter value */
     public static final Integer E_MISSING_FUNCTION_NAME = new Integer (9519);
     /** no Function registered for given name */
     public static final Integer E_FUNCTION_NOT_REGISTERED = new Integer (9520);
     /** incorrect number of arguments for a Function in a parameter value */
     public static final Integer E_WRONG_NUM_FUNCTION_ARGS = new Integer (9521);
    
     
     
     
     
     // ---------------------------------------------------------------------
     // ---------- Question IDs ---------------------------------------------
     // ---------------------------------------------------------------------
     /** if you want to delete the currently opened project */
     public static final Integer Q_DELETE_ACTUAL_PROJECT = new Integer(0);
     /** if you want to delete the selected project */
     public static final Integer Q_DELETE_PROJECT = new Integer(1);
     /** if you want to save editor and extract the selected nodes */
     public static final Integer Q_SAVE_AND_EXTRACT = new Integer(2);
     /** if you type in a reference */
     public static final Integer Q_TYPE_REFERENCE = new Integer(3);  
     /** if you want to merge the currently selected category */
     public static final Integer Q_MERGE_CATEGORY = new Integer(5);
     /** question if testdata should be deleted when an excel file is entered */
     public static final Integer Q_EXCELDATA_INPUT = new Integer(6);
     /** question if Object Mapping should be cleaned or not */
     public static final Integer Q_CLEAN_OM = new Integer(7);
     /** if you want to remove the project languages */
     public static final Integer Q_REMOVE_PROJECT_LANGUAGES = new Integer(8);
     /** if a project to load uses a toolkit with a lower minor version number */
     public static final Integer Q_LOAD_PROJECT_TOOLKIT_MINOR_VERSION_LOWER = 
         new Integer(9);
     /** if a parameter will be removed at a already used SpecTestCase */
     public static final Integer Q_CHANGE_INTERFACE_REMOVE_PARAM = 
         new Integer(10);
     /** if a parameter type be changed at a already used SpecTestCase */
     public static final Integer Q_CHANGE_INTERFACE_CHANGE_PARAM_TYPE = 
         new Integer(11);
     /** if generation is chosen without saving the editor */
     public static final Integer Q_SAVE_AND_GENERATE = 
         new Integer(12);
     /** if a parameter will be renamed at a already used Test Data Cube */
     public static final Integer Q_CHANGE_INTERFACE_CHANGE_PARAM_NAME = 
         new Integer(13);
     
     // ---------------------------------------------------------------------
     // ---------- Warning IDs ----------------------------------------------
     // --------------------------------------------------------------------- 
     /** old gd version - To remove any possible problems with the project, please export and re-import it. */
     public static final Integer W_OLD_GD_VERSION = new Integer(500);
     /** if too many characters where entered */
     public static final Integer W_MAX_CHAR = new Integer(501);
     /** if a java start script was parsed */
     public static final Integer W_SCRIPT_WAS_PARSED = new Integer(502);
     /** if GUIdancer is opening a project that uses an unavailable toolkit */
     public static final Integer W_PROJECT_TOOLKIT_NOT_AVAILABLE = 
         new Integer(503);
     // ---------------------------------------------------------------------
     // ---------- Information IDs ------------------------------------------
     // ---------------------------------------------------------------------
     /** info about converting the prefs */
     public static final Integer I_CONVERT_PREFS = new Integer(100);
     /** info about disabled version control */
     public static final Integer I_CVS_NOT_ENABLED = new Integer(101);
     /** info about null project */
     public static final Integer I_OPEN_PROJECT = new Integer(102);
     /** if you want to delete the currently selected category */
     public static final Integer I_DELETE_CATEGORY = new Integer(103);
     /** if CVS log command has failed */
     public static final Integer I_NO_VERSIONS_IN_HERE = new Integer(104);
     /** when trying to open an old project */
     public static final Integer I_OLD_GD_VERSION = new Integer(105);
     /** when trying to open an old project */
     public static final Integer I_SAVE_AND_REOPEN_EDITOR = new Integer(106);
     /** if an object is currently used in an editor */
     public static final Integer I_LOCK_OBJ_1 = new Integer(107);
     /** if an object is currently reused in an TC */
     public static final Integer I_LOCK_OBJ_2 = new Integer(108);
     /** if an object is currently reused in an TS */
     public static final Integer I_LOCK_OBJ_3 = new Integer(109);
     /** if there is no project in db */
     public static final Integer I_NO_PROJECT_IN_DB = new Integer(110);
     /** if sb. wants to delete a specTC, that is reused */
     public static final Integer I_REUSED_SPEC_TCS = new Integer(111);
     /** if you have to close some editors */
     public static final Integer I_EDITORS_TO_CLOSE = new Integer(112);
     /** if you have to save some editors */
     public static final Integer I_EDITORS_TO_SAVE = new Integer(113);
     /** if entered classpath is too long */
     public static final Integer I_TOO_LONG_CLASSPATH = new Integer(114);
     /** if server preferences are not correct */
     public static final Integer I_WRONG_SERVER_PREFS = new Integer(115);
     /** if there are no parameter at the node */
     public static final Integer I_NO_PARAM_AT_NODE_TO_PUT_DATA = 
         new Integer(116);
     /** if a started aut was modified */
     public static final Integer I_STARTED_AUT_CHANGED = new Integer(117);
     /** if file logging is not enabled */
     public static final Integer I_FILE_LOGGING_NOT_ENABLED = new Integer(118);
     /** if file logging is not enabled */
     public static final Integer I_COULD_NOT_REMOVE_REUSED_PROJECTS = 
         new Integer(119);
     /** if test cases cannot be moved to an external project */
     public static final Integer I_CANNOT_MOVE_TC = new Integer(120);
     /** if a server name has been automatically added to the preferences */
     public static final Integer I_SERVER_NAME_ADDED = new Integer(121);
     /** if the editor needs to be saved before the operation can be performed */
     public static final Integer I_SAVE_EDITOR = new Integer(122);
     /** if sb. wants to delete a test suite, that is reused */
     public static final Integer I_REUSED_TS = new Integer(123);
     /** if sb. wants to connect to a not running aut agent */
     public static final Integer I_SERVER_CANNOT_CONNECTED = new Integer(124);
     /** if sb. wants to delete a test data cube, that is reused */
     public static final Integer I_REUSED_TDC = new Integer(125);
     /** if sb. wants to open an editor for a node from a different project */
     public static final Integer I_NON_EDITABLE_NODE = new Integer(126);
     /** DB locked by background job (cleanup, data gathering, ...) */
     public static final Integer I_DB_BACKGROUND_JOB = new Integer(127);
     /** No log file found */
     public static final Integer I_NO_CLIENT_LOG_FOUND = new Integer(128);
     /** if Automatic perspective changeover has been disabled */ 
     public static final Integer I_NO_PERSPECTIVE_CHANGE = new Integer(129);
 
     // ---------------------------------------------------------------------
     /** key = message id, message object */
     private static MessageMap messageMap = null;
     
     /**
      * Private constructor.
      */
     private MessageIDs() {
         // do nothing
     }
 
     /**
      * @param id the Message.ID.
      * @return The message.
      */
     public static String getMessage(Integer id) {
         initErrorMap();       
         if (id == null) {
             return I18n.getString("ErrorMessage.generalInternalError"); //$NON-NLS-1$
         }
         return ((Message)messageMap.get(id)).getMessage(null);  
     }
 
     /**
      * inits the errorMap
      */
     private static void initErrorMap() {
         if (messageMap == null) {
             messageMap = new MessageMap();
             createIOErrorMessages(); 
             createDatabaseErrorMessages(); 
             createGeneralErrorMessages();            
             createDatamodelErrorMessages(); 
             createConnectionErrorMessages();
             createParamValueErrorMessages();
             createServerErrorMessages(); 
             createRuntimeErrorMessages();
             createServerRuntimeErrorMessages();
             createVersionControlErrorMessages();
             createQuestionMessages();
             createInfoMessages();
             createWarningMessages();
         }
     }
 
     /**
      * Creates data model error messages.
      */
     private static void createDatamodelErrorMessages() {
         int e = Message.ERROR;
         messageMap.put(E_NO_PARAMETER, e, "ErrorMessage.NO_PARAMETER", null); //$NON-NLS-1$
         messageMap.put(E_DUPLICATE_ACTION, e, "ErrorMessage.DUPLICATE_ACTION", null); //$NON-NLS-1$
         messageMap.put(E_NO_ACTION, e, "ErrorMessage.NO_ACTION", null); //$NON-NLS-1$
         messageMap.put(E_ABSTRACT_COMPONENT, e, "ErrorMessage.ABSTRACT_COMPONENT", null); //$NON-NLS-1$
         messageMap.put(E_MULTIPLE_COMPONENT, e, "ErrorMessage.MULTIPLE_COMPONENT", null); //$NON-NLS-1$
         messageMap.put(E_NO_COMPONENT, e, "ErrorMessage.NO_COMPONENT", new String[]{"ErrorDetail.NO_COMPONENT"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_NO_ABSTRACT_COMPONENT, e, "ErrorMessage.NO_ABSTRACT_COMPONENT", null); //$NON-NLS-1$
         messageMap.put(E_GENERAL_COMPONENT_ERROR, e, "ErrorMessage.GENERAL_COMPONENT_ERROR", null); //$NON-NLS-1$
         messageMap.put(E_NO_TESTDATA, e, "ErrorMessage.NO_TESTDATA", null); //$NON-NLS-1$
         messageMap.put(E_TYPE_SUPPORT, e, "ErrorMessage.TYPE_SUPPORT", null); //$NON-NLS-1$
         messageMap.put(E_PO_NOT_FOUND, e, "ErrorMessage.PO_NOT_FOUND", null); //$NON-NLS-1$
         messageMap.put(E_COMPONENT_NOT_FOUND, e, "ErrorMessage.COMPONENT_NOT_FOUND", null); //$NON-NLS-1$
         messageMap.put(E_NO_REFERENCE, e, "ErrorMessage.NO_REFERENCE", null); //$NON-NLS-1$
         messageMap.put(E_UNSUPPORTED_REENTRY, e, "ErrorMessage.UNSUPPORTED_REENTRY", null); //$NON-NLS-1$
         messageMap.put(E_DOUBLE_EVENT, e, "ErrorMessage.DOUBLE_EVENT", null); //$NON-NLS-1$
         messageMap.put(E_COMPONENT_NOT_MANAGED, e, "ErrorMessage.COMPONENT_NOT_MANAGED", null); //$NON-NLS-1$
         messageMap.put(E_COMPSYSTEM_PROBLEM, e, "ErrorMessage.COMPSYSTEM_PROBLEM", null); //$NON-NLS-1$
         messageMap.put(E_COMPONENT_ID_CREATION, e, "ErrorMessage.COMPONENT_ID_CREATION", null); //$NON-NLS-1$
         messageMap.put(E_NOT_SUPP_DATASOURCE, e, "ErrorMessage.NOT_SUPP_DATASOURCE", null); //$NON-NLS-1$
         messageMap.put(E_DATASOURCE_CONTAIN_EMPTY_DATA, e, "ErrorMessage.DATASOURCE_CONTAIN_EMPTY_DATA", null); //$NON-NLS-1$
         messageMap.put(E_DATASOURCE_FILE_IO, e, "ErrorMessage.DATASOURCE_FILE_IO", null); //$NON-NLS-1$
         messageMap.put(E_DATASOURCE_MISSING_PARAMETER, e, "ErrorMessage.DATASOURCE_MISSING_PARAMETER", null); //$NON-NLS-1$
         messageMap.put(E_NO_CAT_DELETE, e, "ErrorMessage.NO_CAT_DELETE", null); //$NON-NLS-1$
         messageMap.put(E_DELETED_OBJECT, e, "ErrorMessage.DELETED_OBJECT", null); //$NON-NLS-1$
         messageMap.put(E_CURRENT_PROJ_DEL, e, "ErrorMessage.CURRENT_PROJ_DEL", null); //$NON-NLS-1$
         messageMap.put(E_DATASOURCE_LOCALE_NOTSUPPORTED, e, "ErrorMessage.DATASOURCE_LOCALE_NOTSUPPORTED", null); //$NON-NLS-1$
         messageMap.put(E_DATASOURCE_READ_ERROR, e, "ErrorMessage.DATASOURCE_READ_ERROR", null); //$NON-NLS-1$
         messageMap.put(E_DATASOURCE_MISSING_VALUES, e, "ErrorMessage.DATASOURCE_MISSING_VALUES", null); //$NON-NLS-1$
         messageMap.put(E_OM_DUPLICATE_NAME, e, "ErrorMessage.OM_DUPLICATE_NAME", null); //$NON-NLS-1$
         messageMap.put(E_OM_WRONG_COMP_TYPE, e, "ErrorMessage.OM_WRONG_COMP_TYPE", null); //$NON-NLS-1$
         messageMap.put(E_PARAMETER_ERROR, e, "ErrorMessage.PARAMETER_ERROR", null); //$NON-NLS-1$
         messageMap.put(E_RESERVED_COMP_NAME, e, "ErrorMessage.RESERVED_COMP_NAME", null); //$NON-NLS-1$
         messageMap.put(E_CANNOT_SAVE_EDITOR, e, "ErrorMessage.CANNOT_SAVE_EDITOR", null); //$NON-NLS-1$
         messageMap.put(E_CANNOT_SAVE_EDITOR_TC_EX, e, "ErrorMessage.CANNOT_SAVE_EDITOR_TC_EX", null); //$NON-NLS-1$
         messageMap.put(E_CANNOT_SAVE_EDITOR_TC_SP, e, "ErrorMessage.CANNOT_SAVE_EDITOR_TC_SP", null); //$NON-NLS-1$
         messageMap.put(E_CANNOT_DELETE_AUT, e, "ErrorMessage.CANNOT_DELETE_AUT", new String[]{"ErrorDetail.CANNOT_DELETE_AUT"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_CANNOT_DELETE_AUT_CONFIG, e, "ErrorMessage.CANNOT_DELETE_AUT_CONFIG", new String[]{"ErrorDetail.CANNOT_DELETE_AUT_CONFIG"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_DELETE_PROJECT_LANG, e, "ErrorMessage.DELETE_PROJECT_LANG", new String[]{"ErrorDetail.DELETE_PROJECT_LANG"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_ENOUGH_EVENT_HANDLER, e, "ErrorMessage.ENOUGH_EVENT_HANDLER", null); //$NON-NLS-1$
         messageMap.put(E_PROJECTNAME_ALREADY_EXISTS, e, "ErrorMessage.PROJECTNAME_ALREADY_EXISTS", new String[]{"ErrorDetail.PROJECTNAME_ALREADY_EXISTS"}); //$NON-NLS-1$ //$NON-NLS-2$   
         messageMap.put(E_INCOMPATIBLE_COMP_TYPE, e, "ErrorMessage.INCOMPATIBLE_COMP_TYPE", null); //$NON-NLS-1$
         messageMap.put(E_GENERAL_TOOLKIT_ERROR, e, "ErrorMessage.GENERAL_TOOLKIT_ERROR", null); //$NON-NLS-1$
         messageMap.put(E_PROJECTVERSION_ALREADY_EXISTS, e, "ErrorMessage.PROJECTVERSION_ALREADY_EXISTS", new String[]{"ErrorDetail.PROJECTVERSION_ALREADY_EXISTS"}); //$NON-NLS-1$ //$NON-NLS-2$   
         messageMap.put(E_PROJ_NAME_CONFLICT, e, "ErrorMessage.PROJ_NAME_CONFLICT", new String[]{"ErrorDetail.PROJ_NAME_CONFLICT"}); //$NON-NLS-1$ //$NON-NLS-2$   
         messageMap.put(E_PROJ_GUID_CONFLICT, e, "ErrorMessage.PROJ_GUID_CONFLICT", new String[]{"ErrorDetail.PROJ_GUID_CONFLICT"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_LOAD_PROJECT_TOOLKIT_MAJOR_VERSION_ERROR, e, "ErrorMessage.CANNOT_LOAD_PROJECT_TOOLKIT_VERSION_ERROR", null); //$NON-NLS-1$
         messageMap.put(E_MOVE_TO_EXT_PROJ_ERROR_TOOLKITLEVEL, e, "ErrorMessage.MOVE_TO_EXT_PROJ_ERROR_TOOLKITLEVEL", null); //$NON-NLS-1$
         messageMap.put(E_PROJ_CIRC_DEPEND, e, "ErrorMessage.PROJ_CIRC_DEPEND", new String[]{"ErrorDetail.PROJ_CIRC_DEPEND"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_COMP_TYPE_INCOMPATIBLE, e, "ErrorMessage.COMP_TYPE_INCOMPATIBLE", null); //$NON-NLS-1$
         messageMap.put(E_MOVE_TC_COMP_NAME_EXISTS, e, "ErrorMessage.MOVE_TC_COMP_NAME_EXISTS", new String [] {"ErrorDetail.MOVE_TC_COMP_NAME_EXISTS"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_MOVE_TC_COMP_TYPE_INCOMPATIBLE, e, "ErrorMessage.MOVE_TC_COMP_TYPE_INCOMPATIBLE", new String [] {"ErrorDetail.MOVE_TC_COMP_TYPE_INCOMPATIBLE"}); //$NON-NLS-1$ //$NON-NLS-2$
     }
     
     /**
      * Creates general error messages.
      */
     private static void createGeneralErrorMessages() {
         int e = Message.ERROR;
         messageMap.put(E_SERILIZATION_FAILED, e, "ErrorMessage.SERILIZATION_FAILED", null); //$NON-NLS-1$
         messageMap.put(E_UNSUPPORTED_LANGUAGE, e, "ErrorMessage.UNSUPPORTED_LANGUAGE", null); //$NON-NLS-1$
         messageMap.put(E_DEPRECATED, e, "ErrorMessage.DEPRECATED", null); //$NON-NLS-1$
         messageMap.put(E_UNEXPECTED_EXCEPTION, e, "ErrorMessage.UNEXPECTED_EXCEPTION", new String[]{"ErrorDetail.UNEXPECTED_EXCEPTION"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_INVALID_GUI_STATE, e, "ErrorMessage.INVALID_GUI_STATE", null); //$NON-NLS-1$
         messageMap.put(E_UNKNOWN_DB_ERROR, e, "ErrorMessage.UNKNOWN_DB_ERROR", new String[]{I18n.getString("ErrorDetail.UNKNOWN_DB_ERROR")}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_UNKNOWN_OBJECT, e, "ErrorMessage.UNKNOWN_OBJECT", null);  //$NON-NLS-1$
         messageMap.put(E_ITEM_NOT_FOUND, e, "ErrorMessage.ITEM_NOT_FOUND", null); //$NON-NLS-1$
         messageMap.put(E_NO_PERSPECTIVE, e, "ErrorMessage.NO_PERSPECTIVE", new String[]{"ErrorDetail.NO_PERSPECTIVE"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(I_NO_PERSPECTIVE_CHANGE, e, "ErrorMessage.NO_PERSPECTIVE_CHANGE", null); //$NON-NLS-1$
         messageMap.put(E_PROBLEM_VIEW_REFRESH, e, "ErrorMessage.PROBLEM_VIEW_REFRESH", null); //$NON-NLS-1$
         messageMap.put(E_SYNCHRONIZATION, e, "ErrorMessage.SYNCHRONIZATION", null); //$NON-NLS-1$        
         messageMap.put(E_UNSUPPORTED_OS, e, "ErrorMessage.UNSUPPORTED_OS", null); //$NON-NLS-1$        
         messageMap.put(E_SCRIPT_PARSING, e, "ErrorMessage.SCRIPT_PARSING", new String[]{"ErrorDetail.SCRIPT_PARSING"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_NO_AUTCONFIG_DIALOG, e, "ErrorMessage.NO_AUTCONFIG_DIALOG", new String [] {"ErrorDetail.MISSING_TOOLKIT"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_CONVERTER_REUSED_PROJ_NOT_FOUND, e, "ErrorMessage.CONVERTER_REUSED_PROJ_NOT_FOUND", null); //$NON-NLS-1$
         messageMap.put(E_AUT_TOOLKIT_NOT_AVAILABLE, e, "ErrorMessage.AUT_TOOLKIT_NOT_AVAILABLE", new String [] {"ErrorDetail.MISSING_TOOLKIT"}); //$NON-NLS-1$ //$NON-NLS-2$
     }
     
     /**
      * Creates connection error messages.
      */
     private static void createConnectionErrorMessages() {
         int e = Message.ERROR;
         messageMap.put(E_AUT_CONNECTION_INIT, e, "ErrorMessage.AUT_CONNECTION_INIT", null); //$NON-NLS-1$
         messageMap.put(E_CONNECTED_CONNECTION, e, "ErrorMessage.CONNECTED_CONNECTION", null); //$NON-NLS-1$
         messageMap.put(E_UNCONNECTED_CONNECTION, e, "ErrorMessage.UNCONNECTED_CONNECTION", null); //$NON-NLS-1$
         messageMap.put(E_GENERAL_CONNECTION, e, "ErrorMessage.GENERAL_CONNECTION", null); //$NON-NLS-1$
         messageMap.put(E_NO_SERVER_CONNECTION_INIT, e, "ErrorMessage.NO_SERVER_CONNECTION_INIT", null); //$NON-NLS-1$
         messageMap.put(E_UNKNOWN_HOST, e, "ErrorMessage.UNKNOWN_HOST", null); //$NON-NLS-1$
         messageMap.put(E_TIMEOUT_CONNECTION, e, "ErrorMessage.TIMEOUT_CONNECTION", null); //$NON-NLS-1$
         messageMap.put(E_INTERRUPTED_CONNECTION, e, "ErrorMessage.INTERRUPTED_CONNECTION", null); //$NON-NLS-1$
         messageMap.put(E_VERSION_ERROR, e, "ErrorMessage.VERSION_ERROR", null); //$NON-NLS-1$
         messageMap.put(E_AUT_START, e, "ErrorMessage.AUT_START", null); //$NON-NLS-1$
         messageMap.put(E_SERVER_ERROR, e, "ErrorMessage.SERVER_ERROR", null); //$NON-NLS-1$
         messageMap.put(E_NO_AUT_CONNECTION_ERROR, e, "ErrorMessage.NO_AUT_CONNECTION_ERROR", new String[]{"ErrorDetail.NO_AUT_CONNECTION_ERROR"}); //$NON-NLS-1$ //$NON-NLS-2$
     }
     
     /**
      * Creates server error messages.
      */
     private static void createServerErrorMessages() {
         int e = Message.ERROR;
         messageMap.put(E_INVALID_HEADER, e, "ErrorMessage.INVALID_HEADER", null); //$NON-NLS-1$
         messageMap.put(E_COMPONENT_UNSUPPORTED, e, "ErrorMessage.COMPONENT_UNSUPPORTED", null); //$NON-NLS-1$
         messageMap.put(E_INTERFACE_UNIMPLEMENTED, e, "ErrorMessage.INTERFACE_UNIMPLEMENTED", null); //$NON-NLS-1$
         messageMap.put(E_LINKAGE, e, "ErrorMessage.LINKAGE", null); //$NON-NLS-1$
         messageMap.put(E_ILLEGAL_ACCESS, e, "ErrorMessage.ILLEGAL_ACCESS", null); //$NON-NLS-1$
         messageMap.put(E_SECURITY, e, "ErrorMessage.SECURITY", null); //$NON-NLS-1$
         messageMap.put(E_INSTANTIATION, e, "ErrorMessage.INSTANTIATION", null); //$NON-NLS-1$
         messageMap.put(E_CLASS_NOT_FOUND, e, "ErrorMessage.CLASS_NOT_FOUND", null); //$NON-NLS-1$
         messageMap.put(E_COMMAND_NOT_CREATED, e, "ErrorMessage.COMMAND_NOT_CREATED", new String[]{"ErrorDetail.SUPPORT"});  //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_COMMAND_NOT_ASSIGNABLE, e, "ErrorMessage.COMMAND_NOT_ASSIGNABLE", new String[]{"ErrorDetail.SUPPORT"});  //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_COMPONENT_NOT_INSTANTIATED, e, "ErrorMessage.COMPONENT_NOT_INSTANTIATED", null); //$NON-NLS-1$
         messageMap.put(E_MESSAGE_NOT_TO_REQUEST, e, "ErrorMessage.MESSAGE_NOT_TO_REQUEST", null); //$NON-NLS-1$
         messageMap.put(E_NO_MESSAGE_TO_SEND, e, "ErrorMessage.NO_MESSAGE_TO_SEND", null); //$NON-NLS-1$
         messageMap.put(E_MESSAGE_SEND, e, "ErrorMessage.MESSAGE_SEND", null); //$NON-NLS-1$
         messageMap.put(E_MESSAGE_NOT_SEND, e, "ErrorMessage.MESSAGE_NOT_SEND", null); //$NON-NLS-1$
         messageMap.put(E_MESSAGE_REQUEST, e, "ErrorMessage.MESSAGE_REQUEST", null); //$NON-NLS-1$
         messageMap.put(E_COMMUNICATOR_CONNECTION, e, "ErrorMessage.COMMUNICATOR_CONNECTION", null); //$NON-NLS-1$
         messageMap.put(E_NO_RECEIVING_COMMAND, e, "ErrorMessage.NO_RECEIVING_COMMAND", null); //$NON-NLS-1$
         messageMap.put(E_MESSAGE_NOT_CREATED, e, "ErrorMessage.MESSAGE_NOT_CREATED", new String[]{"ErrorDetail.SUPPORT"});  //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_MESSAGE_NOT_ASSIGNABLE, e, "ErrorMessage.MESSAGE_NOT_ASSIGNABLE", new String[]{"ErrorDetail.SUPPORT"});  //$NON-NLS-1$ //$NON-NLS-2$
     }
 
     /**
      * Creates database error messages.
      */
     private static void createDatabaseErrorMessages() {
         int e = Message.ERROR;
         messageMap.put(E_NO_DB_CONNECTION, e, "ErrorMessage.NO_DB_CONNECTION", null); //$NON-NLS-1$
         messageMap.put(E_PERSISTENCE_LOAD_FAILED, e, "ErrorMessage.PERSISTENCE_LOAD_FAILED", null); //$NON-NLS-1$
         messageMap.put(E_PERSISTENCE_CONFIG_PROBLEM, e, "ErrorMessage.PERSISTENCE_CONFIG_PROBLEM", null); //$NON-NLS-1$
         messageMap.put(E_PERSISTENCE_CANT_CLOSE, e, "ErrorMessage.PERSISTENCE_CANT_CLOSE", null); //$NON-NLS-1$
         messageMap.put(E_PERSISTENCE_CANT_SETUP, e, "ErrorMessage.PERSISTENCE_CANT_SETUP", null); //$NON-NLS-1$
         messageMap.put(E_PERSISTENCE_CANT_OPEN, e, "ErrorMessage.PERSISTENCE_CANT_OPEN", null); //$NON-NLS-1$
         messageMap.put(E_CANT_READ_PROJECT, e, "ErrorMessage.CANT_READ_PROJECT", null); //$NON-NLS-1$
         messageMap.put(E_NO_DB_SESSION, e, "ErrorMessage.NO_DB_SESSION", null); //$NON-NLS-1$
         messageMap.put(E_START_TRANSACTION, e, "ErrorMessage.START_TRANSACTION", null); //$NON-NLS-1$
         messageMap.put(E_ROLLBACK, e, "ErrorMessage.ROLLBACK", null); //$NON-NLS-1$
         messageMap.put(E_ATTACH_PROJECT, e, "ErrorMessage.ATTACH_PROJECT", null); //$NON-NLS-1$
         messageMap.put(E_DELETE_PROJECT, e, "ErrorMessage.DELETE_PROJECT", null); //$NON-NLS-1$
         messageMap.put(E_COMMIT_PROJECT, e, "ErrorMessage.COMMIT_PROJECT", null); //$NON-NLS-1$
         messageMap.put(E_READ_PROJECT, e, "ErrorMessage.READ_PROJECT", null); //$NON-NLS-1$
         messageMap.put(E_LOAD_PROJECT, e, "ErrorMessage.LOAD_PROJECT", null); //$NON-NLS-1$
         messageMap.put(E_LOAD_PROJECT_CONFIG_CONFLICT, e, "ErrorMessage.LOAD_PROJECT_CONFIG_CONFLICT", null); //$NON-NLS-1$
         messageMap.put(E_MAPPING_GENERAL, e, "ErrorMessage.MAPPING_GENERAL", null); //$NON-NLS-1$
         messageMap.put(E_DATABASE_GENERAL, e, "ErrorMessage.DATABASE_GENERAL", null); //$NON-NLS-1$
         messageMap.put(E_ALREADY_DELETED_PROJECT, e, "ErrorMessage.ALREADY_DELETED_PROJECT", null); //$NON-NLS-1$
         messageMap.put(E_DB_READ, e, "ErrorMessage.DB_READ", null); //$NON-NLS-1$
         messageMap.put(E_MODIFIED_OBJECT, e, "ErrorMessage.MODIFIED_OBJECT", null); //$NON-NLS-1$
         messageMap.put(E_SAVE_TO_DB_FAILED, e, "ErrorMessage.SAVE_TO_DB_FAILED", null); //$NON-NLS-1$
         messageMap.put(E_MASTER_REFRESH, e, "ErrorMessage.MASTER_REFRESH", null); //$NON-NLS-1$
         messageMap.put(E_OBJECT_IN_USE, e, "ErrorMessage.OBJECT_IN_USE", null); //$NON-NLS-1$
         messageMap.put(E_STALE_OBJECT, e, "ErrorMessage.STALE_OBJECT", null); //$NON-NLS-1$
         messageMap.put(E_COMMIT_FAILED, e, "ErrorMessage.COMMIT_FAILED", null); //$NON-NLS-1$
         messageMap.put(E_REFRESH_FAILED, e, "ErrorMessage.REFRESH_FAILED", null); //$NON-NLS-1$
         messageMap.put(E_REFRESH_REQUIRED, e, "ErrorMessage.REFRESH_REQUIRED", null); //$NON-NLS-1$
         messageMap.put(E_DB_SAVE, e, "ErrorMessage.DB_SAVE", null); //$NON-NLS-1$
         messageMap.put(E_DB_IN_USE, e, "ErrorMessage.DB_IN_USE", null); //$NON-NLS-1$
         messageMap.put(E_TRANS_STARTED, e, "ErrorMessage.TRANS_STARTED", null); //$NON-NLS-1$
         messageMap.put(E_EDITOR_REFRESH, e, "ErrorMessage.EDITOR_REFRESH", null); //$NON-NLS-1$
         messageMap.put(E_INVALID_DB_VERSION, e, "ErrorMessage.INVALID_DB_VERSION", null); //$NON-NLS-1$        
         messageMap.put(E_NOT_CHECKABLE_DB_VERSION, e, "ErrorMessage.NOT_CHECKABLE_DB_VERSION", null); //$NON-NLS-1$
         messageMap.put(E_NO_DB_SCHEME, e, "ErrorMessage.NO_DB_SCHEME", new String[]{"ErrorDetail.NO_DB_SCHEME"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_EDITOR_CLOSE, e, "ErrorMessage.EDITOR_CLOSE", null); //$NON-NLS-1$
         messageMap.put(E_DELETED_TC, e, "ErrorMessage.DELETED_TC", null); //$NON-NLS-1$
         messageMap.put(E_IMPORT_PROJECT_XML_FAILED, e, "ErrorMessage.IMPORT_PROJECT_XML_FAILED", null); //$NON-NLS-1$
         messageMap.put(E_IMPORT_PROJECT_CONFIG_CONFLICT, e, "ErrorMessage.IMPORT_PROJECT_CONFIG_CONFLICT", null); //$NON-NLS-1$
         messageMap.put(E_CANNOT_CHANGE_PARAMETER, e, "ErrorMessage.CANNOT_CHANGE_PARAMETER", null); //$NON-NLS-1$
         messageMap.put(E_SAVE_AS_PROJECT_FAILED, e, "ErrorMessage.SAVE_AS_PROJECT_FAILED", null); //$NON-NLS-1$
         messageMap.put(E_CREATE_NEW_VERSION_FAILED, e, "ErrorMessage.CREATE_NEW_VERSION_FAILED", null); //$NON-NLS-1$
         messageMap.put(E_CREATE_NEW_PROJECT_FAILED, e, "ErrorMessage.CREATE_NEW_PROJECT_FAILED", null);  //$NON-NLS-1$
         messageMap.put(E_REVERT_EDITOR_CHANGES_FAILED, e, "ErrorMessage.REVERT_EDITOR_CHANGES_FAILED", null);  //$NON-NLS-1$
         messageMap.put(E_ADD_ATTACHMENTS_TO_MAIL_FAILED, e, "ErrorMessage.ADD_ATTACHMENTS_TO_MAIL_FAILED", null);  //$NON-NLS-1$
         messageMap.put(E_OPEN_MAIL_CLIENT_FAILED, e, "ErrorMessage.OPEN_MAIL_CLIENT_FAILED", null);  //$NON-NLS-1$
         messageMap.put(E_INVALID_PROJECT_VERSION, e, "ErrorMessage.INVALID_PROJECT_VERSION", null);  //$NON-NLS-1$
         messageMap.put(E_SCHEMA_LOAD_FAILED, e, "ErrorMessage.SCHEMA_LOAD_FAILED", null);  //$NON-NLS-1$
         messageMap.put(E_ERROR_IN_SCHEMA_CONFIG, e, "ErrorMessage.ERROR_IN_SCHEMA_CONFIG", null);  //$NON-NLS-1$
         messageMap.put(E_ERROR_IN_DB_CONFIG, e, "ErrorMessage.ERROR_IN_DB_CONFIG", null);  //$NON-NLS-1$
         messageMap.put(E_DELETE_TESTRESULT, e, "ErrorMessage.DELETE_TESTRESULT", null); //$NON-NLS-1$
         messageMap.put(E_STORE_TESTRESULT, e, "ErrorMessage.STORE_TESTRESULT", null); //$NON-NLS-1$
         messageMap.put(E_SQL_EXCEPTION, e, "ErrorMessage.SQL_ERROR", null); //$NON-NLS-1$
     }
 
     /**
      * Creates IO error messages.
      */
     private static void createIOErrorMessages() {
         int e = Message.ERROR;
         messageMap.put(E_FILE_IO, e, "ErrorMessage.FILE_IO", null); //$NON-NLS-1$
         messageMap.put(E_FILE_NOT_FOUND, e, "ErrorMessage.FILE_NOT_FOUND", null); //$NON-NLS-1$
         messageMap.put(E_PROPERTIES_FILE_NOT_FOUND, e, "ErrorMessage.PROPERTIES_FILE_NOT_FOUND", new String[]{"ErrorDetail.PROPERTIES_FILE_NOT_FOUND"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_INVALID_WORKSPACE, e, "ErrorMessage.INVALID_WORKSPACE", new String[]{"ErrorDetail.INVALID_WORKSPACE"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(E_CONFIG_ERROR, e, "ErrorMessage.CONFIG_ERROR", null); //$NON-NLS-1$
     }
     
     /**
      * creates messages to describe syntax or semantic errors in parameter values
      */
     private static void createParamValueErrorMessages() {
         int e = Message.ERROR;
         messageMap.put(E_GENERAL_PARSE_ERROR, e, "ErrorMessage.GENERAL_PARSE_ERROR", null);  //$NON-NLS-1$
         messageMap.put(E_PARSE_NAME_ERROR, e, "ErrorMessage.PARSE_NAME_ERROR", null);  //$NON-NLS-1$
         messageMap.put(E_MISSING_CLOSING_BRACE, e, "ErrorMessage.MISSING_CLOSING_BRACE", null); //$NON-NLS-1$
         messageMap.put(E_INCOMPL_QUOTES, e, "ErrorMessage.INCOMPL_QUOTES", null); //$NON-NLS-1$
         messageMap.put(E_ONE_CHAR_PARSE_ERROR, e, "ErrorMessage.ONE_CHAR_PARSE_ERROR", null); //$NON-NLS-1$
         messageMap.put(E_NO_REF_FOR_SPEC_TC, e, "ErrorMessage.NO_REF_FOR_SPEC_TC", null); //$NON-NLS-1$
         messageMap.put(E_INVALID_REF, e, "ErrorMessage.INVALID_REF", null); //$NON-NLS-1$
         messageMap.put(E_MISSING_CONTENT, e, "ErrorMessage.MISSING_CONTENT", null); //$NON-NLS-1$
         messageMap.put(E_INVALID_REF_TYPE, e, "ErrorMessage.INVALID_REF_TYPE", null); //$NON-NLS-1$
         messageMap.put(E_INVALID_VAR_NAME, e, "ErrorMessage.INVALID_VAR_NAME", null); //$NON-NLS-1$
         messageMap.put(E_REF_IN_TS, e, "ErrorMessage.REF_IN_TS", null); //$NON-NLS-1$
         messageMap.put(E_REF_IN_TDC, e, "ErrorMessage.REF_IN_TDC", null); //$NON-NLS-1$
         messageMap.put(E_BAD_INT, e, "ErrorMessage.BAD_INT", null); //$NON-NLS-1$
         messageMap.put(E_NEG_VAL, e, "ErrorMessage.NEG_VAL", null); //$NON-NLS-1$
         messageMap.put(E_TOO_BIG_VALUE, e, "ErrorMessage.TOO_BIG_VALUE", null); //$NON-NLS-1$
         messageMap.put(E_TOO_SMALL_VALUE, e, "ErrorMessage.TOO_SMALL_VALUE", null); //$NON-NLS-1$
         messageMap.put(E_NOT_SUPP_COMBO_ITEM, e, "ErrorMessage.NOT_SUPP_COMBO_ITEM", null); //$NON-NLS-1$
         messageMap.put(E_MISSING_FUNCTION_NAME, e, "ErrorMessage.MISSING_FUNCTION_NAME", null); //$NON-NLS-1$
         messageMap.put(E_FUNCTION_NOT_REGISTERED, e, "ErrorMessage.FUNCTION_NOT_REGISTERED", null); //$NON-NLS-1$
         messageMap.put(E_WRONG_NUM_FUNCTION_ARGS, e, "ErrorMessage.WRONG_NUM_FUNCTION_ARGS", null); //$NON-NLS-1$
     }
     
     /**
      * Creates runtime error messages.
      */
     private static void createRuntimeErrorMessages() {
         int e = Message.ERROR;
         messageMap.put(E_CANNOT_OPEN_EDITOR, e, "ErrorMessage.CANNOT_OPEN_EDITOR", null); //$NON-NLS-1$
         messageMap.put(E_NO_OPENED_EDITOR, e, "ErrorMessage.NO_OPENED_EDITOR", null); //$NON-NLS-1$
         messageMap.put(E_UNSUPPORTED_NODE, e, "ErrorMessage.UNSUPPORTED_NODE", null); //$NON-NLS-1$
         messageMap.put(E_CANNOT_OPEN_PERSPECTIVE, e, "ErrorMessage.CANNOT_OPEN_PERSPECTIVE", null); //$NON-NLS-1$
         messageMap.put(E_CANNOT_SAVE_UNLOCKED, e, "ErrorMessage.CANNOT_SAVE_UNLOCKED", null); //$NON-NLS-1$
         messageMap.put(E_CANNOT_SAVE_INVALID, e, "ErrorMessage.CANNOT_SAVE_INVALID", null); //$NON-NLS-1$
         messageMap.put(E_NULL_SESSION, e, "ErrorMessage.NULL_SESSION", null); //$NON-NLS-1$
         messageMap.put(E_NON_RECOVERABLE, e, "ErrorMessage.NON_RECOVERABLE", null); //$NON-NLS-1$
         messageMap.put(E_SESSION_FAILED, e, "ErrorMessage.SESSION_FAILED", null); //$NON-NLS-1$
         messageMap.put(E_TEST_EXECUTION_ERROR, e, "ErrorMessage.TEST_EXECUTION_ERROR", null); //$NON-NLS-1$
         messageMap.put(E_UNRESOLV_VAR_ERROR, e, "ErrorMessage.UNRESOLV_VAR_ERROR", null); //$NON-NLS-1$
         messageMap.put(E_SYNTAX_ERROR, e, "ErrorMessage.SYNTAX_ERROR", null); //$NON-NLS-1$
         messageMap.put(E_TEST_STEP_NOT_CREATED, e, "ErrorMessage.TEST_STEP_NOT_CREATED", null); //$NON-NLS-1$
         messageMap.put(E_FUNCTION_EVAL_ERROR, e, "ErrorMessage.FUNCTION_EVAL_ERROR", null); //$NON-NLS-1$
     }
     
     /**
      * Creates server runtime error messages.
      */
     private static void createServerRuntimeErrorMessages() {
         int e = Message.ERROR;
         messageMap.put(E_ROBOT, e, "ErrorMessage.ROBOT", null); //$NON-NLS-1$
         messageMap.put(E_EVENT_SUPPORT, e, "ErrorMessage.EVENT_SUPPORT", null); //$NON-NLS-1$
         messageMap.put(E_STEP_EXEC, e, "ErrorMessage.STEP_EXEC", null); //$NON-NLS-1$
         messageMap.put(E_STEP_VERIFY, e, "ErrorMessage.STEP_VERIFY", null); //$NON-NLS-1$
     }
     
     /**
      *Creates version control error messages
      */
     private static void createVersionControlErrorMessages() {
         int e = Message.ERROR;
         messageMap.put(E_SCRIPT_NOT_FOUND, e, "ErrorMessage.SCRIPT_NOT_FOUND", null); //$NON-NLS-1$
         messageMap.put(E_PROJECT_NOT_FOUND, e, "ErrorMessage.PROJECT_NOT_FOUND", null); //$NON-NLS-1$        
         messageMap.put(E_REPOSITORY_NOT_FOUND, e, "ErrorMessage.REPOSITORY_NOT_FOUND", null); //$NON-NLS-1$
         messageMap.put(E_LOGIN_FAILED, e, "ErrorMessage.LOGIN_FAILED", null); //$NON-NLS-1$
         messageMap.put(E_COMMAND_ABORTED, e, "ErrorMessage.COMMAND_ABORTED", null); //$NON-NLS-1$
         messageMap.put(E_AUTHENTICATION_FAILED, e, "ErrorMessage.AUTHENTICATION_FAILED", null); //$NON-NLS-1$
         messageMap.put(E_WORKING_DIRECTORY_NOT_FOUND, e, "ErrorMessage.WORKING_DIRECTORY_NOT_FOUND", null); //$NON-NLS-1$
         messageMap.put(E_IO_EXCEPTION, e, "ErrorMessage.IO_EXCEPTION", null); //$NON-NLS-1$
     }
     
     /**
      * Creates question messages.
      */
     private static void createQuestionMessages() {
         int q = Message.QUESTION;
         messageMap.put(Q_DELETE_ACTUAL_PROJECT, q, "QuestionMessage.DELETE_ACTUAL_PROJECT", null);  //$NON-NLS-1$
         messageMap.put(Q_DELETE_PROJECT, q, "QuestionMessage.DELETE_PROJECT", null);  //$NON-NLS-1$
         messageMap.put(Q_SAVE_AND_EXTRACT, q, "QuestionMessage.SAVE_AND_EXTRACT", null);  //$NON-NLS-1$
         messageMap.put(Q_TYPE_REFERENCE, q, "QuestionMessage.TYPE_REFERENCE", null);   //$NON-NLS-1$
         messageMap.put(Q_MERGE_CATEGORY, q, "QuestionMessage.MERGE_CATEGORY", null);   //$NON-NLS-1$
         messageMap.put(Q_EXCELDATA_INPUT, q, "QuestionMessage.EXCELDATA_INPUT", null);   //$NON-NLS-1$
         messageMap.put(Q_CLEAN_OM, q, "QuestionMessage.CLEAN_OM", null); //$NON-NLS-1$
         messageMap.put(Q_REMOVE_PROJECT_LANGUAGES, q, "QuestionMessage.REMOVE_PROJECT_LANGUAGES", null); //$NON-NLS-1$
         messageMap.put(Q_LOAD_PROJECT_TOOLKIT_MINOR_VERSION_LOWER, q, "QuestionMessage.LOAD_PROJECT_TOOLKIT_MINOR_VERSION_LOWER", null); //$NON-NLS-1$
         messageMap.put(Q_CHANGE_INTERFACE_REMOVE_PARAM, q, "QuestionMessage.CHANGE_INTERFACE_REMOVE_PARAM", null); //$NON-NLS-1$
         messageMap.put(Q_CHANGE_INTERFACE_CHANGE_PARAM_TYPE, q, "QuestionMessage.CHANGE_INTERFACE_CHANGE_PARAM_TYPE", null); //$NON-NLS-1$
         messageMap.put(Q_SAVE_AND_GENERATE, q, "QuestionMessage.Q_SAVE_AND_GENERATE", null); //$NON-NLS-1$
         messageMap.put(Q_CHANGE_INTERFACE_CHANGE_PARAM_NAME, q, "QuestionMessage.Q_CHANGE_INTERFACE_CHANGE_PARAM_NAME", null); //$NON-NLS-1$
     }
     
     /**
      * Creates info messages.
      */
     private static void createInfoMessages() {
         int i = Message.INFO;
         messageMap.put(I_CONVERT_PREFS, i, "InfoMessage.CONVERT_PREFS", null);   //$NON-NLS-1$
         messageMap.put(I_OPEN_PROJECT, i, "InfoMessage.OPEN_PROJECT", null);   //$NON-NLS-1$
         messageMap.put(I_CVS_NOT_ENABLED, i, "InfoMessage.CVS_NOT_ENABLED", null);   //$NON-NLS-1$
         messageMap.put(I_DELETE_CATEGORY, i, "InfoMessage.DELETE_CATEGORY", null);   //$NON-NLS-1$
         messageMap.put(I_NO_VERSIONS_IN_HERE, i, "InfoMessage.NO_VERSIONS_IN_HERE", null); //$NON-NLS-1$
         messageMap.put(I_OLD_GD_VERSION, i, "InfoMessage.OLD_GD_VERSION", null); //$NON-NLS-1$
         messageMap.put(I_SAVE_AND_REOPEN_EDITOR, i, "InfoMessage.SAVE_AND_REOPEN_EDITOR", null); //$NON-NLS-1$
         messageMap.put(I_LOCK_OBJ_1, i, "InfoMessage.LOCK_OBJ_1", null); //$NON-NLS-1$
         messageMap.put(I_LOCK_OBJ_2, i, "InfoMessage.LOCK_OBJ_2", null); //$NON-NLS-1$
         messageMap.put(I_LOCK_OBJ_3, i, "InfoMessage.LOCK_OBJ_3", null); //$NON-NLS-1$
         messageMap.put(I_NO_PROJECT_IN_DB, i, "InfoMessage.NO_PROJECT_IN_DB", null); //$NON-NLS-1$
         messageMap.put(I_REUSED_SPEC_TCS, i, "InfoMessage.REUSED_SPEC_TCS", null); //$NON-NLS-1$
         messageMap.put(I_REUSED_TS, i, "InfoMessage.REUSED_TS", null); //$NON-NLS-1$
         messageMap.put(I_EDITORS_TO_CLOSE, i, "InfoMessage.EDITORS_TO_CLOSE", null); //$NON-NLS-1$
         messageMap.put(I_EDITORS_TO_SAVE, i, "InfoMessage.EDITORS_TO_SAVE", null); //$NON-NLS-1$
         messageMap.put(I_TOO_LONG_CLASSPATH, i, "InfoMessage.TOO_LONG_CLASSPATH", null); //$NON-NLS-1$
         messageMap.put(I_WRONG_SERVER_PREFS, i, "InfoMessage.WRONG_SERVER_PREFS", null); //$NON-NLS-1$
         messageMap.put(I_NO_PARAM_AT_NODE_TO_PUT_DATA, i, "InfoMessage.NO_PARAM_AT_NODE_TO_PUT_DATA", null); //$NON-NLS-1$
         messageMap.put(I_STARTED_AUT_CHANGED, i, "InfoMessage.STARTED_AUT_CHANGED", null); //$NON-NLS-1$
         messageMap.put(I_FILE_LOGGING_NOT_ENABLED, i, "InfoMessage.FILE_LOGGING_NOT_ENABLED", null); //$NON-NLS-1$
         messageMap.put(I_COULD_NOT_REMOVE_REUSED_PROJECTS, i, "InfoMessage.COULD_NOT_REMOVE_REUSED_PROJECTS", null); //$NON-NLS-1$
         messageMap.put(I_CANNOT_MOVE_TC, i, "InfoMessage.CANNOT_MOVE_TC", new String[]{"InfoDetail.CANNOT_MOVE_TC"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(I_SERVER_NAME_ADDED, i, "InfoMessage.SERVER_NAME_ADDED", new String[]{"InfoDetail.SERVER_NAME_ADDED"}); //$NON-NLS-1$ //$NON-NLS-2$
         messageMap.put(I_SAVE_EDITOR, i, "InfoMessage.SAVE_EDITOR", null); //$NON-NLS-1$
         messageMap.put(I_SERVER_CANNOT_CONNECTED, i, "InfoMessage.connGuiDancerServerFailed", null); //$NON-NLS-1$
         messageMap.put(I_REUSED_TDC, i, "InfoMessage.I_REUSED_TDC", null); //$NON-NLS-1$
         messageMap.put(I_NON_EDITABLE_NODE, i, "InfoMessage.I_NON_EDITABLE_NODE", null); //$NON-NLS-1$
         messageMap.put(I_DB_BACKGROUND_JOB, i, "InfoMessage.DB_BACKGROUND_JOB", null); //$NON-NLS-1$
         messageMap.put(I_NO_CLIENT_LOG_FOUND, i, "InfoMessage.NO_CLIENT_LOG_FOUND", null); //$NON-NLS-1$
         messageMap.put(I_NO_PERSPECTIVE_CHANGE, i, "InfoMessage.NO_PERSPECTIVE_CHANGE", null); //$NON-NLS-1$
     }
     
     /**
      * Creates warning messages.
      */
     private static void createWarningMessages() {
         int w = Message.WARNING;
         messageMap.put(W_OLD_GD_VERSION, w, "WarnMessage.OLD_GD_VERSION", null);   //$NON-NLS-1$
         messageMap.put(W_MAX_CHAR, w, "WarnMessage.MAX_CHAR", null);   //$NON-NLS-1$
         messageMap.put(W_SCRIPT_WAS_PARSED, w, "WarnMessage.SCRIPT_WAS_PARSED", null);   //$NON-NLS-1$
         messageMap.put(W_PROJECT_TOOLKIT_NOT_AVAILABLE, w, "WarnMessage.PROJECT_TOOLKIT_NOT_AVAILABLE", new String [] {"ErrorDetail.MISSING_TOOLKIT"}); //$NON-NLS-1$ //$NON-NLS-2$
     }
     
     /**
      * @param id the message id
      * @return the message object
      */
     public static Message getMessageObject(Integer id) {
         initErrorMap();
         return (Message)messageMap.get(id);
     }
     
     /**
      * @return the message map
      */
     public static Map getMessageMap() {
         initErrorMap();
         return messageMap;
     }
     
     /**
      * MessageMap for GUIdancer messages.
      * @author BREDEX GmbH
      * @created 10.05.2006
      */
     private static class MessageMap extends HashMap {
 
         /**
          * @param id The id of the message.
          * @param severity <code>Message.INFO</code>, <code>Message.ERROR</code>, 
          * <code>Message.WARNING</code> or <code>Message.QUESTION.</code>
          * @param message The message text.
          * @param details The details text or null (if you don't need details).
          * @return see: HashMap.put(Object key, Object value);
          */
         public Object put(Integer id, int severity, String message, 
                 String[] details) {
             Message value = new Message(id, severity, message, details);
             return super.put(id, value);
         }
 
         /**
          * {@inheritDoc}
          * @deprecated use put(Integer id, int severity, String message, 
                 String details) instead
          */
         public Object put(Object key, Object value) {
             return super.put(key, value);
         }
     }
 }
