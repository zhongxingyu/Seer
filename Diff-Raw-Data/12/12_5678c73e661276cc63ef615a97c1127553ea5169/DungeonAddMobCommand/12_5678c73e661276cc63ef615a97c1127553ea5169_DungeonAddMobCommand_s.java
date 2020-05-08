 package com.imdeity.deitydungeons.cmd.dungeon;
 
 import java.sql.SQLDataException;
 
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 import com.imdeity.deityapi.DeityAPI;
 import com.imdeity.deityapi.api.DeityCommandReceiver;
 import com.imdeity.deityapi.records.DatabaseResults;
 import com.imdeity.deitydungeons.DeityDungeons;
 import com.imdeity.deitydungeons.DungeonManager;
 import com.imdeity.deitydungeons.obj.ArmorMaterial;
 import com.imdeity.deitydungeons.obj.Dungeon;
 import com.imdeity.deitydungeons.obj.Mob;
 
 public class DungeonAddMobCommand extends DeityCommandReceiver {
 
 	@Override
 	public boolean onConsoleRunCommand(String[] args) {
 		return false;
 	}
 
 	@Override
 	public boolean onPlayerRunCommand(Player player, String[] args) {
 		//dungeon addmob dungeon skeleton 5			3
 		//dungeon addmob dungeon zombie				2
 
 		//dungeon addmob Zombie 5					2
 		//dungeon addmob Skeleton					1
 
 		if(args.length < 1) {
 			DeityAPI.getAPI().getChatAPI().sendPlayerError(player, "DeityDungeons", "Usage: /dungeon addmob [dungeon] <type> [amount]");
 			return true;
 		}
 
 
 		//get next mob id
 		//temporarily default to 1
 		//after the mob row is inserted, we will update with the correct id
 		//safe to use 0 as auto_increment always starts at 1
 		int id = 0;
 
 
 		if(DungeonManager.dungeonExists(args[0]) && EntityType.fromName(args[1]) != null) {
 			Dungeon dungeon = DungeonManager.getDungeonByName(args[0]);
 			EntityType type = EntityType.fromName(args[1]);
 
 
 			Mob mob = new Mob(id, type, 0, ArmorMaterial.AIR, ArmorMaterial.AIR, ArmorMaterial.AIR, ArmorMaterial.AIR, dungeon, player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(), false, 1, Material.AIR);
 
 			if(args.length == 2) { //dungeon addmob dungeon zombie
 
 
 				DungeonManager.addMobToDungeon(mob);
 				
 				mob.setID(getID());
 
 				DeityAPI.getAPI().getChatAPI().sendPlayerMessage(player, "DeityDungeons", "<green>Your requested mobs were added to the dungeon");
 				return true;
 
 
 			}else if(args.length == 3 && DeityDungeons.isInt(args[2])) { //dungeon addmob dungeon zombie 5
 
 
 				mob = new Mob(id, type, 0, ArmorMaterial.AIR, ArmorMaterial.AIR, ArmorMaterial.AIR, ArmorMaterial.AIR, dungeon, player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(), false, Integer.parseInt(args[2]), Material.AIR);
 
 
 				DungeonManager.addMobToDungeon(mob);
 				
 				mob.setID(getID());
 
 
 				DeityAPI.getAPI().getChatAPI().sendPlayerMessage(player, "DeityDungeons", "<green>Your requested mobs were added to the dungeon");
 				return true;
 
 			}else{
 				DeityAPI.getAPI().getChatAPI().sendPlayerError(player, "DeityDungeons", "Usage: /dungeon addmob [dungeon] <type> [amount]");
 				return true;
 			}
 
 
 		}else if(DungeonManager.playerHasDungeon(player) && EntityType.fromName(args[0]) != null){
 			Dungeon dungeon = DungeonManager.getPlayersDungeon(player);
 			EntityType type = EntityType.fromName(args[0]);
 
 
 			Mob mob = new Mob(id, type, 0, ArmorMaterial.AIR, ArmorMaterial.AIR, ArmorMaterial.AIR, ArmorMaterial.AIR, dungeon, player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(), false, 1, Material.AIR);
 
 			if(args.length == 1) { //dungeon addmob zombie 
 
 				DungeonManager.addMobToDungeon(mob);
 				
 				mob.setID(getID());
 
 				DeityAPI.getAPI().getChatAPI().sendPlayerMessage(player, "DeityDungeons", "<green>Your requested mobs were added to the dungeon");
 				return true;
 
 			}else if(args.length == 2 && DeityDungeons.isInt(args[1])) { //dungeon addmob zombie 5
 
 
 				mob = new Mob(id, type, 0, ArmorMaterial.AIR, ArmorMaterial.AIR, ArmorMaterial.AIR, ArmorMaterial.AIR, dungeon, player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(), false, Integer.parseInt(args[1]), Material.AIR);
 
 				DungeonManager.addMobToDungeon(mob);
 				
 				mob.setID(getID());
 
 
 				DeityAPI.getAPI().getChatAPI().sendPlayerMessage(player, "DeityDungeons", "<green>Your requested mobs were added to the dungeon");
 				return true;
 
 
 			}else{
 				DeityAPI.getAPI().getChatAPI().sendPlayerError(player, "DeityDungeons", "Usage: /dungeon addmob [dungeon] <type> [amount]");
 				return true;
 			}
 		}else{
 			DeityAPI.getAPI().getChatAPI().sendPlayerError(player, "DeityDungeons", "Usage: /dungeon addmob [dungeon] <type> [amount]");
 			return true;
 		}
 	}
 	
 	//When we make this query there will always be at least one row in the table
 	private int getID() {
 		int id = 1;
 
 		//see if there is a row in the table, if there is set the dungeonID
 		DatabaseResults results = DeityAPI.getAPI().getDataAPI().getMySQL().readEnhanced("SELECT * FROM `dungeon_info` ORDER BY `id` DESC LIMIT 1");
 
 		if(results != null && results.hasRows()) {
 			try {
				id = results.getInteger(0, "id") + 1; //this is the highest id, we will add 1 and set the id of the mob we just created to this
 				return id;
 			} catch (SQLDataException e) {
 				DeityAPI.getAPI().getChatAPI().out("DeityDungeons", "There was an SQL error while adding a mob to a dungeon");
 				e.printStackTrace();
 				
 			}
 		} 
 		
 		return id;	
 		
 	}
 }
