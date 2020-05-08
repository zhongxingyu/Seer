 /*
  * This file is part of the Eclipse Virgo project.
  *
  * Copyright (c) 2011 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    VMware Inc. - initial contribution
  */
 
 package org.eclipse.virgo.kernel.osgi.region.internal;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 
 import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
 import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.Version;
 import org.osgi.framework.wiring.BundleRevision;
 
 public class StandardRegionFilterTests {
 
     private static final String BUNDLE_SYMBOLIC_NAME = "A";
 
     private static final Version BUNDLE_VERSION = new Version("0");
 
     private RegionFilter regionFilter;
 
 
     private StubBundle stubBundle;
     private String packageImportPolicy = "(" + BundleRevision.PACKAGE_NAMESPACE + "=*)";
     private String serviceImportPolicy = "(" + Constants.SERVICE_ID + "=*)";
 
     @Before
     public void setUp() throws Exception {
         this.regionFilter = new RegionFilter();
         this.stubBundle = new StubBundle(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION);
     }
 
     @After
     public void tearDown() throws Exception {
     }
 
     private void addBundleFilter(String bundleSymbolicName, Version bundleVersion) throws InvalidSyntaxException {
     	String filter = "(&(" + 
 				RegionFilter.VISIBLE_BUNDLE_NAMESPACE + "=" + bundleSymbolicName + ")(" +
				Constants.BUNDLE_VERSION_ATTRIBUTE + ">=" + bundleVersion + "))";
 		regionFilter.setFilters(RegionFilter.VISIBLE_BUNDLE_NAMESPACE, Arrays.asList(filter));
 
 	}
 
     @Test
     public void testAllowBundle() throws InvalidSyntaxException {
         addBundleFilter(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION);
         assertTrue(this.regionFilter.isBundleAllowed(stubBundle));
     }
 
 	@Test
     public void testBundleNotAllowed() {
         assertFalse(this.regionFilter.isBundleAllowed(stubBundle));
     }
 
     @Test
     public void testBundleNotAllowedInRange() throws InvalidSyntaxException {
         addBundleFilter(BUNDLE_SYMBOLIC_NAME, new Version(1,0,0));
         assertFalse(this.regionFilter.isBundleAllowed(stubBundle));
     }
 
     @Test
     public void testSetPackageImportPolicy() throws InvalidSyntaxException {
 		this.regionFilter.setFilters(RegionFilter.VISIBLE_PACKAGE_NAMESPACE, Arrays.asList(packageImportPolicy));
         assertEquals(Arrays.asList(this.packageImportPolicy), this.regionFilter.getFilters(RegionFilter.VISIBLE_PACKAGE_NAMESPACE));
     }
 
     @Test
     public void testSetServiceFilter() throws InvalidSyntaxException {
         this.regionFilter.setFilters(RegionFilter.VISIBLE_SERVICE_NAMESPACE, Arrays.asList(serviceImportPolicy));
        assertEquals(Arrays.asList(serviceImportPolicy), this.regionFilter.getFilters(RegionFilter.VISIBLE_SERVICE_NAMESPACE));
     }
 
 }
