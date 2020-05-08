 package com.github.thebiologist13.listeners;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 import com.github.thebiologist13.CustomSpawners;
 import com.github.thebiologist13.Spawner;
 
 public class SpawnerPowerEvent implements Listener {
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onSpawnerPower(BlockRedstoneEvent ev) {
 		
		Spawner spawner = CustomSpawners.getSpawnerAt(ev.getBlock().getLocation());
 		
 		if(spawner != null) {
 			
			boolean hasPower = ev.getOldCurrent() < ev.getNewCurrent();
 		
 			if(spawner.isSpawnOnRedstone()) {
 				
 				if(hasPower && !spawner.isPoweredBefore()) {
 					spawner.setPoweredBefore(true);
 					spawner.spawn();
 				}
 				
 				if(!hasPower && spawner.isPoweredBefore()) {
 					spawner.setPoweredBefore(false);
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 }
