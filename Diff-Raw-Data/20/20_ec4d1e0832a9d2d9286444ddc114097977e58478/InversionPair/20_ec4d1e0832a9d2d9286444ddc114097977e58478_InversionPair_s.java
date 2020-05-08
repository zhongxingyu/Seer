 package com.algorithm.sort;
 
 /**
  * Find all of the inversion paris of an array with Integer I make use of merge
  * sort to implements the algorithm
  * 
  * @author Cyanny Liang
  * 
  */
 public class InversionPair {
 	private int[] dataSet = null;
 	private int count = 0;
 
 	public InversionPair(int[] dataSet) {
 		if (dataSet != null) {
 			this.dataSet = dataSet.clone();
 		}
 	}
 
 	public void setDataSet(int[] dataSet) {
 		this.dataSet = dataSet;
 	}
 
 	public int countInversions() {
 		count = 0;
 		mergeSort(0, dataSet.length - 1);
 		return count;
 	}
 
 	private void merge(int p, int q, int r) {
 		int n1 = q - p + 1;
 		int n2 = r - q;
 		int i, j, k;
 		int[] leftArr = new int[n1 + 1];
 		int[] rightArr = new int[n2 + 1];
 		// Create two arrays
 		for (i = 0; i < n1; i++) {
 			leftArr[i] = dataSet[p + i];
 		}
 		for (j = 0; j < n2; j++) {
 			rightArr[j] = dataSet[q + j + 1];
 		}
 		leftArr[n1] = Integer.MAX_VALUE;
 		rightArr[n2] = Integer.MAX_VALUE;
 
 		// Execute merge
 		boolean isCount = false;
 		for (k = p, i = 0, j = 0; k <= r; k++) {
 			if (leftArr[i] <= rightArr[j]) {
 				dataSet[k] = leftArr[i];
				// The problem is here:...
				// When the right array is empty, find if there is inversion pair
				if ((j == n2) && !isCount && (leftArr[i] > rightArr[j - 1])) {
					System.out.println("(" + leftArr[i] + ", " + rightArr[j - 1] + ")");
					count++;
				}
 				i++;
				isCount = false;
 			} else {
				count++;
 				System.out.println("(" + leftArr[i] + ", " + rightArr[j] + ")");
 				dataSet[k] = rightArr[j];
 				j++;
				isCount = true;
 			}
 		}
 	}
 
 	private void mergeSort(int p, int r) {
 		if (p < r) {
 			int q = (p + r) / 2;
 			mergeSort(p, q);
 			mergeSort(q + 1, r);
 			merge(p, q, r);
 		}
 	}
 	
 	public void printRes() {
 		System.out.println("Results:");
 		for (int val: dataSet) {
 			System.out.println(val);
 		}		
 	}
 
 	public static void main(String[] args) {
		int[] arr = {7, 6, 5, 4};
 		InversionPair myInversion = new InversionPair(arr);
 		System.out.println("Sum of inversion pairs is "
 				 + myInversion.countInversions());
 		myInversion.printRes();
 	}
 
 }
