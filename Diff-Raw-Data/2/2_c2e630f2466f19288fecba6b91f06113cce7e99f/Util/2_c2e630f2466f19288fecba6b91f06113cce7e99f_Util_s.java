 package me.limebyte.battlenight.core.util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class Util {
 
     ////////////////////
     //   Locations    //
     ////////////////////
 
     private static final String LOC_SEP = ", ";
 
     public static String locationToString(Location loc) {
         String w = loc.getWorld().getName();
         double x = loc.getBlockX() + 0.5;
         double y = loc.getBlockY();
         double z = loc.getBlockZ() + 0.5;
         float yaw = loc.getYaw();
         float pitch = loc.getPitch();
         return w + "(" + x + LOC_SEP + y + LOC_SEP + z + LOC_SEP + yaw + LOC_SEP + pitch + ")";
     }
 
     public static Location locationFromString(String s) {
        String[] parts = s.split("(");
         World w = Bukkit.getServer().getWorld(parts[0]);
 
         String[] coords = parts[1].substring(0, parts[1].length() - 1).split(LOC_SEP);
         double x = Double.parseDouble(coords[0]);
         double y = Double.parseDouble(coords[1]);
         double z = Double.parseDouble(coords[2]);
         float yaw = Float.parseFloat(coords[3]);
         float pitch = Float.parseFloat(coords[4]);
 
         return new Location(w, x, y, z, yaw, pitch);
     }
 
     ////////////////////
     //     Items      //
     ////////////////////
 
     public static void clearInventory(Player player) {
         PlayerInventory inv = player.getInventory();
         inv.clear();
         inv.setArmorContents(new ItemStack[inv.getArmorContents().length]);
     }
 }
