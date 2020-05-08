 /*
  * Copyright The Apache Software Foundation.
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
 package org.apache.rahas.test.util;
 
 import junit.framework.Assert;
 import org.apache.axiom.om.*;
 import org.apache.axiom.om.impl.builder.StAXBuilder;
 import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
 import org.apache.axiom.om.util.StAXUtils;
 import org.apache.axiom.soap.*;
 import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
 import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
 import org.apache.axis2.addressing.AddressingConstants;
 import org.apache.axis2.context.ConfigurationContext;
 import org.apache.axis2.context.ConfigurationContextFactory;
 import org.apache.axis2.context.MessageContext;
 import org.apache.axis2.description.AxisService;
 import org.apache.axis2.engine.AxisConfiguration;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.rahas.RahasConstants;
 import org.apache.rahas.RahasData;
 import org.apache.rahas.TrustException;
 import org.apache.rahas.TrustUtil;
 import org.apache.rahas.impl.util.CommonUtil;
 import org.apache.ws.secpolicy.Constants;
 import org.apache.ws.security.WSConstants;
 import org.apache.ws.security.WSSecurityEngineResult;
 import org.apache.ws.security.WSSecurityException;
 import org.apache.ws.security.components.crypto.Crypto;
 import org.apache.ws.security.components.crypto.CryptoFactory;
 import org.apache.ws.security.components.crypto.CryptoType;
 import org.apache.ws.security.handler.WSHandlerConstants;
 import org.apache.ws.security.handler.WSHandlerResult;
 import org.apache.ws.security.saml.ext.builder.SAML1Constants;
 import org.apache.xml.security.utils.XMLUtils;
 import org.opensaml.common.xml.SAMLConstants;
 import org.w3c.dom.DOMConfiguration;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLStreamReader;
 import java.io.*;
 import java.security.Principal;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * Utility class for tests.
  */
 public class TestUtil {
 
     private static final Log log = LogFactory.getLog(TestUtil.class);
 
     // Directly copied from WSS4J
     public static final String SAMPLE_SOAP_MSG =
         "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
         + "<SOAP-ENV:Envelope "
         +   "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
         +   "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
         +   "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
         +    "<SOAP-ENV:Header"
         +       " xmlns:wsse=\"http://schemas.xmlsoap.org/ws/2002/07/secext\""
         +       " xmlns:wsu=\"http://schemas.xmlsoap.org/ws/2002/07/utility\"/>"
         +   "<SOAP-ENV:Body>"
         +       "<add xmlns=\"http://ws.apache.org/counter/counter_port_type\">"
         +           "<value xmlns=\"\">15</value>"
         +       "</add>"
         +   "</SOAP-ENV:Body>"
         + "</SOAP-ENV:Envelope>";
 
     /**
      * Convert an SOAP Envelope as a String to a org.w3c.dom.Document.
      * Directly copied from WSS4J
      */
     public static org.w3c.dom.Document toSOAPPart(String xml) throws Exception {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
 
         InputStream in = new ByteArrayInputStream(xml.getBytes());
         DocumentBuilder builder = factory.newDocumentBuilder();
         return builder.parse(in);
     }
 
     public static Crypto getCrypto() throws IOException, WSSecurityException, TrustException {
 
         File file = new File("src/test/resources/crypto.config");
         Assert.assertTrue(file.exists());
 
         Properties properties = new Properties();
         try {
             properties.load(new FileInputStream(file));
         } catch (IOException e) {
             log.error("Unable to open crypto configuration file");
             throw e;
         }
 
         Crypto crypto = CryptoFactory.getInstance(properties);
 
         X509Certificate[] certificates = CommonUtil.getCertificatesByAlias(crypto, "apache");
         Assert.assertEquals(certificates.length, 1);
 
         return crypto;
 
     }
 
     public static X509Certificate getDefaultCertificate() throws WSSecurityException, TrustException, IOException {
         Crypto crypto = getCrypto();
 
         CryptoType cryptoType = new CryptoType(CryptoType.TYPE.ALIAS);
         cryptoType.setAlias("apache");
 
         return crypto.getX509Certificates(cryptoType)[0];
     }
 
     public static Document getTestDocument() throws Exception {
 
         InputStream is = new ByteArrayInputStream(SAMPLE_SOAP_MSG.getBytes());
         SOAPEnvelope envelope = createSOAPEnvelope(is);
 
         return TestUtil.getDocumentFromSOAPEnvelope(envelope, true);
     }
 
     public static SOAPEnvelope createSOAPEnvelope(InputStream in) throws Exception {
         XMLStreamReader xmlreader =
                 StAXUtils.createXMLStreamReader(in);
         StAXBuilder builder = new StAXSOAPModelBuilder(xmlreader, null);
         return (SOAPEnvelope) builder.getDocumentElement();
     }
 
     public static OMElement getRSTTemplate(String samlNamespace) throws Exception {
         OMFactory fac = OMAbstractFactory.getOMFactory();
         OMElement element = null;
         OMElement elem = fac.createOMElement(Constants.RST_TEMPLATE);
         TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_02, elem).setText(samlNamespace);
         TrustUtil.createKeyTypeElement(RahasConstants.VERSION_05_02, elem,
                 RahasConstants.KEY_TYPE_SYMM_KEY);
         TrustUtil.createKeySizeElement(RahasConstants.VERSION_05_02, elem, 256);
         element = TrustUtil.createClaims(RahasConstants.VERSION_05_02, elem, "http://wso2.org");
         addClaimType(element, "http://wso2.org/claims/givenname");
         return elem;
     }
 
     private static void addClaimType(OMElement parent, String uri) {
         OMElement element = null;
         element = parent.getOMFactory().createOMElement(new QName("http://schemas.xmlsoap.org/ws/2005/05/identity", "ClaimType", "wsid"),
                 parent);
         element.addAttribute(parent.getOMFactory().createOMAttribute("Uri", null, uri));
     }
 
     public static TestSTSClient createTestSTSClient(String samlVersion) throws Exception {
 
         ConfigurationContext configurationContext
                 = ConfigurationContextFactory.createConfigurationContextFromFileSystem("src/test/resources/repo",
                 "src/test/resources/repo/conf/client.axis2.xml");
 
         TestSTSClient stsClient = new TestSTSClient(configurationContext);
 
         stsClient.setRstTemplate(getRSTTemplate(samlVersion));
         stsClient.setAction(RahasConstants.WST_NS_05_02 + RahasConstants.RST_ACTION_SCT);
 
         return stsClient;
 
     }
 
     public static MessageContext createDummyMessageContext(String appliesTo) throws Exception {
         TestSTSClient stsClient = TestUtil.createTestSTSClient(SAMLConstants.SAML20_NS);
         OMElement requestSecurityToken = stsClient.createRST(appliesTo);
 
         MessageContext dummyMessageContext = new MessageContext();
 
         populateReceivedResults(dummyMessageContext);
 
         dummyMessageContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                 AddressingConstants.Submission.WSA_NAMESPACE);
 
         SOAP11Factory factory = new SOAP11Factory();
         SOAPEnvelope envelope = factory.createSOAPEnvelope();
 
         SOAPBody soapBody = factory.createSOAPBody(envelope);
         soapBody.addChild(requestSecurityToken);
 
         dummyMessageContext.setEnvelope(envelope);
 
         dummyMessageContext.setAxisService(new AxisService("TestService"));
 
 
         AxisConfiguration axisConfiguration = new AxisConfiguration();
         dummyMessageContext.setConfigurationContext(new ConfigurationContext(axisConfiguration));
 
         return dummyMessageContext;
     }
 
     public static RahasData createTestRahasData(String appliesTo) throws Exception {
         return new RahasData(createDummyMessageContext(appliesTo));
     }
 
     private static void populateReceivedResults(MessageContext messageContext) throws Exception {
         List<WSSecurityEngineResult> wsSecEngineResults = new ArrayList<WSSecurityEngineResult>();
         WSSecurityEngineResult result = new WSSecurityEngineResult(WSConstants.SIGN);
 
         Principal principal = new Principal() {
             public String getName() {
                 return "apache";
             }
         };
 
         result.put(WSSecurityEngineResult.TAG_PRINCIPAL, principal);
         result.put(WSSecurityEngineResult.TAG_X509_CERTIFICATE, getDefaultCertificate());
 
         wsSecEngineResults.add(result);
 
         WSHandlerResult handlerResult = new WSHandlerResult(null, wsSecEngineResults);
 
         List<WSHandlerResult> handlerResultList = new ArrayList<WSHandlerResult>();
         handlerResultList.add(handlerResult);
 
         messageContext.setProperty(WSHandlerConstants.RECV_RESULTS, handlerResultList);
 
     }
 
     /**
      * This is directly taken from rampart-core.
      * TODO we need to move these common code to a new module. Otherwise code will be duplicated.
     * We cannot use following method from rampart-core as it creates a cyclic dependency. Therefore we have
     * to live with following.
      * @param doc The document to convert.
      * @param useDoom Whether to use doom or not.
      * @return A SOAPEnvelope.
      * @throws WSSecurityException If an error occurred during conversion.
      */
     public static SOAPEnvelope getSOAPEnvelopeFromDOMDocument(Document doc, boolean useDoom)
             throws WSSecurityException {
 
         if(useDoom) {
             try {
                 //Get processed headers
                 SOAPEnvelope env = (SOAPEnvelope)doc.getDocumentElement();
                 ArrayList processedHeaderQNames = new ArrayList();
                 SOAPHeader soapHeader = env.getHeader();
 
                 if(soapHeader != null) {
                     Iterator headerBlocs = soapHeader.getChildElements();
                     while (headerBlocs.hasNext()) {
 
                     	OMElement element = (OMElement)headerBlocs.next();
                     	SOAPHeaderBlock header = null;
 
                     	if (element instanceof SOAPHeaderBlock) {
                             header = (SOAPHeaderBlock) element;
 
                         // If a header block is not an instance of SOAPHeaderBlock, it means that
                         // it is a header we have added in rampart eg. EncryptedHeader and should
                         // be converted to SOAPHeaderBlock for processing
                     	} else {
                     		header = soapHeader.addHeaderBlock(element.getLocalName(), element.getNamespace());
                     		Iterator attrIter = element.getAllAttributes();
                     		while (attrIter.hasNext()) {
                     			OMAttribute attr = (OMAttribute)attrIter.next();
                     			header.addAttribute(attr.getLocalName(), attr.getAttributeValue(), attr.getNamespace());
                     		}
                     		Iterator nsIter  = element.getAllDeclaredNamespaces();
                     		while (nsIter.hasNext()) {
                     			OMNamespace ns =  (OMNamespace) nsIter.next();
                     			header.declareNamespace(ns);
                     		}
                     		// retrieve all child nodes (including any text nodes)
                     		// and re-attach to header block
                     		Iterator children = element.getChildren();
                     		while (children.hasNext()) {
                     			OMNode child = (OMNode)children.next();
                     			children.remove();
                     			header.addChild(child);
                     		}
 
                     		element.detach();
 
                     		soapHeader.build();
 
                     		header.setProcessed();
 
                     	}
 
                         if(header.isProcessed()) {
                             processedHeaderQNames.add(element.getQName());
                         }
                     }
 
                 }
                 XMLStreamReader reader = ((OMElement) doc.getDocumentElement())
                         .getXMLStreamReader();
                 SOAPModelBuilder stAXSOAPModelBuilder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(
                         reader);
                 SOAPEnvelope envelope = stAXSOAPModelBuilder.getSOAPEnvelope();
 
                 //Set the processed flag of the processed headers
                 SOAPHeader header = envelope.getHeader();
                 for (Iterator iter = processedHeaderQNames.iterator(); iter
                         .hasNext();) {
                     QName name = (QName) iter.next();
                     Iterator omKids = header.getChildrenWithName(name);
                     if(omKids.hasNext()) {
                         ((SOAPHeaderBlock)omKids.next()).setProcessed();
                     }
                 }
 
                 envelope.build();
 
                 return envelope;
 
             } catch (FactoryConfigurationError e) {
                 throw new WSSecurityException(e.getMessage());
             }
         } else {
             try {
                 ByteArrayOutputStream os = new ByteArrayOutputStream();
                 XMLUtils.outputDOM(doc.getDocumentElement(), os, true);
                 ByteArrayInputStream bais =  new ByteArrayInputStream(os.toByteArray());
 
                 SOAPModelBuilder stAXSOAPModelBuilder = OMXMLBuilderFactory.createSOAPModelBuilder(bais, null);
                 return stAXSOAPModelBuilder.getSOAPEnvelope();
             } catch (Exception e) {
                 throw new WSSecurityException(e.getMessage());
             }
         }
     }
 
     /**
      * TODO we need to move these common code to a new module. Otherwise code will be duplicated.
     * We cannot use following method from rampart-core as it creates a cyclic dependency. Therefore we have
     * to live with following.
 	 * Creates a DOM Document using the SOAP Envelope.
 	 * @param env An org.apache.axiom.soap.SOAPEnvelope instance
 	 * @return Returns the DOM Document of the given SOAP Envelope.
 	 * @throws Exception If an error occurred during conversion.
 	 */
 	public static Document getDocumentFromSOAPEnvelope(SOAPEnvelope env, boolean useDoom)
 			throws WSSecurityException {
 		try {
             if(env instanceof Element) {
                 Element element = (Element)env;
                 Document document = element.getOwnerDocument();
                 // For outgoing messages, Axis2 only creates the SOAPEnvelope, but no document. If
                 // the Axiom implementation also supports DOM, then the envelope (seen as a DOM
                 // element) will have an owner document, but the document and the envelope have no
                 // parent-child relationship. On the other hand, the input expected by WSS4J is
                 // a document with the envelope as document element. Therefore we need to set the
                 // envelope as document element on the owner document.
                 if (element.getParentNode() != document) {
                     document.appendChild(element);
                 }
                 // If the Axiom implementation supports DOM, then it is possible/likely that the
                 // DOM API was used to create the object model (or parts of it). In this case, the
                 // object model is not necessarily well formed with respect to namespaces because
                 // DOM doesn't generate namespace declarations automatically. This is an issue
                 // because WSS4J/Santuario expects that all namespace declarations are present.
                 // If this is not the case, then signature values or encryptions will be incorrect.
                 // To avoid this, we normalize the document. Note that if we disable the other
                 // normalizations supported by DOM, this is generally not a heavy operation.
                 // In particular, the Axiom implementation is not required to expand the object
                 // model (including OMSourcedElements) because the Axiom builder is required to
                 // perform namespace repairing, so that no modifications to unexpanded parts of
                 // the message are required.
                 DOMConfiguration domConfig = document.getDomConfig();
                 domConfig.setParameter("split-cdata-sections", Boolean.FALSE);
                 domConfig.setParameter("well-formed", Boolean.FALSE);
                 domConfig.setParameter("namespaces", Boolean.TRUE);
                 document.normalizeDocument();
                 return document;
             }
 
             if (useDoom) {
                 env.build();
 
                 // Workaround to prevent a bug in AXIOM where
                 // there can be an incomplete OMElement as the first child body
                 OMElement firstElement = env.getBody().getFirstElement();
                 if (firstElement != null) {
                     firstElement.build();
                 }
 
                 //Get processed headers
                 SOAPHeader soapHeader = env.getHeader();
                 ArrayList processedHeaderQNames = new ArrayList();
                 if(soapHeader != null) {
                     Iterator headerBlocs = soapHeader.getChildElements();
                     while (headerBlocs.hasNext()) {
                         SOAPHeaderBlock element = (SOAPHeaderBlock) headerBlocs.next();
                         if(element.isProcessed()) {
                             processedHeaderQNames.add(element.getQName());
                         }
                     }
                 }
 
                 // Check the namespace and find SOAP version and factory
                 String nsURI = null;
                 SOAPFactory factory;
                 if (env.getNamespace().getNamespaceURI().equals(
                         SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                     nsURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
                     factory = DOOMAbstractFactory.getSOAP11Factory();
                 } else {
                     nsURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
                     factory = DOOMAbstractFactory.getSOAP12Factory();
                 }
 
                 StAXSOAPModelBuilder stAXSOAPModelBuilder = new StAXSOAPModelBuilder(
                         env.getXMLStreamReader(), factory, nsURI);
                 SOAPEnvelope envelope = (stAXSOAPModelBuilder)
                         .getSOAPEnvelope();
                 envelope.getParent().build();
 
                 //Set the processed flag of the processed headers
                 SOAPHeader header = envelope.getHeader();
                 for (Iterator iter = processedHeaderQNames.iterator(); iter
                         .hasNext();) {
                     QName name = (QName) iter.next();
                     Iterator omKids = header.getChildrenWithName(name);
                     if(omKids.hasNext()) {
                         ((SOAPHeaderBlock)omKids.next()).setProcessed();
                     }
                 }
 
                 Element envElem = (Element) envelope;
                 return envElem.getOwnerDocument();
             } else {
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 env.build();
                 env.serialize(baos);
                 ByteArrayInputStream bais = new ByteArrayInputStream(baos
                         .toByteArray());
                 DocumentBuilderFactory factory = DocumentBuilderFactory
                         .newInstance();
                 factory.setNamespaceAware(true);
                 return factory.newDocumentBuilder().parse(bais);
             }
 		} catch (Exception e) {
 			throw new WSSecurityException(
 					"Error in converting SOAP Envelope to Document", e);
 		}
 	}
 }
