 package gkimfl.prime;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 
 public class PrimeNumbersRange {
 
     /**
      * Calculate integer square root by binary search.
      */
     public static BigInteger getSquareRoot(BigInteger n) {
         BigInteger high = n.shiftRight(n.bitLength() >> 1);
         BigInteger low = n.divide(high).add(BigInteger.ONE);
 
         if (high.compareTo(low) < 0) {
             BigInteger tmp = low;
             low = high;
             high = tmp;
         }
 
         while (high.compareTo(low) >= 0) {
             BigInteger mid = low.add(high).shiftRight(1);
             if (mid.multiply(mid).compareTo(n) > 0) {
                 high = mid.subtract(BigInteger.ONE);
             }
             else {
                 low = mid.add(BigInteger.ONE);
             }
         }
         return low.subtract(BigInteger.ONE);
     }
 
     /**
      * Generate a list of numbers with potential prime factors.
      * 
      * The list consists of products of prime numbers from two to the square
      * root of val. The numbers are ordered by likelihood; the lowest primes are
      * in the first product, and the highest primes are in the last product.
      */
     public static BigInteger[] getFactoringValues(BigInteger val) {
         BigInteger sqrtVal = getSquareRoot(val);
         ArrayList<BigInteger> factors = new ArrayList<BigInteger>();
         BigInteger current = BigInteger.ONE;
         val = val.pow(50);
         for (BigInteger prime : new PrimeNumbers()) {
             if (prime.compareTo(sqrtVal) > 0) {
                 break;
             }
             if (current.compareTo(val) < 0) {
                 current = current.multiply(prime);
             }
             else {
                 factors.add(current);
                 current = prime;
             }
         }
         factors.add(current);
         BigInteger[] values = factors.toArray(new BigInteger[factors.size()]);
         return values;
     }
 
     /**
      * Test is val shares a common factor with factors.
      */
     public static boolean isPrime(BigInteger val, BigInteger factors) {
         return factors.gcd(val).equals(BigInteger.ONE);
     }
 
     /**
      * Test if val shares a common factor with any factors in factoring.
      */
     public static boolean isPrime(BigInteger val, BigInteger[] factoring) {
         for (BigInteger factors : factoring) {
             if (!isPrime(val, factors)) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Test if a single number is prime.
      * 
      * This is inefficient! It is much better to reuse the factoring values over
      * a large pool of candidate primes.
      */
     public static boolean isPrime(BigInteger val) {
         return isPrime(val, getFactoringValues(val));
     }
 
     /**
      * Ahh, yes! Run the algorithm and print some stats.
      */
     public static void main(String[] argv) {
        // look for primes in the range from start up to end
        BigInteger start = new BigInteger("1000000000000000");
        BigInteger end = start.add(BigInteger.valueOf(10000));

         // print prime numbers that are found
         boolean show = false;
 
         long time = 0;
         int count = 0;
         int delta = end.subtract(start).intValue();
 
         time = -System.nanoTime();
        BigInteger[] factoring = getFactoringValues(end);
         time += System.nanoTime();
         System.out.format("Preparation in %.2f sec (%d composite values)\n",
                 time / 1000000000f, factoring.length);
 
         time = -System.nanoTime();
         for (; !start.equals(end); start = start.add(BigInteger.ONE)) {
             if (isPrime(start, factoring)) {
                 if (show) {
                     time += System.nanoTime();
                     System.out.println(start);
                     time -= System.nanoTime();
                 }
                 ++count;
             }
         }
         time += System.nanoTime();
         System.out.format("Tested %d numbers in %.2f sec (%.2f per sec)\n",
                 delta, time / 1000000000f, 1000000000f * delta / time);
         System.out.format("Found %d prime numbers\n",
                 count);
     }
 }
