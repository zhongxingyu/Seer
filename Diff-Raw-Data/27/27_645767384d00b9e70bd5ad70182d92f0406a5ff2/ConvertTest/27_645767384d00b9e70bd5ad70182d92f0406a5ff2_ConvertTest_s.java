 package com.cffreedom.utils;
 
 import static org.junit.Assert.*;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 public class ConvertTest {
 
 	@Test
 	public void testToBigInteger()
 	{
 		long convert = 2;
 		BigInteger converted = Convert.toBigInteger(convert);
 		assertEquals(convert, converted.longValue());
 		
 		int convert2 = 5;
 		BigInteger converted2 = Convert.toBigInteger(convert2);
 		assertEquals(convert2, converted2.longValue());
 	}
 	
 	@Test
 	public void testToCents()
 	{
 		BigDecimal val = new BigDecimal(1.60);
 		assertEquals(160, Convert.toCents(val));
 	}
 	
 	@Test
 	public void testToBigDecimalFromCents()
 	{
 		BigDecimal expected = Convert.toBigDecimal(1.53);
 		BigDecimal actual = Convert.toBigDecimalFromCents(153);
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void testToStringList()
 	{
 		List<String> vals = new ArrayList<String>();
 		vals.add("A");
 		vals.add("B");
 		vals.add("C");
 		String expected = "A,B,C";
 		assertEquals(expected, Convert.toStringList(vals, ","));
 	}
 }
