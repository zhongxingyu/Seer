 package com.cole2sworld.ColeBans;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 /**
  * Configuration for ColeBans. Loaded from file on startup.
  *
  */
 //LOWPRI Check if there is a better way to do this
 public final class GlobalConf {
 	/**
 	 * Configuration we are using.
 	 */
 	public static FileConfiguration conf;
 	/**
 	 * The main ConfigurationSection.
 	 */
 	public static ConfigurationSection settings;
 	/**
 	 * Do we allow tempbans to be made?
 	 */
 	public static boolean allowTempBans = true;
 	/**
 	 * Message when somebody tries to log in but is banned.
 	 */
 	public static String banMessage = "You are banned for %reason!";
 	/**
 	 * Message when somebody tries to log in but is tempbanned.
 	 */
 	public static String tempBanMessage = "You are tempbanned! %time seconds remaining!";
 	/**
 	 * Show fancy effects?
 	 */
 	public static boolean fancyEffects = true;
 	/**
 	 * Color of the disconnect message when banned.
 	 */
 	public static String banColor = "DARK_RED";
 	/**
 	 * Color of the disconnect message when kicked.
 	 */
 	public static String kickColor = "YELLOW";
 	/**
 	 * Color of the disconnect message when tempbanned.
 	 */
 	public static String tempBanColor = "RED";
 	/**
 	 * Announce when somebody is banned/kicked to the entire server?
 	 */
 	public static boolean announceBansAndKicks = true;
 	/**
 	 * Prefix to use in the console.
 	 */
 	public static String logPrefix = "[ColeBans] ";
 	/**
 	 * Which banhandler?
 	 */
 	public static String banHandlerConf = "MySQL";
 	/**
 	 * Configuration section for SQL.
 	 *
 	 */
 	public static class Sql {
 		/**
 		 * The raw section.
 		 */
 		public static ConfigurationSection section;
 		/**
 		 * Username for the database.
 		 */
 		public static String user = "minecraft";
 		/**
 		 * Password for the database.
 		 */
 		public static String pass = "password";
 		/**
 		 * Host for the database.
 		 */
 		public static String host = "localhost";
 		/**
 		 * Port for the database.
 		 */
 		public static String port = "3306";
 		/**
 		 * Database name.
 		 */
 		public static String db = "minecraft";
 		/**
 		 * Table prefix.
 		 */
 		public static String prefix = "cb_";
 	}
 	/**
 	 * Configuration section for MCBans.
 	 *
 	 */
 	public static class MCBans {
 		/**
 		 * The raw section.
 		 */
 		public static ConfigurationSection section;
 		/**
 		 * The MCBans API key.
 		 */
 		public static String apiKey = "yourAPIKeyHere";
 		/**
 		 * Whether or not to make full dumps of the banlist, including reasons.
 		 */
 		public static boolean fullBackups = false;
 		/**
 		 * Lowest rep you can have and still be able to join.
 		 */
 		public static double minRep = 10;
 	}
 	/**
 	 * Configuration section for YAML.
 	 *
 	 */
 	public static class Yaml {
 		/**
 		 * The raw section.
 		 */
 		public static ConfigurationSection section;
 		/**
 		 * The file to use.
 		 */
 		public static String file;
 	}
 	/**
 	 * Configuration section for YAML.
 	 *
 	 */
 	public static class Json {
 		/**
 		 * The raw section.
 		 */
 		public static ConfigurationSection section;
 		/**
 		 * The file to use.
 		 */
 		public static String file;
 	}
 	/**
 	 * Stuff the user shouldn't touch unless they know what they are doing.
 	 * @author cole2
 	 *
 	 */
 	public static class Advanced {
 		/**
 		 * The raw section.
 		 */
 		public static ConfigurationSection section;
 		/**
 		 * Which package do we get the banhandlers?
 		 */
 		public static String pkg;
 		/**
 		 * What is the suffix on the banhandlers?
 		 */
 		public static String suffix;
 	}
 	/**
 	 * Loads up the config from disk, or creates it if it does not exist.
 	 */
 	public static void loadConfig() throws RuntimeException {
 		File confFile = new File("./plugins/ColeBans/config.yml");
 		try {
 			if (confFile.exists()) {
 				conf.load(confFile);
 				try {
 					settings = conf.getConfigurationSection("settings");
 					allowTempBans = settings.getBoolean("allowTempBans");
 					banMessage = settings.getString("banMessage");
 					tempBanMessage = settings.getString("tempBanMessage");
 					fancyEffects = settings.getBoolean("fancyEffects");
 					banColor = settings.getString("banColor");
 					kickColor = settings.getString("kickColor");
 					tempBanColor = settings.getString("tempBanColor");
 					announceBansAndKicks = settings.getBoolean("announceBansAndKicks");
 					logPrefix = settings.getString("logPrefix")+" ";
					banHandlerConf = settings.getString("banHandler");
 					Sql.section = settings.getConfigurationSection("mysql");
 					Sql.user = Sql.section.getString("user");
 					Sql.pass = Sql.section.getString("pass");
 					Sql.host = Sql.section.getString("host");
 					Sql.port = Sql.section.getString("port");
 					Sql.db = Sql.section.getString("db");
 					Sql.prefix = Sql.section.getString("prefix");
 					MCBans.section = settings.getConfigurationSection("mcbans");
 					MCBans.apiKey = MCBans.section.getString("apiKey");
 					MCBans.fullBackups = MCBans.section.getBoolean("fullBackups");
 					MCBans.minRep = MCBans.section.getDouble("minRep");
 					Advanced.section = settings.getConfigurationSection("advanced");
 					Advanced.pkg = Advanced.section.getString("package");
 					Advanced.suffix = Advanced.section.getString("suffix");
 				}
 				catch (NullPointerException e) {
 					Main.LOG.severe("[ColeBans] Your config file is outdated! Please delete it to regenerate it!");
 					Main.LOG.severe("[ColeBans] COULD NOT LOAD WORKING CONFIG FILE. Aborting operation.");
 					Main.instance.onFatal();
 				}
 			}
 			else {
 				File dir = new File("./plugins/ColeBans");
 				dir.mkdir();
 				if (confFile.createNewFile()) {
 					if (confFile.canWrite()) {
 						System.out.println("[ColeBans] No config file exists, generating.");
 						FileOutputStream fos = new FileOutputStream(confFile);
 						String defaultConfig = ""+
 								"# For information on how to configure ColeBans, go to http://c2wr.com/cbconf\n"+
 								"settings:\n"+
 								"    allowTempBans: true\n"+
 								"    banMessage: You are banned for %reason!\n"+
 								"    tempBanMessage: You are tempbanned! %time minute%plural remaining!\n"+
 								"    fancyEffects: true\n"+
 								"    banColor: DARK_RED\n"+
 								"    kickColor: YELLOW\n"+
 								"    tempBanColor: RED\n"+
 								"    announceBansAndKicks: true\n"+
 								"    logPrefix: [ColeBans]\n"+
 								"    #banHandler can be MySQL, MCBans, YAML, or JSON.\n"+
 								"    banHandler: YAML\n"+
 								"    mysql:\n"+
 								"        user: root\n"+
 								"        pass: pass\n"+
 								"        host: localhost\n"+
 								"        port: 3306\n"+
 								"        db: minecraft\n"+
 								"        prefix: cb_\n"+
 								"    mcbans:\n"+
 								"        ###### THIS LINE IS VERY VERY IMPORTANT IF YOU CHOSE MCBANS FOR THE BAN HANDLER ######\n"+
 								"        apiKey: yourAPIKeyHere\n"+
 								"        # Set this to the BanHandler you want to use for the backups, or \"None\" to turn off backups.\n"+
 								"        backup: None\n"+
 								"        fullBackups: false\n"+
 								"        #The minimum rep a player can have to join your server.\n"+
 								"        minRep: 10\n"+
 								"    yaml:\n"+
 								"        fileName: banlist.yml\n"+
 								"    json:\n"+
 								"        fileName: banlist.json\n"+
 								"    advanced:\n" +
 								"        # The package is where to get the ban handlers. Only change this line if you know what you are doing.\n" +
 								"        package: com.cole2sworld.ColeBans.handlers\n" +
 								"        #The suffix is what is at the end of all the ban handlers. Only change this line if you know what you are doing.\n" +
 								"        suffix: BanHandler";
 						fos.write(defaultConfig.getBytes("utf-8"));
 						loadConfig();
 						fos.close();
 					}
 					return;
 				}
 				else {
 					Main.LOG.severe("[ColeBans] COULD NOT LOAD WORKING CONFIG FILE. Aborting operation.");
 					Main.instance.onFatal();
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 	}
 }
