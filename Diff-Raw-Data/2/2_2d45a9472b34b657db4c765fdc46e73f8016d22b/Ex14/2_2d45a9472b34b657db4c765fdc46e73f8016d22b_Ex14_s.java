 /**
  * 
  * @author (Raskanskyz)
  * @version (1.0)
  */
 
 public class Ex14 {
 
 	/**
 	 * 
 	 * The 'count' methods computes how many times a value appears in a sorted
 	 * array.
 	 * 
 	 * Time Complexity: O(log2N). 
 	 * Space complexity: O(1).
 	 * 
 	 * @param a
 	 *            The sorted array.
 	 * @param x
 	 *            The value we are counting.
 	 * @return The number of appearances of 'x' in the array 'a'.
 	 */
 	public static int count(int[] a, int x) {
 		int startFlag;
 		int endFlag;
 		int lowerBound = 0;
 		int upperBound = a.length - 1;
 		int middle;
 
 		while (lowerBound <= upperBound) {
 
 			middle = ((lowerBound + upperBound) / 2);
 			if (x > a[middle]) {
 				lowerBound = middle + 1;
 			}// if
 			else {
 				upperBound = middle - 1;
 			}// else
 
 		}// while
 		startFlag = lowerBound;// <-----found Lower Bound
 		upperBound = a.length - 1;
 
 		while (lowerBound <= upperBound) {
 
 			middle = ((lowerBound + upperBound) / 2);
 			if (x == a[middle]) {
 				lowerBound = middle + 1;
 			}// if
 			else {
 				upperBound = middle - 1;
 			}// else
 
 		}
 		endFlag = lowerBound;
 
 		return endFlag - startFlag;
 	}// count method
 
 	/**
 	 * The 'f' method compares elements in two arrays(a, b), finds what elements exist in
 	 * 'a' and don't exist in 'b', those elements are then inserted into the
 	 * array 'c' and eventually 'f' returns the highest value in 'c' (in other
 	 * words, 'f' returns the highest element that is in 'a' and not in 'b').
 	 * 
 	 * Original Algorithm:
 	 * 		Time Complexity: O(n^2). 
 	 * 		Space Complexity: O(1).
 	 * 
 	 * Improved Algorithm:
 	 * 		Time Complexity: O(n log(n)).
 	 * 		Space Complexity: O(1).
 	 * @param a
 	 *            The array we want to extract its unique elements from.
 	 * @param b
 	 *            The array we compare its elements with the elements from 'a'
 	 * @param c
 	 *            The array we store the unique elements in.
 	 * @return The highest unique element.
 	 */
 	public static int f(int[] a, int[] b, int[] c) {
 		int t = 0;
 		int k = 0;
 
 		quickSort(b);
 
 		for (int i = 0; i < a.length; i++) {
 			if (binarySearch(b, a[i]) == -1) {
 				c[t] = a[i];
 				if (c[t] > k) {
 					k = c[t];
 
 				}// inner if
 				t++;
 			}// outer if
 		}// for
 		return k;
 	}// static int f
 
 	/**
 	 * The 'generalGCD' method uses 'oddGCD' recursively
 	 * in order to compute the Greatest Common Divisor.
 	 * 
 	 * @param m The first value.
 	 * 
 	 * @param n The second value.
 	 * 
 	 * @return The Greatest Common Divisor.
 	 */
 	public static int generalGCD(int m, int n) {
 
 		if (m % 2 != 0 || n % 2 != 0) {
 			return oddGCD(m, n);
 
 		} else {
 			return 2 * generalGCD(m / 2, n / 2);
 		}
 
 	}// generalGCD
 
 	/**
 	* The 'isSumOf' method calls an overload 'isSumOf' method which in turn returns 'true'
 	 * if 'n' is a sum of elements in the array 's'.
 	 * 
 	 * @param s A given Array.
 	 * @param n The value to check if is summable by elements in 's'.
 	 * @return 'true' if 'n' is a sum of 's', otherwise returns 'false'.
 	 */
 	public static boolean isSumOf(int[] s, int n) {
 		int counter = 0;
 		int sum = 0;
 		return isSumOf(s, n, counter, sum);
 	}// isSumOf
 
 	
 	
 	
 	
 	
 	// my private methods
 
 	/**
 	 * The 'isSumOf' method returns 'true' if 'n' is a sum of elements in 'array'.
 	 * 
 	 * @param array A given Array.
 	 * @param n The value to check if is summable by elements in 'array'.
 	 * @param counter Points on a cell in 'array'.
 	 * @param sum The current sum in a particular recursive call.
 	 * @return 'true' if 'n' is a sum of 'array', otherwise returns 'false'.
 	 * 
 	 */
 	private static boolean isSumOf(int[] array, int n, int counter, int sum) {
 		if(sum==n || n==0){
 			return true;
 		}
 		
 		if (counter > array.length - 1 || sum > n) {
 			return false;
 		}
 		if (counter == array.length - 1) {
			return isSumOf(array, n, counter, sum + array[counter]);// <----explain
 	
 		} else {
 			return ((isSumOf(array, n, counter, sum + array[counter])) || 
 					(isSumOf(array, n, counter + 1, sum + array[counter + 1])));
 		}// else
 		
 	}// isSumOf
 
 	
 	/**
 	 * The 'binarySearch' checks if 'num' is in the array 'data' ('data' must be sorted!).
 	 * @param data The sorted array to search in.
 	 * @param num The value to search.
 	 * @return 'num' if it is in the array, otherwise returns -1.
 	 */
 	private static int binarySearch(int[] data, int num) {
 
 		int middle, lower = 0, upper = (data.length - 1);
 		do {
 			middle = ((lower + upper) / 2);
 			if (num < data[middle])
 				upper = middle - 1;
 			else
 				lower = middle + 1;
 		} while ((data[middle] != num) && (lower <= upper));
 		if (data[middle] == num)
 			return num;
 		else
 			return -1;
 
 	}// binarySearch
 
 	
 	/**
 	 * The 'quickSort' calls an overload 'quickSort' method which in turn sorts the given array.
 	 */
 	private static void quickSort(int array[]) {
 		quickSort(array, 0, array.length - 1);
 	}
 
 	
 	/**
 	 * The 'quickSort' method sorts an array from highest value to lowest value.
 	 * @param array The array to sort.
 	 * @param start The relative start position in the recursion.
 	 * @param end The relative end position in the recursion.
 	 */
 	private static void quickSort(int array[], int start, int end) {
 		int median;
 		if (end > start + 1) {
 			median = partition(array, start, end);
 			quickSort(array, start, median - 1);
 			quickSort(array, median + 1, end);
 		}// base case
 		else {
 			if (end == start + 1 && array[start] > array[end]) {
 				swap(array, start, end);
 			}
 		}
 	}// quickSort
 
 	
 	/**
 	 * The 'partition' method recursively partitions an array into sub-arrays. 
 	 * @param array The array to partition
 	 * @param start The relative starting point.
 	 * @param end The relative end point.
 	 * @return The median point.
 	 */
 	private static int partition(int[] array, int start, int end) {
 		swap(array, start,
 				medianLocation(array, start + 1, end, (start + end) / 2));
 		int median = partition(array, start + 1, end, array[start]);
 		swap(array, start, median);
 		return median;
 	}// partition
 
 	
 	/**
 	 * The 'partition' method recursively partitions an array into sub-arrays.
 	 * @param array The array to partition
 	 * @param start The relative starting point.
 	 * @param end The relative end point.
 	 * @param pivot The relative pivot to compare elements to.
 	 * @return The partition point.
 	 */
 	private static int partition(int[] array, int start, int end, int pivot) {
 		if (start == end) {
 			if (array[start] < pivot) {
 				return start;
 			} else {
 				return start - 1;
 			}
 		} else if (array[start] <= pivot) {
 			return partition(array, start + 1, end, pivot);
 		} else {
 			swap(array, start, end);
 			return partition(array, start, end - 1, pivot);
 		}
 	}
 
 	
 	/**
 	 * The 'swap' methods switches the position of two elements in the array.
 	 * @param array The given array.
 	 * @param index1 The first element to swap.
 	 * @param index2 The second element to swap with 'index1'.
 	 */
 	private static void swap(int array[], int index1, int index2) {
 		int temp = array[index1]; // store the first value in a temp
 		array[index1] = array[index2]; // copy the value of the second into the
 		// first
 		array[index2] = temp; // copy the value of the temp into the second
 	}
 
 	
 	/**
 	 * The 'medianLocation' finds the relative median location out of three given index locations.
 	 * @param array The given array.
 	 * @param i The first index.
 	 * @param j The second index.
 	 * @param k The third index.
 	 * @return The relative median location.
 	 */
 	private static int medianLocation(int[] array, int i, int j, int k) {
 		if (array[i] <= array[j]) {
 			if (array[j] <= array[k]) {
 				return j;
 			} else if (array[i] <= array[k]) {
 				return k;
 			} else {
 				return i;
 			}
 		} else {
 			if (array[i] <= array[k]) {
 				return i;
 			} else if (array[j] <= array[k]) {
 				return k;
 			} else {
 				return j;
 			}
 		}
 	}// medianLocation
 
 
 	/**
 	 * The 'oddGCD' method calculates the Greatest Common Divisor between two odd numbers.
 	 * @param m The first number.
 	 * @param n The second number.
 	 * @return The Greatest Common Divisor.
 	 */
 	private static int oddGCD(int m, int n) {
 		if (n == m)
 			return n;
 		if (m > n)
 			return oddGCD(n, m - n);
 		return oddGCD(m, n - m);
 	}
 
 }// class
