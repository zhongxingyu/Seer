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
 
 
 package com.sun.xml.fastinfoset;
 
 import com.sun.xml.fastinfoset.util.CharArray;
 import com.sun.xml.fastinfoset.util.CharArrayArray;
 import com.sun.xml.fastinfoset.util.ContiguousCharArrayArray;
 import com.sun.xml.fastinfoset.util.QualifiedNameArray;
 import com.sun.xml.fastinfoset.util.StringArray;
 import com.sun.xml.fastinfoset.vocab.ParserVocabulary;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.jvnet.fastinfoset.FastInfosetException;
 import org.jvnet.fastinfoset.FastInfosetParser;
 import org.jvnet.fastinfoset.ReferencedVocabulary;
 import org.jvnet.fastinfoset.Vocabulary;
 
 public abstract class Decoder implements FastInfosetParser {
 
     // String interning system property
     public static final String STRING_INTERNING_SYSTEM_PROPERTY =
         "com.sun.xml.fastinfoset.parser.string-interning";
 
     // Buffer size system property
     public static final String BUFFER_SIZE_SYSTEM_PROPERTY =
         "com.sun.xml.fastinfoset.parser.buffer-size";
 
     protected static boolean _stringInterningSystemDefault = false;
     
     protected static int _bufferSizeSystemDefault = 1024;
 
     protected static QualifiedName DEFAULT_NAMESPACE_DECLARATION = new QualifiedName(
             "",
             EncodingConstants.XMLNS_NAMESPACE_NAME,
             EncodingConstants.XMLNS_NAMESPACE_PREFIX,
             EncodingConstants.XMLNS_NAMESPACE_PREFIX);
     
     static {
         String p = System.getProperty(STRING_INTERNING_SYSTEM_PROPERTY,
             Boolean.toString(_stringInterningSystemDefault));
         _stringInterningSystemDefault = Boolean.valueOf(p).booleanValue();
 
         p = System.getProperty(BUFFER_SIZE_SYSTEM_PROPERTY,
             Integer.toString(_bufferSizeSystemDefault));
         try {
             int i = Integer.valueOf(p).intValue();
             if (i > 0) {
                 _bufferSizeSystemDefault = i;
             }
         } catch (NumberFormatException e) {
         }
     }
 
     
     protected boolean _stringInterning = _stringInterningSystemDefault;
 
     protected int _bufferSize = _bufferSizeSystemDefault;
     
     protected InputStream _s;
 
     protected Map _externalVocabularies;    
 
     protected Map _registeredEncodingAlgorithms = new HashMap();
     
     protected ParserVocabulary _v;
     
     protected boolean _vIsInternal;
 
     protected List _notations;
 
     protected List _unparsedEntities;
 
     protected int _b;
 
     protected boolean _terminate;
 
     protected boolean _doubleTerminate;
 
     protected boolean _addToTable;
 
     protected int _integer;
 
     protected int _identifier;
 
     protected byte[] _octetBuffer = new byte[_bufferSizeSystemDefault];
 
     protected int _octetBufferStart;
 
     protected int _octetBufferOffset;
 
     protected int _octetBufferEnd;
 
     protected int _octetBufferLength;
 
     protected EncodingAlgorithmInputStream _encodingAlgorithmInputStream = new EncodingAlgorithmInputStream();
 
     protected char[] _charBuffer = new char[512];
 
     protected int _charBufferLength;
 
     public Decoder() {
         _v = new ParserVocabulary();
         _vIsInternal = true;
     }
 
     
     // FastInfosetParser
         
     public void setStringInterning(boolean stringInterning) {
         _stringInterning = stringInterning;
     } 
     
     public boolean getStringInterning() {
         return _stringInterning;
     }
     
     public void setBufferSize(int bufferSize) {
         if (_bufferSize > _octetBuffer.length) {
             _bufferSize = bufferSize;
         }
     }
     
     public int getBufferSize() {
         return _bufferSize;
     }
     
     public void setRegisteredEncodingAlgorithms(Map algorithms) {
         _registeredEncodingAlgorithms = algorithms;
         if (_registeredEncodingAlgorithms == null) {
             _registeredEncodingAlgorithms = new HashMap();
         }
     }
     
     public Map getRegisteredEncodingAlgorithms() {
         return _registeredEncodingAlgorithms;
     }
     
     public void setExternalVocabularies(Map referencedVocabualries) {
         throw new UnsupportedOperationException();
     }
 
     public void setDynamicVocabulary(Vocabulary v) {
         throw new UnsupportedOperationException();
     }
 
     public ReferencedVocabulary getExternalVocabulary() {
         throw new UnsupportedOperationException();
     }
 
     public Vocabulary getIntitialVocabulary() {
         throw new UnsupportedOperationException();
     }
 
     public Vocabulary getDynamicVocabulary() {
         throw new UnsupportedOperationException();
     }
 
     public Vocabulary getFinalVocabulary() {
         throw new UnsupportedOperationException();
     }
     
     
     
     public void reset() {
         _terminate = _doubleTerminate = false;
     }
 
     public void setVocabulary(ParserVocabulary v) {
         _v = v;
         _vIsInternal = false;
     }
 
     public void setInputStream(InputStream s) {
         _s = s;
         _octetBufferOffset = 0;
         _octetBufferEnd = 0;
         if (_vIsInternal == true) {
             _v.clear();
         }
     }
 
     public final void decodeDII() throws FastInfosetException, IOException {
         final int b = read();
         if (b == EncodingConstants.DOCUMENT_INITIAL_VOCABULARY_FLAG) {
             decodeInitialVocabulary();
         } else if (b != 0) {
             throw new IOException("Optional values (other than initial vocabulary) of DII not supported");
         }
     }
 
     public final void decodeInitialVocabulary() throws FastInfosetException, IOException {
         // First 5 optionals of 13 bit optional field
         int b = read();
         // Next 8 optionals of 13 bit optional field
         int b2 = read();
 
         // Optimize for the most common case
         if (b == EncodingConstants.INITIAL_VOCABULARY_EXTERNAL_VOCABULARY_FLAG && b2 == 0) {
             decodeExternalVocabularyURI();
             return;
         }
 
         if ((b & EncodingConstants.INITIAL_VOCABULARY_EXTERNAL_VOCABULARY_FLAG) > 0) {
             decodeExternalVocabularyURI();
         }
 
         if ((b & EncodingConstants.INITIAL_VOCABULARY_RESTRICTED_ALPHABETS_FLAG) > 0) {
             decodeTableItems(_v.restrictedAlphabet);
         }
 
         if ((b & EncodingConstants.INITIAL_VOCABULARY_ENCODING_ALGORITHMS_FLAG) > 0) {
             decodeTableItems(_v.encodingAlgorithm);
         }
 
         if ((b & EncodingConstants.INITIAL_VOCABULARY_PREFIXES_FLAG) > 0) {
             decodeTableItems(_v.prefix);
         }
 
         if ((b & EncodingConstants.INITIAL_VOCABULARY_NAMESPACE_NAMES_FLAG) > 0) {
             decodeTableItems(_v.namespaceName);
         }
 
         if ((b2 & EncodingConstants.INITIAL_VOCABULARY_LOCAL_NAMES_FLAG) > 0) {
             decodeTableItems(_v.localName);
         }
 
         if ((b2 & EncodingConstants.INITIAL_VOCABULARY_OTHER_NCNAMES_FLAG) > 0) {
             decodeTableItems(_v.otherNCName);
         }
 
         if ((b2 & EncodingConstants.INITIAL_VOCABULARY_OTHER_URIS_FLAG) > 0) {
             decodeTableItems(_v.otherURI);
         }
 
         if ((b2 & EncodingConstants.INITIAL_VOCABULARY_ATTRIBUTE_VALUES_FLAG) > 0) {
             decodeTableItems(_v.attributeValue);
         }
 
         if ((b2 & EncodingConstants.INITIAL_VOCABULARY_CONTENT_CHARACTER_CHUNKS_FLAG) > 0) {
             decodeTableItems(_v.characterContentChunk);
         }
 
         if ((b2 & EncodingConstants.INITIAL_VOCABULARY_OTHER_STRINGS_FLAG) > 0) {
             decodeTableItems(_v.otherString);
         }
 
         if ((b2 & EncodingConstants.INITIAL_VOCABULARY_ELEMENT_NAME_SURROGATES_FLAG) > 0) {
             decodeTableItems(_v.elementName);
         }
 
         if ((b2 & EncodingConstants.INITIAL_VOCABULARY_ATTRIBUTE_NAME_SURROGATES_FLAG) > 0) {
             decodeTableItems(_v.attributeName);
         }
     }
 
     public void decodeExternalVocabularyURI() throws FastInfosetException, IOException {
         if (_externalVocabularies == null) {
             throw new IOException("No external vocabularies registered");
         }
 
         String externalVocabularyURI = decodeNonEmptyOctetStringOnSecondBitAsUtf8String();
         ParserVocabulary externalVocabulary =
             (ParserVocabulary) _externalVocabularies.get(externalVocabularyURI);
         if (externalVocabulary == null) {
             throw new FastInfosetException("External vocabulary referenced by \"" + externalVocabularyURI + "\" is not registered");
         }
 
         try {
             _v.setReferencedVocabulary(new URI(externalVocabularyURI), externalVocabulary, false);
         } catch (URISyntaxException e) {
             throw new FastInfosetException("URISyntaxException", e);
         }
     }
 
     public final void decodeTableItems(StringArray array) throws FastInfosetException, IOException {
         for (int i = 0; i < decodeIntegerTableItems(); i++) {
             array.add(decodeNonEmptyOctetStringOnSecondBitAsUtf8String());
         }
     }
 
     public final void decodeTableItems(ContiguousCharArrayArray array) throws FastInfosetException, IOException {
         for (int i = 0; i < decodeIntegerTableItems(); i++) {
             switch(decodeNonIdentifyingStringOnFirstBit()) {
                 case NISTRING_STRING:
                     array.add(_charBuffer, _charBufferLength);
                     break;
                 default:
                     throw new FastInfosetException("Illegal state for decoding of EncodedCharacterString");
             }
         }
     }
     
     public final void decodeTableItems(CharArrayArray array) throws FastInfosetException, IOException {
         for (int i = 0; i < decodeIntegerTableItems(); i++) {
             switch(decodeNonIdentifyingStringOnFirstBit()) {
                 case NISTRING_STRING:
                     array.add(new CharArray(_charBuffer, 0, _charBufferLength, true));
                     break;
                 default:
                     throw new FastInfosetException("Illegal state for decoding of EncodedCharacterString");
             }
         }
     }
 
     public final void decodeTableItems(QualifiedNameArray array) throws FastInfosetException, IOException {
         for (int i = 0; i < decodeIntegerTableItems(); i++) {
             final int b = read();
 
             final String prefix = ((b & EncodingConstants.NAME_SURROGATE_PREFIX_FLAG) > 0)
                         ? _v.prefix.get(decodeIntegerIndexOnSecondBit()) : "";
             final String namespaceName = ((b & EncodingConstants.NAME_SURROGATE_NAME_FLAG) > 0)
                         ? _v.namespaceName.get(decodeIntegerIndexOnSecondBit()) : "";
             if (namespaceName == "" && prefix != "") {
                 throw new FastInfosetException("Name surrogate prefix is present when namespace name is absent");
             }
 
             final String localName = _v.localName.get(decodeIntegerIndexOnSecondBit());
 
             QualifiedName qualifiedName = new QualifiedName(prefix, namespaceName, localName);
             array.add(qualifiedName);
         }
     }
 
     public final int decodeIntegerTableItems() throws IOException {
         final int b = read();
         if (b < 128) {
             return b;
         } else {
             return ((b & 0x0F) << 16) | (read() << 8) | read();
         }
     }
 
     public final void decodeNotations() throws FastInfosetException, IOException {
         if (_notations == null) {
             _notations = new ArrayList();
         } else {
             _notations.clear();
         }
 
         int b = read();
         while ((b & EncodingConstants.NOTATIONS_MASK) == EncodingConstants.NOTATIONS) {
             String name = decodeIdentifyingNonEmptyStringOnFirstBit(_v.otherNCName);
 
             String system_identifier = ((_b & EncodingConstants.NOTATIONS_SYSTEM_IDENTIFIER_FLAG) > 0)
                 ? decodeIdentifyingNonEmptyStringOnFirstBit(_v.otherURI) : "";
             String public_identifier = ((_b & EncodingConstants.NOTATIONS_PUBLIC_IDENTIFIER_FLAG) > 0)
                 ? decodeIdentifyingNonEmptyStringOnFirstBit(_v.otherURI) : "";
 
             Notation notation = new Notation(name, system_identifier, public_identifier);
             _notations.add(notation);
 
             b = read();
         }
         if (b != EncodingConstants.TERMINATOR) {
             throw new FastInfosetException("Notation IIs not terminated correctly");
         }
     }
 
     public final void decodeUnparsedEntities() throws FastInfosetException, IOException {
         if (_unparsedEntities == null) {
             _unparsedEntities = new ArrayList();
         } else {
             _unparsedEntities.clear();
         }
 
         int b = read();
         while ((b & EncodingConstants.UNPARSED_ENTITIES_MASK) == EncodingConstants.UNPARSED_ENTITIES) {
             String name = decodeIdentifyingNonEmptyStringOnFirstBit(_v.otherNCName);
             String system_identifier = decodeIdentifyingNonEmptyStringOnFirstBit(_v.otherURI);
 
             String public_identifier = ((_b & EncodingConstants.UNPARSED_ENTITIES_PUBLIC_IDENTIFIER_FLAG) > 0)
                 ? decodeIdentifyingNonEmptyStringOnFirstBit(_v.otherURI) : "";
 
             String notation_name = decodeIdentifyingNonEmptyStringOnFirstBit(_v.otherNCName);
 
             UnparsedEntity unparsedEntity = new UnparsedEntity(name, system_identifier, public_identifier, notation_name);
             _unparsedEntities.add(unparsedEntity);
 
             b = read();
         }
         if (b != EncodingConstants.TERMINATOR) {
             throw new FastInfosetException("Unparsed entities not terminated correctly");
         }
     }
 
     public final void decodeVersion() throws FastInfosetException, IOException {
         switch(decodeNonIdentifyingStringOnFirstBit()) {
         case NISTRING_STRING:
             if (_addToTable) {
                 _v.otherString.add(new CharArray(_charBuffer, 0, _charBufferLength, true));
             }
             break;
         case NISTRING_ENCODING_ALGORITHM:
             throw new IOException("Processing II with encoding algorithm decoding not supported");
         case NISTRING_INDEX:
             break;
         case NISTRING_EMPTY_STRING:
             break;
         }
     }
 
     protected final QualifiedName decodeEIIIndexMedium() throws FastInfosetException, IOException {
         final int i = (((_b & EncodingConstants.INTEGER_3RD_BIT_MEDIUM_MASK) << 8) | read())
             + EncodingConstants.INTEGER_3RD_BIT_SMALL_LIMIT;
         return _v.elementName.get(i);
     }
 
     protected final QualifiedName decodeEIIIndexLarge() throws FastInfosetException, IOException {
         int i;
         if ((_b & 0x10) > 0) {
             // EII large index
             i = (((_b & EncodingConstants.INTEGER_3RD_BIT_LARGE_MASK) << 16) | (read() << 8) | read())
                 + EncodingConstants.INTEGER_3RD_BIT_MEDIUM_LIMIT;
         } else {
             // EII large large index
             i = (((read() & EncodingConstants.INTEGER_3RD_BIT_LARGE_LARGE_MASK) << 16) | (read() << 8) | read())
                 + EncodingConstants.INTEGER_3RD_BIT_LARGE_LIMIT;
         }
         return _v.elementName.get(i);
     }
 
     protected final QualifiedName decodeEIILiteral() throws FastInfosetException, IOException {
         final String prefix = ((_b & EncodingConstants.LITERAL_QNAME_PREFIX_FLAG) > 0)
             ? decodeIdentifyingNonEmptyStringIndexOnFirstBitAsPrefix(_v.prefix) : "";
         final String namespaceName = ((_b & EncodingConstants.LITERAL_QNAME_NAMESPACE_NAME_FLAG) > 0)
             ? decodeIdentifyingNonEmptyStringIndexOnFirstBitAsNamespaceName(_v.namespaceName) : "";
         final String localName = decodeIdentifyingNonEmptyStringOnFirstBit(_v.localName);
 
         final QualifiedName qualifiedName = new QualifiedName(prefix, namespaceName, localName);
         _v.elementName.add(qualifiedName);
         return qualifiedName;
     }
 
 
     public static final int NISTRING_STRING              = 0;
     public static final int NISTRING_INDEX               = 1;
     public static final int NISTRING_ENCODING_ALGORITHM  = 2;
     public static final int NISTRING_EMPTY_STRING        = 3;
 
     /*
      * C.14
      * decodeNonIdentifyingStringOnFirstBit
      */
     public final int decodeNonIdentifyingStringOnFirstBit() throws FastInfosetException, IOException {
         final int b = read();
         switch(DecoderStateTables.NISTRING[b]) {
             case DecoderStateTables.NISTRING_UTF8_SMALL_LENGTH:
                 _addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                 _octetBufferLength = (b & EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_SMALL_MASK) + 1;
                 decodeUtf8StringAsCharBuffer();
                 return NISTRING_STRING;
             case DecoderStateTables.NISTRING_UTF8_MEDIUM_LENGTH:
                 _addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                 _octetBufferLength = read() + EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_SMALL_LIMIT;
                 decodeUtf8StringAsCharBuffer();
                 return NISTRING_STRING;
             case DecoderStateTables.NISTRING_UTF8_LARGE_LENGTH:
             {
                 _addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                 final int length = (read() << 24) |
                     (read() << 16) |
                     (read() << 8) |
                     read();
                 _octetBufferLength = length + EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_MEDIUM_LIMIT;
                 decodeUtf8StringAsCharBuffer();
                 return NISTRING_STRING;
             }
             case DecoderStateTables.NISTRING_UTF16_SMALL_LENGTH:
                 _addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                 _octetBufferLength = (b & EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_SMALL_MASK) + 1;
                 decodeUtf16StringAsCharBuffer();
                 return NISTRING_STRING;
             case DecoderStateTables.NISTRING_UTF16_MEDIUM_LENGTH:
                 _addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                 _octetBufferLength = read() + EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_SMALL_LIMIT;
                 decodeUtf16StringAsCharBuffer();
                 return NISTRING_STRING;
             case DecoderStateTables.NISTRING_UTF16_LARGE_LENGTH:
             {
                 _addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                 final int length = (read() << 24) |
                     (read() << 16) |
                     (read() << 8) |
                     read();
                 _octetBufferLength = length + EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_MEDIUM_LIMIT;
                 decodeUtf16StringAsCharBuffer();
                 return NISTRING_STRING;
             }
             case DecoderStateTables.NISTRING_RA:
             {
                 _addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                 // Decode resitricted alphabet integer
                 _identifier = (b & 0x0F) << 4;
                 final int b2 = read();
                 _identifier |= (b2 & 0xF0) >> 4;
 
                 decodeOctetsOfNonIdentifyingStringOnFirstBit(b2);
                 // TODO obtain restricted alphabet given _identifier value
                 decodeRAOctetsAsCharBuffer(null);
                 return NISTRING_STRING;
             }
             case DecoderStateTables.NISTRING_EA:
             {
                 _addToTable = (b & EncodingConstants.NISTRING_ADD_TO_TABLE_FLAG) > 0;
                 // Decode encoding algorithm integer
                 _identifier = (b & 0x0F) << 4;
                 final int b2 = read();
                 _identifier |= (b2 & 0xF0) >> 4;
 
                 decodeOctetsOfNonIdentifyingStringOnFirstBit(b2);
                 return NISTRING_ENCODING_ALGORITHM;
             }
             case DecoderStateTables.NISTRING_INDEX_SMALL:
                 _integer = b & EncodingConstants.INTEGER_2ND_BIT_SMALL_MASK;
                 return NISTRING_INDEX;
             case DecoderStateTables.NISTRING_INDEX_MEDIUM:
                 _integer = (((b & EncodingConstants.INTEGER_2ND_BIT_MEDIUM_MASK) << 8) | read())
                     + EncodingConstants.INTEGER_2ND_BIT_SMALL_LIMIT;
                 return NISTRING_INDEX;
             case DecoderStateTables.NISTRING_INDEX_LARGE:
                 _integer = (((b & EncodingConstants.INTEGER_2ND_BIT_LARGE_MASK) << 16) | (read() << 8) | read())
                     + EncodingConstants.INTEGER_2ND_BIT_MEDIUM_LIMIT;
                 return NISTRING_INDEX;
             case DecoderStateTables.NISTRING_EMPTY:
                 return NISTRING_EMPTY_STRING;
             default:
                 throw new FastInfosetException("Illegal state when decoding non identifying string");
         }
     }
 
     public final void decodeOctetsOfNonIdentifyingStringOnFirstBit(int b) throws FastInfosetException, IOException {
        // Remove top 4 bits of restricted alphabet or encoding algorithm integer
         b &= 0x0F;
         // Reuse UTF8 length states
         switch(DecoderStateTables.NISTRING[b]) {
             case DecoderStateTables.NISTRING_UTF8_SMALL_LENGTH:
                 _octetBufferLength = b + 1;
                 break;
             case DecoderStateTables.NISTRING_UTF8_MEDIUM_LENGTH:
                 _octetBufferLength = read() + EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_SMALL_LIMIT;
                 break;
             case DecoderStateTables.NISTRING_UTF8_LARGE_LENGTH:
                 final int length = (read() << 24) |
                     (read() << 16) |
                     (read() << 8) |
                     read();
                 _octetBufferLength = length + EncodingConstants.OCTET_STRING_LENGTH_5TH_BIT_MEDIUM_LIMIT;
                 break;
             default:
                 throw new FastInfosetException("Illegal state when decoding octets");
         }
         ensureOctetBufferSize();
         _octetBufferStart = _octetBufferOffset;
         _octetBufferOffset += _octetBufferLength;
     }
 
     public final void decodeOctetsOfNonIdentifyingStringOnThirdBit(int b) throws FastInfosetException, IOException {
        // Remove top 6 bits of restricted alphabet or encoding algorithm integer
        switch (b & 0x03) {
            case 0:
                _octetBufferLength = 1;
                break;
            case 1:
                _octetBufferLength = 2;
                 break;
            case 2:
                 _octetBufferLength = read() + EncodingConstants.OCTET_STRING_LENGTH_7TH_BIT_SMALL_LIMIT;
                 break;
            case 3:
                 _octetBufferLength = (read() << 24) |
                     (read() << 16) |
                     (read() << 8) |
                     read();
                 _octetBufferLength += EncodingConstants.OCTET_STRING_LENGTH_7TH_BIT_MEDIUM_LIMIT;
                 break;
         }

         ensureOctetBufferSize();
         _octetBufferStart = _octetBufferOffset;
         _octetBufferOffset += _octetBufferLength;
     }
 
     /*
      * C.13
      */
     public final String decodeIdentifyingNonEmptyStringOnFirstBit(StringArray table) throws FastInfosetException, IOException {
         final int b = read();
         switch(DecoderStateTables.ISTRING[b]) {
             case DecoderStateTables.ISTRING_SMALL_LENGTH:
             {
                 _octetBufferLength = b + 1;
                 final String s = (_stringInterning) ? decodeUtf8StringAsString().intern() : decodeUtf8StringAsString();
                 table.add(s);
                 return s;
             }
             case DecoderStateTables.ISTRING_MEDIUM_LENGTH:
             {
                 _octetBufferLength = read() + EncodingConstants.OCTET_STRING_LENGTH_2ND_BIT_SMALL_LIMIT;
                 final String s = (_stringInterning) ? decodeUtf8StringAsString().intern() : decodeUtf8StringAsString();
                 table.add(s);
                 return s;
             }
             case DecoderStateTables.ISTRING_LARGE_LENGTH:
             {
                 final int length = (read() << 24) |
                     (read() << 16) |
                     (read() << 8) |
                     read();
                 _octetBufferLength = length + EncodingConstants.OCTET_STRING_LENGTH_2ND_BIT_MEDIUM_LIMIT;
                 final String s = (_stringInterning) ? decodeUtf8StringAsString().intern() : decodeUtf8StringAsString();
                 table.add(s);
                 return s;
             }
             case DecoderStateTables.ISTRING_INDEX_SMALL:
                 return table.get(b & EncodingConstants.INTEGER_2ND_BIT_SMALL_MASK);
             case DecoderStateTables.ISTRING_INDEX_MEDIUM:
             {
                 final int index = (((b & EncodingConstants.INTEGER_2ND_BIT_MEDIUM_MASK) << 8) | read())
                     + EncodingConstants.INTEGER_2ND_BIT_SMALL_LIMIT;
                 return table.get(index);
             }
             case DecoderStateTables.ISTRING_INDEX_LARGE:
             {
                 final int index = (((b & EncodingConstants.INTEGER_2ND_BIT_LARGE_MASK) << 16) | (read() << 8) | read())
                     + EncodingConstants.INTEGER_2ND_BIT_MEDIUM_LIMIT;
                 return table.get(index);
             }
             default:
                 throw new FastInfosetException("Illegal state when decoding identifying string on first bit");
         }
     }
 
     /*
      * C.13
      */
     public final String decodeIdentifyingNonEmptyStringOnFirstBitAsPrefix(StringArray table) throws FastInfosetException, IOException {
         final int b = read();
         switch(DecoderStateTables.ISTRING_PREFIX_NAMESPACE[b]) {
             case DecoderStateTables.ISTRING_PREFIX_NAMESPACE_LENGTH_3:
             {
                 _octetBufferLength = EncodingConstants.XML_NAMESPACE_PREFIX_LENGTH;
                 decodeUtf8StringAsCharBuffer();
                 
                 if (_charBuffer[0] == 'x' &&
                         _charBuffer[1] == 'm' &&
                         _charBuffer[2] == 'l') {
                     throw new FastInfosetException("The literal identifying string for the \"xml\" prefix is illegal");
                 }
                 
                 final String s = (_stringInterning) ? new String(_charBuffer, 0, _charBufferLength).intern() :
                     new String(_charBuffer, 0, _charBufferLength);
                 table.add(s);
                 return s;
             }
             case DecoderStateTables.ISTRING_PREFIX_NAMESPACE_LENGTH_5:
             {
                 _octetBufferLength = EncodingConstants.XMLNS_NAMESPACE_PREFIX_LENGTH;
                 decodeUtf8StringAsCharBuffer();
                 
                 if (_charBuffer[0] == 'x' &&
                         _charBuffer[1] == 'm' &&
                         _charBuffer[2] == 'l' &&
                         _charBuffer[3] == 'n' &&
                         _charBuffer[4] == 's') {
                     throw new FastInfosetException("The prefix \"xmlns\" cannot be bound to any namespace explicitly");
                 }
                 
                 final String s = (_stringInterning) ? new String(_charBuffer, 0, _charBufferLength).intern() :
                     new String(_charBuffer, 0, _charBufferLength);
                 table.add(s);
                 return s;
             }
             case DecoderStateTables.ISTRING_SMALL_LENGTH:
             case DecoderStateTables.ISTRING_PREFIX_NAMESPACE_LENGTH_29:
             case DecoderStateTables.ISTRING_PREFIX_NAMESPACE_LENGTH_36:
             {
                 _octetBufferLength = b + 1;
                 final String s = (_stringInterning) ? decodeUtf8StringAsString().intern() : decodeUtf8StringAsString();
                 table.add(s);
                 return s;
             }
             case DecoderStateTables.ISTRING_MEDIUM_LENGTH:
             {
                 _octetBufferLength = read() + EncodingConstants.OCTET_STRING_LENGTH_2ND_BIT_SMALL_LIMIT;
                 final String s = (_stringInterning) ? decodeUtf8StringAsString().intern() : decodeUtf8StringAsString();
                 table.add(s);
                 return s;
             }
             case DecoderStateTables.ISTRING_LARGE_LENGTH:
             {
                 final int length = (read() << 24) |
                     (read() << 16) |
                     (read() << 8) |
                     read();
                 _octetBufferLength = length + EncodingConstants.OCTET_STRING_LENGTH_2ND_BIT_MEDIUM_LIMIT;
                 final String s = (_stringInterning) ? decodeUtf8StringAsString().intern() : decodeUtf8StringAsString();
                 table.add(s);
                 return s;
             }
             case DecoderStateTables.ISTRING_PREFIX_NAMESPACE_INDEX_ZERO:
                 return EncodingConstants.XML_NAMESPACE_PREFIX;
             case DecoderStateTables.ISTRING_INDEX_SMALL:
                 return table.get((b & EncodingConstants.INTEGER_2ND_BIT_SMALL_MASK) - 1);
             case DecoderStateTables.ISTRING_INDEX_MEDIUM:
             {
                 final int index = (((b & EncodingConstants.INTEGER_2ND_BIT_MEDIUM_MASK) << 8) | read())
                     + (EncodingConstants.INTEGER_2ND_BIT_SMALL_LIMIT - 1);
                 return table.get(index);
             }
             case DecoderStateTables.ISTRING_INDEX_LARGE:
             {
                 final int index = (((b & EncodingConstants.INTEGER_2ND_BIT_LARGE_MASK) << 16) | (read() << 8) | read())
                     + (EncodingConstants.INTEGER_2ND_BIT_MEDIUM_LIMIT - 1);
                 return table.get(index);
             }
             default:
                 throw new FastInfosetException("Illegal state when decoding identifying string for prefix on first bit");
         }
     }
     
     /*
      * C.13
      */
     public final String decodeIdentifyingNonEmptyStringIndexOnFirstBitAsPrefix(StringArray table) throws FastInfosetException, IOException {
         final int b = read();
         switch(DecoderStateTables.ISTRING_PREFIX_NAMESPACE[b]) {
             case DecoderStateTables.ISTRING_PREFIX_NAMESPACE_INDEX_ZERO:
                 return EncodingConstants.XML_NAMESPACE_PREFIX;
             case DecoderStateTables.ISTRING_INDEX_SMALL:
                 return table.get((b & EncodingConstants.INTEGER_2ND_BIT_SMALL_MASK) - 1);
             case DecoderStateTables.ISTRING_INDEX_MEDIUM:
             {
                 final int index = (((b & EncodingConstants.INTEGER_2ND_BIT_MEDIUM_MASK) << 8) | read())
                     + (EncodingConstants.INTEGER_2ND_BIT_SMALL_LIMIT - 1);
                 return table.get(index);
             }
             case DecoderStateTables.ISTRING_INDEX_LARGE:
             {
                 final int index = (((b & EncodingConstants.INTEGER_2ND_BIT_LARGE_MASK) << 16) | (read() << 8) | read())
                     + (EncodingConstants.INTEGER_2ND_BIT_MEDIUM_LIMIT - 1);
                 return table.get(index);
             }
             default:
                 throw new FastInfosetException("Illegal state when decoding identifying string for prefix on first bit");
         }
     }
     
     /*
      * C.13
      */
     public final String decodeIdentifyingNonEmptyStringOnFirstBitAsNamespaceName(StringArray table) throws FastInfosetException, IOException {
         final int b = read();
         switch(DecoderStateTables.ISTRING_PREFIX_NAMESPACE[b]) {
             case DecoderStateTables.ISTRING_PREFIX_NAMESPACE_LENGTH_3:
             case DecoderStateTables.ISTRING_PREFIX_NAMESPACE_LENGTH_5:
             case DecoderStateTables.ISTRING_SMALL_LENGTH:
             {
                 _octetBufferLength = b + 1;
                 final String s = (_stringInterning) ? decodeUtf8StringAsString().intern() : decodeUtf8StringAsString();
                 table.add(s);
                 return s;
             }
             case DecoderStateTables.ISTRING_PREFIX_NAMESPACE_LENGTH_29:
             {
                 _octetBufferLength = EncodingConstants.XMLNS_NAMESPACE_NAME_LENGTH;
                 decodeUtf8StringAsCharBuffer();
                 
                 if (compareCharsWithCharBufferFromEndToStart(EncodingConstants.XMLNS_NAMESPACE_NAME_CHARS)) {
                     throw new FastInfosetException("The namespace \"http://www.w3.org/2000/xmlns/\" cannot be bound to any prfix explicitly");
                 }
                 
                 final String s = (_stringInterning) ? new String(_charBuffer, 0, _charBufferLength).intern() :
                     new String(_charBuffer, 0, _charBufferLength);
                 table.add(s);
                 return s;
             }
             case DecoderStateTables.ISTRING_PREFIX_NAMESPACE_LENGTH_36:
             {
                 _octetBufferLength = EncodingConstants.XML_NAMESPACE_NAME_LENGTH;
                 decodeUtf8StringAsCharBuffer();
                 
                 if (compareCharsWithCharBufferFromEndToStart(EncodingConstants.XML_NAMESPACE_NAME_CHARS)) {
                     throw new FastInfosetException("The literal identifying string for the \"http://www.w3.org/XML/1998/namespace\" namespace name is illegal");
                 }
                 
                 final String s = (_stringInterning) ? new String(_charBuffer, 0, _charBufferLength).intern() :
                     new String(_charBuffer, 0, _charBufferLength);
                 table.add(s);
                 return s;
             }
             case DecoderStateTables.ISTRING_MEDIUM_LENGTH:
             {
                 _octetBufferLength = read() + EncodingConstants.OCTET_STRING_LENGTH_2ND_BIT_SMALL_LIMIT;
                 final String s = (_stringInterning) ? decodeUtf8StringAsString().intern() : decodeUtf8StringAsString();
                 table.add(s);
                 return s;
             }
             case DecoderStateTables.ISTRING_LARGE_LENGTH:
             {
                 final int length = (read() << 24) |
                     (read() << 16) |
                     (read() << 8) |
                     read();
                 _octetBufferLength = length + EncodingConstants.OCTET_STRING_LENGTH_2ND_BIT_MEDIUM_LIMIT;
                 final String s = (_stringInterning) ? decodeUtf8StringAsString().intern() : decodeUtf8StringAsString();
                 table.add(s);
                 return s;
             }
             case DecoderStateTables.ISTRING_PREFIX_NAMESPACE_INDEX_ZERO:
                 return EncodingConstants.XML_NAMESPACE_NAME;
             case DecoderStateTables.ISTRING_INDEX_SMALL:
                 return table.get((b & EncodingConstants.INTEGER_2ND_BIT_SMALL_MASK) - 1);
             case DecoderStateTables.ISTRING_INDEX_MEDIUM:
             {
                 final int index = (((b & EncodingConstants.INTEGER_2ND_BIT_MEDIUM_MASK) << 8) | read())
                     + (EncodingConstants.INTEGER_2ND_BIT_SMALL_LIMIT - 1);
                 return table.get(index);
             }
             case DecoderStateTables.ISTRING_INDEX_LARGE:
             {
                 final int index = (((b & EncodingConstants.INTEGER_2ND_BIT_LARGE_MASK) << 16) | (read() << 8) | read())
                     + (EncodingConstants.INTEGER_2ND_BIT_MEDIUM_LIMIT - 1);
                 return table.get(index);
             }
             default:
                 throw new FastInfosetException("Illegal state when decoding identifying string for namespace name on first bit");
         }
     }
     
     /*
      * C.13
      */
     public final String decodeIdentifyingNonEmptyStringIndexOnFirstBitAsNamespaceName(StringArray table) throws FastInfosetException, IOException {
         final int b = read();
         switch(DecoderStateTables.ISTRING_PREFIX_NAMESPACE[b]) {
             case DecoderStateTables.ISTRING_PREFIX_NAMESPACE_INDEX_ZERO:
                 return EncodingConstants.XML_NAMESPACE_NAME;
             case DecoderStateTables.ISTRING_INDEX_SMALL:
                 return table.get((b & EncodingConstants.INTEGER_2ND_BIT_SMALL_MASK) - 1);
             case DecoderStateTables.ISTRING_INDEX_MEDIUM:
             {
                 final int index = (((b & EncodingConstants.INTEGER_2ND_BIT_MEDIUM_MASK) << 8) | read())
                     + (EncodingConstants.INTEGER_2ND_BIT_SMALL_LIMIT - 1);
                 return table.get(index);
             }
             case DecoderStateTables.ISTRING_INDEX_LARGE:
             {
                 final int index = (((b & EncodingConstants.INTEGER_2ND_BIT_LARGE_MASK) << 16) | (read() << 8) | read())
                     + (EncodingConstants.INTEGER_2ND_BIT_MEDIUM_LIMIT - 1);
                 return table.get(index);
             }
             default:
                 throw new FastInfosetException("Illegal state when decoding identifying string for namespace name on first bit");
         }
     }
     
     public final boolean compareCharsWithCharBufferFromEndToStart(char[] c) {
         int i = _charBufferLength ;
         while (--i >= 0) {
             if (c[i] != _charBuffer[i]) {
                 return false;
             }
         }        
         return true;
     }
     
     /*
      * C.22
      */
     public final String decodeNonEmptyOctetStringOnSecondBitAsUtf8String() throws FastInfosetException, IOException {
         decodeNonEmptyOctetStringOnSecondBitAsUtf8CharArray();
         return new String(_charBuffer, 0, _charBufferLength);
     }
 
     /*
      * C.22
      */
     public final void decodeNonEmptyOctetStringOnSecondBitAsUtf8CharArray() throws FastInfosetException, IOException {
         final int b = read();
         switch(DecoderStateTables.ISTRING[b]) {
             case DecoderStateTables.ISTRING_SMALL_LENGTH:
                 _octetBufferLength = b + 1;
                 break;
             case DecoderStateTables.ISTRING_MEDIUM_LENGTH:
                 _octetBufferLength = read() + EncodingConstants.OCTET_STRING_LENGTH_2ND_BIT_SMALL_LIMIT;
                 break;
             case DecoderStateTables.ISTRING_LARGE_LENGTH:
             {
                 final int length = (read() << 24) |
                     (read() << 16) |
                     (read() << 8) |
                     read();
                 _octetBufferLength = length + EncodingConstants.OCTET_STRING_LENGTH_2ND_BIT_MEDIUM_LIMIT;
                 break;
             }
             case DecoderStateTables.ISTRING_INDEX_SMALL:
             case DecoderStateTables.ISTRING_INDEX_MEDIUM:
             case DecoderStateTables.ISTRING_INDEX_LARGE:
             default:
                 throw new FastInfosetException("Illegal state when decoding non empty octet string on second bit");
         }
         decodeUtf8StringAsCharBuffer();
     }
 
     /*
      * C.25
      */
     public final int decodeIntegerIndexOnSecondBit() throws FastInfosetException, IOException {
         final int b = read();
         switch(DecoderStateTables.ISTRING[b]) {
             case DecoderStateTables.ISTRING_INDEX_SMALL:
                 return b & EncodingConstants.INTEGER_2ND_BIT_SMALL_MASK;
             case DecoderStateTables.ISTRING_INDEX_MEDIUM:
                 return (((b & EncodingConstants.INTEGER_2ND_BIT_MEDIUM_MASK) << 8) | read())
                     + EncodingConstants.INTEGER_2ND_BIT_SMALL_LIMIT;
             case DecoderStateTables.ISTRING_INDEX_LARGE:
                 return (((b & EncodingConstants.INTEGER_2ND_BIT_LARGE_MASK) << 16) | (read() << 8) | read())
                     + EncodingConstants.INTEGER_2ND_BIT_MEDIUM_LIMIT;
             case DecoderStateTables.ISTRING_SMALL_LENGTH:
             case DecoderStateTables.ISTRING_MEDIUM_LENGTH:
             case DecoderStateTables.ISTRING_LARGE_LENGTH:
             default:
                 throw new FastInfosetException("Illegal state when decoding index on second bit");
         }
     }
 
     public final void decodeHeader() throws FastInfosetException, IOException {
         if (!_isFastInfosetDocument()) {
             throw new FastInfosetException("Input stream is not a fast infoset document");
         }
     }
 
     public final void decodeRAOctetsAsCharBuffer(char[] restrictedAlphabet) throws IOException {
         throw new UnsupportedOperationException("Restricted alphabet decoding not implemented");
     }
 
     public final String decodeRAOctetsAsString(char[] restrictedAlphabet) throws IOException {
         decodeRAOctetsAsCharBuffer(null);
         return new String(_charBuffer, 0, _charBufferLength);
     }
 
     public final void decodeUtf8StringAsCharBuffer() throws IOException {
         ensureOctetBufferSize();
         decodeUtf8StringIntoCharBuffer();
     }
 
     public final String decodeUtf8StringAsString() throws IOException {
         decodeUtf8StringAsCharBuffer();
         return new String(_charBuffer, 0, _charBufferLength);
     }
 
     public final void decodeUtf16StringAsCharBuffer() throws IOException {
         ensureOctetBufferSize();
         decodeUtf16StringIntoCharBuffer();
     }
 
     public final String decodeUtf16StringAsString() throws IOException {
         decodeUtf16StringAsCharBuffer();
         return new String(_charBuffer, 0, _charBufferLength);
     }
 
     public final void ensureOctetBufferSize() throws IOException {
         if (_octetBufferEnd < (_octetBufferOffset + _octetBufferLength)) {
             final int bytesRemaining = _octetBufferEnd - _octetBufferOffset;
 
             if (_octetBuffer.length < _octetBufferLength) {
                 byte[] newOctetBuffer = new byte[_octetBufferLength];
                 System.arraycopy(_octetBuffer, _octetBufferOffset, newOctetBuffer, 0, bytesRemaining);
                 _octetBuffer = newOctetBuffer;
             } else {
                 System.arraycopy(_octetBuffer, _octetBufferOffset, _octetBuffer, 0, bytesRemaining);
             }
             _octetBufferOffset = 0;
 
             final int bytesRead = _s.read(_octetBuffer, bytesRemaining, _octetBuffer.length - bytesRemaining);
             if (bytesRead < 0) {
                 throw new EOFException("Unexpeceted EOF");
             }
 
             if (bytesRead < _octetBufferLength - bytesRemaining) {
                 // TODO keep reading until require bytes have been obtained
                 throw new IOException("Full bytes not read");
             }
 
             _octetBufferEnd = bytesRemaining + bytesRead;
         }
     }
     
     public final void decodeUtf8StringIntoCharBuffer() throws IOException {
         _charBufferLength = 0;
         if (_charBuffer.length < _octetBufferLength) {
             _charBuffer = new char[_octetBufferLength];
         }
 
         int b1;
         final int end = _octetBufferLength + _octetBufferOffset;
         while (end != _octetBufferOffset) {
             b1 = _octetBuffer[_octetBufferOffset++];
             // Optimal checking for characters in the range 0x20 to 0x7F
             if (b1 > 0x19) {
                 _charBuffer[_charBufferLength++] = (char) b1;
             } else {
                 b1 = (b1 < 0) ? (b1 & 0x7F) | 0x80 : b1;
                 switch(DecoderStateTables.UTF8[b1]) {
                     // Check for 0x09, 0x0A and 0x0D
                     case DecoderStateTables.UTF8_ONE_BYTE:
                     {
                         _charBuffer[_charBufferLength++] = (char) b1;
                         break;
                     }
                     case DecoderStateTables.UTF8_TWO_BYTES:
                     {
                         // Decode byte 2
                         if (end == _octetBufferOffset) {
                             decodeUtf8StringLengthTooSmall();
                         }
                         final int b2 = _octetBuffer[_octetBufferOffset++] & 0xFF;
                         if ((b2 & 0xC0) != 0x80) {
                             decodeUtf8StringIllegalState();
                         }
 
                         _charBuffer[_charBufferLength++] = (char) (
                             ((b1 & 0x1F) << 6)
                             | (b2 & 0x3F));
                         break;
                     }
                     case DecoderStateTables.UTF8_THREE_BYTES:
                         decodeUtf8ThreeByteChar(_charBuffer, end, b1);
                         break;
                     case DecoderStateTables.UTF8_FOUR_BYTES:
                         decodeUtf8FourByteChar(_charBuffer, end, b1);
                         break;
                     default:
                         decodeUtf8StringIllegalState();
                 }
             }
         }
     }
     
     /*
      * TODO, use the Xerces org.apache.Xerces.util.XMLChar class
      * to detemine valid NCName characters for characters not in
      * the Basic Latin range
      */
     public final void decodeUtf8NCNameIntoCharBuffer() throws IOException {
         _charBufferLength = 0;
         if (_charBuffer.length < _octetBufferLength) {
             _charBuffer = new char[_octetBufferLength];
         }
 
         final int end = _octetBufferLength + _octetBufferOffset;
 
         int b1 = _octetBuffer[_octetBufferOffset++] & 0xFF;
         int state = DecoderStateTables.UTF8_NCNAME[b1];
         if (state == DecoderStateTables.UTF8_NCNAME_NCNAME) {
             _charBuffer[_charBufferLength++] = (char) b1;
         } else {
             switch(state) {
                 case DecoderStateTables.UTF8_TWO_BYTES:
                 {
                     // Decode byte 2
                     if (end == _octetBufferOffset) {
                         decodeUtf8StringLengthTooSmall();
                     }
                     final int b2 = _octetBuffer[_octetBufferOffset++] & 0xFF;
                     if ((b2 & 0xC0) != 0x80) {
                         decodeUtf8StringIllegalState();
                     }
 
                     _charBuffer[_charBufferLength++] = (char) (
                         ((b1 & 0x1F) << 6)
                         | (b2 & 0x3F));
                     break;
                 }
                 case DecoderStateTables.UTF8_THREE_BYTES:
                     decodeUtf8ThreeByteChar(_charBuffer, end, b1);
                     break;
                 case DecoderStateTables.UTF8_FOUR_BYTES:
                     decodeUtf8FourByteChar(_charBuffer, end, b1);
                     break;
                 case DecoderStateTables.UTF8_NCNAME_NCNAME_CHAR:
                 default:
                     decodeUtf8NCNameIllegalState();
             }
         }
 
         while (end != _octetBufferOffset) {
             b1 = _octetBuffer[_octetBufferOffset++] & 0xFF;
             state = DecoderStateTables.UTF8_NCNAME[b1];
             if (state < DecoderStateTables.UTF8_TWO_BYTES) {
                 _charBuffer[_charBufferLength++] = (char) b1;
             } else {
                 switch(state) {
                     case DecoderStateTables.UTF8_TWO_BYTES:
                     {
                         // Decode byte 2
                         if (end == _octetBufferOffset) {
                             decodeUtf8StringLengthTooSmall();
                         }
                         final int b2 = _octetBuffer[_octetBufferOffset++] & 0xFF;
                         if ((b2 & 0xC0) != 0x80) {
                             decodeUtf8StringIllegalState();
                         }
 
                         _charBuffer[_charBufferLength++] = (char) (
                             ((b1 & 0x1F) << 6)
                             | (b2 & 0x3F));
                         break;
                     }
                     case DecoderStateTables.UTF8_THREE_BYTES:
                         decodeUtf8ThreeByteChar(_charBuffer, end, b1);
                         break;
                     case DecoderStateTables.UTF8_FOUR_BYTES:
                         decodeUtf8FourByteChar(_charBuffer, end, b1);
                         break;
                     default:
                         decodeUtf8NCNameIllegalState();
                 }
             }
         }
     }
 
     public final void decodeUtf8ThreeByteChar(char[] c, int end, int b1) throws IOException {
         // Decode byte 2
         if (end == _octetBufferOffset) {
             decodeUtf8StringLengthTooSmall();
         }
         final int b2 = _octetBuffer[_octetBufferOffset++] & 0xFF;
         if ((b2 & 0xC0) != 0x80
             || (b1 == 0xED && b2 >= 0xA0)
             || ((b1 & 0x0F) == 0 && (b2 & 0x20) == 0)) {
             decodeUtf8StringIllegalState();
         }
 
         // Decode byte 3
         if (end == _octetBufferOffset) {
             decodeUtf8StringLengthTooSmall();
         }
         final int b3 = _octetBuffer[_octetBufferOffset++] & 0xFF;
         if ((b3 & 0xC0) != 0x80) {
             decodeUtf8StringIllegalState();
         }
 
         c[_charBufferLength++] = (char) (
             (b1 & 0x0F) << 12
             | (b2 & 0x3F) << 6
             | (b3 & 0x3F));
     }
 
     public final void decodeUtf8FourByteChar(char[] c, int end, int b1) throws IOException {
         // Decode byte 2
         if (end == _octetBufferOffset) {
             decodeUtf8StringLengthTooSmall();
         }
         final int b2 = _octetBuffer[_octetBufferOffset++] & 0xFF;
         if ((b2 & 0xC0) != 0x80
             || ((b2 & 0x30) == 0 && (b1 & 0x07) == 0)) {
             decodeUtf8StringIllegalState();
         }
 
         // Decode byte 3
         if (end == _octetBufferOffset) {
             decodeUtf8StringLengthTooSmall();
         }
         final int b3 = _octetBuffer[_octetBufferOffset++] & 0xFF;
         if ((b3 & 0xC0) != 0x80) {
             decodeUtf8StringIllegalState();
         }
 
         // Decode byte 4
         if (end == _octetBufferOffset) {
             decodeUtf8StringLengthTooSmall();
         }
         final int b4 = _octetBuffer[_octetBufferOffset++] & 0xFF;
         if ((b4 & 0xC0) != 0x80) {
             decodeUtf8StringIllegalState();
         }
 
         final int uuuuu = ((b1 << 2) & 0x001C) | ((b2 >> 4) & 0x0003);
         if (uuuuu > 0x10) {
             decodeUtf8StringIllegalState();
         }
         final int wwww = uuuuu - 1;
 
         final int hs = 0xD800 |
              ((wwww << 6) & 0x03C0) | ((b2 << 2) & 0x003C) |
              ((b3 >> 4) & 0x0003);
         final int ls = 0xDC00 | ((b3 << 6) & 0x03C0) | (b4 & 0x003F);
 
         c[_charBufferLength++] = (char)hs;
         c[_charBufferLength++] = (char)ls;
     }
 
     public final void decodeUtf8StringLengthTooSmall() throws IOException {
         throw new IOException("Length deliminator too small");
     }
 
     public final void decodeUtf8StringIllegalState() throws IOException {
         throw new IOException("Illegal state for UTF-8 encoded string");
     }
 
     public final void decodeUtf8NCNameIllegalState() throws IOException {
         throw new IOException("Illegal state for UTF-8 encoded NCName");
     }
 
     public final void decodeUtf16StringIntoCharBuffer() throws IOException {
         _charBufferLength = _octetBufferLength / 2;
         if (_charBuffer.length < _charBufferLength) {
             _charBuffer = new char[_charBufferLength];
         }
 
         for (int i = 0; i < _charBufferLength; i++) {
             final char c = (char)((read() << 8) | read());
             // TODO check c is a valid Char character
             _charBuffer[i] = c;
         }
         
     }
 
     protected final int read() throws IOException {
         if (_octetBufferOffset < _octetBufferEnd) {
             return _octetBuffer[_octetBufferOffset++] & 0xFF;
         } else {
             _octetBufferEnd = _s.read(_octetBuffer);
             if (_octetBufferEnd < 0) {
                 throw new EOFException("Unexpeceted EOF");
             }
 
             _octetBufferOffset = 1;
             return _octetBuffer[0] & 0xFF;
         }
     }
 
     protected class EncodingAlgorithmInputStream extends InputStream {
 
         public int read() throws IOException {
             if (_octetBufferStart < _octetBufferOffset) {
                 return (_octetBuffer[_octetBufferStart++] & 0xFF);
             } else {
                 return -1;
             }
         }
 
         public int read(byte b[]) throws IOException {
             return read(b, 0, b.length);
         }
 
         public int read(byte b[], int off, int len) throws IOException {
             if (b == null) {
                 throw new NullPointerException();
             } else if ((off < 0) || (off > b.length) || (len < 0) ||
                        ((off + len) > b.length) || ((off + len) < 0)) {
                 throw new IndexOutOfBoundsException();
             } else if (len == 0) {
                 return 0;
             }
 
             final int newOctetBufferStart = _octetBufferStart + len;
             if (newOctetBufferStart < _octetBufferOffset) {
                 System.arraycopy(_octetBuffer, _octetBufferStart, b, off, len);
                 _octetBufferStart = newOctetBufferStart;
                 return len;
             } else if (_octetBufferStart < _octetBufferOffset) {
                 final int bytesToRead = _octetBufferOffset - _octetBufferStart;
                 System.arraycopy(_octetBuffer, _octetBufferStart, b, off, bytesToRead);
                 _octetBufferStart += bytesToRead;
                 return bytesToRead;
             } else {
                 return -1;
             }
         }
     }
 
     protected final boolean _isFastInfosetDocument() throws IOException {
         // TODO
         // Check for <?xml declaration with 'finf' encoding
 
         if (read() != (EncodingConstants.HEADER[0] & 0xFF) ||
                 read() != (EncodingConstants.HEADER[1] & 0xFF) ||
                 read() != (EncodingConstants.HEADER[2] & 0xFF) ||
                 read() != (EncodingConstants.HEADER[3] & 0xFF)) {
             return false;
         }
 
         // TODO
         return true;
     }
 
 
     static public boolean isFastInfosetDocument(InputStream s) throws IOException {
         // TODO
         // Check for <?xml declaration with 'finf' encoding
 
         final byte[] header = new byte[4];
         s.read(header);
         if (header[0] != EncodingConstants.HEADER[0] ||
                 header[1] != EncodingConstants.HEADER[1] ||
                 header[2] != EncodingConstants.HEADER[2] ||
                 header[3] != EncodingConstants.HEADER[3]) {
             return false;
         }
 
         // TODO
         return true;
     }
 }
