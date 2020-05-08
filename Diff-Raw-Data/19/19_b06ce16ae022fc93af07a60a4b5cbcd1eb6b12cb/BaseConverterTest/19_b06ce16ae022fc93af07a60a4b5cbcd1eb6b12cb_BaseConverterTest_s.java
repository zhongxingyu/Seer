 package info.ohgita.android.bincalc.calculator;
 
 import junit.framework.TestCase;
 
 public class BaseConverterTest extends TestCase {
 	public void testConv() throws Exception {
 		BaseConverter bcon = new BaseConverter();
 		
 		/* Reverse bits */
 		assertEquals("1", bcon.reverseBits("0"));
 		assertEquals("0101", bcon.reverseBits("1010"));
 		
 		/* Add bits */
 		assertEquals("1", bcon.add1ToBits("0"));
 		assertEquals("1001", bcon.add1ToBits("1000"));
 		assertEquals("1010", bcon.add1ToBits("1001"));
 		
 		/* Convert - Positive number */
 		// Dec to Bin
 		assertEquals("010000000000.1", bcon.decToN(1024.5, 2));
 		// Dec to Hex
 		assertEquals("400.8", bcon.decToN(1024.5, 16));
 		// Bin to Dec
 		assertEquals(1024.5, bcon.binToDec("010000000000.1"));
 		// Hex to Dec
 		assertEquals(1024.5, bcon.hexToDec("400.8"));
 		// Bin to Hex
 		assertEquals("EE", bcon.decToN(bcon.binToDec("11101110"), 16));
 		
 		/* Convert - Negative number */
 		// Dec to Bin
 		assertEquals("11101110", bcon.decToN(-18, 2));
 		assertEquals("10000000", bcon.decToN(-128, 2));
 		// Dec to hex
 		assertEquals("EE", bcon.decToN(-18, 16));
 		assertEquals("80", bcon.decToN(-128, 16));
 	}
 }
