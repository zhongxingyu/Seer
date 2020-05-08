 public class QSort {
   
 	/**
 	 * Quicksort the array a[] using m as cutoff to insertion sort.
 	 */
 	public static void quicksort(int[] a, int m) {
 		quicksort(a, 0, a.length - 1, m);
 	}
 
 	/**
 	 * Quicksort the subarray a[low .. high].
 	 * Uses median-of-three partitioning
 	 * and a cutoff to insertion sort of m.
 	 */
 	private static void quicksort(int[] a, int low, int high, int m) {
 		if (high <= low + m) {
             insertionsort(a, low, high);
             return;
         }
 
 	} // dummy code
 
     private static int partition(int[] a, int low, int high, int pivotIndex) {
        int pivotValue = medianOfThree(a[low], a[high], a[a.length / 2]);
     }
 
     /**
      * Return the median of three integers.
      */
     private static int medianOfThree(int low, int high, int middle) {
         if (low > middle) swap(low, middle);
         if (low > high) swap(low, high);
         if (middle > high) swap(middle, high);
         return middle;
     }
 
     /**
      * Swap two integers.
      */
     private static void swap(int a, int b) {
         int tmp = a;
         a = b;
         b = tmp;
     }
 
 	/**
 	 * Sort from a[low] to a[high] using insertion sort.
 	 */
 	private static void insertionsort(int[] a, int low, int high) {
 		for (int i = low + 1; i <= high; i++) {
             int cur = a[i];
             int j = i - 1;
             while ((j >= 0) && (a[j] > cur)) {
                 a[j + 1] = a[j--];
             }
             a[j + 1] = cur;
         }
 	} // dummy code
 }
