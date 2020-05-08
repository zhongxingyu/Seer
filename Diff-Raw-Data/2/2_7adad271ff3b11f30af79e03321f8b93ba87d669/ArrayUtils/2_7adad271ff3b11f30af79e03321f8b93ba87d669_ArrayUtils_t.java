 package iensen;
 
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: iensen
  * Date: 2/24/13
  * Time: 10:15 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ArrayUtils
 {
         public static void shuffle(Object[] a) {
             int n = a.length;
             Random random = new Random();
 
             for (int i = 0; i < n; i++) {
                 int change = i + random.nextInt(n - i);
                 swap(a, i, change);
             }
         }
 
         private static void swap(Object[] a, int i, int changeIdx) {
             Object temp = a[i];
             a[i] = a[changeIdx];
             a[changeIdx] = temp;
         }
 
 
     public static int lower_bound(Comparable[] arr, Comparable key) {
         int len = arr.length;
         int lo = 0;
         int hi = len-1;
         int mid = (lo + hi)/2;
         while (true) {
             int cmp = arr[mid].compareTo(key);
             if (cmp == 0 || cmp > 0) {
                 hi = mid-1;
                 if (hi < lo)
                     return mid;
             } else {
                 lo = mid+1;
                 if (hi < lo)
                     return mid<len-1?mid+1:-1;
             }
             mid = (lo + hi)/2;
         }
     }
 
     public static int upper_bound(Comparable[] arr, Comparable key) {
         int len = arr.length;
         int lo = 0;
         int hi = len-1;
         int mid = (lo + hi)/2;
         while (true) {
             int cmp = arr[mid].compareTo(key);
             if (cmp == 0 || cmp < 0) {
                 lo = mid+1;
                 if (hi < lo)
                     return mid<len-1?mid+1:-1;
             } else {
                 hi = mid-1;
                 if (hi < lo)
                     return mid;
             }
             mid = (lo + hi)/2;
         }
     }
 
 
     public static void reverse(int []a)
      {
      for(int i = 0; i < a.length/2; i++)
       {
         int temp = a[i];
         a[i] = a[a.length - i - 1];
         a[a.length - i - 1] = temp;
       }
      }
 }
