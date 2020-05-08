 package me.limebyte.battlenight.core.util;
 
 import me.limebyte.battlenight.core.BattleNight;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class SmokeEffect {
 
     public static void play(final Player player) {
         int taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(BattleNight.instance, new Runnable() {
 
             public void run() {
                 smoke(player.getLocation(), Direction.MIDDLE);
             }
 
        }, 0, 60L);
 
         Bukkit.getServer().getScheduler().cancelTask(taskID);
     }
 
     private static void smoke(Location location, Direction direction) {
         location.getWorld().playEffect(location, Effect.SMOKE, direction.getValue());
     }
 
     private enum Direction {
         SOUTH_EAST(0),
         SOUTH(1),
         SOUTH_WEST(2),
         EAST(3),
         MIDDLE(4),
         WEST(5),
         NORTH_EAST(6),
         NORTH(7),
         NORTH_WEST(8);
 
         private int value;
 
         Direction(int value) {
             this.value = value;
         }
 
         public int getValue() {
             return value;
         }
     }
 }
