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
 
 package net.sf.okapi.filters.xml;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filterwriter.GenericContent;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.common.filters.RoundTripComparison;
 import net.sf.okapi.common.TestUtil;
 import net.sf.okapi.filters.xml.XMLFilter;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class XMLFilterTest {
 
 	private XMLFilter filter;
 	private GenericContent fmt;
 	private String root;
 
 	@Before
 	public void setUp() {
 		filter = new XMLFilter();
 		fmt = new GenericContent();
 		root = TestUtil.getParentDir(this.getClass(), "/test01.xml");
 	}
 
 	@Test
 	public void testComplexIdPointer () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\""
 			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\">"
 			+ "<its:translateRule selector=\"//doc\" translate=\"no\"/>"
 			+ "<its:translateRule selector=\"//src\" translate=\"yes\" itsx:idPointer=\"../../name/@id\"/>"
 			+ "</its:rules>"
 			+ "<grp><name id=\"id1\" /><u><src>text 1</src></u></grp>"
 			+ "<grp><name id=\"id1\" /><u><src xml:id=\"xid2\">text 2</src></u></grp>"
 			+ "</doc>";
 		ArrayList<Event> list = getEvents(snippet);
 		TextUnit tu = FilterTestDriver.getTextUnit(list, 1);
 		assertNotNull(tu);
 		assertEquals("id1", tu.getName());
 		tu = FilterTestDriver.getTextUnit(list, 2);
 		assertNotNull(tu);
 		assertEquals("xid2", tu.getName()); // xml:id overrides global rule
 	}
 	
 	@Test
 	public void testIdPointer () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\""
 			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\">"
 			+ "<its:translateRule selector=\"//p\" translate=\"yes\" itsx:idPointer=\"@name\"/>"
 			+ "</its:rules>"
 			+ "<p name=\"id1\">text 1</p>"
 			+ "<p xml:id=\"xid2\">text 2</p>"
 			+ "<p xml:id=\"xid3\" name=\"id3\">text 3</p></doc>";
 		ArrayList<Event> list = getEvents(snippet);
 		TextUnit tu = FilterTestDriver.getTextUnit(list, 1);
 		assertNotNull(tu);
 		assertEquals("id1", tu.getName());
 		tu = FilterTestDriver.getTextUnit(list, 2);
 		assertNotNull(tu);
 		assertEquals("xid2", tu.getName()); // No 'name' attribute
 		tu = FilterTestDriver.getTextUnit(list, 3);
 		assertNotNull(tu);
 		assertEquals("xid3", tu.getName()); // xml:id overrides global rule
 	}
 	
 	@Test
 	public void testSimpleEntities () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
 			+ "<!DOCTYPE doc ["
 			+ "<!ENTITY aWithRingAndAcute '&#x01fb;'>\r"
 			+ "<!ENTITY text 'TEXT'>\r"
 			+ "]>\r"
 			+ "<doc>"
 			+ "<p>&aWithRingAndAcute;=e1</p>"
 			+ "<p>&text;=e2</p>"
 			+ "</doc>";
 		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 	
 	@Test
 	public void testComplexEntities () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
 			+ "<!DOCTYPE doc ["
 			+ "<!ENTITY entity1 '[&entity2;]'>\r"
 			+ "<!ENTITY entity2 'TEXT'>\r"
 			+ "]>\r"
 			+ "<doc>"
 			+ "<p>&entity1;=[TEXT]</p>"
 			+ "<p>&entity2;=TEXT</p>"
 			+ "</doc>";
 		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 	
 	@Test
 	public void testSpecialEntities () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc>"
 			+ "<p>&lt;=lt &gt;=gt &quot;=quot &apos;=apos</p>"
 			+ "</doc>";
 		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
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
 	public void testStartDocument () {
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
 			new InputDocument(root+"test01.xml", null),
 			"UTF-8", "en", "en"));
 	}
 	
 	@Test
 	public void testStartDocumentFromList () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r"
 			+ "<doc>text</doc>";
 		StartDocument sd = FilterTestDriver.getStartDocument(getEvents(snippet));
 		assertNotNull(sd);
 		assertNotNull(sd.getEncoding());
 		assertNotNull(sd.getType());
 		assertNotNull(sd.getMimeType());
 		assertNotNull(sd.getLanguage());
 		assertEquals("\r", sd.getLineBreak());
 	}
 	
 	@Test
 	public void testOutputBasic_Comment () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><!--c--></doc>";
 		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 	
 	@Test
 	public void testOutputBasic_PI () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><?pi ?></doc>";
 		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 	
 	@Test
 	public void testOutputBasic_OneChar () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc>T</doc>";
 		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 	
 	@Test
 	public void testOutputBasic_EmptyRoot () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc/>";
 		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 	
 	@Test
 	public void testOutputSimpleContent () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><p>test</p></doc>";
 		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 
 	@Test
 	public void testOutputSimpleContent_WithEscapes () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><p>&amp;=amp, &lt;=lt, &quot;=quot..</p></doc>";
 		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 	
 	//TODO: Implement language handling
 /*	@Test
 	public void testOutputSimpleContent_WithLang () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc xml:lang='en'>test</doc>";
 		String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc xml:lang='FR'>test</doc>";
 		//TODO: Implement replacement of the lang value
 		//assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet), snippet, "FR"));
 	}*/
 	
 	@Test
 	public void testOutputSupplementalChars () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<p>[&#x20000;]=U+D840,U+DC00</p>";
 		String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<p>[\uD840\uDC00]=U+D840,U+DC00</p>";
 		assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 	
 	@Test
 	public void testCDATAParsing () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><p><![CDATA[&=amp, <=lt, &#xaaa;=not-a-ncr]]></p></doc>";
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals(tu.getSourceContent().toString(), "&=amp, <=lt, &#xaaa;=not-a-ncr");
 	}
 
 	@Test
 	public void testOutputCDATA () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><p><![CDATA[&=amp, <=lt, &#xaaa;=not-a-ncr]]></p></doc>";
 		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><p>&amp;=amp, &lt;=lt, &amp;#xaaa;=not-a-ncr</p></doc>";
 		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 
 	@Test
 	public void testCommentParsing () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><p>t1 <!--comment--> t2</p></doc>";
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals(fmt.setContent(tu.getSourceContent()).toString(), "t1 <1/> t2");
 	}
 
 	@Test
 	public void testOutputComment () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><p>t1 <!--comment--> t2</p></doc>";
 		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 
 	@Test
 	public void testPIParsing () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><p>t1 <?abc attr=\"value\"?> t2</p></doc>";
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals(fmt.setContent(tu.getSourceContent()).toString(), "t1 <1/> t2");
 	}
 	
 	@Test
 	public void testOutputPI () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><p>t1 <?abc attr=\"value\"?> t2</p></doc>";
 		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 
 	@Test
 	public void testOutputWhitespaces_Preserve () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><p>part 1\npart 2</p>"
 			+ "<p xml:space=\"preserve\">part 1\npart 2</p></doc>";
 		String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><p>part 1 part 2</p>"
 			+ "<p xml:space=\"preserve\">part 1\npart 2</p></doc>";
 		assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 	
 	@Test
 	public void testOutputWhitespaces_Default () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<p>part 1\npart 2\n  part3\n\t part4</p>";
 		String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<p>part 1 part 2 part3 part4</p>";
 		assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet), "en"));
 	}
 	
 	@Test
 	public void testSeveralUnits () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><p>text 1</p><p>text 2</p><p>text 3</p></doc>";
 		ArrayList<Event> list = getEvents(snippet);
 		TextUnit tu = FilterTestDriver.getTextUnit(list, 1);
 		assertNotNull(tu);
 		assertEquals("text 1", tu.getSource().toString());
 		tu = FilterTestDriver.getTextUnit(list, 2);
 		assertNotNull(tu);
 		assertEquals("text 2", tu.getSource().toString());
 		tu = FilterTestDriver.getTextUnit(list, 3);
 		assertNotNull(tu);
 		assertEquals("text 3", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testTranslatableAttributes () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
 			+ "<its:translateRule selector=\"//*/@text\" translate=\"yes\"/></its:rules>"
 			+ "<p text=\"value 1\">text 1</p><p>text 2</p><p>text 3</p></doc>";
 		ArrayList<Event> list = getEvents(snippet);
 		TextUnit tu = FilterTestDriver.getTextUnit(list, 1);
 		assertNotNull(tu);
 		assertEquals("value 1", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testDoubleExtraction () throws URISyntaxException {
 		// Read all files in the data directory
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		list.add(new InputDocument(root+"test01.xml", null));
 		list.add(new InputDocument(root+"test02.xml", null));
 		list.add(new InputDocument(root+"test03.xml", null));
 		list.add(new InputDocument(root+"test04.xml", null));
 		list.add(new InputDocument(root+"test05.xml", null));
 		list.add(new InputDocument(root+"test06.xml", null));
 		list.add(new InputDocument(root+"LocNote-1.xml", null));
 		list.add(new InputDocument(root+"LocNote-2.xml", null));
 		list.add(new InputDocument(root+"LocNote-3.xml", null));
 		list.add(new InputDocument(root+"LocNote-4.xml", null));
 		list.add(new InputDocument(root+"LocNote-5.xml", null));
 		list.add(new InputDocument(root+"LocNote-6.xml", null));
 		list.add(new InputDocument(root+"AndroidTest1.xml", "okf_xml@AndroidStrings.fprm"));
 		list.add(new InputDocument(root+"AndroidTest2.xml", "okf_xml@AndroidStrings.fprm"));
 		list.add(new InputDocument(root+"JavaProperties.xml", "okf_xml@JavaProperties.fprm"));
 		list.add(new InputDocument(root+"TestMultiLang.xml", null));
		list.add(new InputDocument(root+"Test01.resx", "okf_xml@RESX.fprm"));
 		list.add(new InputDocument(root+"MozillaRDFTest01.rdf", "okf_xml@MozillaRDF.fprm"));
 		list.add(new InputDocument(root+"XRTT-Source1.xml", null));
 		list.add(new InputDocument(root+"TestCDATA1.xml", null));
 
 		RoundTripComparison rtc = new RoundTripComparison();
 		assertTrue(rtc.executeCompare(filter, list, "UTF-8", "en", "en"));
 	}
 
 	private ArrayList<Event> getEvents(String snippet) {
 		ArrayList<Event> list = new ArrayList<Event>();
 		filter.open(new RawDocument(snippet, "en"));
 		while ( filter.hasNext() ) {
 			Event event = filter.next();
 			list.add(event);
 		}
 		filter.close();
 		return list;
 	}
 
 }
