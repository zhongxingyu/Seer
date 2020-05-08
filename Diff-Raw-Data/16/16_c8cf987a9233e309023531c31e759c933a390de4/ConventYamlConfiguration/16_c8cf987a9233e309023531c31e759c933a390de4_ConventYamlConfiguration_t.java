 package com.conventnunnery.libraries.config;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 
 public class ConventYamlConfiguration extends YamlConfiguration implements ConventConfiguration {
 
 	private final File file;
 	private final Plugin plugin;
	private final boolean checkUpdate;
 
 	/**
 	 * Instantiates a new com.conventnunnery.libraries.config.ConventYamlConfiguration.
 	 *
 	 * @param plugin   Plugin that the file is used by
 	 * @param filename Name of the file used by the plugin
	 * @param checkUpdate
 	 */
	public ConventYamlConfiguration(Plugin plugin, String filename, boolean checkUpdate) {
		this(plugin, new File(plugin.getDataFolder(), filename), checkUpdate);
 	}
 
 	/**
 	 * Instantiates a new com.conventnunnery.libraries.config.ConventYamlConfiguration.
 	 *
 	 * @param file File to use as the basis
 	 */
	public ConventYamlConfiguration(Plugin plugin, File file, boolean checkUpdate) {
 		super();
 		this.file = file;
 		this.plugin = plugin;
		this.checkUpdate = checkUpdate;
 	}
 
 	@Override
 	public String getName() {
 		return file.getName();
 	}
 
 	@Override
 	public FileConfiguration getFileConfiguration() {
 		return this;
 	}
 
 	/**
 	 * Loads the file specified by the constructor.
 	 *
 	 * @return if the file was correctly loaded
 	 */
 	@Override
 	public boolean load() {
 		if (file == null) {
 			return false;
 		}
 		try {
 			if (file.exists()) {
 				load(file);
				if (checkUpdate && needToUpdate()) {
 					plugin.getLogger().log(Level.INFO, "Backing up " + file.getName());
 					backup();
 					plugin.getLogger().log(Level.INFO, "Updating " + file.getName());
 					saveDefaults(plugin.getResource(file.getName()));
 					load(file);
 				}
 				return true;
 			}
 			if (!file.getParentFile().mkdirs()) {
 				return false;
 			}
 			if (plugin != null) {
 				saveDefaults(plugin.getResource(file.getName()));
 			}
 			load(file);
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Saves the file specified by the constructor.
 	 *
 	 * @return if the file was correctly saved
 	 */
 	@Override
 	public boolean save() {
 		if (file == null) {
 			return false;
 		}
 		try {
 			save(file);
 			return true;
 		} catch (IOException e) {
 			Bukkit.getLogger().severe(e.getMessage());
 			return false;
 		}
 	}
 
 	@Override
 	public void setDefaults(final InputStream inputStream) {
 		super.setDefaults(YamlConfiguration.loadConfiguration(inputStream));
 	}
 
 	@Override
 	public void saveDefaults(final InputStream inputStream) {
 		for (String key : getKeys(true)) {
 			set(key, null);
 		}
 		setDefaults(inputStream);
 		options().copyDefaults(true);
 		save();
 	}
 
 	@Override
 	public boolean needToUpdate() {
 		if (plugin == null || file == null) {
 			return false;
 		}
 		YamlConfiguration inPlugin = YamlConfiguration.loadConfiguration(plugin
 				.getResource(file.getName()));
 		if (inPlugin == null) {
 			return false;
 		}
 		String configVersion = getString("version");
 		String currentVersion = inPlugin.getString("version");
 		return configVersion == null || currentVersion != null && !(configVersion.equalsIgnoreCase(currentVersion));
 	}
 
 	@Override
 	public boolean backup() {
 		if (plugin == null || file == null) {
 			return false;
 		}
 		File actualFile = new File(plugin.getDataFolder(), file.getName());
 		if (!actualFile.exists()) {
 			return false;
 		}
 		File newFile = new File(plugin.getDataFolder(), file.getName().replace(".yml", "_old.yml"));
 		return actualFile.renameTo(newFile);
 	}
 
 }
