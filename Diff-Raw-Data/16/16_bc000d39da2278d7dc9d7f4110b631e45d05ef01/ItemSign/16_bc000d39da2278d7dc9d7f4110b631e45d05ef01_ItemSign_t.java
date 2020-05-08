 package com.edgegames.lachy2901.itemsign;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ItemSign extends JavaPlugin implements Listener {
 
 	public void onEnable() {
 		Bukkit.getPluginManager().registerEvents(this, this);
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
		if (Material.WALL_SIGN.equals(event.getClickedBlock().getType()) || Material.SIGN_POST.equals(event.getClickedBlock().getType())) {
 			Sign sign = (Sign) event.getClickedBlock().getState();
 			if (sign.getLine(1).equalsIgnoreCase("Item ID:")) {
				event.setCancelled(true);
				if (sign.getLine(2).contains(":")) {
					String[] parts = sign.getLine(2).split(":");
					event.getPlayer().getInventory().addItem(new ItemStack(Integer.parseInt(parts[0]), 64, Short.valueOf(parts[1])));
				} else {
					event.getPlayer().getInventory().addItem(new ItemStack(Integer.parseInt(sign.getLine(2)), 64));
 				}
 			}
 		}
 	}
 	
 }
