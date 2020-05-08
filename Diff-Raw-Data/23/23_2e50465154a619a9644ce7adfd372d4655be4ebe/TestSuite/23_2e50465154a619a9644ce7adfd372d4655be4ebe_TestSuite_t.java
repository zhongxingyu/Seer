 package net.sf.okapi.tests;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 @RunWith(Suite.class)
 @SuiteClasses({
	net.sf.okapi.common.tests.AllTests.class,
 	net.sf.okapi.common.filters.tests.AllTests.class,
 	net.sf.okapi.common.tests.AllCommonFiltersTests.class,
 	net.sf.okapi.filters.html.tests.AllHtmlUnitTests.class,
 	net.sf.okapi.filters.html.tests.integration.ExtractionComparisionTest.class,
 	net.sf.okapi.filters.idml.tests.IDMLContentFilterTest.class,
 	net.sf.okapi.filters.idml.tests.IDMLFilterTest.class,
 	net.sf.okapi.filters.mif.tests.MIFFilterTest.class,
 	net.sf.okapi.filters.openoffice.tests.ODFFilterTest.class,
 	net.sf.okapi.filters.openoffice.tests.OpenOfficeFilterTest.class,
 	net.sf.okapi.filters.openxml.tests.AllOpenXMLTests.class,
 	net.sf.okapi.filters.po.tests.POFilterTest.class,
 	net.sf.okapi.filters.properties.tests.AllTests.class,
 	net.sf.okapi.filters.regex.tests.RegexFilterTest.class,
 	net.sf.okapi.filters.rtf.tests.RTFFilterTest.class,
 	net.sf.okapi.filters.rtf.tests.RtfEventTest.class,
 	net.sf.okapi.filters.rtf.tests.RtfSnippetsTest.class,
 	net.sf.okapi.filters.rtf.tests.RtfFullFileTest.class,
 	net.sf.okapi.filters.rtf.tests.integration.ExtractionComparisionTest.class,	
 	net.sf.okapi.filters.tmx.tests.TmxFilterTest.class,
 	net.sf.okapi.filters.xliff.tests.XLIFFFilterTest.class,
 	net.sf.okapi.filters.xml.tests.XMLFilterTest.class,
 	net.sf.okapi.lib.jython.tests.JythonBasedPipelineTest.class,
 	net.sf.okapi.lib.segmentation.tests.AllTests.class,
 	net.sf.okapi.common.pipeline.tests.AllPipelineTests.class,
 	net.sf.okapi.common.pipeline.tests.integration.AllPipelineIntegrationTests.class
 })
 public class TestSuite {
 }
