 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.components.saaj;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 import javax.activation.DataHandler;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.xml.XMLConstants;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.soap.AttachmentPart;
 import javax.xml.soap.MessageFactory;
 import javax.xml.soap.MimeHeader;
 import javax.xml.soap.SOAPBody;
 import javax.xml.soap.SOAPElement;
 import javax.xml.soap.SOAPEnvelope;
 import javax.xml.soap.SOAPException;
 import javax.xml.soap.SOAPMessage;
 import javax.xml.soap.SOAPPart;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.dom.DOMSource;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Iterator;
 
 /**
  * @version $Revision$
  */
 public class SaajMarshaler {
 
     private static final transient Log log = LogFactory.getLog(SaajMarshaler.class);
 
     protected SourceTransformer transformer = new SourceTransformer();
     private MessageFactory messageFactory;
 
     public void toNMS(NormalizedMessage normalizedMessage, SOAPMessage soapMessage) throws MessagingException, SOAPException {
 
         if (log.isDebugEnabled()) {
         	try {
 	            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 	            soapMessage.writeTo(buffer);
 	            log.debug(new String(buffer.toByteArray()));
         	} catch (Exception e) { }
         }
         
     	addNmsProperties(normalizedMessage, soapMessage);
 
         SOAPPart soapPart = soapMessage.getSOAPPart();
         SOAPBody soapBody = soapPart.getEnvelope().getBody();
         SOAPElement elem = null;
         for (Iterator it = soapBody.getChildElements(); it.hasNext();) {
         	Object child =  it.next();
         	if (child instanceof SOAPElement) {
         		elem = (SOAPElement) child;
         		break;
         	}
         }
         if (elem == null) {
         	throw new IllegalStateException("Could not find any element in soap body");
         }
         
         for (SOAPElement parent = elem.getParentElement(); parent != null; parent = parent.getParentElement()) {
         	// The following code works with sun saaj implementation
 		    NamedNodeMap attributes = parent.getAttributes();
 		    if (attributes != null) {
 		        for (int i = 0; i < attributes.getLength(); i++) {
 		            Attr att = (Attr) parent.getAttributes().item(i);
 		            if (att.getName().startsWith(XMLConstants.XMLNS_ATTRIBUTE + ":")
 		                    && elem.getAttributeNodeNS(att.getNamespaceURI(), att.getLocalName()) == null) {
 		        		elem.addNamespaceDeclaration(att.getName().substring(XMLConstants.XMLNS_ATTRIBUTE.length() + 1), att.getValue());
 		                elem.setAttributeNS(att.getNamespaceURI(), att.getName(), att.getValue());
 		            }
 		        }
 		    }
 		    // The following code works with axis saaj implementation
         	for (Iterator itNs = parent.getNamespacePrefixes(); itNs.hasNext();) {
         		String prefix = (String) itNs.next();
         		String nsuri = parent.getNamespaceURI(prefix);
         		if (elem.getAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix) == null) {
 	        		elem.addNamespaceDeclaration(prefix, nsuri);
 	        		elem.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix, nsuri);
         		}
         	}
 		}
         
         if (log.isDebugEnabled()) {
         	try {
         		log.debug(transformer.toString(elem));
         	} catch (Exception e) { }
         }
         
         normalizedMessage.setContent(new DOMSource(elem));
 
         addNmsAttachments(normalizedMessage, soapMessage);
     }
 
     public SOAPMessage createSOAPMessage(NormalizedMessage normalizedMessage) throws SOAPException, IOException, TransformerException, MessagingException, ParserConfigurationException, SAXException {
         SOAPMessage soapMessage = getMessageFactory().createMessage();
 
         addSoapProperties(soapMessage, normalizedMessage);
 
         SOAPPart soapPart = soapMessage.getSOAPPart();
         SOAPEnvelope envelope = soapPart.getEnvelope();
         SOAPBody body = envelope.getBody();
 
         // lets turn the payload into a DOM Node to avoid blatting over the envelope
         // Do not use DOMResult to transform as namespaces are lost (why ?)
         //DOMResult result = new DOMResult(null);
         //transformer.toResult(normalizedMessage.getContent(), result);
         //Document document = (Document) result.getNode();
         Node node = transformer.toDOMNode(normalizedMessage);
         Document document = null;
         if (node instanceof Document) {
         	document = (Document) node;
         } else if (node instanceof Element) {
         	document = transformer.createDocument();
        	document.appendChild(node.cloneNode(true));
         } else {
         	throw new TransformerException("Could not create Document from Source");
         }
         
         body.addDocument(document);
 
         addSoapAttachments(soapMessage, normalizedMessage);
 
         if (log.isDebugEnabled()) {
             ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             soapMessage.writeTo(buffer);
             log.debug(new String(buffer.toByteArray()));
         }
         return soapMessage;
     }
 
     // Properties
     //-------------------------------------------------------------------------
     public MessageFactory getMessageFactory() throws SOAPException {
         if (messageFactory == null) {
             messageFactory = createMessageFactory();
         }
         return messageFactory;
     }
 
     public void setMessageFactory(MessageFactory messageFactory) {
         this.messageFactory = messageFactory;
     }
 
     // Implementation methods
     //-------------------------------------------------------------------------
 
     protected void addNmsProperties(NormalizedMessage normalizedMessage, SOAPMessage soapMessage) {
         Iterator iter = soapMessage.getMimeHeaders().getAllHeaders();
         while (iter.hasNext()) {
             MimeHeader header = (MimeHeader) iter.next();
             normalizedMessage.setProperty(header.getName(), header.getValue());
         }
     }
 
     protected void addNmsAttachments(NormalizedMessage normalizedMessage, SOAPMessage soapMessage) throws MessagingException, SOAPException {
         Iterator iter = soapMessage.getAttachments();
         while (iter.hasNext()) {
             AttachmentPart attachment = (AttachmentPart) iter.next();
             normalizedMessage.addAttachment(attachment.getContentId(), asDataHandler(attachment));
         }
     }
 
     protected void addSoapProperties(SOAPMessage soapMessage, NormalizedMessage normalizedMessage) throws SOAPException {
         for (Iterator iter = normalizedMessage.getPropertyNames().iterator(); iter.hasNext();) {
             String name = (String) iter.next();
             Object value = normalizedMessage.getProperty(name);
             if (shouldIncludeHeader(normalizedMessage, name, value)) {
                 soapMessage.getMimeHeaders().addHeader(name, value.toString());
             }
             if (shouldIncludeProperty(normalizedMessage, name, value)) {
                 soapMessage.setProperty(name, value);
             }
         }
     }
 
     protected void addSoapAttachments(SOAPMessage soapMessage, NormalizedMessage normalizedMessage) throws IOException {
         Iterator iterator = normalizedMessage.getAttachmentNames().iterator();
         while (iterator.hasNext()) {
             String name = (String) iterator.next();
             DataHandler attachment = normalizedMessage.getAttachment(name);
             AttachmentPart attachmentPart = soapMessage.createAttachmentPart(attachment.getContent(), attachment.getContentType());
             attachmentPart.setContentId(name);
             soapMessage.addAttachmentPart(attachmentPart);
         }
     }
 
     /**
      * Decides whether or not the given header should be included in the SAAJ message as a MimeHeader
      */
     protected boolean shouldIncludeHeader(NormalizedMessage normalizedMessage, String name, Object value) {
         return true;
     }
 
     /**
      * Decides whether or not the given property should be included in the SAAJ message as a property
      */
     private boolean shouldIncludeProperty(NormalizedMessage normalizedMessage, String name, Object value) {
         return true;
     }
 
     protected DataHandler asDataHandler(AttachmentPart attachment) throws SOAPException {
         return new DataHandler(attachment.getContent(), attachment.getContentType());
     }
 
 
     protected MessageFactory createMessageFactory() throws SOAPException {
         return MessageFactory.newInstance();
     }
 }
