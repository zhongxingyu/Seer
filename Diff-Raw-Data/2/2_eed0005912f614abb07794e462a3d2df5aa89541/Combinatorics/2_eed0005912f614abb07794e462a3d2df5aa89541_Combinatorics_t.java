 import java.util.HashMap;
 
 public class Combinatorics
 {
     /**
      * C(n, k): no replacement, no ordering.
      */
     public long choose(long n, long k) {
         return factorial(n) / (factorial(k) * (factorial(n - k)));
     }
 
     /**
      * P(n, k): no replacement, with ordering.
      */
     public long permutations(long n, long k) {
         return factorial(n) / (factorial(n - k));
     }
 
     /**
      * Pa(n, r): with replacement, no ordering.
      */
     public long partitions(long n, long r) {
         return factorial(n + r - 1) / (factorial(n) * factorial(r - 1));
     }
 
     /**
      * S(n, k): with replacement, with ordering.
      */
     public long samples(long n, long k) {
         return (long) Math.pow(n, k);
     }
 
     private static HashMap<Long, Long> factorials = new HashMap<Long, Long>();
     /**
      * n!
      */
     public static long factorial(long x) {
         if (x < 0) return 0;
        if (factorials.containsKey(x)) return factorials.get(x);
 
         long xf;
         if (x == 0) xf = 1;
         else xf = x * factorial(x - 1);
 
         factorials.put(x, xf);
         return xf;
     }
 }
