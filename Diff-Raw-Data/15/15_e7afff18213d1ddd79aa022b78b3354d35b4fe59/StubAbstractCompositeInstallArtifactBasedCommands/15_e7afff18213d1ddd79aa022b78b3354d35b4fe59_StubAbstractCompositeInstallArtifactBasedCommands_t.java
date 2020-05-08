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
 
 package org.eclipse.virgo.shell.internal.commands;
 
 import org.eclipse.virgo.kernel.model.management.ManageableCompositeArtifact;
 import org.eclipse.virgo.shell.internal.commands.AbstractInstallArtifactBasedCommands;
import org.eclipse.virgo.test.stubs.region.StubRegionDigraph;
 
 import org.osgi.framework.BundleException;
 
 final class StubAbstractCompositeInstallArtifactBasedCommands extends AbstractInstallArtifactBasedCommands<ManageableCompositeArtifact> {
 
     private static final StubRegionDigraph REGION_DIGRAPH = new StubRegionDigraph();
 
     {
     	try {
 			REGION_DIGRAPH.createRegion("region1");
 		} catch (BundleException e) {
 			e.printStackTrace(System.out);
 		}
     }
     
 	public StubAbstractCompositeInstallArtifactBasedCommands() {
         super("test", new StubRuntimeArtifactModelObjectNameCreator(), new StubInstallArtifactCommandFormatter(), ManageableCompositeArtifact.class, REGION_DIGRAPH);
     }
 
 }
