 package org.codehaus.xfire.service.binding;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.exchange.InMessage;
 import org.codehaus.xfire.exchange.OutMessage;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.service.MessageInfo;
 import org.codehaus.xfire.service.MessagePartInfo;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 
 
 public class DocumentBinding
     extends AbstractBinding
 {
     public DocumentBinding()
     {
     }
 
     public void readMessage(InMessage inMessage, MessageContext context)
         throws XFireFault
     {
         Service endpoint = context.getService();
       
         Collection operations = endpoint.getServiceInfo().getOperations();
         read(inMessage, context, operations);
     }
 
     public void writeMessage(OutMessage message, XMLStreamWriter writer, MessageContext context)
         throws XFireFault
     {
         OperationInfo op = context.getExchange().getOperation();
         Object[] values = (Object[]) message.getBody();
         int i = 0;
         
         MessageInfo msgInfo = null;
         boolean client = isClientModeOn(context);
         if (client)
         {
             msgInfo = op.getInputMessage();
         }
         else
         {
             msgInfo = op.getOutputMessage();
         }
         
         for(Iterator itr = msgInfo.getMessageParts().iterator(); itr.hasNext();)
         {
             MessagePartInfo outParam = (MessagePartInfo) itr.next();
 
             try
             {
                 Object value;
                 if (client) 
                     value = getClientParam(values, outParam, context);
                 else 
                     value = getParam(values, outParam, context);
                 
                 writeParameter(writer, context, value, outParam, getBoundNamespace(context, outParam));
             }
             catch (XMLStreamException e)
             {
                 throw new XFireFault("Could not write to outgoing stream.", e, XFireFault.RECEIVER);
             }
             
             i++;
         }
     }
 }
