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
 
 package com.sun.xml.fastinfoset.dom;
 
 import com.sun.xml.fastinfoset.Encoder;
 import com.sun.xml.fastinfoset.EncodingConstants;
 import com.sun.xml.fastinfoset.QualifiedName;
 import com.sun.xml.fastinfoset.util.LocalNameQualifiedNamesMap;
 import java.io.IOException;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class DOMDocumentSerializer extends Encoder {
     
     public DOMDocumentSerializer() {
     }
     
     public final void serialize(Node n) throws IOException {
         switch (n.getNodeType()) {
             case Node.DOCUMENT_NODE:
                 serialize((Document)n);
             case Node.ELEMENT_NODE:
                 serializeElementAsDocument(n);
                 break;
             case Node.COMMENT_NODE:
                 serializeComment(n);
                 break;
             case Node.PROCESSING_INSTRUCTION_NODE:
                 serializeProcessingInstruction(n);
                 break;
         }
     }
     
     public final void serialize(Document d) throws IOException {
         reset();
         encodeHeader(false);
         encodeInitialVocabulary();
         
         final NodeList nl = d.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++) {
             encodeTermination();
             
             final Node n = nl.item(i);
             switch (n.getNodeType()) {
                 case Node.ELEMENT_NODE:
                     serializeElement(n);
                     break;
                 case Node.COMMENT_NODE:
                     serializeComment(n);
                     break;
                 case Node.PROCESSING_INSTRUCTION_NODE:
                     serializeProcessingInstruction(n);
                     break;
             }
         }
         encodeDocumentTermination();
     }
 
     protected final void serializeElementAsDocument(Node e) throws IOException {
         encodeHeader(false);
         encodeInitialVocabulary();
 
         serializeElement(e);
         
         encodeDocumentTermination();
     }
     
 
     protected Node[] _namespaceAttributes = new Node[4];
     protected Node[] _attributes = new Node[32];
     
     protected final void serializeElement(Node e) throws IOException {
         int namespaceAttributesSize = 0;
         int attributesSize = 0;
         if (e.hasAttributes()) {
             /* 
              * Split the attribute nodes into namespace attributes
              * or normal attributes.
              */
             final NamedNodeMap nnm = e.getAttributes();
             for (int i = 0; i < nnm.getLength(); i++) {
                 final Node a = nnm.item(i);
                 final String namespaceURI = a.getNamespaceURI();
                 if (namespaceURI != null && namespaceURI.equals("http://www.w3.org/2000/xmlns/")) {
                     if (namespaceAttributesSize == _namespaceAttributes.length) {
                         final Node[] attributes = new Node[namespaceAttributesSize * 3 / 2 + 1];
                         System.arraycopy(_namespaceAttributes, 0, attributes, 0, namespaceAttributesSize);
                         _namespaceAttributes = attributes;
                     }
                     _namespaceAttributes[namespaceAttributesSize++] = a;
                 } else {
                     if (attributesSize == _attributes.length) {
                         final Node[] attributes = new Node[attributesSize * 3 / 2 + 1];
                         System.arraycopy(_attributes, 0, attributes, 0, attributesSize);
                         _attributes = attributes;
                     }
                     _attributes[attributesSize++] = a;
                 }
             }
         }
         
         if (namespaceAttributesSize > 0) {
             if (attributesSize > 0) {
                 write(EncodingConstants.ELEMENT | EncodingConstants.ELEMENT_NAMESPACES_FLAG |
                         EncodingConstants.ELEMENT_ATTRIBUTE_FLAG);
             } else {
                 write(EncodingConstants.ELEMENT | EncodingConstants.ELEMENT_NAMESPACES_FLAG);
             }
 
             // Serialize the namespace attributes
             for (int i = 0; i < namespaceAttributesSize; i++) {
                 final Node a = _namespaceAttributes[i];
                 _namespaceAttributes[i] = null;
                 String prefix = a.getLocalName();
                 if (prefix == "xmlns" || prefix.equals("xmlns")) {
                     prefix = "";
                 }
                 final String uri = a.getNodeValue();
                 encodeNamespaceAttribute(prefix, uri);
             }
             
             write(EncodingConstants.TERMINATOR);
             _b = 0;
         } else {
             _b = (attributesSize > 0) ? EncodingConstants.ELEMENT | EncodingConstants.ELEMENT_ATTRIBUTE_FLAG :
                 EncodingConstants.ELEMENT;
         }
         
         String namespaceURI = e.getNamespaceURI();
         namespaceURI = (namespaceURI == null) ? "" : namespaceURI;
         encodeElement(namespaceURI, e.getNodeName(), e.getLocalName());
 
         if (attributesSize > 0) {
             // Serialize the attributes
             for (int i = 0; i < attributesSize; i++) {
                 final Node a = _attributes[i];
                 _attributes[i] = null;
                 namespaceURI = a.getNamespaceURI();
                 namespaceURI = (namespaceURI == null) ? "" : namespaceURI;
                 encodeAttribute(namespaceURI, a.getNodeName(), a.getLocalName());
                 
                 final String value = a.getNodeValue();
                 final boolean addToTable = (value.length() < _v.attributeValueSizeConstraint) ? true : false;
                 encodeNonIdentifyingStringOnFirstBit(value, _v.attributeValue, addToTable);
             }
             
             _b = EncodingConstants.TERMINATOR;
             _terminate = true;            
         }
         
         if (e.hasChildNodes()) {
             // Serialize the children
             final NodeList nl = e.getChildNodes();
             for (int i = 0; i < nl.getLength(); i++) {
                encodeTermination();
    
                 final Node n = nl.item(i);                
                 switch (n.getNodeType()) {
                     case Node.ELEMENT_NODE:
                         serializeElement(n);
                         break;
                     case Node.TEXT_NODE:
                         serializeText(n);
                         break;
                     case Node.COMMENT_NODE:
                         serializeComment(n);
                         break;
                     case Node.PROCESSING_INSTRUCTION_NODE:
                         serializeProcessingInstruction(n);
                         break;
                 }
             }
         }
         encodeElementTermination();
     }
     
     protected final void serializeText(Node t) throws IOException {
         final String text = t.getNodeValue();
         
         final int length = text.length();
         if (length == 0) {
             return;
         } else if (length < _charBuffer.length) {
             text.getChars(0, length, _charBuffer, 0);
             encodeCharacters(_charBuffer, 0, length);
         } else {
             final char ch[] = text.toCharArray();
             encodeCharactersNoClone(ch, 0, length);
         }
     }
 
     protected final void serializeComment(Node c) throws IOException {
         final String comment = c.getNodeValue();
         
         final int length = comment.length();
         if (length == 0) {
             return;
         } else if (length < _charBuffer.length) {
             comment.getChars(0, length, _charBuffer, 0);
             encodeComment(_charBuffer, 0, length);
         } else {
             final char ch[] = comment.toCharArray();
             encodeCommentNoClone(ch, 0, length);
         }
     }
     
     protected final void serializeProcessingInstruction(Node pi) throws IOException {
         final String target = pi.getNodeName();
         final String data = pi.getNodeValue();
         encodeProcessingInstruction(target, data);
     }
     
     protected final void encodeElement(String namespaceURI, String qName, String localName) throws IOException {
         LocalNameQualifiedNamesMap.Entry entry = _v.elementName.obtainEntry(qName);
         if (entry._valueIndex > 0) {
             final QualifiedName[] names = entry._value;
             for (int i = 0; i < entry._valueIndex; i++) {
                 if ((namespaceURI == names[i].namespaceName || namespaceURI.equals(names[i].namespaceName))) {
                     encodeNonZeroIntegerOnThirdBit(names[i].index);
                     return;
                 }
             }                
         }
         
         // Was DOM node created using an NS-aware call?
         if (localName != null) {
             encodeLiteralElementQualifiedNameOnThirdBit(namespaceURI, getPrefixFromQualifiedName(qName), 
                     localName, entry);
         }
         else {
             encodeLiteralElementQualifiedNameOnThirdBit(namespaceURI, "", qName, entry);            
         }
     }
 
     protected final void encodeAttribute(String namespaceURI, String qName, String localName) throws IOException {
         LocalNameQualifiedNamesMap.Entry entry = _v.attributeName.obtainEntry(qName);
         if (entry._valueIndex > 0) {
             final QualifiedName[] names = entry._value;
             for (int i = 0; i < entry._valueIndex; i++) {
                 if ((namespaceURI == names[i].namespaceName || namespaceURI.equals(names[i].namespaceName))) {
                     encodeNonZeroIntegerOnSecondBitFirstBitZero(names[i].index);
                     return;
                 }
             }                
         } 
         
         // Was DOM node created using an NS-aware call?
         if (localName != null) {
             encodeLiteralAttributeQualifiedNameOnSecondBit(namespaceURI, getPrefixFromQualifiedName(qName), 
                     localName, entry);
         }
         else {
             encodeLiteralAttributeQualifiedNameOnSecondBit(namespaceURI, "", qName, entry);            
         }
     }    
 }
