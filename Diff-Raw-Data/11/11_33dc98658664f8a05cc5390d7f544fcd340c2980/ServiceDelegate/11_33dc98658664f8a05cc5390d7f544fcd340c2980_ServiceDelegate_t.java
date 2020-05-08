 package org.codehaus.xfire.jaxws;
 
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executor;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.namespace.QName;
 import javax.xml.transform.Source;
 import javax.xml.ws.Dispatch;
 import javax.xml.ws.WebServiceException;
 import javax.xml.ws.Service.Mode;
 import javax.xml.ws.handler.HandlerResolver;
 
 import org.codehaus.xfire.client.Client;
 import org.codehaus.xfire.client.XFireProxyFactory;
 import org.codehaus.xfire.jaxws.handler.SimpleHandlerResolver;
 import org.codehaus.xfire.service.Endpoint;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceFactory;
 import org.codehaus.xfire.transport.Transport;
 
 public class ServiceDelegate
     extends javax.xml.ws.spi.ServiceDelegate
 {
     private JAXWSHelper jaxWsHelper = JAXWSHelper.getInstance();
     private XFireProxyFactory factory = jaxWsHelper.getProxyFactory();
     private ServiceFactory serviceFactory = jaxWsHelper.getServiceFactory();
 
     private URL wsdlLocation;
     private Executor executor;
     private HandlerResolver handlerResolver;
     private QName serviceName;
     
     private Map<QName, Service> port2Service = new HashMap<QName, Service>();
     private Map<Class, Service> intf2service = new HashMap<Class, Service>();
     private Map<QName, PortInfo> port2info = new HashMap<QName, PortInfo>();
     
     public ServiceDelegate()
     {
         handlerResolver = new SimpleHandlerResolver();
     }
     
     public ServiceDelegate(URL wsdlLocation, QName serviceName, Class clientClass)
     {
         this();
         
         this.wsdlLocation = wsdlLocation;
         this.serviceName = serviceName;
         
         try
         {
             Method method = clientClass.getMethod("getPortClassMap", new Class[0]);
             
             Map port2Class = (Map) method.invoke(null, new Object[0]);
             for (Iterator itr = port2Class.entrySet().iterator(); itr.hasNext();)
             {
                 Map.Entry entry = (Map.Entry) itr.next();
                 
                 QName port = (QName) entry.getKey();
                 Class clazz = (Class) entry.getValue();
 
                 Service service = getService(clazz);
                 port2Service.put(port, service);
                 intf2service.put(clazz, service);
             }
         }
         catch (Exception e)
         {
            throw new IllegalStateException("Could not initialize Service.", e);
         }
     }
     
     private Service getService(Class clazz)
     {
         Service service = intf2service.get(clazz);
         if (service == null)
         {
             service = serviceFactory.create(clazz, serviceName, wsdlLocation, null);
             
             intf2service.put(clazz, service);
         }
         
         return service;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> T getPort(QName portName, Class<T> clazz)
     {
         Endpoint endpoint = getService(clazz).getEndpoint(portName);
         
         if (endpoint == null) throw new WebServiceException("Invalid port name " + portName);
         
         return (T) createPort(endpoint);
     }
 
     private Object createPort(Endpoint endpoint)
     {
         try
         {
             return factory.create(endpoint);
         }
         catch (MalformedURLException e)
         {
             throw new WebServiceException("Invalid url: " + endpoint.getUrl(), e);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> T getPort(Class<T> clazz)
     {
         if (getService(clazz).getEndpoints().size() == 0)
         {
             throw new WebServiceException("No available ports.");
         }
         
         return (T) createPort((Endpoint) getService(clazz).getEndpoints().iterator().next());
     }
 
     @Override
     public void addPort(QName portName, String bindingUri, String address)
     {
         PortInfo portInfo = new PortInfo(bindingUri, address);
         
         port2info.put(portName, portInfo);
     }
 
 
     @SuppressWarnings("unchecked")
     @Override
     public <T> javax.xml.ws.Dispatch<T> createDispatch(QName port, Class<T> type, Mode serviceMode)
     {   
         Transport transport;
         String address;
         
         PortInfo portInfo = getPortInfo(port);
         if (portInfo != null)
         {
             String bindingUri = portInfo.getBindingUri();
             transport = jaxWsHelper.getBinding(bindingUri).getTransport();
             address = portInfo.getAddress();
         }
         else
         {
             Service service = port2Service.get(port);
             
             if (service == null)
             {
                 throw new IllegalStateException("Could not find port " + port);
             }
             
             Endpoint ep = service.getEndpoint(port);
             transport = jaxWsHelper.getTransportManager().getTransport(ep.getBinding().getBindingId());
             address = ep.getUrl();
         }
         
         Client client = new Client(transport,
                                    jaxWsHelper.getSourceService(), 
                                    address);
         
         if (type == Source.class)
         {
             SourceDispatch dispatch = new SourceDispatch(client);
             dispatch.setMode(serviceMode);
             return (Dispatch<T>) dispatch;
         }
         else
         {
             throw new WebServiceException("Unknown dispatch type: " + type.getName());
         }
     }
 
     private PortInfo getPortInfo(QName port)
     {
         return port2info.get(port);
     }
 
     @Override
     public javax.xml.ws.Dispatch<Object> createDispatch(QName arg0, JAXBContext arg1, Mode arg2)
     {
         return null;
     }
 
     @Override
     public QName getServiceName()
     {
         return serviceName;
     }
 
     @Override
     public Iterator<QName> getPorts()
     {
         List<QName> ports = new ArrayList<QName>();
         
         ports.addAll(port2info.keySet());
         ports.addAll(port2Service.keySet());
         
         return ports.iterator();
     }
 
     @Override
     public URL getWSDLDocumentLocation()
     {
         return wsdlLocation;
     }
 
     @Override
     public HandlerResolver getHandlerResolver()
     {
         return handlerResolver;
     }
 
     @Override
     public void setHandlerResolver(HandlerResolver handlerResolver)
     {
         this.handlerResolver = handlerResolver;
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
     
     static class PortInfo 
     {
         private String bindingUri;
         private String address;
         
         public PortInfo(String bindingUri, String address2)
         {
             this.bindingUri = bindingUri;
             this.address = address2;
         }
         public String getAddress()
         {
             return address;
         }
         public void setAddress(String address)
         {
             this.address = address;
         }
         public String getBindingUri()
         {
             return bindingUri;
         }
         public void setBindingUri(String bindingUri)
         {
             this.bindingUri = bindingUri;
         }
     }
 }
