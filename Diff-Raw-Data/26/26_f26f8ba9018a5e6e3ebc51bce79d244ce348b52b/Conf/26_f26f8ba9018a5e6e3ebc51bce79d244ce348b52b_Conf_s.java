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
		InputStream in = null;
		try {
			in = getClass().getResourceAsStream("/config.properties");
 			prop.load(in);
 		} catch (IOException e) {
			LOGGER.error("Could not read config file", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.error("Could not close config file input stream", e);
				}
			}
 		}
 	}

 }
