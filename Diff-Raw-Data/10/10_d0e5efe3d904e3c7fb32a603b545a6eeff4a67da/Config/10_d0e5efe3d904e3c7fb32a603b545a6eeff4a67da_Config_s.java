 package common;
 
 import java.io.File;
 
 
 
 /**
  * Stores configuration data in one convenient container
  * @author stevearc
  *
  */
 public class Config {
	public static final String VERSION = "1.0.0";
 	public static final int RESTART_STATUS = 121;
 	public static final boolean DEBUG = false;
 	public static final boolean SHOW_SQL = false;
 	public static final boolean PRINT_WORKER_OUTPUT = true;
 	public static boolean MOCK_WORKER = false;
 	public static int MOCK_WORKER_SLEEP = 0;
 	
 	// Defaults
 	public static final int DEFAULT_HTTP_PORT = 80;
 	public static final int DEFAULT_DATA_PORT = 8888;
 
 	/** MASTER ONLY: The cutoff value for a map's area for the map to be considered "small" */
 	public static int map_cutoff_small = 1400;
 	/** MASTER ONLY: The cutoff value for a map's area for the map to be considered "medium" */
 	public static int map_cutoff_medium = 2400;
 
 	// Static file locations
 	public static final String matchDir = "static" + File.separator + "matches" + File.separator;
 	public static final String scrimmageDir = "static" + File.separator + "scrimmages" + File.separator;
 	public static final String mapsDir = "maps" + File.separator;
 	public static final String teamsDir = "teams" + File.separator;
 	public static final String libDir = "lib" + File.separator;
 	public static final String logDir = "log" + File.separator;
 	public static final String battlecodeServerFile = libDir + "battlecode-server.jar";
 	public static final String allowedPackagesFile = "AllowedPackages.txt";
 	public static final String disallowedClassesFile = "DisallowedClasses.txt";
 	public static final String methodCostsFile = "MethodCosts.txt";
 	
 }
