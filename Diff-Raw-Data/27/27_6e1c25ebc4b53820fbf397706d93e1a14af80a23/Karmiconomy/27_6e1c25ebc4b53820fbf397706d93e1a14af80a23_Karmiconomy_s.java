 package com.mitsugaru.Karmiconomy;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.mitsugaru.Karmiconomy.config.Config;
 import com.mitsugaru.Karmiconomy.config.LocalizeConfig;
 import com.mitsugaru.Karmiconomy.database.DatabaseHandler;
 import com.mitsugaru.Karmiconomy.events.HeroesListener;
 import com.mitsugaru.Karmiconomy.events.KarmiconomyListener;
 import com.mitsugaru.Karmiconomy.events.EventLogic;
 import com.mitsugaru.Karmiconomy.events.McMMOListener;
 import com.mitsugaru.Karmiconomy.permissions.PermCheck;
 
 public class Karmiconomy extends JavaPlugin
 {
 	public static final String TAG = "[Karmiconomy]";
 	private Commander commander;
 	private Config config;
 	private PermCheck perm;
 	private DatabaseHandler database;
 	private boolean economyFound;
 	public boolean mcmmo, heroes;
 	public static Map<String, String> sentMessages = new HashMap<String, String>();
 
 	/**
 	 * Method that is called when plugin is disabled
 	 */
 	@Override
 	public void onDisable()
 	{
 		// Save config
 		this.reloadConfig();
 		this.saveConfig();
 		// Disconnect from sql database
 		if (database.checkConnection())
 		{
 			// Close connection
 			database.close();
 		}
 		if (economyFound)
 		{
 			// Finish up anything economy related?
 		}
 		// getLogger().info("Plugin disabled");
 	}
 
 	/**
 	 * Method that is called when plugin is enabled
 	 */
 	@Override
 	public void onEnable()
 	{
 		// Config
 		config = new Config(this);
 		// Grab database
 		database = new DatabaseHandler(this, config);
 		// Check update
 		config.checkUpdate();
 		//Localization config
 		LocalizeConfig.init(this);
 		// Setup economy
 		KarmicEcon ke = new KarmicEcon(this);
 		economyFound = ke.setupEconomy();
 		if (!economyFound)
 		{
 			// No economy system found, disable
 			getLogger().warning(Karmiconomy.TAG + " No economy found!");
 			getServer().getPluginManager().disablePlugin(this);
 		}
 
 		// Set permission handler
 		perm = new PermCheck(this);
 
 		// Setup commander
 		commander = new Commander(this);
 		getCommand("kcon").setExecutor(commander);
 		// Setup listeners
 		EventLogic.init(this);
 		KarmiconomyListener listener = new KarmiconomyListener(this);
 		this.getServer().getPluginManager().registerEvents(listener, this);
 		Plugin mcmmoPlugin = getServer().getPluginManager()
 				.getPlugin("mcMMO");
 		if(mcmmoPlugin != null)
 		{
 			McMMOListener mcmmoListener = new McMMOListener(this);
 			this.getServer().getPluginManager().registerEvents(mcmmoListener, this);
 			mcmmo = true;
 			getLogger().info("Hooked into mcMMO");
 		}
 		else
 		{
 			mcmmo = false;
 		}
 		Plugin heroesPlugin = getServer().getPluginManager().getPlugin("Heroes");
 		if(heroesPlugin != null)
 		{
 			HeroesListener heroesListener = new HeroesListener(this);
 			this.getServer().getPluginManager().registerEvents(heroesListener, this);
 			heroes = true;
 			getLogger().info("Hooked into Heroes");
 		}
 		else
 		{
 			heroes = false;
 		}
 		// getLogger().info(TAG + " Enabled v" + getDescription().getVersion());
 	}
 
 	public Commander getCommander()
 	{
 		return commander;
 	}
 
 	public PermCheck getPermissionHandler()
 	{
 		return perm;
 	}
 
 	/**
 	 * Returns Config object
 	 * 
 	 * @return Config object
 	 */
 	public Config getPluginConfig()
 	{
 		return config;
 	}
 
 	/**
 	 * Returns database handler
 	 * 
 	 * @return Database handler
 	 */
 	public DatabaseHandler getDatabaseHandler()
 	{
 		return database;
 	}
 	
 	/**
 	 * Colorizes a given string to Bukkit standards
 	 * 
 	 * http://forums.bukkit.org/threads/methode-to-colorize.69543/#post-1063437
 	 * 
 	 * @param string
 	 * @return String with appropriate Bukkit ChatColor in them
 	 * @author AmberK
 	 */
 	public static String colorizeText(String string)
 	{
 		/**
 		 * Colors
 		 */
 		string = string.replaceAll("&0", "" + ChatColor.BLACK);
 		string = string.replaceAll("&1", "" + ChatColor.DARK_BLUE);
 		string = string.replaceAll("&2", "" + ChatColor.DARK_GREEN);
 		string = string.replaceAll("&3", "" + ChatColor.DARK_AQUA);
 		string = string.replaceAll("&4", "" + ChatColor.DARK_RED);
 		string = string.replaceAll("&5", "" + ChatColor.DARK_PURPLE);
 		string = string.replaceAll("&6", "" + ChatColor.GOLD);
 		string = string.replaceAll("&7", "" + ChatColor.GRAY);
 		string = string.replaceAll("&8", "" + ChatColor.DARK_GRAY);
 		string = string.replaceAll("&9", "" + ChatColor.BLUE);
 		string = string.replaceAll("&a", "" + ChatColor.GREEN);
 		string = string.replaceAll("&b", "" + ChatColor.AQUA);
 		string = string.replaceAll("&c", "" + ChatColor.RED);
 		string = string.replaceAll("&d", "" + ChatColor.LIGHT_PURPLE);
 		string = string.replaceAll("&e", "" + ChatColor.YELLOW);
 		string = string.replaceAll("&f", "" + ChatColor.WHITE);
 		/**
 		 * Formatting
 		 */
 		string = string.replaceAll("&k", "" + ChatColor.MAGIC);
 		string = string.replaceAll("&l", "" + ChatColor.BOLD);
 		string = string.replaceAll("&m", "" + ChatColor.STRIKETHROUGH);
 		string = string.replaceAll("&n", "" + ChatColor.UNDERLINE);
 		string = string.replaceAll("&o", "" + ChatColor.ITALIC);
 		string = string.replaceAll("&r", "" + ChatColor.RESET);
 		return string;
 	}
 }
