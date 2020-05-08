 package org.gvlabs.utils.soap;
 
 import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnectionFactory;
 import javax.xml.soap.SOAPConstants;
 import javax.xml.soap.SOAPException;
 
 /**
  * Client for SOAP Version 1.2
  * 
  * @author Thiago Galbiatti Vespa
  * @version 1.2
  */
 public class Soap12Client extends SoapClient {
 
 	/**
 	 * Constructor with an endpoint
 	 * 
 	 * @param endpoint
 	 *            soap endpoint
 	 */
 	public Soap12Client(String endpoint) {
 		super(endpoint);
 	}
 
 	@Override
 	public MessageFactory getMessageFactory() throws SOAPException {
 		return MessageFactory
 				.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
 	}
 
 }
