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
 
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
 
 
 public abstract class AbstractCapabilityTests extends AbstractDeployerTests {
     
     private DeploymentIdentity host;
     
     protected void deployHost() throws DeploymentException {
        host = deploy("src/test/resources/simple-host.war");
     }
     
     protected void deployRootHost() throws DeploymentException {
         host = deploy("src/test/resources/root-host.war");
     }
     
     protected void undeployHost() throws DeploymentException {
         getDeployer().undeploy(host);        
     }
 }
