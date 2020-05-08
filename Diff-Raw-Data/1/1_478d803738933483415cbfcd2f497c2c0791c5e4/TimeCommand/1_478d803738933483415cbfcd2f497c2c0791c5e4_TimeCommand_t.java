 package com.caindonaghey.commandbin.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.caindonaghey.commandbin.Phrases;
 
 public class TimeCommand implements CommandExecutor {
 	
 	public static boolean isLockRunning = false;
 	public static String worldName;
 	public static long worldTime;
 	
 	public boolean onCommand(CommandSender s, Command c, String l, String [] args) {
 		if(l.equalsIgnoreCase("time")) {
 			if(!(s instanceof Player)) {
 				if(args.length < 2) {
 					System.out.println("[CommandBin] " + Phrases.get("invalid-arguments"));
 					return false;
 				}
 				
 				World world = Bukkit.getServer().getWorld(args[0]);
 				if(world == null) {
 					System.out.println("[CommandBin] " + Phrases.get("invalid-world"));
 					return true;
 				}
 				
 				if(args[1].equalsIgnoreCase("day")) {
 					world.setTime(0);
 					System.out.println("[CommandBin] " + Phrases.get("time-set") + args[1].toLowerCase());
 					return true;
 				}
 				
 				if(args[1].equalsIgnoreCase("night")) {
 					world.setTime(14400);
 					System.out.println("[CommandBin] " + Phrases.get("time-set") + args[1].toLowerCase());
 					return true;
 				}
 				
 				if(args[1].equalsIgnoreCase("lock")) {
 					if(!isLockRunning) {
 						isLockRunning = true;
 						worldName = world.getName();
 						worldTime = world.getTime();
 						System.out.println("[CommandBin] " + Phrases.get("time-locked"));
 						return true;
 					}
 					isLockRunning = false;
 					System.out.println("[CommandBin] " + Phrases.get("time-unlocked"));
 					return true;
 				}
 			}
 			
 			Player player = (Player) s;
 			
 			if(!player.hasPermission("CommandBin.time")) {
 				player.sendMessage(Phrases.get("no-permission"));
 				return true;
 			}
 			
 			if(args.length < 1) {
 				player.sendMessage(Phrases.get("invalid-arguments"));
				return false;
 			}
 			
 			if(args[0].equalsIgnoreCase("day")) {
 				player.getWorld().setTime(0);
 				player.sendMessage(Phrases.get("time-set") + args[0].toLowerCase());
 				return true;
 			}
 			
 			if(args[0].equalsIgnoreCase("night")) {
 				player.getWorld().setTime(14400);
 				player.sendMessage(Phrases.get("time-set") + args[0].toLowerCase());
 				return true;
 			}
 			
 			if(args[0].equalsIgnoreCase("lock")) {
 				if(!isLockRunning) {
 					isLockRunning = true;
 					worldName = player.getWorld().getName();
 					worldTime = player.getWorld().getTime();
 					player.sendMessage(Phrases.get("time-locked"));
 					return true;
 				}
 				isLockRunning = false;
 				player.sendMessage(Phrases.get("time-unlocked"));
 				return true;
 			}
 			return true;
 		}
 		return true;
 	}
 
 }
