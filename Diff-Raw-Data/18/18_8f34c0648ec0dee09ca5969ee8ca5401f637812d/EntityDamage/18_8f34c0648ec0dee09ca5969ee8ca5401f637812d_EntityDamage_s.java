 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.Region.RegionFlag;
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.entity.*;
 
 /**
  * When an entity is damaged.
  * @author UndeadScythes
  */
 public class EntityDamage extends ListenerWrapper implements Listener {
     @EventHandler
     public void onEvent(EntityDamageEvent event) {
        if((event.getEntity() instanceof Player && UDSPlugin.getOnlinePlayers().get(((Player)event.getEntity()).getName()).hasGodMode()) || (!Config.HOSTILE_MOBS.contains(event.getEntity().getType()) && !hasFlag(event.getEntity().getLocation(), RegionFlag.PVE))) {
             event.setCancelled(true);
         }
     }
 }
