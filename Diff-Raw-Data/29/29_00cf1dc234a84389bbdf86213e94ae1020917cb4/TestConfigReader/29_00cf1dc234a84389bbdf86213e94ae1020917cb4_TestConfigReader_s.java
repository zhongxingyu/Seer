 package edu.harvard.cs262.grading;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestConfigReader {
 
 	ConfigReaderImpl cr;
 	
 	@Before
 	public void setUp() throws Exception {
 		cr = new ConfigReaderImpl("config/test.config");
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testConfigReader() {
 		assertEquals("No Service Z", cr.getService("Z").isEmpty(), true);
		assertEquals("Service A", cr.getService("A").size(), 2);
		assertEquals("Service A index 0", cr.getService("A").get(0), "//192.168.56.52:1000");
		assertEquals("Service A index 1", cr.getService("A").get(1), "//192.168.56.52:0900");
		assertEquals("Service B", cr.getService("B").size(), 2);
		assertEquals("Service B index 0", cr.getService("B").get(0), "//192.168.56.52:0800");
		assertEquals("Service B index 1", cr.getService("B").get(1), "//192.168.56.52:0700");
 	}
 
 }
