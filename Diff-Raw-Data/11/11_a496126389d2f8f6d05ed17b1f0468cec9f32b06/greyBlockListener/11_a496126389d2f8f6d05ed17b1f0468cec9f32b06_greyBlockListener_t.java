 package me.ellbristow.greylistVote;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class greyBlockListener extends BlockListener {
 	
 	public static greylistVote plugin;
 	
 	public greyBlockListener(greylistVote instance) {
 		plugin = instance;
 	}
 	
 	public void onBlockPlace (BlockPlaceEvent event) {
 		Player player = event.getPlayer();
 		if (!player.hasPermission("greylistvote.approved")) {
 			player.sendMessage(ChatColor.RED + "You are not yet approved to place blocks!");
 			if (event.getBlockPlaced().getTypeId() == 323) {
 				ItemStack items = new ItemStack(Material.SIGN, event.getPlayer().getItemInHand().getAmount() + 1);
 				event.getPlayer().setItemInHand(items);
 			}
 			event.setCancelled(true);
 		}
 	}
 	
 	public void onBlockBreak (BlockBreakEvent event) {
 		Player player = event.getPlayer();
 		if (!player.hasPermission("greylistvote.approved")) {
 			player.sendMessage(ChatColor.RED + "You are not yet approved to destroy blocks!");
			Block block = event.getBlock();
 			event.setCancelled(true);
			if (block.getTypeId() == 63 || block.getTypeId() == 68) {
				// Block is a sign
				Sign sign = (Sign) block.getState();
				sign.setLine(1, sign.getLine(1));
				sign.update();
			}
 		}
 	}
 	
 	public void onBlockIgnite (BlockIgniteEvent event) {
 		Player player = event.getPlayer();
 		if (player != null && !player.hasPermission("greylistvote.approved")) {
 			player.sendMessage(ChatColor.RED + "Charlie says stop playing with fire!");
 			event.setCancelled(true);
 		}
 	}
 }
