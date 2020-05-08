 package me.ase34.citylanterns.listener;
 
 import me.ase34.citylanterns.CityLanterns;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class LanternSelectListener implements Listener {
 
     private CityLanterns plugin;
 
     public LanternSelectListener(CityLanterns plugin) {
         super();
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onPlayerInteractBlock(PlayerInteractEvent ev) {
         if (ev.isBlockInHand()) {
             return;
         }
         if (plugin.getSelectingPlayers().contains(ev.getPlayer().getName())) {
             if (ev.getClickedBlock().getType() != Material.REDSTONE_LAMP_OFF) {
                 ev.getPlayer().sendMessage(ChatColor.GOLD + "This is not a redstone lamp!");
                 return;
             }
             Location loc = ev.getClickedBlock().getLocation();
             if (plugin.getLanterns().contains(loc)) {
                 plugin.getLanterns().remove(loc);
                 ev.getPlayer().sendMessage(ChatColor.GOLD + "Removed this lantern!");
             } else {
                 plugin.getLanterns().add(loc);
                 ev.getPlayer().sendMessage(ChatColor.GOLD + "Added this lantern!");
             }
             ev.setCancelled(true);
         }
     }
 }
