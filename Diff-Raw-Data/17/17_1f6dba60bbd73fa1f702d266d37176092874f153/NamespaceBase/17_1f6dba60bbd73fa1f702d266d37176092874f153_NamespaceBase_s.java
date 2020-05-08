 /*
  * Fast Infoset ver. 0.1 software ("Software")
  * 
  * Copyright, 2004-2005 Sun Microsystems, Inc. All Rights Reserved. 
  * 
  * Software is licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License. You may
  * obtain a copy of the License at:
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations.
  * 
  *    Sun supports and benefits from the global community of open source
  * developers, and thanks the community for its important contributions and
  * open standards-based technology, which Sun has adopted into many of its
  * products.
  * 
  *    Please note that portions of Software may be provided with notices and
  * open source licenses from such communities and third parties that govern the
  * use of those portions, and any licenses granted hereunder do not alter any
  * rights and obligations you may have under such open source licenses,
  * however, the disclaimer of warranty and limitation of liability provisions
  * in this License will apply to all Software in this distribution.
  * 
  *    You acknowledge that the Software is not designed, licensed or intended
  * for use in the design, construction, operation or maintenance of any nuclear
  * facility.
  *
  * Apache License
  * Version 2.0, January 2004
  * http://www.apache.org/licenses/
  *
  */ 
 
 package com.sun.xml.fastinfoset.stax.events;
 
 import javax.xml.stream.events.Namespace;
 import javax.xml.stream.events.XMLEvent;
 import javax.xml.namespace.QName;
 import com.sun.xml.fastinfoset.util.Util;
 
 public class NamespaceBase extends AttributeBase implements Namespace{
     //J2SE1.5.0 javax.xml.XMLConstants
     static final String DEFAULT_NS_PREFIX = "";
     static final String XML_NS_URI = "http://www.w3.org/XML/1998/namespace";
     static final String XML_NS_PREFIX = "xml";
     static final String XMLNS_ATTRIBUTE_NS_URI = "http://www.w3.org/2000/xmlns/";
     static final String XMLNS_ATTRIBUTE = "xmlns";
     static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
     static final String W3C_XML_SCHEMA_INSTANCE_NS_URI = "http://www.w3.org/2001/XMLSchema-instance";
     
     //is this namespace default declaration?
     private boolean defaultDeclaration = false;
     
     /** a namespace attribute has a form: xmlns:NCName="URI reference" */
     public NamespaceBase(String namespaceURI) {
         super(XMLNS_ATTRIBUTE, "", namespaceURI);
         setEventType(NAMESPACE);
     }
 
   /**
    * Create a new Namespace
    * @param prefix prefix of a namespace is the local name for an attribute
    * @param namespaceURI the uri reference of a namespace is the value for an attribute
    * @return Namespace object
    */
     public NamespaceBase(String prefix, String namespaceURI){
         super(XMLNS_ATTRIBUTE, prefix, namespaceURI);
         setEventType(NAMESPACE);
        if (!Util.isEmptyString(prefix)) {
             defaultDeclaration=true;
         }
     }
     
     void setPrefix(String prefix){
         if(prefix == null)
             setName(new QName(XMLNS_ATTRIBUTE_NS_URI,DEFAULT_NS_PREFIX,XMLNS_ATTRIBUTE));
         else// new QName(uri, localpart, prefix)
             setName(new QName(XMLNS_ATTRIBUTE_NS_URI,prefix,XMLNS_ATTRIBUTE));
     }
     
     public String getPrefix() {
         if (defaultDeclaration) return "";
         return super.getLocalName();
     }
     
     
   /**
    * set Namespace URI reference (xmlns:prefix = "uri")
    * @param uri the uri reference of a namespace is the value for an attribute
    */
     void setNamespaceURI(String uri) {
         setValue(uri);
     }
     public String getNamespaceURI() {
         return getValue();
     }
     
 
     public boolean isNamespace(){
         return true;
     }
     
     public boolean isDefaultNamespaceDeclaration() {
         return defaultDeclaration;
     }
     
 
 }
