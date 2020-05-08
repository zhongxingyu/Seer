 package rio.mergesort;
 
 public class MergeSorter {
 
     private long[] array;
     
     public MergeSorter(long[] array) {
         this.array = array;
     }
     
     private void mergeSort(long[] array, int left, int right) {
         
         if (left < right) {
             int center = (left + right) / 2;
             this.mergeSort(array, left, center);
             this.mergeSort(array, center + 1, right);
             this.merge(array, left, center, right);
         }
     }
     
     private void merge(long[] array, int left, int center, int right) {
         
         int leftArraySize = center - left + 1;
         int rightArraySize = right - center;
         
         long[] leftArray = new long[leftArraySize + 1];
         long[] rightArray = new long[rightArraySize + 1];
         
         // Left array
         for (int i = 0; i < leftArraySize; i++) {
             leftArray[i] = array[left + i];
         }
         
         leftArray[leftArraySize] = Long.MAX_VALUE;
         
         // Right array
         for (int i = 0; i < rightArraySize; i++) {
             rightArray[i] = array[center + i + 1];
         }
         
         rightArray[rightArraySize] = Long.MAX_VALUE;
         
         int i = 0;
         int j = 0;
         
         for (int k = left; k <= right; k++) {
             
             if (leftArray[i] <= rightArray[j]) {
                 array[k] = leftArray[i];
                 i++;
             } else {
                 array[k] = rightArray[j];
                 j++;
             }
         }
     }
     
     public void sort() {
         this.mergeSort(array, 0, array.length - 1);
     }
     
     @Override
     public String toString() {
         StringBuilder stringBuilder = new StringBuilder();
         
         stringBuilder.append("[");
         
         for (int i = 0; i < array.length; i++) {
             stringBuilder.append(array[i]);
             
             if (i != array.length - 1) {
                 stringBuilder.append(", ");
             }
         }
         
         stringBuilder.append("]");
         
         return stringBuilder.toString();
     }
 }
