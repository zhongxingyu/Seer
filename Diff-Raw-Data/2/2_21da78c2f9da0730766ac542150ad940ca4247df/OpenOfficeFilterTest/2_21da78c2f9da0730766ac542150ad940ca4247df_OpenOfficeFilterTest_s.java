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
 
 package net.sf.okapi.filters.openoffice.tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertFalse;
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.TestUtil;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.common.filters.RoundTripComparison;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.filters.openoffice.OpenOfficeFilter;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class OpenOfficeFilterTest {
 
 	private OpenOfficeFilter filter;
 	private String root;
 
 	@Before
 	public void setUp() {
 		filter = new OpenOfficeFilter();
 		root = TestUtil.getParentDir(this.getClass(), "/TestDocument01.odt_content.xml");
 	}
 
 	@Test
 	public void testDefaultInfo () {
 		assertNotNull(filter.getParameters());
 		assertNotNull(filter.getName());
 		List<FilterConfiguration> list = filter.getConfigurations();
 		assertNotNull(list);
 		assertTrue(list.size()>0);
 	}
 
 	@Test
 	public void testFirstTextUnit () {
 		TextUnit tu = FilterTestDriver.getTextUnit(filter,
 			new InputDocument(root+"TestDocument01.odt", null),
 			"UTF-8", "en", "en", 1);
 		assertNotNull(tu);
 		assertEquals("Heading 1", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testStartDocument () {
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
 			new InputDocument(root+"TestDocument01.odt", null),
 			"UTF-8", "en", "en"));
 	}
 	
 	@Test
 	public void testDoubleExtraction () throws URISyntaxException {
 		// Read all files in the data directory
 		
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		list.add(new InputDocument(root+"TestSpreadsheet01.ods", null));
 		list.add(new InputDocument(root+"TestDocument01.odt", null));
 		list.add(new InputDocument(root+"TestDocument02.odt", null));
 		list.add(new InputDocument(root+"TestDocument03.odt", null));
 		list.add(new InputDocument(root+"TestDocument04.odt", null));
 		list.add(new InputDocument(root+"TestDocument05.odt", null));
 		list.add(new InputDocument(root+"TestDocument06.odt", null));
 		list.add(new InputDocument(root+"TestDrawing01.odg", null));
 		list.add(new InputDocument(root+"TestPresentation01.odp", null));
 		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", "en", "en", "output"));
 	}
 
 }
