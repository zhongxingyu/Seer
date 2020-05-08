 package gov.nih.nci.caxchange.caxchangerequest;
 
 import gov.nih.nci.caxchange.messaging.Credentials;
 import gov.nih.nci.caxchange.messaging.Message;
 import gov.nih.nci.caxchange.messaging.MessagePayload;
 import gov.nih.nci.caxchange.messaging.Metadata;
 import gov.nih.nci.caxchange.messaging.ObjectFactory;
 import gov.nih.nci.caxchange.messaging.Request;
 import gov.nih.nci.caxchange.messaging.Response;
 import gov.nih.nci.caxchange.messaging.TransactionControls;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.concurrent.Future;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 
 public class SampleCaXchangeReqClient {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		SampleCaXchangeReqClient testCXFBC = new SampleCaXchangeReqClient();
 
 		Message messageToESB = testCXFBC.buildMessageToESB();
 
 		CaXchangeRequestService caXchangeRequestService;
 		try {
 			caXchangeRequestService = new CaXchangeRequestService(new URL(
 					"http://localhost:8194/CaXchangeRequestService/main?wsdl"),
 					new QName("http://caXchange.nci.nih.gov/caxchangerequest",
 							"CaXchangeRequestService"));
 
 			CaXchangeRequestPortType caXchangeRequestPortType = caXchangeRequestService
 					.getSoap();
 			
 			
 //			non-blocking polling approach - asynchronous
 //			System.out.println("Invoking processRequestAsync...");
 //			javax.xml.ws.Response<gov.nih.nci.caxchange.messaging.ResponseMessage> _processRequestAsync__return = caXchangeRequestPortType
 //					.processRequestAsync(messageToESB);
 //			while (!_processRequestAsync__return.isDone()) {
 //				System.out.println("SLEEPING");
 //				Thread.sleep(100);
 //				
 //			}
 //			System.out.println("RESPONSE STATUS: "
 //					+ _processRequestAsync__return.get().getResponse().getResponseStatus());
 			
 			
 			
 			
 			// Callback approach - asynchronous
 			SampleAsyncHandler testAsyncHandler = new SampleAsyncHandler();
 		    System.out.println("Invoking processRequestAsync using callback object...");
 		    Future<?> response = caXchangeRequestPortType.processRequestAsync(messageToESB, testAsyncHandler);
 		    while (!response.isDone()) {
 		      Thread.sleep(100);
 		    }
 		    Response resp = testAsyncHandler.getResponse();
 		    System.out.println("CALLBACK RESPONSE: "+resp.getResponseStatus());
 		    System.exit(0);
 
 			
 			
 //			synchronous approach
 //			System.out.println("Invoking processRequest...");
 //			gov.nih.nci.caxchange.messaging.ResponseMessage _processRequest_return = caXchangeRequestPortType
 //					.processRequest(messageToESB);
 //			System.out.println("RESPONSE STATUS: "
 //			+ _processRequest_return.getResponse().getResponseStatus());
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	private Message buildMessageToESB() {
 
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
 			metadata.setOperationName(new ObjectFactory().createMetadataOperationName("ProcessRequest"));
 			metadata.setServiceType("LAB_BASED_AE");
 			requestMessageToESB.setMetadata(metadata);
 
 			// Create and set the request
 			Request request = new Request();
 			MessagePayload messagePayload = new MessagePayload();
 
 			messagePayload
 					.setXmlSchemaDefinition("gme://ccts.cabig/1.0/gov.nih.nci.cabig.ccts.domain");
			System.out.println(SampleCaXchangeReqClient.class.getClassLoader().getResourceAsStream("/gov/nih/nci/caxchange/caxchangerequest/sample-payload.xml"));
			InputStream testMessage = SampleCaXchangeReqClient.class.getClassLoader().getResourceAsStream("/src/main/java/gov/nih/nci/caxchange/caxchangerequest/sample-payload.xml");
 			if (testMessage == null) {
 				throw new RuntimeException("Test message does not exist.");
 			}
 
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			Document payload = db.parse(testMessage);
 //			MessageElement messageElement = new MessageElement(payload
 //					.getDocumentElement());
 //			messagePayload.setAny(new MessageElement[] { messageElement });
 			messagePayload.setAny(payload.getDocumentElement());
 			request.setBusinessMessagePayload(messagePayload);
 
 			requestMessageToESB.setRequest(request);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return requestMessageToESB;
 	}
 
 }
