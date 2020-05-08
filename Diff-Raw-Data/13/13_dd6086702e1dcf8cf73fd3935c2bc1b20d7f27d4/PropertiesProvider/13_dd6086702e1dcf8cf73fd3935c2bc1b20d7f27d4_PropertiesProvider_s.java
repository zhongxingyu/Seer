 package vphshare.driservice.providers;
 
import static java.util.logging.Level.SEVERE;

 import java.io.IOException;
 import java.util.Properties;
import java.util.logging.Logger;
 
 
 
 public class PropertiesProvider {
	
	private static final Logger LOG = Logger.getLogger(PropertiesProvider.class.getName());
 
 	private static final Properties properties = new Properties();
 	
 	private PropertiesProvider() {}
 	
 	static {
 		try {
 			properties.load(PropertiesProvider.class.getClassLoader().getResourceAsStream("config.properties"));
 		} catch (IOException e) {
			LOG.log(SEVERE, "Unable to read properties configuration file", e);
 		}
 	}
 	
 	public static Properties getProperties() {
 		return properties;
 	}
 }
