 package org.codehaus.xfire.handler;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Iterator;
 
 import javax.xml.stream.XMLStreamException;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.fault.FaultInfoException;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.service.FaultInfo;
 import org.codehaus.xfire.service.MessagePartInfo;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.binding.AbstractBinding;
 import org.codehaus.xfire.util.stax.ElementStreamWriter;
 
 /**
  * Builds up a custom detail element from an exception.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class CustomFaultHandler
     extends AbstractHandler
 {    
     
     public CustomFaultHandler() {
         super();
         setPhase(Phase.USER);
     }
 
     public void invoke(MessageContext context)
         throws Exception
     {
         XFireFault fault = (XFireFault) context.getExchange().getFaultMessage().getBody();
         
         Throwable cause = fault.getCause();
         OperationInfo op = null;
         
         if (context.getExchange() != null) 
             op = context.getExchange().getOperation();
 
         if (cause == null || op == null)
             return;
 
         FaultInfo faultPart = getFaultForClass(op, cause.getClass());
 
         if (faultPart != null)
         {
             handleFault(context, fault, cause, (MessagePartInfo) faultPart.getMessageParts().get(0));
         }
     }
 
     protected void handleFault(MessageContext context, XFireFault fault, Throwable cause, MessagePartInfo faultPart)
         throws XFireFault
     {
         ElementStreamWriter writer = new ElementStreamWriter(fault.getDetail());
         
         Object faultBean = getFaultBean(cause, faultPart, context);
         
         try
         {
             AbstractBinding.writeParameter(writer, context, faultBean, faultPart, faultPart.getName().getNamespaceURI());
         }
         catch (XMLStreamException e)
         {
             throw new XFireFault("Could not write to outgoing stream.", e, XFireFault.RECEIVER);
         }
     }
 
     protected Object getFaultBean(Throwable cause, MessagePartInfo faultPart, MessageContext context) 
     {
         if (FaultInfoException.class.isAssignableFrom(cause.getClass()))
         {
             try
             {
                 Method method = cause.getClass().getMethod("getFaultInfo", new Class[0]);
                 return method.invoke(cause, new Object[0]);
             }
             catch (InvocationTargetException e)
             {
                 throw new XFireRuntimeException("Couldn't invoke getFaultInfo method.", e);
             }
             catch (NoSuchMethodException e)
             {
                 throw new XFireRuntimeException("Custom faults need a getFaultInfo method.", e);
             }
             catch (Exception e)
             {
                 throw new XFireRuntimeException("Couldn't access getFaultInfo method.", e);
             }
         }
         return cause;
     }
 
     /**
      * Find the correct Fault part for a particular exception.
      * @param op
      * @param class1
      * @return
      */
     public FaultInfo getFaultForClass(OperationInfo op, Class class1)
     {
         for (Iterator itr = op.getFaults().iterator(); itr.hasNext();)
         {
             FaultInfo faultInfo = (FaultInfo) itr.next();
             
            if (faultInfo.getExceptionClass().isAssignableFrom(class1))
             {
                 return faultInfo;
             }
         }
         
         return null;
     }
 }
