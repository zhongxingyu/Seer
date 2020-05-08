 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
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
  */
 package sorcer.core;
 
 /**
  * SORCER interface provides predefined constants, commands and parameter names
  * for the SORCER metacomputing environment. Use them to allow for
  * interoperability between different classes, in particular to communicate
  * between requestors (clients) and providers (servers).
  */
 public interface SorcerConstants {
 	// SORCER provider property names
 	// P_ATTRIBUTE is a provider property defined in a properties file
 	// J_ATTRIBURE is variable name in Jini configuration file
     // S_ATTRIBUTE is variable name in System properties
     // E_ATTRIBUTE is variable name in the system Environment
 	/* service provider genetic properties */
	public static final String SORCER_VERSION = "1.0-SNAPSHOT";
 	// SORCER global properties defined in sorcer.util.Sorcer.java
 	public static final String SORCER_HOME = "sorcer.home";
 
 	public static final String E_SORCER_HOME = "SORCER_HOME";
 
 	public static final String P_UNDEFINED = "undefined";
 
 	public static final String P_PROVIDER_NAME = "provider.name";
 
 	public static final String  EXERT_MONITOR_NAME = "provider.exert.monitor.name";
 	
 	public static final String  DATABASE_STORER_NAME = "database.storer.name";
 	
 	public static final String  DATASPACE_STORER_NAME = "dataspace.storer.name";
 
 	public static final String  SPACER_NAME = "provider.spacer.name";
 
 	public static final String P_PROVIDER_CONFIG = "provider.config.filename";
 
 	public static final String J_PROVIDER_NAME = "name";
 
 	public static final String P_PROVIDR_HOST = "provider.host";
 
 	public static final String P_PROVIDR_ADDRESS = "provider.address";
 
 	public static final String J_PROVIDR_ADDRESS = "providerAddress";
 
 	public static final String J_PROVIDR_HOST = "providerHost";
 
 	public static final String P_PROXY_CLASS = "provider.proxy.class";
 
 	public static final String J_PROXY_CLASS = "providerProxy";
 
 	public final static String SERVER_EXPORTER = "serverExporter";
 
 	public final static String P_EXPORTER_INTERFACE = "provider.exporter.interface";
 		
 	public final static String P_EXPORTER_PORT = "provider.exporter.port";
 
 	public static final String P_ICON_NAME = "provider.icon.name";
 
 	public static final String J_ICON_NAME = "iconName";
 
 	public static final String P_DESCRIPTION = "provider.description";
 
 	public static final String J_DESCRIPTION = "description";
 
 	public static final String P_LOCATION = "provider.location";
 
 	public static final String J_LOCATION = "location";
 
 	public static final String P_INTERFACES = "provider.published.interfaces";
 
 	public static final String J_INTERFACES = "publishedInterfaces";
 	
 	public static final String J_SINGLE_TRHREADED_MODEL = "singleThreadModel";
 
 	public static final String P_CONTEXT_LOGGER = "sorcer.service.context";
 
 	public static final String PRIVATE_CONTEXT_LOGGER = "private.context";
 
 	public static final String PRIVATE_PROVIDER_LOGGER = "private.provider";
 
 	public static final String P_PORTAL_HOST = "provider.portal.host";
 
 	public static final String J_PORTAL_HOST = "portalHost";
 
 	public static final String P_PORTAL_PORT = "provider.portal.port";
 
 	public static final String J_PORTAL_PORT = "portalPort";
 
 	public static final String P_POOL_SIZE = "provider.pool.size";
 
 	public static final String J_POOL_SIZE = "poolSize";
 
 	public static final String P_WORKERS_MAX = "provider.workers.max";
 
 	public static final String J_WORKERS_MAX = "providerWorkersMax";
 
 	public static final String P_DELAY_TIME = "provider.exec.delay";
 
 	public static final String J_DELAY_TIME = "providerExecDelay";
 
 	public static final String P_QOSPOOL_SIZE = "provider.qospool.size";
 
 	public static final String P_QOSWORKERS_MAX = "provider.qosworkers.max";
 
 	// code server, constants with sorcer.* prefix recommended as system
 	// properties
 	public static final String SORCER_WEBSTER_INTERNAL = "webster.internal";
 
 	public static final String SORCER_CODE_SERVER_INTERNAL = "ssb.codeserver.internal";
 
 	public static final String WEBSTER_ROOTS = "webster.roots";
 
 	public static final String J_WEBSTER_HANDLER = "websterHandler";
 
 	public static final String P_WEBSTER_INTERFACE = "provider.webster.interface";
 
 	public static final String R_WEBSTER_INTERFACE = "requester.webster.interface";
 
 	public static final String R_WEBSTER_PORT = "webster.port";
 	
 	public static final String S_WEBSTER_INTERFACE = "webster.interface";
 
 	public static final String J_WEBSTER_INTERFACE = "websterInterface";
 
 	public static final String P_WEBSTER_PORT = "provider.webster.port";
 
 	public static final String S_WEBSTER_PORT = "system.webster.port";
 
 	public static final String J_WEBSTER_PORT = "websterPort";
 
 	public static final String P_WEBSTER_START_PORT = "provider.webster.start.port";
 
 	public static final String J_WEBSTER_START_PORT = "websterStartPort";
 
 	public static final String P_WEBSTER_END_PORT = "provider.webster.end.port";
 
 	public static final String J_WEBSTER_END_PORT = "websterEndPort";
 
     public static final String R_CODEBASE= "requestor.webster.codebase";
 
     public static final String CODEBASE_JARS = "codebase.jars";
 
 	// used by HTTP data server
 	public static final String DATA_SERVER_INTERFACE = "data.server.interface";
 
 	public static final String DATA_SERVER_PORT = "data.server.port";
 
 	public static final String P_DATA_SERVER_INTERFACE = "provider.data.server.interface";
 
 	public static final String P_DATA_SERVER_PORT = "provider.data.server.port";
 
 	public static final String R_DATA_SERVER_INTERFACE = "requestor.data.server.interface";
 
 	public static final String R_DATA_SERVER_PORT = "requestor.data.server.port";
 
 	public static final String DOC_ROOT_DIR = "doc.root.dir";
 
 	public static final String P_DATA_ROOT_DIR = "provider.root.dir";
 
 	public static final String P_DATA_DIR = "provider.data.dir";
 
 	public static final String R_DATA_ROOT_DIR = "requestor.root.dir";
 
 	public static final String R_DATA_DIR = "requestor.data.dir";
 
 	public static final String SCRATCH_DIR = "scratch.dir";
 
 	public static final String P_SCRATCH_DIR = "provider.scratch.dir";
 
 	public static final String R_SCRATCH_DIR = "requestor.scratch.dir";
 
 	public static final String J_SCRATCH_DIR = "scratchDir";
 
 	public static final String P_DATA_LIMIT = "provider.data.limit";
 
 	public static final String J_DATA_LIMIT = "limit";
 
 	/* provider environment */
 	
 	public static final String P_SUFFIX = "sorcer.provider.name.suffix";
 	
 	public static final String P_GROUPS = "provider.groups";
 
 	public static final String J_GROUPS = "providerGroups";
 
 	public static final String P_CATALOGER_NAME = "provider.catalog.name";
 	
 	public static final String J_CATALOG_NAME = "catalogName";
 	
 	public static final String P_SPACE_NAME = "provider.space.name";
 
 	public static final String J_SPACE_NAME = "spaceName";
 
 	public static final String P_SPACE_GROUP = "provider.space.group";
 
 	public static final String J_SPACE_GROUP = "spaceGroup";
 
 	// locators for unicast discovery
 	public static final String P_LOCATORS = "provider.lookup.locators";
 
 	public static final String J_LOCATORS = "locators";
 
 	public static final String MULTICAST_ENABLED = "sorcer.multicast.enabled";
 
 	// persist and reuse service ID
 	public static final String P_SERVICE_ID_PERSISTENT = "provider.id.persistent";
 
 	public static final String J_SERVICE_ID_PERSISTENT = "providerIdPersistent";
 
 	public static final String P_TEMPLATE_MATCH = "provider.template.match";
 
 	public static final String J_TEMPLATE_MATCH = "templateMatch";
 
 	public static final String S_ENV_FIENAME = "sorcer.env";
 
 	public static final String S_SERVICE_ID_FILENAME = "service.id.filename";
 
 	public static final String S_RMI_HOST = "sorcer.rmi.host";
 
 	public static final String S_RMI_PORT = "sorcer.rmi.port";
 
 	public static final String S_PERSISTER_IS_DB_TYPE = "sorcer.is.db.persistent"; // boolean
 
 	public static final String S_IS_DB_ORACLE = "sorcer.is.db.oracle";
 
 	public static final String S_NAME_SUFFIX = "sorcer.provider.name.suffix";
 
 	public static final String S_IS_NAME_SUFFIXED = "sorcer.provider.name.suffixed";
 	
 	public static final String S_PERSISTER_NAME = "sorcer.persister.service";
 
 	public static final String S_JOBBER_NAME = "sorcer.jobber.name";
 
 	public static final String S_SPACER_NAME = "sorcer.spacer.name";
 
 	public static final String S_CATALOGER_NAME = "sorcer.cataloger.name";
 
 	public static final String S_COMMANDER_NAME = "sorcer.commander.name";
 
 	public static final String S_SERVICE_ACCESSOR_PROVIDER_NAME = "provider.lookup.accessor";
 
 	public static final String S_SORCER_REPO = "sorcer.local.repo.location";
 
 	public static final String S_VERSION_SORCER = "v.sorcer";
 
 	/**
 	 * sorcer.env file name (or path)
 	 */
 	String S_KEY_SORCER_ENV = "sorcer.env.file";
 
 	/**
 	 * Webster upload dir
 	 */
 	String S_WEBSTER_TMP_DIR = "webster.tmp.dir";
 
 	/**
 	 * RIO_HOME
 	 */
 	String S_RIO_HOME = "rio.home";
 
 	/**
 	 * RIO_HOME
 	 */
 	String E_RIO_HOME = "RIO_HOME";
 
     String E_WEBSTER_PORT = "SORCER_WEBSTER_PORT";
 
 	/**
 	 * Blitz database directory (Sorcer specific)
 	 */
 	String S_BLITZ_HOME = "sorcer.blitz.home";
 
     String S_WEBSTER_ROOT = "webster.root";
 
 	//public static final String SORCER_HOME = "sorcer.home";
 
 	// discovery and lookup
 
 	public static final String LOOKUP_WAIT = "lookup.wait";
 
 	public static final String LOOKUP_CACHE_ENABLED = "lookup.cache.enabled";
 
 	public static final String LOOKUP_MIN_MATCHES = "lookup.minMatches";
 
 	public static final String LOOKUP_MAX_MATCHES = "lookup.maxMatches";
 
 	/**
 	 * SORCER server side commands
 	 */ 
 	//public static final int SORCER_HOME = 500;
 
 	public static final int AS_PROPS = 501;
 
 	public static final int AS_SESSION = 502;
 
 	public static final int CATALOG_CONTENT = 506;
 
 	public static final int PROVIDER_CONTEXT = 507;
 
 	/**
 	 * SORCER Type commands 1500-1510
 	 */
 	public static final int PERSIST_SORCER_TYPES = 1501;
 
 	/**
 	 * SORCER Notifier commands 1520-1530
 	 */
 	public static final int REGISTER_FOR_NOTIFICATIONS = 1520;
 
 	public static final int GET_NOTIFICATIONS_FOR_SESSION = 1521;
 
 	public static final int ADD_JOB_TO_SESSION = 1522;
 
 	public static final int GET_SESSIONS_FOR_USER = 1523;
 
 	public static final int GET_JOB_NAME_BY_JOB_ID = 1524;
 
 	public static final int GET_TASK_NAME_BY_TASK_ID = 1525;
 
 	public static final int GET_NEW_SERVLET_MESSAGES = 1526;
 
 	public static final int CLEANUP_SESSION = 1527;
 
 	public static final int DELETE_NOTIFICATIONS = 1528;
 
 	public static final int DELETE_SESSION = 1529;
 
 	public static final int PERSIST_SORCER_NAME = 1531;
 
 	public static final int RENAME_SORCER_NAME = 1532;
 
 	/**
 	 * SORCER Notifier Message Indexing Constants used by sorcer.notifier.
 	 * NotificationRetrievalListener* and the launcher
 	 */
 	public static final int MSG_ID = 0;
 
 	public static final int MSG_TYPE = 1;
 
 	public static final int JOB_ID = 2;
 
 	public static final int TASK_ID = 3;
 
 	public static final int MSG_CONTENT = 4;
 
 	public static final int MSG_SOURCE = 5;
 
 	public static final int JOB_NAME = 6;
 
 	public static final int TASK_NAME = 7;
 
 	public static final int CREATION_TIME = 8;
 
 	public static final int IS_NEW = 9;
 
 	public static final int GETALL_DOMAIN_SUB = 601;
 
 	public static final int GET_CONTEXT = 600;
 
 	public static final int GET_CONTEXT_NAMES = 602;
 
 	public static final int PERSIST_CONTEXT = 603;
 
 	public static final int UPDATE_CONTEXT = 605;
 
 	public static final int ADD_DOMAIN = 606;
 
 	public static final int ADD_SUBDOMAIN = 607;
 
 	public static final int UPDATE_DATANODE = 608;
 
 	public static final int REMOVE_DATANODE = 609;
 
 	public static final int REMOVE_CONTEXT = 610;
 
 	public static final int RENAME_CONTEXT = 611;
 
 	public static final int ADD_DATANODE = 613;
 
 	public static final int PERSIST_JOB = 614;
 
 	public static final int DELETE_TASK = 615;
 
 	public static final int ADD_TASK = 616;
 
 	public static final int GET_JOB = 618;
 
 	public static final int GET_JOBDOMAIN = 619;
 
 	public static final int REMOVE_JOB = 620;
 
 	public static final int SAVE_EXERTION_AS_RUNTIME = 623;
 
 	public static final int ADD_TASK_TO_JOB_SAVEAS_RUNTIME = 625;
 
 	public static final int UPDATE_JOB = 629;
 
 	public static final int UPDATE_TASK = 630;
 
 	public static final int GET_JOBNAMES = 632;
 
 	public static final int GET_TASK_NAMES = 634;
 
 	public static final int REMOVE_TASK = 636;
 
 	public static final int SAVEJOB_AS = 637;
 
 	public static final int ADD_TASK_TO_JOB_SAVEAS = 638;
 
 	public static final int GET_TASK = 639;
 
 	public static final int UPDATE_EXERTION = 640;
 
 	public static final int SAVE_TASK_AS = 641;
 
 	public static final int GET_FT = 650;
 
 	/**
 	 * SORCER Notifier Notification Types
 	 */
 	public static final int NOTIFY_FAILURE = 700;
 
 	public static final int NOTIFY_EXCEPTION = 701;
 
 	public static final int NOTIFY_INFORMATION = 702;
 
 	public static final int NOTIFY_WARNING = 703;
 
 	/**
 	 * CMD TO JOBBER
 	 */
 	public static final int STOP_JOB = 710;
 
 	public static final int STOP_TASK = 711;
 
 	public static final int SUSPEND_JOB = 712;
 
 	public static final int RESUME_JOB = 713;
 
 	public static final int STEP_JOB = 714;
 
 	public static final int DROP_EXERTION = 715;
 
 	public static final int SERVICE_EXERTION = 716;
 
 	public static final int GET_RUNTIME_JOBNAMES = 720;
 
 	public static final int GET_RUNTIME_JOB = 721;
 
 	/**
 	 * SORCER service and task states
 	 */
 	// public static final int INITIAL = 1;
 	// public static final int RUNNING = 2;
 	// public static final int DONE = 3;
 	// public static final int SUSPENDED = 4;
 	// public static final int RESUMED = 5;
 	// public static final int HALTED = 6;
 	// public static final int NEXT_STEP = 7;
 	// public static final int ERROR = 0;
 	// public static final int FAIL = -1;
 	// public static final int INVALID_CMD = -2;
 	// public static final int TRANSACTION_ERROR = -3;
 	// public static final int LOCK_ERROR = -4;
 	/**
 	 * SORCER task priorities
 	 */
 	public static final int MIN_PRIORITY = 0;
 
 	public static final int NORMAL_PRIORITY = 5;
 
 	public static final int MAX_PRIORITY = 100;
 
 	/*
 	 * SORCER Method Type
 	 */
 	public static final int Command = 0;
 
 	public static final int Script = 1;
 
 	public static final int Order = 2;
 
 	/**
 	 * SORCER common names
 	 */
 	// final static String BGCOLOR="C0C0C0";
 	final static String BGCOLOR = "FFFFFF";
 
 	final static String SELECT = "Select";
 
 	// SorcerContext Ontology
 	final static String CONTEXT_ATTRIBUTE_VALUES = "values";
 
 	/** context path separator */
 	final static String CPS = "/";
 
 	/**
 	 * context association path separator, for attribute descriptors:
 	 * "result|operation|arg1|arg2", where result is a composite attribute and
 	 * their associations "result|add|3|5" telling that the result is associated
 	 * with the path that references 'add" operation of arguments 3 and 5 in a
 	 * given service context.
 	 */
 	final static String APS = "|";
 
 	final static String PRIVATE = "_";
 
 	// ***Don't change this!***
 	final static String CONTEXT_ATTRIBUTES = PRIVATE + "attributes" + PRIVATE;
 
 	final static String IND = "index";
 
 	final static String OUT_VALUE = "out" + CPS + "value";
 
 	final static String SCRATCH_DIR_KEY = OUT_VALUE + CPS + "scratchDir";
 	final static String SCRATCH_URL_KEY = OUT_VALUE + CPS + "scratchUrl";
 
 	final static String OUT_COMMENT = "out" + CPS + "comment";
 
 	final static String EXCEPTIONS = "exceptions";
 
 	final static String EXCEPTION_OBJ = EXCEPTIONS + CPS + "exception" + IND
 			+ CPS;
 
 	final static String EXCEPTION_ST = EXCEPTIONS + CPS + "stack trace" + IND
 			+ CPS;
 
 	final static String OUT_PATH = "out/path";
 
 	final static String OUT_FILE = "out/filename";
 
 	final static String OUT_SCRIPT = "out/path/script";
 
 	final static String VALUE = "value";
 
 	final static String IN_VALUE = "in" + CPS + "value";
 
 	final static String IN_PATH = "in" + CPS + "path";
 
 	final static String IN_FILE = "in" + CPS + "filename";
 
 	final static String SCRIPT = "script";
 
 	final static String IN_SCRIPT = "in" + CPS + "path" + CPS + "script";
 
 	final static String JOB_TASK = "job" + CPS + "task";
 
 	final static String JOB_STATE = "job" + CPS + "state";
 
 	final static String TASK_PROVIDER = "task" + CPS + "provider";
 
 	final static String EXERTION_PROVIDER = "exertion" + CPS + "provider";
 
 	final static String TRUE = "true";
 
 	final static String FALSE = "false";
 
 	final static String NULL = "NULL";
 
 	final static String SELF_SERVICE = "self/service";
 
 	final static String ANY = "*"; // used for ExertionEnvelop when setting
 
 	// providername in ServiceMethod
 
 	final static String NONE = "none";
 
 	final static String GET = "?";
 
 	/** how long (milliseconds) to wait to discover services */
 	static final long MAX_LOOKUP_WAIT = 6000L;
 
 	// different value_type_code
 	static final int SOC_INTEGER = 0;
 
 	static final int SOC_DOUBLE = 1;
 
 	static final int SOC_BOOLEAN = 2;
 
 	static final int SOC_STRING = 3;
 
 	static final int SOC_DB_OBJECT = 4;
 
 	static final int SOC_LONG = 5;
 
 	static final int SOC_FLOAT = 6;
 
 	// different datatype codes Data_Type_cd
 	// the datatype inside a 'DataNode' object can assume any of these values
 
 	static final int SOC_PRIMITIVE = 10;
 
 	static final int SOC_DATANODE = 11;
 
 	static final int SOC_SERIALIZABLE = 12;
 
 	static final int SOC_CONTEXT_LINK = 13;
 
 	static final int TABLE_NAME = 1;
 
 	// Model aspects
 	static final int MODIFY_LEAFNODE = 0;
 
 	static final int ADD_LEAFNODE = 1;
 
 	/** *********** Static persistence state values ********** */
 	/**
 	 * The meta data of object has been modified either since creation or
 	 * restoring from the data store.
 	 */
 	static public final int META_MODIFIED = 16;
 
 	/**
 	 * The attributes of object has been modified either since creation or
 	 * restoring from the data store.
 	 */
 	static public final int ATTRIBUTE_MODIFIED = 32;
 
 	/** *********** Static persistence scope values ********** */
 	static public final int PRIVATE_SCOPE = 1;
 
 	static public final int SYSTEM_SCOPE = 2;
 
 	static public final int PUBLIC_SCOPE = 4;
 
 	static public final int RUNTIME = 1;
 
 	static public final int NOTRUNTIME = 0;
 
 	static public final String MAIL_SEP = ",";
 
 	static public final String SUBCONTEXT_CONTROL_CONTEXT_STR = "Task Domain";
 
 	// EMPTY LEAF NODE ie. node with nod data and not empty string
 	// final static String EMPTYLEAF = "Enter New Leaf Node";
 
 	static public final String DATANODE_FLAG = "SORCER DATANODE:";
 
 	public static int SELF = 0;
 
 	public static int PROVIDER = 1;
 
 	static final int PREPROCESS = 0;
 
 	static final int PROCESS = 1;
 
 	static final int POSTPROCESS = 2;
 
 	static final int APPEND = 3;
 
 	static final String SPOSTPROCESS = "Postprocess";
 
 	static final String SPREPROCESS = "Preprocess";
 
 	static final String SPROCESS = "Process";
 
 	static final String SAPPEND = "Append";
 
 	/**
 	 * the different types of tasks that are created
 	 */
 	static public final String TASK_COMMAND = "Command";
 
 	static public final String TASK_SCRIPT = "Script";
 
 	static public final String TASK_JOB = "Job";
 
 	/**
 	 * access classes.
 	 */
 	/*
 	 * static public final String PUBLIC = "1"; static public final String
 	 * SENSITIVE = "2"; static public final String CONFIDENTIAL = "3"; static
 	 * public final String SECRET = "4";
 	 */
 	/**
 	 * scratch id's for cache server.
 	 */
 	static public final String SCRATCH_TASKEXERTIONIDS = "taskexertionids";
 
 	static public final String SCRATCH_JOBEXERTIONIDS = "jobexertionids";
 
 	static public final String SCRATCH_METHODIDS = "scratchmethodids";
 
 	static public final String SCRATCH_CONTEXTIDS = "scratchcontextids";
 
 	/**
 	 * All result are stored in control context under ctx node.A abuffer node
 	 * under which all results attached
 	 */
 	static public final String CONTEXT_RESULT = "ctx";
 
 	// Serach criteria names fro context, jobs & tasks
 	static public String OBJECT_NAME = "Name";
 
 	static public String OBJECT_SCOPE = "ScopeCode";
 
 	static public String OBJECT_OWNER = "Owner";
 
 	static public String OBJECT_DOMAIN = "Domain";
 
 	static public String OBJECT_SUBDOMAIN = "SubDomain";
 
 	static public int NEW_JOB_EVT = 1;
 
 	static public int NEW_TASK_EVT = 2;
 
 	static public int NEW_CONTEXT_EVT = 3;
 
 	static public int UPDATE_JOB_EVT = 4;
 
 	static public int UPDATE_TASK_EVT = 5;
 
 	static public int UPDATE_CONTEXT_EVT = 6;
 
 	static public int DELETE_JOB_EVT = 7;
 
 	static public int DELETE_TASK_EVT = 8;
 
 	static public int DELETE_CONTEXT_EVT = 9;
 
 	static public int PERSISTENCE_EVENT = 10;
 
 	static public int CATALOGER_EVENT = 11;
 
 	static public int BROKEN_LINK = 12;
 
 	/**
 	 * For Conditional
 	 */
 	static String C_INCREMENT = "in/conditional/increment/";
 
 	static String C_DECREMENT = "in/conditional/increment/";
 
 	static String PROVIDER_THREAD_GROUP = "sorcer.provider";
 	/**
 	 * SERVME
 	 */
 	static public long LEASE_REFRESH_SERVICER = 60000L;
 
 	static public long LEASE_SIGNING_SERVICER = 30000L;
 
 	// static public String ONDEMAND_PROVISIONER_INTERFACE =
 	// "AdministrableAutonomicProvisioner";
 
 	static public long WAIT_TIME_FOR_PROVISIONER = 9000L;
 
 	static ThreadGroup threadGroup = new ThreadGroup(PROVIDER_THREAD_GROUP);
 
 	// various role names
 	public static final String ANONYMOUS = "anonymous";
 	public static final String ADMIN = "admin";
 	public static final String ROOT = "root";
 	public static String APPROVER = "approver";
 	public static String REVIEWER = "reviewer";
 	public static String PUBLISHER = "publisher";
 	public static String LOOKER = "viewer";
 	public static String ORIGINATOR = "originator";
 	public static String LOGGER = "logger";
 	public static String UPDATER = "updater";
 	public static String ALL = "all";
 	public static String SEED = "ac";
 
 	public static final String SYSTEM = "system";
 	public static final String SERVLET = "servlet";
 	public static final String SYS_LOGIN = "System Login";
 
 	public static final String CUSER = "User";
 	public static final String CGROUP = "Group";
 	public static final String CROLE = "Role";
 	public static final String CPERMISSION = "Permission";
 	public static final String CDOCUMENT = "Document";
 	public static final String CFOLDER = "Folder";
 	public static final String CALL = "All";
 	public static final String CEMAIL = "Email";
 	public static final String FUPLOAD = "FileUpload";
 	public static final String SUPDATE = "ServletUpdate";
 	public static final String CAPPROVAL = "Approval";
 	public static final String CACL = "ACL";
 	public static final String CDRAFT = "Draft";
 	public static final String CVERSION = "Document_Version";
 
 	// Basic permissions used in GAPP ACL.
 	public static final String CADD = "add", CUPDATE = "update",
 			CDELETE = "delete", CREAD = "read", CVIEW = "view";
 
 	final static String SEP = "|";
 	final static String DELIM = ":";
 
 	// user role codes
 	public static final int ANONYMOUS_CD = 1;
 	public static final int VIEWER_CD = 2;
 	public static final int PUBLISHER_CD = 4;
 	public static final int ORIGINATOR_CD = 8;
 	public static final int REVIEWER_CD = 16;
 	public static final int APPROVER_CD = 32;
 	public static final int ADMIN_CD = 64;
 	public static final int ALL_CD = 128;
 	public static final int ROOT_CD = 256;
 
 	final public static int UNMODIFIED = 1;
 
 	/**
 	 * The object is brand new and has not yet been saved to the data store.
 	 */
 	final public static int NEW = 2;
 
 	/**
 	 * The object has been modified either since creation or restoring from the
 	 * data store.
 	 */
 	final public static int MODIFIED = 4;
 
 	/**
 	 * The object has been deleted but is still in the data store.
 	 */
 	final public static int DELETED = 8;
 
 	/**
 	 * The object has been newly attached
 	 */
 	final public static int ATTACHED = 16;
 
 	/**
 	 * access classes.
 	 **/
 	static public final String PUBLIC = "1";
 	static public final String SENSITIVE = "2";
 	static public final String CONFIDENTIAL = "3";
 	static public final String SECRET = "4";
 
 	// For both Objects and principals
 	final public static int ACL_OBJNAME = 0;
 	final public static int ACL_OBJTYPE = 1;
 	final public static int ACL_OBJID = 2;
 
 	final public static int ACL_PNAME = 0;
 	final public static int ACL_PTYPE = 1;
 	final public static int ACL_PID = 2;
 
 	/**
 	 *Keys in ACL Object Representation (Hashtable)
 	 **/
 	final public static String ACL_FOROBJECT = "forobject";
 	final public static String ACL_OWNER = "owner";
 	final public static String ACL_ROLES = "roles";
 	final public static String ACL_PERMISSIONS = "permissions";
 	final public static String ACL_MODE = "mode";
 	final public static String ACL_ID = "id";
 
 	// Private Protocol commands
 	final public static int REDIRECT = 9;
 	final public static int LOGIN = 10;
 	final public static int CHANGEPASSWD = 11;
 	// final public static int CONNECT = 12;
 	// final public static int SETPOOL = 13;
 	// final public static int DSCONNECT = 14;
 	final public static int LOG = 15;
 	final public static int EXECQUERY = 20;
 	final public static int EXECPREPQUERY = 21;
 	final public static int EXECUPDATE = 22;
 	final public static int EXECPREPUPDATE = 23;
 	final public static int EXECCMD = 24;
 	final public static int EXECDEFAULT = 25;
 	final public static int EXECPQUERY = 26;
 	final public static int EXECPROVIDER = 30;
 	final public static int AUTHORIZE = 40;
 	final public static int UPDATE = 50;
 	final public static int SERVLET_UPDATE = 51;
 	final public static int IS_ALIVE = 52;
 	final public static int DO_JOB = 53;
 	final public static int DO_TASK = 54;
 	final public static int EXEC_MANDATE = 55;
 
 	final public static int DISPATCH_CMD = 100;
 
 	/**
 	 * ProtocolStatement default commands number above 500 reserved for
 	 * applications
 	 */
 	final public static int ADD_USER = 101;
 	final public static int DELETE_USER = 102;
 	final public static int UPDATE_USER = 103;
 	final public static int GET_GAPP_PRINCIPAL = 104;
 	final public static int GET_SSO_PRINCIPAL = 105;
 
 	final public static int ADD_GROUP = 111;
 	final public static int DELETE_GROUP = 112;
 	final public static int LOAD_GROUPS = 114;
 	final public static int UPDATE_GROUP = 115;
 
 	final public static int DELETE_DOCUMENT = 122;
 	final public static int MOVE_DOCUMENT = 123;
 	final public static int DELETE_DRAFT = 124;
 	final public static int DELETE_REVIEW = 125;
 	final public static int DELETE_VERSION = 126;
 	final public static int UPDATE_DRAFT = 127;
 	final public static int UPDATE_REVIEW = 128;
 	final public static int UPDATE_VERSION = 129;
 	final public static int ASSIGN_TO_DOC = 130;
 	final public static int MAKE_CURRENT = 131;
 	public static final int GENERATE_HTML = 132;
 
 	public static final int APPROVE_DOC = 135;
 	public static final int VALIDATE_APPROVAL = 136;
 	public static final int VALIDATE_REVIEW = 137;
 
 	final public static int ADD_PERMISSION = 141;
 	final public static int UPDATE_PERMISSION = 142;
 	final public static int DELETE_PERMISSION = 143;
 	final public static int LOAD_PERMISSION = 144;
 
 	final public static int ADD_ROLE = 151;
 	final public static int UPDATE_ROLE = 152;
 	final public static int DELETE_ROLE = 153;
 	final public static int LOAD_ROLES = 154;
 	final public static int GET_ROLES = 155;
 
 	final public static int GET_DRAFT = 161;
 	final public static int GET_DOCUMENT = 162;
 	final public static int MOVE_FOLDER = 163;
 	final public static int DELETE_FOLDER = 164;
 	final public static int DELETE_FOLDERS = 165;
 	final public static int SUBFOLDERS = 166;
 	final public static int SUBSCRIBE_TO_FOLDER = 167;
 	final public static int LOCK_FOLDER = 168;
 	final public static int FREEZE_FOLDER = 169;
 	final public static int OPEN_FOLDER = 170;
 	final public static int ISVIEW_FOLDER = 171;
 
 	final public static int SEND_MAIL = 181;
 
 	final public static int UPLOAD_DOC = 201;
 	final public static int UPLOAD_DRAFT = 202;
 	final public static int UPLOAD_REVIEW = 203;
 	final public static int UPLOAD_ATTACH = 204;
 	final public static int UPLOAD_VERSION = 205;
 	final public static int AUTHORIZE_UPLOAD = 206;
 	final public static int UPLOAD_END = 220;
 
 	final public static int ACL_ISAUTHORIZED = 225;
 	final public static int BUFFER_ACL = 226;
 
 	// final public static int CACHE_ROLES = 230;
 	// final public static int CACHE_GROUPS = 231;
 	// final public static int CACHE_ALL = 232;
 	// final public static int INIT_ROLES = 233;
 	// final public static int INIT_PERMISSIONS = 234;
 
 	final public static int GAPP_CMD_END = 500;
 
 	// use 500-1000 user defined commands with String[] arguments for
 	// Application
 
 	final public static int OBJECT_CMD_START = 1000;
 	final public static int ACL_CMD = 1001;
 	final public static int GET_ACL = 1002;
 	final public static int GET_GAPPACL = 1003;
 
 	final public static int ADD_DOCUMENT = 1120;
 	final public static int UPDATE_DOCUMENT = 1121;
 	// this command involves DocumentDescriptor used by
 	// sorcer.rmi.FileStoreServer
 	final public static int UPLOAD_DESCRIPTOR = 1122;
 	final public static int PREPROCESS_DOCDESC = 1123;
 	final public static int GET_DIRECTORIES = 1124;
 	final public static int LIST_DIRECTORIES = 1125;
 	final public static int LIST_FILES = 1126;
 
 	// For DocumentFileStore
 	final public static int CREATE_DIR = 1127;
 	final public static int DELETE_NODE = 1128; // delete file / folder
 	final public static int GET_PROPERTY = 1129;
 	final public static int SET_PROPERTY = 1130;
 	final public static int PASTE_NODE = 1131;
 	final public static int COPY_NODE = 1132;
 
 	final public static int ADD_FOLDER = 1161;
 	final public static int UPDATE_FOLDER = 1162;
 
 	final public static int STORE_OBJECT = 1170;
 	final public static int RESTORE_OBJECT = 1171;
 
 	// use 1000-1500 user defined GApp commands with Serializable[] arguments
 	// use 1500-2000 user defined GApp commands with Serializable[] arguments for
 	// Applications
 	// use 2000-3000 user defined GApp commands with ServiceSevlet
 	final public static int SERVICE_CMD_START = 2000;
 
 	/**
 	 * Positions in user worksheet
 	 */
 	final public static int STATUS = 0;
 	final public static int ULOGIN = 1;
 	final public static int USSO = 2;
 	final public static int USSOUID = 3;
 	final public static int ULAST = 4;
 	final public static int UFIRST = 5;
 	final public static int UEMAIL = 6;
 	final public static int UPHONE = 7;
 	final public static int UOID = 8;
 	final public static int UPASS = 9;
 	final public static int UROLE = 10;
 	final public static int UACLASS = 11;
 	final public static int UECONTROL = 12;
 
 	/**
 	 * Positions in group worksheet
 	 */
 	final public static int GNAME = 0;
 	final public static int GOLOGIN = 1;
 	final public static int GOLAST = 2;
 	final public static int GOFIRST = 3;
 	final public static int GOEMAIL = 4;
 	final public static int GOPHONE = 5;
 	final public static int GOID = 6;
 
 	/**
 	 * positions of descriptors in a group argument package
 	 */
 	final public static int GROUP = 0;
 	final public static int OWNER = 1;
 	final public static int USERS = 2;
 
 	/**
 	 * Positions in folder worksheet
 	 */
 	final public static int FNAME = 0;
 	final public static int FDESC = 1;
 	final public static int FOWNER = 2;
 	final public static int FPATH = 3;
 	final public static int FOID = 4;
 	final public static int FPARENT = 5;
 	final public static int FACLASS = 6;
 	final public static int FECONTROL = 7;
 
 	/**
 	 * Positions in a document worksheet; the first (zero) position is for STATUS
 	 */
 	final public static int DNAME = 1;
 	final public static int DDESC = 2;
 	final public static int DOWNER = 3;
 	final public static int DLUDATE = 4;
 	final public static int DDDATE = 5;
 	final public static int DWDATE = 6;
 	final public static int DGUDATE = 7;
 	final public static int DOID = 8;
 	final public static int DCVOID = 9;
 	final public static int DCONTEXT = 10;
 	final public static int DCONTEXTOID = 11;
 	final public static int DACLASS = 12;
 	final public static int DECONTROL = 13;
 	final public static int DACL = 14;
 
 	/**
 	 * Positions in a version, draft and review worksheets;
 	 */
 	final public static int XVERSION = 0;
 	final public static int XCOMMENTS = 1;
 	final public static int XOWNER = 2;
 	final public static int XDATE = 3;
 	final public static int XACCESS_NAME = 4;
 	final public static int XOID = 5;
 	final public static int XACLASS = 6;
 	final public static int XECONTROL = 7;
 	final public static int XCONTEXT = 8;
 	final public static int XCONTEXTOID = 9;
 	final public static int XOWNEROID = 10;
 
 	/**
 	 * Positions of descriptors in a document argument package
 	 */
 	final public static int DOC = 0;
 	final public static int DUSER = 1;
 	// document approval group
 	final public static int DAGROUP = 2;
 	// document review group
 	final public static int DRGROUP = 3;
 	final public static int DGROUPS = 4;
 	final public static int DMEMBERS = 5;
 	final public static int DROLES = 6;
 	final public static int DPOSPERMS = 7;
 	final public static int DNEGPERMS = 8;
 
 	/**
 	 * Positions of descriptors in an assign to document argument package
 	 */
 	final public static int ATYPE = 0;
 	final public static int ADRAFT = 1;
 	final public static int ADOC = 2;
 	final public static int AOWNER = 3;
 
 	/**
 	 * positions of descriptors in a document approval package
 	 */
 	public static final int APPROVED_DOC = 0;
 	public static final int APPROVAL_STATUS = 1;
 	public static final int APPROVAL_REJECT_REASON = 2;
 
 	/**
 	 * Positions of descriptors in making a current version
 	 */
 	final public static int CVER = 0;
 	final public static int CDOC = 1;
 	final public static int COWNER = 2;
 
 	/**
 	 * Positions in permission worksheet
 	 */
 	final public static int POPERATION = 0;
 	final public static int PTYPE = 1;
 	final public static int PSIGN = 2;
 	final public static int PID = 3;
 
 	/**
 	 * Positions in role worksheet args
 	 */
 	final public static int ROLE = 0;
 	final public static int RID = 1;
 	final public static int RPERMISSION = 2;
 	final public static int RPERMISSION_OBJTYPE = 3;
 	final public static int RPERMISSION_SIGN = 4;
 	final public static int RPERMISSION_ID = 5;
 
 	/**
 	 * Positions in packing role args
 	 */
 	final public static int RROLE = 0;
 	final public static int ROPERATION = 1;
 	final public static int ROTYPE = 2;
 	final public static int ROID = 3;
 
 	/**
 	 * Positions in mail message array
 	 */
 	final public static int MTO = 0;
 	final public static int MCC = 1;
 	final public static int MBCC = 2;
 	final public static int MSUBJECT = 3;
 	final public static int MTEXT = 4;
 	final public static int MFROM = 5;
 	final public static int MSIZE = 6;
 
 	/**
 	 * Positions in file upload message array
 	 */
 	final public static int FU_FILE = 0;
 	final public static int FU_DESC = 1;
 	final public static int FU_USER = 2;
 	final public static int FU_CONTEXT_ID = 3;
 	final public static int FU_MIME_TYPE = 4;
 	final public static int FU_MODIFIER = 5;
 	final public static int FU_FILE_SIZE = 6;
 	final public static int FU_CLASS_ACCESS = 7;
 	final public static int FU_EXPORT_CONTROL = 8;
 
 	/**
 	 * Positions in servlet update message array
 	 */
 	final public static int U_ACTION = 0;
 	final public static int U_ARG1 = 1;
 	final public static int U_ARG2 = 2;
 	final public static int U_ARG3 = 3;
 
 	/**
 	 * GApp default strings
 	 */
 	public static final String OK = "OK";
 	public static final String QUIT = "Cancel";
 	public static final String CLOSE = "Close";
 	public static final String KILL = "Kill";
 	public static final String YES = "Yes";
 	public static final String NO = "No";
 	public static final String SEND = "Send";
 	public static final String SEARCH = "Search";
 	public static final String CLEAR = "Clear";
 	public static final String SET = "Set";
 	public static final String VALID = "Valid", INVALID = "Invalid";
 	public static final String COMPLETE = "Complete",
 			INCOMPLETE = "Incomplete";
 	public static final String EXPORT_CONTROL = "Export Control",
 			ACCESS_CLASS = "Access Class";
 
 	String CODEBASE_SEPARATOR = " ";
 }
 
 
