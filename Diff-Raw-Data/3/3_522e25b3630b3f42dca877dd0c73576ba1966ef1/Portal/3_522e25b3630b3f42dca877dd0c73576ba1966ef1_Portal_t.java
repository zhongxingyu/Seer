 package com.undeadscythes.udsplugin;
 
 import com.undeadscythes.udsplugin.utilities.*;
 import java.util.ArrayList;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 
 /**
  * A nether portal that goes elsewhere.
  * @author Dave
  */
 public class Portal implements Saveable {
     /**
      * File name of portal file.
      */
     public static final String PATH = "portals.csv";
 
     private final String name;
     private Warp warp;
     private Portal portal;
     private String portalLink = "";
     private final World world;
     private final Vector min, max;
     private Direction exit;
 
     public Portal(final String name, final Warp warp, final World world, final Vector v1, final Vector v2) {
         this.name = name;
         this.warp = warp;
         this.world = world;
         this.min = Region.floor(Vector.getMinimum(v1, v2));
         this.max = Region.floor(Vector.getMaximum(v1, v2));
     }
     
     public Portal(final String record) {
         final String[] recordSplit = record.split("\t");
         switch (recordSplit.length) {
             case 7:
                 exit = Direction.getByName(recordSplit[6]);
             default:
                 name = recordSplit[0];
                 warp = UDSPlugin.getWarps().get(recordSplit[1]);
                 portalLink = recordSplit[2];
                 world = Bukkit.getWorld(recordSplit[3]);
                 min = Region.getBlockPos(recordSplit[4]);
                 max = Region.getBlockPos(recordSplit[5]);
         }
     }
 
     @Override
     public final String getRecord() {
         final ArrayList<String> record = new ArrayList<String>();
         record.add(name);
         record.add(warp == null ? "null" : warp.getName());
         record.add(portal == null ? "null" : portal.getName());
         record.add(world.getName());
         record.add(min.toString());
         record.add(max.toString());
         record.add(exit == null ? "null" : exit.toString());
         return StringUtils.join(record, "\t");
     }
     
     public final boolean hasWarp() {
         if(warp != null || portal != null) {
             return true;
         }
         return false;
     }
     
     public final String getWarpName() {
         if(warp == null) {
             if(portal != null) {
                 return portal.getName() + "*";
             } else {
                 return null;
             }
         }
         return warp.getName();
     }
     
     public final void setWarp(final Warp warp) {
         this.warp = warp;
     }
     
     public final void setPortal(final Portal portal) {
         warp = null;
         this.portal = portal;
     }
     
     public final String getName() {
         return name;
     }
     
     public final Vector getV1() {
         return min;
     }
     
     public final Vector getV2() {
         return max;
     }
     
     public final World getWorld() {
         return world;
     }
     
     public final void warp(final Player player) {
         if(warp == null) {
             if(portal != null) {
                 final Vector half = portal.getV2().clone().subtract(portal.getV1()).multiply(0.5);
                 final Location mid = portal.getV1().clone().add(half).add(new Vector(0.5, 0, 0.5)).toLocation(portal.getWorld());
                 final Location to = Warp.findFloor(mid);
                 to.setYaw(portal.getYaw());
                player.teleport(to);
                player.setFlying(false);
             }
         } else {
             player.teleport(warp.getLocation());
         }
     }
     
     public final void warp(final Entity entity) {
         if(warp == null) {
             if(portal != null) {
                 final Vector half = portal.getV2().clone().subtract(portal.getV1()).multiply(0.5);
                 final Location mid = portal.getV1().clone().add(half).add(new Vector(0.5, 0, 0.5)).toLocation(portal.getWorld());
                 entity.teleport(Warp.findFloor(mid));
             }
         } else {
             entity.teleport(warp.getLocation());
         }
     }
     
     public final boolean crossWorld() {
         if(warp == null) {
             if(portal != null && world.equals(portal.getWorld())) {
                 return false;
             }
             return true;
         } else {
             if(world.equals(warp.getLocation().getWorld())) {
                 return false;
             }
             return true;
         }
     }
     
     public final void linkPortal() {
         if(!portalLink.equals("")) {
             setPortal(UDSPlugin.getPortals().get(portalLink));
         }
     }
     
     public final float getYaw() {
         return exit == null ? 0 : exit.getYaw();
     }
     
     public final void setExit(final Direction exit) {
         this.exit = exit;
     }
 }
