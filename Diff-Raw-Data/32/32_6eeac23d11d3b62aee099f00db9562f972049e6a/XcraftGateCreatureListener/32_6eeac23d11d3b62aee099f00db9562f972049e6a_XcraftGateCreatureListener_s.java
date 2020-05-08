 package de.xcraft.engelier.XcraftGate;
 
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityListener;
 
 public class XcraftGateCreatureListener extends EntityListener {
 	private XcraftGate plugin;
 	
 	public XcraftGateCreatureListener (XcraftGate instance) {
 		plugin = instance;
 	}
 
 	public void onCreatureSpawn(CreatureSpawnEvent event) {
 		Integer max = plugin.config.getInt("worlds." + event.getLocation().getWorld().getName() + ".creatureLimit", 0);
 		
		if (max > 0 && max <= event.getLocation().getWorld().getLivingEntities().size() - event.getLocation().getWorld().getPlayers().size())
 			event.setCancelled(true);
 	}
 }
