 package fi.dy.esav.InfiniteFight;
 
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
 
public class InfiniteFightEntityListener extends EntityListener {
 	
 	int num = 3;

 	public void onEntityDeath(EntityDeathEvent e) {
 		if (e.getEntity() instanceof LivingEntity && !(e.getEntity() instanceof Player && !(e.getEntity().getLastDamageCause().getCause() == DamageCause.FIRE || e.getEntity().getLastDamageCause().getCause() == DamageCause.FIRE_TICK || e.getEntity().getLastDamageCause().getCause() == DamageCause.LAVA || e.getEntity().getLastDamageCause().getCause() == DamageCause.VOID))) {
 
 			for (int x=1;x<=num;x++) {
 				e.getEntity().getWorld().spawn(e.getEntity().getLocation(), e.getEntity().getClass());			
 			}
 			
 		}
 	}
 	
 }
 
