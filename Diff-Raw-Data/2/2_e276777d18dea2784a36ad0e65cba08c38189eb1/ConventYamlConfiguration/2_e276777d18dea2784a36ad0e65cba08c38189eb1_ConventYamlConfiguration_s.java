 package com.conventnunnery.libraries.config;
 
 import org.apache.commons.lang.Validate;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Logger;
 
 public class ConventYamlConfiguration extends YamlConfiguration implements ConventConfiguration {
 
 	private final File file;
 	private final Logger logger;
 	private final String version;
 
 	/**
 	 * Instantiates a new com.conventnunnery.libraries.config.ConventYamlConfiguration.
 	 *
 	 * @param plugin      Plugin that the file is used by
 	 * @param filename    Name of the file used by the plugin
 	 * @param checkUpdate
 	 */
 	@Deprecated
 	public ConventYamlConfiguration(Plugin plugin, String filename, boolean checkUpdate) {
 		this(plugin, new File(plugin.getDataFolder(), filename), checkUpdate);
 	}
 
 	/**
 	 * Instantiates a new com.conventnunnery.libraries.config.ConventYamlConfiguration.
 	 *
 	 * @param file File to use as the basis
 	 */
 	@Deprecated
 	public ConventYamlConfiguration(Plugin plugin, File file, boolean checkUpdate) {
 		super();
 
 		this.logger = plugin.getLogger();
 		this.file = file;
 		this.options().updateOnLoad(checkUpdate);
 		this.version = YamlConfiguration.loadConfiguration(plugin.getResource(file.getName())).getString("version");
 	}
 
 	@Override
 	public ConventYamlConfigurationOptions options() {
 		if (options == null) {
 			options = new ConventYamlConfigurationOptions(this);
 		}
 		return (ConventYamlConfigurationOptions) options;
 	}
 
 	/**
 	 * Instantiates a new com.conventnunnery.libraries.config.ConventYamlConfiguration.
 	 *
 	 * @param file File to use as the basis
 	 */
 	public ConventYamlConfiguration(File file) {
 		this(file, null);
 	}
 
 	/**
 	 * Instantiates a new com.conventnunnery.libraries.config.ConventYamlConfiguration.
 	 *
 	 * @param version The version of the default values
 	 * @param file    File to use as the basis
 	 */
 	public ConventYamlConfiguration(File file, String version) {
 		super();
 
 		Validate.notNull(file, "File cannot be null.");
 
 		this.file = file;
 		this.logger = Bukkit.getLogger();
 		this.version = version;
 
 	}
 
 	@Override
 	public String getName() {
 		return file.getName();
 	}
 
 	/**
 	 * Loads the file specified by the constructor.
 	 *
 	 * @return if the file was correctly loaded
 	 */
 	@Override
 	public boolean load() {
 		return load(options().updateOnLoad(), options().createDefaultFile());
 	}
 
 	/**
 	 * Saves the file specified by the constructor.
 	 *
 	 * @return if the file was correctly saved
 	 */
 	@Override
 	public boolean save() {
 		try {
			if (!file.getParentFile().mkdirs()) return false;
 			save(file);
 			return true;
 		} catch (IOException e) {
 			logger.severe(e.getMessage());
 			return false;
 		}
 	}
 
 	@Override
 	public void setDefaults(final InputStream inputStream) {
 		super.setDefaults(YamlConfiguration.loadConfiguration(inputStream));
 	}
 
 	@Override
 	public void saveDefaults(InputStream inputStream) {
 
 	}
 
 	@Override
 	public boolean needToUpdate() {
 		return getString("version") == null || (version != null && !version.equalsIgnoreCase(getString("version")));
 	}
 
 	@Override
 	public boolean backup() {
 
 		File backup = new File(file.getParent(), file.getName().replace(".yml", "_old.yml"));
 
 		try {
 
 			if (!backup.getParentFile().mkdirs()) return false;
 
 			save(backup);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		return true;
 
 	}
 
 	@Override
 	public FileConfiguration getFileConfiguration() {
 		return this;
 	}
 
 	/**
 	 * Loads the file specified by the constructor.
 	 *
 	 * @param update            specifies if it should update the file using the defaults if versions differ
 	 * @param createDefaultFile specifies if it should create a default file if there is no existing one
 	 * @return if the file was correctly loaded
 	 */
 	public boolean load(boolean update, boolean createDefaultFile) {
 
 		try {
 
 			if (file.exists()) {
 
 				load(file);
 
 				if (needToUpdate() && update) update();
 
 			} else if (createDefaultFile) {
 
 				options().copyDefaults(true);
 
 				return save();
 
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		return true;
 
 	}
 
 	/**
 	 * Updates the file with the defaults
 	 *
 	 * @return if the file was correctly updated
 	 */
 	public boolean update() {
 
 		if (options().backupOnUpdate())
 			if (!backup()) return false;
 
 		options().copyDefaults(true);
 
 		return save();
 
 	}
 
 	/**
 	 * Loads the file specified by the constructor.
 	 *
 	 * @param update specifies if it should update the file using the defaults if versions differ
 	 * @return if the file was correctly loaded
 	 */
 	public boolean load(boolean update) {
 		return load(update, options().createDefaultFile());
 	}
 }
