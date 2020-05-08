 /*
  * Copyright (C) 2013 Lasse Dissing Hansen
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in the Software without restriction,
  * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  */
 
 package volpes.ldk.utils;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 import volpes.ldk.client.LDKException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
 * @auther Lasse Dissing Hansen
  */
 public class Parsers {
 
     /**
      * Utility functions that wraps much of the xml boilerplate code.
      * @param is The inpustream of the XML file
      * @return The {@link org.w3c.dom.Document} of the XML file
      * @throws volpes.ldk.client.LDKException
      */
     public static Document parseXML(InputStream is) throws LDKException {
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder documentBuilder;
         try {
             documentBuilder = documentBuilderFactory.newDocumentBuilder();
         }   catch (ParserConfigurationException e) {
             throw new LDKException("Unable to load resources",e);
         }
         Document doc;
         try {
             doc = documentBuilder.parse(is);
         }   catch (SAXException e) {
             throw new LDKException("Unable to load resources",e);
         }   catch (IOException e) {
             throw new LDKException("Unable to load resources",e);
         }
 
         doc.getDocumentElement().normalize();
 
         return doc;
     }
 }
