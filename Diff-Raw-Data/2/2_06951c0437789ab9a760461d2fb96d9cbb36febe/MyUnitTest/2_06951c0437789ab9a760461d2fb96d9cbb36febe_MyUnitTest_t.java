 package test.java;
 
 import org.junit.*;
 
 public class MyUnitTest {
 	
 	@Test
 	public void testFirstTest() {
 		Assert.assertEquals(4, 2 + 2);
		Assert.assertEquals("Hello World", "Hello World");
 	}
 
 }
