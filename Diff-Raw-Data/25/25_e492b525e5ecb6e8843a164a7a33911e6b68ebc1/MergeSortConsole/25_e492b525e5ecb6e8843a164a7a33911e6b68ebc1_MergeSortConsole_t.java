 package com.wesleyreisz.samples.mergesort;
 
 public class MergeSortConsole {
 
 	public static void main(String[] args) {
		int[] values = {21,5,0,7,-5,7,34,10,11,-56};
 		for(int value: MergeSort.sort(values)){
 			System.out.print(value + " ");
 		}	
 	}
 }
