package com.igordaoconsulting.training;
 
 import com.igordaoconsulting.training.sorting.SortingAlgorithmFactory;
 
 public class ArrayUtils {
 
 	private static final SortingAlgorithmFactory ALGORITHM_FACTORY = SortingAlgorithmFactory.getInstance();
 
 	/** In-Place; Worst-case Θ(nˆ2); Average-case Θ(nˆ2) */
 	public static void naiveSort(int[] unsorted) {
 		ALGORITHM_FACTORY.naiveSort(unsorted).run();
 	}
 
 	/** In-Place; Worst-case Θ(nˆ2); Average-case Θ(nˆ2) */
 	public static void insertionSort(int[] unsorted) {
 		ALGORITHM_FACTORY.insertionSort(unsorted).run();
 	}
 
 	/** In-Place; Worst-case Θ(nˆ2); Average-case Θ(nˆ2) */
 	public static void bubbleSort(int[] unsorted) {
 		ALGORITHM_FACTORY.bubbleSort(unsorted).run();
 	}
 
 	/** Worst-case Θ(n log n); Average-case Θ(n log n) */
 	public static void mergeSort(int[] unsorted) {
 		ALGORITHM_FACTORY.mergeSort(unsorted).run();
 	}
 
 	/** Worst-case Θ(n log n) */
 	public static void heapSort(int[] unsorted) {
		throw new RuntimeException("Not Implemetend Yet!");
 	}
 
 	/** Worst-case Θ(nˆ2); Expected-case Θ(n log n) */
 	public static void quickSort(int[] unsorted) {
 		ALGORITHM_FACTORY.quickSort(unsorted).run();
 	}
 
 	/** Worst-case Θ(k + n); Average-case Θ(k + n) */
 	public static void countingSort(int[] unsorted) {
 		throw new RuntimeException("Not Implemetend Yet!");
 	}
 
 	/** Worst-case Θ(d(n + k)); Average-case Θ(d(n + k) */
 	public static void radixSort(int[] unsorted) {
 		throw new RuntimeException("Not Implemetend Yet!");
 	}
 
 	/** Worst-case Θ(nˆ2); Average-case Θ(n) */
 	public static void bucketSort(int[] unsorted) {
 		throw new RuntimeException("Not Implemetend Yet!");
 	}
 
 }
