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
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.fail;
 
 import java.lang.management.ManagementFactory;
 
 import javax.management.JMException;
 import javax.management.JMX;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 
 import org.junit.Test;
 
 
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
 import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
 import org.eclipse.virgo.util.io.PathReference;
 
 
 /**
  */
 public class HotDeploymentTests extends AbstractDeployerIntegrationTest {
     
     private static final long TIMEOUT = 10000;
     
     private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
     
     @Test
     public void programmaticUndeployDeletesHotDeployedPropertiesFile() throws DeploymentException {
         PathReference pathReference = new PathReference("src/test/resources/hot-deployment-tests/test.properties");
         doTest(pathReference, "configuration", "test", "0.0.0");
     }
     
     @Test
     public void programmaticUndeployDeletesHotDeployedExplodedBundle() throws DeploymentException {
         PathReference pathReference = new PathReference("src/test/resources/hot-deployment-tests/bundle");
         doTest(pathReference, "bundle", "test", "1.0.0");
     }
     
     @Test
     public void programmaticUndeployDeletesHotDeployedBundle() throws DeploymentException {
         PathReference pathReference = new PathReference("src/test/resources/hot-deployment-tests/bundle.jar");
         doTest(pathReference, "bundle", "test", "1.0.0");
     }
     
     private void doTest(PathReference artifact, String type, String name, String version) throws DeploymentException {
         PathReference copyInPickup = artifact.copy(new PathReference("target/pickup"), true);
         
         try {
             while (!this.deployer.isDeployed(copyInPickup.toURI())) {
                 try {
                     Thread.sleep(50);
                 } catch (InterruptedException _) {
                 }
             }
             
             awaitActive(type, name, version);
             
             this.deployer.undeploy(type, name, version);
             
             assertFalse(copyInPickup.toFile().exists());
             
         } finally {
             copyInPickup.delete();
         }
     }
     
     private void awaitActive(String type, String name, String version) {
         try {
            ObjectName objectName = new ObjectName("org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=" + type + ",name=" + name + ",version=" + version);
             ManageableArtifact artifact = JMX.newMXBeanProxy(this.mBeanServer, objectName, ManageableArtifact.class);
             
             long startTime = System.currentTimeMillis();
             
             while (!"ACTIVE".equals(artifact.getState())) {
                 if (System.currentTimeMillis() - startTime > TIMEOUT) {
                     fail("Artifact " + type + " " + name + " " + version + " was not active within " + TIMEOUT + "ms.");
                 }
                 try {
                     Thread.sleep(50);
                 } catch (InterruptedException _) {
                 }
             }
         } catch (JMException jme) {
             fail(jme.getMessage());
         }
     }
 }
