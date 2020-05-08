 package com.adamki11s.commands;
 
import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
import com.adamki11s.questx.QuestX;

 public class QPerms {
 	
 	public static boolean hasPermission(Player p, String perm){
 		if(p.isOp()){
 			return true;
 		} else if(p.hasPermission("questx.*")){
 			return true;
 		} else {
			if(p.hasPermission(perm)){
				return true;
			} else {
				QuestX.logChatError(p, ChatColor.RED + "You do not have permission to do this.");
				QuestX.logChatError(p, ChatColor.RED + "Permission node needed = " + ChatColor.YELLOW + perm);
				return false;
			}
 		}
 	}
 
 }
