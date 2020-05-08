 /**
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Tim Bornholtz,  Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ********************************************************************************/
 
 package test.nchelp.meteor;
 
 import java.util.Date;
 
 import junit.framework.TestCase;
 import junit.textui.TestRunner;
 import org.nchelp.meteor.util.XMLDataTypes;
 
 public class XMLDataTypesTest extends TestCase {
 	
 	public XMLDataTypesTest(String name) {
 		super(name);
 	}
 
 	public static void main(String args[]) {
 		TestRunner.run(XMLDataTypesTest.class);
 	}
 
 	public void testDates() {
 		Date d = null;
 		
		String originalDate = "1971-07-01T02:06:00CDT";
 		try{
 			d = XMLDataTypes.XML2Date(originalDate);
 			assertEquals("Dates not equal", 
 							originalDate, 
 							XMLDataTypes.Date2XML(d));
 		} catch(Exception e){
 			e.printStackTrace();
 			fail();
 		}
 		
 								
 	}
 	
 
 }
 
 
 
 
 
 
 
 
