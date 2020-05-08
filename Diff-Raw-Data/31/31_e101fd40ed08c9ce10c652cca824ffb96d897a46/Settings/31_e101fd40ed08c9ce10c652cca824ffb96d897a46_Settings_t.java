 package ntnu.it1901.gruppe4.db;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Static class for managing program settings
  * 
  * @author Lars Kinn Ekroll
  */
 public class Settings {
 	/*
 	 * Configuration key strings are set here, for convenience
 	 */
 	private static final String DELIVERY_FEE = "DELIVERY_FEE";
 	private static final String FREE_DELIVERY_LIMIT = "FREE_DELIVERY_LIMIT";
 	private static final String DB_VERSION = "DB_VERSION";
 
 	/**
 	 * A map of default settings, to be used when initializing the database
 	 */
 	public static final Map<String, String> DEFAULT_SETTINGS;
 	static {
 		Map<String, String> m = new HashMap<String, String>();
 		// Put default configuration key/values here
 		m.put(DELIVERY_FEE, "20");
 		m.put(FREE_DELIVERY_LIMIT, "500");
 		m.put(DB_VERSION, "2"); // DB Version can only be set here!
 		DEFAULT_SETTINGS = Collections.unmodifiableMap(m);
 	}
	
	private static String getConfig(String key) {
		String result = DataAPI.getConfig(key);
		if (result == null) {
			return DEFAULT_SETTINGS.get(key);
		}
		return result;
	}
 	/**
 	 * @return the sum an order has to exceed to qualify for free delivery
 	 */
 	public static float getFreeDeliveryLimit() {
		return Float.parseFloat(getConfig(FREE_DELIVERY_LIMIT));
 	}
 
 	/**
 	 * @param limit
 	 *            the sum an order has to exceed to qualify for free delivery
 	 */
 	public static void setFreeDeliveryLimit(float limit) {
 		DataAPI.setConfig(FREE_DELIVERY_LIMIT, Float.toString(limit));
 	}
 	
 	/**
 	 * @return the price of delivery
 	 */
 	public static float getDeliveryFee() {
		return Float.parseFloat(getConfig(DELIVERY_FEE));
 	}
 	
 	/**
 	 * @param fee
 	 *            the price of delivery
 	 */
 	public static void setDeliveryFee(float fee) {
 		DataAPI.setConfig(DELIVERY_FEE, Float.toString(fee));
 	}
 
 	/**
 	 * @return the database version number
 	 */
 	public static int getDBVersion() {
		return Integer.parseInt(getConfig(DB_VERSION));
 	}
 }
