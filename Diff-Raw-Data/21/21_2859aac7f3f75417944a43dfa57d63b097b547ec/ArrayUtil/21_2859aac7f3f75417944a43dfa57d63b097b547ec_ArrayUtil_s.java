 package util;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 public class ArrayUtil
 {
 	public static String toCSVString(Object a[])
 	{
 		return toCSVString(a, false);
 	}
 
 	public static String toCSVString(Object a[], boolean addQuotes)
 	{
 		String s = "";
		for (Object st : a)
		{
			if ((st + "").contains(","))
				throw new IllegalArgumentException("cannot convert elem with ',' to csv string: " + st);
			if (addQuotes)
				s += "\"" + st + "\",";
			else
				s += st + ",";
		}
 		return s;
 	}
 
 	public static String intToCSVString(int a[])
 	{
 		String s = "";
 		for (int i : a)
 			s += i + ",";
 		return s;
 	}
 
 	public static String booleanToCSVString(boolean a[])
 	{
 		String s = "";
 		for (boolean i : a)
 			s += i + ",";
 		return s;
 	}
 
 	public static int[] intFromCSVString(String csv)
 	{
 		List<Integer> res = new ArrayList<Integer>();
 		if (csv != null)
 		{
 			StringTokenizer tok = new StringTokenizer(csv, ",");
 			while (tok.hasMoreElements())
 			{
 				String s = (String) tok.nextElement();
 				if (s.trim().length() > 0)
 					res.add(Integer.parseInt(s));
 			}
 		}
 		return ArrayUtil.toPrimitiveIntArray(res);
 	}
 
 	public static int sum(int[] array)
 	{
 		int sum = 0;
 		for (int i : array)
 			sum += i;
 		return sum;
 	}
 
 	public static int indexOf(int[] array, int elem)
 	{
 		for (int i = 0; i < array.length; i++)
 			if (array[i] == elem)
 				return i;
 		return -1;
 	}
 
 	public static int indexOf(double[] array, double elem)
 	{
 		for (int i = 0; i < array.length; i++)
 			if (array[i] == elem)
 				return i;
 		return -1;
 	}
 
 	public static <T> int indexOf(T[] array, T elem)
 	{
 		for (int i = 0; i < array.length; i++)
 			if (ObjectUtil.equals(array[i], elem))
 				return i;
 		return -1;
 	}
 
 	public static <T> int occurences(T[] array, T elem)
 	{
 		int o = 0;
 		for (int i = 0; i < array.length; i++)
 			if (array[i].equals(elem))
 				o++;
 		return o;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T, T2> T[] cast(Class<T> type, T2[] array)
 	{
 		T a[] = (T[]) Array.newInstance(type, array.length);
 		for (int i = 0; i < array.length; i++)
 			a[i] = (T) array[i];
 		return a;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> T[] reverse(T[] array)
 	{
 		T a[] = (T[]) Array.newInstance(array[0].getClass(), array.length);
 		for (int i = 0; i < a.length; i++)
 			a[i] = array[a.length - (i + 1)];
 		return a;
 	}
 
 	public static Double[] toDoubleArray(Vector<Double> vector)
 	{
 		Double[] a = new Double[vector.size()];
 		return vector.toArray(a);
 	}
 
 	public static Double[] toDoubleArray(double array[])
 	{
 		Double[] a = new Double[array.length];
 		for (int i = 0; i < a.length; i++)
 			a[i] = new Double(array[i]);
 		return a;
 	}
 
 	public static Integer[] toIntegerArray(int array[])
 	{
 		Integer[] a = new Integer[array.length];
 		for (int i = 0; i < a.length; i++)
 			a[i] = new Integer(array[i]);
 		return a;
 	}
 
 	public static <T> T[] toArray(List<T> list)
 	{
 		return ListUtil.toArray(list);
 	}
 
 	public static <T> T[] toArray(Set<T> set)
 	{
 		return CollectionUtil.toArray(set);
 	}
 
 	public static Integer[] toIntegerArray(List<Integer> values)
 	{
 		Integer[] i = new Integer[values.size()];
 		return values.toArray(i);
 	}
 
 	/**
 	 * no casting, +"" !
 	 */
 	public static String[] toStringArray(Object array[])
 	{
 		String[] a = new String[array.length];
 		for (int i = 0; i < a.length; i++)
 			a[i] = array[i] + "";
 		return a;
 	}
 
 	public static Double[] toDoubleArray(Integer array[])
 	{
 		Double[] a = new Double[array.length];
 		for (int i = 0; i < a.length; i++)
 			a[i] = new Double(array[i]);
 		return a;
 	}
 
 	public static List<Double> removeNullValues(Double array[])
 	{
 		List<Double> d = new ArrayList<Double>();
 		for (int i = 0; i < array.length; i++)
 			if (array[i] != null)
 				d.add(array[i]);
 		return d;
 	}
 
 	public static double[] toPrimitiveDoubleArray(Double[] doubles)
 	{
 		double[] d = new double[doubles.length];
 		for (int j = 0; j < d.length; j++)
 			d[j] = doubles[j];
 		return d;
 	}
 
 	public static double[] toPrimitiveDoubleArray(Collection<Double> doubles)
 	{
 		double[] d = new double[doubles.size()];
 		Iterator<Double> it = doubles.iterator();
 		int i = 0;
 		while (it.hasNext())
 		{
 			d[i] = it.next();
 			i++;
 		}
 		return d;
 	}
 
 	public static int[] toPrimitiveIntArray(Collection<Integer> ints)
 	{
 		int[] d = new int[ints.size()];
 		Iterator<Integer> it = ints.iterator();
 		int i = 0;
 		while (it.hasNext())
 		{
 			d[i] = it.next();
 			i++;
 		}
 		return d;
 	}
 
 	public static boolean[] toPrimitiveBooleanArray(Collection<Boolean> bools)
 	{
 		boolean[] d = new boolean[bools.size()];
 		Iterator<Boolean> it = bools.iterator();
 		int i = 0;
 		while (it.hasNext())
 		{
 			d[i] = it.next();
 			i++;
 		}
 		return d;
 	}
 
 	//	public static List<Object> toList(Object[] o)
 	//	{
 	//		List<Object> d = new ArrayList<Object>();
 	//		for (Object e : o)
 	//			d.add(e);
 	//		return d;
 	//	}
 
 	public static <T> List<T> toList(T[] o)
 	{
 		List<T> d = new ArrayList<T>();
 		for (T e : o)
 			d.add(e);
 		return d;
 	}
 
 	public static List<Double> toList(double[] doubles)
 	{
 		List<Double> d = new ArrayList<Double>();
 		for (double e : doubles)
 			d.add(e);
 		return d;
 	}
 
 	public static List<Integer> toList(int[] doubles)
 	{
 		List<Integer> d = new ArrayList<Integer>();
 		for (int e : doubles)
 			d.add(e);
 		return d;
 	}
 
 	public static <T> HashSet<T> getDistinctValues(T array[])
 	{
 		return new HashSet<T>(ArrayUtil.toList(array));
 	}
 
 	public static Double[] normalizeObjectArray(Object array[])
 	{
 		if (array.length == 0)
 			return new Double[0];
 		// the old way sorted in order of number of occurences
 		//List<Object> valuesInOrderOfCounts = CountedSet.fromArray(array).values();
 
 		//remove duplicates and convert to list, sort list
 		List<Object> s = new ArrayList<Object>(new HashSet<Object>(ArrayUtil.toList(array)));
 		Collections.sort(s, new DefaultComparator<Object>());
 		if (s.contains(null))
 			s.remove(null);
 
 		Double[] indices = new Double[array.length];
 		for (int i = 0; i < indices.length; i++)
 			if (array[i] != null)
 				indices[i] = (double) s.indexOf(array[i]);
 			else
 				indices[i] = null;
 
 		return normalize(indices, false);
 	}
 
 	/**
 	 * normalizes to 0-1<br>
 	 * REPLACES NULL VALUES WITH 0.5
 	 * 
 	 * @param array
 	 * @return
 	 */
 	public static Double[] normalize(Double array[], boolean replaceNullWithMedian)
 	{
 		return normalize(array, 0, 1, replaceNullWithMedian);
 	}
 
 	public static Double[] normalizeLog(Double array[], boolean replaceNullWithMedian)
 	{
 		DoubleArraySummary summary = DoubleArraySummary.create(array);
 		array = replaceNull(array, summary.mean);
 		if (summary.min <= 0)
 			array = translate(array, Math.abs(summary.min) + 1);
 		array = log(array);
 		return normalize(array, 0, 1, replaceNullWithMedian);
 	}
 
 	public static Double[] replaceNull(Double array[], double replace)
 	{
 		Double a[] = new Double[array.length];
 		for (int i = 0; i < a.length; i++)
 			a[i] = array[i] == null ? replace : array[i];
 		return a;
 	}
 
 	public static Double[] translate(Double array[], double offset)
 	{
 		Double a[] = new Double[array.length];
 		for (int i = 0; i < a.length; i++)
 			a[i] = array[i] + offset;
 		return a;
 	}
 
 	public static Double[] log(Double array[])
 	{
 		Double a[] = new Double[array.length];
 		for (int i = 0; i < a.length; i++)
 			a[i] = Math.log10(array[i]);
 		return a;
 	}
 
 	/**
 	 * normalizes to MIN-MAX<br>
 	 * REPLACES NULL VALUES WITH MEDIAN(MIN,MAX)
 	 * 
 	 * @param array
 	 * @return
 	 */
 	public static Double[] normalize(Double array[], double min, double max, boolean replaceNullWithMedian)
 	{
 		DoubleArraySummary sum = DoubleArraySummary.create(array);
 		double deltaVal = sum.max - sum.min;
 		double delta = (max - min);
 		Double a[] = new Double[array.length];
 		if (sum.min == sum.max)
 		{
 			Arrays.fill(a, min + delta / 2.0);
 			return a;
 		}
 		else
 		{
 			for (int i = 0; i < a.length; i++)
 			{
 				Double v = array[i];
 				if (replaceNullWithMedian && (v == null || Double.isNaN(v)))
 					v = sum.median;
 
 				if (v == null)
 					a[i] = null;
 				else
 					a[i] = (v - sum.min) / deltaVal * delta + min;
 			}
 			return a;
 		}
 	}
 
 	public static void normalize(double array[][])
 	{
 		normalize(array, 0, 1);
 	}
 
 	public static void normalize(double array[][], double min, double max)
 	{
 		double[] minMax = getMinMax(array);
 		double deltaVal = minMax[1] - minMax[0];
 		double delta = (max - min);
 		for (int i = 0; i < array.length; i++)
 			for (int j = 0; j < array[0].length; j++)
 				array[i][j] = (array[i][j] - minMax[0]) / deltaVal * delta + min;
 	}
 
 	public static <T> int[] getOrdering(T[] array, Comparator<T> comp)
 	{
 		return getOrdering(array, comp, true);
 	}
 
 	public static <T> int[] getOrdering(T[] array, Comparator<T> comp, boolean ascending)
 	{
 		int order[] = new int[array.length];
 		for (int i = 0; i < order.length; i++)
 			order[i] = i;
 		int invert = ascending ? -1 : 1;
 		for (int i = 0; i < order.length - 1; i++)
 			for (int j = i + 1; j < order.length; j++)
 			{
 				if (comp.compare(array[order[i]], array[order[j]]) * invert < 1)
 				{
 					int tmp = order[i];
 					order[i] = order[j];
 					order[j] = tmp;
 				}
 			}
 		return order;
 	}
 
 	public static int[] getRanking(int[] ordering)
 	{
 		int ranking[] = new int[ordering.length];
 		for (int i = 0; i < ranking.length; i++)
 			ranking[ordering[i]] = i;
 		return ranking;
 	}
 
 	public static int[] getOrdering(double[] array, boolean ascending)
 	{
 		int order[] = new int[array.length];
 		for (int i = 0; i < order.length; i++)
 			order[i] = i;
 		for (int i = 0; i < order.length - 1; i++)
 			for (int j = i + 1; j < order.length; j++)
 			{
 				if ((ascending && array[order[i]] > array[order[j]])
 						|| (!ascending && array[order[i]] < array[order[j]]))
 				{
 					int tmp = order[i];
 					order[i] = order[j];
 					order[j] = tmp;
 				}
 			}
 		return order;
 	}
 
 	public static <T> T[] sort(T[] array, Comparator<T> comp)
 	{
 		return sortAccordingToOrdering(getOrdering(array, comp), array);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> T[] sortAccordingToOrdering(int[] order, T[] array)
 	{
 		T[] res = (T[]) Array.newInstance(array[0].getClass(), array.length);
 		for (int j = 0; j < array.length; j++)
 			res[j] = array[order[j]];
 		return res;
 	}
 
 	public static double[] sortAccordingToOrdering(int[] order, double[] array)
 	{
 		double[] res = new double[array.length];
 		for (int j = 0; j < array.length; j++)
 			res[j] = array[order[j]];
 		return res;
 	}
 
 	public static double getMean(double[] a)
 	{
 		double sum = 0;
 		for (int i = 0; i < a.length; i++)
 			sum += a[i];
 		if (sum == 0)
 			return 0;
 		else
 			return sum / (double) a.length;
 	}
 
 	public static double[] getMinMax(double[] a)
 	{
 		double min = Double.MAX_VALUE;
 		double max = -Double.MAX_VALUE;
 
 		for (int i = 0; i < a.length; i++)
 		{
 			if (min > a[i])
 				min = a[i];
 			if (max < a[i])
 				max = a[i];
 		}
 		return new double[] { min, max };
 	}
 
 	public static double[] getMinMax(double[][] a)
 	{
 		double min = Double.MAX_VALUE;
 		double max = -Double.MAX_VALUE;
 
 		for (int i = 0; i < a.length; i++)
 		{
 			double mm[] = getMinMax(a[i]);
 			if (min > mm[0])
 				min = mm[0];
 			if (max < mm[1])
 				max = mm[1];
 		}
 		return new double[] { min, max };
 	}
 
 	public static int[] getMinMax(int[] a)
 	{
 		int min = Integer.MAX_VALUE;
 		int max = Integer.MIN_VALUE;
 
 		for (int i = 0; i < a.length; i++)
 		{
 			if (min > a[i])
 				min = a[i];
 			if (max < a[i])
 				max = a[i];
 		}
 		return new int[] { min, max };
 	}
 
 	public static double[] concat(double[] a, double[] b)
 	{
 		double[] c = new double[a.length + b.length];
 		System.arraycopy(a, 0, c, 0, a.length);
 		System.arraycopy(b, 0, c, a.length, b.length);
 		return c;
 	}
 
 	public static int[] concat(int[] a, int[] b)
 	{
 		int[] c = new int[a.length + b.length];
 		System.arraycopy(a, 0, c, 0, a.length);
 		System.arraycopy(b, 0, c, a.length, b.length);
 		return c;
 	}
 
 	public static <T> T[] concat(T[]... arrays)
 	{
 		if (arrays[0].length == 0)
 			throw new IllegalArgumentException("no entry in first array");
 		return concat(arrays[0][0].getClass(), arrays);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> T[] concat(Class<?> type, T[]... arrays)
 	{
 
 		// Determine required size of new array
 		int count = 0;
 		for (T[] array : arrays)
 		{
 			//System.out.println(toString(array));
 			count += array.length;
 		}
 
 		// create new array of required class
 		T[] mergedArray = (T[]) Array.newInstance(type, count);
 
 		// Merge each array into new array
 		int start = 0;
 		for (T[] array : arrays)
 		{
 			System.arraycopy(array, 0, mergedArray, start, array.length);
 			start += array.length;
 		}
 		return (T[]) mergedArray;
 	}
 
 	public static <T> List<T[]> split(T[] array, int n)
 	{
 		int length = array.length / n;
 		int rest = array.length - (length * n);
 
 		List<T[]> list = new ArrayList<T[]>();
 		int array_index = 0;
 
 		for (int i = 0; i < n; i++)
 		{
 			int a_length = length;
 			if (i < rest)
 				a_length++;
 
 			list.add(Arrays.copyOfRange(array, array_index, array_index + a_length));
 			array_index += a_length;
 		}
 		return list;
 	}
 
 	public static <T> void scramble(T[] array)
 	{
 		scramble(array, new Random());
 	}
 
 	public static <T> void scramble(T[] array, Random r)
 	{
 		for (int i = 0; i < array.length; i++)
 		{
 			int j = r.nextInt(array.length);
 			T tmp = array[j];
 			array[j] = array[i];
 			array[i] = tmp;
 		}
 	}
 
 	public static void scramble(float[] array)
 	{
 		scramble(array, new Random());
 	}
 
 	public static void scramble(float[] array, Random r)
 	{
 		for (int i = 0; i < array.length; i++)
 		{
 			int j = r.nextInt(array.length);
 			float tmp = array[j];
 			array[j] = array[i];
 			array[i] = tmp;
 		}
 	}
 
 	public static void scramble(int[] array)
 	{
 		scramble(array, new Random());
 	}
 
 	public static void scramble(int[] array, Random r)
 	{
 		for (int i = 0; i < array.length; i++)
 		{
 			int j = r.nextInt(array.length);
 			int tmp = array[j];
 			array[j] = array[i];
 			array[i] = tmp;
 		}
 	}
 
 	public static String toString(int array[])
 	{
 		return toString(array, -1);
 	}
 
 	public static String toString(int array[], int formatSpace)
 	{
 		String s = "[ ";
 		for (int i = 0; i < array.length; i++)
 			s += (formatSpace != -1 ? StringUtil.concatWhitespace(array[i] + "", formatSpace, false) : array[i]) + "; ";
 		if (array.length > 0)
 			s = s.substring(0, s.length() - 2);
 		s += " ]";
 		return s;
 	}
 
 	public static String toString(boolean array[])
 	{
 		return toString(array, new String[] { "true", "false" });
 	}
 
 	public static String toString(boolean array[], String trueFalse[])
 	{
 		String t = trueFalse[0];
 		String f = trueFalse[1];
 		String s = "[ ";
 		for (int i = 0; i < array.length; i++)
 			s += (array[i] ? t : f) + "; ";
 		if (array.length > 0)
 			s = s.substring(0, s.length() - 2);
 		s += " ]";
 		return s;
 	}
 
 	public static String toString(Object array[], String seperator, String openingBracket, String closingBracket)
 	{
 		String s = openingBracket;
 		for (int i = 0; i < array.length; i++)
 			s += array[i] + seperator + " ";
 		if (array.length > 0)
 			s = s.substring(0, s.length() - (1 + seperator.length()));
 		s += closingBracket;
 		return s;
 	}
 
 	public static String toString(Object array[], String seperator)
 	{
 		return toString(array, seperator, "[ ", " ]");
 	}
 
 	public static String toString(Object array[])
 	{
 		return toString(array, ";");
 	}
 
 	public static String toString(Double array[], boolean format)
 	{
 		return toString(ArrayUtil.toPrimitiveDoubleArray(array), format);
 	}
 
 	public static String toString(double array[])
 	{
 		return toString(array, false);
 	}
 
 	public static String toString(double array[], boolean format)
 	{
 		String s = "[ ";
 		for (int i = 0; i < array.length; i++)
 			s += (format ? StringUtil.formatDouble(array[i]) : array[i]) + "; ";
 		if (array.length > 0)
 			s = s.substring(0, s.length() - 2);
 		s += " ]";
 		return s;
 	}
 
 	public static String toString(double array[][])
 	{
 		return toString(array, false);
 	}
 
 	public static String toString(double array[][], boolean format)
 	{
 		String s = "[\n";
 		for (int i = 0; i < array.length; i++)
 			s += " " + ArrayUtil.toString(array[i], format) + "\n";
 		s += "]";
 		return s;
 	}
 
 	public static String toString(String array[][])
 	{
 		String s = "";
 		int length[] = new int[array[0].length];
 		for (int j = 0; j < array[0].length; j++)
 		{
 			int l = -1;
 			for (int i = 0; i < array.length; i++)
 				if (array[i][j].length() > l)
 					l = array[i][j].length();
 			length[j] = l;
 		}
 		for (int i = 0; i < array.length; i++)
 		{
 			for (int j = 0; j < array[0].length; j++)
 				s += StringUtil.concatWhitespace(array[i][j], length[j]) + " ";
 			s += "\n";
 		}
 		return s;
 	}
 
 	public static String toString(String desc[], Object vals[])
 	{
 		if (desc.length != vals.length)
 			throw new IllegalArgumentException();
 		String s = "";
 		for (int i = 0; i < vals.length; i++)
 		{
 			if (i > 0)
 				s += "_";
 			s += desc[i] + "-" + vals[i];
 		}
 		return s;
 	}
 
 	/**
 	 * trys to parse String array into Double array (empty strings/null are inserted as null)
 	 * returns null if a non-parsable String is included
 	 * 
 	 * @param array
 	 * @return
 	 */
 	public static Double[] parse(Object[] array)
 	{
 		Double d[] = new Double[array.length];
 		for (int i = 0; i < d.length; i++)
 		{
 			if (array[i] == null || array[i].equals("null") || array[i].toString().trim().length() == 0)
 				d[i] = null;
 			else if (array[i].equals("nan"))
 				d[i] = Double.NaN;
 			else
 			{
 				Double doub = DoubleUtil.parseDouble(array[i].toString());
 				if (doub == null)
 					return null;
 				else
 					d[i] = doub;
 			}
 		}
 		return d;
 	}
 
 	/**
 	 * trys to parse String array into Integer array (empty strings/null are inserted as null)
 	 * returns null if a non-parsable String is included
 	 * 
 	 * @param array
 	 * @return
 	 */
 	public static Integer[] parseIntegers(Object[] array)
 	{
 		Integer ints[] = new Integer[array.length];
 		for (int i = 0; i < ints.length; i++)
 		{
 			if (array[i] == null || array[i].equals("null") || array[i].toString().trim().length() == 0)
 				ints[i] = null;
 			else
 			{
 				Integer parse = IntegerUtil.parseInteger(array[i].toString());
 				if (parse == null)
 					return null;
 				else
 					ints[i] = parse;
 			}
 		}
 		return ints;
 	}
 
 	public static boolean[] parseBoolean(String[] array)
 	{
 		boolean bArray[] = new boolean[array.length];
 		for (int i = 0; i < bArray.length; i++)
 		{
 			Boolean b = Boolean.parseBoolean(array[i]);
 			if (b == null)
 				throw new IllegalArgumentException("not a bool: " + array[i]);
 			else
 				bArray[i] = b;
 		}
 		return bArray;
 	}
 
 	public static double euclDistance(double[] values, double[] values2)
 	{
 		double d = 0;
 		for (int i = 0; i < values2.length; i++)
 			d += Math.pow(values[i] - values2[i], 2);
 		return Math.sqrt(d);
 	}
 
 	public static Object analyze(Object[] v)
 	{
 
 		try
 		{
 			return DoubleArraySummary.create(ArrayUtil.cast(Double.class, v)).format();
 		}
 		catch (ArrayStoreException e)
 		{
 			CountedSet<Object> set = CountedSet.fromArray(v);
 			if (v.length <= 3)
 				return set;
 			else if (v.length < 10 && set.size() < v.length * 2 / 3)
 				return set;
 			else if (set.size() < v.length * 2 / 3)
 				return set;
 			return "...";
 
 		}
 
 	}
 
 	public static <T> int getMedianIndex(T[] array, Comparator<T> cmp)
 	{
 		int[] order = ArrayUtil.getOrdering(array, cmp);
 		return order[(order.length + 1) / 2 - 1];
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> T[] cut(Class<T> type, T[] a1, T[] a2)
 	{
 		List<T> cut = new ArrayList<T>();
 		for (int i = 0; i < a1.length; i++)
 			if (ArrayUtil.indexOf(a2, a1[i]) != -1)
 				cut.add(a1[i]);
 		T[] c = (T[]) Array.newInstance(type, cut.size());
 		cut.toArray(c);
 		return c;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> T[] remove(Class<T> type, T[] a1, T[] a2)
 	{
 		List<T> rem = new ArrayList<T>();
 		for (int i = 0; i < a1.length; i++)
 			if (ArrayUtil.indexOf(a2, a1[i]) == -1)
 				rem.add(a1[i]);
 		T[] c = (T[]) Array.newInstance(type, rem.size());
 		rem.toArray(c);
 		return c;
 	}
 
 	private static class Permutation<T>
 	{
 		T[] input;
 		int outputIndex = 0;
 		List<T[]> output;
 
 		@SuppressWarnings("unchecked")
 		private Permutation(T[] input)
 		{
 			this.input = input;
 			int faculty = faculty(input.length);
 			output = new ArrayList<T[]>(faculty);
 			for (int i = 0; i < faculty; i++)
 				output.add((T[]) Array.newInstance(input[0].getClass(), input.length));
 			permutations(0);
 		}
 
 		private void permutations(int offset)
 		{
 			if (input.length - offset == 1)
 			{
 				// Input now contains a permutation, here I store it in output,
 				// but you can do anything you like with it
 				System.arraycopy(input, 0, output.get(outputIndex++), 0, input.length);
 				return;
 			}
 			T a = input[offset];
 			for (int i = offset; i < input.length; i++)
 			{
 				// Swap elements
 				T b = input[i];
 				input[i] = a;
 				input[offset] = b;
 
 				permutations(offset + 1);
 				// Restore element
 				input[i] = b;
 			}
 			input[offset] = a;
 		}
 
 		private static int faculty(int n)
 		{
 			return n == 1 ? n : n * faculty(n - 1);
 		}
 	}
 
 	public static <T> List<T[]> permute(T input[])
 	{
 		Permutation<T> p = new Permutation<T>(input);
 		return p.output;
 	}
 
 	/**
 	 * returns ordering to sort input array2
 	 * 
 	 * Integer[] input1 = new Integer[] { 1, 2, 3, 4, 5, 6 };
 	 * Integer[] input2 = new Integer[] { 7, 8, 5, 1, 11, 1 };
 	 * int[] ordering = naiveMap(input1, input2, new Comparator<Integer>()
 	 * {
 	 * 	public int compare(Integer o1, Integer o2)
 	 * 	{
 	 * 		return Math.abs(o1 - o2);
 	 * 	}
 	 * });
 	 * System.out.println(toString(ordering) + "\n");
 	 * System.out.println(toString(input1));
 	 * System.out.println(toString(sortAccordingToOrdering(ordering, input2)));
 	 * 
 	 * @param input1
 	 * @param input2
 	 * @param comp
 	 * @return
 	 */
 	public static <T> int[] naiveMap(T input1[], T input2[], Comparator<T> comp)
 	{
 		if (input1.length != input2.length)
 			throw new IllegalArgumentException();
 		int[] ordering = new int[input1.length];
 		Arrays.fill(ordering, -1);
 		for (int i = 0; i < input1.length; i++)
 		{
 			for (int j = 0; j < input2.length; j++)
 			{
 				if (indexOf(ordering, j) == -1 && comp.compare(input1[i], input2[j]) == 0)
 				{
 					ordering[i] = j;
 					break;
 				}
 			}
 		}
 		for (int i = 0; i < input1.length; i++)
 		{
 			if (ordering[i] != -1)
 				continue;
 			for (int j = 0; j < input2.length; j++)
 			{
 				if (indexOf(ordering, j) != -1)
 					continue;
 				ordering[i] = j;
 				break;
 			}
 		}
 		return ordering;
 	}
 
 	public static void main(String args[])
 	{
 		//		Integer[] input1 = new Integer[] { 1, 2, 3, 4, 5, 6 };
 		//		Integer[] input2 = new Integer[] { 7, 8, 5, 1, 11, 1 };
 		//		int[] ordering = naiveMap(input1, input2, new Comparator<Integer>()
 		//		{
 		//			public int compare(Integer o1, Integer o2)
 		//			{
 		//				return Math.abs(o1 - o2);
 		//			}
 		//		});
 		//		System.out.println(toString(ordering) + "\n");
 		//		System.out.println(toString(input1));
 		//		System.out.println(toString(sortAccordingToOrdering(ordering, input2)));
 
 		String s[] = { "ene", "miste", "mene" };
 		Integer[] rank = new Integer[] { 1, 3, 2 };
 		int order[] = getOrdering(rank, new Comparator<Integer>()
 		{
 			@Override
 			public int compare(Integer o1, Integer o2)
 			{
 				return o1.compareTo(o2);
 			}
 		}, true);
 		System.out.println(toString(order) + "\n");
 		System.out.println(toString(sortAccordingToOrdering(order, rank)) + "\n");
 		System.out.println(toString(sortAccordingToOrdering(order, s)) + "\n");
 
 		//		Integer[] input = new Integer[] { 1, 2, 3, 4, 5, 6 };
 		//		List<Integer[]> output = permute(input);
 
 		//		for (Integer[] a : output)
 		//			System.out.println(toString(a));
 
 		//		Object o[] = { "a", "b", "a", "c", "b", "b" };
 		//		System.out.println(toString(normalize(o)));
 
 		//		Double d[] = new Double[] { -4.0, 2.0, 0.0, -5.0, 1.0, 5.0, null };
 		//		System.out.println(toString(normalize(d, false)));
 
 		//		Object s[] = { new Double(5), new Double(3) };
 		//		Double o[] = ArrayUtil.cast(Double.class, s);
 		//		System.out.println(o[0].getClass());
 
 		// double d[] = new double[] { 0.0, 100.0, 50, 22 };
 		// System.out.println(Arrays.toString(ArrayUtil.normalize(d)));
 
 		// Integer[] test = new Integer[66];
 		// for (int i = 0; i < test.length; i++)
 		// {
 		// test[i] = i;
 		// }
 		//
 		// scramble(test);
 		//
 		// List<Integer[]> l = split(test, 10);
 		//
 		// for (Integer[] integers : l)
 		// {
 		// System.out.print("[");
 		// for (Integer integer : integers)
 		// {
 		// System.out.printf(" %2s", integer.toString());
 		// }
 		// System.out.println(" ]");
 		// }
 	}
 }
