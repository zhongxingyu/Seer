 package fr.aumgn.tobenamed.util;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.tobenamed.TBN;
 
 public final class TBNUtil {
 
     private TBNUtil() {
     }
 
     public static void broadcast(String message) {
         for (Player player : Bukkit.getOnlinePlayers()) {
             player.sendMessage(message);
         }
     }
 
     public static void broadcast(String permission, String message) {
         for (Player player : Bukkit.getOnlinePlayers()) {
             if (player.hasPermission(permission)) {
                 player.sendMessage(message);
             }
         }
     }
 
     public static <T> T pickRandom(List<T> from) {
         return from.get(TBN.getRandom().nextInt(from.size()));
     }
 
     public static int scheduleDelayed(int delay, Runnable runnable) {
         return Bukkit.getScheduler().scheduleSyncDelayedTask(
                 TBN.getPlugin(), runnable, delay);
     }
 
     public static int scheduleRepeating(int delay, Runnable runnable) {
         return Bukkit.getScheduler().scheduleSyncRepeatingTask(
                 TBN.getPlugin(), runnable, delay, delay);
     }
 }
