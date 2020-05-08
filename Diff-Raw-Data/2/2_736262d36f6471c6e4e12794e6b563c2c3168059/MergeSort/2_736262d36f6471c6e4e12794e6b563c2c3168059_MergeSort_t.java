 /*
  * The MIT License (MIT)
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * ****************************************************************************
  */
 
 package ch2;
 
 import edu.princeton.cs.introcs.StdOut;
 
 /**
 * Implements MergeSort algorithm.
  * 
  * Algorithm:
  * 
  * This is the first divide and conquer technique. There are many more to come.
  * 
  * MergeSort divides the input array into two equal halves and recursively sorts
  * each half and merges them resulting in a fully sorted array.
  * 
  * MergeSort is O(NlgN) algorithms with respect to time but uses an auxiliary
  * array equivalent to the size of the input array. A MergeSort algorithm not
  * using an auxiliary array was proposed by Donald Knuth in 1969.
  * 
  * Practially, mergesort can be enhanced in several ways:
  * 
  * We can fall back to insertion sort when the size of the array is 7 or less. 7
  * being the CUT_OFF_MARK.
  * 
  * 
  */
 public class MergeSort {
 
     /**
      * Merge two halves of an array in sorted fashion into a single array
      * utilizing an auxiliary array.
      * 
      * @param a
      *            input array
      * @param aux
      *            auxiliary array
      * @param lo
      *            position of the lower bound
      * @param mid
      *            position of the mid element in the array
      * @param hi
      *            position of the higher bound
      */
     private static void merge(Comparable[] a, Comparable[] aux, int lo,
             int mid, int hi, boolean ascending) {
         /* Pre-condition: the first half has to be in sorted order */
         assert isSorted(a, lo, mid, ascending);
 
         /* Pre-condition: the second half should also be in sorted order */
         assert isSorted(a, mid + 1, hi, ascending);
 
         /*
          * Deep-copy the elements of the input array (with bounds specified by
          * lo and hi) to the auxiliary array
          */
         for (int k = lo; k <= hi; k++)
             aux[k] = a[k];
 
         int i = lo;
         int j = mid + 1;
 
         for (int k = lo; k <= hi; k++) {
             if (i > mid)
                 a[k] = aux[j++];
             else if (j > hi)
                 a[k] = aux[i++];
             else if (lessThan(aux[j], aux[i])) {
                 /* Second value is less than first value. */
                 if (ascending == true)
                     a[k] = aux[j++]; // Copy the least value to the first slot.
                 else
                     a[k] = aux[i++]; // Copy the highest value to the first slot
                                      // since we need the descending order.
             } else {
                 /* Second value is greater than the first value. */
                 if (ascending == true)
                     a[k] = aux[i++]; // Copy the least value to the first slot.
                 else
                     a[k] = aux[j++]; // Copy the highest value to the first slot
                 // since we need the descending order.
             }
         }
 
         /* Post-condition: the merged array should be in sorted order */
         assert isSorted(aux, lo, hi, ascending);
     }
 
 
     /**
      * Performs the core operation of this program i.e. sorting the entries in
      * an array that is expected to have implemented the Comparable interface.
      * 
      * @param a
      *            an input array which is to be sorted and has implemented the
      *            Comparable interface
      * 
      * @param ascending
      *            if true, the input will be sorted in ascending order, else
      *            descending order
      */
     public static void sort(Comparable[] a, Comparable[] aux, int lo, int hi,
             boolean ascending) {
         if (hi <= lo) return;
         int mid = lo + (hi - lo) / 2;
         sort(a, aux, lo, mid, ascending);
         sort(a, aux, mid + 1, hi, ascending);
         merge(a, aux, lo, mid, hi, ascending);
     }
 
 
     /**
      * Performs the core operation of this program by creating and allocating
      * memory to an auxiliary array and triggering the recursive overloaded sort
      * method on the contents of the array.
      * 
      * @param a
      *            an input array which is to be sorted and has implemented the
      *            Comparable interface
      * 
      * @param ascending
      *            if true, the input will be sorted in ascending order, else
      *            descending order
      */
     public static void sort(Comparable[] a, boolean ascending) {
         Comparable[] aux = new Comparable[a.length];
         sort(a, aux, 0, a.length - 1, ascending); // length - 1 is required
                                                   // because we would be
                                                   // computing mid using the
                                                   // formula lo+(hi-lo)/2 so hi
                                                   // has to be an inclusive
                                                   // value.
     }
 
 
     /**
      * A helper method to return true if the first argument is less than the
      * second argument.
      * 
      * Note: Both the first and the second arguments should implement the
      * {@link Comparable} interface and should override the compareTo method.
      * 
      * @param v
      *            first value to be compared against the second value
      * @param w
      *            second value that would be compared against the first
      * @return true if the first value is less than the second value, else false
      */
     private static boolean lessThan(Comparable v, Comparable w) {
         return v.compareTo(w) < 0;
     }
 
 
     /**
      * Swaps two values in the array which is expected to have implemented the
      * Comparable interface provided the reference to the array and the
      * positions to be swapped are passed as arguments.
      * 
      * @param a
      *            an array that is expected to have implemented the Comparable
      *            interface
      * @param i
      *            the position in the array which has to be swapped with the
      *            other position signified by the second argument
      * @param j
      *            the position in the array which has to be swapped with the
      *            other position signified by the first argument
      */
     private static void exchange(Comparable[] a, int i, int j) {
         Comparable t = a[i];
         a[i] = a[j];
         a[j] = t;
     }
 
 
     /**
      * Display the array on the console.
      * 
      * @param a
      *            the input array which is expected to have implemented the
      *            Comparable interface
      */
     private static void show(Comparable[] a) {
         // Print the array, on a single line.
         for (int i = 0; i < a.length; i++) {
             StdOut.print(a[i] + "  ");
         }
         StdOut.println();
     }
 
 
     /**
      * A helper method that verifies if the contents of the array are in sorted
      * order. The contents are scanned between lo and hi values (inclusive).
      * 
      * @param a
      *            input array which is expected to have implemented the
      *            Comparable interface.
      * 
      * @param lo
      *            lower bound
      * 
      * @param hi
      *            higher bound
      * 
      * @param ascending
      *            if true, the input will be checked for ascending order, else
      *            descending order
      * 
      * @return true if all the entries in the array are in the expected order.
      */
     public static boolean isSorted(Comparable[] a, int lo, int hi,
             boolean ascending) {
         // Test whether the array entries are in order.
         for (int i = lo + 1; i <= hi; i++) {
             if (ascending == true) {
                 // If the consecutive number in less the previous number in the
                 // array, return false
                 if (lessThan(a[i], a[i - 1])) return false;
             } else {
                 // Descending - the next number in the array should be less than
                 // the current number.
                 if (lessThan(a[i - 1], a[i])) return false;
             }
         }
         return true;
     }
 
 }
