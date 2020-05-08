 package com.hackhalo2.tp.utils;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 import com.hackhalo2.tp.TPToggle;
 
 public class TPUtil {
 	
 	private static TPToggle plugin;
 	
 	public static void setPlugin(TPToggle instance) {
 		TPUtil.plugin = instance;
 	}
 
 	public static boolean TPTargetToOrigin(Player origin, Player target) {
 		//Get the TPPlayer references, because we are going to use them alot
 		TPPlayer tpOrigin = plugin.getTPPlayer(origin);
 		TPPlayer tpTarget = plugin.getTPPlayer(target);
 		
 		if(tpOrigin.getOverrideStatus()) {
 			target.sendMessage(ChatColor.GRAY+"Teleporting you to "+ChatColor.GREEN+origin.getName());
 			tpTarget.setRecallPosition(target.getLocation());
 			return target.teleport(origin, TeleportCause.COMMAND);
 		}
 		
 		//Thirdly, check to see if Origin has automatic override permissions
 		if(!origin.hasPermission("tptoggle.override")) {
 
 			//Forthly, check to see if the target has banned the origin from Teleporting to them
 			if(tpOrigin.isTPBanned(target.getName())) {
 				target.sendMessage(ChatColor.GRAY+"You are not allowed to teleport to "+ChatColor.GREEN+origin.getName());
 				return false;
 			}
 
 			//Fifthly, check alert status
			if(!tpTarget.alert()) {
				origin.sendMessage(ChatColor.AQUA+"Player "+ChatColor.GREEN+target.getName()+ChatColor.AQUA+" is currently in "+ChatColor.GREEN+"DND mode");
 				return false;
 			}
 
 		}
 		
 		//If we got here, then we can teleport! :D
 		target.sendMessage(ChatColor.AQUA+"Teleporting to "+ChatColor.GREEN+target.getName());
 		tpTarget.setRecallPosition(target.getLocation());
 		return target.teleport(origin, TeleportCause.COMMAND);
 	}
 
 	public static boolean TPOriginToTarget(Player origin, Player target, boolean tpo) {
 		//Firstly, check canSee status on the players
 		if(origin.canSee(target) || origin.hasPermission("tptoggle.seer")) {
 			//Get the TPPlayer references, because we are going to use them alot
 			TPPlayer tpOrigin = plugin.getTPPlayer(origin);
 			TPPlayer tpTarget = plugin.getTPPlayer(target);
 
 			//Secondly, check Master Override status and allow if enabled
 			if(tpOrigin.getOverrideStatus()) {
 				origin.sendMessage(ChatColor.AQUA+"Teleporting to "+ChatColor.GREEN+target.getName());
 				if(target.canSee(origin)) target.sendMessage(ChatColor.GREEN+origin.getName()+ChatColor.GRAY+" teleported to you.");
 				tpOrigin.setRecallPosition(origin.getLocation());
 				return origin.teleport(target, TeleportCause.COMMAND);
 			}
 
 			//Thirdly, check to see if Origin has automatic override permissions
 			if(!origin.hasPermission("tptoggle.override")) {
 
 				//Forthly, check to see if the target has banned the origin from Teleporting to them
 				if(tpTarget.isTPBanned(origin.getName())) {
 					origin.sendMessage(ChatColor.AQUA+"You are not allowed to teleport to "+ChatColor.GREEN+target.getName());
 					return false;
 				}
 
 				//Fifthly, check alert status
 				if(!tpTarget.alert()) {
 					origin.sendMessage(ChatColor.AQUA+"Player "+ChatColor.GREEN+target.getName()+ChatColor.AQUA+" is currently in "+ChatColor.GREEN+"DND mode");
 					return false;
 				}
 
 			}
 
 			if(!tpo) { //Sixthly, check TPO status
 				//Lastly, check TPT status
 				if(tpTarget.isTPT()) {
 					tpTarget.addRequestName(origin.getName());
 					origin.sendMessage(ChatColor.AQUA+"Sending teleport request to "+ChatColor.GREEN+target.getName());
 					target.sendMessage(ChatColor.GRAY+origin.getName()+" would like to TP to you. Type /tph to bring them");
 					if(!origin.canSee(target)) target.sendMessage(ChatColor.GRAY+"Remember to become visible first!");
 					plugin.addWorker(tpTarget, origin.getName());
 					return false;
 				}
 			}
 
 			//If we got here, then we can teleport! :D
 			origin.sendMessage(ChatColor.AQUA+"Teleporting to "+ChatColor.GREEN+target.getName());
 			if(target.canSee(origin)) target.sendMessage(ChatColor.GREEN+origin.getName()+ChatColor.GRAY+" teleported to you.");
 			tpOrigin.setRecallPosition(origin.getLocation());
 			return origin.teleport(target, TeleportCause.COMMAND);
 		}
 		return false;
 	}
 
 }
