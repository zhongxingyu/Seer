 package com.github.thebiologist13.commands.spawners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.thebiologist13.CustomSpawners;
 import com.github.thebiologist13.commands.SpawnerCommand;
 
 public class RemoveCommand extends SpawnerCommand {
 
 	private CustomSpawners plugin;
 	
 	public RemoveCommand(CustomSpawners plugin) {
 		this.plugin = plugin;
 	}
 	
 	public void run(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
 		//Player
 		Player p = null;
 		//ID of spawner to remove
 		int removeId = -1;
 		
 		//Check that sender is a player
 		if(!(arg0 instanceof Player)) {
 			plugin.log.info(NO_CONSOLE);
 			return;
 		}
 		
 		p = (Player) arg0;
 		
 		//Check permissions
 		if(p.hasPermission("customspawners.spawners.remove")) {
 			
 			//If the player wants to remove the selected spawner
 			if(CustomSpawners.spawnerSelection.containsKey(p) && arg3.length == 1) {
 				
 				removeId = CustomSpawners.spawnerSelection.get(p);
 			
 			//When the arg3 length is 1, but no spawner is selected.
 			} else if(arg3.length == 1){
 					
 					p.sendMessage(NEEDS_SELECTION);
 					return;
 					
 			//If the player want to remove a spawner with a specified ID
 			} else if(arg3.length == 2) {
 				
 				//Check that the ID entered is a number
 				if(!plugin.isInteger(arg3[1])) {
 					p.sendMessage(ID_NOT_NUMBER);
 					return;
 				}
 				
 				removeId = Integer.parseInt(arg3[1]);
 				
 				//Check if the ID entered is the ID of a spawner
 				if(!plugin.isValidSpawner(removeId)) {
 					p.sendMessage(NO_ID);
 					return;
 				}
 			
 			//General error message
 			} else {
 				
 				plugin.log.info(GENERAL_ERROR);
 				return;
 				
 			}
 			
 			//Remove the spawner by calling the remove() method
 			plugin.getSpawnerById(removeId).remove();
 			plugin.removeDataFile(removeId, true);
 			
			for(Player p1 : CustomSpawners.spawnerSelection.keySet()) {
 				if(CustomSpawners.spawnerSelection.containsKey(p1)) {
 					if(p1.isOnline()) {
 						if(p1 != p) {
 							p1.sendMessage(ChatColor.GOLD + "Your selected spawner has been removed by another player.");
 						}
 					}
 					CustomSpawners.spawnerSelection.remove(p1);
 				}
			}
 			
 			//Send success message
 			p.sendMessage(ChatColor.GREEN + "Successfully removed spawner with ID " + ChatColor.GOLD +
 					String.valueOf(removeId) + ChatColor.GREEN + "!");
 		} else {
 			p.sendMessage(NO_PERMISSION);
 			return;
 		}
 	}
 }
