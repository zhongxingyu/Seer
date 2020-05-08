 /**
  * Copyright 2009-2010 the original author or authors.
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
 package net.javacrumbs.springws.test.util;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import net.javacrumbs.springws.test.context.WsTestContextHolder;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.core.io.Resource;
 import org.springframework.ws.WebServiceMessage;
 import org.springframework.ws.soap.SoapMessage;
 import org.springframework.xml.namespace.SimpleNamespaceContext;
 import org.springframework.xml.transform.ResourceSource;
 import org.springframework.xml.transform.StringResult;
 import org.w3c.dom.Document;
 
 public class DefaultXmlUtil implements XmlUtil {
 
 	private static final Log logger = LogFactory.getLog(DefaultXmlUtil.class);
 	
 	private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
 	
	private static XmlUtil instance = new DefaultXmlUtil();
 	
 	private static final String SOAP11_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
 	private static final String SOAP12_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";
 
 	private static final SimpleNamespaceContext SOAP_NAMESPACE_CONTEXT = new SimpleNamespaceContext();
 	
 	static
 	{
 		Map<String, String> bindings = new HashMap<String, String>();
 		bindings.put("soap11", SOAP11_NAMESPACE);
 		bindings.put("soap12", SOAP12_NAMESPACE);
 		SOAP_NAMESPACE_CONTEXT.setBindings(bindings);
 	}
 	
 
 	public static final XmlUtil getInstance()
 	{
		return instance;
 	}
 	
	/**
	 * Sets default {@link XmlUtil} implementation.
	 * @param instance
	 */
	public static void setInstance(XmlUtil instance) {
		DefaultXmlUtil.instance = instance;
	}

 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.util.XmlUtil#loadDocument(javax.xml.transform.Source)
 	 */
 	public Document loadDocument(Source source) {
 		DOMResult messageContent = new DOMResult();
 		transform(source, messageContent);
 		return (Document) messageContent.getNode();
 	}
 
 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.util.XmlUtil#loadDocument(org.springframework.core.io.Resource)
 	 */
 	public Document loadDocument(Resource resource) throws IOException {
 		return loadDocument(new ResourceSource(resource));
 	}
 
 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.util.XmlUtil#transform(javax.xml.transform.Source, javax.xml.transform.Result)
 	 */
 	public void transform(Source source, Result result) {
 		try {
 			TRANSFORMER_FACTORY.newTransformer().transform(source, result);
 		} catch (TransformerException e) {
 			// FIXME throw better exception here.
 			throw new RuntimeException("Unexpected exception", e);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.util.XmlUtil#transform(javax.xml.transform.Source, javax.xml.transform.Source, javax.xml.transform.Result)
 	 */
 	public void transform(Source template, Source source, Result result) {
 		try {
 			Transformer transformer = TRANSFORMER_FACTORY.newTransformer(template);
 			setParameters(transformer);
 			transformer.transform(source, result);
 		} catch (TransformerException e) {
 			// FIXME throw better exception here.
 			throw new RuntimeException("Unexpected exception", e);
 		}
 	}
 
 	private void setParameters(Transformer transformer) {
 		for (Map.Entry<String, Object> entry : WsTestContextHolder.getTestContext().getAttributeMap().entrySet()) {
 			transformer.setParameter(entry.getKey(), entry.getValue());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.util.XmlUtil#loadDocument(org.springframework.ws.WebServiceMessage)
 	 */
 	public Document loadDocument(WebServiceMessage message) {
 		return loadDocument(getEnvelopeSource(message));
 	}
 
 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.util.XmlUtil#getEnvelopeSource(org.springframework.ws.WebServiceMessage)
 	 */
 	public Source getEnvelopeSource(WebServiceMessage message) {
 		if (message instanceof SoapMessage) {
 			SoapMessage soapMessage = (SoapMessage) message;
 			return soapMessage.getEnvelope().getSource();
 		} else {
 			throw new UnsupportedOperationException("Can not load message that is not SoapMessage");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.util.XmlUtil#serializeDocument(javax.xml.transform.Source)
 	 */
 	public String serializeDocument(Source source) {
 		StringResult result = new StringResult();
 		transform(source, result);
 		return result.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.util.XmlUtil#serializeDocument(org.springframework.ws.WebServiceMessage)
 	 */
 	public String serializeDocument(WebServiceMessage message) {
 		return serializeDocument(getEnvelopeSource(message));
 	}
 
 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.util.XmlUtil#serializeDocument(org.w3c.dom.Document)
 	 */
 	public String serializeDocument(Document document) {
 		return serializeDocument(new DOMSource(document));
 	}
 
 
 	@SuppressWarnings("unchecked")
 	public boolean isSoap(Document document) {
 		XPathFactory factory = XPathFactory.newInstance();
 		XPath xpath = factory.newXPath();
 		xpath.setNamespaceContext(SOAP_NAMESPACE_CONTEXT);
 		try {
 			for (Iterator iterator = SOAP_NAMESPACE_CONTEXT.getBoundPrefixes(); iterator.hasNext();) {
 				String prefix = (String) iterator.next();
 				if ((Boolean)xpath.evaluate("/"+prefix+":Envelope", document, XPathConstants.BOOLEAN))
 				{
 					return true;
 				}
 			}
 		} catch (XPathExpressionException e) {
 			logger.warn(e);
 		}
 		return false;
 	}
 }
