 package com.bettercraft.betachest.event;
 
 import org.bukkit.plugin.Plugin;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.getspout.spoutapi.event.inventory.InventoryCloseEvent;
 
 import com.bettercraft.betachest.BetaChestPlugin;
 
 public class InventoryListener implements Listener {
 
 	private BetaChestPlugin plugin;
 	
 	public InventoryListener(Plugin plugin) {
 		this.plugin = (BetaChestPlugin)plugin;
 		
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler
 	public void HandleChestClose(InventoryCloseEvent e) {
		plugin.getChestManager().savePlayer(e.getPlayer().getName());
 	}
 }
