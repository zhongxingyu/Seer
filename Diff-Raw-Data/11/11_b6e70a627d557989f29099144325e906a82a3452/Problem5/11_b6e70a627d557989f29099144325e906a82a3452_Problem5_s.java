 public class Problem5 {
     //What is the smallest positive number that is evenly divisible by all of the numbers from 1 to 20?
     public static final int LIMIT = 20;
 
 
     public static void main(String[] args) {
         int[] result = highestPowerOfEachPrimeFactor();
         int answer = 1;
         for (int i = 0; i < LIMIT; i++) {
            if (result[i] != 0) System.out.println(i + " to the " + result[i] + " *");
             if (result[i] != 0) answer *= Math.pow(i, result[i]);
         }
         System.out.println(answer);
     }
 
     static boolean[] isPrime = primes(LIMIT);
 
     //factors are represented with an array as: n = Product of (i to the factors[i]) for i in [1..20]
     static int[] primeDecomposition(int n, int[] factors) {
         if (isPrime[n]) {
             factors[n]++;
             return factors;
         }
         for (int i = 0; i < n; i++) {
             if (isPrime[i] && n % i == 0) {
                 factors[i]++;
                 primeDecomposition(n / i, factors);
                 return factors;
             }
         }

         return factors;
     }
 
    static int[][] allDecompositions() {
        int[][] all = new int[20][20];
         for (int i = 0; i < LIMIT; i++) {
             all[i] = primeDecomposition(i, new int[LIMIT]);
         }
         return all;
     }
 
     static int[] highestPowerOfEachPrimeFactor() {
         int[] result = new int[LIMIT];
        int[][] allFactors = allDecompositions();
         for (int i = 0; i < LIMIT; i++) {
             int max = 0;
             for (int j = 0; j < LIMIT; j++) {
                 if (allFactors[j][i] > max) max = allFactors[j][i];
             }
             result[i] = max;
         }
         return result;
     }
 
     private static boolean[] primes(int n) {
         boolean[] isPrime = new boolean[n + 1];
         for (int i = 0; i < isPrime.length; i++) isPrime[i] = true;
         isPrime[0] = isPrime[1] = false;
         for (int j = 2; j < isPrime.length; j++) {
             int prime = j;
             if (!isPrime[prime]) continue;
             for (int i = prime * 2; i < isPrime.length; i = i + prime) isPrime[i] = false;
         }
         return isPrime;
     }
 }
