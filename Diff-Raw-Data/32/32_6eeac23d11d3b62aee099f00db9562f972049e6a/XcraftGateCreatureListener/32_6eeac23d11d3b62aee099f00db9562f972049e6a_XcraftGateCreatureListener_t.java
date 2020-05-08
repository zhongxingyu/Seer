 package de.xcraft.engelier.XcraftGate;
 
import net.minecraft.server.WorldServer;

import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 
 public class XcraftGateCreatureListener extends EntityListener {
 	private XcraftGate plugin;
 	
 	public XcraftGateCreatureListener (XcraftGate instance) {
 		plugin = instance;
 	}
 
 	public void onCreatureSpawn(CreatureSpawnEvent event) {
 		Integer max = plugin.config.getInt("worlds." + event.getLocation().getWorld().getName() + ".creatureLimit", 0);
		Integer alive = event.getLocation().getWorld().getLivingEntities().size() - event.getLocation().getWorld().getPlayers().size();
 		
		if (max > 0 && max <= alive) {
 			event.setCancelled(true);
			WorldServer world = ((CraftWorld)event.getLocation().getWorld()).getHandle();
			world.allowAnimals  = false;
			world.allowMonsters = false;
		}
	}
	
	public void onEntityDeath(EntityDeathEvent event) {
		Double max = plugin.config.getDouble("worlds." + event.getEntity().getLocation().getWorld().getName() + ".creatureLimit", 0.0) * 0.9;
		Integer alive = event.getEntity().getLocation().getWorld().getLivingEntities().size() - event.getEntity().getLocation().getWorld().getPlayers().size();
		
		if (max > alive || max <= 0) {
			WorldServer world = ((CraftWorld)event.getEntity().getLocation().getWorld()).getHandle();
			world.allowAnimals  = true;
			world.allowMonsters = true;
		}		
 	}
 }
