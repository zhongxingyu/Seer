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
 
import org.apache.ws.policy.PolicyConstants;

 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 /**
  * PolicyReference is a wrapper that holds explict PolicyReferences.
  */
 public class PolicyReference implements PolicyComponent {
 
     private String uri;
 
     public void setURI(String uri) {
         this.uri = uri;
     }
 
     public String getURI() {
         return uri;
     }
 
     public boolean equal(PolicyComponent policyComponent) {
         throw new UnsupportedOperationException("TODO");
     }
 
     public short getType() {
         return Constants.TYPE_POLICYREF;
     }
 
     public PolicyComponent normalize() {
         throw new UnsupportedOperationException("PolicyReference.normalize() is meaningless");
     }
     
     public PolicyComponent normalize(PolicyRegistry reg, boolean deep) {
         String key = getURI();
         if (key.startsWith("#")) {
             key = key.substring(1);
         }
         
         Policy policy = reg.lookup(key);
         
         if (policy == null) {
             throw new RuntimeException(key + " can't be resolved" );
         }
         
         return policy.normalize(reg, deep);
     }
 
     public void serialize(XMLStreamWriter writer) throws XMLStreamException {
 
        String writerPrefix = writer
                .getPrefix(PolicyConstants.POLICY_NAMESPACE_URI);
        if (writerPrefix != null) {
            writer.writeStartElement(PolicyConstants.POLICY_NAMESPACE_URI,
                    PolicyConstants.POLICY_REFERENCE);
        } else {

            writer.writeStartElement(PolicyConstants.POLICY_PREFIX,
                    PolicyConstants.POLICY_REFERENCE,
                    PolicyConstants.POLICY_NAMESPACE_URI);
            writer.writeNamespace(PolicyConstants.POLICY_PREFIX,
                    PolicyConstants.POLICY_NAMESPACE_URI);
            writer.setPrefix(PolicyConstants.POLICY_PREFIX,
                    PolicyConstants.POLICY_NAMESPACE_URI);

        }
        writer.writeAttribute("URI", uri);

        writer.writeEndElement();
     }
 }
