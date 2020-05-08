 package com.craftrepo.gimme;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 
 
 import com.craftrepo.gimme.gimmeConfiguration;
 
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.*;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 
 // permissions 2.4 imports
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 /**
 * CraftRepo Bans for Bukkit
  * @author AllGamer
  * 
  * Copyright 2011 AllGamer, LLC.
  * See LICENSE for licensing information.
  */
 
 public class gimme extends JavaPlugin
 {
 	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
 	private final Logger log = Logger.getLogger("Minecraft");
 	public static PermissionHandler Permissions = null;
 	public static String logPrefix = "gimme";
 	private gimmeConfiguration confSetup;
 	public gimme plugin;
 	public static Configuration config;
 	public static String id = null;
 
 	public gimme(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
 	{
 	}
 	public gimme(gimme plugin) 
 	{
 		this.plugin = plugin;
 	}
 
 	public void configInit()
 	{
 		getDataFolder().mkdirs();
 		config = new Configuration(new File(this.getDataFolder(), "config.yml"));
 		confSetup = new gimmeConfiguration(this.getDataFolder(), this);
 	}
 
 	public void setupPermissions() 
 	{
		Plugin agbs = this.getServer().getPluginManager().getPlugin("Permissions");
 		PluginDescriptionFile pdfFile = this.getDescription();
 
 		if (gimme.Permissions == null) 
 		{
			if (agbs != null) 
 			{
				this.getServer().getPluginManager().enablePlugin(agbs);
				gimme.Permissions = ((Permissions) agbs).getHandler();
 				log.info(logPrefix + " version " + pdfFile.getVersion() + " Permissions detected...");
 			}
 			else 
 			{
				log.severe(logPrefix + " version " + pdfFile.getVersion() + " not enabled. Permissions not detected");
 				this.getServer().getPluginManager().disablePlugin(this);
 			}
 		}
 	}
 
 	public boolean itemdeny(ItemStack args)
 	{
 		gimme.config.load();
 		String x = gimme.config.getNodeList("denied", null).toString();
 		if (args.toString().contains(x))
 			return true;
 
 		return false;
 	}
 
 	public boolean onCommand(CommandSender sender, Command commandArg, String commandLabel, String[] arg) 
 	{
 		ConcurrentHashMap<String[], ItemStack> stackmap = new ConcurrentHashMap<String[], ItemStack>();;
 		stackmap.put(arg, null);
 		ItemStack item = stackmap.get(arg);
 		Player player = (Player) sender;
 		String command = commandArg.getName().toLowerCase();
 		if (command.equalsIgnoreCase("gimme")) 
 		{
 			if (player.isOp() || gimme.Permissions.has(player, "gimme.gimme") || (gimme.Permissions.has(player, "gimme.*") || gimme.Permissions.has(player, "*"))) 
 			{
 				if (arg.length == 2) {
 					@SuppressWarnings("unused")
 					boolean check = itemdeny(item);
 					if (check = false)
 					{
 						player.sendMessage("Here you go!");
 						player.getInventory().addItem(item);
 					}
 				}
 				else
 				{
 					player.sendMessage("Correct usage is /gimme [item]");
 				}
 			} 
 			else 
 			{
 				player.sendMessage("You don't have access to this command.");
 				log.info(logPrefix + " - " + player.getDisplayName() + " tried to use command " + command + "! Denied access." );
 			}
 			return true;
 		}
 		return true;
 	}
 
 	public void onEnable() 
 	{
		// Register our events
		//PluginManager pm = getServer().getPluginManager();
 		setupPermissions();
 		configInit();
 		confSetup.setupConfigs();
 		log.info(logPrefix + " version " + this.getDescription().getVersion() + " enabled!");
 	}
 
 	public void onDisable() 
 	{

 		log.info(logPrefix + " version " + this.getDescription().getVersion() + " disabled!");
 	}
 
 	public boolean isDebugging(final Player player) 
 	{
 		if (debugees.containsKey(player)) 
		{
 			return debugees.get(player);
		} 
		else 
		{
			return false;
		}
 	}
 
 	public void setDebugging(final Player player, final boolean value) 
 	{
 		debugees.put(player, value);
 	}
 
 }
 
