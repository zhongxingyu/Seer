 package tk.nekotech.war.events;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityExplodeEvent;
 
 public class EntityExplode implements Listener {
 	
 	@EventHandler
 	public void onEntityExplode(EntityExplodeEvent event) {
		event.blockList().clear();
 	}
 
 }
