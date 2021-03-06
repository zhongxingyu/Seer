 /*
  * Copyright 2004,2005 The Apache Software Foundation.
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
 package org.apache.axiom.om.impl.dom;
 
 import org.apache.axiom.om.OMConstants;
 import org.apache.axiom.om.OMContainer;
 import org.apache.axiom.om.OMDocument;
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.OMException;
 import org.apache.axiom.om.OMFactory;
 import org.apache.axiom.om.OMNode;
 import org.apache.axiom.om.OMOutputFormat;
 import org.apache.axiom.om.OMXMLParserWrapper;
 import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
 import org.w3c.dom.Attr;
 import org.w3c.dom.CDATASection;
 import org.w3c.dom.Comment;
 import org.w3c.dom.DOMConfiguration;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.DocumentType;
 import org.w3c.dom.Element;
 import org.w3c.dom.EntityReference;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.ProcessingInstruction;
 import org.w3c.dom.Text;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import java.io.OutputStream;
 import java.util.Hashtable;
 
 public class DocumentImpl extends ParentNode implements Document, OMDocument {
 
     protected Hashtable identifiers;
 
     private String xmlVersion;
 
     private String charEncoding;
 
     protected ElementImpl documentElement;
 
     /**
      * @param ownerDocument
      */
     public DocumentImpl(DocumentImpl ownerDocument, OMFactory factory) {
         super(ownerDocument, factory);
         ((OMDOMFactory)factory).setDocument(this);
         this.done = true;
     }
 
     public DocumentImpl(OMXMLParserWrapper parserWrapper, OMFactory factory) {
         super(factory);
         this.builder = parserWrapper;
         ((OMDOMFactory)factory).setDocument(this);
     }
 
     public DocumentImpl(OMFactory factory) {
         super(factory);
         ((OMDOMFactory)factory).setDocument(this);
         this.done = true;
     }
 
     // /
     // /OMNode methods
     // //
     public void setType(int nodeType) throws OMException {
         throw new UnsupportedOperationException(
                 "In OM Document object doesn't have a type");
     }
 
     public int getType() throws OMException {
         throw new UnsupportedOperationException(
                 "In OM Document object doesn't have a type");
     }
 
     public void internalSerialize(XMLStreamWriter writer) throws XMLStreamException {
         // TODO Auto-generated method stub
     }
 
     // /
     // /Overrides ChildNode specific methods.
     // /
     public OMNode getNextOMSibling() throws OMException {
         throw new UnsupportedOperationException("This is the document node");
     }
 
     public Node getNextSibling() {
         throw new UnsupportedOperationException("This is the document node");
     }
 
     public OMContainer getParent() throws OMException {
         throw new UnsupportedOperationException("This is the document node");
     }
 
     public OMNode getPreviousOMSibling() {
         throw new UnsupportedOperationException("This is the document node");
     }
 
     public Node getPreviousSibling() {
         throw new UnsupportedOperationException("This is the document node");
     }
 
     public void setNextOMSibling(OMNode node) {
         throw new UnsupportedOperationException("This is the document node");
     }
 
     public void setParent(OMContainer element) {
         throw new UnsupportedOperationException("This is the document node");
     }
 
     public void setPreviousOMSibling(OMNode node) {
         throw new UnsupportedOperationException("This is the document node");
     }
 
     // /
     // /org.w3c.dom.Node methods
     // /
     public String getNodeName() {
         return "#document";
     }
 
     public short getNodeType() {
         return Node.DOCUMENT_NODE;
     }
 
     // /org.w3c.dom.Document methods
     // /
 
     public Attr createAttribute(String name) throws DOMException {
         if (!DOMUtil.isValidChras(name)) {
             String msg = DOMMessageFormatter.formatMessage(
                     DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR",
                     null);
             throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
         }
         return new AttrImpl(this, name, this.factory);
     }
 
     public Attr createAttributeNS(String namespaceURI, String qualifiedName)
             throws DOMException {
         String localName = DOMUtil.getLocalName(qualifiedName);
         String prefix = DOMUtil.getPrefix(qualifiedName);
 
         if(!OMConstants.XMLNS_NS_PREFIX.equals(localName)) {
             this.checkQName(prefix, localName);
         } else {
             return this.createAttribute(localName);
         }
 
         return new AttrImpl(this, localName, new NamespaceImpl(
                 namespaceURI, prefix), this.factory);
     }
 
     public CDATASection createCDATASection(String arg0) throws DOMException {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public Comment createComment(String data) {
         return new CommentImpl(this, data, this.factory);
     }
 
     public DocumentFragment createDocumentFragment() {
         return new DocumentFragmentimpl(this, this.factory);
     }
 
     public Element createElement(String tagName) throws DOMException {
         return new ElementImpl(this, tagName, this.factory);
     }
 
     public Element createElementNS(String ns, String qualifiedName)
             throws DOMException {
 
         String localName = DOMUtil.getLocalName(qualifiedName);
         String prefix = DOMUtil.getPrefix(qualifiedName);
 
         if (ns != null && (prefix != null || "".equals(prefix))) {
             this.checkQName(prefix, localName);
         }
 
        NamespaceImpl namespace = new NamespaceImpl(ns, prefix == null ? "" : prefix);
         return new ElementImpl(this, localName, namespace, this.factory);
     }
 
     public EntityReference createEntityReference(String arg0)
             throws DOMException {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public ProcessingInstruction createProcessingInstruction(String arg0,
             String arg1) throws DOMException {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public Text createTextNode(String value) {
         return new TextImpl(this, value, this.factory);
     }
 
     public DocumentType getDoctype() {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public Element getElementById(String arg0) {
         // TODO getElementById
         throw new UnsupportedOperationException("TODO: getElementById");
     }
 
     public NodeList getElementsByTagName(String arg0) {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public NodeList getElementsByTagNameNS(String arg0, String arg1) {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public DOMImplementation getImplementation() {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public Node importNode(Node importedNode, boolean deep) throws DOMException {
 
         short type = importedNode.getNodeType();
         Node newNode = null;
         switch (type) {
         case Node.ELEMENT_NODE: {
             Element newElement;
             if (importedNode.getLocalName() == null) {
                 newElement = this.createElement(importedNode.getNodeName());
             } else {
                 newElement = createElementNS(importedNode.getNamespaceURI(),
                         importedNode.getNodeName());
             }
 
             // Copy element's attributes, if any.
             NamedNodeMap sourceAttrs = importedNode.getAttributes();
             if (sourceAttrs != null) {
                 int length = sourceAttrs.getLength();
                 for (int index = 0; index < length; index++) {
                     Attr attr = (Attr) sourceAttrs.item(index);
                     if (attr.getNamespaceURI() != null
                             && !attr.getNamespaceURI().equals(
                                     OMConstants.XMLNS_NS_URI)) {
                         Attr newAttr = (Attr) importNode(attr, true);
                         newElement.setAttributeNodeNS(newAttr);
                     } else { // if (attr.getLocalName() == null) {
                         Attr newAttr = (Attr) importNode(attr, true);
                         newElement.setAttributeNode(newAttr);
                     }
 
                 }
             }
             newNode = newElement;
             break;
         }
 
         case Node.ATTRIBUTE_NODE: {
             if ("".equals(importedNode.getNamespaceURI())
                     || importedNode.getNamespaceURI() == null) {
                 newNode = createAttribute(importedNode.getNodeName());
             } else {
                 //Check whether it is a default ns decl
                 if(OMConstants.XMLNS_NS_PREFIX.equals(importedNode.getNodeName())) {
                     newNode = createAttribute(importedNode.getNodeName());
                 } else {
                     newNode = createAttributeNS(importedNode.getNamespaceURI(),
                             importedNode.getNodeName());
                 }
             }
             ((Attr) newNode).setValue(importedNode.getNodeValue());
             break;
         }
 
         case Node.TEXT_NODE: {
             newNode = createTextNode(importedNode.getNodeValue());
             break;
         }
 
         case Node.DOCUMENT_FRAGMENT_NODE: {
             newNode = createDocumentFragment();
             // No name, kids carry value
             break;
         }
 
         case Node.CDATA_SECTION_NODE:
         case Node.ENTITY_REFERENCE_NODE:
         case Node.ENTITY_NODE:
         case Node.PROCESSING_INSTRUCTION_NODE:
         case Node.COMMENT_NODE:
         case Node.DOCUMENT_TYPE_NODE:
         case Node.NOTATION_NODE:
             throw new UnsupportedOperationException("TODO");
 
         case Node.DOCUMENT_NODE: // Can't import document nodes
         default: { // Unknown node type
             String msg = DOMMessageFormatter.formatMessage(
                     DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
             throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
         }
 
         }
 
         // If deep, replicate and attach the kids.
         if (deep) {
             for (Node srckid = importedNode.getFirstChild(); srckid != null; 
                     srckid = srckid.getNextSibling()) {
                 newNode.appendChild(importNode(srckid, true));
             }
         }
 
         return newNode;
 
     }
 
     public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public void internalSerializeAndConsume(XMLStreamWriter writer)
             throws XMLStreamException {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public void serializeAndConsume(XMLStreamWriter xmlWriter)
             throws XMLStreamException {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     // /
     // /OMDocument Methods
     // /
     public String getCharsetEncoding() {
         return this.charEncoding;
     }
 
     public String getXMLVersion() {
         return this.xmlVersion;
     }
 
     public String isStandalone() {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public void setCharsetEncoding(String charsetEncoding) {
         this.charEncoding = charsetEncoding;
     }
 
     public void setOMDocumentElement(OMElement rootElement) {
         this.firstChild = (ElementImpl) rootElement;
     }
 
     public void setStandalone(String isStandalone) {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public void serializeAndConsume(OutputStream output, OMOutputFormat format)
             throws XMLStreamException {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public void serialize(OutputStream output, OMOutputFormat format)
             throws XMLStreamException {
         // TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public void setXMLVersion(String version) {
         this.xmlVersion = version;
     }
 
     /**
      * Returns the document element.
      * 
      * @see org.apache.axiom.om.OMDocument#getOMDocumentElement()
      */
     public OMElement getOMDocumentElement() {
         
         //We'r sure that only an element can be the first child of a Document
         if (this.documentElement == null && !this.done) {
             this.builder.next();
         }
         return this.documentElement;
     }
 
     /**
      * Returns the document element.
      * 
      * @see org.w3c.dom.Document#getDocumentElement()
      */
     public Element getDocumentElement() {
 
         return (Element) this.getOMDocumentElement();
     }
 
     /**
      * Borrowed from the Xerces impl. Checks if the given qualified name is
      * legal with respect to the version of XML to which this document must
      * conform.
      * 
      * @param prefix
      *            prefix of qualified name
      * @param local
      *            local part of qualified name
      */
     protected final void checkQName(String prefix, String local) {
 
         // check that both prefix and local part match NCName
         boolean validNCName = (prefix == null || XMLChar.isValidNCName(prefix))
                 && XMLChar.isValidNCName(local);
 
         if (!validNCName) {
             // REVISIT: add qname parameter to the message
             String msg = DOMMessageFormatter.formatMessage(
                     DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR",
                     null);
             throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
         }
 
         if (prefix == null || prefix.equals("")) {
             String msg = DOMMessageFormatter.formatMessage(
                     DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null);
             throw new DOMException(DOMException.NAMESPACE_ERR, msg);
         }
     }
 
     public void build() {
         if(this.firstChild != null && !this.firstChild.done) {
             this.firstChild.build();
         }
         this.done = true;
     }
     
     /*
      * DOM-Level 3 methods
      */
 
     public Node adoptNode(Node arg0) throws DOMException {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public String getDocumentURI() {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public DOMConfiguration getDomConfig() {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public String getInputEncoding() {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public boolean getStrictErrorChecking() {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public String getXmlEncoding() {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public boolean getXmlStandalone() {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public String getXmlVersion() {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public void normalizeDocument() {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public Node renameNode(Node arg0, String arg1, String arg2)
             throws DOMException {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public void setDocumentURI(String arg0) {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public void setStrictErrorChecking(boolean arg0) {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public void setXmlStandalone(boolean arg0) throws DOMException {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
     public void setXmlVersion(String arg0) throws DOMException {
         // TODO TODO
         throw new UnsupportedOperationException("TODO");
     }
 
 }
