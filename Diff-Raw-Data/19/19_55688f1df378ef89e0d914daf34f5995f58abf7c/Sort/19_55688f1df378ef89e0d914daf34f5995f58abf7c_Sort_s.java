 /* Insertion Sort, Binary Insertion Sort
  * @Author Juan Jos Lpez Delgado
 */
 
 public class Sort {
   
   public Sort() {
   }
   
   public static void insertionSort(int[] arr) {
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
 }
