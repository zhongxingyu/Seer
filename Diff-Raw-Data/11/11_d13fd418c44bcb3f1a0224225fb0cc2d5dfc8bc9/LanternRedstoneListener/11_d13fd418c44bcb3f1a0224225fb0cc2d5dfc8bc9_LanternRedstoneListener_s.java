 package me.ase34.citylanterns.listener;
 
 import me.ase34.citylanterns.CityLanterns;
 
 import org.bukkit.Location;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 public class LanternRedstoneListener implements Listener {
 
     private CityLanterns plugin;
     
     public LanternRedstoneListener(CityLanterns plugin) {
         this.plugin = plugin;
     }
     
     @EventHandler
     public void onRedstoneChange(BlockRedstoneEvent ev) {
         Location loc = ev.getBlock().getLocation();
         if (plugin.getLanterns().contains(loc)) {
            if (loc.getWorld().getTime() % 24000 >= plugin.getConfig().getLong("night_time")) {
                 ev.setNewCurrent(15);                
             } else if (loc.getWorld().getTime() % 24000 >= plugin.getConfig().getLong("day_time")) {
                 ev.setNewCurrent(0); 
             }
         }
     }
 }
