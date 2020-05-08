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
 }
