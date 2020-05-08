 package com.wesleyreisz.samples.mergesort;
 
 public class MergeSortConsole {
 
 	public static void main(String[] args) {
		int[] values = {2,5,7,7,34,8,3,6,8,10,11};
 		for(int value: MergeSort.sort(values)){
 			System.out.print(value + " ");
 		}	
 	}
 }
