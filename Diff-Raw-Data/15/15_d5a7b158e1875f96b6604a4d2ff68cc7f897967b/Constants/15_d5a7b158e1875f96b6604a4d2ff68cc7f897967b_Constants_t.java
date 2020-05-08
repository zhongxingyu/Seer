 package network;
 
 public class Constants {
 
 	public static final String SERVER_ADDRESS = "invert.ed.ntnu.no";
	public static final int STANDARD_SERVER_PORT = 13340;
 	public static final long STANDARD_TIME_BETWEEN_UPDATES = 5 * 60 * 1000; // 5 minutes
 	public static final long TIME_BETWEEN_WRITES_TO_DATABASE = 15 * 60 * 1000; // 15 minutes
 	public static final long MINIMUM_INACTIVE_TIME_BEFORE_DISCONNECT = 20 * 60 * 1000; // 20 minutes
	public static final String SERVER_LOG_FILE = "TEST_debtlog.txt";
 }
