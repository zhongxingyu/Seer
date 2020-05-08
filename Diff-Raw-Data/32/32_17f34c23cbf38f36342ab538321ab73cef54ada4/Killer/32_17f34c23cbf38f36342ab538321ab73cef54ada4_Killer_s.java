 package org.awesomecraft.plugins.killer;
 
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Killer extends JavaPlugin implements Listener {
     public void onDisable() {
        // TODO: Place any custom disable code here.
     }
 
     public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     @EventHandler
     public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
         if(event.getPlayer().getItemInHand().getType().equals(Material.BLAZE_ROD) 
                 && event.getPlayer().isSneaking()
                && event.getPlayer().hasPermission("killer.kill")) {
             event.getRightClicked().setFallDistance(1000000000000000000F);
         }
     }
 }
 
