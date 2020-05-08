 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.Region.RegionFlag;
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.hanging.*;
 
 /**
  * When a painting gets broken.
  * @author UndeadScythes
  */
 public class HangingBreak extends ListenerWrapper implements Listener {
     @EventHandler
    public void onEvent(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        Location location = event.getEntity().getLocation();
        if(getAbsoluteEntity(remover) instanceof Player ? !UDSPlugin.getOnlinePlayers().get(((Player)remover).getName()).canBuildHere(location) : hasFlag(location, RegionFlag.PROTECTION)) {
            event.setCancelled(true);
        }
     }
 }
