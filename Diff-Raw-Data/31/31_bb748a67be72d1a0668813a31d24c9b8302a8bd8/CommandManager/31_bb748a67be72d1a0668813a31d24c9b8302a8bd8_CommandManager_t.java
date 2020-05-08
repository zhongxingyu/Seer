 /**
  *  Name: CommandManager.java
  *  Date: 19:55:50 - 12 okt 2012
  * 
  *  Author: LucasEmanuel @ bukkit forums
  *  
  *  
  *  Description:
  *  
  *  
  *  
  * 
  * 
  */
 
 package me.lucasemanuel.crownconquest.managers;
 
 import me.lucasemanuel.crownconquest.Main;
 import me.lucasemanuel.crownconquest.utils.ConsoleLogger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CommandManager implements CommandExecutor {
 	
 	private Main plugin;
 	private ConsoleLogger logger;
 	
 	public CommandManager(Main instance) {
 		plugin = instance;
 		logger = new ConsoleLogger(instance, "CommandExecutor");
 		
 		logger.debug("Initiated");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		
 		String command = cmd.getName().toLowerCase();
 		
 		switch(command) {
 			
 			case "startcq":
 				plugin.start();
				plugin.getServer().broadcastMessage(ChatColor.GOLD + "PVP aktiveras om " + ChatColor.LIGHT_PURPLE + "2 minuter!");
 				return true;
 				
 			case "stopcq":
 				plugin.stop();
 				return true;
 				
 			case "setcrownspawn":
 				if(!isPlayer(sender)) return true;
 				
 				plugin.getCrownManager().setSpawn(((Player)sender).getLocation());
 				sender.sendMessage(ChatColor.GREEN + "Plats sparad!");
 				
 				return true;
 				
 			case "setteamspawn":
 				if(!isPlayer(sender)) return true;
 				if(args.length != 1) return false;
 				
 				plugin.getTeamManager().setSpawn(((Player)sender).getLocation(), args[0]);
 				sender.sendMessage(ChatColor.GREEN + "Lagplats sparad med ID: " + ChatColor.LIGHT_PURPLE + args[0]);
 				
 				return true;
 				
 			case "spawncrown":
 				plugin.getCrownManager().spawnCrown();
 				return true;
 				
 			case "teams":
 				plugin.getTeamManager().sendTeamInfo(sender);
 				return true;
 				
 			case "setspectatorspawn":
 				if(!isPlayer(sender)) return true;
				sender.sendMessage(ChatColor.GREEN + "Spectator plats sparad!");
 				plugin.getLocationManager().setSpectatorSpawn(((Player)sender).getLocation());
 				return true;
 				
 			case "savecq":
 				sender.sendMessage(ChatColor.GREEN + "Saving data...");
 				
 				plugin.getTeamManager().saveData();
 				plugin.getLocationManager().saveSpectatorSpawn();
 				
 				return true;
 		}
 		
 		return false;
 	}
 	
 	private boolean isPlayer(CommandSender sender) {
 		if(!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED + "Du mste vara spelare fr detta kommando!");
 			return false;
 		}
 		return true;
 	}
 }
