 
 /**
  * Application for sorting numeric arrays based on Donald Shell sorting 
  * algorithim (1959). This is an improved version of my ShellSort that does
  * not use a 5 tier nest.
  *
  * @author Tyler McKinney
  * 
  * @version 30.11.11
  */
 
public class ShellSortV2 {
 
     /**
      * ArrayList of ints that contains the numbers in their original order.
      */
     private int[] set;
     private int gap;
     private int first;
     private final String VERBOSE = System.getProperty("verbose", "off");
 
     /**
      * Initializes the ShellSort and builds and ArrayList of Integer object
      * parsed from the command line arguments.
      */
     public static void main(String[] args) {
 
         ShellSortV2 sorter = new ShellSortV2(args);
         sorter.sort();
 
     }
 
     /**
      * Constructor to initiate the original field. 
      * @param original The numbers to be sorted in there original order.
      */
     public ShellSortV2(String[] original) {
 
         this.set = new int[original.length];
         for (int i = 0; i < original.length; i++) {
             this.set[i] = Integer.parseInt(original[i]);
         }
         this.gap = (int) Math.floor(set.length / 2);
         this.first = 0;
         if (VERBOSE.equals("on")) {
             print(set);
         }
     }
 
     /** Sorts the ints given in the command line arguements. */
     private void sort() {
 
         int gap = (int) Math.floor(set.length / 2);
         while (gap >= 1 ) {
             while ((first + gap) < set.length) {
                 sortRec(first, gap);
                 if (VERBOSE.equals("on")) {
                     System.out.print("Gap = " + gap + " : First = "
                             + set[first] + " : ");
                     print(set);
                 }
                 first++;
             }
             gap = (int) Math.floor(gap / 2);
             first = 0;
         }
         print(set);
     }
 
 
     /**
      * Helper method to sort, short for SortRecursion. Recursively moves
      * backwards to sort down the set.
      */
     public void sortRec(int first, int gap) {
         if (set[first] > set[first + gap]) {
             int temp = set[first];
             set[first] = set[first + gap];
             set[first + gap] = temp;
 
             while ((first - gap) >= 0) {
                 first = first - gap;
                 sortRec(first, gap);
             }
         }
     }
 
     /** Prints the given array. Mainly used for testing. */
     public void print(int[] array) {
         System.out.print("< ");
         for(int num : array) {
             System.out.print(num + " ");
         }
         System.out.println(" >");
     }
 }
 
 
 
 
 
