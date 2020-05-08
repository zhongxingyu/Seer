 package me.yukonapplegeek.bowkilldistance;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BowKillDistance extends JavaPlugin implements Listener {
     public void onDisable() {
         // TODO: Place any custom disable code here.
     }
 
     public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     @EventHandler
     public void onPlayerDeath(PlayerDeathEvent event) {
         Player deadPlayer = event.getEntity();
 
         if (deadPlayer.getLastDamageCause().getCause() == DamageCause.PROJECTILE && deadPlayer.getKiller() instanceof Player) {
             int distance = (int) deadPlayer.getLocation().distance(deadPlayer.getKiller().getLocation());
            event.setDeathMessage(deadPlayer.getDisplayName()+" was shoot by "+deadPlayer.getKiller().getDisplayName()+" ("+distance+" blocks)");
         }
     }
 }
