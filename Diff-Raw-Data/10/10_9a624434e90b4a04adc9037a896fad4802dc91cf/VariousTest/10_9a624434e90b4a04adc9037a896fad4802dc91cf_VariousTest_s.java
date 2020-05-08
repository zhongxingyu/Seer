 package test;
 
 import fundamentals.Various;
 import junit.framework.TestCase;
 
 
 public class VariousTest extends TestCase {
 
 	public void testFib() {
 		assertTrue(Various.fib(0) == 0);
 		assertTrue(Various.fib(1) == 1);
 		assertTrue(Various.fib(2) == 2);
 		assertTrue(Various.fib(3) == 3);
 		assertTrue(Various.fib(4) == 5);
 		assertTrue(Various.fib(5) == 8);
 		assertTrue(Various.fib(6) == 13);
 		assertTrue(Various.fib(7) == 21);
 	}
 	
 	public void testFindBreakingHeight(){
 		int [] answer = Various.findBreakingHeight(100, Various.makeBulb(13));
 		assertTrue(answer[0] == 13);
 		assertTrue(answer[1] == 14);
 	}
 	
 	public void testSieveOfEratosthenes(){
 		boolean [] primes = Various.sieveOfEratosthenes(10);
		assertTrue(primes.length == 5);
 	}
 
 }
