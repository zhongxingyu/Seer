 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.flags.area;
 
 import io.github.alshain01.flags.*;
 import io.github.alshain01.flags.System;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import io.github.alshain01.flags.exception.InvalidAreaException;
 import io.github.alshain01.flags.exception.InvalidSubdivisionException;
 import me.tabinol.factoid.Factoid;
 import me.tabinol.factoid.lands.Land;
 import me.tabinol.factoid.playercontainer.PlayerContainerType;
 
 import org.apache.commons.lang.Validate;
 import org.bukkit.Location;
 
 import javax.annotation.Nonnull;
 
 
 /**
  * Class for creating areas to manage a Factoid Land.
  */
 public class FactoidLand extends Area implements Removable, Subdivision {
     private final Land land;
 
     /**
      * Creates an instance of FactoidLand based on a Bukkit
      * Location
      *
      * @param location
      *            The Bukkit location
      */
     public FactoidLand(Location location) {
         land = Factoid.getLands().getLand(location);
     }
 
     /**
      * Creates an instance of FactoidLand based on a Land
      * name
      *
      * @param name
      *            The land name
      */
     public FactoidLand(String name) {
         land = Factoid.getLands().getLand(name);
     }
 
     /**
      * Gets if there is a land at the location.
      *
      * @return True if a land exists at the location.
      */
     public static boolean hasLand(Location location) {
         return Factoid.getLands().getLand(location) != null;
     }
 
     /**
      * Gets the Land object embedded in the area class.
      *
      * @return The Land object
      */
     @SuppressWarnings("WeakerAccess") // API
     public Land getLand() {
         return land;
     }
 
     @Override
     public String getSystemID() {
         if(isArea()) { return land.getAncestor(land.getGenealogy()).getName(); }
         throw new InvalidAreaException();
     }
 
     @Override
     public System getSystemType() {
         return System.FACTOID;
     }
     @Override
     public Set<String> getOwners() {
         if(isArea()) {
             String owner = null;
             if(land.getOwner().getContainerType() == PlayerContainerType.PLAYER) {
                 owner = land.getOwner().getName();
             }
             return new HashSet<String>(Arrays.asList(owner));
         }
         throw new InvalidAreaException();
     }
 
     @Override
     public org.bukkit.World getWorld() {
        if(isArea()) { return land.getWord(); }
         throw new InvalidAreaException();
     }
 
     @Override
     public boolean isArea() { return land != null; }
 
     @Override
     public String getSystemSubID() {
         if(isArea()) {
             if(isSubdivision()) { return land.getName(); }
             throw new InvalidSubdivisionException();
         }
         throw new InvalidAreaException();
     }
 
     @Override
     public boolean isSubdivision() {
         if(isArea()) { return land.getParent() != null; }
         throw new InvalidAreaException();
     }
 
     @Override
     public boolean isParent(Area area) {
         Validate.notNull(area);
         if(isArea()) {
             if(isSubdivision()) { return area instanceof FactoidLand && land.getParent() == ((FactoidLand)area).getLand(); }
             throw new InvalidSubdivisionException();
         }
         throw new InvalidAreaException();
     }
 
     @Override
     public Area getParent() {
         if(isArea()) {
             if(isSubdivision()) { return new FactoidLand(land.getParent().getName()); }
             throw new InvalidSubdivisionException();
         }
         throw new InvalidAreaException();
     }
 
     @Override
     public boolean isInherited() {
         if(isArea()) {
             if(!isSubdivision()) { return Flags.getDataStore().readInheritance(this); }
             throw new InvalidAreaException();
         }
         throw new InvalidSubdivisionException();
     }
 
     @Override
     public void setInherited(boolean value) {
         if(isArea()) {
             if(isSubdivision()) { Flags.getDataStore().writeInheritance(this, value); }
             throw new InvalidSubdivisionException();
         }
         throw new InvalidAreaException();
     }
 
     /**
      * Permanently removes the area from the data store USE CAUTION!
      */
     @Override
     public void remove() {
         if(isArea()) { Flags.getDataStore().remove(this); }
         throw new InvalidAreaException();
     }
 
     /**
      * 0 if the the claims are the same
      * -1 if the claim is a subdivision of the provided claim.
      * 1 if the claim is a parent of the provided claim.
      * 2 if they are "sister" subdivisions. 3 if they are completely unrelated.
      *
      * @return The value of the comparison.
      */
     @Override
     public int compareTo(@Nonnull Area a) {
         Validate.notNull(a);
         if (!(a instanceof FactoidLand)) {
             return 3;
         }
 
         Land testLand = ((FactoidLand)a).getLand();
         if (land.equals(testLand)) {
             return 0;
         } else if (land.getParent() != null && land.getParent().equals(testLand)) {
             return -1;
         } else if (testLand.getParent() != null && testLand.getParent().equals(land)) {
             return 1;
         } else if (land.getParent() != null && land.getParent().equals(testLand.getParent())) {
             return 2;
         }
         return 3;
     }
 }
