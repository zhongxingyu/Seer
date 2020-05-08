 package vphshare.driservice.providers;
 
 import java.io.IOException;
 import java.util.Properties;
 
 
 
 public class PropertiesProvider {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PropertiesProvider.class);
 
 	private static final Properties properties = new Properties();
 	
 	private PropertiesProvider() {}
 	
 	static {
 		try {
 			properties.load(PropertiesProvider.class.getClassLoader().getResourceAsStream("config.properties"));
 		} catch (IOException e) {
			logger.fatal("Unable to read properties configuration file", e);
            throw new RuntimeException(e);
 		}
 	}
 	
 	public static Properties getProperties() {
 		return properties;
 	}
 }
