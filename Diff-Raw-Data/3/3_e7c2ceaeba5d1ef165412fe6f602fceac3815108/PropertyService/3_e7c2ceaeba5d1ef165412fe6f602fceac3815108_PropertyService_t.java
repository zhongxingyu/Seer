 package service;
 
 import javax.xml.bind.PropertyException;
 import java.io.*;
 import java.util.Properties;
 
 public class PropertyService {
 	public static final String FILENAME = "settings.properties";
 	public static final String FILE_DIRECTORY = "filedirectory";
 	private static Properties properties;
 
 	public PropertyService() throws PropertyException{
 		loadProperties();
 	}
 
 	public void saveInProperty(String propertyName, String propertyValue)
 			throws PropertyException {
 		properties.setProperty(propertyName, propertyValue);
 		savePropertyFile();
 	}
 
 	private void savePropertyFile() throws PropertyException {
 		try {
 			FileWriter out = new FileWriter(new File(FILENAME));
 			properties.store(out, "Stored Properties");
 		} catch (FileNotFoundException e) {
 			throw new PropertyException("Storing Properties failed");
 		} catch (IOException e) {
 			throw new PropertyException("Storing Properties failed");
 		}
 	}
 
 	private void loadProperties() throws PropertyException {
 		properties = new Properties();
 		try {
 			properties.load(new FileInputStream(FILENAME));
 		} catch (FileNotFoundException e) {
 			File file = new File(FILENAME);
 			try {
 				file.createNewFile();
 				properties.load(new FileInputStream(FILENAME));
 			} catch (IOException e1) {
 				throw new PropertyException("Properties file can not be loaded");
 			}
 
 		} catch (IOException e) {
 			throw new PropertyException("Properties file can not be loaded");
 		}
 
 	}
 
 	public String getProperty(String key) {
 		return properties.getProperty(key);
 	}
}
