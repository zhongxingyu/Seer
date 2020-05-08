 package org.codehaus.xfire.jaxws;
 
 import java.net.URL;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.Endpoint;
 import javax.xml.ws.spi.ServiceDelegate;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.transport.Transport;
 import org.codehaus.xfire.transport.TransportManager;
 
 public class Provider
     extends javax.xml.ws.spi.Provider
 {
     private static final Log log = LogFactory.getLog(Provider.class);
 
     public Provider()
     {
     }
     
     @Override
     public ServiceDelegate createServiceDelegate(URL wsdlLocation,
                                                  QName serviceName,
                                                  Class serviceClass)
     {
         log.debug("Creating service delegate " + serviceName + " for service class " +
                   serviceClass.getName());
         
         return new org.codehaus.xfire.jaxws.ServiceDelegate(wsdlLocation, serviceName, serviceClass);
     }
 
     @Override
     public Endpoint createEndpoint(String bindingId, Object implementor)
     {
         org.codehaus.xfire.jaxws.Endpoint endpoint = 
             new org.codehaus.xfire.jaxws.Endpoint(bindingId, implementor);
         
         return endpoint;
     }
 
     @Override
     public Endpoint createAndPublishEndpoint(String address, Object implementor)
     {
        TransportManager transportManager = JAXWSHelper.getInstance().getTransportManager();
        Transport t = transportManager.getTransportForUri(address);
         
         org.codehaus.xfire.jaxws.Endpoint endpoint = 
            new org.codehaus.xfire.jaxws.Endpoint(t.getSupportedBindings()[0], implementor);
         endpoint.publish(address);
         return endpoint;
     }
 
 }
