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
 package org.apache.servicemix.gbean;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.geronimo.common.DeploymentException;
 import org.apache.geronimo.deployment.ConfigurationBuilder;
 import org.apache.geronimo.deployment.DeploymentContext;
 import org.apache.geronimo.deployment.util.DeploymentUtil;
 import org.apache.geronimo.gbean.GBeanData;
 import org.apache.geronimo.gbean.GBeanInfo;
 import org.apache.geronimo.gbean.GBeanInfoBuilder;
 import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
 import org.apache.geronimo.kernel.Kernel;
 import org.apache.geronimo.kernel.config.ConfigurationData;
 import org.apache.geronimo.kernel.config.ConfigurationModuleType;
 import org.apache.servicemix.jbi.config.spring.XBeanProcessor;
 import org.apache.servicemix.jbi.deployment.Descriptor;
 import org.springframework.core.io.UrlResource;
 import org.apache.xbean.spring.context.ResourceXmlApplicationContext;
 
 import javax.management.ObjectName;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.jar.JarFile;
 
 public class ServiceMixConfigBuilder implements ConfigurationBuilder {
 
     private static final Log log = LogFactory.getLog(ServiceMixConfigBuilder.class);
 
     private final List defaultParentId;
     private final Kernel kernel;
     private final ServiceMixContainer servicemix;
 
     public static final GBeanInfo GBEAN_INFO;
 
     static {
         GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ServiceMixConfigBuilder.class, NameFactory.CONFIG_BUILDER);
         infoFactory.addInterface(ConfigurationBuilder.class);
         infoFactory.addAttribute("defaultParentId", List.class, true);
         infoFactory.addReference("servicemix", ServiceMixContainer.class);
         infoFactory.addAttribute("kernel", Kernel.class, false);
         infoFactory.setConstructor(new String[]{"defaultParentId", "kernel", "servicemix"});
         GBEAN_INFO = infoFactory.getBeanInfo();
     }
 
     public static GBeanInfo getGBeanInfo() {
         return GBEAN_INFO;
     }
 
     public ServiceMixConfigBuilder(List defaultParentId, Kernel kernel, ServiceMixContainer servicemix) {
         this.defaultParentId = defaultParentId;
         this.kernel = kernel;
         this.servicemix = servicemix;
     }
 
     /**
      * Builds a deployment plan specific to this builder from a planFile and/or
      * module if this builder can process it.
      * @param planFile the deployment plan to examine; can be null
      * @param module the URL of the module to examine; can be null
      * @return the deployment plan, or null if this builder can not handle the module
      * @throws org.apache.geronimo.common.DeploymentException if there was a problem with the configuration
      */
     public Object getDeploymentPlan(File planFile, JarFile module) throws DeploymentException {
         log.debug("Checking for ServiceMix deployment.");
         System.err.println("Checking for ServiceMix deployment.");
         if (module == null) {
             return null;
         }
         
         // Check that the jbi descriptor is present
         try {
             URL url = DeploymentUtil.createJarURL(module, "META-INF/jbi.xml");
             Descriptor descriptor = buildDescriptor(url);
             if (descriptor != null) {
                 return descriptor;
             }
             return null;
         } catch (Exception e) {
             log.debug("Not a ServiceMix deployment: no jbi.xml found.", e);
             //no jbi.xml, not for us
             return null;
         }
     }
 
     /**
      * Checks what configuration URL will be used for the provided module.
      * @param plan the deployment plan
      * @param module the module to build
      * @return the ID that will be used for the Configuration
      * @throws IOException if there was a problem reading or writing the files
      * @throws org.apache.geronimo.common.DeploymentException if there was a problem with the configuration
      */
     public URI getConfigurationID(Object plan, JarFile module) throws IOException, DeploymentException {
         try {
             Descriptor descriptor = (Descriptor) plan;
             if (descriptor.getComponent() != null) {
                 return new URI("org/apache/servicemix/components/" + descriptor.getComponent().getIdentification().getName());
             } else if (descriptor.getServiceAssembly() != null) {
                 return new URI("org/apache/servicemix/assemblies/" + descriptor.getServiceAssembly().getIdentification().getName());
             } else if (descriptor.getSharedLibrary() != null) {
                 return new URI("org/apache/servicemix/libraries/" + descriptor.getSharedLibrary().getIdentification().getName());
             } else {
                 throw new DeploymentException("Unable to construct configuration ID " + module.getName() + ": unrecognized jbi package. Should be a component, assembly or library.");
             }
         } catch (URISyntaxException e) {
             throw new DeploymentException("Unable to construct configuration ID " + module.getName(), e);
         }
     }
 
     /**
      * Build a configuration from a local file
      *
      * @param plan the deployment plan
      * @param module the module to build
      * @param outfile the file in which the configiguration files should be written
      * @return the Configuration information
      * @throws IOException if there was a problem reading or writing the files
      * @throws org.apache.geronimo.common.DeploymentException if there was a problem with the configuration
      */
     public ConfigurationData buildConfiguration(Object plan, JarFile module, File outfile) throws IOException, DeploymentException {
         if (plan == null) {
             log.warn("Expected a Descriptor but received null");
             return null;
         }
         if (plan instanceof Descriptor == false) {
             log.warn("Expected a Descriptor but received a " + plan.getClass().getName());
             return null;
         }
         
         DeploymentContext context = null;
         try {
             URI configId = getConfigurationID(plan, module);
             List parentId = new ArrayList();
            if (parentId != null) {
                 for (Iterator iter = defaultParentId.iterator(); iter.hasNext();) {
                     String element = (String) iter.next();
                     parentId.add(new URI(element));
                 }
             }
             context = new DeploymentContext(outfile, configId, ConfigurationModuleType.SERVICE, parentId, null, null, kernel);
             
             // Create the JBI deployment managed object
             Properties props = new Properties();
             props.put("jbiType", "JBIModule");
             props.put("name", configId.toString());
             ObjectName name = ObjectName.getInstance(context.getDomain(), props);
             GBeanData gbeanData = new GBeanData(name, ServiceMixDeployment.GBEAN_INFO);
             gbeanData.setAttribute("name", module.getName());
             context.addGBean(gbeanData);
 
             try {
                 servicemix.getContainer().getAdminCommandsService().installArchive(module.getName());
             } catch (javax.jbi.management.DeploymentException e) {
                 throw new DeploymentException("Could not deploy jbi package", e);
             }
         } catch (Exception e) {
             throw new DeploymentException("Unable to deploy", e);
         } finally {
             if (context != null) {
                 context.close();
             }
         }
         
         return context.getConfigurationData();
     }
 
     /**
      * Build a Descriptor from a file archieve
      * 
      * @param tmpDir
      * @return the Descriptor object
      */
     protected static Descriptor buildDescriptor(URL url) {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         try {
             Thread.currentThread().setContextClassLoader(Descriptor.class.getClassLoader());
             ResourceXmlApplicationContext context = new ResourceXmlApplicationContext(
                     new UrlResource(url),
                     Arrays.asList(new Object[] { new XBeanProcessor()}));
             return (Descriptor) context.getBean("jbi");
         } finally {
             Thread.currentThread().setContextClassLoader(cl);
         }
     }
 
 }
