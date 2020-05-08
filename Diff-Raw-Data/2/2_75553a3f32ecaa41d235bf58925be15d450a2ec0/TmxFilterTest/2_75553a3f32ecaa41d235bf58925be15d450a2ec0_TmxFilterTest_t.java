 package net.sf.okapi.filters.tmx;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.TestUtil;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.RoundTripComparison;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.filters.tmx.Parameters;
 import net.sf.okapi.filters.tmx.TmxFilter;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TmxFilterTest {
 
 	private TmxFilter filter;
 	private FilterTestDriver testDriver;
 	private String root;
 	
 	String simpleSnippet = "<?xml version=\"1.0\"?>\r"
 		+ "<!-- document level comment --><tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en-us\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><note>hello world note</note><tuv xml:lang=\"en-us\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en-us\"><seg>Hello Universe!</seg></tuv></tu></body></tmx>\r";
 
 	String simpleBilingualSnippet = "<?xml version=\"1.0\"?>\r"
 		+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en-us\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><tuv xml:lang=\"en-us\"><seg>Hello World!</seg></tuv><tuv xml:lang=\"fr-fr\"><seg>Bonjour le monde!</seg></tuv></tu></body></tmx>\r";
 	
 	String tuMissingXmlLangSnippet = "<?xml version=\"1.0\"?>\r"
 		+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en-us\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><tuv><seg>Hello World!</seg></tuv></tu></body></tmx>\r";
 
 	String invalidXmlSnippet = "<?xml version=\"1.0\"?>\r"
 		+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en-us\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><tuv xml:lang=\"en-us\"><seg>Hello World!</seg></tu></body></tmx>\r";
 
 	String emptyTuSnippet = "<?xml version=\"1.0\"?>\r"
 		+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en-us\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"></tu></body></tmx>\r";
 
 	String invalidElementsInsideTuSnippet = "<?xml version=\"1.0\"?>\r"
 		+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en-us\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><tuv xml:lang=\"en-us\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en-us\"><InvalidTag>Invalid Tag Content</InvalidTag><seg>Hello Universe!</seg></tuv></tu></body></tmx>\r";
 
 	String invalidElementInsidePlaceholderSnippet = "<?xml version=\"1.0\"?>\r"
 		+ "<!-- document level comment --><tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en-us\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><note>hello world note</note><tuv xml:lang=\"en-us\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en-us\"><seg>Hello <ph type=\"fnote\">Before Sub\"<sub>Hello Subflow. </sub>After <invalid> test invalid placeholder element </invalid> Sub</ph>Universe!</seg></tuv></tu></body></tmx>\r";
 	
 	String invalidElementInsideSubSnippet = "<?xml version=\"1.0\"?>\r"
 		+ "<!-- document level comment --><tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en-us\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><note>hello world note</note><tuv xml:lang=\"en-us\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en-us\"><seg>Hello <ph type=\"fnote\">Before Sub\"<sub>Hello <invalid> test invalid sub element </invalid> Subflow. </sub>After Sub</ph>Universe!</seg></tuv></tu></body></tmx>\r";
 
 	String multiTransSnippet = "<?xml version=\"1.0\"?>"
 		+ "<tmx version=\"1.4\"><header creationtool=\"x\" creationtoolversion=\"1\" segtype=\"sentence\" o-tmf=\"x\" adminlang=\"en\" srclang=\"en-us\" datatype=\"plaintext\"></header><body><tu>"
 		+ "<tuv xml:lang=\"en-us\"><seg>Hello</seg>s</tuv>"
 		+ "<tuv xml:lang=\"fr\"><seg>Bonjour</seg></tuv>"
 		+ "<tuv xml:lang=\"fr\"><seg>Salut</seg></tuv>"
 		+ "<tuv xml:lang=\"de\"><seg>Hallo</seg></tuv>"
 		+ "<tuv xml:lang=\"it\"><seg>Buongiorno</seg></tuv>"
 		+ "</tu></body></tmx>\r";
 	
 	String utSnippetInSeg = "<?xml version=\"1.0\"?>\r"
 		+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en-us\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><tuv xml:lang=\"en-us\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en-us\"><seg>Hello <ut>Ut Content</ut> Universe!</seg></tuv></tu></body></tmx>\r";
 
 	String utSnippetInSub = "<?xml version=\"1.0\"?>\r"
 		+ "<!-- document level comment --><tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en-us\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><note>hello world note</note><tuv xml:lang=\"en-us\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en-us\"><seg>Hello <ph type=\"fnote\">Before Sub\"<sub>Hello <ut> ut content </ut> Subflow. </sub>After Sub</ph>Universe!</seg></tuv></tu></body></tmx>\r";
 
 	String utSnippetInHi = "<?xml version=\"1.0\"?>\r"
 		+ "<!-- document level comment --><tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en-us\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><note>hello world note</note><tuv xml:lang=\"en-us\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en-us\"><seg>Hello <hi type=\"fnote\">Start hi <ut> ut content </ut> End hi.</hi>Universe!</seg></tuv></tu></body></tmx>\r";
 	
 	
 	
 	@Before
 	public void setUp() {
 		filter = new TmxFilter();
 		testDriver = new FilterTestDriver();
 		testDriver.setDisplayLevel(0);
 		testDriver.setShowSkeleton(true);
 		root = TestUtil.getParentDir(this.getClass(), "/Paragraph_TM.tmx");
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
 	public void testGetName() {
 		assertEquals("okf_tmx", filter.getName());
 	}
 
 	@Test
 	public void testGetMimeType() {
 		assertEquals("text/x-tmx", filter.getMimeType());
 	}	
 	
 	@Test
 	public void testCancel() {
 		Event event;
 		filter.open(new RawDocument(simpleSnippet,"en-us","fr-fr"));			
 		while (filter.hasNext()) {
 			event = filter.next();
 			if (event.getEventType() == EventType.START_DOCUMENT) {
 				assertTrue(event.getResource() instanceof StartDocument);
 			} else if (event.getEventType() == EventType.TEXT_UNIT) {
 				//--cancel after first text unit--
 				filter.cancel();
 				assertTrue(event.getResource() instanceof TextUnit);
 			} else if (event.getEventType() == EventType.DOCUMENT_PART) {
 				assertTrue(event.getResource() instanceof DocumentPart);
 			} 
 		}
 		
 		event = filter.next();
 		assertEquals(EventType.CANCELED, event.getEventType());
 		filter.close();		
 		
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
 	
 	@Test (expected=NullPointerException.class)
 	public void testSourceLangEmpty() {
 		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, "","fr-fr"));
 	}	
 	
 	@Test (expected=NullPointerException.class)
 	public void testTargetLangEmpty() {
 		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, "en-us",""));
 	}	
 	
 	@Test (expected=OkapiBadFilterInputException.class)
 	public void testTuXmlLangMissing() {
 		FilterTestDriver.getStartDocument(getEvents(tuMissingXmlLangSnippet, "en-us","fr-fr"));
 	}
 	
 	@Test (expected=OkapiIOException.class)
 	public void testInvalidXml() {
 		FilterTestDriver.getStartDocument(getEvents(invalidXmlSnippet, "en-us","fr-fr"));
 	}
 
 	@Test (expected=OkapiBadFilterInputException.class)
 	public void testEmptyTu() {
 		FilterTestDriver.getStartDocument(getEvents(emptyTuSnippet, "en-us","fr-fr"));
 	}
 
 	@Test (expected=OkapiBadFilterInputException.class)
 	public void testInvalidElementInTu() {
 		FilterTestDriver.getStartDocument(getEvents(invalidElementsInsideTuSnippet, "en-us","fr-fr"));
 	}
 	
 	@Test (expected=OkapiBadFilterInputException.class)
 	public void testInvalidElementInSub() {
 		FilterTestDriver.getStartDocument(getEvents(invalidElementInsideSubSnippet, "en-us","fr-fr"));
 	}
 
 	@Test (expected=OkapiBadFilterInputException.class)
 	public void testInvalidElementInPlaceholder() {
 		FilterTestDriver.getStartDocument(getEvents(invalidElementInsidePlaceholderSnippet, "en-us","fr-fr"));
 	}
 	
 	@Test (expected=OkapiIOException.class)
 	public void testOpenInvalidInputStream() {
 		InputStream nullStream=null;
 		filter.open(new RawDocument(nullStream,"en-us","fr-fr"));			
 		if ( !testDriver.process(filter) ) Assert.fail();
 		filter.close();	
 	}
 	
 	@Test (expected=OkapiIOException.class)
 	public void testOpenInvalidUri() throws Exception{
 		String basePath = TmxFilterTest.class.getResource("/Paragraph_TM.tmx").toURI().getPath();
 		basePath = "file://"+basePath.replace("/bin/Paragraph_TM.tmx","");
 
 		URI invalid_uri = new URI(basePath+"/invalid_filename.tmx");
 		filter.open(new RawDocument(invalid_uri,"en-us","fr-fr"));			
 		if ( !testDriver.process(filter) ) Assert.fail();
 		filter.close();	
 	}
 	
 	@Test
 	public void testInputStream() {
 		InputStream htmlStream = TmxFilterTest.class.getResourceAsStream("/Paragraph_TM.tmx");
 		filter.open(new RawDocument(htmlStream, "UTF-8", "en-us","fr-fr"));
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
 	public void testUnConsolidatedStream() {
 		Parameters params = (Parameters)filter.getParameters();
 		params.consolidateDpSkeleton=false;
 		
 		filter.open(new RawDocument(simpleSnippet, "en-us","fr-fr"));
 		if ( !testDriver.process(filter) ) Assert.fail();
 		filter.close();
 		//System.out.println(FilterTestDriver.generateOutput(getEvents(simpleSnippet, "en-us","fr-fr"), simpleSnippet, "fr-fr"));
 	}	
 	
 	/*
 	@Test
 	public void testOutputBasic_Comment () {
 		assertEquals(simpleBilingualSnippet, FilterTestDriver.generateOutput(getEvents(simpleBilingualSnippet,"en-us","fr-fr"), simpleSnippet, "fr-fr"));
 		System.out.println(FilterTestDriver.generateOutput(getEvents(simpleBilingualSnippet,"en-us","fr-fr"), simpleSnippet, "en"));
 	}*/	
 	
 	@Test
 	public void testStartDocument () {
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
 			new InputDocument(root+"Paragraph_TM.tmx", null),
 			"UTF-8", "en", "en"));
 	}
 	
 	@Test
 	public void testStartDocumentFromList () {
 		StartDocument sd = FilterTestDriver.getStartDocument(getEvents(simpleSnippet, "en-us","fr-fr"));
 		assertNotNull(sd);
 		assertNotNull(sd.getEncoding());
 		assertNotNull(sd.getType());
 		assertNotNull(sd.getMimeType());
 		assertNotNull(sd.getLanguage());
 		assertEquals("\r", sd.getLineBreak());
 	}
 	
 	@Test
 	public void testSimpleTransUnit () {
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(simpleSnippet, "en-us","fr-fr"), 1);
 		assertNotNull(tu);
 		assertEquals("Hello World!", tu.getSource().toString());
 		assertEquals("tuid_1", tu.getName());
 	}
 	
 	@Test
 	public void testMulipleTargets () {
 		ArrayList<Event> events = getEvents(multiTransSnippet, "en-us", "fr");
 
 		TextUnit tu = FilterTestDriver.getTextUnit(events, 1);
 		assertNotNull(tu);
 		assertEquals("Hello", tu.getSource().toString());
 		assertEquals(3, tu.getTargetLanguages().size());
 		assertTrue(tu.hasTarget("fr"));
 		assertEquals("Bonjour", tu.getTarget("fr").toString());
 		assertTrue(tu.hasTarget("de"));
 		assertEquals("Hallo", tu.getTarget("de").toString());
 		assertTrue(tu.hasTarget("it"));
 		assertEquals("Buongiorno", tu.getTarget("it").toString());
 
 		tu = FilterTestDriver.getTextUnit(events, 2);
 		assertNotNull(tu);
 		assertEquals("Hello", tu.getSource().toString());
 		assertEquals(1, tu.getTargetLanguages().size());
 		assertTrue(tu.hasTarget("fr"));
 		assertEquals("Salut", tu.getTarget("fr").toString());
 	}
 	
 	@Test
 	public void testUtInSeg () {
 		FilterTestDriver.getStartDocument(getEvents(utSnippetInSeg, "en-us","fr-fr"));
 	}
 
 	@Test
 	public void testUtInSub () {
 		FilterTestDriver.getStartDocument(getEvents(utSnippetInSub, "en-us","fr-fr"));
 	}
 
 	@Test
 	public void testUtInHi () {
 		FilterTestDriver.getStartDocument(getEvents(utSnippetInHi, "en-us","fr-fr"));
 	}
 
 		
 
 	
 /*	@Test
 	public void runTest () {
 		FilterTestDriver testDriver = new FilterTestDriver();
 		TmxFilter filter = null;		
 		try {
 			filter = new TmxFilter();
 			URL url = TmxFilterTest.class.getResource("/ImportTest2A.tmx");
 			filter.open(new RawDocument(new URI(url.toString()), "UTF-8", "EN-US", "FR-CA"));			
 			if ( !testDriver.process(filter) ) Assert.fail();
 			filter.close();
 			//process(filter);
 			//filter.close();
 			
 		}
 		catch ( Throwable e ) {
 			e.printStackTrace();
 			Assert.fail("Exception occured");
 		}
 		finally {
 			if ( filter != null ) filter.close();
 		}
 	}	*/
 	
 /*	private void process (IFilter filter) {
 		
 		System.out.println("==================================================");
 		Event event;
 		while ( filter.hasNext() ) {
 			event = filter.next();
 			switch ( event.getEventType() ) {		
 			case START_DOCUMENT:
 				System.out.println("---Start Document");
 				printSkeleton(event.getResource());
 				break;
 			case END_DOCUMENT:
 				System.out.println("---End Document");
 				printSkeleton(event.getResource());
 				break;
 			case START_GROUP:
 				System.out.println("---Start Group");
 				printSkeleton(event.getResource());
 				break;
 			case END_GROUP:
 				System.out.println("---End Group");
 				printSkeleton(event.getResource());
 				break;
 			case TEXT_UNIT:
 				System.out.println("---Text Unit");
 				TextUnit tu = (TextUnit)event.getResource();
 				printResource(tu);
 				System.out.println("S=["+tu.toString()+"]");
 				int i = 1;
 				for ( String lang : tu.getTargetLanguages() ) {
 					System.out.println("T"+(i++)+" "+lang+"=["+tu.getTarget(lang).toString()+"]");
 				}
 				printSkeleton(tu);
 				break;
 			case DOCUMENT_PART:
 				System.out.println("---Document Part");
 				printResource((INameable)event.getResource());
 				printSkeleton(event.getResource());
 				break;				
 			}
 		}
 	}
 	
 	private void printResource (INameable res) {
 		System.out.println("  id="+res.getId());
 		System.out.println("  name="+res.getName());
 		System.out.println("  type="+res.getType());
 		System.out.println("  mimeType="+res.getMimeType());
 	}
 
 	private void printSkeleton (IResource res) {
 		ISkeleton skel = res.getSkeleton();
 		if ( skel != null ) {
 			System.out.println("---");
 			System.out.println(skel.toString());
 			System.out.println("---");
 		}
 	}*/
 	
 	
 	
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
 	
 	@Test
 	public void testDoubleExtraction () throws URISyntaxException {
 		// Read all files in the data directory
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root+"Paragraph_TM.tmx", null));
 
 		RoundTripComparison rtc = new RoundTripComparison();
 		assertTrue(rtc.executeCompare(filter, list, "UTF-8", "en-us", "fr-fr"));
 	}	
 	
 }
