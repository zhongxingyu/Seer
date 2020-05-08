 
 import java.util.Arrays;
 import java.util.Random;
 
 public class RadixSort {
   private static final Random rand = new Random();
   private static final boolean VERIFY = Boolean.getBoolean("verify");
   private static final int MAX_INT = Integer.getInteger("maxint", 100);

   private static void gen(int[] a) {
     for (int i = 0; i < a.length; i++) {
       a[i] = rand.nextInt(MAX_INT);
     }
   }
 
   private static void sort(int[] a, int binWidth) {
     final int base = 1 << binWidth;
     final int remainderMask = ~(~0 << binWidth);
     final int[] c = new int[base];
     final int[] b = new int[a.length];
 
     // find the max to cap the number of radix iterations
     // alternatively use type bit width to be sure.
     //
     int max = 0;
     for (int i = 0; i < a.length; i++) {
       if (a[i] > max) {
         max = a[i];
       }
     }
     final int hb = Integer.SIZE - Integer.numberOfLeadingZeros(max);
 
    for (int e = 0; e < hb; e += binWidth) {
       // zero count array from previous iter
      Arrays.fill(c, 0);

      // count sort based on current radix
       //
       for (int i = 0; i < a.length; i++) {
         c[(a[i] >> e) & remainderMask]++;
       }
       for (int i = 1; i < c.length; i++) {
         c[i] += c[i - 1];
       }
       // stable order: fill backwards from a to b
       for (int i = a.length - 1; i >= 0; i--) {
         b[--c[(a[i] >> e) & remainderMask]] = a[i];
       }
       System.arraycopy(b, 0, a, 0, a.length);
     }
   }

   private static void print(String header, int[] a) {
     System.out.println("######## " + header);
     System.out.println(Arrays.toString(a));
     System.out.println();
   }
 
   public static void main(String[] args) {
     final int[] arr = new int[1000];
     final int[] arrPreSort = new int[1000];
     int[] arrRef = null;
     gen(arrPreSort);
     if (VERIFY) {
       arrRef = new int[arr.length];
       System.arraycopy(arrPreSort, 0, arrRef, 0, arr.length);
       Arrays.sort(arrRef);
     }
     for (int i = 0; i < 10; i++) {
       for (int binWidth : new int[] {1, 2, 4, 8, 16}) {
         System.arraycopy(arrPreSort, 0, arr, 0, arr.length);
         final long start = System.nanoTime();
         sort(arr, binWidth);
         final long dur = System.nanoTime() - start;
         System.out.println("radixBinWidth=" + binWidth + " nanos=" + dur);
 
         if (VERIFY) {
           if (!Arrays.equals(arrRef, arr)) {
             print("Generated", arrPreSort);
            print("Reference Arrays.sort", arrRef);
            print("Radix.sort", arr);
             throw new RuntimeException("Radix broken");
           }
         }
       }
       System.out.println();
     }
   }
 }
