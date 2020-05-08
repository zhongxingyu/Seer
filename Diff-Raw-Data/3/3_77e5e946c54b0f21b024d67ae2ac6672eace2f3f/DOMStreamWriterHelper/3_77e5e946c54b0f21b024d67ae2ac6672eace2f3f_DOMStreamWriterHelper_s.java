 package org.codehaus.xfire.util.stax;
 
 import javax.xml.stream.XMLStreamWriter;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 /**
  * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
  * 
  */
 public class DOMStreamWriterHelper
 {
     /**
      * Its a tool class, so don't allow to create any instance.
      */
     private DOMStreamWriterHelper()
     {
 
     }
 
     /**
      * @param writer
      * @param document
      * @throws Exception
      */
     public static void write(XMLStreamWriter writer, Document document)
         throws Exception
     {
         writer.writeStartDocument();
         Element root = document.getDocumentElement();
         writeElement(writer, root);
         writer.writeEndDocument();
     }
 
     /**
      * @param writer
      * @param element
      * @throws Exception
      */
     private static void writeElement(XMLStreamWriter writer, Element element)
         throws Exception
     {
         String name = element.getLocalName();
         String prefix = element.getPrefix();
         String uri = element.getNamespaceURI();
         if (uri != null)
         {
             if (prefix != null)
             {
                 writer.writeStartElement(prefix, name, uri);
             }
             else
             {
                 writer.writeStartElement(uri, name);
             }
         }
         else
         {
             writer.writeStartElement(name);
         }
 
         writeAttributes(writer, element);
 
         NodeList nodeList = element.getChildNodes();
         for (int n = 0; n < nodeList.getLength(); n++)
         {
             Node node = nodeList.item(n);
             if (node instanceof Element)
             {
                 writeElement(writer, (Element) node);
             }
             else
             {
                 if (node instanceof Text)
                 {
                     writer.writeCharacters(((Text) node).getData());
                 }
             }
         }
         writer.writeEndElement();
     }
 
     /**
      * @param writer
      * @param element
      * @throws Exception
      */
     private static void writeAttributes(XMLStreamWriter writer, Element element)
         throws Exception
     {
         NamedNodeMap attrs = element.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++)
         {
             Node node = attrs.item(i);
             String localName = node.getLocalName();
             String nodeUri = node.getNamespaceURI();
             String nodePrefix = node.getPrefix();
             String nodeValue = node.getNodeValue();
             writer.writeAttribute(nodePrefix, nodeUri, localName, nodeValue);
         }
 
     }
 }
