 package us.exultant.ahs.util;
 
 import java.util.*;
import java.lang.reflect.Array;
 import java.nio.*;
 
 /**
  * Similar to java.util.Arrays, but with some added functionality. Helps maintain sanity
  * across java versions. Adds a bunch of convenience methods for primitive array copying,
  * concatenating, etc.
  * 
  * @author hash
  * 
  */
 public class Arr {
 	public <T> T[] array(T... $xs) {
 		return $xs;
 	}
 	
 	
 	
 	
 	
 	//MAKEMORE
 	public static boolean contains(String[] $arr, String $x) {
 		for (int $i = 0; $i < $arr.length; $i++)
 			if ($arr[$i].equals($x)) return true;
 		return false;
 	}
 	
 	
 	
 	public static Map<String,String> asMap(String[][] $nx2) {
 		Map<String,String> $v = new HashMap<String,String>($nx2.length);
 		for (String[] $s : $nx2)
 			$v.put($s[0], $s[1]);
 		return $v;
 	}
 	
 	
 	/**
 	 * <p>
 	 * Produces a new byte array by reading the contents of the given ByteBuffer.
 	 * After this method returns, the given ByteBuffer's position will match its
 	 * limit.
 	 * </p>
 	 * 
 	 * <p>
 	 * If it is known that the ByteBuffer's backing array is not longer than its limit
 	 * and you are comfortable with recycling that backing array, the
 	 * <code>array()</code> method on ByteBuffer itself is a more memory-efficent
 	 * choice (even if you use this method and then immediately allow the ByteBuffer
 	 * to be gc'd).
 	 * </p>
 	 * 
 	 * @param $bb
 	 * @return a new byte array that has a length matching the ByteBuffer's current
 	 *         limit and has contents matching the ByteBuffer's content from 0 to
 	 *         limit.
 	 */
 	public static byte[] toArray(ByteBuffer $bb) {
 		byte[] $v = new byte[$bb.limit()];
 		$bb.rewind();
 		$bb.get($v);
 		return $v;
 	}
 	
 	public static float[] toArray(FloatBuffer $b) {
 		float[] $v = new float[$b.limit()];
 		$b.rewind();
 		$b.get($v);
 		return $v;
 	}
 	
 	/**
 	 * <p>
 	 * This behaves exactly as per ByteBuffer.wrap(Arr.toArray($bb)).
 	 * </p>
 	 * 
 	 * @param $bb
 	 * @return a new ByteBuffer that wraps a new byte array, having a length matching
 	 *         the given ByteBuffer's current limit and has contents matching the
 	 *         given ByteBuffer's content from 0 to limit.
 	 */
 	public static ByteBuffer makeWrapped(ByteBuffer $bb) {
 		return ByteBuffer.wrap(Arr.toArray($bb));
 	}
 	
 	//MAKEMORE
 	/**
 	 * Returns a new array with only non-null elements (in the same order as the old
 	 * array) truncated to the smallest size possible.  The given array is not mutated.
 	 * 
 	 * @return an array without any nulls.
 	 */
 	public static String[] arrayCleanse(String[] $arr) {
		return arrayCleanseDestructive(Arr.copy($arr, String.class));
 		// you might think there'd be a more efficient way to do this... but you're wrong.
 		// you'll inevitably need a new array at the end for trimming (unless you're willing to double-walk it first and count)
 		// and you'll need a working array other than the original one in the meantime.
 		// so basically, clone the first one, screw with it, and return a truncated one is the best you can do.
 		// if I go ape-shit over efficiency options and want to enable a trade to spend more cpu to save memory, i might make a double-walk method someday.
 	}
 	
 	//MAKEMORE
 	/**
 	 * Mutates original, compacting all nulls to the end and all non-nulls to the
 	 * beginning (leaving array size unchanged, obviously). Returns a new array in the
 	 * same order as the mutated old array, but truncated to contain only the non-null
 	 * elements.
 	 * 
 	 * @return an array without any nulls.
 	 */
 	public static String[] arrayCleanseDestructive(String[] $arr) {
 		// this could be made smarter by keeping pointer to the first empty space, the beginning of the next block, and the end of the next block, and then doing copies of whole ranges.
 		int $i, $r;
 		for ($i = 0, $r = 0; $i < $arr.length; $i++) {
 			if ($arr[$i] == null) continue;
 			$arr[$r] = $arr[$i];
 			$r++;
 		}
 		String[] $newarr = new String[$r];
 		System.arraycopy($arr, 0, $newarr, 0, $r);
 		return $newarr;
 	}
 	
 	
 
 	public static final byte[] copy(byte[] $a) {
 		byte[] $v = new byte[$a.length];
 		System.arraycopy($a, 0, $v, 0, $a.length);
 		return $v;
 	}
 	
 	public static final char[] copy(char[] $a) {
 		char[] $v = new char[$a.length];
 		System.arraycopy($a, 0, $v, 0, $a.length);
 		return $v;
 	}
 	
 	public static final double[] copy(double[] $a) {
 		double[] $v = new double[$a.length];
 		System.arraycopy($a, 0, $v, 0, $a.length);
 		return $v;
 	}
 	
 	public static final float[] copy(float[] $a) {
 		float[] $v = new float[$a.length];
 		System.arraycopy($a, 0, $v, 0, $a.length);
 		return $v;
 	}
 	
 	public static final int[] copy(int[] $a) {
 		int[] $v = new int[$a.length];
 		System.arraycopy($a, 0, $v, 0, $a.length);
 		return $v;
 	}
 	
 	public static final long[] copy(long[] $a) {
 		long[] $v = new long[$a.length];
 		System.arraycopy($a, 0, $v, 0, $a.length);
 		return $v;
 	}
 	
	// THIS JUST ISN'T POSSIBLE.
//	public static final <T> T[] copy(T[] $a) {
//		T[] $v = (T[]) new Object[$a.length];
//		// also full of fail:	T[] $v = (T[]) Array.newInstance($a.getClass(), $a.length);
//		System.arraycopy($a, 0, $v, 0, $a.length);
//		return $v;
//	}
	
	public static final <T> T[] copy(T[] $a, Class<T> $k) {
		@SuppressWarnings("unchecked")
		T[] $v = (T[]) Array.newInstance($k, $a.length);
 		System.arraycopy($a, 0, $v, 0, $a.length);
 		return $v;
		// this is extremely similiar to just return Arrays.copyOf($a, $a.length, $k); in 1.6, but this doesn't take the Class[] argument, which i expect may sometimes be a significant convenience.
 	}
 	
 	public static final short[] copy(short[] $a) {
 		short[] $v = new short[$a.length];
 		System.arraycopy($a, 0, $v, 0, $a.length);
 		return $v;
 	}
 	
 	
 	
 	public static final byte[] copy(byte[] $a, int $start, int $length) {
 		byte[] $v = new byte[$length];
 		System.arraycopy($a, $start, $v, 0, $length);
 		return $v;
 	}
 	
 	public static final byte[] copyOfRange(byte[] $a, int $start, int $finish) {
 		byte[] $v = new byte[$finish - $start];
 		System.arraycopy($a, $start, $v, 0, $finish - $start);
 		return $v;
 	}
 	
 	public static final char[] copyOfRange(char[] $a, int $start, int $finish) {
 		char[] $v = new char[$finish - $start];
 		System.arraycopy($a, $start, $v, 0, $finish - $start);
 		return $v;
 	}
 	
 	public static final double[] copyOfRange(double[] $a, int $start, int $finish) {
 		double[] $v = new double[$finish - $start];
 		System.arraycopy($a, $start, $v, 0, $finish - $start);
 		return $v;
 	}
 	
 	public static final float[] copyOfRange(float[] $a, int $start, int $finish) {
 		float[] $v = new float[$finish - $start];
 		System.arraycopy($a, $start, $v, 0, $finish - $start);
 		return $v;
 	}
 	
 	public static final int[] copyOfRange(int[] $a, int $start, int $finish) {
 		int[] $v = new int[$finish - $start];
 		System.arraycopy($a, $start, $v, 0, $finish - $start);
 		return $v;
 	}
 	
 	public static final long[] copyOfRange(long[] $a, int $start, int $finish) {
 		long[] $v = new long[$finish - $start];
 		System.arraycopy($a, $start, $v, 0, $finish - $start);
 		return $v;
 	}
 	
 	//	@SuppressWarnings("unchecked")
 	//	public static final <T> T[] copyOfRange(T[] $a, int $start, int $finish) {
 	//		T[] $v = (T[]) new Object[$finish-$start];
 	//		System.arraycopy($a, $start, $v, 0, $finish-$start);
 	//		return $v;
 	//	}
 	public static final Object[] copyOfRange(Object[] $a, int $start, int $finish) {
 		Object[] $v = new Object[$finish - $start];
 		System.arraycopy($a, $start, $v, 0, $finish - $start);
 		return $v;
 	}
 	
 	public static final String[] copyOfRange(String[] $a, int $start, int $finish) {
 		String[] $v = new String[$finish - $start];
 		System.arraycopy($a, $start, $v, 0, $finish - $start);
 		return $v;
 	}
 	
 	public static final short[] copyOfRange(short[] $a, int $start, int $finish) {
 		short[] $v = new short[$finish - $start];
 		System.arraycopy($a, $start, $v, 0, $finish - $start);
 		return $v;
 	}
 	
 	
 	public static final byte[] copyToEnd(byte[] $a, int $start) {
 		return copyOfRange($a, $start, $a.length);
 	}
 	
 	// a.k.a. trunk(byte[], int)?
 	public static final byte[] copyFromBeginning(byte[] $a, int $length) {
 		byte[] $v = new byte[$length];
 		System.arraycopy($a, 0, $v, 0, $length);
 		return $v;
 	}
 	
 	public static final char[] copyToEnd(char[] $a, int $start) {
 		return copyOfRange($a, $start, $a.length);
 	}
 	
 	public static final double[] copyToEnd(double[] $a, int $start) {
 		return copyOfRange($a, $start, $a.length);
 	}
 	
 	public static final float[] copyToEnd(float[] $a, int $start) {
 		return copyOfRange($a, $start, $a.length);
 	}
 	
 	public static final int[] copyToEnd(int[] $a, int $start) {
 		return copyOfRange($a, $start, $a.length);
 	}
 	
 	public static final long[] copyToEnd(long[] $a, int $start) {
 		return copyOfRange($a, $start, $a.length);
 	}
 	
 	//	public static final <T> T[] copyToEnd(T[] $a, int $start) {
 	//		return (T[]) copyOfRange($a,$start,$a.length);
 	//	}
 	public static final Object[] copyToEnd(Object[] $a, int $start) {
 		return copyOfRange($a, $start, $a.length);
 	}
 	
 	public static final String[] copyToEnd(String[] $a, int $start) {
 		return copyOfRange($a, $start, $a.length);
 	}
 	
 	public static final short[] copyToEnd(short[] $a, int $start) {
 		return copyOfRange($a, $start, $a.length);
 	}
 	
 	//MAKEMORE
 	public static int compare(byte[] $a, byte[] $b) {
 		return ByteBuffer.wrap($a).compareTo(ByteBuffer.wrap($b));	// a one liner... but makes much garbage.
 	}
 	
 	//MAKEMORE
 	public static final String flatten(Object[] $r) {
 		return flatten($r, " ");
 	}
 	
 	//MAKEMORE
 	public static final String flatten(Object[] $r, String $glue) {
 		StringBuilder $sb = new StringBuilder();
 		for (Object $s : $r)
 			$sb.append($s.toString()).append($glue);
 		return $sb.toString();
 	}
 	
 	//MAKEMORE
 	public static final byte[] cat(byte[]... $bs) {
 		int $len = 0;
 		for (byte[] $b : $bs)
 			$len += $b.length;
 		byte[] $d = new byte[$len];
 		int $i = 0;
 		for (byte[] $b : $bs) {
 			System.arraycopy($b, 0, $d, $i, $b.length);
 			$i += $b.length;
 		}
 		return $d;
 	}
 	
 	public static final float[] cat(float[]... $bs) {
 		int $len = 0;
 		for (float[] $b : $bs)
 			$len += $b.length;
 		float[] $d = new float[$len];
 		int $i = 0;
 		for (float[] $b : $bs) {
 			System.arraycopy($b, 0, $d, $i, $b.length);
 			$i += $b.length;
 		}
 		return $d;
 	}
 	
 	// surprisingly, if you try to just name this method "asList", it compiles, but you can never actually use it without a complaint about ambiguity.
 	public static final <T> List<T> asTypedList(Class<T> $klass, T... $a) {
 		return java.util.Arrays.asList($a);
 	}
 
 	////////////////////////////////////////////////////////////////
 	//////// FUCKERS:
 	////////////////////////////////////////////////////////////////
 	
 	public static final <T> List<T> asList(T... $a) {
 		return java.util.Arrays.asList($a);
 	}
 	
 	public static final int binarySearch(byte[] $a, byte $key) {
 		return java.util.Arrays.binarySearch($a, $key);
 	}
 	
 	public static final int binarySearch(char[] $a, char $key) {
 		return java.util.Arrays.binarySearch($a, $key);
 	}
 	
 	public static final int binarySearch(double[] $a, double $key) {
 		return java.util.Arrays.binarySearch($a, $key);
 	}
 	
 	public static final int binarySearch(float[] $a, float $key) {
 		return java.util.Arrays.binarySearch($a, $key);
 	}
 	
 	public static final int binarySearch(int[] $a, int $key) {
 		return java.util.Arrays.binarySearch($a, $key);
 	}
 	
 	public static final int binarySearch(long[] $a, long $key) {
 		return java.util.Arrays.binarySearch($a, $key);
 	}
 	
 	public static final int binarySearch(Object[] $a, Object $key) {
 		return java.util.Arrays.binarySearch($a, $key);
 	}
 	
 	public static final int binarySearch(short[] $a, short $key) {
 		return java.util.Arrays.binarySearch($a, $key);
 	}
 	
 	public static final <T> int binarySearch(T[] $a, T $key, Comparator<? super T> $c) {
 		return java.util.Arrays.binarySearch($a, $key, $c);
 	}
 	
 	public static final boolean deepEquals(Object[] $a1, Object[] $a2) {
 		return java.util.Arrays.deepEquals($a1, $a2);
 	}
 	
 	public static final int deepHashCode(Object[] $a) {
 		return java.util.Arrays.deepHashCode($a);
 	}
 	
 	public static final String deepToString(Object[] $a) {
 		return java.util.Arrays.deepToString($a);
 	}
 	
 	public static final boolean equals(boolean[] $a, boolean[] $a2) {
 		return java.util.Arrays.equals($a, $a2);
 	}
 	
 	public static final boolean equals(byte[] $a, byte[] $a2) {
 		return java.util.Arrays.equals($a, $a2);
 	}
 	
 	public static final boolean equals(char[] $a, char[] $a2) {
 		return java.util.Arrays.equals($a, $a2);
 	}
 	
 	public static final boolean equals(double[] $a, double[] $a2) {
 		return java.util.Arrays.equals($a, $a2);
 	}
 	
 	public static final boolean equals(float[] $a, float[] $a2) {
 		return java.util.Arrays.equals($a, $a2);
 	}
 	
 	public static final boolean equals(int[] $a, int[] $a2) {
 		return java.util.Arrays.equals($a, $a2);
 	}
 	
 	public static final boolean equals(long[] $a, long[] $a2) {
 		return java.util.Arrays.equals($a, $a2);
 	}
 	
 	public static final boolean equals(Object[] $a, Object[] $a2) {
 		return java.util.Arrays.equals($a, $a2);
 	}
 	
 	public static final boolean equals(short[] $a, short[] $a2) {
 		return java.util.Arrays.equals($a, $a2);
 	}
 	
 	public static final void fill(boolean[] $a, boolean $val) {
 		java.util.Arrays.fill($a, $val);
 	}
 	
 	public static final void fill(boolean[] $a, int $fromIndex, int $toIndex, boolean $val) {
 		java.util.Arrays.fill($a, $fromIndex, $toIndex, $val);
 	}
 	
 	public static final void fill(byte[] $a, byte $val) {
 		java.util.Arrays.fill($a, $val);
 	}
 	
 	public static final void fill(byte[] $a, int $fromIndex, int $toIndex, byte $val) {
 		java.util.Arrays.fill($a, $fromIndex, $toIndex, $val);
 	}
 	
 	public static final void fill(char[] $a, char $val) {
 		java.util.Arrays.fill($a, $val);
 	}
 	
 	public static final void fill(char[] $a, int $fromIndex, int $toIndex, char $val) {
 		java.util.Arrays.fill($a, $fromIndex, $toIndex, $val);
 	}
 	
 	public static final void fill(double[] $a, double $val) {
 		java.util.Arrays.fill($a, $val);
 	}
 	
 	public static final void fill(double[] $a, int $fromIndex, int $toIndex, double $val) {
 		java.util.Arrays.fill($a, $fromIndex, $toIndex, $val);
 	}
 	
 	public static final void fill(float[] $a, float $val) {
 		java.util.Arrays.fill($a, $val);
 	}
 	
 	public static final void fill(float[] $a, int $fromIndex, int $toIndex, float $val) {
 		java.util.Arrays.fill($a, $fromIndex, $toIndex, $val);
 	}
 	
 	public static final void fill(int[] $a, int $fromIndex, int $toIndex, int $val) {
 		java.util.Arrays.fill($a, $fromIndex, $toIndex, $val);
 	}
 	
 	public static final void fill(int[] $a, int $val) {
 		java.util.Arrays.fill($a, $val);
 	}
 	
 	public static final void fill(long[] $a, int $fromIndex, int $toIndex, long $val) {
 		java.util.Arrays.fill($a, $fromIndex, $toIndex, $val);
 	}
 	
 	public static final void fill(long[] $a, long $val) {
 		java.util.Arrays.fill($a, $val);
 	}
 	
 	public static final void fill(Object[] $a, int $fromIndex, int $toIndex, Object $val) {
 		java.util.Arrays.fill($a, $fromIndex, $toIndex, $val);
 	}
 	
 	public static final void fill(Object[] $a, Object $val) {
 		java.util.Arrays.fill($a, $val);
 	}
 	
 	public static final void fill(short[] $a, int $fromIndex, int $toIndex, short $val) {
 		java.util.Arrays.fill($a, $fromIndex, $toIndex, $val);
 	}
 	
 	public static final void fill(short[] $a, short $val) {
 		java.util.Arrays.fill($a, $val);
 	}
 	
 	public static final int hashCode(boolean[] $a) {
 		return java.util.Arrays.hashCode($a);
 	}
 	
 	public static final int hashCode(byte[] $a) {
 		return java.util.Arrays.hashCode($a);
 	}
 	
 	public static final int hashCode(char[] $a) {
 		return java.util.Arrays.hashCode($a);
 	}
 	
 	public static final int hashCode(double[] $a) {
 		return java.util.Arrays.hashCode($a);
 	}
 	
 	public static final int hashCode(float[] $a) {
 		return java.util.Arrays.hashCode($a);
 	}
 	
 	public static final int hashCode(int[] $a) {
 		return java.util.Arrays.hashCode($a);
 	}
 	
 	public static final int hashCode(long[] $a) {
 		return java.util.Arrays.hashCode($a);
 	}
 	
 	public static final int hashCode(Object[] $a) {
 		return java.util.Arrays.hashCode($a);
 	}
 	
 	public static final int hashCode(short[] $a) {
 		return java.util.Arrays.hashCode($a);
 	}
 	
 	public static final void sort(byte[] $a, int $fromIndex, int $toIndex) {
 		java.util.Arrays.sort($a, $fromIndex, $toIndex);
 	}
 	
 	public static final void sort(byte[] $a) {
 		java.util.Arrays.sort($a);
 	}
 	
 	public static final void sort(char[] $a, int $fromIndex, int $toIndex) {
 		java.util.Arrays.sort($a, $fromIndex, $toIndex);
 	}
 	
 	public static final void sort(char[] $a) {
 		java.util.Arrays.sort($a);
 	}
 	
 	public static final void sort(double[] $a, int $fromIndex, int $toIndex) {
 		java.util.Arrays.sort($a, $fromIndex, $toIndex);
 	}
 	
 	public static final void sort(double[] $a) {
 		java.util.Arrays.sort($a);
 	}
 	
 	public static final void sort(float[] $a, int $fromIndex, int $toIndex) {
 		java.util.Arrays.sort($a, $fromIndex, $toIndex);
 	}
 	
 	public static final void sort(float[] $a) {
 		java.util.Arrays.sort($a);
 	}
 	
 	public static final void sort(int[] $a, int $fromIndex, int $toIndex) {
 		java.util.Arrays.sort($a, $fromIndex, $toIndex);
 	}
 	
 	public static final void sort(int[] $a) {
 		java.util.Arrays.sort($a);
 	}
 	
 	public static final void sort(long[] $a, int $fromIndex, int $toIndex) {
 		java.util.Arrays.sort($a, $fromIndex, $toIndex);
 	}
 	
 	public static final void sort(long[] $a) {
 		java.util.Arrays.sort($a);
 	}
 	
 	public static final void sort(Object[] $a, int $fromIndex, int $toIndex) {
 		java.util.Arrays.sort($a, $fromIndex, $toIndex);
 	}
 	
 	public static final void sort(Object[] $a) {
 		java.util.Arrays.sort($a);
 	}
 	
 	public static final void sort(short[] $a, int $fromIndex, int $toIndex) {
 		java.util.Arrays.sort($a, $fromIndex, $toIndex);
 	}
 	
 	public static final void sort(short[] $a) {
 		java.util.Arrays.sort($a);
 	}
 	
 	public static final <T> void sort(T[] $a, Comparator<? super T> $c) {
 		java.util.Arrays.sort($a, $c);
 	}
 	
 	public static final <T> void sort(T[] $a, int $fromIndex, int $toIndex, Comparator<? super T> $c) {
 		java.util.Arrays.sort($a, $fromIndex, $toIndex, $c);
 	}
 	
 	public static final String toString(boolean[] $a) {
 		return java.util.Arrays.toString($a);
 	}
 	
 	public static final String toString(byte[] $a) {
 		return java.util.Arrays.toString($a);
 	}
 	
 	public static final String toString(char[] $a) {
 		return java.util.Arrays.toString($a);
 	}
 	
 	public static final String toString(double[] $a) {
 		return java.util.Arrays.toString($a);
 	}
 	
 	public static final String toString(float[] $a) {
 		return java.util.Arrays.toString($a);
 	}
 	
 	public static final String toString(int[] $a) {
 		return java.util.Arrays.toString($a);
 	}
 	
 	public static final String toString(long[] $a) {
 		return java.util.Arrays.toString($a);
 	}
 	
 	public static final String toString(Object[] $a) {
 		return java.util.Arrays.toString($a);
 	}
 	
 	public static final String toString(short[] $a) {
 		return java.util.Arrays.toString($a);
 	}
 }
