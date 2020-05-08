 package shadowlauch.advancedbow.main;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.ProjectileHitEvent;
 
 public class ArrowDamageListener extends EntityListener {
 	
     public static AdvancedBow plugin;
     public static List<Player> enabledfire=new ArrayList<Player>();
 
     public ArrowDamageListener(AdvancedBow instance) {
         plugin = instance;
     }
     
     public void onEntityDamage(EntityDamageEvent e) {
     	if(e instanceof EntityDamageByProjectileEvent){
 	    	EntityDamageByProjectileEvent f=(EntityDamageByProjectileEvent)e;
 	    	if(f.getProjectile() instanceof Arrow){
 	    		if(f.getProjectile().getShooter() instanceof Player){
 	    			Player p=(Player) f.getProjectile().getShooter();
 	    			e.setDamage((int) (e.getDamage()*plugin.config.ad));
 	    			if(p.getInventory().contains(327) && plugin.hasPerm(p, "advancedbow.fire") && enabledfire.contains(p) && plugin.config.fae){
	    				if(!(e.getEntity() instanceof Player) || (p.getWorld().getPVP()))
	    					e.getEntity().setFireTicks(plugin.config.ft);
	    				}
 	    		}
 	    	}
     	}
     }
     
     public void onProjectileHit(ProjectileHitEvent e){
     	if(e.getEntity() instanceof Arrow){
     		Arrow a=(Arrow) e.getEntity();
 	    	if(a.getShooter() instanceof Player){
 	    		Player p=(Player)a.getShooter();
 	    		if(p.getInventory().contains(327) && plugin.hasPerm(p, "advancedbow.fire") && enabledfire.contains(p) && plugin.config.fae){
 	    			a.setFireTicks(100);
 	    		}
 	    	}
     	}
     }
 }
