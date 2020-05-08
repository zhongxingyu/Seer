 /*
  * Chapter 1
  * Paragraph 1
  *
  * Min algorithm implementation
  */
 
 
 public class Min {
    public static <T extends Comparable<? super T>> T min(T A[]) {
         if (A.length == 0)
             return null;
 
         T min_value = A[0];
         for (int i=1; i<A.length; i++) {
             T elem = A[i];
             if (elem.compareTo(min_value) < 0)
                 min_value = elem;
         }
 
         return min_value;
     }
 
     public static void main(String[] args) {
         Integer A[] = {5, 12, 213, 75, 34, 7, 3, 92, 43, 1, -132, 232, 4572};
         System.out.println(min(A));
     }
 }
