 package algorithms;
 
 import programming.contract.*;
 
 public class ArrayAlgorithms {
 
	public static void fill(int [] a, int startValue, int incrementValue) throws PreconditionError, InterruptedException {
 		PreconditionError.Test(a == null || a.length < 1, "The array must have at least one element.");
 		
 		a[0] = startValue;
 		DisplayTool.instance().DisplayHistogram(a, 1000);
 		
 		for(int i = 1; i < a.length; ++i) {
 			a[i] = a[i - 1] + incrementValue;
 			DisplayTool.instance().DisplayHistogram(a, 1000);
 		}
 	}
 	
	public static void swap(int [] a, int i, int j) throws PreconditionError {
 		PreconditionError.Test(a == null || a.length < 1, "The array must have at least one element.");
 		
 		int tempI = a[i];
 		a[i] = a[j];
 		a[j] = tempI;
 	}
 	
 	public static void reverse(int [] a) throws PreconditionError, InterruptedException {
 		if (a == null || a.length < 2)
 			return;
 		
 		for(int left = 0, right = a.length - 1; left < right; ++left, --right) {
			swap(a, left, right);
 			DisplayTool.instance().DisplayHistogram(a, 1000);
 		}
 	}
 	public static int findMin(int [] a) {
 		int min = 0x7FFFFFFF;
 		
 		for( int i = 0; i < a.length; ++i) {
 			if(a[i] < min)
 				min = a[i];
 		}
 		
 		return min;
 	}
 
 	public static int findMax(int [] a) {
 		int max = 0xFFFFFFFF;
 		
 		for( int i = 0; i < a.length; ++i) {
 			if(a[i] > max)
 				max = a[i];
 		}
 		
 		return max;
 	}
 
 }
