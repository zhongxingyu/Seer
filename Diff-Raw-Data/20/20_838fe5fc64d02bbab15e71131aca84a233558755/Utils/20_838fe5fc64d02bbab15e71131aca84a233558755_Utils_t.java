 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.tika.utils;
 
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 /**
  * Class util
  * 
  * 
  */
 
 public class Utils {
 
     static Logger logger = Logger.getRootLogger();
 
     public static Document parse(InputStream is) {
         org.jdom.Document xmlDoc = new org.jdom.Document();
         try {
             SAXBuilder builder = new SAXBuilder();
             builder.setValidation(false);
             xmlDoc = builder.build(is);
         } catch (JDOMException e) {
             logger.error(e.getMessage());
         } catch (IOException e) {
             logger.error(e.getMessage());
         }
         return xmlDoc;
     }
 
     public static List unzip(InputStream is) {
         List res = new ArrayList();
         try {
             ZipInputStream in = new ZipInputStream(is);
             ZipEntry entry = null;
             while ((entry = in.getNextEntry()) != null) {
                 ByteArrayOutputStream stream = new ByteArrayOutputStream();
                 byte[] buf = new byte[1024];
                 int len;
                 while ((len = in.read(buf)) > 0) {
                     stream.write(buf, 0, len);
                 }
                 InputStream isEntry = new ByteArrayInputStream(stream
                         .toByteArray());
                File file = File.createTempFile("TIKA_unzip_", "_" + entry.getName());
                
                // TODO we might want to delete the file earlier than on exit,
                // in case Tika is used inside a long-running app
                file.deleteOnExit();
                 saveInputStreamInFile(isEntry, new BufferedOutputStream(
                         new FileOutputStream(file)));
                 res.add(file);
                 isEntry.close();
             }
             in.close();
         } catch (IOException e) {
             logger.error(e.getMessage());
         }
         return res;
     }
 
     private static void saveInputStreamInFile(InputStream in, OutputStream out)
             throws IOException {
         byte[] buffer = new byte[1024];
         int len;
 
         while ((len = in.read(buffer)) >= 0)
             out.write(buffer, 0, len);
 
         in.close();
         out.close();
     }
 
     public static void saveInXmlFile(Document doc, String file) {
         Format f = Format.getPrettyFormat().setEncoding("UTF-8");
 
         XMLOutputter xop = new XMLOutputter(f);
 
         try {
 
             xop.output(doc, new FileOutputStream(file));
 
         }
 
         catch (IOException ex) {
 
             logger.error(ex.getMessage());
 
         }
     }
 
 }
