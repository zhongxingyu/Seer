 package me.limebyte.battlenight.core.util;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 
 import me.limebyte.battlenight.api.battle.Waypoint;
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.hooks.Nameplates;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 public class SafeTeleporter implements Listener {
 
     public static Set<String> telePass = new HashSet<String>();
     private static Queue<String> playerQueue = new LinkedList<String>();
     private static Queue<Location> locationQueue = new LinkedList<Location>();
     private static int taskID = 0;
 
     private static Map<String, Location> teleporters = new HashMap<String, Location>();
 
     public static void queue(Player player, Waypoint waypoint) {
         queue(player, waypoint.getLocation());
     }
 
     public static void queue(Player player, Location location) {
         playerQueue.add(player.getName());
         locationQueue.add(location);
     }
 
     public static void tp(Player player, Waypoint waypoint) {
         tp(player, waypoint.getLocation().clone());
     }
 
     public static void tp(Player player, Location location) {
         safeTP(player, location.clone());
     }
 
     public static void startTeleporting() {
         taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(BattleNight.instance, new Runnable() {
             @Override
             public void run() {
                 if (playerQueue.isEmpty()) {
                     stopTeleporting();
                 } else {
                     tp(Bukkit.getPlayerExact(playerQueue.poll()), locationQueue.poll());
                 }
             }
         }, 0L, 10L);
     }
 
     private static void stopTeleporting() {
         Bukkit.getServer().getScheduler().cancelTask(taskID);
         taskID = 0;
     }
 
     private static void safeTP(final Player player, Location location) {
        if (player.hasMetadata("NPC")) return;

         Location loc = location;
         loc.setY(loc.getY() + 1);
 
         String name = player.getName();
 
         telePass.add(name);
         player.teleport(loc, TeleportCause.PLUGIN);
         telePass.remove(name);
 
         teleporters.put(name, loc);
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         Player player = event.getPlayer();
         String name = player.getName();
 
         if (teleporters.containsKey(name)) {
             Location loc = teleporters.get(name);
             teleporters.remove(name);
 
             telePass.add(name);
             player.teleport(loc, TeleportCause.PLUGIN);
             telePass.remove(name);
 
             Nameplates.refresh(player);
         }
     }
 }
