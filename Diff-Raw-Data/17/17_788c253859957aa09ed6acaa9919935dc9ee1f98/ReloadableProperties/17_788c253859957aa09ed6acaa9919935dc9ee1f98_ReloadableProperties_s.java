 package configuration;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.util.Date;
 import java.util.Properties;
 
 public class ReloadableProperties {
 
 	private Properties properties = new Properties();
	private Long lastLoadTime;
 	private String filename;
 	private static final int DEFAULT_EXPIRATION_TIME = 300000;
 	private static final String EXPIRATION_TIME_KEY = "expirationTime";
 	
 	/**
 	 * @param lastLoadTime 
 	 * @param filename     The filename-
 	 */
	public ReloadableProperties(long lastLoadTime,
			String filename) throws FileNotFoundException, IOException {
 		super();
		this.lastLoadTime = lastLoadTime;
 		this.filename = filename;
 		
 		loadProperties();
 	}
 	
 	/**
 	 * Searches for the property with the specified key in this property list. 
 	 * If the key is not found in this property list, the default property list, 
 	 * and its defaults, recursively, are then checked. 
 	 * The method returns null if the property is not found. 
 	 * 
 	 * @param key The property key. 
 	 * @return the value in this property list with the specified key value.
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	public String get(String key) throws FileNotFoundException, IOException {
		Long now = new Date().getTime();
		if (now - lastLoadTime > getExpirationTime()) {
 			loadProperties();
 		}
 		
 		return properties.getProperty(key);
 	}
 	
 	/**
 	 * Loads the properties file.
 	 * 
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private void loadProperties() throws FileNotFoundException, IOException {
 		properties.load(new FileInputStream(filename));
 	}
 	
 	/**
 	 * Retrieves the expiration time.
 	 * 
 	 * @return the expiration time.
 	 */
 	private int getExpirationTime() {
 		Integer time = Integer.valueOf(properties.getProperty(EXPIRATION_TIME_KEY));
 		
 		if (time == null) {
 			return DEFAULT_EXPIRATION_TIME;
 		}
 		
 		return time;
 	}
 	
 }
