 package com.hackhalo2.tp.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.hackhalo2.tp.TPToggle;
 import com.hackhalo2.tp.utils.TPPlayer;
 import com.hackhalo2.tp.utils.TPUtil;
 
 public class TPHCommand implements CommandExecutor {
 
 	private final TPToggle plugin;
 
 	public TPHCommand(TPToggle instance) {
 		this.plugin = instance;
 	}
 
 	public boolean onCommand(CommandSender cs, Command c, String l, String[] args) {
 		if(!(cs instanceof Player)) return true; //The Console can't TPHere
 		Player origin = (Player)cs;
 		TPPlayer tpOrigin = this.plugin.getTPPlayer(origin);
 		if(origin.hasPermission("tptoggle.request")) {
 			if(args.length == 0) { //This is to respond to /tpc requests
 				String temp = tpOrigin.getRequestName();
 				if(temp != null) {
 					Player target = Bukkit.getPlayerExact(temp);
 					this.plugin.removeWorker(tpOrigin, target.getName());
 					return TPUtil.TPTargetToOrigin(origin, target);
 				} else {
 					origin.sendMessage(ChatColor.AQUA+"Your request queue is empty");
 					return true;
 				}
 			} else if(args.length == 1) {
 				if(Bukkit.getPlayer(args[0]) != null) {
 					Player target = Bukkit.getPlayer(args[0]);
 					TPPlayer tpTarget = this.plugin.getTPPlayer(target);
 					if(tpOrigin.isRequesting(target.getName()) || tpOrigin.getOverrideStatus()) {
						this.plugin.removeWorker(this.plugin.getTPPlayer(origin), target.getName());
						return TPUtil.TPTargetToOrigin(origin, target);
 					} else if(tpTarget.canBeTPHTo(origin) && !tpTarget.isTPBanned(origin.getName())) {
 						if(tpTarget.alert() || !origin.canSee(target)) {
 							tpTarget.addRequestName(origin.getName());
 							origin.sendMessage(ChatColor.AQUA+"Sending teleport request to "+ChatColor.GREEN+target.getName());
 							target.sendMessage(ChatColor.GRAY+origin.getName()+" would like to TP to you. Type /tp #answer to bring them to you");
 							this.plugin.addWorker(tpTarget, origin.getName());
 						} else {
 							origin.sendMessage(ChatColor.AQUA+"Player "+ChatColor.GREEN+target.getName()+ChatColor.AQUA+" is currently in "+ChatColor.GREEN+"DND mode");
 						}
 						return true;
 					} else if(tpTarget.isTPBanned(origin.getName())) {
 						origin.sendMessage(ChatColor.AQUA+"You are not allowed to teleport "+ChatColor.GREEN+target.getName());
 						return true;
 					}
 				} else {
 					origin.sendMessage(ChatColor.AQUA+"Unable to find a match for string "+ChatColor.GREEN+args[0]+ChatColor.AQUA+" (Was null or invalid)");
 					return true;
 				}
 			}
 		}
 		return true;
 	}
 
 }
