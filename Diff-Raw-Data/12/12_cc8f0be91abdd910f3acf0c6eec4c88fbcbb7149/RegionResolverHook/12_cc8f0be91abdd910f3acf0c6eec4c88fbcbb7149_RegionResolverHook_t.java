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
 
 import org.eclipse.osgi.service.resolver.BundleDescription;
 import org.eclipse.virgo.kernel.osgi.region.Region;
 import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
 import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
 import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
 import org.eclipse.virgo.kernel.serviceability.Assert;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.hooks.resolver.ResolverHook;
 import org.osgi.framework.wiring.BundleCapability;
 import org.osgi.framework.wiring.BundleRequirement;
 import org.osgi.framework.wiring.BundleRevision;
 
 /**
  * {@link RegionResolverHook} manages the visibility of bundles across regions according to the {@link RegionDigraph}.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * Thread safe.
  */
 final class RegionResolverHook implements ResolverHook {
 
     private static final long INVALID_BUNDLE_ID = -1L;
 
     private static final Boolean DEBUG = false;
 
     private final RegionDigraph regionDigraph;
 
     RegionResolverHook(RegionDigraph regionDigraph) {
         this.regionDigraph = regionDigraph;
     }
 
     @Override
     public void filterMatches(BundleRequirement requirement, Collection<BundleCapability> candidates) {
         BundleRevision requirer = requirement.getRevision();
         try {
             if (DEBUG) {
                 debugEntry(requirer, candidates);
             }
 
             if (getBundleId(requirer) == 0L) {
                 return;
             }
 
             Region requirerRegion = getRegion(requirer);
             if (requirerRegion == null) {
                 candidates.clear();
                 return;
             }
 
             Set<BundleCapability> allowed = getAllowed(requirerRegion, candidates, new HashSet<Region>());
 
             candidates.retainAll(allowed);
         } finally {
             if (DEBUG) {
                 debugExit(requirer, candidates);
             }
         }
     }
 
     private Region getRegion(BundleRevision bundleRevision) {
         Bundle bundle = bundleRevision.getBundle();
         if (bundle != null) {
             return getRegion(bundle);
         }
         Long bundleId = getBundleId(bundleRevision);
         return getRegion(bundleId);
     }
 
     private Region getRegion(Long bundleId) {
         return this.regionDigraph.getRegion(bundleId);
     }
 
     private Long getBundleId(BundleRevision bundleRevision) {
         // For testability, use the bundle revision's bundle before casting to ResolverBundle.
         Bundle bundle = bundleRevision.getBundle();
         if (bundle != null) {
             return bundle.getBundleId();
         }
         if (bundleRevision instanceof BundleDescription) {
             BundleDescription bundleDescription = (BundleDescription) bundleRevision;
             return bundleDescription.getBundleId();
         }
         Assert.isTrue(false, "Cannot determine bundle id of BundleRevision '%s'", bundleRevision);
         return INVALID_BUNDLE_ID;
     }
 
     private Set<BundleCapability> getAllowed(Region r, Collection<BundleCapability> candidates, Set<Region> path) {
         Set<BundleCapability> allowed = new HashSet<BundleCapability>();
 
         if (!path.contains(r)) {
             allowCapabilitiesInRegion(allowed, r, candidates);
             allowImportedCapabilities(allowed, r, candidates, path);
         }
 
         return allowed;
     }
 
     private void allowImportedCapabilities(Set<BundleCapability> allowed, Region r, Collection<BundleCapability> candidates, Set<Region> path) {
         for (FilteredRegion fr : this.regionDigraph.getEdges(r)) {
             Set<BundleCapability> a = getAllowed(fr.getRegion(), candidates, extendPath(r, path));
             filter(a, fr.getFilter());
             allowed.addAll(a);
         }
     }
 
     private void allowCapabilitiesInRegion(Set<BundleCapability> allowed, Region r, Collection<BundleCapability> candidates) {
         for (BundleCapability b : candidates) {
             if (r.equals(getRegion(b.getRevision()))) {
                 allowed.add(b);
             }
         }
     }
 
     private Set<Region> extendPath(Region r, Set<Region> path) {
         Set<Region> newPath = new HashSet<Region>(path);
         newPath.add(r);
         return newPath;
     }
 
     private void filter(Set<BundleCapability> capabilities, RegionFilter filter) {
         Iterator<BundleCapability> i = capabilities.iterator();
         while (i.hasNext()) {
             BundleCapability c = i.next();
            if (!filter.isCapabilityAllowed(c) && !filter.isBundleAllowed(c.getRevision()))
             	i.remove();
         }
     }
 
     private Region getRegion(Bundle bundle) {
         for (Region r : this.regionDigraph) {
             if (r.contains(bundle)) {
                 return r;
             }
         }
         return null;
     }
 
     @Override
     public void end() {
     }
 
     @Override
     public void filterResolvable(Collection<BundleRevision> candidates) {
     }
 
     @Override
     public void filterSingletonCollisions(BundleCapability singleton, Collection<BundleCapability> collisionCandidates) {
         collisionCandidates.clear(); //XXX temporary hack in lieu of Borislav's changes
     }
 
     private void debugEntry(BundleRevision requirer, Collection<BundleCapability> candidates) {
         System.out.println("Requirer: " + requirer.getSymbolicName() + "_" + requirer.getVersion() + "[" + getBundleId(requirer) + "]");
         System.out.println("  Candidates: ");
         Iterator<BundleCapability> i = candidates.iterator();
         while (i.hasNext()) {
             BundleCapability c = i.next();
             String namespace = c.getNamespace();
             if (BundleRevision.PACKAGE_NAMESPACE.equals(namespace)) {
                 BundleRevision providerRevision = c.getRevision();
                 String pkg = (String) c.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE);
                 System.out.println("    Package " + pkg + " from provider " + providerRevision.getSymbolicName() + "_"
                     + providerRevision.getVersion() + "[" + getBundleId(providerRevision) + "]");
                 if (pkg.equals("slow")) {
                     System.out.println(">>> put breakpoint here <<<");
                 }
             } else {
                 BundleRevision providerRevision = c.getRevision();
                 System.out.println("    Bundle from provider " + providerRevision.getSymbolicName() + "_" + providerRevision.getVersion() + "["
                     + getBundleId(providerRevision) + "]");
             }
         }
     }
 
     private void debugExit(BundleRevision requirer, Collection<BundleCapability> candidates) {
         System.out.println("  Filtered candidates: ");
         Iterator<BundleCapability> i = candidates.iterator();
         while (i.hasNext()) {
             BundleCapability c = i.next();
             String namespace = c.getNamespace();
             if (BundleRevision.PACKAGE_NAMESPACE.equals(namespace)) {
                 BundleRevision providerRevision = c.getRevision();
                 String pkg = (String) c.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE);
                 System.out.println("    Package " + pkg + " from provider " + providerRevision.getSymbolicName() + "_"
                     + providerRevision.getVersion() + "[" + getBundleId(providerRevision) + "]");
                 if (pkg.equals("slow")) {
                     System.out.println(">>> put breakpoint here <<<");
                 }
             } else {
                 BundleRevision providerRevision = c.getRevision();
                 System.out.println("    Bundle from provider " + providerRevision.getSymbolicName() + "_" + providerRevision.getVersion() + "["
                     + getBundleId(providerRevision) + "]");
             }
         }
     }
 }
