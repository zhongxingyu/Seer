 package org.codehaus.xfire.transport;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.exchange.InExchange;
 import org.codehaus.xfire.exchange.InMessage;
 import org.codehaus.xfire.exchange.OutMessage;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.handler.HandlerPipeline;
 import org.codehaus.xfire.service.Service;
 
 /**
  * A <code>ChannelEndpoint</code> which executes the in pipeline
  * on the service and starts a <code>MessageExchange</code>.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class DefaultEndpoint
     implements ChannelEndpoint
 {
     private static final Log log = LogFactory.getLog(DefaultEndpoint.class);
     public static final String SERVICE_HANDLERS_REGISTERED = "service.handlers.registered";
 
     public DefaultEndpoint()
     {
     }
     
     public void onReceive(MessageContext context, InMessage msg)
     {
         if (log.isDebugEnabled()) log.debug("Received message to " + msg.getUri());
         
         if (context.getExchange() == null)
         {
             InExchange exchange = new InExchange(context);
             exchange.setInMessage(msg);
         }
         
         // Create the handlerpipeline and invoke it
         HandlerPipeline pipeline = new HandlerPipeline(context.getXFire().getInPhases());
         pipeline.addHandlers(context.getXFire().getInHandlers());
         pipeline.addHandlers(msg.getChannel().getTransport().getInHandlers());
 
         if (context.getService() != null)
         {
             pipeline.addHandlers(context.getService().getInHandlers());
             context.setProperty(SERVICE_HANDLERS_REGISTERED, Boolean.TRUE);
         }
         
         context.setInPipeline(pipeline);
         
         try
         {
             pipeline.invoke(context);
             
             finishReadingMessage(msg, context);
         }
         catch (Exception e)
         {
             log.debug("Fault occurred!", e);
             XFireFault fault = XFireFault.createFault(e);
 
             // Give the previously invoked pipeline a chance to clean up.
             pipeline.handleFault(fault, context);
             
             Service service = context.getService();
            if (service == null || service.getFaultSerializer() == null)
             {
                 sendToDeadLetter(fault, context);
             }
             else
             {
                 sendFault(fault, context);
             }
         }
     }
 
     protected void sendToDeadLetter(XFireFault fault, MessageContext context)
     {
         log.error("Could not find service.", fault);
     }
 
     protected void sendFault(XFireFault fault, MessageContext context)
     {
         // Create the outgoing fault message
         OutMessage outMsg = (OutMessage) context.getExchange().getFaultMessage();
         
         outMsg.setSerializer(context.getService().getFaultSerializer());
         outMsg.setBody(fault);
         
         // Create a fault pipeline
         HandlerPipeline faultPipe = new HandlerPipeline(context.getXFire().getFaultPhases());
         
         faultPipe.addHandlers(context.getXFire().getFaultHandlers());
         
         Channel faultChannel = context.getExchange().getFaultMessage().getChannel();
         if (faultChannel != null)
         {
             faultPipe.addHandlers(faultChannel.getTransport().getFaultHandlers());
         }
 
         if (context.getService() != null)
         {
             faultPipe.addHandlers(context.getService().getFaultHandlers());
         }
         
         try
         {
             faultPipe.invoke(context);
         }
         catch (Exception e1)
         {
             // An exception occurred while sending the fault. Log and move on.
             XFireFault fault2 = XFireFault.createFault(e1);
             faultPipe.handleFault(fault2, context);
             
             log.error("Could not send fault.", e1);
         }
     }
 
     public void finishReadingMessage(InMessage message, MessageContext context)
         throws XFireFault
     {
         XMLStreamReader reader = message.getXMLStreamReader();
 
         try
         {
             while (reader.hasNext()) reader.next();
         }
         catch (XMLStreamException e)
         {
             throw new XFireFault("Couldn't parse message.", e, XFireFault.SENDER);
         }
     }
 }
