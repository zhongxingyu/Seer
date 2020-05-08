 package org.sample.app.ogiSecond;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class CocoaTest {
 	private Cocoa testObject;
 	@Before
 	public void setUp() throws Exception {
 		testObject = new Cocoa();
 	}
 
 	@Test
 	public final void testCocoa() {
 		assertEquals("ok",testObject.cocoaChecker(true));
		
 	}
 
 }
