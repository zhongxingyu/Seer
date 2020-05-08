 /**
  * 
  */
 package com.qut.spc.calculations;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author GEILIO
  *
  */
 public class TotalCostCalculatorTest {
 
 	TotalCostCalculator total;
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		total = new TotalCostCalculator();
 	}
 
 	/**
 	 * Test method for {@link com.qut.spc.calculations.TotalCostCalculator#getSystemTotalCost(double, int, double, int, double)}.
 	 */
 	@Test
 	public void testGetSystemTotalCost_normal() {
		assertEquals(2200.0,total.getSystemTotalCost(300, 3, 500, 1, 800),0.01);
 	}
 	
 	@Test
 	public void testGetSystemTotalCost_panelPrice_zero() {
 		//System.out.println(total.getSystemTotalCost(0, 3, 500, 1, 800));
		assertEquals(1300.0,total.getSystemTotalCost(0, 3, 500, 1, 800),0.01);
 	}
 	@Test
 	public void testGetSystemTotalCost_panelsQuantity_zero() {
 		//System.out.println(total.getSystemTotalCost(300,0, 500, 1, 800));
		assertEquals(1300.0,total.getSystemTotalCost(300,0, 500, 1, 800),0.01);
 	}
 	@Test
 	public void testGetSystemTotalCost_batterPrice_zero() {
 		//System.out.println(total.getSystemTotalCost(300, 3, 0, 1, 800));
		assertEquals(1700.0,total.getSystemTotalCost(300, 3, 0, 1, 800),0.01);
 	}
 	@Test
 	public void testGetSystemTotalCost_batteriesQuantity_zero() {
		assertEquals(1700.0,total.getSystemTotalCost(300, 3, 500, 0, 800),0.01);
 	}
 	@Test
 	public void testGetSystemTotalCost_intverterPrice_zero() {
		assertEquals(1400.0,total.getSystemTotalCost(300, 3, 500, 1, 0),0.01);
 	}
 	//panelPrice,panelsQuantity,batterPrice,batteriesQuantity,intverterPrice
 }
