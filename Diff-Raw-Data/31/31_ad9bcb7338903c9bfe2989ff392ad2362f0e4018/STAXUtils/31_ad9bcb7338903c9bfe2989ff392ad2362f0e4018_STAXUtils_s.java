 package org.codehaus.xfire.util;
 
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Common StAX utilities.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Oct 26, 2004
  */
 public class STAXUtils
 {
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
         
         while ( true )
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
         String prefix = reader.getPrefix();
         if ( prefix == null )
         {
             prefix = "";
         }
 
         boolean wroteDefault = false;
         String uri = reader.getNamespaceURI();
         
         // Write out the element name and possible the default namespace
         if ( uri != null )
         {
             String definedNS = writer.getNamespaceContext().getNamespaceURI(prefix);
             if ( prefix.equals("") )
             {
                 if ( definedNS == null || !definedNS.equals(uri) )
                 {
                     writer.setDefaultNamespace(uri);
                     writer.writeStartElement( uri, reader.getLocalName() );
                     writer.writeDefaultNamespace( uri );
                     wroteDefault = true;
                 }
                 else
                 {
                     writer.writeStartElement( uri, reader.getLocalName() );
                 }  
             }
             else
             {
                 if ( definedNS == null || !definedNS.equals(uri) )
                     writer.setPrefix( prefix, uri );
                 
                 writer.writeStartElement( 
                     prefix,
                     reader.getLocalName(),
                     uri);
             }
         }
         else
         {
             writer.writeStartElement( reader.getLocalName() );
         }
 
         // Write out the namespaces
         for ( int i = 0; i < reader.getNamespaceCount(); i++ )
         {
             String nsPrefix = reader.getNamespacePrefix(i);
             String nsURI = reader.getNamespaceURI(i);
             
             if ( nsPrefix == null || nsPrefix.length() ==  0 )
             {
                break;
             }
             
             writer.writeNamespace(nsPrefix, nsURI);
         }
 
         // Write out attributes
         for ( int i = 0; i < reader.getAttributeCount(); i++ )
         {
             String ns = reader.getAttributeNamespace(i);
             if ( ns == null || ns.length() == 0 )
             {
                 writer.writeAttribute(
                         reader.getAttributeLocalName(i),
                         reader.getAttributeValue(i));
             }
             else
             {
                 writer.writeAttribute(
                     reader.getAttributeNamespace(i),
                     reader.getAttributeLocalName(i),
                     reader.getAttributeValue(i));
             }
         }
     }
 
     /**
      * Writes an Element to an XMLStreamWriter.  The writer must already
      * have started the doucment (via writeStartDocument()).
      * 
      * @param e
      * @param writer
      * @throws XMLStreamException
      */
     public static void writeElement( Element e, XMLStreamWriter writer ) 
         throws XMLStreamException
     {
 
         String prefix = e.getPrefix();
         String ns = e.getNamespaceURI();
         System.out.println("ns:" + ns);
         String localName = e.getNodeName();
         
         if ( prefix == null )
         {
             if ( ns == null )
             {
                 writer.writeStartElement( localName );
             }
             else
             {
                 prefix = "";
                 
                 writer.writeStartElement( ns, localName );
                 
                 String curUri = writer.getNamespaceContext().getNamespaceURI(prefix);
                 if ( curUri == null || curUri.length() != ns.length() )
                 {
                     writer.writeDefaultNamespace(ns);
                 }
             }
         }
         else
         {
             writer.writeStartElement(prefix, localName, ns);
             
             String curUri = writer.getNamespaceContext().getNamespaceURI(prefix);
             if ( curUri == null || curUri.length() != ns.length() || !curUri.equals(ns) )
             {
                 System.out.println("writing namespace: " + ns);
                 writer.writeNamespace(prefix, ns);
             }
         }
 
         
         /*
         NamedNodeMap attrs = e.getAttributes();
         for ( int i = 0; i < attrs.getLength(); i++ )
         {
             Node attr = attrs.item(i);
             
             boolean writeAttrNamespace = false;
             String attrPrefix = writer.getNamespaceContext().getPrefix(attr.getNamespaceURI());
             if ( attrPrefix == null )
             {
                 writer.writeAttribute(attr.getNamespaceURI(), attr.getNodeName(), attr.getNodeValue());
             }
             else
             {
                 writer.writeAttribute(attrPrefix, attr.getNamespaceURI(), attr.getNodeName(), attr.getNodeValue());
             }
         }*/
     
         String value = DOMUtils.getContent(e);
         
         if ( value != null && value.length() > 0)
             writer.writeCharacters( value );
         
         NodeList nodes = e.getChildNodes();
         for ( int i = 0; i < nodes.getLength(); i++ )
         {
             Node n = nodes.item(i);
             if ( n instanceof Element )
             {
                 writeElement((Element)n, writer);
             }
         }
 
         writer.writeEndElement();
     }
     
     public static void readElements(Element root, XMLStreamReader reader)
     	throws XMLStreamException
     {
         int read = 0; // number of elements read in
         
         Document doc = root.getOwnerDocument();
         Element e = null;
         
         StringBuffer text = new StringBuffer();
         
         while ( true )
         {
             int event = reader.next();
             switch( event )
             {
                 case XMLStreamConstants.START_ELEMENT:
                     read++;
                     e = doc.createElementNS(reader.getNamespaceURI(), reader.getLocalName());
                     root.appendChild(e);
 
                     for ( int i = 0; i < reader.getAttributeCount(); i++ )
                     {
                         Attr attr = doc.createAttributeNS(reader.getAttributeNamespace(i),
                                                           reader.getAttributeLocalName(i));
                         attr.setValue(reader.getAttributeValue(i));
                         e.setAttributeNode(attr);
                     }
                     
                     readElements(e, reader);
                     break;
                 case XMLStreamConstants.END_ELEMENT:
                     DOMUtils.setText(root, text.toString());  
                     return;
                 case XMLStreamConstants.CHARACTERS:
                     text.append(reader.getText());
                     break;
                 case XMLStreamConstants.END_DOCUMENT:
                     return;
                 case XMLStreamConstants.CDATA:
                 case XMLStreamConstants.START_DOCUMENT:
                 case XMLStreamConstants.ATTRIBUTE:
                 case XMLStreamConstants.NAMESPACE:
                 default:
                     break;
             }
         }
     }
 }
