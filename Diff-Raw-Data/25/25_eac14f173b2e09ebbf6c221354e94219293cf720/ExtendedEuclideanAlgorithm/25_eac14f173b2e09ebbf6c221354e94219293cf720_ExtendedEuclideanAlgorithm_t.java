public class ExtendedEuclideanAlgorithm {
    public static long gcd(long p, long q) {
         if (q == 0) return p;
         else return gcd(q, p % q);
     }
 
     public static long[] gcdExtended(long p, long q) {
         if (q == 0)
            return new long[] { p, 1, 0 };
 
        long[] vals = gcdExtended(q, p % q);
        long d = vals[0];
        long a = vals[2];
        long b = vals[1] - (p / q) * vals[2];
        return new long[] { d, a, b };
     }
 }
