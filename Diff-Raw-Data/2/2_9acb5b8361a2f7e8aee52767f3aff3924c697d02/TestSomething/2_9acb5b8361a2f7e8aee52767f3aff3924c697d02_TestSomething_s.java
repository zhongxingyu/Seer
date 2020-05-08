 package com.sb;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestSomething {
	private static final int _EXPECTED_NUMBER = 8;
 	Something abstractSomething = null;
 	
 	@Before
 	public void setUp() throws Exception {
 		abstractSomething = new Something();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testDoSomething() {
 		assertTrue(abstractSomething.doSomething() == _EXPECTED_NUMBER);
 	}
 
 }
