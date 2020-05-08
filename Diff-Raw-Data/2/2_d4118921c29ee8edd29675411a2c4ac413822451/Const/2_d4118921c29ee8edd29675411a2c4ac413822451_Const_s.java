 package jp.tkgktyk.wimaxhelperforaterm;
 
 import java.util.UUID;
 
 public class Const {
 	public static final String ATERM_BASIC_USERNAME = "smart-user";
 	public static final String ATERM_BASIC_PASSWORD = "smart-user";
 	public static final String ATERM_CMD_GET_INFO = "info_remote_main";
 	public static final String ATERM_CMD_STANDBY_BT = "info_remote_main_btstandby";
 	public static final String ATERM_CMD_STANDBY = "info_remote_main_standby";
 	public static final String ATERM_CMD_REBOOT = "info_remote_main_reboot";
 	public static final String ATERM_DEFAULT_HOST_NAME = "aterm.me";
 	public static final int ATERM_PORT = 80;
 
 	public static final String USER_AGENT = "Android";
 	// UUID for Bluetooth SPP profile 
 	public static final UUID BLUETOOTH_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 	
 	// minimum interval to update information
	public static final long UPDATE_INTERVAL_IN_MILLIS = 1*60*1000; // 10 minutes
 }
