 package de.sofd.util;
 
 public class IntRange
 {
     protected int min;
     
     protected int max;
 
     public IntRange( int min, int max )
     {
         this.min = min;
         this.max = max;
     }
     
     public int getDelta()
     {
         return (max - min);
     }
 
     public int getMax()
     {
         return max;
     }
 
     public int getMin()
     {
         return min;
     }
 
     public void setMax( int max )
     {
         this.max = max;
     }
 
     public void setMin( int min )
     {
         this.min = min;
     }
     
     public IntRange intersect(IntRange other) {
         return IntRange.intersect(this, other);
     }
     
     public IntRange[] subtract(IntRange other) {
         return IntRange.subtract(this, other);
     }
 
     /**
      * Intersection of two IntRanges. null references (in parameters or return values)
      * interpreted as empty ranges.
      *
      * @param r1
      * @param r2
      * @return
      */
     public static IntRange intersect(IntRange r1, IntRange r2) {
         if (r1 == null || r2 == null) {
             return null;
         }
         int newmin = Math.max(r1.getMin(), r2.getMin());
         int newmax = Math.min(r1.getMax(), r2.getMax());
         if (newmax >= newmin) {
             return new IntRange(newmin, newmax);
         } else {
             return null;
         }
     }
 
     /**
      * Subtraction of two IntRanges. null references are interpreted as
      * empty ranges.
      *
      * @param r1
      * @param r2
      * @return IntRanges (none, one or two) that together form r1-r2.
      */
     public static IntRange[] subtract(IntRange r1, IntRange r2) {
         if (r1 == null) {
            return new IntRange[]{null, null};
         }
         if (r2 == null) {
            return new IntRange[]{r1, null};
         }
         int min0 = r1.getMin();
         int max0 = Math.min(r2.getMin(), r1.getMax() + 1) - 1;
         int min1 = Math.max(r1.getMin(), r2.getMax() + 1);
         int max1 = r1.getMax();
         if (max0 >= min0) {
             if (max1 >= min1) {
                 return new IntRange[]{new IntRange(min0, max0), new IntRange(min1, max1)};
             } else {
                 return new IntRange[]{new IntRange(min0, max0)};
             }
         } else if (max1 >= min1) {
             return new IntRange[]{new IntRange(min1, max1)};
         } else {
             return new IntRange[0];
         }
     }
 
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof IntRange)) {
             return false;
         }
         IntRange irObj = (IntRange) obj;
         return getMin() == irObj.getMin() && getMax() == irObj.getMax();
     }
 
     @Override
     public String toString()
     {
         return "[" + min + ", " + max + "]";
     }
     
 }
