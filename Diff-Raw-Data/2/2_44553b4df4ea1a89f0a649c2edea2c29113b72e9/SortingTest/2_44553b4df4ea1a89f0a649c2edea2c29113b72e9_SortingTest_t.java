 package sortingalgorithms;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  *  A class for testing sorting algoritms.
  *
  *  @author  Joel Abrahamsson
  *  @version %G%
  */
 public class SortingTest
 {
   private int[] randomArray;
   private int[] nearlySortedArray;
   private int[] reversedArray;
   private int[] fewUniqueArray;
   
   private Random random;
 
   public static SortingTest tester = new SortingTest();
 
   /**
    *  Creates a new SortingTest-object
    */
   protected SortingTest()
   {
     random = new Random(System.nanoTime());
   }
 
   /**
    *  Checks whether the given array is sorted.
    */
   private boolean checkArraySorted(int[] array)
   {
     int length = array.length;
     boolean sorted = true;
 
     for (int i = 1; i < length && sorted; i++)
     {
       if (array[i] < array[i - 1])
         sorted = false;
     }
 
     return sorted;
   }
 
   /**
    *  Generates new arrays of given length.
    *
    *  @param length array length
    */
   private void generateArrays(int length)
   {
     fewUniqueArray    = generateFewUniqueArray(length);
     nearlySortedArray = generateNearlySortedArray(length);
     randomArray       = generateRandomArray(length);
     reversedArray     = generateReversedArray(length);
   }
   
   /**
    *  Generates and returns an array of given length containing few unique elements.
    *
    *  @param length array length
    *  @return       the generated array
    */
   private int[] generateFewUniqueArray(int length)
   {
     int array[] = new int[length];
 
     int uniqueValuesCount = randomInteger(10, 20);
     int[] uniqueValues    = generateRandomArray(uniqueValuesCount);
 
     for (int i = 0; i < length; i++)
       array[i] = uniqueValues[random.nextInt(uniqueValuesCount)];
 
     return array;
   }
 
   /**
    *  Generates and returns an array of given length containing nearly sorted elements.
    *
    *  @param length array length
    *  @return       the generated array
    */
   private int[] generateNearlySortedArray(int length)
   {
     int array[] = new int[length];
 
     for (int i = 0; i < length; i++)
       array[i] = randomInteger(i - 5, i + 5);
 
     return array;
   }
   
   /**
    *  Generates and returns an array of given length containing random elements.
    *
    *  @param length array length
    *  @return       the generated array
    */
   private int[] generateRandomArray(int length)
   {
     int array[] = new int[length];
     for (int i = 0; i < length; i++)
       array[i] = randomInteger(0, Integer.MAX_VALUE);
 
     return array;
   }
 
   /**
    *  Generates and returns an array of given length containing values sorted in descending order.
    *
    *  @param length array length
    *  @return       the generated array
    */
   private int[] generateReversedArray(int length)
   {
     int array[] = new int[length];
     
     for (int i = 0; i < length; i++)
       array[i] = length - i;
 
     return array;
   }
 
   /**
    *  Tests the given algorithm on the given array.
    *
    *  @param description  a description of the test
    *  @param algorithm    the SortingAlgorithm to test
    *  @param array        the array to perform the test on
    *  @return             A TestResult-object with the test results.
    */
   private TestResult performTest(String description, SortingAlgorithm algorithm, int[] array)
   {
     long timeBefore, timeAfter;
     boolean sorted;
 
     timeBefore = System.nanoTime();
 
      algorithm.sort(array);
 
     timeAfter = System.nanoTime();
 
     sorted = checkArraySorted(array);
 
     return new TestResult(description, sorted, timeAfter - timeBefore);
   }
   
   /**
    *  Returns a random integer in the given interval.
    *
    *  @param low  the lower possible value
    *  @param high the highest possible value
    *  @return a random integer in the given interval.
    */
   private int randomInteger(int low, int high)
   {
     int randomValue = low + random.nextInt(high - low);
 
     random.setSeed(System.nanoTime());
 
     return randomValue;
   }
 
   /**
    *  Tests the given SortingAlgorithm the given number of times.
    *
    *  @param algorithm SortingAlgorithm to test
    *  @param length    array length
    *  @param n         number of times to perform the test
    *  @return          TestResult-array with all the test results
    */
   public TestResult[] test(SortingAlgorithm algorithm, int length, int n)
   {
     ArrayList<TestResult> testResults = new ArrayList<TestResult>();
     for (int i = 0; i < n; i++)
     {
       generateArrays(length);
 
       testResults.add(performTest("Few Unique", algorithm, fewUniqueArray));
       testResults.add(performTest("Nearly sorted", algorithm, nearlySortedArray));
       testResults.add(performTest("Random", algorithm, randomArray));
       testResults.add(performTest("Reversed", algorithm, reversedArray));
     }
 
     TestResult[] testResultsArray = new TestResult[n * 4];
     testResults.toArray(testResultsArray);
 
     return testResultsArray;
   }
 }
