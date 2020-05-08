 package org.gvlabs.utils.soap;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.soap.MessageFactory;
 import javax.xml.soap.MimeHeaders;
 import javax.xml.soap.SOAPBody;
 import javax.xml.soap.SOAPConnection;
 import javax.xml.soap.SOAPConnectionFactory;
 import javax.xml.soap.SOAPException;
 import javax.xml.soap.SOAPMessage;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 public class SoapClient {
 
 	private String endpoint;
 
 	public SoapClient(String endpoint) {
 		super();
 		this.endpoint = endpoint;
 	}
 
	public String invokeOperation(String operation, String input) throws UnsupportedOperationException, SOAPException, ParserConfigurationException, SAXException, IOException, TransformerException {
 		String response = null;
 		SOAPConnection connection = null;
 		try {
 			// Connection
 			SOAPConnectionFactory soaConnFactory = SOAPConnectionFactory
 					.newInstance();
 			connection = soaConnFactory.createConnection();
 
 			// Message
 			MessageFactory messageFactory = MessageFactory.newInstance();
 			SOAPMessage message = messageFactory.createMessage();
 
 			// Header
 			MimeHeaders headers = message.getMimeHeaders();
 			headers.addHeader("SOAPAction", operation);
 
 			// Body
 			SOAPBody body = message.getSOAPBody();
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			dbf.setNamespaceAware(true);
 
 			DocumentBuilder parser = dbf.newDocumentBuilder();
 
 			StringBuffer inputData = new StringBuffer(input);
 			ByteArrayInputStream bis = new ByteArrayInputStream(inputData
 					.toString().getBytes("UTF-8"));
 			Document xmlDoc = parser.parse(bis);
 
 			xmlDoc.setStrictErrorChecking(false);
 			body.addDocument(xmlDoc);
 
 			message.saveChanges();
 
 			// Call a operation
 			SOAPMessage soapResponse = connection.call(message, new URL(
 					endpoint));
 
 			if (soapResponse != null) {
 				// Get response
 				TransformerFactory tff = TransformerFactory.newInstance();
 				Transformer tf = tff.newTransformer();
 
 				Source sc = soapResponse.getSOAPPart().getContent();
 
 				ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				StreamResult result = new StreamResult(baos);
 				tf.transform(sc, result);
 				response = baos.toString("UTF-8");
 			}
 
 		} finally {
 			if (connection != null) {
 				connection.close();
 			}
 		}
 		return response;
 	}
 }
