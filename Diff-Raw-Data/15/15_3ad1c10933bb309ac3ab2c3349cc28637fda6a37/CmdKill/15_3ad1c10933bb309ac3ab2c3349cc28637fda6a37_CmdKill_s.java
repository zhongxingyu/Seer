 package com.github.CorporateCraft.necessities.Commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 //import com.github.CorporateCraft.necessities.*;
 
 public class CmdKill
 {
 	public static boolean CommandUse(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (sender instanceof Player)
 		{
			Player target = sender.getServer().getPlayer(args[0]);
 			if(args.length == 0)
 			{
 				return false;
 			}
 			if(args.length == 1)
 			{
 				target.setHealth(0);
 				Bukkit.broadcastMessage(target.getName() + " died.");
 				return true;
 			}
 			int i = 1;
 			String DeathMessage = target.getName() + " ";
 			while (i < args.length)
 			{
 				DeathMessage += args[i] + " ";
 				i++;
 			}        	   
 			DeathMessage = DeathMessage.trim();
 			target.setHealth(0);
 			Bukkit.broadcastMessage(DeathMessage);
 			return true;
         } 
 		else
 		{
			Player target = sender.getServer().getPlayer(args[0]);
 			if(args.length == 0)
 			{
 				return false;
 			}
 			if(args.length == 1)
 			{
 				target.setHealth(0);
 				Bukkit.broadcastMessage(target.getName() + " died.");
 				return true;
 			}
 			int i = 1;
 			String DeathMessage = target.getName() + " ";
 			while (i < args.length)
 			{
 				DeathMessage += args[i] + " ";
 				i++;
 			}        	   
 			DeathMessage = DeathMessage.trim();
 			target.setHealth(0);
 			Bukkit.broadcastMessage(DeathMessage);
         	return true;
 	    }
 	}
 }
