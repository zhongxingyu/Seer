 import java.util.ArrayList;
 
 
 public class MathUtilities {
 	//find euclidean distance between two entries
 	public static double euc_dist (Entry e1, Entry e2) throws Exception{
 		Entry.check(e1, e2); 
 		double sum = 0; 
 		for (int i = 0; i < e1.features.length; i++){
 			sum += (Math.pow(e1.features[i] - e2.features[i], 2.0)); 
 		}
 	
 		return Math.sqrt(sum); 
 	}
 	
 	public static double get_average (ArrayList<Double> lst){
 		double sum = 0; 
 		for (Double d : lst){
 			sum += d;
 		}
 		return sum/lst.size(); 
 		
 	}
 	
 	public static double get_stddev (ArrayList<Double> lst){
 		double sum = 0;
 		double average = get_average(lst); 
 		for (Double d : lst){
 			sum += (d - average) * (d-average);
 		}
 		return Math.sqrt(sum/lst.size()); 
 	}
 	
 	// Compares two doubles to see if they're equal
 	public boolean DoubleEquals (double A, double B){
 	    if (A == B)
 	        return true;
 	    
 		double maxRelativeError = 0.0001; 
 	    double relativeError;
 	    if (B > A)
 	        relativeError = (B - A) / B;
 	    else
 	        relativeError = (A - B) / A;
 	    return relativeError <= maxRelativeError;
 	}
 	
 	// Generate a random integer in range [lowRange, highRange)
 	public static int genRandomIntRange(int lowRange, int highRange) {
		return lowRange+(int)(Math.random()*(highRange-lowRange));
 	}			
 }
