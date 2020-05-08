 package com.wesleyreisz.samples.mergesort;
 
 public class MergeSort {
 	/*
 	 * purpose: implement a merge sort. Merge sort works
 	 * but dividing the list in half and then running a comparison
 	 * from the left list and the right list. The minimum value is
 	 * copied into a new result list.
 	 */
 	
 	public static int[] sort(int[] values) {
		if(values.length<=1){return values;}
		
 		int[] result = new int[values.length];
 		
 		// step 1: divide list in half
 		int halfway = result.length/2;
 		int[] left = new int[halfway];
 		int[] right = new int[values.length-halfway];
 		for(int i = 0; i<values.length; i++){
 			if (i<=halfway-1){
 				left[i]=values[i];
 			}else{
 				right[i-halfway]=values[i];
 			}
 		}
		
		left = sort(left);
		right = sort(right);
		
		return merge(left,right);
	}

	private static int[] merge(int[] left, int[] right) {
		int[] result = new int[left.length + right.length];
		
 		int i = 0;
 		int j = 0;
		
 		for(int k = 0; k<result.length; k++){
 			if(i<left.length && j<right.length){
 				//core data check
 				if(left[i]<right[j]){
 					result[k]=left[i];
 					i++;
 				}else{
 					result[k]=right[j];
 					j++;
 				}
 			}else{
 				if(i<left.length){
 					result[k]=left[i];
 					i++;
 				}else{
 					result[k]=right[j];
 					j++;
 				}
 			}
 		}
 		// step 3: return list
 		return result;
 	}
 }
