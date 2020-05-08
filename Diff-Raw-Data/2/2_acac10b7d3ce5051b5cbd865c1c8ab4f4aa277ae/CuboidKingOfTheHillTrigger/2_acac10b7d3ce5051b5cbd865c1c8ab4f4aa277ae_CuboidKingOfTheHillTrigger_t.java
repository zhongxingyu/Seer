 package me.tehbeard.BeardAch.achievement.triggers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import me.tehbeard.BeardAch.BeardAch;
 import me.tehbeard.BeardAch.achievement.Achievement;
 import me.tehbeard.BeardAch.achievement.help.Argument;
 import me.tehbeard.BeardAch.achievement.help.Usage;
 import me.tehbeard.BeardAch.dataSource.configurable.Configurable;
 import me.tehbeard.utils.cuboid.Cuboid;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 /**
  * Checks if a players is in a cuboid for a specified amount of time
  * @author James
  *
  */
 @Configurable(tag="koth")
 @Usage(arguments={
         @Argument(name="World",desc=""),
         @Argument(name="x1",desc=""),
        @Argument(name="y1",desc=""),
         @Argument(name="z1",desc=""),
         @Argument(name="x2",desc=""),
         @Argument(name="y2",desc=""),
         @Argument(name="z2",desc=""),
         @Argument(name="/",desc=""),
         @Argument(name="time",desc="time in seconds player must be inside area"),
         },packageName="base",blurb="Provides a king of the hill trigger, player must be inside area continuously for x seconds")
 public class CuboidKingOfTheHillTrigger implements ITrigger,Listener {
 
 
     private Cuboid c = new Cuboid();
     private int time = 0;
     private Achievement ach;
 
     private Map<String,Long> times = new HashMap<String, Long>();
 
 
     public void configure(Achievement ach,String config) {
         this.ach = ach;
         String[] con= config.split("\\/");
         if(con.length == 2){
             c.setCuboid(con[0]);
             time = Integer.parseInt(con[1]);
         }
     }
 
     public boolean checkAchievement(Player player) {
 
         long currentTime = System.currentTimeMillis() / 1000L;
         
         if(hasTime(player.getName())){
             return (currentTime-getTime(player.getName()))>=time;
         }
 
         return false;
     }
 
     public ArrayList<String> getCache(){
         return c.getChunks();
     }
 
     private boolean hasTime(String player){
         return times.containsKey(player);
     }
 
     private Long getTime(String player){
         if(!hasTime(player)){
             times.put(player, System.currentTimeMillis()/1000L);
         }
         return times.get(player);
     }
 
     @EventHandler(priority=EventPriority.HIGHEST)
     public void onPlayerMove(PlayerMoveEvent event){
         if(event.getTo().getBlockX() != event.getFrom().getBlockX() ||
                 event.getTo().getBlockY() != event.getFrom().getBlockY() || 
                 event.getTo().getBlockZ() != event.getFrom().getBlockZ()
                 ){
             Player player = event.getPlayer();
             boolean wasInside = c.isInside(event.getFrom());
             boolean isInside  = c.isInside(event.getTo());
             long currentTime = System.currentTimeMillis() / 1000L;
 
             if(wasInside){
                 l(player.getName() + " was inside cuboid");
                 if((currentTime-getTime(player.getName()))>=time){
                     l(player.getName() + " was inside cuboid for required amount of time");
                     BeardAch.self.getAchievementManager().checkAchievement(ach);
                     
                 }
                 if(!isInside){
                     l(player.getName() + " left cuboid, clearing time");
                     times.remove(player.getName());
                 }
             }
             if(isInside && !wasInside){
                 l(player.getName() + " entered cuboid, starting timer.");
                 getTime(player.getName());
             }
 
             
 
         }
     }
 
         private void l(String l){
             System.out.println(l);
         }
 
     }
