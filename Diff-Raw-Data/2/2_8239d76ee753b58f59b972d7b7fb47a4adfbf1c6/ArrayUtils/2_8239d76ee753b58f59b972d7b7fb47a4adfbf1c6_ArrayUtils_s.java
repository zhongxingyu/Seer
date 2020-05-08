 package collections;
 
 import collections.array.CharArray;
 import collections.array.DoubleArray;
 import collections.array.IntArray;
 import collections.array.LongArray;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 
 /**
  * Created with IntelliJ IDEA.
  * User: riad
  */
 public class ArrayUtils {
     public static void sort(int[] array) {
         Collections.sort(new IntArray(array));
     }
 
     public static void sort(double[] array) {
         Collections.sort(new DoubleArray(array));
     }
 
     public static void sort(long[] array) {
         Collections.sort(new LongArray(array));
     }
 
     public static void sort(char[] array) {
         Collections.sort(new CharArray(array));
     }
 
     public static <T extends Comparable<? super T>> void sort(T[] array) {
         Arrays.sort(array);
     }
 
     public static void sort(int[] array, Comparator<Integer> comparator) {
         Collections.sort(new IntArray(array), comparator);
     }
 
     public static void sort(double[] array, Comparator<Double> comparator) {
         Collections.sort(new DoubleArray(array), comparator);
     }
 
     public static void sort(long[] array, Comparator<Long> comparator) {
         Collections.sort(new LongArray(array), comparator);
     }
 
     public static void sort(char[] array, Comparator<Character> comparator) {
         Collections.sort(new CharArray(array), comparator);
     }
 
     public static <T> void sort(T[] array, Comparator<? super T> comparator) {
         Arrays.sort(array, comparator);
     }
 
     /**
      * @param array array to reverse
      * @param from  inclusive
      * @param to    exclusive
      *              if (from == to) nothing happens
      */
     private static void reverse(int[] array, int from, int to) {
         int length = to - from;
         for (int i = 0; i < length / 2; ++i) {
             int tmp = array[from + i];
             array[from + i] = array[to - i - 1];
             array[to - i - 1] = tmp;
         }
     }
 
     public static boolean nextPermutation(int[] array) {
         for (int i = array.length - 2; i >= 0; --i) {
             if (array[i] < array[i + 1]) {
                 int lastGreater = i + 1;
                 while (lastGreater + 1 < array.length && array[lastGreater + 1] > array[i]) {
                     ++lastGreater;
                 }
                 int tmp = array[i];
                 array[i] = array[lastGreater];
                 array[lastGreater] = tmp;
                 reverse(array, i + 1, array.length);
                 return true;
             }
         }
        reverse(array, 0, array.length - 1);
         return false;
     }
 
 	public static int upperBound(long[] a, long v){
 		int l = -1, r = a.length;
 		while(l + 1< r){
 			int c = l + (r - l) / 2;
 			if(a[c] > v)
 				r = c;
 			else
 				l = c;
 		}
 		return r;
 	}
 }
