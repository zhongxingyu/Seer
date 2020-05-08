 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.support.components.datatype.parsers;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.BytesMap;
 import org.seasr.datatypes.BasicDataTypes.Bytes;
 import org.seasr.datatypes.BasicDataTypes.Integers;
 import org.seasr.datatypes.BasicDataTypes.IntegersMap;
 import org.seasr.datatypes.BasicDataTypes.Strings;
 import org.seasr.datatypes.BasicDataTypes.StringsMap;
 import org.seasr.meandre.support.components.exceptions.UnsupportedDataTypeException;
 import org.seasr.meandre.support.generic.io.DOMUtils;
 import org.seasr.meandre.support.generic.io.ModelUtils;
 import org.seasr.meandre.support.generic.io.StreamUtils;
 import org.xml.sax.SAXException;
 
 import com.hp.hpl.jena.rdf.model.Model;
 
 /**
  * @author Boris Capitanu
  */
 public abstract class DataTypeParser {
     /**
      * Attempts to convert the given data to a String array
      *
      * @param data The data
      * @return The String array
      * @throws UnsupportedDataTypeException Thrown if the data is in an unsupported format
      */
     public static String[] parseAsString(Object data) throws UnsupportedDataTypeException {
         String[] text;
 
         if (data == null)
             text = null;
 
         else
 
         if (data instanceof Strings)
             text = BasicDataTypesTools.stringsToStringArray((Strings)data);
 
         else
 
         if (data instanceof String)
             text = new String[] { (String)data };
 
         else
 
         if (data instanceof byte[])
             text = new String[] { new String((byte[])data) };
 
         else
 
         if (data instanceof Bytes)
             text = new String[] { new String(BasicDataTypesTools.bytestoByteArray((Bytes)data)) };
 
         else
             text = new String[] { data.toString() };
 
         return text;
     }
 
     /**
      * Attempts to convert the given data to a Integer array
      *
      * @param data
      * @return
      * @throws UnsupportedDataTypeException
      */
     public static Integer[] parseAsInteger(Object data) throws UnsupportedDataTypeException {
     	Integer[] value;
 
     	if (data == null)
     		value = null;
 
     	else
 
     	if (data instanceof Integer)
     		value = new Integer[] { (Integer)data };
 
     	else
 
     	if (data instanceof Integers)
     		value = BasicDataTypesTools.integersToIntegerArray((Integers)data);
 
     	else
 
     	if (data instanceof String)
     		value = new Integer[] { Integer.parseInt((String)data) };
 
     	else
 
     	if (data instanceof Strings) {
     	    String[] strings = parseAsString(data);
     		value = new Integer[strings.length];
     		for (int i = 0, len = strings.length; i < len; i++)
     		    value[i] = Integer.parseInt(strings[i]);
     	}
 
     	else
     		throw new UnsupportedDataTypeException(data.getClass().getName());
 
     	return value;
     }
 
     /**
      * Attempts to convert the given data to a URI
      *
      * @param data The data
      * @return The URI
      * @throws URISyntaxException Thrown if a URI cannot be created due to syntax error in the input data
      * @throws UnsupportedDataTypeException Thrown if the data is in an unsupported format
      */
     public static URI parseAsURI(Object data) throws URISyntaxException, UnsupportedDataTypeException {
         URI uri;
 
         if (data == null)
             uri = null;
 
         else
 
         if (data instanceof URI)
             uri = (URI)data;
 
         else
 
         if (data instanceof URL)
             uri = ((URL)data).toURI();
 
         else
 
         if (data instanceof Strings)
            uri = new URI(BasicDataTypesTools.stringsToStringArray((Strings)data)[0]);

         else
 
         if (data instanceof String)
            uri = new URI((String)data);

         else
             throw new UnsupportedDataTypeException(data.getClass().getName());
 
         return uri;
     }
 
     /**
      * Attempts to convert the given data to a Map<String, Integer>
      *
      * @param data The data
      * @return The Map<String, Integer>
      * @throws UnsupportedDataTypeException Thrown if the data is in an unsupported format
      */
     @SuppressWarnings("unchecked")
     public static Map<String, Integer> parseAsStringIntegerMap(Object data) throws UnsupportedDataTypeException {
         Map<String, Integer> map;
 
         if (data == null)
             map = null;
 
         else
 
         if (data instanceof IntegersMap)
             map = BasicDataTypesTools.IntegerMapToMap((IntegersMap)data);
 
         else
 
         if (data instanceof Map) {
             map = (Map<String, Integer>)data;
             if (!(map.values().iterator().next() instanceof Integer))
                 throw new UnsupportedDataTypeException("The given map is not in the correct format!");
         }
 
         else
             throw new UnsupportedDataTypeException(data.getClass().getName());
 
         return map;
     }
 
     /**
      * Attempts to convert the given data to a Map<String, String>
      *
      * @param data The data
      * @return The Map<String, String>
      * @throws UnsupportedDataTypeException Thrown if the data is in an unsupported format
      */
     @SuppressWarnings("unchecked")
     public static Map<String, String[]> parseAsStringStringArrayMap(Object data) throws UnsupportedDataTypeException {
         Map<String, String[]> map;
 
         if (data == null)
             map = null;
 
         else
 
         if (data instanceof StringsMap)
             map = BasicDataTypesTools.StringMapToMap((StringsMap)data);
 
         else
 
         if (data instanceof Map) {
             map = (Map<String, String[]>)data;
             if (!(map.values().iterator().next() instanceof String[]))
                 throw new UnsupportedDataTypeException("The given map is not in the correct format!");
         }
 
         else
             throw new UnsupportedDataTypeException(data.getClass().getName());
 
         return map;
     }
 
     /**
      * Attempts to convert the given data to a Map<String, byte[]>
      *
      * @param data The data
      * @return The Map<String, byte[]>
      * @throws UnsupportedDataTypeException Thrown if the data is in an unsupported format
      */
     @SuppressWarnings("unchecked")
     public static Map<String, byte[]> parseAsStringByteArrayMap(Object data) throws UnsupportedDataTypeException {
         Map<String, byte[]> map;
 
         if (data == null)
             map = null;
 
         else
 
         if (data instanceof BytesMap)
             map = BasicDataTypesTools.ByteMapToMap((BytesMap)data);
 
         else
 
         if (data instanceof Map) {
             map = (Map<String, byte[]>)data;
             if (!(map.values().iterator().next() instanceof byte[]))
                 throw new UnsupportedDataTypeException("The given map is not in the correct format!");
         }
 
         else
             throw new UnsupportedDataTypeException(data.getClass().getName());
 
         return map;
     }
 
     /**
      * Attempts to convert the given data to a Model
      *
      * @param data The data
      * @return The Model
      * @throws UnsupportedDataTypeException Thrown if the data is in an unsupported format
      */
     public static Model parseAsModel(Object data) throws UnsupportedDataTypeException, IOException, URISyntaxException {
         Model model;
 
         if (data == null)
             model = null;
 
         else
 
         if (data instanceof Model)
             model = (Model)data;
 
         else
 
         if (data instanceof Bytes)
             model = ModelUtils.getModel(
                     BasicDataTypesTools.bytestoByteArray((Bytes) data),
                     null);
 
         else
 
         if (data instanceof byte[])
             model = ModelUtils.getModel((byte[])data, null);
 
         else
 
         if (data instanceof Strings)
             model = ModelUtils.getModel(
                     BasicDataTypesTools.stringsToStringArray((Strings)data)[0],
                     null);
 
         else
 
         if (data instanceof String)
             model = ModelUtils.getModel((String)data, null);
 
         else
 
         if (data instanceof URL)
             model = ModelUtils.getModel(((URL)data).toURI(), null);
 
         else
 
         if (data instanceof URI)
             model = ModelUtils.getModel((URI)data, null);
 
         else
             throw new UnsupportedDataTypeException(data.getClass().getName());
 
         return model;
     }
 
     /**
      * Attempts to convert the given data to a DOM Document
      *
      * @param data The data
      * @return The DOM Document
      * @throws UnsupportedDataTypeException
      * @throws SAXException
      * @throws IOException
      * @throws ParserConfigurationException
      */
     public static org.w3c.dom.Document parseAsDomDocument(Object data)
         throws UnsupportedDataTypeException, SAXException, IOException, ParserConfigurationException {
 
         org.w3c.dom.Document doc;
 
         if (data == null)
             doc = null;
 
         else
 
         if (data instanceof org.w3c.dom.Document)
             doc = (org.w3c.dom.Document)data;
 
         else
 
         if (data instanceof String)
             doc = DOMUtils.createDocument((String)data);
 
         else
 
         if (data instanceof Strings) {
             String[] strings = BasicDataTypesTools.stringsToStringArray((Strings)data);
             if (strings.length > 1)
                 throw new UnsupportedDataTypeException("Cannot process more than one String data at a time");
 
             doc = DOMUtils.createDocument(strings[0]);
         }
 
         else
             throw new UnsupportedDataTypeException(data.getClass().getName());
 
         return doc;
     }
 
 
     /**
      *
      * @param data
      * @return
      * @throws UnsupportedOperationException
      * @throws IOException
      */
     public static byte[] parseAsByteArray(Object data) throws UnsupportedOperationException, IOException {
         byte[] barr;
 
         if (data == null)
             barr = null;
 
         else
 
         if (data instanceof byte[])
             barr = (byte[])data;
 
         else
 
         if (data instanceof Bytes)
             barr = BasicDataTypesTools.bytestoByteArray((Bytes)data);
 
         else
 
         if (data instanceof URI)
             barr = StreamUtils.getBytesFromStream(StreamUtils.getInputStreamForResource((URI)data));
 
         else
 
         if (data instanceof URL)
             barr = StreamUtils.getBytesFromStream(((URL)data).openStream());
 
         else
             throw new UnsupportedOperationException(data.getClass().getName());
 
         return barr;
     }
 }
