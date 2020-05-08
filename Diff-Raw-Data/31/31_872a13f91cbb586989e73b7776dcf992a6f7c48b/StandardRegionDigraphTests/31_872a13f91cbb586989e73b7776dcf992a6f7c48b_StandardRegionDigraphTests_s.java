 /*******************************************************************************
  * This file is part of the Virgo Web Server.
  *
  * Copyright (c) 2011 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.osgi.region.internal;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 import java.util.Set;
 
 import org.easymock.EasyMock;
 import org.eclipse.virgo.kernel.osgi.region.Region;
 import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
 import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
 import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
 import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
 import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
 import org.eclipse.virgo.util.math.OrderedPair;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.Version;
 
 public class StandardRegionDigraphTests {
 
     private RegionDigraph digraph;
 
     private Region mockRegion1;
 
     private Region mockRegion2;
 
     private Region mockRegion3;
 
     private RegionFilter regionFilter1;
 
     private RegionFilter regionFilter2;
 
     private Bundle mockBundle;
 
     @Before
     public void setUp() throws Exception {
         StubBundle stubSystemBundle = new StubBundle(0L, "osgi.framework", new Version("0"), "loc");
         StubBundleContext stubBundleContext = new StubBundleContext();
         stubBundleContext.addInstalledBundle(stubSystemBundle);
         this.digraph = new StandardRegionDigraph(stubBundleContext, new ThreadLocal<Region>());
        
         this.mockRegion1 = EasyMock.createMock(Region.class);
         EasyMock.expect(this.mockRegion1.getName()).andReturn("mockRegion1").anyTimes();
       
         this.mockRegion2 = EasyMock.createMock(Region.class);
         EasyMock.expect(this.mockRegion2.getName()).andReturn("mockRegion2").anyTimes();
 
         this.mockRegion3 = EasyMock.createMock(Region.class);
         EasyMock.expect(this.mockRegion3.getName()).andReturn("mockRegion3").anyTimes();
 
         this.regionFilter1 = new RegionFilter();
         this.regionFilter2 = new RegionFilter();
         this.mockBundle = EasyMock.createMock(Bundle.class);
     }
 
     private void setDefaultMockFilters() throws InvalidSyntaxException {
         this.regionFilter1.removeFilters(RegionFilter.VISIBLE_BUNDLE_NAMESPACE);
         this.regionFilter2.removeFilters(RegionFilter.VISIBLE_BUNDLE_NAMESPACE);
     }
 
 
     private void setMockFilterAllowedBundle(RegionFilter regionFilter, OrderedPair<String, Version> bundle) throws InvalidSyntaxException {
    		String filter = "(&(" + 
 				RegionFilter.VISIBLE_BUNDLE_NAMESPACE + "=" + bundle.getFirst() + ")(" +
				Constants.BUNDLE_VERSION_ATTRIBUTE + "=" + bundle.getSecond();
 		regionFilter.setFilters(RegionFilter.VISIBLE_BUNDLE_NAMESPACE, Arrays.asList(filter));
     }
 
     private void replayMocks() {
        EasyMock.replay(this.mockRegion1, this.mockRegion2, this.mockRegion3, this.regionFilter1, this.regionFilter2, this.mockBundle);
     }
 
     @After
     public void tearDown() throws Exception {
        EasyMock.verify(this.mockRegion1, this.mockRegion2, this.mockRegion3, this.regionFilter1, this.regionFilter2, this.mockBundle);
     }
 
     @Test
     public void testConnect() throws BundleException, InvalidSyntaxException {
         setDefaultMockFilters();
         replayMocks();
 
         this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion2);
     }
 
     @Test
     public void testConnectWithFilterContents() throws BundleException, InvalidSyntaxException {
         OrderedPair<String, Version> b1 = new OrderedPair<String, Version>("b1", new Version("0"));
         setMockFilterAllowedBundle(this.regionFilter1, b1);
         EasyMock.expect(this.mockRegion1.getBundle(b1.getFirst(), b1.getSecond())).andReturn(null).anyTimes();
 
         OrderedPair<String, Version> b2 = new OrderedPair<String, Version>("b2", new Version("0"));
         setMockFilterAllowedBundle(this.regionFilter2, b2);
         EasyMock.expect(this.mockRegion1.getBundle(b2.getFirst(), b2.getSecond())).andReturn(null).anyTimes();
 
         replayMocks();
 
         this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion2);
         this.digraph.connect(this.mockRegion1, this.regionFilter2, this.mockRegion3);
     }
 
     @Test(expected = BundleException.class)
     public void testConnectLoop() throws BundleException, InvalidSyntaxException {
         setDefaultMockFilters();
         replayMocks();
 
         this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion1);
     }
 
     @Test(expected = BundleException.class)
     public void testDuplicateConnection() throws BundleException, InvalidSyntaxException {
         setDefaultMockFilters();
         replayMocks();
 
         this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion2);
         this.digraph.connect(this.mockRegion1, this.regionFilter2, this.mockRegion2);
     }
 
     @Test
     public void testGetEdges() throws BundleException, InvalidSyntaxException {
         setDefaultMockFilters();
         replayMocks();
 
         this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion2);
         this.digraph.connect(this.mockRegion1, this.regionFilter2, this.mockRegion3);
         this.digraph.connect(this.mockRegion2, this.regionFilter2, this.mockRegion1);
 
         Set<FilteredRegion> edges = this.digraph.getEdges(this.mockRegion1);
 
         assertEquals(2, edges.size());
 
         for (FilteredRegion edge : edges) {
             if (edge.getRegion().equals(this.mockRegion2)) {
                 assertEquals(this.regionFilter1, edge.getFilter());
             } else if (edge.getRegion().equals(this.mockRegion3)) {
                 assertEquals(this.regionFilter2, edge.getFilter());
             } else {
                 fail("unexpected edge");
             }
         }
     }
 
     @Test
     public void testRemoveRegion() throws BundleException, InvalidSyntaxException {
         setDefaultMockFilters();
         replayMocks();
 
         this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion2);
         this.digraph.connect(this.mockRegion2, this.regionFilter2, this.mockRegion1);
         assertNotNull(this.digraph.getRegion("mockRegion1"));
         assertNotNull(this.digraph.getRegion("mockRegion2"));
         this.digraph.removeRegion(this.mockRegion1);
         assertNull(this.digraph.getRegion("mockRegion1"));
         assertNotNull(this.digraph.getRegion("mockRegion2"));
     }
     
     @Test
     public void testGetRegions() throws BundleException, InvalidSyntaxException {
         setDefaultMockFilters();
         replayMocks();
         
         this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion2);
         Set<Region> regions = this.digraph.getRegions();
         assertEquals(2, regions.size());
         assertTrue(regions.contains(this.mockRegion1));
         assertTrue(regions.contains(this.mockRegion2));
     }
     
 }
