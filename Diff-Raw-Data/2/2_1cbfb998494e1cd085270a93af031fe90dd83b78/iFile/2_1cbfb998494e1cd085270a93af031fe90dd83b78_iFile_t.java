 package com.github.desmaster.Devio.util;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Properties;
 
 import com.github.desmaster.Devio.Devio;
 import com.github.desmaster.Devio.cons.Console;
 
 public class iFile {
 
	private String location = System.getProperty("user.home") + "\\Local Settings\\Application Data\\.Devio";
 	public static Properties config;
 
 	public static int WIDTH;
 	public static int HEIGHT;
 
 	public void save() {
 
 	}
 
 	public void saveConfig() {
 		File folder = getDataFolder();
 
 		if (!folder.exists()) {
 			folder.mkdir();
 		}
 
 		File config = new File(folder, "config.cfg");
 
 		if (!config.exists()) {
 			try {
 				config.createNewFile();
 				Console.log("Created config.cfg!");
 				FileWriter fw = new FileWriter(config);
 				BufferedWriter out = new BufferedWriter(fw);
 				out.write("Width: " + "\n");
 				out.write("Height: " + "\n");
 				out.close();
 				Console.log("Saved config.cfg!");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void load() {
 
 	}
 
 	public void loadConfig() {
 		File file = getDataFolder();
 		File configFile = new File(file, "config.cfg");
 		String filename = getDataFolder() + "/config.cfg";
 		config = new Properties();
 
 		while (!configFile.exists()) {
 			saveConfig();
 		}
 
 		try {
 			config.load(new FileInputStream(filename));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		WIDTH = Integer.parseInt(config.getProperty("Width"));
 		HEIGHT = Integer.parseInt(config.getProperty("Height"));
 
 		setGameWidth(WIDTH);
 		setGameHeight(HEIGHT);
 		Console.log("Loaded configurations");
 	}
 
 	public void setGameWidth(final int WIDTH) {
 		Devio.WIDTH = WIDTH;
 	}
 
 	public void setGameHeight(final int HEIGHT) {
 		Devio.HEIGHT = HEIGHT;
 	}
 
 	private File getDataFolder() {
 		File file = new File(location);
 		return file;
 	}
 
 }
