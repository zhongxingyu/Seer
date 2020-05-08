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
 
 package net.sf.okapi.filters.regex;
 
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.common.filters.RoundTripComparison;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.ISkeleton;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.TestUtil;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.ITextUnit;
 import org.junit.Assert;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 public class RegexFilterTest {
 	
 	private RegexFilter filter;
 	private String root;
 	private LocaleId locEN = LocaleId.fromString("en");
 	private LocaleId locFR = LocaleId.fromString("fr");
 	private LocaleId locFRCA = LocaleId.fromString("fr-ca");
 	
 	@Before
 	public void setUp() {
 		filter = new RegexFilter();
 		root = TestUtil.getParentDir(this.getClass(),"/Test01_stringinfo_en.info" );
 	}
 
 	@Test
 	public void testStartDocument () {
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
 			new InputDocument(root+"Test01_stringinfo_en.info", null),
 			"UTF-8", locEN, locEN));
 	}
 	
 	@Test
 	public void testDoubleExtraction () {
 		// Read all files in the data directory
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		list.add(new InputDocument(root+"Test01_srt_en.srt", "okf_regex@SRT.fprm"));
 		list.add(new InputDocument(root+"Test01_stringinfo_en.info", "okf_regex@StringInfo.fprm"));
 		list.add(new InputDocument(root+"TestRules01.txt", "okf_regex@TestRules01.fprm"));
 		list.add(new InputDocument(root+"TestRules02.txt", "okf_regex@TestRules02.fprm"));
 		list.add(new InputDocument(root+"TestRules03.txt", "okf_regex@TestRules03.fprm"));
 		list.add(new InputDocument(root+"TestRules04.txt", "okf_regex@TestRules04.fprm"));
 		list.add(new InputDocument(root+"TestRules05.txt", "okf_regex@TestRules05.fprm"));
 		list.add(new InputDocument(root+"TestRules06.txt", "okf_regex@TestRules06.fprm")); 
 		RoundTripComparison rtc = new RoundTripComparison();
 		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locFR));
 
 		list.clear();
 		list.add(new InputDocument(root+"TestFrenchISL.isl", "okf_regex@INI.fprm")); 
 		assertTrue(rtc.executeCompare(filter, list, "Windows-1252", locFR, locFRCA));
 	}
 
 	@Test
 	public void testConfigurations () {
 		List<FilterConfiguration> list = filter.getConfigurations();
 		assertNotNull(list);
 		assertEquals(4, list.size());
 		IParameters params = filter.getParameters();
 		assertNotNull(params);
 		for ( FilterConfiguration config : list ) {
 			if ( config.parametersLocation == null ) continue; // Default
 			URL url = filter.getClass().getResource(config.parametersLocation);
 			try {
 				params.load(url.toURI(), false);
 			} catch (URISyntaxException e) {
 				e.printStackTrace();
 			}
 			assertNotNull(params.toString());
 		}
 	}
 	
 	@Test
 	public void testSimpleRule () {
 		String snippet = "test1=\"text1\"\ntest2=\"text2\"\n";
 		Parameters params = new Parameters();
 		Rule rule = new Rule();
 		rule.setRuleType(Rule.RULETYPE_STRING);
 		rule.setExpression("=(.+)$");
 		rule.setSourceGroup(1);
 		params.getRules().add(rule);
 		filter.setParameters(params);
 		// Process
 		ArrayList<Event> list = getEvents(snippet);
 		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
 		assertNotNull(tu);
 		assertEquals("text1", tu.getSource().toString());
 		tu = FilterTestDriver.getTextUnit(list, 2);
 		assertNotNull(tu);
 		assertEquals("text2", tu.getSource().toString());
 	}
 
 	@Test
 	public void testIDAndText () {
 		String snippet = "[Id1]\tText1\r\n[Id2]\tText2";
 		Parameters params = new Parameters();
 		Rule rule = new Rule();
 		rule.setRuleType(Rule.RULETYPE_CONTENT);
 		rule.setExpression("^\\[(.*?)]\\s*(.*?)(\\n|\\Z)");
 		rule.setSourceGroup(2);
 		rule.setNameGroup(1);
 		rule.setPreserveWS(true);
 		params.getRules().add(rule);
 		filter.setParameters(params);
 		// Process
 		ArrayList<Event> list = getEvents(snippet);
 		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
 		assertNotNull(tu);
 		assertEquals("Text1", tu.getSource().toString());
 		assertEquals("Id1", tu.getName());
 		tu = FilterTestDriver.getTextUnit(list, 2);
 		assertNotNull(tu);
 		assertEquals("Text2", tu.getSource().toString());
 		assertEquals("Id2", tu.getName());
 	}
 
 	@Test
 	public void testEscapeDoubleChar () {
 		String snippet = "id = [\"\"\"a\"\"b\"\"c\"\"\"]";
 		Parameters params = new Parameters();
 		Rule rule = new Rule();
 		rule.setRuleType(Rule.RULETYPE_CONTENT);
 		rule.setExpression("^.*?\\[(.*?)]");
 		rule.setSourceGroup(1);
 		rule.setRuleType(Rule.RULETYPE_STRING);
 		params.getRules().add(rule);
 		params.setUseDoubleCharEscape(true);
 		filter.setParameters(params);
 		// Process
 		ArrayList<Event> list = getEvents(snippet);
 		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
 		assertNotNull(tu);
 		assertEquals("\"\"a\"\"b\"\"c\"\"", tu.getSource().toString());
 	}
 
 	private ArrayList<Event> getEvents(String snippet) {
 		ArrayList<Event> list = new ArrayList<Event>();
 		filter.open(new RawDocument(snippet, locEN));
 		while (filter.hasNext()) {
 			Event event = filter.next();
 			list.add(event);
 		}
 		filter.close();
 		return list;
 	}
 
 	@Test
 	public void testEmptyLines() {
 		Parameters params = new Parameters();
 		Rule rule = new Rule();
 		rule.setRuleType(Rule.RULETYPE_CONTENT);
 		params.setRegexOptions(Pattern.MULTILINE);
 		
 		rule.setExpression("^(.*?)$");
 		rule.setSourceGroup(1);
 		
 		//rule.setExpression("(^(?=.+))(.*?)$");
 		//rule.setSourceGroup(2);
 		
 		params.getRules().add(rule);
 		filter.setParameters(params);
 						
 		String inputText = "Line 1\n\nLine 2\n\n\n\n\nLine 3\n\n\nLine 4";
 		//                  0123456 7 8901234 5 6 7 8 9012345 6 7 890123 
 		//                  0           1              2            3
 				
 		listEvents(inputText);
 		
 		// Test individual events
 		filter.open(new RawDocument(inputText, locEN));
 		
 		testEvent(EventType.START_DOCUMENT, "");
 		testEvent(EventType.TEXT_UNIT, "Line 1");
 		testEvent(EventType.DOCUMENT_PART, "\n\n");
 		testEvent(EventType.TEXT_UNIT, "Line 2");
 		testEvent(EventType.DOCUMENT_PART, "\n\n\n\n\n");
 		testEvent(EventType.TEXT_UNIT, "Line 3");
 		testEvent(EventType.DOCUMENT_PART, "\n\n\n");
 		testEvent(EventType.TEXT_UNIT, "Line 4");
 		testEvent(EventType.END_DOCUMENT, "");
 		
 		
 		String inputText2 = "Line 1\nLine 2\n\nLine 3\n\n\nLine 4\n\n\n\n\n\n";
 		//                   0123456 7890123 4 5678901 2 3 4567890 1 2 3 4 5  
 		//                   0          1           2            3
 		
 		filter.open(new RawDocument(inputText2, locEN));
 		
 		testEvent(EventType.START_DOCUMENT, "");
 		testEvent(EventType.TEXT_UNIT, "Line 1");
 		testEvent(EventType.DOCUMENT_PART, "\n");
 		testEvent(EventType.TEXT_UNIT, "Line 2");
 		testEvent(EventType.DOCUMENT_PART, "\n\n");
 		testEvent(EventType.TEXT_UNIT, "Line 3");
 		testEvent(EventType.DOCUMENT_PART, "\n\n\n");
 		testEvent(EventType.TEXT_UNIT, "Line 4");
 		testEvent(EventType.DOCUMENT_PART, "\n\n\n\n\n\n");
 		testEvent(EventType.END_DOCUMENT, "");
 	}	
 	
 	private void testEvent(EventType expectedType, String expectedText) {
 		assertNotNull(filter);
 		Event event = filter.next();		
 		assertNotNull(event);
 		assertTrue(event.getEventType() == expectedType);
 
         switch (event.getEventType()) {
             case TEXT_UNIT:
                 IResource res = event.getResource();
                 assertTrue(res instanceof ITextUnit);
                 assertEquals(res.toString(), expectedText);
                 break;
 
             case DOCUMENT_PART:
                 res = event.getResource();
                 assertTrue(res instanceof DocumentPart);
                 ISkeleton skel = res.getSkeleton();
                 if (skel != null) {
                     assertEquals(skel.toString(), expectedText);
                 }
                 break;
             case CANCELED:
                 break;
             case CUSTOM:
                 break;
             case END_BATCH:
                 break;
             case END_BATCH_ITEM:
                 break;
             case END_DOCUMENT:
                 break;
             case END_GROUP:
                 break;
             case END_SUBDOCUMENT:
                 break;
             case NO_OP:
                 break;
             case RAW_DOCUMENT:
                 break;
             case START_BATCH:
                 break;
             case START_BATCH_ITEM:
                 break;
             case START_DOCUMENT:
                 break;
             case START_GROUP:
                 break;
             case START_SUBDOCUMENT:
                 break;
         }
 	}
 		
 	private void listEvents(String inputText) { 
 		// List all events in Console
 		FilterTestDriver testDriver = new FilterTestDriver();
 		testDriver.setDisplayLevel(0);
 		testDriver.setShowSkeleton(true);
 		
 		filter.open(new RawDocument(inputText, locEN));
 		if (!testDriver.process(filter)) Assert.fail();
 		filter.close();
 	}
 	
 }
 
