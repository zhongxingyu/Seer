 package com.rakosmanjr.InfTools;
 
 import java.util.*;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class InfTools extends JavaPlugin
 {
 	Logger log;
 	public Map<String, Boolean> isInfTool;
 	PlayerInteractListener pil;
 	FileConfiguration config;
 	
 	public void onEnable ()
 	{	
 		log = this.getLogger();
 		log.info("InfTools has been enabled.");
 		
 		this.isInfTool = new HashMap<String, Boolean>();
 		this.pil = new PlayerInteractListener(this);
 		
 		config = this.getConfig();
 		
 		for (String p : config.getKeys(false))
 		{
 			isInfTool.put(p, config.getBoolean(p));
 		}
 		
 		this.getServer().getPluginManager().registerEvents(pil, this);
 	}
 	
 	public void onDisable ()
 	{
 		for (String p : isInfTool.keySet())
 		{
 			config.set(p, isInfTool.get(p));
 		}
 		
 		this.saveConfig();
 		
 		log.info("InfTools has been disabled.");
 	}
 	
 	public boolean onCommand (CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		Player player = null;
 		
 		if (sender instanceof Player)
 		{
 			player = (Player)sender;
 		}
 		
 		//command is /inftool
 		if (player != null && player.hasPermission("inftools.inftool"))
 		{
 			if (cmd.getName().equalsIgnoreCase("inftool"))
 			{
 				if (args.length == 0)
 				{
 					this.isInfTool.put(player.getName(), this.isInfTool.containsKey(player.getName()) ? !this.isInfTool.get(player.getName()) : true);
					player.sendMessage("InfTools is " + (isInfTool.get(player.getName()) ? "on" : "off"));
					return true;
 				}
 				else
 				{
 					sender.sendMessage(ChatColor.RED
 							+ "Incorrect number of arguments!");
 				}
 			}
 		}
 		
 		return false;
 	}
 }
