 package com.adamki11s.commands;
 
 import org.bukkit.entity.Player;
 
 public class QPerms {
 	
 	public static boolean hasPermission(Player p, String perm){
 		if(p.isOp()){
 			return true;
 		} else if(p.hasPermission("questx.*")){
 			return true;
 		} else {
			return p.hasPermission(perm);
 		}
 	}
 
 }
