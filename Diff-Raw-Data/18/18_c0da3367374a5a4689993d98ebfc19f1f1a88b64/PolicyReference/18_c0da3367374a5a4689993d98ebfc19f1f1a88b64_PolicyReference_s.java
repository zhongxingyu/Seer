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
        Policy policy = reg.lookup(getURI());
         
         if (policy == null) {
            throw new RuntimeException(getURI() + " can't be resolved" );
         }
         
         return policy.normalize(reg, deep);
     }
 
     public void serialize(XMLStreamWriter writer) throws XMLStreamException {
         throw new UnsupportedOperationException();
 
     }
 }
