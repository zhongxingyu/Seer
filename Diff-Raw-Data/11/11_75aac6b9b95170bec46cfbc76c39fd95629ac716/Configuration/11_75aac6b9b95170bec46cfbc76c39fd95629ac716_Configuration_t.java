 package de.uni.leipzig.IR15.Support;
 
 import java.io.FileInputStream;
 import java.io.BufferedInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Properties;
 
 public class Configuration {
	private static HashMap<String, Configuration> instances = new HashMap<String, Configuration>();
 	private Properties properties = null;
 	
 	private Configuration(String file) {
 		try {
 			BufferedInputStream stream;
 			stream = new BufferedInputStream(new FileInputStream("src/main/resources/" + file + ".properties"));
 			properties = new Properties();
 			properties.load(stream);
 			stream.close();
 		} catch (FileNotFoundException e) {
 			properties = null;
			System.out.println("Configuration not found — please copy, customize and save the existing config/application.properties.example as config/application.properties");
 			e.printStackTrace();
 		} catch (IOException e) {
 			properties = null;
			System.out.println("Configuration read error — please check your config/application.properties");
 			e.printStackTrace();
 		}
 	}
 	
 	public Integer getPropertyAsInteger(String property) {
 		if (properties != null) {
 			return Integer.parseInt(properties.getProperty(property));
 		}
 		return null;
 	}
 	
 	public String getPropertyAsString(String property) {
 		if (properties != null) {
 			return properties.getProperty(property);			
 		}
 		return null;
 	}
 	
 	public static Configuration getInstance(String file) {
 		if (instances.get(file) == null) {
 			instances.put(file, new Configuration(file));
 			return instances.get(file);  
 		}
 		return instances.get(file);
 	}
 }
