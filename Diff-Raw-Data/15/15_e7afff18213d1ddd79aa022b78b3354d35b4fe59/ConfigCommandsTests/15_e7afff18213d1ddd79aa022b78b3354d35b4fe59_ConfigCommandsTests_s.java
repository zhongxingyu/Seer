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
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.util.Arrays;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 
 import org.eclipse.virgo.shell.internal.formatting.StubManageableCompositeArtifact;
 import org.eclipse.virgo.test.stubs.region.StubRegionDigraph;
import org.eclipse.virgo.test.stubs.service.cm.StubConfigurationAdmin;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.BundleException;
 
 import org.eclipse.virgo.shell.internal.commands.ConfigCommands;
import org.eclipse.virgo.shell.internal.formatting.StubManageableCompositeArtifact;
 import org.eclipse.virgo.test.stubs.service.cm.StubConfigurationAdmin;
 
 public class ConfigCommandsTests {
 
     private static final StubRegionDigraph REGION_DIGRAPH = new StubRegionDigraph();
 
 	private static final StubConfigurationAdmin CONFIG_ADMIN = new StubConfigurationAdmin();
 
     private final ConfigCommands commands = new ConfigCommands(new StubRuntimeArtifactModelObjectNameCreator(), CONFIG_ADMIN, REGION_DIGRAPH);
 
     private final StubManageableCompositeArtifact artifact = new StubManageableCompositeArtifact();
 
     private volatile ObjectName name;
     {
         try {
             this.name = new ObjectName("test:type=ArtifactModel,artifact-type=configuration,name=test1,version=0.0.0,region=global");
         } catch (MalformedObjectNameException e) {
         } catch (NullPointerException e) {
         }
     }
 
     @Before
     public void installTestBean() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, BundleException {
     	REGION_DIGRAPH.createRegion("global");
         ManagementFactory.getPlatformMBeanServer().registerMBean(this.artifact, this.name);
     }
 
     @After
     public void uninstallTestBean() throws MBeanRegistrationException, InstanceNotFoundException {
         ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.name);
     }
 
     @Test
     public void examineActive() throws IOException {
     	CONFIG_ADMIN.createConfiguration("test1");
         assertEquals(Arrays.asList("Factory pid:     com.springsource.testName", "Bundle Location: "), this.commands.examine("test1", "0.0.0", "global"));
     }
 
     @Test
     public void examineNotActive() {
         this.artifact.setState("RESOLVED");
         assertEquals(Arrays.asList("Unable to examine configuration in non-active state"), this.commands.examine("test1", "0.0.0", "global"));
     }
 }
