 package org.codehaus.xfire.handler;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.codehaus.xfire.MessageContext;
 
 /**
  * Delegates the SOAP Body and Header to appropriate handlers.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Oct 28, 2004
  */
 public class SoapHandler 
     extends AbstractHandler
 {
     private Handler bodyHandler;
     private Handler headerHandler;
 
     public SoapHandler( Handler bodyHandler )
     {
         this.bodyHandler = bodyHandler;
     }
     
     public SoapHandler( Handler bodyHandler, Handler headerHandler )
     {
         this.bodyHandler = bodyHandler;
         this.headerHandler = headerHandler;
     }
     
     /**
      * Invoke the Header and Body Handlers for the SOAP message.
      */
     public void invoke(MessageContext context, XMLStreamReader reader)
             throws Exception
     {
         XMLStreamWriter writer = null;
         String encoding = null;
         
         boolean end = false;
         while ( !end && reader.hasNext() )
         {
             int event = reader.next();
             switch( event )
             {
             case XMLStreamReader.START_DOCUMENT:
                 encoding = reader.getCharacterEncodingScheme();
                 break;
             case XMLStreamReader.END_DOCUMENT:
                 end = true;
                 break;
             case XMLStreamReader.END_ELEMENT:
                 break;
             case XMLStreamReader.START_ELEMENT:
                 if( reader.getLocalName().equals("Header") && headerHandler != null )
                 {
                     writer.writeStartElement("soap", "Header", context.getSoapVersion());
                    reader.nextTag();
                     headerHandler.invoke(context, reader);
                     writer.writeEndElement();
                 }
                 else if ( reader.getLocalName().equals("Body") )
                 {
                     writer.writeStartElement("soap", "Body", context.getSoapVersion());
                     bodyHandler.invoke(context, reader);
                     writer.writeEndElement();
                 }
                 else if ( reader.getLocalName().equals("Envelope") )
                 {
                     writer = createResponseEnvelope(context, reader, encoding);
                 }
                 break;
             default:
                 break;
             }
         }
 
         writer.writeEndElement();  // Envelope
 
         writer.writeEndDocument();
         writer.close();
     }
 
     /**
      * @param context
      * @param reader
      * @throws XMLStreamException
      */
     private XMLStreamWriter createResponseEnvelope(MessageContext context, 
                                                    XMLStreamReader reader,
                                                    String encoding)
         throws XMLStreamException
     {
         XMLStreamWriter writer = getXMLStreamWriter(context);
         if ( encoding == null )
             writer.writeStartDocument("UTF-8", "1.0");
         else
             writer.writeStartDocument(encoding, "1.0");
         
         String soapVersion = reader.getNamespaceURI();
         context.setSoapVersion(soapVersion);
  
         writer.setPrefix("soap", soapVersion);
         writer.writeStartElement("soap", "Envelope", soapVersion);
         writer.writeNamespace("soap", soapVersion);
         
         return writer;
     }
 }
