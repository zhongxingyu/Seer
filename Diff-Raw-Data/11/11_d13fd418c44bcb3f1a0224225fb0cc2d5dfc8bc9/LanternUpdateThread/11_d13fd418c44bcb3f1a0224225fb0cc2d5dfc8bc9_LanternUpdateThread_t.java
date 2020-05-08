 package me.ase34.citylanterns.runnable;
 
 import me.ase34.citylanterns.CityLanterns;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 
 public class LanternUpdateThread implements Runnable {
 
     private static final Material LAMP_OFF = Material.REDSTONE_LAMP_OFF;
     private static final Material LAMP_ON = Material.REDSTONE_LAMP_ON;
     
     private CityLanterns plugin;
 
     public LanternUpdateThread(CityLanterns plugin) {
         super();
         this.plugin = plugin;
     }
 
     public void run() {
         for (int i = 0; i < plugin.getLanterns().size(); i++) {
             Location loc = plugin.getLanterns().get(i);
             if (loc.getBlock().getType() != LAMP_ON
                     && loc.getBlock().getType() != LAMP_OFF) {
                 plugin.getLanterns().remove(i);
                 i--;
                 continue;
             }
             
            if (loc.getWorld().isThundering() && plugin.getConfig().getBoolean("lamps_on_thundering")) {
                 if (loc.getBlock().getType() != LAMP_ON) {
                     loc.getBlock().setType(LAMP_ON);
                 }
             } else if (loc.getWorld().getTime() % 24000 >= plugin.getConfig().getLong("night_time")) {
                 if (loc.getBlock().getType() != LAMP_ON) {
                     loc.getBlock().setType(LAMP_ON);
                 }
             } else if (loc.getWorld().getTime() % 24000 >= plugin.getConfig().getLong("day_time")) {
                 if (loc.getBlock().getType() != LAMP_OFF) {
                     loc.getBlock().setType(LAMP_OFF);
                 }
             }
         }
     }
 
 }
