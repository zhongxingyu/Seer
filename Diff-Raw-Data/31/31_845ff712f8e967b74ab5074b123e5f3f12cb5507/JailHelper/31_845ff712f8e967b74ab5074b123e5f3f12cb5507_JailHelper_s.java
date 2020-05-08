 package com.andoutay.jailhelper;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class JailHelper extends JavaPlugin
 {
 	public static Logger log = Logger.getLogger("Minecraft");
 	public static String logPref = "[JailHelper] ";
 	public static String chPref = ChatColor.GOLD + logPref + ChatColor.RESET;
 	
 	public void onLoad()
 	{
 		new JHConfig(this);
 	}
 	
 	public void onEnable()
 	{
 		//disable JailHelper if Jail isn't found
 		if (getServer().getPluginManager().getPlugin("Jail") == null) getServer().getPluginManager().disablePlugin(this);
 		
 		getServer().getPluginManager().registerEvents(new JHEventHandler(this), this);
 		
 		JHUtils.setMsgTimeout(this);
 		
		JHConfig.onEnable();
 		log.info(logPref + "enabled");
 	}
 	
 	public void onDisable()
 	{
 		log.info(logPref + "disabled");
 	}
 
 	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args)
 	{
 		if (cmd.getName().equalsIgnoreCase("jailhelper")  && args.length == 1 && args[0].equalsIgnoreCase("reload"))
 		{
 			if(!(s instanceof ConsoleCommandSender || s.hasPermission("jailhelper.reload")))
 			{
 				s.sendMessage(chPref + ChatColor.RED + "Please contact your server administrator if you are supposed to have access to this command");
 				return true;
 			}
 			
 			JHConfig.reload();
 			s.sendMessage(chPref + "Config reloaded");
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("jailhelper") && args.length == 1 && args[0].equalsIgnoreCase("version"))
 		{
 			if (!(s instanceof ConsoleCommandSender || s.hasPermission("jailhelper.version")))
 			{
 				s.sendMessage(chPref + ChatColor.RED + "Please contact your server administrator if you are supposed to have access to this command");
 				return true;
 			}
 			
 			s.sendMessage(chPref + "Current version: " + getDescription().getVersion());
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("jailhelper") && args.length == 1 && args[0].equalsIgnoreCase("help"))
 		{
 			if (!(s instanceof ConsoleCommandSender || s.hasPermission("jailhelper.help")))
 			{
 				s.sendMessage(chPref + ChatColor.RED + "Please contact your server administrator if you are supposed to have access to this command");
 				return true;
 			}
 				
 			s.sendMessage(chPref + "Help");
 			s.sendMessage("/jailhelper version: Shows the current version of JailHelper");
 			s.sendMessage("/jailhelper reload: Reloads the config file");
 			s.sendMessage("/jailhelper help: Show this help message");
 			return true;
 		}
 		
 		return false;
 	}
 	
 
 }
