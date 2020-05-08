 package org.sonar.duplications.index;
 
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 
public class DataUtils {
 
   public interface Sortable {
 
     int size();
 
     void swap(int i, int j);
 
     boolean isLess(int i, int j);
 
   }
 
   public static int[] byteToIntArray(byte[] byteArray) {
     int size = (byteArray.length / 4) + (byteArray.length % 4 == 0 ? 0 : 1); // Pad the size to multiple of 4
     ByteBuffer bb = ByteBuffer.allocate(size * 4);
     bb.put(byteArray);
     bb.rewind();
     IntBuffer ib = bb.asIntBuffer();
     int[] result = new int[size];
     ib.get(result);
     return result;
   }
 
   public static byte[] intToByteArray(int[] intArray) {
     ByteBuffer bb = ByteBuffer.allocate(intArray.length * 4);
     for (int i : intArray) {
       bb.putInt(i);
     }
     return bb.array();
   }
 
   public static int binarySearch(Sortable data, int element) {
     int lower = 0;
     int upper = data.size();
     while (lower < upper) {
       int mid = (lower + upper) >> 1;
       if (data.isLess(mid, element)) {
         lower = mid + 1;
       } else {
         upper = mid;
       }
     }
     return lower;
   }
 
   public static void sort(Sortable data) {
     quickSort(data, 0, data.size() - 1);
   }
 
   private static void bubbleSort(Sortable data, int left, int right) {
     for (int i = right; i > left; i--) {
       for (int j = left; j < i; j++) {
         if (data.isLess(j + 1, j)) {
           data.swap(j, j + 1);
         }
       }
     }
   }
 
   private static int partition(Sortable data, int low, int high) {
     int pivot = low;
     int i = low - 1;
     int j = high + 1;
     while (i < j) {
       i++;
       while (data.isLess(i, pivot)) {
         i++;
       }
       j--;
       while (data.isLess(pivot, j)) {
         j--;
       }
       if (i < j) {
         data.swap(i, j);
       }
     }
     return j;
   }
 
   private static void quickSort(Sortable data, int low, int high) {
     if (low >= high) {
       return;
     }
     if (high - low == 5) {
       bubbleSort(data, low, high);
       return;
     }
     int p = partition(data, low, high);
     quickSort(data, low, p);
     quickSort(data, p + 1, high);
   }
 
   private DataUtils() {
   }
 
 }
