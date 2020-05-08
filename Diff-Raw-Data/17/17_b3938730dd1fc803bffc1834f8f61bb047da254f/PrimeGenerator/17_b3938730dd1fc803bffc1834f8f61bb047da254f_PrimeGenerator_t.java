 import java.util.ArrayList;
 
 public class PrimeGenerator {
 
 	public static ArrayList<Integer> generatePrimes(int i) {
 		if (i <= 2) {
 			return new ArrayList<Integer>();
 		}
 		else {
 			ArrayList<Integer> temp = new ArrayList<Integer>();
 			for (int j = 2; j < i; j++){
 				if (j == 2 || j == 3){
 					temp.add(j);
 				}
				if (j % 2 != 0 && j % 3 != 0){
					temp.add(j);
				}
 			}
 			return temp;
 		}
 	}
 
 }
