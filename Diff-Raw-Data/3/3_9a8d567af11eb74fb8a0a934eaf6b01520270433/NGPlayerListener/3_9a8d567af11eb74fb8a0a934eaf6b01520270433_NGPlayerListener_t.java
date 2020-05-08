 package com.bukkit.N4th4.NuxGrief;
 
 import org.bukkit.Material;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 
 public class NGPlayerListener extends PlayerListener {
     public final NuxGrief plugin;
 
     public NGPlayerListener(NuxGrief instance) {
         plugin = instance;
     }
 
     public void onPlayerPickupItem(PlayerPickupItemEvent event) {
         if(!plugin.permissions.has(event.getPlayer(), "nuxgrief.pickup")) {
         	event.setCancelled(true);
         }
     }
     
     public void onPlayerInteract(PlayerInteractEvent event) {
    	if (event.getClickedBlock() == null) {
    		return;
    	}
     	if (event.getClickedBlock().getType() == Material.CHEST && !plugin.permissions.has(event.getPlayer(), "nuxgrief.interact.chests")) {
     		event.setCancelled(true);
     	}
     	else if (event.getClickedBlock().getType() == Material.FURNACE && !plugin.permissions.has(event.getPlayer(), "nuxgrief.interact.furnaces")) {
     		event.setCancelled(true);
     	}
     	else if (event.getClickedBlock().getType() == Material.STORAGE_MINECART && !plugin.permissions.has(event.getPlayer(), "nuxgrief.interact.storage_minecarts")) {
     		event.setCancelled(true);
     	}
     	
     }
 }
