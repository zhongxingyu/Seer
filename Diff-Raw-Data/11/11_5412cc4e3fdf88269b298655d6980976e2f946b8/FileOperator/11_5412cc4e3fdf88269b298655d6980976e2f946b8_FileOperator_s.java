 package com.dfgames.lastplanet.tools.dialog_creator.utils;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 /**
  * Author: Ivan Melnikov
  * Date: 11.11.12 21:38
  */
 public class FileOperator {
     public static String readFile(File file) {
         final StringBuilder text = new StringBuilder();
 
         XMLReader reader = null;
         try {
             reader = XMLReaderFactory.createXMLReader();
             DefaultHandler handler = new DefaultHandler() {
                 String thisElement = "";
                 @Override
                 public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                     if(qName.equals("message")) {
                         thisElement = qName;
                     }
                 }
                 public void characters(char[] ch, int start, int length) throws SAXException {
                     if(thisElement.equals("message")) {
                         text.append(new String(ch, start, length).trim() + "\n");
                     }
                 }
             };
             reader.setContentHandler(handler);
             reader.parse(new InputSource(new FileReader(file.getPath())));
         } catch (SAXException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return text.toString();
     }
 
     public static String buildContent(String text) {
         StringBuilder content = new StringBuilder();
 
         String[] records = text.split("\n\n");
 
         content.append("<last_planet>" + "\n");
         for(int i = 0; i < records.length; i++) {
             content.append("    <message>" + records[i] + "</message>" + "\n");
         }
        content.append("<last_planet>" + "\n");
 
         return content.toString();
     }
 }
