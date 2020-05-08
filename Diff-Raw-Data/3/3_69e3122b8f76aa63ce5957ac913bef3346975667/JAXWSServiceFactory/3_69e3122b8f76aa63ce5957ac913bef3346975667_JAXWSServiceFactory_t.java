 package org.codehaus.xfire.jaxws;
 
 import java.lang.reflect.Method;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.RequestWrapper;
 import javax.xml.ws.ResponseWrapper;
 import javax.xml.ws.WebFault;
 
 import org.codehaus.xfire.aegis.AegisBindingProvider;
 import org.codehaus.xfire.annotations.AnnotationServiceFactory;
 import org.codehaus.xfire.annotations.jsr181.Jsr181WebAnnotations;
 import org.codehaus.xfire.fault.FaultSender;
 import org.codehaus.xfire.handler.OutMessageSender;
 import org.codehaus.xfire.jaxws.handler.WebFaultHandler;
 import org.codehaus.xfire.jaxws.type.JAXWSTypeRegistry;
 import org.codehaus.xfire.service.FaultInfo;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.binding.ServiceInvocationHandler;
 import org.codehaus.xfire.soap.AbstractSoapBinding;
 import org.codehaus.xfire.transport.TransportManager;
 
 /**
  * Creates JAX-WS services from a service class.
  * 
  * @author Dan Diephouse
  */
 public class JAXWSServiceFactory
     extends AnnotationServiceFactory
 {
     public JAXWSServiceFactory(TransportManager transportManager)
     {
         super(new Jsr181WebAnnotations(), 
               transportManager, 
               new AegisBindingProvider(new JAXWSTypeRegistry()));
     }
     
     protected void registerHandlers(Service service)
     {
         service.addInHandler(new ServiceInvocationHandler());
         service.addOutHandler(new OutMessageSender());
         service.addFaultHandler(new FaultSender());
         service.addFaultHandler(new WebFaultHandler());
     }
 
     @Override
     protected OperationInfo addOperation(Service endpoint, Method method, String style)
     {
         OperationInfo op = super.addOperation(endpoint, method, style);
         
         return op;
     }
 
     @Override
     protected FaultInfo addFault(Service service, OperationInfo op, Class exClazz)
     {
         FaultInfo info = super.addFault(service, op, exClazz);
         
         return info;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected boolean isFaultInfoClass(Class exClass) {
         return exClass.isAnnotationPresent(WebFault.class);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected QName getFaultName(Service service, OperationInfo o, Class exClass, Class beanClass)
     {
         WebFault webFault = (WebFault) exClass.getAnnotation(WebFault.class);
         
        if (webFault == null)
            return super.getFaultName(service, o, exClass, beanClass);
        
         String ns = webFault.targetNamespace();
         if (ns == null) ns = service.getTargetNamespace();
         
         return new QName(ns, webFault.name());
     }
 
     /**
      * Creates an AbstractSoapBinding using the JAXWSBinding as the serializer.
      */
     @Override
     protected void createSoapBinding(Service service, AbstractSoapBinding binding)
     {
         super.createSoapBinding(service, binding);
         
         binding.setSerializer(new JAXWSBinding(binding.getSerializer()));
     }
 
     @Override
     public void createBindingOperation(Service service, AbstractSoapBinding binding, OperationInfo op)
     {
         super.createBindingOperation(service, binding, op);
 
         binding.setSerializer(op, new JAXWSOperationBinding(op, binding.getSerializer()));
     }
 
     @Override
     protected QName createInputMessageName(OperationInfo op)
     {
         if (op.getMethod().isAnnotationPresent(RequestWrapper.class))
         {
             RequestWrapper wrapper = op.getMethod().getAnnotation(RequestWrapper.class);
             
             String ns = wrapper.targetNamespace();
             if (ns.length() == 0) ns = op.getService().getPortType().getNamespaceURI();
 
             String name = wrapper.localName();
             if (name.length() == 0) name = op.getName();
             
             return new QName(ns, name);
         }
         
         return super.createInputMessageName(op);
     }
 
     @Override
     protected QName createOutputMessageName(OperationInfo op)
     {
         if (op.getMethod().isAnnotationPresent(ResponseWrapper.class))
         {
             ResponseWrapper wrapper = op.getMethod().getAnnotation(ResponseWrapper.class);
             
             String ns = wrapper.targetNamespace();
             if (ns.length() == 0) ns = op.getService().getPortType().getNamespaceURI();
 
             String name = wrapper.localName();
             if (name.length() == 0) name = op.getName();
             
             return new QName(ns, name);
         }
         
         return super.createOutputMessageName(op);
     }
 }
