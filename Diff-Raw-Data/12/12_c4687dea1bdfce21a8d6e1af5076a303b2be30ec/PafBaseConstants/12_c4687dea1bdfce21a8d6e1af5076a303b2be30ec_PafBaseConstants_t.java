 package com.pace.base;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 
 import com.pace.base.app.VersionType;
 
 public class PafBaseConstants {
 
 	//Java is better than .NET
 	public static Locale AppLocal = Locale.ENGLISH;
     public static final String DEFAULT_LANGUAGE = "En-us";
 	public static final String LINE_TERM = "\n";
 	public static final String NOT_INITIALIZED = "[Not Initialized]";
 	public static final String PAF_BLANK = "PAFBLANK";
 	public static final String PAF_SECURITY_DB = "db" + File.separator + "data" + File.separator + "pafsecurity";
     public static final String PAF_CACHE_DB = "db" + File.separator + "data" + File.separator + "pafdb";
     public static final String PAF_EXT_ATTR_DB = "db" + File.separator + "data" + File.separator + "pafextattr";
     public static final String PAF_CLIENT_CACHE_DB = "db" + File.separator + "data" + File.separator + "pafclientcache";
     
     public static final String PACE_SERVER_HOME_ENV = "PaceHome";
 	public static final String UOW_ROOT = "@UOW_ROOT";
 	public static final String USER_SEL_TAG = "@USER_SEL";
 	public static final String PLAN_VERSION = "@PLAN_VERSION";
 	
 	// time horizon constants
 	public static final String TIME_HORIZON_DEFAULT_YEAR = "**YEAR.NA**";
 	public static final String TIME_HORIZON_DIM = "**TIME.HORIZON**";
 	public static final String TIME_HORIZON_MBR_DELIM = "||";  
 	public static final int TIME_HORIZON_MBR_DELIM_LEN = TIME_HORIZON_MBR_DELIM.length();  
 
 	// synthetic member constants
 	public static final String SYNTHETIC_ROOT_ALIAS_PREFIX = "[FILTERED ";
 	public static final String SYNTHETIC_ROOT_ALIAS_SUFFIX = "]";
 	public static final String SYNTHETIC_YEAR_ROOT_ALIAS = "[ALL YEARS]";
 	
 	// version type constants
 	public static final VersionType[] BASE_VERSION_TYPE_ARRAY =
		new VersionType[] {VersionType.ForwardPlannable, VersionType.NonPlannable, VersionType.Plannable, VersionType.Offset};
 	public static final List<VersionType> BASE_VERSION_TYPE_LIST = Arrays.asList(BASE_VERSION_TYPE_ARRAY);
 	public static final VersionType[] DERIVED_VERSION_TYPE_ARRAY =
		new VersionType[] {VersionType.Calculated, VersionType.ContribPct, VersionType.Variance};
 	public static final List<VersionType> DERIVED_VERSION_TYPE_LIST = Arrays.asList(DERIVED_VERSION_TYPE_ARRAY);
 	
 	public static final String CONN_PROPERTY_ESB_APP = "APPLICATION";
 	public static final String CONN_PROPERTY_ESB_DB = "DATABASE";
 	public static final String CONN_PROPERTY_ESB_PSWD = "PASSWORD";
 	public static final String CONN_PROPERTY_ESB_SERVER = "SERVER";
 	public static final String CONN_PROPERTY_ESB_USER = "USER";
 
 	// data cache constants
 	public static final double DC_DECIMAL_PRECISION = 8;
 	public static final double DC_ROUNDING_CONSTANT = Math.pow(10, DC_DECIMAL_PRECISION);
 	public static final double DC_TRACK_CHANGES_THRESHHOLD = 1 / DC_ROUNDING_CONSTANT;
 
 	// essbase constants
 	public static final int ESS_FILE_OJBECT_NAME_MAX_LEN = 8;
 	public static final String ESS_CALC_SCRIPT_SUFFIX = ".csc";
 	public static final String ESS_REPORT_SCRIPT_SUFFIX = ".rep";
 	public static final String ESS_TEMP_FILE_PREFIX = "esb";
 	public static final String ESS_TEXT_FILE_SUFFIX = ".txt";
 	public static final String ESS_DEF_ALIAS_TABLE = "Default";
 	public static final int ESS_CONNECT_TIMEOUT_ERR = 1042006;
 	public static final int ESS_NET_TIMEOUT_DEFAULT = 30000;
     
 	// ruleset constants
 	public static final int RULESET_TYPE_HIERARCHY = 0;    
     public static final int RULESET_TYPE_MEASURE = 2;
     public static final int RULESET_TYPE_TIME_BAL_FIRST = 5;
     public static final int RULESET_TYPE_TIME_BAL_LAST = 6;
 
 	public static final String FN_ApplicationMetaData = "paf_apps.xml";
 	public static final String FN_CustomMenus = "paf_custom_menus.xml";
 	public static final String FN_CustomFunctionMetaData = "paf_functions.xml";
 	public static final String FN_GenerationFormats = "paf_generation_formats.xml";
 	public static final String FN_GlobalStyleMetaData = "paf_global_styles.xml";
 	public static final String FN_MeasureMetaData = "paf_measures.xml";
 	public static final String FN_NumericFormatsMetaData = "paf_numeric_formats.xml";
 	public static final String FN_PlannerConfigs = "paf_planner_conf.xml";
 	public static final String FN_RoleMetaData = "paf_roles.xml";
 	public static final String FN_RuleSetMetaData = "paf_rules.xml";
 	public static final String FN_RoundingRules = "paf_rounding_rules.xml";
 	public static final String FN_SecurityMetaData = "paf_security.xml";
 	public static final String FN_ServerInitialization = "Install.properties";
 	public static final String FN_ScreenMetaData = "paf_screen.xml";
 //	public static final String FN_SpringConfig = "spring-config.xml";
 	public static final String FN_UserSelections = "paf_user_selections.xml";
 	public static final String FN_VersionMetaData = "paf_versions.xml";
 	public static final String FN_ViewsMetaData = "paf_views.xml";
 	public static final String FN_ViewSectionsMetaData = "paf_view_sections.xml";
 	public static final String FN_ViewGroups = "paf_view_groups.xml";
 	public static final String FN_ElapsedLastPeriod = "lastperiod.dat";
 	public static final String FN_HierarchyFormats = "paf_hierarchy_formats.xml";
 	public static final String FN_DynamicMembers = "paf_dynamic_members.xml";	
 	public static final String FN_MemberTagMetaData = "paf_member_tags.xml";
 	public static final String FN_EssbaseConnPropTest = "connection.properties";
 	public static final String FN_PrintStyles = "paf_print_styles.xml";
 	public static final String FN_UserMemberLists = "user_memberlists.xml";
 	
 	public static final String FN_ServerSettings = "serverSettings.xml";
 	public static final String FN_MdbDataSources = "mdbDs.xml";	
 	public static final String FN_RdbDataSources = "rdbDs.xml";
 	
     
     public static final String DN_ConfFldr = "conf";
     public static final String DN_ConfServerFldr = "conf_server";    
     public static final String DN_MdbDriverFldr = "mdb_drivers";    
     public static final String DN_TransferFldr = "transfer";
     public static final String DN_ViewsFldr = "paf_views";
     public static final String DN_ViewSectionsFldr = "paf_view_sections";
     public static final String DN_RuleSetsFldr = "paf_rule_sets";
     
     public static final String DN_PaceTestFldr = "pace-test";
     public static final String DN_PaceTmpFldr = "pace-tmp";
     public static final String DN_JUnit = "junit";
     
     public static final String XML_EXT = ".xml";
     public static final String BAK_EXT = ".bak";
 	public static final String XLSX_EXT = ".xlsx";
 	public static final String XLSM_EXT = ".xlsm";
 
     //AC
     public static final String FN_Project = "paf_project.xml";
     
     // JNDI locations
     public static final String JN_InitCtx = "java:";    
     public static final String JN_PaceRoot = "pace";
     public static final String JN_PaceHome = "paceHome";
     
     // custom command script tokens (could apply to calc scripts, report scripts, etc.)
 	public static final String CC_PARM_DEFAULT = "DEFAULT";
 	public static final String CC_SESSION_TOKEN_CLIENTID = "CLIENTID";
 	public static final String CC_SESSION_TOKEN_CYCLENAME = "CYCLENAME";
 	public static final String CC_SESSION_TOKEN_DATE = "DATE";
 	public static final String CC_SESSION_TOKEN_DATETIME = "DATETIME";
 	public static final String CC_SESSION_TOKEN_ROLENAME = "ROLENAME";
 	public static final String CC_SESSION_TOKEN_USERNAME = "USERNAME";
 	public static final String CC_SESSION_TOKEN_PLAN_VERSION = "PLANVERSION";
 	public static final String CC_TOKEN_PREFIX_ACTION_PARM = "ACTIONPARM.";
 	public static final String CC_TOKEN_PREFIX_MENU_DEF = "MENUDEF.";
 	public static final String CC_TOKEN_PREFIX_MENU_INPUT = "MENUINPUT.";
 	public static final String CC_TOKEN_PREFIX_SESSION = "SESSION.";
 	public static final String CC_TOKEN_SUFFIX_NOQUOTES = ".NOQUOTES";
 	public static final String CALC_TOKEN_PREFIX_ROLEFILTER_SEL = "ROLEFILTER.SEL.";
 	public static final String CALC_TOKEN_PREFIX_UOW_AGG_FLOOR_GEN = "UOWAGGFLOOR.GEN.";
 	public static final String CALC_TOKEN_PREFIX_UOW_AGG_FLOOR_LVL = "UOWAGGFLOOR.LEVEL.";
 	public static final String CALC_TOKEN_PREFIX_UOW_FLOOR_GEN = "UOWFLOOR.GEN.";
 	public static final String CALC_TOKEN_PREFIX_UOW_FLOOR_LVL = "UOWFLOOR.LEVEL.";
 	public static final String CALC_TOKEN_PREFIX_UOW_ROOT = "UOWROOT.";
 	public static final String CALC_TOKEN_PREFIX_UOW_MBRS = "UOWMEMBERS.";
 	public static final String CALC_TOKEN_PREFIX_UOW_FLOOR_MBRS = "UOWMEMBERS.FLOOR.";
 	public static final String CALC_TOKEN_PREFIX_USER_SEL = "USERSEL.";
 	public static final String CC_TOKEN_START_CHAR = "[";
 	public static final String CC_TOKEN_END_CHAR = "]";
 	
 	// function tokens
 	public static final String FUNC_TOKEN_PARENT = "[PARENT]";
 	public static final String FUNC_TOKEN_UOWROOT = "[UOWROOT]";
 	public static final String FUNC_TOKEN_START_CHAR = "[";
 	
 	// header tokens
 	public static final String HEADER_TOKEN_START_CHAR = "@";
 	public static final String HEADER_TOKEN_MEMBER_TAG = "@MEMBER_TAG";
 	public static final String HEADER_TOKEN_PLAN_VERSION = "@PLAN_VERSION";
 	public static final String HEADER_TOKEN_ROLE_FILTER_SEL = "@ROLEFILTER_SEL";
 	public static final String HEADER_TOKEN_USER_SEL = "@USER_SEL";
 	public static final String HEADER_TOKEN_PARM_START_CHAR = "(";
 	public static final String HEADER_TOKEN_PARM_END_CHAR = ")";
 
 	// version formula tokens
 	public static final String VF_TOKEN_PARENT = "@PARENT";
 	public static final String VF_TOKEN_UOWROOT = "@UOWROOT";
 
 	public static final String LEVELGEN_TOKEN_LEVEL_IDENT = "L";
 	public static final String LEVELGEN_TOKEN_GENERATION_IDENT = "G";
 	
 //	public static final String PK_PafServerInstallPath = System
 //			.getenv(SERVER_HOME_ENV) + File.separator;
 
 //	public static final String FN_DataTransferPath = ".." + File.separator + ".." + File.separator
 //			+ DN_TransferFldr + File.separator;
 	
 	public static final String FN_ConfPath = ".." + File.separator + ".." + File.separator
 	+ DN_ConfFldr + File.separator;
 
 
 	// public static final String FN_SeasonMetaData = "paf_seasons.xml";
 	
 	public static final String HIBERNATE_PAF_DB_CONFIG_FL = "pafdb.cfg.xml";
 	public static final String HIBERNATE_PAF_EXT_ATTR_DB_CONFIG_FL = "pafExtAttr.cfg.xml";
 	public static final String HIBERNATE_PAF_SECURITY_DB_CONFIG_FL = "pafsecurity.cfg.xml";
 	public static final String HIBERNATE_PAF_CLIENT_CACHE_DB_CONFIG_FL = "pafClientCache.cfg.xml";
 	public static final String HIBERNATE_PAF_CLIENT_CACHE_DB_CONFIG_FL_JUNIT = "junit-pafClientCache.cfg.xml";
 	
 	//default colors
 	public static final String COLOR_PROTECTED_NON_PLANNABLE = "008080";
 	public static final String COLOR_PROTECTED_FORWARD_PLANNABLE = "FF8080";
 	public static final String COLOR_PROTECTED = "FF6600";
 	public static final String COLOR_SYSTEM_LOCK = "FFFF00";
 	public static final String COLOR_USER_LOCK = "00FFFF";
 	public static final String COLOR_NOTE = "99CCFF";
 	
 	//default alias table
 	public static final String ALIAS_TABLE_DEFAULT = "Default";
 	
 	//delimeters
 	public static final String  DELIMETER_ELEMENT = "^";  //unit separator
 	public static final String  DELIMETER_GROUP = "|^";	 //group separator
 	
 	//escape characters
 	public static final String ESCAPE_CHARACTER_ELEMENT = "/";
 	public static final String ESCAPE_CHARACTER_GROUP = "\\";
 	
 	//LDAP
 	public static final String Native_Domain_Name = "Native";
 	
 	//auth
 	public static final AuthMode DEFAULT_AUTH_MODE = AuthMode.nativeMode;
 	
 	//encryption
 	public static final String AES_Password = "Steak Dinner";
 	
 	public static final String DEFAULT_SERVER_URL_TIMEOUT_IN_MILLISECONDS = "30000";
 	
 	//MDB
 	public static final String DEFAULT_MDB_CLASS_LOADER = "com.pace.base.server.EsbClassLoader";
 
 	//password min/max default length
 	public static final int DEFAULT_PASSWORD_MAX_LENGTH = 16;
 	public static final int DEFAULT_PASSWORD_MIN_LENGTH = 6;
 	public static final int DATABASE_PASSWORD_MAX_LENGTH = 20;
 	
 	
 	//XML open/close tags
 	public static final String XML_OPEN_TAG = "<";
 	
 	public static final String XML_CLOSE_TAG = ">";
 	
 	//xsd namespaces
 	public static final String XML_NAMESPACE_URL = "http://www.thepalladiumgroup.com/";
 	
 	public static final String XML_NAMESPACE_START = "xmlns=\"";
 	
 	public static final String XML_NAMESPACE_END = "\"";
 	
 	public static final String HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_APPS = createXMLNamespace("paf_apps");
 	public static final String HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_CUSTOM_MENUS = createXMLNamespace("paf_custom_menus");
 	public static final String HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_FUNCTIONS = createXMLNamespace("paf_functions");
 	public static final String HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_MEASURES = createXMLNamespace("paf_measures");
 	public static final String HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_ROUNDING_RULES = createXMLNamespace("paf_rounding_rules");	
 	public static final String HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_RULE_SET = createXMLNamespace("paf_rule_set");
 	public static final String HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_VERSIONS = createXMLNamespace("paf_versions");
 	
 	public static final String SVN_HIDDEN_DIR_NAME = ".svn";
 	
 	
 	//TTN 900 - Print Preferences - added by Iris
 	public static final String PRINTSTYLE_DEFAULT_PRINT_SETTINGS_FILE = "default_printsettings.xml";
 	public static final String PRINTSTYLE_PAGE_ORIENTATION_LANDSCAPE = "Landscape";
 	public static final String PRINTSTYLE_PAGE_ORIENTATION_PORTRAIT = "Portrait";
 	public static final String PRINTSTYLE_PAGE_SCALING_ADJUSTTO = "AdjustTo";
 	public static final String PRINTSTYLE_PAGE_SCALING_FITTO = "FitTo";
 	public static final String PRINTSTYLE_PRINTEREA_ENTIREVIEW = "Entire View";
 	public static final String PRINTSTYLE_SHEET_PAGEORDER_DOWNTHENOVER = "DownThenOver";
 	public static final String PRINTSTYLE_SHEET_PAGEORDER_OVERTHENDOWN = "OverThenDown";
 	public static final String PRINTSTYLE_PAGE_PAPERSIZE_LETTER = "Letter";
 	public static final String PRINTSTYLE_PAGE_PAPERSIZE_TABLOID = "Tabloid";
 	public static final String PRINTSTYLE_PAGE_PAPERSIZE_LEGAL = "Legal";
 	public static final String PRINTSTYLE_PAGE_PAPERSIZE_EXECUTIVE = "Executive";
 	public static final String PRINTSTYLE_PAGE_PAPERSIZE_A3 = "A3";
 	public static final String PRINTSTYLE_PAGE_PAPERSIZE_A4 = "A4";
 	public static final String PRINTSTYLE_PAGE_PAPERSIZE_B4JIS = "B4 (JIS)";
 	public static final String PRINTSTYLE_PAGE_PAPERSIZE_B5JIS = "B5 (JIS)";
 	public static final String PRINTSTYLE_PAGE_PAPERSIZE_ENVELOPE10 = "Envelope #10";
 	public static final String PRINTSTYLE_PAGE_PAPERSIZE_ENVELOPEMONARCH = "Envelope Monarch";
 	public static final String PRINTSTYLE_SHEET_COMMENTS_NONE = "(None)";
 	public static final String PRINTSTYLE_SHEET_COMMENTS_ATENDOFSHEET = "At end of sheet";
 	public static final String PRINTSTYLE_SHEET_COMMENTS_ASDISPLAYEDONSHEET = "As displayed on sheet";
 	public static final String PRINTSTYLE_SHEET_CELLERRORSAS_DISPLAYED = "displayed";
 	public static final String PRINTSTYLE_SHEET_CELLERRORSAS_BLANK = "<blank>";
 	public static final String PRINTSTYLE_SHEET_CELLERRORSAS_DASHDASH= "--";
 	public static final String PRINTSTYLE_SHEET_CELLERRORSAS_NA= "#N/A";
 	public static final String EMBEDED_PRINT_SETTINGS = "Embeded Print Settings";
 	
 	public static final String VIEW_PAGE_ORIENTATION_LANDSCAPE = "Landscape";
 	public static final String VIEW_PAGE_ORIENTATION_PORTRAIT = "Portrait";
 	//performance logging statistic constants
 	public static final String PERFORMANCE_LOGGER_AUTH = "pace.performance.authentication";
 	public static final String PERFORMANCE_LOGGER_CELLNOTES_IO = "pace.performance.cellnotes.io";
 	public static final String PERFORMANCE_LOGGER_DC = "pace.performance.datacache";
 	public static final String PERFORMANCE_LOGGER_EVAL = "pace.performance.eval";
 	public static final String PERFORMANCE_LOGGER_MBRTAGS_IO = "pace.performance.membertags.io";
 	public static final String PERFORMANCE_LOGGER_MDB_IO = "pace.performance.mdb.io";
 	public static final String PERFORMANCE_LOGGER_VIEW_RNDR = "pace.performance.view.render";
 	public static final String PERFORMANCE_LOGGER_UOW_LOAD = "pace.performance.uow.load";
 
 	
 	public static final String PACE_SERVER_SETTINGS_CONTEXT_NAME = "settings";
 	
 	
 	/**
 	 * 
 	 *  Generates the namespace line for xml validation
 	 *
 	 * @param xsdFilename - name of xsd file
 	 * @return exampe: xmlns="http://www.thepalladiumgroup.com/paf_apps.xsd"
 	 */
 	private static String createXMLNamespace(String xsdFilename) {
 
 		return XML_NAMESPACE_START + XML_NAMESPACE_URL + xsdFilename + XML_NAMESPACE_END; 
 		
 	}
 
 	//    public static final String SERVER_HOME_ENV = "PafServerHome";
 	
 }
