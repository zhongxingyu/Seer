 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
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
 import java.util.Map;
 import java.util.Set;
 
 import javax.jbi.JBIException;
 import javax.jbi.component.ComponentContext;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import javax.management.JMException;
 import javax.management.ObjectName;
 import javax.wsdl.Definition;
 import javax.wsdl.Port;
 import javax.wsdl.PortType;
 import javax.wsdl.Service;
 import javax.wsdl.factory.WSDLFactory;
 import javax.xml.namespace.QName;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.deployment.Provides;
 import org.apache.servicemix.jbi.deployment.Services;
 import org.apache.servicemix.jbi.event.EndpointEvent;
 import org.apache.servicemix.jbi.event.EndpointListener;
 import org.apache.servicemix.jbi.servicedesc.AbstractServiceEndpoint;
 import org.apache.servicemix.jbi.servicedesc.ExternalEndpoint;
 import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
 import org.apache.servicemix.jbi.servicedesc.LinkedEndpoint;
 import org.w3c.dom.Document;
 
 import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Registry for Components
  * 
  * @version $Revision$
  */
 public class EndpointRegistry {
     
     private static final Log logger = LogFactory.getLog(EndpointRegistry.class);
     
     private Registry registry;
     
     private Map endpointMBeans;
     
     private Map internalEndpoints;
     
     private Map externalEndpoints;
     
     private Map linkedEndpoints;
     
     private Map interfaceConnections;
     
     /**
      * Constructor
      * 
      * @param cr
      */
     public EndpointRegistry(Registry registry) {
         this.registry = registry;
         this.endpointMBeans = new ConcurrentHashMap();
         this.internalEndpoints = new ConcurrentHashMap();
         this.externalEndpoints = new ConcurrentHashMap();
         this.linkedEndpoints = new ConcurrentHashMap();
         this.interfaceConnections = new ConcurrentHashMap();
     }
     
     public ServiceEndpoint[] getEndpointsForComponent(ComponentNameSpace cns) {
         Collection endpoints = new ArrayList();
         for (Iterator iter = getInternalEndpoints().iterator(); iter.hasNext();) {
             InternalEndpoint endpoint = (InternalEndpoint) iter.next();
             if (cns.equals(endpoint.getComponentNameSpace())) {
                 endpoints.add(endpoint);
             }
         }
         return asEndpointArray(endpoints);
     }
     
     /**
      * Returns a collection of Endpoint objects
      */
     public Collection getEndpointMBeans() {
         return endpointMBeans.values();
     }
 
     /**
      * Get all endpoints for a given service
      * 
      * @param serviceName
      * @return array of endpoints
      */
     public ServiceEndpoint[] getEndpointsForService(QName serviceName) {
         Collection collection = getEndpointsByService(serviceName, getInternalEndpoints());
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
         InterfaceConnection conn = (InterfaceConnection) interfaceConnections.get(interfaceName);
         if (conn != null) {
             String key = getKey(conn.service, conn.endpoint);
             ServiceEndpoint ep = (ServiceEndpoint) internalEndpoints.get(key);
             if (ep == null) {
                 logger.warn("Connection for interface " + interfaceName + " could not find target for service " + conn.service + " and endpoint " + conn.endpoint);
                 return new ServiceEndpoint[0];
             } else {
                 return new ServiceEndpoint[] { ep };
             }
         }
         Collection result = getEndpointsByInterface(interfaceName, getInternalEndpoints());
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
     public InternalEndpoint registerInternalEndpoint(ComponentContextImpl provider, QName serviceName, String endpointName) throws JBIException {
         // Create endpoint
         String key = getKey(serviceName, endpointName);
         InternalEndpoint registered = (InternalEndpoint) internalEndpoints.get(key);
         // Check if the endpoint has already been activated by another component
         if (registered != null && registered.isLocal()) {
             throw new JBIException("An internal endpoint for service " + serviceName + " and endpoint " + endpointName + " is already registered");
         }        
         // Create a new endpoint
         InternalEndpoint serviceEndpoint = new InternalEndpoint(provider.getComponentNameSpace(), endpointName, serviceName);
         // Get interface from activationSpec
         if (provider.getActivationSpec().getInterfaceName() != null) {
             serviceEndpoint.addInterface(provider.getActivationSpec().getInterfaceName());
         }
         // Get interface from SU jbi descriptor
         retrieveInterfaceFromSUDescriptor(serviceEndpoint);
         // Get interfaces from WSDL
         retrieveInterfacesFromDescription(serviceEndpoint);
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
             // set endpoint to be no more local
             serviceEndpoint.setComponentName(null);
         } else {
             String key = getKey(serviceEndpoint);
             internalEndpoints.remove(key);
             unregisterEndpoint(key);
         }
         fireEvent(serviceEndpoint, EndpointEvent.INTERNAL_ENDPOINT_UNREGISTERED);
     }
     
     /**
      * Retrieve interface implemented by the given endpoint using the SU jbi descriptors.
      * 
      * @param serviceEndpoint the endpoint being checked
      */
     protected void retrieveInterfaceFromSUDescriptor(InternalEndpoint serviceEndpoint) {
         ServiceUnitLifeCycle[] sus = registry.getDeployedServiceUnits(serviceEndpoint.getComponentNameSpace().getName());
         for (int i = 0; i < sus.length; i++) {
             Services services = sus[i].getServices();
             if (services != null) {
                 Provides[] provides = services.getProvides();
                for (int j = 0; j < provides.length; j++) {
                    if (provides[j].getInterfaceName() != null &&
                        serviceEndpoint.getServiceName().equals(provides[j].getServiceName()) &&
                        serviceEndpoint.getEndpointName().equals(provides[j].getEndpointName())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Endpoint " + serviceEndpoint + " is provided by SU " + sus[i].getName());
                            logger.debug("Endpoint " + serviceEndpoint + " implements interface " + provides[j].getInterfaceName());
                         }
                        serviceEndpoint.addInterface(provides[j].getInterfaceName());
                     }
                 }
             }
         }
     }
 
     /**
      * Retrieve interfaces implemented by the given endpoint using its WSDL description.
      * 
      * @param serviceEndpoint the endpoint being checked
      */
     protected void retrieveInterfacesFromDescription(InternalEndpoint serviceEndpoint) {
         try {
             Document document = registry.getEndpointDescriptor(serviceEndpoint);
             if (document == null) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("Endpoint " + serviceEndpoint + " has no service description");
                 }
                 return;
             }
             Definition definition = WSDLFactory.newInstance().newWSDLReader().readWSDL(null, document);
             // Check if the wsdl is only a port type
             // In these cases, only the port type is used, as the service name and endpoint name
             // are provided on the jbi endpoint
             if (definition.getPortTypes().keySet().size() == 1 &&
                 definition.getServices().keySet().size() == 0) {
                 PortType portType = (PortType) definition.getPortTypes().values().iterator().next();
                 QName interfaceName = portType.getQName();
                 if (logger.isDebugEnabled()) {
                     logger.debug("Endpoint " + serviceEndpoint + " implements interface " + interfaceName);
                 }
                 serviceEndpoint.addInterface(interfaceName);
             } else {
                 Service service = definition.getService(serviceEndpoint.getServiceName());
                 if (service == null) {
                     logger.info("Endpoint " + serviceEndpoint + " has a service description, but no matching service found in " + definition.getServices().keySet());
                     return;
                 }
                 Port port = service.getPort(serviceEndpoint.getEndpointName());
                 if (port == null) {
                     logger.info("Endpoint " + serviceEndpoint + " has a service description, but no matching endpoint found in " + service.getPorts().keySet());
                     return;
                 }
                 if (port.getBinding() == null) {
                     logger.info("Endpoint " + serviceEndpoint + " has a service description, but no binding found");
                     return;
                 }
                 if (port.getBinding().getPortType() == null) {
                     logger.info("Endpoint " + serviceEndpoint + " has a service description, but no port type found");
                     return;
                 }
                 QName interfaceName = port.getBinding().getPortType().getQName();
                 if (logger.isDebugEnabled()) {
                     logger.debug("Endpoint " + serviceEndpoint + " implements interface " + interfaceName);
                 }
                 serviceEndpoint.addInterface(interfaceName);
             }
         } catch (Exception e) {
             logger.warn("Error retrieving interfaces from service description: " + e.getMessage());
             if (logger.isDebugEnabled()) {
                 logger.debug("Error retrieving interfaces from service description", e);
             }
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
         InternalEndpoint endpoint = (InternalEndpoint) internalEndpoints.get(getKey(remote));
         if (endpoint != null) {
             endpoint.removeRemoteEndpoint(remote);
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
         ServiceEndpoint ep = (ServiceEndpoint) linkedEndpoints.get(key);
         if (ep == null) {
             ep = (ServiceEndpoint) internalEndpoints.get(key);
         }
         return ep;
     }
     
     public ServiceEndpoint getInternalEndpoint(QName service, String name) {
         return (ServiceEndpoint) internalEndpoints.get(getKey(service, name));
     }
 
     /**
      * Registers the given external endpoint with the NMR. This indicates to the NMR that the given endpoint is used as
      * a proxy for external service consumers to access an internal service of the same service name (but a different
      * endpoint name).
      * 
      * @param provider
      * @param externalEndpoint the external endpoint to be registered, must be non-null.
      * @throws JBIException 
      */
     public void registerExternalEndpoint(ComponentNameSpace cns, ServiceEndpoint externalEndpoint) throws JBIException {
         ExternalEndpoint serviceEndpoint = new ExternalEndpoint(cns, externalEndpoint); 
         if (externalEndpoints.get(getKey(serviceEndpoint)) != null) {
             throw new JBIException("An external endpoint for service " + externalEndpoint.getServiceName() + " and endpoint " + externalEndpoint.getEndpointName() + " is already registered");
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
      * @param provider
      * @param externalEndpoint the external endpoint to be deregistered; must be non-null.
      */
     public void unregisterExternalEndpoint(ComponentNameSpace cns, ServiceEndpoint externalEndpoint) {
         externalEndpoints.remove(getKey(externalEndpoint));
         unregisterEndpoint(getKey(externalEndpoint));
         fireEvent(externalEndpoint, EndpointEvent.EXTERNAL_ENDPOINT_UNREGISTERED);
     }
 
     /**
      * This methods returns only registered external endpoints
      * 
      * @param interfaceName qualified name of interface implemented by the endpoints; must be non-null.
      * @return an array of available external endpoints for the specified interface name; must be non-null; may be
      * empty.
      */
     public ServiceEndpoint[] getExternalEndpointsForInterface(QName interfaceName) {
         Collection endpoints = getEndpointsByInterface(interfaceName, getExternalEndpoints());
         return asEndpointArray(endpoints);
     }
 
     /**
      * Get external endpoints for the service
      * 
      * @param serviceName qualified name of service that contains the endpoints; must be non-null.
      * @return an array of available external endpoints for the specified service name; must be non-null; may be empty.
      */
     public ServiceEndpoint[] getExternalEndpointsForService(QName serviceName) {
         Collection endpoints = getEndpointsByService(serviceName, getExternalEndpoints());
         return asEndpointArray(endpoints);
     }
 
     /**
      * Helper method to convert the given collection into an array of endpoints
      * 
      * @param collection
      * @return array of endpoints
      */
     protected ServiceEndpoint[] asEndpointArray(Collection collection) {
         if (collection == null) {
             return new ServiceEndpoint[0];
         }
         ServiceEndpoint[] answer = new ServiceEndpoint[collection.size()];
         answer = (ServiceEndpoint[]) collection.toArray(answer);
         return answer;
     }
 
     /**
      * return a collection of endpoints
      * 
      * @param serviceName
      * @param endpoints
      * @return collection of endpoints
      */
     protected Collection getEndpointsByService(QName serviceName, Collection endpoints) {
         Collection answer = new ArrayList();
         for (Iterator i = endpoints.iterator(); i.hasNext();) {
             ServiceEndpoint endpoint = (ServiceEndpoint) i.next();
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
     protected Collection getEndpointsByInterface(QName interfaceName, Collection endpoints) {
         if (interfaceName == null) {
             return endpoints;
         }
         Set answer = new HashSet();
         for (Iterator i = endpoints.iterator(); i.hasNext();) {
             ServiceEndpoint endpoint = (ServiceEndpoint) i.next();
             QName[] interfaces = endpoint.getInterfaces();
             if (interfaces != null) {
                 for (int k = 0; k < interfaces.length;k ++) {
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
     protected Collection getInternalEndpoints() {
         return internalEndpoints.values();
     }
 
     /**
      * @return all external endpoints
      */
     protected Collection getExternalEndpoints() {
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
     public void registerEndpointConnection(QName fromSvc, String fromEp, QName toSvc, String toEp, String link) throws JBIException {
         LinkedEndpoint ep = new LinkedEndpoint(fromSvc, fromEp, toSvc, toEp, link);
         if (linkedEndpoints.get(getKey(ep)) != null) {
             throw new JBIException("An endpoint connection for service " + ep.getServiceName() + " and name " + ep.getEndpointName() + " is already registered");
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
         unregisterEndpoint(getKey(fromSvc, fromEp));
         LinkedEndpoint ep = (LinkedEndpoint) linkedEndpoints.remove(getKey(fromSvc, fromEp));
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
         String key = getKey(serviceEndpoint);
         try {
             Endpoint endpoint = new Endpoint(serviceEndpoint, registry);
             ObjectName objectName = registry.getContainer().getManagementContext().createObjectName(endpoint);
             registry.getContainer().getManagementContext().registerMBean(objectName, endpoint, EndpointMBean.class);
             endpointMBeans.put(key, endpoint);
         } catch (JMException e) {
             logger.error("Could not register MBean for endpoint", e);
         }
     }
     
     private void unregisterEndpoint(String key) {
         Endpoint ep = (Endpoint) endpointMBeans.remove(key);
         if (ep != null) {
             try {
                 registry.getContainer().getManagementContext().unregisterMBean(ep);
             } catch (JBIException e) {
                 logger.error("Could not unregister MBean for endpoint", e);
             }
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
 
     protected void fireEvent(ServiceEndpoint ep, int type) {
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
             }
         }
         
     }
 
 }
