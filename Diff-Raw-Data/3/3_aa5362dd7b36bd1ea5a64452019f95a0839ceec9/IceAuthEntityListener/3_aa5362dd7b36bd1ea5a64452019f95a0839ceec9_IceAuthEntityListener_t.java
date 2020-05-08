 package eu.icecraft.iceauth;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityTargetEvent;
 
 public class IceAuthEntityListener extends EntityListener {
 
 	private final IceAuth plugin;
 
 	public IceAuthEntityListener(final IceAuth plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public void onEntityDamage(EntityDamageEvent event) {
 		if(event.isCancelled()) {
 			return;
 		}
 		Entity entity = event.getEntity();
 		if(!(entity instanceof Player)) {
 			return;
 		}
 
 		Player player = (Player) entity;
 		if(!plugin.checkAuth(player)) {
 			event.setCancelled(true);
 		}
 	}
 
 	@Override
 	public void onEntityDeath(EntityDeathEvent event) {
 		Entity entity = event.getEntity();
 		if(!(entity instanceof Player)) {
 			return;
 		}
 
 		Player player = (Player) entity;
		
		player.teleport(player.getWorld().getSpawnLocation()); // should fix user reported bug
		
 		if(!plugin.checkAuth(player)) {
 			plugin.restoreInv(player); // is this needed?
 		}
 	}
 
 	@Override
 	public void onEntityTarget(EntityTargetEvent event) {
 		if(event.isCancelled()) {
 			return;
 		}
 		Entity entity = event.getEntity();
 		if(entity instanceof Player) {
 			return;
 		}
 
 		Entity target = event.getTarget();
 		if(!(target instanceof Player)) {
 			return;
 		}
 		Player targetPlayer = (Player) target;
 
 		if(!plugin.checkAuth(targetPlayer)) {
 			event.setCancelled(true);
 		}
 	}
 }
