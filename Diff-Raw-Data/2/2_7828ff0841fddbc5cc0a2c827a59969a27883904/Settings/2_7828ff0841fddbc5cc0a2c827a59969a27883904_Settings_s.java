 package btwmods.io;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 public class Settings {
 	
 	public static Settings readSettings(File file) throws IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 		Map settings = new LinkedHashMap<String, String>();
 
 		String section = "";
 		String line;
 		int equalsIndex;
 		while ((line = reader.readLine()) != null) {
 			// Trim leading whitespace.
 			line = line.replaceAll("^[ \t]+", "");
 			
 			if (!line.startsWith("#")) {
 				if (line.startsWith("[") && line.trim().endsWith("]")) {
 					section = line.trim();
 				}
 				else if ((equalsIndex = line.indexOf('=')) >= 0) {
					settings.put(line.substring(0, equalsIndex).toLowerCase(), line.substring(equalsIndex + 1));
 				}
 			}
 		}
 		reader.close();
 		
 		return new Settings(settings);
 	}
 	
 	public final Map<String, String> settings;
 	
 	public Settings() {
 		this(new HashMap<String, String>());
 	}
 	
 	public Settings(Map<String, String> settings) {
 		this.settings = Collections.unmodifiableMap(settings);
 	}
 	
 	public boolean isBoolean(String key) {
 		if (settings.containsKey(key)) {
 			String setting = settings.get(key).trim().toLowerCase();
 			return setting.equalsIgnoreCase("yes") || setting.equalsIgnoreCase("true")|| setting.equalsIgnoreCase("1") || setting.equalsIgnoreCase("on")
 					 || setting.equalsIgnoreCase("no") || setting.equalsIgnoreCase("false") || setting.equalsIgnoreCase("0") || setting.equalsIgnoreCase("off");
 		}
 		return false;
 	}
 	
 	public boolean getBoolean(String key) {
 		if (!isBoolean(key)) throw new IllegalArgumentException("setting is not a valid boolean. check with isBoolean() first");
 		String setting = settings.get(key).trim().toLowerCase();
 		return setting.equalsIgnoreCase("yes") || setting.equalsIgnoreCase("true") || setting.equalsIgnoreCase("1") || setting.equalsIgnoreCase("on");
 	}
 	
 	public boolean isInt(String key) {
 		try { return settings.containsKey(key) && Integer.valueOf(settings.get(key)) != null; }
 		catch (NumberFormatException e) { return false; }
 	}
 	
 	public int getInt(String key) {
 		if (!isInt(key)) throw new IllegalArgumentException("setting is not a valid Integer. check with isInt() first");
 		return Integer.parseInt(settings.get(key));
 	}
 	
 	public boolean isLong(String key) {
 		try { return settings.containsKey(key) && Long.valueOf(settings.get(key)) != null; }
 		catch (NumberFormatException e) { return false; }
 	}
 	
 	public long getLong(String key) {
 		if (!isLong(key)) throw new IllegalArgumentException("setting is not a valid Long. check with isLong() first");
 		return Long.parseLong(settings.get(key));
 	}
 	
 	public boolean hasKey(String key) {
 		return settings.containsKey(key);
 	}
 	
 	public String get(String key) {
 		return settings.get(key);
 	}
 }
