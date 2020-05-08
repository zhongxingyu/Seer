 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import com.undeadscythes.udsplugin.utilities.*;
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 
 /**
  * Triggered when a player teleports.
  * @author UndeadScythes
  */
 public class PlayerTeleport extends ListenerWrapper implements Listener {
     @EventHandler
     public void onEvent(final PlayerTeleportEvent event) {
         final World from = event.getFrom().getWorld();
         final World to = event.getTo().getWorld();
        if(from == null || to == null) {
            event.setCancelled(true);
        }
         if(!from.equals(to) && !PlayerUtils.getOnlinePlayer(event.getPlayer().getName()).hasPermission(Perm.SHAREDINV)) {
             for(String string : UDSPlugin.getConfigStringList(ConfigRef.SHARES)) {
                 List<String> shares = Arrays.asList(string.split(","));
                 if(!shares.contains(from.getName()) || !shares.contains(to.getName())) {
                     PlayerUtils.saveInventory(PlayerUtils.getOnlinePlayer(event.getPlayer().getName()), from);
                     PlayerUtils.loadInventory(PlayerUtils.getOnlinePlayer(event.getPlayer().getName()), to);
                 }
             }
             GameMode mode = UDSPlugin.getWorldMode(to);
             if(mode != null && !PlayerUtils.getOnlinePlayer(event.getPlayer().getName()).hasPermission(Perm.ANYMODE)) {
                 event.getPlayer().setGameMode(mode);
             }
         }
     }
 }
