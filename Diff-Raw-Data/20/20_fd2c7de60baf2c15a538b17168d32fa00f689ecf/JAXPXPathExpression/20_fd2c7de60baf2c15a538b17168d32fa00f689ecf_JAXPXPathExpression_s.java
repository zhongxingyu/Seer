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
 package org.apache.servicemix.expression;
 
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.springframework.beans.factory.InitializingBean;
 import org.xml.sax.SAXException;
 
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import javax.xml.xpath.XPathFunctionResolver;
 
 import java.io.IOException;
 
 /**
  * Evalutes an XPath expression on the given message using JAXP
  *
  * @version $Revision$
  */
 public class JAXPXPathExpression implements Expression, InitializingBean {
     private String xpath;
     private SourceTransformer transformer = new SourceTransformer();
     private MessageVariableResolver variableResolver = new MessageVariableResolver();
     private XPathExpression xPathExpression;
     private XPathFunctionResolver functionResolver;
     private NamespaceContext namespaceContext;
     private XPathFactory factory;
 
     public JAXPXPathExpression() {
     }
 
     /**
     * A helper constructor to make a fully created expression. This constructor will
     * call the {@link #afterPropertiesSet()} method to ensure this POJO is properly constructed.
      */
     public JAXPXPathExpression(String xpath) throws Exception {
         this.xpath = xpath;
        afterPropertiesSet();
     }
 
    public void afterPropertiesSet() throws Exception {
         if (xPathExpression == null) {
             if (xpath == null) {
                 throw new IllegalArgumentException("You must specify the xpath property");
             }
 
             if (factory == null) {
                 factory = XPathFactory.newInstance();
             }
             XPath xpathObject = factory.newXPath();
             xpathObject.setXPathVariableResolver(variableResolver);
             if (functionResolver != null) {
                 xpathObject.setXPathFunctionResolver(functionResolver);
             }
             if (namespaceContext != null) {
                 xpathObject.setNamespaceContext(namespaceContext);
             }
             xPathExpression = xpathObject.compile(xpath);
         }
     }
 
     public Object evaluate(MessageExchange exchange, NormalizedMessage message) throws MessagingException {
         try {
             Object object = getXMLNode(exchange, message);
             synchronized (this) {
                 variableResolver.setExchange(exchange);
                 variableResolver.setMessage(message);
                 return evaluateXPath(object);
             }
         }
         catch (TransformerException e) {
             throw new MessagingException(e);
         }
         catch (XPathExpressionException e) {
             throw new MessagingException(e);
         } 
         catch (ParserConfigurationException e) {
             throw new MessagingException(e);
         } 
         catch (IOException e) {
             throw new MessagingException(e);
         } 
         catch (SAXException e) {
             throw new MessagingException(e);
         }
     }
 
     // Properties
     //-------------------------------------------------------------------------
     public String getXPath() {
         return xpath;
     }
 
     public void setXPath(String xpath) {
         this.xpath = xpath;
     }
 
     public SourceTransformer getTransformer() {
         return transformer;
     }
 
     public void setTransformer(SourceTransformer transformer) {
         this.transformer = transformer;
     }
 
     public MessageVariableResolver getVariableResolver() {
         return variableResolver;
     }
 
     public void setVariableResolver(MessageVariableResolver variableResolver) {
         this.variableResolver = variableResolver;
     }
 
     public XPathFactory getFactory() {
         return factory;
     }
 
     public void setFactory(XPathFactory factory) {
         this.factory = factory;
     }
 
     public XPathFunctionResolver getFunctionResolver() {
         return functionResolver;
     }
 
     public void setFunctionResolver(XPathFunctionResolver functionResolver) {
         this.functionResolver = functionResolver;
     }
 
     public NamespaceContext getNamespaceContext() {
         return namespaceContext;
     }
 
     public void setNamespaceContext(NamespaceContext namespaceContext) {
         this.namespaceContext = namespaceContext;
     }
 
     // Implementation methods
     //-------------------------------------------------------------------------
     protected Object evaluateXPath(Object object) throws XPathExpressionException {
         return xPathExpression.evaluate(object);
     }
 
     protected XPathExpression getXPathExpression() {
         return xPathExpression;
     }
 
     protected Object getXMLNode(MessageExchange exchange, NormalizedMessage message) throws TransformerException, MessagingException, ParserConfigurationException, IOException, SAXException {
         return transformer.toDOMNode(message);
     }
 }
