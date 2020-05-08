 package com.bitlimit.pulse.bukkit;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.logging.Level;
 
 public class PulsePlugin extends JavaPlugin
 {
 	public Level broadcastLevel;
 
 	@Override
 	public void onEnable()
 	{
 		this.handleDefaults();
 
 		this.broadcastLevel = Level.parse((String)this.getConfig().get("broadcastLevel"));
 
 		this.recordCondition(this, false, ChatColor.DARK_GREEN +
 				"\n " + ChatColor.DARK_RED + "Pulse 0.1" + ChatColor.DARK_GREEN + " /\\\n" +
 				"____/\\   /  \\      ______\n" +
 				"         \\/     \\   /\n" +
 				"                  \\/" + ChatColor.DARK_GRAY + "    Vital.\n", Level.FINEST);
 
 
		this.recordCondition(this, "Loaded.", Level.FINE);
 	}
 
 	@Override
 	public void onDisable()
 	{
 		this.saveConfig();
 	}
 
 	@Override
 	public void saveConfig() {
 		this.getConfig().set("broadcastLevel", this.broadcastLevel.toString());
 
 		super.saveConfig();
 	}
 
 	private void handleDefaults()
 	{
 		this.getConfig().addDefault("broadcastLevel", Level.ALL.toString());
 	}
 
 	public void setBroadcastLevel(Level newLevel)
 	{
 		this.broadcastLevel = newLevel;
 
 		this.saveConfig();
 	}
 
 	public void recordCondition(Plugin plugin, String condition, Level level)
 	{
 		this.recordCondition(plugin, true, condition, level);
 	}
 
 	public void recordCondition(Plugin plugin, boolean shouldPrefix, String condition, Level level)
 	{
 		String content = ChatColor.AQUA + condition;
 		String message = content;
 		String prefix = ChatColor.GRAY + "[" + plugin.getName() + ChatColor.GRAY + "] ";
 
 		if (shouldPrefix)
 		{
 			message = prefix + content;
 		}
 
 		if (level.intValue() <= this.broadcastLevel.intValue())
 		{
 			Bukkit.broadcast(message, "pulse.observe");
 		}
 	}
 
 	public void notePluginCondition(Plugin plugin, PluginCondition pluginCondition, boolean success, String additionalInformation)
 	{
 		this.recordCondition(plugin, PulsePlugin.getDescriptionForPluginCondition(pluginCondition, success, additionalInformation), PulsePlugin.getLevelForPluginCondition(pluginCondition, success));
 	}
 
 	public static String getDescriptionForPluginCondition(PluginCondition pluginCondition, boolean success, String description) {
 		String message = "";
 		switch (pluginCondition) {
 			case LOADED: {
 				if (success) {
 					message = "loaded successfully.";
 				} else {
 					message = "failed to load.";
 				}
 			}
 			case UNLOADED: {
 				if (success) {
 					message = "unloaded successfully.";
 				} else {
 					message = "failed to unload.";
 				}
 			}
 			case CREATED_PREFERENCES: {
 				if (success) {
 					message = "successfully initialized preferences.";
 				} else {
 					message = "failed to initialize preferences.";
 				}
 			}
 			case LOADED_PREFERENCES: {
 				if (success) {
 					message = "successfully loaded preferences.";
 				} else {
 					message = "failed to load preferences.";
 				}
 			}
 			case RECEIVED_COMMAND: {
 				if (success) {
 					message = "received and will execute command \"" + description + "\"";
 				} else {
 					message = "received and will not execute command \"" + description + "\"";
 				}
 			}
 			case EXECUTED_COMMAND: {
 				if (success) {
 					message = "successfully executed intended command \"" + description + "\"";
 				} else {
 					message = "failed to executed intended command \"" + description + "\"";
 				}
 			}
 			case EVENT_LISTENED: {
 				if (success) {
 					message = "observed event \"" + description + "\" and intends to react.";
 				} else {
 					message = "observed event \"" + description + "\" but does not intend to react.";
 				}
 			}
 		}
 
 		if (success) {
 			message = ChatColor.AQUA + message;
 		} else {
 			message = ChatColor.RED + message;
 		}
 
 		return message;
 	}
 
 	public static Level getLevelForPluginCondition(PluginCondition pluginCondition, boolean success)
 	{
 		if (!success)
 		{
 			switch (pluginCondition)
 			{
 				case LOADED:
 				{
 					return Level.SEVERE;
 				}
 				case UNLOADED:
 				{
 					return Level.SEVERE;
 				}
 				case CREATED_PREFERENCES:
 				{
 					return Level.WARNING;
 				}
 				case LOADED_PREFERENCES:
 				{
 					return Level.WARNING;
 				}
 				case RECEIVED_COMMAND:
 				{
 					return Level.FINE;
 				}
 				case EXECUTED_COMMAND:
 				{
 					return Level.SEVERE;
 				}
 				case EVENT_LISTENED:
 				{
 					return Level.FINEST;
 				}
 			}
 		}
 		else
 		{
 			switch (pluginCondition)
 			{
 				case LOADED:
 				{
 					return Level.INFO;
 				}
 				case UNLOADED:
 				{
 					return Level.INFO;
 				}
 				case CREATED_PREFERENCES:
 				{
 					return Level.FINE;
 				}
 				case LOADED_PREFERENCES:
 				{
 					return Level.FINE;
 				}
 				case RECEIVED_COMMAND:
 				{
 					return Level.FINE;
 				}
 				case EXECUTED_COMMAND:
 				{
 					return Level.INFO;
 				}
 				case EVENT_LISTENED:
 				{
 					return Level.FINEST;
 				}
 			}
 		}
 
 		return Level.FINEST;
 	}
 }
