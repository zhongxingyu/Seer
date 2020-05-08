 package com.xmlmachines.tests;
 
 import java.io.IOException;
 
 import junit.framework.Assert;
 import nu.xom.ParsingException;
 import nu.xom.ValidityException;
 
 import org.apache.log4j.Logger;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.marklogic.xcc.Request;
 import com.marklogic.xcc.ResultSequence;
 import com.marklogic.xcc.Session;
 import com.marklogic.xcc.exceptions.RequestException;
 import com.xmlmachines.Consts;
 import com.xmlmachines.TestHelper;
 import com.xmlmachines.providers.IOUtilsProvider;
 import com.xmlmachines.providers.MarkLogicContentSourceProvider;
 
 public class MLBuildAndConfigurationTest {
 
 	private static Logger LOG = Logger
 			.getLogger(MLBuildAndConfigurationTest.class);
 
 	@BeforeClass
 	public static void setUp() throws RequestException {
 		// LOG.info("creating ML database for tests");
 		Session s = MarkLogicContentSourceProvider.getInstance()
 				.getProductionContentSource().newSession();
 
 		s.submitRequest(
 				s.newAdhocQuery(IOUtilsProvider.getInstance().readFileAsString(
 						"src/main/resources/xqy/basic-test-setup.xqy")))
 				.close();
 		// Populate
 		TestHelper t = new TestHelper();
 		try {
 			t.processMedlineXML("xom");
 		} catch (ValidityException e) {
 			LOG.error(e);
 		} catch (ParsingException e) {
 			LOG.error(e);
 		} catch (IOException e) {
 			LOG.error(e);
 		}
 
 	}
 
 	@Test
 	public void wasIngestCompleted() throws RequestException {
 		Session s = MarkLogicContentSourceProvider.getInstance()
 				.getProductionContentSource().newSession(Consts.UNIT_TEST_DB);
 		Request r = s.newAdhocQuery("xdmp:database()");
 		ResultSequence rs = s.submitRequest(r);
 		LOG.info("Database id: " + rs.asString());
 		r = s.newAdhocQuery("xdmp:estimate(doc())");
 		rs = s.submitRequest(r);
 		Assert.assertEquals("156", rs.asString());
 		s.close();
 	}
 
 	@Test
 	public void isFirstDocumentElemOfExpectedType() throws RequestException {
 		Session s = MarkLogicContentSourceProvider.getInstance()
 				.getProductionContentSource().newSession(Consts.UNIT_TEST_DB);
 		Request r = s
 				.newAdhocQuery("fn:name(doc()[1]/element()) eq 'MedlineCitation'");
 		ResultSequence rs = s.submitRequest(r);
 		Assert.assertEquals("true", rs.asString());
 		s.close();
 	}
 
 	@Test
 	public void isUriLexiconAvailable() throws RequestException {
 		Session s = MarkLogicContentSourceProvider.getInstance()
 				.getProductionContentSource().newSession(Consts.UNIT_TEST_DB);
 		Request r = s.newAdhocQuery("cts:uris( '', ('document') )");
 		ResultSequence rs = s.submitRequest(r);
 		// LOG.info(rs.asString());
		Assert.assertEquals(157, rs.size());
 		s.close();
 	}
 
 	@Test
 	public void isCollectionLexiconAvailable() throws RequestException {
 		Session s = MarkLogicContentSourceProvider.getInstance()
 				.getProductionContentSource().newSession(Consts.UNIT_TEST_DB);
 
         // Given I have a doc with a collections
         Request r = s.newAdhocQuery("xdmp:document-insert('2.xml', <two/>, (), 'test')");
         s.submitRequest(r);
 
         // When I call cts:collections for that doc
         // Then the total collections for that doc should be one
         Request r1 = s.newAdhocQuery("count(cts:collections('2.xml'))");
         ResultSequence rs1 = s.submitRequest(r1);
         Assert.assertEquals(1, rs1.size());
         Assert.assertEquals("2", rs1.asString());
 
         Request r3 = s.newAdhocQuery("xdmp:document-delete('2.xml')");
         s.submitRequest(r3);
         s.close();
 	}
 
 	@Test
 	public void addDocsToCollection() throws RequestException {
 		Session s = MarkLogicContentSourceProvider.getInstance()
 				.getProductionContentSource().newSession(Consts.UNIT_TEST_DB);
 		Request r = s
 				.newAdhocQuery("for $doc in doc()/MedlineCitation/@Status[. ne 'MEDLINE']\nreturn (xdmp:document-add-collections(xdmp:node-uri($doc), 'not-medline'), <done/>)");
 		ResultSequence rs = s.submitRequest(r);
 		//LOG.info(rs.asString());
 		Assert.assertEquals(21, rs.size());
 		s.close();
 	}
 
 	@Test
 	public void doesDatabaseContainOneCollection() throws RequestException {
 		Session s = MarkLogicContentSourceProvider.getInstance()
 				.getProductionContentSource().newSession(Consts.UNIT_TEST_DB);
 
         Request r = s.newAdhocQuery("xdmp:document-insert('1.xml', <one/>)");
         s.submitRequest(r);
         Request r1 = s.newAdhocQuery("count(cts:collections('1.xml'))");
         ResultSequence rs = s.submitRequest(r1);
         Assert.assertEquals("0", rs.asString());
 
         Request r2 = s.newAdhocQuery("xdmp:document-add-collections('1.xml', 'test')");
 		ResultSequence rs1 = s.submitRequest(r2);
         ResultSequence rs2 = s.submitRequest(r1);
 
 		Assert.assertEquals(1, rs2.size());
 		Assert.assertEquals("1", rs2.asString());
 
         Request r3 = s.newAdhocQuery("xdmp:document-delete('1.xml')");
         s.submitRequest(r3);
 		s.close();
 	}
 
 	@AfterClass
 	public static void tearDown() throws RequestException {
 		Session s = MarkLogicContentSourceProvider.getInstance()
 				.getProductionContentSource().newSession();
 
 		s.submitRequest(
 				s.newAdhocQuery(IOUtilsProvider.getInstance().readFileAsString(
 						"src/main/resources/xqy/basic-test-teardown.xqy")))
 				.close();
 
 	}
 
 }
