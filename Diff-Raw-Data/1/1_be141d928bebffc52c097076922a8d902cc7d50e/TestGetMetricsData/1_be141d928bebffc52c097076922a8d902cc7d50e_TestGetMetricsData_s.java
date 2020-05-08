 package org.ebayopensource.turmeric.services.monitoringservice.junit;
 
 import org.custommonkey.xmlunit.XMLUnit;
 import org.ebayopensource.turmeric.monitoring.util.CSVImporter;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * The Class TestGetMetricsData.
  *
  * @author Udayasankar Natarajan
  */
 
 public class TestGetMetricsData extends AbstractSOAQueryMetricsTest {
 	
 	/** The base path. */
 	private static String basePath = "META-INF/data/testcases/GetMetricsData";
 
 	/**
 	 * Sets the up before class.
 	 *
 	 * @throws Exception the exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		CSVImporter.importCSV();
 		XMLUnit.setIgnoreWhitespace(true);
 	}
 
 	/**
 	 * Testrequest1.
 	 *
 	 * @throws Exception the exception
 	 */
 	@Test
 	public void testResponseTime() throws Exception {
 		String requestXmlPath = basePath + "/turmeric/responseTimeRequest.xml";
 		String respXmlPath = basePath + "/turmeric/responseTimeResponse.xml";
 		testGetMetricsData(requestXmlPath, respXmlPath);
 	}
 	
 	@Test
         @Ignore
 	public void testCallCountOperation() throws Exception {
 		String requestXmlPath = basePath + "/turmeric/callcountRequest.xml";
 		String respXmlPath = basePath + "/turmeric/callcountResponse.xml";
 		testGetMetricsData(requestXmlPath, respXmlPath);
 	}
 
 	@Test
         @Ignore
 	public void testCallCountService() throws Exception {
 		String requestXmlPath = basePath + "/turmeric/callcountRequestService.xml";
 		String respXmlPath = basePath + "/turmeric/callcountResponseService.xml";
 		testGetMetricsData(requestXmlPath, respXmlPath);
 	}
 
 }
