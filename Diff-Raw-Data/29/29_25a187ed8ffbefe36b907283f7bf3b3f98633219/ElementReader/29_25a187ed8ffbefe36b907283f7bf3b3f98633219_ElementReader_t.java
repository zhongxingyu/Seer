 package org.codehaus.xfire.aegis.stax;
 
 import java.io.InputStream;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.aegis.AbstractMessageReader;
 import org.codehaus.xfire.aegis.MessageReader;
 import org.codehaus.xfire.util.STAXUtils;
 import org.codehaus.xfire.util.stax.DepthXMLStreamReader;
 
 /**
  * Reads literal encoded messages.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class ElementReader
     extends AbstractMessageReader
     implements MessageReader
 {
     private DepthXMLStreamReader root;
     private StringBuffer value;
     private String localName;
     private QName name;
     private boolean hasCheckedChildren = false;
     private boolean hasChildren = false;
     private boolean hasFoundText = false;
     private String namespace;
     private int depth;
     private int currentAttribute = 0;
     
     /**
      * @param root
      */
     public ElementReader(DepthXMLStreamReader root)
     {
         this.root = root;
         this.localName = root.getLocalName();
         this.name = root.getName();
         this.namespace = root.getNamespaceURI();
         
         depth = root.getDepth();
     }
     
     public ElementReader(XMLStreamReader reader)
     {
         this(new DepthXMLStreamReader(reader));
     }
 
     /**
      * @param is
      * @throws XMLStreamException 
      */
     public ElementReader(InputStream is) 
         throws XMLStreamException
     {
         //XMLInputFactory factory = XMLInputFactory.newInstance();
         //XMLStreamReader xmlReader = factory.createXMLStreamReader(is);
         XMLStreamReader xmlReader = STAXUtils.createXMLStreamReader(is,null,null);
 
        xmlReader.nextTag();
         
         this.root = new DepthXMLStreamReader(xmlReader);
         this.localName = root.getLocalName();
         this.name = root.getName();
         this.namespace = root.getNamespaceURI();
         
         depth = root.getDepth();
     }
 
     /**
      * @see org.codehaus.xfire.aegis.MessageReader#getValue()
      */
     public String getValue()
     {
         while( !hasFoundText && checkHasMoreChildReaders() )
         {
             if (hasChildren)
                 readToEnd(this);
         }
 
         if (value == null)
             return "";
         
         return value.toString().trim();
     }
     
     private void readToEnd(MessageReader childReader)
     {
         if (value == null) value = new StringBuffer();
         
         while (childReader.hasMoreElementReaders())
         {
             MessageReader reader = childReader.getNextElementReader();
             String s = reader.getValue();
             if (s != null) value.append(s);
         }
     }
 
     public String getValue( String ns, String attr )
     {
         return root.getAttributeValue(ns, attr);
     }
 
     public boolean hasMoreElementReaders()
     {
         // Check to see if we checked before, 
         // so we don't mess up the stream position.
         if ( !hasCheckedChildren )
             checkHasMoreChildReaders();
         
         return hasChildren;
     }
     
     private boolean checkHasMoreChildReaders()
     {
         try
         {
             int event = root.getEventType();
             while ( root.hasNext() )
             {
                 switch( event )
                 {
                 case XMLStreamReader.START_ELEMENT:
                     if ( root.getDepth() > depth )
                     {
                         hasCheckedChildren = true;
                         hasChildren = true;
 
                         return true;
                     }
                     break;
                 case XMLStreamReader.END_ELEMENT:
                     if ( root.getDepth() <= depth )
                     {
                         hasCheckedChildren = true;
                         hasChildren = false;
                         
                         if (root.hasNext()) root.next();
                         return false;
                     }
                     break;
                 case XMLStreamReader.CHARACTERS:
                     if (value == null)
                         value = new StringBuffer();
                     
                     value.append(root.getText());
                     hasFoundText = true;
                     break;
                 case XMLStreamReader.END_DOCUMENT:
                     // We should never get here...
                     hasCheckedChildren = true;
                     hasChildren = false;
                     return false;
                 default:
                     break;
                 }
                 
                 if (root.hasNext())
                     event = root.next();
             }
             
             hasCheckedChildren = true;
             hasChildren = false;
             return false;
         }
         catch (XMLStreamException e)
         {
             throw new XFireRuntimeException("Error parsing document.", e);
         }
     }
 
     public MessageReader getNextElementReader()
     {
         if ( !hasCheckedChildren )
             checkHasMoreChildReaders();
             
         if ( !hasChildren )
             return null;
         
         hasCheckedChildren = false;
 
         return new ElementReader( root );
     }
     
     public QName getName()
     {
         return name;
     }
 
     public String getLocalName()
     {
         return localName;
     }
 
     public String getNamespace()
     {
         return namespace;
     }
 
     public XMLStreamReader getXMLStreamReader()
     {
         return root;
     }
 
     public boolean hasMoreAttributeReaders()
     {
        if (!root.isStartElement()) return false;
        
         return currentAttribute < root.getAttributeCount();
     }
 
     public MessageReader getAttributeReader( QName qName )
     {
         return new AttributeReader( qName, root.getAttributeValue( qName.getNamespaceURI(), qName.getLocalPart() ) );
     }
     
     public MessageReader getNextAttributeReader()
     {
         MessageReader reader = new AttributeReader(root.getAttributeName(currentAttribute),
                                                    root.getAttributeValue(currentAttribute));
         currentAttribute++;
         
         return reader;
     }
 
     public String getNamespaceForPrefix( String prefix )
     {
         return root.getNamespaceURI( prefix );
     }
 }
