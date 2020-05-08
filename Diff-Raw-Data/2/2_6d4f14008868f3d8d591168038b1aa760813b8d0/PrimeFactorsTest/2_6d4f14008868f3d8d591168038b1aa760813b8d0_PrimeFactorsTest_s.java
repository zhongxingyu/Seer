 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 
 public class PrimeFactorsTest {
 	@Test
 	public void testJUnit() {
 		assertTrue("The computer isn't feeling well today...", true);
 	}
 
 	@Test
 	public void testOne() {
 		assertEquals(new ArrayList<Integer>(), PrimeNumbers.generatePrimes(1));
 	}
 	
 	@Test
 	public void testTwo() {
		assertEquals(list(3), PrimeNumbers.generatePrimes(2));
 	}
 	
 	private ArrayList<Integer> list(int... ints) {
 		ArrayList<Integer> ret = new ArrayList<Integer>();
 		for (int i : ints)
 			ret.add(i);
 		return ret;
 	}
 }
