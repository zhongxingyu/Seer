 package lt.doubleem.gpt;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.math.BigInteger;
 
 import org.junit.Test;
 
 public class LinearizedPolynomialTest {
 	
 	@Test
 	public void shouldMultiplyPolynomials() {
 		BigInteger powerOfPrime = BigInteger.valueOf(3).pow(2);
 		
 		LinearizedPolynomial<FiniteFieldElement> poly1 = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "023");
 		LinearizedPolynomial<FiniteFieldElement> poly2 = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "041");
 		
 		LinearizedPolynomial<FiniteFieldElement> actual = poly1.mul(poly2);
 		LinearizedPolynomial<FiniteFieldElement> expected = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "00253");
 		assertEquals(expected, actual);
 		
 		actual = poly2.mul(poly1);
 		expected = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "0058");
 		assertEquals(expected, actual);
 		
 		poly1 = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "023");
 		poly2 = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "042");
 		
 		actual = poly1.mul(poly2);
 		expected = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "00216");
 		assertEquals(expected, actual);
 		
 		actual = poly2.mul(poly1);
 		expected = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "0057");
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void shouldDividePolynomials() {
 		BigInteger powerOfPrime = BigInteger.valueOf(3).pow(2);
 		
 		LinearizedPolynomial<FiniteFieldElement> f1 = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "00253");
 		LinearizedPolynomial<FiniteFieldElement> g1 = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "041");
 		
 		LinearizedPolynomial<FiniteFieldElement> actual = f1.div(g1);
 		LinearizedPolynomial<FiniteFieldElement> expected = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "023");
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void shouldNotDividePolynomials() {
 		BigInteger powerOfPrime = BigInteger.valueOf(3).pow(2);
 		LinearizedPolynomial<FiniteFieldElement> f1 = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "0058");
 		LinearizedPolynomial<FiniteFieldElement> g1 = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "023");
 		 
 		try {
 			LinearizedPolynomial<FiniteFieldElement> actual = f1.div(g1);
 			fail("ArithmeticException expected");
 		} catch (ArithmeticException ex) {
 			// ok
 		}
 	}
 	
 	@Test
 	public void shouldReturnRoots() {
 		BigInteger powerOfPrime = BigInteger.valueOf(2).pow(1);
		LinearizedPolynomial<FiniteFieldElement> f1 = new LinearizedPolynomial<>(powerOfPrime, new FiniteFieldElement(powerOfPrime), "231");
		System.out.println(f1.getRoots());
 	}
 	
 }
