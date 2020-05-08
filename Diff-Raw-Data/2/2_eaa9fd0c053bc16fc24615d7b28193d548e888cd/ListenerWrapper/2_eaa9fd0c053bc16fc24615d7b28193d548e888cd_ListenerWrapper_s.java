 package com.undeadscythes.udsplugin;
 
 import com.undeadscythes.udsplugin.utilities.*;
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 import org.bukkit.material.*;
 import org.bukkit.util.Vector;
 
 /**
  * Provides checks for listeners.
  * 
  * @author UndeadScythes
  */
 public class ListenerWrapper { //TODO: This is dumb, make a new class or summat.
     public static ItemStack findItem(final String item) {
         ItemStack itemStack;
         if(item.contains(":")) {
             final String itemName = item.split(":")[0];
             Material material;
             if(itemName.matches(UDSPlugin.INT_REGEX)) {
                 material = Material.getMaterial(Integer.parseInt(itemName));
             } else {
                 material = Material.matchMaterial(itemName);
             }
             if(material == null) {
                 return null;
             } else {
                 final MaterialData mat = new MaterialData(material, Byte.parseByte(item.split(":")[1]));
                 itemStack = mat.toItemStack(1);
             }
         } else {
             Material material;
             if(item.matches(UDSPlugin.INT_REGEX)) {
                 material = Material.getMaterial(Integer.parseInt(item));
             } else {
                 material = Material.matchMaterial(item);
             }
             if(material == null) {
                 final ShortItem myItem = ShortItem.getByName(item);
                 if(myItem == null) {
                     return null;
                 } else {
                     itemStack = myItem.toItemStack();
                 }
             } else {
                 itemStack = new ItemStack(material, 1);
             }
         }
         return itemStack;
     }
 
     public static boolean isShopSign(final String[] lines) {
         if(lines[0].equals(Color.SIGN + "[SHOP]")) {
             return true;
         }
         final String shopLine = lines[1];                                                           //
         final String ownerLine = lines[0];                                                          //
         final String priceLine = lines[3];                                                          //
         return (shopLine.equalsIgnoreCase(Color.SIGN + "shop")                                      //
                 || shopLine.equalsIgnoreCase("shop"))                                               //
             && ((PlayerUtils.getPlayer(ownerLine.replace(Color.SIGN.toString(), "")) != null   // Update hack fix TODO: FIX MEH!
                 || "".equals(ownerLine)                                                             //
                 || (Color.SIGN + "server").equalsIgnoreCase(ownerLine)))                            //
             && findItem(lines[2]) != null                                                           //
             && priceLine.contains(":")                                                              //
             && priceLine.split(":")[0].replace("B ", "").matches("[0-9][0-9]*")                     //
             && priceLine.split(":")[1].replace(" S", "").matches("[0-9][0-9]*");                    //
     }
 
     public final SaveablePlayer findShopOwner(final Location location) {
         for(Region shop : RegionUtils.getRegions(RegionType.SHOP)) {
             if(location.toVector().isInAABB(shop.getV1(), shop.getV2())) {
                 return shop.getOwner();
             }
         }
         return null;
     }
 
     public final Entity getAbsoluteEntity(final Entity entity) {
         if(entity instanceof Arrow) {
             return ((Arrow)entity).getShooter();
         }
         return entity;
     }
 
     public final boolean hasFlag(final Location location, final RegionFlag flag) {
         for(Region region : RegionUtils.getRegions()) {
             if(location.getWorld().equals(region.getWorld()) && location.toVector().isInAABB(region.getV1(), region.getV2())) {
                 if(region.hasFlag(flag)) return true;
             }
         }
        return Config.GLOBAL_FLAGS.get(flag);
     }
 
     public final boolean regionsContain(final List<Region> regions, final Location location) {
         for(Region region : regions) {
             if(regionContains(region, location)) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean regionContains(final Region region, final Location location) {
         return region != null && location.toVector().isInAABB(region.getV1(), region.getV2());
     }
 
     public final boolean isInQuarry(final Location location) {
         for(Region quarry : RegionUtils.getRegions(RegionType.QUARRY)) {
             if(location.toVector().isInAABB(quarry.getV1(), quarry.getV2())) {
                 return true;
             }
         }
         return false;
     }
 
     public final boolean crossesBoundary(final List<Region> regionsA, final List<Region> regionsB) {
         for(Region regionA : regionsA) {
             if(!regionA.hasFlag(RegionFlag.PISTON)) {
                 for(Region regionB : regionsB) {
                     if(!regionB.hasFlag(RegionFlag.PISTON) && !regionA.equals(regionB)) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
     
     public final Portal findPortal(final Location location) {
         for(Portal portal : PortalUtils.getPortals()) {
             if(location.toVector().isInAABB(portal.getV1().clone().add(new Vector(-1.5, -1.5, -1.5)), portal.getV2().clone().add(new Vector(1.5, 1.5, 1.5)))) {
                 return portal;
             }
         }
         return null;
     }
 }
