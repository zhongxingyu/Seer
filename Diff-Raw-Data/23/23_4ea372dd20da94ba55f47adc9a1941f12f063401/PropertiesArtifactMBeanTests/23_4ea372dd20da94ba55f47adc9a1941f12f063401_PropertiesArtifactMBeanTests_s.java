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
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.net.URI;
 
 import javax.management.InstanceNotFoundException;
 import javax.management.IntrospectionException;
 import javax.management.JMException;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 
 import org.junit.Test;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.service.cm.Configuration;
 
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
 import org.eclipse.virgo.util.io.FileCopyUtils;
 
 
 /**
  */
 public class PropertiesArtifactMBeanTests extends AbstractDeployerIntegrationTest {
     
     private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
     
     private final ObjectName objectName;    
     {
         try {
            objectName = new ObjectName("org.eclipse.virgo.kernel:type=Model,artifact-type=configuration,name=test,version=0.0.0");
         } catch (JMException jme) {
             throw new RuntimeException(jme);
         }
     }
     
     @Test
     public void lifecycle() throws DeploymentException, IOException, InvalidSyntaxException, JMException {
         File artifactFile = new File("target/test.properties");
         
         FileCopyUtils.copy(new File("src/test/resources/test.properties"), artifactFile);
         
         URI artifactUri = artifactFile.toURI();
         
         this.deployer.deploy(artifactUri);
         
         assertConfigurationPresentAndCorrect("foo", "bar");
         
         mBeanServer.invoke(objectName, "stop", null, null);
         
         assertConfigurationNotPresent();
         
         mBeanServer.invoke(objectName, "start", null, null);
         
         assertConfigurationPresentAndCorrect("foo", "bar");
         
         mBeanServer.invoke(objectName, "uninstall", null, null);
         
         assertConfigurationNotPresent();
         assertMBeanNotPresent();
         assertFalse(this.deployer.isDeployed(artifactUri));
     }
     
     private void assertConfigurationPresentAndCorrect(String key, String value) throws IOException, InvalidSyntaxException {
         Configuration configuration = getConfiguration("test");
         assertNotNull(configuration);
         assertEquals(value, configuration.getProperties().get(key));
     }
     
     private void assertConfigurationNotPresent() throws IOException, InvalidSyntaxException {
         Configuration configuration = getConfiguration("test");
         assertNull(configuration);
     }
     
     private void assertMBeanNotPresent() throws IntrospectionException, ReflectionException {
         try {
             mBeanServer.getMBeanInfo(objectName);
             fail("MBean still present after uninstall");
         } catch (InstanceNotFoundException infe) {            
         }
     }
 }
