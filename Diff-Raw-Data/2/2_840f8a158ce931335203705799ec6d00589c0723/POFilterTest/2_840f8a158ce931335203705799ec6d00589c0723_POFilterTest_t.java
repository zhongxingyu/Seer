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
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartDocument;
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
			new InputDocument(root+"Test01.po", null),
 			"UTF-8", "en", "en"));
 	}
 	
 	@Test
 	public void testOuputOptionLine_JustFormat () {
 		String snippet = "#, c-format\n"
 			+ "msgid \"Text 1\"\n"
 			+ "msgstr \"Texte 1\"\n";
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
 		
 		assertTrue(tu.hasProperty("references"));
 		prop = tu.getProperty("references");
 		assertEquals("Reference", prop.getValue());
 		assertTrue(prop.isReadOnly());
 
 		assertTrue(tu.hasProperty("transnote"));
 		prop = tu.getProperty("transnote");
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
