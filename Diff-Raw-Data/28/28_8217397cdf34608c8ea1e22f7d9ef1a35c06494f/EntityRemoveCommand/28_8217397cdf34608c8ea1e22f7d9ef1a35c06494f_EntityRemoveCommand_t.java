 package com.github.thebiologist13.commands.entities;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.thebiologist13.CustomSpawners;
 import com.github.thebiologist13.SpawnableEntity;
 import com.github.thebiologist13.Spawner;
 import com.github.thebiologist13.commands.SpawnerCommand;
 
 public class EntityRemoveCommand extends SpawnerCommand {
 
 	private CustomSpawners plugin = null;
 	
 	public EntityRemoveCommand(CustomSpawners plugin) {
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public void run(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
 		//Player
 		Player p = null;
 		//ID of entity to remove
 		int removeId = -1;
 		
 		//Check that sender is a player
 		if(!(arg0 instanceof Player)) {
 			plugin.log.info(NO_CONSOLE);
 			return;
 		}
 		
 		p = (Player) arg0;
 		
 		//Check permissions
 		if(p.hasPermission("customspawners.entities.remove")) {
 			
 			//If the player wants to remove the selected entity
 			if(CustomSpawners.entitySelection.containsKey(p) && arg3.length == 1) {
 				
 				removeId = CustomSpawners.entitySelection.get(p);
 			
 			//When the arg3 length is 1, but no entity is selected.
 			} else if(arg3.length == 1){
 					
 					p.sendMessage(NEEDS_SELECTION);
 					return;
 					
 			//If the player want to remove a entity with a specified ID
 			} else if(arg3.length == 2) {
 				
 				//Check that the ID entered is a number
 				if(!plugin.isInteger(arg3[1])) {
 					p.sendMessage(ID_NOT_NUMBER);
 					return;
 				}
 				
 				removeId = Integer.parseInt(arg3[1]);
 				
 				//Check if the ID entered is the ID of a entity
 				if(!plugin.isValidEntity(removeId)) {
 					p.sendMessage(NO_ID);
 					return;
 				}
 			
 			//General error message
 			} else {
 				
 				plugin.log.info(GENERAL_ERROR);
 				return;
 				
 			}
 			
 			//Remove the entity by calling the remove() method
 			plugin.getEntityById(removeId).remove();
 			plugin.removeDataFile(removeId, false);
 			
			/*for(Player p1 : CustomSpawners.entitySelection.keySet()) {
 				if(CustomSpawners.entitySelection.containsKey(p1)) {
 					if(p1.isOnline()) {
 						if(p1 != p) {
 							p1.sendMessage(ChatColor.GOLD + "Your selected entity has been removed by another player.");
 						}
 					}
 					CustomSpawners.entitySelection.remove(p1);
 				}
			}*/
 			
 			Iterator<Spawner> spawnerItr = CustomSpawners.spawners.iterator();
 			while(spawnerItr.hasNext()) {
 				Spawner s = spawnerItr.next();
 				
 				if(s.getTypeData().containsKey(removeId)) {
 					HashMap<Integer, SpawnableEntity> defaultEntity = new HashMap<Integer, SpawnableEntity>();
 					defaultEntity.put(CustomSpawners.defaultEntity.getId(), CustomSpawners.defaultEntity);
 					s.setTypeData(defaultEntity);
 					
 					p.sendMessage(ChatColor.GOLD + "The spawner with ID " + s.getId() + " has been set to the default entity due to removal.");
 				}
 			}
 			
 			//Send success message
 			p.sendMessage(ChatColor.GREEN + "Successfully removed entity with ID " + ChatColor.GOLD +
 					String.valueOf(removeId) + ChatColor.GREEN + "!");
 		} else {
 			p.sendMessage(NO_PERMISSION);
 			return;
 		}
 	}
 }
