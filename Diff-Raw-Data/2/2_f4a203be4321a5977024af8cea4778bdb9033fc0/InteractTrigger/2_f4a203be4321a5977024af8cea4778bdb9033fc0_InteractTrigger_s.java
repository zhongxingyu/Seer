 package me.tehbeard.BeardAch.achievement.triggers;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.google.gson.annotations.Expose;
 
 import me.tehbeard.BeardAch.achievement.Achievement;
 import me.tehbeard.BeardAch.dataSource.configurable.Configurable;
 import me.tehbeard.BeardAch.dataSource.json.editor.EditorField;
 import me.tehbeard.BeardAch.dataSource.json.editor.EditorFieldType;
 
 @Configurable(tag="interact",name="Click block at location")
 
 public class InteractTrigger implements ITrigger, Listener {
 
     
     @Expose
     @EditorField(alias="Block to interact",type=EditorFieldType.location)
     private Location l;
     
     private Achievement ach;
     private Set<String> active = new HashSet<String>();
     public void configure(Achievement ach, String config) {
         this.ach = ach;
         String[] c = config.split(":");
         if(c.length!=4){
             throw new IllegalArgumentException("invalid interact config");
         }
         l = new Location(Bukkit.getWorld(c[0]),
                 Double.parseDouble(c[1]),
                 Double.parseDouble(c[2]),
                 Double.parseDouble(c[3])
                 
                 );
 
     }
 
     public boolean checkAchievement(Player player) {
 
         return active.contains(player.getName());
     }
     
     @EventHandler
     public void interactEvent(PlayerInteractEvent event){
         Block b = event.getClickedBlock();
         if(b!=null){
             if(b.getLocation().equals(l)){
                 active.add(event.getPlayer().getName());
                 ach.checkAchievement(event.getPlayer());
                 active.remove(event.getPlayer().getName());
             }
         }
     }
 
     public void configure(Achievement ach) {
        
     }
 
 }
