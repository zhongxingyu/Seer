 package me.tehbeard.BeardAch.achievement.triggers;
 
 import me.tehbeard.BeardAch.BeardAch;
 import me.tehbeard.BeardAch.achievement.Achievement;
 import me.tehbeard.BeardAch.dataSource.configurable.Configurable;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 @Configurable(tag="wgregion")
public class WorldGuardRegionTrigger implements ITrigger{
 
     private RegionManager rm;
     private String region = "";
     private String world = "";
     private Achievement ach;
 
     public void configure(Achievement ach, String config) {
         this.ach=ach;
         String[] c = config.split(":");
         if(c.length !=2){
             throw new IllegalArgumentException("Region AND World must be defined");
         }
         World w = Bukkit.getWorld(c[0]);
         WorldGuardPlugin wg = BeardAch.self.getWorldGuard();
         if(wg==null){
             BeardAch.printCon("[ERROR] WorldGuard not loaded! trigger will fail!");
             return;
         }
         rm = wg.getRegionManager(w);
         world = w.getName();
         region = c[1];
 
 
     }
 
     public boolean checkAchievement(Player player) {
         if(rm==null){
             return false;
         }
 
         ApplicableRegionSet zones = rm.getApplicableRegions(player.getLocation());
         for(ProtectedRegion zone : zones){
             if(zone.getId().equals(region)){
                 return true;
             }
         }
         // TODO Auto-generated method stub
         return false;
     }
 
     @EventHandler
     public void move(PlayerMoveEvent event){
         if(event.getTo().getBlockX() != event.getFrom().getBlockX() ||
                 event.getTo().getBlockY() != event.getFrom().getBlockY() || 
                 event.getTo().getBlockZ() != event.getFrom().getBlockZ()
                 ){
             if(event.getPlayer().getWorld().getName().equals(world)){
                 checkAchievement(event.getPlayer());
             }
         }
     }
 
 }
