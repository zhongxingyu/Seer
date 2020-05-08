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
 package org.apache.servicemix.soap;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 
 import javax.jbi.component.ComponentContext;
 import javax.jbi.messaging.MessageExchange.Role;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import javax.wsdl.Definition;
 import javax.wsdl.factory.WSDLFactory;
 import javax.wsdl.xml.WSDLReader;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.servicemix.common.Endpoint;
 import org.apache.servicemix.common.ExchangeProcessor;
 import org.apache.servicemix.common.wsdl1.JbiExtension;
 import org.apache.servicemix.common.xbean.XBeanServiceUnit;
 import org.springframework.core.io.Resource;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 
 public abstract class SoapEndpoint extends Endpoint {
 
     protected ServiceEndpoint activated;
     protected ExchangeProcessor processor;
     protected Role role;
     protected URI defaultMep;
     protected boolean soap;
     protected String soapVersion;
     protected Resource wsdlResource;
     protected QName defaultOperation;
     protected QName targetInterfaceName;
     protected QName targetService;
     protected String targetEndpoint;
     
     /**
      * @return Returns the defaultMep.
      */
     public URI getDefaultMep() {
         return defaultMep;
     }
     /**
      * @param defaultMep The defaultMep to set.
      */
     public void setDefaultMep(URI defaultMep) {
         this.defaultMep = defaultMep;
     }
     /**
      * @return Returns the defaultOperation.
      */
     public QName getDefaultOperation() {
         return defaultOperation;
     }
     /**
      * @param defaultOperation The defaultOperation to set.
      */
     public void setDefaultOperation(QName defaultOperation) {
         this.defaultOperation = defaultOperation;
     }
     /**
      * @return Returns the role.
      */
     public Role getRole() {
         return role;
     }
     /**
      * @param role The role to set.
      */
     public void setRole(Role role) {
         this.role = role;
     }
     /**
      * @return Returns the soap.
      */
     public boolean isSoap() {
         return soap;
     }
     /**
      * @param soap The soap to set.
      */
     public void setSoap(boolean soap) {
         this.soap = soap;
     }
     /**
      * @return Returns the soapVersion.
      */
     public String getSoapVersion() {
         return soapVersion;
     }
     /**
      * @param soapVersion The soapVersion to set.
      */
     public void setSoapVersion(String soapVersion) {
         this.soapVersion = soapVersion;
     }
     /**
      * @return Returns the targetEndpoint.
      */
     public String getTargetEndpoint() {
         return targetEndpoint;
     }
     /**
      * @param targetEndpoint The targetEndpoint to set.
      */
     public void setTargetEndpoint(String targetEndpoint) {
         this.targetEndpoint = targetEndpoint;
     }
     /**
      * @return Returns the targetInterfaceName.
      */
     public QName getTargetInterfaceName() {
         return targetInterfaceName;
     }
     /**
      * @param targetInterfaceName The targetInterfaceName to set.
      */
     public void setTargetInterfaceName(QName targetInterfaceName) {
         this.targetInterfaceName = targetInterfaceName;
     }
     /**
      * @return Returns the targetServiceName.
      */
     public QName getTargetService() {
         return targetService;
     }
     /**
      * @param targetServiceName The targetServiceName to set.
      */
     public void setTargetService(QName targetServiceName) {
         this.targetService = targetServiceName;
     }
     /**
      * @return Returns the wsdlResource.
      */
     public Resource getWsdlResource() {
         return wsdlResource;
     }
     /**
      * @param wsdlResource The wsdlResource to set.
      */
     public void setWsdlResource(Resource wsdlResource) {
         this.wsdlResource = wsdlResource;
     }
     /**
      * @org.apache.xbean.Property alias="role"
      * @param role
      */
     public void setRoleAsString(String role) {
         if (role == null) {
             throw new IllegalArgumentException("Role must be specified");
         } else if (JbiExtension.ROLE_CONSUMER.equals(role)) {
             setRole(Role.CONSUMER);
         } else if (JbiExtension.ROLE_PROVIDER.equals(role)) {
             setRole(Role.PROVIDER);
         } else {
             throw new IllegalArgumentException("Unrecognized role: " + role);
         }
     }
 
     /**
      * Load the wsdl for this endpoint.
      */
     protected void loadWsdl() {
         // Load WSDL from the resource
         if (description == null && wsdlResource != null) {
             InputStream is = null;
             ClassLoader cl = Thread.currentThread().getContextClassLoader();
             try {
                 if (serviceUnit instanceof XBeanServiceUnit) {
                     XBeanServiceUnit su = (XBeanServiceUnit) serviceUnit;
                     Thread.currentThread().setContextClassLoader(su.getKernel().getClassLoaderFor(su.getConfiguration()));
                 }
                 is = wsdlResource.getInputStream();
                 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                 dbf.setNamespaceAware(true);
                 Definition def = WSDLFactory.newInstance().newWSDLReader().readWSDL(null, new InputSource(is));
                 overrideDefinition(def);
             } catch (Exception e) {
                 logger.warn("Could not load description from resource", e);
             } finally {
                 Thread.currentThread().setContextClassLoader(cl);
                 if (is != null) {
                     try {
                         is.close();
                     } catch (IOException e) {
                         // Ignore
                     }
                 }
             }
         }
         // If the endpoint is a consumer, try to find
         // the proxied endpoint description
         if (description == null && definition == null && getRole() == Role.CONSUMER) {
             retrieveProxiedEndpointDefinition();
         }
         // If the wsdl definition is provided,
         // convert it to a DOM document
         if (description == null && definition != null) {
             try {
                 description = WSDLFactory.newInstance().newWSDLWriter().getDocument(definition);
             } catch (Exception e) {
                 logger.warn("Could not create document from wsdl description", e);
             }
         }
         // If the dom description is provided
         // convert it to a WSDL definition
         if (definition == null && description != null) {
             try {
                 definition = WSDLFactory.newInstance().newWSDLReader().readWSDL(null, description);
             } catch (Exception e) {
                 logger.warn("Could not create wsdl definition from dom document", e);
             }
         }
     }
 
     /**
      * Create a wsdl definition for a consumer endpoint.
      * Loads the target endpoint definition and add http binding
      * informations to it.
      */
     protected void retrieveProxiedEndpointDefinition() {
         try {
             ComponentContext ctx = this.serviceUnit.getComponent().getComponentContext();
             ServiceEndpoint ep = null;
             if (targetService != null && targetEndpoint != null) {
                 ep = ctx.getEndpoint(targetService, targetEndpoint);
             }
             if (ep == null && targetService != null) {
                 ServiceEndpoint[] eps = ctx.getEndpointsForService(targetService);
                 if (eps != null && eps.length > 0) {
                     ep = eps[0];
                 }
             }
             if (ep == null && targetInterfaceName != null) {
                 ServiceEndpoint[] eps = ctx.getEndpoints(targetInterfaceName);
                 if (eps != null && eps.length > 0) {
                     ep = eps[0];
                 }
             }
             if (ep == null && service != null && endpoint != null) {
                 ep = ctx.getEndpoint(service, endpoint);
             }
             if (ep != null) {
                 Document doc = ctx.getEndpointDescriptor(ep);
                 if (doc != null) {
                     WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
                     Definition def = reader.readWSDL(null, doc);
                     if (def != null) {
                         overrideDefinition(def);
                     }
                 }
             }
         } catch (Exception e) {
             logger.debug("Unable to retrieve target endpoint descriptor", e);
         }
     }
     
     /* (non-Javadoc)
      * @see org.apache.servicemix.common.Endpoint#getProcessor()
      */
     public ExchangeProcessor getProcessor() {
         return this.processor;
     }
 
     /* (non-Javadoc)
      * @see org.servicemix.common.Endpoint#activate()
      */
     public void activate() throws Exception {
         ComponentContext ctx = this.serviceUnit.getComponent().getComponentContext();
         loadWsdl();
         if (getRole() == Role.PROVIDER) {
             activated = ctx.activateEndpoint(service, endpoint);
             processor = createProviderProcessor();
         } else {
             activated = createExternalEndpoint();
             ctx.registerExternalEndpoint(activated);
             processor = createConsumerProcessor();
         }
         processor.start();
     }
 
     /* (non-Javadoc)
      * @see org.servicemix.common.Endpoint#deactivate()
      */
     public void deactivate() throws Exception {
         ComponentContext ctx = this.serviceUnit.getComponent().getComponentContext();
         if (getRole() == Role.PROVIDER) {
             ServiceEndpoint ep = activated;
             activated = null;
             ctx.deactivateEndpoint(ep);
         } else {
             ServiceEndpoint ep = activated;
             activated = null;
             ctx.deregisterExternalEndpoint(ep);
         }
         processor.stop();
     }
 
    protected abstract void overrideDefinition(Definition def);
     
     protected abstract ExchangeProcessor createProviderProcessor();
     
     protected abstract ExchangeProcessor createConsumerProcessor();
     
     protected abstract ServiceEndpoint createExternalEndpoint();
     
 }
