 package ar.edu.itba.pdc.utils;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 public class ConfigurationCommands {
 	private Properties props;
 
 	private static ConfigurationCommands instance;
 
 	public static ConfigurationCommands getInstance() {
 		if (instance == null)
 			instance = new ConfigurationCommands();
 		return instance;
 	}
 
 	private ConfigurationCommands() {
 		this.props = new Properties();
 		try {
			props.load(getClass().getResourceAsStream("/parsedcommands.properties"));
 		} catch (Exception e) {
 			System.out.println("IO Error");
 		}
 	}
 
 	public String getProperty(String property) {
 		if (props.get(property) == null)
 			return "";
 		return props.get(property).toString();
 	}
 
 	public void setProperty(String property, String value) {
 		props.setProperty(property, value);
 	}
 	
 	public boolean hasProperty(String property) {
 		return props.containsKey(property);
 	}
 
 	public void saveFile() {
 		String current = "";
 		FileOutputStream ops = null;
 		try {
 			current = new java.io.File(".").getCanonicalPath();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		try {
 			ops = new FileOutputStream(
 					current
 							+ "/src/main/java/ar/edu/itba/pdc/resources/parsedcommands.properties",
 					false);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		try {
 			props.store(ops, "Commands");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
