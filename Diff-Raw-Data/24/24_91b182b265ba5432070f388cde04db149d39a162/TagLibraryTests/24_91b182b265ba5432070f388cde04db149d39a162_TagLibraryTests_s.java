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
 
 package org.eclipse.virgo.snaps.test;
 
 import java.util.concurrent.CountDownLatch;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
 
 public class TagLibraryTests extends AbstractDeployerTests {
     
     private DeploymentIdentity host;
     
     @Before
     public void deployHost() throws DeploymentException {
        host = deploy("src/test/resources/taglib-host.war");
     }
     
     private DeploymentIdentity deploySnapOne() throws Exception {
         return deploySnap("src/test/resources/taglib-snap-one.jar", "taglib.snap.one"); 
     }
     
     private DeploymentIdentity deploySnapTwo() throws Exception {
         return deploySnap("src/test/resources/taglib-snap-two.jar", "taglib.snap.two"); 
     }
     
     private DeploymentIdentity deploySnap(String snap, final String symbolicName) throws Exception {
         
         final CountDownLatch latch = new CountDownLatch(1);
         
         ServiceListener listener = new ServiceListener() {
             public void serviceChanged(ServiceEvent event) {
                 if (event.getType() == ServiceEvent.REGISTERED && symbolicName.equals(event.getServiceReference().getBundle().getSymbolicName())) {
                     latch.countDown();
                 }
             }
         };
         
         getContext().addServiceListener(listener, "(objectClass=org.eclipse.virgo.snaps.core.internal.Snap)");
         
         DeploymentIdentity deployed = deploy(snap);
         
         latch.await();
         
         getContext().removeServiceListener(listener);
         
         return deployed;
     }
     
     @Test
     public void noSnaps() throws Exception {
         RequestUtils.assertContent("", "/taglib-host", "/index.jsp");        
     }
     
     @Test
     public void singleSnap() throws Exception {
         DeploymentIdentity snapOne = deploySnapOne();
         try {
             RequestUtils.assertContentContains("Taglib Snap One", "/taglib-host", "/index.jsp");
             RequestUtils.assertContentDoesNotContain("Taglib Snap Two", "/taglib-host", "/index.jsp");
         } finally {
             getDeployer().undeploy(snapOne);
         }
         DeploymentIdentity snapTwo = deploySnapTwo();
         try {
             RequestUtils.assertContentDoesNotContain("Taglib Snap One", "/taglib-host", "/index.jsp");
             RequestUtils.assertContentContains("Taglib Snap Two", "/taglib-host", "/index.jsp");
         } finally {
             getDeployer().undeploy(snapTwo);
         }
     }
     
     @Test
     public void multipleSnaps() throws Exception {
         DeploymentIdentity snapOne = deploySnapOne();
         DeploymentIdentity snapTwo = deploySnapTwo();        
         try {
             RequestUtils.assertContentContains("Taglib Snap TwoTaglib Snap One", "/taglib-host", "/index.jsp");
             getDeployer().undeploy(snapOne);
             snapOne = deploySnapOne();
             RequestUtils.assertContentContains("Taglib Snap OneTaglib Snap Two", "/taglib-host", "/index.jsp");
         } finally {
             getDeployer().undeploy(snapOne);
             getDeployer().undeploy(snapTwo);
         }        
     }
     
     @After
     public void undeployHost() throws DeploymentException {
         getDeployer().undeploy(host);
     }
 }
