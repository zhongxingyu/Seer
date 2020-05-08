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
 ============================================================================*/
 
 package net.sf.okapi.filters.ts;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.TestUtil;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.common.filters.RoundTripComparison;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 public class TsFilterTest {
 
 	private TsFilter filter;
 	private String root;
 	
 	private FilterTestDriver testDriver;
 	
 	String completeTs = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\r" +
 	"<!DOCTYPE TS>\r" +
 	"<!-- comment -->\r" +
 	"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
 	"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
 	"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
 	"<context encoding=\"utf-8\">\r" +
 	"<name>context name 1</name>\r" +
 	"<comment>context comment 1</comment>\r" +
 	"<context encoding=\"utf-8\">\r" +
 	"<name>context name 2</name>\r" +
 	"<comment>context comment 2</comment>\r" +
 	"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
 	"<location filename=\"test.ts\" line=\"55\"/>\r" +
 	"<source>hello <byte value=\"79\"/>world</source>\r" +
 	"<oldsource>old hello world</oldsource>\r" +
 	"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
 	"<oldcomment>old hello old comment</oldcomment>\r" +
 	"<extracomment>old hello extra comment</extracomment>\r" +
 	"<translatorcomment>old hello translator comment</translatorcomment>\r" +
 	"<translation type=\"unfinished\" variants=\"no\">hejsan <byte value=\"79\"/>varlden</translation>\r" +
 	"<userdata>hello userdata</userdata>\r" +
 	"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
 	"</message>\r" +
 	"<message id=\"2\" encoding=\"utf-8\" numerus=\"no\">\r" +
 	"<location filename=\"test.ts\" line=\"55\"/>\r" +
 	"<source>hello <byte value=\"79\"/>world</source>\r" +
 	"<oldsource>old hello world</oldsource>\r" +
 	"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
 	"<oldcomment>old hello old comment</oldcomment>\r" +
 	"<extracomment>old hello extra comment</extracomment>\r" +
 	"<translatorcomment>old hello translator comment</translatorcomment>\r" +
 	"<translation type=\"obsolete\" variants=\"no\">hejsan <byte value=\"79\"/>varlden</translation>\r" +
 	"<userdata>hello userdata</userdata>\r" +
 	"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
 	"</message>\r" +
 	"<![CDATA[hello cdata]]>\r"+
 	"</context>\r" +
 	"</context>\r" +
 	"</TS>";
 	String simpleSnippet = "<TS><context><name>AlarmAddLogDlg</name><message><source>Add Entry To System Log</source><translation type=\"unfinished\">Lagg till i system Loggen</translation></message></context></TS>";
 
 	@Before
 	public void setUp() throws ParserConfigurationException, SAXException, IOException {
 		filter = new TsFilter();
 		root = TestUtil.getParentDir(this.getClass(), "/Complete_valid_utf8_bom_crlf.ts");
 		
 		testDriver = new FilterTestDriver();
 		testDriver.setDisplayLevel(0);
 		testDriver.setShowSkeleton(true);
 		
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 	      DocumentBuilder builder = factory.newDocumentBuilder();
 	      InputSource is = new InputSource(new StringReader("<hello><name>john</name></hello>"));
 	      builder.parse( is );
 		
 	}
 		
 	@Test
 	public void StartDocument() {
 		StartDocument sd = FilterTestDriver.getStartDocument(getEvents(completeTs, "en-us", "fr-fr"));
 		
 		assertEquals("Incorrect id","1", sd.getId());
 		assertEquals("Incorrect mimeType",MimeTypeMapper.TS_MIME_TYPE, sd.getMimeType());
 		assertNull("Name should be null", sd.getName());
 		assertEquals("Incorrect encoding", "utf-8", sd.getEncoding());
 		assertEquals("Incorrect src language", "en-us", sd.getLanguage());
 		assertEquals("Incorrect linebreak", "\r", sd.getLineBreak());
 		assertEquals("Incorrect multilingual", true, sd.isMultilingual());
 		assertEquals("Incorrect utf8bom", false, sd.hasUTF8BOM());
 		assertNotNull(sd.getFilterParameters());
 		assertTrue(sd.getFilterWriter() instanceof GenericFilterWriter);
 		assertEquals("utf-8", sd.getProperty("encoding").getValue());
 		assertEquals(
 				"<?xml version=\"1.0\" encoding=\"[#$$self$@%encoding]\"?>", 
 				sd.getSkeleton().toString());
 	}	
 	@Test
 	public void DocumentPartTsPart() {
 		DocumentPart dp = FilterTestDriver.getDocumentPart(getEvents(completeTs, "en-us", "fr-fr"), 1);
 		
 		assertEquals("2", dp.getId());
 		assertEquals("4.5.1", dp.getProperty("version").getValue());
 		assertEquals("en-us", dp.getProperty("sourcelanguage").getValue());
 		assertEquals("fr-fr", dp.getProperty("language").getValue());
 		assertEquals( 
 				"\r<!DOCTYPE TS []>\r" +
 				"<!-- comment -->\r" +
 				"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
 				"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
 				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r",
 				dp.getSkeleton().toString());
 
 		//q. should all iresources have mimetype set, or only the ones with text?		
 	}	
 	@Test
 	public void StartGroupContextPart() {
 		StartGroup sg = FilterTestDriver.getGroup(getEvents(completeTs, "en-us", "fr-fr"), 1);
 
 		assertEquals("3", sg.getId());
 		assertEquals("utf-8", sg.getProperty("encoding").getValue());
 		//assertEquals("context name 1", sg.getProperty("name").getValue());
 		//assertEquals("context comment 1", sg.getProperty("comment").getValue());
 		
 		assertEquals( 
 				"<context encoding=\"utf-8\">\r" +
 				"<name>context name 1</name>\r" +
 				"<comment>context comment 1</comment>\r", 
 				sg.getSkeleton().toString());
 
 		
 		sg = FilterTestDriver.getGroup(getEvents(completeTs, "en-us", "fr-fr"), 2);
 
 		assertEquals("4", sg.getId());
 		assertEquals("utf-8", sg.getProperty("encoding").getValue());
 		//assertEquals("context name 2", sg.getProperty("name").getValue());
 		//assertEquals("context comment 2", sg.getProperty("comment").getValue());
 		assertEquals( 
 				"<context encoding=\"utf-8\">\r" +
 				"<name>context name 2</name>\r" +
 				"<comment>context comment 2</comment>\r", 
 				sg.getSkeleton().toString());
 	}	
 	@Test
 	public void TextUnitMessageUnfinished() {
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(completeTs, "en-us", "fr-fr"), 1);
 
 		assertEquals("1", tu.getId());
 		assertEquals(MimeTypeMapper.TS_MIME_TYPE, tu.getMimeType());
 		assertEquals("1", tu.getName());
 		assertFalse(tu.isEmpty());
 		
 		assertEquals("1", tu.getProperty("id").getValue());
 		assertEquals("utf-8", tu.getProperty("encoding").getValue());
 		assertEquals("no", tu.getProperty("numerus").getValue());
 		assertEquals(0, tu.getSourcePropertyNames().size());
 		assertEquals("no", tu.getTargetProperty("fr-fr", "variants").getValue());
 		assertEquals("no", tu.getTargetProperty("fr-fr", "approved").getValue());
 		assertEquals( 
 				"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
 				"<location filename=\"test.ts\" line=\"55\"/>\r" +
 				"<source>[#$$self$]</source>\r" +
 				"<oldsource>old hello world</oldsource>\r" +
 				"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
 				"<oldcomment>old hello old comment</oldcomment>\r" +
 				"<extracomment>old hello extra comment</extracomment>\r" +
 				"<translatorcomment>old hello translator comment</translatorcomment>\r" +
 				"<translation[#$$self$@%approved] variants=\"no\">[#$$self$]</translation>\r" +
 				"<userdata>hello userdata</userdata>\r" +
 				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
 				"</message>", 
 				tu.getSkeleton().toString());
 	}
 	@Test
 	public void AllEvents () {
 		String snippet = "<?xml version=\"1.0\" encoding=\"[#$$self$@%encoding]\"?>\r" +
 		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
 		"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
 		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
 		"<context encoding=\"utf-8\">\r" +
 		"<name>context name 1</name>\r" +
 		"<comment>context comment 1</comment>\r" +
 		"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
 		"<location filename=\"test.ts\" line=\"55\"/>\r" +
 		"<source>hello <byte value=\"79\"/>world</source>\r" +
 		"<oldsource>old hello world</oldsource>\r" +
 		"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
 		"<oldcomment>old hello old comment</oldcomment>\r" +
 		"<extracomment>old hello extra comment</extracomment>\r" +
 		"<translatorcomment>old hello translator comment</translatorcomment>\r" +
 		"<translation type=\"obsolete\" variants=\"no\">hejsan <byte value=\"79\"/>varlden</translation>\r" +
 		"<userdata>hello userdata</userdata>\r" +
 		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
 		"</message>\r" +
 		"</context>\r" +
 		"</TS>";
 		
 		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r" +
 		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
 		"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
 		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
 		"<context encoding=\"utf-8\">\r" +
 		"<name>context name 1</name>\r" +
 		"<comment>context comment 1</comment>\r" +
 		"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
 		"<location filename=\"test.ts\" line=\"55\"/>\r" +
 		"<source>hello <byte value=\"79\"/>world</source>\r" +
 		"<oldsource>old hello world</oldsource>\r" +
 		"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
 		"<oldcomment>old hello old comment</oldcomment>\r" +
 		"<extracomment>old hello extra comment</extracomment>\r" +
 		"<translatorcomment>old hello translator comment</translatorcomment>\r" +
 		"<translation type=\"obsolete\" variants=\"no\">hejsan <byte value=\"79\"/>varlden</translation>\r" +
 		"<userdata>hello userdata</userdata>\r" +
 		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
 		"</message>\r" +
 		"</context>\r" +
 		"</TS>";
 		
 		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet,"en-us","fr-fr"), "fr"));
 	}
	
 	@Test
 	public void StartDocument_FromFile() {
 		StartDocument sd = FilterTestDriver.getStartDocument(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"));
 		assertEquals("Incorrect id","1", sd.getId());
 		assertEquals("Incorrect mimeType",MimeTypeMapper.TS_MIME_TYPE, sd.getMimeType());
		assertTrue(sd.getName().startsWith(root));
		assertTrue(sd.getName().endsWith("/Complete_valid_utf8_bom_crlf.ts"));
 		assertEquals("Incorrect encoding", "utf-8", sd.getEncoding());
 		assertEquals("Incorrect src language", "en-us", sd.getLanguage());
 		assertEquals("Incorrect linebreak", "\r\n", sd.getLineBreak());
 		assertEquals("Incorrect multilingual", true, sd.isMultilingual());
 		assertEquals("Incorrect utf8bom", true, sd.hasUTF8BOM());
 		assertNotNull(sd.getFilterParameters());
 		assertTrue(sd.getFilterWriter() instanceof GenericFilterWriter);
 		assertEquals("utf-8", sd.getProperty("encoding").getValue());
 		assertEquals(
			"<?xml version=\"1.0\" encoding=\"[#$$self$@%encoding]\"?>", 
			sd.getSkeleton().toString());
 	}	

 	@Test
 	public void DocumentPartTsPart_FromFile() {
 		DocumentPart dp = FilterTestDriver.getDocumentPart(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 1);
 		
 		assertEquals("2", dp.getId());
 		assertEquals("4.5.1", dp.getProperty("version").getValue());
 		assertEquals("en-us", dp.getProperty("sourcelanguage").getValue());
 		assertEquals("fr-fr", dp.getProperty("language").getValue());
 		assertEquals( 
 				"\r\n<!DOCTYPE TS []>\r\n" +
 				"<!-- comment -->\r\n" +
 				"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r\n" +
 				"<defaultcodec>hello defaultcodec</defaultcodec>\r\n" +
 				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n",
 				dp.getSkeleton().toString());
 
 		//q. should all iresources have mimetype set, or only the ones with text?		
 	}	
 	@Test
 	public void StartGroupContextPart_FromFile() {
 		StartGroup sg = FilterTestDriver.getGroup(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 1);
 
 		assertEquals("3", sg.getId());
 		assertEquals("utf-8", sg.getProperty("encoding").getValue());
 		//assertEquals("context name 1", sg.getProperty("name").getValue());
 		//assertEquals("context comment 1", sg.getProperty("comment").getValue());
 		
 		assertEquals( 
 				"<context encoding=\"utf-8\">\r\n" +
 				"<name>context name 1</name>\r\n" +
 				"<comment>context comment 1</comment>\r\n", 
 				sg.getSkeleton().toString());
 
 		
 		sg = FilterTestDriver.getGroup(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 2);
 
 		assertEquals("4", sg.getId());
 		assertEquals("utf-8", sg.getProperty("encoding").getValue());
 		//assertEquals("context name 2", sg.getProperty("name").getValue());
 		//assertEquals("context comment 2", sg.getProperty("comment").getValue());
 		assertEquals( 
 				"<context encoding=\"utf-8\">\r\n" +
 				"<name>context name 2</name>\r\n" +
 				"<comment>context comment 2</comment>\r\n", 
 				sg.getSkeleton().toString());
 	}	
 	@Test
 	public void TextUnitMessageUnfinished_FromFile() {
 		TextUnit tu = FilterTestDriver.getTextUnit(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 1);
 
 		assertEquals("1", tu.getId());
 		assertEquals(MimeTypeMapper.TS_MIME_TYPE, tu.getMimeType());
 		assertEquals("1", tu.getName());
 		assertFalse(tu.isEmpty());
 		
 		assertEquals("1", tu.getProperty("id").getValue());
 		assertEquals("utf-8", tu.getProperty("encoding").getValue());
 		assertEquals("no", tu.getProperty("numerus").getValue());
 		assertEquals(0, tu.getSourcePropertyNames().size());
 		assertEquals("no", tu.getTargetProperty("fr-fr", "variants").getValue());
 		assertEquals("no", tu.getTargetProperty("fr-fr", "approved").getValue());
 		assertEquals( 
 				"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
 				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
 				"<source>[#$$self$]</source>\r\n" +
 				"<oldsource>old hello world</oldsource>\r\n" +
 				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
 				"<oldcomment>old hello old comment</oldcomment>\r\n" +
 				"<extracomment>old hello extra comment</extracomment>\r\n" +
 				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
 				"<translation[#$$self$@%approved] variants=\"no\">[#$$self$]</translation>\r\n" +
 				"<userdata>hello userdata</userdata>\r\n" +
 				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
 				"</message>", 
 				tu.getSkeleton().toString());
 	}
 	@Test
 	public void TextUnitMessageApproved_FromFile() {
 		TextUnit tu = FilterTestDriver.getTextUnit(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 2);
 
 		assertEquals("2", tu.getId());
 		assertEquals(MimeTypeMapper.TS_MIME_TYPE, tu.getMimeType());
 		assertEquals("3", tu.getName());
 		assertFalse(tu.isEmpty());
 		
 		assertEquals("3", tu.getProperty("id").getValue());
 		assertEquals("utf-8", tu.getProperty("encoding").getValue());
 		assertEquals("no", tu.getProperty("numerus").getValue());
 		assertEquals(0, tu.getSourcePropertyNames().size());
 		assertEquals("no", tu.getTargetProperty("fr-fr", "variants").getValue());
 		assertEquals("yes", tu.getTargetProperty("fr-fr", "approved").getValue());
 		
 		assertEquals( 
 				"\r\n<message id=\"3\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
 				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
 				"<source>[#$$self$]</source>\r\n" +
 				"<oldsource>old hello world</oldsource>\r\n" +
 				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
 				"<oldcomment>old hello old comment</oldcomment>\r\n" +
 				"<extracomment>old hello extra comment</extracomment>\r\n" +
 				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
 				"<translation variants=\"no\"[#$$self$@%approved]>[#$$self$]</translation>\r\n" +
 				"<userdata>hello userdata</userdata>\r\n" +
 				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
 				"</message>", 
 				tu.getSkeleton().toString());
 	}	
 	@Test
 	public void TextUnitMessageObsolete_FromFile() {
 		
 		DocumentPart dp = FilterTestDriver.getDocumentPart(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 2);
 	
 		assertEquals("5", dp.getId());
 		assertEquals(0, dp.getPropertyNames().size());
 		assertEquals(0, dp.getSourcePropertyNames().size());
 		assertEquals(0, dp.getTargetPropertyNames("fr-fr").size());
 		assertEquals( 
 				"\r\n<message id=\"2\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
 				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
 				"<source>hello <byte value=\"79\"/>world</source>\r\n" +
 				"<oldsource>old hello world</oldsource>\r\n" +
 				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
 				"<oldcomment>old hello old comment</oldcomment>\r\n" +
 				"<extracomment>old hello extra comment</extracomment>\r\n" +
 				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
 				"<translation type=\"obsolete\" variants=\"no\">hejsan <byte value=\"79\"/>varlden</translation>\r\n" +
 				"<userdata>hello userdata</userdata>\r\n" +
 				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
 				"</message>",
 				dp.getSkeleton().toString());
 	}	
 	@Test
 	public void TextUnitMessageMissingTranslation_FromFile() {
 		TextUnit tu = FilterTestDriver.getTextUnit(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 3);
 
 		assertEquals("3", tu.getId());
 		assertEquals(MimeTypeMapper.TS_MIME_TYPE, tu.getMimeType());
 		assertEquals("4", tu.getName());
 		assertFalse(tu.isEmpty());
 		
 		assertEquals("4", tu.getProperty("id").getValue());
 		assertEquals("utf-8", tu.getProperty("encoding").getValue());
 		assertEquals("no", tu.getProperty("numerus").getValue());
 		assertEquals(0, tu.getSourcePropertyNames().size());
 		assertEquals("no", tu.getTargetProperty("fr-fr", "variants").getValue());
 		assertEquals("no", tu.getTargetProperty("fr-fr", "approved").getValue());
 	
 		assertEquals( 
 				"\r\n<message id=\"4\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
 				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
 				"<source>[#$$self$]</source>\r\n" +
 				"<oldsource>old hello world</oldsource>\r\n" +
 				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
 				"<oldcomment>old hello old comment</oldcomment>\r\n" +
 				"<extracomment>old hello extra comment</extracomment>\r\n" +
 				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
 				"<translation[#$$self$@%approved] variants=\"no\">[#$$self$]</translation>\r\n" +
 				"<userdata>hello userdata</userdata>\r\n" +
 				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
 				"</message>", 
 				tu.getSkeleton().toString());
 	}	
 	@Test
 	public void TextUnitMessageMissingSourceAndTranslation_FromFile() {
 		
 		DocumentPart dp = FilterTestDriver.getDocumentPart(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 3);
 	
 		assertEquals("6", dp.getId());
 		assertEquals(0, dp.getPropertyNames().size());
 		assertEquals(0, dp.getSourcePropertyNames().size());
 		assertEquals(0, dp.getTargetPropertyNames("fr-fr").size());
 		assertEquals( 
 				"\r\n<message id=\"5\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
 				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
 				"<oldsource>old hello world</oldsource>\r\n" +
 				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
 				"<oldcomment>old hello old comment</oldcomment>\r\n" +
 				"<extracomment>old hello extra comment</extracomment>\r\n" +
 				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
 				"<userdata>hello userdata</userdata>\r\n" +
 				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
 				"</message>",
 				dp.getSkeleton().toString());
 	}	
 	@Test
 	public void TextUnitMessageMissingSourceNotTranslation_FromFile() {
 		
 		DocumentPart dp = FilterTestDriver.getDocumentPart(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 4);
 
 		
 		assertEquals("7", dp.getId());
 		assertEquals(0, dp.getPropertyNames().size());
 		assertEquals(0, dp.getSourcePropertyNames().size());
 		assertEquals(0, dp.getTargetPropertyNames("fr-fr").size());
 		assertEquals( 
 				"\r\n<message id=\"6\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
 				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
 				"<oldsource>old hello world</oldsource>\r\n" +
 				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
 				"<oldcomment>old hello old comment</oldcomment>\r\n" +
 				"<extracomment>old hello extra comment</extracomment>\r\n" +
 				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
 				"<translation variants=\"no\">hejsan <byte value=\"79\"/>varlden</translation>\r\n" +
 				"<userdata>hello userdata</userdata>\r\n" +
 				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
 				"</message>",
 				dp.getSkeleton().toString());
 	}		

 	@Test
 	public void testDoubleExtraction () {
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		list.add(new InputDocument(root+"Complete_valid_utf8_bom_crlf.ts", null));
 		RoundTripComparison rtc = new RoundTripComparison();
 		assertTrue(rtc.executeCompare(filter, list, "UTF-8", "en-us", "fr-fr"));
 	}
 	
 	//TODO: empty source
 	//TODO: empty target
 	//TODO: split numerus forms
 	
 	//--methods--
 	@Test
 	public void testGetName() {
 		assertEquals("okf_ts", filter.getName());
 	}
 
 	@Test
 	public void testGetMimeType() {
 		assertEquals("application/x-ts", filter.getMimeType());
 	}	
 	
 	//--exceptions--
 	@Test (expected=NullPointerException.class)
 	public void testSourceLangNotSpecified() {
 		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, null));
 	}
 
 	@Test (expected=NullPointerException.class)
 	public void testTargetLangNotSpecified() {
 		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, "en-us"));
 	}
 
 	@Test (expected=NullPointerException.class)
 	public void testTargetLangNotSpecified2() {
 		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, "en-us", null));
 	}
 	
 /*	@Test (expected=NullPointerException.class)
 	public void testSourceLangEmpty() {
 		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, "","fr-fr"));
 	}*/	
 	
 /*	@Test (expected=NullPointerException.class)
 	public void testTargetLangEmpty() {
 		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, "en-us",""));
 	}*/	
 	
 	@Test
 	public void testInputStream() {
 		InputStream tsStream = TsFilterTest.class.getResourceAsStream("/alarm_ro.ts");
 		filter.open(new RawDocument(tsStream, "UTF-8", "en-us","fr-fr"));
 		if ( !testDriver.process(filter) ) Assert.fail();
 		filter.close();
 	}	
 
 	@Test
 	public void testConsolidatedStream() {
 		filter.open(new RawDocument(simpleSnippet, "en-us","fr-fr"));
 		if ( !testDriver.process(filter) ) Assert.fail();
 		filter.close();
 		//System.out.println(FilterTestDriver.generateOutput(getEvents(simpleSnippet, "en-us","fr-fr"), simpleSnippet, "fr-fr"));
 	}	
 
 	@Test
 	public void testTu() {
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(simpleSnippet, "en-us", "fr-fr"), 1);
 		assertNotNull(tu);
 		assertEquals("Add Entry To System Log", tu.getSourceContent().getCodedText());
 		assertEquals("Lagg till i system Loggen", tu.getTargetContent("fr-fr").getCodedText());
 		
 		System.out.println(tu.getId());
 		System.out.println(tu.getMimeType());
 		System.out.println(tu.getName());
 		System.out.println(tu.getType());
 		System.out.println(tu.getPropertyNames());
 		System.out.println(tu.getSkeleton());
 		System.out.println(tu.getTargetLanguages());
 		
 		tu.setTargetProperty("fr-fr", new Property(Property.APPROVED, "no", false));
 		System.out.println(tu.getTargetPropertyNames("fr-fr"));
 		/*Property prop = dp.getProperty(Property.ENCODING);
 		assertNotNull(prop);
 		assertEquals("UTF-8", prop.getValue());
 		assertFalse(prop.isReadOnly());*/
 		
 	}	
 
 	
 
 	
 	/*
 	@Test
 	public void testOutputBasic_Comment () {
 		assertEquals(simpleBilingualSnippet, FilterTestDriver.generateOutput(getEvents(simpleBilingualSnippet,"en-us","fr-fr"), simpleSnippet, "fr-fr"));
 		System.out.println(FilterTestDriver.generateOutput(getEvents(simpleBilingualSnippet,"en-us","fr-fr"), simpleSnippet, "en"));
 	}*/	
 	
 	/*@Test
 	public void testStartDocument () {
 		StartDocument sd = FilterTestDriver.getStartDocument(getEvents(simpleSnippet, "en-us","fr-fr"));
 		assertNotNull(sd);
 		assertNotNull(sd.getEncoding());
 		assertNotNull(sd.getType());
 		assertNotNull(sd.getMimeType());
 		assertNotNull(sd.getLanguage());
 		assertEquals("\r", sd.getLineBreak());
 	}*/
 	
 	/*@Test
 	public void testSimpleTransUnit () {
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(simpleSnippet, "en-us","fr-fr"), 1);
 		assertNotNull(tu);
 		assertEquals("Hello World!", tu.getSource().toString());
 		assertEquals("tuid_1", tu.getName());
 	}*/
 	
 	@Test
 	public void testStartDocument () {
 		URL url = TsFilterTest.class.getResource("/TSTest01.ts");
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
 			new InputDocument(url.getPath(), null),
 			"UTF-8", "en", "en"));
 	}
 	
 	@Test
 	public void runTest () {
 		FilterTestDriver testDriver = new FilterTestDriver();
 		TsFilter filter = null;		
 		try {
 			filter = new TsFilter();
 			URL url = TsFilterTest.class.getResource("/TSTest01.ts");
 			filter.open(new RawDocument(new URI(url.toString()), "UTF-8", "EN-US", "fr-fr"));			
 			if ( !testDriver.process(filter) ) Assert.fail();
 			//process(filter);
 			filter.close();
 			
 		}
 		catch ( Throwable e ) {
 			e.printStackTrace();
 			Assert.fail("Exception occured");
 		}
 		finally {
 			if ( filter != null ) filter.close();
 		}
 	}	
 	
 //	private void process (IFilter filter) {
 //		
 //		System.out.println("==================================================");
 //		Event event;
 //		while ( filter.hasNext() ) {
 //			event = filter.next();
 //			switch ( event.getEventType() ) {		
 //			case START_DOCUMENT:
 //				System.out.println("---Start Document");
 //				printSkeleton(event.getResource());
 //				break;
 //			case END_DOCUMENT:
 //				System.out.println("---End Document");
 //				printSkeleton(event.getResource());
 //				break;
 //			case START_GROUP:
 //				System.out.println("---Start Group");
 //				printSkeleton(event.getResource());
 //				break;
 //			case END_GROUP:
 //				System.out.println("---End Group");
 //				printSkeleton(event.getResource());
 //				break;
 //			case TEXT_UNIT:
 //				System.out.println("---Text Unit");
 //				TextUnit tu = (TextUnit)event.getResource();
 //				printResource(tu);
 //				System.out.println("S=["+tu.toString()+"]");
 //				int i = 1;
 //				for ( String lang : tu.getTargetLanguages() ) {
 //					System.out.println("T"+(i++)+" "+lang+"=["+tu.getTarget(lang).toString()+"]");
 //				}
 //				printSkeleton(tu);
 //				break;
 //			case DOCUMENT_PART:
 //				System.out.println("---Document Part");
 //				printResource((INameable)event.getResource());
 //				printSkeleton(event.getResource());
 //				break;				
 //			}
 //		}
 //	}
 	
 //	private void printResource (INameable res) {
 //		System.out.println("  id="+res.getId());
 //		System.out.println("  name="+res.getName());
 //		System.out.println("  type="+res.getType());
 //		System.out.println("  mimeType="+res.getMimeType());
 //	}
 
 //	private void printSkeleton (IResource res) {
 //		ISkeleton skel = res.getSkeleton();
 //		if ( skel != null ) {
 //			System.out.println("---");
 //			System.out.println(skel.toString());
 //			System.out.println("---");
 //		}
 //	}
 
 	private ArrayList<Event> getEvents(String snippet, String srcLang, String trgLang){
 		ArrayList<Event> list = new ArrayList<Event>();
 		filter.open(new RawDocument(snippet, srcLang, trgLang));
 		while ( filter.hasNext() ) {
 			Event event = filter.next();
 			list.add(event);
 		}
 		filter.close();
 		return list;
 	}
 	
 	//--without specifying target language--
 	private ArrayList<Event> getEvents(String snippet, String srcLang){
 		ArrayList<Event> list = new ArrayList<Event>();
 		filter.open(new RawDocument(snippet, srcLang));
 		while ( filter.hasNext() ) {
 			Event event = filter.next();
 			list.add(event);
 		}
 		filter.close();
 		return list;
 	}	
 
 	private ArrayList<Event> getEventsFromFile(String file){
 		ArrayList<Event> list = new ArrayList<Event>();
 
 		URL url = TsFilterTest.class.getResource("/"+file);
 
 		try {
 			filter.open(new RawDocument(new URI(url.toString()), "utf-8", "en-us", "fr-fr"));
 			while ( filter.hasNext() ) {
 				Event event = filter.next();
 				list.add(event);
 			}
 			filter.close();
 		}
 		catch ( Throwable e ) {
 			e.printStackTrace();
 			Assert.fail("Exception occured");
 		}
 		
 		return list;
 	}	
 }
