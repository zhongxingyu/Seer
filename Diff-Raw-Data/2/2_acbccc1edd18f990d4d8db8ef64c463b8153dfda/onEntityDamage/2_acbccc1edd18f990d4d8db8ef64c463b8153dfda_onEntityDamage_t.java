 package cz.vojtamaniak.komplex.listeners;
 
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.EventHandler;
 
 import cz.vojtamaniak.komplex.Komplex;
 
 public class onEntityDamageEvent extends IListener {
 
   public onEntityDamageEvent(Komplex plg) {
 		super(plg);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
 	public void onEntityDamageEvent(EntityDamageEvent e){
 		if(e.getEntityType() == EntityType.PLAYER){
 			Player player = (Player)e.getEntity();
			if(plg.getUser(player.getName()).getGodMode() && e.getCause() != DamageCause.VOID){
 				e.setCancelled(true);
 			}
 		}
 	}
 }
