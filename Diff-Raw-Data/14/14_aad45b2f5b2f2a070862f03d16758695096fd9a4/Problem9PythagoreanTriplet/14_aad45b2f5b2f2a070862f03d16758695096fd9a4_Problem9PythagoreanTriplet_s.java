 package de.olilo.euler.level1;
 
 public class Problem9PythagoreanTriplet {
 
     public Triplet findPythagoreanTriplet(int sum) {
         int a = 1;
         int b = sum / 4;
         int c = sum - b - a;
        while (a*a + b*b != c*c && a < b) {
            if (a*a + b*b < c*c) {
                 b++;
                 c--;
             } else {
                 a++;
                 b--;
             }
         }
 
         // if a >= b then no pythagorean triplet could be found
         if (a >= b) {
             throw new IllegalArgumentException("No pythagorean triplet found for " + sum);
         }
 
         return new Triplet(a, b, c);
     }
 
     public static class Triplet {
 
         public final int a;
         public final int b;
         public final int c;
 
         public Triplet(int a, int b, int c) {
             this.a = a;
             this.b = b;
             this.c = c;
         }
 
         public long getProduct() {
            return a * b * c;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             Triplet triplet = (Triplet) o;
 
             return a == triplet.a && b == triplet.b && c == triplet.c;
         }
 
         @Override
         public int hashCode() {
             int result = a;
             result = 31 * result + b;
             result = 31 * result + c;
             return result;
         }
 
         @Override
         public String toString() {
             return "Triplet{" +
                     "a=" + a +
                     ", b=" + b +
                     ", c=" + c +
                     ";; product=" + getProduct() +
                     '}';
         }
     }
 
 }
