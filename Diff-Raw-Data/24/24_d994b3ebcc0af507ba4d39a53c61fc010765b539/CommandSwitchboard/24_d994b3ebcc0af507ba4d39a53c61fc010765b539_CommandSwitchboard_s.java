 package com.deaboy.manhunt.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
  * This class takes the Manhunt command and determines where it should be redirected.
  * @author Deaboy
  *
  */
 public class CommandSwitchboard implements CommandExecutor
 {
 	
 	public CommandSwitchboard()
 	{
 		Bukkit.getPluginCommand("manhunt").setExecutor(this);
 		Bukkit.getPluginCommand("mstartgame").setExecutor(this);
 		Bukkit.getPluginCommand("mstopgame").setExecutor(this);
 	}
 	
 	public boolean onCommand(CommandSender sender, Command c, String cmd, String[] arguments)
 	{
 
		if (cmd.equalsIgnoreCase("manhunt"))
 			return HelpCommands.manhunt(sender, arguments);
 		
		if (cmd.equalsIgnoreCase("mstartgame"))
 			return LobbyCommands.mstartgame(sender, arguments);
 		
		if (cmd.equalsIgnoreCase("mstopgame"))
 			return LobbyCommands.mstopgame(sender, arguments);
 		
 		
 		
 		return false;
 		
 	}
 	
 	
 	
 }
