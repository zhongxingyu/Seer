 package com.runetooncraft.plugins.EasyMobArmory;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.runetooncraft.plugins.EasyMobArmory.core.Messenger;
 
 
 public class Commandlistener implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player p = (Player) sender;
		if(commandLabel.equalsIgnoreCase("cdt")) {
 			if(args.length == 0) {
 				Messenger.playermessage("Usage: /EMA enable, /EMA disable", p);
 			}else if(args.length == 1) {
 				if(p.hasPermission("EMA.use")) {
 					if(args[0].equalsIgnoreCase("enable")) {EMAListener.Armoryenabled.put(p, true);}
 					if(args[0].equalsIgnoreCase("disable")) {EMAListener.Armoryenabled.put(p, false);}
 				}else{
 					Messenger.playermessage("You do not have permission for this command", p);
 				}
 			}
 		}
 		return false;
 	}
 
 }
