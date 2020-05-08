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
 package org.apache.servicemix.geronimo;
 
 import java.io.File;
 import java.util.Collection;
 
 import javax.jbi.JBIException;
import javax.resource.spi.work.WorkManager;
 import javax.transaction.TransactionManager;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.geronimo.gbean.GBeanInfo;
 import org.apache.geronimo.gbean.GBeanInfoBuilder;
 import org.apache.geronimo.gbean.GBeanLifecycle;
 import org.apache.geronimo.kernel.Kernel;
 import org.apache.geronimo.transaction.context.GeronimoTransactionManager;
 import org.apache.geronimo.transaction.context.TransactionContextManager;
 import org.apache.servicemix.jbi.container.ComponentEnvironment;
 import org.apache.servicemix.jbi.container.JBIContainer;
 import org.apache.servicemix.jbi.container.ServiceAssemblyEnvironment;
 import org.apache.servicemix.jbi.framework.ComponentContextImpl;
 import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
 import org.apache.servicemix.jbi.framework.ComponentNameSpace;
 import org.apache.servicemix.jbi.framework.ServiceAssemblyLifeCycle;
 
 public class ServiceMixGBean implements GBeanLifecycle, Container {
 
     private Log log = LogFactory.getLog(getClass().getName());
     
     private JBIContainer container;
     private String name;
     private String directory;
     private TransactionContextManager transactionContextManager;
    private WorkManager workManager;
     private Kernel kernel;
     private Collection jndiResources;
 
     public static final GBeanInfo GBEAN_INFO;
 
     static {
         GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("ServiceMix JBI Container", ServiceMixGBean.class, "JBIContainer");
         infoFactory.addInterface(Container.class);
         infoFactory.addAttribute("name", String.class, true);
         infoFactory.addAttribute("directory", String.class, true);
         infoFactory.addReference("transactionContextManager", TransactionContextManager.class);
        infoFactory.addReference("workManager", WorkManager.class);
         infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.setConstructor(new String[]{"name", "directory", "transactionContextManager", "workManager", "kernel"});
         GBEAN_INFO = infoFactory.getBeanInfo();
     }
 
     public static GBeanInfo getGBeanInfo() {
         return GBEAN_INFO;
     }
 
     public ServiceMixGBean(String name, 
     					   String directory, 
     					   TransactionContextManager transactionContextManager, 
    					   WorkManager workManager,
     					   Kernel kernel) {
         this.name = name;
         this.directory = directory;
         this.transactionContextManager = transactionContextManager;
        this.workManager = workManager;
         this.kernel = kernel;
         if (log.isDebugEnabled()) {
             log.debug("ServiceMixGBean created");
         }
         /*
         // Print available jndi resources
         Set patterns = new HashSet();
         this.jndiResources = kernel.listGBeans(patterns);
         for (Iterator it = jndiResources.iterator(); it.hasNext();) {
         	ObjectName name = (ObjectName) it.next();
         	log.info("Resource name: " + name);
         	log.info("Resource inst: " + kernel.)
         }
         */
     }
     
     /**
      * Starts the GBean.  This informs the GBean that it is about to transition to the running state.
      *
      * @throws Exception if the target failed to start; this will cause a transition to the failed state
      */
     public void doStart() throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("ServiceMixGBean doStart");
         }
         ClassLoader old = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(ServiceMixGBean.class.getClassLoader());
         try {
             if (container == null) {
                 container = createContainer();
                 container.init();
                 container.start();
             }
         } finally {
             Thread.currentThread().setContextClassLoader(old);
         }
     }
 
     /**
      * Stops the target.  This informs the GBean that it is about to transition to the stopped state.
      *
      * @throws Exception if the target failed to stop; this will cause a transition to the failed state
      */
     public void doStop() throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("ServiceMixGBean doStop");
         }
         try {
             if (container != null) {
                 container.shutDown();
             }
         } finally {
             container = null;
         }
     }
 
     /**
      * Fails the GBean.  This informs the GBean that it is about to transition to the failed state.
      */
     public void doFail() {
         if (log.isDebugEnabled()) {
             log.debug("ServiceMixGBean doFail");
         }
         try {
             if (container != null) {
                 try {
                     container.shutDown();
                 }
                 catch (JBIException e) {
                     log.info("Caught while closing due to failure: " + e, e);
                 }
             }
         } finally {
             container = null;
         }
     }
 
     private JBIContainer createContainer() {
         JBIContainer container = new JBIContainer();
         container.setName(name);
         container.setRootDir(directory);
         container.setTransactionManager(getTransactionManager());
         container.setMonitorInstallationDirectory(false);
         container.setMonitorDeploymentDirectory(false);
        container.setWorkManager(workManager);
         return container;
     }
     
     public TransactionManager getTransactionManager() {
         if (transactionContextManager != null) {
             return new GeronimoTransactionManager(transactionContextManager);
         }
         return null;
     }
     
     public void register(Component component) throws Exception {
         ComponentNameSpace cns = new ComponentNameSpace(container.getName(), component.getName());
     	ComponentContextImpl context = new ComponentContextImpl(container, cns);
         ComponentEnvironment env = new ComponentEnvironment();
         env.setComponentRoot(new File(component.getRootDir()));
         env.setInstallRoot(new File(component.getInstallDir()));
         env.setWorkspaceRoot(new File(component.getWorkDir()));
         context.setEnvironment(env);
         
         container.activateComponent(null,
         							component.getComponent(),
         							component.getDescription(),
         							context,
         							component.getType().equals("binding-component"),
         							component.getType().equals("service-engine"),
                                     null);
         ComponentMBeanImpl cmb = container.getComponent(component.getName());
         File stateFile = cmb.getContext().getEnvironment().getStateFile();
         if (stateFile.isFile()) {
         	cmb.setInitialRunningState();
         } else {
         	cmb.start();
         }
     }
 
     public void unregister(Component component) throws Exception {
     	container.deactivateComponent(component.getName());
     }
     
     public void register(ServiceAssembly assembly) throws Exception {
     	File rootDir = new File(assembly.getRootDir());
     	ServiceAssemblyEnvironment env = new ServiceAssemblyEnvironment();
     	env.setRootDir(rootDir);
     	env.setInstallDir(new File(rootDir, "install"));
     	env.setSusDir(new File(rootDir, "sus"));
     	env.setStateFile(new File(rootDir, "state.xml"));
     	ServiceAssemblyLifeCycle salc = container.getRegistry().registerServiceAssembly(assembly.getDescriptor().getServiceAssembly(), env);
     	if (env.getStateFile().isFile()) {
     		salc.restore();
     	} else {
     		salc.start();
     	}
     }
     
     public void unregister(ServiceAssembly assembly) throws Exception {
     	ServiceAssemblyLifeCycle salc = container.getRegistry().getServiceAssembly(assembly.getName());
     	salc.shutDown(false);
     	container.getRegistry().unregisterServiceAssembly(assembly.getName());
     }
 
 }
