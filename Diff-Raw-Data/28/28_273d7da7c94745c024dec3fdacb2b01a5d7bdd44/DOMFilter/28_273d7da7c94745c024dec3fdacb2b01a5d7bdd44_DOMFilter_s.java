 package com.main.xmlfilter.parsers.dom;
 
 import com.main.xmlfilter.XmlFilter;
 import com.main.xmlfilter.config.Config;
 import org.w3c.dom.*;
 import org.xml.sax.InputSource;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import java.io.*;
 
 /**
  * XML Filter using DOM parser
  *
  * @author sergiu.indrie
  */
 public class DOMFilter implements XmlFilter {
 
     private String filter;
     private int depth = -1;
     private boolean found = false;
     private boolean firstFound = false;
     private Document document;
 
     public void filter(Reader reader, String filter, OutputStream outputStream) throws Exception {
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         document = dBuilder.parse(new InputSource(reader));
         this.filter = filter;
         process(document);
         writeOutput(document, outputStream);
     }
 
     private void writeOutput(Document doc, OutputStream outputStream) throws TransformerException {
         // Use a Transformer for output
         TransformerFactory tFactory = TransformerFactory.newInstance();
         Transformer transformer = tFactory.newTransformer();
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 
         DOMSource source = new DOMSource(doc);
         StreamResult result = new StreamResult(outputStream);
         transformer.transform(source, result);
     }
 
     /**
      * Checks the given node and its children for the given filter and removes all nodes from the search depth on that do not contain it leaving only the filter nodes in the DOM.
      *
      * @param node the DOM node to search in
      */
     private void process(Node node) {
         depth++;
 
         if (depth >= Config.getSearchDepth()) {
             switch (node.getNodeType()) {
                 case Node.ELEMENT_NODE:
                     handleElement(node);
                     break;
                case Node.ATTRIBUTE_NODE:
                    handleAttribute(node);
                    break;
                 case Node.TEXT_NODE:
                     handleText(node);
                     break;
             }
         }
 
         if (node.hasChildNodes()) {
             Node current = node.getFirstChild();
             Node next;
             while (current != null) {
                 next = current.getNextSibling();       // need to store the next sibling since node deletion invalidates the links
                 process(current);
                 current = next;
             }
         }
 
         depth--;
 
         // if we just reached the search level and nothing has been found, delete the node
         if (depth == Config.getSearchDepth() && !found) {
             node.getParentNode().removeChild(node);
         }
        // if we just reached the search level and the filter was found, reset the filter flag and leave the node alone
        else if (depth == Config.getSearchDepth() && found) {
            found = false;
        }
         // if we just reached the search level and the filter was found for the first time, insert the custom node
         else if (depth == Config.getSearchDepth() && found && !firstFound) {
             insertCustomNode(node);
             firstFound = true;
         }
     }
 
     private void insertCustomNode(Node node) {
         String name = Config.getInsertionName();
         Element newNode = document.createElement(name);
         Attr attribute = document.createAttribute(name + "Attr");
         attribute.setValue(name + "Value");
         newNode.setAttributeNode(attribute);
         Element child = document.createElement(name + "Child");
         Text text = document.createTextNode(name + "Data");
         child.appendChild(text);
         newNode.appendChild(child);
 
         Node parentNode = node.getParentNode();
         if (node.getNextSibling() == null) {
             parentNode.appendChild(newNode);
         } else {
             parentNode.insertBefore(newNode, node.getNextSibling());
         }
     }
 
     private void handleText(Node node) {
         if (Config.match(filter, node.getNodeValue())) {
             found = true;
         }
     }
 
     private void handleAttribute(Node node) {
         if (Config.match(filter, node.getNodeName()) || Config.match(filter, node.getNodeValue())) {
             found = true;
         }
     }
 
     private void handleElement(Node node) {
         if (Config.match(filter, node.getNodeName())) {
             found = true;
         }
     }
 
     public static void main(String[] args) throws Exception {
         String filename = "D:\\sample.xml";
         String filter = "apple";
         String outputFile = "D:\\output.xml";
         InputStream inputStream = new FileInputStream(filename);
         Reader reader = new InputStreamReader(inputStream, Config.ENCODING);
         FileOutputStream outputStream = new FileOutputStream(outputFile);
         new DOMFilter().filter(reader, filter, outputStream);
         inputStream.close();
         outputStream.close();
     }
 }
