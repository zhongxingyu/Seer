 package net.madz.db.core.meta.immutable.types;
 
 public enum SortDirectionEnum {
     ascending,
     descending,
     unknown;
 
     public static SortDirectionEnum getSortDirection(String direction) {
         if ( direction.equalsIgnoreCase("A") ) {
             return SortDirectionEnum.ascending;
         } else if ( direction.equalsIgnoreCase("D") ) {
             return SortDirectionEnum.descending;
         } else {
             return SortDirectionEnum.unknown;
         }
     }
 }
