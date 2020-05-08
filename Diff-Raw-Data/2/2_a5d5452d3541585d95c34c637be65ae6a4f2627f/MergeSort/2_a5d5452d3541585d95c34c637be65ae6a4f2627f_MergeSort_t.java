 /*
  * Chapter 2
  * Paragraph 3
  *
  * Merge sort algorithm implementation
  */
 
 package sorts;
 
 public class MergeSort {
   public static <T extends Comparable<? super T>> void sort(T A[]) {
     Comparable[] B = new Comparable[A.length];
     merge_sort(A,0,A.length-1, B);
   }
 
   public static <T extends Comparable<? super T>> void merge_sort(T A[],
                                                                   int first,
                                                                   int last,
                                                                   T B[]) {
     if (first<last)
     {
       int m = (first+last) / 2;
       merge_sort(A, first, m, B);
       merge_sort(A, m+1, last, B);
       merge(A,first,last,m, B);
     }
   }
 
   public static <T extends Comparable<? super T>> void merge(T A[],
                                                              int first,
                                                              int last,
                                                              int m,
                                                              T B[]) {
     int i, j, k;
     i = first;
     j = m+1;
     k = first;
     while (i<=m && j<=last)
     {
       if (A[i].compareTo(A[j]) < 0)
       {
         B[k] = A[i];
         i += 1;
       }
       else
       {
         B[k] = A[j];
         j +=1;
       }
       k +=1;
     }
     j = last;
     for (int h=m; h>=i; h--)
     {
       A[j] = A[h];
       j -=1;
     }
     for (j=first; j<k; j++)
       A[j] = B[j];
   }
 
  public static void main(String args[]) {
     Integer A[] = new Integer[100];
     for (int i=0; i<100; i++)
     {
       A[i] = 100 - i;
     }
     sort(A);
     for (int a : A)
       System.out.println(""+a);
   }
 }
