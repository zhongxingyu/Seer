 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 public class PrimeGeneratorTest {
 
 	@Test
 	public void testOne() {
 		assertEquals(list(), PrimeNumbers.primeNumberGen(1));
 	}
 	
 	@Test
 	public void testTwo() {
		assertEquals(list(2), PrimeNumbers.primeNumberGen(2));
 	}
 
 	private ArrayList<Integer> list(int... ints) {
 		ArrayList<Integer> ret = new ArrayList<Integer>();
 
 		for (int i : ints) {
 			ret.add(i);
 		}
 
 		return ret;
 	}
 }
