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
 
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLEventFactory;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.*;
 import javax.xml.stream.util.XMLEventAllocator;
 import javax.xml.stream.util.XMLEventConsumer;
 import javax.xml.XMLConstants;
 import javax.xml.namespace.QName;
 
 /**
  * This class provides the same functionality as StAXEventAllocatorBase, but without
  * using EventFactory and creating a new object for each call.
  *
  */
 public class StAXEventAllocator implements XMLEventAllocator {
     StartElementEvent startElement = new StartElementEvent();
     EndElementEvent endElement = new EndElementEvent();
     CharactersEvent characters = new CharactersEvent();
     CharactersEvent cData = new CharactersEvent("",true);
     CharactersEvent space = new CharactersEvent();
     CommentEvent comment = new CommentEvent();
     EntityReferenceEvent entity = new EntityReferenceEvent();
     ProcessingInstructionEvent pi = new ProcessingInstructionEvent();
     StartDocumentEvent startDoc = new StartDocumentEvent();
     EndDocumentEvent endDoc = new EndDocumentEvent();
     DTDEvent dtd = new DTDEvent();
     
     /** Creates a new instance of StAXEventAllocator */
     public StAXEventAllocator() {
     }
     public XMLEventAllocator newInstance() {
         return new StAXEventAllocator();
     }
 
   /**
    * This method allocates an event given the current state of the XMLStreamReader.  
    * If this XMLEventAllocator does not have a one-to-one mapping between reader state
    * and events this method will return null.  
    * @param reader The XMLStreamReader to allocate from
    * @return the event corresponding to the current reader state
    */
     public XMLEvent allocate(XMLStreamReader streamReader) throws XMLStreamException {
         if(streamReader == null )
             throw new XMLStreamException("Reader cannot be null");
         return getXMLEvent(streamReader);
     }
     
   /**
    * This method allocates an event or set of events given the current state of 
    * the XMLStreamReader and adds the event or set of events to the consumer that 
    * was passed in.  
    * @param reader The XMLStreamReader to allocate from
    * @param consumer The XMLEventConsumer to add to.
    */
     public void allocate(XMLStreamReader streamReader, XMLEventConsumer consumer) throws XMLStreamException {
         consumer.add(getXMLEvent(streamReader));
 
     }
     // ---------------------end of methods defined by XMLEventAllocator-----------------//
     
     
     XMLEvent getXMLEvent(XMLStreamReader reader){
         EventBase event = null;
         int eventType = reader.getEventType();
         
         switch(eventType){
             
             case XMLEvent.START_ELEMENT:
             {
                 startElement.reset();
                 startElement.setName(new QName(reader.getNamespaceURI(),
                                    reader.getLocalName(), reader.getPrefix()));
 
                 addAttributes(startElement,reader);
                 addNamespaces(startElement, reader);
                 //need to fix it along with the Reader
                 //setNamespaceContext(startElement,reader);
                 event = startElement;
                 break;
             }
             case XMLEvent.END_ELEMENT:
             {
                 endElement.reset();
                 endElement.setName(new QName(reader.getNamespaceURI(),
                                  reader.getLocalName(),reader.getPrefix()));
                 addNamespaces(endElement,reader);
                 event = endElement ;
                 break;
             }
             case XMLEvent.PROCESSING_INSTRUCTION:
             {
                 pi.setTarget(reader.getPITarget());
                 pi.setData(reader.getPIData());
                 event = pi;
                 break;
             }
             case XMLEvent.CHARACTERS:
             {
                characters.setData(reader.getText()); 
                 if (reader.isWhiteSpace()) {
                     space.setData(reader.getText());
                     space.setSpace(true);
                     event = space;
                 }
                 else {
                     characters.setData(reader.getText()); 
                     event = characters;
                 }
                 break;
             }
             case XMLEvent.COMMENT:
             {
                 comment.setText(reader.getText());
                 event = comment;
                 break;
             }
             case XMLEvent.START_DOCUMENT:
             {
                 startDoc.reset();
                 String encoding = reader.getEncoding();
                 String version = reader.getVersion();
                 if (encoding != null)
                     startDoc.setEncoding(encoding);
                 if (version != null)
                     startDoc.setVersion(version);
                 startDoc.setStandalone(reader.isStandalone());
                 if(reader.getCharacterEncodingScheme() != null){
                     startDoc.setDeclaredEncoding(true);
                 }else{
                     startDoc.setDeclaredEncoding(false);
                 }
                 event = startDoc ;
                 break;
             }
             case XMLEvent.END_DOCUMENT:{
                 event = endDoc;
                 break;
             }
             case XMLEvent.ENTITY_REFERENCE:{
                 entity.setName(reader.getLocalName());
                 entity.setDeclaration(new EntityDeclarationImpl(reader.getLocalName(),reader.getText()));
                 event = entity;
                 break;
                 
             }
             case XMLEvent.ATTRIBUTE:{
                 event = null ;
                 break;
             }
             case XMLEvent.DTD:{
                 dtd.setDTD(reader.getText());
                 event = dtd;
                 break;
             }
             case XMLEvent.CDATA:{
                 cData.setData(reader.getText()); 
                 event = cData;
                 break;
             }
             case XMLEvent.SPACE:{
                 space.setData(reader.getText());
                 space.setSpace(true);
                 event = space;
                 break;
             }
         }
         event.setLocation(reader.getLocation());
         return event ;
     }
     
     //use event.addAttribute instead of addAttributes to avoid creating another list
     protected void addAttributes(StartElementEvent event,XMLStreamReader reader){        
         AttributeBase attr = null;
         for(int i=0; i<reader.getAttributeCount() ;i++){
             attr =  new AttributeBase(reader.getAttributeName(i), reader.getAttributeValue(i));
             attr.setAttributeType(reader.getAttributeType(i));
             attr.setSpecified(reader.isAttributeSpecified(i));
             event.addAttribute(attr);
         }
     }
     
     //add namespaces to StartElement/EndElement
     protected void addNamespaces(StartElementEvent event,XMLStreamReader reader){
         Namespace namespace = null;
         for(int i=0; i<reader.getNamespaceCount(); i++){
             namespace =  new NamespaceBase(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
             event.addNamespace(namespace);
         }
     }
     
     protected void addNamespaces(EndElementEvent event,XMLStreamReader reader){
         Namespace namespace = null;
         for(int i=0; i<reader.getNamespaceCount(); i++){
             namespace =  new NamespaceBase(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
             event.addNamespace(namespace);
         }
     }
         
 }
