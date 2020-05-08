 abstract class SortAlg
 {
     protected static boolean less(Comparable v, Comparable w)
     {
         return v.compareTo(w) < 0;
     }
 
     protected static void exch(Comparable[] a, int i, int j)
     {
         Comparable tmp = a[i];
         a[i] = a[j];
         a[j] = tmp;
     }
 }
 
 class Selection extends SortAlg
 {
     public static void sort(Comparable[] a)
     {
         int N = a.length;
         for (int i = 0; i < N; i++)
         {
             int min = i;
             for (int j = i + 1; j < N; j++)
                 if (less(a[j], a[min]))
                     min = j;
             exch(a, i, min);
         }
     }
 }
 
 class Insertion extends SortAlg
 {
     public static void sort(Comparable[] a)
     {
         int N = a.length;
         for (int i = 0; i < N; i++)
             for (int j = i; j > 0; j--)
                 if (less(a[j], a[j - 1]))
                     exch(a, j, j - 1);
                 else break;
     }
 }
 
 class Shell extends SortAlg
 {
     public static void sort(Comparable[] a)
     {
         int N = a.length;
 
         int h = 1;
         while (h < N/3) h = 3*h + 1;
 
         while (h >= 1)
         {
             //h-sort the array
             for (int i = h; i < N; i++)
             {
                 for (int j = i; j >= h && less(a[j], a[j - h]); j -= h)
                     exch(a, j, j - h);
             }
            h /= 3;
         }
     }
 }
 
 class Experiment
 {
     public static void main(String[] args)
     {
        Integer[] a = new Integer[args.length];
        for (int i = 0; i < args.length; i++)
        {
            a[i] = Integer.parseInt(args[i]);
        }

        Shell.sort(a);
        for (int i = 0; i < a.length; i++)
             StdOut.println(a[i]);
     }
 }
