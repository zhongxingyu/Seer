 package org.codehaus.xfire.soap.handler;
 
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.exchange.InMessage;
 import org.codehaus.xfire.exchange.MessageSerializer;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.handler.AbstractHandler;
 import org.codehaus.xfire.handler.Phase;
 import org.codehaus.xfire.util.jdom.StaxBuilder;
 import org.codehaus.xfire.util.stax.FragmentStreamReader;
 import org.jdom.Element;
 
 public class ReadHeadersHandler
     extends AbstractHandler
 {
     public static final String DECLARED_NAMESPACES = "declared.namespaces";
 
     public String getPhase()
     {
         return Phase.PARSE;
     }
 
     public void invoke(MessageContext context)
         throws Exception
     {
         InMessage message = context.getInMessage();
         if (message.hasHeader()) return;
         
         XMLStreamReader reader = message.getXMLStreamReader();
 
         Map namespaces = new HashMap();
         context.setProperty(DECLARED_NAMESPACES, namespaces);
         
         boolean end = !reader.hasNext();
         while (!end && reader.hasNext())
         {
             int event = reader.next();
             switch (event)
             {
                 case XMLStreamReader.START_DOCUMENT:
                     String encoding = reader.getCharacterEncodingScheme();
                     message.setEncoding(encoding);
                     break;
                 case XMLStreamReader.END_DOCUMENT:
                    end = true;
                     return;
                 case XMLStreamReader.END_ELEMENT:
                     break;
                 case XMLStreamReader.START_ELEMENT:
                     if (reader.getLocalName().equals("Header"))
                     {
                         readHeaders(context);
                     }
                     else if (reader.getLocalName().equals("Body"))
                     {
                         readNamespaces(reader, namespaces);
                         
                         seekToWhitespaceEnd(reader);
 
                         checkForFault(context, message, reader);
 
                         return;
                     }
                     else if (reader.getLocalName().equals("Envelope"))
                     {
                         readNamespaces(reader, namespaces);
                         
                         message.setSoapVersion(reader.getNamespaceURI());
                     }
                     break;
                 default:
                     break;
             }
         }
     }
 
     private void readNamespaces(XMLStreamReader reader, Map namespaces)
     {
         for (int i = 0; i < reader.getNamespaceCount(); i++)
         {
             namespaces.put(reader.getNamespacePrefix(i),
                            reader.getNamespaceURI(i));
         }
     }
 
     protected void checkForFault(MessageContext context, InMessage msg, XMLStreamReader reader) 
         throws XFireFault
     {
         if (reader.getEventType() == XMLStreamReader.START_ELEMENT)
         {
             if (reader.getName().equals(msg.getSoapVersion().getFault()))
             {
                 MessageSerializer serializer = context.getService().getFaultSerializer();
                 
                 serializer.readMessage(msg, context);
                 
                 throw (XFireFault) msg.getBody();
             }
         }
     }
 
     private void seekToWhitespaceEnd(XMLStreamReader reader)
         throws XMLStreamException
     {
         int event = reader.next();
         if (event != XMLStreamReader.SPACE) return;
         
         do
         {
             event = reader.next();
         }
         while (event == XMLStreamReader.SPACE);
        
        return;
     }
 
     /**
      * Read in the headers as a YOM Element and create a response Header.
      *
      * @param context
      * @throws XMLStreamException
      */
     protected void readHeaders(MessageContext context)
             throws XMLStreamException
     {
         StaxBuilder builder = new StaxBuilder();
 
         InMessage msg = context.getInMessage();
        Element header = builder.build(new FragmentStreamReader(msg.getXMLStreamReader())).getRootElement();
 
        context.getInMessage().setHeader(header);
     }
 }
