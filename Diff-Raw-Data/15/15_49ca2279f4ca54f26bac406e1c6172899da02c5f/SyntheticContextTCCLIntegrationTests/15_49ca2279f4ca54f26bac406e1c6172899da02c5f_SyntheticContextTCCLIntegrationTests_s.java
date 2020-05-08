 /*******************************************************************************
  * Copyright (c) 2011 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.deployer.test;
 
 import java.io.File;
 
 import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
 import org.junit.Test;
 
 public class SyntheticContextTCCLIntegrationTests extends AbstractDeployerIntegrationTest {
 
     @Test
     public void testSyntheticContextIsTCCL() throws Exception {
         File libraryBundle = new File("src/test/resources/synthetic-tccl/synthetic.tccl.global.jar");
         DeploymentIdentity libraryBundleDeploymentId = this.deployer.deploy(libraryBundle.toURI());
 
         try {
             File par = new File("src/test/resources/synthetic-tccl/synthetic.tccl.par");
             DeploymentIdentity parDeploymentId = this.deployer.deploy(par.toURI());
 
             this.deployer.undeploy(parDeploymentId);
         } finally {
             this.deployer.undeploy(libraryBundleDeploymentId);
         }
     }
 }
