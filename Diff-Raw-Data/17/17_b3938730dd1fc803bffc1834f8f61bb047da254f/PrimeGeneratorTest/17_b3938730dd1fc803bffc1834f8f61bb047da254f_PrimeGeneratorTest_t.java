 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 
 
 public class PrimeGeneratorTest {
 	
 	@Test
 	public void testPrimeGenerateOne(){
 		assertEquals(PrimeGenerator.generatePrimes(1), new ArrayList<Integer>());
 	}
 	
 	@Test
 	public void testPrimeGenerateTwo(){
 		assertEquals(PrimeGenerator.generatePrimes(2), new ArrayList<Integer>());
 	}
 	
 	@Test
 	public void testPrimeGenerateThree(){
 		ArrayList<Integer> list = new ArrayList<Integer>();
 		list.add(2);
 		assertEquals(PrimeGenerator.generatePrimes(3), list);
 	}
 	
 	@Test
 	public void testPrimeGenerateFour(){
 		ArrayList<Integer> list = new ArrayList<Integer>();
 		list.add(2);
 		list.add(3);
 		assertEquals(PrimeGenerator.generatePrimes(4), list);
 	}
 	
 	@Test
 	public void testPrimeGenerateFive(){
 		ArrayList<Integer> list = new ArrayList<Integer>();
 		list.add(2);
 		list.add(3);
 		assertEquals(PrimeGenerator.generatePrimes(5), list);
 	}
	
	@Test
	public void testPrimeGenerateSix(){
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(2);
		list.add(3);
		list.add(5);
		assertEquals(PrimeGenerator.generatePrimes(6), list);
	}
 
 }
