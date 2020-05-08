 package uk.codingbadgers.bFundamentals.module;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.permission.Permission;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.Validate;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 
 import uk.codingbadgers.bFundamentals.bFundamentals;
 import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
 import uk.codingbadgers.bFundamentals.commands.ModuleCommandHandler;
 import uk.codingbadgers.bFundamentals.config.ConfigFactory;
 import uk.codingbadgers.bFundamentals.config.ConfigFile;
 import uk.codingbadgers.bFundamentals.config.annotation.Element;
 import uk.codingbadgers.bFundamentals.module.loader.Loadable;
 import uk.codingbadgers.bFundamentals.update.UpdateThread;
 import uk.codingbadgers.bFundamentals.update.Updater;
 import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;
 
 /**
  * The base Module class any module should extend this, it also provides helper
  * methods for the module .
  */
 public abstract class Module extends Loadable implements Listener {
 
 	/** The base {@link bFundamentals} plugin instance. */
 	protected final bFundamentals m_plugin;
 
 	/** The logger for this module */
 	private ModuleLogger m_log;
 
 	/** The Update thread for this module */
 	private UpdateThread m_updater;
 
 	/** The base config file for this module. */
 	protected FileConfiguration m_config;
 
 	/** The base config file for this module, unparsed. */
 	protected File m_configFile = null;
 
 	/**
 	 * The map of all language keys associated with this module for the current
 	 * loaded language.
 	 */
 	private HashMap<String, String> m_languageMap = new HashMap<String, String>();
 
 	/** The commands registered to this module. */
 	protected List<ModuleCommand> m_commands = new ArrayList<ModuleCommand>();
 
 	/** All the listeners registered to this module */
 	private List<Listener> m_listeners = new ArrayList<Listener>();
 
 	/** The database registered to the modules. */
 	protected static BukkitDatabase m_database = null;
 
 	/** The Vault Permissions instance. */
 	private static Permission m_permissions = null;
 
 	/** Whether this module is in debug mode */
 	private boolean m_debug = false;
 
 	/** Whether the language file is loaded. */
 	private boolean loadedLanguageFile;
 
 	/** A list of all the config classes registered to this module */
 	private ArrayList<Class<? extends ConfigFile>> m_configFiles;
 
 	private boolean m_enabled;
 
 	/**
 	 * Instantiates a new module with default settings.
 	 */
 	public Module() {
 		super();
 		m_plugin = bFundamentals.getInstance();
 		m_database = bFundamentals.getBukkitDatabase();
 		m_debug = bFundamentals.getConfigurationManager().getDebug();
 		m_permissions = bFundamentals.getPermissions();
 	}
 
 	public void init() {
 		m_log = new ModuleLogger(this);
 	}
 
 	protected void setUpdater(Updater updater) {
 		m_updater = new UpdateThread(updater);
 		log(Level.INFO, "Set new updater to " + m_updater.getUpdater().getUpdater());
 	}
 
 	public void update() {
 		if (m_updater == null) {
 			log(Level.INFO, "Updater is null, cannot check for updates");
 			return;
 		}
 
 		m_updater.start();
 	}
 
 	/**
 	 * Load language file.
 	 */
 	protected void loadLanguageFile() {
 		File languageFile = new File(getDataFolder() + File.separator + getName() + "_" + bFundamentals.getConfigurationManager().getLanguage() + ".lang");
 
 		if (!languageFile.exists()) {
 			log(Level.SEVERE, "Missing language file '" + languageFile.getAbsolutePath() + "'!");
 
 			boolean foundLangFile = false;
 			InputStream stream = null;
 			FileOutputStream fstream = null;
 
 			try {
 				stream = getClass().getResourceAsStream("/" + languageFile.getName());
 
 				// if default file exists in jar, copy it out to the right
 				// directory
 				if (stream != null) {
 					fstream = new FileOutputStream(languageFile);
 
 					foundLangFile = true;
 					IOUtils.copy(stream, fstream);
 				}
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					if (stream != null) {
 						stream.close();
 					}
 
 					if (fstream != null) {
 						fstream.close();
 					}
 				} catch (IOException ex) {
 					ex.printStackTrace();
 				}
 			}
 
 			if (foundLangFile) {
 				log(Level.INFO, "Copied default language file from jar file");
 			} else {
 				return;
 			}
 		}
 
 		log(Level.INFO, "Loading Language File: " + languageFile.getName());
 
 		FileInputStream fstream = null;
 		DataInputStream in = null;
 		BufferedReader br = null;
 
 		try {
 			fstream = new FileInputStream(languageFile);
 			in = new DataInputStream(fstream);
 			br = new BufferedReader(new InputStreamReader(in));
 
 			String line = null;
 			String key = null;
 			while ((line = br.readLine()) != null) {
 
 				if (line.isEmpty() || line.startsWith("//"))
 					continue;
 
 				if (line.startsWith("#")) {
 					key = line.substring(1);
 					continue;
 				}
 
 				if (key == null) {
 					log(Level.WARNING, "Trying to parse a language value, with no key set!");
 					continue;
 				}
 
 				m_languageMap.put(key.toLowerCase(), line);
 			}
 
 			loadedLanguageFile = true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (fstream != null) {
 					fstream.close();
 				}
 				if (in != null) {
 					in.close();
 				}
 				if (br != null) {
 					br.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/**
 	 * Log a message console via this modules logger.
 	 * 
 	 * @param level
 	 *            the Log level
 	 * @param string
 	 *            the message
 	 */
 	public void log(Level level, String string) {
 		m_log.log(Level.INFO, string);
 	}
 
 	/**
 	 * Get the logger associated with this module
 	 * 
 	 * @return this modules logger
 	 */
 	public Logger getLogger() {
 		return m_log;
 	}
 
 	/**
 	 * Register a bukkit event listener.
 	 * 
 	 * @param listener
 	 *            the bukkit event listener
 	 */
 	public final void register(Listener listener) {
 		m_plugin.getServer().getPluginManager().registerEvents(listener, m_plugin);
 		m_listeners.add(listener);
 	}
 
 	/**
 	 * Gets the vault permissions instance.
 	 * 
 	 * @return the vault permissions instance
 	 */
 	public Permission getPermissions() {
 		return m_permissions;
 	}
 
 	/**
 	 * The enable method for this module, called on enabling the module via 
 	 * {@link #setEnabled(boolean)} this is used to register commands, events
 	 * and any other things that should be registered on enabling the module.
 	 */
 	public abstract void onEnable();
 
 	/**
 	 * The disable method for this module, called on disabling the module via
 	 * {@link #setEnabled(boolean)} this is used to clean up after the module when
 	 * it is disabled.
 	 */
 	public abstract void onDisable();
 
 	/**
 	 * The load method for this module, called on loading the module via the
 	 * {@link ModuleLoader} this is called before any module in that load batch
 	 * is loaded. 
 	 */
 	public void onLoad() {}
 	
 	/**
 	 * Sets the module enabled status, will call {@link #onEnable()} if the 
 	 * module isn't already enabled and you want to enable it and will call
 	 * {@link #onDisable()} if the module isn't already disabled and you want
 	 * to disable it.
 	 * 
 	 * @param enabled if you want to enable or disable the module
 	 */
 	public void setEnabled(boolean enabled) {
 		if (enabled) {
 			if (m_enabled) {
 				return;
 			}
 			
 			onEnable();
 			m_enabled = true;
 		} else {
 			if (!m_enabled) {
 				return;
 			}
 			
 			onDisable();
 			
 			for (ModuleCommand command : m_commands) {
				ModuleCommandHandler.deregisterCommand(command);
 			}
 			m_enabled = false;
 		}
 	}
 	
 	/**
 	 * Returns the current state of the module, if it is enabled or disabled.
 	 * 
 	 * @return if the module is enabled
 	 */
 	public boolean isEnabled() {
 		return m_enabled;
 	}
 
 	/**
 	 * The command handing method for this module, this is only called if the
 	 * command handing for that {@link ModuleCommand} returns false, preferably
 	 * the {@link ModuleCommand#onCommand(CommandSender, String, String[])}
 	 * should be used, this is just left for backwards comparability.
 	 * 
 	 * @param sender
 	 *            the command sender
 	 * @param label
 	 *            the command label used
 	 * @param args
 	 *            the arguments for the command
 	 * @return true, if the command has been handled, false if it hasn't
 	 */
 	public boolean onCommand(CommandSender sender, String label, String[] args) {
 		return false;
 	}
 
 	/**
 	 * Gets the version of this module loaded from the path.yml file.
 	 * 
 	 * @return the module version
 	 */
 	public String getVersion() {
 		return getDesciption().getVersion();
 	}
 
 	/**
 	 * Checks if a player has a specific permission.
 	 * 
 	 * @param player
 	 *            the player to check
 	 * @param node
 	 *            the permission node
 	 * @return true, if the player has the permission
 	 */
 	public static boolean hasPermission(final Player player, final String node) {
 		if (m_permissions.has(player, node)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Send message to a player formated in the default style.
 	 * 
 	 * @param name
 	 *            the name of the module
 	 * @param player
 	 *            the player to send to
 	 * @param message
 	 *            the message
 	 */
 	public static void sendMessage(String name, CommandSender player, String message) {
 		player.sendMessage(ChatColor.DARK_PURPLE + "[" + name + "] " + ChatColor.RESET + message);
 	}
 
 	/**
 	 * Register a command to this module.
 	 * 
 	 * @param command
 	 *            the command
 	 */
 	protected void registerCommand(ModuleCommand command) {
 		ModuleCommandHandler.registerCommand(this, command);
 	}
 
 	/**
 	 * Get all commands registered to this module
 	 * 
 	 * @return the commands
 	 */
 	public List<ModuleCommand> getCommands() {
 		return m_commands;
 	}
 
 	/**
 	 * Gets the language value for the current loaded language, case
 	 * insensitive, all keys are forced to be in lower case.
 	 * 
 	 * @param key
 	 *            the language key
 	 * @return the language value, if available, the key with hyphens removed
 	 *         and in lower case otherwise
 	 */
 	public String getLanguageValue(String key) {
 		Validate.notNull(key, "Language key cannot be null");
 
 		if (!loadedLanguageFile) {
 			log(Level.SEVERE, "Cannot get language value before loading language file");
 		}
 
 		String value = m_languageMap.get(key.toLowerCase());
 
 		if (value == null) {
 			value = key.toLowerCase().replace("-", " ");
 		}
 
 		return value;
 	}
 
 	/**
 	 * Get all the listeners registered to this module, for cleaning up on
 	 * disable
 	 * 
 	 * @return a list of all listeners
 	 */
 	public List<Listener> getListeners() {
 		return m_listeners;
 	}
 
 	/**
 	 * Is debug mode enabled on this module
 	 * 
 	 * @return if debug is enabled
 	 */
 	public boolean isDebug() {
 		return m_debug;
 	}
 
 	/**
 	 * Set the debug mode for this module
 	 * 
 	 * @param debug
 	 *            whether debug is on or not
 	 */
 	public void setDebug(boolean debug) {
 		m_debug = debug;
 	}
 
 	/**
 	 * Output a message to console if debug mode is on
 	 * 
 	 * @param message
 	 *            the message to output
 	 */
 	public void debugConsole(String message) {
 		if (!m_debug)
 			return;
 
 		log(Level.INFO, "[Debug] " + message);
 	}
 
 	/**
 	 * Registers a config class as a config and loads it, class must extend
 	 * {@link ConfigFile} and each element that is going to be included in the
 	 * file should be {@code static} and have a {@link Element}
 	 * annotation associated with it.
 	 * 
 	 * @param clazz
 	 *            the config class
 	 */
 	public void registerConfig(Class<? extends ConfigFile> clazz) {
 		if (m_configFiles == null) {
 			m_configFiles = new ArrayList<Class<? extends ConfigFile>>();
 		}
 
 		log(Level.INFO, "Load config file for " + clazz.getName());
 
 		try {
 			ConfigFactory.load(clazz, getDataFolder());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		m_configFiles.add(clazz);
 	}
 }
