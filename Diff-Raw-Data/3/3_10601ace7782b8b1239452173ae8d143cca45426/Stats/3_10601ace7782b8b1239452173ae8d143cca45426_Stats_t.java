 public class Stats {
 	public static void main(String[] args) {
 
 		//Max
 		//Min
 		//Mean
 		//Median
 		//Q1
 		//Q3
 		//mode
 		//standard deviation
 
 
 		int[] a = {9,2,5,4,12,7,8,11,9,3,7,4,12,5,4,10,9,6,9};
 
 		System.out.println(max(a));
 		System.out.println(min(a));
 		System.out.println(mean(a));
 		System.out.println(median(a));
 		System.out.println(quartile1(a));
 		System.out.println(quartile3(a));
 		System.out.println(mode(a));
 		System.out.println(standardDeviation(a));
 
 	}
 
 
 	public static int max(int[] a) {
 		int max = a[0]; 
 		for (int i=0; i<a.length; i++) {
 			if (a[i]>max) {
 				max = a[i];
 			}
 
 		}
 		return max;
 	}
 
 	public static int min(int[] a) {
 		int min = a[0];
 
 	for (int i=0; i<a.length; i++) {
 			if (a[i]<min) {
 				min=a[i];
 			}
 		}
 		return min;
 	}
 
 	public static double mean(int[] a) {
 		double mean =0;
 		int sum = 0;
 		for (int i=0; i<a.length; i++) {
 			sum =sum + a[i];
 		}
		mean = sum/(a.length);
 		return mean;
 	}
 
 	// 1,2,3,4
 
 	public static double median(int[] a) {
 		double median = 0;
 		int mid = a.length/2;
 		if (a.length % 2 ==0) {
 			median = (a[mid-1] + a[mid]) / 2.0;
 		} else {
 			median = a[mid];
 		}
 		return median;
 
 	}
 
 	public static double quartile1(int[] a) {
 		double length = a.length;
 		double quarter = length/4;
 		int placement = (int)quarter;
 		double number=0.0;
 		double sum = a[placement] + a[placement-1];
 		if (length % 4 !=0) {
 			number = a[placement];
 		} else {
 			number = sum/2;
 		}
 		return number;
 	}
 
 	public static double quartile3(int[] a) {
 		double length = a.length;
 		double quarter = length/4;
 		double multiply = quarter * 3;
 		int placement = (int)multiply;
 		double number=0.0;
 		double sum = a[placement] + a[placement-1];
 		if (length % 4 != 0) {
 			number = a[placement];
 		} else {
 			number = sum/2;
 		}
 		return number;
 	}
 
 	public static int mode(int[] a) {
 		int temporary, counter, number;
 		counter=0;
 		number=0;
 		for (int i=0; i<a.length; i++) {
 			temporary=0;
 			for (int k=0; k<a.length; k++) {
 				if(a[i]==a[k]) {
 					temporary++;
 					if(counter<temporary) {
 						counter=temporary;
 						number=a[i];
 					}
 				}
 			}
 		}
 		return number;
 	}
 
 	public static double standardDeviation(int[] a) {
 		double mean = mean(a);
 		double sum = 0;
 		double variance =0;
 		for (int i=0; i<a.length; i++) {
 			double diff = mean - a[i];
 			double square = diff * diff;
 			sum += square;
 		}
 		variance = sum/(a.length-1);
 		double sd = Math.sqrt(variance);
 		return sd;
 	}
 }
