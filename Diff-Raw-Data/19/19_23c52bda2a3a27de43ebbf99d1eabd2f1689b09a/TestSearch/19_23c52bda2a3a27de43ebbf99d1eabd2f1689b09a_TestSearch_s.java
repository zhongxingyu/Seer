 public class TestSearch {
     public static Double[] createRandomData(int length) {
         Double[] data = new Double[length];
 
         for(int i = 0; i < data.length; i++)
             data[i] = length * Math.random();
         return data;
     }
 
     public static void main(String[] args) {
         long start, stop;
         System.out.print("Creating Array: ");
         start = System.nanoTime();
         Double[] d = createRandomData(10000000);
         stop = System.nanoTime();
         System.out.println((stop - start)/1000000000.0);
 
         System.out.print("Sorting Array Concurrently: ");
         start = System.nanoTime();
         MergeSort.sort(d);
         stop = System.nanoTime();
         System.out.println((stop - start)/1000000000.0);
         
         System.out.print("Recreating Array: ");
         start = System.nanoTime();
         d = createRandomData(10000000);
         stop = System.nanoTime();
         System.out.println((stop - start)/1000000000.0);
 
         System.out.print("Sorting Array Single Thread: ");
         start = System.nanoTime();
         mergeSort(d);
         stop = System.nanoTime();
         System.out.println((stop - start)/1000000000.0);
     }
 
     public static void mergeSort(Comparable[] a) { mergeSort(a, 0, a.length - 1); }
 
     private static void mergeSort(Comparable[] data, int min, int max) {
         Comparable[] temp;
         int index, left, right;
 
         // return on list of length 1
         if(min == max)
             return;
 
         // Find the length and the midpoint of the list
         int size = max - min + 1;
         int pivot = (min + max) / 2;
         temp = (new Comparable[size]);  // Java doesn't handle generic arrays well.
 
 
         // Copy sorted data into workspace
         for(index = 0; index < size; index++)
             temp[index] = data[min + index];
 
         // Merge the two sorted lists
         left = 0;
         right = pivot - min + 1;
         for(index = 0; index < size; index++)
             if(right <= max - min)
                 if (left <= pivot - min)
                     if (temp[left].compareTo(temp[right]) > 0)
                         data[index + min] = temp[right++];
                     else 
                         data[index + min] = temp[left++];
                 else 
                     data[index + min] = temp[right++];
             else 
                 data[index + min] = temp[left++];
     }
 }
 
