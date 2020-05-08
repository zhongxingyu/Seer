 package org.codehaus.xfire.util.dom;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.exchange.OutMessage;
 import org.codehaus.xfire.handler.AbstractHandler;
 import org.codehaus.xfire.handler.Phase;
 import org.codehaus.xfire.soap.handler.SoapSerializerHandler;
 import org.codehaus.xfire.util.stax.W3CDOMStreamWriter;
 import org.w3c.dom.Document;
 
 /**
  * Creates a org.w3c.dom.Document for the outgoing message and sets the
  * outgoing message's serializer to a DOMSerializer.
  * <p>
  * To access the DOM simply do:
  * <pre>
  * OutMessage msg = context.getOutMessage();
  * Document doc = (Document) msg.getProperty(DOMOutHandler.DOM_MESSAGE);
  * </pre>
  */
 public class DOMOutHandler
     extends AbstractHandler
 {
     public static final String DOM_MESSAGE = "dom.message";
     private DocumentBuilder builder;
     
     public DOMOutHandler()
     {
         super();
         setPhase(Phase.POST_INVOKE);
         after(SoapSerializerHandler.class.getName());
         before(FaultSoapSerializerHandler.class.getName());
         
         try
         {
             builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         }
         catch (ParserConfigurationException e)
         {
             throw new XFireRuntimeException("Couldn't create DocumentBuilder.", e);
         }
     }
 
     public void invoke(MessageContext context)
         throws Exception
     {
         OutMessage message = (OutMessage) context.getCurrentMessage();
         W3CDOMStreamWriter writer = new W3CDOMStreamWriter(builder);
         message.getSerializer().writeMessage(message, writer, context);
        
         Document doc = writer.getDocument();
         message.setProperty(DOM_MESSAGE, doc);
         message.setSerializer(new DOMSerializer());
     }
 }
