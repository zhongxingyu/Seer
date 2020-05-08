 package org.powerbat.methods;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 
 public class XMLParser {
 
     private static XMLParser instance;
     private final DocumentBuilder builder;
     private Document document;
 
     public static XMLParser getInstance() {
         if (instance == null) {
             try {
                 instance = new XMLParser();
             } catch (final ParserConfigurationException e) {
                 e.printStackTrace();
             }
         }
         return instance;
     }
 
     private XMLParser() throws ParserConfigurationException {
         final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         builder = factory.newDocumentBuilder();
     }
 
     public void prepare(final File file) throws IOException, SAXException {
         document = builder.parse(file);
         document.getDocumentElement().normalize();
     }
 
     public HashMap<String, String> getAttributeMapping() {
         final HashMap<String, String> map = new HashMap<>();
         addAll(map, document.getChildNodes());
         return map;
     }
 
     private void addAll(final HashMap<String, String> map, final NodeList list) {
         for (int i = 0; i < list.getLength(); i++) {
             final Node n = list.item(i);
             if (n.getNodeType() == Node.ELEMENT_NODE) {
                 final Element e = (Element) n;
                 addAll(map, e.getChildNodes());
                 if (!e.getAttribute("value").isEmpty()) {
                     final String attrib = e.getAttribute("value").replace("&quot;", "\"");
                     if (map.containsKey(e.getTagName())) {
                         map.put(e.getTagName(), map.get(e.getTagName()) + ", " + attrib);
                     } else {
                         map.put(e.getTagName(), attrib);
                     }
                 }
             }
         }
     }
 
 }
