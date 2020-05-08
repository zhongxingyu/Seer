 
 package com.conwet.samson;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 
 import java.io.IOError;
 import java.io.IOException;
 import java.io.StringReader;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.conwet.samson.jaxb.ContextElement;
 import com.conwet.samson.jaxb.ContextElementResponse;
 import com.conwet.samson.jaxb.ContextRegistration;
 import com.conwet.samson.jaxb.EntityId;
 import com.conwet.samson.jaxb.RegisterContextRequest;
 import com.conwet.samson.jaxb.UpdateContextRequest;
 
 /**
  *
  * @author sergio
  */
 public class QuerierTest {
 	
 	private static JAXBContext context;
 	
 	static {
 		try {
 			context = JAXBContext.newInstance("com.conwet.samson.jaxb");
 		} catch (JAXBException e) {
 			throw new ExceptionInInitializerError(e);
 		}
 	}
 	
 	private String request;
 	private QueryBroker instance;
 	private MockServer mockServer;
 	private Unmarshaller unmarshaller;
 	
 	
 	@Before
 	public void setUp()throws Exception {
 		
 		unmarshaller = context.createUnmarshaller();
 		mockServer = new MockServer();
 		mockServer.start();
 		
 		instance = new Querier("localhost", mockServer.getServerPort());
 	}
 
 	@After
 	public void tearDown() {
 
 		mockServer.stop();
 	}
 	
 	private void loadXML(String reqFileName, String resFileName) {
 		
 		try {
 			request = loadString(reqFileName);
 			mockServer.setReply(loadString(resFileName));
 		} catch (IOException e) {
 			throw new IOError(e);
 		}
 	}
 	
 	private String loadString(String fileName) throws IOException {
 		
 		Path path = Paths.get("src/main/resources", fileName);
 		String xml = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
 		return stripXML(xml);
 	}
 	
 	/**
 	 * Removes header from XML and empty space between tags:
 	 * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
 	 * 
 	 * @param xml the XML content to clean
 	 * @return the cleaned XML
 	 */
 	private String stripXML(String xml) {
 		
 		return xml.replaceFirst("<\\?xml[^>]+>", "")
 					.replaceAll(">\\s+<", "><").trim();
 	}
 	
 	/**
 	 * Checks the request sent to the server
 	 * @param urlPath the path to use in HTTP path
 	 */
 	private void checkServerRequest(String urlPath) {
 		
 		String sentRequest = stripXML(mockServer.getRequest());
 		assertThat(sentRequest).contains(urlPath).contains(request);
 	}
 	
 	private <T> T unmarshall(Class<T> clazz) throws JAXBException {
 		
 		JAXBElement<?> elem = (JAXBElement<?>) unmarshaller.unmarshal(new StringReader(request));
 		return clazz.cast(elem.getValue());
 	}
 	
 	
 	@Test
 	public void shouldCreateNewEntityInstance() {
 		
 		String type = "MyType";
 		String id = "myID";
 		
 		EntityId entity = instance.newEntityId(type, id);
 		
 		assertThat(entity.getType()).isEqualTo(type);
 		assertThat(entity.getId()).isEqualTo(id);
 		assertThat(entity.isIsPattern()).isFalse();
 		assertThat(entity).isNotSameAs(instance.newEntityId(type, id));
 	}
 	
 	@Test
 	public void shouldQueryAllVendingMachines() throws Exception {
 		
 		loadXML("query.xml", "query-response.xml");
 		String type = "VendingMachine";
 		String id = "ven";
 		EntityId entityID = instance.newEntityId(type, id + "*");
 		
 		// query the service
 		List<ContextElementResponse> response = instance.queryContext(entityID)
 													.getContextResponseList()
 													.getContextElementResponse();
 		
 		// verify response contains all elements
 		assertThat(response).hasSize(10);
 		
 		for (ContextElementResponse cxtElemResp : response) {
 			
 			EntityId entity = cxtElemResp.getContextElement().getEntityId();
 			
 			assertThat(entity.getType()).isEqualTo(type);
 			assertThat(entity.getId()).startsWith(id);
 		}
 		
 		checkServerRequest("/ngsi10/queryContext");
 	}
 	
 	@Test
 	public void shouldRegisterContext() throws Exception {
 		
 		loadXML("register.xml", "register-response.xml");
 		
 		RegisterContextRequest regCxtReq = unmarshall(RegisterContextRequest.class);
 		ContextRegistration cxtReg = regCxtReq.getContextRegistrationList()
 											.getContextRegistration().get(0);
 		
		instance.registerContext(cxtReg, regCxtReq.getDuration());
 		
 		checkServerRequest("/ngsi9/registerContext");
 	}
 
 	@Test
 	public void shouldUpdateContext() throws Exception {
 		
 		loadXML("update.xml", "update-response.xml");
 		
 		UpdateContextRequest upCxtReq = unmarshall(UpdateContextRequest.class);
 		ContextElement cxtElem = upCxtReq.getContextElementList()
 										.getContextElement().get(0);
 		
		instance.updateContext(cxtElem, upCxtReq.getUpdateAction());
 		
 		checkServerRequest("/ngsi10/updateContext");		
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void shouldntAcceptNullHost() {
 		
 		new Querier(null, 22);
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void shouldntAcceptZeroPort() {
 		
 		new Querier("localhost", 0);
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void shouldntAcceptNegativePort() {
 		
 		new Querier("localhost", -5);
 	}
 }
