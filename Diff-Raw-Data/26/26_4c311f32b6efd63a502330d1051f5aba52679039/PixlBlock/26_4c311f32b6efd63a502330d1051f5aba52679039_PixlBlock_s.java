 package com.hackhalo2.creative;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.inventory.ItemStack;
 
 public class PixlBlock extends BlockListener {
     private final Pixl plugin;
 
     public PixlBlock(final Pixl p) { this.plugin = p; }
 
     @Override
     public void onBlockDamage(BlockDamageEvent e) {
 	Player player = e.getPlayer();
 
 	Block block = e.getBlock();
 	if (block.getType() == Material.BEDROCK) {
 	    // Can't break bedrock with PixlBreak.
 	    return;
	} else if (!plugin.breakMode(player)) {
	    // PixlBreak isn't active for this player.
 	    return;
 	} else if (!plugin.isPickaxe(player.getItemInHand().getType())) {
 	    // The player isn't holding a pick.
 	    return;
 	} else if (!player.hasPermission("pixl.break")) {
 	    // This player is no longer allowed to use PixlBreak.
 	    return;
 	}
 
 	// Check to see if the material type can be PixlBroken by limited users.
 	Material type = block.getType();
 	boolean limited_ok = false;
 	for (Material available : plugin.limitedUserMaterials) {
 	    if (available.equals(type)) {
 		limited_ok = true;
 		break;
 	    }
 	}
 
 	// Check for special case blocks
 	for (Material available : plugin.specialCaseMaterials) {
 	    if (available.equals(type)) {
 		if(plugin.shatterMode(player)) {
 		    e.getBlock().setType(Material.AIR);
 		} else {
		    e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), new ItemStack(available));
 		}
 		return;
 	    }
 	}
 
 	// If the player has appropriate permissions, instabreak the block.
 	if (limited_ok || !player.hasPermission("pixl.break.limited")) {
 	    if(plugin.shatterMode(player)) {
 		e.getBlock().setType(Material.AIR);
 	    } else {
 		e.setInstaBreak(true);
 	    }
 	}
     }
 }
