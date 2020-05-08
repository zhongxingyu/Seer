 package com.carmatech.algo.sorting;
 
 import java.util.Random;
 
 public final class SortingUtilities {
 	private static final Random random = new Random();
 
 	private SortingUtilities() {
 		// Pure utility class, do NOT instantiate.
 	}
 
 	public static <T extends Comparable<T>> boolean less(final T a, final T b) {
 		return a.compareTo(b) < 0;
 	}
 
 	public static <T extends Comparable<T>> void swap(final T[] array, final int i, final int j) {
		if (i == j)
			return;

 		final T temp = array[i];
 		array[i] = array[j];
 		array[j] = temp;
 	}
 
 	public static <T extends Comparable<T>> boolean isSorted(final T[] array) {
 		for (int i = 1; i < array.length; i++)
 			if (less(array[i], array[i - 1]))
 				return false;
 		return true;
 	}
 
 	public static <T extends Comparable<T>> void shuffle(final T[] array) {
 		final int length = array.length;
 		for (int i = 0; i < length; i++) {
 			final int r = random.nextInt(i + 1);
 			swap(array, i, r);
 		}
 	}
 }
