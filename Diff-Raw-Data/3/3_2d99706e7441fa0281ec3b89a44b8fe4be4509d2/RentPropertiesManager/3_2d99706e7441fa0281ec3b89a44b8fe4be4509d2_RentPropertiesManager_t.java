 package me.krotn.Rent;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 /**
  * This class provides properties file support to the Rent plugin.<br/>
  * Current properties:<br/>
  * - defaultCost
  * @author karl
  *
  */
 public class RentPropertiesManager {
 	private static final String defaultPropertiesName = "config.properties";
 	private String propertiesName;
 	File propFile;
 	Properties prop;
 	RentLogManager logManager;
 	Hashtable<String,String> defaults = new Hashtable<String,String>();
 	
 	/**
 	 * Constructs a RentPropertiesManager using the default properties file name ("config.properties").
 	 */
 	public RentPropertiesManager(){
 		this(defaultPropertiesName);
 	}
 	
 	/**
 	 * Constructs a RentPropertiesManager using the given properties file name.
 	 * @param propertiesName The name of the properties file to use.
 	 */
 	public RentPropertiesManager(String propertiesName){
 		this.propertiesName = propertiesName;
 		propFile = new File(RentDirectoryManager.getPathInDir(propertiesName));
		if(!fileExists()){
			setup();
		}
 		prop = new Properties();
 		logManager = new RentLogManager(Logger.getLogger("Minecraft"));
 		update();
 		setupDefaults();
 		checkDefaults();
 	}
 	
 	/**
 	 * Puts the default values into the default values hashtable.
 	 */
 	private void setupDefaults(){
 		defaults.put("defaultCost", "10");
 	}
 	
 	/**
 	 * Makes sure that all the default config nodes are in the config file. If any are missing they are added.
 	 */
 	private void checkDefaults(){
 		Enumeration keys = defaults.keys();
 		while(keys.hasMoreElements()){
 			String key = (String) keys.nextElement();
 			if(getProperty(key)==null){
 				setProperty(key,defaults.get(key));
 			}
 		}
 	}
 	
 	/**
 	 * Creates the properties file using the name specified at the construction of the RentPropertiesManager.
 	 */
 	public void setup(){
 		try {
 			propFile.createNewFile();
 		} catch (IOException e) {
 			logManager.severe("Error creating the properties file!");
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Updates the values in the properties cache from the actual properties file.
 	 */
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
 	
 	/**
 	 * Returns the value of the specified properties node.
 	 * @param propertyName The {@code String} requested properties node.
 	 * @return The value of the properties node or {@code null} if the node is unset.
 	 */
 	public String getProperty(String propertyName){
 		return prop.getProperty(propertyName);
 	}
 	
 	/**
 	 * Returns whether or not the properties file specified at the construction of this {@code RentPropertiesManager} exists.
 	 * @return {@code true} the properties file exists {@code false} otherwise.
 	 */
 	public boolean fileExists(){
 		return propFile.exists();
 	}
 	
 	/**
 	 * Sets the value of the specified properties node to the specified value.
 	 * @param propertyName The name of the properties node to set.
 	 * @param propertyValue The value to which the properties node should be set.
 	 */
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
