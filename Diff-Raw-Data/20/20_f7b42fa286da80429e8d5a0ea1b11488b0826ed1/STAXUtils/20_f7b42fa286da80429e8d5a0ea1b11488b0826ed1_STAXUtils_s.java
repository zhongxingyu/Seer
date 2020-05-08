 package org.codehaus.xfire.util;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Reader;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.util.stax.DepthXMLStreamReader;
 import org.w3c.dom.Attr;
 import org.w3c.dom.CDATASection;
 import org.w3c.dom.Comment;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 /**
  * Common StAX utilities.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Oct 26, 2004
  */
 public class STAXUtils
 {
     private static final String XML_NS = "http://www.w3.org/2000/xmlns/";
 
     /**
      * Returns true if currently at the start of an element, otherwise move forwards to the next
      * element start and return true, otherwise false is returned if the end of the stream is reached.
      */
     public static boolean skipToStartOfElement(XMLStreamReader in)
         throws XMLStreamException
     {
         for (int code = in.getEventType(); code != XMLStreamReader.END_DOCUMENT; code = in.next())
         {
             if (code == XMLStreamReader.START_ELEMENT)
             {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean toNextElement(DepthXMLStreamReader dr)
     {
         if (dr.getEventType() == XMLStreamReader.START_ELEMENT)
             return true;
         
         if (dr.getEventType() == XMLStreamReader.END_ELEMENT)
             return false;
         
         try
         {
             int depth = dr.getDepth();
             
             for (int event = dr.getEventType(); dr.getDepth() >= depth && dr.hasNext(); event = dr.next())
             {
                 if (event == XMLStreamReader.START_ELEMENT && dr.getDepth() == depth + 1)
                 {
                     return true;
                 }
                 else if (event == XMLStreamReader.END_ELEMENT)
                 {
                     depth--;
                 }
             }
             
             return false;
         }
         catch (XMLStreamException e)
         {
             throw new XFireRuntimeException("Couldn't parse stream.", e);
         }
     }
    
     public static boolean skipToStartOfElement(DepthXMLStreamReader in)
         throws XMLStreamException
     {
         for (int code = in.getEventType(); code != XMLStreamReader.END_DOCUMENT; code = in.next())
         {
             if (code == XMLStreamReader.START_ELEMENT)
             {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Copies the reader to the writer.  The start and end document
      * methods must be handled on the writer manually.
      * 
      * TODO: if the namespace on the reader has been declared previously
      * to where we are in the stream, this probably won't work.
      * 
      * @param reader
      * @param writer
      * @throws XMLStreamException
      */
     public static void copy( XMLStreamReader reader, XMLStreamWriter writer ) 
         throws XMLStreamException
     {
         int read = 0; // number of elements read in
         int event = reader.getEventType();
         
         while ( reader.hasNext() )
         {
             switch( event )
             {
                 case XMLStreamConstants.START_ELEMENT:
                     read++;
                     writeStartElement( reader, writer );
                     break;
                 case XMLStreamConstants.END_ELEMENT:
                     writer.writeEndElement();
                     read--;
                     if ( read <= 0 )
                         return;
                     break;
                 case XMLStreamConstants.CHARACTERS:
                     writer.writeCharacters( reader.getText() );  
                     break;
                 case XMLStreamConstants.START_DOCUMENT:
                 case XMLStreamConstants.END_DOCUMENT:
                 case XMLStreamConstants.ATTRIBUTE:
                 case XMLStreamConstants.NAMESPACE:
                     break;
                 default:
                     break;
             }
             event = reader.next();
         }
     }
 
     private static void writeStartElement(XMLStreamReader reader, XMLStreamWriter writer) 
         throws XMLStreamException
     {
         String local = reader.getLocalName();
         String uri = reader.getNamespaceURI();
         String prefix = reader.getPrefix();
         if (prefix == null)
         {
             prefix = "";
         }
         
         String boundPrefix = writer.getPrefix(uri);
         boolean writeElementNS = false;
         if ( boundPrefix == null || !prefix.equals(boundPrefix) )
         {   
             writeElementNS = true;
         }
         
         // Write out the element name
         if (uri != null)
         {
             if (prefix.length() == 0) 
             { 
                 writer.writeStartElement(local); 
                 writer.setDefaultNamespace(uri); 
             } 
             else 
             { 
                 writer.writeStartElement(prefix, local, uri); 
                 writer.setPrefix(prefix, uri); 
             } 
         }
         else
         {
             writer.writeStartElement( reader.getLocalName() );
         }
 
         // Write out the namespaces
         for ( int i = 0; i < reader.getNamespaceCount(); i++ )
         {
             String nsURI = reader.getNamespaceURI(i);
             String nsPrefix = reader.getNamespacePrefix(i);
             if (nsPrefix == null) nsPrefix = "";
             
             if ( nsPrefix.length() ==  0 )
             {
                 writer.writeDefaultNamespace(nsURI);
             }
             else
             {
                 writer.writeNamespace(nsPrefix, nsURI);
             }
 
             if (nsURI.equals(uri) && nsPrefix.equals(prefix))
             {
                 writeElementNS = false;
             }
         }
         
         // Check if the namespace still needs to be written.
         // We need this check because namespace writing works 
         // different on Woodstox and the RI.
         if (writeElementNS)
         {
             if ( prefix == null || prefix.length() ==  0 )
             {
                 writer.writeDefaultNamespace(uri);
             }
             else
             {
                 writer.writeNamespace(prefix, uri);
             }
         }
 
         // Write out attributes
         for ( int i = 0; i < reader.getAttributeCount(); i++ )
         {
             String ns = reader.getAttributeNamespace(i);
             String nsPrefix = reader.getAttributePrefix(i);
             if ( ns == null || ns.length() == 0 )
             {
                 writer.writeAttribute(
                         reader.getAttributeLocalName(i),
                         reader.getAttributeValue(i));
             }
             else if (nsPrefix == null || nsPrefix.length() == 0)
             {
                 writer.writeAttribute(
                     reader.getAttributeNamespace(i),
                     reader.getAttributeLocalName(i),
                     reader.getAttributeValue(i));
             }
             else
             {
                 writer.writeAttribute(reader.getAttributePrefix(i),
                                       reader.getAttributeNamespace(i),
                                       reader.getAttributeLocalName(i),
                                       reader.getAttributeValue(i));
             }
             
             
         }
     }
 
     public static void writeDocument( Document d, XMLStreamWriter writer, boolean repairing ) 
         throws XMLStreamException
     {
         writer.writeStartDocument();
         Element root = d.getDocumentElement();
         writeElement(root, writer, repairing);
         writer.writeEndDocument();
     }
 
     /**
      * Writes an Element to an XMLStreamWriter.  The writer must already
      * have started the doucment (via writeStartDocument()). Also, this probably
      * won't work with just a fragment of a document. The Element should be
      * the root element of the document.
      * 
      * @param e
      * @param writer
      * @throws XMLStreamException
      */
     public static void writeElement( Element e, XMLStreamWriter writer, boolean repairing ) 
         throws XMLStreamException
     {
         String prefix = e.getPrefix();
         String ns = e.getNamespaceURI();
         String localName = e.getLocalName();
 
         if (prefix == null) prefix = "";
         
         String decUri = writer.getNamespaceContext().getNamespaceURI(prefix);
         boolean declareNamespace = (decUri == null || !decUri.equals(ns));
 
         if (ns == null || ns.length() == 0)
         {
             writer.writeStartElement( localName );
         }
         else
         {
             writer.writeStartElement(prefix, localName, ns);
         }
 
         NamedNodeMap attrs = e.getAttributes();
         for ( int i = 0; i < attrs.getLength(); i++ )
         {
             Node attr = attrs.item(i);
             
             String name = attr.getNodeName();
             String attrPrefix = "";
             int prefixIndex = name.indexOf(':');
             if (prefixIndex != -1)
             {
                 attrPrefix = name.substring(0, prefixIndex);
                 name = name.substring(prefixIndex+1);
             }
 
             if (attrPrefix.equals("xmlns"))
             {
             	writer.writeNamespace(name, attr.getNodeValue());
                 if (name.equals(prefix) && attr.getNodeValue().equals(ns))
                 {
                     declareNamespace = false;
                 }
             }
             else
             {
             	if (name.equals("xmlns") && attrPrefix.equals(""))
             	{
             		writer.writeNamespace("", attr.getNodeValue());
             		if (attr.getNodeValue().equals(ns)) 
             		{
             			declareNamespace = false;
             		}
             	} 
             	else
             	{
             		writer.writeAttribute(attrPrefix, attr.getNamespaceURI(), name, attr.getNodeValue());
                 }
             }
         }
 
         if (declareNamespace && repairing)
         {
             writer.writeNamespace(prefix, ns);
         }
         
         NodeList nodes = e.getChildNodes();
         for ( int i = 0; i < nodes.getLength(); i++ )
         {
             Node n = nodes.item(i);
             if ( n instanceof Element )
             {
                 writeElement((Element)n, writer, repairing);
             }
             else if ( n instanceof Text )
             {
                 writer.writeCharacters(((Text) n).getNodeValue());
             }
             else if ( n instanceof CDATASection )
             {
                 writer.writeCData(((CDATASection) n).getData());
             }
             else if ( n instanceof Comment )
             {
                 writer.writeComment(((Comment) n).getData());
             }
         }
 
         writer.writeEndElement();
     }
 
     public static Document read(DocumentBuilder builder, XMLStreamReader reader, boolean repairing)
         throws XMLStreamException
     {
         Document doc = builder.newDocument();
 
         readDocElements(doc, reader, repairing);
         
         return doc;
     }
 
     /**
      * @param parent
      * @return
      */
     private static Document getDocument(Node parent)
     {
         return (parent instanceof Document) ? (Document) parent : parent.getOwnerDocument();
     }
 
     /**
      * @param parent
      * @param reader
      * @return
      * @throws XMLStreamException
      */
     private static Element startElement(Node parent, XMLStreamReader reader, boolean repairing)
         throws XMLStreamException
     {
         Document doc = getDocument(parent);
         
         Element e = doc.createElementNS(reader.getNamespaceURI(), reader.getLocalName());
         e.setPrefix(reader.getPrefix());
         parent.appendChild(e);
         
         declareNamespaces(reader, e);
         
        if (repairing)
         {
            if (!isDeclared(e, reader.getNamespaceURI(), reader.getPrefix()))
            {
                declare(e, reader.getNamespaceURI(), reader.getPrefix());
            }
         }
         
         for (int i = 0; i < reader.getAttributeCount(); i++)
         {
            Attr attr = doc.createAttributeNS(reader.getAttributeNamespace(i), reader
                    .getAttributeLocalName(i));
             attr.setValue(reader.getAttributeValue(i));
             e.setAttributeNode(attr);
         }
         
         reader.next();
         
         readDocElements(e, reader, repairing);
         
         return e;
     }
 
     private static boolean isDeclared(Element e, String namespaceURI, String prefix)
     {
         Attr att;
         if (prefix != null && prefix.length() > 0)
         {
             att = e.getAttributeNodeNS(XML_NS, "xmlns:" + prefix);
         }
         else 
         {
             att = e.getAttributeNode("xmlns");
         }
         
         if (att != null && att.getNodeValue().equals(namespaceURI))
             return true;
         
         if (e.getParentNode() instanceof Element)
             return isDeclared((Element) e.getParentNode(), namespaceURI, prefix);
         
         return false;
     }
     
 
     /**
      * @param parent
      * @param reader
      * @throws XMLStreamException
      */
     public static void readDocElements(Node parent, XMLStreamReader reader, boolean repairing)
         throws XMLStreamException
     {
         Document doc = getDocument(parent);
 
         int event = reader.getEventType();
         while (reader.hasNext())
         {
             switch (event)
             {
             case XMLStreamConstants.START_ELEMENT:
                 startElement(parent, reader, repairing);
 
                 if (parent instanceof Document) 
                 {
                     if (reader.hasNext()) reader.next();
                     return;
                 }
                 
                 break;
             case XMLStreamConstants.END_ELEMENT:
                 return;
             case XMLStreamConstants.CHARACTERS:
                 if (parent != null)
                 {
                     parent.appendChild(doc.createTextNode(reader.getText()));
                 }
 
                 break;
             case XMLStreamConstants.COMMENT:
                 if (parent != null)
                 {
                     parent.appendChild(doc.createComment(reader.getText()));
                 }
 
                 break;
             default:
                 break;
             }
             
             if (reader.hasNext())
             {
                 event = reader.next();
             }
         }
     }
 
     private static void declareNamespaces(XMLStreamReader reader, Element node)
     {
         for (int i = 0; i < reader.getNamespaceCount(); i++)
         {
             String uri = reader.getNamespaceURI(i);
             String prefix = reader.getNamespacePrefix(i);
            
             declare(node, uri, prefix);
         }
     }
 
     private static void declare(Element node, String uri, String prefix)
     {
         if (prefix != null && prefix.length() > 0)
         {
             node.setAttributeNS(XML_NS, "xmlns:" + prefix, uri);
         }
         else
         {
             if (uri != null && uri.length() > 0)
             {
                 node.setAttribute("xmlns", uri);
             }
         }
     }
 
     public static XMLStreamWriter createXMLStreamWriter(OutputStream out, String encoding)
     {
         XMLOutputFactory factory = XMLOutputFactory.newInstance();
 
         if (encoding == null) encoding = "UTF-8";
         
         try
         {
             XMLStreamWriter writer = factory.createXMLStreamWriter(out, encoding);
 
             return writer;
         }
         catch (XMLStreamException e)
         {
             throw new XFireRuntimeException("Couldn't parse stream.", e);
         }
     }
 
     public static XMLStreamReader createXMLStreamReader(InputStream in, String encoding)
     {
         XMLInputFactory factory = XMLInputFactory.newInstance();
 
         if (encoding == null) encoding = "UTF-8";
         
         try
         {
             return factory.createXMLStreamReader(in, encoding);
         }
         catch (XMLStreamException e)
         {
             throw new XFireRuntimeException("Couldn't parse stream.", e);
         }
     }
 
     public static XMLStreamReader createXMLStreamReader(Reader reader)
     {
         XMLInputFactory factory = XMLInputFactory.newInstance();
  
         try
         {
             return factory.createXMLStreamReader(reader);
         }
         catch (XMLStreamException e)
         {
             throw new XFireRuntimeException("Couldn't parse stream.", e);
         }
     }
 }
