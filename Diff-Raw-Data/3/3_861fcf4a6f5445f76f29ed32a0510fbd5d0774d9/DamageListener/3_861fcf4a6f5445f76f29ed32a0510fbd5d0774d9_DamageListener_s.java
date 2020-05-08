 package me.limebyte.battlenight.core.Listeners;
 
 import me.limebyte.battlenight.core.BattleNight;
 
 import org.bukkit.entity.Entity;
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
 	    if (!(event.getEntity() instanceof Player)) return;
 	    Player player = (Player) event.getEntity();
 	    
 		if (event instanceof EntityDamageByEntityEvent) {
 			EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
 			
 			if (plugin.BattleSpectators.containsKey(player.getName())) event.setCancelled(true);
 			
 			if (!plugin.BattleUsersTeam.containsKey(player.getName())) return;
 			
     		subEvent.setCancelled(!canBeDamaged(player, subEvent.getDamager()));
     		
 		}
 		
 	}
 
 	private boolean canBeDamaged(Player damaged, Entity eDamager) {
 		if (eDamager == damaged) return true;
 
 		Player damager;
 		
 		if (eDamager instanceof Projectile) {
 			LivingEntity shooter = ((Projectile) eDamager).getShooter();
 			if (shooter instanceof Player)
 			    damager = (Player) shooter;
 			else
 				return true;
 		} else {
 		    if (eDamager instanceof Player) {
 		        damager = (Player) eDamager;
 		    }
 		    else {
 		        return true;
 		    }
 		}
 		
 		if (plugin.BattleUsersTeam.containsKey(damager.getName())) {
 			if (plugin.playersInLounge)
 				return false;
 			if (areEnemies(damager, damaged)) {
 				return true;
 			} else {
				if (plugin.configFriendlyFire)
					return false;
 			}
 		}
 
 		return true;
 	}
 
 	private boolean areEnemies(Player player1, Player player2) {
 		if (plugin.BattleUsersTeam.get(player1.getName()) != plugin.BattleUsersTeam.get(player2.getName())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 }
