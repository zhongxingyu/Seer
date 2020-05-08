 import java.util.ArrayList;
 
 
 public class PrimeNumbers {
 	public static ArrayList<Integer> generatePrimes(int n){
 		ArrayList<Integer> ret = new ArrayList<Integer>();
 		
 		if(n != 1){
			ret.add(n);
 		}
 		
 		return ret;
 	}
 }
