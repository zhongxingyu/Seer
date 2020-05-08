 package ab5_adts;
 
 import java.util.*;
 
 public class Aufgabe5_2 {
 
 	static int accessCount = 0;
 
 	// Aufgabe 5.2.1:
	// f [0..9]=[1,1,1,,6,26,66,151,361,861]
 
 	public static void main(String[] args) {
 		aufgabe5_2_3();
 		aufgabe5_2_4();
 	}
 
 	// Aufgabe 5.2.2:
 	private static int f(int n) {
 		accessCount += 1; // increase accessCount each call
 
 		if (n < 0) {
 			System.out.println("Whoops! N=" + n + " is too small!");
 			return -1;
 		}
 
 		// f (0|1|2) = 1
 		// f n = f (n-1) + (2 * f (n-2)) + (3 * f (n-3))
 		if (n < 3) {
 			return 1;
 		} else {
 			return f(n - 1) + 2 * f(n - 2) + 3 * f(n - 3);
 		}
 	}
 
 	// Aufgabe 5.2.3:
 	private static void aufgabe5_2_3() {
 		System.out.println("\nAufgabe 5.2.3:");
 
 		int prevResult = 1;
 		for (int n = 0; true; n++) {
 			int result = f(n);
 
 			// check whether the current result is
 			// smaller than the previous one for overflows
 			// checking for negative numbers isn't sufficient
 			// because the result is too big to become negative
 			if (result < prevResult) {
 				System.out.println("overflow at n=" + n);
 				break;
 			}
 			prevResult = result;
 			System.out.println("n=" + n + " works! result: " + result);
 		}
 	}
 
 	// Aufgabe 5.2.4:
 	private static void aufgabe5_2_4() {
 		System.out.println("\nAufgabe 5.2.4:");
 		int maxN = 26;
 		Map<Integer, Integer> noOfCalls = new HashMap<>();
 		for (int i = 0; i <= maxN; i++) {
 			resetAccessCount();
 			f(i);
 			noOfCalls.put(i, accessCount());
 		}
 
 		System.out.println(noOfCalls);
 	}
 //  Output:
 //	0=1,
 //	1=1,
 //	2=1,
 //	3=4,
 //	4=7,
 //	5=13,
 //	6=25,
 //	7=46,
 //	8=85,
 //	9=157,
 //	10=289,
 //	11=532,
 //	12=979,
 //	13=1801,
 //	14=3313,
 //	15=6094,
 //	7=20617,
 //	16=11209,
 //	19=69748,
 //	18=37921,
 //	21=235957,
 //	20=128287,
 //	23=798238,
 //	22=433993,
 //	25=2700421,
 //	24=1468189,
 //	26=4966849
 	
 
 	public static int accessCount() {
 		return accessCount;
 	}
 
 	public static void resetAccessCount() {
 		accessCount = 0;
 	}
 }
