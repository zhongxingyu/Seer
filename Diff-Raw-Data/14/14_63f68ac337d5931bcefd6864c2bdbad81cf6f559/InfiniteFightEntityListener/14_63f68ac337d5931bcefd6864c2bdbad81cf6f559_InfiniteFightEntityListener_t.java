 package fi.dy.esav.InfiniteFight;
 
 
import java.util.Arrays;
import java.util.List;

 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 public class InfiniteFightEntityListener implements Listener {
 	
	static final List<DamageCause> ignoredCauses = Arrays.asList(DamageCause.DROWNING, DamageCause.FIRE, DamageCause.FIRE_TICK, DamageCause.LAVA, DamageCause.SUFFOCATION, DamageCause.VOID);
	
	int num = 1; // Debug, prevent explosions
 	
 	@EventHandler(priority= EventPriority.NORMAL)
 	public void onEntityDeath(EntityDeathEvent e) {
		if (	e.getEntity() instanceof LivingEntity &&
				!(e.getEntity() instanceof Player) && 
				!(ignoredCauses.contains(e.getEntity().getLastDamageCause().getCause())) 
			) {
 
 			for (int x=1;x<=num;x++) {
 				e.getEntity().getWorld().spawn(e.getEntity().getLocation(), e.getEntity().getClass());			
 			}
 			
 		}
 	}
 	
 }
 
