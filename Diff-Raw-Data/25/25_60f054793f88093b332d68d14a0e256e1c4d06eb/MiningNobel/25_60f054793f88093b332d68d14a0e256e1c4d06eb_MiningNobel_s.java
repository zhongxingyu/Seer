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
    
 	// main
     public static void main(String[] args) {
 	
         StdOut.println(exhaustiveCount(test));
 
		double[] ds = readDoublesBADS("constants.csv", " , ");

		ArrayList alist = new ArrayList();
 		In in = new In("constants.csv");
 		while (!in.isEmpty()) {
 			String s  = in.readLine();
 			String sub = s.substring(s.indexOf(" , ") + 3);
 			double d = Double.parseDouble(sub);
 		}
 		in.close();
     }
     
 	
 }
