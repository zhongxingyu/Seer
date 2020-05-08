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
 
 	@Test
	public void testGetLogger() {
 
 		HasLogger obj = null;
 		Logger result = null;
 		
 		obj = createHasLogger();
 		result = obj.getLogger();
 		Assert.assertNotNull(result);
 	}
 
 	private AbstractHasLogger createHasLogger() {
 
 		return new AbstractHasLogger(){};
 	}
 
 }
