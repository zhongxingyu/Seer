 /*
  * Copyright 2004,2005 The Apache Software Foundation.
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
 
 package org.apache.ws.commons.soap.impl.llom.builder;
 
 import org.apache.ws.commons.om.OMAbstractFactory;
 import org.apache.ws.commons.om.OMDocument;
 import org.apache.ws.commons.om.OMElement;
 import org.apache.ws.commons.om.OMException;
 import org.apache.ws.commons.om.OMNamespace;
 import org.apache.ws.commons.om.OMNode;
 import org.apache.ws.commons.om.impl.OMNodeEx;
 import org.apache.ws.commons.om.impl.llom.builder.StAXOMBuilder;
 import org.apache.ws.commons.soap.SOAP11Constants;
 import org.apache.ws.commons.soap.SOAP12Constants;
 import org.apache.ws.commons.soap.SOAPBody;
 import org.apache.ws.commons.soap.SOAPConstants;
 import org.apache.ws.commons.soap.SOAPEnvelope;
 import org.apache.ws.commons.soap.SOAPFactory;
 import org.apache.ws.commons.soap.SOAPHeader;
 import org.apache.ws.commons.soap.SOAPMessage;
 import org.apache.ws.commons.soap.SOAPProcessingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
 import javax.xml.stream.XMLStreamReader;
 
 /**
  * Class StAXSOAPModelBuilder
  */
 public class StAXSOAPModelBuilder extends StAXOMBuilder {
 
     SOAPMessage soapMessage;
     /**
      * Field envelope
      */
     private SOAPEnvelope envelope;
     private OMNamespace envelopeNamespace;
 
 
     private SOAPFactory soapFactory;
 
     /**
      * Field headerPresent
      */
     private boolean headerPresent = false;
 
     /**
      * Field bodyPresent
      */
     private boolean bodyPresent = false;
 
     /**
      * Field log
      */
     private Log log = LogFactory.getLog(getClass());
 
     /**
      * element level 1 = envelope level element level 2 = Header or Body level
      * element level 3 = HeaderElement or BodyElement level
      */
     protected int elementLevel = 0;
 
     private boolean processingFault = false;
 
 
     //added
     /* This is used to indicate whether detail element is processing in soap 1.2 builderhelper
     */
     private boolean processingDetailElements = false;
 
     private SOAPBuilderHelper builderHelper;
     private String senderfaultCode;
     private String receiverfaultCode;
     private boolean processingMandatoryFaultElements;
     
     // We need to have soap factory, temporary, until we find out the correct SOAP version. If user has not provided
     // a SOAP factory, internally we are creating a default one. This flag will be set if we create one internally, to
     // warn that this should be replaced later.
     private boolean isTempSOAPFactory = true;
 
     /**
      * Constructor StAXSOAPModelBuilder
      * soapVersion parameter is to give the soap version from the transport. For example, in HTTP case
      * you can identify the version of the soap message u have recd by looking at the HTTP headers. 
      * It is used to check whether the actual soap message contained is of that version.
      * If one is creates the builder from the transport, then can just pass null for version.
      *
      * @param parser
      * @param soapVersion   parameter is to give the soap version for the transport.  
      */
     public StAXSOAPModelBuilder(XMLStreamReader parser, String soapVersion) {
         super(parser);
         soapFactory = OMAbstractFactory.getDefaultSOAPFactory();
         isTempSOAPFactory = true;
         soapMessage = soapFactory.createSOAPMessage(this);
         this.document = soapMessage;
         if(parser.getCharacterEncodingScheme() != null) {
             document.setCharsetEncoding(parser.getCharacterEncodingScheme());
         }
         identifySOAPVersion(soapVersion);
     }
 
     /**
      * @param parser
      * @param factory
      * @param soapVersion parameter is to give the soap version from the transport. For example, in
      *                    HTTP case you can identify the version of the soap message u have recd by looking at
      *                    the HTTP headers. It is used to check whether the actual soap message 
      *                    contained is of that version.If one is creates the builder from the transport, 
      *                    then can just pass null for version.
      */
     public StAXSOAPModelBuilder(XMLStreamReader parser, SOAPFactory factory, String soapVersion) {
         super(factory, parser);
         soapFactory = factory;
         isTempSOAPFactory = false;
         soapMessage = soapFactory.createSOAPMessage(this);
         this.document = soapMessage;
         identifySOAPVersion(soapVersion);
     }
 
     protected void identifySOAPVersion(String soapVersionURIFromTransport) {
 
         SOAPEnvelope soapEnvelope = getSOAPEnvelope();
         if (soapEnvelope == null) {
             throw new SOAPProcessingException("SOAP Message does not contain an Envelope",
                     SOAPConstants.FAULT_CODE_VERSION_MISMATCH);
         }
 
         envelopeNamespace = soapEnvelope.getNamespace();
         String namespaceName = envelopeNamespace.getName();
         if ((soapVersionURIFromTransport != null) && !(soapVersionURIFromTransport.equals(namespaceName))) {
             throw new SOAPProcessingException("Transport level information does not match with SOAP" +
                    " Message namespace URI", SOAPConstants.FAULT_CODE_VERSION_MISMATCH);
 
         }
         if(isTempSOAPFactory) {
 	        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceName)) {
 	            soapFactory = OMAbstractFactory.getSOAP12Factory();
 	            log.debug("Starting to process SOAP 1.2 message");
 	        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceName)) {
 	            soapFactory = OMAbstractFactory.getSOAP11Factory();
 	            log.debug("Starting to process SOAP 1.1 message");
 	
 	        } else {
 	            throw new SOAPProcessingException("Only SOAP 1.1 or SOAP 1.2 messages are supported in the" +
 	                    " system", SOAPConstants.FAULT_CODE_VERSION_MISMATCH);
 	        }
         }
     }
 
     /**
      * Method getSOAPEnvelope.
      *
      * @return Returns SOAPEnvelope.
      * @throws OMException
      */
     public SOAPEnvelope getSOAPEnvelope() throws OMException {
         while ((envelope == null) && !done) {
             next();
         }
         return envelope;
     }
 
     /**
      * Method createOMElement.
      *
      * @return Returns OMNode.
      * @throws OMException
      */
     protected OMNode createOMElement() throws OMException {
         elementLevel++;
         OMElement node;
         String elementName = parser.getLocalName();
         if (lastNode == null) {
             node = constructNode(null, elementName, true);
             setSOAPEnvelope(node);
         } else if (lastNode.isComplete()) {
             node =
                     constructNode((OMElement) lastNode.getParent(),
                             elementName,
                             false);
             ((OMNodeEx) lastNode).setNextOMSibling(node);
             ((OMNodeEx) node).setPreviousOMSibling(lastNode);
         } else {
             OMElement e = (OMElement) lastNode;
             node = constructNode((OMElement) lastNode, elementName, false);
             e.setFirstChild(node);
         }
 
 
         log.debug("Build the OMElelment " + node.getLocalName() +
                 "By the StaxSOAPModelBuilder");
         return node;
     }
 
     protected void setSOAPEnvelope(OMElement node) {
         soapMessage.setSOAPEnvelope((SOAPEnvelope) node);
         soapMessage.setXMLVersion(parser.getVersion());
         soapMessage.setCharsetEncoding(parser.getCharacterEncodingScheme());
     }
 
     /**
      * Method constructNode
      *
      * @param parent
      * @param elementName
      * @param isEnvelope
      */
     protected OMElement constructNode(OMElement parent, String elementName,
                                       boolean isEnvelope) {
         OMElement element = null;
         if (parent == null) {
             if (!elementName.equalsIgnoreCase(SOAPConstants.SOAPENVELOPE_LOCAL_NAME)) {
                 throw new SOAPProcessingException("First Element must contain the local name, "
                         + SOAPConstants.SOAPENVELOPE_LOCAL_NAME, SOAPConstants.FAULT_CODE_VERSION_MISMATCH);
             }
             envelope = soapFactory.createSOAPEnvelope(this);
             element = envelope;
             processNamespaceData(element, true);
             // fill in the attributes
             processAttributes(element);
 
         } else if (elementLevel == 2) {
 
             // this is either a header or a body
             if (elementName.equals(SOAPConstants.HEADER_LOCAL_NAME)) {
                 if (headerPresent) {
                     throw new SOAPProcessingException("Multiple headers encountered!", getSenderFaultCode());
                 }
                 if (bodyPresent) {
                     throw new SOAPProcessingException("Header Body wrong order!", getSenderFaultCode());
                 }
                 headerPresent = true;
                 element =
                         soapFactory.createSOAPHeader((SOAPEnvelope) parent,
                                 this);
 
                 processNamespaceData(element, true);
                 processAttributes(element);
 
             } else if (elementName.equals(SOAPConstants.BODY_LOCAL_NAME)) {
                 if (bodyPresent) {
                     throw new SOAPProcessingException("Multiple body elements encountered", getSenderFaultCode());
                 }
                 bodyPresent = true;
                 element =
                         soapFactory.createSOAPBody((SOAPEnvelope) parent,
                                 this);
 
                 processNamespaceData(element, true);
                 processAttributes(element);
 
             } else {
                 throw new SOAPProcessingException(elementName
                         +
                         " is not supported here. Envelope can not have elements other than Header and Body.", getSenderFaultCode());
             }
         } else if ((elementLevel == 3)
                 &&
                 parent.getLocalName().equalsIgnoreCase(SOAPConstants.HEADER_LOCAL_NAME)) {
 
             // this is a headerblock
             try {
                 element =
                         soapFactory.createSOAPHeaderBlock(elementName, null,
                                 (SOAPHeader) parent, this);
             } catch (SOAPProcessingException e) {
                 throw new SOAPProcessingException("Can not create SOAPHeader block", getReceiverFaultCode(), e);
             }
             processNamespaceData(element, false);
             processAttributes(element);
 
         } else if ((elementLevel == 3) &&
                 parent.getLocalName().equalsIgnoreCase(SOAPConstants.BODY_LOCAL_NAME) &&
                 elementName.equalsIgnoreCase(SOAPConstants.BODY_FAULT_LOCAL_NAME)) {
 
             // this is a headerblock
             element = soapFactory.createSOAPFault((SOAPBody) parent, this);
             processNamespaceData(element, false);
             processAttributes(element);
 
 
             processingFault = true;
             processingMandatoryFaultElements = true;
             if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(envelopeNamespace.getName())) {
                 builderHelper = new SOAP12BuilderHelper(this);
             } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(envelopeNamespace.getName())) {
                 builderHelper = new SOAP11BuilderHelper(this);
             }
 
         } else if (elementLevel > 3 && processingFault) {
             element = builderHelper.handleEvent(parser, parent, elementLevel);
         } else {
 
             // this is neither of above. Just create an element
             element = soapFactory.createOMElement(elementName, null,
                     parent, this);
             processNamespaceData(element, false);
             processAttributes(element);
 
         }
         return element;
     }
 
     private String getSenderFaultCode() {
         if (senderfaultCode == null) {
             senderfaultCode = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(envelopeNamespace.getName()) ? SOAP12Constants.FAULT_CODE_SENDER : SOAP11Constants.FAULT_CODE_SENDER;
         }
         return senderfaultCode;
     }
 
     private String getReceiverFaultCode() {
         if (receiverfaultCode == null) {
             receiverfaultCode = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(envelopeNamespace.getName()) ? SOAP12Constants.FAULT_CODE_RECEIVER : SOAP11Constants.FAULT_CODE_RECEIVER;
         }
         return receiverfaultCode;
     }
 
     public void endElement() {
         if (lastNode.isComplete()) {
             OMElement parent = (OMElement) lastNode.getParent();
             ((OMNodeEx) parent).setComplete(true);
             lastNode = parent;
         } else {
             OMNode e = lastNode;
             ((OMNodeEx) e).setComplete(true);
         }
         elementLevel--;
     }
 
     /**
      * Method createDTD.
      * Overriding the default behaviour as a SOAPMessage should not have a DTD.
      */
     protected OMNode createDTD() throws OMException {
         throw new OMException("SOAP message MUST NOT contain a Document Type Declaration(DTD)");
     }
 
     /**
      * Method createPI.
      * Overriding the default behaviour as a SOAP Message should not have a PI.
      */
     protected OMNode createPI() throws OMException {
         throw new OMException("SOAP message MUST NOT contain Processing Instructions(PI)");
     }
 
     /**
      * Method getDocumentElement.
      *
      * @return Returns OMElement.
      */
     public OMElement getDocumentElement() {
         return getSOAPEnvelope();
     }
 
     /**
      * Method processNamespaceData.
      *
      * @param node
      * @param isSOAPElement
      */
     protected void processNamespaceData(OMElement node, boolean isSOAPElement) {
 
         super.processNamespaceData(node, isSOAPElement);
 
         if (isSOAPElement) {
             if (node.getNamespace() != null &&
                     !node.getNamespace().getName().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI) &&
                     !node.getNamespace().getName().equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                 throw new SOAPProcessingException("invalid SOAP namespace URI. " +
                         "Only " + SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI +
                         " and " + SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI +
                         " are supported.", SOAP12Constants.FAULT_CODE_SENDER);
             }
         }
 
     }
 
 /*these three methods to set and check detail element processing or mandatory fault element are present
 */
 
     public OMNamespace getEnvelopeNamespace() {
         return envelopeNamespace;
     }
 
     public void setBooleanProcessingMandatoryFaultElements(boolean value) {
         this.processingMandatoryFaultElements = value;
     }
 
     public boolean isProcessingDetailElements() {
         return processingDetailElements;
     }
 
     public void setProcessingDetailElements(boolean value) {
         processingDetailElements = value;
     }
 
     public SOAPMessage getSoapMessage() {
         return soapMessage;
     }
 
     public OMDocument getDocument() {
         return (OMDocument) this.soapMessage;
     }
 
 	/**
 	 * @return Returns the soapFactory.
 	 */
 	protected SOAPFactory getSoapFactory() {
 		return soapFactory;
 	}
 
 }
