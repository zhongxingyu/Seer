 package common;
 
 
 
 /**
  * Stores configuration data in one convenient container
  * @author stevearc
  *
  */
 public class Config {
	public static final String VERSION = "0.1.1";
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
 
 	/* These options are generated from the above options */
 	/** The path to the script that will generate the proper bc.conf file */
 	public static final String cmd_gen_conf = "./scripts/gen_conf.sh";
 	/** The path to the script that will rename the team in the source */
 	public static final String cmd_rename_team = "./scripts/rename_team.sh";
 	/** The path to the script that runs the battlecode match */
 	public static final String cmd_run_match = "./scripts/run_match.sh";
 
 }
