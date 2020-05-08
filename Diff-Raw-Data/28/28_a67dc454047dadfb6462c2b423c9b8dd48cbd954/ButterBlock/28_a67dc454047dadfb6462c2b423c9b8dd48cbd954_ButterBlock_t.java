 package io.github.tjstretchalot.butterblock;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class ButterBlock extends JavaPlugin {
 	private List<String> separated;
 	private PlayerMessageListener playerMessageListener;
 	private File memoryFile;
 	private YamlConfiguration memory;
 
 	@Override
 	public void onEnable() {
 		if(separated == null) {
 			separated = Collections.synchronizedList(new ArrayList<String>());
 		}
 
 		List<String> regexes = getConfig().getStringList("regexes");
 		boolean rem = getConfig().getBoolean("remember");

 		playerMessageListener = new PlayerMessageListener(this, memory, regexes.toArray(new String[0]), rem, separated);
 		getServer().getPluginManager().registerEvents(playerMessageListener, this);
 		getCommand("unseparate").setExecutor(new UnseperateCommandExecutor(separated, getLogger()));
 		Communications.info(getLogger(), Communications.ENABLED);

 		boolean executeCommand = getConfig().getBoolean("executeCommand");
 		if(executeCommand) {
 			Communications.info(getLogger(), Communications.USING_COMMAND);
 			playerMessageListener.addOnRegexMatched(new ExecuteCommandOnRegexMatched(getConfig()));
 		}else {
 			Communications.info(getLogger(), Communications.SEPERATE_BELIEVERS);
 			playerMessageListener.addOnRegexMatched(new SeperateOnRegexMatched());
 		}
 		saveConfig();
 	}
 
 	@Override
 	public void onDisable() {
 		if(!getConfig().getBoolean("rememeber")) {
 			synchronized(separated) {
 				separated.clear();
 			}
 		}
 		Communications.info(getLogger(), Communications.DISABLED);
 	}
 
 
 	@Override
 	public void reloadConfig() {
 		super.reloadConfig();
 
 
 		List<String> regexes = getConfig().getStringList("regexes");
 		boolean rem = getConfig().getBoolean("remember");
 		if(playerMessageListener != null) {
 			playerMessageListener.setRegexes(regexes.toArray(new String[0]));
 			playerMessageListener.setRemember(rem);
 		}
 
 		boolean executeCommand = getConfig().getBoolean("executeCommand");
		if(playerMessageListener != null) {
			playerMessageListener.clearOnRegexMatchedListeners();
			if(executeCommand) {
				Communications.info(getLogger(), Communications.USING_COMMAND);
				playerMessageListener.addOnRegexMatched(new ExecuteCommandOnRegexMatched(getConfig()));
			}else {
				Communications.info(getLogger(), Communications.SEPERATE_BELIEVERS);
				playerMessageListener.addOnRegexMatched(new SeperateOnRegexMatched());
			}
 		}

 		reloadMyMemory();
 	}
 
 	private void reloadMyMemory() {
 		if(!getConfig().getBoolean("remember"))
 			return;
 		if (memoryFile == null) {
 			memoryFile = new File(getDataFolder(), "memory.yml");
 		}
 		memory = YamlConfiguration.loadConfiguration(memoryFile);
 
 		// Look for defaults in the jar
 		InputStream defConfigStream = this.getResource("memory.yml");
 		if (defConfigStream != null) {
 			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 			memory.setDefaults(defConfig);
 		}
 
 		synchronized(separated) {
 			separated.clear();
 			separated.addAll(memory.getStringList("memory.seperated"));
 		}
 	}

 	@Override
 	public void saveConfig() {
 		super.saveConfig();
 		saveMyMemory();
 	}
 
 	public FileConfiguration getMemory() {
 		if (memory == null) {
 			this.reloadMyMemory();
 		}
 		return memory;
 	}
 
 	private void saveMyMemory() {
 		if (memory == null || memoryFile == null) {
 			return;
 		}
 		try {
 			getMemory().save(memoryFile);
 		} catch (IOException ex) {
 			this.getLogger().log(Level.SEVERE, "Could not save config to " + memoryFile, ex);
 		}
 	}
 }
