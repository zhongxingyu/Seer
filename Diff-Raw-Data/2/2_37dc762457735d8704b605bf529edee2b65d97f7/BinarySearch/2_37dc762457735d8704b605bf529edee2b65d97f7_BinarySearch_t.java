 import static java.lang.System.out;
 
 import java.util.Arrays;
 
 /**
  * Implements a simple a binary search in a array (sorted).
  * 
  * <h5>Lecture: Order-of-Growth Classifications (Week 1)</h5>
  * 
  * <p>Uses at most 1 + lg N compares to search a element.
  * 
  * @author eder.magalhaes
  */
 public class BinarySearch {
 
     public static int search(int key, int[] a) {
         int lo = 0;
         int hi = a.length - 1;
         
         while (lo <= hi) {
            int mid = lo + (hi - lo) / 2; //Java bug (2006)
             
             if (key < a[mid]) 
             	hi = mid - 1;
             else if (key > a[mid]) 
             	lo = mid + 1;
             else 
             	return mid;
         }
         return -1;
     }
 
     public static void main(String[] args) {
         int[] data = new int [] { 5, 1, 2, 4, 3, 9, 8, 7, 0, 6 };
 
         Arrays.sort(data);
         int k = 4;
         out.printf("search for %s return %s", k, search(k, data));
     }
 
 }
