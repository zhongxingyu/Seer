 package com.dj.example;
 
 public class Sorting {
 
 	public static int[] selectionSort(int[] data) {
 		int lenD = data.length;
 		int j = 0;
 		int tmp = 0;
 		for (int i = 0; i < lenD; i++) {
 			j = i;
 			for (int k = i; k < lenD; k++) {
 				if (data[j] > data[k]) {
 					j = k;
 				}
 			}
 			tmp = data[i];
 			data[i] = data[j];
 			data[j] = tmp;
 		}
 		return data;
 	}
 
 	public int[] InsertionSort(int[] data) {
 		int len = data.length;
 		int key = 0;
 		int i = 0;
 		for (int j = 1; j < len; j++) {
 			key = data[j];
 			i = j - 1;
 			while (i >= 0 && data[i] > key) {
 				data[i + 1] = data[i];
 				i = i - 1;
 				data[i + 1] = key;
 			}
 		}
 		return data;
 	}
 
 	public int[] bubbleSort(int[] data) {
 		int lenD = data.length;
 		int tmp = 0;
 		for (int i = 0; i < lenD; i++) {
 			for (int j = (lenD - 1); j >= (i + 1); j--) {
 				if (data[j] < data[j - 1]) {
 					tmp = data[j];
 					data[j] = data[j - 1];
 					data[j - 1] = tmp;
 				}
 			}
 		}
 		return data;
 	}
 
 	// Quick Sort
 
 	private int[] numbers;
 	private int number;
 
 	public int[] qSort(int[] values) {
 		// check for empty or null array
 		if (values == null || values.length == 0) {
 			return null;
 		}
 		this.numbers = values;
 		number = values.length;
 		quicksort(0, number - 1);
 		return numbers;
 	}
 
 	private void quicksort(int low, int high) {
 		int i = low, j = high;
 		// Get the pivot element from the middle of the list
 		int pivot = numbers[low + (high - low) / 2];
 		System.out.println("Pivot: " + pivot);
 
 		// Divide into two lists
 		while (i <= j) {
 			// If the current value from the left list is smaller then the pivot
 			// element then get the next element from the left list
 			while (numbers[i] < pivot) {
 				i++;
 			}
 			// If the current value from the right list is larger then the pivot
 			// element then get the next element from the right list
 			while (numbers[j] > pivot) {
 				j--;
 			}
 
 			// If we have found a values in the left list which is larger then
 			// the pivot element and if we have found a value in the right list
 			// which is smaller then the pivot element then we exchange the
 			// values.
 			// As we are done we can increase i and j
 			if (i <= j) {
 				exchange(i, j);
 				i++;
 				j--;
 			}
 		}
 
 		System.out.println("***********************");
 		for (int z : numbers)
 			System.out.print(z +  " ");
 		System.out.println("***********************");
 		System.out.println("");
 
 		// Recursion
 		if (low < j)
 			quicksort(low, j);
 		if (i < high)
 			quicksort(i, high);
 	}
 
 	private void exchange(int i, int j) {
 		int temp = numbers[i];
 		numbers[i] = numbers[j];
 		numbers[j] = temp;
 	}
 
 	public int[] mergeSort(int[] data) {
 		int lenD = data.length;
 		if (lenD <= 1) {
 			return data;
 		} else {
 			int[] sorted = new int[lenD];
 			int middle = lenD / 2;
 			int rem = lenD - middle;
 			int[] L = new int[middle];
 			int[] R = new int[rem];
 			System.arraycopy(data, 0, L, 0, middle);
 			System.arraycopy(data, middle, R, 0, rem);
 			L = this.mergeSort(L);
 			R = this.mergeSort(R);
 			sorted = merge(L, R);
 			return sorted;
 		}
 	}
 
 	public int[] merge(int[] L, int[] R) {
 		int lenL = L.length;
 		int lenR = R.length;
 		int[] merged = new int[lenL + lenR];
 		int i = 0;
 		int j = 0;
 		while (i < lenL || j < lenR) {
 			if (i < lenL & j < lenR) {
 				if (L[i] <= R[j]) {
 					merged[i + j] = L[i];
 					i++;
 				} else {
 					merged[i + j] = R[j];
 					j++;
 				}
 			} else if (i < lenL) {
 				merged[i + j] = L[i];
 				i++;
 			} else if (j < lenR) {
 				merged[i + j] = R[j];
 				j++;
 			}
 		}
 		return merged;
 	}
 
 }
>>>>>>> 39fe24117354c49f43f0d42f25ff74dd930565d7
