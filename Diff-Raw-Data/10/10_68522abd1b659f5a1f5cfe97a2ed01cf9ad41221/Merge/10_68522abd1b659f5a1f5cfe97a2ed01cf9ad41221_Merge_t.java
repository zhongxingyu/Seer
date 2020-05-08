 package cse.buffalo.edu.algorithms.sort;
 
 import cse.buffalo.edu.algorithms.stdlib.StdIn;
 import cse.buffalo.edu.algorithms.stdlib.StdOut;
 
/**
 * Top-down merge sort without any improvement.
 *
 */
 public class Merge {
 
   // Stably merge a[lo .. mid] with a[mid+1 .. hi] using aux[lo .. hi]
   // a[] and aux[] remains the same size of the original array. What changes is just
   // the lo, mid and hi position!
   private static void merge(Comparable[] a, Comparable[] aux, int lo, int mid, int hi) {
 
     // Copy to aux[]
     for (int i = lo; i <= hi; i++) {
       aux[i] = a[i];
     }
 
     // Merge back to a[]
     int i = lo, j = mid + 1;
     for (int k = lo; k <= hi; k++) {
       if      (i > mid)          a[k] = aux[j++];
       else if (j > hi)           a[k] = aux[i++];
      // Pay attention to the next line:
      // We need to merge back to a[]
      // So what we compare is aux[], not a[]
      else if (less(aux[i], aux[j])) a[k] = aux[i++];
       else                       a[k] = aux[j++];
     }
   }
 
   private static void sort(Comparable[] a, Comparable[] aux, int lo, int hi) {
 
     // Stop condition for this recursion.
     // Simply means now we have already divided to only 1 element in the array.
     if (lo >= hi) return;
 
     int mid = lo + (hi - lo) / 2;
     sort(a, aux, lo, mid);
     sort(a, aux, mid + 1, hi);
     merge(a, aux, lo, mid, hi);
   }
 
   public static void sort(Comparable[] a) {
     Comparable[] aux = new Comparable[a.length];
     sort(a, aux, 0, a.length - 1);
   }
 
   private static boolean less(Comparable v, Comparable w) {
     return (v.compareTo(w) < 0);
   }
 
   private static void exch(Comparable[] a, int i, int j) {
     Comparable tmp = a[i];
     a[i] = a[j];
     a[j] = tmp;
   }
 
   private static void show(Comparable[] a) {
     for (int i = 0; i < a.length; i++) {
       StdOut.println(a[i]);
     }
   }
 
   public static void main(String[] args) {
     String[] a = StdIn.readStrings();
     Merge.sort(a);
     show(a);
   }
 }
 
