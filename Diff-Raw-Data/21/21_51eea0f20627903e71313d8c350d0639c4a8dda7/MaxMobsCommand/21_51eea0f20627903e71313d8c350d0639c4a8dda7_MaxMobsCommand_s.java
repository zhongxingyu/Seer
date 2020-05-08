 package com.github.thebiologist13.commands.spawners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.thebiologist13.CustomSpawners;
 import com.github.thebiologist13.Spawner;
 import com.github.thebiologist13.commands.SpawnerCommand;
 
 public class MaxMobsCommand extends SpawnerCommand {
 
 	private CustomSpawners plugin;
 	
 	public MaxMobsCommand(CustomSpawners plugin) {
 		this.plugin = plugin;
 	}
 	
 	public void run(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
 		//Player
 		Player p = null;
 		
 		//Spawner ID
 		int id = -1;
 		
 		//Spawner
 		Spawner s = null;
 		
 		//Maximum mobs
 		int maxMobs = -1;
 		
 		//Check to make sure the command is from a player
 		if(!(arg0 instanceof Player)) {
 			plugin.log.info(NO_CONSOLE);
 			return;
 		}
 		
 		p = (Player) arg0;
 		
 		//Permission checking
 		if(p.hasPermission("customspawners.spawners.setmaxmobs")) {
 			
 			//If the player wants to set the max mobs of a selected spawner
 			if(CustomSpawners.spawnerSelection.containsKey(p) && arg3.length == 2) {
 				
 				s = plugin.getSpawnerById(CustomSpawners.spawnerSelection.get(p));
 				
 				if(!plugin.isInteger(arg3[1])) {
 					p.sendMessage(SPECIFY_NUMBER);
 					return;
 				}
 				
 				maxMobs = Integer.parseInt(arg3[1]);
 				
 			//If no spawner is selected, but arguments for a selected spawner were put in.
 			} else if(arg3.length == 2) {
 				
 				p.sendMessage(NEEDS_SELECTION);
 				
 			//If the player wants to set the max mobs of a spawner by ID
 			} else if(arg3.length == 3) {
 				
 				if(!plugin.isInteger(arg3[1])) {
 					p.sendMessage(ID_NOT_NUMBER);
 					return;
 				}
 				
 				id = Integer.parseInt(arg3[1]);
 				
 				if(!plugin.isValidSpawner(id)) {
 					p.sendMessage(NO_ID);
 					return;
 				}
 				
 				s = plugin.getSpawnerById(id);
 				
 				if(!plugin.isInteger(arg3[2])) {
 					p.sendMessage(SPECIFY_NUMBER);
 					return;
 				}
 				
 				maxMobs = Integer.parseInt(arg3[2]);
 			
 			//General error message
 			} else {
 				
 				plugin.log.info(GENERAL_ERROR);
 				return;
 				
 			}
 			
 			if(maxMobs > plugin.getCustomConfig().getDouble("spawners.maxMobsLimit", 128) || maxMobs < 0) {
 				if(!p.hasPermission("customspawners.spawners.limitoverride")) {
 					p.sendMessage(INVALID_VALUES);
 					return;
 				}
 			}
 			
 			//Set the new max mobs
 			s.setMaxMobs(maxMobs);
 			
 			//Success message
 			p.sendMessage(ChatColor.GREEN + "Set the maximum mobs of the spawner with ID "+ ChatColor.GOLD + 
 					String.valueOf(s.getId()) + ChatColor.GREEN + " to " + ChatColor.GOLD + String.valueOf(maxMobs) + 
 					ChatColor.GREEN + "!");
 		} else {
 			p.sendMessage(NO_PERMISSION);
 			return;
 		}
 	}
 }
