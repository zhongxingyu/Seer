 package test.Math.GreatestCommonDivisor;
 
import main.Math.GreatestCommonDivisior.GCD;
 
 public class TestGCD {
 	public static boolean success = true;
 	
 	public static void main(String[] args) {
 		testOne();
 		
 		if (!success) {
 			System.out.println("FAILED: TestGCD");
 			System.exit(1);
 		}
 		else {
 			System.exit(0);
 		}
 	}
 
 	public static void testOne() {
 		if (GCD.EuclidGCD(867, 1989) != 51) {
 			success = false;
 		}
 		
 		if (GCD.EuclidGCD(54, 24) != 6) {
 			success = false;
 		}
 		
 		if (GCD.EuclidGCD(4, 2) != 2) {
 			success = false;
 		}
 		
 		if (GCD.EuclidGCD(1234567, 9876) != 1) {
 			success = false;
 		}
 		
 		if (GCD.EuclidGCD(987654321, 234657) != 9) {
 			success = false;
 		}
 		
 		if (GCD.EuclidGCD(234657, 987654321) != 9) {
 			success = false;
 		}
 		
 		if (GCD.EuclidGCD(987654321, 234657) != 9) {
 			success = false;
 		}
 		
 		if (GCD.EuclidGCD(Long.MAX_VALUE, Long.MAX_VALUE) != Long.MAX_VALUE) {
 			success = false;
 		}
 	}
 }
