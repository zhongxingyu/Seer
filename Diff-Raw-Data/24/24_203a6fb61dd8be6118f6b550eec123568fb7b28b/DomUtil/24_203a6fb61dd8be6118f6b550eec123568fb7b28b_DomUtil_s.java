 /*
 Copyright 2008 Flaptor (flaptor.com) 
 
 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 
 
     http://www.apache.org/licenses/LICENSE-2.0 
 
 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License.
 */
 
 package com.flaptor.util;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 
 /**
  * This class implements some static methods to manipulate doms.
  */
 public final class DomUtil {
 
     private static final Logger logger = Logger.getLogger(Execute.whoAmI());
 
     /**
     * Private empty default constructor to prevent inheritance and instiantiation.
     */
    private DomUtil() {}

    /**
      * Returns a pretty string representation of a dom.
      * @param doc the dom to be stringified
      * @return the string representation of the document
      * @throws IOException in case of an error
      */
     public static String domToString(final Document doc) {
         OutputFormat of = OutputFormat.createPrettyPrint();
         StringWriter sw = new StringWriter();
         XMLWriter writer = null;
         try {
             writer = new XMLWriter(sw, of);
             try {
                 writer.write(doc);
             } catch (IOException e) {
                 logger.error("domToString: caught exception. This is very odd.", e);
                 throw new IllegalStateException(e);
             }
             return sw.toString();
         } finally {
             Execute.close(writer);
         }
     }
 
     /**
      * Returns the first attribute's value of the first ocurrence of an element.
      * @param root the root element of the dom
      * @param elementName the name of the element
      * @param attribName the name of the attribute
      * @return the value of the first element, or null if not found.
      * The first matching element is considered only.
      */
     public static String getAttributeValue(final Element root, final String elementName, final String attribName) {
         String value = null;
         Element element;
         Iterator elementIterator = root.elementIterator(elementName);
         while (elementIterator.hasNext()) {
             element = (Element)elementIterator.next();
             value =  element.attributeValue(attribName);
         }
         return value;
     }
 
     /**
      * Returns the text of the first ocurrence of an element.
      * @param root the root element of the dom
      * @param elementName the name of the element
      * @return the value of the text of the first element, or null if not found.
      * The first matching element is considered only.
      */
     public static String getElementText(final Element root, final String elementName) {
         String value = null;
         Element element;
         Iterator elementIterator = root.elementIterator(elementName);
         while (elementIterator.hasNext()) {
             element = (Element)elementIterator.next();
             value =  element.getText();
             if (value != null) break;
         }
         return value;
     }
    
     
     /**
      * Returns the first attribute's value of the first ocurrence of an element.
      * @param root the root element of the dom
      * @param elementName the name of the element
      * @param attribName the name of the attribute
      * @return the value of the first element, or null if not found.
      * The first matching element is considered only.
      */
     public static void replaceElementValueByName(final Element root, final String elementName, final String name, final String value) {
         Element element;
         Iterator elementIterator = root.elementIterator(elementName);
         while (elementIterator.hasNext()) {
             element = (Element)elementIterator.next();
             if (name.equals(element.attributeValue("name"))) {
                 element.setText(DomUtil.filterXml(value));
                 break;
             }
         }
     }
 
     /*
      * Ensure string is legal xml.
      * @param text String to verify.
      * @return Passed <code>text</code> or a new string with illegal
      * characters removed if any found in <code>text</code>.
      * @see http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char
      */
     public static final String filterXml(final String text) {
         if (text == null) {
             return null;
         }
         StringBuffer buffer = null;
         for (int i = 0; i < text.length(); i++) {
             char c = text.charAt(i);
             if (!isLegalXml(c)) {
                 if (buffer == null) {
                     // Start up a buffer.  Copy characters here from now on
                     // now we've found at least one bad character in original.
                     buffer = new StringBuffer(text.length());
                     buffer.append(text.substring(0, i));
                 }
             } else {
                 if (buffer != null) {
                     buffer.append(c);
                 }
             }
         }
         return (buffer != null)? buffer.toString(): text;
     }
 
     /**
      * Ensure string is legal xml.
      * @param text String to verify.
      * @return true if every char in text is valid
      * @see http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char
      */
     public static final boolean isLegalXml(final String text) {
         for (int i = 0; i < text.length(); i++) {
             char c = text.charAt(i);
             if (!isLegalXml(c)) {
                 return false;
             }
         }
         return true;
     }
 
     private static final boolean isLegalXml(final char c) {
         return c == 0x9 || c == 0xa || c == 0xd || (c >= 0x20 && c <= 0xd7ff)
             || (c >= 0xe000 && c <= 0xfffd) || (c >= 0x10000 && c <= 0x10ffff);
     }
 
 }
 
