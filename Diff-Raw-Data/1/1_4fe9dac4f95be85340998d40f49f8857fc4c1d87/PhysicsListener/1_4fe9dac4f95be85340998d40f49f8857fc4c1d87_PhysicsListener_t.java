 package com.minecraftdimensions.bungeesuiteportals.listeners;
 
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 
 import com.minecraftdimensions.bungeesuiteportals.managers.PortalsManager;
 import com.minecraftdimensions.bungeesuiteportals.objects.Portal;
 
 public class PhysicsListener implements Listener {
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onBlockPhysics(BlockPhysicsEvent e) {
 				if(!(e.getBlock().isLiquid() || e.getBlock().getType()==Material.PORTAL || e.getBlock().getType()==Material.ENDER_PORTAL || e.getBlock().getType()==Material.SUGAR_CANE_BLOCK)){
 					return;
 				}
 				if(!PortalsManager.PORTALS.containsKey(e.getBlock().getWorld())){
 					return;
 				}
 				
 				for(Portal p: PortalsManager.PORTALS.get(e.getBlock().getWorld())){
 					if(p.isBlockInPortal(e.getBlock())){
 						e.setCancelled(true);
 					}
 				}
 	
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onBlockPhysics(BlockFromToEvent e) {
 		if(!(e.getBlock().isLiquid() || e.getBlock().getType()==Material.PORTAL || e.getBlock().getType()==Material.ENDER_PORTAL || e.getBlock().getType()==Material.SUGAR_CANE_BLOCK)){
 			return;
 		}
 		if(!PortalsManager.PORTALS.containsKey(e.getBlock().getWorld())){
 			return;
 		}
 		
 		for(Portal p: PortalsManager.PORTALS.get(e.getBlock().getWorld())){
 			if(p.isBlockInPortal(e.getBlock())){
 				e.setCancelled(true);
 			}
 		}
 	
 	}
 
 }
