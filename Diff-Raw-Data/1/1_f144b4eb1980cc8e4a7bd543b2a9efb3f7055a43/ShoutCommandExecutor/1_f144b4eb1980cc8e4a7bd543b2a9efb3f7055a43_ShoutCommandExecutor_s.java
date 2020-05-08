 package com.pixelvent.bukkit.shout.commands;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.pixelvent.bukkit.shout.Shout;
 
 public class ShoutCommandExecutor implements CommandExecutor
 {
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if(args.length > 0)
 		{
 			if(args[0].equalsIgnoreCase("reload"))
 			{
 				Shout.instance.reloadPublicConfig();
 				
 				sender.sendMessage(ChatColor.GREEN + "Configuration file reloaded!");
 			}
 			else if(args[0].equalsIgnoreCase("interval"))
 			{
 				if(args.length == 2)
 				{
 					if(StringUtils.isNumeric(args[1]))
 					{
 						int interval = Integer.parseInt(args[1]);
 						
 						Shout.instance.publicConfig.set("settings.announceInterval", interval);
 						Shout.instance.saveConfig();
 						Shout.instance.reloadPublicConfig();
 						
 						String secondsText = " seconds";
 						if(interval == 1)
 						{
 							secondsText = " second";
 						}
 						
 						sender.sendMessage(ChatColor.GREEN + "Announcement interval set to " + interval + secondsText);
 					}
 					else
 					{
 						sender.sendMessage(ChatColor.RED + "Argument is not a number!");
 						
 						sender.sendMessage(ChatColor.RED + "Command usage:\n" + ChatColor.RESET + "/shout interval <SECONDS> - Sets the announcement interval time in seconds");
 					}
 				}
 				else
 				{
 					sender.sendMessage(ChatColor.RED + "Invalid arguments!");
 					
 					sender.sendMessage(ChatColor.RED + "Command usage:\n" + ChatColor.RESET + "/shout interval <SECONDS> - Sets the announcement interval time in seconds");
 				}
 			}
 			else if(args[0].equalsIgnoreCase("add"))
 			{
 				if(args.length > 1)
 				{
 					List<String> configAnnouncmentList = Shout.instance.publicConfig.getStringList("messages");
 					
 					String message = "";
 					
 					for(int i = 1; i < args.length; i++)
 					{
 						if(i == 1)
 						{
 							message += args[i];	
 						}
 						else
 						{
 							message += " " + args[i];
 						}
 					}
 					
 					Shout.instance.announcementsList.add(message);
 					configAnnouncmentList.add(message);
 					Shout.instance.publicConfig.set("messages", configAnnouncmentList);
 					Shout.instance.saveConfig();
 					
 					sender.sendMessage(ChatColor.GREEN + "Announcement message added!");
 				}
 				else
 				{
 					sender.sendMessage(ChatColor.RED + "No message was entered!");
 					
 					sender.sendMessage(ChatColor.RED + "Command usage:\n" + ChatColor.RESET + "/shout add <MESSAGE> - Adds the specified message to the message list in the config file");
 				}
 			}
 			else if(args[0].equalsIgnoreCase("togglerandom"))
 			{
 				Shout.instance.publicConfig.set("settings.random", !Shout.instance.publicConfig.getBoolean("settings.random"));
 				Shout.instance.reloadPublicConfig();
 				
 				sender.sendMessage(ChatColor.GREEN + "Random message order option set to: " + Shout.instance.publicConfig.getBoolean("settings.random"));
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED + "Invalid arguments!");
 				
 				sender.sendMessage(ChatColor.RED + "Command usage:\n" + ChatColor.RESET + "/shout reload - Reloads the shout configuration file\n/shout interval <SECONDS> - Sets the announcement interval time in seconds\n/shout add <MESSAGE> - Adds the specified message to the message list in the config file\n/shout togglerandom - Toggles the random message order option");
 			}
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.RED + "Missing arguments!");
 			
 			sender.sendMessage(ChatColor.RED + "Command usage:\n" + ChatColor.RESET + "/shout reload - Reloads the shout configuration file\n/shout interval <SECONDS> - Sets the announcement interval time in seconds\n/shout add <MESSAGE> - Adds the specified message to the message list in the config file\n/shout togglerandom - Toggles the random message order option");
 		}
 		
 		return true;
 	}
 }
