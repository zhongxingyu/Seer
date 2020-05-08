 /* gvSIG. Sistem a de Información Geográfica de la Generalitat Valenciana
  *
  * Copyright (C) 2007 Generalitat Valenciana.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *  Generalitat Valenciana
  *   Conselleria d'Infraestructures i Transport
  *   Av. Blasco Ibáñez, 50
  *   46010 VALENCIA
  *   SPAIN
  *
  *      +34 9638 62 495
  *      gvsig@gva.es
  *      www.gvsig.gva.es
  */
 package org.gvsig.bxml.stream.impl;
 
 import static org.gvsig.bxml.stream.EventType.ATTRIBUTE;
 import static org.gvsig.bxml.stream.EventType.ATTRIBUTES_END;
 import static org.gvsig.bxml.stream.EventType.COMMENT;
 import static org.gvsig.bxml.stream.EventType.END_DOCUMENT;
 import static org.gvsig.bxml.stream.EventType.END_ELEMENT;
 import static org.gvsig.bxml.stream.EventType.NAMESPACE_DECL;
 import static org.gvsig.bxml.stream.EventType.START_DOCUMENT;
 import static org.gvsig.bxml.stream.EventType.START_ELEMENT;
 import static org.gvsig.bxml.stream.EventType.VALUE_BOOL;
 import static org.gvsig.bxml.stream.EventType.VALUE_BYTE;
 import static org.gvsig.bxml.stream.EventType.VALUE_DOUBLE;
 import static org.gvsig.bxml.stream.EventType.VALUE_FLOAT;
 import static org.gvsig.bxml.stream.EventType.VALUE_INT;
 import static org.gvsig.bxml.stream.EventType.VALUE_LONG;
 import static org.gvsig.bxml.stream.EventType.VALUE_STRING;
 import static org.gvsig.bxml.stream.io.Header.Compression.GZIP;
 import static org.gvsig.bxml.stream.io.Header.Compression.NO_COMPRESSION;
 import static org.gvsig.bxml.stream.io.TokenType.AttributeListEnd;
 import static org.gvsig.bxml.stream.io.TokenType.AttributeStart;
 import static org.gvsig.bxml.stream.io.TokenType.Comment;
 import static org.gvsig.bxml.stream.io.TokenType.ContentAttrElement;
 import static org.gvsig.bxml.stream.io.TokenType.ContentElement;
 import static org.gvsig.bxml.stream.io.TokenType.ElementEnd;
 import static org.gvsig.bxml.stream.io.TokenType.EmptyAttrElement;
 import static org.gvsig.bxml.stream.io.TokenType.EmptyElement;
 import static org.gvsig.bxml.stream.io.ValueType.ArrayCode;
 import static org.gvsig.bxml.stream.io.ValueType.BoolCode;
 import static org.gvsig.bxml.stream.io.ValueType.ByteCode;
 import static org.gvsig.bxml.stream.io.ValueType.DoubleCode;
 import static org.gvsig.bxml.stream.io.ValueType.FloatCode;
 import static org.gvsig.bxml.stream.io.ValueType.IntCode;
 import static org.gvsig.bxml.stream.io.ValueType.LongCode;
 import static org.gvsig.bxml.stream.io.ValueType.StringCode;
 
 import java.io.IOException;
 import java.nio.ByteOrder;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.XMLConstants;
 import javax.xml.namespace.QName;
 
 import org.gvsig.bxml.stream.BxmlStreamWriter;
 import org.gvsig.bxml.stream.EncodingOptions;
 import org.gvsig.bxml.stream.EventType;
 import org.gvsig.bxml.stream.impl.workers.StringTable;
 import org.gvsig.bxml.stream.io.BxmlOutputStream;
 import org.gvsig.bxml.stream.io.CommentPositionHint;
 import org.gvsig.bxml.stream.io.Header;
 import org.gvsig.bxml.stream.io.TokenType;
 import org.gvsig.bxml.stream.io.ValueType;
 import org.gvsig.bxml.stream.io.Header.Compression;
 import org.gvsig.bxml.stream.io.Header.Flags;
 
 /**
  * @author Gabriel Roldan (OpenGeo)
  * @version $Id$
  */
 public final class DefaultBxmlStreamWriter implements BxmlStreamWriter {
 
     private final EncodingOptions encodingOptions;
 
     private final BxmlOutputStream writer;
 
     private final NamesResolver namesResolver;
 
     private final StringTable stringTable;
 
     /**
      * A placeholder to pass to {@link NamespaceAwareNameResolver#toQName} in order to reuse the
      * space used to construct the prefixed element and attribute names.
      * 
      * @see #writeStartAttribute(String, String)
      * @see #writeStartElement(String, String)
      */
     private final StringBuilder qNamesPlaceHolder;
 
     private final Map<String, String> schemaLocations;
 
     private final Map<String, String> pendingNamespaces;
 
     private EventType lastEvent;
 
     private EventType lastTagEvent;
 
     /**
      * This flag controls whether the value tokens being written correspond to an attribute value
      * (true) or an element value(false)
      * 
      * @see #writeStartAttribute(String, String)
      * @see #writeEndAttributes()
      * @see #startValue(ValueType)
      */
     private boolean processingAttributes;
 
     private final ElementStack openElements;
 
     private ValueType currentValueType;
 
     /**
      * How long is the current value being written
      */
     public long valueLength;
 
     /**
      * How many elements for the current value has already being written
      */
     public long writtenValueLength;
 
     private boolean arrayInProgress;
 
     /**
      * This will only be true if a namespace was written and no attributes were written, meaning the
      * list of attributes shall automatically be closed
      */
     private boolean pendingNamespacesJustWritten;
 
     /**
      * @param encodingOptions
      * @param writer
      *            the bxml output stream writer to write on
      * @throws IOException
      */
     public DefaultBxmlStreamWriter(final EncodingOptions encodingOptions,
             final BxmlOutputStream writer) throws IOException {
         this.encodingOptions = encodingOptions;
         this.writer = writer;
 
         openElements = new ElementStack();
         stringTable = new StringTable();
         qNamesPlaceHolder = new StringBuilder();
         namesResolver = new NamespaceAwareNameResolver();
         pendingNamespaces = new HashMap<String, String>();
         schemaLocations = new HashMap<String, String>();
         this.lastEvent = EventType.NONE;
         // NOTE: writer's endianess and charset will be set when writeStartDocument is called
     }
 
     /**
      * @see BxmlStreamWriter#getEncodingOptions()
      */
     public EncodingOptions getEncodingOptions() {
         return encodingOptions.clone();
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getLastEvent()
      */
     public EventType getLastEvent() {
         return lastEvent;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getTagDeep()
      */
     public int getTagDeep() {
         final int size = openElements.size();
         return size;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getLastTagEvent()
      */
     public EventType getLastTagEvent() {
         return lastTagEvent;
     }
 
     /**
      * @return whether the underlying {@link BxmlOutputStream#isOpen() BxmlOutputStream} is open.
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#isOpen()
      */
     public boolean isOpen() {
         return this.writer.isOpen();
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#setSchemaLocation(java.lang.String,
      *      java.lang.String)
      */
     public void setSchemaLocation(final String namespace, final String schemaLocationUri) {
         schemaLocations.put(namespace, schemaLocationUri);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getPrefix(java.lang.String)
      */
     public String getPrefix(String uri) {
         return namesResolver.getPrefix(uri);
     }
 
     /**
      * @throws IOException
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#setDefaultNamespace(java.lang.String)
      */
     public void setDefaultNamespace(final String defaultNamespaceUri) throws IOException {
         writeNamespace(XMLConstants.DEFAULT_NS_PREFIX, defaultNamespaceUri);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeNamespace(java.lang.String,
      *      java.lang.String)
      */
     public void writeNamespace(final String prefix, final String namespaceUri) throws IOException {
         this.namesResolver.declarePrefix(prefix, namespaceUri);
         this.pendingNamespaces.put(prefix, namespaceUri);
         writePendingNamespaces();
         lastEvent = NAMESPACE_DECL;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartDocument()
      */
     public final void writeStartDocument() throws IOException {
         Header header = toHeader(encodingOptions);
         writer.writeHeader(header);
         // write xml declaration
         writer.writeTokenType(TokenType.XmlDeclaration);
         writer.writeString(encodingOptions.getXmlVersion());
         Boolean standaloneSetting = encodingOptions.isStandalone();
         final boolean standaloneIsSet = standaloneSetting != null;
         final boolean standalone = standaloneSetting != null ? standaloneSetting.booleanValue()
                 : true;
         writer.writeBoolean(standalone);
         writer.writeBoolean(standaloneIsSet);
 
         lastEvent = START_DOCUMENT;
     }
 
     /**
      * Utility method to create a {@link Header} instance from an {@link EncodingOptions}
      * 
      * @param encodingOptions
      * @return
      */
     static Header toHeader(EncodingOptions encodingOptions) {
         final ByteOrder endianess = encodingOptions.getByteOrder();
         // TODO: revisit, I guess charsEndianess is just a hint for the cases where the charset does
         // not imposes a byte order
         final ByteOrder charsEndianess = endianess;
         final boolean hasRandomAccessInfo = false;
         final boolean hasStrictXmlStrings = encodingOptions.isUseStrictXmlStrings();
         final boolean isValidated = encodingOptions.isValidated();
         final Flags flags = Flags.valueOf(endianess, charsEndianess, hasRandomAccessInfo,
                 hasStrictXmlStrings, isValidated);
 
         final Compression compression = encodingOptions.isUseCompression() ? GZIP : NO_COMPRESSION;
         final Charset charsEncoding = encodingOptions.getCharactersEncoding();
 
         final Header header = Header.valueOf(flags, compression, charsEncoding);
 
         return header;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#close()
      */
     public final void close() throws IOException {
         if (writer.isOpen()) {
             if (!writer.isAutoFlushing()) {
                 writer.setAutoFlushing(true);
             }
             flush();
         }
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#flush()
      */
     public final void flush() throws IOException {
         writer.flush();
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getCurrentElementName()
      */
     public String getCurrentElementName() {
         return openElements.getCurrentElement().localName;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getCurrentElementNamespace()
      */
     public String getCurrentElementNamespace() {
         return openElements.getCurrentElement().namespaceUri;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#require
      */
     public void require(final EventType type, final String namespaceUri, final String localName)
             throws IllegalStateException {
         if (type != null && type != getLastEvent()) {
             throw new IllegalStateException("expected current event to be " + type + " but is "
                     + getLastEvent());
         }
         if (namespaceUri != null && !namespaceUri.equals(getCurrentElementNamespace())) {
             throw new IllegalStateException("expected namespace URI to be " + namespaceUri
                     + " but is " + getCurrentElementNamespace());
         }
         if (localName != null && !localName.equals(getCurrentElementName())) {
             throw new IllegalStateException("expected element's local name to be " + localName
                     + " but is " + getCurrentElementName());
         }
     }
 
     /**
      * Writes the trailer token and content
      * 
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeEndDocument()
      */
     public final void writeEndDocument() throws IOException {
         final long initialPosition = writer.getPosition();
         // Write the trailer token
         writer.writeTokenType(TokenType.Trailer);
         final byte[] id = { 0x01, 'T', 'R', 0x00 };
         writer.writeByte(id, 0, 4);
 
         writeStringTableIndex();
         writeIndexTableIndex();
 
         final long endPosition = writer.getPosition();
 
         // 4 + trailer written bytes to account for the TOTAL trailer length, including this last
         // tokenLength integer
         final int tokenLength = 4 + (int) (endPosition - initialPosition);
         writer.writeInt(tokenLength);
         flush();
         lastEvent = END_DOCUMENT;
     }
 
     private void writeStringTableIndex() throws IOException {
         final boolean isUsed = true;
         writer.writeBoolean(isUsed);
         final int nFragments = stringTable.size();
         writer.writeCount(nFragments);
 
         long offset;
         for (int entry = 0; entry < nFragments; entry++) {
             /*
              * write one StringTableIndexEntry per string, not optimal, but the way our StringTables
              * work thanks for #getForceStringTableName
              */
             writer.writeCount(1);
             offset = stringTable.getOffset(entry);
             writer.writeCount(offset);
         }
 
     }
 
     /**
      * @throws IOException
      */
     private void writeIndexTableIndex() throws IOException {
         final boolean isUsed = false;
         final int nEntries = 0;
         writer.writeBoolean(isUsed);
         writer.writeCount(nEntries);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartAttribute(javax.xml.namespace.QName)
      * @see #writeStartAttribute(String, String)
      */
     public final void writeStartAttribute(final QName qname) throws IOException {
         writeStartAttribute(qname.getNamespaceURI(), qname.getLocalPart());
     }
 
     private final Set<String> autoReferenceableAttributes = new HashSet<String>();
 
     private String currentAttributeQName;
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartAttribute(java.lang.String,
      *      java.lang.String)
      */
     public final void writeStartAttribute(final String namespaceUri, final String localName)
             throws IOException {
         processingAttributes = true;
 
         namesResolver.toQName(namespaceUri, localName, qNamesPlaceHolder);
         long nameIndex = getForceStringTableName(qNamesPlaceHolder);
         writer.writeTokenType(AttributeStart);
         writer.writeCount(nameIndex);
 
         currentAttributeQName = qNamesPlaceHolder.toString();
 
         // no more need to check for auto closing the attribute list, the user is now responsible
         this.pendingNamespacesJustWritten = false;
         this.writtenValueLength = 0;
         this.valueLength = 0;
         lastEvent = ATTRIBUTE;
     }
 
     public void setWriteAttributeValueAsStringTable(final String qName) {
         autoReferenceableAttributes.add(qName);
     }
 
     private final void writeNamespaceInternal(final String prefixNamespaceUri, final String prefix,
             final String namespaceUri) throws IOException {
 
         namesResolver.toQName(prefixNamespaceUri, prefix, qNamesPlaceHolder);
         long nameIndex = getForceStringTableName(qNamesPlaceHolder);
         writer.writeTokenType(AttributeStart);
         writer.writeCount(nameIndex);
 
         writer.writeTokenType(TokenType.CharContent);
         writer.writeByte(StringCode.getCode());
         writer.writeString(namespaceUri);
 
         this.pendingNamespacesJustWritten = true;
         this.processingAttributes = true;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeEndAttributes()
      */
     public final void writeEndAttributes() throws IOException {
         // migrates from EmptyElement to EmptyAttrElement
         if (processingAttributes) {// no attributes may have been written
             setCurrentElementType(EmptyAttrElement);
             processingAttributes = false;
             pendingNamespacesJustWritten = false;
             writer.writeTokenType(AttributeListEnd);
             this.writtenValueLength = 0;
             this.valueLength = 0;
         }
         lastEvent = ATTRIBUTES_END;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartElement(javax.xml.namespace.QName)
      * @see #writeStartElement(String, String)
      */
     public final void writeStartElement(final QName qname) throws IOException {
         writeStartElement(qname.getNamespaceURI(), qname.getLocalPart());
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartElement(java.lang.String,
      *      java.lang.String)
      */
     public final void writeStartElement(final String namespaceUri, final String localName)
             throws IOException {
         if (openElements.size() > 0) {
             // Got a start element directly after another one and there are pending namespaces for
             // the previous element?
             if (pendingNamespacesJustWritten) {
                 writeEndNamespaces();
                 setCurrentElementType(ContentAttrElement);
             } else {
                 TokenType currentElementType = openElements.getCurrentElementType();
                 if (currentElementType == TokenType.EmptyElement) {
                     setCurrentElementType(ContentElement);
                 } else if (currentElementType == EmptyAttrElement) {
                     setCurrentElementType(ContentAttrElement);
                 }
             }
         }
         // new element, new namespace context
         namesResolver.pushContext();
 
         // do not flush content until we know the exact token type for this element (that is,
         // whether its an EmptyElement, ContentAttrElement, etc)
         writer.setAutoFlushing(false);
 
         namesResolver.toQName(namespaceUri, localName, qNamesPlaceHolder);
         final long nameIndex = getForceStringTableName(qNamesPlaceHolder);
 
         // start a new current element
         {
             long fileOffset = writer.getPosition();
             openElements.newElement(EmptyElement, fileOffset, namespaceUri, localName);
         }
 
         writer.writeTokenType(EmptyElement);
         writer.writeCount(nameIndex);
 
         if (!XMLConstants.NULL_NS_URI.equals(namespaceUri)) {
             final String prefix = namesResolver.getPrefix(namespaceUri);
             if (prefix == null) {
                 // we got a non prefix mapped namespace for this element
                 // lets declare the element namespace inline
                 writeNamespaceInternal(XMLConstants.NULL_NS_URI, XMLConstants.XMLNS_ATTRIBUTE,
                         namespaceUri);
             }
         }
 
         lastEvent = lastTagEvent = START_ELEMENT;
         if (openElements.size() == 1 && pendingNamespaces.size() > 0) {
             writePendingNamespaces();
         }
         if (openElements.size() == 1) {
             // declareSchemaLocations();
         }
         lastEvent = lastTagEvent = START_ELEMENT;
         this.writtenValueLength = 0;
         this.valueLength = 0;
     }
 
     private void writePendingNamespaces() throws IOException {
         if (pendingNamespaces.size() == 0) {
             return;
         }
         if (openElements.size() == 0) {
             return;
         }
         if (lastEvent != EventType.START_ELEMENT && lastEvent != NAMESPACE_DECL) {
             return;
         }
 
         if (pendingNamespaces.size() > 0) {
             String prefix, namespaceUri;
             for (Map.Entry<String, String> entry : pendingNamespaces.entrySet()) {
                 prefix = entry.getKey();
                 namespaceUri = entry.getValue();
                 if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                     // do not declare the "xmlns" namespace, its not allowed
                     continue;
                 }
                 if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
                     // it's the default namespace
                     writeNamespaceInternal(XMLConstants.NULL_NS_URI, XMLConstants.XMLNS_ATTRIBUTE,
                             namespaceUri);
                 } else {
                     writeNamespaceInternal(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix,
                             namespaceUri);
                 }
             }
             pendingNamespaces.clear();
         }
     }
 
     private void declareSchemaLocations() throws IOException {
         if (schemaLocations.size() > 0) {
             writeStartAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation");
 
             for (Iterator<Map.Entry<String, String>> it = schemaLocations.entrySet().iterator(); it
                     .hasNext();) {
 
                 Map.Entry<String, String> entry = it.next();
 
                 String nsUri = entry.getKey();
                 String schemaLocationUri = entry.getValue();
 
                 writeValue(nsUri);
                 writeValue(" ");
                 writeValue(schemaLocationUri);
                 if (it.hasNext()) {
                     writeValue(" ");
                 }
             }
             schemaLocations.clear();
         }
     }
 
     /**
      * Closes the current element if needed and flushes any buffered write that might be pending.
      * 
      * @see BxmlStreamWriter#writeEndElement()
      */
     public final void writeEndElement() throws IOException {
         // call writePendingNamespaces before popping the current element in case it was a direct
         // start/end element call without value or attributes
         writePendingNamespaces();
 
         final TokenType elementType = openElements.getCurrentElementType();
 
         if (this.pendingNamespacesJustWritten) {
             writeEndNamespaces();
         }
 
         writer.setAutoFlushing(true);
 
         // we only need to write down an EndElement token if its a content element
         if (elementType == ContentElement || elementType == ContentAttrElement) {
             writer.writeTokenType(ElementEnd);
         }
 
         openElements.popCurrentElement();
         namesResolver.popContext();
         lastEvent = lastTagEvent = END_ELEMENT;
         flush();
     }
 
     private void writeEndNamespaces() throws IOException {
         final TokenType elementType = openElements.getCurrentElementType();
         if (elementType == TokenType.EmptyElement) {
             writeEndAttributes();
         } else {
             throw new IllegalStateException(
                     "pendingNamespacesJustWritten should only be true if elementType == EmptyElement");
         }
     }
 
     /**
      * Returns the string table index corresponding to the given string, creating the string table
      * entry if needed.
      * 
      * @param qName
      * @return
      * @throws IOException
      * @TODO handle namespaces
      */
     private long getForceStringTableName(final CharSequence stringToHandle) throws IOException {
 
         long stringIndex = stringTable.get(stringToHandle);
         if (-1 == stringIndex) {
             final long position = writer.getPosition();
             stringIndex = stringTable.add(stringToHandle, position);
             writer.writeTokenType(TokenType.StringTable);
             writer.writeCount(1L);
             writer.writeString(stringToHandle);
         }
         return stringIndex;
     }
 
     /**
      * Writes the provided String value.
      * <p>
      * If processing attributes (ie, this is the value of an attribute) and the current attribute
      * qualified name resolves to one of the set with
      * {@link #setWriteAttributeValueAsStringTable(String)}, the value will be written as a
      * StringTable token reference instead.
      * </p>
      * 
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(java.lang.String)
      */
     public final void writeValue(final String value) throws IOException {
         if (processingAttributes) {
             if (autoReferenceableAttributes.contains(this.currentAttributeQName)) {
                 long stringTableRef = getForceStringTableName(value);
                 writeStringTableValue(stringTableRef);
                 valueLength = 1;
                 writtenValueLength = 1;
                 lastEvent = VALUE_STRING;
                 return;
             }
         }
 
         startValue(StringCode);
         writer.writeString(value);
         writtenValueLength++;
         lastEvent = VALUE_STRING;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getStringTableReference(CharSequence)
      */
     public long getStringTableReference(final CharSequence stringValue) throws IOException {
         long stringTableIndex = getForceStringTableName(stringValue);
         return stringTableIndex;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStringTableValue(long)
      */
     public void writeStringTableValue(long stringTableEntryId) throws IOException {
         writer.writeTokenType(TokenType.CharContentRef);
         writer.writeCount(stringTableEntryId);
         lastEvent = VALUE_STRING;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(char[], int, int)
      */
     public final void writeValue(final char[] chars, final int offset, final int length)
             throws IOException {
         startValue(StringCode);
         writer.writeString(chars, offset, length);
         writtenValueLength++;
         lastEvent = VALUE_STRING;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(byte)
      */
     public final void writeValue(byte value) throws IOException {
         startValue(ByteCode);
         writer.writeByte(value);
         writtenValueLength++;
         lastEvent = VALUE_BYTE;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(byte[], int, int)
      */
     public final void writeValue(final byte[] value, final int offset, final int length)
             throws IOException {
         if (!isArrayInProgress()) {
             startArrayValue(ByteCode, length);
         }
         writer.writeByte(value, offset, length);
         lastEvent = VALUE_BYTE;
         if (!isArrayInProgress()) {
             valueLength = length;
         }
         writtenValueLength += length;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(int)
      */
     public final void writeValue(int value) throws IOException {
         startValue(IntCode);
         writer.writeInt(value);
         writtenValueLength++;
         lastEvent = VALUE_INT;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(long)
      */
     public final void writeValue(long value) throws IOException {
         startValue(LongCode);
         writer.writeLong(value);
         writtenValueLength++;
         lastEvent = VALUE_LONG;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(float)
      */
     public final void writeValue(float value) throws IOException {
         startValue(FloatCode);
         writer.writeFloat(value);
         writtenValueLength++;
         lastEvent = VALUE_FLOAT;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(double)
      */
     public final void writeValue(double value) throws IOException {
         startValue(DoubleCode);
         writer.writeDouble(value);
         writtenValueLength++;
         lastEvent = VALUE_DOUBLE;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(boolean)
      */
     public final void writeValue(boolean value) throws IOException {
         startValue(BoolCode);
         writer.writeBoolean(value);
         writtenValueLength++;
         lastEvent = VALUE_BOOL;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(boolean[], int, int)
      */
     public final void writeValue(final boolean[] value, final int offset, final int length)
             throws IOException {
         if (!isArrayInProgress()) {
             startArrayValue(BoolCode, length);
         }
         writer.writeBoolean(value, offset, length);
         lastEvent = VALUE_BOOL;
         writtenValueLength += length;
         if (!isArrayInProgress()) {
             valueLength = length;
         }
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(int[], int, int)
      */
     public final void writeValue(final int[] value, final int offset, final int length)
             throws IOException {
         if (!isArrayInProgress()) {
             startArrayValue(IntCode, length);
         }
         writer.writeInt(value, offset, length);
         lastEvent = VALUE_INT;
         if (!isArrayInProgress()) {
             valueLength = length;
         }
         writtenValueLength += length;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(long[], int, int)
      */
     public final void writeValue(final long[] value, final int offset, final int length)
             throws IOException {
         if (!isArrayInProgress()) {
             startArrayValue(LongCode, length);
         }
         writer.writeLong(value, offset, length);
         lastEvent = VALUE_LONG;
         if (!isArrayInProgress()) {
             valueLength = length;
         }
         writtenValueLength += length;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(float[], int, int)
      */
     public final void writeValue(final float[] value, final int offset, final int length)
             throws IOException {
         if (!isArrayInProgress()) {
             startArrayValue(FloatCode, length);
         }
         writer.writeFloat(value, offset, length);
         lastEvent = VALUE_FLOAT;
         if (!isArrayInProgress()) {
             valueLength = length;
         }
         writtenValueLength += length;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(double[], int, int)
      */
     public final void writeValue(final double[] value, final int offset, final int length)
             throws IOException {
         if (!isArrayInProgress()) {
             startArrayValue(DoubleCode, length);
         }
         writer.writeDouble(value, 0, length);
         lastEvent = VALUE_DOUBLE;
         if (!isArrayInProgress()) {
             valueLength = length;
         }
         writtenValueLength += length;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeComment(java.lang.String)
      */
     public final void writeComment(final String string) throws IOException {
         writer.writeTokenType(Comment);
         writer.writeByte(CommentPositionHint.START_OF_LINE.getCode());
         writer.writeString(string);
         lastEvent = COMMENT;
     }
 
     private void startValue(ValueType valueTypeCode) throws IOException {
         if (currentValueType == ValueType.ArrayCode) {
             // ignore, being called from a writeValue(primitiveType) while encoding an array
             return;
         }
         if (pendingNamespacesJustWritten) {
             writeEndNamespaces();
         }
         if (!processingAttributes) {
             final TokenType elementType = openElements.getCurrentElementType();
             if (elementType == EmptyAttrElement) {
                 setCurrentElementType(ContentAttrElement);
             } else if (elementType == EmptyAttrElement || elementType == EmptyElement) {
                 setCurrentElementType(ContentElement);
             }
             // Got the final current element type, can go back to auto flush mode
             writer.setAutoFlushing(true);
         }
         writer.writeTokenType(TokenType.CharContent);
         writer.writeByte(valueTypeCode.getCode());
         currentValueType = valueTypeCode;
         valueLength = 1;
         writtenValueLength = 0;
     }
 
     void setCurrentElementType(final TokenType elementType) throws IOException {
         openElements.setCurrentElementType(elementType);
         final long currentPosition = writer.getPosition();
         final long elementTokenPosition = openElements.getCurrentElementPosition();
         writer.setPosition(elementTokenPosition);
         writer.writeTokenType(elementType);
         writer.setPosition(currentPosition);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getValueLength()
      */
     public long getValueLength() {
         return valueLength;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getWrittenValueCount()
      */
     public long getWrittenValueCount() {
         return writtenValueLength;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#startArray(org.gvsig.bxml.stream.EventType, int)
      */
     public void startArray(EventType valueType, int arrayLength) throws IOException {
         ValueType arrayElementType;
         switch (valueType) {
         case VALUE_BOOL:
             arrayElementType = ValueType.BoolCode;
             break;
         case VALUE_BYTE:
             arrayElementType = ValueType.ByteCode;
             break;
         case VALUE_INT:
             arrayElementType = ValueType.IntCode;
             break;
         case VALUE_LONG:
             arrayElementType = ValueType.LongCode;
             break;
         case VALUE_FLOAT:
             arrayElementType = ValueType.FloatCode;
             break;
         case VALUE_DOUBLE:
             arrayElementType = ValueType.DoubleCode;
             break;
         default:
             throw new IllegalArgumentException("Not a numeric value type: " + valueType);
         }
         startArrayValue(arrayElementType, arrayLength);
         this.arrayInProgress = true;
         lastEvent = valueType;
         valueLength = arrayLength;
         writtenValueLength = 0;
     }
 
     private void startArrayValue(final ValueType arrayElementType, final int length)
             throws IOException {
         startValue(ArrayCode);
         writer.writeByte(arrayElementType.getCode());
         writer.writeCount(length);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#endArray()
      */
     public void endArray() throws IOException {
         this.arrayInProgress = false;
         this.currentValueType = null;
         this.writtenValueLength = 0;
         this.valueLength = 0;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#isArrayInProgress()
      */
     public boolean isArrayInProgress() {
         return arrayInProgress;
     }
 
     /**
      * Represents a stack of open elements.
      * 
      * @author Gabriel Roldan (OpenGeo)
      * @version $Id$
      */
     private static final class ElementStack {
         private static class OpenElement {
             TokenType elementType;
 
             long tokenPosition;
 
             String namespaceUri, localName;
 
             @Override
             public String toString() {
                 return "[" + elementType + ":" + tokenPosition + namespaceUri + "#" + localName
                         + "]";
             }
         }
 
         /**
          * Use an arraylist to simulate the open elements stack because additions/removals perform
          * much better then Stack and LinkedList. The current element is the last one in the list.
          */
         private List<OpenElement> openElements = new ArrayList<OpenElement>();
 
         /**
          * Pool of openelements to avoid creating thousands of objects
          */
         private List<OpenElement> openElementPool = new ArrayList<OpenElement>();
 
         private OpenElement currentElement;
 
         public OpenElement getCurrentElement() {
             return currentElement;
         }
 
         public TokenType getCurrentElementType() {
             return currentElement.elementType;
         }
 
         /**
          * Returns a new current element
          * 
          * @param elementType
          * @param fileOffset
          * @param localName
          * @param namespaceUri
          */
         public void newElement(final TokenType elementType, final long fileOffset,
                 final String namespaceUri, final String localName) {
             OpenElement element;
             if (openElementPool.size() > 0) {
                 element = openElementPool.remove(openElementPool.size() - 1);
             } else {
                 element = new OpenElement();
             }
 
             element.elementType = EmptyElement;
             element.tokenPosition = fileOffset;
             element.namespaceUri = namespaceUri;
             element.localName = localName;
 
             openElements.add(element);
             currentElement = element;
         }
 
         public int size() {
             return openElements.size();
         }
 
         public void setCurrentElementType(TokenType elementType) {
             currentElement.elementType = elementType;
         }
 
         public void popCurrentElement() {
             // remove currentElement from openElements and return it to the pool
             final int currentElementIndex = openElements.size() - 1;
             openElements.remove(currentElementIndex);
             openElementPool.add(currentElement);
 
             // establish the new current element
             if (currentElementIndex > 0) {
                 currentElement = openElements.get(currentElementIndex - 1);
             } else {
                 currentElement = null;
             }
         }
 
         public long getCurrentElementPosition() {
             return currentElement.tokenPosition;
         }
     }
 }
