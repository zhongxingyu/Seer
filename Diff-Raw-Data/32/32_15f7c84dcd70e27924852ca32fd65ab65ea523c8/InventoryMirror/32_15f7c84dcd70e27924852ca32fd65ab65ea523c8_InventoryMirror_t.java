 package com.turt2live.antishare;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.metadata.FixedMetadataValue;
 
 public class InventoryMirror {
 
	// from = target, to = viewer
 	public static void mirror(Player from, Player to, AntiShare plugin){
 		//Find where the chest can be placed
 		Block block = to.getTargetBlock(null, 5);
 		if(block == null){
 			Location location = to.getLocation();
 			location.setY(location.getY() - 1);
 			block = to.getWorld().getBlockAt(location);
 		}
 		//Convert it to a chest
 		Location location = block.getLocation();
 		location.setX(location.getX() + 1);
 		Block otherBlock = block.getLocation().getWorld().getBlockAt(location);
 		block.setType(Material.CHEST);
 		otherBlock.setType(Material.CHEST);
 		Chest chest = (Chest) block.getState();
 		// Set inventory and key for listener
 		chest.getInventory().setContents(from.getInventory().getContents());
 		block.setMetadata("invmirror", new FixedMetadataValue(plugin, to.getName()));
 		otherBlock.setMetadata("invmirror", new FixedMetadataValue(plugin, to.getName()));
 		ASUtils.sendToPlayer(to, ChatColor.YELLOW + "A chest has spawned with " + from.getName() + "'s inventory.");
 		ASUtils.sendToPlayer(to, ChatColor.YELLOW + "Break the chest when done, it will not spew items.");
		System.out.println("Called");
 	}
 }
