 package com.zand.areaguard;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.List;
 import java.util.Properties;
 
 import com.zand.areaguard.area.Storage;
 import com.zand.areaguard.area.cache.CacheStorage;
 import com.zand.areaguard.area.sql.SqlStorage;
 
 public class Config {
 	private static final String config = getConfigDir() + "/areaguard.properties";
 	private static String configDir;
 	public static int createTool;
 	public static int checkTool;
 	public static String[] creators = new String[0];
 	public static String[] defaultRestict = new String[0];
 	
 	public static Storage storage;
 	
 	public static boolean isCreator(String name) {
 		for (String creator : creators)
 			if (creator.equalsIgnoreCase(name))
 				return true;
 		return false;
 	}
 
 	public static void deleteConfig() {
 		System.out.println("Deleating \"" + config + "\"");
 		File f = new File(config);
 		if (f.exists())
 			f.delete();
 	}
 
 	public static boolean readConfig(String file) {
 		Properties props = new Properties();
 
 		try {
 			props.load(new FileInputStream(file));
 		} catch (FileNotFoundException e) {
 			System.err.println("[AreaGuard]: Config file not Found \"" + file
 					+ "\"");
 			return false;
 		} catch (IOException e) {
 			System.err.println("[AreaGuard]: IO Error while loading config \""
 					+ file + "\", " + e.getMessage());
 			return false;
 		}
 		creators = props.getProperty("area-creators").split(" ");
 		createTool = Integer.valueOf(props.getProperty("create-tool"));
 		checkTool = Integer.valueOf(props.getProperty("check-tool"));
 		defaultRestict = props.getProperty("default-restrict").split(" ");
 		
 		// TODO Add Cacheing
 		//String dataStorage = props.getProperty("data-storage").replaceAll("\\W", "").toLowerCase();
 		if (storage instanceof SqlStorage)
 			((SqlStorage)storage).disconnect();
 		
 		String storageType = props.getProperty("area-creators");
 		if (storageType != null) storageType = "com.zand.areaguard.area.sql.SqlStorage";
 		if (storageType.equals("com.zand.areaguard.area.cache.CacheStorage"))
 			storage = new CacheStorage(new SqlStorage(config));
		storage = new SqlStorage(config);
 		System.out.println("[AreaGuard]: Useing Storage Method " + storage.getInfo());
 		
 		return true;
 	}
 	
 	public static String getConfigDir() {
 		// Making a new config class to find the Jar's directory
 		if (configDir == null || configDir.isEmpty()) {
 			try {
 				return Config.class.getProtectionDomain()
 				.getCodeSource()
 				.getLocation()
 				.toURI().resolve("AreaGuard/")
 				.getPath();
 			} catch (URISyntaxException e) {
 				e.printStackTrace();
 			}
 		}
 		return "plugins/AreaGuard";
 	}
 
 	public static boolean setup() {
 		System.out.println("AreaGuard: Setup");
 
 		// Test the config file
 		File f = new File(config);
 		boolean found = f.exists();
 		System.out.println("Config file \"" + config + "\" "
 				+ (found ? "found" : "missing"));
 		if (!found) {
 			try {
 				System.out.println("Creating \"" + config + "\"");
 				List<String> data = JarFile.toList("data/areaguard.properties");
 				
 				(new File(getConfigDir())).mkdir();
 				f.createNewFile();
 				FileWriter w = new FileWriter(config);
 				BufferedWriter out = new BufferedWriter(w);
 				
 				// Write the config data
 				for (String line : data) {
 					out.write(line); 
 					out.newLine();
 				}
 				out.close();
 			} catch (IOException e) {
 				System.err.println("Error: " + e.getMessage());
 			}
 		}
 
 		// read the config
 		readConfig(config);
 
 		return true;
 	}
 }
