 package ch.zhaw.jroute.config;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 public class ConfigHandler {
 	Properties properties;
 
 	public ConfigHandler() {
 		properties = new Properties();
 
 		try {
			String configFile = new File("src/main/resources/jroute.properties")
					.getAbsolutePath();
			properties.load(new FileInputStream(configFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
 		} catch (IOException e) {
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
