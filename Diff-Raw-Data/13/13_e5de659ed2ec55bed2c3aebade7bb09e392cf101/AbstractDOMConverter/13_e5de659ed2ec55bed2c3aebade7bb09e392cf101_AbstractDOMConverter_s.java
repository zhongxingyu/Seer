 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.neethi.builders.converters;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 
 /**
  * 
  */
 public abstract class AbstractDOMConverter {
 
 
     public QName getQName(Element s) {
        return new QName(s.getNamespaceURI(), s.getLocalName());
     }
 
     public Map<QName, String> getAttributes(Element s) {
         Map<QName, String> mp = new HashMap<QName, String>();
         NamedNodeMap attrs = s.getAttributes();
         for (int x = 0; x < attrs.getLength(); x++) {
             Attr attr = (Attr)attrs.item(x);
            mp.put(new QName(attr.getNamespaceURI(), attr.getLocalName()), attr.getValue());
         }
         return mp;
     }
     
     public Iterator<Element> getChildren(Element s) {
         List<Element> children = new LinkedList<Element>();
         Node nd = s.getFirstChild();
         while (nd != null) {
             if (nd instanceof Element) {
                 children.add((Element)nd);
             }
             nd = nd.getNextSibling();
         }
         return children.listIterator();
     }
 }
