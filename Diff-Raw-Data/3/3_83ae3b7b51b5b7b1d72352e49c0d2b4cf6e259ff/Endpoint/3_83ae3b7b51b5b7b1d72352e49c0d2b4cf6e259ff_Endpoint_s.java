 package org.codehaus.xfire.jaxws;
 
 import java.util.HashMap;
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
     private AbstractBinding binding;
     private Object implementor;
     private boolean published;
     private List<Source> metadata;
     private Executor executor;
     private Service service;
     
     private Map<String,Object> properties = new HashMap<String,Object>();
     
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
         if (bindingId == null || bindingId.length() == 0)
         {
             bindingId = SOAPBinding.SOAP11HTTP_BINDING;
         }
         
         this.binding = jaxWsHelper.getBinding(bindingId);
 
         if (binding == null)
         {
             throw new IllegalStateException("Could not find binding: " + bindingId);
         }
         
         this.implementor = implementor;
         
         this.service = jaxWsHelper.getServiceFactory().create(implementor.getClass());
         this.service.setInvoker(new BeanInvoker(implementor));
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
         {
             throw new IllegalStateException("Endpoint has already been published.");
         }
         
         jaxWsHelper.getXFire().getServiceRegistry().register(service);
 
         published = true;
     }
 
     @Override
     public void publish(Object context)
     {
         if (published)
         {
             throw new IllegalStateException("Endpoint has already been published.");
         }
         
         published = true;
     }
 
     @Override
     public void stop()
     {
         if (published)
         {
             jaxWsHelper.getXFire().getServiceRegistry().unregister(service);
 
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
         return properties;
     }
 
     @Override
     public void setProperties(Map<String, Object> properties)
     {
         this.properties = properties;
     }
 }
