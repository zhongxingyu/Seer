 package net.adbenson.codekata.romanNumerals.test;
 
 import net.adbenson.codekata.romanNumerals.RomanNumerals;
 
 import org.junit.Test;
 
 public class RomanNumeralsTest {
 	
 	@Test
 	public void romanNumerals1returnsI() {
 		assertEquals("I", RomanNumerals.roman(1));
 	}
 
 }
