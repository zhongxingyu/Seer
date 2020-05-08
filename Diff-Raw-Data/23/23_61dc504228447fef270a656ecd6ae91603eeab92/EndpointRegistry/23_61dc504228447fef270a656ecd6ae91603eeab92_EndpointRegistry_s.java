 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.jbi.framework;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import javax.jbi.JBIException;
 import javax.jbi.component.ComponentContext;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import javax.management.JMException;
 import javax.management.ObjectName;
 import javax.xml.namespace.QName;
 
 import org.apache.servicemix.jbi.event.EndpointEvent;
 import org.apache.servicemix.jbi.event.EndpointListener;
 import org.apache.servicemix.jbi.framework.support.EndpointProcessor;
 import org.apache.servicemix.jbi.servicedesc.AbstractServiceEndpoint;
 import org.apache.servicemix.jbi.servicedesc.ExternalEndpoint;
 import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
 import org.apache.servicemix.jbi.servicedesc.LinkedEndpoint;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Registry for Components
  * 
  * @version $Revision$
  */
 public class EndpointRegistry {
     
     private static final transient Logger LOGGER = LoggerFactory.getLogger(EndpointRegistry.class);
     
     private Registry registry;
     
     private Map<AbstractServiceEndpoint, Endpoint> endpointMBeans;
     
     private Map<String, ServiceEndpoint> internalEndpoints;
     
     private Map<String, ServiceEndpoint> externalEndpoints;
     
     private Map<String, ServiceEndpoint> linkedEndpoints;
     
     private Map<QName, InterfaceConnection> interfaceConnections;
     
     private List<EndpointProcessor> endpointProcessors;
     
    private Executor executor = Executors.newSingleThreadExecutor();
     
     /**
      * Constructor
      * 
      * @param cr
      */
     public EndpointRegistry(Registry registry) {
         this.registry = registry;
         this.endpointMBeans = new ConcurrentHashMap<AbstractServiceEndpoint, Endpoint>();
         this.internalEndpoints = new ConcurrentHashMap<String, ServiceEndpoint>();
         this.externalEndpoints = new ConcurrentHashMap<String, ServiceEndpoint>();
         this.linkedEndpoints = new ConcurrentHashMap<String, ServiceEndpoint>();
         this.interfaceConnections = new ConcurrentHashMap<QName, InterfaceConnection>();
         this.endpointProcessors = getEndpointProcessors();
         this.executor.execute(new Runnable() {
             public void run() {
                 LOGGER.debug("Initializing endpoint event dispatch thread");
             }
         });
     }
     
     private List<EndpointProcessor> getEndpointProcessors() {
         List<EndpointProcessor> l = new ArrayList<EndpointProcessor>();
         String[] classes = {"org.apache.servicemix.jbi.framework.support.SUDescriptorProcessor",
                             "org.apache.servicemix.jbi.framework.support.WSDL1Processor",
                             "org.apache.servicemix.jbi.framework.support.WSDL2Processor" };
         for (int i = 0; i < classes.length; i++) {
             try {
                 EndpointProcessor p = (EndpointProcessor) Class.forName(classes[i]).newInstance();
                 p.init(registry);
                 l.add(p);
             } catch (Throwable e) {
                 LOGGER.warn("Disabled endpoint processor '{}", classes[i], e);
             }
         }
         return l;
     }
     
     public ServiceEndpoint[] getEndpointsForComponent(ComponentNameSpace cns) {
         Collection<ServiceEndpoint> endpoints = new ArrayList<ServiceEndpoint>();
         for (Iterator<ServiceEndpoint> iter = getInternalEndpoints().iterator(); iter.hasNext();) {
             InternalEndpoint endpoint = (InternalEndpoint) iter.next();
             if (cns.equals(endpoint.getComponentNameSpace())) {
                 endpoints.add(endpoint);
             }
         }
         return asEndpointArray(endpoints);
     }
     
     public ServiceEndpoint[] getAllEndpointsForComponent(ComponentNameSpace cns) {
         Collection<ServiceEndpoint> endpoints = new ArrayList<ServiceEndpoint>();
         for (Iterator<ServiceEndpoint> iter = getInternalEndpoints().iterator(); iter.hasNext();) {
             InternalEndpoint endpoint = (InternalEndpoint) iter.next();
             if (cns.equals(endpoint.getComponentNameSpace())) {
                 endpoints.add(endpoint);
             }
         }
         for (Iterator<ServiceEndpoint> iter = getExternalEndpoints().iterator(); iter.hasNext();) {
             ExternalEndpoint endpoint = (ExternalEndpoint) iter.next();
             if (cns.equals(endpoint.getComponentNameSpace())) {
                 endpoints.add(endpoint);
             }
         }
         return asEndpointArray(endpoints);
     }
     
     /**
      * Returns a collection of Endpoint objects
      */
     public Collection<Endpoint> getEndpointMBeans() {
         return endpointMBeans.values();
     }
 
     /**
      * Get all endpoints for a given service
      * 
      * @param serviceName
      * @return array of endpoints
      */
     public ServiceEndpoint[] getEndpointsForService(QName serviceName) {
         Collection<ServiceEndpoint> collection = getEndpointsByService(serviceName, getInternalEndpoints());
         return asEndpointArray(collection);
     }
 
     /**
      * This will return the endpoints for all services and endpoints that implement the named interface (portType in
      * WSDL 1.1). This method does NOT include external endpoints.
      * 
      * @param interfaceName qualified name of interface/portType that is implemented by the endpoint; if
      * <code>null</code> then all activated endpoints in the JBI environment must be returned.
      * @return an array of available endpoints for the specified interface name; must be non-null; may be empty.
      */
     public ServiceEndpoint[] getEndpointsForInterface(QName interfaceName) {
         if (interfaceName == null) {
             return asEndpointArray(internalEndpoints.values());
         }
         InterfaceConnection conn = interfaceConnections.get(interfaceName);
         if (conn != null) {
             String key = getKey(conn.service, conn.endpoint);
             ServiceEndpoint ep = internalEndpoints.get(key);
             if (ep == null) {
                 LOGGER.warn("Connection for interface " + interfaceName + " could not find target for service "
                                 + conn.service + " and endpoint " + conn.endpoint);
                 return new ServiceEndpoint[0];
             } else {
                 return new ServiceEndpoint[] {ep };
             }
         }
         Collection<ServiceEndpoint> result = getEndpointsByInterface(interfaceName, getInternalEndpoints());
         return asEndpointArray(result);
     }
 
     /**
      * Activate an endpoint
      * 
      * @param provider
      * @param serviceName
      * @param endpointName
      * @return the endpoint
      * @throws JBIException 
      */
     public InternalEndpoint registerInternalEndpoint(ComponentContextImpl provider, 
                                                      QName serviceName, 
                                                      String endpointName) throws JBIException {
         // Create endpoint
         String key = getKey(serviceName, endpointName);
         InternalEndpoint registered = (InternalEndpoint) internalEndpoints.get(key);
         // Check if the endpoint has already been activated by another component
         if (registered != null && registered.isLocal()) {
             throw new JBIException("An internal endpoint for service " + serviceName
                                         + " and endpoint " + endpointName + " is already registered");
         }        
         // Create a new endpoint
         InternalEndpoint serviceEndpoint = new InternalEndpoint(provider.getComponentNameSpace(), endpointName, serviceName);
         // Get interface from activationSpec
         if (provider.getActivationSpec().getInterfaceName() != null) {
             serviceEndpoint.addInterface(provider.getActivationSpec().getInterfaceName());
         }
         // Get interfaces
         for (Iterator<EndpointProcessor> it = endpointProcessors.iterator(); it.hasNext();) {
             EndpointProcessor p = it.next();
             p.process(serviceEndpoint);
         }
         // Set remote namespaces
         if (registered != null) {
             InternalEndpoint[] remote = registered.getRemoteEndpoints();
             for (int i = 0; i < remote.length; i++) {
                 serviceEndpoint.addRemoteEndpoint(remote[i]);
             }
         }
         // Register endpoint
         internalEndpoints.put(key, serviceEndpoint);
         registerEndpoint(serviceEndpoint);
         fireEvent(serviceEndpoint, EndpointEvent.INTERNAL_ENDPOINT_REGISTERED);
         return serviceEndpoint;
     }
 
     /**
      * Called by component context when endpoints are being deactivated.
      * 
      * @param provider
      * @param serviceEndpoint
      */
     public void unregisterInternalEndpoint(ComponentContext provider, InternalEndpoint serviceEndpoint) {
         if (serviceEndpoint.isClustered()) {
             fireEvent(serviceEndpoint, EndpointEvent.INTERNAL_ENDPOINT_UNREGISTERED);
             // set endpoint to be no more local
             serviceEndpoint.setComponentName(null);
         } else {
             String key = getKey(serviceEndpoint);
             internalEndpoints.remove(key);
             unregisterEndpoint(serviceEndpoint);
             fireEvent(serviceEndpoint, EndpointEvent.INTERNAL_ENDPOINT_UNREGISTERED);
         }
     }
     
     /**
      * Registers a remote endpoint
      * 
      * @param remote
      */
     public void registerRemoteEndpoint(InternalEndpoint remote) {
         InternalEndpoint endpoint = (InternalEndpoint) internalEndpoints.get(getKey(remote));
         // Create endpoint if not already existing
         if (endpoint == null) {
             endpoint = new InternalEndpoint(null, remote.getEndpointName(), remote.getServiceName());
             internalEndpoints.put(getKey(endpoint), endpoint);
         }
         // Add remote endpoint
         endpoint.addRemoteEndpoint(remote);
         fireEvent(remote, EndpointEvent.REMOTE_ENDPOINT_REGISTERED);
     }
     
     /**
      * Unregisters a remote endpoint
      * 
      * @param remote
      */
     public void unregisterRemoteEndpoint(InternalEndpoint remote) {
         String key = getKey(remote);
         InternalEndpoint endpoint = (InternalEndpoint) internalEndpoints.get(key);
         if (endpoint != null) {
             endpoint.removeRemoteEndpoint(remote);
             if (!endpoint.isClustered() && !endpoint.isLocal()) {
                 internalEndpoints.remove(key);
                 unregisterEndpoint(endpoint);
             }
             fireEvent(remote, EndpointEvent.REMOTE_ENDPOINT_UNREGISTERED);
         }
     }
 
     /**
      * Get the named ServiceEndpoint, if activated
      * 
      * @param service
      * @param name
      * @return the activated ServiceEndpoint or null
      */
     public ServiceEndpoint getEndpoint(QName service, String name) {
         String key = getKey(service, name);
         ServiceEndpoint ep = linkedEndpoints.get(key);
         if (ep == null) {
             ep = internalEndpoints.get(key);
         }
         return ep;
     }
     
     public ServiceEndpoint getInternalEndpoint(QName service, String name) {
         return internalEndpoints.get(getKey(service, name));
     }
 
     /**
      * Registers the given external endpoint with the NMR. This indicates to the NMR that the given endpoint is used as
      * a proxy for external service consumers to access an internal service of the same service name (but a different
      * endpoint name).
      * 
      * @param cns
      * @param externalEndpoint the external endpoint to be registered, must be non-null.
      * @throws JBIException 
      */
     public void registerExternalEndpoint(ComponentNameSpace cns, ServiceEndpoint externalEndpoint) throws JBIException {
         ExternalEndpoint serviceEndpoint = new ExternalEndpoint(cns, externalEndpoint); 
         if (externalEndpoints.get(getKey(serviceEndpoint)) != null) {
             throw new JBIException("An external endpoint for service " + externalEndpoint.getServiceName()
                                     + " and endpoint " + externalEndpoint.getEndpointName() + " is already registered");
         }
         registerEndpoint(serviceEndpoint);
         externalEndpoints.put(getKey(serviceEndpoint), serviceEndpoint);
         fireEvent(serviceEndpoint, EndpointEvent.EXTERNAL_ENDPOINT_REGISTERED);
     }
 
     /**
      * Deregisters the given external endpoint with the NMR. This indicates to the NMR that the given external endpoint
      * can no longer be used as a proxy for external service consumers to access an internal service of the same service
      * name.
      * 
      * @param cns
      * @param externalEndpoint the external endpoint to be deregistered; must be non-null.
      */
     public void unregisterExternalEndpoint(ComponentNameSpace cns, ServiceEndpoint externalEndpoint) {
         ExternalEndpoint ep = (ExternalEndpoint) externalEndpoints.remove(getKey(externalEndpoint));
         unregisterEndpoint(ep);
         fireEvent(ep, EndpointEvent.EXTERNAL_ENDPOINT_UNREGISTERED);
     }
 
     /**
      * This methods returns only registered external endpoints
      * 
      * @param interfaceName qualified name of interface implemented by the endpoints; must be non-null.
      * @return an array of available external endpoints for the specified interface name; must be non-null; may be
      * empty.
      */
     public ServiceEndpoint[] getExternalEndpointsForInterface(QName interfaceName) {
         Collection<ServiceEndpoint> endpoints = getEndpointsByInterface(interfaceName, getExternalEndpoints());
         return asEndpointArray(endpoints);
     }
 
     /**
      * Get external endpoints for the service
      * 
      * @param serviceName qualified name of service that contains the endpoints; must be non-null.
      * @return an array of available external endpoints for the specified service name; must be non-null; may be empty.
      */
     public ServiceEndpoint[] getExternalEndpointsForService(QName serviceName) {
         Collection<ServiceEndpoint> endpoints = getEndpointsByService(serviceName, getExternalEndpoints());
         return asEndpointArray(endpoints);
     }
 
     /**
      * Helper method to convert the given collection into an array of endpoints
      * 
      * @param collection
      * @return array of endpoints
      */
     protected ServiceEndpoint[] asEndpointArray(Collection<ServiceEndpoint> collection) {
         if (collection == null) {
             return new ServiceEndpoint[0];
         }
         ServiceEndpoint[] answer = new ServiceEndpoint[collection.size()];
         answer = collection.toArray(answer);
         return answer;
     }
 
     /**
      * return a collection of endpoints
      * 
      * @param serviceName
      * @param endpoints
      * @return collection of endpoints
      */
     protected Collection<ServiceEndpoint> getEndpointsByService(QName serviceName, Collection<ServiceEndpoint> endpoints) {
         Collection<ServiceEndpoint> answer = new ArrayList<ServiceEndpoint>();
         for (Iterator<ServiceEndpoint> i = endpoints.iterator(); i.hasNext();) {
             ServiceEndpoint endpoint = i.next();
             if (endpoint.getServiceName().equals(serviceName)) {
                 answer.add(endpoint);
             }
         }
         return answer;
     }
     
     /**
      * Filters the given endpoints and returns those implementing the
      * given interface name.  If interfaceName is null, then no filter
      * is applied.
      * 
      */
     protected Collection<ServiceEndpoint> getEndpointsByInterface(QName interfaceName, Collection<ServiceEndpoint> endpoints) {
         if (interfaceName == null) {
             return endpoints;
         }
         Set<ServiceEndpoint> answer = new HashSet<ServiceEndpoint>();
         for (Iterator<ServiceEndpoint> i = endpoints.iterator(); i.hasNext();) {
             ServiceEndpoint endpoint = i.next();
             QName[] interfaces = endpoint.getInterfaces();
             if (interfaces != null) {
                 for (int k = 0; k < interfaces.length; k++) {
                     QName qn = interfaces[k];
                     if (qn != null && qn.equals(interfaceName)) {
                         answer.add(endpoint);
                         break;
                     }
                 }
             }
         }
         return answer;
     }
 
     /**
      * @return all default endpoints
      */
     protected Collection<ServiceEndpoint> getInternalEndpoints() {
         return internalEndpoints.values();
     }
 
     /**
      * @return all external endpoints
      */
     protected Collection<ServiceEndpoint> getExternalEndpoints() {
         return externalEndpoints.values();
     }
 
     /**
      * Registers an endpoint connection.
      * 
      * @param fromSvc
      * @param fromEp
      * @param toSvc
      * @param toEp
      * @param link
      * @throws JBIException
      */
     public void registerEndpointConnection(QName fromSvc, String fromEp, 
                                            QName toSvc, String toEp, String link) throws JBIException {
         LinkedEndpoint ep = new LinkedEndpoint(fromSvc, fromEp, toSvc, toEp, link);
         if (linkedEndpoints.get(getKey(ep)) != null) {
             throw new JBIException("An endpoint connection for service " + ep.getServiceName() + " and name "
                                         + ep.getEndpointName() + " is already registered");
         }
         linkedEndpoints.put(getKey(ep), ep);
         registerEndpoint(ep);
         fireEvent(ep, EndpointEvent.LINKED_ENDPOINT_REGISTERED);
     }
 
     /**
      * Unregister an endpoint connection.
      * 
      * @param fromSvc
      * @param fromEp
      */
     public void unregisterEndpointConnection(QName fromSvc, String fromEp) {
         LinkedEndpoint ep = (LinkedEndpoint) linkedEndpoints.remove(getKey(fromSvc, fromEp));
         unregisterEndpoint(ep);
         fireEvent(ep, EndpointEvent.LINKED_ENDPOINT_UNREGISTERED);
     }
     
     /**
      * Registers an interface connection.
      * 
      * @param fromItf
      * @param toSvc
      * @param toEp
      * @throws JBIException
      */
     public void registerInterfaceConnection(QName fromItf, QName toSvc, String toEp) throws JBIException {
         if (interfaceConnections.get(fromItf) != null) {
             throw new JBIException("An interface connection for " + fromItf + " is already registered");
         }
         interfaceConnections.put(fromItf, new InterfaceConnection(toSvc, toEp));
     }
 
     /**
      * Unregisters an interface connection.
      * 
      * @param fromItf
      */
     public void unregisterInterfaceConnection(QName fromItf) {
         interfaceConnections.remove(fromItf);
         
     }
     
     private void registerEndpoint(AbstractServiceEndpoint serviceEndpoint) {
         try {
             Endpoint endpoint = new Endpoint(serviceEndpoint, registry);
             ObjectName objectName = registry.getContainer().getManagementContext().createObjectName(endpoint);
             registry.getContainer().getManagementContext().registerMBean(objectName, endpoint, EndpointMBean.class);
             endpointMBeans.put(serviceEndpoint, endpoint);
         } catch (JMException e) {
             LOGGER.error("Could not register MBean for endpoint", e);
         }
     }
     
     private void unregisterEndpoint(AbstractServiceEndpoint se) {
         Endpoint ep = endpointMBeans.remove(se);
         try {
             registry.getContainer().getManagementContext().unregisterMBean(ep);
         } catch (JBIException e) {
             LOGGER.error("Could not unregister MBean for endpoint", e);
         }
     }
 
     private String getKey(ServiceEndpoint ep) {
         return getKey(ep.getServiceName(), ep.getEndpointName());
     }
     
     private String getKey(QName svcName, String epName) {
         return svcName + epName;
     }
 
     private static class InterfaceConnection {
         QName service;
         String endpoint;
         InterfaceConnection(QName service, String endpoint) {
             this.service = service;
             this.endpoint = endpoint;
         }
     }
 
     protected synchronized void fireEvent(final ServiceEndpoint ep, final int type) {
         executor.execute(new Runnable() {
             public void run() {
                 EndpointEvent event = new EndpointEvent(ep, type);
                 EndpointListener[] listeners = (EndpointListener[]) registry.getContainer().getListeners(EndpointListener.class);
                 for (int i = 0; i < listeners.length; i++) {
                     switch (type) {
                     case EndpointEvent.INTERNAL_ENDPOINT_REGISTERED:
                         listeners[i].internalEndpointRegistered(event);
                         break;
                     case EndpointEvent.INTERNAL_ENDPOINT_UNREGISTERED:
                         listeners[i].internalEndpointUnregistered(event);
                         break;
                     case EndpointEvent.EXTERNAL_ENDPOINT_REGISTERED:
                         listeners[i].externalEndpointRegistered(event);
                         break;
                     case EndpointEvent.EXTERNAL_ENDPOINT_UNREGISTERED:
                         listeners[i].externalEndpointUnregistered(event);
                         break;
                     case EndpointEvent.LINKED_ENDPOINT_REGISTERED:
                         listeners[i].linkedEndpointRegistered(event);
                         break;
                     case EndpointEvent.LINKED_ENDPOINT_UNREGISTERED:
                         listeners[i].linkedEndpointUnregistered(event);
                         break;
                     case EndpointEvent.REMOTE_ENDPOINT_REGISTERED:
                         listeners[i].remoteEndpointRegistered(event);
                         break;
                     case EndpointEvent.REMOTE_ENDPOINT_UNREGISTERED:
                         listeners[i].remoteEndpointUnregistered(event);
                         break;
                     default:
                         break;
                     }
                 }
             }
         });
     }
 
 }
