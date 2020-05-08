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
 
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import javax.xml.namespace.QName;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.NamedNodeMap;
 import org.xml.sax.SAXException;
 
 import org.apache.ws.policy.model.AndCompositeAssertion;
 import org.apache.ws.policy.model.Assertion;
 import org.apache.ws.policy.model.Policy;
 import org.apache.ws.policy.model.PolicyReference;
 import org.apache.ws.policy.model.PrimitiveAssertion;
 import org.apache.ws.policy.model.PolicyConstants;
 import org.apache.ws.policy.model.XorCompositeAssertion;
 
 /**
  * @author Werner Dittmann (werner@apache.org)
  * @author Sanka Samaranayake (sanka@apache.org)
  */
 public class DOMPolicyReader implements PolicyReader {
 	DOMPolicyReader() {
 	}
 
 	public Policy readPolicy(InputStream in) {
         try {
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 
 			dbf.setNamespaceAware(true);
 			dbf.setValidating(false);
 
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			Document doc = db.parse(in);
 			Element element = doc.getDocumentElement();
 			return readPolicy(element);
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 			throw new RuntimeException("error : " + e.getMessage());
 		} catch (SAXException e) {
 			e.printStackTrace();
 			throw new RuntimeException("error : " + e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new RuntimeException("error : " + e.getMessage());
 		}
 	}
 	
 	private Assertion readAssertion(Element element) {
 		String namespace = element.getNamespaceURI();
 		String localName = element.getLocalName();
 		
 		if (!(namespace.equals(PolicyConstants.WS_POLICY_NAMESPACE_URI))) {
 			return readPrimitiveAssertion(element);
 		}
 		
 		if (localName.equals(PolicyConstants.WS_POLICY)) {
 			return readPolicy(element);
 			
 		} else if (localName.equals(PolicyConstants.AND_COMPOSITE_ASSERTION)) {
 			return readAndComposite(element);
 			
 		} else if (localName.equals(PolicyConstants.XOR_COMPOSITE_ASSERTION)) {
 			return readXorComposite(element);
 			
 		} else if (localName.equals(PolicyConstants.WS_POLICY_REFERENCE)) {
 			return readPolicyReference(element);
 			
 		} else {
 			throw new RuntimeException("unknown element ..");
 		}		
 	}
 	
 	private Policy readPolicy(Element element) {
 		Policy policy = new Policy();
         Attr attri;
         attri = element.getAttributeNodeNS(PolicyConstants.WSU_NAMESPACE_URI, "Id");
         if (attri != null) {
             policy.setId(attri.getValue());
         }
 
     	attri = element.getAttributeNodeNS(PolicyConstants.XML_NAMESPACE_URI, "base");
         if (attri != null) {
             policy.setBase(attri.getValue());
         }
 		
 		policy.addTerms(readTerms(element));
 		return policy;
 	}
 	
 	private AndCompositeAssertion readAndComposite(Element element) {
 		AndCompositeAssertion andCompositeAssertion = new AndCompositeAssertion();
 		andCompositeAssertion.addTerms(readTerms(element));
 		return andCompositeAssertion;
 	}	
 	
 	private XorCompositeAssertion readXorComposite(Element element) {
 		XorCompositeAssertion xorCompositeAssertion = new XorCompositeAssertion();
 		xorCompositeAssertion.addTerms(readTerms(element));
 		return xorCompositeAssertion;
 	}
 	
 	private PolicyReference readPolicyReference(Element element) {
 		Attr attribute = element.getAttributeNode("URI");
 		return new PolicyReference(attribute.getValue());
 	}
 	
 	private PrimitiveAssertion readPrimitiveAssertion(Element element) {
 		QName qname = new QName(element.getNamespaceURI(), element.getLocalName(), element.getPrefix());
         PrimitiveAssertion result = new PrimitiveAssertion(qname);
         
         result.setAttributes(getAttributes(element));
         String isOptional = result.getAttribute(new QName(
                 PolicyConstants.WS_POLICY_NAMESPACE_URI, "Optional"));
        result.setOptional(new Boolean(isOptional).booleanValue());
                        
         //CHECK ME
 		NodeList list = element.getChildNodes();
 		int length = list.getLength();
 
 		for (int i = 0; i < length; i++) {
 			Node node = list.item(i);
 			short nodeType = node.getNodeType();
 			
 			if (nodeType == Node.ELEMENT_NODE) {
 				Element childElement = (Element) node;
 				if (childElement.getNamespaceURI().equals(
 						PolicyConstants.WS_POLICY_NAMESPACE_URI)
 						&& childElement.getLocalName().equals(
 								PolicyConstants.WS_POLICY)) {
 					Policy policy = readPolicy(childElement);
 					result.addTerm(policy);
 
 				} else {
 					PrimitiveAssertion pa = readPrimitiveAssertion(childElement);
 					result.addTerm(pa);
 				}
 			}
 			else if (nodeType == Node.TEXT_NODE) {
 				String strValue = node.getNodeValue();
 		        
 		        if (strValue != null && strValue.length() != 0) {
 		        	result.setStrValue(strValue);            
 		        }
 			}
         }        
         return result;       
     }
 	
 	private ArrayList readTerms(Element element) {
 		ArrayList terms = new ArrayList();
 		NodeList list = element.getChildNodes();
 		int length = list.getLength();
 
 		for (int i = 0; i < length; i++) {
 			Object obj = list.item(i);
 			
 			if (obj instanceof Element) {
 				Element e = (Element) obj;
 				terms.add(readAssertion(e));				
 			}
 		}
 		return terms;		
 	}
 	
 	private Hashtable getAttributes(Element element) {
 		Hashtable attributes = new Hashtable();
 		NamedNodeMap map = element.getAttributes();
 
 		int length = map.getLength();
 
 		for (int i = 0; i < length; i++) {
 			Attr attribute = (Attr) map.item(i);
 			String prefix = attribute.getPrefix();
 			QName qn = null;
 			if (prefix != null) {
 				qn = new QName(attribute.getNamespaceURI(), attribute.getLocalName(), prefix);
 			}
 			else {
 				qn = new QName(attribute.getNamespaceURI(), attribute.getLocalName());				
 			}
 			attributes.put(qn, attribute.getValue());
 		}
 		return attributes;
 	}
 }
