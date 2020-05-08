 /*===========================================================================
   Copyright (C) 2008-2011 by the Okapi Framework contributors
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
 
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.filters.properties.PropertiesFilter;
 import net.sf.okapi.common.filters.DefaultFilters;
 import net.sf.okapi.common.filters.FilterConfigurationMapper;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.common.filters.RoundTripComparison;
 import net.sf.okapi.common.LocaleId;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class PropertiesFilterTest {
 	
 	private PropertiesFilter filter;
 	private LocaleId locEN = LocaleId.ENGLISH;
 	private LocaleId locFR = LocaleId.FRENCH;
 
 	@Before
 	public void setUp() {
 		filter = new PropertiesFilter();
 		IFilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
 		DefaultFilters.setMappings(fcMapper, true, true);
 		filter.setFilterConfigurationMapper(fcMapper);
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
 	public void testDoubleExtraction () throws URISyntaxException {
 		// Read all files in the data directory
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		URL url = PropertiesFilterTest.class.getResource("/Test01.properties");
 		list.add(new InputDocument(url.toURI().getPath(), null));
 		url = PropertiesFilterTest.class.getResource("/Test02.properties");
 		list.add(new InputDocument(url.toURI().getPath(), "okf_properties@Test02.fprm"));
 		url = PropertiesFilterTest.class.getResource("/Test03.properties");
 		list.add(new InputDocument(url.toURI().getPath(), "okf_properties@Test03.fprm"));
 		url = PropertiesFilterTest.class.getResource("/Test04.properties");
 		list.add(new InputDocument(url.toURI().getPath(), "okf_properties@Test04.fprm"));
 	
 		RoundTripComparison rtc = new RoundTripComparison();
 		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locFR));
 	}
 
 	@Test
 	public void testStartDocument () throws URISyntaxException {
 		URL url = PropertiesFilterTest.class.getResource("/Test01.properties");
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
 			new InputDocument(url.toURI().getPath(), null),
 			"UTF-8", locEN, locEN));
 	}
 	
 	@Test
 	public void testLineBreaks_CR () {
 		String snippet = "Key1=Text1\rKey2=Text2\r";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet),
 			filter.getEncoderManager(), locEN);
 		assertEquals(snippet, result);
 	}
 	
 	@Test
 	public void testMessagePlaceholders () {
 		String snippet = "Key1={1}Text1{2}";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals(2, tu.getSource().getFirstContent().getCodes().size());
 		assertEquals("{1}Text1{2}", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testMessagePlaceholdersEscaped () {
 		// Message with place holders. They are treated an inline code by default
 		String snippet = "Key1={1}Text1{2}";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals(2, tu.getSource().getFirstContent().getCodes().size());
 		assertEquals("{1}Text1{2}", tu.getSource().toString());
 	}
 
 	@Test
 	public void testineBreaks_CRLF () {
 		String snippet = "Key1=Text1\r\nKey2=Text2\r\n";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet),
 			filter.getEncoderManager(), locEN);
 		assertEquals(snippet, result);
 	}
 	
 	@Test
 	public void testLineBreaks_LF () {
 		String snippet = "Key1=Text1\n\n\nKey2=Text2\n";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet),
 			filter.getEncoderManager(), locEN);
 		assertEquals(snippet, result);
 	}
 	
 	@Test
 	public void testEntry () {
 		String snippet = "Key1=Text1\n# Comment\nKey2=Text2\n";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
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
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
 		assertNotNull(tu);
 		assertEquals("Text2 Second line", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testEscapes () {
 		String snippet = "Key1=Text with \\u00E3";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("Text with \u00E3", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testKeySpecial () {
 		String snippet = "\\:\\= : Text1";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("Text1", tu.getSource().toString());
 		assertEquals("\\:\\=", tu.getName());
 	}
 	
 	@Test
 	public void testLocDirectives_Skip () {
 		String snippet = "#_skip\nKey1:Text1\nKey2:Text2";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		// Text1 not extracted because of the directive
 		assertEquals("Text2", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testLocDirectives_Group () {
 		String snippet = "#_bskip\nKey1:Text1\n#_text\nKey2:Text2\nKey2:Text3";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		// Text1 not extracted because of the directive
 		assertEquals("Text2", tu.getSource().toString());
 		// No next TU because of _bskip
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
 		assertNull(tu);
 	}
 	
 	@Test
 	public void testSpecialChars () {
 		String snippet = "Key1:Text1\\n=lf, \\t=tab, \\w=w, \\r=cr, \\\\=bs\n";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu); // Convert the \n
 		assertEquals("Text1\n=lf, \t=tab, \\w=w, \\r=cr, \\\\=bs", tu.getSource().toString());
 	}
 
 	@Test
 	public void testSpecialCharsInKey () {
 		String snippet = "Key\\ \\:\\\\:Text1\n";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("Key\\ \\:\\\\", tu.getName());
 		assertEquals("Text1", tu.getSource().toString());
 	}
 
 	@Test
 	public void testSpecialCharsOutput () {
 		String snippet = "Key1:Text1\\n=lf, \\t=tab \\w=w, \\r=cr, \\\\=bs\n";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet),
 			filter.getEncoderManager(), locEN);
 		assertEquals(snippet, result);
 	}
 	
 	@Test
 	public void testWithSubfilter() {
 		Parameters p = (Parameters)filter.getParameters();
 		p.setSubfilter("okf_html");
 		String snippet = "Key1=<b>Text with \\u00E3 more <br> test</b>";
 		System.out.print(snippet);
 		List<Event> el = getEvents(snippet);
 		ITextUnit tu = FilterTestDriver.getTextUnit(el, 1);
 		p.setSubfilter(null);
 		filter.setParameters(p);
 		assertNotNull(tu);
 		assertEquals("<b>Text with ã more <br> test</b>", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testWithSubfilterTwoParas() {
 		Parameters p = (Parameters)filter.getParameters();
 		p.setSubfilter("okf_html");
 		String snippet = "Key1=<b>Text with \\u00E3 more</b> <p> test";
 		List<Event> el = getEvents(snippet);
 		ITextUnit tu1 = FilterTestDriver.getTextUnit(el, 1);
 		ITextUnit tu2 = FilterTestDriver.getTextUnit(el, 2);
 		p.setSubfilter(null);
 		filter.setParameters(p);
 		assertNotNull(tu1);
 		assertNotNull(tu2);
 		assertEquals("<b>Text with ã more</b>", tu1.getSource().toString());
 		assertEquals("test", tu2.getSource().toString());
 	}
 	
 	@Test
 	public void testWithSubfilterWithEmbeddedMessagePH() {
 		Parameters p = (Parameters)filter.getParameters();
 		p.setSubfilter("okf_html");
 		String snippet = "Key1=<b>Text with {1} more {2} test</b>";
 		List<Event> el = getEvents(snippet);
 		ITextUnit tu = FilterTestDriver.getTextUnit(el, 1);
 		p.setSubfilter(null);
 		filter.setParameters(p);
 		assertNotNull(tu);
 		// the Properties filter code-finder rules are passed to the HTML/XML sub-filters, so {1} and {2} are seen as codes 
 		assertEquals(4, tu.getSource().getFirstContent().getCodes().size());
 		assertEquals("<b>Text with {1} more {2} test</b>", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testWithSubfilterWithHTMLEscapes() {
 		Parameters p = (Parameters)filter.getParameters();
 		p.setSubfilter("okf_html");
 		String snippet = "Key1=<b>Text with &amp;=amp test</b>";
 		List<Event> el = getEvents(snippet);
 		ITextUnit tu = FilterTestDriver.getTextUnit(el, 1);
 		p.setSubfilter(null);
 		filter.setParameters(p);
 		assertNotNull(tu);
 		assertEquals(2, tu.getSource().getFirstContent().getCodes().size());
 		assertEquals("<b>Text with &=amp test</b>", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testWithSubfilterOutput () {
 		Parameters p = (Parameters)filter.getParameters();
 		p.setSubfilter("okf_html");
 		String snippet = "Key1=<b>Text with &amp;=amp test</b>\n";
		List<Event> el = getEvents(snippet);
 		String result = FilterTestDriver.generateOutput(getEvents(snippet),
 			filter.getEncoderManager(), locEN);
 		assertEquals(snippet, result);
 		p.setSubfilter(null);
 		filter.setParameters(p);
 	}
 	
 	@Test
 	public void testWithSubfilterWithEmbeddedEscapedMessagePH() {
 		Parameters p = (Parameters)filter.getParameters();
 		p.setSubfilter("okf_html");
 		String snippet = "Key1=<b>Text with \\{1\\} more \\{2\\} test</b>";
 		List<Event> el = getEvents(snippet);
 		ITextUnit tu = FilterTestDriver.getTextUnit(el, 1);
 		p.setSubfilter(null);
 		filter.setParameters(p);
 		assertNotNull(tu);
 		// The Properties filter code-finder rules are passed to the HTML/XML sub-filters,
 		// But {1} and {2} are escaped, so not seen as codes 
 		assertEquals(2, tu.getSource().getFirstContent().getCodes().size());
 		assertEquals("<b>Text with \\{1\\} more \\{2\\} test</b>", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testDoubleExtractionSubFilter() throws URISyntaxException {
 		// Read all files in the data directory
 		Parameters p = (Parameters)filter.getParameters();
 		p.setSubfilter("okf_html");
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		URL url = PropertiesFilterTest.class.getResource("/Test05.properties");
 		list.add(new InputDocument(url.toURI().getPath(), null));	
 		RoundTripComparison rtc = new RoundTripComparison();
 		p.setSubfilter(null);
 		filter.setParameters(p);
 		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locFR));
 	}
 	
 	private ArrayList<Event> getEvents(String snippet) {
 		ArrayList<Event> list = new ArrayList<Event>();
 		filter.open(new RawDocument(snippet, locEN));
 		while (filter.hasNext()) {
 			Event event = filter.next();
 			if (event.isMultiEvent()) {
 				for (Event e : event.getMultiEvent()) {
 					list.add(e);
 				}
 			}
 			list.add(event);
 		}
 		filter.close();
 		return list;
 	}
 
 }
