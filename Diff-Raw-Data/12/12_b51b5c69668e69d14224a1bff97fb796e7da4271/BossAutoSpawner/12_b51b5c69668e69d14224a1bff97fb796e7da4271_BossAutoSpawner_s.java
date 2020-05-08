 package com.ne0nx3r0.rareitemhunter.boss;
 
 import com.ne0nx3r0.rareitemhunter.RareItemHunter;
 import java.util.logging.Level;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class BossAutoSpawner implements Runnable{
     private final BossManager bm;
     private final double AUTOSPAWN_DISTANCE;
     private final RareItemHunter plugin;
 
     public BossAutoSpawner(RareItemHunter plugin, BossManager bm, double autoSpawnDistance)
     {
         this.plugin = plugin;
         this.bm = bm;
         
         // So don't square before you hand it to the object!
         this.AUTOSPAWN_DISTANCE = autoSpawnDistance * autoSpawnDistance;
     }
 
     @Override
     public void run()
     {        
         for (BossEgg egg : this.bm.bossEggs) {
             if(egg.getAutoSpawn()) {                
                 for(Player p : egg.getLocation().getWorld().getPlayers()) {
                     if(p.getLocation().distanceSquared(egg.getLocation()) < this.AUTOSPAWN_DISTANCE)
                     {                    
                         if(p.hasPermission("rareitemhunter.hunter.hatch"))
                         {
                             final BossEgg eggToHatch = egg;
 
                             // Jump back into sync
                             plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                                 @Override
                                 public void run() {
                                     plugin.getLogger().log(Level.INFO, "A legendary monster egg has been awakened at X:{0} Y:{1} Z:{2}]", new Object[]{
                                         eggToHatch.getLocation().getBlockX(), 
                                         eggToHatch.getLocation().getBlockX(), 
                                         eggToHatch.getLocation().getBlockX()}
                                     );
 
                                     for(Player p : eggToHatch.getLocation().getWorld().getPlayers())
                                     {
                                        p.sendMessage(ChatColor.DARK_GREEN+"Legendary boss "+ChatColor.WHITE+eggToHatch.getName()+ChatColor.DARK_GREEN+" has been awakened by "+ChatColor.WHITE+p.getName()+ChatColor.DARK_GREEN+"!");
                                     }
 
                                     bm.hatchBoss(eggToHatch);
                                 }
                             },1);
 
                             // only spawn one egg per attempt, could help with lag if a mass of eggs occurs
                             return;
                         }
                     }
                 }
             }
         }
     }
 }
