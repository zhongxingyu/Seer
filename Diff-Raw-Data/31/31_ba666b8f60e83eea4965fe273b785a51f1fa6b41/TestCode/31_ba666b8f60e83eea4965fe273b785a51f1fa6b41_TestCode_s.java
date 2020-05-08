 package org.hacdc.tecs.project_6.assembler;
 
 import junit.framework.*;
 
 public class TestCode extends TestCase {
 	public TestCode (String name) {
 		super(name);
 	}
 
 	public void testTranslateACommand () {
 		Code code = new Code();
 		assertEquals("0000000000000000", code.translate(new ACommand("@0")));
 		assertEquals("0000000000000001", code.translate(new ACommand("@1")));
		assertEquals("0000000000000101", code.translate(new ACommand("@5")));
		assertEquals("0000000000010000", code.translate(new ACommand("@16")));
 		assertEquals("0111111111111111", code.translate(new ACommand("@32767")));
 		try {
 			code.translate(new ACommand("@32768"));
 			fail("This should fail, address too large.");
 		} catch (IllegalCommandException ex) {
 			assertTrue(true);
 		}
 		try {
 			code.translate(new ACommand("@-1"));
 			fail("This should fail, invalid address.");
 		} catch (IllegalCommandException ex) {
 			assertTrue(true);
 		}
 	}
 
 	public void testTranslateCCommand () {
		// Need some test cases.
 	}
 }
 
