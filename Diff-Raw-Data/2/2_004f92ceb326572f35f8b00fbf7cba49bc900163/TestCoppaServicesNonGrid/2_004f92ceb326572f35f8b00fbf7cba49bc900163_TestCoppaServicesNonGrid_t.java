 package gov.nih.nci.cagrid.caxchange.test;
 
 import gov.nih.nci.caxchange.caxchangerequest.CaXchangeRequestPortType;
 import gov.nih.nci.caxchange.caxchangerequest.CaXchangeRequestService;
import gov.nih.nci.caxchange.cxfsamples.SampleAsyncHandler;
 import gov.nih.nci.caxchange.messaging.Credentials;
 import gov.nih.nci.caxchange.messaging.Message;
 import gov.nih.nci.caxchange.messaging.MessagePayload;
 import gov.nih.nci.caxchange.messaging.Metadata;
 import gov.nih.nci.caxchange.messaging.ObjectFactory;
 import gov.nih.nci.caxchange.messaging.Request;
 import gov.nih.nci.caxchange.messaging.Response;
 import gov.nih.nci.caxchange.messaging.Statuses;
 import gov.nih.nci.caxchange.messaging.TransactionControls;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.concurrent.Future;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.w3c.dom.Document;
 
 public class TestCoppaServicesNonGrid extends TestCase {
 
 	private String serviceType = null;
 	private String payloadFileName = null;
 	private String operationName = null;
 	private String url = null;
 	private String synchronousProcessing = null;
 
 	public TestCoppaServicesNonGrid(String name) {
 		super(name);
 	}
 
 	public void testCoppaServiceNonGrid() {
 		try {
 			serviceType = System.getProperty("service.type");
 			payloadFileName = System.getProperty("payload.file.name");
 			operationName = System.getProperty("operation.name");
 			url = System.getProperty("caxchange.cxfbc.url");
 			synchronousProcessing = System.getProperty("synchronous");
 
 			Message messageToCXFBC = buildMessageToCXFBC(serviceType,
 					payloadFileName, operationName);
 
 			CaXchangeRequestService caXchangeRequestService = new CaXchangeRequestService(
 					new URL(url),
 					new QName("http://caXchange.nci.nih.gov/caxchangerequest",
 							"CaXchangeRequestService"));
 
 			CaXchangeRequestPortType caXchangeRequestPortType = caXchangeRequestService
 					.getSoap();
 
 			if ("true".equals(synchronousProcessing)) {
 				System.out.println("Invoking the service Synchronously.");
 				gov.nih.nci.caxchange.messaging.ResponseMessage _processRequest_return = caXchangeRequestPortType
 						.processRequest(messageToCXFBC);
 				System.out.println("RESPONSE STATUS: "
 						+ _processRequest_return.getResponse()
 								.getResponseStatus());
 				assertNotNull(_processRequest_return);
 				assertEquals(_processRequest_return.getResponse()
 						.getResponseStatus(), Statuses.SUCCESS);
 			} else {
 				System.out.println("Invoking the service Asynchronously.");
 				// Callback approach - asynchronous
 				SampleAsyncHandler testAsyncHandler = new SampleAsyncHandler();
 				
 				Future<?> response = caXchangeRequestPortType
 						.processRequestAsync(messageToCXFBC, testAsyncHandler);
 				while (!response.isDone()) {
 					Thread.sleep(100);
 				}
 				Response resp = testAsyncHandler.getResponse();
 				assertNotNull(resp);
 				assertEquals(resp.getResponseStatus(), Statuses.SUCCESS);
 				System.out.println("CALLBACK RESPONSE: "
 						+ resp.getResponseStatus());
 
 				// non-blocking polling approach - asynchronous
 				// System.out.println("Invoking processRequestAsync...");
 				// javax.xml.ws.Response<gov.nih.nci.caxchange.messaging.ResponseMessage>
 				// _processRequestAsync__return = caXchangeRequestPortType
 				// .processRequestAsync(messageToESB);
 				// while (!_processRequestAsync__return.isDone()) {
 				// System.out.println("SLEEPING");
 				// Thread.sleep(100);
 				//			
 				// }
 				// System.out.println("RESPONSE STATUS: "
 				// +
 				// _processRequestAsync__return.get().getResponse().getResponseStatus());
 				
 			}
 		} catch (Exception e) {
 
 			System.out.println("Error sending message .");
 			e.printStackTrace();
 			fail();
 		}
 
 	}
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite();
 
 		suite.addTest(new TestCoppaServicesNonGrid("testCoppaServiceNonGrid"));
 
 		return suite;
 	}
 
 	public static void main(String[] args) {
 		System.out.println("STARTING TEST");
 		junit.textui.TestRunner.run(suite());
 	}
 
 	/**
 	 * Builds the request message to CXF binding component
 	 * @param serviceType
 	 * @param payloadFileName
 	 * @param operationName
 	 * @return
 	 */
 	private Message buildMessageToCXFBC(String serviceType,
 			String payloadFileName, String operationName) {
 
 		Message requestMessageToESB = new Message();
 		try {
 			// Create and set the metadata
 			Metadata metadata = new Metadata();
 			metadata.setTransactionControl(TransactionControls.PROCESS);
 
 			// build the credentials object. Only one of the credential choices
 			// can be set the last choice values will reset the other choices in
 			// the group
 
 			Credentials credentials = new Credentials();
 			credentials.setUserName("ccts@nih.gov");
 			credentials.setPassword("!Ccts@nih.gov1");
 			metadata.setCredentials(credentials);
 
 			metadata
 					.setCaXchangeIdentifier("037068f0-23a8-11de-a5f1-d00caf9050fd");
 			metadata.setExternalIdentifier("myExternalIdentifier");
 			metadata.setOperationName(new ObjectFactory()
 					.createMetadataOperationName(operationName));
 			metadata.setServiceType(serviceType);
 			requestMessageToESB.setMetadata(metadata);
 
 			// Create and set the request
 			Request request = new Request();
 			MessagePayload messagePayload = new MessagePayload();
 
 			messagePayload
 					.setXmlSchemaDefinition("");
 
 			InputStream testMessage = getResourceInputStream("/payloads/"
 					+ payloadFileName);
 
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			dbf.setNamespaceAware(true);
 
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			Document payload = db.parse(testMessage);
 			messagePayload.setAny(payload.getDocumentElement());
 			request.setBusinessMessagePayload(messagePayload);
 
 			requestMessageToESB.setRequest(request);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return requestMessageToESB;
 	}
 
 	/**
 	 * 
 	 * @param fileName
 	 * @return
 	 * @throws Exception
 	 */
 	public InputStream getResourceInputStream(String fileName) throws Exception {
 		
 		InputStream testMessage = this.getClass()
 		.getResourceAsStream(fileName);
 		if (testMessage == null) {
 			throw new Exception("Test message does not exist.");
 		}
 		return testMessage;
 		
 		/*ClassPathResource cpr = new ClassPathResource(fileName);
 		if (!cpr.exists()) {
 			throw new Exception(fileName + " does not exist.");
 		}
 		try {
 			InputStream inputStream = cpr.getInputStream();
 			return inputStream;
 		} catch (IOException e) {
 			throw new Exception("Error loading file " + fileName);
 		}*/
 	}
 
 }
