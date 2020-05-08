 package com.mitsugaru.Karmiconomy.config;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import com.mitsugaru.Karmiconomy.Karmiconomy;
 
 public class LocalizeConfig
 {
 	// Class variables
 	private static Karmiconomy plugin;
 	private static File file;
 	private static YamlConfiguration config;
	public static String lackMessage, reasonMoney, reasonLimit,
 			reasonUnknown, econFailure, unknownCommand, noPermission, helpHelp,
 			helpAdminReload, helpVersion, reloadConfig, localMessage;
 
 	public static void init(Karmiconomy kcon)
 	{
 		plugin = kcon;
 		file = new File(plugin.getDataFolder().getAbsolutePath()
 				+ "/localization.yml");
 		config = YamlConfiguration.loadConfiguration(file);
 		loadDefaults();
 		loadVariables();
 	}
 
 	public static void save()
 	{
 		// Set config
 		try
 		{
 			// Save the file
 			config.save(file);
 		}
 		catch (IOException e1)
 		{
 			plugin.getLogger().warning(
 					"File I/O Exception on saving localization config");
 			e1.printStackTrace();
 		}
 	}
 
 	public static void reload()
 	{
 		try
 		{
 			config.load(file);
 		}
 		catch (FileNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		catch (InvalidConfigurationException e)
 		{
 			e.printStackTrace();
 		}
 		loadDefaults();
 		loadVariables();
 	}
 
 	private static void loadDefaults()
 	{
 		// LinkedHashmap of defaults
 		final Map<String, String> defaults = new LinkedHashMap<String, String>();
 		// defaults for all strings
 		defaults.put("message.localMessage",
 				"&a%tag Paid &6$%amount&a for &b%event &c%extra");
 		defaults.put("message.econFailure",
 				"&c%tag Could not pay &6%amount &cfor &b%event");
 		defaults.put("message.lackEvent",
 				"&c%tag %reason for action: &b%event &c%extra");
 		defaults.put("message.noPermission", "&c%tag Lack permission: %extra");
 		defaults.put("message.reloadConfig", "&a%tag &fConfig reloaded.");
 		defaults.put("message.unknownCommand",
 				"&c%tag Unknown command '&6%extra&c'. Bad syntax.");
 		defaults.put("help.help", "&a/kcon help&e : Show help menu");
 		defaults.put("help.admin.reload",
 				"&a/kcon reload&e : Reload all config files");
 		defaults.put("help.version",
 				"&a/kcon version&e : Show version and config");
 		defaults.put("reason.limit", "Hit limit");
 		defaults.put("reason.money", "Lack money");
 		defaults.put("reason.unknown", " Unknown DenyType");
 		// TODO debug messages
 		// Add to config if missing
 		for (final Entry<String, String> e : defaults.entrySet())
 		{
 			if (!config.contains(e.getKey()))
 			{
 				config.set(e.getKey(), e.getValue());
 			}
 		}
 		save();
 	}
 
 	private static void loadVariables()
 	{
 		// load variables
 		/**
 		 * Messages
 		 */
 		lackMessage = config.getString("message.lack",
 				"&c%tag %reason for action: &b%event &c%extra");
 		econFailure = config.getString("message.econFailure",
 				"&c%tag Could not pay &6%amount &cfor &b%event");
 		noPermission = config.getString("message.noPermission",
 				"&c%tag Lack permission: %extra");
 		reloadConfig = config.getString("message.reloadConfig",
 				"&a%tag &fConfig reloaded.");
 		localMessage = config.getString("message.localMessage",
				"&a%tag Paid &6$%amount&a for &b%event &c%extra");;
 		/**
 		 * help
 		 */
 		helpHelp = config.getString("help.help",
 				"&a/kcon help&e : Show help menu");
 		helpAdminReload = config.getString("help.admin.reload",
 				"&a/kcon reload&e : Reload all config files");
 		helpVersion = config.getString("help.version",
 				"&a/kcon version&e : Show version and config");
 		/**
 		 * Reasons
 		 */
 		reasonLimit = config.getString("reason.money", "Hit limit");
 		reasonMoney = config.getString("reason.money", "Lack money");
 		reasonUnknown = config.getString("reason.unknown", " Unknown DenyType");
 	}
 }
