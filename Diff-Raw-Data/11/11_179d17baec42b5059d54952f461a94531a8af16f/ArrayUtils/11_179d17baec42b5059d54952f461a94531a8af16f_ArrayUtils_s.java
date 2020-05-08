 package dna.util;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 
 import dna.util.parameters.Parameter;
 
 public class ArrayUtils {
 
 	public static Object[] append(Object[] array1, Object... array2) {
 		if (array1 == null || array1.length == 0) {
 			if (array2 == null) {
 				return new Object[] {};
 			} else {
 				return array2;
 			}
 		}
 		if (array2 == null || array2.length == 0) {
 			return array1;
 		}
 		Object[] appended = new Object[array1.length + array2.length];
 		System.arraycopy(array1, 0, appended, 0, array1.length);
 		System.arraycopy(array2, 0, appended, array1.length, array2.length);
 		return appended;
 	}
 
 	public static Parameter[] append(Parameter[] array1, Parameter... array2) {
 		if (array1 == null || array1.length == 0) {
 			if (array2 == null) {
 				return new Parameter[] {};
 			} else {
 				return array2;
 			}
 		}
 		if (array2 == null || array2.length == 0) {
 			return array1;
 		}
 		Parameter[] appended = new Parameter[array1.length + array2.length];
 		System.arraycopy(array1, 0, appended, 0, array1.length);
 		System.arraycopy(array2, 0, appended, array1.length, array2.length);
 		return appended;
 	}
 
 	public static int[] incr(int[] values, int index) {
 		try {
 			values[index]++;
 			return values;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			int[] valuesNew = new int[index + 1];
 			System.arraycopy(values, 0, valuesNew, 0, values.length);
 			valuesNew[index] = 1;
 			return valuesNew;
 		}
 	}
 
 	public static double[] incr(double[] values, int index) {
 		try {
 			values[index]++;
 			return values;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			double[] valuesNew = new double[index + 1];
 			System.arraycopy(values, 0, valuesNew, 0, values.length);
 			valuesNew[index] = 1;
 			return valuesNew;
 		}
 	}
 
 	public static long[] incr(long[] values, int index) {
 		try {
 			values[index]++;
 			return values;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			long[] valuesNew = new long[index + 1];
 			System.arraycopy(values, 0, valuesNew, 0, values.length);
 			valuesNew[index] = 1;
 			return valuesNew;
 		}
 	}
 
 	public static int[] decr(int[] values, int index) {
 		try {
 			values[index]--;
 			return values;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			int[] valuesNew = new int[index + 1];
 			System.arraycopy(values, 0, valuesNew, 0, values.length);
 			valuesNew[index] = -1;
 			return valuesNew;
 		}
 	}
 
 	public static double[] decr(double[] values, int index) {
 		try {
 			values[index]--;
 			return values;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			double[] valuesNew = new double[index + 1];
 			System.arraycopy(values, 0, valuesNew, 0, values.length);
 			valuesNew[index] = -1;
 			return valuesNew;
 		}
 	}
 
 	public static long[] decr(long[] values, int index) {
 		try {
 			values[index]--;
 			return values;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			long[] valuesNew = new long[index + 1];
 			System.arraycopy(values, 0, valuesNew, 0, values.length);
 			valuesNew[index] = -1;
 			return valuesNew;
 		}
 	}
 
 	public static double[] set(double[] values, int index, double value,
 			double defaultValue) {
 		try {
 			values[index] = value;
 			return values;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			double[] valuesNew = new double[index + 1];
 			System.arraycopy(values, 0, valuesNew, 0, values.length);
 			valuesNew[index] = value;
 			for (int i = index - 1; i >= values.length; i--) {
 				valuesNew[i] = defaultValue;
 			}
 			return valuesNew;
 		}
 	}
 
 	/**
 	 * Used for NodeNodeValueList values field operations. Sets all nodevalues
 	 * for one node, identified by the given index.
 	 * 
 	 * Note: The array or values field will be expanded if necessary.
 	 */
 	public static double[][] set(double[][] valuesField, int index,
 			double[] values, double defaultValue) {
 		try {
 			valuesField[index] = values;
 			if (valuesField[index].length < valuesField.length) {
 				valuesField[index] = ArrayUtils.set(valuesField[index],
 						valuesField.length - 1, defaultValue, defaultValue);
 			}
 			return valuesField;
 		} catch (IndexOutOfBoundsException e) {
 			double[][] valuesFieldNew = new double[index + 1][];
 
 			for (int i = 0; i < valuesField.length; i++) {
 				System.arraycopy(valuesField[i], 0, valuesFieldNew, 0,
 						valuesField[i].length);
 				valuesFieldNew[i] = ArrayUtils.set(valuesField[i], index,
 						defaultValue, defaultValue);
 			}
 			valuesFieldNew[index] = values;
 
 			for (int i = index - 1; i >= valuesField.length; i--) {
 				double[] valuesNew = new double[index + 1];
 				for (int j = 0; j < valuesNew.length; j++) {
 					valuesNew[j] = defaultValue;
 				}
 				valuesFieldNew[i] = valuesNew;
 			}
 
 			return valuesFieldNew;
 		}
 	}
 
 	public static long[] set(long[] values, int index, long value,
 			long defaultValue) {
 		try {
 			values[index] = value;
 			return values;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			long[] valuesNew = new long[index + 1];
 			System.arraycopy(values, 0, valuesNew, 0, values.length);
 			valuesNew[index] = value;
 			for (int i = index - 1; i >= values.length; i--) {
 				valuesNew[i] = defaultValue;
 			}
 			return valuesNew;
 		}
 	}
 
 	public static int[] set(int[] values, int index, int value, int defaultValue) {
 		try {
 			values[index] = value;
 			return values;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			int[] valuesNew = new int[index + 1];
 			System.arraycopy(values, 0, valuesNew, 0, values.length);
 			valuesNew[index] = value;
 			for (int i = index - 1; i >= values.length; i--) {
 				valuesNew[i] = defaultValue;
 			}
 			return valuesNew;
 		}
 	}
 
 	public static void divide(double[] values, double by) {
 		for (int i = 0; i < values.length; i++) {
 			values[i] /= by;
 		}
 	}
 
 	/**
 	 * truncates the given array by removing all fields at the end that contain
 	 * the given value, e.g., {0,1,0,2,3,0,0} => {0,1,0,2,3} for value=0
 	 * 
 	 * @param values
 	 *            array to truncate
 	 * @param value
 	 *            value to remove from the end of the array
 	 * @return truncated array
 	 */
 	public static int[] truncate(int[] values, int value) {
 		if (values[values.length - 1] != value) {
 			return values;
 		}
 		int index = values.length - 1;
 		for (int i = values.length - 1; i >= 0; i--) {
 			if (values[i] != value) {
 				break;
 			}
 			index--;
 		}
 		int[] valuesNew = new int[index + 1];
 		System.arraycopy(values, 0, valuesNew, 0, index + 1);
 		return valuesNew;
 	}
 
 	/**
 	 * truncates the given array by removing all fields at the end that contain
 	 * the given value, e.g., {0,1,0,2,3,0,0} => {0,1,0,2,3} for value=0
 	 * 
 	 * @param values
 	 *            array to truncate
 	 * @param value
 	 *            value to remove from the end of the array
 	 * @return truncated array
 	 */
 	public static double[] truncate(double[] values, double value) {
 		if (values[values.length - 1] != value) {
 			return values;
 		}
 		int index = values.length - 1;
 		for (int i = values.length - 1; i >= 0; i--) {
 			if (values[i] != value) {
 				break;
 			}
 			index--;
 		}
 		double[] valuesNew = new double[index + 1];
 		System.arraycopy(values, 0, valuesNew, 0, index + 1);
 		return valuesNew;
 	}
 
 	/**
 	 * truncates the given array by removing all fields at the end that contain
 	 * the given value, e.g., {0,1,0,2,3,0,0} => {0,1,0,2,3} for value=0
 	 * 
 	 * @param values
 	 *            array to truncate
 	 * @param value
 	 *            value to remove from the end of the array
 	 * @return truncated array
 	 */
 	public static long[] truncate(long[] values, long value) {
 		if (values[values.length - 1] != value) {
 			return values;
 		}
 		int index = values.length - 1;
 		for (int i = values.length - 1; i >= 0; i--) {
 			if (values[i] != value) {
 				break;
 			}
 			index--;
 		}
 		long[] valuesNew = new long[index + 1];
 		System.arraycopy(values, 0, valuesNew, 0, index + 1);
 		return valuesNew;
 	}
 
 	/**
 	 * truncates the given array by removing all fields at the end that contain
 	 * the given value, e.g., {0,1,0,2,3,0,0} => {0,1,0,2,3} for value=0
 	 * 
 	 * @param values
 	 *            array to truncate
 	 * @param value
 	 *            value to remove from the end of the array
 	 * @return truncated array
 	 */
 	public static double[] truncateNaN(double[] values) {
 		if (!Double.isNaN(values[values.length - 1])) {
 			return values;
 		}
 		int index = values.length - 1;
 		for (int i = values.length - 1; i >= 0; i--) {
 			if (!Double.isNaN(values[i])) {
 				break;
 			}
 			index--;
 		}
 		double[] valuesNew = new double[index + 1];
 		System.arraycopy(values, 0, valuesNew, 0, index + 1);
 		return valuesNew;
 	}
 
 	public static int sum(int[] values) {
 		int sum = 0;
 		for (int v : values) {
 			sum += v;
 		}
 		return sum;
 	}
 
 	public static long sum(long[] values) {
 		long sum = 0;
 		for (long v : values) {
 			sum += v;
 		}
 		return sum;
 	}
 
 	public static double sum(double[] values) {
 		double sum = 0;
 		for (double v : values) {
 			sum += v;
 		}
 		return sum;
 	}
 
 	public static String toString(int[] values) {
 		StringBuffer buff = new StringBuffer();
 		for (int v : values) {
 			buff.append(v + " ");
 		}
 		return buff.toString();
 	}
 
 	@SuppressWarnings("rawtypes")
 	public static String toString(Set set) {
 		StringBuffer buff = new StringBuffer();
 		for (Object obj : set) {
 			buff.append(obj.toString() + " ");
 		}
 		return buff.toString();
 	}
 
 	public static boolean equals(int[] v1, int[] v2) {
 		return ArrayUtils.equals(v1, v2, null);
 	}
 
 	/**
 	 * 
 	 * @param v1
 	 *            arrays of long values
 	 * @param v2
 	 *            array of long values to compare
 	 * @param name
 	 *            if a name is given, debug log output is printed in case the
 	 *            arrays are not equal
 	 * @return true if both arrays have the same length and all values are equal
 	 */
 	public static boolean equals(int[] v1, int[] v2, String name) {
 		if (v1.length != v2.length) {
 			if (name != null) {
 				Log.warn(name + " - length differs: " + v1.length + " != "
 						+ v2.length);
 			}
 			return false;
 		}
 		for (int i = 0; i < v1.length; i++) {
 			if (v1[i] != v2[i]) {
 				if (name != null) {
 					Log.warn(name + " - values @ index " + i + " differs: "
 							+ v1[i] + " != " + v2[i]);
 				}
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Calculates the average over an given array of doubles.
 	 * 
 	 * @param values
 	 *            double array the average is calculated from
 	 * @return average value of the given double array
 	 */
 	public static double avg(double[] values) {
 		double avg = 0;
 		for (double v : values) {
 			avg += v;
 
 		}
 		return avg / values.length;
 	}
 
 	/**
 	 * Calculates the average over an given array of doubles, considering
 	 * Double.NaN's.
 	 * 
 	 * @param values
 	 *            double array the average is calculated from
 	 * @return average value of the given double array
 	 */
 	public static double avgIncludingNaN(double[] values) {
 		int counter = 0;
 		double avg = 0;
 
 		for (double v : values) {
 			if (!Double.isNaN(v))
 				avg += v;
 			else {
 				Log.warn("Double.NaN detected");
 				counter++;
 			}
 		}
 		if ((values.length - counter) == 0)
 			return Double.NaN;
 		else
 			return avg / (values.length - counter);
 	}
 
 	public static double avgIgnoreNaN(double[] values) {
 		double avg = 0;
 		int counter = 0;
 		for (double v : values) {
 			if (!Double.isNaN(v)) {
 				avg += v;
 				counter++;
 			}
 		}
 		if (counter == 0) {
 			return 0;
 		}
 		return avg / (double) counter;
 	}
 
 	/**
 	 * Calculates the average over an given array of integers.
 	 * 
 	 * @param values
 	 *            integer array the average is calculated from
 	 * @return average value of the given integer array
 	 */
 	public static double avg(int[] values) {
 		double avg = 0;
 		for (int v : values) {
 			avg += v;
 
 		}
 		return avg / values.length;
 	}
 
 	/**
 	 * Calculates the average over an given array of longs.
 	 * 
 	 * @param values
 	 *            long array the average is calculated from
 	 * @return average value of the given long array
 	 */
 	public static double avg(long[] values) {
 		double avg = 0;
 		for (long v : values) {
 			avg += v;
 
 		}
 		return avg / values.length;
 	}
 
 	/**
 	 * Calculates the maximum over an given array of doubles.
 	 * 
 	 * @param values
 	 *            double array the maximum is calculated from
 	 * @return maximum value of the given double array
 	 */
 	public static double max(double[] values) {
 		try {
 			double max = values[0];
 			for (double v : values) {
 				if (v > max)
 					max = v;
 			}
 			return max;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return 0;
 		}
 	}
 
 	/**
 	 * Calculates the maximum over an given array of doubles, while considering
 	 * Double.NaN's.
 	 * 
 	 * @param values
 	 *            double array the maximum is calculated from
 	 * @return maximum value of the given double array
 	 */
 	public static double maxIncludingNaN(double[] values) {
 		try {
 			double max = values[0];
 			for (double v : values) {
 				if (!Double.isNaN(v)) {
 					if (v > max)
 						max = v;
 				} else {
 					Log.warn("Double.NaN detected");
 				}
 			}
 			return max;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return 0;
 		}
 	}
 
 	/**
 	 * Calculates the maximum over an given array of integers.
 	 * 
 	 * @param values
 	 *            integer array the maximum is calculated from
 	 * @return maximum value of the given integer array
 	 */
 	public static int max(int[] values) {
 		try {
 			int max = values[0];
 			for (int v : values) {
 				if (v > max)
 					max = v;
 			}
 			return max;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return 0;
 		}
 	}
 
 	/**
 	 * Calculates the maximum over an given array of longs.
 	 * 
 	 * @param values
 	 *            long array the maximum is calculated from
 	 * @return maximum value of the given long array
 	 */
 	public static long max(long[] values) {
 		try {
 			long max = values[0];
 			for (long v : values) {
 				if (v > max)
 					max = v;
 			}
 			return max;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return 0;
 		}
 	}
 
 	/**
 	 * Calculates the minimum over an given array of doubles.
 	 * 
 	 * @param values
 	 *            double array the minimum is calculated from
 	 * @return minimum value of the given double array
 	 */
 	public static double min(double[] values) {
 		double min = values[0];
 		for (double v : values) {
 			if (v < min)
 				min = v;
 		}
 		return min;
 	}
 
 	/**
 	 * Calculates the minimum over an given array of integers.
 	 * 
 	 * @param values
 	 *            integer array the minimum is calculated from
 	 * @return minimum value of the given integer array
 	 */
 	public static int min(int[] values) {
 		int min = values[0];
 		for (int v : values) {
 			if (v < min)
 				min = v;
 		}
 		return min;
 	}
 
 	/**
 	 * Calculates the minimum over an given array of longs.
 	 * 
 	 * @param values
 	 *            long array the minimum is calculated from
 	 * @return minimum value of the given long array
 	 */
 	public static long min(long[] values) {
 		long min = values[0];
 		for (long v : values) {
 			if (v < min)
 				min = v;
 		}
 		return min;
 	}
 
 	/**
 	 * Calculates the minimum over an given array of doubles, while considering
 	 * Double.NaN's.
 	 * 
 	 * @param values
 	 *            double array the minimum is calculated from
 	 * @return minimum value of the given double array
 	 */
 	public static double minIncludingNaN(double[] values) {
 		try {
 			double min = values[0];
 			for (double v : values) {
 				if (!Double.isNaN(v)) {
 					if (v < min)
 						min = v;
 				} else {
 					Log.warn("Double.NaN detected");
 				}
 			}
 			return min;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return 0;
 		}
 	}
 
 	/**
 	 * Calculates the median over an given array of doubles. Caution: Due to the
 	 * Arrays.sort call, the input array will be sorted.
 	 * 
 	 * @param values
 	 *            double array the median is calculated from
 	 * @return median of the given double array
 	 */
 	public static double med(double[] values) {
 		double[] temp = new double[values.length];
 		System.arraycopy(values, 0, temp, 0, values.length);
 
 		Arrays.sort(temp);
 		return temp[temp.length / 2];
 	}
 
 	/**
 	 * Calculates the median over an given array of doubles, while considering
 	 * Double.NaN's. Due to the Arrays.sort call, a copy of the input array is
 	 * used to calculate the median. Therefore use with caution: runtime O(n)
 	 * with n being the size of the input array.
 	 * 
 	 * @param values
 	 *            double array the median is calculated from
 	 * @return median of the given double array
 	 */
 	public static double medIncludingNaN(double[] values) {
 		double[] temp = new double[values.length];
 		System.arraycopy(values, 0, temp, 0, values.length);
 		int counter = 0;
 		for (double v : temp) {
 			if (Double.isNaN(v)) {
 				Log.warn("Double.NaN detected");
 				counter++;
 			}
 		}
 		Arrays.sort(temp);
 		return temp[(temp.length - counter) / 2];
 	}
 
 	/**
 	 * Calculates the median over an given array of integers. Caution: Due to
 	 * the Arrays.sort call, the input array will be sorted.
 	 * 
 	 * @param values
 	 *            integer array the median is calculated from
 	 * @return median of the given integer array
 	 */
 	public static int med(int[] values) {
 		int[] temp = new int[values.length];
 		System.arraycopy(values, 0, temp, 0, values.length);
 
 		Arrays.sort(temp);
 		return temp[temp.length / 2];
 	}
 
 	/**
 	 * Calculates the median over an given array of longs. Caution: Due to the
 	 * Arrays.sort call, the input array will be sorted.
 	 * 
 	 * @param values
 	 *            long array the median is calculated from
 	 * @return median of the given long array
 	 */
 	public static long med(long[] values) {
 		long[] temp = new long[values.length];
 		System.arraycopy(values, 0, temp, 0, values.length);
 
 		Arrays.sort(temp);
 		return temp[temp.length / 2];
 	}
 
 	/**
 	 * Calculates the variance of the given array.
 	 * 
 	 * @param values
 	 *            double array the variance is calculated from
 	 * @return variance of the given double array
 	 */
 	public static double var(double[] values) {
 		double mean = ArrayUtils.avg(values);
 		double x = 0;
 		for (double v : values) {
 			x += (v - mean) * (v - mean);
 		}
 		return x / (values.length - 1);
 	}
 
 	/**
 	 * Calculates the variance of the given array.
 	 * 
 	 * @param values
 	 *            long array the variance is calculated from
 	 * @return variance of the given double array
 	 */
 	public static double var(long[] values) {
 		double mean = ArrayUtils.avg(values);
 		double x = 0;
 		for (double v : values) {
 			x += (v - mean) * (v - mean);
 		}
 		return x / (values.length - 1);
 	}
 
 	/**
 	 * Calculates the variance of the given array.
 	 * 
 	 * @param values
 	 *            int array the variance is calculated from
 	 * @return variance of the given double array
 	 */
 	public static double var(int[] values) {
 		double mean = ArrayUtils.avg(values);
 		double x = 0;
 		for (double v : values) {
 			x += (v - mean) * (v - mean);
 		}
 		return x / (values.length - 1);
 	}
 
 	/**
 	 * Calculates the variance, variance-low and variance-up of the given double
 	 * array.
 	 * 
 	 * @param values
 	 *            double array the variances are calculated from
 	 * @param avg
 	 *            the average of the given values
 	 * @return variances of the given double array
 	 */
 	public static double[] varLowUp(double[] values, double avg) {
 		double var = 0;
 		double varLow = 0;
 		double varUp = 0;
 		int countLow = 0;
 		int countUp = 0;
 		for (double v : values) {
 			var += Math.pow(v - avg, 2);
 			if (v < avg) {
 				varLow += Math.pow(v - avg, 2);
 				countLow++;
 			} else if (v > avg) {
 				varUp += Math.pow(v - avg, 2);
 				countUp++;
 			}
 		}
 		var /= values.length;
 		if (countLow == 0) {
 			varLow = 0;
 		} else {
 			varLow /= countLow;
 		}
 		if (countUp == 0) {
 			varUp = 0;
 		} else {
 			varUp /= countUp;
 		}
 		return new double[] { var, varLow, varUp };
 	}
 
 	/**
 	 * Calculates the variance, variance-low and variance-up of the given long
 	 * array.
 	 * 
 	 * @param values
 	 *            long array the variances are calculated from
 	 * @param avg
 	 *            the average of the given values
 	 * @return variances of the given double array
 	 */
 	public static double[] varLowUp(long[] values, double avg) {
 		double var = 0;
 		double varLow = 0;
 		double varUp = 0;
 		int countLow = 0;
 		int countUp = 0;
 		for (long v : values) {
 			var += Math.pow(v - avg, 2);
 			if (v < avg) {
 				varLow += Math.pow(v - avg, 2);
 				countLow++;
 			} else if (v > avg) {
 				varUp += Math.pow(v - avg, 2);
 				countUp++;
 			}
 		}
 		var /= values.length;
 		if (countLow == 0) {
 			varLow = 0;
 		} else {
 			varLow /= countLow;
 		}
 		if (countUp == 0) {
 			varUp = 0;
 		} else {
 			varUp /= countUp;
 		}
 		return new double[] { var, varLow, varUp };
 	}
 
 	/**
 	 * Calculates the variance, variance-low and variance-up of the given int
 	 * array.
 	 * 
 	 * @param values
 	 *            int array the variances are calculated from
 	 * @param avg
 	 *            the average of the given values
 	 * @return variances of the given double array
 	 */
 	public static double[] varLowUp(int[] values, double avg) {
 		double var = 0;
 		double varLow = 0;
 		double varUp = 0;
 		int countLow = 0;
 		int countUp = 0;
 		for (int v : values) {
 			var += Math.pow(v - avg, 2);
 			if (v < avg) {
 				varLow += Math.pow(v - avg, 2);
 				countLow++;
 			} else if (v > avg) {
 				varUp += Math.pow(v - avg, 2);
 				countUp++;
 			}
 		}
 		var /= values.length;
 		if (countLow == 0) {
 			varLow = 0;
 		} else {
 			varLow /= countLow;
 		}
 		if (countUp == 0) {
 			varUp = 0;
 		} else {
 			varUp /= countUp;
 		}
 		return new double[] { var, varLow, varUp };
 	}
 
 	/**
 	 * Calculates the variance of the given array, while considering
 	 * Double.NaN's.
 	 * 
 	 * @param values
 	 *            double array the variance is calculated from
 	 * @return variance of the given double array
 	 */
 	public static double varIncludingNaN(double[] values) {
 		double mean = ArrayUtils.avgIncludingNaN(values);
 		double x = 0;
 		int counter = 0;
 		for (double v : values) {
 			if (!Double.isNaN(v)) {
 				x += (v - mean) * (v - mean);
 			} else {
 				Log.warn("Double.NaN detected");
 				counter++;
 			}
 		}
 		return x / (values.length - 1 - counter);
 	}
 
 	/**
 	 * Calculates the confidence interval of the given array. Student-t
 	 * distribution with 0,95 confidence niveau.
 	 * 
 	 * @param values
 	 *            double array the confidence is calculated from
 	 * @return confidence of the given double array
 	 */
 	public static double[] conf(double[] values) {
 
 		if (values.length == 1) {
 			return new double[] { values[0], values[0] };
 		}
 
 		double var = ArrayUtils.var(values);
 		double mean = ArrayUtils.avg(values);
 
 		double t = Settings.getStudentT(0.95, values.length - 1);
 		double x = t * (Math.sqrt(var) / Math.sqrt(values.length));
 
 		double low = mean - x;
 		double up = mean + x;
 
 		double[] conf = { low, up };
 
 		return conf;
 	}
 
 	/**
 	 * Calculates the confidence interval of the given array. Student-t
 	 * distribution with 0,95 confidence niveau.
 	 * 
 	 * @param values
 	 *            long array the confidence is calculated from
 	 * @return confidence of the given double array
 	 */
 	public static double[] conf(long[] values) {
 
 		if (values.length == 1) {
 			return new double[] { values[0], values[0] };
 		}
 
 		double var = ArrayUtils.var(values);
 		double mean = ArrayUtils.avg(values);
 
 		double t = Settings.getStudentT(0.95, values.length - 1);
 		double x = t * (Math.sqrt(var) / Math.sqrt(values.length));
 
 		double low = mean - x;
 		double up = mean + x;
 
 		double[] conf = { low, up };
 
 		return conf;
 	}
 
 	/**
 	 * Calculates the confidence interval of the given array. Student-t
 	 * distribution with 0,95 confidence niveau.
 	 * 
 	 * @param values
 	 *            int array the confidence is calculated from
 	 * @return confidence of the given double array
 	 */
 	public static double[] conf(int[] values) {
 
 		if (values.length == 1) {
 			return new double[] { values[0], values[0] };
 		}
 
 		double var = ArrayUtils.var(values);
 		double mean = ArrayUtils.avg(values);
 
 		double t = Settings.getStudentT(0.95, values.length - 1);
 		double x = t * (Math.sqrt(var) / Math.sqrt(values.length));
 
 		double low = mean - x;
 		double up = mean + x;
 
 		double[] conf = { low, up };
 
 		return conf;
 	}
 
 	/**
 	 * Calculates the confidence interval of the given array, while considering
 	 * Double.NaN's. Student-t distribution with 0,95 confidence niveau.
 	 * 
 	 * @param values
 	 *            double array the confidence is calculated from
 	 * @return confidence of the given double array
 	 */
 	public static double[] confIncludingNaN(double[] values) {
 		double var = ArrayUtils.varIncludingNaN(values);
 		double mean = ArrayUtils.avgIncludingNaN(values);
 
 		int counter = 0;
 		for (double v : values) {
 			if (Double.isNaN(v))
 				counter++;
 		}
 		double t = Settings.getStudentT(0.95, values.length - 1 - counter);
 		double x = t * (Math.sqrt(var) / Math.sqrt(values.length - counter));
 
 		double low = mean - x;
 		double high = mean + x;
 
 		double[] conf = { low, high };
 
 		return conf;
 	}
 
 	/**
 	 * 
 	 * @param v1
 	 *            arrays of double values
 	 * @param v2
 	 *            array of double values to compare arrays are not equal
 	 * @return true if both arrays have the same length and all values are equal
 	 */
 	public static boolean equals(double[] v1, double[] v2) {
 		return ArrayUtils.equals(v1, v2, null);
 	}
 
 	/**
 	 * 
 	 * @param v1
 	 *            arrays of double values
 	 * @param v2
 	 *            array of double values to compare
 	 * @param name
 	 *            if a name is given, debug log output is printed in case the
 	 *            arrays are not equal
 	 * @return true if both arrays have the same length and all values are equal
 	 */
 	public static boolean equals(double[] v1, double[] v2, String name) {
 		if (v1.length != v2.length) {
 			if (name != null) {
 				Log.warn(name + " - length differs: " + v1.length + " != "
 						+ v2.length);
 			}
 			return false;
 		}
 		for (int i = 0; i < v1.length; i++) {
 			if (v1[i] != v2[i]
 					&& (!Double.isNaN(v1[i]) || !Double.isNaN(v2[i]))) {
 				if (name != null) {
 					Log.warn(name + " - values @ index " + i + " differs: "
 							+ v1[i] + " != " + v2[i]);
 				}
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * 
 	 * @param v1
 	 *            arrays of long values
 	 * @param v2
 	 *            array of long values to compare arrays are not equal
 	 * @return true if both arrays have the same length and all values are equal
 	 */
 	public static boolean equals(long[] v1, long[] v2) {
 		return ArrayUtils.equals(v1, v2, null);
 	}
 
 	/**
 	 * 
 	 * @param v1
 	 *            arrays of long values
 	 * @param v2
 	 *            array of long values to compare
 	 * @param name
 	 *            if a name is given, debug log output is printed in case the
 	 *            arrays are not equal
 	 * @return true if both arrays have the same length and all values are equal
 	 */
 	public static boolean equals(long[] v1, long[] v2, String name) {
 		if (v1.length != v2.length) {
 			if (name != null) {
 				Log.warn(name + " - length differs: " + v1.length + " != "
 						+ v2.length);
 			}
 			return false;
 		}
 		for (int i = 0; i < v1.length; i++) {
 			if (v1[i] != v2[i]) {
 				if (name != null) {
 					Log.warn(name + " - values @ index " + i + " differs: "
 							+ v1[i] + " != " + v2[i]);
 				}
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public static double[] init(int length, double value) {
 		double[] array = new double[length];
 		for (int i = 0; i < array.length; i++) {
 			array[i] = value;
 		}
 		return array;
 	}
 
 	public static int[] init(int length, int value) {
 		int[] array = new int[length];
 		for (int i = 0; i < array.length; i++) {
 			array[i] = value;
 		}
 		return array;
 	}
 
 	public static long[] init(int length, long value) {
 		long[] array = new long[length];
 		for (int i = 0; i < array.length; i++) {
 			array[i] = value;
 		}
 		return array;
 	}
 
 	public static boolean isIncluded(int[] values, int value) {
 		for (int i = 0; i < values.length; i++) {
 			if (values[i] == value)
 				return true;
 		}
 		return false;
 	}
 
 	public static String[] toStringArray(Vector<String> v) {
 		String[] array = new String[v.size()];
 		Iterator<String> iter = v.listIterator();
 		int index = 0;
 		while (iter.hasNext()) {
 			array[index++] = iter.next();
 		}
 		return array;
 	}
 }
