 package org.shininet.bukkit.itemrenamer.configuration;
 
 import java.io.File;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.shininet.bukkit.itemrenamer.ItemRenamer;
 
 /**
  * Represents a item renamer configuration file.
  * 
  * @author Kristian
  */
 public class ItemRenamerConfiguration {
 	// Locations in the configuration file
 	private static final String AUTO_UPDATE = "autoupdate";
 	private static final String WORLD_PACKS = "worlds";
 	
 	private RenameConfiguration renameConfig;
 	private FileConfiguration config;
 	private ItemRenamer plugin;
 	
 	// Path to the configuration file
 	private String path;
 	
 	// Store whether or not the top level has changed
 	private boolean changed;
 	
 	public ItemRenamerConfiguration(ItemRenamer plugin, String path) {
 		this.plugin = plugin;
 		this.path = path;
 		
 		// Copy over default values
 		plugin.getConfig().options().copyDefaults(true);
 		plugin.saveDefaultConfig();
 		initializeConfig();
 	}
 	
 	public void save() {
 		// Backup the current configuration
 		File currentFile = new File(path);
 		File backupFolder = new File(currentFile.getParentFile(), "backup");
 		File backupFile = new File(backupFolder, "config" + System.currentTimeMillis() + ".yml");
 		
 		// Don't overwrite - rename
		if ((backupFolder.exists() || backupFolder.mkdirs()) && currentFile.renameTo(backupFile)) {
 			// Save and reload
 			changed = false;
 			renameConfig.saveAll();
 			plugin.saveConfig();
 			config = plugin.getConfig();
 		} else {
 			throw new RuntimeException("Cannot rename " + currentFile + " to " + backupFile + " for backup.");
 		}
 	}
 	
 	public void reload() {
 		changed = false;
 		plugin.reloadConfig();
 		initializeConfig();
 	}
 	
 	private void initializeConfig() {
 		config = plugin.getConfig();
 		renameConfig = new RenameConfiguration(config.getConfigurationSection("packs"));
 	}
 	
 	public boolean isAutoUpdate() {
 		return config.getBoolean(AUTO_UPDATE);
 	}
 	
 	public void setAutoUpdate(boolean value) {
 		changed = true;
 		config.set(AUTO_UPDATE, value);
 	}
 	
 	/**
 	 * Set the default item name pack for a given world.
 	 * @param world - the world name.
 	 * @param pack - the pack name.
 	 */
 	public void setWorldPack(String world, String pack) {
 		changed = true;
 		config.set(WORLD_PACKS + "." + world, pack);
 	}
 	
 	/**
 	 * Retrieve the default item name pack for a given world.
 	 * @param world - the name of the world.
 	 * @return The default item name pack.
 	 */
 	public String getWorldPack(String world) {
 		return config.getString(WORLD_PACKS + "." + world);
 	}
 	
 	/**
 	 * Determine if the configuration has changed in any way.
 	 * @return TRUE if it has, FALSE otherwise.
 	 */
 	public boolean hasChanged() {
 		if (changed) {
 			System.out.println("[ItemRenamer] Main config has changed");
 			return true;
 		}
 		return renameConfig.hasChanged();
 	}
 	
 	/**
 	 * Access the current rename rules for every pack.
 	 * @return Rename rules for every pack.
 	 */
 	public RenameConfiguration getRenameConfig() {
 		return renameConfig;
 	}
 }
