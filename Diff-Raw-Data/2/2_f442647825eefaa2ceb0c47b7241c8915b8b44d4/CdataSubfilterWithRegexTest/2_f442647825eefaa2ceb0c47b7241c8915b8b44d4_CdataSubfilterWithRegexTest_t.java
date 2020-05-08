 package net.sf.okapi.filters.xmlstream.integration;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.FilterConfigurationMapper;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.common.filters.RoundTripComparison;
 import net.sf.okapi.filters.xmlstream.XmlStreamFilter;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class CdataSubfilterWithRegexTest {
 
 	private XmlStreamFilter xmlStreamFilter;	
 	private String root;
 	private LocaleId locEN = LocaleId.ENGLISH;
 	private FilterConfigurationMapper fcMapper;
 	
 	@Before
 	public void setUp() throws Exception {		       
 		xmlStreamFilter = new XmlStreamFilter();			
 		URL ditaUrl = DitaExtractionComparisionTest.class.getResource("/bookmap-readme.dita");				
 		root = Util.getDirectoryName(ditaUrl.toURI().getPath()) + File.separator;
 		
 		fcMapper = new FilterConfigurationMapper();
 		fcMapper.addConfigurations("net.sf.okapi.filters.html.HtmlFilter");
		fcMapper.addConfigurations("net.sf.okapi.filters.xmlstream.XmlStreamFilter");
 		fcMapper.setCustomConfigurationsDirectory(root);
         fcMapper.addCustomConfiguration("okf_html@spaces_freemarker_regex");
         fcMapper.addCustomConfiguration("okf_html@spaces_freemarker_no_regex");
         fcMapper.updateCustomConfigurations();
         xmlStreamFilter.setFilterConfigurationMapper(fcMapper);
 	}
 	
 	//@Test
 	public void testDoubleExtractionWithRegex() throws URISyntaxException, MalformedURLException {
 		xmlStreamFilter.setParametersFromURL(XmlStreamFilter.class.getResource("/okf_xmlstream@freemarker.fprm"));
 		RoundTripComparison rtc = new RoundTripComparison();
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		list.add(new InputDocument(root + "xml-freemarker.xml", null));
 		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN, locEN));
 	}
 	
 	@Test
 	public void testDoubleExtractionWithoutRegex() throws URISyntaxException, MalformedURLException {
 		xmlStreamFilter.setParametersFromURL(XmlStreamFilter.class.getResource("/okf_xmlstream@freemarker_no_regex.fprm"));
 		RoundTripComparison rtc = new RoundTripComparison();
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		list.add(new InputDocument(root + "xml-freemarker.xml", null));
 		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN, locEN));
 	}
 
 	//@Test
 	public void testDoubleExtractionWithoutSubfilter() throws URISyntaxException, MalformedURLException {
 		xmlStreamFilter.setParametersFromURL(XmlStreamFilter.class.getResource("/okf_html@spaces_freemarker_regex.fprm"));
 		RoundTripComparison rtc = new RoundTripComparison();
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		list.add(new InputDocument(root + "freemarker.html", null));
 		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN, locEN));
 	}
 }
