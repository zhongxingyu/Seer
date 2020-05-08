 /*
 **
 **	CPTR440
 **	Andrew Breja
 **	Java Threaded Merge Sort
 **
 */
 
 package mergesort;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Vector;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public class MergeSort 
 {
         public static Vector<Integer> masterItemCollection = new Vector<Integer>();
 
         // Function to generate random numbers
         public static void generateIntegers(int givenCollectionSize)
         {
                 // Create a random generager
                 Random randomNumberGenerator = new Random();
 
                 // For each 
                 for (int i = 0; i < givenCollectionSize; i++)
                 {
                         // Generate numbers between 0 and 1000
                         masterItemCollection.add(randomNumberGenerator.nextInt(1000));
                 }
         }
 
 
         public static void main(String[] args) 
         {
                 // Variables
                 int collectionSize = 0;
                 int numberOfThreads = 0;
                 
                 ArrayList<Sorter> sorterList = new ArrayList<Sorter>();
 
                 // Get command line parameters
                 if (args.length == 2)
                 {
                         collectionSize = Integer.parseInt(args[0]);
                         numberOfThreads = Integer.parseInt(args[1]);
                 }
                 // Bad parameters
                 else
                 {
                         System.err.println("You really dun goofd");
                         System.exit(0);
                 }
                 
                 // Generate random numbers in the master item collection
                 generateIntegers(collectionSize);
 
                 // Executor service
                 ExecutorService threadScheduler = Executors.newFixedThreadPool(numberOfThreads);
 
                 for (int iteratorIndex = 1; iteratorIndex <= numberOfThreads; iteratorIndex++)
                 {
                         // Create new sorter
                         Sorter mergeSorter = new Sorter(masterItemCollection, collectionSize, numberOfThreads, iteratorIndex);
 
                         // Keep track of the sorter
                         sorterList.add(mergeSorter);
 
                         // Add item to thread scheduler
                         threadScheduler.execute(mergeSorter);
                 }
 
                 // Tell the threadScheduler that there are no more things to process and it should shutdown
                 // and stop expecting new tasks to process
                 threadScheduler.shutdown();
 
                 // Hold/pause main method (this one) execution until the threadScheduler is completed
                 while (!threadScheduler.isTerminated()) {}
 
                 // Merge items back together
 
                 boolean notSorted == true;
 
                int currentValue = -1;
                int foreignValue = 0;
                 int completed = 0;
                 int listSize = sorterList.count();
 
                 ArrayList<Integer> sortedItemList = new ArrayList<Integer>();
 
                 while (notSorted)
                 {
                         for (Sorter currentSorter : sorterList)
                         {
                                 completed = 0;
                                
                                 if (currentValue == currentSorter.getItem())
                                 {
                                         while (currentValue == currentSorter.getItem())
                                         {
                                                 // Get a fresh item and add it to the sorted list
                                                 sortedItemList.add(currentSorter.getItem());
 
                                                 // Increment sorter index
                                                 currentSorter.incrementIndex();
                                         }
                                 }
                                 else
                                 {
                                         completed++;
 
                                         if (completed == listSize)
                                         {
                                                 notSorted = false;
                                         }
                                 }
                         }
 
                         // Increment current value
                         currentValue++;
                 }

                /*

                

                */
         }
 }
