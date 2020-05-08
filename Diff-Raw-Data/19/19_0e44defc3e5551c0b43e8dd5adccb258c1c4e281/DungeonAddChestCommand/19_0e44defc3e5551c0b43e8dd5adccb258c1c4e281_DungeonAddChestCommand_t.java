 package com.imdeity.deitydungeons.cmd.dungeon;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.imdeity.deityapi.DeityAPI;
 import com.imdeity.deityapi.api.DeityCommandReceiver;
 import com.imdeity.deitydungeons.DungeonManager;
 import com.imdeity.deitydungeons.obj.Dungeon;
 
@SuppressWarnings("deprecation")
 public class DungeonAddChestCommand extends DeityCommandReceiver {
 
 	@Override
 	public boolean onConsoleRunCommand(String[] args) {
 		return false;
 	}
 
 	@Override
 	public boolean onPlayerRunCommand(Player player, String[] args) {
 		if(args.length != 1) {
 			DeityAPI.getAPI().getChatAPI().sendPlayerMessage(player, "DeityDungeons", "<red>Usage: /dungeon addchest <dungeon name>");
 			return true;
 		}
 		
 		if(!DungeonManager.dungeonExists(args[0])) {
 			DeityAPI.getAPI().getChatAPI().sendPlayerMessage(player, "DeityDungeons", "<red>The dungeon <green>" + args[0] + " <red>does not exist!");
 			return true;
 		}
 		
 		Dungeon dungeon = DungeonManager.getDungeonByName(args[0]);
 		
 		Block block = player.getTargetBlock(null, 0);
 
 		if(player.getTargetBlock(null, 0).getType() != Material.CHEST) {
 			DeityAPI.getAPI().getChatAPI().sendPlayerMessage(player, "DeityDungeons", "<red>You must be looking at a chest");
 			return true;
 		}
 		
 		Chest chest;
 		if(block.getState() instanceof Chest) {
 			chest = (Chest)block.getState();
 		}else{
 			DeityAPI.getAPI().getChatAPI().sendPlayerMessage(player, "DeityDungeons", "<red>Error accessing chest");
 			return true;
 		}		
 		
 		ItemStack[] items = chest.getBlockInventory().getContents();
 		
 		DungeonManager.addChest(dungeon, player, chest, items);
 		
 		return true;
 	}
 
 }
