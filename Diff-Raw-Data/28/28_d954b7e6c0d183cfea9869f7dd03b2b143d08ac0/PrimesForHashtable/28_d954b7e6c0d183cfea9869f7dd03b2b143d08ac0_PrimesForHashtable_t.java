 
 import java.util.Scanner;
 
 public class PrimesForHashtable {
 
   /* Largest value with input space */
   final static int PRIME_SPACE = (int) Math.pow(2, 16);
 
   /**
    * Utilise Sieve of Eratosthenes to create an array of prime numbers
    */
   public static int[] Eratosthenes(int maxPrimeNumber) {
 
     // Define new vector A with all numbers from 2 to N
     int[] vectorA = new int[maxPrimeNumber + 1];
     for (int index = 2; index <= maxPrimeNumber; index++) {
       vectorA[index] = index;
     }
 
     int nonPrimeCount = 0;
     // Flag all non-primes in vector A.
     int sqrtMaxPrime = (int) Math.sqrt(maxPrimeNumber);
     for (int index = 2; index < sqrtMaxPrime; index++) {
       // Skip this number if known composite
       if (vectorA[index] != 0) {
         int composite = index * index;
         // Flag all composites of this number as non-prime
         while (composite <= maxPrimeNumber) {
           if (vectorA[composite] != 0) {
             vectorA[composite] = 0;
             nonPrimeCount++;
           }
           composite = composite + index;
         }
       }
     }
     // Create new array from A, filled with just primes.
     int resultIndex = 0;
    int[] primes = new int[maxPrimeNumber - nonPrimeCount - 1];
     for (int index = 2; index <= maxPrimeNumber; index++) {
       if (vectorA[index] != 0) {
         primes[resultIndex++] = vectorA[index];
       }
     }
     return primes;
   }
 
   /**
    * Perform binary search of array to locate prime number.
    */
   public static int BinarySearch(int[] vector, int key, int low, int high) {
     if (low > high) {
      return (high >= 0 ? high : 0); // Return the index of the current high value, as this
      // points to 1 below the key, if not found
     }
     int mid = (low + high) / 2;
     if (vector[mid] == key) {
       return mid;
     } else {
       if (vector[mid] > key) {
         return BinarySearch(vector, key, low, (mid - 1));
       } else {
         return BinarySearch(vector, key, (mid + 1), high);
       }
     }
   }
 
   /**
    * Returns a prime equal to or below to the number given.
    */
   public static int LookUpPrime(int[] primes, int number) {
    return primes[BinarySearch(primes, number, 0, primes.length - 1)];
   }
 
   /**
    * Main
    */
   public static void main(String[] args) {
 
     // Create a list of primes for the given input space.
     int[] primes = Eratosthenes(PRIME_SPACE);
 
     Scanner in = new Scanner(System.in);
     String line = in.nextLine();
 
     // Keep processing input until 0.
     while (!line.equals("0")) {
       // Get our number, and it's closest prime.
       int number = Integer.parseInt(line);
      // Handle special case of number 1.
      if (number != 1) {
        int prime = LookUpPrime(primes, number);
 
        // Display if it's a prime, or closet prime.
        if (prime == number) {
          System.out.println("prime");
        } else {
          System.out.println(prime);
        }
       } else {
        System.out.println("1");
       }
 
       line = in.nextLine();
     }
   }
 }
