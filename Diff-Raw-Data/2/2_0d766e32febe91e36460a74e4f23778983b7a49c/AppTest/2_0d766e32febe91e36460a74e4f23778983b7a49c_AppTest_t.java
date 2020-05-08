 /**
  * 
  */
 package com.osonmez.demo;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * @author OSONMEZ
  *
  */
 public class AppTest {
 	
 	private App app;
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		app = new App();
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testNumber1() {
 		assertEquals(1, app.getNumber1());
 	}
 	
 	@Test
 	public void testNumber2() {
 		assertEquals(2, app.getNumber2());
 	}
 	
 	@Test
 	public void testNumber3() {
		assertEquals(3, app.getNumber3());
 	}
 
 }
