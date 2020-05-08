 package net.sf.okapi.filters.html.integration;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.filters.html.HtmlFilter;
 import net.sf.okapi.filters.html.HtmlUtils;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.common.filters.RoundTripComparison;
 import net.sf.okapi.common.LocaleId;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ExtractionComparisionTest {
 
 	private HtmlFilter htmlFilter;
 	private String[] testFileList;
 	private String root;
 	private LocaleId locEN = LocaleId.fromString("en");
 
 	@Before
 	public void setUp() throws Exception {
 		htmlFilter = new HtmlFilter();
		//htmlFilter.setParametersFromURL(ExtractionComparisionTest.class.getResource("/testConfiguration1.yml"));
 		testFileList = HtmlUtils.getHtmlTestFiles();
 
 		URL url = ExtractionComparisionTest.class.getResource("/324.html");
 		root = Util.getDirectoryName(url.getPath()) + File.separator;
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testStartDocument() {
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(htmlFilter,
 				new InputDocument(root + "324.html", null),
 				"UTF-8", locEN, locEN));
 	}
	
	//@Test
	public void testDoubleExtractionSingle() throws URISyntaxException {
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root + "W3CHTMHLTest1.html", null));
		assertTrue(rtc.executeCompare(htmlFilter, list, "UTF-8", locEN, locEN));
	}
	
	@Test
 	public void testDoubleExtraction() throws URISyntaxException {
 		RoundTripComparison rtc = new RoundTripComparison();
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		for (String f : testFileList) {
 			list.add(new InputDocument(root + f, null));
 		}
 		assertTrue(rtc.executeCompare(htmlFilter, list, "UTF-8", locEN, locEN));
 	}
 }
