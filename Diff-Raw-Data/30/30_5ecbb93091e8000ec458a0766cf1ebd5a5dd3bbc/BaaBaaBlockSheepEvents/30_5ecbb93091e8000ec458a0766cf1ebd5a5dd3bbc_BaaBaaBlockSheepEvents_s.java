 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gibstick.bukkit.discosheep;
 
 import org.bukkit.entity.Sheep;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 
 /**
  *
  * @author Mauve
  */
 public class BaaBaaBlockSheepEvents implements Listener {
 	
 	DiscoSheep parent;
 	
 	public BaaBaaBlockSheepEvents(DiscoSheep parent) {
 		this.parent = parent;
 	}
 	
 	@EventHandler
 	public void onPlayerShear(PlayerShearEntityEvent e) {
 		if (e.getEntity() instanceof Sheep) {
 			for (DiscoParty party : parent.getParties()) {
 				if (party.getSheep().contains((Sheep) e.getEntity())) {
 					e.setCancelled(true);
 				}
 			}
 		}
 	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (e.getEntity() instanceof Sheep && 
				e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.BREEDING)) {
			for (DiscoParty party : parent.getParties()) {
				if (party.getSheep().contains((Sheep) e.getEntity())) {
					e.setCancelled(true);
					
				}
			}
		}
	}
 }
