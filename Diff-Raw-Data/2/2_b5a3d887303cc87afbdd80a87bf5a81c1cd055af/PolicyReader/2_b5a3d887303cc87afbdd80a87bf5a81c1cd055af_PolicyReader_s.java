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
 
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.apache.axis2.om.OMAbstractFactory;
 import org.apache.axis2.om.OMAttribute;
 import org.apache.axis2.om.OMElement;
 import org.apache.axis2.om.OMXMLParserWrapper;
 import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;
 import org.apache.ws.policy.model.AndCompositeAssertion;
 import org.apache.ws.policy.model.Assertion;
 import org.apache.ws.policy.model.Policy;
 import org.apache.ws.policy.model.PolicyReference;
 import org.apache.ws.policy.model.PrimitiveAssertion;
 import org.apache.ws.policy.model.PolicyConstants;
 import org.apache.ws.policy.model.XorCompositeAssertion;
 
 /**
  * @author Sanka Samaranayake (sanka@apache.org)
  */
 public class PolicyReader {
 	PolicyReader() {
 	}
 
 	public Policy readPolicy(InputStream in) {
 		try {
 			XMLStreamReader reader = XMLInputFactory.newInstance()
 					.createXMLStreamReader(in);
 			OMXMLParserWrapper builder = OMXMLBuilderFactory
 					.createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
 							reader);
 
 			OMElement element = builder.getDocumentElement();
 			return readPolicy(element);
 
 		} catch (XMLStreamException ex) {
 			throw new RuntimeException("error : " + ex.getMessage());
 		}
 	}
 	
 	private Assertion readAssertion(OMElement element) {
 		String namespace = element.getNamespace().getName();
 		String localName = element.getLocalName();
 		
		if (!(namespace.equals(PolicyConstants.WSU_NAMESPACE_URI))) {
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
 	
 	private Policy readPolicy(OMElement element) {
 		Policy policy = new Policy();
 		policy.addTerms(readTerms(element));
 		return policy;
 	}
 	
 	private AndCompositeAssertion readAndComposite(OMElement element) {
 		AndCompositeAssertion andCompositeAssertion = new AndCompositeAssertion();
 		andCompositeAssertion.addTerms(readTerms(element));
 		return andCompositeAssertion;
 	}	
 	
 	private XorCompositeAssertion readXorComposite(OMElement element) {
 		XorCompositeAssertion xorCompositeAssertion = new XorCompositeAssertion();
 		xorCompositeAssertion.addTerms(readTerms(element));
 		return xorCompositeAssertion;
 	}
 	
 	private PolicyReference readPolicyReference(OMElement element) {
 		OMAttribute attribute = element.getAttribute(new QName("URI"));
 		return new PolicyReference(attribute.getAttributeValue());
 	}
 	
 	private PrimitiveAssertion readPrimitiveAssertion(OMElement element) {
         QName qname = element.getQName();
         PrimitiveAssertion result = new PrimitiveAssertion(qname);
         
         result.setAttributes(getAttributes(element));
         
         // setting the text value ..
         String strValue = element.getText();
      
         if (strValue != null && strValue.length() != 0) {
             result.setStrValue(strValue);            
         }
         
         //CHECK ME
         Iterator childElements = element.getChildElements();
         
         while (childElements.hasNext()) {
             OMElement childElement = (OMElement) childElements.next();
             
             if (childElement.getNamespace().getName().equals(PolicyConstants.WS_POLICY_NAMESPACE_URI)
                     && childElement.getLocalName().equals(PolicyConstants.WS_POLICY)) {
                 Policy policy = readPolicy(childElement);
                 result.addTerm(policy);
         
             } else {
                 PrimitiveAssertion pa = readPrimitiveAssertion(childElement);
                 result.addTerm(pa);                
             }
         }        
         return result;       
     }
 	
 	private ArrayList readTerms(OMElement element) {
 		ArrayList terms = new ArrayList();
 		Iterator childElements = element.getChildren();
 		
 		while (childElements.hasNext()) {
 			Object obj = childElements.next();
 			
 			if (obj instanceof OMElement) {
 				OMElement e = (OMElement) obj;
 				terms.add(readAssertion(e));				
 			}
 		}
 		return terms;		
 	}
 	
 	private Hashtable getAttributes(OMElement element) {
 		Hashtable attributes = new Hashtable();
 		Iterator iterator = element.getAllAttributes();
 
 		while (iterator.hasNext()) {
 			OMAttribute attribute = (OMAttribute) iterator.next();
 			attributes.put(attribute.getQName(), attribute.getAttributeValue());
 		}
 
 		return attributes;
 	}
 }
