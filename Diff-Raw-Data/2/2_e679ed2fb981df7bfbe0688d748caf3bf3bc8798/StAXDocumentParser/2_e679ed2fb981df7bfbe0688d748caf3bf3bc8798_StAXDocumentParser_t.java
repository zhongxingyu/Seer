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
 
 
 package com.sun.xml.fastinfoset.stax;
 
 import com.sun.xml.fastinfoset.Decoder;
 import com.sun.xml.fastinfoset.DecoderStateTables;
 import com.sun.xml.fastinfoset.EncodingConstants;
 import com.sun.xml.fastinfoset.QualifiedName;
 import com.sun.xml.fastinfoset.algorithm.BuiltInEncodingAlgorithmFactory;
 import com.sun.xml.fastinfoset.sax.AttributesHolder;
 import com.sun.xml.fastinfoset.util.CharArray;
 import com.sun.xml.fastinfoset.util.CharArrayString;
 import com.sun.xml.fastinfoset.util.XMLChar;
 import com.sun.xml.fastinfoset.util.EventLocation;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.EmptyStackException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Stack;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.namespace.QName;
 import javax.xml.stream.Location;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import org.jvnet.fastinfoset.EncodingAlgorithm;
 import org.jvnet.fastinfoset.EncodingAlgorithmException;
 import org.jvnet.fastinfoset.FastInfosetException;
 
 public class StAXDocumentParser extends Decoder implements XMLStreamReader {
     protected static final int INTERNAL_STATE_START_DOCUMENT = 0;
     protected static final int INTERNAL_STATE_START_ELEMENT_TERMINATE = 1;
     protected static final int INTERNAL_STATE_SINGLE_TERMINATE_ELEMENT_WITH_NAMESPACES = 2;
     protected static final int INTERNAL_STATE_DOUBLE_TERMINATE_ELEMENT = 3;
     protected static final int INTERNAL_STATE_END_DOCUMENT = 4;
     protected static final int INTERNAL_STATE_VOID = -1;
 
     protected int _internalState;
     
     /**
      * Current event
      */
     protected int _eventType;
     
     /**
      * Stack of qualified names and namespaces
      */
     protected QualifiedName[] _qNameStack = new QualifiedName[32];
     protected int[] _namespaceAIIsStartStack = new int[32];
     protected int[] _namespaceAIIsEndStack = new int[32];
     protected int _stackCount = -1;
     
     protected String[] _namespaceAIIsPrefix = new String[32];
     protected String[] _namespaceAIIsNamespaceName = new String[32];
     protected int _namespaceAIIsIndex;
     
     /**
      * Namespaces associated with START_ELEMENT or END_ELEMENT
      */
     protected int _currentNamespaceAIIsStart;
     protected int _currentNamespaceAIIsEnd;
     
     /**
      * Qualified name associated with START_ELEMENT or END_ELEMENT.
      */
     protected QualifiedName _qualifiedName;
     
     /**
      * List of attributes
      */
     protected AttributesHolder _attributes = new AttributesHolder();
 
     protected boolean _clearAttributes = false;
     
     /**
      * Characters associated with event.
      */
     protected char[] _characters;
     protected int _charactersOffset;
     protected int _charactersLength;
 
     protected String _algorithmURI;
     protected int _algorithmId;
     protected byte[] _algorithmData;
     protected int _algorithmDataOffset;
     protected int _algorithmDataLength;
     
     /**
      * State for processing instruction
      */
     protected String _piTarget;
     protected String _piData;
     
     /**
      * Mapping between prefixes and URIs.
      */
     protected Map _prefixMap = new HashMap();
     
     protected NamespaceContextImpl _nsContext = new NamespaceContextImpl();
     
     protected StAXManager _manager;
     
     public StAXDocumentParser() {
         reset();
         resetNamespaces();
     }
     
     public StAXDocumentParser(InputStream s) {
         this();
         setInputStream(s);
     }
     
     public StAXDocumentParser(InputStream s, StAXManager manager) {
         this(s);
         _manager = manager;
     }
     
     public void resetNamespaces() {
         _prefixMap.clear();
         _prefixMap.put("", "");
         _prefixMap.put(EncodingConstants.XML_NAMESPACE_PREFIX, EncodingConstants.XML_NAMESPACE_NAME);
     }
 
     public void setInputStream(InputStream s) {
         super.setInputStream(s);        
         reset();
     }
     
     public void reset() {
         super.reset();
         
         if (_internalState != INTERNAL_STATE_START_DOCUMENT &&
             _internalState != INTERNAL_STATE_END_DOCUMENT) {
             resetNamespaces();
         
             _stackCount = -1;
 
             _namespaceAIIsIndex = 0;
             _characters = null;
             _algorithmData = null;
         }
         
         _eventType = START_DOCUMENT;
         _internalState = INTERNAL_STATE_START_DOCUMENT;        
     }
     
     // -- XMLStreamReader Interface -------------------------------------------
     
     public Object getProperty(java.lang.String name)
             throws java.lang.IllegalArgumentException {
         if (_manager != null) {
             return _manager.getProperty(name);
         }
         return null;
     }
         
     public int next() throws XMLStreamException {
         try {
             if (_internalState != INTERNAL_STATE_VOID) {
                 switch (_internalState) {
                     case INTERNAL_STATE_START_DOCUMENT:
                         decodeHeader();
                         decodeDII();
                         _internalState = INTERNAL_STATE_VOID;
                         break;
                     case INTERNAL_STATE_START_ELEMENT_TERMINATE:
                         if (_currentNamespaceAIIsEnd > 0) {
                             for (int i = _currentNamespaceAIIsStart; i < _currentNamespaceAIIsEnd; i++) {
                                 popNamespaceDecl(_namespaceAIIsPrefix[i]);
                             }
                             _namespaceAIIsIndex = _currentNamespaceAIIsStart;
                         }
                         
                         // Pop information off the stack
                         _qualifiedName = _qNameStack[_stackCount];
                         _currentNamespaceAIIsStart = _namespaceAIIsStartStack[_stackCount];
                         _currentNamespaceAIIsEnd = _namespaceAIIsEndStack[_stackCount];
                         _qNameStack[_stackCount--] = null;
 
                         _internalState = INTERNAL_STATE_VOID;
                         return _eventType = END_ELEMENT;
                     case INTERNAL_STATE_SINGLE_TERMINATE_ELEMENT_WITH_NAMESPACES:
                         // Undeclare namespaces
                         for (int i = _currentNamespaceAIIsStart; i < _currentNamespaceAIIsEnd; i++) {
                             popNamespaceDecl(_namespaceAIIsPrefix[i]);
                         }
                         _namespaceAIIsIndex = _currentNamespaceAIIsStart;
                         _internalState = INTERNAL_STATE_VOID;
                         break;
                     case INTERNAL_STATE_DOUBLE_TERMINATE_ELEMENT:
                         if (_stackCount == -1) {
                             _internalState = INTERNAL_STATE_END_DOCUMENT;
                             return _eventType = END_DOCUMENT;
                         }
                         
                         // Undeclare namespaces
                         if (_currentNamespaceAIIsEnd > 0) {
                             for (int i = _currentNamespaceAIIsStart; i < _currentNamespaceAIIsEnd; i++) {
                                 popNamespaceDecl(_namespaceAIIsPrefix[i]);
                             }
                             _namespaceAIIsIndex = _currentNamespaceAIIsStart;
                         }
                     
                         // Pop information off the stack
                         _qualifiedName = _qNameStack[_stackCount];
                         _currentNamespaceAIIsStart = _namespaceAIIsStartStack[_stackCount];
                         _currentNamespaceAIIsEnd = _namespaceAIIsEndStack[_stackCount];
                         _qNameStack[_stackCount--] = null;
                         
                         _internalState = (_currentNamespaceAIIsEnd > 0) ? 
                                 INTERNAL_STATE_SINGLE_TERMINATE_ELEMENT_WITH_NAMESPACES :
                                 INTERNAL_STATE_VOID;                        
                         return _eventType = END_ELEMENT;
                     case INTERNAL_STATE_END_DOCUMENT:
                         throw new NoSuchElementException("No more events to report (EOF).");
                 }
             }
                         
             // Reset internal state
             _characters = null;
             _algorithmData = null;
             _currentNamespaceAIIsEnd = 0;
             
             // Process information item
             final int b = read();
             switch(DecoderStateTables.EII[b]) {
                 case DecoderStateTables.EII_NO_AIIS_INDEX_SMALL:
                     processEII(_v.elementName.get(b), false);
                     return _eventType;
                 case DecoderStateTables.EII_AIIS_INDEX_SMALL:
                     processEII(_v.elementName.get(b & EncodingConstants.INTEGER_3RD_BIT_SMALL_MASK), true);
                     return _eventType;
                 case DecoderStateTables.EII_INDEX_MEDIUM:
                 {
                     final int i = (((b & EncodingConstants.INTEGER_3RD_BIT_MEDIUM_MASK) << 8) | read())
                             + EncodingConstants.INTEGER_3RD_BIT_SMALL_LIMIT;
                     processEII(_v.elementName.get(i), (b & EncodingConstants.ELEMENT_ATTRIBUTE_FLAG) > 0);
                     return _eventType;
                 }
                 case DecoderStateTables.EII_INDEX_LARGE:
                 {
                     int i;
                     if ((b & 0x10) > 0) {
                         // EII large index
                         i = (((b & EncodingConstants.INTEGER_3RD_BIT_LARGE_MASK) << 16) | (read() << 8) | read())
                                 + EncodingConstants.INTEGER_3RD_BIT_MEDIUM_LIMIT;
                     } else {
                         // EII large large index
                         i = (((read() & EncodingConstants.INTEGER_3RD_BIT_LARGE_LARGE_MASK) << 16) | (read() << 8) | read())
                                 + EncodingConstants.INTEGER_3RD_BIT_LARGE_LIMIT;
                     }
                     processEII(_v.elementName.get(i), (b & EncodingConstants.ELEMENT_ATTRIBUTE_FLAG) > 0);
                     return _eventType;
                 }
                 case DecoderStateTables.EII_LITERAL:
                 {
                     final String prefix = ((b & EncodingConstants.LITERAL_QNAME_PREFIX_FLAG) > 0)
                     ? decodeIdentifyingNonEmptyStringIndexOnFirstBitAsPrefix(_v.prefix) : "";
                     final String namespaceName = ((b & EncodingConstants.LITERAL_QNAME_NAMESPACE_NAME_FLAG) > 0)
                     ? decodeIdentifyingNonEmptyStringIndexOnFirstBitAsNamespaceName(_v.namespaceName) : "";
                     final String localName = decodeIdentifyingNonEmptyStringOnFirstBit(_v.localName);
                     
                     final QualifiedName qualifiedName = new QualifiedName(prefix, namespaceName, localName, "");
                     _v.elementName.add(qualifiedName);
                     processEII(qualifiedName, (b & EncodingConstants.ELEMENT_ATTRIBUTE_FLAG) > 0);
                     return _eventType;
                 }
                 case DecoderStateTables.EII_NAMESPACES:
                     processEIIWithNamespaces((b & EncodingConstants.ELEMENT_ATTRIBUTE_FLAG) > 0);
                     return _eventType;
                 case DecoderStateTables.CII_UTF8_SMALL_LENGTH:
                     _octetBufferLength = (b & EncodingConstants.OCTET_STRING_LENGTH_7TH_BIT_SMALL_MASK)
                             + 1;
                     decodeUtf8StringAsCharBuffer();
                     if ((b & EncodingConstants.CHARACTER_CHUNK_ADD_TO_TABLE_FLAG) > 0) {
                         _v.characterContentChunk.add(_charBuffer, _charBufferLength);
                     }
                     
                     _characters = _charBuffer;
                     _charactersOffset = 0;
                     _charactersLength = _charBufferLength;
                     return _eventType = CHARACTERS;
                 case DecoderStateTables.CII_UTF8_MEDIUM_LENGTH:
                     _octetBufferLength = read() + EncodingConstants.OCTET_STRING_LENGTH_7TH_BIT_SMALL_LIMIT;
                     decodeUtf8StringAsCharBuffer();
                     if ((b & EncodingConstants.CHARACTER_CHUNK_ADD_TO_TABLE_FLAG) > 0) {
                         _v.characterContentChunk.add(_charBuffer, _charBufferLength);
                     }
                     
                     _characters = _charBuffer;
                     _charactersOffset = 0;
                     _charactersLength = _charBufferLength;
                     return _eventType = CHARACTERS;
                 case DecoderStateTables.CII_UTF8_LARGE_LENGTH:
                     _octetBufferLength = (read() << 24) |
                             (read() << 16) |
                             (read() << 8) |
                             read();
                     _octetBufferLength += EncodingConstants.OCTET_STRING_LENGTH_7TH_BIT_MEDIUM_LIMIT;
                     decodeUtf8StringAsCharBuffer();
                     if ((b & EncodingConstants.CHARACTER_CHUNK_ADD_TO_TABLE_FLAG) > 0) {
                         _v.characterContentChunk.add(_charBuffer, _charBufferLength);
                     }
                     
                     _characters = _charBuffer;
                     _charactersOffset = 0;
                     _charactersLength = _charBufferLength;
                     return _eventType = CHARACTERS;
                 case DecoderStateTables.CII_UTF16_SMALL_LENGTH:
                     _octetBufferLength = (b & EncodingConstants.OCTET_STRING_LENGTH_7TH_BIT_SMALL_MASK)
                     + 1;
                     decodeUtf16StringAsCharBuffer();
                     if ((b & EncodingConstants.CHARACTER_CHUNK_ADD_TO_TABLE_FLAG) > 0) {
                         _v.characterContentChunk.add(_charBuffer, _charBufferLength);
                     }
                     
                     _characters = _charBuffer;
                     _charactersOffset = 0;
                     _charactersLength = _charBufferLength;
                     return _eventType = CHARACTERS;
                 case DecoderStateTables.CII_UTF16_MEDIUM_LENGTH:
                     _octetBufferLength = read() + EncodingConstants.OCTET_STRING_LENGTH_7TH_BIT_SMALL_LIMIT;
                     decodeUtf16StringAsCharBuffer();
                     if ((b & EncodingConstants.CHARACTER_CHUNK_ADD_TO_TABLE_FLAG) > 0) {
                         _v.characterContentChunk.add(_charBuffer, _charBufferLength);
                     }
                     
                     _characters = _charBuffer;
                     _charactersOffset = 0;
                     _charactersLength = _charBufferLength;
                     return _eventType = CHARACTERS;
                 case DecoderStateTables.CII_UTF16_LARGE_LENGTH:
                     _octetBufferLength = (read() << 24) |
                             (read() << 16) |
                             (read() << 8) |
                             read();
                     _octetBufferLength += EncodingConstants.OCTET_STRING_LENGTH_7TH_BIT_MEDIUM_LIMIT;
                     decodeUtf16StringAsCharBuffer();
                     if ((b & EncodingConstants.CHARACTER_CHUNK_ADD_TO_TABLE_FLAG) > 0) {
                         _v.characterContentChunk.add(_charBuffer, _charBufferLength);
                     }
                     
                     _characters = _charBuffer;
                     _charactersOffset = 0;
                     _charactersLength = _charBufferLength;
                     return _eventType = CHARACTERS;
                 case DecoderStateTables.CII_RA:
                 {
                     // Decode resitricted alphabet integer
                     _identifier = (b & 0x02) << 6;
                     final int b2 = read();
                     _identifier |= (b2 & 0xFC) >> 2;
                     
                     decodeOctetsOnSeventhBitOfNonIdentifyingStringOnThirdBit(b2);
                     // TODO obtain restricted alphabet given _identifier value
                     decodeRAOctetsAsCharBuffer(null);
                     if ((b & EncodingConstants.CHARACTER_CHUNK_ADD_TO_TABLE_FLAG) > 0) {
                         _v.characterContentChunk.add(_charBuffer, _charBufferLength);
                     }
                     
                     _characters = _charBuffer;
                     _charactersOffset = 0;
                     _charactersLength = _charBufferLength;
                     return _eventType = CHARACTERS;
                 }
                 case DecoderStateTables.CII_EA:
                 {
                     if ((b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0) {
                         throw new EncodingAlgorithmException("Add to table not supported for Encoding algorithms");
                     }
                     
                     // Decode encoding algorithm integer
                     _algorithmId = (b & 0x02) << 6;
                     final int b2 = read();
                     _algorithmId |= (b2 & 0xFC) >> 2;
                     
                     decodeOctetsOnSeventhBitOfNonIdentifyingStringOnThirdBit(b2);                    
                     processCIIEncodingAlgorithm();
                     
                     return _eventType = CHARACTERS;
                 }
                 case DecoderStateTables.CII_INDEX_SMALL:
                 {
                     final CharArray ca = _v.characterContentChunk.get(b & EncodingConstants.INTEGER_4TH_BIT_SMALL_MASK);
                     
                     _characters = ca.ch;
                     _charactersOffset = ca.start;
                     _charactersLength = ca.length;
                     return _eventType = CHARACTERS;
                 }
                 case DecoderStateTables.CII_INDEX_MEDIUM:
                 {
                     final int index = (((b & EncodingConstants.INTEGER_4TH_BIT_MEDIUM_MASK) << 8) | read())
                     + EncodingConstants.INTEGER_4TH_BIT_SMALL_LIMIT;
                     final CharArray ca = _v.characterContentChunk.get(index);
                     
                     _characters = ca.ch;
                     _charactersOffset = ca.start;
                     _charactersLength = ca.length;
                     return _eventType = CHARACTERS;
                 }
                 case DecoderStateTables.CII_INDEX_LARGE:
                 {
                     int index = ((b & EncodingConstants.INTEGER_4TH_BIT_LARGE_MASK) << 16) |
                             (read() << 8) |
                             read();
                     index += EncodingConstants.INTEGER_4TH_BIT_MEDIUM_LIMIT;
                     final CharArray ca = _v.characterContentChunk.get(index);
                     
                     _characters = ca.ch;
                     _charactersOffset = ca.start;
                     _charactersLength = ca.length;
                     return _eventType = CHARACTERS;
                 }
                 case DecoderStateTables.CII_INDEX_LARGE_LARGE:
                 {
                     int index = (read() << 16) |
                             (read() << 8) |
                             read();
                     index += EncodingConstants.INTEGER_4TH_BIT_LARGE_LIMIT;
                     final CharArray ca = _v.characterContentChunk.get(index);
                     
                     _characters = ca.ch;
                     _charactersOffset = ca.start;
                     _charactersLength = ca.length;
                     return _eventType = CHARACTERS;
                 }
                 case DecoderStateTables.COMMENT_II:
                     processCommentII();
                     return _eventType;
                 case DecoderStateTables.PROCESSING_INSTRUCTION_II:
                     processProcessingII();
                     return _eventType;
                 case DecoderStateTables.UNEXPANDED_ENTITY_REFERENCE_II:
                 {
                     /*
                      * TODO
                      * How does StAX report such events?
                      */
                     String entity_reference_name = decodeIdentifyingNonEmptyStringOnFirstBit(_v.otherNCName);
                     
                     String system_identifier = ((b & EncodingConstants.UNEXPANDED_ENTITY_SYSTEM_IDENTIFIER_FLAG) > 0)
                     ? decodeIdentifyingNonEmptyStringOnFirstBit(_v.otherURI) : "";
                     String public_identifier = ((b & EncodingConstants.UNEXPANDED_ENTITY_PUBLIC_IDENTIFIER_FLAG) > 0)
                     ? decodeIdentifyingNonEmptyStringOnFirstBit(_v.otherURI) : "";
                     return _eventType;
                 }
                 case DecoderStateTables.TERMINATOR_DOUBLE:
                     if (_stackCount != -1) {
                         // Pop information off the stack
                         _qualifiedName = _qNameStack[_stackCount];
                         _currentNamespaceAIIsStart = _namespaceAIIsStartStack[_stackCount];
                         _currentNamespaceAIIsEnd = _namespaceAIIsEndStack[_stackCount];
                         _qNameStack[_stackCount--] = null;
 
                         _internalState = INTERNAL_STATE_DOUBLE_TERMINATE_ELEMENT;
                         return _eventType = END_ELEMENT;
                     } 
                      
                     _internalState = INTERNAL_STATE_END_DOCUMENT;
                     return _eventType = END_DOCUMENT;
                 case DecoderStateTables.TERMINATOR_SINGLE:
                     if (_stackCount != -1) {
                         // Pop information off the stack
                         _qualifiedName = _qNameStack[_stackCount];
                         _currentNamespaceAIIsStart = _namespaceAIIsStartStack[_stackCount];
                         _currentNamespaceAIIsEnd = _namespaceAIIsEndStack[_stackCount];
                         _qNameStack[_stackCount--] = null;
 
                         if (_currentNamespaceAIIsEnd > 0) {
                             _internalState = INTERNAL_STATE_SINGLE_TERMINATE_ELEMENT_WITH_NAMESPACES;
                         }
                         return _eventType = END_ELEMENT;
                     }
                     
                     _internalState = INTERNAL_STATE_END_DOCUMENT;
                     return _eventType = END_DOCUMENT;
                 default:
                     throw new FastInfosetException("Illegal state when decoding a child of an EII");
             }
         } catch (IOException e) {
             reset();
             e.printStackTrace();
             throw new XMLStreamException(e);
         } catch (FastInfosetException e) {
             reset();
             e.printStackTrace();
             throw new XMLStreamException(e);
         } catch (RuntimeException e) {
             reset();
             e.printStackTrace();
             throw e;
         }
     }
     
     /** Test if the current event is of the given type and if the namespace and name match the current namespace and name of the current event.
      * If the namespaceURI is null it is not checked for equality, if the localName is null it is not checked for equality.
      * @param type the event type
      * @param namespaceURI the uri of the event, may be null
      * @param localName the localName of the event, may be null
      * @throws XMLStreamException if the required values are not matched.
      */
     public final void require(int type, String namespaceURI, String localName)
     throws XMLStreamException {
         if( type != _eventType)
             throw new XMLStreamException("Event type " +getEventTypeString(type)+" specified did not match with current parser event");
         if( namespaceURI != null && !namespaceURI.equals(getNamespaceURI()) )
             throw new XMLStreamException("Namespace URI " +namespaceURI+" specified did not match with current namespace URI");
         if(localName != null && !localName.equals(getLocalName()))
             throw new XMLStreamException("LocalName " +localName+" specified did not match with current local name");
         
         return;
     }
     
     /** Reads the content of a text-only element. Precondition:
      * the current event is START_ELEMENT. Postcondition:
      * The current event is the corresponding END_ELEMENT.
      * @throws XMLStreamException if the current event is not a START_ELEMENT or if
      * a non text element is encountered
      */
     public final String getElementText() throws XMLStreamException {
         
         if(getEventType() != START_ELEMENT) {
             throw new XMLStreamException(
                     "parser must be on START_ELEMENT to read next text", getLocation());
         }
         //current is StartElement, move to the next
         int eventType = next();
         return getElementText(true);
     }
     /**
      * @param startElementRead flag if start element has already been read
      */
     public final String getElementText(boolean startElementRead) throws XMLStreamException {
         if (!startElementRead) {
             throw new XMLStreamException(
                     "parser must be on START_ELEMENT to read next text", getLocation());
         }
         int eventType = getEventType();
         StringBuffer content = new StringBuffer();
         while(eventType != END_ELEMENT ) {
             if(eventType == CHARACTERS
                     || eventType == CDATA
                     || eventType == SPACE
                     || eventType == ENTITY_REFERENCE) {
                 content.append(getText());
             } else if(eventType == PROCESSING_INSTRUCTION
                     || eventType == COMMENT) {
                 // skipping
             } else if(eventType == END_DOCUMENT) {
                 throw new XMLStreamException("unexpected end of document when reading element text content");
             } else if(eventType == START_ELEMENT) {
                 throw new XMLStreamException(
                         "getElementText() function expects text only elment but START_ELEMENT was encountered.", getLocation());
             } else {
                 throw new XMLStreamException(
                         "Unexpected event type "+ getEventTypeString(eventType), getLocation());
             }
             eventType = next();
         }
         return content.toString();
     }
     
     /** Skips any white space (isWhiteSpace() returns true), COMMENT,
      * or PROCESSING_INSTRUCTION,
      * until a START_ELEMENT or END_ELEMENT is reached.
      * If other than white space characters, COMMENT, PROCESSING_INSTRUCTION, START_ELEMENT, END_ELEMENT
      * are encountered, an exception is thrown. This method should
      * be used when processing element-only content seperated by white space.
      * This method should
      * be used when processing element-only content because
      * the parser is not able to recognize ignorable whitespace if
      * then DTD is missing or not interpreted.
      * @return the event type of the element read
      * @throws XMLStreamException if the current event is not white space
      */
     public final int nextTag() throws XMLStreamException {
         int eventType = next();
         return nextTag(true);
     }
     /** if the current tag has already read, such as in the case EventReader's
      * peek() has been called, the current cursor should not move before the loop
      */
     public final int nextTag(boolean currentTagRead) throws XMLStreamException {
         int eventType = getEventType();
         if (!currentTagRead) {
             eventType = next();
         }
         while((eventType == CHARACTERS && isWhiteSpace()) // skip whitespace
         || (eventType == CDATA && isWhiteSpace())
         || eventType == SPACE
                 || eventType == PROCESSING_INSTRUCTION
                 || eventType == COMMENT) {
             eventType = next();
         }
         if (eventType != START_ELEMENT && eventType != END_ELEMENT) {
             throw new XMLStreamException("expected start or end tag", getLocation());
         }
         return eventType;
     }
     
     public final boolean hasNext() throws XMLStreamException {
         return (_eventType != END_DOCUMENT);
     }
         
     public void close() throws XMLStreamException {
     }
     
     public final String getNamespaceURI(String prefix) {
         String namespace = getNamespaceDecl(prefix);
         if (namespace == null) {
             if (prefix == null) {
                 throw new IllegalArgumentException("Prefix cannot be null.");
             }
             return null;  // unbound
         }
         return namespace;
     }
     
     public final boolean isStartElement() {
         return (_eventType == START_ELEMENT);
     }
     
     public final boolean isEndElement() {
         return (_eventType == END_ELEMENT);
     }
     
     public final boolean isCharacters() {
         return (_eventType == CHARACTERS);
     }
     
     /**
      *  Returns true if the cursor points to a character data event that consists of all whitespace
      *  Application calling this method needs to cache the value and avoid calling this method again
      *  for the same event.
      * @return true if the cursor points to all whitespace, false otherwise
      */
     public final boolean isWhiteSpace() {
         if(isCharacters() || (_eventType == CDATA)){
             char [] ch = this.getTextCharacters();
             int start = this.getTextStart();
             int length = this.getTextLength();
             for (int i=start; i< length;i++){
                 if(!XMLChar.isSpace(ch[i])){
                     return false;
                 }
             }
             return true;
         }
         return false;
         //throw new UnsupportedOperationException("Not implemented");
     }
     
     public final String getAttributeValue(String namespaceURI, String localName) {
         if (_eventType != START_ELEMENT) {
             throw new IllegalStateException("Method getAttributeValue() called in invalid state");
         }
         
         // Search for the attributes in _attributes
         for (int i = 0; i < _attributes.getLength(); i++) {
             if (_attributes.getLocalName(i) == localName &&
                     _attributes.getURI(i) == namespaceURI) {
                 return _attributes.getValue(i);
             }
         }
         return null;
     }
     
     public final int getAttributeCount() {
         if (_eventType != START_ELEMENT) {
             throw new IllegalStateException("Method getAttributeValue() called in invalid state");
         }
         
         return _attributes.getLength();
     }
     
     public final javax.xml.namespace.QName getAttributeName(int index) {
         if (_eventType != START_ELEMENT) {
             throw new IllegalStateException("Method getAttributeValue() called in invalid state");
         }
         return _attributes.getQualifiedName(index).getQName();
     }
     
     public final String getAttributeNamespace(int index) {
         if (_eventType != START_ELEMENT) {
             throw new IllegalStateException("Method getAttributeValue() called in invalid state");
         }
         
         return _attributes.getURI(index);
     }
     
     public final String getAttributeLocalName(int index) {
         if (_eventType != START_ELEMENT) {
             throw new IllegalStateException("Method getAttributeValue() called in invalid state");
         }
         return _attributes.getLocalName(index);
     }
     
     public final String getAttributePrefix(int index) {
         if (_eventType != START_ELEMENT) {
             throw new IllegalStateException("Method getAttributeValue() called in invalid state");
         }
         return _attributes.getPrefix(index);
     }
     
     public final String getAttributeType(int index) {
         if (_eventType != START_ELEMENT) {
             throw new IllegalStateException("Method getAttributeValue() called in invalid state");
         }
         return _attributes.getType(index);
     }
     
     public final String getAttributeValue(int index) {
         if (_eventType != START_ELEMENT) {
             throw new IllegalStateException("Method getAttributeValue() called in invalid state");
         }
         return _attributes.getValue(index);
     }
     
     public final boolean isAttributeSpecified(int index) {
         return false;   // non-validating parser
     }
     
     public final int getNamespaceCount() {
         if (_eventType == START_ELEMENT || _eventType == END_ELEMENT) {
             return (_currentNamespaceAIIsEnd > 0) ? (_currentNamespaceAIIsEnd - _currentNamespaceAIIsStart) : 0;
         } else {
             throw new IllegalStateException("Method getNamespaceCount() called in invalid state");
         }
     }
     
     public final String getNamespacePrefix(int index) {
         if (_eventType == START_ELEMENT || _eventType == END_ELEMENT) {
             return _namespaceAIIsPrefix[_currentNamespaceAIIsStart + index];
         } else {
             throw new IllegalStateException("Method getNamespacePrefix() called in invalid state");
         }
     }
     
     public final String getNamespaceURI(int index) {
         if (_eventType == START_ELEMENT || _eventType == END_ELEMENT) {
             return _namespaceAIIsNamespaceName[_currentNamespaceAIIsStart + index];
         } else {
             throw new IllegalStateException("Method getNamespacePrefix() called in invalid state");
         }
     }
     
     public final NamespaceContext getNamespaceContext() {
         return _nsContext;
     }
     
     public final int getEventType() {
         return _eventType;
     }
     
     public final String getText() {
         if (_characters == null) {
             checkTextState();
         }
         
         return new String(_characters,
                 _charactersOffset,
                 _charactersLength);
     }
     
     public final char[] getTextCharacters() {
         if (_characters == null) {
             checkTextState();
         }
         
         return _characters;
     }
     
     public final int getTextStart() {
         if (_characters == null) {
             checkTextState();
         }
                 
         return _charactersOffset;
     }
     
     public final int getTextLength() {
         if (_characters == null) {
             checkTextState();
         }
         
         return _charactersLength;
     }
     
     public final int getTextCharacters(int sourceStart, char[] target,
             int targetStart, int length) throws XMLStreamException {
         if (_characters == null) {
             checkTextState();
         }
         
         try {
             System.arraycopy(_characters, sourceStart, target,
                     targetStart, length);
             return length;
         } catch (IndexOutOfBoundsException e) {
             throw new XMLStreamException(e);
         }
     }
     
     protected final void checkTextState() {
         if (_algorithmData == null) {
             throw new IllegalStateException("Invalid state for text");
         }
         
         try {
             convertEncodingAlgorithmDataToCharacters();
         } catch (Exception e) {
             throw new IllegalStateException("Invalid state for text");
         }
     }
     
     public final String getEncoding() {
         return "UTF-8";     // for now
     }
     
     public final boolean hasText() {
         return (_characters != null);
     }
     
     public final Location getLocation() {
         //location should be created in next()
         //returns a nil location for now
         return EventLocation.getNilLocation();
     }
     
     public final QName getName() {
         if (_eventType == START_ELEMENT || _eventType == END_ELEMENT) {
             return _qualifiedName.getQName();
         } else {
             throw new IllegalStateException("Method getName() called in invalid state");
         }
     }
     
     public final String getLocalName() {
         if (_eventType == START_ELEMENT || _eventType == END_ELEMENT) {
             return _qualifiedName.localName;
         } else {
             throw new IllegalStateException("Method getLocalName() called in invalid state");
         }
     }
     
     public final boolean hasName() {
         return (_eventType == START_ELEMENT || _eventType == END_ELEMENT);
     }
     
     public final String getNamespaceURI() {
         if (_eventType == START_ELEMENT || _eventType == END_ELEMENT) {
             return _qualifiedName.namespaceName;
         } else {
             throw new IllegalStateException("Method getNamespaceURI() called in invalid state");
         }
     }
     
     public final String getPrefix() {
         if (_eventType == START_ELEMENT || _eventType == END_ELEMENT) {
             return _qualifiedName.prefix;
         } else {
             throw new IllegalStateException("Method getPrefix() called in invalid state");
         }
     }
     
     public final String getVersion() {
         return null;
     }
     
     public final boolean isStandalone() {
         return false;
     }
     
     public final boolean standaloneSet() {
         return false;
     }
     
     public final String getCharacterEncodingScheme() {
         return null;
     }
     
     public final String getPITarget() {
         if (_eventType != PROCESSING_INSTRUCTION) {
             throw new IllegalStateException("Method getPITarget() called in invalid state");
         }
         
         return _piTarget;
     }
     
     public final String getPIData() {
         if (_eventType != PROCESSING_INSTRUCTION) {
             throw new IllegalStateException("Method getPIData() called in invalid state");
         }
         
         return _piData;
     }
     
 
     
     
     
     public final String getTextAlgorithmURI() {
         return _algorithmURI;
     }
  
     public final int getTextAlgorithmIndex() {
         return _algorithmId;
     }
     
     public final byte[] getTextAlgorithmBytes() {
         return _algorithmData;
     }
     
     public final byte[] getTextAlgorithmBytesClone() {
         if (_algorithmData == null) {
             return null;
         }
         
         byte[] algorithmData = new byte[_algorithmDataLength];
         System.arraycopy(_algorithmData, _algorithmDataOffset, algorithmData, 0, _algorithmDataLength);
         return algorithmData;
     }
     
     public final int getTextAlgorithmStart() {
         return _algorithmDataOffset;
     }
     
     public final int getTextAlgorithmLength() {
         return _algorithmDataLength;
     }
     
     public final int getTextAlgorithmBytes(int sourceStart, byte[] target,
             int targetStart, int length) throws XMLStreamException {        
         try {
             System.arraycopy(_algorithmData, sourceStart, target,
                     targetStart, length);
             return length;
         } catch (IndexOutOfBoundsException e) {
             throw new XMLStreamException(e);
         }
     }
     
 
     
     //
     
     protected final void processEIIWithNamespaces(boolean hasAttributes) throws FastInfosetException, IOException {
         
         _currentNamespaceAIIsStart = _namespaceAIIsIndex;
         int b = read();
         while ((b & EncodingConstants.NAMESPACE_ATTRIBUTE_MASK) == EncodingConstants.NAMESPACE_ATTRIBUTE) {
             // NOTE a prefix without a namespace name is an undeclaration
             // of the namespace bound to the prefix
             // TODO need to investigate how the startPrefixMapping works in
             // relation to undeclaration
             
             if (_namespaceAIIsIndex == _namespaceAIIsPrefix.length) {
                 final String[] namespaceAIIsPrefix = new String[_namespaceAIIsIndex * 2];
                 System.arraycopy(_namespaceAIIsPrefix, 0, namespaceAIIsPrefix, 0, _namespaceAIIsIndex);
                 _namespaceAIIsPrefix = namespaceAIIsPrefix;
                 
                 final String[] namespaceAIIsNamespaceName = new String[_namespaceAIIsIndex * 2];
                 System.arraycopy(_namespaceAIIsNamespaceName, 0, namespaceAIIsNamespaceName, 0, _namespaceAIIsIndex);
                 _namespaceAIIsNamespaceName = namespaceAIIsNamespaceName;
             }
             
             // Prefix
             final String prefix = _namespaceAIIsPrefix[_namespaceAIIsIndex] = ((b & EncodingConstants.NAMESPACE_ATTRIBUTE_PREFIX_FLAG) > 0)
             ? decodeIdentifyingNonEmptyStringOnFirstBitAsPrefix(_v.prefix) : "";
             
             // Namespace name
             final String namespaceName = _namespaceAIIsNamespaceName[_namespaceAIIsIndex++] = ((b & EncodingConstants.NAMESPACE_ATTRIBUTE_NAME_FLAG) > 0)
             ? decodeIdentifyingNonEmptyStringOnFirstBitAsNamespaceName(_v.namespaceName) : "";
             
             // Push namespace declarations onto the stack
             pushNamespaceDecl(prefix, namespaceName);
             
             b = read();
         }
         if (b != EncodingConstants.TERMINATOR) {
             throw new FastInfosetException("Namespace names of EII not terminated correctly");
         }
         _currentNamespaceAIIsEnd = _namespaceAIIsIndex;
         
         b = read();
         switch(DecoderStateTables.EII[b]) {
             case DecoderStateTables.EII_NO_AIIS_INDEX_SMALL:
                 processEII(_v.elementName.get(b), hasAttributes);
                 break;
             case DecoderStateTables.EII_INDEX_MEDIUM:
             {
                 final int i = (((b & EncodingConstants.INTEGER_3RD_BIT_MEDIUM_MASK) << 8) | read())
                 + EncodingConstants.INTEGER_3RD_BIT_SMALL_LIMIT;
                 processEII(_v.elementName.get(i), hasAttributes);
                 break;
             }
             case DecoderStateTables.EII_INDEX_LARGE:
             {
                 int i;
                 if ((b & 0x10) > 0) {
                     // EII large index
                     i = (((b & EncodingConstants.INTEGER_3RD_BIT_LARGE_MASK) << 16) | (read() << 8) | read())
                     + EncodingConstants.INTEGER_3RD_BIT_MEDIUM_LIMIT;
                 } else {
                     // EII large large index
                     i = (((read() & EncodingConstants.INTEGER_3RD_BIT_LARGE_LARGE_MASK) << 16) | (read() << 8) | read())
                     + EncodingConstants.INTEGER_3RD_BIT_LARGE_LIMIT;
                 }
                 processEII(_v.elementName.get(i), hasAttributes);
                 break;
             }
             case DecoderStateTables.EII_LITERAL:
             {
                 final String prefix = ((b & EncodingConstants.LITERAL_QNAME_PREFIX_FLAG) > 0)
                 ? decodeIdentifyingNonEmptyStringIndexOnFirstBitAsPrefix(_v.prefix) : "";
                 final String namespaceName = ((b & EncodingConstants.LITERAL_QNAME_NAMESPACE_NAME_FLAG) > 0)
                 ? decodeIdentifyingNonEmptyStringIndexOnFirstBitAsNamespaceName(_v.namespaceName) : "";
                 final String localName = decodeIdentifyingNonEmptyStringOnFirstBit(_v.localName);
                 
                 final QualifiedName qualifiedName = new QualifiedName(prefix, namespaceName, localName, "");
                 _v.elementName.add(qualifiedName);
                 processEII(qualifiedName, hasAttributes);
                 break;
             }
             default:
                 throw new FastInfosetException("Illegal state when decoding EII after the namespace AIIs");
         }
     }
     
     protected final void processEII(QualifiedName name, boolean hasAttributes) throws FastInfosetException, IOException {
         _eventType = START_ELEMENT;
         _qualifiedName = name;
 
         if (_clearAttributes) {
             _attributes.clear();
             _clearAttributes = false;
         }
         
         if (hasAttributes) {
             processAIIs();
         }
         
         // Push element holder onto the stack
         _stackCount++;
         if (_stackCount == _qNameStack.length) {
             QualifiedName[] qNameStack = new QualifiedName[_qNameStack.length * 2];
             System.arraycopy(_qNameStack, 0, qNameStack, 0, _qNameStack.length);
             _qNameStack = qNameStack;
             
             int[] namespaceAIIsStartStack = new int[_namespaceAIIsStartStack.length * 2];
             System.arraycopy(_namespaceAIIsStartStack, 0, namespaceAIIsStartStack, 0, _namespaceAIIsStartStack.length);
             _namespaceAIIsStartStack = namespaceAIIsStartStack;
             
             int[] namespaceAIIsEndStack = new int[_namespaceAIIsEndStack.length * 2];
             System.arraycopy(_namespaceAIIsEndStack, 0, namespaceAIIsEndStack, 0, _namespaceAIIsEndStack.length);
             _namespaceAIIsEndStack = namespaceAIIsEndStack;
         }
         _qNameStack[_stackCount] = _qualifiedName;
         _namespaceAIIsStartStack[_stackCount] = _currentNamespaceAIIsStart;
         _namespaceAIIsEndStack[_stackCount] = _currentNamespaceAIIsEnd;        
     }
     
     protected final void processAIIs() throws FastInfosetException, IOException {
         QualifiedName name;
         int b;
         String value;
         
         _clearAttributes = true;
         
         boolean terminate = false;
         do {
             // AII qualified name
             b = read();
             switch (DecoderStateTables.AII[b]) {
                 case DecoderStateTables.AII_INDEX_SMALL:
                     name = _v.attributeName.get(b);
                     break;
                 case DecoderStateTables.AII_INDEX_MEDIUM:
                 {
                     final int i = (((b & EncodingConstants.INTEGER_2ND_BIT_MEDIUM_MASK) << 8) | read())
                     + EncodingConstants.INTEGER_2ND_BIT_SMALL_LIMIT;
                     name = _v.attributeName.get(i);
                     break;
                 }
                 case DecoderStateTables.AII_INDEX_LARGE:
                 {
                     final int i = (((b & EncodingConstants.INTEGER_2ND_BIT_LARGE_MASK) << 16) | (read() << 8) | read())
                     + EncodingConstants.INTEGER_2ND_BIT_MEDIUM_LIMIT;
                     name = _v.attributeName.get(i);
                     break;
                 }
                 case DecoderStateTables.AII_LITERAL:
                 {
                     final String prefix = ((b & EncodingConstants.LITERAL_QNAME_PREFIX_FLAG) > 0)
                     ? decodeIdentifyingNonEmptyStringIndexOnFirstBitAsPrefix(_v.prefix) : "";
                     final String namespaceName = ((b & EncodingConstants.LITERAL_QNAME_NAMESPACE_NAME_FLAG) > 0)
                     ? decodeIdentifyingNonEmptyStringIndexOnFirstBitAsNamespaceName(_v.namespaceName) : "";
                     final String localName = decodeIdentifyingNonEmptyStringOnFirstBit(_v.localName);
                     
                     name = new QualifiedName(prefix, namespaceName, localName, "");
                     _v.attributeName.add(name);
                     break;
                 }
                 case DecoderStateTables.AII_TERMINATOR_DOUBLE:
                     _internalState = INTERNAL_STATE_START_ELEMENT_TERMINATE;
                 case DecoderStateTables.AII_TERMINATOR_SINGLE:
                     terminate = true;
                     // AIIs have finished break out of loop
                     continue;
                 default:
                     throw new FastInfosetException("Illegal state when decoding AIIs");
             }
             
             // [normalized value] of AII
             
             b = read();
             switch(DecoderStateTables.NISTRING[b]) {
                 case DecoderStateTables.NISTRING_UTF8_SMALL_LENGTH:
                 {
                     final boolean addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                     _octetBufferLength = (b & EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_SMALL_MASK) + 1;
                     value = decodeUtf8StringAsString();
                     if (addToTable) {
                         _v.attributeValue.add(value);
                     }
                     
                     _attributes.addAttribute(name, value);
                     break;
                 }
                 case DecoderStateTables.NISTRING_UTF8_MEDIUM_LENGTH:
                 {
                     final boolean addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                     _octetBufferLength = read() + EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_SMALL_LIMIT;
                     value = decodeUtf8StringAsString();
                     if (addToTable) {
                         _v.attributeValue.add(value);
                     }
                     
                     _attributes.addAttribute(name, value);
                     break;
                 }
                 case DecoderStateTables.NISTRING_UTF8_LARGE_LENGTH:
                 {
                     final boolean addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                     final int length = (read() << 24) |
                             (read() << 16) |
                             (read() << 8) |
                             read();
                     _octetBufferLength = length + EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_MEDIUM_LIMIT;
                     value = decodeUtf8StringAsString();
                     if (addToTable) {
                         _v.attributeValue.add(value);
                     }
                     
                     _attributes.addAttribute(name, value);
                     break;
                 }
                 case DecoderStateTables.NISTRING_UTF16_SMALL_LENGTH:
                 {
                     final boolean addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                     _octetBufferLength = (b & EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_SMALL_MASK) + 1;
                     value = decodeUtf16StringAsString();
                     if (addToTable) {
                         _v.attributeValue.add(value);
                     }
                     
                     _attributes.addAttribute(name, value);
                     break;
                 }
                 case DecoderStateTables.NISTRING_UTF16_MEDIUM_LENGTH:
                 {
                     final boolean addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                     _octetBufferLength = read() + EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_SMALL_LIMIT;
                     value = decodeUtf16StringAsString();
                     if (addToTable) {
                         _v.attributeValue.add(value);
                     }
                     
                     _attributes.addAttribute(name, value);
                     break;
                 }
                 case DecoderStateTables.NISTRING_UTF16_LARGE_LENGTH:
                 {
                     final boolean addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                     final int length = (read() << 24) |
                             (read() << 16) |
                             (read() << 8) |
                             read();
                     _octetBufferLength = length + EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_MEDIUM_LIMIT;
                     value = decodeUtf16StringAsString();
                     if (addToTable) {
                         _v.attributeValue.add(value);
                     }
                     
                     _attributes.addAttribute(name, value);
                     break;
                 }
                 case DecoderStateTables.NISTRING_RA:
                 {
                     final boolean addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                     // Decode resitricted alphabet integer
                     _identifier = (b & 0x0F) << 4;
                     b = read();
                     _identifier |= (b & 0xF0) >> 4;
                     
                     decodeOctetsOnFifthBitOfNonIdentifyingStringOnFirstBit(b);
                     // TODO obtain restricted alphabet given _identifier value
                     value = decodeRAOctetsAsString(null);
                     if (addToTable) {
                         _v.attributeValue.add(value);
                     }
                     
                     _attributes.addAttribute(name, value);
                     break;
                 }
                 case DecoderStateTables.NISTRING_EA:
                 {
                     if ((b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0) {
                         throw new EncodingAlgorithmException("Add to table not supported for Encoding algorithms");
                     }
                     
                     // Decode encoding algorithm integer
                     _identifier = (b & 0x0F) << 4;
                     b = read();
                     _identifier |= (b & 0xF0) >> 4;
                     
                     decodeOctetsOnFifthBitOfNonIdentifyingStringOnFirstBit(b);
                     processAIIEncodingAlgorithm(name);
                     break;
                 }
                 case DecoderStateTables.NISTRING_INDEX_SMALL:
                     value = _v.attributeValue.get(b & EncodingConstants.INTEGER_2ND_BIT_SMALL_MASK);
                     
                     _attributes.addAttribute(name, value);
                     break;
                 case DecoderStateTables.NISTRING_INDEX_MEDIUM:
                 {
                     final int index = (((b & EncodingConstants.INTEGER_2ND_BIT_MEDIUM_MASK) << 8) | read())
                     + EncodingConstants.INTEGER_2ND_BIT_SMALL_LIMIT;
                     value = _v.attributeValue.get(index);
                     
                     _attributes.addAttribute(name, value);
                     break;
                 }
                 case DecoderStateTables.NISTRING_INDEX_LARGE:
                 {
                     final int index = (((b & EncodingConstants.INTEGER_2ND_BIT_LARGE_MASK) << 16) | (read() << 8) | read())
                     + EncodingConstants.INTEGER_2ND_BIT_MEDIUM_LIMIT;
                     value = _v.attributeValue.get(index);
                     
                     _attributes.addAttribute(name, value);
                     break;
                 }
                 case DecoderStateTables.NISTRING_EMPTY:
                     _attributes.addAttribute(name, "");
                     break;
                 default:
                     throw new FastInfosetException("Illegal state when decoding AII value");
             }
             
         } while (!terminate);
     }
     
     protected final void processCommentII() throws FastInfosetException, IOException {
         _eventType = COMMENT;
         
         switch(decodeNonIdentifyingStringOnFirstBit()) {
             case NISTRING_STRING:
                 if (_addToTable) {
                     _v.otherString.add(new CharArray(_charBuffer, 0, _charBufferLength, true));
                 }
                 
                 _characters = _charBuffer;
                 _charactersOffset = 0;
                 _charactersLength = _charBufferLength;
                 break;
             case NISTRING_ENCODING_ALGORITHM:
                 throw new FastInfosetException("Comment II with encoding algorithm decoding not supported");
             case NISTRING_INDEX:
                 final CharArray ca = _v.otherString.get(_integer);
                 
                 _characters = ca.ch;
                 _charactersOffset = ca.start;
                 _charactersLength = ca.length;
                 break;
             case NISTRING_EMPTY_STRING:
                 _characters = _charBuffer;
                 _charactersOffset = 0;
                 _charactersLength = 0;
                 break;
         }
     }
     
     protected final void processProcessingII() throws FastInfosetException, IOException {
         _eventType = PROCESSING_INSTRUCTION;
         
         _piTarget = decodeIdentifyingNonEmptyStringOnFirstBit(_v.otherNCName);
         
         switch(decodeNonIdentifyingStringOnFirstBit()) {
             case NISTRING_STRING:
                 _piData = new String(_charBuffer, 0, _charBufferLength);
                 if (_addToTable) {
                     _v.otherString.add(new CharArrayString(_piData));
                 }
                 break;
             case NISTRING_ENCODING_ALGORITHM:
                 throw new FastInfosetException("Processing II with encoding algorithm decoding not supported");
             case NISTRING_INDEX:
                 _piData = _v.otherString.get(_integer).toString();
                 break;
             case NISTRING_EMPTY_STRING:
                 _piData = "";
                 break;
         }
     }
     
     protected final void processCIIEncodingAlgorithm() throws FastInfosetException, IOException {
         _algorithmData = _octetBuffer;
         _algorithmDataOffset = _octetBufferStart;
         _algorithmDataLength = _octetBufferLength;
             
         if (_algorithmId >= EncodingConstants.ENCODING_ALGORITHM_APPLICATION_START) {
             _algorithmURI = _v.encodingAlgorithm.get(_algorithmId - EncodingConstants.ENCODING_ALGORITHM_APPLICATION_START);
             if (_algorithmURI == null) {
                 throw new EncodingAlgorithmException("URI not present for encoding algorithm identifier " + _identifier);
             }
         } else if (_algorithmId > EncodingConstants.ENCODING_ALGORITHM_BUILTIN_END) {
             // Reserved built-in algorithms for future use
             // TODO should use sax property to decide if event will be
             // reported, allows for support through handler if required.
             throw new EncodingAlgorithmException("Encoding algorithm identifiers 10 up to and including 31 are reserved for future use");
         }
     }
     
     protected final void processAIIEncodingAlgorithm(QualifiedName name) throws FastInfosetException, IOException {
         String URI = null;
         if (_identifier >= EncodingConstants.ENCODING_ALGORITHM_APPLICATION_START) {
             URI = _v.encodingAlgorithm.get(_identifier - EncodingConstants.ENCODING_ALGORITHM_APPLICATION_START);
             if (URI == null) {
                 throw new EncodingAlgorithmException("URI not present for encoding algorithm identifier " + _identifier);
             }
         } else if (_identifier > EncodingConstants.ENCODING_ALGORITHM_BUILTIN_END) {
             // Reserved built-in algorithms for future use
             // TODO should use sax property to decide if event will be
             // reported, allows for support through handler if required.
             throw new EncodingAlgorithmException("Encoding algorithm identifiers 10 up to and including 31 are reserved for future use");
         }
         
         final byte[] data = new byte[_octetBufferLength];
         System.arraycopy(_octetBuffer, _octetBufferStart, data, 0, _octetBufferLength);
         _attributes.addAttributeWithAlgorithmData(name, URI, _identifier, data);
     }
 
     protected final void convertEncodingAlgorithmDataToCharacters() throws FastInfosetException, IOException {
         StringBuffer buffer = new StringBuffer();
         if (_algorithmId <= EncodingConstants.ENCODING_ALGORITHM_BUILTIN_END) {
            Object array = BuiltInEncodingAlgorithmFactory.table[_algorithmId].
                 decodeFromBytes(_algorithmData, _algorithmDataOffset, _algorithmDataLength);
             BuiltInEncodingAlgorithmFactory.table[_algorithmId].convertToCharacters(array,  buffer);
         } else if (_algorithmId >= EncodingConstants.ENCODING_ALGORITHM_APPLICATION_START) {
             final EncodingAlgorithm ea = (EncodingAlgorithm)_registeredEncodingAlgorithms.get(_algorithmURI);
             if (ea != null) {
                 final Object data = ea.decodeFromBytes(_octetBuffer, _octetBufferStart, _octetBufferLength);
                 ea.convertToCharacters(data, buffer);
             } else {
                 throw new EncodingAlgorithmException(
                         "Document contains application-defined encoding algorithm data that cannot be reported");
             }
         }
         
         _characters = new char[buffer.length()];
         buffer.getChars(0, buffer.length(), _characters, 0);
         _charactersOffset = 0;
         _charactersLength = _characters.length;                    
     }
     
     protected class NamespaceContextImpl implements NamespaceContext {
         public final String getNamespaceURI(String prefix) {
             return getNamespaceDecl(prefix);
         }
         
         public final String getPrefix(String namespaceURI) {
             throw new UnsupportedOperationException("getPrefix");
         }
         
         public final Iterator getPrefixes(String namespaceURI) {
             throw new UnsupportedOperationException("getPrefixes");
         }
     }
     
     public final String getNamespaceDecl(String prefix) {
         try {
             Object o = _prefixMap.get(prefix);
             if (o instanceof String) {
                 return (String) o;
             } else if (o instanceof Stack) {
                 return (String) ((Stack) o).peek();
             }
         } catch (EmptyStackException e) {
             // falls through
         }
         return null;
     }
     
     private final void popNamespaceDecl(String prefix) {
         try {
             Object o = _prefixMap.get(prefix);
             if (o instanceof String) {
                 _prefixMap.remove(prefix);
             } else if (o instanceof Stack) {
                 ((Stack) o).pop();
             }
         } catch (EmptyStackException e) {
             // falls through
         }
     }
     
     private final void pushNamespaceDecl(String prefix, String namespace) {
         try {
             Object o = _prefixMap.get(prefix);
             if (o == null) {
                 _prefixMap.put(prefix, namespace);
             } else if (o instanceof String) {
                 Stack s = new Stack();
                 s.push(o); s.push(namespace);
                 _prefixMap.put(prefix, s);
             } else {
                 ((Stack) o).push(namespace);
             }
         } catch (ClassCastException e) {
             throw new RuntimeException("Malformed namespace stack.");
         }
     }
     
     
     public final AttributesHolder getAttributesHolder() {
         return _attributes;
     }
     
     public final String getURI(String prefix) {
         return getNamespaceDecl(prefix);
     }
     
     public final Iterator getPrefixes() {
         return _prefixMap.keySet().iterator();
     }
     
     public final void setManager(StAXManager manager) {
         _manager = manager;
     }
     
     final static String getEventTypeString(int eventType) {
         switch (eventType){
             case START_ELEMENT:
                 return "START_ELEMENT";
             case END_ELEMENT:
                 return "END_ELEMENT";
             case PROCESSING_INSTRUCTION:
                 return "PROCESSING_INSTRUCTION";
             case CHARACTERS:
                 return "CHARACTERS";
             case COMMENT:
                 return "COMMENT";
             case START_DOCUMENT:
                 return "START_DOCUMENT";
             case END_DOCUMENT:
                 return "END_DOCUMENT";
             case ENTITY_REFERENCE:
                 return "ENTITY_REFERENCE";
             case ATTRIBUTE:
                 return "ATTRIBUTE";
             case DTD:
                 return "DTD";
             case CDATA:
                 return "CDATA";
         }
         return "UNKNOWN_EVENT_TYPE";
     }
     
 }
