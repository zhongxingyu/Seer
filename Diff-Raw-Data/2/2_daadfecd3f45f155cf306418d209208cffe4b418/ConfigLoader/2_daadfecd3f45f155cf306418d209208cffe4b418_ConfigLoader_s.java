 package ca.wacos;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.LinkedHashMap;
 import java.util.Scanner;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ConfigLoader {
 
 	private static final String PREFIX = "[NAMETAG CONFIG] ";
 	
 	public static LinkedHashMap<String, LinkedHashMap<String, String>> load(JavaPlugin plugin) {
 		String folder = "plugins/" + plugin.getName();
 		File folderFile = new File(folder);
 		if (!folderFile.exists()) {
 			folderFile.mkdir();
 		}
 		String path = "plugins/" + plugin.getName() + "/config.txt";
 		File source = new File(path);
 		if (source.exists()) {
 			return loadConfig(source);
 		}
 		else {
 			try {
 				source.createNewFile();
 			} catch (IOException e) {
 				print("Failed to create config file: ");
 				e.printStackTrace();
 			}
 			return generateConfig(source);
 		}
 	}
 	private static LinkedHashMap<String, LinkedHashMap<String, String>> generateConfig(File target) {
 		PrintWriter out = null;
 		try {
 			out = new PrintWriter(target);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		out.println("// This file contains several configurable properties for this plugin.");
 		out.println("// You may delete this file and reload the plugin / server to re-generate this file.");
 		out.println("");
 		out.println("// If enabled, this will remove any formatting created in the death messages by prefixes / suffixes");
 		out.println("// Do not set this to true if you already have a plugin that changes death messages");
 		out.println("death-message-mask enabled = false");
 		out.println("");
 		out.println("// If enabled, this will remove any formatting created in the tab list by prefixes / suffixes");
 		out.println("// Do not set this to true if you already have a plugin that changes the tab list");
 		out.println("tab-list-mask enabled = false");
 		
 		out.close();
 		
 		return loadConfig(target);
 	}
 	private static LinkedHashMap<String, LinkedHashMap<String, String>> loadConfig(File source) {
 		Scanner in = null;
 		try {
 			in = new Scanner(source);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		LinkedHashMap<String, LinkedHashMap<String, String>> map = new LinkedHashMap<String, LinkedHashMap<String, String>>();
 
 		
 		boolean syntaxError = false;
 		
 		while (in.hasNext()) {
 			String line = in.nextLine();
 			if (!line.trim().startsWith("//") && !line.isEmpty()) {
 				
 				syntaxError = checkWords(line);
 				if (syntaxError) {
 					print("Error in syntax, not enough elements on line!");
 				}
 				
 				Scanner lineScanner = new Scanner(line);
 				
 				String node = lineScanner.next();
 				String operation = lineScanner.next();
 				String equals = lineScanner.next();
 				if (!equals.trim().equals("=")) {
 					print("Error in syntax, \"=\" expected at third element, \"" + equals + "\" given.");
 					syntaxError = true;
 					break;
 				}
 				String value = lineScanner.nextLine().trim();
 				
 				LinkedHashMap<String, String> entry = new LinkedHashMap<String,String>();
 				
 				if (map.get(node) != null) {
 					entry = map.get(node);
 				}
 				
 				entry.put(operation.toLowerCase(), value);
 				
 				if (map.get(node) == null) {
 					map.put(node, entry);
 				}
 				lineScanner.close();
 			}
 		}
 		in.close();
 		
 		if (syntaxError)
 			return null;
 		return map;
 	}
 	private static void print(String p) {
 		System.out.println(PREFIX + p);
 	}
 	private static void printDebug(String p) {
		if (DEBUG)
 			System.out.println(PREFIX + p);
 	}
 	private static boolean checkWords(String line) {
 		int count = 0;
 		Scanner reader = new Scanner(line);
 		while (reader.hasNext()) {
 			count++;
 			reader.next();
 		}
 		reader.close();
 		if (count >= 4)
 			return false;
 		else return true;
 	}
 	public static boolean parseBoolean(String name, String operation, LinkedHashMap<String, LinkedHashMap<String, String>> config, boolean d) {
 		if (config.containsKey(name) && config.get(name).containsKey(operation)) {
 			String value = config.get(name).get(operation);
 			if (value.equalsIgnoreCase("true")) {
 				return true;
 			}
 			else if (value.equalsIgnoreCase("false")) {
 				return false;
 			}
 			System.out.println("[NametagEdit] Could not parse boolean for \"" + name + " " + operation + "\" in config.txt, value given: " + value + ", defaulting to " + d);
 		}
 		else {
 			System.out.println("[NametagEdit] Value does not exist for \"" + name + " " + operation + "\" in config.txt, defaulting to " + d);
 		}
 		return d;
 	}
 }
