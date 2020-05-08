 package com.bukkit.Top_Cat.MIA;
 
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByBlockEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityListener;
 
 /**
  * Handle events for all Player related events
  * @author Thomas Cheyney
  */
 public class MIAEntityListener extends EntityListener {
     private final MIA plugin;
     
     public MIAEntityListener(MIA instance) {
         plugin = instance;
     }
     
     @Override
     public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
     	event.setCancelled(onDamage(event.getDamager(), event.getEntity(), event.getDamage()));
     }
     
     public boolean onDamage(Entity attacker, Entity defender) {
     	return onDamage(attacker, defender, 0);
     }
     
     HashMap<Player, Player> lastattacker = new HashMap<Player, Player>();
     
     public boolean onDamage(Entity attacker, Entity defender, int damage) {
     	if (defender instanceof Player) {
 	    	int x = defender.getLocation().getBlockX();
 	    	int z = defender.getLocation().getBlockZ();
 	    	
 	    	if (plugin.mf.intown(x, z) > 0) {
 	        	return true;
 	    	} else if (attacker instanceof Player) {
 	    		if ((((Player) defender).getDisplayName().equalsIgnoreCase("Top_Cat") || ((Player) defender).getDisplayName().equalsIgnoreCase("Welsh_Sniper")) && ((Player) attacker).getDisplayName().equalsIgnoreCase("Gigthank")) {
 	    			attacker.teleportTo(new Location(attacker.getWorld(), 645, 3, 200));
 	    			int xs = 643;
 	    			int zs = 198;
 	    			for (int i = 1; i < 6; i++) {
 	    				for (int j = 0; j < 5; j++) {
 	    					for (int k = 0; k < 5; k++) {
 	    						if (j == 0 || j == 4 || k == 0 || k == 4) {
 	    							attacker.getWorld().getBlockAt(j + xs, i, k + zs).setType(Material.OBSIDIAN);
 	    						}
 	    					}
 	    				}
 	    			}
 	    			return true;
 	    		}
 		    	if (((Player) defender).getHealth() - damage <= 0) {
 		    		lastattacker.put((Player) defender, (Player) attacker);
 		    	}
 	    	}
     	}
     	return false;
     }
     
     @Override
     public void onEntityDamageByProjectile(EntityDamageByProjectileEvent event) {
     	event.setCancelled(onDamage(null, event.getEntity(), event.getDamage()));
     }
     
     @Override
     public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
     	event.setCancelled(onDamage(null, event.getEntity(), event.getDamage()));
     }
     
     @Override
     public void onEntityDeath(EntityDeathEvent event) {
     	Entity defender = event.getEntity();
    	if (defender instanceof Player) {
     		Player attacker = lastattacker.get(defender);
 	    	// Death
 			int amm = (int) (plugin.playerListener.userinfo.get(((Player) defender).getDisplayName()).getBalance() * 0.05);
 			plugin.playerListener.cbal(
 					((Player) defender).getDisplayName(),
 					-amm
 			);
 			plugin.playerListener.cbal(
 					((Player) attacker).getDisplayName(),
 					amm
 			);
 			plugin.mf.sendmsg(plugin.getServer().getOnlinePlayers(), ((Player) attacker).getDisplayName() + " got " + amm + " ISK for killing " + ((Player) defender).getDisplayName());
     	}
     	// DMC
     	//event.getDrops()
     }
     
     @Override
     public void onEntityExplode(EntityExplodeEvent event) {
     	for (Block i : event.blockList()) {
         	if (plugin.mf.intown(i.getX(), i.getZ()) > 0) {
         		event.setCancelled(true);
         	}
     	}
     }
 
     //Insert Player related code here
 }
 
