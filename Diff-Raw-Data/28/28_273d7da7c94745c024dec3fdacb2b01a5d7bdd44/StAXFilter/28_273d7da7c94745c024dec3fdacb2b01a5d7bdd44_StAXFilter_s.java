 package com.main.xmlfilter.parsers.stax;
 
 import com.main.xmlfilter.config.Config;
 import com.main.xmlfilter.XmlFilter;
 import com.main.xmlfilter.parsers.stax.elements.ElementType;
 import com.main.xmlfilter.parsers.stax.elements.XMLElement;
 
 import javax.xml.stream.*;
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 /**
  * StAX XML filter.
  *
  * @author sergiu.indrie
  */
 public class StAXFilter implements XmlFilter {
 
     private String filter;
 
     /**
      * the current depth
      */
     private int depth;
     /**
      * the filtered XML's elements
      */
     private Stack<XMLElement> elements;
     /**
      * indicates that the searched element was found in the current node
      */
     private boolean found = false;
     /**
      * tells the depth of the next tag that has to be added into the element stack even if the found element is not true
      */
     private int lastFoundDepth = -1;
     /**
      * tells if the custom node has been inserted
      */
     private boolean customNodeInserted = false;
 
     public StAXFilter() {
         elements = new Stack<XMLElement>();
     }
 
     public void filter(Reader reader, String filter, OutputStream outputStream) throws Exception {
         this.filter = filter;
         XMLInputFactory inputFactory = XMLInputFactory.newInstance();
         inputFactory.setProperty("javax.xml.stream.isValidating", false);
         XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(reader);
         XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
         // try to resolve indentation problem
         XMLStreamWriter writer = outputFactory.createXMLStreamWriter(outputStream);
         //        XMLStreamWriter writer = new IndentingXMLStreamWriter(outputFactory.createXMLStreamWriter(outputStream));     // compile error
         writer = new javanet.staxutils.IndentingXMLStreamWriter(writer);
         process(xmlStreamReader, writer);
         reader.close();
         writer.flush();
         writer.close();
     }
 
     /**
      * Holds the main filter logic.
      */
     private void process(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
         while (reader.hasNext()) {
             int event = reader.next();
 
             switch (event) {
                 case XMLStreamReader.START_ELEMENT:
                     startElement(reader);
                     break;
                 case XMLStreamReader.CHARACTERS:
                     characters(reader);
                     break;
                 case XMLStreamReader.END_ELEMENT:
                     endElement(reader);
                     break;
                 case XMLStreamReader.END_DOCUMENT:
                     endDocument(writer);
                     break;
             }
         }
     }
 
     private void startElement(XMLStreamReader reader) {
         String fullName = getFullName(reader.getPrefix(), reader.getLocalName());
         Map<String, String> attributes = buildAttributeMap(reader);
         elements.push(new XMLElement(ElementType.START, reader.getPrefix(), reader.getLocalName(), null, attributes));
 
         // if search depth has not been reached, don't start searching
         if (depth >= Config.getSearchDepth()) {
             if (Config.match(filter, fullName)) {
                 found = true;
                 lastFoundDepth = depth;
             } else if (attributes.size() > 0) {
                 for (String attributeName : attributes.keySet()) {
                     if (Config.match(filter, attributes.get(attributeName)) || Config.match(filter, attributeName)) {
                         found = true;
                         lastFoundDepth = depth;
                         break;
                     }
                 }
             }
         }
         depth++;
     }
 
     private Map<String, String> buildAttributeMap(XMLStreamReader reader) {
         Map<String, String> result = new HashMap<String, String>(reader.getAttributeCount());
         for (int i = 0; i < reader.getAttributeCount(); i++) {
            result.put(getFullName(reader.getAttributePrefix(i), reader.getAttributeValue(i)), reader.getAttributeValue(i));
         }
         return result;
     }
 
     private void endElement(XMLStreamReader reader) {
         String qName = getFullName(reader.getPrefix(), reader.getLocalName());
         elements.push(new XMLElement(ElementType.END, reader.getPrefix(), reader.getLocalName(), null, null));
         depth--;
 
         if (depth <= Config.getSearchDepth() && !found) {
             // if this depth is not the expected one, remove the one
             if (depth != lastFoundDepth - 1) {
                 // we are outside the search level and the filter has not been found, remove this node
                 popNode(qName);
             }
         } else if (depth <= Config.getSearchDepth() && found) {
             // the search level has been reached and a filter was found, leave the elements in the stack and continue search
             found = false;
 
             // add the custom node
             if (!customNodeInserted) {
                 pushCustomNode();
                 customNodeInserted = true;
             }
         }
 
         // if the depth is the expected one, change it accordingly
         if (depth == lastFoundDepth - 1) {
             lastFoundDepth--;
         }
     }
 
     private void characters(XMLStreamReader reader) {
         String data = reader.getText();
         if (data.contains("\n")) {
             return;
         }
 
         if (Config.match(filter, data)) {
             found = true;
             lastFoundDepth = depth;
         }
         elements.push(new XMLElement(ElementType.DATA, null, null, data, null));
     }
 
     private void endDocument(XMLStreamWriter writer) throws XMLStreamException {
         for (XMLElement element : elements) {
             switch (element.getType()) {
                 case START:
                     writer.writeStartElement(element.getPrefix() != null ? element.getPrefix() : "", element.getLocalName(), "");
                     Map<String, String> attributes = element.getAttributes();
                     if (attributes != null && attributes.size() > 0) {
                         for (String attribute : attributes.keySet()) {
                             writer.writeAttribute(attribute, attributes.get(attribute));
                         }
                     }
                     break;
                 case DATA:
                     writer.writeCharacters(element.getData());
                     break;
                 case END:
                     writer.writeEndElement();
                     break;
             }
         }
     }
 
     private void popNode(String qName) {
         boolean nodeFound;
         // remove first node with identical name
         do {
             elements.pop();
             XMLElement element = elements.peek();
             nodeFound = qName.equals(getFullName(element.getPrefix(), element.getLocalName()));
         } while (!nodeFound);
 
         elements.pop();
     }
 
     private void pushCustomNode() {
         String name = Config.getInsertionName();
         Map<String, String> attributes = new HashMap<String, String>();
         attributes.put(name + "Attr", name + "Value");
         elements.push(new XMLElement(ElementType.START, null, name, null, attributes));
         elements.push(new XMLElement(ElementType.START, null, name + "Child", null, null));
         elements.push(new XMLElement(ElementType.DATA, null, null, name + "Data", null));
         elements.push(new XMLElement(ElementType.END, null, name + "Child", null, null));
         elements.push(new XMLElement(ElementType.END, null, name, null, null));
     }
 
     private String getFullName(String prefix, String local) {
         StringBuilder qName = new StringBuilder();
         qName.append(prefix != null ? prefix + ":" : "");
         qName.append(local != null ? local : "");
         return qName.toString();
     }
 
     public static void main(String[] args) throws Exception {
         String filename = "D:\\sample.xml";
         String filter = "apple";
         String outputFile = "D:\\output.xml";
         InputStream inputStream = new FileInputStream(filename);
         Reader reader = new InputStreamReader(inputStream, Config.ENCODING);
         FileOutputStream outputStream = new FileOutputStream(outputFile);
         new StAXFilter().filter(reader, filter, outputStream);
         inputStream.close();
         outputStream.close();
     }
 }
