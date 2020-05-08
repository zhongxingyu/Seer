import java.util.Arrays;

 public class MiningNobel 
 {
     private static double[] test = { 0.11, 0.22, 0.33, -0.66 };
 
 	// Exhaustive search approach
     public static int exhaustiveCount(double[] ds) {
 		int n = ds.length;
         int count = 0;
         for (int i = 0; i < n; ++i) {
             for (int j = i + 1; j < n; ++j) {
                 for (int k = j + 1; k < n; ++k) {
                     for (int l = k + 1; l < n; l++) {
                         if (ds[i] + ds[j] + ds[k] + ds[l] == 0) { count++; }
                     }
                 }
             }
         }
         return count;
     }

	// ~ N^3 approach
	public static int fastCount(double[] ds) {
		int n = ds.length;
		Arrays.sort(ds);
		int count = 0;
		for (int i = 0; i < n; i++) {
	    	for (int j = i + 1; j < n; j++) {
				for (int k = j + 1; k < n; k++) {
					int l = Arrays.binarySearch(ds, -(ds[i] + ds[j] + ds[k]));
	        		if (l > k) count++;
				}
			}
		}
		return count;
	}
    
 	// main
     public static void main(String[] args) {
 	
         StdOut.println(exhaustiveCount(test));
		StdOut.println(fastCount(test));
 
 		In in = new In("constants.csv");
 		while (!in.isEmpty()) {
 			String s  = in.readLine();
 			String sub = s.substring(s.indexOf(" , ") + 3);
 			double d = Double.parseDouble(sub);
 		}
 		in.close();
     }
     
 	
 }
