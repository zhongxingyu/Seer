 package com.operativus.senacrs.audit.common;
 
 import org.apache.logging.log4j.Logger;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class AbstractHasLoggerTest {
 
 	@Before
 	public void setUp() throws Exception {
 
 	}
 
 	@After
 	public void tearDown() throws Exception {
 
 	}
	
	public static class MyHasLogger extends AbstractHasLogger {
		
	}
 
 	@Test
	public void testGetLoggerNonNull() {
 
 		HasLogger obj = null;
 		Logger result = null;
 		
 		obj = createHasLogger();
 		result = obj.getLogger();
 		Assert.assertNotNull(result);
 	}
 
 	private AbstractHasLogger createHasLogger() {
 
 		return new AbstractHasLogger(){};
 	}
 

	@Test
	public void testGetLoggerClassName() {

		HasLogger obj = null;
		Logger result = null;
		
		obj = new MyHasLogger();
		result = obj.getLogger();
		Assert.assertEquals(MyHasLogger.class.getName(), result.getName());
	}
 }
