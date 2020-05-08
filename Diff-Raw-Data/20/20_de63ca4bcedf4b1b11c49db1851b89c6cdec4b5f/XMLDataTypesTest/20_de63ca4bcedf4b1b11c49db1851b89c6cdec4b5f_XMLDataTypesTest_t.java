 /*******************************************************************************
  * 
  * Copyright 2002 - 2005 NCHELP
  * 
  * Author:	Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified by the
  * National Council of Higher Education Loan Programs, Inc. (NCHELP) and the
  * Meteor Sponsors, and developed by Priority Technologies, Inc. (PTI).
  * 
  * 
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *  
  ******************************************************************************/
 
 package test.nchelp.meteor;
 
 import java.text.ParseException;
 import java.util.Date;
 
 import junit.framework.TestCase;
 import junit.textui.TestRunner;
 
 import org.nchelp.meteor.util.XMLDataTypes;
 
 
 public class XMLDataTypesTest extends TestCase
 {
 	public XMLDataTypesTest (String name)
 	{
 		super(name);
 	}
 
 	public static void main (String args[])
 	{
 		TestRunner.run(XMLDataTypesTest.class);
 	}
 
 	public void testDates1 ()
 	{
 		Date d = null;
 
		String originalDate = "1971-07-01T02:06:00-0400";
 		try
 		{
 			d = XMLDataTypes.xmlToDateTime(originalDate);
 			assertEquals("Dates not equal", originalDate, XMLDataTypes.dateTimeToXml(d));
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	public void testDates2 ()
 	{
 		Date d = null;
 
 		String originalDate = "1971-07-01";
 		try
 		{
 			d = XMLDataTypes.xmlToDateTime(originalDate);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 
 	}
 
	public void testDatesPM ()
	{
		Date d = null;

		String originalDate = "1971-07-01T17:06:00-0400";
		try
		{
			d = XMLDataTypes.xmlToDateTime(originalDate);
			assertEquals("Dates not equal", originalDate, XMLDataTypes.dateTimeToXml(d));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}

 	public void testNulDates ()
 	{
 		Date d = null;
 
 		try
 		{
 			d = XMLDataTypes.xmlToDateTime(null);
 		}
 		catch (ParseException e)
 		{
 			fail(e.getMessage());
 		}
 
 		if (d != null)
 		{
 			fail("XML2Date did not return null when passed null");
 		}
 
 		String s = null;
 
 		s = XMLDataTypes.dateTimeToXml(null);
 
 		if (s != null)
 		{
 			fail("Date2XML did not return null when passed null");
 		}
 	}
 
 	public void testBoolean ()
 	{
 		if (!"1".equals(XMLDataTypes.booleanToXml(true)))
 		{
 			fail("Boolean2XML did not return the correct value for 'true'");
 		}
 
 		if (!"1".equals(XMLDataTypes.booleanToXml(true)))
 		{
 			fail("Boolean2XML did not return the correct value for 'true'");
 		}
 
 		if (XMLDataTypes.xmlToBoolean("0"))
 		{
 			fail("XML2Boolean did not return false when passed the paramater '0'");
 		}
 	}
 
 }
 
