 import java.util.Random;
 import java.util.Arrays;
 
 class Stats {
 	
 	public static void main(String[] args) {
 		Random n = new Random();
 		//int [] a = {9, 2, 5, 4, 12, 7, 8, 11, 9, 3, 7, 4, 12, 5, 4, 10, 9, 6, 9, 4};
		int [] a = new int [3/*3 + (int)(Math.random()*7)*/];
 		for (int i = 0; i < a.length; i++) {
 			a [i] = -50 + (int)(Math.random()*100); 
 		}
 		int[] as = a;
 		Arrays.sort(as);
 		System.out.println("Original Array\n");
 		printArray(a);
		System.out.println(1%2);
 		System.out.println();
 		System.out.println("Max\n");
 		System.out.println(max(a));
 		System.out.println("Min\n");
 		System.out.println(min(a));
 		System.out.println("Mean\n");
 		System.out.println(mean(a));
 		System.out.println("Sorted Array\n");
 		printArray(as);
 		System.out.println("Median\n");
 		System.out.println(median(as));
 		System.out.println("Lower Quartile\n");
 		System.out.println(quartile1(as));
 		System.out.println("Upper Quartile\n");
 		System.out.println(quartile3(as));
 		System.out.println("Mode\n");
 		System.out.println(mode(a));
 		System.out.println("Standard Deviation\n");
 		System.out.println(standardDeviation(a));
 	// 	printArray(a);
 	}
 
 	// public static int[] sort(int[] a) {
 	// 	int [] b = new int [a.length];
 	// 	int max = 0;
 	// 	for (int j = 0; j < a.length; j++) {
 	// 		for (int i = 0; i < a.length; i++) {
 	// 			if (j == 0) {
 	// 				b[0] = min(a);
 	// 				max = b[0];
 	// 			} else {
 	// 				if (b[j-1] < a[j] && a[j] < max) {
 	// 					b[j] = a[j];
 	// 				}
 	// 			}
 	// 		}
 	// 	}
 	// 	return b;			
 	// }
 
 	public static void printArray(int[] a) {
 		for (int i = 0; i < a.length; i++) {
 			System.out.print(a[i] + " ");
 		}
 		System.out.println();
 	}
 
 	public static int max(int[] a) {
 		int max = a[0];
 		for (int i = 0; i< a.length; i++) {
 			if (a[i] > max) {
 				max = a[i];
 			}
 		}
 		return max;
 	}
 
 	public static int min(int[] a) {
 		int min = a[0];
 		for (int i = 0; i < a.length; i++) {
 			if (a[i] < min) {
 				min = a[i];
 			}
 		}
 		return min;
 	}
 
 	public static double mean(int[] a) {
 		double mean = 0.0;
 		for (int i = 0; i < a.length; i++) {
 			mean = mean + a[i];
 		}
 		return (mean / (a.length));
 	}
 
 	public static double median(int[] a) {
		/*double median = 0.0;
 		if (a.length % 2 == 0) {
 			median = (((double)a[a.length/2] + (double)a[a.length/2 - 1]) / 2.0);
 		} else {
 			median = (a[a.length/2]);
 		}
		return median;*/
 	}
 
 	public static double quartile1(int[] a) {
 		double median = 0.0;
 		int oddLength = a.length + 1;
 		if (a.length % 2 == 0) {
 			if ((a.length/2) % 2 == 0) {
 				median = ((double)a[a.length/4] + (double)a[(a.length/4 - 1)]) / 2.0;
 			} else {
 				median = a[(oddLength/4)];
 			}
 		} else {
 			if ((oddLength/2) % 2 == 0) {
				median = ((double)a[oddLength/4 - 1]/* + (double)a[(oddLength/4)]*/)/* / 2.0*/;
 			} else {
 				median = (a[((oddLength - 2)/4 - 1)] + a[(oddLength - 2)/4]) / 2.0;
 			}
 		}
 		return median;
 	}
 
 	public static double quartile3(int[] a) {
 		double median = 0.0;
 		int oddLength = a.length - 1;
 		if (a.length % 2 == 0) {
 			if ((a.length/2) % 2 == 0) {
 				median = ((double)a[a.length/4 + (a.length/2)] + (double)a[(a.length/4 - 1) + (a.length/2)]) / 2.0;
 			} else {
 				median = a[(oddLength/4) + (int)(oddLength/2 + 1)];
 			}
 		} else {
 			if ((oddLength/2) % 2 == 0) {
 				median = ((double)a[oddLength/4 + (oddLength/2)] + (double)a[(oddLength/4 + 1) + (oddLength/2)]) / 2.0;
 			} else {
				median = a[((oddLength - 2)/4 + ((a.length + 1) / 2))];
				//median = (a[(oddLength/2) + (int)(oddLength/2 + 1)]) / 2.0;
 			}
 		}
 		return median;
 	}
 
 	public static int mode(int[] a) {
 		int mode = a[0];
 		int countMode = 1;
 		int[] count = new int [a.length];
 		for (int i = 0; i < a.length; i++) {
 			count [i] = 0;
 		}
 		for (int i = 0; i < a.length; i++) {
 			for (int j = 0; j < a.length; j++) {
 				if (a[i] == a[j]) {
 					count [i] += 1;
 				}
 			}
 		}
 		for (int i = 0; i < a.length; i++) {
 			if (count[i] > countMode) {
 				mode = a[i];
 				countMode = count[i];
 			}
 		}
 		return mode;
 	}
 
 	public static double standardDeviation(int[] a) {
 		int u = 0;
 		int [] m = new int [a.length];
 		int sum = 0;
 		double standardDeviation = 0;
 		for (int i = 0; i < a.length; i++) {
 			u += a[i];
 		}
 		u /= a.length;
 		for (int i = 0; i < a.length; i++) {
 			m[i] = a[i] - u;
 			m[i] *= m[i];
 			sum += m[i];
 
 		}
 		standardDeviation = sum / ((double)a.length - 1);
 		return Math.sqrt(standardDeviation);
 
 	}
 
 }
