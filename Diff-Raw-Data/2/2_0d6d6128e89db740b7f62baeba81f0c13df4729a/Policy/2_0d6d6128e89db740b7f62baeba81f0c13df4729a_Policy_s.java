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
 package org.apache.neethi;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 public class Policy extends All {
 
     private HashMap attributes = new HashMap();
 
     public PolicyComponent normalize(boolean deep) {
         return normalize(null, deep);
     }
     
     public PolicyComponent normalize(PolicyRegistry reg, boolean deep) {
         return normalize(this, reg, deep);
     }
 
     public Policy merge(Policy policy) {
         Policy result = new Policy();
         result.addPolicyComponents(getPolicyComponents());
         result.addPolicyComponents(policy.getPolicyComponents());
         return result;
     }
 
     public Policy intersect(Policy policy) {
         throw new UnsupportedOperationException();
     }
 
     public void serialize(XMLStreamWriter writer) throws XMLStreamException {
         String prefix = writer.getPrefix(Constants.ATTR_NAME);
 
         if (prefix == null) {
             writer.writeStartElement(Constants.POLICY_PREFIX, Constants.ELEM_POLICY, Constants.URI_POLICY_NS);
             writer.writeNamespace(Constants.POLICY_PREFIX, Constants.URI_POLICY_NS);
             writer.setPrefix(Constants.POLICY_PREFIX, Constants.URI_POLICY_NS);
 
         } else {
             writer.writeStartElement(Constants.URI_POLICY_NS, Constants.ELEM_POLICY);
         }
 
         PolicyComponent policyComponent;
 
         for (Iterator iterator = getPolicyComponents().iterator(); iterator
                 .hasNext();) {
             policyComponent = (PolicyComponent) iterator.next();
             policyComponent.serialize(writer);
         }
 
         writer.writeEndElement();
 
     }
 
     public short getType() {
         return Constants.TYPE_POLICY;
     }
 
     public void addAlternatives(PolicyAlternatives policyAlternatives) {
         policyComponents.add(policyAlternatives);
     }
 
     public Iterator getAlternatives() {
         return new PolicyIterator(this);
     }
 
     private class PolicyIterator implements Iterator {
         Iterator alternatives = null;
 
         public PolicyIterator(Policy policy) {
             policy = (Policy) policy.normalize(false);
             ExactlyOne exactlyOne = (ExactlyOne) policy
                     .getFirstPolicyComponent();
             alternatives = exactlyOne.getPolicyComponents().iterator();
         }
 
         public boolean hasNext() {
             return alternatives.hasNext();
         }
 
         public Object next() {
             return ((All) alternatives.next()).getPolicyComponents();
         }
 
         public void remove() {
             throw new UnsupportedOperationException(
                     "policyAlternative.remove() is not supported");
         }
     }
 
     public void addAttribute(QName name, String value) {
         attributes.put(name, value);
     }
 
     public String getAttribute(QName name) {
         return (String) attributes.get(name);
     }
 
     public Map getAttributes() {
         return attributes;
     }
 
     public void setName(String name) {
         addAttribute(new QName("", Constants.ATTR_NAME), name);
     }
 
     public String getName() {
         return getAttribute(new QName("", Constants.ATTR_NAME));
     }
 
     public void setId(String id) {
         addAttribute(
                 new QName(Constants.URI_WSU_NS, Constants.ATTR_ID),
                getName());
     }
 
     public String getId() {
         return getAttribute(new QName(Constants.URI_WSU_NS,
                 Constants.ATTR_ID));
     }
 }
