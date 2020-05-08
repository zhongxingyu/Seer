 package com.muni.fi.pa165.enums;
 
 /**
  * Represents different types of Weapons.
  *
  * @author Aubrey Oosthuizen
  */
 public enum WeaponType {
 
    GUN, BALDE, BLUNT, EXPLOSIVE;
 
     /**
      * Gives all possible weapon types separated by comma.
      *
      * @return string with all values in WeaponType
      */
     public static String getList() {
         StringBuilder builder = new StringBuilder();
 
         for (WeaponType t : values()) {
 
             builder.append(t.name());
             builder.append(", ");
 
         }
 
         builder.deleteCharAt(builder.length() - 1);
         builder.deleteCharAt(builder.length() - 1);
 
         return builder.toString();
     }
 }
