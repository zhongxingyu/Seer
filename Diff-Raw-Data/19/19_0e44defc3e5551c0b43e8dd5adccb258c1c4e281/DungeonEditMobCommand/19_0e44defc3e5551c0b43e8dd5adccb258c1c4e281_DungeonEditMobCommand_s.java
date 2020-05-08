 package com.imdeity.deitydungeons.cmd.dungeon;
 
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 import com.imdeity.deityapi.DeityAPI;
 import com.imdeity.deityapi.api.DeityCommandReceiver;
 import com.imdeity.deitydungeons.DeityDungeons;
 import com.imdeity.deitydungeons.DungeonManager;
 import com.imdeity.deitydungeons.obj.ArmorMaterial;
 import com.imdeity.deitydungeons.obj.ArmorPiece;
 import com.imdeity.deitydungeons.obj.Dungeon;
 import com.imdeity.deitydungeons.obj.Mob;
 
 public class DungeonEditMobCommand extends DeityCommandReceiver {
 
 	@Override
 	public boolean onConsoleRunCommand(String[] arg0) {
 		return false;
 	}
 
 	@Override
 	public boolean onPlayerRunCommand(Player player, String[] args) {
 		//dungeon editmob dungeon mobID helm chest leggings boots health type target
 		//dungeon editmob TestDungeon 5 A G I L 40 zombie false
 		//armor types do not need to be checked because they will default to air
 
 		//Manually entering a dungeon
 		if(args.length == 10 &&
 				DungeonManager.dungeonExists(args[0]) && //dungeon
 				DeityDungeons.isInt(args[1]) &&	//mob id
 				DungeonManager.getDungeonByName(args[0]).hasMob(Integer.parseInt(args[1])) && //mob
 				DeityDungeons.isInt(args[6]) && //health
 				EntityType.valueOf(args[7]) != null && //type
 				(args[8].equalsIgnoreCase("t") || args[8].equalsIgnoreCase("f") || args[8].equalsIgnoreCase("true") || args[8].equalsIgnoreCase("false")) && //target
 				DeityDungeons.isInt(args[9])){ //weapon id 
 
 			Dungeon dungeon = DungeonManager.getDungeonByName(args[0]);
 			Mob mob = dungeon.getMobByID(Integer.parseInt(args[1]));
 
 			DungeonManager.setMobArmor(mob, ArmorMaterial.getByChar(args[2].toUpperCase().charAt(0)), ArmorPiece.HELMET);
 			DungeonManager.setMobArmor(mob, ArmorMaterial.getByChar(args[3].toUpperCase().charAt(0)), ArmorPiece.CHESTPLATE);
 			DungeonManager.setMobArmor(mob, ArmorMaterial.getByChar(args[4].toUpperCase().charAt(0)), ArmorPiece.LEGGINGS);
 			DungeonManager.setMobArmor(mob, ArmorMaterial.getByChar(args[5].toUpperCase().charAt(0)), ArmorPiece.BOOTS);
 
 			DungeonManager.setMobHealth(mob, Integer.parseInt(args[6]));
 
 			DungeonManager.setMobType(mob, EntityType.fromName(args[7]));
 
 			String f = args[8];
 
 			if(f.equalsIgnoreCase("true") || f.equalsIgnoreCase("t")) {
 				DungeonManager.setMobTarget(mob, true);
 			}else if(f.equalsIgnoreCase("false") || f.equalsIgnoreCase("f")){
 				DungeonManager.setMobTarget(mob, false);
 			}else{
 				DeityAPI.getAPI().getChatAPI().sendPlayerError(player, "DeityDungeons", "Usage: /dungeon editmob <dungeon> <mobid> <helm> <chest> <legs> <boots> <health> <type> <target> <weapon id>");
 			}
 			
 			DungeonManager.setMobWeapon(mob, Integer.parseInt(args[9]));
 			
 			DeityAPI.getAPI().getChatAPI().sendPlayerMessage(player, "DeityDungeons", "<green>The mob attributes have been set for mob with id <red>" + args[1]);
 			return true;
 
 			//Already having a dungeon selected
 		}else if(args.length == 9 &&
 				DungeonManager.playerHasDungeon(player) &&
 				DeityDungeons.isInt(args[0]) &&	//mob id
 				DungeonManager.getPlayersDungeon(player).hasMob(Integer.parseInt(args[0])) && //mob
 				DeityDungeons.isInt(args[5]) && //health
 				EntityType.valueOf(args[6]) != null && //type
 				(args[7].equalsIgnoreCase("t") || args[7].equalsIgnoreCase("f") || args[7].equalsIgnoreCase("true") || args[7].equalsIgnoreCase("false")) && //target
 				DeityDungeons.isInt(args[8])) {
 			
 			Dungeon dungeon = DungeonManager.getPlayersDungeon(player);
 			Mob mob = dungeon.getMobByID(Integer.parseInt(args[0]));
 
 			DungeonManager.setMobArmor(mob, ArmorMaterial.getByChar(args[1].toUpperCase().charAt(0)), ArmorPiece.HELMET);
 			DungeonManager.setMobArmor(mob, ArmorMaterial.getByChar(args[2].toUpperCase().charAt(0)), ArmorPiece.CHESTPLATE);
 			DungeonManager.setMobArmor(mob, ArmorMaterial.getByChar(args[3].toUpperCase().charAt(0)), ArmorPiece.LEGGINGS);
 			DungeonManager.setMobArmor(mob, ArmorMaterial.getByChar(args[4].toUpperCase().charAt(0)), ArmorPiece.BOOTS);
 
 			DungeonManager.setMobHealth(mob, Integer.parseInt(args[5]));
 
 			DungeonManager.setMobType(mob, EntityType.valueOf(args[6]));
 
 			String f = args[7];
 
 			if(f.equalsIgnoreCase("true") || f.equalsIgnoreCase("t")) {
 				DungeonManager.setMobTarget(mob, true);
 			}else if(f.equalsIgnoreCase("false") || f.equalsIgnoreCase("f")){
 				DungeonManager.setMobTarget(mob, false);
 			}else{
 				DeityAPI.getAPI().getChatAPI().sendPlayerError(player, "DeityDungeons", "Usage: /dungeon editmob <dungeon> <mobid> <helm> <chest> <legs> <boots> <health> <type> <target> <weapon id>");
 			}
 			
 			DungeonManager.setMobWeapon(mob, Integer.parseInt(args[8]));
 			
 			DeityAPI.getAPI().getChatAPI().sendPlayerMessage(player, "DeityDungeons", "<green>The mob attributes have been set for mob with id <red>" + args[1]);
 			return true;
 
 		}
 		
 		DeityAPI.getAPI().getChatAPI().sendPlayerError(player, "DeityDungeons", "Usage: /dungeon editmob <dungeon> <mobid> <helm> <chest> <legs> <boots> <health> <type> <target> <weapon id>");
 		return true;
 	}
 
 }
