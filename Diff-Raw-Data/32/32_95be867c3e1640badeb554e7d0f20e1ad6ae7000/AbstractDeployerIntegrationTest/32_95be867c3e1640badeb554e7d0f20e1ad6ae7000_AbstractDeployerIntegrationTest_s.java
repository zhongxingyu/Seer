 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.deployer.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 
 import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
 import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;
 import org.eclipse.virgo.test.framework.dmkernel.DmKernelTestRunner;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.runner.RunWith;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.Version;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.packageadmin.PackageAdmin;
 
 @RunWith(DmKernelTestRunner.class)
 @SuppressWarnings("deprecation")
 public abstract class AbstractDeployerIntegrationTest {
 
     protected final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
 
     protected volatile OsgiFramework framework;
 
     protected volatile ApplicationDeployer deployer;
 
     protected volatile BundleContext kernelContext;
 
     protected volatile PackageAdmin packageAdmin;
 
     @Before
     public void setup() {
         ServiceReference<OsgiFramework> osgiFrameworkServiceReference = context.getServiceReference(OsgiFramework.class);
         if (osgiFrameworkServiceReference != null) {
             this.framework = context.getService(osgiFrameworkServiceReference);
         }
 
         ServiceReference<ApplicationDeployer> applicationDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
         if (applicationDeployerServiceReference != null) {
             this.deployer = this.context.getService(applicationDeployerServiceReference);
         }
 
         this.kernelContext = getKernelContext();
 
         ServiceReference<PackageAdmin> packageAdminServiceReference = context.getServiceReference(PackageAdmin.class);
         if (packageAdminServiceReference != null) {
             this.packageAdmin = context.getService(packageAdminServiceReference);
         }
     }
     
     private BundleContext getKernelContext() {
         return this.context.getBundle(0L).getBundleContext();
     }
 
     @BeforeClass
     public static void awaitKernelStartup() throws Exception {
         MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        int sleepCount = 600;
         while (!"STARTED".equals(platformMBeanServer.getAttribute(new ObjectName("org.eclipse.virgo.kernel:type=KernelStatus"), "Status"))) {
             Thread.sleep(50);
             if (--sleepCount == 0)
                 break;
         }
         assertFalse("Waited for Kernel too long.", sleepCount == 0);
     }
 
     protected static void assertDeploymentIdentityEquals(DeploymentIdentity deploymentIdentity, String name, String type, String symbolicName,
         String version) {
         String header = String.format("DeploymentIdentity('%s').", name);
 
         assertEquals(header + "type is incorrect", type, deploymentIdentity.getType());
         assertEquals(header + "symbolicName is incorrect", symbolicName, deploymentIdentity.getSymbolicName());
         assertEquals(header + "version is incorrect", new Version(version), new Version(deploymentIdentity.getVersion()));
     }
 
     protected Configuration getConfiguration(String pid) throws IOException, InvalidSyntaxException {
         ServiceReference<?> serviceReference = this.context.getServiceReference(ConfigurationAdmin.class.getName());
         ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) this.context.getService(serviceReference);
         try {
             Configuration[] listConfigurations = configurationAdmin.listConfigurations(null);
 
             Configuration match = null;
 
             for (Configuration configuration : listConfigurations) {
                 if (pid.equals(configuration.getPid())) {
                     match = configuration;
                 }
             }
             return match;
         } finally {
             this.context.ungetService(serviceReference);
         }
     }
 
     protected Bundle getBundle(String symbolicName, Version version) {
         Bundle[] bundles = this.context.getBundles();
         for (Bundle bundle : bundles) {
             if (symbolicName.equals(bundle.getSymbolicName()) && version.equals(bundle.getVersion())) {
                 return bundle;
             }
         }
         return null;
     }
 }
