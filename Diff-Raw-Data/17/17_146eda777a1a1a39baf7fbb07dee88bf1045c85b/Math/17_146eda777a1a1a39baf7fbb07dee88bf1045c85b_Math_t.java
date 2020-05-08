 package math;
 
 /**
  * A class that implements functionalities of the java.lang.Math class and adds
  * a few more functions. Also all functions are casting their results to float.
  */
 public class Math {
 	public static float E = (float) java.lang.Math.E;
 	public static float PI = (float) java.lang.Math.PI;
 
 	public static float sqrt(float a) {
 		return (float) java.lang.Math.sqrt(a);
 	}
 
 	public static float pow(float a, float b) {
 		return (float) java.lang.Math.pow(a, b);
 	}
 
 	public static float sin(float a) {
 		return (float) java.lang.Math.sin(a);
 	}
 
 	public static float cos(float a) {
 		return (float) java.lang.Math.cos(a);
 	}
 
 	public static float tan(float a) {
 		return (float) java.lang.Math.tan(a);
 	}
 
 	public static float asin(float a) {
 		return (float) java.lang.Math.asin(a);
 	}
 
 	public static float acos(float a) {
 		return (float) java.lang.Math.acos(a);
 	}
 
 	public static float atan(float a) {
 		return (float) java.lang.Math.atan(a);
 	}
 
 	public static float atan2(float y, float x) {
 		return (float) java.lang.Math.atan2(y, x);
 	}
 
 	public static float max(float a, float b) {
 		return (float) java.lang.Math.max(a, b);
 	}
 
 	public static float min(float a, float b) {
 		return (float) java.lang.Math.min(a, b);
 	}
 
 	public static float log(float a) {
 		return (float) java.lang.Math.log(a);
 	}
 
 	public static float log10(float a) {
 		return (float) java.lang.Math.log10(a);
 	}
 
 	public static float floor(float a) {
 		return (float) java.lang.Math.floor(a);
 	}
 
 	public static float ceil(float a) {
 		return (float) java.lang.Math.ceil(a);
 	}
 
 	public static float round(float a) {
 		return (float) java.lang.Math.round(a);
 	}
 
 	public static float signum(float a) {
 		return (float) java.lang.Math.signum(a);
 	}
 
 	public static float abs(float a) {
 		return (float) java.lang.Math.abs(a);
 	}
 
 	public static float exp(float a) {
 		return (float) java.lang.Math.pow(java.lang.Math.E, a);
 	}
 
	public static float random() {
		return (float) java.lang.Math.random();
	}

 	/**
 	 * If the sum of a and b is divisible by two, this function returns one,
 	 * otherwise -1;
 	 * 
 	 * @param a
 	 * @param b
 	 * @return
 	 */
 	public static int parity(int a, int b) {
 		if ((a + b) % 2 == 0)
 			return 1;
 		else
 			return -1;
 	}
 
 	/**
 	 * Returns the index of the highest absolute value in arr.
 	 * 
 	 * @param arr
 	 * @param minIndex
	 *            The minimal index to consider.
 	 * @param maxIndex
	 *            The maximal index to consider.
 	 * @return
 	 */
 	public static int argMax(float[] arr, int minIndex, int maxIndex) {
 		int result = 0;
 		float max = Integer.MIN_VALUE;
 		for (int i = minIndex; i < maxIndex; i++) {
 			if (abs(arr[i]) > max) {
 				max = abs(arr[i]);
 				result = i;
 			}
 		}
 		return result;
 	}
 }
