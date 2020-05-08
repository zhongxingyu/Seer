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
 
 package org.eclipse.virgo.kernel.shell.state.internal;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import java.io.File;
 import java.util.List;
 
 import org.easymock.EasyMock;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
 import org.eclipse.virgo.kernel.osgi.region.Region;
 import org.eclipse.virgo.kernel.osgi.region.RegionMembership;
 import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
 import org.eclipse.virgo.kernel.shell.stubs.StubQuasiFrameworkFactory;
 import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.junit.Before;
import org.junit.Test;
 
 /**
  * <p>
  * Tests for {@link StandardStateService}
  * </p>
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Tests
  * 
  */
 public class StandardStateServiceTests {
 
     private final static File TEST_DUMP_FILE = new File("src/test/resources/fakeDump");
 
     private StandardStateService standardStateService;
 
     private StubBundleContext stubBundleContext;
 
     private QuasiFrameworkFactory stubQuasiFrameworkFactory;
 
     private Region mockUserRegion;
 
     private RegionMembership mockRegionMembership;
 
     private Region mockKernelRegion;
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
         this.stubBundleContext = new StubBundleContext();
         this.stubQuasiFrameworkFactory = new StubQuasiFrameworkFactory();
         this.mockUserRegion = EasyMock.createMock(Region.class);
         this.mockKernelRegion = EasyMock.createMock(Region.class);
         this.mockRegionMembership = EasyMock.createMock(RegionMembership.class);
         EasyMock.expect(this.mockRegionMembership.getRegion(EasyMock.anyLong())).andReturn(this.mockUserRegion).anyTimes();
         EasyMock.expect(this.mockRegionMembership.getKernelRegion()).andReturn(this.mockKernelRegion).anyTimes();
         EasyMock.replay(this.mockUserRegion, this.mockKernelRegion, this.mockRegionMembership);
         this.standardStateService = new StandardStateService(this.stubQuasiFrameworkFactory, this.stubBundleContext, this.mockRegionMembership);
     }
 
     @Test
     public void getAllBundlesNullDump() {
         List<QuasiBundle> result = this.standardStateService.getAllBundles(null);
         assertNotNull(result);
         assertEquals(1, result.size());
     }
 
     @Test
     public void getAllBundlesFromDump() {
         List<QuasiBundle> result = this.standardStateService.getAllBundles(TEST_DUMP_FILE);
         assertNotNull(result);
         assertEquals(1, result.size());
     }
 
     @Test
     public void getBundleNullDumpExists() {
         QuasiBundle quasiBundle = this.standardStateService.getBundle(null, 4);
         assertNotNull(quasiBundle);
         assertEquals("fake.test.bundle", quasiBundle.getSymbolicName());
     }
 
     @Test
     public void getBundleFromDumpExists() {
         QuasiBundle quasiBundle = this.standardStateService.getBundle(TEST_DUMP_FILE, 4);
         assertNotNull(quasiBundle);
         assertEquals("fake.test.bundle", quasiBundle.getSymbolicName());
     }
 
     @Test
     public void getBundleNullDumpNoExists() {
         QuasiBundle quasiBundle = this.standardStateService.getBundle(null, 5);
         assertNull(quasiBundle);
     }
 
     @Test
     public void getBundleFromDumpNoExists() {
         QuasiBundle quasiBundle = this.standardStateService.getBundle(TEST_DUMP_FILE, 5);
         assertNull(quasiBundle);
     }
 
     @Test
     public void getAllServices() {
         List<QuasiLiveService> allServices = this.standardStateService.getAllServices(new File(""));
         assertNotNull(allServices);
     }
 
 }
