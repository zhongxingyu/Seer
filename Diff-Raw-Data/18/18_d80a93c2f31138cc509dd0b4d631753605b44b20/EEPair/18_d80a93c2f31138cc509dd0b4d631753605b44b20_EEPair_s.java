 package com.github.kuben.realshopping;
 
 import org.bukkit.Location;
 
 public final class EEPair {//An entrance and exit
 
     final private Location entrance, exit;
 
     public EEPair(Location entrance, Location exit) {
         if (entrance == null || exit == null) {
             throw new NullPointerException();
         }
         this.entrance = entrance;
         this.exit = exit;
     }
 
     public boolean hasEntrance(Location en) {
         return entrance.equals(en);
     }
 
     public boolean hasExit(Location ex) {
         return exit.equals(ex);
     }
     //Clone because 0.5 will be added
 
     public Location getEntrance() {
         return entrance.clone();
     }
 
     public Location getExit() {
         return exit.clone();
     }
 
     @Override
     public String toString() {
        return "Entrance: " + entrance + ", exit: " + exit;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((entrance == null) ? 0 : entrance.hashCode());
         result = prime * result + ((exit == null) ? 0 : exit.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         EEPair other = (EEPair) obj;
         if (entrance == null) {
             if (other.entrance != null) {
                 return false;
             }
         } else if (!entrance.equals(other.entrance)) {
             return false;
         }
         if (exit == null) {
             if (other.exit != null) {
                 return false;
             }
         } else if (!exit.equals(other.exit)) {
             return false;
         }
         return true;
     }
 }
