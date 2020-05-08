 /*******************************************************************************
  * This file is part of the Virgo Web Server.
  *
  * Copyright (c) 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.osgi.region;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Filter;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.wiring.BundleCapability;
 import org.osgi.framework.wiring.BundleRevision;
 
 /**
  * A {@link RegionFilter} is associated with a connection from one region to another and determines the bundles,
  * packages, and services which are visible across the connection.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * This implementations is thread safe.
  * 
  */
 public class RegionFilter {
 
 	public static final String VISIBLE_PACKAGE_NAMESPACE = BundleRevision.PACKAGE_NAMESPACE;
 	public static final String VISIBLE_REQUIRE_NAMESPACE = BundleRevision.BUNDLE_NAMESPACE;
 	public static final String VISIBLE_HOST_NAMESPACE = BundleRevision.HOST_NAMESPACE;
 	public static final String VISIBLE_SERVICE_NAMESPACE = "org.eclipse.equinox.allow.service";
 	public static final String VISIBLE_BUNDLE_NAMESPACE = "org.eclipse.equinox.allow.bundle";
 	public static final String VISIBLE_ALL_NAMESPACE = "org.eclipse.equinox.allow.all";
 
     public static final String ALL_PACKAGES = '(' + VISIBLE_PACKAGE_NAMESPACE + "=*)";
     public static final String ALL_REQUIRES = '(' + VISIBLE_REQUIRE_NAMESPACE + "=*)";
     public static final String ALL_HOSTS = '(' + VISIBLE_HOST_NAMESPACE + "=*)";
     public static final String ALL_SERVICES = '(' + org.osgi.framework.Constants.SERVICE_ID + "=*)";
     public static final String ALL_BUNDLES = '(' + org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE + "=*)";
     // pretty much a hack to make Filter always return true
     public static final String ALL = "(|(x=*)(!(x=*)))";
 
     public static final RegionFilter TOP = new RegionFilter() {
 
 		@Override
 		public boolean isBundleAllowed(Bundle bundle) {
 			return true;
 		}
 
 		@Override
 		public boolean isBundleAllowed(BundleRevision bundle) {
 			return true;
 		}
 
 		@Override
 		public boolean isServiceAllowed(ServiceReference<?> service) {
 			return true;
 		}
 
 		@Override
 		public boolean isCapabilityAllowed(BundleCapability capability) {
 			return true;
 		}
 
 		@Override
 		public RegionFilter setFilters(String namespace, Collection<String> filters)
 				throws InvalidSyntaxException {
 			throw new UnsupportedOperationException("TOP is immutable");
 		}
     };
 
     private Object monitor = new Object();
     private Collection<Filter> packages = null;
 	private Collection<Filter> requires = null;
 	private Collection<Filter> hosts = null;
 	private Collection<Filter> services = null;
     private Collection<Filter> bundles = null;
     private Collection<Filter> all = null;
     private Map<String, Collection<Filter>> generics = null;
  
     /**
      * Determines whether this filter allows the given bundle
      * 
      * @param bundle the bundle
      * @return <code>true</code> if the bundle is allowed and <code>false</code>otherwise
      */
     public boolean isBundleAllowed(Bundle bundle) {
     	HashMap<String, Object> attrs = new HashMap<String, Object>(3);
     	String bsn = bundle.getSymbolicName();
     	if (bsn != null)
    		attrs.put(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, bsn);
     	attrs.put(org.osgi.framework.Constants.BUNDLE_VERSION_ATTRIBUTE, bundle.getVersion());
     	return isBundleAllowed(attrs);
     }
 
     /**
      * Determines whether this filter allows the given bundle
      * 
      * @param bundle the bundle revision
      * @return <code>true</code> if the bundle is allowed and <code>false</code>otherwise
      */
     public boolean isBundleAllowed(BundleRevision bundle) {
     	HashMap<String, Object> attrs = new HashMap<String, Object>(3);
     	String bsn = bundle.getSymbolicName();
     	if (bsn != null)
    		attrs.put(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, bsn);
     	attrs.put(org.osgi.framework.Constants.BUNDLE_VERSION_ATTRIBUTE, bundle.getVersion());
     	return isBundleAllowed(attrs);
     }
 
     /**
      * Determines whether this filter allows the bundle with the given attributes
      * 
      * @param bundleAttributes the bundle attributes
      * @return <code>true</code> if the bundle is allowed and <code>false</code>otherwise
      */
     private boolean isBundleAllowed(Map<String, ?> bundleAttributes) {
     	synchronized (this.monitor) {
 			if (match(bundles, bundleAttributes))
 				return true;
 			return match(all, bundleAttributes);
 		}
     }
 
     private boolean match(Collection<Filter> filters, Map<String, ?> attrs) {
     	if (filters == null)
     		return false;
     	for (Filter filter : filters) {
 			if (filter.matches(attrs))
 				return true;
 		}
     	return false;
     }
 
     private boolean match(Collection<Filter> filters, ServiceReference<?> service) {
     	if (filters == null)
     		return false;
     	for (Filter filter : filters) {
 			if (filter.match(service))
 				return true;
 		}
     	return false;
     }
 
     /**
      * Determines whether this filter allows the given service reference.
      * 
      * @param service the service reference of the service
      * @return <code>true</code> if the service is allowed and <code>false</code>otherwise
      */
     public boolean isServiceAllowed(ServiceReference<?> service) {
     	synchronized (this.monitor) {
 			if (match(services, service))
 				return true;
 			return match(all, service);
 		}
     }
 
     /**
      * Determines whether this filter allows the given capability.
      * 
      * @param capability the bundle capability
      * @return <code>true</code> if the capability is allowed and <code>false</code>otherwise
      */
     public boolean isCapabilityAllowed(BundleCapability capability) {
     	String namespace = capability.getNamespace();
     	Collection<Filter> filter = null;
     	synchronized (this.monitor) {
 			if (VISIBLE_PACKAGE_NAMESPACE.equals(namespace))
 				filter = packages;
 			else if (VISIBLE_REQUIRE_NAMESPACE.equals(namespace))
 				filter = requires;
 			else if (VISIBLE_HOST_NAMESPACE.equals(namespace))
 				filter = hosts;
 			else
 				filter = generics == null ? null : generics.get(namespace);
 			Map<String, ?> attrs = capability.getAttributes();
 			if (match(filter, attrs))
 				return true;
 			return match(all, attrs);
 		}
     }
 
     public RegionFilter setFilters(String namespace, Collection<String> filters) throws InvalidSyntaxException {
     	if (namespace == null || filters == null)
     		throw new IllegalArgumentException("The namespace and filters must not be null.");
     	Collection<Filter> filterImpls = createFilters(filters);
     	synchronized (monitor) {
 			if (VISIBLE_PACKAGE_NAMESPACE.equals(namespace))
 				packages = filterImpls;
 			else if (VISIBLE_REQUIRE_NAMESPACE.equals(namespace))
 				requires  = filterImpls;
 			else if (VISIBLE_HOST_NAMESPACE.equals(namespace))
 				hosts = filterImpls;
 			else if (VISIBLE_SERVICE_NAMESPACE.equals(namespace))
 				services = filterImpls;
 			else if (VISIBLE_BUNDLE_NAMESPACE.equals(namespace))
 				bundles = filterImpls;
 			else if (VISIBLE_ALL_NAMESPACE.equals(namespace))
 				all = filterImpls;
 			else {
 				if (generics == null)
 					generics = new HashMap<String, Collection<Filter>>();
 				generics.put(namespace, filterImpls);
 			}
     	}
     	return this;
     }
 
     public RegionFilter removeFilters(String namespace) {
     	if (namespace == null)
     		throw new IllegalArgumentException("The namespace must not be null.");
    	synchronized (monitor) {
 			if (VISIBLE_PACKAGE_NAMESPACE.equals(namespace))
 				packages = null;
 			else if (VISIBLE_REQUIRE_NAMESPACE.equals(namespace))
 				requires  = null;
 			else if (VISIBLE_HOST_NAMESPACE.equals(namespace))
 				hosts = null;
 			else if (VISIBLE_SERVICE_NAMESPACE.equals(namespace))
 				services = null;
 			else if (VISIBLE_BUNDLE_NAMESPACE.equals(namespace))
 				bundles = null;
 			else if (VISIBLE_ALL_NAMESPACE.equals(namespace))
 				all = null;
 			else {
 				if (generics == null)
 					generics = new HashMap<String, Collection<Filter>>();
 				generics.remove(namespace);
 			}
     	}
     	return this;
     }
 
     public Collection<String> getFilters(String namespace) {
     	if (namespace == null)
     		throw new IllegalArgumentException("The namespace not be null.");
     	synchronized (monitor) {
 			if (VISIBLE_PACKAGE_NAMESPACE.equals(namespace))
 				return getFilters(packages);
 			else if (VISIBLE_REQUIRE_NAMESPACE.equals(namespace))
 				return getFilters(requires);
 			else if (VISIBLE_HOST_NAMESPACE.equals(namespace))
 				return getFilters(hosts);
 			else if (VISIBLE_SERVICE_NAMESPACE.equals(namespace))
 				return getFilters(services);
 			else if (VISIBLE_BUNDLE_NAMESPACE.equals(namespace))
 				return getFilters(bundles);
 			else if (VISIBLE_ALL_NAMESPACE.equals(namespace))
 				return getFilters(all);
 			else {
 				if (generics != null)
 					return getFilters(generics.get(namespace));
 			}
 			return null;
     	}
     }
 
     private static Collection<String> getFilters(Collection<Filter> filters) {
     	Collection<String> result = new ArrayList<String>(filters.size());
     	for (Filter filter : filters) {
 			result.add(filter.toString());
 		}
     	return result;
     }
 
     private static Collection<Filter> createFilters(Collection<String> filters) throws InvalidSyntaxException {
     	if (filters == null)
     		return null;
     	Collection<Filter> result = new ArrayList<Filter>(filters.size());
     	//TODO Using FrameworkUtil for now;  Should move to using BundleContext
     	for (String filter : filters) {
 			result.add(FrameworkUtil.createFilter(filter));
     	}
     	return result;
     }
 }
