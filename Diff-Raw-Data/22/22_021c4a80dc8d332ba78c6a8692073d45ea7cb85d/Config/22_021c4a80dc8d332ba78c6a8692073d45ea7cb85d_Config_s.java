 package common;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import backend.Server;
 import backend.WebPollHandler;
 import db.Database;
 
 /**
  * Handles the parsing of bs-tester.conf and stores
  * all the data in one convenient container
  * @author stevearc
  *
  */
 public class Config {
 	private static Config rootConfig;
 	private static Database rootDB;
 	private static Server rootServer;
 	private static WebPollHandler webPollHandler;
 	
 	/* These constants are for interfacing with the DB */
 	public static final int STATUS_QUEUED = 0;
 	public static final int STATUS_RUNNING = 1;
 	public static final int STATUS_COMPLETE = 2;
 	public static final int STATUS_ERROR = 3;
 	public static final int STATUS_CANCELED = 4;
 
 	/* These options must be changed in source */
 	public long timeout = 300000;
 	
 	/* These options are only specified on the command line */
 	private boolean isServer;
 	public int http_port = 80;
 	public int https_port = 443;
 	public boolean reset_db = false;
 	public String client_tar = "";
 	public String keystore = "";
 
 	/* These options are specified in the battlecode.conf file */
 	/** The password to the keystore */
 	public String keystore_pass = "";
 	/** The directory to place log files */
 	private String log_dir = "/var/log";
 	/** The directory the application is installed into */
 	public String install_dir = "";
 	/** The port number of the server to connect to (or listen on, if running as server) */
 	public int port = 8888;
 	/** The version control program being used (currently supports git and svn) */
 	public String version_control = "";
 	/** CLIENT ONLY: The internet address of the server to connect to */
 	public String server = "";
 	/** CLIENT ONLY: The number of simultaneous matches to run at a time */
 	public int cores = 1;
 	/** SERVER ONLY: The type of database to use */
 	public String db_type = "hsql";
 	/** SERVER ONLY: The name of the database to use */
 	public String db_name = "battlecode";
 	/** SERVER ONLY: The host of the database */
 	public String db_host = "localhost";
 	/** SERVER ONLY: The username to use when connecting to the database. */
 	public String db_user = "battlecode";
 	/** SERVER ONLY: The password to use when connecting to the database. */
 	public String db_pass = "battlepass";
 	/** SERVER ONLY: The cutoff value for a map's area for the map to be considered "small" */
 	public int map_cutoff_small = 1000;
 	/** SERVER ONLY: The cutoff value for a map's area for the map to be considered "medium" */
 	public int map_cutoff_medium = 2000;
 	
 	/* These options are generated from the above options */
 	/** The location of the repository being used */
 	public String repo = "";
 	/** The path to the script that will update the repository */
 	public String cmd_update = "";
 	/** The path to the script that will retrieve the appropriate teams from repository history */
 	public String cmd_grabole = "";
 	/** The path to the script that will generate the proper bc.conf file */
 	public String cmd_gen_conf = "./scripts/gen_conf.sh";
 	/** The path to the script that runs the battlecode match */
 	public String cmd_run_match = "./scripts/run_match.sh";
 	/** The path to the script that will remove the ADMIN and ADMIN_PASS fields from the config file */
 	public String cmd_clean_config_file = "./scripts/clean_config_file.sh";
 	
 	/* These are special case options.  They exist initially but are deleted as soon as they are
 	 * put into the database.  They start in the config file to make setup easier */
 	/** The initial web admin username */
 	public String admin = "";
 	/** The initial web admin password */
 	public String admin_pass = "";
 
 	public Config(boolean isServer) throws IOException {
 		this.isServer = isServer;
 		File file = new File("/etc/bs-tester.conf");
 		if (!file.exists()) {
 			throw new IOException("Config file /etc/bs-tester.conf does not exist.  Make sure you have run setup.sh");
 		}
 		FileInputStream f = new FileInputStream(file);
 		BufferedReader read = new BufferedReader(new InputStreamReader(f));
 		for (String line = read.readLine(); line != null; line = read.readLine()) {
 			int comment = line.indexOf("#");
 			if (comment != -1)
 				line = line.substring(0,comment).trim();
 			if ("".equals(line))
 				continue;
 			String[] data = line.split("=");
 			if (data.length == 2)
 				parse(data[0], data[1]);
 		}
 		validate();
 		setWebPollHandler(new WebPollHandler());
 	}
 
 	public static Config getConfig() {
 		return rootConfig;
 	}
 
 	public static void setConfig(Config c) {
 		rootConfig = c;
 	}
 
 	public static Database getDB() {
 		return rootDB;
 	}
 
 	public static void setDB(Database db) {
 		rootDB = db;
 	}
 
 	public static Server getServer() {
 		return rootServer;
 	}
 
 	public static void setServer(Server s) {
 		rootServer = s;
 	}
 
 	public static WebPollHandler getWebPollHandler() {
 		return webPollHandler;
 	}
 
 	private static void setWebPollHandler(WebPollHandler wph) {
 		webPollHandler = wph;
 	}
 
 	public Logger getLogger() {
 		if (isServer) {
 			Logger logger = Logger.getLogger("Server");
 			logger.setLevel(Level.ALL);
 			try {
 				if (logger.getHandlers().length < 2) {
 					FileHandler server_handler = new FileHandler(log_dir + "/bs-tester.log");
 					server_handler.setFormatter(new SimpleFormatter());
 					logger.addHandler(server_handler);
 				}
 			} catch (SecurityException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return logger;
 		} else {
 			Logger logger = Logger.getLogger("Client");
 			logger.setLevel(Level.ALL);
 			try {
 				if (logger.getHandlers().length < 2) {
 					FileHandler client_handler = new FileHandler(log_dir + "/bs-client.log");
 					client_handler.setFormatter(new SimpleFormatter());
 					logger.addHandler(client_handler);
 				}
 			} catch (SecurityException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return logger;
 		}
 	}
 
 	/**
 	 * Assigns the values of a parameter in the conf file to variables in Config
 	 * @param option
 	 * @param value
 	 */
 	private void parse(String option, String value) {
 		option = option.toLowerCase();
 		if (option.equals("install_dir")) {
 			install_dir = value;
 			client_tar = install_dir + "/client.tar";
 			keystore = install_dir + "/keystore";
 			repo = install_dir + "/repo";
 		}
 		else if (option.equals("keystore_pass")) {
 			keystore_pass = value;
 		}
 		else if (option.equals("admin")) {
 			admin = value;
 		}
 		else if (option.equals("admin_pass")) {
 			admin_pass = value;
 		}
 		else if (option.equals("log_dir")) {
 			log_dir = value;
 		}
 		else if (option.equals("server")) {
 			server = value;
 		}
 		else if (option.equals("port")) {
 			port = Integer.parseInt(value);
 		} 
 		else if (option.equals("map_cutoff_small")) {
 			map_cutoff_small = Integer.parseInt(value);
 		} 
 		else if (option.equals("map_cutoff_medium")) {
 			map_cutoff_medium = Integer.parseInt(value);
 		} 
 		else if (option.equals("db_type")) {
 			db_type = value.toLowerCase();
 		}
 		else if (option.equals("db_name")) {
 			db_name = value;
 		}
 		else if (option.equals("db_host")) {
 			db_host = value;
 		}
 		else if (option.equals("db_user")) {
 			db_user = value;
 		}
 		else if (option.equals("db_pass")) {
 			db_pass = value;
 		}
 		else if (option.equals("version_control")) {
 			version_control = value.toLowerCase();
 			cmd_grabole = "./scripts/" + value + "/grabole.sh";
 			cmd_update = "./scripts/" + value + "/update.sh";
 		}
 		else if (option.equals("cores")) {
 			cores = Integer.parseInt(value);
 		}
 		else if (option.equals("team")) {
 			// This is only used in the commandline scripts
 		}
 		else {
 			System.err.println("Unrecognized option: " + option);
 		}
 	}
 
 	/**
 	 * Check to see if the config values are valid
 	 * @throws InvalidConfigException
 	 */
 	private void validate() throws InvalidConfigException {
 		// Check the server and client values
 		if (!new File(log_dir).exists())
 			throw new InvalidConfigException("Invalid log directory " + log_dir);
 
 		if (!new File(repo).exists())
 			throw new InvalidConfigException("Invalid repository location " + repo);
 
 		if ("".equals(keystore_pass))
 			throw new InvalidConfigException("Keystore password cannot be blank");
 
 		if (!new File(install_dir).exists())
 			throw new InvalidConfigException("Invalid install directory " + install_dir);
 
 		if (port < 1 || port > 65535)
 			throw new InvalidConfigException("Invalid port number " + port);
 
 		if (!version_control.equals("svn") && !version_control.equals("git"))
 			throw new InvalidConfigException("Invalid version control " + version_control);
 
 		// Check the client values
 		if (!isServer) {
 			if (server.equals(""))
 				throw new InvalidConfigException("Invalid server address " + server);
 
 			if (cores < 1) 
 				throw new InvalidConfigException("Invalid number of cores: " + cores);
 		}
 
 		// Check the server values
 		if (isServer) {
 			if (!(db_type.equals("mysql") || db_type.equals("hsql")))
 				throw new InvalidConfigException("Invalid database type: " + db_type);
 
 			if (db_user.equals(""))
 				throw new InvalidConfigException("Invalid database user");
 
 			if (db_pass.equals(""))
 				throw new InvalidConfigException("Must have a non-blank database password");
 
 			if (db_name.equals(""))
 				throw new InvalidConfigException("Must have a non-blank database name");
 
 			if (db_host.equals(""))
 				throw new InvalidConfigException("Must have a valid database host");
 			
 			if (map_cutoff_medium < 0)
 				throw new InvalidConfigException("MAP_CUTOFF_MEDIUM must be positive");
 			
 			if (map_cutoff_small < 0)
 				throw new InvalidConfigException("MAP_CUTOFF_SMALL must be positive");
 			
 			if (map_cutoff_medium <= map_cutoff_small)
 				throw new InvalidConfigException("MAP_CUTOFF_SMALL must be less than MAP_CUTOFF_MEDIUM");
 		}
 
 	}
 
 }
 
 class InvalidConfigException extends IOException {
 	private static final long serialVersionUID = -5660377810876368215L;
 	public InvalidConfigException(String msg) {
 		super (msg);
 	}
 }
