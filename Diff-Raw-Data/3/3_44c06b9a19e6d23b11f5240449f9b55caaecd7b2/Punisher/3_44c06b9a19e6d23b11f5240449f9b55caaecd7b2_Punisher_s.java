 package com.minecraftserver.warn;
 
 import java.util.List;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.Server;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.defaults.KickCommand;
 import org.bukkit.command.defaults.BanCommand;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class Punisher {
 
 	private static final Server Server = Bukkit.getServer();
 	int Warnings;
 	static Command kick = new KickCommand();
 	static Command ban = new BanCommand();
 	static Command tempban = Bukkit.getPluginCommand("tempban");
 	static Command jail = Bukkit.getPluginCommand("jail");
 	static Command mute = Bukkit.getPluginCommand("mute");
 	static CommandSender Console = (Bukkit.getServer().getConsoleSender());
 
 	public static boolean punish(Player target, int warnings, String reason, CommandSender sender, YamlConfiguration config) {
 		String playername = target.getName();
 		if (reason == null) {
 			reason = "You have been warned!";
 		}
 		
 		String command = config.getString("warning." + warnings + ".command");
 		if (command != null)
 //			if (command.equalsIgnoreCase("mute")){
 //				if (reason.toLowerCase().contains("spam")){
 //					if (!sender.hasPermission("warner.punish.mute")){
 //						return false;
 //					}
 //					String duration = config.getString("warning." + warnings + ".duration");
 //					if (duration == null)
 //						duration = "30 min";
 //					String[] mute_arg = {playername, duration};
 //					mute.execute(Console, "mute", mute_arg);
 //					sender.sendMessage(target.getName() + " has been muted for" + duration);
 //				}
 //			}
 			if (command.equalsIgnoreCase("jail")){
 				if (!sender.hasPermission("warner.punish.jail")){
 					return false;
 				}
 				String duration = config.getString("warning." + warnings + ".duration");
 				if (duration == null){
 					duration = "30 min";
 				}
 				String jail_number = config.getString("warning." + warnings + ".jail_number");
 				if (jail_number == null){
 					sender.sendMessage(ChatColor.RED + "No jail has been specified in config file.");
 				}
 				String[] jail_arg = {playername, jail_number, duration};
 				jail.execute(Console, "jail", jail_arg);
 				return true;
 			} else if (command.equalsIgnoreCase("kick")) {
 				if (!sender.hasPermission("warner.punish.kick")){
 					return false;
 				}
 				String[] kick_arg = { playername, "You have been warned by " + sender.getName() + " for: " + reason + ". [" + warnings + "]" };
 				kick.execute(Console, "kick", kick_arg);
 				return true;
 			} else if (command.equalsIgnoreCase("tempban")) {
 				if (!sender.hasPermission("warner.punish.tempban")){
 					return false;
 				}
 				String duration = config.getString("warning." + warnings + ".duration");
 				if (duration == null)
 					duration = "3 Day";
 				String[] tempban_arg = { playername, duration };
 				tempban.execute(Console, "tempban", tempban_arg);
 				return true;
 			} else if (command.equalsIgnoreCase("ban")) {
 				if (!sender.hasPermission("warner.punish.ban")){
 					return false;
 				}
 				String[] ban_arg = { playername, "You have been warned by " + sender.getName() + " for: " + reason + ". [" + warnings + "]" };
 				ban.execute(Console, "ban", ban_arg);
 				return true;
 			}
 		return false;
 	}
 }
