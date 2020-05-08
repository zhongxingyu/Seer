 package me.Travja.HungerArena;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
 
 public class PvP implements Listener {
 	public Main plugin;
 	public PvP(Main m) {
 		this.plugin = m;
 	}
 	int a = 0;
 	@EventHandler(priority= EventPriority.MONITOR)
 	public void PlayerPvP(EntityDamageByEntityEvent event){
 		Entity pl = event.getEntity();
 		Entity dl = event.getDamager();
 		if(pl instanceof Player && dl instanceof Player){
 			Player p = (Player) pl;
 			Player d = (Player) dl;
 			if(plugin.getArena(p)!= null && plugin.getArena(d)!= null){
 				a = plugin.getArena(p);
 				if(plugin.canjoin.get(a)){
 					if(event.isCancelled()){
 						event.setCancelled(false);
 					}
 				}
 			}
 			if(plugin.getArena(p)!= null){
 				a = plugin.getArena(p);
 				if(!plugin.canjoin.get(a)){
 					if(!event.isCancelled()){
 						event.setCancelled(true);
 					}
 				}
 			}
 			if(plugin.getArena(p)== null && plugin.getArena(d)!= null){
 				if(!event.isCancelled()){
 					event.setCancelled(true);
 				}
 			}
 		}else if(pl instanceof Player && dl instanceof Projectile){
 			Projectile projectile = (Projectile) dl;
 			Player p = (Player) pl;
 			if(projectile.getShooter() instanceof Player){
 				if(plugin.getArena(p) != null){
 					Player shooter = (Player) projectile.getShooter();
 					if(plugin.getArena(shooter)!= null){
 						event.setCancelled(false);
 					}
 				}
 			}else if(projectile.getShooter() instanceof Entity){
 				Entity e = projectile.getShooter();
 				if(e instanceof Skeleton){
 					if(plugin.getArena(p)!= null){
 						event.setCancelled(false);
 					}
 				}
 			}
 		}
 	}
	@EventHandler
	public void PlayerDamage(EntityDamageEvent event){
		Entity e = event.getEntity();
		if(e instanceof Player){
			if(plugin.gp!= 0)
				event.setCancelled(true);
		}
	}
 }
