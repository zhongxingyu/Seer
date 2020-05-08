 package org.apache.servicemix.soap.handlers.addressing;
 
 import javax.xml.namespace.QName;
 
 import org.apache.servicemix.soap.Context;
 import org.apache.servicemix.soap.marshalers.SoapMessage;
 import org.w3c.dom.DocumentFragment;
 
 import junit.framework.TestCase;
 
 public class AddressingHandlerTest extends TestCase {
 
 
 	private AddressingHandler handler;
 
 	public AddressingHandlerTest(String name) {
 		super(name);
 	}
 
 	protected void setUp() throws Exception {
 		this.handler = new AddressingHandler();
 	}
 	
 	public void testCreateHeader() throws Exception {
		QName messageIdQN = new QName(AddressingHandler.WSA_NAMESPACE_200408, AddressingHandler.EL_MESSAGE_ID, AddressingHandler.WSA_PREFIX);
 		String messageId = "uuid:1234567890";
 		DocumentFragment wsaMessageId = this.handler.createHeader(messageIdQN, messageId);
 
 		assertNotNull("DocumentFragment is null", wsaMessageId);
 		assertEquals("messageId", messageId, wsaMessageId.getTextContent());
 		
 	}
 	
 	public void testWSAEmptyPrefix() throws Exception {
 		// setup
		QName messageIdQN = new QName(AddressingHandler.WSA_NAMESPACE_200408, AddressingHandler.EL_MESSAGE_ID, "");
		QName relatesToQN = new QName(AddressingHandler.WSA_NAMESPACE_200408, AddressingHandler.EL_RELATES_TO, "");
 
 		
 		// create messages and add them to the context
 		Context msgContext = new Context();
 		SoapMessage inMessage = new SoapMessage();
 		SoapMessage outMessage = new SoapMessage();
 		msgContext.setInMessage(inMessage);
 		msgContext.setOutMessage(outMessage);
 		
 		// add wsa MessageID header to in message
 		String messageId = "uuid:1234567890";
 		DocumentFragment wsaMessageId = this.handler.createHeader(messageIdQN, messageId);
 		inMessage.addHeader(messageIdQN, wsaMessageId);
 		
 		// run handler
 		this.handler.onReply(msgContext);
 		
 		// verify relates-to
 		DocumentFragment wsaRelatesTo = (DocumentFragment) outMessage.getHeaders().get(relatesToQN);
 		assertNotNull("No RelatesTo header", wsaRelatesTo);
 		assertEquals("Value", messageId, wsaRelatesTo.getTextContent());
 	}
 	
 	public void testWSAPrefix() throws Exception {
 		// setup
		QName messageIdQN = new QName(AddressingHandler.WSA_NAMESPACE_200408, AddressingHandler.EL_MESSAGE_ID, AddressingHandler.WSA_PREFIX);
		QName relatesToQN = new QName(AddressingHandler.WSA_NAMESPACE_200408, AddressingHandler.EL_RELATES_TO, AddressingHandler.WSA_PREFIX);
 
 		
 		// create messages and add them to the context
 		Context msgContext = new Context();
 		SoapMessage inMessage = new SoapMessage();
 		SoapMessage outMessage = new SoapMessage();
 		msgContext.setInMessage(inMessage);
 		msgContext.setOutMessage(outMessage);
 		
 		// add wsa MessageID header to in message
 		String messageId = "uuid:1234567890";
 		DocumentFragment wsaMessageId = this.handler.createHeader(messageIdQN, messageId);
 		inMessage.addHeader(messageIdQN, wsaMessageId);
 		
 		// run handler
 		this.handler.onReply(msgContext);
 		
 		// verify relates-to
 		DocumentFragment wsaRelatesTo = (DocumentFragment) outMessage.getHeaders().get(relatesToQN);
 		assertNotNull("No RelatesTo header", wsaRelatesTo);
 		assertEquals("Value", messageId, wsaRelatesTo.getTextContent());
 	}
 	
 
 }
