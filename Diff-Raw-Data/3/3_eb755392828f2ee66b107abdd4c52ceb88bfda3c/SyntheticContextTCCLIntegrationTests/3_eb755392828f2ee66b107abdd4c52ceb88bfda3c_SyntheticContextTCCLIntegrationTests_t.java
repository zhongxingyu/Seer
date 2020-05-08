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
import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  * Test that the synthetic context bundle's class loader is used as the TCCL for a PAR.
  * <p/>
  * A bundle "b" in the test PAR invokes a static "run" method in a bundle "global" outside the PAR. The run method
  * attempts to load a class "AClass" of a package exported by bundle "a" in the PAR (but not imported by bundle "b")
  * using the current thread context class loader (TCCL). The TCCL should be the class loader of the synthetic context
  * bundle of the PAR and this class loader should be able to load classes from all packages exported by bundles in the
  * PAR, including "AClass".
  * <p/>
  * For the source of the PAR and "global" bundle, see test-apps/synthetic-tccl. Instructions for building are in
  * README.TXT.
  */
@Ignore("Bug 360965 - Avoid Spring DM overriding TCCL of scoped applications")
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
