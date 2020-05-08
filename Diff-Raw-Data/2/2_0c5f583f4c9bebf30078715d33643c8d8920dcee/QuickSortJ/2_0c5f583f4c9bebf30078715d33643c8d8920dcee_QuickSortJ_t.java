 package guibin.zhang.algorithm;
 
 /**
  * The basic concept is to pick one of the elements in the array as a pivot
  * value around which the other elements will be rearranged. Everything less
  * than the pivot is moved left of the pivot (into the left partition).
  * Similarly, everything greater than the pivot goes into the right partition.
  * At this point each partition is recursively quicksorted.
  *
  * The Quicksort algorithm is fastest when the median of the array is chosen as
  * the pivot value. That is because the resulting partitions are of very similar
  * size. Each partition splits itself in two and thus the base case is reached
  * very quickly.
  *
  * In practice, the Quicksort algorithm becomes very slow when the array passed
  * to it is already close to being sorted. Because there is no efficient way for
  * the computer to find the median element to use as the pivot, the first
  * element of the array is used as the pivot. So when the array is almost
  * sorted, Quicksort doesn't partition it equally. This means that one of the
  * recursion branches is much deeper than the other, and causes execution time
  * to go up. Thus, it is said that the more random the arrangement of the array,
  * the faster the Quicksort Algorithm finishes.
  *
  * @author guibin
  */
 public class QuickSortJ {
 
     //http://www.mycstutorials.com/articles/sorting/quicksort
     public void doSort(int[] array, int start, int end) {
         int i = start;
         int k = end;
 
         // check that there are at least two elements to sort
         if (end - start >= 1) {
             // set the pivot as the first element in the partition
             int pivot = array[start];
 
             while (k > i) { // while the scan indices from left and right have not met,
                 // From the left find the first element great than the pivot.
                 while (array[i] <= pivot && i < end) {
                     i++;
                 }
                 // From the right find the first element not greater than the pivot.
                 while (array[k] > pivot && k > start) {
                     k--;
                 }
                 // If the left index is still smaller than the right index, 
                 // swap the corresponding elements
                 if (k > i) {
                     swap(array, i, k);
                 }
             }
             // after the indices have crossed, 
             // swap the last element in the left partition with the pivot 
             swap(array, start, k);
             // Now the pivot is at position k
 
             //Sort the left partition
             doSort(array, start, k - 1);
             //Sort the right partition
             doSort(array, k + 1, end);
         }
     }
 
     /**
      * It partitions the portion of the array between indexes left and right,
      * inclusively, by moving all elements less than array[pivotIndex] before
      * the pivot, and the equal or greater elements after it.
      *
      * @param array
      * @param left left is the index of the leftmost element of the array
      * @param right right is the index of the rightmost element of the array
      * (inclusive) number of elements in subarray = right-left+1
      * @param pivotIndex
      * @return new pivot index
      */
     public int partition(int[] array, int left, int right, int pivotIndex) {
         int pivotValue = array[pivotIndex];
         // Move pivot to end
         swap(array, pivotIndex, right);
         // storeIndex is used to mark the position of the small one partition.
         int storeIndex = left;
 
         for (int i = left; i < right; i++) {
             if (array[i] < pivotValue) {
                 swap(array, i, storeIndex);
                 storeIndex++;
             }
         }
         // Move pivot to its final place
         swap(array, storeIndex, right);
 
         return storeIndex;
     }
 
     public int partition(int[] array, int p, int r) {
         //Let the last element the pivot
        int pivotValue = array[r];
         //The marker to less or equal to  the pivot
         int i = p - 1;
         for (int j=p; j<r; j++) {
             if(array[j] <= pivotValue) {
                 i++;
                 swap(array, i, j);
             }
         }
         //Move the pivot to the final place(The position i+1)
         swap(array, i+1, r);
         
         return i+1;
     }
     
     public void doSortInplaceVersion(int[] array, int start, int end) {
 
         if (start < end) {
             //choose any 'pivotIndex' such that 'left' ≤ 'pivotIndex' ≤ 'right'
             int pivotIndex = start;
 
             // Get lists of bigger and smaller items and final position of pivot
             int pivotIndexNew = partition(array, start, end, pivotIndex);
             doSortInplaceVersion(array, start, pivotIndexNew - 1);
             doSortInplaceVersion(array, pivotIndexNew + 1, end);
         }
     }
 
     private void swap(int[] array, int i, int k) {
         int temp = array[i];
         array[i] = array[k];
         array[k] = temp;
     }
 
     private void printArray(int[] array) {
         for (int i : array) {
             System.out.print(i + ",");
         }
         System.out.println();
     }
 
     public static void main(String[] args) {
         QuickSortJ qs = new QuickSortJ();
 
         int[] array = {3, 1, 4, 5, 9, 2, 6, 8, 7};
 //        int[] array = {1,2,3,4,5,6,7,10,8};
 //        qs.doSort(array, 0, array.length - 1);
         qs.doSortInplaceVersion(array, 0, array.length - 1);
         qs.printArray(array);
     }
 }
