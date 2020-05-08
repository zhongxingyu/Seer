 package gov.usgs.gdp.helper;
 
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 import org.apache.log4j.Logger;
 
 /**
  * Factory class for returning String values for given keys
  * 
  * @author isuftin
  *
  */
 public class PropertyFactory {
 	private static org.apache.log4j.Logger log = Logger.getLogger(PropertyFactory.class);
 	private static Properties properties = null;
 
 	// Static class - can not be instantiated
 	private PropertyFactory() {
 		// Empty private constructor
 	}
 
 	// Returns all the keys available to the application 
 	static public Enumeration<Object> getKeys() {
 		if (properties == null) {
 			log.debug("Loading properties file");
 			try {	
 			loadProperties();
 			} catch (RuntimeException e) {
 				log.error(e.getMessage());
 			}
 			log.debug("Loaded properties file");
 		} 
 		return  properties.keys();
 	}
 	
 	/**
 	 * Get a property from the factory
 	 * 
 	 * @param key
 	 *            A key for the property
 	 * @return a property based on key given, "" if not found
 	 */
 	static public String getProperty(String key) {
 		if (properties == null) {
 			try {
 				log.debug("Loading properties file");
 			} catch (NullPointerException e) {
 				// Do nothing
 			}
 			try {	
 				loadProperties();				
 			} catch (RuntimeException e) {
 				// Do nothing
 			}
 			
 			try {
 				log.debug("Loaded properties file");
 			} catch (NullPointerException e) {
 				// Do nothing
 			}
 		} 
 		String result = (properties.get(key) == null) ? "" : (String) properties.get(key);
 		if (result == null) {
 			// Whoa. Ok, this is weird. Even the file didn't have the property. 
 			// Well, we do what we can. We return "". Not the best, but it will do.
 			log.info("unable to find property for key: " + key);
 			return "";
 		}
 		return result;
 	}
 	
 
 	/**
 	 * Overwrite a property during runtime. 
 	 * @param key
 	 * @param property
 	 */
 	static public void setProperty(String key, String property) {
 		// Well, if there was anything there before, it's been changed. 
 		properties.setProperty(key, property);
 	}
 	
 	/**
 	 * Uses the PropertyLoader class to cleverly load properties file
 	 * 
 	 * @throws RuntimeException
 	 */
 	private static void loadProperties() throws RuntimeException {
 		properties = PropertyLoader.loadProperties("application.properties");
 		if (properties == null) {
 			throw new RuntimeException("Unable to load properties file");
 		}
 	}
 
 	public static List<String> getValueList(String key) {
 		List<String> result = new ArrayList<String>();
 		int index = 0;
 		String valueResult = PropertyFactory.getProperty(key + "." + index);
 		
 		while (!"".equals(valueResult)) {
 			index++;
 			result.add(valueResult);
 			valueResult = PropertyFactory.getProperty(key + "." + index);
 		}
 		
 		return result;
 	}
 }
