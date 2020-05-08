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
		// Examples from the book.
		assertEquals("0000000000010000", code.translate(new ACommand("@16")));
		assertEquals("0000000000010001", code.translate(new ACommand("@17")));
		assertEquals("0000000001100100", code.translate(new ACommand("@100")));
		assertEquals("0000000000010010", code.translate(new ACommand("@18")));
		assertEquals("0000000000000100", code.translate(new ACommand("@4")));
 	}
 
 	public void testTranslateCCommand () {
		Code code = new Code();
		// Examples from the book.
		assertEquals("1110111111001000", code.translate(new CCommand("M=1")));
		assertEquals("1110101010001000", code.translate(new CCommand("M=0")));
		assertEquals("1111110000010000", code.translate(new CCommand("D=M")));
		assertEquals("1110010011010000", code.translate(new CCommand("D=D-A")));
		assertEquals("1110001100000001", code.translate(new CCommand("D;JGT")));
		assertEquals("1111000010001000", code.translate(new CCommand("M=D+M")));
		assertEquals("1111110111001000", code.translate(new CCommand("M=M+1")));
		assertEquals("1110101010000111", code.translate(new CCommand("0;JMP")));
 	}
 }
 
