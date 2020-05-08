 package org.codehaus.xfire.xmlbeans;
 
import javax.xml.namespace.QName;
 import javax.xml.stream.XMLStreamException;
 
 import org.apache.xmlbeans.SchemaType;
 import org.apache.xmlbeans.XmlCursor;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlObject;
 import org.apache.xmlbeans.XmlOptions;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.message.MessageReader;
 import org.codehaus.xfire.message.MessageWriter;
 import org.codehaus.xfire.message.stax.ElementReader;
 import org.codehaus.xfire.message.stax.ElementWriter;
 import org.codehaus.xfire.type.Type;
 import org.codehaus.xfire.util.STAXUtils;
 import org.codehaus.yom.Element;
 
 /**
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Nov 13, 2004
  */
 public class XMLBeansType
 	extends Type
 {
    private SchemaType schemaType;

     final static XmlOptions options = new XmlOptions();
     static
     {
         options.setSaveInner();
     }
     
     public XMLBeansType()
     {
     }
     
     public XMLBeansType(SchemaType schemaType)
     {
        this.schemaType = schemaType;
     }
 
     public Object readObject(MessageReader reader, MessageContext context) 
     	throws XFireFault
     {
         try
         {
             return XmlObject.Factory.parse(((ElementReader) reader).getXMLStreamReader());
         }
         catch( XmlException e )
         {
             throw new XFireFault("Could not read request.", e, XFireFault.SENDER);
         }
     }
 
     public void writeObject(Object o, MessageWriter writer, MessageContext context)
     	throws XFireFault
     {
         try
         {
             XmlObject obj = (XmlObject) o; 
 
             XmlCursor cursor = obj.newCursor();
             if (cursor.toFirstChild() && cursor.toFirstChild())
             {
                 do
                 {
                     STAXUtils.copy(cursor.newXMLStreamReader(), 
                                    ((ElementWriter) writer).getXMLStreamWriter());
                 }
                 while(cursor.toNextSibling());
             }
         } 
         catch (XMLStreamException e)
         {
             throw new XFireFault("Could not write response.", e, XFireFault.SENDER);
         }
     }
 
     public void writeSchema(Element element)
     {
         // todo:
     }
     
     public boolean isComplex()
     {
         return true;
     }
    
    /**
     * @return Returns the schemaType.
     */
    public QName getSchemaType()
    {
        return schemaType.getDocumentElementName();
    }
 }
