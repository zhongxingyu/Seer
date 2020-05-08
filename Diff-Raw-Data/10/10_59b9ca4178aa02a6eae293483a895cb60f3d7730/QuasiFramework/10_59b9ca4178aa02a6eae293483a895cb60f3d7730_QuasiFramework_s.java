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
 package org.eclipse.virgo.kernel.osgi.quasi;
 
 import java.net.URI;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.equinox.region.Region;
 import org.eclipse.osgi.service.resolver.State;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Version;
 
 /**
  * {@link QuasiFramework} is a snapshot of the OSGi framework state into which bundles can be installed and resolved
  * and, in the normal case, committed into the OSGi framework.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Implementations of this class must be thread safe.
  * 
  */
 public interface QuasiFramework {
 
     /**
      * Installs a bundle into this {@link QuasiFramework} using the given {@link URI} and {@link BundleManifest} and
      * returns a {@link QuasiBundle}.
      * 
      * @param location a <code>URI</code> to the bundle's contents
      * @param bundleManifest the <code>BundleManifest</code> to be used for the bundle, regardless of any manifest at
      *        the location
      * @return a <code>QuasiBundle</code>
      * @throws BundleException if the bundle could not be installed
      */
     QuasiBundle install(URI location, BundleManifest bundleManifest) throws BundleException;
 
     /**
      * Returns a list of {@link QuasiBundle} that represent all the bundles currently installed in this {@link QuasiFramework}.
      * 
      * @return a non<code>null</code> list of <code>QuasiBundle</code>
      */
     List<QuasiBundle> getBundles();
 
     /**
      * Returns a {@link QuasiBundle} with the given bundle id.
      * 
      * @param bundleId 
      * @return <code>QuasiBundle</code> or <code>null</code> if the id is not known.
      */
     QuasiBundle getBundle(long bundleId);
    
    //TODO DELETE THIS METHOD
    /**
     * Returns a {@link QuasiBundle} with the given bundle name and version.
     * 
     * @param name
     * @param version
     * @return <code>QuasiBundle</code> or <code>null</code> if the name/version is not found.
     */
    QuasiBundle getBundle(String name, Version version);
 
     /**
      * Return the set of {@link Region} present in this {@link QuasiFramework}.
      * 
      * @return the set of regions present in this framework
      */
     Set<Region> getRegions();
     
     /**
      * Attempts to resolve the {@link QuasiBundle QuasiBundles} that have been installed in this {@link QuasiFramework}.
      * <p/>
      * If all the bundles can be resolved, returns an empty list. If some of the bundles cannot be resolved, returns a
      * list of {@link QuasiResolutionFailure QuasiResolutionFailures}.
      * 
      * @return a non<code>null</code> list of <code>QuasiResolutionFailure</code>, which is empty if resolution succeeded
      */
     List<QuasiResolutionFailure> resolve();    
     
     /**
      * Diagnoses failures to resolve of the {@link QuasiBundle} given.
      * <p/>
      * If the bundle can be resolved, returns null otherwise a {@link QuasiResolutionFailure} is returned.
      * @param bundleId identifier (in the {@link State}) of bundle to diagnose
      * 
      * @return a non<code>null</code> list of <code>QuasiResolutionFailure</code>, which is empty if resolution succeeded
      */
     List<QuasiResolutionFailure> diagnose(long bundleId);
 
     
     /**
      * Attempts to resolve any unresolved {@link QuasiBundle QuasiBundles} in this {@link QuasiFramework} and then
      * commits the contents of this {@link QuasiFramework} by installing each of its <code>QuasiBundles</code> into the
      * OSGi framework.
      * <p/>
      * If any unresolved <code>QuasiBundles</code> cannot be resolved, {@link BundleException} is thrown.
      * <p/>
      * If a <code>QuasiBundle</code> fails to install, any <code>QuasiBundles</code> which have been installed are
      * uninstalled and <code>BundleException</code> is thrown.
      * <p/>
      * 
      * @throws BundleException if the contents could not be resolved and installed
      */
     void commit() throws BundleException;
 
     /**
      * Delete any resources associated with this {@link QuasiFramework}.
      */
     void destroy();
 
 }
