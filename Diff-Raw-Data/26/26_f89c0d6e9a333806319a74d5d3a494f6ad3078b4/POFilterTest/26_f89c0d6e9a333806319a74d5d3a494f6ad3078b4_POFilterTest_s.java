 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.po;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.io.File;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.common.filters.RoundTripComparison;
 import net.sf.okapi.filters.po.POFilter;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class POFilterTest {
 	
 	private POFilter filter;
 	private String root;
 	
 	@Before
 	public void setUp() {
 		filter = new POFilter();
 		URL url = POFilterTest.class.getResource("/Test01.po");
 		root = Util.getDirectoryName(url.getPath()) + File.separator;
 	}
 
 	@Test
 	public void testPOHeader () {
		String snippet = "msgid \"\"\r"
 			+ "msgstr \"\"\r"
 			+ "\"Project-Id-Version: PACKAGE VERSION\\n\"\r"
 			+ "\"Report-Msgid-Bugs-To: \\n\"\r"
 			+ "\"POT-Creation-Date: 2009-03-25 15:39-0700\\n\"\r"
 			+ "\"PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE\\n\"\r"
 			+ "\"Last-Translator: FULL NAME <EMAIL@ADDRESS>\\n\"\r"
 			+ "\"Language-Team: LANGUAGE <LL@li.org>\\n\"\r"
 			+ "\"MIME-Version: 1.0\\n\"\r"
 			+ "\"Content-Type: text/plain; charset=UTF-8\\n\"\r"
 			+ "\"Content-Transfer-Encoding: 8bit\\n\"\r"
 			+ "\"Plural-Forms: nplurals=2; plural=(n!=1);\\n\"\r\r"
 			+ "msgid \"Text\"\r"
 			+ "msgstr \"Texte\"\r";
 		DocumentPart dp = FilterTestDriver.getDocumentPart(getEvents(snippet, "en", "fr"), 1);
 		assertNotNull(dp);
 
 		Property prop = dp.getProperty(Property.ENCODING);
 		assertNotNull(prop);
 		assertEquals("UTF-8", prop.getValue());
 		assertFalse(prop.isReadOnly());
 
 		prop = dp.getProperty(POFilter.PROPERTY_PLURALFORMS);
 		assertNotNull(prop);
 		assertEquals("nplurals=2; plural=(n!=1);", prop.getValue());
 		assertFalse(prop.isReadOnly());
 		
 		String result = FilterTestDriver.generateOutput(getEvents(snippet, "en", "fr"), "fr");
 		assertEquals(snippet, result);
 	}
 
 
 	@Test
 	public void testPOTHeader () {
 		String snippet = "msgid \"\"\n"
 			+ "msgstr \"\"\n"
 			+ "\"Project-Id-Version: PACKAGE VERSION\\n\"\n"
 			+ "\"Report-Msgid-Bugs-To: \\n\"\n"
 			+ "\"POT-Creation-Date: 2009-03-25 15:39-0700\\n\"\n"
 			+ "\"PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE\\n\"\n"
 			+ "\"Last-Translator: FULL NAME <EMAIL@ADDRESS>\\n\"\n"
 			+ "\"Language-Team: LANGUAGE <LL@li.org>\\n\"\n"
 			+ "\"MIME-Version: 1.0\\n\"\n"
 			+ "\"Content-Type: text/plain; charset=ENCODING\\n\"\n"
 			+ "\"Content-Transfer-Encoding: 8bit\\n\"\n"
 			+ "msgid \"Text\"\n"
 			+ "msgstr \"\"\n";
 		DocumentPart dp = FilterTestDriver.getDocumentPart(getEvents(snippet, "en", "fr"), 1);
 		assertNotNull(dp);
 
 		Property prop = dp.getProperty(Property.ENCODING);
 		assertNotNull(prop);
 		assertEquals("ENCODING", prop.getValue());
 		assertFalse(prop.isReadOnly());
 
 		prop = dp.getProperty(POFilter.PROPERTY_PLURALFORMS);
 		assertNull(prop);
 	}
 
 	@Test
 	public void testHeaderNoNPlurals () {
 		String snippet = "msgid \"\"\n"
 			+ "msgstr \"\"\n"
 			+ "\"MIME-Version: 1.0\\n\"\n"
 			+ "\"Content-Type: text/plain; charset=ENCODING\\n\"\n"
 			+ "\"Content-Transfer-Encoding: 8bit\\n\"\n"
 			+ "\"Plural-Forms: nplurzzzals=2; plural=(n!=1);\\n\"\n\n"
 			+ "msgid \"Text\"\n"
 			+ "msgstr \"\"\n";
 		DocumentPart dp = FilterTestDriver.getDocumentPart(getEvents(snippet, "en", "fr"), 1);
 		assertNotNull(dp);
 		// We should also get a warning about the nplurals field missing
 		Property prop = dp.getProperty(POFilter.PROPERTY_PLURALFORMS);
 		assertNotNull(prop);
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
 	public void testPluralFormAccess () {
 		assertEquals("nplurals=2; plural= n==1 or n%10==1 ? 0 : 1;",
 			PluralForms.getExpression("mk"));
 		assertEquals(4, PluralForms.getNumber("cy"));
 	}
 
 	@Test
 	public void testPluralFormDefaults () {
 		assertEquals("nplurals=2; plural=(n!=1);",
 			PluralForms.getExpression("not-a-valid-code"));
 		assertEquals(2, PluralForms.getNumber("not-a-valid-code"));
 	}
 
 	@Test
 	public void testStartDocument () {
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
 			new InputDocument(root+"Test01.po", null),
 			"UTF-8", "en", "en"));
 	}
 	
 	@Test
 	public void testOuputOptionLine_JustFormatWithMacLB () {
 		String snippet = "#, c-format\r"
 			+ "msgid \"Text 1\"\r"
 			+ "msgstr \"Texte 1\"\r";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet, "en", "fr"), "fr");
 		assertEquals(result, snippet);
 	}
 		
 	@Test
 	public void testOuputOptionLine_FormatFuzzy () {
 		String snippet = "#, c-format, fuzzy\n"
 			+ "msgid \"Text 1\"\n"
 			+ "msgstr \"Texte 1\"\n";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet, "en", "fr"), "fr");
 		assertEquals(result, snippet);
 	}
 		
 	@Test
 	public void testInlines () {
 		String snippet = "msgid \"Text %s and %d and %f\"\n"
 			+ "msgstr \"Texte %f et %d et %s\"\n";
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, "en", "fr"), 1);
 		assertNotNull(tu);
 		assertTrue(tu.hasTarget("fr"));
 		TextFragment src = tu.getSourceContent();
 		TextFragment trg = tu.getTargetContent("fr");
 		assertEquals(3, src.getCodes().size());
 		assertEquals(src.getCodes().size(), trg.getCodes().size());
 		FilterTestDriver.checkCodeData(src, trg);
 	}
 		
 	@Test
 	public void testOuputOptionLine_FuzyFormat () {
 		String snippet = "#, fuzzy, c-format\n"
 			+ "msgid \"Text 1\"\n"
 			+ "msgstr \"Texte 1\"\n";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet, "en", "fr"), "fr");
 		assertEquals(result, snippet);
 	}
 
 	@Test
 	public void testOuputOptionLine_StuffFuzyFormat () {
 		String snippet = "#, x-stuff, fuzzy, c-format\n"
 			+ "msgid \"Text 1\"\n"
 			+ "msgstr \"Texte 1\"\n";
 		String result = FilterTestDriver.generateOutput(getEvents(snippet, "en", "fr"), "fr");
 		assertEquals(result, snippet);
 	}
 	
 	@Test
 	public void testOuputSimpleEntry () {
 		String snippet = "msgid \"Text 1\"\n"
 			+ "msgstr \"Texte 1\"\n";
 		String expect = "msgid \"Text 1\"\n"
 			+ "msgstr \"Texte 1\"\n";
 		assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet, "en", "fr"), "fr"));
 	}
 	
 	@Test
 	public void testOuputAddTranslation () {
 		String snippet = "msgid \"Text 1\"\n"
 			+ "msgstr \"\"\n";
 		String expect = "msgid \"Text 1\"\n"
 			+ "msgstr \"Text 1\"\n";
 		assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet, "en", "fr"), "fr"));
 	}
 	
 	@Test
 	public void testTUEmptyIDEntry () {
 		String snippet = "msgid \"\"\n"
 			+ "msgstr \"Some stuff\"\n";
 		assertEquals(null, FilterTestDriver.getTextUnit(getEvents(snippet, "en", "fr"), 1));
 	}
 	
 	@Test
 	public void testTUCompleteEntry () {
 		String snippet = "#, fuzzy\n"
 			+ "#. Comment\n"
 			+ "#: Reference\n"
 			+ "# Translator note\n"
 			+ "#| Context\n"
 			+ "msgid \"Source\"\n"
 			+ "msgstr \"Target\"\n";
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, "en", "fr"), 1);
 
 		assertNotNull(tu);
 		assertEquals("Source", tu.getSource().toString());
 		assertEquals("Target", tu.getTarget("fr").toString());
 
 		assertTrue(tu.hasTargetProperty("fr", Property.APPROVED));
 		Property prop = tu.getTargetProperty("fr", Property.APPROVED);
 		assertEquals("no", prop.getValue());
 		assertFalse(prop.isReadOnly());
 		
 		assertTrue(tu.hasProperty(Property.NOTE));
 		prop = tu.getProperty(Property.NOTE);
 		assertEquals("Comment", prop.getValue());
 		assertTrue(prop.isReadOnly());
 		
 		assertTrue(tu.hasProperty(POFilter.PROPERTY_REFERENCES));
 		prop = tu.getProperty(POFilter.PROPERTY_REFERENCES);
 		assertEquals("Reference", prop.getValue());
 		assertTrue(prop.isReadOnly());
 
 		assertTrue(tu.hasProperty(POFilter.PROPERTY_TRANSNOTE));
 		prop = tu.getProperty(POFilter.PROPERTY_TRANSNOTE);
 		assertEquals("Translator note", prop.getValue());
 		assertTrue(prop.isReadOnly());
 	}
 	
 	@Test
 	public void testTUPluralEntry_DefaultGroup () {
 		StartGroup sg = FilterTestDriver.getGroup(getEvents(makePluralEntry(), "en", "fr"), 1);
 		assertNotNull(sg);
 		assertEquals("x-gettext-plurals", sg.getType());
 	}
 
 	@Test
 	public void testTUPluralEntry_DefaultSingular () {
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(makePluralEntry(), "en", "fr"), 1);
 		assertNotNull(tu);
 		assertEquals("untranslated-singular", tu.getSource().toString());
 		assertFalse(tu.hasTarget("fr"));
 	}
 
 	@Test
 	public void testTUPluralEntry_DefaultPlural () {
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(makePluralEntry(), "en", "fr"), 2);
 		assertNotNull(tu);
 		assertEquals("untranslated-plural", tu.getSource().toString());
 		assertFalse(tu.hasTarget("fr"));
 	}
 	
 	@Test
 	public void testOuputPluralEntry () {
 		String snippet = makePluralEntry();
 		String result = FilterTestDriver.generateOutput(getEvents(snippet, "en", "fr"), "fr");
 		String expected = "msgid \"untranslated-singular\"\n"
 			+ "msgid_plural \"untranslated-plural\"\n"
 			+ "msgstr[0] \"untranslated-singular\"\n"
 			+ "msgstr[1] \"untranslated-plural\"\n";
 		assertEquals(expected, result);
 	}
 		
 	@Test
 	public void testPluralEntryFuzzy () {
 		String snippet = makePluralEntryFuzzy();
 		// First TU
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, "en", "fr"), 1);
 		assertNotNull(tu);
 		assertEquals("translation-singular", tu.getTarget("fr").toString());
 		Property prop = tu.getTargetProperty("fr", Property.APPROVED);
 		assertNotNull(prop);
 		assertEquals("no", prop.getValue());
 		assertEquals(MimeTypeMapper.PO_MIME_TYPE, tu.getMimeType());
 		// Second TU
 		tu = FilterTestDriver.getTextUnit(getEvents(snippet, "en", "fr"), 2);
 		assertNotNull(tu);
 		assertEquals("translation-plural", tu.getTarget("fr").toString());
 		prop = tu.getTargetProperty("fr", Property.APPROVED);
 		assertNotNull(prop);
 		assertEquals("no", prop.getValue());
 	}
 		
 	@Test
 	public void testOuputPluralEntryFuzzy () {
 		String snippet = makePluralEntryFuzzy();
 		String result = FilterTestDriver.generateOutput(getEvents(snippet, "en", "fr"), "fr");
 		assertEquals(snippet, result);
 	}
 
 	@Test
 	public void testDoubleExtraction () {
 		// Read all files in the data directory
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		list.add(new InputDocument(root+"Test01.po", null));
 		list.add(new InputDocument(root+"Test02.po", null));
 		list.add(new InputDocument(root+"Test03.po", null));
 		list.add(new InputDocument(root+"Test04.po", null));
 		list.add(new InputDocument(root+"Test05.po", null));
 		list.add(new InputDocument(root+"TestMonoLingual_EN.po", "okf_po@Monolingual.fprm"));
 		list.add(new InputDocument(root+"TestMonoLingual_FR.po", "okf_po@Monolingual.fprm"));
 		list.add(new InputDocument(root+"AllCasesTest.po", null));
 		list.add(new InputDocument(root+"Test_nautilus.af.po", null));
 		list.add(new InputDocument(root+"Test_DrupalRussianCP1251.po", null));
 		list.add(new InputDocument(root+"POT-Test01.pot", null));
 	
 		RoundTripComparison rtc = new RoundTripComparison();
 		assertTrue(rtc.executeCompare(filter, list, "UTF-8", "en", "fr"));
 	}
 
 	private ArrayList<Event> getEvents(String snippet,
 		String srcLang,
 		String trgLang)
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
 
 	private String makePluralEntry () {
 		return "msgid \"untranslated-singular\"\n"
 			+ "msgid_plural \"untranslated-plural\"\n"
 			+ "msgstr[0] \"\"\n"
 			+ "msgstr[1] \"\"\n";
 	}
 
 	private String makePluralEntryFuzzy () {
 		return "#, fuzzy\n"
 			+ "msgid \"untranslated-singular\"\n"
 			+ "msgid_plural \"untranslated-plural\"\n"
 			+ "msgstr[0] \"translation-singular\"\n"
 			+ "msgstr[1] \"translation-plural\"\n";
 	}
 
 }
