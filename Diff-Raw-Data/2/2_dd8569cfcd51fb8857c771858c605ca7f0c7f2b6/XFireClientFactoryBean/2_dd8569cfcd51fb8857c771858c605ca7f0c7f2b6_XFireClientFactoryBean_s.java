 package org.codehaus.xfire.spring.remoting;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.wsdl.Definition;
 import javax.wsdl.factory.WSDLFactory;
 import javax.xml.namespace.QName;
 
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.client.Client;
 import org.codehaus.xfire.client.XFireProxyFactory;
 import org.codehaus.xfire.service.Endpoint;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceFactory;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.soap.AbstractSoapBinding;
 import org.codehaus.xfire.spring.SpringUtils;
 import org.codehaus.xfire.transport.Channel;
 import org.codehaus.xfire.util.Resolver;
 import org.springframework.aop.framework.ProxyFactory;
 import org.springframework.aop.support.AopUtils;
 import org.springframework.beans.factory.FactoryBean;
 import org.springframework.beans.factory.InitializingBean;
 
 /**
  * Factory bean to easily create XFire clients via Spring, if the service's Java
  * interface is available. Naming of properties is done to be the same as
  * {@link org.springframework.remoting.jaxrpc.JaxRpcPortProxyFactoryBean}. 
  * <br>
  * The only mandatory properties to set before using this factory are:
  * {@link #setServiceClass(Class)} and {@link #setWsdlDocumentUrl(String)}.
  * <br>
  * By default this factory bean creates a service endpoint using an instance of 
  * {@link org.codehaus.xfire.service.binding.ObjectServiceFactory}. Another one can
  * be configured using {@link #setServiceFactory(ServiceFactory)}
  * <br>
  * serviceName and namespaceUri can be derived from the content of the WSDL document 
  * (if the document only contains one service), but unfortunately that does not (yet) 
  * work if username/password needs to be supplied to get at the WSDL.
  * 
  * @author Fried Hoeben
  */
 public class XFireClientFactoryBean
     implements FactoryBean, InitializingBean
 {
     private static final Log LOG = LogFactory.getLog(XFireClientFactoryBean.class);
 
     // client proxy, in case lookupServiceOnStartup == true
     // proxy to the client proxy, otherwise
     private Object _serviceProxy;
 
     private Class _serviceClass;
 
     private ServiceFactory _serviceFactory = new ObjectServiceFactory();
 
     private String _wsdlDocumentUrl;
 
     private String _serviceName;
 
     private String _namespaceUri;
 
     private String _username;
 
     private String _password;
 
     private String _url;
     
     private QName _endpointName;
     
     private Map _properties;
     
     private boolean _lookupServiceOnStartup = true;
     
     private List outHandlers = null;
 
     private List inHandlers = null;
 
     private List faultHandlers = null;
     
     public Object getObject()
         throws Exception
     {
         return _serviceProxy;
     }
 
     public Class getObjectType()
     {
         return (_serviceProxy != null) ? _serviceProxy.getClass() : getServiceClass();
     }
 
     public boolean isSingleton()
     {
         return true;
     }
 
     public void afterPropertiesSet()
         throws Exception
     {
         if (getServiceClass() == null)
         {
             throw new IllegalStateException("serviceInterface is required");
         }
         
         ProxyInterceptor interceptor;
         if (getLookupServiceOnStartup())
         {
             // create XFire client proxy directly
             _serviceProxy = createClient();
         }
         else
         {
             // create proxy for XFire client proxy, this will create the XFire
             // client proxy
             // when it is first called
             interceptor = new ProxyInterceptor();
             _serviceProxy = ProxyFactory.getProxy(getServiceClass(), interceptor);
         }
     }
 
     /**
      * @return the service factory that this factory will use
      */
     public ServiceFactory getServiceFactory()
     {
         return _serviceFactory;
     }
 
     /**
      * Sets the service factory that will be used to create a client. If this method is never
      * called an instance of {@link org.codehaus.xfire.service.binding.ObjectServiceFactory} will
      * be used.
      * 
      * @param factory
      *            service factory this factory should use to create a client
      */
     public void setServiceFactory(ServiceFactory factory)
     {
         if (factory == null)
         {
             throw new IllegalArgumentException("Can not set the service factory to null");
         }
         _serviceFactory = factory;
     }
 
     /**
      * @return Returns the service's interface.
      */
     public Class getServiceClass()
     {
         return _serviceClass;
     }
 
     /**
      * @param serviceClass
      *            The interface implemented by the service called via the proxy.
      */
     public void setServiceInterface(Class serviceClass)
     {
         _serviceClass = serviceClass;
     }
 
     /**
      * @return Returns the service's interface.
      */
     public Class getServiceInterface()
     {
         return _serviceClass;
     }
 
     /**
      * @param serviceClass
      *            The interface implemented by the service called via the proxy.
      */
     public void setServiceClass(Class serviceClass)
     {
         _serviceClass = serviceClass;
     }
     
     /**
      * @return Returns the URL where the WSDL to this service can be found.
      */
     public String getWsdlDocumentUrl()
     {
         return _wsdlDocumentUrl;
     }
 
     /**
      * @param wsdlUrl
      *            The URL where the WSDL to this service can be found.
      */
     public void setWsdlDocumentUrl(String wsdlUrl)
     {
         _wsdlDocumentUrl = wsdlUrl.trim();
     }
 
     /**
      * Gets the name of the service. If <code>null</code> the name will be
      * looked up from the WSDL, or generated from the interface name by XFire.
      * 
      * @return Returns the serviceName.
      */
     public String getServiceName()
     {
         return _serviceName;
     }
 
     /**
      * Sets the name of the service to access. If left <code>null</code> the
      * name will be looked up from the WSDL, or generated from the interface
      * name by XFire.
      * 
      * @param serviceName
      *            The service name to set.
      */
     public void setServiceName(String serviceName)
     {
         _serviceName = serviceName;
     }
 
     /**
      * Gets the default namespace for the service. If <code>null</code> the
      * namespace will be looked up from the WSDL, or generated from the
      * interface package by XFire.
      * 
      * @return Returns the namespace for the service.
      */
     public String getNamespaceUri()
     {
         return _namespaceUri;
     }
 
     /**
      * Sets the default namespace for the service. If left <code>null</code>
      * the namespace will be looked up from the WSDL, or generated from the
      * interface package by XFire.
      * 
      * @param namespace
      *            The namespace to set.
      */
     public void setNamespaceUri(String namespace)
     {
         _namespaceUri = namespace;
     }
 
     /**
      * Gets whether to look up the XFire service on startup.
      * 
      * @return whether to look up the service on startup.
      */
     public boolean getLookupServiceOnStartup()
     {
         return _lookupServiceOnStartup;
     }
 
     /**
      * Sets whether to look up the XFire service on startup. Default is
      * <code>true</code>.
      * <p>
      * Can be set to <code>false</code> to allow for late start of the target
      * server. In this case, the XFire service client proxy will be created on
      * first access. This does add some overhead (on each call) since
      * synchronization is used to ensure only one client proxy is ever created,
      * furthermore errors in the WSDL document URL are not detected until the
      * first call.
      * 
      * @param lookupServiceOnStartup
      *            whether to look up the service on startup.
      */
     public void setLookupServiceOnStartup(boolean lookupServiceOnStartup)
     {
         _lookupServiceOnStartup = lookupServiceOnStartup;
     }
 
     /**
      * Gets the username for HTTP basic authentication.
      * 
      * @return Returns the username to send.
      */
     public String getUsername()
     {
         return _username;
     }
 
     /**
      * Sets the username for HTTP basic authentication.
      * 
      * @param username
      *            The username to set.
      */
     public void setUsername(String username)
     {
         _username = username;
     }
 
     /**
      * Gets the password for HTTP basic authentication.
      * 
      * @return Returns the password to send.
      */
     public String getPassword()
     {
         return _password;
     }
 
     /**
      * Sets the password for HTTP basic authentication.
      * 
      * @param password
      *            The password to set.
      */
     public void setPassword(String password)
     {
         _password = password;
     }
     
     /**
      * The properties that will be set on the Client.
      */
     public Map getProperties()
     {
         return _properties;
     }
 
     /**
      * Set the properties for the Client.
      */
     public void setProperties(Map properties)
     {
         this._properties = properties;
     }
 
     public QName getEndpoint()
     {
         return _endpointName;
     }
 
     /**
      * Set the name of the Endpoint/Port in the WSDL to use with the Client.
      * 
      * @param name
      */
     public void setEndpoint(QName name)
     {
         _endpointName = name;
     }
 
     public String getUrl()
     {
         return _url;
     }
 
     /**
      * Set the URL the Client is to invoke. If this is not supplied, the one from the
      * WSDL will be used instead.
      * @return
      */
     public void setUrl(String _url)
     {
         this._url = _url;
     }
 
     public List getFaultHandlers()
     {
         return faultHandlers;
     }
 
     public void setFaultHandlers(List faultHandlers)
     {
         this.faultHandlers = faultHandlers;
     }
 
     public List getInHandlers()
     {
         return inHandlers;
     }
 
     public void setInHandlers(List inHandlers)
     {
         this.inHandlers = inHandlers;
     }
 
     public List getOutHandlers()
     {
         return outHandlers;
     }
 
     public void setOutHandlers(List outHandlers)
     {
         this.outHandlers = outHandlers;
     }
 
     /**
      * Creates actual XFire client proxy that this interceptor will delegate to.
      * 
      * @throws Exception
      *             if the client proxy could not be created.
      */
     private Object createClient()
         throws Exception
     {
         Object serviceClient = makeClient();
 
         Class interf = getServiceInterface();
         if (LOG.isDebugEnabled())
         {
             LOG.debug("Created: " + toString());
         }
 
         Client client = Client.getInstance(serviceClient);
         
         String username = getUsername();
         if (username != null)
         {
             client.setProperty(Channel.USERNAME, username);
 
             String password = getPassword();
             client.setProperty(Channel.PASSWORD, password);
 
             if (LOG.isDebugEnabled())
             {
                 LOG.debug("Enabled HTTP basic authentication for: " + interf + " with username: "
                         + username);
             }
         }
         
         configureClientHandlers(client);
         
         return serviceClient;
     }
     
     /**
      * Configures the client with the specified inHandlers, outHandlers and
      * faultHandlers.
      * 
      * @param client
      */
     private void configureClientHandlers(Client client)
     {
         if (this.inHandlers != null)
         {
             client.getInHandlers().addAll(inHandlers);
         }
 
         if (this.outHandlers != null)
         {
             client.getOutHandlers().addAll(outHandlers);
         }
 
         if (this.faultHandlers != null)
         {
             client.getFaultHandlers().addAll(faultHandlers);
         }
     }
     
     /**
      * Performs actual creation of XFire client proxy.
      * 
      * @return XFire proxy to the service
      * @throws java.net.MalformedURLException
      *             if {@link XFireProxyFactory#create} threw one
      */
     private Object makeClient()
         throws Exception
     {
         String serviceName = getServiceName();
         String namespace = getNamespaceUri();
 
         Service serviceModel;
         if (_wsdlDocumentUrl == null)
         {
             serviceModel = getServiceFactory().create(getServiceInterface(),
                                                       serviceName,
                                                       namespace,
                                                       _properties);
             
         }
         else
         {
             QName name = null;
             if (serviceName != null && namespace != null)
             {
                 name = new QName(namespace, serviceName);
             }
             
             Resolver resolver = new Resolver(_wsdlDocumentUrl);
 
             URI uri = resolver.getURI();
             if (uri == null)
             {
                 throw new XFireRuntimeException("Could not resolve uri " + uri);
             }
             
             serviceModel = getServiceFactory().create(getServiceInterface(),
                                                       name,
                                                      uri.toURL(),
                                                       _properties);
         }
 
         String serviceUrl = getUrl();
         if (serviceUrl != null) 
         {
             return new XFireProxyFactory().create(serviceModel, serviceUrl);
         }
         
         if (_endpointName == null)
         {
             _endpointName = findFirstSoapEndpoint(serviceModel.getEndpoints());
         }
         
         if (_endpointName != null)
         {
             Endpoint ep = serviceModel.getEndpoint(_endpointName);
             if (ep == null)
                 throw new IllegalStateException("Could not find endpoint with name " + _endpointName + " on service.");
             
             return new XFireProxyFactory().create(ep);
         }
         else
             throw new IllegalStateException("A WSDL URL or service URL must be supplied.");
     }
 
     private QName findFirstSoapEndpoint(Collection endpoints)
     {
         for (Iterator itr = endpoints.iterator(); itr.hasNext();)
         {
             Endpoint ep = (Endpoint) itr.next();
             
             if (ep.getBinding() instanceof AbstractSoapBinding)
                 return ep.getName();
         }
         return null;
     }
 
     /**
      * Gets the Definition contained in the WSDL document (does not currently support reading
      * WSDL that is protected with authentication).
      * @return Definition describing the service(s)
      * @throws Exception if the definition could not be read
      */
     protected Definition getWSDLDefinition()
         throws Exception
     {
         return WSDLFactory.newInstance().newWSDLReader().readWSDL(getWsdlDocumentUrl());
     }
 
     public String toString()
     {
         StringBuffer builder = new StringBuffer();
         builder.append("XFire client proxy for: ");
         builder.append(getServiceInterface());
         if (getUrl() != null)
         {
             builder.append(" at: ");
             builder.append(getUrl());
         }
         
         return builder.toString();
     }
 
     /**
      * Interceptor for (i.e. proxy to) the actual XFire client proxy. This class
      * performs lazy initialization of the XFire client proxy, which can come in
      * handy if the service is not guaranteed to be available by the time the
      * factory bean is used to created an instance, but will be available by the
      * time the client is actually called.
      * <p>
      * This does add some overhead since synchronization is used to ensure only
      * one client is ever allocated. Furthermore if there is a problem accessing
      * the service it is not detected until the first call.
      */
     private class ProxyInterceptor
         implements MethodInterceptor
     {
         // actual XFire client proxy
         private Object _serviceClient;
 
         public Object invoke(MethodInvocation invocation)
             throws Throwable
         {
             if (_serviceClient == null)
             {
                 if (AopUtils.isToStringMethod(invocation.getMethod()))
                 {
                     // do not lookup service for toString()
                     return "Un-initialized " + XFireClientFactoryBean.this.toString();
                 }
             }
             Method method = invocation.getMethod();
             Object[] args = invocation.getArguments();
             Object client = getClient();
             try
             {
                 return method.invoke(client, args);
             }
             catch (InvocationTargetException e)
             {
                 Object target = SpringUtils.getUserTarget(client);
                 Client c = Client.getInstance(target);
                 StringBuffer callTarget = new StringBuffer(c.getUrl()).append(" arguments: ");
                 for(int x = 0 ; x < args.length ; x ++ )
                 {
                     callTarget.append(args[x].getClass().getName()).append(" : ").append(args[x].toString()).append(" |");
                 }
                 Throwable targetException = e.getTargetException();
                 if (targetException instanceof XFireRuntimeException)
                 {
                     // convert XFire runtime exception to one detailing call
                     // made
                     XFireRuntimeException xfRt = (XFireRuntimeException) targetException;
                     Throwable cause = xfRt.getCause();
                     throw new XFireRuntimeException("Exception while calling: " + callTarget.toString(), cause);
                 }
                 throw targetException;
             }
         }
 
         /**
          * Gets the actual client proxy. This implementation ensures only one
          * client proxy is ever created, even in multi-threaded situations
          * 
          * @return service client proxy
          * @throws MalformedURLException
          */
         private synchronized Object getClient()
             throws Exception
         {
             if (_serviceClient == null)
             {
                 _serviceClient = createClient();
             }
             return _serviceClient;
         }
     }
 }
