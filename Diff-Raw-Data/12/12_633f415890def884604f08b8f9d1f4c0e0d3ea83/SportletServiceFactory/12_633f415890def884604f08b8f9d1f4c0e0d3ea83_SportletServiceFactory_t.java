 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet.service.spi.impl;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.service.PortletService;
 import org.gridlab.gridsphere.portlet.service.PortletServiceNotFoundException;
 import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceAuthorizer;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridlab.gridsphere.portlet.service.spi.impl.descriptor.SportletServiceCollection;
 import org.gridlab.gridsphere.portlet.service.spi.impl.descriptor.SportletServiceDefinition;
 import org.gridlab.gridsphere.portlet.service.spi.impl.descriptor.SportletServiceDescriptor;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfigProperties;
 import org.gridlab.gridsphere.portletcontainer.PortletSessionManager;
 import org.gridlab.gridsphere.services.core.user.impl.GridSphereUserManager;
 import org.gridlab.gridsphere.services.core.user.UserSessionManager;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 
 import javax.servlet.ServletConfig;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.*;
 
 /**
  * The <code>SportletServiceFactory</code> provides a factory for the creation
  * of portlet services. The <code>SportletServiceFactory</code> is also
  * responsible for portlet service lifecycle management including
  * initialization and shutdown.
  */
 public class SportletServiceFactory implements PortletServiceFactory, PortletSessionListener {
 
     private static PortletLog log = SportletLog.getInstance(SportletServiceFactory.class);
     private static SportletServiceFactory instance = null;
     private static GridSphereUserManager userManager = GridSphereUserManager.getInstance();
     private static PortletSessionManager portletSessionManager = PortletSessionManager.getInstance();
     private static UserSessionManager userSessionManager = UserSessionManager.getInstance();
 
 
     // Maintain a single copy of each service instantiated
     // as a classname and PortletServiceProvider pair
     private Hashtable initServices = new Hashtable();
 
     // Hash of all services key = service interface name, value = SportletServiceDefinition
     private Hashtable allServices = new Hashtable();
 
     // Hash of all user services
     private static Hashtable userServices = new Hashtable();
 
     // List of all guest cached guest services
     private Hashtable guestServices = new Hashtable();
 
     /**
      * Private constructor. Use getDefault() instead.
      */
     private SportletServiceFactory() {
         // Reads in the service definitions from the xml file and stores them in allServices
         // organized according to service interface keys and service definition values
         String servicesPath = GridSphereConfig.getServletContext().getRealPath("/WEB-INF/PortletMaster.xml");
         String servicesMappingPath = GridSphereConfig.getServletContext().getRealPath("/WEB-INF/mapping/portlet-services-mapping.xml");
         addServices(servicesPath, servicesMappingPath);
     }
 
     public void login(PortletRequest req) throws PortletException {
 
     }
 
     public void logout(PortletSession session) throws PortletException {
         String userid = userSessionManager.getUserIdFromSession(session);
         if ((userid != null) && (userServices.containsKey(userid))) {
             log.debug("Removing services for userid: " + userid);
             userServices.remove(userid);
         }
     }
 
     /**
      * Umarshalls services from the descriptor file found in servicesPath
      * using the mapping file specified
      *
      * @param servicesPath the path to the portlet services descriptor file
      * @param mappingPath the path to the portlet services mapping file
      */
     public synchronized void addServices(String servicesPath, String mappingPath) {
         SportletServiceDescriptor descriptor = null;
         try {
             descriptor = new SportletServiceDescriptor(servicesPath, mappingPath);
         } catch (IOException e) {
             log.error("IO error unmarshalling " + servicesPath + " using " + mappingPath + " : " + e.getMessage());
             return;
         } catch (PersistenceManagerException e) {
             log.error("Unable to unmarshall " + servicesPath + " using " + mappingPath + " : " + e.getMessage());
             return;
         }
         SportletServiceCollection serviceCollection = descriptor.getServiceCollection();
         List services = serviceCollection.getPortletServicesList();
         Iterator it = services.iterator();
         while (it.hasNext()) {
             SportletServiceDefinition serviceDef = (SportletServiceDefinition) it.next();
             allServices.put(serviceDef.getServiceInterface(), serviceDef);
             log.debug("adding service: " + serviceDef.getServiceInterface() + " service def: " + serviceDef.toString());
         }
     }
 
     public static synchronized SportletServiceFactory getInstance() {
         if (instance == null) {
             instance = new SportletServiceFactory();
         }
         return instance;
     }
 
     /**
      * createPortletServiceFactory instantiates the given class and initializes it. The portlet service properties
      * file must be specified in the ServletContext as an InitParameter with the "PortletServices.properties" key.
      * If no properties file is found or any error occurs an exception is thrown.
      *
      * @param service the class of the service
      * @param servletConfig the servlet configuration
      * @return the instantiated portlet service
      * @throws PortletServiceUnavailableException if the portlet service is unavailable
      * @throws PortletServiceNotFoundException if the PortletService is not found
      */
     public PortletService createPortletService(Class service,
                                                ServletConfig servletConfig,
                                                boolean useCachedService)
             throws PortletServiceUnavailableException, PortletServiceNotFoundException {
 
         PortletServiceProvider psp = null;
 
         // see if we already have an instance of this service
         if (service == null) {
             throw new PortletServiceUnavailableException("Received null service class");
         }
 
         // if init'ed service exists then use it
         if (useCachedService) {
             psp = (PortletServiceProvider) initServices.get(service);
             if (psp != null) return psp;
         }
 
         String serviceName = service.getName();
         SportletServiceDefinition def = (SportletServiceDefinition) allServices.get(serviceName);
         if (def == null) {
             log.error("Unable to find portlet service interface: " + serviceName +
                     " . Please check PortletServices.xml file for proper service entry");
             throw new PortletServiceNotFoundException("Unable to find portlet service: " + serviceName);
         }
 
         // if user is required then pass in Guest user privileges
         if (def.getUserRequired()) {
             return createUserPortletService(service, GuestUser.getInstance(), servletConfig, useCachedService);
         }
 
         /* Create the service implementation */
         String serviceImpl = def.getServiceImplementation();
         if (serviceImpl == null) {
             log.error("Unable to find implementing portlet service: " + serviceName +
                     " . Please check PortletServices.xml file for proper service entry");
             throw new PortletServiceNotFoundException("Unable to find implementing portlet service for interface: " + serviceName);
         }
 
         Properties configProperties = def.getConfigProperties();
 
         PortletServiceConfig portletServiceConfig =
                 new SportletServiceConfig(service, configProperties, servletConfig);
 
         try {
             psp = (PortletServiceProvider) Class.forName(serviceImpl).newInstance();
         } catch (Exception e) {
             log.error("Unable to create portlet service: " + serviceImpl, e);
             throw new PortletServiceNotFoundException("Unable to create portlet service: " + serviceImpl, e);
         }
 
         try {
             psp.init(portletServiceConfig);
         } catch (PortletServiceUnavailableException e) {
             log.error("Unable to initialize portlet service: " + serviceImpl, e);
             throw new PortletServiceNotFoundException("Unable to initialize portlet service: " + serviceImpl, e);
         }
 
         initServices.put(service, psp);
         return psp;
     }
 
     /**
      * Creates a user specific portlet service. If no instance exists, the service
      * will be initialized before it is returned to the client.
      *
      * @param service the class of the service
      * @param user the User
      * @param servletConfig the servlet configuration
      * @param useCachedService reuse a previous initialized service if <code>true</code>,
      * otherwise create a new service instance if <code>false</code>
      * @return the instantiated portlet service
      * @throws PortletServiceUnavailableException if the portlet service is unavailable
      * @throws PortletServiceNotFoundException if the PortletService is not found
      */
     public PortletService createUserPortletService(Class service, User user,
                                                    ServletConfig servletConfig,
                                                    boolean useCachedService)
             throws PortletServiceUnavailableException, PortletServiceNotFoundException {
 
         //PortletServiceProvider psp = null;
 
         String serviceName = service.getName();
 
         SportletServiceDefinition def = (SportletServiceDefinition) allServices.get(serviceName);
         if (def == null) {
             log.error("Unable to find portlet service interface: " + serviceName +
                     " . Please check PortletServices.xml file for proper service entry");
             throw new PortletServiceNotFoundException("Unable to find portlet service: " + serviceName);
         }
         if (!def.getUserRequired()) {
             return createPortletService(service, servletConfig, useCachedService);
         } else if (user == null) {
             throw new PortletServiceNotFoundException("Unable to create service: " + serviceName + " user is null");
         }
 
         if (useCachedService) {
             Map userServiceMap = (Map)userServices.get(user.getID());
             if (userServiceMap != null) {
                 if (userServiceMap.containsKey(serviceName)) {
                     return (PortletService)userServiceMap.get(serviceName);
                 }
             }
         }
 
         if ((user instanceof GuestUser) && (guestServices.containsKey(serviceName))) {
             return (PortletService)guestServices.get(serviceName);
         }
 
         /* Create the service implementation */
         String serviceImpl = def.getServiceImplementation();
         if (serviceImpl == null) {
             log.error("Unable to find implementing portlet service: " + serviceName +
                     " . Please check PortletServices.xml file for proper service entry");
             throw new PortletServiceNotFoundException("Unable to find implementing portlet service for interface: " + serviceName);
         }
 
         Properties configProperties = def.getConfigProperties();
 
         PortletServiceConfig portletServiceConfig =
                 new SportletServiceConfig(service, configProperties, servletConfig);
 
         // Create an authroizer for the secure service
         PortletServiceAuthorizer auth = new SportletServiceAuthorizer(user, userManager);
 
         // instantiate wrapper with user and impl
         PortletServiceProvider psp = null;
         //PortletServiceProvider psp = (PortletServiceProvider)initServices.get(service);
         //if (psp == null) {
         try {
             Class c = Class.forName(serviceImpl);
             Class[] parameterTypes = new Class[]{PortletServiceAuthorizer.class};
             Object[] obj = new Object[]{auth};
             Constructor con = c.getConstructor(parameterTypes);
             psp = (PortletServiceProvider) con.newInstance(obj);
         } catch (Exception e) {
             log.error("Unable to create portlet service wrapper: " + serviceImpl, e);
             throw new PortletServiceNotFoundException("Unable to create portlet service: " + serviceName, e);
         }
 
         try {
             psp.init(portletServiceConfig);
             initServices.put(service, psp);
         } catch (PortletServiceUnavailableException e) {
             log.error("Unable to initialize portlet service: " + serviceImpl, e);
             throw new PortletServiceNotFoundException("The SportletServiceFactory was unable to initialize the portlet service: " + serviceImpl, e);
         }
 
         if (user instanceof GuestUser) {
             guestServices.put(serviceName, psp);
             return psp;
         }
 
         Map userServiceMap = (Map)userServices.get(user.getID());
         if (userServiceMap == null) userServiceMap = new HashMap();
 
         userServiceMap.put(serviceName, psp);
         userServices.put(user.getID(), userServiceMap);
 
         log.debug("printing user services " + user.getID());
         Enumeration enum = userServices.keys();
         while (enum.hasMoreElements()) {
             String u = (String)enum.nextElement();
             Map l = (Map)userServices.get(u);
             Iterator it = l.keySet().iterator();
             while (it.hasNext()) {
                 log.debug("service: " + (String)it.next());
             }
         }
 
         log.debug("printing guest services");
         enum = guestServices.keys();
         while (enum.hasMoreElements()) {
             String s = (String)enum.nextElement();
             log.debug("service: " + s);
         }
 
         List sessions = userSessionManager.getSessions(user);
        if (sessions != null) {
            Iterator it = sessions.iterator();
            while (it.hasNext()) {
                PortletSession session = (PortletSession)it.next();
                if (session != null) portletSessionManager.addSessionListener(session.getId(), this);
            }
         }
         return psp;
     }
 
     /**
      *  Returns an enumaration of the active services (services that have been
      * initialized)
      *
      * @return an enumaration of the active services
      */
     public Enumeration getActiveServices() {
         return initServices.keys();
     }
 
     /**
      * Destroys a portlet service identified by its class
      *
      * @param service the service class to shutdown
      */
     public void shutdownService(Class service) {
         if (initServices.containsKey(service)) {
             log.info("Shutting down service: " + service.getName());
             PortletServiceProvider psp = (PortletServiceProvider) initServices.get(service);
             psp.destroy();
         }
     }
 
     /**
      * Shuts down all portlet services managed by this factory
      */
     public void shutdownServices() {
         // Calls destroy() on all services we know about
         Enumeration keys = initServices.keys();
         log.info("Shutting down all services:");
         while (keys.hasMoreElements()) {
             Class service = (Class) keys.nextElement();
             PortletServiceProvider psp = (PortletServiceProvider) initServices.get(service);
             log.info("Shutting down service: " + service.getName() + " impl: " + psp.getClass().getName());
             psp.destroy();
         }
     }
 
 }
