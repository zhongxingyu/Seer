 package me.limebyte.battlenight.core.Listeners;
 
 import me.limebyte.battlenight.core.BattleNight;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 public class DamageListener implements Listener {
 
 	// Get Main Class
 	public static BattleNight plugin;
 	public DamageListener(BattleNight instance) {
 		plugin = instance;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityDamage(EntityDamageEvent event) {
 		if (event instanceof EntityDamageByEntityEvent) {
 			EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent)event;
			if(!canBeDamaged(subEvent) || event.isCancelled()) event.setCancelled(true);
 			else event.setCancelled(false);
 		}
 	}
 	
 	private boolean canBeDamaged(EntityDamageByEntityEvent event) {
 		
 		if(!(event.getEntity() instanceof Player)) return true;
 		
 		if (event.getDamager() == event.getEntity()) return true;
 		
 		Player damaged = (Player) event.getEntity();
 		Player damager;
 		
 		if (event.getDamager() instanceof Projectile) {
 			LivingEntity shooter = ((Projectile) event.getDamager()).getShooter();
 			if (shooter instanceof Player) damager = (Player) shooter;
 			else return true;
 		}
 		else if (event.getDamager() instanceof Player) {
 			damager = (Player) event.getDamager();
 		}
 		else {
 			return true;
 		}
 		
 		if(plugin.BattleSpectators.containsKey(damager.getName())) return false;
 		
 		if(plugin.BattleUsersTeam.containsKey(damager.getName()) && plugin.BattleUsersTeam.containsKey(damaged.getName())) {
 			if(plugin.playersInLounge) return false;
 			if(areEnemies(damager, damaged)) {
 				return true;
 			}
 			else {
 				if(plugin.configFriendlyFire) return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean areEnemies(Player player1, Player player2) {
 		if(plugin.BattleUsersTeam.get(player1.getName()) != plugin.BattleUsersTeam.get(player2.getName())) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 }
