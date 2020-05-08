 /*
  * Copyright 2001-2004 The Apache Software Foundation.
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
 
 package org.apache.ws.policy.util;
 
 import java.io.OutputStream;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.ws.policy.AndCompositeAssertion;
 import org.apache.ws.policy.Assertion;
 import org.apache.ws.policy.Policy;
 import org.apache.ws.policy.PolicyConstants;
 import org.apache.ws.policy.PolicyReference;
 import org.apache.ws.policy.PrimitiveAssertion;
 import org.apache.ws.policy.XorCompositeAssertion;
 
 /**
  * @author Sanka Samaranayake (sanka@apache.org)
  */
 public class StAXPolicyWriter implements PolicyWriter {
 
     private int num = 1;
 
     StAXPolicyWriter() {
     }
 
     public void writePolicy(Policy policy, OutputStream output) {
         XMLStreamWriter writer = null;
         try {
             writer = XMLOutputFactory.newInstance().createXMLStreamWriter(
                     output);
             writePolicy(policy, writer);
 
             writer.flush();
 
         } catch (XMLStreamException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     public void writePolicy(Policy policy, XMLStreamWriter writer)
             throws XMLStreamException {
         String writerPerfix = writer
                 .getPrefix(PolicyConstants.WS_POLICY_NAMESPACE_URI);
 
         if (writerPerfix != null) {
             writer.writeStartElement(PolicyConstants.WS_POLICY_NAMESPACE_URI,
                     PolicyConstants.WS_POLICY);
 
         } else {
             writer.writeStartElement(PolicyConstants.WS_POLICY_PREFIX,
                     PolicyConstants.WS_POLICY,
                     PolicyConstants.WS_POLICY_NAMESPACE_URI);
             writer.writeNamespace(PolicyConstants.WS_POLICY_PREFIX,
                     PolicyConstants.WS_POLICY_NAMESPACE_URI);
             writer.setPrefix(PolicyConstants.WS_POLICY_PREFIX,
                     PolicyConstants.WS_POLICY_NAMESPACE_URI);
 
         }
 
         if (policy.getId() != null) {
        	
        	
        	writer.writeNamespace(PolicyConstants.WSU_NAMESPACE_PREFIX,
                    PolicyConstants.WSU_NAMESPACE_URI);
            writer.setPrefix(PolicyConstants.WSU_NAMESPACE_PREFIX,
                    PolicyConstants.WSU_NAMESPACE_URI);
        	
             writer.writeAttribute("wsu", PolicyConstants.WSU_NAMESPACE_URI,
                     "Id", policy.getId());
         }
 
         Iterator iterator = policy.getTerms().iterator();
         while (iterator.hasNext()) {
             Assertion term = (Assertion) iterator.next();
             writeAssertion(term, writer);
         }
 
         writer.writeEndElement();
     }
 
     private void writeAssertion(Assertion assertion, XMLStreamWriter writer)
             throws XMLStreamException {
         if (assertion instanceof PrimitiveAssertion) {
             writePrimitiveAssertion((PrimitiveAssertion) assertion, writer);
 
         } else if (assertion instanceof XorCompositeAssertion) {
             writeXorCompositeAssertion((XorCompositeAssertion) assertion,
                     writer);
 
         } else if (assertion instanceof PolicyReference) {
             writePolicyReference((PolicyReference) assertion, writer);
 
         } else if (assertion instanceof Policy) {
             writePolicy((Policy) assertion, writer);
         } else if (assertion instanceof AndCompositeAssertion) {
             writeAndCompositeAssertion((AndCompositeAssertion) assertion,
                     writer);
 
         } else {
             throw new RuntimeException("unknown element type");
         }
     }
 
     private void writeAndCompositeAssertion(AndCompositeAssertion assertion,
             XMLStreamWriter writer) throws XMLStreamException {
 
         String writerPrefix = writer
                 .getPrefix(PolicyConstants.WS_POLICY_NAMESPACE_URI);
 
         if (writerPrefix == null) {
             writer.writeStartElement(PolicyConstants.WS_POLICY_PREFIX,
                     PolicyConstants.AND_COMPOSITE_ASSERTION,
                     PolicyConstants.WS_POLICY_NAMESPACE_URI);
             writer.writeNamespace(PolicyConstants.WS_POLICY_PREFIX,
                     PolicyConstants.WS_POLICY_NAMESPACE_URI);
             writer.setPrefix(PolicyConstants.WS_POLICY_PREFIX,
                     PolicyConstants.WS_POLICY_NAMESPACE_URI);
 
         } else {
             writer.writeStartElement(PolicyConstants.WS_POLICY_NAMESPACE_URI,
                     PolicyConstants.AND_COMPOSITE_ASSERTION);
         }
 
         List terms = assertion.getTerms();
         writeTerms(terms, writer);
 
         writer.writeEndElement();
     }
 
     private void writeXorCompositeAssertion(XorCompositeAssertion assertion,
             XMLStreamWriter writer) throws XMLStreamException {
         String writerPrefix = writer
                 .getPrefix(PolicyConstants.WS_POLICY_NAMESPACE_URI);
 
         if (writerPrefix == null) {
             writer.writeStartElement(PolicyConstants.WS_POLICY_PREFIX,
                     PolicyConstants.XOR_COMPOSITE_ASSERTION,
                     PolicyConstants.WS_POLICY_NAMESPACE_URI);
             writer.writeNamespace(PolicyConstants.WS_POLICY_PREFIX,
                     PolicyConstants.WS_POLICY_NAMESPACE_URI);
             writer.setPrefix(PolicyConstants.WS_POLICY_PREFIX,
                     PolicyConstants.WS_POLICY_NAMESPACE_URI);
 
         } else {
             writer.writeStartElement(PolicyConstants.WS_POLICY_NAMESPACE_URI,
                     PolicyConstants.XOR_COMPOSITE_ASSERTION);
         }
 
         List terms = assertion.getTerms();
         writeTerms(terms, writer);
 
         writer.writeEndElement();
     }
 
     private void writePrimitiveAssertion(PrimitiveAssertion assertion,
             XMLStreamWriter writer) throws XMLStreamException {
         QName qname = assertion.getName();
 
         String writerPrefix = writer.getPrefix(qname.getNamespaceURI());
         if (writerPrefix != null) {
             writer.writeStartElement(qname.getNamespaceURI(), qname
                     .getLocalPart());
         } else {
             String prefix = (qname.getPrefix() != null) ? qname.getPrefix()
                     : generateNamespace();
             writer.writeStartElement(prefix, qname.getLocalPart(), qname
                     .getNamespaceURI());
             writer.writeNamespace(prefix, qname.getNamespaceURI());
             writer.setPrefix(prefix, qname.getNamespaceURI());
 
         }
 
         Hashtable attributes = assertion.getAttributes();
         writeAttributes(attributes, writer);
 
         String text = (String) assertion.getStrValue();
         if (text != null) {
             writer.writeCharacters(text);
         }
 
         List terms = assertion.getTerms();
         writeTerms(terms, writer);
 
         writer.writeEndElement();
     }
 
     public void writePolicyReference(PolicyReference assertion,
             XMLStreamWriter writer) throws XMLStreamException {
 
         String writerPrefix = writer
                 .getPrefix(PolicyConstants.WS_POLICY_NAMESPACE_URI);
         if (writerPrefix != null) {
             writer.writeStartElement(PolicyConstants.WS_POLICY_NAMESPACE_URI,
                     PolicyConstants.WS_POLICY_REFERENCE);
         } else {
 
             writer.writeStartElement(PolicyConstants.WS_POLICY_PREFIX,
                     PolicyConstants.WS_POLICY_REFERENCE,
                     PolicyConstants.WS_POLICY_NAMESPACE_URI);
             writer.writeNamespace(PolicyConstants.WS_POLICY_PREFIX,
                     PolicyConstants.WS_POLICY_NAMESPACE_URI);
             writer.setPrefix(PolicyConstants.WS_POLICY_PREFIX,
                     PolicyConstants.WS_POLICY_NAMESPACE_URI);
 
         }
         writer.writeAttribute("URI", assertion.getPolicyURIString());
         
         writer.writeEndElement();
     }
 
     private void writeTerms(List terms, XMLStreamWriter writer)
             throws XMLStreamException {
 
         Iterator iterator = terms.iterator();
         while (iterator.hasNext()) {
             Assertion assertion = (Assertion) iterator.next();
             writeAssertion(assertion, writer);
         }
     }
 
     private void writeAttributes(Hashtable attributes, XMLStreamWriter writer)
             throws XMLStreamException {
 
         Iterator iterator = attributes.keySet().iterator();
         while (iterator.hasNext()) {
             QName qname = (QName) iterator.next();
             String value = (String) attributes.get(qname);
 
             String prefix = qname.getPrefix();
             if (prefix != null) {
                 writer.writeAttribute(prefix, qname.getNamespaceURI(), qname
                         .getLocalPart(), value);
             } else {
                 writer.writeAttribute(qname.getNamespaceURI(), qname
                         .getLocalPart(), value);
             }
         }
     }
 
     private String generateNamespace() {
         return "ns" + num++;
     }
 }
