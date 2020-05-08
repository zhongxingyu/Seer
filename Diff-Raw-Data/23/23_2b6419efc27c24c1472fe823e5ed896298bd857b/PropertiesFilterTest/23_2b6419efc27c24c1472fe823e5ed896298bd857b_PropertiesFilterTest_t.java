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
 
 package net.sf.okapi.filters.properties;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.filters.properties.PropertiesFilter;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.common.filters.RoundTripComparison;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class PropertiesFilterTest {
 	
 	private PropertiesFilter filter;
 
 	@Before
 	public void setUp() {
 		filter = new PropertiesFilter();
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
 	public void testDoubleExtraction () {
 		// Read all files in the data directory
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		URL url = PropertiesFilterTest.class.getResource("/Test01.properties");
		list.add(new InputDocument(url.getPath(), null));
		url = PropertiesFilterTest.class.getResource("/Test02.properties");
		list.add(new InputDocument(url.getPath(), "okf_properties@Test02.fprm"));
		url = PropertiesFilterTest.class.getResource("/Test03.properties");
		list.add(new InputDocument(url.getPath(), "okf_properties@Test03.fprm"));
		url = PropertiesFilterTest.class.getResource("/Test04.properties");
		list.add(new InputDocument(url.getPath(), "okf_properties@Test04.fprm"));
 	
 		RoundTripComparison rtc = new RoundTripComparison();
 		assertTrue(rtc.executeCompare(filter, list, "UTF-8", "en", "fr"));
 	}
 
 	@Test
 	public void testStartDocument () {
		URL url = PropertiesFilterTest.class.getResource("/Test01.properties");
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			new InputDocument(url.getPath(), null),
 			"UTF-8", "en", "en"));
 	}
 	
 	@Test
 	public void testLineBreaks_CR () {
 		String snippet = "Key1=Text1\rKey2=Text2\r";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet), "en");
 		assertEquals(snippet, result);
 	}
 
 	@Test
 	public void testineBreaks_CRLF () {
 		String snippet = "Key1=Text1\r\nKey2=Text2\r\n";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet), "en");
 		assertEquals(snippet, result);
 	}
 	
 	@Test
 	public void testLineBreaks_LF () {
 		String snippet = "Key1=Text1\n\n\nKey2=Text2\n";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet), "en");
 		assertEquals(snippet, result);
 	}
 	
 	@Test
 	public void testEntry () {
 		String snippet = "Key1=Text1\n# Comment\nKey2=Text2\n";
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
 		assertNotNull(tu);
 		assertEquals("Text2", tu.getSource().toString());
 		assertEquals("Key2", tu.getName());
 		assertTrue(tu.hasProperty(Property.NOTE));
 		Property prop = tu.getProperty(Property.NOTE);
 		assertEquals(" Comment", prop.getValue());
 		assertTrue(prop.isReadOnly());
 	}
 	
 	@Test
 	public void testSplicedEntry () {
 		String snippet = "Key1=Text1\nKey2=Text2 \\\nSecond line";
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
 		assertNotNull(tu);
 		assertEquals("Text2 Second line", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testEscapes () {
 		String snippet = "Key1=Text with \\u00E3";
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("Text with \u00E3", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testKeySpecial () {
 		String snippet = "\\:\\= : Text1";
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("Text1", tu.getSource().toString());
 		assertEquals("\\:\\=", tu.getName());
 	}
 	
 	@Test
 	public void testLocDirectives_Skip () {
 		String snippet = "#_skip\nKey1:Text1\nKey2:Text2";
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		// Text1 not extracted because of the directive
 		assertEquals("Text2", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testLocDirectives_Group () {
 		String snippet = "#_bskip\nKey1:Text1\n#_text\nKey2:Text2\nKey2:Text3";
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		// Text1 not extracted because of the directive
 		assertEquals("Text2", tu.getSource().toString());
 		// No next TU because of _bskip
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
 		assertNull(tu);
 	}
 	
 	private ArrayList<Event> getEvents(String snippet) {
 		ArrayList<Event> list = new ArrayList<Event>();
 		filter.open(new RawDocument(snippet, "en"));
 		while (filter.hasNext()) {
 			Event event = filter.next();
 			list.add(event);
 		}
 		filter.close();
 		return list;
 	}
 
 }
