 package com.github.marschall.osgi.remoting.ejb.client;
 
 import java.io.IOException;
 import java.lang.reflect.Proxy;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.xml.stream.XMLStreamException;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleListener;
 import org.osgi.framework.ServiceRegistration;
 
 import com.github.marschall.osgi.remoting.ejb.api.InitialContextService;
 import com.github.marschall.osgi.remoting.ejb.api.ProxyFlusher;
 
 final class ProxyService implements BundleListener, ProxyFlusher {
 
   private final ConcurrentMap<Bundle, BundleProxyContext> contexts;
 
   private final ServiceXmlParser parser;
 
   private final BundleContext bundleContext;
 
   private final LoggerBridge logger;
 
   private final ClassLoader parent;
 
   private final InitialContextService initialContextService;
 
 
   ProxyService(BundleContext bundleContext, LoggerBridge logger, InitialContextService initialContextService) {
     this.bundleContext = bundleContext;
     this.logger = logger;
     this.initialContextService = initialContextService;
     this.contexts = new ConcurrentHashMap<Bundle, BundleProxyContext>();
     this.parser = new ServiceXmlParser();
     this.parent = new BundlesProxyClassLoader(this.lookUpParentBundles(bundleContext));
   }
 
   private Collection<Bundle> lookUpParentBundles(BundleContext bundleContext) {
     Set<String> symbolicNames = this.initialContextService.getClientBundleSymbolicNames();
     Map<String, Bundle> found = new HashMap<String, Bundle>(symbolicNames.size());
     for (Bundle bundle : bundleContext.getBundles()) {
       String symbolicName = bundle.getSymbolicName();
       if (symbolicNames.contains(symbolicName)) {
         // TODO check version
         found.put(symbolicName, bundle);
       }
     }
     // TODO sort?
     return found.values();
   }
 
 
   void initialBundles(Bundle[] bundles) {
     for (Bundle bundle : bundles) {
       int bundleState = bundle.getState();
       if (bundleState == BundleEvent.STARTING || bundleState == BundleEvent.STARTED) {
         this.addPotentialBundle(bundle);
       }
     }
   }
 
   private String getResourceLocation(Bundle bundle) {
     // http://cxf.apache.org/distributed-osgi-reference.html
     Dictionary<String,String> headers = bundle.getHeaders();
     String remoteServiceHeader = headers.get("Remote-Service");
     if (remoteServiceHeader != null) {
       return remoteServiceHeader;
     } else {
       // TODO check
       return "OSGI-INF/remote-service";
     }
   }
 
   private List<URL> getServiceUrls(Bundle bundle) {
     String resourceLocation = this.getResourceLocation(bundle);
     Enumeration<URL> resources;
     try {
       resources = bundle.getResources(resourceLocation);
     } catch (IOException e) {
       this.logger.warning("failed to access location '" + resourceLocation + "' in bundle: " + bundle);
       return Collections.emptyList();
     }
     if (resources != null && resources.hasMoreElements()) {
       List<URL> serviceXmls = new ArrayList<URL>(1);
       while (resources.hasMoreElements()) {
         URL nextElement = resources.nextElement();
         if (nextElement.getFile().endsWith(".xml")) {
           serviceXmls.add(nextElement);
         }
       }
       return serviceXmls;
     } else {
       return Collections.emptyList();
     }
 
   }
 
   void addPotentialBundle(Bundle bundle) {
     List<URL> serviceUrls = this.getServiceUrls(bundle);
     if (!serviceUrls.isEmpty()) {
       List<ParseResult> results = new ArrayList<ParseResult>(serviceUrls.size());
       for (URL serviceXml : serviceUrls) {
         ParseResult result;
         try {
           result = this.parser.parseServiceXml(serviceXml);
         } catch (IOException e) {
           this.logger.warning("could not parse XML: " + serviceXml + " in bundle:" + bundle + ", ignoring",  e);
           continue;
         } catch (XMLStreamException e) {
           this.logger.warning("could not parse XML: " + serviceXml + " in bundle:" + bundle + ", ignoring",  e);
           continue;
         }
         if (!result.isEmpty()) {
           results.add(result);
         }
       }
       if (results.isEmpty()) {
         return;
       }
 
       ParseResult result = ParseResult.flatten(results);
       this.registerServices(bundle, result);
     }
   }
 
   void registerServices(Bundle bundle, ParseResult result) {
     ClassLoader classLoader = createClassLoader(bundle);
     Thread currentThread = Thread.currentThread();
     ClassLoader oldContextClassLoader = currentThread.getContextClassLoader();
     // switch TCCL only once for all the look ups
     currentThread.setContextClassLoader(classLoader);
     
     List<ServiceCaller> callers = new ArrayList<ServiceCaller>(result.size());
     List<ServiceRegistration<?>> registrations = new ArrayList<ServiceRegistration<?>>(result.size());
     Context namingContext;
     try {
       namingContext = this.createNamingContext();
     } catch (NamingException e) {
       // there isn't really anything anybody can do
       // but we shouldn't pump exception into the OSGi framework
       this.logger.warning("could not register bundle: " + bundle, e);
       return;
     }
 
     try {
       for (ServiceInfo info : result.services) {
         Class<?> interfaceClazz;
         Object jBossProxy;
         try {
           interfaceClazz = classLoader.loadClass(info.interfaceName);
           jBossProxy = this.lookUpJBossProxy(interfaceClazz, info.jndiName, namingContext);
         } catch (ClassNotFoundException e) {
           this.logger.warning("failed to load interface class: " + info.interfaceName
               + ", remote service will not be available", e);
           continue;
         } catch (NamingException e) {
           this.logger.warning("failed to look up interface class: " + info.interfaceName
               + " with JNDI name: " + info.jndiName
               + ", remote service will not be available", e);
           continue;
         } catch (ClassCastException e) {
           this.logger.warning("failed to load interface class: " + info.interfaceName
               + ", remote service will not be available", e);
           continue;
         }
         ServiceCaller serviceCaller = new ServiceCaller(jBossProxy, classLoader, this.logger);
         Object service = Proxy.newProxyInstance(classLoader, new Class[]{interfaceClazz}, serviceCaller);
         callers.add(serviceCaller);
         // TODO properties
         // TODO connection name
         // org.osgi.framework.Constants objectClass value must be of type String[]
         // org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID
         Dictionary<String, Object> properties = new Hashtable<String, Object>();
         properties.put("service.imported", true);
         properties.put("com.github.marschall.osgi.remoting.ejb.jndiName", info.jndiName);
         ServiceRegistration<?> serviceRegistration = this.bundleContext.registerService(info.interfaceName, service, properties);
         registrations.add(serviceRegistration);
       }
     } finally {
       currentThread.setContextClassLoader(oldContextClassLoader);
     }
 
     BundleProxyContext bundleProxyContext = new BundleProxyContext(namingContext, callers, registrations, classLoader);
     // detect double registration is case of concurrent call by #bundleChanged and #initialBundles
     BundleProxyContext previous = this.contexts.putIfAbsent(bundle, bundleProxyContext);
     if (previous != null) {
       // undo registration
       bundleProxyContext.unregisterServices(this.bundleContext);
     }
   }
 
   ClassLoader createClassLoader(Bundle bundle) {
     return new BundleProxyClassLoader(bundle, this.parent);
   }
 
   private Object lookUpJBossProxy(Class<?> interfaceClazz, String jndiName, Context namingContext)
       throws NamingException, ClassCastException {
     // TODO needs to go to custom thread for stateful
     Object proxy = namingContext.lookup(jndiName);
     return interfaceClazz.cast(proxy);
   }
 
   private Context createNamingContext() throws NamingException {
     // create a namingContext passing these properties
     Hashtable<?, ?> environment = this.initialContextService.getEnvironment();
     if (environment != null) {
       return new InitialContext(environment);
     } else {
       return new InitialContext();
     }
   }
 
   void removePotentialBundle(Bundle bundle) {
     BundleProxyContext context = this.contexts.remove(bundle);
     if (context != null) {
       try {
         context.release(bundleContext);
       } catch (NamingException e) {
         // there isn't really anything anybody can do
         // but we shouldn't pump exception into the OSGi framework
         this.logger.warning("could not unregister bundle: " + bundle, e);
       }
     }
   }
 
   @Override
   public void bundleChanged(BundleEvent event) {
     int eventType = event.getType();
     switch (eventType) {
       case BundleEvent.STARTING:
         this.addPotentialBundle(event.getBundle());
         break;
       case BundleEvent.STOPPING:
         this.removePotentialBundle(event.getBundle());
         break;
     }
   }
   
   @Override
   public void flushProxies() {
     for (BundleProxyContext proxyContext : contexts.values()) {
       proxyContext.flushProxies();
     }
   }
   
   void stop() {
     for (BundleProxyContext context : this.contexts.values()) {
       try {
         context.release(bundleContext);
       } catch (NamingException e) {
         // there isn't really anything anybody can do
         // but we shouldn't pump exception into the OSGi framework
         // and we should continue the loop
         this.logger.warning("could not unregister service", e);
       }
     }
   }
   
 
   static final class BundleProxyContext {
 
     private final Context namingContext;
 
     private final Collection<ServiceCaller> callers;
 
     private final Collection<ServiceRegistration<?>> registrations;
 
     private final ClassLoader classLoader;
 
     BundleProxyContext(Context namingContext, Collection<ServiceCaller> callers,
         Collection<ServiceRegistration<?>> registrations, ClassLoader classLoader) {
       this.namingContext = namingContext;
       this.callers = callers;
       this.registrations = registrations;
       this.classLoader = classLoader;
     }
 
     void release(BundleContext bundleContext) throws NamingException {
       this.unregisterServices(bundleContext);
       this.invalidateCallers();
       this.closeNamingConext();
     }
 
     private void closeNamingConext() throws NamingException {
       this.namingContext.close();
     }
 
     private void unregisterServices(BundleContext bundleContext) {
       for (ServiceRegistration<?> registration : this.registrations) {
         bundleContext.ungetService(registration.getReference());
       }
     }
     
     void flushProxies() {
       // TODO
     }
 
     private void invalidateCallers() {
       for (ServiceCaller caller : callers) {
         caller.invalidate();
       }
     }
 
   }
 
 }
