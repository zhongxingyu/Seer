 package net.sf.okapi.filters.openxml.tests;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.TextUnit;
 //import net.sf.okapi.common.skeleton.GenericSkeleton;
 //import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.filters.openxml.OpenXMLContentFilter;
 import net.sf.okapi.filters.openxml.OpenXMLContentSkeletonWriter;
 import net.sf.okapi.filters.openxml.OpenXMLFilter;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class OpenXMLSnippetsTest {
	private static final Logger LOGGER = Logger.getLogger(OpenXMLSnippetsTest.class.getName());
 	private OpenXMLContentFilter openXMLContentFilter;
 	private static final int MSWORD=1;
 	private static final int MSEXCEL=2;
 	private static final int MSPOWERPOINT=3;
 	public final static int MSWORDCHART=4; // DWH 4-16-09
 	private String snappet;
 	
 	@Before
 	public void setUp()  {
 		openXMLContentFilter = new OpenXMLContentFilter();	
 		openXMLContentFilter.setLogger(LOGGER);
 		LOGGER.setLevel(Level.FINER);
		LOGGER.addHandler(new LogHandlerSystemOut());
 	}
 
 	@After
 	public void tearDown() {
 	}
/*
 	@Test
 	public void testInsertion() {
 		String snippet = "<w:p><w:ins><w:r><w:t xml:space=\"preserve\">zorcon</w:t></w:r></w:ins></w:p>";
 		snappet = generateOutput(getEvents(snippet, MSWORD), snippet);
 		assertEquals(snappet, snippet);
 	}
 
 	@Test
 	public void testBareText() {
 		String snippet = "<w:p><w:r><w:t xml:space=\"preserve\">zorcon</w:t></w:r></w:p>";
 		snappet = generateOutput(getEvents(snippet, MSWORD), snippet);
 		assertEquals(snappet, snippet);
 	}
 
 	@Test
 	public void testOneWord() {
 		String snippet = "<w:p><w:r><w:rPr><w:lang w:val=\"en-US\"/><w:b/><w:bCs></w:rPr><w:t>zorcon</w:t></w:r></w:p>";
 		snappet = generateOutput(getEvents(snippet, MSWORD), snippet);
 		assertEquals(snappet, snippet);
 	}
 
 	@Test
 	public void testInlineLanguageWithText() {
 		String snippet = "<w:p><w:r><w:lang w:val=\"en-US\"/><w:t>zorcon</w:t></w:r></w:p>";
 		snappet = generateOutput(getEvents(snippet, MSWORD), snippet);
 		assertEquals(snappet, snippet);
 	}
*/
 	@Test
 	public void testInlineLanguage() {
 		String snippet = "<w:p><w:r><w:lang w:val=\"en-US\"/></w:r></w:p>";
 		snappet = generateOutput(getEvents(snippet, MSWORD), snippet);
 		assertEquals(snappet, snippet);
 	}
 
 	@Test
 	public void testInlineTranslatable() {
 		String snippet = "<w:p><wp:docPr id=\"2\" name=\"Picture 1\"></w:p>";
 		snappet = generateOutput(getEvents(snippet, MSWORD), snippet);
 		assertEquals(snappet, snippet);
 	}
 
 	@Test
 	public void testLostDocParts() {
 		String snippet = "<w:p><w:r><w:rPr><w:lang w:val=\"en-US\" w:eastAsia=\"zh-TW\"/></w:rPr></w:r><wp:docPr name=\"Picture 1\"><pic:cNvPr name=\"Picture 1\"><a:stretch/></w:p>";				
 		snappet = generateOutput(getEvents(snippet, MSWORD), snippet);
 		assertEquals(snappet, snippet);
 	}
 
 	@Test
 	public void testAuthor() {
 		String snippet = "<comments><author>Dan Higinbotham</author></comments>";
 		snappet = generateOutput(getEvents(snippet, MSEXCEL), snippet);
 		assertEquals(snappet, snippet);
 	}
 	/*
 	@Test
 	public void testComplexEmptyElement() {
 		String snippet = "<dummy write=\"w\" readonly=\"ro\" trans=\"tu1\" />";
 		assertEquals(generateOutput(getEvents(snippet, MSWORD), snippet), snippet);
 	}
 
 	@Test
 	public void testPWithInlines() {
 		String snippet = "<p>Before <b>bold</b> <a href=\"there\"/> after.</p>";
 		assertEquals(generateOutput(getEvents(snippet, MSWORD), snippet), snippet);
 	}
 
 	@Test
 	public void testMETATag2() {
 		String snippet = "<meta http-equiv=\"Content-Language\" content=\"en\"/>";
 		assertEquals(generateOutput(getEvents(snippet, MSWORD), snippet), snippet);
 	}
 	
 	@Test
 	public void testPWithInlines2() {
 		String snippet = "<p>Before <img href=\"img.png\" alt=\"text\"/> after.</p>";
 		assertEquals(generateOutput(getEvents(snippet, MSWORD), snippet), snippet);
 	}
 	
 	@Test
 	public void testTableGroups() {
 		String snippet = "<table id=\"100\"><tr><td>text</td></tr></table>";
 		assertEquals(generateOutput(getEvents(snippet, MSWORD), snippet), snippet);
 	}
 	
 	@Test
 	public void testGroupInPara() {
 		String snippet = "<p>Text before list:"
 			 + "<ul>"
 			 + "<li>Text of item 1</li>"
 			 + "<li>Text of item 2</li>"
 			 + "</ul>"
 			 + "and text after the list.</p>";
 		assertEquals(generateOutput(getEvents(snippet, MSWORD), snippet), snippet);
 	}
 */	
 	private ArrayList<Event> getEvents(String snippet, int filetype) {
 		ArrayList<Event> list = new ArrayList<Event>();
 		openXMLContentFilter.setLogger(LOGGER);
 		openXMLContentFilter.setUpConfig(filetype);
 		openXMLContentFilter.open(new RawDocument(snippet, "en-US"));
 		while (openXMLContentFilter.hasNext()) {
 			Event event = openXMLContentFilter.next();
 			openXMLContentFilter.displayOneEvent(event);
 			list.add(event);
 		}
 		openXMLContentFilter.close();
 		return list;
 	}
 
 	private String generateOutput(ArrayList<Event> list, String original) {
 		int configurationType=openXMLContentFilter.getConfigurationType();
 		OpenXMLContentSkeletonWriter writer = new OpenXMLContentSkeletonWriter(configurationType);
 		StringBuilder tmp = new StringBuilder();
 		for (Event event : list) {
 			switch (event.getEventType()) {
 			case START_DOCUMENT:
 				writer.processStartDocument("en-US", "utf-8", null, new EncoderManager(),
 					(StartDocument)event.getResource());
 				break;
 			case TEXT_UNIT:
 				TextUnit tu = (TextUnit) event.getResource();
 				tmp.append(writer.processTextUnit(tu));
 				break;
 			case DOCUMENT_PART:
 				DocumentPart dp = (DocumentPart) event.getResource();
 				tmp.append(writer.processDocumentPart(dp));
 				break;
 			case START_GROUP:
 				StartGroup startGroup = (StartGroup) event.getResource();
 				tmp.append(writer.processStartGroup(startGroup));
 				break;
 			case END_GROUP:
 				Ending ending = (Ending) event.getResource();
 				tmp.append(writer.processEndGroup(ending));
 				break;
 			}
 		}		
 
 		LOGGER.log(Level.FINER,"nOriginal: "+original);
 		LOGGER.log(Level.FINER,"Output:    "+tmp.toString());
 		writer.close();
 		return tmp.toString();
 	}
 }
