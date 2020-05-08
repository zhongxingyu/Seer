 import java.util.ArrayList;
 
 public class PrimeNumbers {
	public static ArrayList<Integer> generatePrimes(int n) {
 		ArrayList<Integer> ret = new ArrayList<Integer>();

		for (int canidate = 2; n > 1; canidate++) {
			while (n % canidate == 0) {
				ret.add(canidate);
				n /= canidate;
			}
 		}

		if (n > 1) {
 			ret.add(n);
 		}

 		return ret;
 	}
 }
