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
 
 
 package algorithm;
 
 import com.sun.xml.fastinfoset.QualifiedName;
 import com.sun.xml.fastinfoset.algorithm.FloatEncodingAlgorithm;
 import com.sun.xml.fastinfoset.algorithm.IntEncodingAlgorithm;
 import com.sun.xml.fastinfoset.sax.AttributesHolder;
 import com.sun.xml.fastinfoset.sax.Properties;
 import com.sun.xml.fastinfoset.sax.SAXDocumentParser;
 import com.sun.xml.fastinfoset.sax.SAXDocumentSerializer;
 import com.sun.xml.fastinfoset.vocab.ParserVocabulary;
 import com.sun.xml.fastinfoset.vocab.SerializerVocabulary;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 import junit.framework.*;
 import org.jvnet.fastinfoset.EncodingAlgorithmIndexes;
 import org.jvnet.fastinfoset.sax.EncodingAlgorithmAttributes;
 import org.jvnet.fastinfoset.sax.FastInfosetDefaultHandler;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 public class AlgorithmTest extends TestCase {
     protected static final int ARRAY_SIZE = 8;
     protected static final int APPLICATION_DEFINED_ALGORITHM_ID = 32;
     protected static final String APPLICATION_DEFINED_ALGORITHM_URI = "algorithm-32";
     protected static final String EXTERNAL_VOCABULARY_URI_STRING = "urn:external-vocabulary";
 
     protected AttributesHolder _attributes = new AttributesHolder();
 
     protected byte[] _byteArray = new byte[ARRAY_SIZE];
     protected int[] _intArray = new int[ARRAY_SIZE];
     protected float[] _floatArray = new float[ARRAY_SIZE];
     
     public AlgorithmTest(String testName) {
         super(testName);
     }
 
     protected void setUp() throws java.lang.Exception {
     }
 
     protected void tearDown() throws java.lang.Exception {
     }
 
     public static junit.framework.Test suite() {
         junit.framework.TestSuite suite = new junit.framework.TestSuite(AlgorithmTest.class);
         
         return suite;
     }
     
     public void testFloatAlgorithm() throws Exception {
         createArrayValues();
         
         byte[] b = new byte[ARRAY_SIZE * 4];
         
         FloatEncodingAlgorithm fea = new FloatEncodingAlgorithm();
         fea.encodeToBytesFromFloatArray(_floatArray, 0, ARRAY_SIZE, b, 0);
         
         float[] f = new float[ARRAY_SIZE];
         fea.decodeFromBytesToFloatArray(f, 0, b, 0, b.length);
 
         for (int is = 0; is < ARRAY_SIZE; is++) {
            assertEquals(_floatArray[is], f[is], 0.0);
         }
     }
 
     public void testIntAlgorithm() throws Exception {
         createArrayValues();
         
         byte[] b = new byte[ARRAY_SIZE * 4];
         
         IntEncodingAlgorithm iea = new IntEncodingAlgorithm();
         iea.encodeToBytesFromIntArray(_intArray, 0, ARRAY_SIZE, b, 0);
         
         int[] i = new int[ARRAY_SIZE];
         iea.decodeFromBytesToIntArray(i, 0, b, 0, b.length);
 
         for (int is = 0; is < ARRAY_SIZE; is++) {
             assertEquals(_intArray[is], i[is]);
         }
     }
     
     public void testBuiltInAlgorithms() throws Exception {
         createArrayValues();
                 
         byte[] b = createBuiltInTestFastInfosetDocument();
         InputStream bais = new ByteArrayInputStream(b);
         
         SAXDocumentParser p = new SAXDocumentParser();
         
         BuiltInTestHandler h = new BuiltInTestHandler();
         
         p.setContentHandler(h);
         p.setLexicalHandler(h);
         p.setPrimitiveTypeContentHandler(h);
         
         p.parse(bais);
     }
 
     protected byte[] createBuiltInTestFastInfosetDocument() throws Exception {
         SAXDocumentSerializer s = new SAXDocumentSerializer();
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         s.setOutputStream(baos);
         
         _attributes.clear();
         
         
         s.startDocument();
 
         s.startElement("", "e", "e", _attributes);
         
         // Bytes
         _attributes.addAttributeWithAlgorithmData(new QualifiedName("", "", "byte", "byte"), 
                 null, EncodingAlgorithmIndexes.BASE64, _byteArray);
         s.startElement("", "byte", "byte", _attributes);
         _attributes.clear();
         s.bytes(_byteArray, 0, _byteArray.length);
         s.endElement("", "byte", "byte");
         
         // Ints
         _attributes.addAttributeWithAlgorithmData(new QualifiedName("", "", "int", "int"), 
                 null, EncodingAlgorithmIndexes.INT, _intArray);
         s.startElement("", "int", "int", _attributes);
         _attributes.clear();
         s.ints(_intArray, 0, _intArray.length);
         s.endElement("", "int", "int");
 
         // Floats
         _attributes.addAttributeWithAlgorithmData(new QualifiedName("", "", "float", "float"), 
                 null, EncodingAlgorithmIndexes.FLOAT, _floatArray);
         s.startElement("", "float", "float", _attributes);
         _attributes.clear();
         s.floats(_floatArray, 0, _floatArray.length);
         s.endElement("", "float", "float");        
         
         s.endElement("", "e", "e");
         
         s.endDocument();
         
         return baos.toByteArray();
     }
     
     public class BuiltInTestHandler extends FastInfosetDefaultHandler {
         
         // ContentHandler
         
         public final void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
             if (atts.getLength() > 0) {
                 EncodingAlgorithmAttributes eas = (EncodingAlgorithmAttributes)atts;
                 
                 assertEquals(1, atts.getLength());
                 
                 if (localName.equals("byte")) {
                     assertEquals("byte", eas.getLocalName(0));
                     assertEquals(EncodingAlgorithmIndexes.BASE64, eas.getAlgorithmIndex(0));
                     assertEquals(null, eas.getAlgorithmURI(0));
                     assertEquals(true, eas.getAlgorithmData(0) instanceof byte[]);
                     byte[] b = (byte[])eas.getAlgorithmData(0);
                     for (int is = 0; is < ARRAY_SIZE; is++) {
                         assertEquals(_byteArray[is], b[is]);
                     }
                 } else if (localName.equals("int")) {   
                     assertEquals("int", eas.getLocalName(0));
                     assertEquals(EncodingAlgorithmIndexes.INT, eas.getAlgorithmIndex(0));
                     assertEquals(null, eas.getAlgorithmURI(0));
                     assertEquals(true, eas.getAlgorithmData(0) instanceof int[]);
                     int[] i = (int[])eas.getAlgorithmData(0);
                     for (int is = 0; is < ARRAY_SIZE; is++) {
                         assertEquals(_intArray[is], i[is]);
                     }
                 } else if (localName.equals("float")) {
                     assertEquals("float", eas.getLocalName(0));
                     assertEquals(EncodingAlgorithmIndexes.FLOAT, eas.getAlgorithmIndex(0));
                     assertEquals(null, eas.getAlgorithmURI(0));
                     assertEquals(true, eas.getAlgorithmData(0) instanceof float[]);
                     float[] f = (float[])eas.getAlgorithmData(0);
                     for (int is = 0; is < ARRAY_SIZE; is++) {
                        assertEquals(_floatArray[is], f[is], 0.0);
                     }
                 }
             } else {
                 assertEquals("e", localName);
             }
         }
         
         public final void endElement(String namespaceURI, String localName, String qName) throws SAXException {
         }
                 
         // PrimitiveTypeContentHandler
 
         public final void bytes(byte[] b, int start, int length) throws SAXException {
             assertEquals(ARRAY_SIZE, length);
             for (int i = 0; i < ARRAY_SIZE; i++) {
                 assertEquals(_byteArray[i], b[i + start]);
             }
         }
 
         public final void ints(int[] i, int start, int length) throws SAXException {
             assertEquals(ARRAY_SIZE, length);
 
             for (int is = 0; is < ARRAY_SIZE; is++) {
                 assertEquals(_intArray[is], i[is + start]);
             }
         }
 
         public final void floats(float[] f, int start, int length) throws SAXException {
             assertEquals(ARRAY_SIZE, length);
 
             for (int i = 0; i < ARRAY_SIZE; i++) {
                assertEquals(_floatArray[i], f[i + start], 0.0);
             }
         }        
     }
 
     
     public void testGenericAlgorithms() throws Exception {
         createArrayValues();
 
         byte[] b = createGenericTestFastInfosetDocument();
         InputStream bais = new ByteArrayInputStream(b);
         
         SAXDocumentParser p = new SAXDocumentParser();
 
         ParserVocabulary externalVocabulary = new ParserVocabulary();
         externalVocabulary.encodingAlgorithm.add(APPLICATION_DEFINED_ALGORITHM_URI);
         
         Map externalVocabularies = new HashMap();
         externalVocabularies.put(EXTERNAL_VOCABULARY_URI_STRING, externalVocabulary);        
         p.setProperty(Properties.EXTERNAL_VOCABULARIES_PROPERTY, externalVocabularies);
                 
         GenericTestHandler h = new GenericTestHandler();
         
         p.setContentHandler(h);
         p.setLexicalHandler(h);
         p.setEncodingAlgorithmContentHandler(h);
         
         p.parse(bais);
     }
     
     protected byte[] createGenericTestFastInfosetDocument() throws Exception {
         SAXDocumentSerializer s = new SAXDocumentSerializer();
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         s.setOutputStream(baos);
 
         SerializerVocabulary externalVocabulary = new SerializerVocabulary();
         externalVocabulary.encodingAlgorithm.add(APPLICATION_DEFINED_ALGORITHM_URI);
 
         SerializerVocabulary initialVocabulary = new SerializerVocabulary();
         initialVocabulary.setExternalVocabulary(
                 new URI(EXTERNAL_VOCABULARY_URI_STRING),
                 externalVocabulary, false);
         
         s.setVocabulary(initialVocabulary);
         
         _attributes.clear();
         
         
         s.startDocument();
 
         s.startElement("", "e", "e", _attributes);
         
         // Application-defined algorithm 31
         _attributes.addAttributeWithAlgorithmData(new QualifiedName("", "", "algorithm", "algorithm"), 
                 APPLICATION_DEFINED_ALGORITHM_URI, APPLICATION_DEFINED_ALGORITHM_ID, _byteArray);
         s.startElement("", "algorithm", "algorithm", _attributes);
         _attributes.clear();
         s.octets(APPLICATION_DEFINED_ALGORITHM_URI, APPLICATION_DEFINED_ALGORITHM_ID, _byteArray, 0, _byteArray.length);
         s.endElement("", "algorithm", "algorithm");
         
         
         s.endElement("", "e", "e");
         
         s.endDocument();
         
         return baos.toByteArray();
     }
     
     public class GenericTestHandler extends FastInfosetDefaultHandler {
         
         // ContentHandler
         
         public final void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
             if (atts.getLength() > 0) {
                 EncodingAlgorithmAttributes eas = (EncodingAlgorithmAttributes)atts;
                 
                 assertEquals(1, atts.getLength());
                 
                 if (localName.equals("algorithm")) {
                     assertEquals("algorithm", eas.getLocalName(0));
                     assertEquals(APPLICATION_DEFINED_ALGORITHM_ID, eas.getAlgorithmIndex(0));
                     assertEquals(APPLICATION_DEFINED_ALGORITHM_URI, eas.getAlgorithmURI(0));
                     assertEquals(true, eas.getAlgorithmData(0) instanceof byte[]);
                     byte[] b = (byte[])eas.getAlgorithmData(0);
                     for (int is = 0; is < ARRAY_SIZE; is++) {
                         assertEquals(_byteArray[is], b[is]);
                     }
                 }
             } else {
                 assertEquals("e", localName);
             }
         }
         
         public final void endElement(String namespaceURI, String localName, String qName) throws SAXException {
         }
         
         // EncodingAlgorithmContentHandler
 
         public final void object(String URI, int algorithm, Object data)  throws SAXException {
             assertTrue(true);
         }
         
         public final void octets(String URI, int algorithm, byte[] b, int start, int length)  throws SAXException {
             assertEquals(APPLICATION_DEFINED_ALGORITHM_ID, algorithm);
             assertEquals(APPLICATION_DEFINED_ALGORITHM_URI, URI);
             assertEquals(ARRAY_SIZE, length);
 
             for (int i = 0; i < ARRAY_SIZE; i++) {
                 assertEquals(_byteArray[i], b[i + start]);
             }
         }        
     }
         
     public void testRegisteredAlgorithms() throws Exception {
         createArrayValues();
         
         byte[] b = createRegisteredTestFastInfosetDocument();
         InputStream bais = new ByteArrayInputStream(b);
         
         SAXDocumentParser p = new SAXDocumentParser();
 
         ParserVocabulary externalVocabulary = new ParserVocabulary();
         externalVocabulary.encodingAlgorithm.add(APPLICATION_DEFINED_ALGORITHM_URI);
         
         Map externalVocabularies = new HashMap();
         externalVocabularies.put(EXTERNAL_VOCABULARY_URI_STRING, externalVocabulary);        
         p.setProperty(Properties.EXTERNAL_VOCABULARIES_PROPERTY, externalVocabularies);
 
         Map algorithms = new HashMap();
         algorithms.put(APPLICATION_DEFINED_ALGORITHM_URI, new FloatEncodingAlgorithm());
         p.setRegisteredEncodingAlgorithms(algorithms);
         
         RegisteredTestHandler h = new RegisteredTestHandler();
         
         p.setContentHandler(h);
         p.setLexicalHandler(h);
         p.setEncodingAlgorithmContentHandler(h);
         
         p.parse(bais);
     }
     
     protected byte[] createRegisteredTestFastInfosetDocument() throws Exception {
         SAXDocumentSerializer s = new SAXDocumentSerializer();
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         s.setOutputStream(baos);
 
         SerializerVocabulary externalVocabulary = new SerializerVocabulary();
         externalVocabulary.encodingAlgorithm.add(APPLICATION_DEFINED_ALGORITHM_URI);
 
         SerializerVocabulary initialVocabulary = new SerializerVocabulary();
         initialVocabulary.setExternalVocabulary(
                 new URI(EXTERNAL_VOCABULARY_URI_STRING),
                 externalVocabulary, false);
         
         s.setVocabulary(initialVocabulary);
         
         Map algorithms = new HashMap();
         algorithms.put(APPLICATION_DEFINED_ALGORITHM_URI, new FloatEncodingAlgorithm());
         s.setRegisteredEncodingAlgorithms(algorithms);
         
         _attributes.clear();
         
         
         s.startDocument();
 
         s.startElement("", "e", "e", _attributes);
         
         // Application-defined algorithm 31
         _attributes.addAttributeWithAlgorithmData(new QualifiedName("", "", "algorithm", "algorithm"), 
                 APPLICATION_DEFINED_ALGORITHM_URI, APPLICATION_DEFINED_ALGORITHM_ID, _floatArray);
         s.startElement("", "algorithm", "algorithm", _attributes);
         _attributes.clear();
         s.object(APPLICATION_DEFINED_ALGORITHM_URI, APPLICATION_DEFINED_ALGORITHM_ID, _floatArray);
         s.endElement("", "algorithm", "algorithm");
         
         
         s.endElement("", "e", "e");
         
         s.endDocument();
         
         return baos.toByteArray();
     }
 
     public class RegisteredTestHandler extends FastInfosetDefaultHandler {
         
         // ContentHandler
         
         public final void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
             if (atts.getLength() > 0) {
                 EncodingAlgorithmAttributes eas = (EncodingAlgorithmAttributes)atts;
                 
                 assertEquals(1, atts.getLength());
                 
                 if (localName.equals("algorithm")) {
                     assertEquals("algorithm", eas.getLocalName(0));
                     assertEquals(APPLICATION_DEFINED_ALGORITHM_ID, eas.getAlgorithmIndex(0));
                     assertEquals(APPLICATION_DEFINED_ALGORITHM_URI, eas.getAlgorithmURI(0));
                     assertEquals(true, eas.getAlgorithmData(0) instanceof float[]);
                     float[] b = (float[])eas.getAlgorithmData(0);
                     for (int is = 0; is < ARRAY_SIZE; is++) {
                        assertEquals(_floatArray[is], b[is], 0.0);
                     }
                 }
             } else {
                 assertEquals("e", localName);
             }
         }
         
         public final void endElement(String namespaceURI, String localName, String qName) throws SAXException {
         }
         
         // EncodingAlgorithmContentHandler
 
         public final void object(String URI, int algorithm, Object data)  throws SAXException {
             assertEquals(APPLICATION_DEFINED_ALGORITHM_ID, algorithm);
             assertEquals(APPLICATION_DEFINED_ALGORITHM_URI, URI);
             assertEquals(true, data instanceof float[]);
             float[] b = (float[])data;
             for (int is = 0; is < ARRAY_SIZE; is++) {
                assertEquals(_floatArray[is], b[is], 0.0);
             }
         }
         
         public final void octets(String URI, int algorithm, byte[] b, int start, int length)  throws SAXException {
             assertTrue(true);
         }        
     }
     
     protected void createArrayValues() {
         for (int i = 0; i < ARRAY_SIZE; i++) {
             _byteArray[i] = (byte)i;
             _intArray[i] = i * Integer.MAX_VALUE / ARRAY_SIZE;
             _floatArray[i] = (float)(i * Math.E);
         }
     }
     
 }
