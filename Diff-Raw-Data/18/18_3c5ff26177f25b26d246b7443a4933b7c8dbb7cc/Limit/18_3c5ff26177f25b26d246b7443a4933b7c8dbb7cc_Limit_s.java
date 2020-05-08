 package ru.alepar.minesweeper;
 
 public class Limit {
 
     public final Region region;
     public final int min;
     public final int max;
 
     public Limit(Region region, int min, int max) {
         this.region = region;
         this.min = min;
         this.max = max;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Limit limit = (Limit) o;
 
        if (max != limit.max) return false;
        if (min != limit.min) return false;
        if (!region.equals(limit.region)) return false;
 
        return true;
     }
 
     @Override
     public int hashCode() {
         int result = region.hashCode();
         result = 31 * result + min;
         result = 31 * result + max;
         return result;
     }
 }
