 package net.sf.okapi.filters.xmlstream;
 
 import java.util.ArrayList;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.filters.xmlstream.XmlStreamFilter;
 
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public class XmlStreamConfigurationSupportTest {
 
 	private XmlStreamFilter filter = new XmlStreamFilter();
 	private LocaleId locEN = LocaleId.ENGLISH;
 	private LocaleId locFR = LocaleId.FRENCH;
 	
 	@Test
 	public void test_collapse_whitespace () {
 		String config = "preserve_whitespace: false";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<p> t1  \nt2  </p>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t1 t2", tu.getSource().toString());
 
 		config = "preserve_whitespace: true";
 		filter.setParameters(new Parameters(config));
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals(" t1  \nt2  ", tu.getSource().toString());
 	}
 
 	@Test
 	public void test_PRESERVE_WHITESPACE () {
 		String config = 
 				"preserve_whitespace: false\n" +
 			    "elements:\n" +
 				"  pre: \n" +
 			    "    ruleTypes: [PRESERVE_WHITESPACE]";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<p> t1  \nt2  </p><pre> t3  \nt4  </pre>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t1 t2", tu.getSource().toString());
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
 		assertEquals(" t3  \nt4  ", tu.getSource().toString());
 	}
 	
 	@Test
 	public void test_MATCHES() {
 		String config = 
 			"elements:\n" +
 			"  p:\n" +
 			"    ruleTypes: [EXCLUDE]\n" +
 			"    conditions: [x, MATCHES, 'ABZ']";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<p x='ABZ'>t1</p><p x='ZBA'>t2</p>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t2", tu.toString());		
 	}
 	
 	@Test
 	public void test_INLINE_with_positive_condition() {
 		String config = 
 			    "elements:\n" +
 				"  b: \n" +
 			    "    ruleTypes: [INLINE]\n" +
 			    "    conditions: [x, EQUALS, 'true']";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<p><b x=\"true\">t2</b></p>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("<b x=\"true\">t2</b>", tu.getSource().toString());
 	}
 	
 	@Test
 	public void test_INLINE_with_negative_condition() {
 		String config = 
 			    "elements:\n" +
 				"  b: \n" +
 			    "    ruleTypes: [INLINE]\n" +
 			    "    conditions: [x, EQUALS, 'true']";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<p><b x=\"false\">t2</b></p>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t2", tu.getSource().toString());
 	}
 
 	@Test
 	public void test_EXCLUDE_with_positive_condition() {
 		String config = 
 			    "elements:\n" +
 				"  pre: \n" +
 			    "    ruleTypes: [EXCLUDE]\n" +
 			    "    conditions: [x, EQUALS, 'true']";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<pre x = \"true\">t1</pre><p>t2</p>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t2", tu.getSource().toString());
 	}
 	
 	@Test
 	public void test_EXCLUDE_with_positive_condition_and_regex() {
 		String config = 
 			    "elements:\n" +
 				"  '.+': \n" +
 			    "    ruleTypes: [EXCLUDE]\n" +
 			    "    conditions: [translate, EQUALS, 'no']";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<pre translate=\"no\">t1</pre><p>t2</p>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t2", tu.getSource().toString());
 	}
 
 	@Test
 	public void test_EXCLUDE_with_negative_condition() {
 		String config = 
 			    "elements:\n" +
 				"  pre: \n" +
 			    "    ruleTypes: [EXCLUDE]\n" +
 			    "    conditions: [x, EQUALS, 'false']";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<pre x =\"true\">t1</pre><p>t2</p>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t1", tu.getSource().toString());
 	}
 	
 	@Test
 	public void test_EXCLUDE () {
 		String config = 
 			    "elements:\n" +
 				"  pre: \n" +
 			    "    ruleTypes: [EXCLUDE]";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<pre>t1</pre><p>t2</p>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t2", tu.getSource().toString());
 	}
 	
 	@Test
 	public void test_EXCLUDEWithRegexExcludeWithoutAttribute() {
 		String config = 
 			    "elements:\n" +
 				"  prolog: \n" +
 			    "    ruleTypes: [EXCLUDE]\n" +
 				"  '.*':\n" +
 				"    ruleTypes: [EXCLUDE]\n" +
 				"    conditions: [translate, EQUALS, 'no']";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<prolog><author>xyz</author></prolog>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertNull(tu);
 	}
 	
 	@Test
 	public void test_EXCLUDEWithRegexExcludeWithAttribute() {
 		String config = 
 			    "elements:\n" +
 				"  prolog: \n" +
 			    "    ruleTypes: [EXCLUDE]\n" +
 				"  '.*':\n" +
 				"    ruleTypes: [INCLUDE]\n" +
 				"    conditions: [translate, EQUALS, 'yes']";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<prolog><author translate=\"yes\">xyz</author></prolog>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertNotNull(tu);
 		
 		snippet = "<prolog><author translate=\"no\">xyz</author></prolog>";
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertNull(tu);
 	}
 	
 	@Test
 	public void test_INCLUDE () {
 		String config = 
 			"elements:\n" +
 			"  pre: \n" +
 			"    ruleTypes: [EXCLUDE] \n" +
 			"  b: \n" +
 			"    ruleTypes: [INCLUDE]";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<pre>t1<b>t2</b>t3</pre><p>t4</p>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t2", tu.getSource().toString());
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
 		assertEquals("t4", tu.getSource().toString());
 	}
 
 	@Test
 	public void test_ATTRIBUTE_ID () {
 		String config = 
 			"attributes:\n" +
 			"  id: \n" +
 			"    ruleTypes: [ATTRIBUTE_ID]\n" +
 			"elements:\n" +
 			"  p:\n" +
 			"    ruleTypes: [TEXTUNIT]\n" +
 			"  pre:\n" +
 			"    ruleTypes: [TEXTUNIT]\n";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<p id='id1'>t1</p><pre id='id2'>t2</pre>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t1", tu.getSource().toString());
 		assertEquals("id1-id", tu.getName());
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
 		assertEquals("t2", tu.getSource().toString());
 		assertEquals("id2-id", tu.getName());
 	}
 
 	@Test
 	public void test_idAttributes () {
 		String config = 
 			"elements:\n" +
 			"  p:\n" +
 			"    ruleTypes: [TEXTUNIT]\n" +
 			"    idAttributes: [id, 'xml:id']";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<p id='id1'>t1</p><p xml:id='id2'>t2</p>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t1", tu.getSource().toString());
 		assertEquals("id1-id", tu.getName());
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
 		assertEquals("t2", tu.getSource().toString());
 		assertEquals("id2-xml:id", tu.getName());
 	}
 
 	@Test
 	public void test_allElementsExcept () {
 		String config = 
 			"attributes:\n" +
 			"  alt:\n" +
 			"    ruleTypes: [ATTRIBUTE_TRANS]\n" +
 			"    allElementsExcept: [elem2, elem3]";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<elem1 alt='t1'>t2</elem1><elem2 alt='t3'>t4</elem2><elem3 alt='t5'>t6</elem3>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t1", tu.getSource().toString()); // alt
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
 		assertEquals("t2", tu.getSource().toString());
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 3);
 		assertEquals("t4", tu.getSource().toString());
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 4);
 		assertEquals("t6", tu.getSource().toString());
 	}
 
 	@Test
 	public void test_onlyTheseElements () {
 		String config = 
 			"attributes:\n" +
 		    "  alt:\n" +
 		    "    ruleTypes: [ATTRIBUTE_TRANS]\n" +
 			"    onlyTheseElements: [elem1, elem3]"; // only in elem1 and elem3, not elem2
 		filter.setParameters(new Parameters(config));
 		String snippet = "<elem1 alt='t1'>t2</elem1><elem2 alt='t3'>t4</elem2><elem3 alt='t5'>t6</elem3>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t1", tu.getSource().toString()); // alt of elem1
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
 		assertEquals("t2", tu.getSource().toString()); // elem1
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 3);
 		assertEquals("t4", tu.getSource().toString()); // elem2
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 4);
 		assertEquals("t5", tu.getSource().toString()); // alt of elem3
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 5);
 		assertEquals("t6", tu.getSource().toString()); // elem3
 	}
 	
 	@Test
 	public void test_translatableAttributes_withCondition () {
 		String config = 
 			"elements:\n" +
 			"  p: \n" +
 			"    ruleTypes: [TEXTUNIT]\n" +
 			"    translatableAttributes: {alt: [attr1, EQUALS, trans]}";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<p alt='t1' attr1='NOTRANS'>t2</p><p alt='t-alt' attr1='trans'>t4</p>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
 		assertEquals("t-alt", tu.getSource().toString());
 	}
 	
 	@Test
 	public void test_translatableAttributes_with2ORConditions () {
 		String config = 
 			"elements:\n" +
 			"  p: \n" +
 			"    ruleTypes: [TEXTUNIT]\n" +
 			"    translatableAttributes: {alt: [[attr1, EQUALS, trans], [attr2, EQUALS, 'yes']]}";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<p alt='t-alt1' attr2='yes'>t2</p><p alt='t-alt2' attr1='trans'>t4</p>";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t-alt1", tu.getSource().toString());
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 3);
 		assertEquals("t-alt2", tu.getSource().toString());
 	}
 	
 	@Test
 	public void test_ATTRIBUTE_WRITABLE () {
 		String config = 
 			"attributes:\n" +
 			"  dir: \n" +
 			"    ruleTypes: [ATTRIBUTE_WRITABLE]\n" +
 			"elements:\n" +
 			"  p:\n" +
 			"    ruleTypes: [TEXTUNIT]"; 
 		filter.setParameters(new Parameters(config));
 		String snippet = "<p dir='rtl'>t1</p><pre dir='ltr'>t2</pre>";
 		// p is defined as TEXTUNIT so the property is with the TU
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t1", tu.getSource().toString());
 		assertNotNull(tu.getSource().getProperty("dir"));
 		assertEquals("rtl", tu.getSource().getProperty("dir").getValue());
 		// pre is not defined as TEXTUNIT so the property is with the skeleton in the previous document part
 		DocumentPart dp = FilterTestDriver.getDocumentPart(getEvents(snippet, locEN, locFR), 1);
 		assertNotNull(dp.getSourceProperty("dir"));
 		assertEquals("ltr", dp.getSourceProperty("dir").getValue());
 	}
 
 	@Test
 	public void test_regex_ATTRIBUTE_WRITABLE () {
 		String config = 
 			"attributes:\n" +
 			"  '.+': \n" +
 			"    ruleTypes: [ATTRIBUTE_WRITABLE]\n" +
 			"elements:\n" +
 			"  '.+':\n" +
 			"    ruleTypes: [TEXTUNIT]";
 		filter.setParameters(new Parameters(config));
 		String snippet = "<p dir='rtl'>t1</p><pre dir='ltr'>t2</pre>";
 		// p is defined as TEXTUNIT so the property is with the TU
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
 		assertEquals("t1", tu.getSource().toString());
 		assertNotNull(tu.getSource().getProperty("dir"));
 		assertEquals("rtl", tu.getSource().getProperty("dir").getValue());
 		// pre is also defined as TEXTUNIT 
 		ITextUnit tu2 = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
 		assertEquals("t2", tu2.getSource().toString());
 		assertNotNull(tu2.getSource().getProperty("dir"));
 		assertEquals("ltr", tu2.getSource().getProperty("dir").getValue());
 	}
 	
 	@Test
 	public void test_INLINE_WITH_EXCLUDE () {
 		String config = 
 			    "elements:\n" +
 				"  foo: \n" +
 			    "    ruleTypes: [INLINE, EXCLUDE]";
 		filter.setParameters(new Parameters(config));
 		String snippet = "test1<foo>remove</foo>test2";
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertEquals("test1removetest2", tu.getSource().getCodedText());
 	}
 	
 	private ArrayList<Event> getEvents(String snippet,
 		LocaleId srcLang,
 		LocaleId trgLang)
 	{
 		ArrayList<Event> list = new ArrayList<Event>();
 		filter.open(new RawDocument(snippet, srcLang, trgLang));
 		while ( filter.hasNext() ) {
 			Event event = filter.next();
 			list.add(event);
 		}
 		filter.close();
 		return list;
 	}
 }
