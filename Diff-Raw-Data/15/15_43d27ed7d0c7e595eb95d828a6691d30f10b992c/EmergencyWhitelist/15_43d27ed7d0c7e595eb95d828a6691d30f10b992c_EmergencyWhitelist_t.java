 package com.emergencywhitelist;
 
 import java.io.File;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.emergencywhitelist.listeners.EWLListener;
 
 public class EmergencyWhitelist extends JavaPlugin
 {
 	public static boolean isEnabled;
 	ChatColor yellowChat = ChatColor.YELLOW;
 	ChatColor goldChat = ChatColor.GOLD;
 	ChatColor boldChat = ChatColor.BOLD;
 	public String noPermission = "[EmergencyWhitelist] You do not have permission for this command.";
 	
 	File configFile;
 	FileConfiguration config;
 	
 	public Logger logger()
 	{
 		return getLogger();
 	}
 	
 	public void onEnable()
 	{
 		getServer().getPluginManager().registerEvents(new EWLListener(this), this);
 		
 		configFile = new File(getDataFolder(), "config.yml");
 		saveDefaultConfig();
 		config = new YamlConfiguration();
 		try
 		{
 			config.load(configFile);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		isEnabled = this.config.getBoolean("Enabled");
 		
 		if (isEnabled == true)
 		{
 			logger().info("Whitelist is currently enabled.");
 			logger().info("Use /ewl toggle to disable the whitelist.");
 			
 			Bukkit.broadcastMessage(goldChat + "[EmergencyWhitelist] Whitelist enabled. Kicking non-whitelisted players.");
 
 			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					Player[] players = Bukkit.getOnlinePlayers();
 					for (Player player : players)
 					{
 						if (!player.hasPermission("EmergencyWhitelist.whitelist"))
 							player.kickPlayer("Server Whitelist has been enabled.");
 					}
 				}
 			}, 100L);
 		}
 		else if (isEnabled == false)
 		{
 			logger().info("Whitelist is currently disabled.");
 		}
 			
 	}
 	
 	public void onDisable()
 	{
 		logger().info("Shutting down.");
 	}
 	
 	@EventHandler
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		try
 		{
 			if (command.getName().equalsIgnoreCase("EWL"))
 			{
 				if (args[0].equalsIgnoreCase("Help"))
 				{
					if (!sender.hasPermission("emergencywhitelist.help") == true)
 					{
 						sender.sendMessage(goldChat + noPermission);
 						return true;
 					}
 					else
 					{
 						sender.sendMessage(ChatColor.WHITE + "---------" + goldChat + "EmergencyWhitelist" + ChatColor.WHITE + "---------");
 						sender.sendMessage(goldChat + "/ewl toggle: " + ChatColor.WHITE + "Toggles the server whitelist.");
 						sender.sendMessage(goldChat + "/ewl version: " + ChatColor.WHITE + "Shows the plugin's version number.");
 						return true;
 					}
 				}
 				else if (args[0].equalsIgnoreCase("Toggle"))
 				{
					if (!sender.hasPermission("emergencywhitelist.toggle") == true)
 					{
 						sender.sendMessage(goldChat + noPermission);
 						return true;
 					}
 					else
 					{
 						isEnabled = !isEnabled;
 						
 						getConfig().set("Enabled", isEnabled);
 						saveConfig();
 						reloadConfig();
 						
 						if (isEnabled == true)
 						{
 							Bukkit.broadcastMessage(goldChat + "[EmergencyWhitelist] Whitelist enabled. Kicking non-whitelisted players.");
 
 							getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
 							{
 								@Override
 								public void run()
 								{
 									Player[] players = Bukkit.getOnlinePlayers();
 									for (Player player : players)
 									{
 										if (!player.hasPermission("EmergencyWhitelist.whitelist"))
 											player.kickPlayer("Server Whitelist has been enabled.");
 									}
 								}
 							}, 100L);
 							
 						}	
 						else if (isEnabled == false)
 							sender.sendMessage(goldChat + "[EmergencyWhitelist] Whitelist disabled.");
 						return true;
 					}
 				}
 				else if (args[0].equalsIgnoreCase("Version"))
 				{
					if (!sender.hasPermission("emergencywhitelist.version") == true)
 					{
 						sender.sendMessage(goldChat + noPermission);
 						return true;
 					}
 					else
 					{
 						PluginDescriptionFile pdf = getDescription();
 						sender.sendMessage(goldChat + "[EmergencyWhitelist] Version " + pdf.getVersion() + " for Craftbukkit 1.4.5-R1.0.");
 						return true;
 					}
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			PluginDescriptionFile pdf = getDescription();
 			if (isEnabled == false)
 				sender.sendMessage(goldChat + "[EmergencyWhitelist] EWL v"+ pdf.getVersion() + " is disabled.");
 			else if (isEnabled == true)
 				sender.sendMessage(goldChat + "[EmergencyWhitelist] EWL v"+ pdf.getVersion() + " is enabled.");
 			sender.sendMessage(goldChat + "[EmergencyWhitelist] Type /ewl help for a list of commands.");
 		}
 		return false;
 	}
 	
 }
