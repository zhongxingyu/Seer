 package com.beepscore.DivideWithoutOperator;
 //import com.beepscore.DivideWithoutOperator.DivideWithoutOperator;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class DivideWithoutOperatorTest {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testDivideWithoutOperator()
 	{
 		assertTrue(1L == DivideWithoutOperator.divideWithoutOperator(1, 1));
	}
 
 }
