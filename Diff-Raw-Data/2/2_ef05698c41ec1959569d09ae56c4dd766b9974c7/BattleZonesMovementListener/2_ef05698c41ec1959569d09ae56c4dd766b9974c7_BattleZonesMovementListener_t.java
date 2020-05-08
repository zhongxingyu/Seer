 /*
  * Origonally written by Jacob Tyo for the BattleZones project. You may modify
  * and use the following code however you wish, as long as you give credit to its
  * original author.
  */
 package js2.battlezones;
 
 import java.util.Iterator;
 import java.util.logging.Level;
 import javax.vecmath.Point3i;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 /**
  * Listener that fires when a player moves. This class is designed to detect whether
  * a {@link Player} enters or leaves a registered PvP zone.
  * 
  * @author Jacob Tyo
  * @version 12/11/2011
  */
 public class BattleZonesMovementListener extends PlayerListener {
     public BattleZones plugin;
     public FileConfiguration zoneConfig;
     public FileConfiguration prefConfig;
     
     /**
      * Create a new instance of {@code BattleZonesMovementListener}.
      * @param plugin Parent {@link BattleZones} instance.
      */
     public BattleZonesMovementListener(BattleZones plugin)
     {
         init(plugin);
     }
 
     /**
      * Initialize all variables.
      */
     private void init(BattleZones plugin)
     {
         this.plugin                     = plugin;
         zoneConfig                      = plugin.zoneConfig.getConfig();
         prefConfig                      = plugin.prefConfig.getConfig();
     }
     
     /**
      * Calculates whether a block at {@code pLocation} collides with a cube created
      * with {@code zonePos1} and {@code zonePos2}.
      * @param pLocation Location to detect if inside cube.
      * @param zonePos1 Corner 1 of the collision bounds.
      * @param zonePos2 Corner 2 of the collision bounds.
      * @return 
      */
     public boolean isIntersecting(Location pLocation, Point3i zonePos1, Point3i zonePos2)
     {
         if (pLocation.getBlockX() >= zonePos1.x && pLocation.getBlockX() <= zonePos2.x &&
             pLocation.getBlockY() <= zonePos1.y && pLocation.getBlockY() >= zonePos2.y &&
             pLocation.getBlockZ() >= zonePos1.z && pLocation.getBlockZ() <= zonePos2.z) return true;
         return false;
     }
     
     /**
      * Calculates whether two {@link Location} objects point to the same {@link Block}
      * or not.
      * @param local1 Location to compare to local2
      * @param local2 Location to compare to local1
      * @return {@code true} if the two locations point to the same Block. {@code false}
      * if not.
      */
     public boolean isSameBlock(Location local1, Location local2)
     {
         if (local1.getBlockX() == local2.getBlockX() && local1.getBlockY() == local2.getBlockY() && local1.getBlockZ() == local2.getBlockZ()) return true;
         return false;
     }
     
     /**
      * Every time a {@link Player} moves, determine whether they are inside an enabled
      * zone or not. If so, determine what PvP permissions should be applied to the 
      * player.
      * @param event Relevant event details
      */
     @Override
     public void onPlayerMove(PlayerMoveEvent event) {
         super.onPlayerMove(event);
         if (!plugin.isZonesSet) return;
         if (isSameBlock(event.getTo(), event.getFrom().getBlock().getLocation())) return;
         boolean isInsideZone = false;
         for (Iterator<String> it = plugin.nestedZones.iterator(); it.hasNext();) {
             String[] string = it.next().split("\\.");
             if (string[0].equals(event.getPlayer().getWorld().getName()) && zoneConfig.getBoolean("zones." + string[0] + "." + string[1] + ".enabled")) {
                 String root = "zones." + string[0] + "." + string[1] + ".";
                 String currentZone = plugin.pvpHandler.playerZoneMap.get(event.getPlayer().getName());
                 boolean isOldZonePVP = isIntersecting(event.getFrom(), 
                         new Point3i(zoneConfig.getInt(root + "x1"), zoneConfig.getInt(root + "y1"), zoneConfig.getInt(root + "z1")), 
                         new Point3i(zoneConfig.getInt(root + "x2"), zoneConfig.getInt(root + "y2"), zoneConfig.getInt(root + "z2")));
                 boolean isNewZonePVP = isIntersecting(event.getTo(),
                         new Point3i(zoneConfig.getInt(root + "x1"), zoneConfig.getInt(root + "y1"), zoneConfig.getInt(root + "z1")), 
                         new Point3i(zoneConfig.getInt(root + "x2"), zoneConfig.getInt(root + "y2"), zoneConfig.getInt(root + "z2")));
                 // Is walking inside a zone
                 if (plugin.pvpHandler.playerHasPVPPermissions(event.getPlayer()) &&
                     currentZone.equals(string[0] + "." + string[1]) && isNewZonePVP && isOldZonePVP)
                 {
                     isInsideZone = true;
                 }
                 // If entering zone from outside the zone.
                 else if (plugin.pvpHandler.playerHasPVPPermissions(event.getPlayer()) && isNewZonePVP && !isOldZonePVP)
                 {
                     if (prefConfig.getBoolean(PrefConfig.PREF_DEBUG)) BattleZones.LOG.log(Level.INFO, (Message.getPrefix() + event.getPlayer().getName() + " entered the zone: " + string[1]));
                     plugin.pvpHandler.setPlayerPVP(event.getPlayer(), true);
                     plugin.pvpHandler.playerZoneMap.put(event.getPlayer().getName(), string[0] + "." + string[1]);
                     isInsideZone = true;
                     Message.send(event.getPlayer(), "Entered: " + string[1] + ". PvP " + ChatColor.GREEN + "ON!");
                 }
                 // If walking from one zone to another
                 else if (plugin.pvpHandler.playerHasPVPPermissions(event.getPlayer()) &&
                     !currentZone.equals(string[0] + "." + string[1]) && isNewZonePVP && !isOldZonePVP)
                 {
                     plugin.pvpHandler.setPlayerPVP(event.getPlayer(), true);
                     plugin.pvpHandler.playerZoneMap.put(event.getPlayer().getName(), string[0] + "." + string[1]);
                     isInsideZone = true;
                     Message.send(event.getPlayer(), "Entered: " + string[1] + ". PvP " + ChatColor.GREEN + "ON!");
                 }
             }
         }
         // If leaving a zone
         if (!isInsideZone && !plugin.pvpHandler.playerZoneMap.get(event.getPlayer().getName()).equals(""))
         {
             if (prefConfig.getBoolean(PrefConfig.PREF_DEBUG)) BattleZones.LOG.log(Level.INFO, (Message.getPrefix() + event.getPlayer().getName() + " left the zone: " + plugin.pvpHandler.playerZoneMap.get(event.getPlayer().getName())));
             plugin.pvpHandler.setPlayerPVP(event.getPlayer(), false);
             plugin.pvpHandler.playerZoneMap.put(event.getPlayer().getName(), "");
            Message.send(event.getPlayer(), "Leaving Zone. PvP " + ChatColor.RED + "OFF!");
         }
     }
     
 }
