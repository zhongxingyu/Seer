 package net.milkycraft;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.configuration.ConfigurationSection;
 
 /**
  * This Config class handles the plugin configuration YAML files. Handles
  * defaults, modification, and reloading config.yml. Can handle other YAML files
  * as necessary in separate methods.
  * 
  * Modeled after DiddiZ's implementation from LogBlock
  * 
  * @author Mitsugaru
  * 
  */
 public class Config
 {
 	/**
 	 * Class variables that are called on from other classes. Other classes can
 	 * get config settings from these variables.
 	 * 
 	 * Typically, have variables for all nodes within the config. Classes can
 	 * call on variables for what they want.
 	 * 
 	 * This is better: Instead of having to remember paths of various configuration 
 	 * nodes and settings, we only need to grab the variable that we care about 
 	 * from this class since this class knows of all config nodes. Also saves from 
 	 * typos of paths within your Java code. If there was a typo, it probably only 
 	 * happened here rather than the many times it can be called in other classes.
 	 */
 	private Banlisting plugin;
 	// public String host, port, database, user, password, tablePrefix;
 	public boolean log, debugTime/*, importSQL, unjailTeleport, removeGroups, useMySQL*/;
 	public int limit;
 
 	/**
 	 * Loads config from yaml file
 	 * 
 	 * @param Plugin
 	 *            this class is associated with
 	 */
 	public Config(Banlisting plugin)
 	{
 		// Set plugin to be used in other methods
 		this.plugin = plugin;
 		// Initialize config file from the base plugin.
 		ConfigurationSection config = plugin.getConfig();
 		/**
 		 * These are your default values. For each key, there is an associated
 		 * object. Allows for the typical yml grouping and structure. This
 		 * implementation completely removes the config.yml defaults file as it
 		 * acts in place of that. Centralizes everything config related into
 		 * this one class, including defaults. Much easier to manage.
 		 * 
 		 * Just be aware that you CANNOT set a group node to an object.
 		 * 
 		 * So, if you do: 
 		 * 		defaults.put("mysql.use", false);
 		 * 		defaults.put("mysql.user", "username"); 
 		 * DO NOT DO:
 		 * 		defaults.put("mysql", "whatever");
 		 * 
 		 * As it will not put anything for the key "mysql". This is because
 		 * groups take priority over single keys. The above bad entry "whatever"
 		 * for key "mysql" will be lost as the node "mysql" has child nodes.
 		 */
 		final Map<String, Object> defaults = new HashMap<String, Object>();
 		defaults.put("log", false);
 		defaults.put("limit", 10);
 		/*
 		 * TODO these defaults will be added when we do MySQL stuff
 		 * 
 		 * defaults.put("mysql.use", false); defaults.put("mysql.host",
 		 * "localhost"); defaults.put("mysql.port", 3306);
 		 * defaults.put("mysql.database", "minecraft");
 		 * defaults.put("mysql.user", "username");
 		 * defaults.put("mysql.password", "pass");
 		 * defaults.put("mysql.tablePrefix", "kj_");
 		 * defaults.put("mysql.import", false);
 		 */
 
 		/**
 		 * This one is absolutely necessary as this would be used for
 		 * auto-updating. Although, probably out of scope for this plugin... as
 		 * updating the config file happens here, with this hashmap of defaults.
 		 */
 		defaults.put("version", plugin.getDescription().getVersion());
 
 		/**
 		 * Insert defaults into config file if they're not present This will
 		 * auto-add any future config entires that we require as default within
 		 * new releases. As well as add missing entries that we require, with
 		 * their preset default value. Just in case users go messing about and
 		 * remove stuff that we need.
 		 */
 		for (final Entry<String, Object> e : defaults.entrySet())
 		{
 			if (!config.contains(e.getKey()))
 			{
 				config.set(e.getKey(), e.getValue());
 			}
 		}
 		/**
 		 * Save config
 		 */
 		plugin.saveConfig();
 		/**
 		 * Load variables from config into the class variables.
 		 * 
 		 * Optimally, you want to include a default value if it is not found.
 		 * Such as for the hidden entry of debugTime, which is not added by
 		 * default but is known by the plugin.
 		 */
 		/*
 		 * TODO to be added later when we do MySQL stuff useMySQL =
 		 * config.getBoolean("mysql.use", false); host =
 		 * config.getString("mysql.host", "localhost"); port =
 		 * config.getString("mysql.port", "3306"); database =
 		 * config.getString("mysql.database", "minecraft"); user =
 		 * config.getString("mysql.user", "user"); password =
 		 * config.getString("mysql.password", "password");
 		 */
 		debugTime = config.getBoolean("debugTime", false); //I used to use this to test how long commands took
 		log = config.getBoolean("log", false);
		limit = config.getInt("entrylimit", 10);
 		/**
 		 * Check bounds of config entries
 		 */
 		boundsCheck(config);
 	}
 
 	/**
 	 * This reloads the config from the file. This way, any changes made to the
 	 * file while the plugin was active are not lost. Also, the plugin will
 	 * reflect those changes made into the variables in the class.
 	 */
 	public void reload()
 	{
 		/**
 		 * Reload plugin configuration file
 		 */
 		plugin.reloadConfig();
 		/**
 		 * Grab config file
 		 */
 		ConfigurationSection config = plugin.getConfig();
 		/**
 		 * Load variables from config into the class variables
 		 */
 		debugTime = config.getBoolean("debugTime", false);
 		log = config.getBoolean("log", false);
 		limit = config.getInt("limit", 10);
 		/**
 		 * Check bonds of config entries
 		 */
 		boundsCheck(config);
 	}
 
 	/**
 	 * This checks the config for any illegal settings that were valid types.
 	 * 
 	 * For instance, on the page limit, we don't want to have a negative number
 	 * as then nothing would show. Likewise, we don't want the number so high
 	 * that entries go beyond the standard chat window line limit.
 	 * 
 	 * Basically, this has two purposes:
 	 * 
 	 * 1) Not have the admins worry about the plugin breaking due to their 
 	 * mistakes in editing the config file wrongly. This is because the 
 	 * Config class will warn of their mistakes and use correct values.
 	 * 
 	 * 2) Us not having to deal with admins editing the plugin config.yml 
 	 * wrongly. We can pretty much guarantee to expect values that are 
 	 * valid and working in the above class variables.
 	 * 
 	 * @param ConfigurationSection
 	 *            in use
 	 */
 	public void boundsCheck(ConfigurationSection config)
 	{
 		/**
 		 * Bounds check on the limit
 		 */
 		if (limit <= 0 || limit > 16)
 		{
 			/**
 			 * Bad entry for limit. Notify admins via console warning.
 			 */
 			plugin.getLogger()
 					.warning(
 							Banlisting.prefix
 									+ " Entry limit cannot be <= 0 or > 16. Reverting to default: 10");
 			/**
 			 * Use default value rather than use the bad entry
 			 */
 			limit = 10;
 			/**
 			 * Optional: reset bad config entry to default
 			 */
 			// config.set("limit", 10);
 		}
 	}
 
 	/**
 	 * Check if updates are necessary, based on the running plugin version in
 	 * the description file versus the saved plugin version in the config.yml
 	 */
 	public void checkUpdate()
 	{
 		/**
 		 * Grab config from plugin
 		 */
 		ConfigurationSection config = plugin.getConfig();
 		/**
 		 * Get the version from plugin description and compare it to the one in
 		 * the config entry.
 		 */
 		if (Double.parseDouble(plugin.getDescription().getVersion()) > Double
 				.parseDouble(config.getString("version")))
 		{
 			/**
 			 * Update to latest version
 			 */
 			plugin.getLogger().info(
 					Banlisting.prefix + " Updating to v"
 							+ plugin.getDescription().getVersion());
 			update();
 		}
 	}
 
 	/**
 	 * This method is called to make the appropriate changes, most likely only
 	 * necessary for database schema modification, for a proper update.
 	 * 
 	 * Probably will never be used for this plugin... unless we keep a local
 	 * database for our own use? If we move to integrating a ban system, any
 	 * changes to database schema between versions would be handled in this
 	 * method.
 	 */
 	// SupressWarning temporary until we actually have stuff to put in this
 	// method
 	@SuppressWarnings("unused")
 	private void update()
 	{
 		/**
 		 * Grab current version of plugin
 		 */
 		double ver = Double
 				.parseDouble(plugin.getConfig().getString("version"));
 		/*
 		 * TODO Insert version specific actions of updating within if statments
 		 * here example: 
 		 * 
 		 * if(ver < 2.0) //Or whatever version is next 
 		 * { 
 		 * 		//Insert changes that need to be done 
 		 * }
 		 * 
 		 */
 		// Update version number in config.yml to current version
 		plugin.getConfig().set("version", plugin.getDescription().getVersion());
 		// Save config
 		plugin.saveConfig();
 	}
 
 	/**
 	 * Sets an object for a given path into the config.
 	 * 
 	 * @param path
 	 * @param object
 	 */
 	public void set(String path, Object o)
 	{
 		final ConfigurationSection config = plugin.getConfig();
 		config.set(path, o);
 		plugin.saveConfig();
 	}
 }
