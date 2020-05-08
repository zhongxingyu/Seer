 package pro.homiecraft;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 
 import pro.homiecraft.Commands.resources.toggleMap;
 
 public class Egg implements Listener {
 	@EventHandler(priority = EventPriority.HIGH)
 	public void egg(CreatureSpawnEvent event) {
 		if (toggleMap.ee.containsValue(true)) {
 			if (event.getSpawnReason() == SpawnReason.EGG) {
 				event.setCancelled(true);
 			} else {
 				if (event.getSpawnReason() == SpawnReason.EGG) {
 					event.setCancelled(false);
 				}

 			}
 		}
 	}
 }
