 package de.olilo.euler;
 
 import de.olilo.euler.level1.*;
 
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Oliver Lorenz
  * @since 0.1
  */
 public class AllProblems {
 
     public static void main(String[] args) throws Exception {
         final long startTime = System.currentTimeMillis();
         final List<Long> timestamps = new ArrayList<Long>();
         runLevel1(timestamps);
 
         System.out.println();
         System.out.println("Solved all " + timestamps.size() + " problems");
         LongestDurationStatistic longest = findLongestRuntime(timestamps, startTime);
         System.out.printf("Average time needed: %.2fms; longest time: %dms (problem #%d); total time: %dms\n",
                 averageOf(timestamps, startTime),
                 longest.getDuration(), longest.getIndex() + 1,
                 sum(timestamps, startTime));
     }
 
     static void runLevel1(List<Long> timestamps) throws Exception {
         System.out.println("Problem 1: Multiples of 3 and 5; result: " +
                 new Problem1Multiples().solveIteratively(1000));
         timestamps.add(System.currentTimeMillis());
         System.out.println("Problem 2: Sum of even Fibonaccis to 4.000.000; result: " +
                 new Problem2Fibonacci().solve(4000000));
         timestamps.add(System.currentTimeMillis());
         // problem 3 and the addendum are merged into same timestamp statistic
         System.out.println("Problem 3: Largest prime factor of 600.851.475.143: " +
                 new Problem3LargestPrime().getLargestPrimeOf(600851475143L));
         System.out.println("Addendum Problem 3: Smallest number with 100 unique prime factors: " +
                 new Problem3PrimeFactorsAddendum().findSmallestNumberWithNDistinctPrimeFactors(100));
         timestamps.add(System.currentTimeMillis());
         System.out.println("Problem 4: Biggest palindrome that is the product of two 3-digit numbers: " +
                 new Problem4Palindrome().findBiggestPalindromeFromTwoNDigitedNumbers(3));
         timestamps.add(System.currentTimeMillis());
         System.out.println("Problem 5: Smallest multiple up to 20: " +
                 new Problem5SmallestMultiple().findSmallestMultipleUpTo(20));
         timestamps.add(System.currentTimeMillis());
         System.out.println("Problem 6: Difference between square of sum and sum of squares up to 100: " +
                 new Problem6SumSquareDIfference().getSumSquareDifference(100));
         timestamps.add(System.currentTimeMillis());
         System.out.println("Problem 7: 10001st prime number is: " +
                 new Problem7NthPrime().getNthPrime(10001));
         timestamps.add(System.currentTimeMillis());
         final Problem8GreatestProduct problem8 = new Problem8GreatestProduct();
         final FileReader fileWithNumber = new FileReader("problem8number.txt");
         System.out.println("Problem 8: Greatest product of five consecutive digits" +
                 " in 1000-digit number: " +
                 problem8.greatestProduct(problem8.readNumberFrom(fileWithNumber), 5));
         fileWithNumber.close();
         timestamps.add(System.currentTimeMillis());
         System.out.println("Problem 9: Pythagorean triplet with sum of 1000: " +
                 new Problem9PythagoreanTriplet().findPythagoreanTriplet(1000));
        System.out.println("Problem 9 Addendum: Pythagorean triplet with sum of 100000: " +
                new Problem9PythagoreanTriplet().findPythagoreanTriplet(100000));
         timestamps.add(System.currentTimeMillis());
         System.out.println("Problem 10: Sum of all primes up to 2.000.000: " +
                 new Problem10SumOfPrimes().sumOfPrimesUpTo(2000000));
         timestamps.add(System.currentTimeMillis());
     }
 
     static double averageOf(List<Long> timestamps, long start) {
         return ((double) sum(timestamps, start)) / timestamps.size();
     }
 
     static long sum(List<Long> timestamps, long start) {
         return timestamps.get(timestamps.size() - 1) - start;
     }
 
     static LongestDurationStatistic findLongestRuntime(List<Long> timestamps, long start) {
         final LongestDurationStatistic statistic = new LongestDurationStatistic();
         long previous = start;
         for (int i = 0; i < timestamps.size(); i++) {
             long timestamp = timestamps.get(i);
             statistic.checkAndUpdate(timestamp - previous, i);
             previous = timestamp;
         }
         return statistic;
     }
 
 }
