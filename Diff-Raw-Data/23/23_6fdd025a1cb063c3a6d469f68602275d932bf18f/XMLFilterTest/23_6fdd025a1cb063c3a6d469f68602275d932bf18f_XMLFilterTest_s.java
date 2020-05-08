 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.xml.tests;
 
import java.net.URI;
 import java.net.URL;
 
 import net.sf.okapi.filters.tests.FilterTestDriver;
 import net.sf.okapi.filters.xml.XMLFilter;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class XMLFilterTest {
 
 	@Test
 	public void runTest () {
 		FilterTestDriver testDriver = new FilterTestDriver();
 		XMLFilter filter = null;		
 		try {
 			filter = new XMLFilter();
 			filter.setOptions("en", "es", "UTF-8", true);

			filter.open("<doc><p>p1<q>q1</q>p2</p></doc>\n <!--com-->");
 			if ( !testDriver.process(filter) ) Assert.fail();
 			filter.close();
			
			URL url = XMLFilterTest.class.getResource("/Translate2.xml");
			filter.open(new URI(url.toString()));
 			if ( !testDriver.process(filter) ) Assert.fail();
 			filter.close();
 		}
 		catch ( Throwable e ) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 		finally {
 			if ( filter != null ) filter.close();
 		}
 	}
 
 }
