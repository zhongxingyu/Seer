 package me.krotn.Rent;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 public class RentPropertiesManager {
 	private static final String defaultPropertiesName = "config.properties";
 	private String propertiesName;
 	File propFile;
 	Properties prop;
 	RentLogManager logManager;
 	
 	public RentPropertiesManager(){
 		this(defaultPropertiesName);
 	}
 	
 	public RentPropertiesManager(String propertiesName){
 		this.propertiesName = propertiesName;
 		propFile = new File(RentDirectoryManager.getPathInDir(propertiesName));
 		prop = new Properties();
 		logManager = new RentLogManager(Logger.getLogger("Minecraft"));
		update();
 		setupDefaults();
 	}
 	
 	private void setupDefaults(){
 		
 	}
 	
 	public void setup(){
 		try {
 			propFile.createNewFile();
 		} catch (IOException e) {
 			logManager.severe("Error creating the properties file!");
 			e.printStackTrace();
 		}
 		setProperty("testProperty","woahTesting");
 	}
 	
 	public void update(){
 		try {
 			FileInputStream in = new FileInputStream(propFile);
 			prop.load(in);
 			in.close();
 		} catch (Exception e) {
 			logManager.severe("Error updating the properties cache!");
 			e.printStackTrace();
 		}
 	}
 	
 	public String getProperty(String propertyName){
 		return prop.getProperty(propertyName);
 	}
 	
 	public boolean fileExists(){
 		return propFile.exists();
 	}
 	
 	public void setProperty(String propertyName,String propertyValue){
 		prop.setProperty(propertyName,propertyValue);
 		try{
 			FileOutputStream out = new FileOutputStream(propFile);
 			prop.store(out, "");
 			out.flush();
 			out.close();
 		}catch(IOException e){
 			logManager.severe("Error saving properties to file!");
 		}
 	}
 }
