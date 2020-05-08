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
 package org.gvsig.bxml.stream;
 
 import static org.gvsig.bxml.stream.Contract.assertPost;
 import static org.gvsig.bxml.stream.Contract.assertPre;
 import static org.gvsig.bxml.stream.EventType.ATTRIBUTE;
 import static org.gvsig.bxml.stream.EventType.ATTRIBUTES_END;
 import static org.gvsig.bxml.stream.EventType.COMMENT;
 import static org.gvsig.bxml.stream.EventType.END_DOCUMENT;
 import static org.gvsig.bxml.stream.EventType.END_ELEMENT;
 import static org.gvsig.bxml.stream.EventType.NAMESPACE_DECL;
 import static org.gvsig.bxml.stream.EventType.NONE;
 import static org.gvsig.bxml.stream.EventType.START_DOCUMENT;
 import static org.gvsig.bxml.stream.EventType.START_ELEMENT;
 import static org.gvsig.bxml.stream.EventType.VALUE_BOOL;
 import static org.gvsig.bxml.stream.EventType.VALUE_BYTE;
 import static org.gvsig.bxml.stream.EventType.VALUE_DOUBLE;
 import static org.gvsig.bxml.stream.EventType.VALUE_FLOAT;
 import static org.gvsig.bxml.stream.EventType.VALUE_INT;
 import static org.gvsig.bxml.stream.EventType.VALUE_LONG;
 import static org.gvsig.bxml.stream.EventType.VALUE_STRING;
 
 import java.io.IOException;
 
 import javax.xml.XMLConstants;
 import javax.xml.namespace.QName;
 
 /**
  * @author Gabriel Roldan (OpenGeo)
  * @version $Id$
  */
 final class BxmlStreamWriter_Contract extends BxmlStreamWriterAdapter implements BxmlStreamWriter {
 
     private EventType lastEvent;
 
     private int arrayLength;
 
     private int arrayWrittenCount;
 
     public BxmlStreamWriter_Contract(final BxmlStreamWriter impl) {
         super(impl);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getCurrentElementName()
      */
     @Override
     public String getCurrentElementName() {
         assertPre(impl.getTagDeep() > 0, "There are no open elements");
         String currentElementName = impl.getCurrentElementName();
         assertPost(currentElementName != null, "current element name is null");
         return currentElementName;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getCurrentElementNamespace()
      */
     @Override
     public String getCurrentElementNamespace() {
         assertPre(impl.getTagDeep() > 0, "There are no open elements");
         String currentElementNamespace = impl.getCurrentElementNamespace();
         assertPost(currentElementNamespace != null, "current element namespace is null");
         return currentElementNamespace;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#require
      */
     @Override
     public void require(final EventType type, final String namespaceUri, final String localName)
             throws IllegalStateException {
         // @pre {if( namespaceUri != null || localName != null ) getTagDeep() > 0}
         if (namespaceUri != null || localName != null) {
             assertPre(impl.getTagDeep() > 0,
                     "Can't check the current element's namespace or localname, there are no open elements");
         }
         impl.require(type, namespaceUri, localName);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getLastEvent()
      */
     @Override
     public EventType getLastEvent() {
         lastEvent = impl.getLastEvent();
         assertPost(lastEvent != null, "getLastEvent() can't return null");
         return lastEvent;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getTagDeep()
      */
     @Override
     public int getTagDeep() {
         int deepness = impl.getTagDeep();
         assertPost(deepness > -1, "getTagDeep: return value should be >= 0: ", deepness);
         return deepness;
     }
 
     /**
      * @throws IOException
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#setDefaultNamespace(java.lang.String)
      */
     @Override
     public void setDefaultNamespace(String defaultNamespaceUri) throws IOException {
         assertPre(defaultNamespaceUri != null, "default namespace URI can't be null");
         // @pre {getLastEvent() IN (START_DOCUMENT , START_ELEMENT, NAMESPACE_DECL)}
         lastEvent = impl.getLastEvent();
         assertPre(
                 lastEvent == NONE || lastEvent == START_DOCUMENT || lastEvent == START_ELEMENT
                         || lastEvent == NAMESPACE_DECL,
                 "setDefaultNamespace: last event shall be either NONE, START_ELEMENT or NAMESPACE_DECL: ",
                 lastEvent);
         impl.setDefaultNamespace(defaultNamespaceUri);
         lastEvent = impl.getLastEvent();
         assertPost(lastEvent == NAMESPACE_DECL,
                 "expected last event to be NAMESPACE_DECL, but is ", lastEvent);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getPrefix(java.lang.String)
      */
     @Override
     public String getPrefix(String uri) {
         assertPre(uri != null, "uri can't be null. Did you mean the empty string?");
         return impl.getPrefix(uri);
     }
 
     /**
      * @throws IOException
      * @see {@link org.gvsig.bxml.stream.BxmlStreamWriter#writeNamespace(String, String)}
      */
     @Override
     public void writeNamespace(String prefix, String namespaceUri) throws IOException {
         // @pre {getLastEvent() IN (START_DOCUMENT, START_ELEMENT, NAMESPACE_DECL)}
         lastEvent = impl.getLastEvent();
         assertPre(
                 lastEvent == EventType.NAMESPACE_DECL || lastEvent == START_DOCUMENT,
                 lastEvent == START_ELEMENT,
                 "setPrefix: last event shall be either NAMESPACE_DECL, START_DOCUMENT or START_ELEMENT: ",
                 lastEvent);
         assertPre(prefix != null, "setPrefix: prefix can't be null");
         assertPre(namespaceUri != null, "setPrefix: namespaceUri can't be null");
         impl.writeNamespace(prefix, namespaceUri);
 
         String boundPrefix = impl.getPrefix(namespaceUri);
         if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
             assertPost(null == boundPrefix,
                     "The 'xmlns' attribute should not be declared as prefix, but got it bound to ",
                     namespaceUri);
         } else {
             assertPost(prefix.equals(boundPrefix), "Expected prefix ", prefix,
                     " to be bound to namespace ", namespaceUri, " but got ", boundPrefix);
         }
 
         lastEvent = impl.getLastEvent();
         assertPost(lastEvent == NAMESPACE_DECL,
                 "expected last event to be NAMESPACE_DECL, but is ", lastEvent);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#setSchemaLocation(java.lang.String,
      *      java.lang.String)
      */
     @Override
     public void setSchemaLocation(String namespaceUri, String schemaLocationUri) {
         // * @pre {getLastEvent() == NONE}
         lastEvent = impl.getLastEvent();
         assertPre(lastEvent == START_DOCUMENT,
                 "setSchemaLocation: last event shall be START_DOCUMENT: ", lastEvent);
 
         assertPre(namespaceUri != null && schemaLocationUri != null,
                 "setSchemaLocation: namespace and schema location can't be null");
 
         assertPre(!XMLConstants.NULL_NS_URI.equals(namespaceUri),
                 "setSchemaLocation: It is not allowed specify the location of the null namespace");
 
         impl.setSchemaLocation(namespaceUri, schemaLocationUri);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#startArray(org.gvsig.bxml.stream.EventType, int)
      */
     @Override
     public void startArray(EventType valueType, int arrayLength) throws IOException {
         assertPre(impl.isArrayInProgress() == false,
                 "Before startArray, isArrayInProgress() should be false");
         // * @pre {valueType != null}
         assertPre(valueType != null, "startArray: valueType can't be null");
 
         // * @pre {valueType.isValue() == true}
         assertPre(valueType.isValue(), "startArray: valueType shall be of a value type");
 
         // * @pre {valueType != VALUE_STRING}
         assertPre(valueType != VALUE_STRING, "startArray: valueType can't be VALUE_STRING");
 
         // * @pre {arrayLength >= 0}
         assertPre(arrayLength > -1, "startArray: arrayLength shall be >= 0");
 
         this.arrayLength = arrayLength;
         this.arrayWrittenCount = 0;
         impl.startArray(valueType, arrayLength);
 
         // * @post {getLastEvent() == valueType}
         lastEvent = impl.getLastEvent();
         assertPost(valueType == lastEvent,
                 "startArray: getLastEvent() shall return the same value type: ", valueType,
                 ". Got ", lastEvent);
 
         // * @post {getWrittenValueCount() == 0}
         assertPost(0 == impl.getWrittenValueCount(),
                 "startArray: written value count should be zero");
 
         // * @post {getValueLength() == arrayLength}
         assertPost(arrayLength == impl.getValueLength(),
                 "startArray: value length should be equal to the arrayLength argument");
         assertPost(impl.isArrayInProgress() == true,
                 "After startArray, isArrayInProgress() should be true");
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#endArray()
      */
     @Override
     public void endArray() throws IOException {
         assertPre(impl.isArrayInProgress() == true,
                 "Before array end, isArrayInProgress() should be true");
         lastEvent = impl.getLastEvent();
         assertPre(lastEvent.isValue(),
                 "endArray can't be called if last event is not a value event");
 
         // * @pre {getWrittenValueCount() == getValueLength()}
         final long written = impl.getWrittenValueCount();
         final long len = impl.getValueLength();
         assertPre(arrayLength == len, "Array length stated at startArray (", arrayLength,
                 ") and returned by getValueLength(", len, ") do not match");
 
         assertPre(written == len, "endArray: written count (", written,
                 ") shall be equal to value length:" + len);
 
         impl.endArray();
 
         this.arrayLength = 0;
         this.arrayWrittenCount = 0;
 
         final long postLenght = impl.getValueLength();
         assertPost(postLenght == 0, "At array end, getValueLength() should be 0", postLenght);
 
         final long postWritten = impl.getWrittenValueCount();
         assertPost(postWritten == 0, "At array end, getWrittenValueCount() should be 0",
                 postWritten);
         assertPost(impl.isArrayInProgress() == false,
                 "At array end, isArrayInProgress() should be false");
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getValueLength()
      */
     @Override
     public long getValueLength() {
         // * @pre {getLastEvent().isValue() == true}
         lastEvent = impl.getLastEvent();
         assertPre(lastEvent.isValue(), "getValueLength: last event shall be a value event: ",
                 lastEvent);
         final long valueLength = impl.getValueLength();
         // * @post {$return >= 0}
         assertPost(valueLength > -1, "getValueLength: value length shall be >= 0");
         return valueLength;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getWrittenValueCount()
      */
     @Override
     public long getWrittenValueCount() {
         // * @pre {getLastEvent().isValue() == true}
         lastEvent = impl.getLastEvent();
         assertPre(lastEvent.isValue(), "getValueLength: last event shall be a value event: ",
                 lastEvent);
 
         final long writtenCount = impl.getWrittenValueCount();
 
         // * @post {$return <= getValueLength()}
         assertPost(writtenCount > -1, "getValueLength: return value shall be >= 0");
 
         final long valueLength = impl.getValueLength();
         assertPost(writtenCount <= valueLength,
                 "getValueLength: return value shall be <= valueLength");
 
         return writtenCount;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeComment(java.lang.String)
      */
     @Override
     public void writeComment(String commentContent) throws IOException {
         impl.writeComment(commentContent);
         lastEvent = impl.getLastEvent();
         assertPost(lastEvent == COMMENT, "writeComment: last event should be COMMENT:", lastEvent);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeEndAttributes()
      */
     @Override
     public void writeEndAttributes() throws IOException {
         // * @pre {getLastTagEvent() == START_ELEMENT}
         final EventType lastTagEvent = impl.getLastTagEvent();
         assertPre(START_ELEMENT == lastTagEvent,
                 "writeEndAttributes: lastTagEvent should be START_ELEMENT: ", lastTagEvent);
 
         impl.writeEndAttributes();
 
         // * @post {getLastEvent() == ATTRIBUTES_END}
         lastEvent = impl.getLastEvent();
         assertPost(ATTRIBUTES_END == lastEvent,
                 "writeEndAttributes: lastEvent should be ATTRIBUTES_END: ", lastEvent);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeEndDocument()
      */
     @Override
     public void writeEndDocument() throws IOException {
         // * @pre {getTagDeep() == 0}
         final int tagDeep = impl.getTagDeep();
         assertPre(0 == tagDeep, "writeEndDocument: tag deepness should be 0: ", tagDeep);
 
         impl.writeEndDocument();
 
         // * @post {getLastEvent() == END_DOCUMENT}
         lastEvent = impl.getLastEvent();
         assertPost(END_DOCUMENT == lastEvent,
                 "writeEndDocument: last event should be END_DOCUMENT: ", lastEvent);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeEndElement()
      */
     @Override
     public void writeEndElement() throws IOException {
         // * @pre { (getLastEvent().isValue() AND getWrittenValueCount() == getValueLength()}) ||
         // * getLastEvent() IN ( START_ELEMENT, END_ELEMENT, ATTRIBUTES_END, COMMENT}
         lastEvent = impl.getLastEvent();
         if (lastEvent.isValue()) {
             assertPre(impl.getWrittenValueCount() == impl.getValueLength(),
                     "writeEndElement: value length and written value count do not match");
         } else {
             assertPre(
                     lastEvent == START_ELEMENT || lastEvent == END_ELEMENT
                             || lastEvent == ATTRIBUTES_END || lastEvent == COMMENT,
                     "writeEndElement: last event should be one of START_ELEMENT, ATTRIBUTES_END or COMMENT: ",
                     lastEvent);
         }
         // * @pre {getTagDeep() > 0}
         assertPre(impl.getTagDeep() > 0, "writeEndElement: there are no open tags");
 
         impl.writeEndElement();
 
         // * @post {getLastEvent() == END_ELEMENT}
         assertPost(END_ELEMENT == impl.getLastEvent(),
                 "writeEndElement: last event should be END_ELEMENT: ", lastEvent);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartAttribute(java.lang.String,
      *      java.lang.String)
      */
     @Override
     public void writeStartAttribute(String namespaceUri, String localName) throws IOException {
         assertPre(namespaceUri != null && localName != null,
                 "writeStartAttribute: namespace and localName can't be null");
         impl.writeStartAttribute(namespaceUri, localName);
     }
 
     /**
      * @param qname
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartAttribute(javax.xml.namespace.QName)
      */
     @Override
     public void writeStartAttribute(QName qname) throws IOException {
         assertPre(qname != null, "writeStartAttribute: qName can't be null");
         impl.writeStartAttribute(qname);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartDocument()
      */
     @Override
     public void writeStartDocument() throws IOException {
         // * @pre {getLastEvent() == NONE}
         lastEvent = impl.getLastEvent();
         assertPre(NONE == lastEvent || NAMESPACE_DECL == lastEvent,
                 "writeStartDocument: last event should be NONE or NAMESPACE_DECL: ", lastEvent);
         impl.writeStartDocument();
 
         // * @post {getLastEvent() == START_DOCUMENT}
         lastEvent = impl.getLastEvent();
         assertPre(START_DOCUMENT == lastEvent,
                 "writeStartDocument: last event should be START_DOCUMENT: ", lastEvent);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartElement(java.lang.String,
      *      java.lang.String)
      */
     @Override
     public void writeStartElement(String namespaceUri, String localName) throws IOException {
         assertPre(namespaceUri != null, "writeStartElement: namespaceUri can't be null");
         assertPre(localName != null, "writeStartElement: localName can't be null");
 
         impl.writeStartElement(namespaceUri, localName);
 
         assertPost(START_ELEMENT == impl.getLastEvent(),
                 "writeStartElement: last event should be START_ELEMENT");
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStartElement(javax.xml.namespace.QName)
      */
     @Override
     public void writeStartElement(QName qname) throws IOException {
         assertPre(qname != null, "writeStartElement: qname can't be null");
 
         impl.writeStartElement(qname);
 
         assertPost(START_ELEMENT == impl.getLastEvent(),
                 "writeStartElement: last event should be START_ELEMENT");
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(java.lang.String)
      */
     @Override
     public void writeValue(final String value) throws IOException {
         assertPreValue("writeValue(String)");
         assertPre(value != null, "writeValue(String value): value can't be null");
         impl.writeValue(value);
 
         // * @post {getLastEvent() == VALUE_STRING}
         assertPostValue(VALUE_STRING);
 
         // * @post {getValueLength() == 1}
         long valueLength = impl.getValueLength();
         assertPost(1 == valueLength, "writeValue(String): value lenght should be 1: ", valueLength);
         // * @post {getWrittenValueCount() == 1}
         assertPostValueWrritenCountAndValueLength(0, 0, 1);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(char[], int, int)
      */
     @Override
     public void writeValue(char[] chars, int offset, int length) throws IOException {
         assertPreValue("writeValue(String)");
 
         impl.writeValue(chars, offset, length);
 
         // * @post {getLastEvent() == VALUE_STRING}
         assertPostValue(VALUE_STRING);
         // * @post {getValueLength() == 1}
         assertPost(1 == impl.getValueLength(), "writeValue(String): value lenght should be 1");
         // * @post {getWrittenValueCount() == 1}
         assertPostValueWrritenCountAndValueLength(0, 0, 1);
     }
 
     /**
      * @param value
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(int)
      */
     @Override
     public void writeValue(int value) throws IOException {
         assertPreValue("writeValue(int)");
         final long preValueLength = impl.getValueLength();
         final long preWrittenValueCount = impl.getWrittenValueCount();
 
         impl.writeValue(value);
         assertPostValue(VALUE_INT);
         assertPostValueWrritenCountAndValueLength(preValueLength, preWrittenValueCount, 1);
     }
 
     /**
      * @param value
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(long)
      */
     @Override
     public void writeValue(long value) throws IOException {
         assertPreValue("writeValue(long)");
         final long preValueLength = impl.getValueLength();
         final long preWrittenValueCount = impl.getWrittenValueCount();
         impl.writeValue(value);
         assertPostValue(VALUE_LONG);
         assertPostValueWrritenCountAndValueLength(preValueLength, preWrittenValueCount, 1);
     }
 
     /**
      * @param value
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(float)
      */
     @Override
     public void writeValue(float value) throws IOException {
         assertPreValue("writeValue(float)");
         final long preValueLength = impl.getValueLength();
         final long preWrittenValueCount = impl.getWrittenValueCount();
         impl.writeValue(value);
         assertPostValue(VALUE_FLOAT);
         assertPostValueWrritenCountAndValueLength(preValueLength, preWrittenValueCount, 1);
     }
 
     /**
      * @param value
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(double)
      */
     @Override
     public void writeValue(double value) throws IOException {
         assertPreValue("writeValue(double)");
         final long preValueLength = impl.getValueLength();
         final long preWrittenValueCount = impl.getWrittenValueCount();
         impl.writeValue(value);
         assertPostValue(VALUE_DOUBLE);
         assertPostValueWrritenCountAndValueLength(preValueLength, preWrittenValueCount, 1);
     }
 
     /**
      * @param value
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(boolean)
      */
     @Override
     public void writeValue(boolean value) throws IOException {
         assertPreValue("writeValue(boolean");
         final long preValueLength = impl.getValueLength();
         final long preWrittenValueCount = impl.getWrittenValueCount();
         impl.writeValue(value);
         assertPostValue(VALUE_BOOL);
         assertPostValueWrritenCountAndValueLength(preValueLength, preWrittenValueCount, 1);
     }
 
     /**
      * @param value
      * @param offset
      * @param length
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(boolean[], int, int)
      */
     @Override
     public void writeValue(boolean[] value, int offset, int length) throws IOException {
         assertPreValue("writeValue(boolean[], int, int)");
         final long preValueLength = impl.getValueLength();
         final long preWrittenValueCount = impl.getWrittenValueCount();
         impl.writeValue(value, offset, length);
         assertPostValue(VALUE_BOOL);
         assertPostValueWrritenCountAndValueLength(preValueLength, preWrittenValueCount, length);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(byte)
      */
     @Override
     public void writeValue(byte value) throws IOException {
         assertPreValue("writeValue(byte");
         final long preValueLength = impl.getValueLength();
         final long preWrittenValueCount = impl.getWrittenValueCount();
         impl.writeValue(value);
         assertPostValue(VALUE_BYTE);
         assertPostValueWrritenCountAndValueLength(preValueLength, preWrittenValueCount, 1);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(byte[], int, int)
      */
     @Override
     public void writeValue(final byte[] value, final int offset, final int length)
             throws IOException {
         assertPreValue("writeValue(byte[], int, int)");
         final long preValueLength = impl.getValueLength();
         final long preWrittenValueCount = impl.getWrittenValueCount();
         impl.writeValue(value, offset, length);
         assertPostValue(VALUE_BYTE);
         assertPostValueWrritenCountAndValueLength(preValueLength, preWrittenValueCount, length);
     }
 
     /**
      * @param value
      * @param offset
      * @param length
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(int[], int, int)
      */
     @Override
     public void writeValue(int[] value, int offset, int length) throws IOException {
         assertPreValue("writeValue(int[], int, int)");
         final long preValueLength = impl.getValueLength();
         final long preWrittenValueCount = impl.getWrittenValueCount();
         impl.writeValue(value, offset, length);
         assertPostValue(VALUE_INT);
         assertPostValueWrritenCountAndValueLength(preValueLength, preWrittenValueCount, length);
     }
 
     /**
      * @param value
      * @param offset
      * @param length
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(long[], int, int)
      */
     @Override
     public void writeValue(long[] value, int offset, int length) throws IOException {
         assertPreValue("writeValue(long[], int, int)");
         final long preValueLength = impl.getValueLength();
         final long preWrittenValueCount = impl.getWrittenValueCount();
         impl.writeValue(value, offset, length);
         assertPostValue(VALUE_LONG);
         assertPostValueWrritenCountAndValueLength(preValueLength, preWrittenValueCount, length);
     }
 
     /**
      * @param value
      * @param offset
      * @param length
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(float[], int, int)
      */
     @Override
     public void writeValue(float[] value, int offset, int length) throws IOException {
         assertPreValue("writeValue(float[], int, int)");
         final long preValueLength = impl.getValueLength();
         final long preWrittenValueCount = impl.getWrittenValueCount();
         impl.writeValue(value, offset, length);
         assertPostValue(VALUE_FLOAT);
         assertPostValueWrritenCountAndValueLength(preValueLength, preWrittenValueCount, length);
     }
 
     /**
      * @param value
      * @param offset
      * @param length
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeValue(double[], int, int)
      */
     @Override
     public void writeValue(double[] value, int offset, int length) throws IOException {
         assertPreValue("writeValue(double[], int, int)");
         final long preValueLength = impl.getValueLength();
         final long preWrittenValueCount = impl.getWrittenValueCount();
         impl.writeValue(value, offset, length);
         assertPostValue(VALUE_DOUBLE);
         assertPostValueWrritenCountAndValueLength(preValueLength, preWrittenValueCount, length);
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#getStringTableReference(java.lang.CharSequence)
      */
     @Override
     public long getStringTableReference(final CharSequence stringValue) throws IOException {
         // * @pre {getLastEvent() IN (START_DOCUMENT, END_ELEMENT, START_ELEMENT)}
         lastEvent = impl.getLastEvent();
         assertPre(START_ELEMENT == lastEvent || ATTRIBUTE == lastEvent
                 || START_DOCUMENT == lastEvent,
                 "last event shall be one of START_DOCUMENT, START_ELEMENT, END_ELEMENT: ",
                 lastEvent);
 
         long ref = impl.getStringTableReference(stringValue);
 
         // * @pos {$return >= 0}
         assertPost(ref > -1, "Returned StringTable reference should be >= 0");
         return ref;
     }
 
     /**
      * @see org.gvsig.bxml.stream.BxmlStreamWriter#writeStringTableValue(long)
      */
     @Override
     public void writeStringTableValue(long longValue) throws IOException {
         // * @pre {getLastEvent().isValue() == true || getLastEvent() IN (START_ELEMENT, ATTRIBUTE
         // )}
         lastEvent = impl.getLastEvent();
         assertPre(
                 lastEvent.isValue() || lastEvent == ATTRIBUTE || lastEvent == START_ELEMENT
                         || lastEvent == ATTRIBUTES_END,
                 "writeStringTableValue: last event shall be a value event or one of START_ELEMENT, ATTRIBUTE, ATTRIBUTES_END: ",
                 lastEvent);
 
         impl.writeStringTableValue(longValue);
 
         // * @post {getLastEvent() == VALUE_STRING}
         lastEvent = impl.getLastEvent();
         assertPost(lastEvent == VALUE_STRING,
                 "writeStringTableValue: last event should be VALUE_STRING: ", lastEvent);
     }
 
     private void assertPreValue(final String methodName) {
         lastEvent = impl.getLastEvent();
         // * @pre {getLastEvent().isValue() == true || getLastEvent() IN (START_ELEMENT, ATTRIBUTE,
        // ATTRIBUTES_END, COMMENT)}
         assertPre(lastEvent.isValue() || lastEvent == START_ELEMENT || lastEvent == ATTRIBUTE
                || lastEvent == ATTRIBUTES_END || lastEvent == COMMENT, methodName,
                 ": last event should be a value event or one of START_ELEMENT, ATTRIBUTE, COMMENT");
     }
 
     private void assertPostValue(final EventType expectedEventType) {
         assertPost(expectedEventType == impl.getLastEvent(), "last event should be ",
                 expectedEventType);
     }
 
     /**
      * Performs the following postcondition checks for all the writeValue methods of a primitive
      * type.
      * <ul>
      * <li>@post {getValueLength() = isArrayInProgress()? $pre:getValueLength() : length}
      * <li>@post {getWrittenValueCount == isArrayInProgress()? $pre:getWrittenValueLength() + length
      * : length}}
      * </ul>
      * 
      * @param preValueLength
      *            the value of {@code impl.getValueLength()} before writing the value(s)
      * @param preWrittenValueCount
      *            the value of {@code getWrittenValueCount()} before writing the value(s)
      * @param expectedIncrease
      *            the expected increase in {@code getWrittenValueCount()} after writing the value(s)
      */
     private void assertPostValueWrritenCountAndValueLength(final long preValueLength,
             final long preWrittenValueCount, final int expectedIncrease) {
         final long postValueLength = impl.getValueLength();
         final long postWrittenValueCount = impl.getWrittenValueCount();
 
         final boolean isArrayInProgress = impl.isArrayInProgress();
 
         if (isArrayInProgress) {
             if (preValueLength != postValueLength) {
                 throw new PostconditionViolationException(
                         "An array is being written, pre and post value length should match: "
                                 + preValueLength + ", " + postValueLength);
             }
             if ((preWrittenValueCount + expectedIncrease) != postWrittenValueCount) {
                 throw new PostconditionViolationException("final written value count("
                         + postWrittenValueCount + ") does not match previous value("
                         + preWrittenValueCount + ") plus written count(" + expectedIncrease + ")");
             }
         } else {
             if (postValueLength != expectedIncrease) {
                 throw new PostconditionViolationException("final value length(" + postValueLength
                         + ") should be equal to expected written count: " + expectedIncrease);
             }
            if ((preWrittenValueCount + expectedIncrease) != postWrittenValueCount) {
                // ///if (expectedIncrease != postWrittenValueCount) {
                 throw new PostconditionViolationException("final written value count("
                         + postWrittenValueCount
                         + ") does not match the required written element count: "
                         + expectedIncrease);
             }
 
         }
     }
 }
