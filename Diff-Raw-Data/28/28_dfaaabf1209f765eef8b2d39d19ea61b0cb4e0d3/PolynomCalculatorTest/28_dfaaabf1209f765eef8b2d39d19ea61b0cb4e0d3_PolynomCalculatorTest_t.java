 package de.htw.berlin.student.polynom;
 
 import de.htw.berlin.student.polynom.controller.PolynomCalculator;
 
 import java.math.BigDecimal;
 import java.util.Arrays;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import de.htw.berlin.student.polynom.model.DividedPolynom;
 import de.htw.berlin.student.polynom.model.Polynom;
 
 /**
  * Testcase for the {@link PolynomCalculator}.
  * 
  * @author Matthias Drummer
  */
 public class PolynomCalculatorTest {
 
 	private Polynom p1 = new Polynom(Arrays.asList(new BigDecimal(15), new BigDecimal(-6), new BigDecimal(5), new BigDecimal(-20)));
 	private Polynom p2 = new Polynom(Arrays.asList(new BigDecimal(-8), new BigDecimal(9), new BigDecimal(4)));
 
 	private PolynomCalculator testInstance;
 
 	@Before
 	public void before() {
 		this.testInstance = new PolynomCalculator();
 	}
 
 	/**
 	 * Tests the {@link PolynomCalculator#add(Polynom, Polynom)}.ÏÍ
 	 */
 	@Test
 	public void testAddition() {
 
 		Polynom result = testInstance.add(p1, p2);
 
 		Assert.assertEquals(new BigDecimal(7), result.getCoefficients().get(0));
 		Assert.assertEquals(new BigDecimal(-20), result.getCoefficients().get(3));
 	}
 
 	/**
 	 * Tests the {@link PolynomCalculator#derivate(de.htw.berlin.student.polynom.model.Polynom)}.
 	 */
 	@Test
 	public void testDerivation() {
 
 		Polynom result = testInstance.derivate(p1);
 
 		Assert.assertEquals(new BigDecimal(-6), result.getCoefficients().get(0));
 		Assert.assertEquals(new BigDecimal(10), result.getCoefficients().get(1));
 		Assert.assertEquals(new BigDecimal(-60), result.getCoefficients().get(2));
 	}
 
 	/**
 	 * Tests the {@link PolynomCalculator#divideWithHorner(Polynom, BigDecimal)}.
 	 */
 	@Test
 	public void testDivision() {
 
 		Polynom po = new Polynom(Arrays.asList(new BigDecimal(1), new BigDecimal(2), new BigDecimal(3)));
 		DividedPolynom result = testInstance.divideWithHorner(po, new BigDecimal(2));
 
 		Assert.assertTrue(result.getPolynom().getCoefficients().size() == 2);
 		Assert.assertEquals(new BigDecimal(8), result.getPolynom().getCoefficients().get(0));
 		Assert.assertEquals(new BigDecimal(3), result.getPolynom().getCoefficients().get(1));
 		Assert.assertEquals(new BigDecimal(17), result.getRest());
 	}

	/**
	 * Tests the {@link PolynomCalculator#multiply(Polynom, Polynom)}.
	 */
	@Test
	public void testMultiply() {

		Polynom po = new Polynom(Arrays.asList(new BigDecimal(1), new BigDecimal(2), new BigDecimal(3)));
		Polynom result = testInstance.multiply(po, po);

		Assert.assertNotNull(result);

		// if grad of polynom one and two is each 3, then the new grade is 7.
		Assert.assertTrue(result.getCoefficients().size() == 5);
		
		// TODO: do multiplication via feet and verify correct values.
	}
 }
