 package network;
 
 public class Constants {
 
 	public static final String VERSION = "0";
 	
	// Network
 	public static final String SERVER_ADDRESS = "192.168.0.100";
 	public static final int STANDARD_SERVER_PORT = 13337;
 	public static final long STANDARD_TIME_BETWEEN_UPDATES = 5 * 60 * 1000; // 5 minutes
 	public static final long TIME_BETWEEN_WRITES_TO_DATABASE = 15 * 60 * 1000; // 15 minutes
 	public static final long MINIMUM_INACTIVE_TIME_BEFORE_DISCONNECT = 20 * 60 * 1000; // 20 minutes
	/**
	 * How long the client will wait to receive a response from the server
	 */
	public static final int STANDARD_SOCKET_TIMEOUT = 0; 
	
	
 	public static final String SERVER_LOG_FILE = "TEST_debtlog.txt";
 	
 	public static final String SESSION_TOKEN_REQUEST = "REQUEST";
 }
