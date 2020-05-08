 package net.metadata.auselit.lorestore.servlet;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathFactory;
 
 import net.metadata.auselit.lorestore.access.AllowEverythingAccessPolicy;
 import net.metadata.auselit.lorestore.access.DefaultOREAccessPolicy;
 import net.metadata.auselit.lorestore.exceptions.InvalidQueryParametersException;
 import net.metadata.auselit.lorestore.exceptions.NotFoundException;
 import net.metadata.auselit.lorestore.exceptions.OREException;
 import net.metadata.auselit.lorestore.triplestore.MemoryTripleStoreConnectorFactory;
 import net.metadata.auselit.lorestore.triplestore.TripleStoreConnectorFactory;
 import net.metadata.auselit.lorestore.util.UIDGenerator;
 import net.metadata.auselit.test.mocks.MockAuthenticationContext;
 import net.metadata.auselit.test.mocks.MockOREIdentityProvider;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.ontoware.rdf2go.model.Model;
 import org.springframework.security.access.AccessDeniedException;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import au.edu.diasb.chico.mvc.RequestFailureException;
 
 @RunWith(JUnit4.class)
 public class OREControllerTest {
 
 	private TripleStoreConnectorFactory cf;
 	private MockOREIdentityProvider ip;
 	static OREController controller;
 	static OREController authController;
 	static XPath xPath;
 
 	@Rule
 	public ExpectedException exception = ExpectedException.none();
 	
 	@Before
 	public  void setUp() throws Exception {
 		cf = new MemoryTripleStoreConnectorFactory();
 		controller = getController();
 		authController = getAuthController();
 		xPath = XPathFactory.newInstance().newXPath();
 	}
 
 	private  OREController getController() throws InterruptedException {
 		OREControllerConfig occ = new OREControllerConfig();
 		occ.setContainerFactory(cf);
 		// HttpTripleStoreConnectorFactory cf = new
 		// HttpTripleStoreConnectorFactory();
 		// cf.setRepositoryURL("http://localhost:8080/openrdf-sesame/repositories/lore");
 		// occ.setContainerFactory(cf);
 		occ.setAccessPolicy(new AllowEverythingAccessPolicy());
 		occ.setBaseUri("http://example.com/");
 		occ.setUidGenerator(new UIDGenerator());
 		occ.setIdentityProvider(new MockOREIdentityProvider());
 		return new OREController(occ);
 	}
 	
 	private OREController getAuthController() throws Exception {
 		OREControllerConfig occ = new OREControllerConfig();
 		occ.setContainerFactory(cf);
 		ip = new MockOREIdentityProvider();
 		DefaultOREAccessPolicy ap = new DefaultOREAccessPolicy();
 		ap.setReadAuthorities("ROLE_USER,ROLE_ANONYMOUS");
 		ap.setWriteAuthorities("ROLE_ORE");
 		ap.afterPropertiesSet();
 		ap.setIdentityProvider(ip);
 		occ.setAccessPolicy(ap);
 
 		occ.setIdentityProvider(ip);
 		occ.setBaseUri("http://example.com/");
 		occ.setUidGenerator(new UIDGenerator());
 		return new OREController(occ);
 	}
 
 	private void updateAuthenticationContext(String username, String uri, String[] authorities) throws Exception {
 		DefaultOREAccessPolicy accessPolicy = (DefaultOREAccessPolicy)authController.getControllerConfig().getAccessPolicy();
 		accessPolicy.setAuthenticationContext(new MockAuthenticationContext(username, uri, authorities));
 		accessPolicy.afterPropertiesSet();
 		
 		ip.setUserURI(uri);
 	}
 
 
 	@Test(expected = NotFoundException.class)
 	public void deleteUnknown() throws Exception {
 		controller.delete("13532");
 
 	}
 
 	@Test
 	public void postEmpty() throws Exception {
 		InputStream in = new ByteArrayInputStream("".getBytes());
 		try {
 			controller.post(in);
 			fail("should have thrown exception");
 		} catch (RequestFailureException e) {
 			assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
 		}
 	}
 
 	@Test(expected = RequestFailureException.class)
 	public void postBadCompoundObject() throws Exception {
 		ByteArrayInputStream in = new ByteArrayInputStream(
 				CommonTestRecords.BAD_ORE_BROKEN_XML.getBytes());
 		controller.post(in);
 		// expect 400 - bad request
 	}
 
 	@Test(expected = OREException.class)
 	public void postBadCompoundObject2() throws Exception {
 		ByteArrayInputStream in = new ByteArrayInputStream(
 				CommonTestRecords.BAD_ORE_NO_RESOURCEMAP.getBytes());
 		controller.post(in);
 		// expect 400 - bad request
 	}
 
 	@Test
 	public void postCompoundObjectAndGet() throws Exception {
 		InputStream in = new ByteArrayInputStream(
 				CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
 		OREResponse response = controller.post(in);
 		
 		assertNotNull(response.getLocationHeader());
 		assertNotNull(response.getReturnStatus());
 		assertEquals(201, response.getReturnStatus());
 
 		// assertEquals(16, cf.size());
 	}
 
 	@Test
 	public void postGetDeleteGet() throws Exception {
 		String createdId = saveRecordToStore(CommonTestRecords.SIMPLE_ORE_EXAMPLE);
 
 		OREResponse oreResponse2 = controller.get(createdId);
 		assertNotNull(oreResponse2);
 
 		assertEquals("ore", oreResponse2.getViewName());
 		Map<String, Object> model2 = oreResponse2.getModel();
 		Model rdf2 = (Model) model2.get(OREResponse.RESPONSE_RDF_KEY);
 		assertNotNull(rdf2);
 
 		controller.delete(createdId);
 
 		try {
 			controller.get(createdId);
 			fail("Object should have been deleted, method should have thrown exception");
 		} catch (NotFoundException e) {
 			// Expected
 		}
 
 		// check responses of get and delete pass (are 200), and then the second
 		// get should 404
 
 	}
 
 	@Test(expected = OREException.class)
 	public void putNonexistent() throws Exception {
 		InputStream in = new ByteArrayInputStream(
 				CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
 
 		controller.put("", in);
 	}
 
 	@Test
 	public void postThenPut() throws Exception {
 		String id = saveRecordToStore(CommonTestRecords.SIMPLE_ORE_EXAMPLE);
 
 		InputStream in = new ByteArrayInputStream(
 				CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
 		controller.put(id, in);
 
 	}
 
 	private String saveRecordToStore(String recordXML) throws Exception {
 		InputStream in = new ByteArrayInputStream(recordXML.getBytes());
 		OREResponse response = controller.post(in);
 		
 		String id = findUIDFromResponse(response);
 
 		return id;
 		
 	}
 
 	private String findUIDFromResponse(OREResponse response) {
 //		assertTrue(redirect.startsWith("redirect:"));
 		String redirect = response.getLocationHeader();
 		String createdId = redirect.substring(redirect.lastIndexOf("/") + 1);
 		return createdId;
 	}
 
 	@Test(expected = InvalidQueryParametersException.class)
 	public void queryRefersToEmpty() throws Exception {
 		controller.refersToQuery("");
 	}
 
 	// @Test(expected = InvalidQueryParametersException.class)
 	// public void queryRefersToInvalidURL() throws Exception {
 	// controller.refersToQuery("zxcv");
 	// }
 
 	@Test
 	public void queryRefersToNonExistentURL() throws Exception {
 		String body = controller.refersToQuery("http://omad.net/").getBody();
 		assertNotNull(body);
 
 		Document document = parseXmlToDocument(body);
 		NodeList results = document.getElementsByTagName("result");
 		assertEquals(0, results.getLength());
 	}
 	
 	private Document parseXmlToDocument(String xml) throws Exception {
 		return DocumentBuilderFactory.newInstance().newDocumentBuilder()
 				.parse(new InputSource(new StringReader(xml)));
 	}
 
 	@Test
 	public void queryRefersToExistingURL() throws Exception {
 		// save an object
 		String createdId = saveRecordToStore(CommonTestRecords.SIMPLE_ORE_EXAMPLE);
 
 		// query for it
 		String body = controller.refersToQuery("http://omad.net/").getBody();
 
 		// check the results include the object
 		assertNotNull(body);
 		assertTrue(body.contains(createdId));
 
 		Document document = parseXmlToDocument(body);
 		assertEquals(1, document.getElementsByTagName("result").getLength());
 
 		assertEquals("Damien Ayers",
 				xPath.evaluate("//binding[@name='a']/literal", document));
 	}
 
 	
 	@Test
 	public void keywordSearch() throws Exception {
 		String body = controller.keywordSearch("test").getBody();
 
 		assertNotNull(body);
 	}
 	
 	@Test(expected = AccessDeniedException.class)
 	public void postWithNoAuth() throws Exception {
 		InputStream in = new ByteArrayInputStream(
 				CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
		authController.post(in);
 	}
 	
 	@Test
 	public void postWithAuth() throws Exception {
 		updateAuthenticationContext("bob", "http://example.com/user/bob", new String[]{"ROLE_USER","ROLE_ORE"});
 		
 		InputStream in = new ByteArrayInputStream(
 				CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
		authController.post(in);
 	}
 	
 	@Test(expected = AccessDeniedException.class)
 	public void postWithNoAuth2() throws Exception {
 		InputStream in = new ByteArrayInputStream(
 				CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
		authController.post(in);
 	}
 	
 	
 	@Test(expected = OREException.class)
 	public void putToNonExistant() throws Exception {
 		InputStream in = new ByteArrayInputStream(
 				CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
 		authController.put("http://nonexistant.example.com/ore/id", in);
 	}
 	
 	@Test
 	public void putSuccessful() throws Exception {
 		String createdId = saveRecordToStore(CommonTestRecords.SIMPLE_ORE_EXAMPLE);
 		
 		InputStream in = new ByteArrayInputStream(
 				CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
		controller.put(createdId, in);
 		
 		in = new ByteArrayInputStream(
 				CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
		controller.put(createdId, in);
 	}
 	
 	@Test
 	public void putWithNoAuth() throws Exception {
 		String createdId = saveRecordToStore(CommonTestRecords.SIMPLE_ORE_EXAMPLE);
 		
 		InputStream in = new ByteArrayInputStream(
 				CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
 		
 		exception.expect(AccessDeniedException.class);
 		exception.expectMessage("Authentication problem: request has no authentication object");
		authController.put(createdId, in);
 	}
 	
 	@Test
 	public void postThenPutWithAuth() throws Exception {
 		updateAuthenticationContext("bob", "http://example.com/user/bob", new String[]{"ROLE_USER","ROLE_ORE"});
 		InputStream in = new ByteArrayInputStream(CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
 		OREResponse response = authController.post(in);
 		
 		String recordId = findUIDFromResponse(response);
 		
 		in = new ByteArrayInputStream(
 				CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
 		response = authController.put(recordId, in);
 	}
 	
 
 	
 	@Test
 	public void postThenPutWithChangedAuth() throws Exception {
 		updateAuthenticationContext("bob", "http://example.com/user/bob", new String[]{"ROLE_USER","ROLE_ORE"});
 		InputStream in = new ByteArrayInputStream(CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
 		OREResponse response = authController.post(in);
 		
 		String recordId = findUIDFromResponse(response);
 
 		updateAuthenticationContext("james", "http://example.com/user/james", new String[]{"ROLE_USER","ROLE_ORE"});
 		in = new ByteArrayInputStream(
 				CommonTestRecords.SIMPLE_ORE_EXAMPLE.getBytes());
 		
 		exception.expect(AccessDeniedException.class);
 		exception.expectMessage("You do not own this object");
 		response = authController.put(recordId, in);
 	}
 	
 //	@Test
 //	public void bulkImport() throws Exception {
 //		InputStream fileInput = new BufferedInputStream(new FileInputStream("D:/export.trig"));
 //		controller.bulkImport(fileInput, "application/x-trig");
 //	}
 }
