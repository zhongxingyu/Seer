 package com.webkonsept.bukkit.simplechestlock.listener;
 
 import com.webkonsept.bukkit.simplechestlock.SCL;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 
 public class SCLEntityListener implements Listener {
 	final SCL plugin;
 
 	public SCLEntityListener(SCL instance) {
 		plugin = instance;
 	}
 
     @EventHandler
     public void onPaintingDamaged (final HangingBreakByEntityEvent event){
         Entity remover = event.getRemover();
         Entity removed = event.getEntity();
         if (remover instanceof Player){
             Player player = (Player) remover;
             if (player.isOp()){
                 player.sendMessage("You removed a "+removed.getType().toString()+" with ID "+removed.getUniqueId());
             }
         }
     }
 
 	@EventHandler
 	public void onEntityExplode (final EntityExplodeEvent event){
 	    if (!plugin.isEnabled()) return;
 	    if (event.isCancelled()) return;
         if (plugin.cfg.preventExplosions()){ // If not, what's the point in checking at all?
 		    for (Block block : event.blockList()){
 			    if (plugin.chests.isLocked(block)){
                     //event.setCancelled(true); // No need to cancel the whole thing now that the list is no longer immutable.
                    event.blockList().remove(block);
                     SCL.verbose(plugin.chests.getOwner(block)+"'s "+block.getType().toString()+ " was saved from explosion.  Boom: "+String.valueOf(event.getEntity()).replace("^Craft","")); // Thanks SonarBerserk
                     //break; // Can't break any more:  Need to run through the whole list!
                 }
			}
 		}
 	}
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void monitorEntityExplode (final EntityExplodeEvent event){
         if (!plugin.isEnabled()) return;
         if (event.isCancelled()) return;
         for (Block block : event.blockList()){
             if (plugin.chests.isLocked(block)){
                 SCL.verbose(plugin.chests.getOwner(block)+"'s "+block.getType().toString()+ " was destroyed by an exploding "+String.valueOf(event.getEntity()).replace("^Craft","")); // Thanks SonarBerserk
                 plugin.chests.unlock(block);
             }
         }
     }
 }
