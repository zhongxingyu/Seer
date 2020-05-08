 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lightmagic.magics;
 
 import bluenova.fairytailcraft.plugin.MagePluginEvent;
 import bluenova.fairytailcraft.plugin.MagePluginRegion;
 import java.util.List;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.util.Vector;
 
 /**
  *
  * @author Sven
  */
 public class LightWall extends MagePluginEvent {
 
     @Override
     public boolean callPlayerInteractEvent(PlayerInteractEvent event) {
         if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             Player player = event.getPlayer();
             Vector dir = player.getTargetBlock(null, 200).getLocation().toVector();
             Location pos = player.getLocation();
             Location[] directArr = new Location[]{
                 new Location(pos.getWorld(), pos.getBlockX() + 1, pos.getBlockY(), pos.getBlockZ()),
                 new Location(pos.getWorld(), pos.getBlockX() - 1, pos.getBlockY(), pos.getBlockZ()),
                 new Location(pos.getWorld(), pos.getBlockX(), pos.getBlockY(), pos.getBlockZ() + 1),
                 new Location(pos.getWorld(), pos.getBlockX(), pos.getBlockY(), pos.getBlockZ() - 1)
             };
             Location[] edgesArr = new Location[]{
                 new Location(pos.getWorld(), pos.getBlockX() + 1, pos.getBlockY(), pos.getBlockZ() + 1),
                 new Location(pos.getWorld(), pos.getBlockX() - 1, pos.getBlockY(), pos.getBlockZ() - 1),
                 new Location(pos.getWorld(), pos.getBlockX() - 1, pos.getBlockY(), pos.getBlockZ() + 1),
                 new Location(pos.getWorld(), pos.getBlockX() + 1, pos.getBlockY(), pos.getBlockZ() - 1)
             };
             double minDis = 100000.0;
             double distance = 0.0;
             Location loc = null;
             boolean isEdge = false;
             wallDirections dirrect = wallDirections.NORTH;
             for (int i = 0; i < 4; i++) {
                 distance = dir.distance(directArr[i].toVector());
                 if (distance < minDis) {
                     minDis = distance;
                     loc = directArr[i];
                     switch (i) {
                         case 0:
                             dirrect = wallDirections.NORTH;
                             break;
                         case 1:
                             dirrect = wallDirections.SOUTH;
                             break;
                         case 2:
                             dirrect = wallDirections.WEST;
                             break;
                         case 3:
                             dirrect = wallDirections.EAST;
                             break;
                     }
                 }
 
             }
             for (int i = 0; i < 4; i++) {
                 distance = dir.distance(edgesArr[i].toVector());
 
                 if (distance < minDis) {
                     minDis = distance;
                     loc = edgesArr[i];
                     isEdge = true;
                     switch (i) {
 
                         case 0:
                             dirrect = wallDirections.NORTHWEST;
                             break;
                         case 1:
                             dirrect = wallDirections.SOUTHEAST;
                             break;
                         case 2:
                             dirrect = wallDirections.SOUTHWEST;
                             break;
                         case 3:
                             dirrect = wallDirections.NORTHEAST;
                             break;
                     }
                 }
             }
 
             Location[] wallArr;
             //Generate Wall
             if (!isEdge) {
                 if (dirrect == wallDirections.NORTH || dirrect == wallDirections.SOUTH) {
                     MagePluginRegion regio = new MagePluginRegion(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() - 2), new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() + 2, loc.getBlockZ() + 2), loc.getWorld());
                     wallArr = regio.getLocations().toArray(new Location[0]);
                 } else {
                     MagePluginRegion regio = new MagePluginRegion(new Location(loc.getWorld(), loc.getBlockX() - 2, loc.getBlockY(), loc.getBlockZ()), new Location(loc.getWorld(), loc.getBlockX() + 2, loc.getBlockY() + 2, loc.getBlockZ()), loc.getWorld());
                     wallArr = regio.getLocations().toArray(new Location[0]);
                 }
             } else {
                 if (dirrect == wallDirections.NORTHEAST) {
                     MagePluginRegion regio = new MagePluginRegion(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), new Location(loc.getWorld(), loc.getBlockX() - 2, loc.getBlockY() + 2, loc.getBlockZ()), loc.getWorld());
                     MagePluginRegion regio2 = new MagePluginRegion(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() + 2, loc.getBlockZ() + 2), loc.getWorld());
                     List<Location> locations = regio.getLocations();
                     List<Location> locations2 = regio2.getLocations();
                     locations.addAll(locations2);
                     wallArr = locations.toArray(new Location[0]);
                 } else if (dirrect == wallDirections.NORTHWEST) {
                     MagePluginRegion regio = new MagePluginRegion(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), new Location(loc.getWorld(), loc.getBlockX() - 2, loc.getBlockY() + 2, loc.getBlockZ()), loc.getWorld());
                     MagePluginRegion regio2 = new MagePluginRegion(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() + 2, loc.getBlockZ() - 2), loc.getWorld());
                     List<Location> locations = regio.getLocations();
                     List<Location> locations2 = regio2.getLocations();
                     locations.addAll(locations2);
                     wallArr = locations.toArray(new Location[0]);
 
                 } else if (dirrect == wallDirections.SOUTHWEST) {
                     MagePluginRegion regio = new MagePluginRegion(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), new Location(loc.getWorld(), loc.getBlockX() + 2, loc.getBlockY() + 2, loc.getBlockZ()), loc.getWorld());
                    MagePluginRegion regio2 = new MagePluginRegion(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() + 2, loc.getBlockZ() - 2), loc.getWorld());
                     List<Location> locations = regio.getLocations();
                     List<Location> locations2 = regio2.getLocations();
                     locations.addAll(locations2);
                     wallArr = locations.toArray(new Location[0]);
 
                 } else {
                     MagePluginRegion regio = new MagePluginRegion(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), new Location(loc.getWorld(), loc.getBlockX() + 2, loc.getBlockY() + 2, loc.getBlockZ()), loc.getWorld());
                    MagePluginRegion regio2 = new MagePluginRegion(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() + 2, loc.getBlockZ() + 2), loc.getWorld());
                     List<Location> locations = regio.getLocations();
                     List<Location> locations2 = regio2.getLocations();
                     locations.addAll(locations2);
                     wallArr = locations.toArray(new Location[0]);
                 }
             }
             for (int i = 0; i < wallArr.length; i++) {
                if (wallArr[i].getBlock().getType() == Material.AIR || wallArr[i].getBlock().getType() == Material.GRASS) {
                     wallArr[i].getBlock().setType(Material.GLOWSTONE);
                 }
             }
             return true;
         }
         return false;
     }
 
     private enum wallDirections {
 
         NORTH, SOUTH, EAST, WEST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST
     }
 }
