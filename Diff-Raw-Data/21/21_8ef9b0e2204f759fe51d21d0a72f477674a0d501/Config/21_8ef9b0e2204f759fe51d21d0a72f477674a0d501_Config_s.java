 package sw.server.simulator;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * Handles configuration for the simulator
  * 
  * @author Lars Kristian
  * 
  */
 public class Config {
 
 	public static String filename = "simulator.properties";
 
 	public static String USER_PREFIX = "user";
 	public static String USER_PASSWORD = "pw";
 
	public static int NUM_PRODUCERS = 1;
 	public static int NUM_SHEEP = 1000;
 	public static int DAILY_UPDATES = 3;
 
 	public static boolean DEBUG = false;
 
 	public static double LAT_MIN = 62.5;
 	public static double LAT_MAX = 63;
 
 	public static double LONG_MIN = 10.5;
 	public static double LONG_MAX = 11;
 
 	public static void load() {
 		Properties props = new Properties();
 		try {
 			File file = new File(filename);
 			props.load(new FileInputStream(file));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		USER_PREFIX = props.getProperty("user_prefix");
 		USER_PASSWORD = props.getProperty("user_password");
		NUM_PRODUCERS = Integer.valueOf(props.getProperty("num_producers"));
		NUM_SHEEP = Integer.valueOf(props.getProperty("num_sheep"));
 		DAILY_UPDATES = Integer.valueOf(props.getProperty("daily_updates"));
 		LAT_MIN = Double.valueOf(props.getProperty("lat_min"));
 		LAT_MAX = Double.valueOf(props.getProperty("lat_max"));
 		LONG_MIN = Double.valueOf(props.getProperty("long_min"));
 		LONG_MAX = Double.valueOf(props.getProperty("long_max"));
 		DEBUG = Boolean.valueOf(props.getProperty("debug"));
 	}
 
 }
