 package com.undeadscythes.udsplugin;
 
 import com.undeadscythes.udsplugin.SaveablePlayer.PlayerRank;
 import java.util.*;
 import org.apache.commons.lang.*;
 import org.bukkit.*;
 
 /**
  * A warp point used for player teleportation.
  * @author UndeadScythes
  */
 public class Warp implements Saveable {
     /**
      * File name of warp file.
      */
     public static String PATH = "warps.csv";
 
     private String name;
     private Location location;
     private PlayerRank rank;
     private int price;
 
     /**
      * Initialise a brand new warp point.
      * @param name Warp name.
      * @param location Location of the warp.
      * @param rank Rank required to use the warp.
      * @param price Money required to use the warp.
      */
     public Warp(String name, Location location, PlayerRank rank, int price) {
         this.name = name;
         this.location = location;
         this.rank = rank;
         this.price = price;
     }
 
     /**
      * Initialise a warp from a string record.
      * @param record A line from a file.
      */
     public Warp(String record) {
         String[] recordSplit = record.split("\t");
         name = recordSplit[0];
         location = new Bearing(recordSplit[1]);
         rank = PlayerRank.getByName(recordSplit[2]);
         price = Integer.parseInt(recordSplit[3]);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public String getRecord() {
         ArrayList<String> record = new ArrayList<String>();
         record.add(name);
         record.add(location.toString());
         record.add(rank.toString());
         record.add(price + "");
         return StringUtils.join(record, "\t");
     }
 
     /**
      * Get the name of the warp.
      * @return Warp name.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Get the price required to use the warp.
      * @return The price to use the warp.
      */
     public int getPrice() {
         return price;
     }
 
     @Override
     public String toString() {
         return name;
     }
 
     /**
      * Get the location of the warp.
      * @return Warp location.
      */
     public Location getLocation() {
         return location;
     }
 
     /**
      * Get the rank required to use this warp.
      * @return Rank needed to use this warp.
      */
     public PlayerRank getRank() {
         return rank;
     }
 
     /**
      * Find a centered safe place at the given coordinates.
      * @param world World to search.
      * @param x X coordinate.
      * @param z Z coordinate.
      * @return A safe place centered in a block.
      */
     public static Location findSafePlace(World world, double x, double z) {
         Location safePlace = world.getHighestBlockAt((int)x, (int)z).getLocation();
         safePlace.add(.5, 0, .5);
         return safePlace;
     }
 
     public static Location findSafePlace(Location location) {
         return findSafePlace(location.getWorld(), location.getX(), location.getZ());
     }
 }
