 /*===========================================================================
   Copyright (C) 2009-2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.doxygen;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.TestUtil;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.common.filters.RoundTripComparison;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.filters.doxygen.DelimiterTokenizer.Token;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class DoxygenFilterTest {
 	
 	private DoxygenFilter filter;
 	private String root;
 		
 	@Before
 	public void setUp() {
 		filter = new DoxygenFilter();
 		filter.setOptions(LocaleId.ENGLISH, LocaleId.SPANISH, "UTF-8", true);
 		root = TestUtil.getParentDir(this.getClass(), "/sample.h");
 	}
 	
 	@Test
 	public void testDefaultInfo () {		
 		assertNotNull(filter.getName());
 		assertNotNull(filter.getDisplayName());
 		List<FilterConfiguration> list = filter.getConfigurations();
 		assertNotNull(list);
 		assertTrue(list.size()>0);
 	}
 	
 	@Test
 	public void testStartDocument () {
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
 			new InputDocument(root + "sample.h", null),
 			"UTF-8", LocaleId.ENGLISH, LocaleId.FRENCH));
 	}
 
 	@Test
 	public void testSimpleLine() {
 		String snippet = "foo foo foo /// This is a test.";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("This is a test.", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testMultipleLines() {
 		String snippet = "foo foo foo /// This is \nbar bar bar /// a test.\n baz baz baz /// ";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("This is a test.", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testOneLiner() {
 		String snippet = "int foo; ///< This is a test. \nint bar; ///< New paragraph.";
 		ArrayList<Event> events = getEvents(snippet);
 		ITextUnit tu1 = FilterTestDriver.getTextUnit(events, 1);
 		assertNotNull(tu1);
 		assertEquals("This is a test.", tu1.getSource().toString());
 		ITextUnit tu2 = FilterTestDriver.getTextUnit(events, 2);
 		assertNotNull(tu2);
 		assertEquals("New paragraph.", tu2.getSource().toString());
 	}
 	
 	@Test
 	public void testJavadocLine() {
 		String snippet = "int foo; /** This is a test. */";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("This is a test.", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testJavadocMultiline() {
 		String snippet = "int foo; /** \n"
 				+ "  * This is \n"
 				+ "  * a test.\n"
 				+ "  */";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("This is a test.", tu.getSource().toString());
 	}
 	
 	@Test
 	public void testDoxygenClassCommand1() {
 		/* class: 
 		    type: PLACEHOLDER
 		    parameters: 
 		      - name: name 
 		        length: WORD
 		        required: true
 		        translatable: false
 		      - name: header-file 
 		        length: WORD
 		        required: false
 		        translatable: false
 		      - name: header-name 
 		        length: WORD
 		        required: false
 		        translatable: false
 		 */
 		String snippet = "int foo; /** \\class MyClass MyClass.h \"inc/class.h\" \n This is a test. */";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("This is a test.", tu.getSource().getCodedText());
 	}
 	
 	@Test
 	public void testDoxygenClassCommand2() {
 		// This time an optional parameter is missing.
 		String snippet = "int foo; /** \\class MyClass MyClass.h \n This is a test. */";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("This is a test.", tu.getSource().getCodedText());
 	}
 	
 	@Test
 	public void testDoxygenCodeCommand() {
 		/* code: 
 		    type: OPENING
 		    translatable: false
 		    pair: endcode
 		 */
 		String snippet = "int foo; /** \\code \n blahblahblah\n \\endcode\n This is a test. */";
 		ArrayList<Event> events = getEvents(snippet);
 		ITextUnit tu1 = FilterTestDriver.getTextUnit(events, 1);
 		assertNotNull(tu1);
 		assertTrue(!tu1.isTranslatable());
 		ITextUnit tu2 = FilterTestDriver.getTextUnit(events, 2);
 		assertNotNull(tu2);
 		assertTrue(tu2.isTranslatable());
 		assertEquals("This is a test.", tu2.getSource().getCodedText());;
 	}
 	
 	@Test
 	public void testDoxygenItalicCommand() {
 		/* a: 
 		    type: PLACEHOLDER
 		    inline: true
 		    parameters: 
 		      - name: word 
 		        length: WORD
 		        required: true
 		        translatable: true
 		 */
 		String snippet = "int foo; /** This is a \\a test. */";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("This is a  test.", tu.getSource().getCodedText());
 	}
 	
 	@Test
 	public void testDoxygenImageCommand() {
 		/* image: 
 		    type: PLACEHOLDER
 		    parameters: 
 		      - name: format 
 		        length: WORD
 		        required: true
 		        translatable: false
 		      - name: file 
 		        length: WORD
 		        required: true
 		        translatable: false
 		      - name: caption 
 		        length: PHRASE
 		        required: false
 		        translatable: true
 		      - name: <sizeindication>=<size> 
 		        length: WORD
 		        required: false
 		        translatable: false
 		 */
 		String snippet = "int foo; /** \\image format file.ext \"This is a test.\" width=10cm */";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals(" \"This is a test.\"", tu.getSource().getCodedText());
 	}
 	
 	@Test
 	public void testHtmlBoldCommand() {
 		/* b:
 		    type: OPENING
 		    inline: true
 		 */
 		String snippet = "int foo; /** This is a <b>test</b>. */";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("This is a test.", tu.getSource().getCodedText());
 	}
 	
 	@Test
 	public void testOutputSimpleLine() {
 		String snippet = "foo foo foo /// This is a test.\n";
 		String expected = "foo foo foo /// This is a test.\n"; 
 		String result = FilterTestDriver.generateOutput(getEvents(snippet),
 			filter.getEncoderManager(), LocaleId.ENGLISH);
 		assertEquals(expected, result);
 	}
 	
 	@Test
 	public void testOutputOneLiner() {
 		String snippet = "int foo; ///< This is a test. \n"
 					   + "int bar; ///< New paragraph.";
 		String expected = "int foo; ///int bar; ///< This is a test. \n< New paragraph.\n";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet),
 				filter.getEncoderManager(), LocaleId.FRENCH);
 		assertEquals(expected, result);
 	}
 
 	@Test
 	public void testOutputMultipleLines () {
 		String snippet = "foo foo foo /// This is \n"
 					   + "bar bar bar /// a test.\n"
 					   + "baz baz baz /// ";
 		// Expected string looks wonky because the filter does a lot of
 		// skeleton manipulation. The rationale here is:
 		//    [foo foo foo ///]{ This is \n}
 		//    [bar bar bar ///]{ a test.\n}
 		//    [baz baz baz ///]{ }
 		// [Bracketed parts] are skeleton and are ouput first; {curly braced parts}
 		// are comment pieces, which have outer whitespace preserved but inner
 		// whitespace deflated. Skeleton comes first, then comment.
 		String expected = "foo foo foo ///bar bar bar ///baz baz baz /// This is a test.\n \n";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet),
 			filter.getEncoderManager(), LocaleId.FRENCH);
 		assertEquals(expected, result);
 	}
 	
 	@Test
 	public void testOutputJavadocMultipleLines() {
 		String snippet = "foo foo foo /** \n"
 					+ "  * This is \n"
 					+ "  * a test.\n"
 				    + "  */ ";
 		String expected = "foo foo foo /** \n  *   * This is a test.\n  */ \n";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet),
 			filter.getEncoderManager(), LocaleId.FRENCH);
 		assertEquals(expected, result);
 	}
 
 	
 	@Test
 	public void testDoubleExtractionSample() throws URISyntaxException {
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		list.add(new InputDocument(root+"sample.h", null));		
 		RoundTripComparison rtc = new RoundTripComparison(false);
 		assertTrue(rtc.executeCompare(filter, list, "utf-8", LocaleId.ENGLISH, LocaleId.ENGLISH));
 	}
 	
 	@Test
 	public void testDoubleExtractionQtStyle() throws URISyntaxException {
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		list.add(new InputDocument(root+"qt-style.h", null));		
 		RoundTripComparison rtc = new RoundTripComparison(false);
 		assertTrue(rtc.executeCompare(filter, list, "utf-8", LocaleId.ENGLISH, LocaleId.ENGLISH));
 	}
 	
 	@Test
 	public void testDoubleExtractionJavadocStyle() throws URISyntaxException {
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		list.add(new InputDocument(root+"javadoc-style.h", null));		
 		RoundTripComparison rtc = new RoundTripComparison(false);
 		assertTrue(rtc.executeCompare(filter, list, "utf-8", LocaleId.ENGLISH, LocaleId.ENGLISH));
 	}
 	
 	@Test
 	public void testDoubleExtractionSpecialCommands() throws URISyntaxException {
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		list.add(new InputDocument(root+"special_commands.h", null));		
 		RoundTripComparison rtc = new RoundTripComparison(false);
 		assertTrue(rtc.executeCompare(filter, list, "utf-8", LocaleId.ENGLISH, LocaleId.ENGLISH));
 	}
 	
 	@Test
 	public void testOpenTwiceWithString() {
 		RawDocument rawDoc = new RawDocument("|vtest", LocaleId.ENGLISH);
 		filter.open(rawDoc);
 		filter.open(rawDoc);
 		filter.close();
 	}
 	
 	@Test
 	public void testDelimiterTokenizer() {
 		IdentityHashMap<Pattern, Object> set = new IdentityHashMap<Pattern, Object>();
 		set.put(Pattern.compile("(?=\\d)"), null);
 		set.put(Pattern.compile("(?<=\\d)"), null);
 		
 		DelimiterTokenizer t = new DelimiterTokenizer(set, "foo1bar");
 		
 		assertTrue(t.iterator().hasNext());
 		Token u = t.iterator().next();
 		assertEquals(u.delimiter(), null);
 		assertEquals(u.toString(), "foo");
 		u = t.iterator().next();
 		assertEquals(u.delimiter(), "");
 		assertEquals(u.toString(), "1");
 		u= t.iterator().next();
 		assertEquals(u.delimiter(), "");
 		assertEquals(u.toString(), "bar");
 		assertTrue(!t.iterator().hasNext());
 	}
 
 	@Test
 	public void testPrefixSuffixTokenizer() {
 		IdentityHashMap<Pattern, Pattern> set = new IdentityHashMap<Pattern, Pattern>();
 		set.put(Pattern.compile("(?=1)"), Pattern.compile("(?=2)"));
 		set.put(Pattern.compile("(?=2)"), Pattern.compile("(?=4)"));
 		
 		PrefixSuffixTokenizer t = new PrefixSuffixTokenizer(set, "1foo2bar3baz4");
 		
 		assertTrue(t.iterator().hasNext());
 		// 0
 		PrefixSuffixTokenizer.Token u = t.iterator().next();
 		assertEquals(u.prefix(), null);
 		assertEquals(u.toString(), "");
 		assertEquals(u.suffix(), "");
 		// 1
 		u = t.iterator().next();
 		assertEquals(u.prefix(), "");
 		assertEquals(u.toString(), "1foo");
 		assertEquals(u.suffix(), "");
 		// 2
 		u = t.iterator().next();
 		assertEquals(u.prefix(), "");
 		assertEquals(u.toString(), "");
 		assertEquals(u.suffix(), "");
 		// 3
 		u = t.iterator().next();
 		assertEquals(u.prefix(), "");
 		assertEquals(u.toString(), "2bar3baz");
 		assertEquals(u.suffix(), "");
 		// 4
 		u = t.iterator().next();
 		assertEquals(u.prefix(), "");
 		assertEquals(u.toString(), "4");
 		assertEquals(u.suffix(), null);
 		assertTrue(!t.iterator().hasNext());
 	}
 	
 	private ArrayList<Event> getEvents (String snippet) {
 		ArrayList<Event> list = new ArrayList<Event>();		
 		filter.open(new RawDocument(snippet, LocaleId.ENGLISH, LocaleId.SPANISH));
 		while (filter.hasNext()) {
 			Event event = filter.next();
 			list.add(event);
 		}
 		filter.close();
 		return list;
 	}
 }
