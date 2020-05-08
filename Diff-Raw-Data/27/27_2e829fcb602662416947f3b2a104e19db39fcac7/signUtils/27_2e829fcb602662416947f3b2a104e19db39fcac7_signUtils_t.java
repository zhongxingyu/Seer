 //@author Chris Price (xCP23x)
 
 package org.xcp23x.restockit;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class signUtils {
     
     public static Material getMaterial(String line){
         return getType(line) > 0 ? Material.getMaterial(getType(line)) : Material.AIR;
     }
     
     public static short getDamage(String line) {
         String dmgStr = (line.indexOf(":") >= 0 ? line : line+":0").split(":")[1];
         return Short.parseShort(dmgStr);
     }
     
     public static int getType(String line) {
         if (line.equalsIgnoreCase("Incinerator")) {
             return 0;
         }
         Material t;
 	String sa[] = (line.indexOf(":") ==1 ? line : line+":0").split(":");
         
 	if ((t = Material.getMaterial(sa[0])) == null) {
             try {
             	if ((t = Material.getMaterial(Integer.parseInt(sa[0]))) == null) {
                     return -1; // it's an int, but not an valid Material ID.
             	}
             } catch (NumberFormatException ex) {
             	return -2; // it's not an int or ID.
             }
 	}
 
         try {
             Short.parseShort(sa[1]);
 	} catch (NumberFormatException ex) {
             return -3; // problem with damage value
 	}
 		
 	return t.getId();
     }
     
     public static void setMaxItems(int items, Block block) {
         Sign sign = (Sign)block.getState();
         String part = sign.getLine(3).split(",")[1];
         sign.setLine(3, items + "," + part);
         sign.update();
     }
     
     public static int getMaxItems(String line) {
         String str = line.contains(",") ? line.split(",")[0].toLowerCase().replaceAll(" ", "") : null;
         if(str==null) return 0;
         
         try {
             if(str.contains("i") || str.contains("items") || str.contains("item")) {
                 return Integer.parseInt(str.replaceAll("items", "").replaceAll("i", "").replaceAll("item", ""));
             }
             
             if(str.contains("s") || str.contains("stacks") || str.contains("stack")) {
                 return Integer.parseInt(str.replaceAll("stack", "").replaceAll("s", ""))*getMaterial(line).getMaxStackSize();
             }
             
             return Integer.parseInt(str);
         } catch(Exception ex){}
         
         return 0;
     }
     
     public static void setPeriod(float period, Block block) {
         Sign sign = (Sign)block.getState();
         String part1 = sign.getLine(3).split(",")[0];
         String part2 = period%20 == 0 ? period/20 + "s" : period + "t";
         sign.setLine(3, part1 + ", " + part2);
         sign.update();
     }
     
     public static int getPeriod(String line) {
         String periodStr = line.contains(",") ? line.split(",")[1].replaceAll(" ", "") : "0";
         return periodStr.contains("s") ? Short.parseShort(periodStr.replaceAll("s", ""))*20 : Integer.parseInt(periodStr);
     }
     
     public static boolean isRIsign(String line){
         String str = line.toLowerCase();
         return (str.contains("restockit") || str.contains("restock it") || str.contains("full chest") || str.contains("full dispenser"));
     }
     
     public static void dropSign(Block sign) {
         Location loc = sign.getWorld().getBlockAt(sign.getX(), sign.getY(), sign.getZ()).getLocation();
         sign.setType(Material.AIR);
         sign.getWorld().dropItem(loc, new ItemStack(Material.SIGN, 1));
     }
     
     public static boolean isDelayedSign(String line) {
         if(getMaxItems(line) == 0 || getPeriod(line) == 0) return false;
         return true;
     }
     
     public static boolean isIncinerator(String line) {
         if(line.toLowerCase().contains("incinerator")) return true;
         else return false;
     }
     
     public static Block getSignFromChest(Block chest) {
         if(chestUtils.isSignAboveChest(chest)) return getSignAboveChest(chest);
         if(chestUtils.isSignBelowChest(chest)) return getSignBelowChest(chest);
         return null;
     }
     
     public static Block getSignAboveChest(Block chest) {
         return chest.getWorld().getBlockAt(chest.getX(), chest.getY() +1, chest.getZ());
     }
     
     public static Block getSignBelowChest(Block chest) {
         return chest.getWorld().getBlockAt(chest.getX(), chest.getY() -1, chest.getZ());
     }
     
     public static boolean line2hasErrors(String line, Player player) {
         
         switch(getType(line)){
             case -3:
                 playerUtils.sendPlayerMessage(player, 5, line.split(":")[1]);
                 return true;
             case -2:
                 playerUtils.sendPlayerMessage(player, 4, line.contains(":") ? line.split(":")[0] : line);
                 return true;
             case -1:
                 playerUtils.sendPlayerMessage(player, 3, line.contains(":") ? line.split(":")[0] : line);
                 return true;
         }
         return false;
     }
 }
