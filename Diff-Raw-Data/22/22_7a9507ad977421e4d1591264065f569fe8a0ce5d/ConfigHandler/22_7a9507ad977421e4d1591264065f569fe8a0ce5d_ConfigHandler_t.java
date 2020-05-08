 package ch.zhaw.jroute.config;
 
 import java.io.IOException;
import java.io.InputStream;
 import java.util.Properties;
 
 public class ConfigHandler {
 	Properties properties;
 
 	public ConfigHandler() {
 		properties = new Properties();
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("jroute.properties");

		if (inputStream == null) {
			throw new RuntimeException("property file jroute.properties"
					+ "' not found in the classpath");
		}
 
 		try {
			properties.load(inputStream);
 		} catch (IOException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public String getConfig(String key) {
 
 		String value = properties.getProperty(key);
 		return value;
 
 	}
 
 	public void setConfig(String key, String value) {
 		properties.setProperty(key, value);
 	}
 
 }
