 package onethatwalks.satools.listeners;
 
 import onethatwalks.satools.SATools;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 
 /**
  * Handle events for all Entity related events
  * 
  * @author OneThatWalks
  */
 public class SAToolsEntityListener extends EntityListener {
 
 	@Override
 	public void onEntityDamage(EntityDamageEvent ede) {
 		Entity entity = ede.getEntity();
 		if (entity instanceof Player) {
 			Player player = (Player) entity;
			if (SATools.gods.contains(player.getDisplayName())) {
 				ede.setCancelled(true);
 			}
 		}
 	}
 
 }
