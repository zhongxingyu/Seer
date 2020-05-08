 package edu.tum.lua.junit.stdlib.string;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import edu.tum.lua.stdlib.string.Sub;
 
 public class SubTest {
 
 	@Test
 	public void test() {
 		Sub f = new Sub();
 
 		assertEquals("Hello", f.apply("Hello", 1.0).get(0));
 		assertEquals("Hello", f.apply("Hello", 1.0, 5.0).get(0));
 		assertEquals("Hello", f.apply("Hello", 1.0, -1.0).get(0));
		assertEquals("Hello", f.apply("Hello", 0.0, -1.0).get(0));
 
 		assertEquals("ello", f.apply("Hello", 2.0).get(0));
 		assertEquals("ell", f.apply("Hello", 2.0, 4.0).get(0));
 
 		assertEquals("o", f.apply("Hello", -1.0, -1.0).get(0));
 		assertEquals("H", f.apply("Hello", 1.0, -5.0).get(0));
		assertEquals("Hel", f.apply("Hello", 1.0, -3.0).get(0));
 	}
 
 }
