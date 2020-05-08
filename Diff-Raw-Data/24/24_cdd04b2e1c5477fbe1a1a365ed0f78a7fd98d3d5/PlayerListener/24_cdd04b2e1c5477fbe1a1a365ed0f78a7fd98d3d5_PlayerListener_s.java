 
 package com.quartercode.basiccommands.listener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import com.quartercode.minecartrevolution.MinecartRevolution;
 import com.quartercode.minecartrevolution.get.Perm;
 import com.quartercode.minecartrevolution.util.GlobalConfig;
 
 public class PlayerListener implements Listener {
 
     private final MinecartRevolution minecartRevolution;
 
     public PlayerListener(final MinecartRevolution minecartRevolution) {
 
         this.minecartRevolution = minecartRevolution;
         Bukkit.getPluginManager().registerEvents(this, minecartRevolution.getPlugin());
     }
 
     @EventHandler
     public void onPlayerJoin(final PlayerJoinEvent event) {
 
         if (minecartRevolution.getConfiguration().getBool(GlobalConfig.AUTO_UPDATE)) {
             new Thread() {
 
                 @Override
                 public void run() {
 
                     Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecartrevolution update");
                 }
             }.start();
         }
 
        new Thread() {
 
            @Override
            public void run() {
 
                final Player player = event.getPlayer();
                if (Perm.has(player, "update.update") && minecartRevolution.getConfiguration().getBool(GlobalConfig.CHECK_VERSION_ON_JOIN) && !minecartRevolution.getConfiguration().getBool(GlobalConfig.AUTO_UPDATE)) {
                     Bukkit.dispatchCommand(player, "minecartrevolution checkversions");
                 }
            }
        }.start();
     }
 
 }
