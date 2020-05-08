 package edu.ucsb.cs56.S13.lab04.BrandonNewman;
 
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertFalse;
 
 
 /**
 Test class for StreetAddress
 
 @author Brandon Newman
 @version 2013/05/02 for lab04, cs56, s13
 @see StreetAddress
 */
 public class StreetAddressTest
 {
 	public static final double TOL = 0.00001;
 
 	/**
 	Test case for the no argument Constructor
 	@see StreetAddress
 	*/
 	@Test public void testNoArgConstructor()
 	{
 		StreetAddress s = new StreetAddress();
		assertEquals("No Where Lane", s.getStreetName());
 		assertEquals(0, s.getStreetNumber());
 	}
 
 	/**
 	Test case for the two argument Constructor
 	@see StreetAddress
 	*/
 	@Test public void testTwoArgConstructor()
 	{
 		StreetAddress s = new StreetAddress("Pardall", 192);
 		assertEquals("Pardall", s.getStreetName());
 		assertEquals(192, s.getStreetNumber());
 	}
 
 	/**
 	Test case for StreetAddress.setStreetName()
 	@see StreetAddress
 	*/
 	@Test public void testSetStreetName()
 	{
 		StreetAddress s = new StreetAddress();
 		s.setStreetName("Alana Lane");
 		assertEquals("Alana Lane", s.getStreetName());
 	}
 
 	/**
 	Test case for StreetAddress.setStreetNumber()
 	@see StreetAddress
 	*/
 	@Test public void testSetStreetNumber()
 	{
 		StreetAddress s = new StreetAddress();
 		s.setStreetNumber(544);
 		assertEquals(544, s.getStreetNumber());
 	}
 
 	/**
 	Test case for StreetAddress.getStreetName()
 	@see StreetAddress
 	*/
 	@Test public void testGetStreetName()
 	{
 		StreetAddress s = new StreetAddress("Camino Del Sur", 785);
 		assertEquals("Camino Del Sur", s.getStreetName());
 	}
 
 	/**
 	Test case for StreetAddress.getStreetNumber()
 	@see StreetAddress
 	*/
 	@Test public void testGetStreetNumber()
 	{
 		StreetAddress s = new StreetAddress("Camino Del Sur", 785);
 		assertEquals(785, s.getStreetNumber());
 	}
 
 	/**
 	Test case for StreetAddress.toString()
 	@see StreetAddress
 	*/
 	@Test public void testToString()
 	{
 		StreetAddress s = new StreetAddress("Lone Oak Drive", 2413);
 		assertEquals("2413 Lone Oak Drive", s.toString());
 	}
 
 	/**
 	Test case for StreetAddress.equals()
 	@see StreetAddress
 	*/
 	@Test public void testEquals()
 	{
 		StreetAddress s = new StreetAddress("Lone Oak Drive", 2413);
 		StreetAddress s2 = new StreetAddress("Lone Oak Drive", 2413);
 		assertEquals(true, s.equals(s2));
 	}
 
 
 }
