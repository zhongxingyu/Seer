 package test.Math.PrimalityTest;
 import java.util.*;
 import main.Math.PrimalityTest.NaivePrimeTest;
 
 public class TestNaivePrimeTest
 {
 	public static boolean success = true;
 	public static void main(String[] args)
 	{
 		testOne(true);
 		if(!success)
 		{
 			System.out.println("FAILED: TestNaivePrimeTest");
 			System.exit(1);
 		}
 		else
 		{
 			System.exit(0);
 		}
 	}
 
 	public static void testOne(boolean printDebug)
 	{
 		NaivePrimeTest npt = new NaivePrimeTest();
 
		int[] primes = {5, 13, 19, 2003, 2011, 200003, 500009, 590000003, 79000000063};
		int[] notPrimes = {6, 14, 21, 2005, 2013, 200005, 5000011, 590000005,790000000067 , 9223372036854775807};
 
 		for (int i = 0; i < primes.length; ++i) {
			if (!npt.primalityTest(primes[i]) {
 				success = false;
 				return;
 			}
 		}
 
 		for (int i = 0; i < notPrimes.length; ++i) {
			if (npt.primalityTest(notPrimes[i]) {
 				success = false;
 				return;
 			}
 		}
 	}
 }
