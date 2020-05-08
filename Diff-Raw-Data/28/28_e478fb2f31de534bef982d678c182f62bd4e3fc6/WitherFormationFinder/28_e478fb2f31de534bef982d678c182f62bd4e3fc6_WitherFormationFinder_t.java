 package me.limebyte.endercraftessentials;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 
 public class WitherFormationFinder {
 
     public static void searchArea(Player player, int radius) {
         Location check = player.getLocation();
         final int x = check.getBlockX();
         final int z = check.getBlockZ();
        boolean found = false;
 
         for (int xOffset = -radius; xOffset < radius; xOffset++) {
             check.setX(x + xOffset);
             for (int zOffset = -radius; zOffset < radius; zOffset++) {
                 check.setZ(z + zOffset);
                 if (checkY(check)) {
                     player.sendMessage("Found bedrock formation at " + check.getBlockX() + ", " + check.getBlockZ() + ".");
                    found = true;
                 }
             }
         }

        if (!found) {
            player.sendMessage("No bedrock formations found.");
        }
     }
 
     private static boolean checkY(Location location) {
         location.setY(1);
        if (location.getBlock().getType() == Material.BEDROCK) return false;
 
         location.setY(2);
        if (location.getBlock().getType() == Material.BEDROCK) return false;
 
         location.setY(3);
        if (location.getBlock().getType() == Material.BEDROCK) return false;
 
         location.setY(4);
         if (location.getBlock().getType() != Material.BEDROCK) return false;
 
         return true;
     }
 
 }
