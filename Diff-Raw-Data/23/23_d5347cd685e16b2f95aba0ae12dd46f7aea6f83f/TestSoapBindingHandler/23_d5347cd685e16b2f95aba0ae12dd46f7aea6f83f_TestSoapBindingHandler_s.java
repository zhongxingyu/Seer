 package org.fabric3.tests.binding.metro.soap;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import javax.xml.namespace.QName;
 import javax.xml.soap.SOAPException;
 import javax.xml.soap.SOAPMessage;
 
 import org.fabric3.spi.binding.handler.BindingHandler;
 import org.fabric3.spi.invocation.Message;
 import org.fabric3.tests.binding.metro.wsdl.HelloJavaService;
 import org.oasisopen.sca.annotation.Reference;
 
 public class TestSoapBindingHandler implements BindingHandler<SOAPMessage> {
 	
 	@Reference private HelloJavaService h;
 
 	public QName getType() {
 		return null;
 	}
 	
 	public void handleOutbound(Message message, SOAPMessage context) {
 		printMessage("Out ",context);		
 	}
 
 	public void handleInbound(SOAPMessage context, Message message) {
 		printMessage("In ",context);
 	}
 	
 	private void printMessage(String prefix ,SOAPMessage context) {
 		System.out.println("h reference is " + (h != null) );
 		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
 		try {
 			context.writeTo(bOut);
 			System.out.println(prefix + " "+ new String(bOut.toByteArray()));
 		} catch (SOAPException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 }
