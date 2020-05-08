 package network;
 
 import versioning.PrereleaseVersion;
 import versioning.Version;
 
 public class Constants {
 	
 	// Versions
 														// v0.0.0.0 PRE-RELEASE
 	public static final Version SERVER_VERSION = new PrereleaseVersion(0, 0, 0, 1);
 //	public static final Version ANDROID_VERSION = new PrereleaseVersion(0, 0, 0, 0);
 //	public static final Version CONSOLE_VERSION = new PrereleaseVersion(-1, 0, 0, 0);
 //	public static final Version SWING_VERSION = new PrereleaseVersion(-2, 0, 0, 0);
 	
 	// Network
//	public static final String SERVER_ADDRESS = "192.168.0.100";
	public static final String SERVER_ADDRESS = "invert.ed.ntnu.no";
 	public static final int STANDARD_SERVER_PORT = 13337;
	public static final long STANDARD_TIME_BETWEEN_UPDATES = 5 * 60 * 1000; // 5 minutes !!!!FIXME!!!! NOT SUPPOSED TO GO LIVE WITH 1 MINUTE!!!
 	public static final long TIME_BETWEEN_WRITES_TO_DATABASE = 15 * 60 * 1000; // 15 minutes
 	/**
 	 * For how long a session token will remain active and can receive updates
 	 */
 //	public static final long MINIMUM_INACTIVE_TIME_BEFORE_DISCONNECT = 10000;  // 20 minutes FIXME
 	public static final long MINIMUM_INACTIVE_TIME_BEFORE_DISCONNECT = 20 * 60 * 1000; // 20 minutes
 	/**
 	 * How long the client will wait to receive a response from the server
 	 */
 	public static final int STANDARD_SOCKET_RECEIVE_TIMEOUT = 0; 
 	
 	public static final int STANDARD_SOCKET_CONNECT_TIMEOUT = 10 * 1000; // 10 seconds
 	
 	public static final String SERVER_LOG_FILE = "TEST_debtlog.txt";
 	
 	public static final String SESSION_TOKEN_REQUEST = "REQUEST";
 	
 	public static final boolean ANDROID_DEBUG_MODE = true;
 	
 	public static final boolean STANDARD_DISABLE_UPDATES_WHEN_NOT_ON_WIFI = false;
 
 	public static final String SESSION_EXPIRED = "session-expired";
 	
 	public static final String NEW_VERSION_URL = "http://github.com/stianwe/DebtList";
 }
