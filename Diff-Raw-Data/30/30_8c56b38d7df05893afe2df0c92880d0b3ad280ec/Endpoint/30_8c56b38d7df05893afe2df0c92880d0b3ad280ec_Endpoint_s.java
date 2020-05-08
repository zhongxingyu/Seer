 package org.codehaus.xfire.jaxws;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executor;
 
 import javax.xml.transform.Source;
 import javax.xml.ws.Binding;
 import javax.xml.ws.BindingType;
 import javax.xml.ws.soap.SOAPBinding;
 
 import org.codehaus.xfire.jaxws.binding.AbstractBinding;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.binding.BeanInvoker;
 import org.codehaus.xfire.transport.TransportManager;
 
 public class Endpoint
     extends javax.xml.ws.Endpoint
 {
     private JAXWSHelper jaxWsHelper = JAXWSHelper.getInstance();
     private TransportManager transportManager = jaxWsHelper.getTransportManager();
 
     private AbstractBinding binding;
     private Object implementor;
     private boolean published;
     private List<Source> metadata;
     private Executor executor;
     private Service service;
     
     public Endpoint(String bindingId, Object implementor)
     {
         // Try the BindingType annotation
         if (bindingId == null)
         {
             BindingType type = implementor.getClass().getAnnotation(BindingType.class);
             if (type != null)
             {
                 bindingId = type.value();
             }
         }
        
         // We now must use the SOAP 1.1 HTTP binding
        if (bindingId == null)
         {
             bindingId = SOAPBinding.SOAP11HTTP_BINDING;
         }
         
         this.binding = jaxWsHelper.getBinding(bindingId);
         this.implementor = implementor;
         
         this.service = jaxWsHelper.getServiceFactory().create(implementor.getClass());
         this.service.setInvoker(new BeanInvoker(implementor));
         
         transportManager = jaxWsHelper.getXFire().getTransportManager();
         transportManager.disableAll(service);
     }
 
     @Override
     public Binding getBinding()
     {
         return binding;
     }
 
     @Override
     public Object getImplementor()
     {
         return implementor;
     }
 
     @Override
     public void publish(String address)
     {
         if (published)
             throw new IllegalStateException("Endpoint has already been published.");
         
         transportManager.enable(binding.getTransport(), service);
         
         published = true;
     }
 
     @Override
     public void publish(Object context)
     {
         if (published)
             throw new IllegalStateException("Endpoint has already been published.");
         
         published = true;
     }
 
     @Override
     public void stop()
     {
         if (published)
         {
             transportManager.disable(binding.getTransport(), service);
             
             published = false;
         }
     }
 
     @Override
     public boolean isPublished()
     {
         return published;
     }
 
     @Override
     public List<Source> getMetadata()
     {
         return metadata;
     }
 
     @Override
     public void setMetadata(List<Source> metadata)
     {
         this.metadata = metadata;
     }
 
     @Override
     public Executor getExecutor()
     {
         return executor;
     }
 
     @Override
     public void setExecutor(Executor executor)
     {
         this.executor = executor;
     }
 
     @Override
     public Map<String, Object> getProperties()
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void setProperties(Map<String, Object> arg0)
     {
         // TODO Auto-generated method stub
         
     }
 }
