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
 
 package org.eclipse.virgo.shell.internal.completers;
 
 import static org.junit.Assert.assertEquals;
 
 import java.lang.management.ManagementFactory;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 
 import org.eclipse.virgo.shell.internal.commands.StubRuntimeArtifactModelObjectNameCreator;
 import org.eclipse.virgo.shell.internal.completers.ConfigCompleter;
 import org.eclipse.virgo.shell.internal.formatting.StubManageableCompositeArtifact;
import org.eclipse.virgo.test.stubs.region.StubRegionDigraph;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.BundleException;
 
 
 public class ConfigCompleterTests {
 
     private static final StubRegionDigraph REGION_DIGRAPH = new StubRegionDigraph();
     
 	private final ConfigCompleter completer = new ConfigCompleter(new StubRuntimeArtifactModelObjectNameCreator(), REGION_DIGRAPH);
 
     @Before
     public void installTestBean() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, BundleException {
     	REGION_DIGRAPH.createRegion("global");
         ManagementFactory.getPlatformMBeanServer().registerMBean(getActiveArtifact(), getObjectName("test1", "0.0.0"));
         ManagementFactory.getPlatformMBeanServer().registerMBean(getActiveArtifact(), getObjectName("test1", "1.0.0"));
         ManagementFactory.getPlatformMBeanServer().registerMBean(getActiveArtifact(), getObjectName("test2", "0.0.0"));
         ManagementFactory.getPlatformMBeanServer().registerMBean(getInactiveArtifact(), getObjectName("test2", "1.0.0"));
         ManagementFactory.getPlatformMBeanServer().registerMBean(getInactiveArtifact(), getObjectName("test3", "0.0.0"));
         ManagementFactory.getPlatformMBeanServer().registerMBean(getInactiveArtifact(), getObjectName("test3", "1.0.0"));
     }
 
     @After
     public void uninstallTestBean() throws MBeanRegistrationException, InstanceNotFoundException {
         ManagementFactory.getPlatformMBeanServer().unregisterMBean(getObjectName("test1", "0.0.0"));
         ManagementFactory.getPlatformMBeanServer().unregisterMBean(getObjectName("test1", "1.0.0"));
         ManagementFactory.getPlatformMBeanServer().unregisterMBean(getObjectName("test2", "0.0.0"));
         ManagementFactory.getPlatformMBeanServer().unregisterMBean(getObjectName("test2", "1.0.0"));
         ManagementFactory.getPlatformMBeanServer().unregisterMBean(getObjectName("test3", "0.0.0"));
         ManagementFactory.getPlatformMBeanServer().unregisterMBean(getObjectName("test3", "1.0.0"));
     }
 
     @Test
     public void filterNames() {
         assertEquals(2, this.completer.getCompletionCandidates("examine", "").size());
     }
 
     @Test
     public void filterVersions() {
         assertEquals(2, this.completer.getCompletionCandidates("examine", "test1", "").size());
         assertEquals(1, this.completer.getCompletionCandidates("examine", "test2", "").size());
         assertEquals(0, this.completer.getCompletionCandidates("examine", "test3", "").size());
     }
 
     private final ObjectName getObjectName(String name, String version) {
         try {
             return new ObjectName("test:type=ArtifactModel,artifact-type=configuration,name=" + name + ",version=" + version + ",region=global");
         } catch (MalformedObjectNameException e) {
         } catch (NullPointerException e) {
         }
         return null;
     }
 
     private final StubManageableCompositeArtifact getActiveArtifact() {
         return new StubManageableCompositeArtifact().setState("ACTIVE");
     }
 
     private final StubManageableCompositeArtifact getInactiveArtifact() {
         return new StubManageableCompositeArtifact().setState("RESOLVED");
     }
 }
