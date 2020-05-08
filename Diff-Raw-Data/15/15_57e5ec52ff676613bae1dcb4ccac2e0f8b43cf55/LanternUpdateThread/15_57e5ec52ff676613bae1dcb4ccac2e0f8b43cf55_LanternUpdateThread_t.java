 package me.ase34.citylanterns.runnable;
 
 import me.ase34.citylanterns.BlockUpdateAction;
 import me.ase34.citylanterns.CityLanterns;
 import me.ase34.citylanterns.Lantern;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 
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
             Lantern lantern = plugin.getLanterns().get(i);
             
             Location loc = lantern.getLocation();
             if (loc == null) {
                 continue;
             }
             
             Block block = lantern.getLanternBlock();
             if (block.getType() != LAMP_ON && block.getType() != LAMP_OFF) {
                 plugin.getLanterns().remove(i);
                 plugin.getBlockUpdateQueue().remove(new BlockUpdateAction(loc, LAMP_ON));
                 plugin.getBlockUpdateQueue().remove(new BlockUpdateAction(loc, LAMP_OFF));
                 i--;
                 continue;
             }
             
             String group = lantern.getGroup();
            long time = loc.getWorld().getTime() % 24000;
            long nighttime = plugin.getSettings().getNighttime(group);
            long daytime = plugin.getSettings().getDaytime(group);
             
             if (loc.getWorld().hasStorm() && plugin.getSettings().onThunder(group)) {
                 addUpdateAction(block, true);
                return;
            }
            
            if (daytime < nighttime) {
                addUpdateAction(block, !(time >= daytime && time < nighttime));
            } else if (nighttime < daytime) {
                addUpdateAction(block, time >= nighttime && time < daytime);
             }
         }
     }
     
     private void addUpdateAction(Block block, boolean state) {
     	Material type = state ? LAMP_ON : LAMP_OFF;
     	
     	if (plugin.getBlockUpdateQueue().contains(new BlockUpdateAction(block.getLocation(), type))) {
     		return;
     	}
     	
     	if (block.getType() != type) {
     		plugin.getBlockUpdateQueue().offer(new BlockUpdateAction(block.getLocation(), type));
     	}
     }
 
 }
