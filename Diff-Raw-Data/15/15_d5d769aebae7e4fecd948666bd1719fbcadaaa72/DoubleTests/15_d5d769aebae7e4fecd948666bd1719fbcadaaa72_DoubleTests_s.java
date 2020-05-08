 package ro.alexbolboaca.JavaLearningTests;
 
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertFalse;
 
 public class DoubleTests {
 
 	@Test
 	public void constructWithZero() {
 		Double value = new Double(0);
 		
 		assertEquals(0.0, value.doubleValue(), 0);
 	}
 	
 	@Test
 	public void constructWithNegativeZero() {
 		Double value = new Double(-0.0);
 		
 		assertEquals(-0.0, value.doubleValue(), 0);
 	}
 	
 	@Test
 	public void constructWithSmallNegative() {
 		Double value = new Double(-0.1);
 		
 		assertEquals(-0.1, value.doubleValue(), 0);
 	}
 
 	@Test
 	public void constructWithSmallPositive() {
 		Double value = new Double(0.1);
 		
 		assertEquals(0.1, value.doubleValue(), 0);
 	}
 
 	@Test
 	public void constructWithAPositive() {
 		Double value = new Double(21947129847294723498045723957.32984729304752938475);
 		
 		assertEquals(21947129847294723498045723957.32984729304752938475, value.doubleValue(), 0);
 	}
 
 	@Test
 	public void constructWithANegative() {
 		Double value = new Double(392857234980457239805729085072349808.293887590823750982837592375);
 		
 		assertEquals(392857234980457239805729085072349808.293887590823750982837592375, value.doubleValue(), 0);
 	}
 
 	@Test
 	public void constructWithMax() {
 		Double value = new Double(Double.MAX_VALUE);
 		
 		assertEquals(Double.MAX_VALUE, value.doubleValue(), 0);
 	}
 
 	@Test
 	public void constructWithMin() {
 		Double value = new Double(Double.MIN_VALUE);
 		
 		assertEquals(Double.MIN_VALUE, value.doubleValue(), 0);
 	}
 
 	@Test
 	public void constructWithPositiveInfinity() {
 		Double value = new Double(Double.POSITIVE_INFINITY);
 		
 		assertEquals(Double.POSITIVE_INFINITY, value.doubleValue(), 0);
 	}
 
 	@Test
 	public void constructWithNegativeInfinity() {
 		Double value = new Double(Double.NEGATIVE_INFINITY);
 		
 		assertEquals(Double.NEGATIVE_INFINITY, value.doubleValue(), 0);
 	}
 
 	@Test
 	public void constructWithNAN() {
 		Double value = new Double(Double.NaN);
 		
 		assertEquals(Double.NaN, value.doubleValue(), 0);
 	}
 	
 	@Test
 	public void infinitiesAreDifferent() throws Exception {
 		assertThat(Double.NEGATIVE_INFINITY, is(not(Double.POSITIVE_INFINITY)));
 	}
 
 	@Test
 	public void negativeInfinityLessThanPositiveInfinity() throws Exception {
 		assertTrue(Double.NEGATIVE_INFINITY < Double.POSITIVE_INFINITY);
 	}
 
 	@Test
 	public void negativeInfinityLessThanMin() throws Exception {
 		assertTrue(Double.NEGATIVE_INFINITY < Double.MIN_VALUE);
 	}
 
 	@Test
 	public void positiveInfinityGreaterThanMax() throws Exception {
 		assertTrue(Double.POSITIVE_INFINITY > Double.MAX_VALUE);
 	}
 
 	@Test
 	public void NANDoesntCompare() throws Exception {
 		assertDoesntCompareWithNaN(Double.POSITIVE_INFINITY);		
 		assertDoesntCompareWithNaN(Double.NEGATIVE_INFINITY);		
 		assertDoesntCompareWithNaN(Double.MIN_VALUE);		
 		assertDoesntCompareWithNaN(Double.MAX_VALUE);		
 		assertDoesntCompareWithNaN(Double.NaN);		
 		assertDoesntCompareWithNaN(new Double(21972493785293872895.29735092347520983572903));		
 	}
 
	private void assertDoesntCompareWithNaN(double positiveInfinity) {
		assertFalse(positiveInfinity > Double.NaN);
		assertFalse(positiveInfinity >= Double.NaN);
		assertFalse(positiveInfinity < Double.NaN);		
		assertFalse(positiveInfinity <= Double.NaN);
	}

 	@Test
 	public void infinitiesAreDifferentFromNAN() throws Exception {
 		assertThat(Double.NEGATIVE_INFINITY, is(not(Double.NaN)));
 		assertThat(Double.POSITIVE_INFINITY, is(not(Double.NaN)));
 	}
 
 	@Test
 	public void infinitiesAreDifferentFromMin() throws Exception {
 		assertThat(Double.NEGATIVE_INFINITY, is(not(Double.MIN_VALUE)));
 		assertThat(Double.POSITIVE_INFINITY, is(not(Double.MIN_VALUE)));
 	}
 
 	@Test
 	public void infinitiesAreDifferentFromMax() throws Exception {
 		assertThat(Double.NEGATIVE_INFINITY, is(not(Double.MAX_VALUE)));
 		assertThat(Double.POSITIVE_INFINITY, is(not(Double.MAX_VALUE)));
 	}
 }
