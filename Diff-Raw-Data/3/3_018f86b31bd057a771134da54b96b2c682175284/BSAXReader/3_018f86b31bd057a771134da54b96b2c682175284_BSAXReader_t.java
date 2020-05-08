 /*
  * BSAXReader.java
  * 
  *  Copyright 2005 Gregor N. Purdy. All rights reserved.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.gregorpurdy.xml.sax;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xml.sax.ContentHandler;
 import org.xml.sax.DTDHandler;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXNotRecognizedException;
 import org.xml.sax.SAXNotSupportedException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.AttributesImpl;
 
 import com.gregorpurdy.xml.bsax.AbstractBSAXReader;
 import com.gregorpurdy.xml.bsax.BSAXConstants;
 
 /**
  * This class lives in the *.xml.sax package in analogy with
  * the org.xml.sax.XMLReader class, which reads XML and produces
  * SAX events. What it reads as input is in the class name, and
  * what it produces as output is in the package name.
  * 
 * @author Gregor N. Purdy &lt;gregor@focusresearch.com&gt; http://www.gregorpurdy.com/gregor/
  * @version $Id$
  */
 public class BSAXReader extends AbstractBSAXReader implements XMLReader {
   
   private ContentHandler contentHandler;
   
   private DTDHandler dtdHandler;
   
   private EntityResolver entityResolver;
   
   private ErrorHandler errorHandler;
   
   private int maxStringTableSize = BSAXConstants.UNLIMITED_STRING_TABLE_SIZE;
   
   private InputStream stream = null;
   
   private List stringTable = null;
 
   //
   // Used during the processing of a start-element operation:
   //
   
   private String elementUriString = null;
   private String elementLocalNameString = null;
   private String elementQNameString = null;
   private AttributesImpl attrs = null; 
 
   /**
    * @param attrs
    * @param i
    * @throws SAXException
    */
   protected void doOpAttribute(int i, int attrUri, int attrLocalName, int attrQName, int attrType, int attrValue) throws SAXException {
     String attrUriString = getString(attrUri);
     String attrLocalNameString = getString(attrLocalName);
     String attrQNameString = getString(attrQName);
     String attrTypeString = getString(attrType);
     String attrValueString = getString(attrValue);
     
     attrs.addAttribute(attrUriString, attrLocalNameString, attrQNameString,
         attrTypeString, attrValueString);
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpCharacters(int characters) throws SAXException {
     String characterString = getString(characters);
     
     if (characterString != null) {
       contentHandler.characters(characterString.toCharArray(), 0, characterString.length());
     }
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpEndDocument() throws SAXException {
     contentHandler.endDocument();
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpEndElement(int uri, int localName, int qName) throws SAXException {
     String uriString = getString(uri);
     String localNameString = getString(localName);
     String qNameString = getString(qName);
     
     contentHandler.endElement(uriString, localNameString, qNameString);
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpEndPrefixMapping(int prefix) throws SAXException {
     String prefixString = getString(prefix);
     
     contentHandler.endPrefixMapping(prefixString);
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpIgnorableWhitespace(int characters) throws SAXException {
     String characterString = getString(characters);
     
     contentHandler.ignorableWhitespace(characterString.toCharArray(), 0, characterString
         .length());
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpProcessingInstruction(int target, int data) throws SAXException {
     String targetString = getString(target);
     String dataString = getString(data);
     
     contentHandler.processingInstruction(targetString, dataString);
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpSkippedEntity(int name) throws SAXException {
     String nameString = getString(name);
     
     contentHandler.skippedEntity(nameString);
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpStartDocument() throws SAXException {
     contentHandler.startDocument();
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpStartElement(int uri, int localName, int qName, int attributeCount) throws SAXException {
     elementUriString = getString(uri);
     elementLocalNameString = getString(localName);
     elementQNameString = getString(qName);
 
     attrs = new AttributesImpl();    
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpStartElementFinalize() throws SAXException {
     contentHandler.startElement(elementUriString, elementLocalNameString, elementQNameString, attrs);
 
     attrs = null;
     
     elementUriString = null;
     elementLocalNameString = null;
     elementQNameString = null;
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpStartPrefixMapping(int prefix, int uri) throws SAXException {
     String prefixString = getString(prefix);
     String uriString = getString(uri);
     
     contentHandler.startPrefixMapping(prefixString, uriString);
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpString(int id, String value) throws SAXException {
     
     //
     // Store the string in the string table. If the table size is
     // unlimited, then it is only allowed to overwrite existing
     // entries or tack one on the end. If the table size is fixed
     // then writing anywhere in the string table is permitted (with
     // auto setting of null values for any intervening entries).
     //
     
     if (maxStringTableSize == BSAXConstants.UNLIMITED_STRING_TABLE_SIZE) {
       if (id < stringTable.size()) {
         stringTable.set(id, value);
       }
       else if (id == stringTable.size()) {
         stringTable.add(value);
       }
       else {
         throw new SAXException("Stream with unlimited string table size attempted to create string entry more than one position beyond the end of the string table");
       }
     }
     else {
       while (id >= stringTable.size()) {
         stringTable.add(null);
       }
       
       stringTable.set(id, value);
     }
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.xml.sax.XMLReader#getContentHandler()
    */
   public ContentHandler getContentHandler() {
     return contentHandler;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.xml.sax.XMLReader#getDTDHandler()
    */
   public DTDHandler getDTDHandler() {
     return dtdHandler;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.xml.sax.XMLReader#getEntityResolver()
    */
   public EntityResolver getEntityResolver() {
     return entityResolver;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.xml.sax.XMLReader#getErrorHandler()
    */
   public ErrorHandler getErrorHandler() {
     return errorHandler;
   }
   
   /**
    * This class doesn't support any SAX "features".
    * 
    * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
    */
   public boolean getFeature(String name) throws SAXNotRecognizedException,
   SAXNotSupportedException {
     throw new SAXNotRecognizedException("Feature '" + name + "'");
   }
   
   /**
    * This class doesn't support any SAX "properties".
    * 
    * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
    */
   public Object getProperty(String name) throws SAXNotRecognizedException,
   SAXNotSupportedException {
     throw new SAXNotRecognizedException("Property '" + name + "'");
   }
   
   /**
    * @param stream
    * @throws IOException
    * @throws SAXException
    */
   protected void doStartStream() {
     stringTable = new ArrayList(maxStringTableSize);
     
     stringTable.add(null); // Index zero
     stringTable.add(""); // Index one
   }
   
   
   /**
    * Reads a string from the stream by reading the string id (index) and then
    * looking that string up in the string table.
    * 
    * @return
    * @throws SAXException
    */
   private String getString(int id) throws SAXException {
     if (id == 0) {
       return null;
     } else if (id == 1) {
       return "";
     } else if ((maxStringTableSize != BSAXConstants.UNLIMITED_STRING_TABLE_SIZE)
         && (id >= maxStringTableSize)) {
       throw new SAXException(
       "Illegal reference to string index beyond the end of the fixed-size string table");
     } else if (id >= stringTable.size()) {
       throw new SAXException(
       "Illegal reference to string index beyond the current end of the variable-size string table");
     }
     
     return (String) stringTable.get(id);
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.xml.sax.XMLReader#setContentHandler(org.xml.sax.ContentHandler)
    */
   public void setContentHandler(ContentHandler handler) {
     this.contentHandler = handler;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
    */
   public void setDTDHandler(DTDHandler handler) {
     this.dtdHandler = handler;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
    */
   public void setEntityResolver(EntityResolver resolver) {
     this.entityResolver = resolver;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
    */
   public void setErrorHandler(ErrorHandler handler) {
     this.errorHandler = handler;
   }
   
   /**
    * This class doesn't support any SAX "features".
    * 
    * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
    */
   public void setFeature(String name, boolean value)
   throws SAXNotRecognizedException, SAXNotSupportedException {
     throw new SAXNotRecognizedException("Feature '" + name + "'");
   }
   
   /**
    * This class does't support any SAX "properties".
    * 
    * @see org.xml.sax.XMLReader#setProperty(java.lang.String, java.lang.Object)
    */
   public void setProperty(String name, Object value)
   throws SAXNotRecognizedException, SAXNotSupportedException {
     throw new SAXNotRecognizedException("Property '" + name + "'");
   }
   
 }
