 package com.github.CorporateCraft.necessities.Commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import com.github.CorporateCraft.necessities.*;
 
 public class CmdPermissions
 {
 	public static boolean CommandUse(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (sender instanceof Player)
 		{
 			Player player = (Player) sender;
 			if (args.length != 1)
	        		{
	      	  		 	return false;
	        		}
 			Command com;
 			com = player.getServer().getPluginCommand(args[0]);
 			if(com == null)
 			{
 				player.sendMessage(Necessities.messages + "The command " + args[0] + " is a nonexistant or built in command permissions not able to read built in yet.");
 				return true;
 			}
 			player.sendMessage(Necessities.messages + com.getPermission());
 			return true;
 		}
 		else
 		{
 			if (args.length != 1)
	        	{
	      	  		 return false;
	       		}
 			Command com = sender.getServer().getPluginCommand(args[0]);
 			if(com == null)
 			{
 				sender.sendMessage(Necessities.messages + "The command " + args[0] + " is a nonexistant or built in command permissions not able to read built in yet.");
 				return true;
 			}
 			sender.sendMessage(Necessities.messages + com.getPermission().toString());
 			return true;
 		}
 	}
 }
