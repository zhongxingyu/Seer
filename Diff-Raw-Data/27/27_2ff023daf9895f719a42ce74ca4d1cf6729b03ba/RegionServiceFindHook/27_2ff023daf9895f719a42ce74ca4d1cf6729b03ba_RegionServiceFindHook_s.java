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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.virgo.kernel.osgi.region.Region;
 import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
 import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
 import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Filter;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.hooks.service.FindHook;
 
 /**
  * {@link RegionServiceFindHook} manages the visibility of services across regions according to the
  * {@link RegionDigraph}.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * Thread safe.
  */
 public final class RegionServiceFindHook implements FindHook {
 
     private final RegionDigraph regionDigraph;
    private final org.osgi.framework.hooks.bundle.FindHook bundleFindHook;
 
    public RegionServiceFindHook(RegionDigraph regionDigraph, org.osgi.framework.hooks.bundle.FindHook bundleFindHook) {
         this.regionDigraph = regionDigraph;
        this.bundleFindHook = bundleFindHook;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void find(BundleContext context, String name, String filter, boolean allServices, Collection<ServiceReference<?>> references) {
         if (context.getBundle().getBundleId() == 0L) {
             return;
         }
 
         Region finderRegion = getRegion(context);
         if (finderRegion == null) {
             references.clear();
             return;
         }
 
         Set<ServiceReference<?>> allowed = getAllowed(finderRegion, references, new HashSet<Region>());
        Collection<Bundle> registrant = new ArrayList<Bundle>(1);
        for (ServiceReference<?> reference : references) {
        	if (allowed.contains(reference))
        		continue;
        	registrant.clear();
        	registrant.add(reference.getBundle());
        	bundleFindHook.find(context, registrant);
        	if (!registrant.isEmpty())
        		allowed.add(reference);
		}
 
         references.retainAll(allowed);
     }
 
     private Set<ServiceReference<?>> getAllowed(Region r, Collection<ServiceReference<?>> references, Set<Region> path) {
         Set<ServiceReference<?>> allowed = new HashSet<ServiceReference<?>>();
 
         if (!path.contains(r)) {
             allowServiceReferencesInRegion(allowed, r, references);
             allowImportedServices(allowed, r, references, path);
         }
 
         return allowed;
     }
 
     private void allowImportedServices(Set<ServiceReference<?>> allowed, Region r, Collection<ServiceReference<?>> references, Set<Region> path) {
         for (FilteredRegion fr : this.regionDigraph.getEdges(r)) {
             Set<ServiceReference<?>> a = getAllowed(fr.getRegion(), references, extendPath(r, path));
             filter(a, fr.getFilter());
             allowed.addAll(a);
         }
     }
 
     private void allowServiceReferencesInRegion(Set<ServiceReference<?>> allowed, Region r, Collection<ServiceReference<?>> references) {
         for (ServiceReference<?> b : references) {
             if (r.contains(b.getBundle())) {
                 allowed.add(b);
             }
         }
     }
 
     private Set<Region> extendPath(Region r, Set<Region> path) {
         Set<Region> newPath = new HashSet<Region>(path);
         newPath.add(r);
         return newPath;
     }
 
     private void filter(Set<ServiceReference<?>> references, RegionFilter filter) {
         Iterator<ServiceReference<?>> i = references.iterator();
         while (i.hasNext()) {
             ServiceReference<?> sr = i.next();
            if (!filter.isServiceAllowed(sr)) {
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
