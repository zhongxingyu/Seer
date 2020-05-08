 package pe.edu.pucp.algorithms.sorting.algs.impl;
 
 import pe.edu.pucp.algorithms.sorting.algs.BaseSorter;
 
 /**
  * Implementation of Quick Sort algorithm.
  * 
  * @author Carlos Gavidia (c.gavidia@hotmail.com)
  * 
  * @param <T>
  *            Type of the array to be sorted
  */
 public class QuickSorter<T extends Comparable<T>> extends BaseSorter<T> {
 
 	public QuickSorter(T[] data) {
 		super(data);
 	}
 
 	@Override
 	protected void sortData() {
 		recursiveSort(0, getMaxIndex());
 
 	}
 
 	private void recursiveSort(int lowerIndex, int higherIndex) {
 		if (higherIndex < lowerIndex) {
 			return;
 		}
 		int pivotPosition = partition(lowerIndex, higherIndex);
 		recursiveSort(lowerIndex, pivotPosition - 1);
 		recursiveSort(pivotPosition + 1, higherIndex);
 	}
 
 	private int partition(int lowerIndex, int higherIndex) {
 		int leftScanner = lowerIndex;
 		int rightScanner = higherIndex + 1;
 		T pivotItem = getData(lowerIndex);
 		while (true) {
			while (leftScanner < (getMaxIndex() - 1)
					&& getData(++leftScanner).compareTo(pivotItem) < 0) {
 				if (leftScanner == higherIndex) {
 					break;
 				}
 			}
			while (rightScanner > 0
					&& pivotItem.compareTo(getData(--rightScanner)) < 0) {
 				if (rightScanner == lowerIndex) {
 					break;
 				}
 			}
 
 			if (leftScanner >= rightScanner) {
 				break;
 			}
 			exchange(leftScanner, rightScanner);
 		}
 		exchange(lowerIndex, rightScanner);
 		return rightScanner;
 	}
 
 }
