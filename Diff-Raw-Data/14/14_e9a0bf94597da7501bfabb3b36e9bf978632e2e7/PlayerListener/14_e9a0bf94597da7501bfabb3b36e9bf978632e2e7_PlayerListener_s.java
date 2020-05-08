 package com.dsh105.sparktrail.listeners;
 
 import com.dsh105.sparktrail.SparkTrailPlugin;
 import com.dsh105.sparktrail.data.EffectManager;
 import com.dsh105.sparktrail.trail.EffectHolder;
 import com.dsh105.sparktrail.util.Lang;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.scheduler.BukkitRunnable;
 
 
 public class PlayerListener implements Listener {
 
     @EventHandler
     public void onQuit(PlayerQuitEvent event) {
         Player p = event.getPlayer();
         EffectHolder eh = EffectManager.getInstance().getEffect(p.getName());
         if (eh != null) {
             EffectManager.getInstance().remove(eh);
         }
     }
 
     @EventHandler
     public void onLogin(PlayerLoginEvent event) {
         final Player p = event.getPlayer();
         if (event.getResult().equals(PlayerLoginEvent.Result.ALLOWED)) {
             new BukkitRunnable() {
                 @Override
                 public void run() {
                     if (p != null) {
                         EffectHolder eh = EffectManager.getInstance().createFromFile(p.getName());
                         if (eh != null && !eh.getEffects().isEmpty()) {
                             Lang.sendTo(p, Lang.EFFECTS_LOADED.toString());
                         }
                     }
                 }
             }.runTaskLater(SparkTrailPlugin.getInstance(), 20L);
         }
     }
 }
