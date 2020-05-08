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
 package org.apache.geronimo.servicemix;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.Set;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 
 import javax.management.MalformedObjectNameException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.geronimo.common.DeploymentException;
 import org.apache.geronimo.deployment.ConfigurationBuilder;
 import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
 import org.apache.geronimo.deployment.service.EnvironmentBuilder;
 import org.apache.geronimo.deployment.util.DeploymentUtil;
 import org.apache.geronimo.gbean.AbstractName;
 import org.apache.geronimo.gbean.AbstractNameQuery;
 import org.apache.geronimo.gbean.GBeanData;
 import org.apache.geronimo.gbean.GBeanInfo;
 import org.apache.geronimo.gbean.GBeanInfoBuilder;
 import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
 import org.apache.geronimo.kernel.Kernel;
 import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
 import org.apache.geronimo.kernel.config.ConfigurationModuleType;
 import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
 import org.apache.geronimo.kernel.repository.Artifact;
 import org.apache.geronimo.kernel.repository.ArtifactResolver;
 import org.apache.geronimo.kernel.repository.Environment;
 import org.apache.geronimo.kernel.repository.ImportType;
 import org.apache.geronimo.kernel.repository.Repository;
 import org.apache.geronimo.kernel.repository.Version;
 import org.apache.servicemix.jbi.deployment.Descriptor;
 import org.apache.servicemix.jbi.deployment.DescriptorFactory;
 import org.apache.servicemix.jbi.deployment.ServiceUnit;
 import org.apache.servicemix.jbi.deployment.SharedLibraryList;
 
 public class ServiceMixConfigBuilder implements ConfigurationBuilder {
 
     private static final Log log = LogFactory.getLog(ServiceMixConfigBuilder.class);
 
     private final Environment defaultEnvironment;
     private final Collection repositories;
     private final Kernel kernel;
 
     public static final GBeanInfo GBEAN_INFO;
 
     static {
         GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ServiceMixConfigBuilder.class, NameFactory.CONFIG_BUILDER);
         infoFactory.addInterface(ConfigurationBuilder.class);
         infoFactory.addAttribute("defaultEnvironment", Environment.class, true, true);
         infoFactory.addAttribute("kernel", Kernel.class, false);
         infoFactory.addReference("Repositories", Repository.class, "Repository");
         infoFactory.setConstructor(new String[]{"defaultEnvironment", "Repositories", "kernel"});
         GBEAN_INFO = infoFactory.getBeanInfo();
     }
 
     public static GBeanInfo getGBeanInfo() {
         return GBEAN_INFO;
     }
 
     public ServiceMixConfigBuilder(Environment defaultEnvironment,
                                    Collection repositories,
                                    Kernel kernel) {
         this.defaultEnvironment = defaultEnvironment;
         this.repositories = repositories;
         this.kernel = kernel;
     }
 
     /**
      * Builds a deployment plan specific to this builder from a planFile and/or
      * module if this builder can process it.
      * @param planFile the deployment plan to examine; can be null
      * @param module the URL of the module to examine; can be null
      * @return the deployment plan, or null if this builder can not handle the module
      * @throws org.apache.geronimo.common.DeploymentException if there was a problem with the configuration
      */
    public Object getDeploymentPlan(File planFile, JarFile module, ModuleIDBuilder idBuilder) throws DeploymentException {
         log.debug("Checking for ServiceMix deployment.");
         System.err.println("Checking for ServiceMix deployment.");
         if (module == null) {
             return null;
         }
         
         // Check that the jbi descriptor is present
         try {
             URL url = DeploymentUtil.createJarURL(module, "META-INF/jbi.xml");
             Descriptor descriptor = DescriptorFactory.buildDescriptor(url);
             if (descriptor == null) {
             	return null;
             }
             DescriptorFactory.checkDescriptor(descriptor);
             return descriptor;
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
    public Artifact getConfigurationID(Object plan, JarFile module, ModuleIDBuilder idBuilder) throws IOException, DeploymentException {
         Descriptor descriptor = (Descriptor) plan;
         if (descriptor.getComponent() != null) {
             return new Artifact("servicemix-components", descriptor.getComponent().getIdentification().getName(), (Version) null, "car");
         } else if (descriptor.getServiceAssembly() != null) {
             return new Artifact("servicemix-assemblies", descriptor.getServiceAssembly().getIdentification().getName(), (Version) null,  "car");
         } else if (descriptor.getSharedLibrary() != null) {
             return new Artifact("servicemix-libraries", descriptor.getSharedLibrary().getIdentification().getName(), Double.toString(descriptor.getSharedLibrary().getVersion()), "car");
         } else {
             throw new DeploymentException("Unable to construct configuration ID " + module.getName() + ": unrecognized jbi package. Should be a component, assembly or library.");
         }
     }
 
     /**
      * Build a configuration from a local file
      *
      * @param inPlaceDeployment
      * @param configId
      * @param plan
      * @param earFile
      * @param configurationStores
      * @param artifactResolver
      * @param targetConfigurationStore
      * @return the DeploymentContext information
      * @throws IOException if there was a problem reading or writing the files
      * @throws org.apache.geronimo.common.DeploymentException if there was a problem with the configuration
      */
     public DeploymentContext buildConfiguration(
                 boolean inPlaceDeployment, 
                 Artifact configId, 
                 Object plan, 
                 JarFile jarFile, 
                 Collection configurationStores, 
                 ArtifactResolver artifactResolver, 
                 ConfigurationStore targetConfigurationStore) throws IOException, DeploymentException {
         if (plan == null) {
             log.warn("Expected a Descriptor but received null");
             return null;
         }
         if (plan instanceof Descriptor == false) {
             log.warn("Expected a Descriptor but received a " + plan.getClass().getName());
             return null;
         }
         File configurationDir;
         try {
             configurationDir = targetConfigurationStore.createNewConfigurationDir(configId);
         } catch (ConfigurationAlreadyExistsException e) {
             throw new DeploymentException(e);
         }
 
         Environment environment = new Environment(); 
         environment.setConfigId(configId);
         EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
         
         DeploymentContext context = null;
         try {
             Descriptor descriptor = (Descriptor) plan;
             context = new DeploymentContext(
                             configurationDir, 
                             inPlaceDeployment ? DeploymentUtil.toFile(jarFile) : null,
                             environment,
                             ConfigurationModuleType.SERVICE, 
                             kernel.getNaming(),
                            ConfigurationUtil.getConfigurationManager(kernel),
                            repositories);
             if (descriptor.getComponent() != null) {
             	buildComponent(descriptor, context, jarFile);
             } else if (descriptor.getServiceAssembly() != null) {
             	buildServiceAssembly(descriptor, context, jarFile);
             } else if (descriptor.getSharedLibrary() != null) {
             	buildSharedLibrary(descriptor, context, jarFile);
             } else {
             	throw new IllegalStateException("Invalid jbi descriptor");
             }
         } catch (Exception e) {
             if (context != null) {
                 context.close();
             }
             DeploymentUtil.recursiveDelete(configurationDir);
             throw new DeploymentException("Unable to deploy", e);
         }
         
         return context;
     }
 
     protected void buildComponent(Descriptor descriptor, DeploymentContext context, JarFile module) throws Exception {
         Environment environment = context.getConfiguration().getEnvironment();
     	// Unzip the component
     	File targetDir = new File(context.getBaseDir(), "install");
     	targetDir.mkdirs();
     	unzip(context, module, new URI("install/"));
     	// Create workspace dir
     	File workDir = new File(context.getBaseDir(), "workspace");
     	workDir.mkdirs();
     	// Create the bootstrap and perform installation
     	// TODO:  Create the bootstrap and perform installation
     	// Add classpath entries
     	if ("self-first".equals(descriptor.getComponent().getComponentClassLoaderDelegation())) {
             context.getConfiguration().getEnvironment().setInverseClassLoading(true);
     	}
     	SharedLibraryList[] slList = descriptor.getComponent().getSharedLibraries();
     	if (slList != null) {
     		for (int i = 0; i < slList.length; i++) {
                 Artifact sl = new Artifact("servicemix-libraries", slList[i].getName(), slList[i].getVersion(), "car");
                 environment.addDependency(sl, ImportType.CLASSES);
 			}
     	}
     	if (descriptor.getComponent().getComponentClassPath() != null) {
 	    	String[] pathElements = descriptor.getComponent().getComponentClassPath().getPathElements();
 	    	if (pathElements != null) {
 		    	for (int i = 0; i < pathElements.length; i++) {
 		    		// We can not add includes directly, so move the file and include it
 		    		File include = new File(targetDir, pathElements[i]);
 		    		File temp = new File(workDir, pathElements[i]);
 		    		if (!include.isFile()) {
 		    			throw new Exception("Classpath element '" + pathElements[i] + "' not found");
 		    		}
 		    		temp.getParentFile().mkdirs();
 		    		include.renameTo(temp);
 		    		context.addInclude(new URI("install/").resolve(pathElements[i]), temp);
 		    		temp.delete();
 				}
 	    	}
     	}
         // Create the JBI deployment managed object
         Properties props = new Properties();
         props.put("jbiType", "JBIComponent");
         props.put("name", descriptor.getComponent().getIdentification().getName());
         AbstractName name = new AbstractName(environment.getConfigId(), props);
         GBeanData gbeanData = new GBeanData(name, Component.GBEAN_INFO);
         gbeanData.setAttribute("name", descriptor.getComponent().getIdentification().getName());
         gbeanData.setAttribute("description", descriptor.getComponent().getIdentification().getDescription());
         gbeanData.setAttribute("type", descriptor.getComponent().getType());
         gbeanData.setAttribute("className", descriptor.getComponent().getComponentClassName());
         gbeanData.setReferencePattern("container", getContainerObjectName());
         context.addGBean(gbeanData);
     }
     
     protected void buildServiceAssembly(Descriptor descriptor, DeploymentContext context, JarFile module) throws Exception {
         Environment environment = context.getConfiguration().getEnvironment();
     	// Unzip the component
     	File targetDir = new File(context.getBaseDir(), "install");
     	targetDir.mkdirs();
     	unzip(context, module, new URI("install/"));
     	// Unzip SUs
     	ServiceUnit[] sus = descriptor.getServiceAssembly().getServiceUnits();
     	for (int i = 0; i < sus.length; i++) {
 			String name = sus[i].getIdentification().getName();
 			String zip  = sus[i].getTarget().getArtifactsZip();
 			String comp = sus[i].getTarget().getComponentName();
 			unzip(context, new JarFile(new File(targetDir, zip)), new URI("sus/" + comp + "/" + name + "/"));
 	        // Deploy the SU on the component
 			// TODO: deploy
 			// Add component config as a dependency
             Artifact sl = new Artifact("servicemix-components", comp, (Version) null, "car");
             environment.addDependency(sl, ImportType.ALL);
 		}
         // Create the JBI deployment managed object
         Properties props = new Properties();
         props.put("jbiType", "JBIServiceAssembly");
         props.put("name", descriptor.getServiceAssembly().getIdentification().getName());
         AbstractName name = new AbstractName(environment.getConfigId(), props);
         GBeanData gbeanData = new GBeanData(name, ServiceAssembly.GBEAN_INFO);
         gbeanData.setAttribute("name", descriptor.getServiceAssembly().getIdentification().getName());
         gbeanData.setReferencePattern("container", getContainerObjectName());
     	for (int i = 0; i < sus.length; i++) {
 			String comp = sus[i].getTarget().getComponentName();
             gbeanData.addDependency(getComponentName(comp));
     	}
         context.addGBean(gbeanData);
     }
     
     protected void buildSharedLibrary(Descriptor descriptor, DeploymentContext context, JarFile module) throws Exception {
     	throw new UnsupportedOperationException("Not implemented");
     }
     
     protected void unzip(DeploymentContext context, JarFile module, URI targetUri) throws IOException {
         Enumeration entries = module.entries();
         while (entries.hasMoreElements()) {
             ZipEntry entry = (ZipEntry) entries.nextElement();
             URI target = targetUri.resolve(entry.getName());
             context.addFile(target, module, entry);
         }
     }
     
     protected AbstractName getContainerObjectName() {
         AbstractNameQuery query = new AbstractNameQuery(Container.class.getName());
         Set names = kernel.listGBeans(query);
         return (AbstractName) names.iterator().next();
     }
     
     protected AbstractNameQuery getComponentName(String name) throws MalformedObjectNameException {
         URI uri = URI.create("servicemix-components/" + name + "//car?jbiType=JBIComponent");
         return new AbstractNameQuery(uri);
     }
 
 }
