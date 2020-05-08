 /**
  * SOAP Security Framework
  *
  * Copyright (C) 2010 by Networld Project
  * Written by Alex Oberhauser <oberhauseralex@networld.to>
  * All Rights Reserved
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by 
  * the Free Software Foundation, version 3 of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of 
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this software.  If not, see <http://www.gnu.org/licenses/>
  */
 
 package to.networld.soap.security.security;
 
 import java.io.IOException;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateException;
 import java.util.Calendar;
 import java.util.UUID;
 
 import javax.xml.soap.MessageFactory;
 import javax.xml.soap.SOAPBody;
 import javax.xml.soap.SOAPElement;
 import javax.xml.soap.SOAPEnvelope;
 import javax.xml.soap.SOAPException;
 import javax.xml.soap.SOAPHeader;
 import javax.xml.soap.SOAPMessage;
 import javax.xml.soap.SOAPPart;
 
 import org.apache.ws.security.components.crypto.CredentialException;
 
 import to.networld.soap.security.common.DateHandler;
 import to.networld.soap.security.interfaces.ISecSOAPMessage;
 
 /**
  * @author Alex Oberhauser
  */
 public abstract class SOAPSecMessageFactory {
 	
 	/**
 	 * Creates a SOAP message with basic security constraints.
 	 * 
 	 * @param _expiresInMinutes The expire time in minutes from the current data. If 0 or negative than ignored.
 	 * @return The generated SOAP message.
 	 * @throws SOAPException
 	 * @throws InvalidAlgorithmParameterException 
 	 * @throws NoSuchAlgorithmException 
 	 * @throws IOException 
 	 * @throws CredentialException 
 	 * @throws CertificateException 
 	 * @throws KeyStoreException 
 	 */
 	public static ISecSOAPMessage newInstance(int _expiresInMinutes) throws SOAPException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, CredentialException, IOException, CertificateException, KeyStoreException {
		SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
 	    SOAPPart soapPart = soapMessage.getSOAPPart();
 	    SOAPBody soapBody = soapMessage.getSOAPBody();
 	    SOAPHeader soapHeader = soapMessage.getSOAPHeader();
 	    
 	    SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
         soapEnvelope.addNamespaceDeclaration("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
         soapEnvelope.addNamespaceDeclaration("SOAP-SEC", "http://schemas.xmlsoap.org/soap/security/2000-12");
         
         soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
         
         /*
          * Header Part
          */
         soapHeader.addNamespaceDeclaration("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
         SOAPElement secElement = soapHeader.addChildElement(soapHeader.createQName("Security", "wsse"));
         secElement.addAttribute(soapEnvelope.createQName("mustUnderstand", soapEnvelope.getPrefix()), "1");
               
         SOAPElement timestampElement = secElement.addChildElement(soapEnvelope.createQName("Timestamp", "wsu"));
         timestampElement.addAttribute(soapEnvelope.createQName("Id", "wsu"), UUID.randomUUID().toString());
         
        Calendar currentDate = Calendar.getInstance();
        timestampElement.addChildElement(soapEnvelope.createQName("Created", "wsu")).addTextNode(DateHandler.getDateString(currentDate, 0));
        if ( _expiresInMinutes > 0 )
        	timestampElement.addChildElement(soapEnvelope.createQName("Expires", "wsu")).addTextNode(DateHandler.getDateString(currentDate, _expiresInMinutes));
        timestampElement.addAttribute(soapEnvelope.createQName("id", "SOAP-SEC"), "Timestamp");
         
         /*
          * Body Part
          */
         soapBody.addAttribute(soapEnvelope.createQName("Id", "wsu"), UUID.randomUUID().toString());
         soapBody.addAttribute(soapEnvelope.createQName("id", "SOAP-SEC"), "Body");
         
         soapMessage.saveChanges();
 	    return new SecSOAPMessage(soapMessage);
 	}
 	
 }
