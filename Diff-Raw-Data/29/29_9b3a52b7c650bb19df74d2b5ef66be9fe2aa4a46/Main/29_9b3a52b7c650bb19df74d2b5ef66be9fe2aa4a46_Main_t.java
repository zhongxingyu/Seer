 package org.vanillaworld.CustomMusicDiscs;
 
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin implements Listener {
 	
 	public static JavaPlugin plugin;
 	
 	public void onEnable()
 	{
 		plugin = this;
 		Setup.setupFolders(); // Setup the folder structure
 	}
 	
 	public void onDisable()
 	{
 		plugin = null;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if(cmd.getName().equalsIgnoreCase("music"))
 		{
			if(args.length == 3)
			{
				if(args[0].equalsIgnoreCase("give"))
				{
					Player p = Bukkit.getPlayerExact(args[1]);
					if(p == null)
					{
						sender.sendMessage(ChatColor.RED + "The player must be online to give a music disc!");
					}
					else
					{
						
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "Invaild command. Correct usage: /music give <player> <disc>");
				}
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "To many/few args. Correct usage: /music give <player> <disc>");
			}
 			return true;
 		}
 		return false; 
 	}
 
 }
