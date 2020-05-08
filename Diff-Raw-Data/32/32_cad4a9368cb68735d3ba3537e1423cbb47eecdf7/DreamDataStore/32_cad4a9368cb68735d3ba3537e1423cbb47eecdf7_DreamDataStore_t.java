 package de.blablubbabc.dreamworld.managers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import de.blablubbabc.dreamworld.DreamworldPlugin;
 import de.blablubbabc.dreamworld.objects.DreamData;
 
 public class DreamDataStore {
 	private final DreamworldPlugin plugin;
 	private final File file;
 	private final YamlConfiguration store;
 	private Map<String, DreamData> storedDreams = new HashMap<String, DreamData>();
 	
 	public DreamDataStore(final DreamworldPlugin plugin) {
 		this.plugin = plugin;
 		// load saved data:
 		file = new File(plugin.getDataFolder(), "dreamDataStore.yml");
 		store = YamlConfiguration.loadConfiguration(file);
 		// create file, if it didn't exist yet:
 		saveCurrentDreamData();
 		
 		// load dream data:
 		for (String playerName : store.getKeys(false)) {
			Map<String, Object> values = getValuesAsMap(store.getConfigurationSection(playerName));
			storedDreams.put(playerName, new DreamData(playerName, values));
 		}
 		
 		// start purge process:
 		plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
 			
 			@Override
 			public void run() {
 				long currentTime = System.currentTimeMillis();
 				long purgeAfterMilliSeconds = plugin.getConfigManager().purgeAfterMinutes * 60 * 1000L;
 				boolean dirty = false;
 				
 				Iterator<Entry<String, DreamData>> iterator = storedDreams.entrySet().iterator();
 				while (iterator.hasNext()) {
 					Entry<String, DreamData> entry = iterator.next();
 					String playerName = entry.getKey();
 					DreamData dreamData = entry.getValue();
 					
 					long startTime = dreamData.getStartTime();
 					if (currentTime - startTime > purgeAfterMilliSeconds) {
 						// the purge:
 						iterator.remove();
 						store.set(playerName, null);
 						
 						dirty = true;
 					}
 				}
 				
 				if (dirty) saveCurrentDreamData();
 			}
 		}, 20L, 20 * 60L);
 	}
		
	private Map<String, Object> getValuesAsMap(ConfigurationSection section) {
		Map<String, Object> values = section.getValues(false);
		for (Entry<String, Object> entry : values.entrySet()) {
			if (entry.getValue() instanceof ConfigurationSection) {
				entry.setValue(getValuesAsMap((ConfigurationSection) entry.getValue()));
			}
		}
		return values;
	}
 	
 	public DreamData getStoredDreamData(String playerName) {
 		return storedDreams.get(playerName);
 	}
 	
 	public void storeDreamState(String playerName, DreamData dreamData, boolean save) {
 		storedDreams.put(playerName, dreamData);
 		store.set(playerName, dreamData.serialize());
 		
 		if (save) saveCurrentDreamData();
 	}
 	
 	public void removeDreamData(String playerName, boolean save) {
 		storedDreams.remove(playerName);
 		store.set(playerName, null);
 		
 		if (save) saveCurrentDreamData();
 	}
 	
 	public void saveCurrentDreamData() {
 		try {
 			store.save(file);
 		} catch (IOException e) {
 			plugin.getLogger().warning("Couldn't save dream data to '" + file.getName() + "'");
 			e.printStackTrace();
 		}
 	}
 }
