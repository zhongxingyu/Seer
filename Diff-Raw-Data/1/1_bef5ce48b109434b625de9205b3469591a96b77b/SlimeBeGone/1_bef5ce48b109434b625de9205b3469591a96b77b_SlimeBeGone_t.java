 package com.github.finalfred.slimebegone;
 
 import org.bukkit.Location;
 import org.bukkit.block.Biome;
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SlimeBeGone extends JavaPlugin implements Listener {
 
 	@Override
 	public void onEnable() {
 		this.getServer().getPluginManager().registerEvents(this, this);
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onCreatureSpawn(CreatureSpawnEvent e) {
 		if (e.getEntityType() != EntityType.SLIME) {
 			// Don't care
 			return;
 		}
 		// Slimes are allowed to split and be spawned by spawn eggs
 		switch (e.getSpawnReason()) {
 			case SLIME_SPLIT:
 			case SPAWNER_EGG:
 				return;
 			default:
 				break;
 		}
 
 		Location l = e.getLocation();
 
 		// Verify > 60 requirement
 		if (l.getY() > 60) {
 
 			// Verify Spawp biome
 			if (l.getWorld().getBiome(l.getBlockX(), l.getBlockZ()) == Biome.SWAMPLAND) {
 
 				// Slime is fine
 				return;
 			}
 		}
 
 		// Nope! No spawning!
 		e.setCancelled(true);
 	}
 }
