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
 
 package org.eclipse.virgo.kernel.osgi.region.hook;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.virgo.kernel.osgi.region.Region;
 import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
 import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
 import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.hooks.bundle.FindHook;
 
 /**
  * {@link RegionBundleFindHook} manages the visibility of bundles across regions according to the {@link RegionDigraph}.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * Thread safe.
  */
 public final class RegionBundleFindHook implements FindHook {
 
     private final RegionDigraph regionDigraph;
 
     public RegionBundleFindHook(RegionDigraph regionDigraph) {
         this.regionDigraph = regionDigraph;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void find(BundleContext context, Collection<Bundle> bundles) {
         if (context.getBundle().getBundleId() == 0L) {
             return;
         }
 
         Region finderRegion = getRegion(context);
         if (finderRegion == null) {
             bundles.clear();
             return;
         }
 
         Set<Bundle> allowed = getAllowed(finderRegion, bundles, new HashSet<Region>());
 
         bundles.retainAll(allowed);
     }
 
     private class Visitor {
 
        private final Collection<Bundle> bundles;
 
        private final Set<Bundle> allowed = new HashSet<Bundle>();

        private Visitor(Collection<Bundle> bundles) {
            this.bundles = bundles;
        }

        private Set<Bundle> getAllowed() {
             return this.allowed;
         }

        private Collection<Bundle> getBundles() {
            return this.bundles;
        }
     }
 
     private Set<Bundle> getAllowed(Region r, Collection<Bundle> bundles, Set<Region> path) {
        Visitor visitor = new Visitor(bundles);
 
         if (!path.contains(r)) {
            allowBundlesInRegion(visitor.getAllowed(), r, visitor.getBundles());
            allowImportedBundles(visitor.getAllowed(), r, visitor.getBundles(), path);
         }
 
         return visitor.getAllowed();
     }
 
     private void allowImportedBundles(Set<Bundle> allowed, Region r, Collection<Bundle> bundles, Set<Region> path) {
         for (FilteredRegion fr : this.regionDigraph.getEdges(r)) {
             Set<Bundle> a = getAllowed(fr.getRegion(), bundles, extendPath(r, path));
             filter(a, fr.getFilter());
             allowed.addAll(a);
         }
     }
 
     private void allowBundlesInRegion(Set<Bundle> allowed, Region r, Collection<Bundle> bundles) {
         for (Bundle b : bundles) {
             if (r.contains(b)) {
                 allowed.add(b);
             }
         }
     }
 
     private Set<Region> extendPath(Region r, Set<Region> path) {
         Set<Region> newPath = new HashSet<Region>(path);
         newPath.add(r);
         return newPath;
     }
 
     private void filter(Set<Bundle> bundles, RegionFilter filter) {
         Iterator<Bundle> i = bundles.iterator();
         while (i.hasNext()) {
             Bundle b = i.next();
             if (!filter.isBundleAllowed(b.getSymbolicName(), b.getVersion())) {
                 i.remove();
             }
         }
     }
 
     private Region getRegion(BundleContext context) {
         Bundle b = context.getBundle();
         for (Region r : this.regionDigraph) {
             if (r.contains(b)) {
                 return r;
             }
         }
         return null;
     }
 
 }
