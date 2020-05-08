 /* Insertion Sort, Binary Insertion Sort
  * @Author Juan Jos Lpez Delgado
 */
import java.util.*;
 
 public class Sort {
   
   public Sort() {
   }
   
   public static void insertionSort(int[] arr) {
    if (arr == null || arr.length <= 1 ) { return; } 
     int buff, hole = 0;
     for (int i = 1; i < arr.length; i++) {
       buff = arr[i];
       hole = i;
       while (hole > 0 && buff < arr[hole - 1]) {
         arr[hole] = arr[hole - 1];
         hole--;
       }
       arr[hole] = buff;
     }  
   }
   
   public static void binaryInsertionSort(int[] arr) {
    if (arr == null || arr.length <= 1 ) { return; }
     int buff, left, right = 0;
     for (int i = 1; i < arr.length; i++){
       buff = arr[i];
       left = 0;
       right = i;
       while (left  < right) {
         if (buff >= arr[(left + right) / 2]) {
           left = ((left + right) / 2) + 1;
         } else { 
           right = (left + right) / 2;
         }
       }
       for (int j = i; j > left; j--) {
         arr[j] = arr[j-1];
       }
       arr[left] = buff;
     }
   }
  
  public static int[] mergeSort(int[] arr){
    if (arr == null || arr.length <= 1 )
      return arr;
    int i = (arr.length % 2 == 0) ? arr.length/2  : (arr.length + 1)/2;
    int[] left = mergeSort(Arrays.copyOfRange(arr,0,i));
    int[] right = mergeSort(Arrays.copyOfRange(arr,i,arr.length));
    return merge(left, right);
  }
  
  private static int[] merge(int[] left, int[] right){
    return right;
  }
 }
