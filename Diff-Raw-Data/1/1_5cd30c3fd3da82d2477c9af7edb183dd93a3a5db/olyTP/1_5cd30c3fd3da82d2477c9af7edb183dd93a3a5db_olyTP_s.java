 package com.olympuspvp.tp;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class olyTP extends JavaPlugin{
 
 	final String tag = ChatColor.GOLD + "[" + ChatColor.YELLOW + "olyTP" + ChatColor.GOLD+"] " + ChatColor.YELLOW;
 
 	@Override
 	public void onEnable(){
 		System.out.println("[olyTP] olyTP by willno123 (badass coderleetzors) has been enabled!");
 	}
 
 	public boolean onCommand(CommandSender s, Command cmd, String c, String[] args){
 		if(s instanceof Player == false){
 			System.out.println("[olyTP] Go home console, you're drunk.");
 			return true;
 		}Player p = (Player)s;
 		if((p.isOp()==false)&&(p.hasPermission("olyTP.use")==false)){
 			p.sendMessage(tag+"You do not have permission to use this command.");
 		}
 		if(args.length==1){
 			String to = args[0];
 			List<Player> matches = Bukkit.matchPlayer(to);
 			if(matches.size() !=1){
 				p.sendMessage(tag+"The player could not be found.");
 				return true;
 			}Player pto=matches.get(0);
 			if(p==pto){
 				p.sendMessage(tag+"You cannot teleport to yourself.");
 				return true;
 			}p.teleport(pto, TeleportCause.PLUGIN);
 			p.sendMessage(tag + "Teleporting you to " + ChatColor.GOLD + pto.getName());
 			if(p.isOp() || p.hasPermission("olyTP.use")) pto.sendMessage(tag+ ChatColor.GOLD + p.getName() + ChatColor.YELLOW +  " teleported to you.");
 		}else if(args.length == 2){
 			String to = args[1];
 			String from = args[0];
 			List<Player> matches = Bukkit.matchPlayer(to);
 			if(matches.size() !=1){
 				p.sendMessage(tag + to + " could not be found.");
 				return true;
 			}
 			Player pto = matches.get(0);
 			matches = Bukkit.matchPlayer(from);
 			if(matches.size() !=1){
 				p.sendMessage(tag + from + " could not be found.");
 			}
 			Player pfrom = matches.get(0);
 			pfrom.teleport(pto, TeleportCause.PLUGIN);
 			pfrom.sendMessage(tag + "Teleporting you to " + ChatColor.GOLD + pto.getName());
 			if(!p.equals(pfrom) && !(p.equals(pto))) p.sendMessage(tag + "Teleporting " + ChatColor.GOLD + pfrom.getName() + ChatColor.YELLOW + " to " + ChatColor.GOLD + pto.getName());
 			pto.sendMessage(tag + ChatColor.GOLD + pfrom.getName() + ChatColor.YELLOW + " has been teleported to you.");
 			return true;
 		}else{
 			p.sendMessage(tag + "Incorrect usage. /tp {player} {player}");
 			return true;
 		}return true;
 	}
 }
