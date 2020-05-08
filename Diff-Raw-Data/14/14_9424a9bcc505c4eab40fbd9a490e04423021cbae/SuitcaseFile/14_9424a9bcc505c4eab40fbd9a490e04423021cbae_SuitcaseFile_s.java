 package me.eighth.suitcase.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Map;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import me.eighth.suitcase.Suitcase;
 import me.eighth.suitcase.util.SuitcaseConsole.actionType;
 
 public class SuitcaseFile {
 
 	private Suitcase plugin;
 	
 	public SuitcaseFile(Suitcase plugin) {
 		this.plugin = plugin;
 	}
 	
 	public boolean loadFile(String filename, Map<String, Object> defaults) {
 		
 		// get given File and FileConfiguration
 		File file = getFile(filename);
 		FileConfiguration fileConfig;
 		
 		if (file != null) {
 			fileConfig = YamlConfiguration.loadConfiguration(file);
 			if (fileConfig.getKeys(true).size() == 0) {
				
 			}
 			for (int i = 0; i < defaults.size(); i++) {
 				String path = defaults.keySet().toArray()[i].toString();
 				// add property if missing
 				if (!fileConfig.contains(path)) {
 					// set missing property and log to console if file wasn't empty
 					fileConfig.set(path, defaults.get(path));
					if (i >= fileConfig.getKeys(true).size()) {
 						plugin.console.sendAction(actionType.PROPERTY_MISSING, new ArrayList<String>(Arrays.asList(path, filename, defaults.get(path).toString())));
 					}
 				}
 				// compare object types
 				else if (fileConfig.get(path).getClass() != defaults.get(path).getClass()) {
 					// reset value and log to console
 					fileConfig.set(path, defaults.get(path));
 					plugin.console.sendAction(actionType.PROPERTY_BAD_TYPE, new ArrayList<String>(Arrays.asList(path, filename, fileConfig.get(path).getClass().toString(), defaults.get(path).getClass().toString())));
 				}
 			}
 			// save and use verified keys
 			try {
 				saveFile(file, fileConfig);
 				return true;
 			} catch (IOException e) {
 				plugin.console.sendAction(actionType.FILE_SAVE_ERROR, new ArrayList<String>(Arrays.asList(filename, e.toString())));
 				return false;
 			}
 		}
 		else {
 			plugin.console.sendAction(actionType.FILE_SAVE_ERROR, new ArrayList<String>(Arrays.asList(filename, "fileNullError")));
 			return false;
 		}
 	}
 
 	private File getFile(String filename) {
 		File file = new File("plugins/Suitcase/" + filename);
 		if (!file.exists()) {
 			try {
 				plugin.console.sendAction(actionType.FILE_NOT_FOUND, new ArrayList<String>(Arrays.asList(filename)));
 				new File("plugins/Suitcase").mkdir();
 				file.createNewFile();
 			} catch (IOException e) {
 				plugin.console.sendAction(actionType.FILE_SAVE_ERROR, new ArrayList<String>(Arrays.asList(filename, e.toString())));
 				plugin.disable(plugin);
 				return null;
 			}
 		}
 		return file;
 	}
 	
 	private void saveFile(File file, FileConfiguration fileConfig) throws IOException {
 		fileConfig.save(file);
 	}
 	
 }
