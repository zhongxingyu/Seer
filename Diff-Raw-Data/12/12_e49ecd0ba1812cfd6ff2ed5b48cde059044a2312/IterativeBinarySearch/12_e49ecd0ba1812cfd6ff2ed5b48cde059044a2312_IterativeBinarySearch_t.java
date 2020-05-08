 /*
  * Chapter 1
  * Paragraph 1
  *
  * IterativeBinarySearch implementation
  */
 
 
 public class IterativeBinarySearch {
    public static <T extends Comparable<? super T>> int search(T A[], T v) {
         int i = 0;
         int j = A.length - 1;
         int m = (i + j) / 2;  // integer division
 
         while (i < j && A[m] != v) {
             if (A[m].compareTo(v) < 0)
                 i = m + 1;
             else
                 j = m - 1;
             m = (i + j) / 2;  // integer division
         }
 
         if (i > j || A[m].compareTo(v) != 0)
            return -1;
         return m;
     }
 
     public static void main(String[] args) {
         Integer A[] = {-20, 2, 4, 9, 34, 42, 67, 234};
         System.out.println(search(A, 4));
         System.out.println(search(A, -20));
         System.out.println(search(A, 234));
     }
 }
