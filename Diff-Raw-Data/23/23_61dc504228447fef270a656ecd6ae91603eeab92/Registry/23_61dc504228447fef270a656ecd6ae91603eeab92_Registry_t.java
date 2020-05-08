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
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.jbi.JBIException;
 import javax.jbi.component.Component;
 import javax.jbi.component.ComponentContext;
 import javax.jbi.management.DeploymentException;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import javax.management.JMException;
 import javax.management.MBeanAttributeInfo;
 import javax.management.ObjectName;
 import javax.xml.namespace.QName;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import org.apache.servicemix.executors.Executor;
 import org.apache.servicemix.jbi.container.ActivationSpec;
 import org.apache.servicemix.jbi.container.EnvironmentContext;
 import org.apache.servicemix.jbi.container.JBIContainer;
 import org.apache.servicemix.jbi.container.ServiceAssemblyEnvironment;
 import org.apache.servicemix.jbi.container.SubscriptionSpec;
 import org.apache.servicemix.jbi.deployment.ServiceAssembly;
 import org.apache.servicemix.jbi.deployment.ServiceUnit;
 import org.apache.servicemix.jbi.management.AttributeInfoHelper;
 import org.apache.servicemix.jbi.management.BaseSystemService;
 import org.apache.servicemix.jbi.messaging.DeliveryChannelImpl;
 import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
 import org.apache.servicemix.jbi.resolver.URIResolver;
 import org.apache.servicemix.jbi.servicedesc.AbstractServiceEndpoint;
 import org.apache.servicemix.jbi.servicedesc.DynamicEndpoint;
 import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
 import org.apache.servicemix.jbi.util.DOMUtil;
 import org.apache.servicemix.jbi.util.WSAddressingConstants;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Registry - state infomation including running state, SA's deployed etc.
  * 
  * @version $Revision$
  */
 public class Registry extends BaseSystemService implements RegistryMBean {
     
     private static final transient Logger LOGGER = LoggerFactory.getLogger(Registry.class);
     
     private ComponentRegistry componentRegistry;
     private EndpointRegistry endpointRegistry;
     private SubscriptionRegistry subscriptionRegistry;
     private ServiceAssemblyRegistry serviceAssemblyRegistry;
     private Map<String, SharedLibrary> sharedLibraries;
     private Map<String, ServiceUnitLifeCycle> serviceUnits;
     private List<ServiceAssemblyLifeCycle> pendingAssemblies;
     private List<ComponentMBeanImpl> pendingComponents;
     private Executor executor;
 
     /**
      * Constructor
      */
     public Registry() {
         this.componentRegistry = new ComponentRegistry(this);
         this.endpointRegistry = new EndpointRegistry(this);
         this.subscriptionRegistry = new SubscriptionRegistry(this);
         this.serviceAssemblyRegistry = new ServiceAssemblyRegistry(this);
         this.serviceUnits = new ConcurrentHashMap<String, ServiceUnitLifeCycle>();
         this.pendingAssemblies = new CopyOnWriteArrayList<ServiceAssemblyLifeCycle>();
         this.sharedLibraries = new ConcurrentHashMap<String, SharedLibrary>();
         this.pendingComponents = new CopyOnWriteArrayList<ComponentMBeanImpl>();
     }
     
     /**
      * Get the description
      * @return description
      */
     public String getDescription() {
         return "Registry of Components/SU's and Endpoints";
     }
 
     protected Class getServiceMBean() {
         return RegistryMBean.class;
     }
 
     
     public ComponentRegistry getComponentRegistry() {
         return componentRegistry;
     }
 
     public EndpointRegistry getEndpointRegistry() {
         return endpointRegistry;
     }
 
     public void init(JBIContainer container) throws JBIException {
         super.init(container);
         executor = container.getExecutorFactory().createExecutor("services.registry");
     }
     
     /**
      * start brokering
      * 
      * @throws JBIException
      */
     public void start() throws JBIException {
         componentRegistry.start();
         serviceAssemblyRegistry.start();
         super.start();
     }
 
     /**
      * stop brokering
      * 
      * @throws JBIException
      */
     public void stop() throws JBIException {
         serviceAssemblyRegistry.stop();
         componentRegistry.stop();
         super.stop();
     }
 
     /**
      * shutdown all Components
      * 
      * @throws JBIException
      */
     public void shutDown() throws JBIException {
         serviceAssemblyRegistry.shutDown();
         componentRegistry.shutDown();
        endpointRegistry.shutDown();
         super.shutDown();
         container.getManagementContext().unregisterMBean(this);
         executor.shutdown();
     }
     
     /**
      * @return the EnvironmentContext
      */
     protected EnvironmentContext getEnvironmentContext() {
         return container.getEnvironmentContext();
     }
     
     /**
      * @return true if the container is embedded
      */
     protected boolean isContainerEmbedded() {
         return container.isEmbedded();
     }
 
     protected InternalEndpoint matchEndpointByName(ServiceEndpoint[] endpoints, String endpointName) {
         InternalEndpoint result = null;
         if (endpoints != null && endpointName != null && endpointName.length() > 0) {
             for (int i = 0; i < endpoints.length; i++) {
                 if (endpoints[i].getEndpointName().equals(endpointName)) {
                     result = (InternalEndpoint) endpoints[i];
                     break;
                 }
             }
         }
         return result;
     }
 
     /**
      * @param context
      * @param serviceName
      * @param endpointName
      * @return EndPointReference
      * @throws JBIException
      */
     public ServiceEndpoint activateEndpoint(ComponentContextImpl context, 
                                             QName serviceName,
                                             String endpointName) throws JBIException {
         return endpointRegistry.registerInternalEndpoint(context, serviceName, endpointName);
     }
 
     public ServiceEndpoint[] getEndpointsForComponent(ComponentNameSpace cns) {
         return endpointRegistry.getEndpointsForComponent(cns);
     }
     
     /**
      * @param interfaceName qualified name
      * @return an array of available endpoints for the specified interface name;
      */
     public ServiceEndpoint[] getEndpointsForInterface(QName interfaceName) {
         return endpointRegistry.getEndpointsForInterface(interfaceName);
     }
 
     /**
      * @param provider
      * @param serviceEndpoint
      */
     public void deactivateEndpoint(ComponentContext provider, InternalEndpoint serviceEndpoint) {
         endpointRegistry.unregisterInternalEndpoint(provider, serviceEndpoint);
     }
 
     /**
      * Retrieve the service description metadata for the specified endpoint.
      * <p>
      * Note that the result can use either the WSDL 1.1 or WSDL 2.0 description language.
      * 
      * @param endpoint endpoint reference; must be non-null.
      * @return metadata describing endpoint, or <code>null</code> if metadata is unavailable.
      * @throws JBIException invalid endpoint reference.
      */
     public Document getEndpointDescriptor(ServiceEndpoint endpoint) throws JBIException {
         if (!(endpoint instanceof AbstractServiceEndpoint)) {
             throw new JBIException("Descriptors can not be queried for external endpoints");
         }
         AbstractServiceEndpoint se = (AbstractServiceEndpoint) endpoint;
         // TODO: what if the endpoint is linked or dynamic
         ComponentMBeanImpl component = getComponent(se.getComponentNameSpace());
         return component.getComponent().getServiceDescription(endpoint);
     }
 
     /**
      * Resolve the given endpoint reference into a service endpoint. This is called by the component when it has an EPR
      * that it wants to resolve into a service endpoint.
      * <p>
      * Note that the service endpoint returned refers to a dynamic endpoint; the endpoint will exist only as long as
      * this component retains a strong reference to the object returned by this method. The endpoint may not be included
      * in the list of "activated" endpoints.
      * 
      * @param epr endpoint reference as an XML fragment; must be non-null.
      * @return the service endpoint corresponding to the given endpoint reference; <code>null</code> if the reference
      * cannot be resolved.
      */
     public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
         for (ComponentMBeanImpl connector : getComponents()) {
             ServiceEndpoint se = connector.getComponent().resolveEndpointReference(epr);
             if (se != null) {
                 return new DynamicEndpoint(connector.getComponentNameSpace(), se, epr);  
             }
         }
         ServiceEndpoint se = resolveInternalEPR(epr);
         if (se != null) {
             return se;
         }
         return resolveStandardEPR(epr);
     }
     
     /**
      * <p>
      * Resolve an internal JBI EPR conforming to the format defined in the JBI specification.
      * </p>
      * 
      * <p>The EPR would look like:
      * <pre>
      * <jbi:end-point-reference xmlns:jbi="http://java.sun.com/xml/ns/jbi/end-point-reference"
      *      jbi:end-point-name="endpointName" 
      *      jbi:service-name="foo:serviceName" 
      *      xmlns:foo="urn:FooNamespace"/>
      * </pre>
      * </p>
      * 
      * @author Maciej Szefler m s z e f l e r @ g m a i l . c o m 
      * @param epr EPR fragment
      * @return internal service endpoint corresponding to the EPR, or <code>null</code>
      *         if the EPR is not an internal EPR or if the EPR cannot be resolved
      */
     public ServiceEndpoint resolveInternalEPR(DocumentFragment epr) {
         if (epr == null) {
             throw new NullPointerException("resolveInternalEPR(epr) called with null epr.");
         }
         NodeList nl = epr.getChildNodes();
         for (int i = 0; i < nl.getLength(); ++i) {
             Node n = nl.item(i);
             if (n.getNodeType() != Node.ELEMENT_NODE) {
                 continue;
             }
             Element el = (Element) n;
             // Namespace should be "http://java.sun.com/jbi/end-point-reference"
             if (el.getNamespaceURI() == null 
                     || !el.getNamespaceURI().equals("http://java.sun.com/jbi/end-point-reference")) 
             {
                 continue;
             }
             if (el.getLocalName() == null || !el.getLocalName().equals("end-point-reference")) {
                 continue;
             }
             String serviceName = el.getAttributeNS(el.getNamespaceURI(), "service-name");
             // Now the DOM pain-in-the-you-know-what: we need to come up with QName for this; 
             // fortunately, there is only one place where the xmlns:xxx attribute could be, on 
             // the end-point-reference element!
             QName serviceQName = DOMUtil.createQName(el, serviceName);
             String endpointName = el.getAttributeNS(el.getNamespaceURI(), "end-point-name");
             return getInternalEndpoint(serviceQName, endpointName);
         }
         return null;
     }
     
     /**
      * Resolve a standard EPR understood by ServiceMix container.
      * Currently, the supported syntax is the WSA one, the address uri
      * being parsed with the following possiblities:
      *    jbi:endpoint:service-namespace/service-name/endpoint
      *    jbi:endpoint:service-namespace:service-name:endpoint
      *    
      * The full EPR will look like:
      *   <epr xmlns:wsa="http://www.w3.org/2005/08/addressing">
      *     <wsa:Address>jbi:endpoint:http://foo.bar.com/service/endpoint</wsa:Address>
      *   </epr>
      *   
      * BCs should also be able to resolve such EPR but using their own URI parsing,
      * for example:
      *   <epr xmlns:wsa="http://www.w3.org/2005/08/addressing">
      *     <wsa:Address>http://foo.bar.com/myService?http.soap=true</wsa:Address>
      *   </epr>
      * 
      * or
      *   <epr xmlns:wsa="http://www.w3.org/2005/08/addressing">
      *     <wsa:Address>jms://activemq/queue/FOO.BAR?persistent=true</wsa:Address>
      *   </epr>
      *    
      * Note that the separator should be same as the one used in the namespace
      * depending on the namespace:
      *     http://foo.bar.com  => '/'
      *     urn:foo:bar         => ':' 
      *    
      * The syntax is the same as the one that can be used to specifiy a target
      * for a JBI exchange with the restriction that it only allows the
      * endpoint subprotocol to be used. 
      * 
      * @param epr
      * @return
      */
     public ServiceEndpoint resolveStandardEPR(DocumentFragment epr) {
         try {
             NodeList children = epr.getChildNodes();
             for (int i = 0; i < children.getLength(); ++i) {
                 Node n = children.item(i);
                 if (n.getNodeType() != Node.ELEMENT_NODE) {
                     continue;
                 }
                 Element elem = (Element) n;
                 String[] namespaces = new String[] {WSAddressingConstants.WSA_NAMESPACE_200508,
                                                     WSAddressingConstants.WSA_NAMESPACE_200408,
                                                     WSAddressingConstants.WSA_NAMESPACE_200403,
                                                     WSAddressingConstants.WSA_NAMESPACE_200303 };
                 NodeList nl = null;
                 for (int ns = 0; ns < namespaces.length; ns++) {
                     NodeList tnl = elem.getElementsByTagNameNS(namespaces[ns], WSAddressingConstants.EL_ADDRESS);
                     if (tnl.getLength() == 1) {
                         nl = tnl;
                         break;
                     }
                 }
                 if (nl != null) {
                     Element address = (Element) nl.item(0);
                     String uri = DOMUtil.getElementText(address);
                     if (uri != null) {
                         uri = uri.trim();
                     }
                     if (uri.startsWith("endpoint:")) {
                         uri = uri.substring("endpoint:".length());
                         String[] parts = URIResolver.split3(uri);
                         return getInternalEndpoint(new QName(parts[0], parts[1]), parts[2]);
                     } else if (uri.startsWith("service:")) {
                         uri = uri.substring("service:".length());
                         String[] parts = URIResolver.split2(uri);
                         return getEndpoint(new QName(parts[0], parts[1]), parts[1]);
                     }
                     // TODO should we support interface: and operation: here?
                 }
             }
         } catch (Exception e) {
             LOGGER.debug("Unable to resolve EPR: " + e);
         }
         return null;
     }
 
     /**
      * @param cns
      * @param externalEndpoint the external endpoint to be registered, must be non-null.
      * @throws JBIException 
      */
     public void registerExternalEndpoint(ComponentNameSpace cns, ServiceEndpoint externalEndpoint) throws JBIException {
         if (externalEndpoint != null) {
             endpointRegistry.registerExternalEndpoint(cns, externalEndpoint);
         }
     }
 
     /**
      * @param cns
      * @param externalEndpoint the external endpoint to be deregistered; must be non-null.
      */
     public void deregisterExternalEndpoint(ComponentNameSpace cns, ServiceEndpoint externalEndpoint) {
         endpointRegistry.unregisterExternalEndpoint(cns, externalEndpoint);
     }
 
     /**
      * @param service
      * @param name
      * @return endpoint
      */
     public ServiceEndpoint getEndpoint(QName service, String name) {
         return endpointRegistry.getEndpoint(service, name);
     }
     
     public ServiceEndpoint getInternalEndpoint(QName service, String name) {
         return endpointRegistry.getInternalEndpoint(service, name);
     }
 
     /**
      * @param serviceName
      * @return endpoints
      */
     public ServiceEndpoint[] getEndpointsForService(QName serviceName) {
         return endpointRegistry.getEndpointsForService(serviceName);
     }
 
     /**
      * @param interfaceName
      * @return endpoints
      */
     public ServiceEndpoint[] getExternalEndpoints(QName interfaceName) {
         return endpointRegistry.getExternalEndpointsForInterface(interfaceName);
     }
 
     /**
      * @param serviceName
      * @return endpoints
      */
     public ServiceEndpoint[] getExternalEndpointsForService(QName serviceName) {
         return endpointRegistry.getExternalEndpointsForService(serviceName);
     }
 
     /**
      * REgister a local Component
      * 
      * @param name
      * @param description
      * @param component
      * @param binding
      * @param service
      * @return ComponentConnector
      * @throws JBIException
      */
     public ComponentMBeanImpl registerComponent(ComponentNameSpace name, 
                                                 String description,
                                                 Component component,
                                                 boolean binding, 
                                                 boolean service,
                                                 String[] sharedLibs) throws JBIException {
         return componentRegistry.registerComponent(name, description, component, binding, service, sharedLibs);
     }
 
     /**
      * @param component
      * @return ComponentConnector
      */
     public void deregisterComponent(ComponentMBeanImpl component) {
         componentRegistry.deregisterComponent(component);
     }
 
     /**
      * @return all local ComponentConnectors
      */
     public Collection<ComponentMBeanImpl> getComponents() {
         return componentRegistry.getComponents();
     }
 
     /**
      * Get a Component
      * @param cns
      * @return the Component
      */
     public ComponentMBeanImpl getComponent(ComponentNameSpace cns) {
         return componentRegistry.getComponent(cns);
     }
     
     /**
      * Get a Component
      * @param name
      * @return the Componment
      */
     public ComponentMBeanImpl getComponent(String name) {
         ComponentNameSpace cns = new ComponentNameSpace(container.getName(), name);
         return getComponent(cns);
     }
     
     /**
      * Get a list of all engines currently installed.
      * @return array of JMX object names of all installed SEs.
      */
     public ObjectName[] getEngineComponents() {
         ObjectName[] result = null;
         List<ObjectName> tmpList = new ArrayList<ObjectName>();
         for (ComponentMBeanImpl lcc : getComponents()) {
             if (!lcc.isPojo() && lcc.isService() && lcc.getMBeanName() != null) {
                 tmpList.add(lcc.getMBeanName());
             }
         }
         result = new ObjectName[tmpList.size()];
         tmpList.toArray(result);
         return result;
         
     }
     
     /**
      * Get a list of all binding components currently installed.
      * @return array of JMX object names of all installed BCs.
      */
     public ObjectName[] getBindingComponents() {
         ObjectName[] result = null;
         List<ObjectName> tmpList = new ArrayList<ObjectName>();
         for (ComponentMBeanImpl lcc : getComponents()) {
             if (!lcc.isPojo() && lcc.isBinding() && lcc.getMBeanName() != null) {
                 tmpList.add(lcc.getMBeanName());
             }
         }
         result = new ObjectName[tmpList.size()];
         tmpList.toArray(result);
         return result;
     }
 
     /**
      * Get a list of all pojos currently installed.
      * @return array of JMX object names of all installed PJOJO Conponents.
      */
     public ObjectName[] getPojoComponents() {
         ObjectName[] result = null;
         List<ObjectName> tmpList = new ArrayList<ObjectName>();
         for (ComponentMBeanImpl lcc : getComponents()) {
             if (lcc.isPojo() && lcc.getMBeanName() != null) {
                 tmpList.add(lcc.getMBeanName());
             }
         }
         result = new ObjectName[tmpList.size()];
         tmpList.toArray(result);
         return result;
     }
     
     /**
      * Register All subscriptions
      * @param context 
      * @param as 
      */
     public void registerSubscriptions(ComponentContextImpl context, ActivationSpec as) {
         QName service = as.getService();
         String endpointName = as.getEndpoint();
         InternalEndpoint endpoint = new InternalEndpoint(context.getComponentNameSpace(), endpointName, service);
         SubscriptionSpec[] specs = as.getSubscriptions();
         if (specs != null) {
             for (int i = 0; i < specs.length; i++) {
                 registerSubscription(context, specs[i], endpoint);
             }
         }
     }
     
     /**
      * Deregister All subscriptions
      * @param context
      * @param as
      */
     public void deregisterSubscriptions(ComponentContextImpl context, ActivationSpec as) {
         SubscriptionSpec[] specs = as.getSubscriptions();
         if (specs != null) {
             for (int i = 0; i < specs.length; i++) {
                 deregisterSubscription(context, specs[i]);
             }
         }
     }
     
     /**
      * @param context 
      * @param subscription
      * @param endpoint
      */
     public void registerSubscription(ComponentContextImpl context, SubscriptionSpec subscription, ServiceEndpoint endpoint) {
         InternalEndpoint sei = (InternalEndpoint)endpoint;
         subscription.setName(context.getComponentNameSpace());
         subscriptionRegistry.registerSubscription(subscription, sei);
     }
 
     /**
      * @param context 
      * @param subscription
      * @return the ServiceEndpoint
      */
     public InternalEndpoint deregisterSubscription(ComponentContextImpl context, SubscriptionSpec subscription) {
         subscription.setName(context.getComponentNameSpace());
         return subscriptionRegistry.deregisterSubscription(subscription);
     }
     
     
     /**
      * @param exchange 
      * @return a List of matching endpoints - can return null if no matches
      */
     public List<InternalEndpoint> getMatchingSubscriptionEndpoints(MessageExchangeImpl exchange) {
         return subscriptionRegistry.getMatchingSubscriptionEndpoints(exchange);
     }
     
     /**
      * Register a service assembly
      * @param sa
      * @return true if not already registered
      * @throws DeploymentException 
      */
     public ServiceAssemblyLifeCycle registerServiceAssembly(ServiceAssembly sa,
                                                             ServiceAssemblyEnvironment env) throws DeploymentException {
         return serviceAssemblyRegistry.register(sa, env);
     }
     
     /**
      * Register a service assembly
      * @param sa
      * @return true if not already registered
      * @throws DeploymentException 
      */
     public ServiceAssemblyLifeCycle registerServiceAssembly(ServiceAssembly sa,
                                                             String[] suKeys,
                                                             ServiceAssemblyEnvironment env) throws DeploymentException {
         return serviceAssemblyRegistry.register(sa, suKeys, env);
     }
     
     /**
      * Un-register a service assembly
      * @param saName 
      * @return true if successfully unregistered
      */
     public boolean unregisterServiceAssembly(String saName) {
         return serviceAssemblyRegistry.unregister(saName);
     }
     
     /**
      * Get a named ServiceAssembly
      * @param saName
      * @return the ServiceAssembly or null if it doesn't exist
      */
     public ServiceAssemblyLifeCycle getServiceAssembly(String saName) {
         return serviceAssemblyRegistry.getServiceAssembly(saName);
     }
 
     /**
      * Returns a list of Service Units that are currently deployed to the given component.
      * 
      * @param componentName name of the component.
      * @return List of deployed service units
      */
     public ServiceUnitLifeCycle[] getDeployedServiceUnits(String componentName)  {
         List<ServiceUnitLifeCycle> tmpList = new ArrayList<ServiceUnitLifeCycle>();
         for (ServiceUnitLifeCycle su : serviceUnits.values()) {
             if (su.getComponentName().equals(componentName)) {
                 tmpList.add(su);
             }
         }
         ServiceUnitLifeCycle[] result = new ServiceUnitLifeCycle[tmpList.size()];
         tmpList.toArray(result);
         return result;
     }
 
     /**
      * Return a list of all service units.
      * 
      * @return list of all service units
      */
     public Collection<ServiceUnitLifeCycle> getServiceUnits() {
         return serviceUnits.values();
     }
     
     public Collection<ServiceAssemblyLifeCycle> getServiceAssemblies() {
         return serviceAssemblyRegistry.getServiceAssemblies();
     }
     
     /**
      * Returns a list of Service Assemblies deployed to the JBI enviroment.
      * 
      * @return list of Service Assembly Name's.
      */
     public String[] getDeployedServiceAssemblies()  {
         return serviceAssemblyRegistry.getDeployedServiceAssemblies();
     }
     
     /**
      * Returns a list of Service Assemblies that contain SUs for the given component.
      * 
      * @param componentName name of the component.
      * @return list of Service Assembly names.
      */
     public String[] getDeployedServiceAssembliesForComponent(String componentName)  {
         return serviceAssemblyRegistry.getDeployedServiceAssembliesForComponent(componentName);
     }
 
     /**
      * Returns a list of components(to which SUs are targeted for) in a Service Assembly.
      * 
      * @param saName name of the service assembly.
      * @return list of component names.
      */
     public String[] getComponentsForDeployedServiceAssembly(String saName) {
         return serviceAssemblyRegistry.getComponentsForDeployedServiceAssembly(saName);
     }
 
     /**
      * Returns a boolean value indicating whether the SU is currently deployed.
      * 
      * @param componentName - name of component.
      * @param suName - name of the Service Unit.
      * @return boolean value indicating whether the SU is currently deployed.
      */
     public boolean isSADeployedServiceUnit(String componentName, String suName)  {
         return serviceAssemblyRegistry.isDeployedServiceUnit(componentName, suName);
     }
     
     /**
      * Get a ServiceUnit by its key.
      * 
      * @param suKey the key of the service unit
      * @return the ServiceUnit or null of it doesn't exist
      */
     public ServiceUnitLifeCycle getServiceUnit(String suKey) {
         return serviceUnits.get(suKey);
     }
     
     /**
      * Register a ServiceUnit.
      * 
      * @param su the service unit to register
      * @param saName the service assembly the service unit belongs to
      * @return the service unit key
      */
     public String registerServiceUnit(ServiceUnit su, String saName, File suDir) {
         ServiceUnitLifeCycle sulc = new ServiceUnitLifeCycle(
                 su, 
                 saName, 
                 this,
                 suDir);
         this.serviceUnits.put(sulc.getKey(), sulc);
         try {
             ObjectName objectName = getContainer().getManagementContext().createObjectName(sulc);
             getContainer().getManagementContext().registerMBean(objectName, sulc, ServiceUnitMBean.class);
         } catch (JMException e) {
             LOGGER.error("Could not register MBean for service unit", e);
         }
         return sulc.getKey();
     }
     
     /**
      * Unregister a ServiceUnit by its key.
      * 
      * @param suKey the key of the service unit
      */
     public void unregisterServiceUnit(String suKey) {
         ServiceUnitLifeCycle sulc = this.serviceUnits.remove(suKey);
         if (sulc != null) {
             try {
                 getContainer().getManagementContext().unregisterMBean(sulc);
             } catch (JBIException e) {
                 LOGGER.error("Could not unregister MBean for service unit", e);
             }
         }
     }
     
     public void registerSharedLibrary(org.apache.servicemix.jbi.deployment.SharedLibrary sl,
                                       File installationDir) {
         SharedLibrary library = new SharedLibrary(sl, installationDir);
         this.sharedLibraries.put(library.getName(), library);
         try {
             ObjectName objectName = getContainer().getManagementContext().createObjectName(library);
             getContainer().getManagementContext().registerMBean(objectName, library, SharedLibraryMBean.class);
         } catch (JMException e) {
             LOGGER.error("Could not register MBean for service unit", e);
         }
         checkPendingComponents();
     }
     
     public void unregisterSharedLibrary(String name) {
         // TODO: check for components depending on this library,
         // shutdown them and add them to the list of pending components
         SharedLibrary sl = this.sharedLibraries.remove(name);
         if (sl != null) {
             try {
                 getContainer().getManagementContext().unregisterMBean(sl);
                 sl.dispose();
             } catch (JBIException e) {
                 LOGGER.error("Could not unregister MBean for shared library", e);
             }
         }
     }
     
     public SharedLibrary getSharedLibrary(String name) {
         return sharedLibraries.get(name);
     }
     
     public Collection<SharedLibrary> getSharedLibraries() {
         return sharedLibraries.values();
     }
 
     public void registerEndpointConnection(QName fromSvc, String fromEp, QName toSvc, String toEp, String link) throws JBIException {
         endpointRegistry.registerEndpointConnection(fromSvc, fromEp, toSvc, toEp, link);
     }
 
     public void unregisterEndpointConnection(QName fromSvc, String fromEp) {
         endpointRegistry.unregisterEndpointConnection(fromSvc, fromEp);
     }
     
     public void registerInterfaceConnection(QName fromItf, QName toSvc, String toEp) throws JBIException {
         endpointRegistry.registerInterfaceConnection(fromItf, toSvc, toEp);
     }
 
     public void unregisterInterfaceConnection(QName fromItf) {
         endpointRegistry.unregisterInterfaceConnection(fromItf);
     }
 
     public void registerRemoteEndpoint(ServiceEndpoint endpoint) {
         endpointRegistry.registerRemoteEndpoint((InternalEndpoint) endpoint);
     }
 
     public void unregisterRemoteEndpoint(ServiceEndpoint endpoint) {
         endpointRegistry.unregisterRemoteEndpoint((InternalEndpoint) endpoint);
     }
 
     public void checkPendingAssemblies() {
         executor.execute(new Runnable() {
             public void run() {
                 startPendingAssemblies();
             }
         });
     }
 
     public void addPendingAssembly(ServiceAssemblyLifeCycle sa) {
         if (!pendingAssemblies.contains(sa)) {
             pendingAssemblies.add(sa);
         }
     }
     
     protected synchronized void startPendingAssemblies() {
         for (ServiceAssemblyLifeCycle sa : pendingAssemblies) {
             ServiceUnitLifeCycle[] sus = sa.getDeployedSUs();
             boolean ok = true;
             for (int i = 0; i < sus.length; i++) {
                 ComponentMBeanImpl c = getComponent(sus[i].getComponentName());
                 if (c == null || !c.isStarted()) {
                     ok = false;
                     break;
                 }
             }
             if (ok) {
                 try {
                     sa.restore();
                     pendingAssemblies.remove(sa);
                 } catch (Exception e) {
                     LOGGER.error("Error trying to restore service assembly state", e);
                 }
             }
         }
     }
 
     public void checkPendingComponents() {
         executor.execute(new Runnable() {
             public void run() {
                 startPendingComponents();
             }
         });
     }
 
     public void addPendingComponent(ComponentMBeanImpl comp) {
         if (!pendingComponents.contains(comp)) {
             pendingComponents.add(comp);
         }
     }
     
     protected synchronized void startPendingComponents() {
         for (ComponentMBeanImpl lcc : pendingComponents) {
             // TODO: restore component state if 
         }
     }
 
     public ObjectName[] getComponentNames() {
         List<ObjectName> tmpList = new ArrayList<ObjectName>();
         for (ComponentMBeanImpl lcc : getComponents()) {
             tmpList.add(container.getManagementContext().createObjectName(lcc));
         }
         return tmpList.toArray(new ObjectName[tmpList.size()]);
     }
     
     public ObjectName[] getEndpointNames() {
         List<ObjectName> tmpList = new ArrayList<ObjectName>();
         for (Endpoint ep : container.getRegistry().getEndpointRegistry().getEndpointMBeans()) {
             tmpList.add(container.getManagementContext().createObjectName(ep));
         }
         return tmpList.toArray(new ObjectName[tmpList.size()]);
     }
 
     public ObjectName[] getServiceAssemblyNames() {
         List<ObjectName> tmpList = new ArrayList<ObjectName>();
         for (ServiceAssemblyLifeCycle sa : getServiceAssemblies()) {
             tmpList.add(container.getManagementContext().createObjectName(sa));
         }
         return tmpList.toArray(new ObjectName[tmpList.size()]);
     }
 
     public ObjectName[] getServiceUnitNames() {
         List<ObjectName> tmpList = new ArrayList<ObjectName>();
         for (ServiceUnitLifeCycle su : serviceUnits.values()) {
             tmpList.add(container.getManagementContext().createObjectName(su));
         }
         return tmpList.toArray(new ObjectName[tmpList.size()]);
     }
 
     public ObjectName[] getSharedLibraryNames() {
         List<ObjectName> tmpList = new ArrayList<ObjectName>();
         for (SharedLibrary sl : sharedLibraries.values()) {
             tmpList.add(container.getManagementContext().createObjectName(sl));
         }
         return tmpList.toArray(new ObjectName[tmpList.size()]);
     } 
     
     /**
      * Get an array of MBeanAttributeInfo
      * 
      * @return array of AttributeInfos
      * @throws JMException
      */
     public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
         AttributeInfoHelper helper = new AttributeInfoHelper();
         helper.addAttribute(getObjectToManage(), "componentNames", "list of components");
         helper.addAttribute(getObjectToManage(), "serviceUnitNames", "list of service units");
         helper.addAttribute(getObjectToManage(), "serviceAssemblyNames", "list of service assemblies");
         helper.addAttribute(getObjectToManage(), "endpointNames", "list of endpoints");
         helper.addAttribute(getObjectToManage(), "sharedLibraryNames", "list of shared libraries");
         return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
     }
 
     /**
      * Cancel pending exchanges in all components
      */
     public void cancelPendingExchanges() {
         for (ComponentMBeanImpl mbean : componentRegistry.getComponents()) {
             DeliveryChannelImpl channel = mbean.getDeliveryChannel();
             if (channel != null) {
                 channel.cancelPendingExchanges();
             }
         }      
     }
 
 }
