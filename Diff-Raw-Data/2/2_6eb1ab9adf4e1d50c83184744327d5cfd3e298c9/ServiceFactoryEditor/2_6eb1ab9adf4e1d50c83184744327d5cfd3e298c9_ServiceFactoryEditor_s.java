 package org.codehaus.xfire.spring.editors;
 
 import java.beans.PropertyEditorSupport;
 
 import org.codehaus.xfire.service.ServiceFactory;
 import org.codehaus.xfire.spring.config.ServiceFactoryBean;
 import org.codehaus.xfire.transport.TransportManager;
 
 /**
  * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
  *
  */
 public class ServiceFactoryEditor
     extends PropertyEditorSupport
 {
     private TransportManager transportManager;
     
     public TransportManager getTransportManager()
     {
         return transportManager;
     }
 
     public void setTransportManager(TransportManager transportManager)
     {
         this.transportManager = transportManager;
     }
 
     public void setAsText(String text)
         throws IllegalArgumentException
     {
         ServiceFactoryBean factoryBean = new ServiceFactoryBean(text);
         factoryBean.setTransportManager(transportManager);
         ServiceFactory factory;
         try
         {
             factory = (ServiceFactory) factoryBean.getObject();
         }
         catch (Exception e)
         {
            throw new IllegalArgumentException(e);
         }
         setValue(factory);
     }
 
 }
