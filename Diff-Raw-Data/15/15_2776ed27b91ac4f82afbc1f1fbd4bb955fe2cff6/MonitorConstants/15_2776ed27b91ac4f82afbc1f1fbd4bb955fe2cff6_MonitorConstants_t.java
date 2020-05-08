 package main.java;
 
 
 /**
  * This class is used to define global constants.
  */
 public class MonitorConstants {
 
	public static final int SYS_MONITOR_INTERVAL = 500;
 	public static final int BROKER_HOST = 100;
 	public static final int BROKER_PORT = 100;
 	public static final int BROKER_CLUSTER = 100;
 	public static final int BROKER_DAEMON_NUM = 100;
 
 	public static final String HIST_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
 	
 	public static class ConfigProperties{
 		public static final String MONITOR_INTERVAL = "monitor.interval";
 		public static final String BROKER_HOST = "broker.host";
 		public static final String BROKER_PORT = "broker.port";
 		public static final String BROKER_CLUSTER = "broker.cluster";
 		public static final String BROKER_DAEMON_NUM = "broker.daemon";
 		public static final String BROKER_ENABLE = "broker.enable";
 		public static final String BROKER_TTL = "broker.ttl";
 	}
 }
 
