 package com.ztools.xml;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.List;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 public class XMLReader implements Serializable {
     /**
 	 * 
 	 */
     private static final long serialVersionUID = 2102385690043309167L;
 
     public static void readXMLBean(XMLBean xmlBean) {
         if (null != xmlBean) {
             String filename = null;
             filename = xmlBean.getPath();
             SAXParserFactory spf = SAXParserFactory.newInstance();
             SAXParser saxParser = null;
 
             AbsHandler handler = xmlBean.getHandler();
             if (null == handler) {
                XMLHandler mr = new XMLHandler(xmlBean);
//                handler = mr;//new ZHandler();
                 handler = new ZHandler();
             }
             try {
                 saxParser = spf.newSAXParser();
                 saxParser.parse(new File(filename), handler);
                if (null != handler.getXmlObject()) {
                    xmlBean.setBean(handler.getXmlObject());
                    if (handler.getXmlObject() instanceof List<?>) {
                        xmlBean.setItemList((List<?>)handler.getXmlObject());
                    }
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     public static Object  xmlStringToObject(String str) {
         return xmlStringToObject(str, null);
     }
 
     public static Object xmlStringToObject(String str, AbsHandler handler) {
         if (null != str) {
             ByteArrayInputStream is = new ByteArrayInputStream(str.getBytes());
             return xmlStreamToObject(is, handler);
         }
         return null;
     }
 
     public static Object xmlStreamToObject(InputStream is, AbsHandler handler) {
         if (null != is) {
             try {
                 SAXParserFactory spf = SAXParserFactory.newInstance();
                 SAXParser saxParser = null;
 
                 if (null == handler) {
                     handler = new ZHandler();
                 }
                 saxParser = spf.newSAXParser();
                 saxParser.parse(is, handler);
                 return (handler.getXmlObject());
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                 try {
                     is.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
         return null;
     }
 
 }
