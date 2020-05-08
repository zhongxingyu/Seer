 package com.edgegames.lachy2901.itemsign;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ItemSign extends JavaPlugin implements Listener {
 
 	public void onEnable() {
 		Bukkit.getPluginManager().registerEvents(this, this);
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
		if (Material.SIGN.equals(event.getClickedBlock().getType()) || Material.SIGN_POST.equals(event.getClickedBlock().getType())) {
 			Sign sign = (Sign) event.getClickedBlock().getState();
 			if (sign.getLine(1).equalsIgnoreCase("Item ID:")) {
				int ID = Integer.parseInt(sign.getLine(3));
				if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
					event.getPlayer().getInventory().addItem(new ItemStack(ID, 64));
				} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					event.getPlayer().getInventory().addItem(new ItemStack(ID, 1));
 				}
 			}
 		}
 	}
 	
 }
