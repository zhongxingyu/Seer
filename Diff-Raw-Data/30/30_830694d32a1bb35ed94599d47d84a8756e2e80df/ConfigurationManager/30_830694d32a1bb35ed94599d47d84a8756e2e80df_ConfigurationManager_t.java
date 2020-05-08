 package uk.codingbadgers.bFundamentals;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 /**
  * The Class ConfigurationManager.
  * Handles loading of the configuration and access to
  * each configuration option.
  */
 public class ConfigurationManager {
 	
 	/** The language. */
 	private String m_language = "UK";
 	
 	/** Should we auto update modules. */
 	private boolean m_autoUpdate = false;
 	
 	/** Whether to automatically download updates. */
 	private boolean m_autoDownload = false;
 	
 	/** Whether to automatically apply updates. */
 	private boolean m_autoApply = false;
 	
 	/** whether modules should start in debug mode. */
 	private boolean m_debug = false;
 	
 	/** The logger prefix. */
 	private String m_logPrefix = null;
 
 	/**
 	 * Load configuration for the plugin.
 	 *
 	 * @param plugin the main bFundamentals plugin
 	 */
 	public void loadConfiguration(bFundamentals plugin) {
 		FileConfiguration config = plugin.getConfig();
 		
 		config.addDefault("general.language", "UK");
 		config.addDefault("general.debug", false);
 		
 		config.addDefault("module.update.enabled", false);
 		config.addDefault("module.update.download", false);
 		config.addDefault("module.update.apply", false);
 		
 		config.addDefault("module.logging.prefix", "[Module]");
 		
         config.addDefault("database.driver", "SQLite");
         config.addDefault("database.host", "localhost");
         config.addDefault("database.name", "bFundamentals");
         config.addDefault("database.tablePrefix", "bFundamentals_");
         config.addDefault("database.user", "root");
         config.addDefault("database.password", "");
         config.addDefault("database.port", 3306);
         config.addDefault("database.updateRate", 5);
 		
 		config.options().copyDefaults(true);
 		plugin.saveConfig();
 		
 		m_language = config.getString("general.language");
 		m_debug = config.getBoolean("general.debug");
 		
		m_autoUpdate = config.getBoolean("module.update.enabled");
		m_autoDownload = config.getBoolean("module.update.download");
		m_autoApply = config.getBoolean("module.update.apply");
 		
 		m_logPrefix = config.getString("module.logging.prefix");
 	}
 	
 	/**
 	 * Gets the language.
 	 *
 	 * @return the language
 	 */
 	public String getLanguage() {
 		return m_language;
 	}
 	
 	/**
 	 * Gets whether modules should start in debug mode.
 	 *
 	 * @return true if modules should start in debug, false if not
 	 */
 	public boolean getDebug() {
 		return m_debug;
 	}
 	
 	/**
 	 * Gets whether auto update is enabled.
 	 *
 	 * @return true if auto update is enabled, false if not
 	 */
 	public boolean getAutoUpdate() {
 		return m_autoUpdate;
 	}
 	
 	/**
 	 * Gets whether auto download for the updater is enabled.
 	 *
 	 * @return true if auto download is enabled, false if not
 	 */
 	public boolean getAutoDownload() {
 		return m_autoDownload;
 	}
 	
 	/**
 	 * Gets whether auto applying of updates is enabled.
 	 *
 	 * @return true if auto apply is enabled, false if not
 	 */
 	public boolean getAutoApply() {
 		return m_autoApply;
 	}
 	
 	/**
 	 * Gets the log prefix.
 	 *
 	 * @return the log prefix
 	 */
 	public String getLogPrefix() {
 		return m_logPrefix;
 	}
 
 }
