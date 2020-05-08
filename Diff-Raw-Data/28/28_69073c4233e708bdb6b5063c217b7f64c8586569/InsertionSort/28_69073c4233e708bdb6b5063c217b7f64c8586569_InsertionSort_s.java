 package com.grosser.sort;
 
 import com.grosser.array.IntArray;
 
 public class InsertionSort {
 	
 	/**
 	 * Static insertion sort method. Pass in an array of integers to be sorted,
 	 * and the method will sort the integers in O(N2) time, and then return
 	 * the sorted array, where N is the number of items in the array.
 	 * 
 	 * @param arrayToSort
 	 * @return The sorted array.
 	 */
 	public static IntArray sort(IntArray arrayToSort){
 		
 		// The insertion sort runs in 0(N2) time, but is about twice as fast as the bubble sort on average.
 		// The insertion sort is the most commonly used sort of the "simple" sorts, and is often used as 
 		// the final stage of a more advanced sort, such as the quicksort.
 		
 		// Starting at index 1 (not 0), move the value at that index (outer/inner) to the temp variable. Compare
 		// the value at that index (inner) with the value at marker-1. If the value in temp is smaller than
 		// the value at inner-1, move inner to the left and continue the cycle until the value at temp is no
 		// longer smaller than the value at marker-1, or else the marker has reached index 0. When the final
 		// resting place for the temp value is found, copy the value in temp to that location. Increment outer,
 		// then repeat the whole cycle.
 		
 		int temp = 0;
 		
 		// n is the size of the array
 		int n = arrayToSort.size();
 		
 		// If there are less than two values in the array, the array is already sorted!
 		if(n < 2){ return arrayToSort; }
 		
 		// Loop through remaining unsorted values left to right, starting at index 1
 		for(int outer = 1; outer < n; outer++){
 			
 			// Copy the value at outer to temp
 			temp = arrayToSort.get(outer);
 			
 			// Loop through sorted values, right to left, and find the smallest
 			// value to swap with "outer"
 			for(int inner = (outer - 1); inner >= 0; inner--){
 				
				if(inner == 0){
					// Shift the value at inner to the right
					arrayToSort.set(inner+1, arrayToSort.get(inner));
					
					// Copy the value in temp to the inner+1 index
					arrayToSort.set(inner, temp);
				}
				
 				if(arrayToSort.get(inner) > temp){
 
 					// Shift the value at inner to the right
 					arrayToSort.set(inner+1, arrayToSort.get(inner));
 				} else {
 					
 					// Copy the value in temp to the inner+1 index
 					arrayToSort.set(inner + 1, temp);
 					
 					// Quit looping through the inner loop
 					break;
 				}
 			}
 		}
 		
 		return arrayToSort;
 	}
 }
