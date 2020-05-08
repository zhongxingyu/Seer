 package introductiontoalgo.gettingstart.designingalgorithms;
 
 import introductiontoalgo.common.BinarySearchOutputData;
 
 /**
  * This case is the answer of exercise 2.3.7.
  * input: an integer array S, and an integer x.
  * output found if there are two element in S whose sum is exactly x.
  * 
  * Solution: using merge sort to sort the array to a sorted array
  * then using for loop + binary search to found the sum of two integer
  * 
  * as big-theta of merge sort is nlogn, and big-theta of for + binary search also nlogn, so in total, the whole logic big-theta is nlogn
  * 
  * @author Tang Hao
  *
  */
 public class Exercises_2_3_7_nlogn_sumOfTwoElement {
 	public static void main(String [] args){
 		int [] array = {1,61,23,56,26,21,51,74,123,42,61,653,123,32,53};
 		int x = 85;
 		BinarySearchOutputData data = new BinarySearchOutputData();
 		
 		//using nlogn big-theta mergeSort algorithm to do merge sort first
 		OriginalModelOfMergeSort.mergeSort(array, 0, array.length - 1);
 		//once done, start searching algorithm
 		findSumOfTwoInteger(array, x, data);
 		
 	}
 	
 	public static void findSumOfTwoInteger(int [] array, int input, BinarySearchOutputData data){
 		for(int outIndex = 0; outIndex < (array.length - 1) ; outIndex++){
 			Exercises_2_3_5_BinarySearch.binarySearch(array, outIndex + 1, array.length, (input - array[outIndex]), data);
 			if(data.isAssinged()){
 				System.out.println("Successfully found two integers in array could make their sum is " + input);
 				System.out.println("First integer is " + array[outIndex]);
 				System.out.println("Second integer is " + array[data.getValue()]);
 				return;
 			}
 		}
 		
		System.out.println("No any two of integers in array that thier sum are " + input);
 	}
 	
 	 
 	
 }
