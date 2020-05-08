 
 
 import java.awt.Color;
 
 import AnimatedDataStructure.AnimatedDataStructure;
 import AnimatedLinkedList.AnimatedLinkedList;
 import AnimatedArray.AnimatedArray;
 
 import java.util.Random;
 
 public class Main {
     
     private static AnimatedLinkedList anim;
     public static int time = 200;
     
 	public static void main(String[] args) {
 		int[] arr = {4,3,8,1,2,12};
 		anim = new AnimatedLinkedList(arr);
 		/*setup thread for creating GUI */
 		
 		//quicksort(arr, 0, arr.length-1, time);
		bubblesort(arr);
 		anim.addToHead(20);
 		//anim.swap(0, 1, "");
		anim.findAndRemove("20", anim); //remove nodes with label = 20
         anim.endAnimation();
 	}
 	// taken from http://en.wikipedia.org/wiki/Quicksort , translated to Java and our API applied
 	
 	// left is the index of the leftmost element of the array
 	// right is the index of the rightmost element of the array (inclusive)
 	// number of elements in subarray = right-left+1
 	public static int partition(int[] array, int left, int right, int pivotIndex, int time) {
 	    int pivotValue = array[pivotIndex];
 	    // Move pivot to end
 	    int temp = array[pivotIndex];
 	    array[pivotIndex] = array[right];
 	    array[right] = temp;
 	    anim.swap(pivotIndex, right, "");
 	    int storeIndex = left;
 	    for(int i = left; i < right; i++) { // left <= i <= right
 	    	if (array[i] < pivotValue) {
 	    		temp = array[i];
 	    		array[i] = array[storeIndex];
 	    		array[storeIndex] = temp;
 	    		anim.swap(i, storeIndex, "");
 	    		storeIndex = storeIndex + 1;
 	    	}
 	    }
 	    // Move pivot to its final place
 	    temp = array[storeIndex];
 	    array[storeIndex] = array[right];
 	    array[right] = temp;
 	    anim.swap(storeIndex, right, "");
 	    return storeIndex;
 	}
 	
 	public static void quicksort(int[] array, int left, int right, int time) {
 		// If the list has 2 or more items
 		if (left < right) {
 			Random random = new Random();
 			int pivotIndex = random.nextInt(right-left) + left;
 			// Get lists of bigger and smaller items and final position of pivot
 			int pivotNewIndex = partition(array, left, right, pivotIndex, time);
 			// Recursively sort elements smaller than the pivot
 			quicksort(array, left, pivotNewIndex - 1, time);
 			// Recursively sort elements at least as big as the pivot
 			quicksort(array, pivotNewIndex+1, right, time);
 		}
 		
 	}
 	
 	public static void bubblesort(int[] arr) {
 		int i, j,t=0;
 		for(i = 0; i < arr.length; i++){
 			for(j = 1; j < (arr.length-i); j++){
 				anim.setColor(j, Color.RED, "");
 				anim.setColor(j-1, Color.RED, "");
 					if(arr[j-1] > arr[j]){
 					t = arr[j-1];
 					arr[j-1]=arr[j];
 					arr[j]=t;
 					anim.swap(j, j-1, "Swapping");
 				}
 				anim.setColor(j, Color.BLUE, "");
 				anim.setColor(j-1, Color.BLUE, "");
 			}
 			anim.setColor(j-1, Color.DARK_GRAY, "Gray out an element that's in it's place already.");
 		}
 	}
 	
 }
