 /******************************************************************************
  * Copyright (C) 2013 David Rusk
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal in the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * IN THE SOFTWARE.
  *****************************************************************************/
 package rusk.david.algorithms.utils;
 
 public class ArrayUtils {
 
 	/**
 	 * Create a copy of a slice of an array.
 	 * 
 	 * @param array
 	 *            The array to be sliced.
 	 * @param startIndex
 	 *            The index of the first element to be sliced.
 	 * @param stopIndex
 	 *            The slice will stop when it reaches this index (not
 	 *            inclusive).
 	 * @return A new array containing containing the elements of the input array
 	 *         starting at startIndex and going up to but not including
 	 *         stopIndex.
 	 */
 	public static int[] copySlice(int[] array, int startIndex, int stopIndex) {
 		assert stopIndex > startIndex;
 
 		int[] copiedArray = new int[stopIndex - startIndex];
 		for (int i = 0; startIndex + i < stopIndex; i++) {
 			copiedArray[i] = array[startIndex + i];
 		}
 
 		return copiedArray;
 	}
 
 	/**
 	 * Copies the elements of one array into another. The arrays are expected to
 	 * have the same length.
 	 * 
 	 * @param targetArray
 	 *            The array which elements are being written into.
 	 * @param sourceArray
 	 *            The array which elements are read from.
 	 */
 	public static void copyInto(int[] targetArray, int[] sourceArray) {
 		assert targetArray.length == sourceArray.length;
 
 		for (int i = 0; i < sourceArray.length; i++) {
 			targetArray[i] = sourceArray[i];
 		}
 	}
 
 	/**
 	 * Copies the elements of the array into a new array in reversed order.
 	 * 
 	 * @param array
 	 *            The array to reverse.
 	 * @return A new, reversed array.
 	 */
 	public static int[] reverse(int[] array) {
 		int[] reversed = new int[array.length];
 		for (int i = 0; i < array.length; i++) {
			reversed[i] = array[array.length - 1 - i];
 		}
 		return reversed;
 	}
 
 }
