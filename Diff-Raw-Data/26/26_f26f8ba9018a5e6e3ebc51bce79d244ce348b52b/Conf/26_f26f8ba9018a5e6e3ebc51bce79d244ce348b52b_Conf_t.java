 package net.carmgate.morph.conf;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Conf {
 
 	private static final Conf _instance = new Conf();
 	private static final Logger LOGGER = LoggerFactory.getLogger(Conf.class);
 
 	/** Singleton getter. */
 	public static Conf getInstance() {
 		return _instance;
 	}
 
 	public static int getIntProperty(String key) {
 		return Integer.parseInt(_instance.prop.getProperty(key));
 	}
 
 	public static String getProperty(String key) {
 		return _instance.prop.getProperty(key);
 	}
 
 	private final Properties prop;
 
 	private Conf() {
 		prop = new Properties();
		try (InputStream in = getClass().getResourceAsStream("/config.properties")) {
 			prop.load(in);
 		} catch (IOException e) {
			LOGGER.error("Exception raised loading properties", e);
 		}
 	}
 }
