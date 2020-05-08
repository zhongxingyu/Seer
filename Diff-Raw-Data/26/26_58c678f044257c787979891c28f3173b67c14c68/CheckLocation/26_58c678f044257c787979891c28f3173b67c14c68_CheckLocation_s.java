 package com.nullblock.vemacs.antifoottrap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 public class CheckLocation extends BukkitRunnable {
 
     public static int interval = 2;
     public float sec = 10;
 
     public CheckLocation() {
 
     }
 
     public void run() {
         for (ConcurrentHashMap.Entry<String, Location> entry : AntiFootTrap.triggeredPlayers.entrySet()) {
             Player p = Bukkit.getPlayer(entry.getKey());
             Location l1 = entry.getValue();
            AntiFootTrap.triggeredPlayers.put(p.getName(), p.getLocation());
             if (isSameXZ(l1, p.getLocation())) {
                 AntiFootTrap.playerTimes.put(p.getName(), AntiFootTrap.playerTimes.get(p.getName()) + 1);
                 if (AntiFootTrap.playerTimes.get(p.getName()) >= sec * (20 / interval)) {
                     p.teleport(p.getWorld().getSpawnLocation());
                     AntiFootTrap.triggeredPlayers.remove(p.getName());
                     AntiFootTrap.playerTimes.remove(p.getName());
                     getPlugin().getLogger().info(p.getName() + " has been TPed to spawn");
                 }
             }
         }
     }
 
     private boolean isSameXZ(Location loc1, Location loc2) {
         float range = 1.1F;
         if (Math.abs(loc1.getX() - loc2.getX()) < (range / 2) && Math.abs(loc1.getZ() - loc2.getZ()) < (range / 2)) {
             return true;
         }
         return false;
     }
 
     private Plugin getPlugin() {
         return Bukkit.getPluginManager().getPlugin("AntiFootTrap");
     }
 
 }
