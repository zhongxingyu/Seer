 package algdat.arithmetics;
 
 public class Modulo {
 
     public static long exp(long n, long k, long p) {
         long result = 1;
        while (k > 0) {
             if ((k & 1) == 1) {
                 result = (result * n) % p;
             }
             k >>= 1;
             n = (n * n) % p;
         }
         return result;
     }
 
 }
