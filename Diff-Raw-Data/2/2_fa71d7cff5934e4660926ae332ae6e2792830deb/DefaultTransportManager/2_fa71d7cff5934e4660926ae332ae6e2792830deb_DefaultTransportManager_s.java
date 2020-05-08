 package org.codehaus.xfire.transport;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceRegistry;
 import org.codehaus.xfire.service.event.RegistrationEvent;
 import org.codehaus.xfire.service.event.RegistrationEventListener;
 import org.codehaus.xfire.soap.SoapTransport;
 import org.codehaus.xfire.transport.dead.DeadLetterTransport;
 import org.codehaus.xfire.transport.http.HttpTransport;
 import org.codehaus.xfire.transport.http.SoapHttpTransport;
 import org.codehaus.xfire.transport.local.LocalTransport;
 
 /**
  * The default <code>TransportManager</code> implementation. 
  *
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class DefaultTransportManager
         implements TransportManager, RegistrationEventListener
 {
     private static final Log log = LogFactory.getLog(DefaultTransportManager.class);
 
     private Map services = new HashMap();
     private Set transports = new HashSet();
     private Map uri2Transport = new HashMap();
     private Map binding2Transport = new HashMap();
 
     private ServiceRegistry serviceRegistry;
     
     public DefaultTransportManager()
     {        
     }
     
     /**
      * Creates a DefaultTransportManager. A LocalTransport and DeadLetterTransport 
      * are registered automatically.
      * @param xfire
      */
     public DefaultTransportManager(ServiceRegistry registry)
     {
         this.serviceRegistry = registry;
         
         initialize();
     }
 
     public ServiceRegistry getServiceRegistry()
     {
         return serviceRegistry;
     }
 
     public void setServiceRegistry(ServiceRegistry serviceRegistry)
     {
         this.serviceRegistry = serviceRegistry;
     }
 
     /**
      * Initializes transports for each service.  This also registers a LocalTransport and 
      * DeadLetterTransport.
      */
     public void initialize()
     {
         initializeTransports();
         
         register(SoapTransport.createSoapTransport(new LocalTransport()));
         register(SoapTransport.createSoapTransport(new DeadLetterTransport()));
         register(new HttpTransport());
         register(new SoapHttpTransport());
     }
 
     /**
      * @param registry
      */
     protected void initializeTransports()
     {
         for (Iterator itr = serviceRegistry.getServices().iterator(); itr.hasNext();)
         {
             Service endpoint = (Service) itr.next();
             enableAll(endpoint.getName());
         }
         serviceRegistry.addRegistrationEventListener(this);
     }
 
     public void register(Transport transport)
     {
         transports.add(transport);
 
         String[] schemes = transport.getKnownUriSchemes();
         for (int i = 0; i < schemes.length; i++)
         {
             uri2Transport.put(schemes[i], transport);
         }
         
         String[] bindingIds = transport.getSupportedBindings();
         for (int i = 0; i < bindingIds.length; i++)
         {
             binding2Transport.put(bindingIds[i], transport);
         } 
         
         for (Iterator itr = services.values().iterator(); itr.hasNext();)
         {
             Set serviceTransports = (Set) itr.next();
             serviceTransports.add(transport);
         }
 
         log.debug("Registered transport " + transport);
     }
 
     public void unregister(Transport transport)
     {
         transports.remove(transport);
 
         for (Iterator itr = services.values().iterator(); itr.hasNext();)
         {
            Map serviceTransports = (Map) itr.next();
             if (serviceTransports != null)
             {
                 serviceTransports.remove(transport);
             }
         }
     }
 
     public void enable(Transport transport, String serviceName)
     {
         Set serviceTransports = (Set) services.get(serviceName);
         if (serviceTransports == null)
         {
             serviceTransports = new HashSet();
             services.put(serviceName, serviceTransports);
         }
 
         serviceTransports.add(transport);
     }
 
     public void disable(Transport transport, String serviceName)
     {
         Set serviceTransports = (Set) services.get(serviceName);
         if (serviceTransports == null)
         {
             return;
         }
 
         serviceTransports.remove(transport);
     }
 
     /**
      * @param service
      * @return
      * @see org.codehaus.xfire.transport.TransportManager#getTransports(java.lang.String)
      */
     public Collection getTransports(String service)
     {
         Set transports = ((HashSet) services.get(service));
 
         if (transports != null)
             return transports;
         else
             return null;
     }
 
     public Collection getTransports()
     {
         return transports;
     }
 
     /**
      * @param serviceName
      */
     public void enableAll(String serviceName)
     {
         Set serviceTransports = (Set) services.get(serviceName);
         if (serviceTransports == null)
         {
             serviceTransports = new HashSet();
             services.put(serviceName, serviceTransports);
         }
 
         for (Iterator itr = transports.iterator(); itr.hasNext();)
         {
             Transport t = (Transport) itr.next();
 
             serviceTransports.add(t);
         }
     }
 
     /**
      * @param serviceName
      */
     public void disableAll(String serviceName)
     {
         Set serviceTransports = (HashSet) services.get(serviceName);
         if (serviceTransports == null)
         {
             return;
         }
 
         for (Iterator itr = transports.iterator(); itr.hasNext();)
         {
             Transport t = (Transport) itr.next();
 
             serviceTransports.remove(t);
         }
     }
 
     /**
      * @param service
      * @param transportName
      * @return
      */
     public boolean isEnabled(Transport transport, String service)
     {
         Set serviceTransports = (HashSet) services.get(service);
         if (serviceTransports == null)
         {
             return false;
         }
 
         if (serviceTransports.contains(transport))
         {
             return true;
         }
 
         return false;
     }
 
     /**
      * Notifies this <code>RegistrationEventListener</code> that the <code>ServiceEndpointRegistry</code> has registered
      * an endpoint.
      *
      * @param event an event object describing the source of the event
      */
     public void endpointRegistered(RegistrationEvent event)
     {
         enableAll(event.getEndpoint().getName());
     }
 
     /**
      * Notifies this <code>RegistrationEventListener</code> that the <code>ServiceEndpointRegistry</code> has
      * deregistered an endpoint.
      *
      * @param event an event object describing the source of the event
      */
     public void endpointUnregistered(RegistrationEvent event)
     {
         disableAll(event.getEndpoint().getName());
     }
 
     public Transport getTransportForUri(String uri)
     {
         for (Iterator itr = uri2Transport.entrySet().iterator(); itr.hasNext();)
         {
             Map.Entry entry = (Map.Entry) itr.next();
             
             if (uri.startsWith((String) entry.getKey()))
             {
                 return (Transport) entry.getValue();
             }
         }
         
         return null;
     }
     
     public Transport getTransport(String id)
     {
         return (Transport) binding2Transport.get(id);
     }
 }
