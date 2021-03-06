 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.sling.jcr.resource.internal.helper;
 
 import org.apache.commons.collections.FastTreeMap;
 import org.apache.sling.api.resource.Resource;
 import org.apache.sling.api.resource.ResourceProvider;
 import org.apache.sling.api.resource.ResourceResolver;
 import org.apache.sling.api.resource.SyntheticResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 /**
  * The <code>ResourceProviderEntry</code> class represents a node in the tree of
  * resource providers spanned by the root paths of the provider resources.
  * <p>
  * This class is comparable to itself to help keep the child entries list sorted
  * by their prefix.
  */
 public class ResourceProviderEntry2 implements
         Comparable<ResourceProviderEntry2> {
 
     /**
      * 
      */
     private static final long serialVersionUID = 7420631325909144862L;
     
     private static Logger LOGGER = LoggerFactory.getLogger(ResourceProviderEntry2.class);
 
     // the path to resources provided by the resource provider of this
     // entry. this path is relative to the path of the parent resource
     // provider entry and has no trailing slash.
     private final String path;
 
     // the path to resources provided by the resource provider of this
     // entry. this is the same path as the path field but with a trailing
     // slash to be used as a prefix match resource paths to resolve
     private final String prefix;
 
     // the resource provider kept in this entry supporting resources at and
     // below the path of this entry.
     private WrappedResourceProvider[] providers = new WrappedResourceProvider[0];
 
     private long ttime = 0L;
 
     private long nmiss = 0L;
 
     private long nsynthetic = 0L;
 
     private long nreal = 0L;
 
     private FastTreeMap storageMap = new FastTreeMap();
 
     /**
      * Creates an instance of this class with the given path relative to the
      * parent resource provider entry, encapsulating the given ResourceProvider,
      * and a number of inital child entries.
      * 
      * @param path
      *            The relative path supported by the provider
      * @param provider
      *            The resource provider to encapsulate by this entry.
      */
     public ResourceProviderEntry2(String path, ResourceProvider[] providerList) {
         if (path.endsWith("/")) {
             this.path = path.substring(0, path.length() - 1);
             this.prefix = path;
         } else {
             this.path = path;
             this.prefix = path + "/";
         }
         if ( providerList != null ) {
           providers = new WrappedResourceProvider[providerList.length];
           for ( int i = 0; i < providerList.length; i++ ) {
             if ( providerList[i] instanceof WrappedResourceProvider ) {
               providers[i] = (WrappedResourceProvider) providerList[i];
             } else {
               providers[i] = new WrappedResourceProvider(providerList[i], null);
             }
           }
         }
         
         // this will consume slightly more memory but ensures read is fast.
         storageMap.setFast(true);
     }
 
     /**
      * Returns the resource provider contained in this entry
      */
     public ResourceProvider[] getResourceProviders() {
         return providers;
     }
 
     /**
      * Returns the resource with the given path or <code>null</code> if neither
      * the resource provider of this entry nor the resource provider of any of
      * the child entries can provide the resource.
      * 
      * @param path
      *            The path to the resource to return.
      * @return The resource for the path or <code>null</code> if no resource can
      *         be found.
      * @throws org.apache.sling.api.SlingException
      *             if an error occurrs trying to access an existing resource.
      */
     public Resource getResource(ResourceResolver resourceResolver, String path) {
         return getInternalResource(resourceResolver, path);
     }
 
     public Iterator<Resource> listChildren(final Resource resource) {
         LOGGER.debug("Child Iterator for {}",resource.getPath());
         return new Iterator<Resource>() {
             private final Iterator<ResourceProvider> providers;
 
             private Iterator<Resource> resources;
 
             private Resource nextResource;
 
             private Map<String, Resource> delayed;
 
             private Set<String> visited;
             
             private String iteratorPath;
 
             private Iterator<Resource> delayedIter;
 
             {
                 String path = resource.getPath();
                 if (!path.endsWith("/")) {
                     path += "/";
                 }
 
                 // gather the providers in linked set, such that we keep
                 // the order of addition and make sure we only get one entry
                 // for each resource provider
                 Set<ResourceProvider> providersSet = new LinkedHashSet<ResourceProvider>();
                 getResourceProviders(path, providersSet);
                 
                 LOGGER.debug(" Provider Set for path {} {} ",path,Arrays.toString(providersSet.toArray(new ResourceProvider[0])));
 
                 this.iteratorPath = path;
                 providers = providersSet.iterator();
                 delayed = new HashMap<String, Resource>();
                 visited = new HashSet<String>();
                 nextResource = seek();
             }
 
             public boolean hasNext() {
                 return nextResource != null;
             }
 
             public Resource next() {
                 if (!hasNext()) {
                     throw new NoSuchElementException();
                 }
 
                 Resource result = nextResource;
                 nextResource = seek();
                 LOGGER.debug("  Child Resoruce [{}] [{}] ", iteratorPath, result.getPath());
                 return result;
             }
 
             public void remove() {
                 throw new UnsupportedOperationException("remove");
             }
 
             private Resource seek() {
                 for (;;) {
                     while ((resources == null || !resources.hasNext())
                             && providers.hasNext()) {
                         ResourceProvider provider = providers.next();
                         resources = provider.listChildren(resource);
                         LOGGER.debug("     Checking Provider {} ", provider);
                     }
 
                     if (resources != null && resources.hasNext()) {
                         Resource res = resources.next();
                         String resPath = res.getPath();
 
                         if (visited.contains(resPath)) {
 
                             // ignore a path, we have already visited and
                             // ensure it will not be listed as a delayed
                             // resource at the end
                             delayed.remove(resPath);
 
                         } else if (res instanceof SyntheticResource) {
 
                             // don't return synthetic resources right away,
                             // since a concrete resource for the same path
                             // may be provided later on
                             delayed.put(resPath, res);
 
                         } else {
 
                             // we use this concrete, unvisited resource but
                             // mark it as visited
                             visited.add(resPath);
                             // also remove it from delayed if it was there.
                             if ( delayed.containsKey(resPath) ) {
                                 delayed.remove(resPath);
                             }
                             LOGGER.debug("      resource {} {}", resPath, res.getClass());
                             return res;
 
                         }
                     } else {
                         break;
                     }
                 }
 
                 // we exhausted all resource providers with their concrete
                 // resources. now lets do the delayed (synthetic) resources
                 if (delayedIter == null) {
                     delayedIter = delayed.values().iterator();
                 }
                 Resource res = delayedIter.hasNext() ? delayedIter.next() : null;
                 if ( res != null ) {
                     LOGGER.info("   D  resource {} {}", res.getPath(), res.getClass());
                 }
                 return res;
             }
         };
     }
 
     /**
      * Adds the given resource provider into the tree for the given prefix.
      * 
      * @return <code>true</code> if the provider could be entered into the
      *         subtree below this entry. Otherwise <code>false</code> is
      *         returned.
      */
     public boolean addResourceProvider(String prefix, ResourceProvider provider, Comparable<?> comparable) {
         synchronized (this) {
             String[] elements = split(prefix, '/');
             List<ResourceProviderEntry2> entryPath = new ArrayList<ResourceProviderEntry2>();
             entryPath.add(this); // add this the start so if the list is empty we have a position to add to
             populateProviderPath(entryPath, elements);
             for (int i = entryPath.size() - 1; i < elements.length; i++) {
                 String stubPrefix = elements[i];
                 ResourceProviderEntry2 rpe2 = new ResourceProviderEntry2(
                         stubPrefix, new ResourceProvider[0]);
                 entryPath.get(i).put(elements[i], rpe2);
                 entryPath.add(rpe2);
             }
             return entryPath.get(elements.length).addInternalProvider(new WrappedResourceProvider(provider, comparable));
 
         }
     }
 
 
     //------------------ Map methods, here so that we can delegate 2 maps together
     /**
      * @param string
      * @param rpe2
      */
     public void put(String key, ResourceProviderEntry2 value) {
         storageMap.put(key,value);
     }
     
     /**
      * @param element
      * @return
      */
     public boolean containsKey(String key) {
         return storageMap.containsKey(key);
     }
 
 
     /**
      * @param element
      * @return
      */
     public ResourceProviderEntry2 get(String key) {
         return (ResourceProviderEntry2) storageMap.get(key);
     }
 
     /**
      * @return
      */
     @SuppressWarnings("unchecked")
     public Collection<ResourceProviderEntry2> values() {
         return storageMap.values();
     }
 
     public boolean removeResourceProvider(String prefix,
             ResourceProvider resourceProvider, Comparable<?> comparable) {
         synchronized (this) {
             String[] elements = split(prefix, '/');
             List<ResourceProviderEntry2> entryPath = new ArrayList<ResourceProviderEntry2>();
             populateProviderPath(entryPath, elements);
            if (entryPath.size() == elements.length) {
                 // the last element is a perfect match;
                 return entryPath.get(entryPath.size()-1).removeInternalProvider(new WrappedResourceProvider(resourceProvider, comparable));
             }
             return false;
         }
     }
 
     // ---------- Comparable<ResourceProviderEntry> interface ------------------
 
     public int compareTo(ResourceProviderEntry2 o) {
         return prefix.compareTo(o.prefix);
     }
 
     // ---------- internal -----------------------------------------------------
 
     /**
      * Adds a list of providers to this entry.
      * 
      * @param provider
      */
     private boolean addInternalProvider(WrappedResourceProvider provider) {
         synchronized (providers) {
             int before = providers.length;
             Set<WrappedResourceProvider> set = new HashSet<WrappedResourceProvider>();
             if (providers != null) {
                 set.addAll(Arrays.asList(providers));
             }
             set.add(provider);
             providers = conditionalSort(set);
             return providers.length > before;
         }
 
     }
 
     /**
      * @param provider
      * @return
      */
     private boolean removeInternalProvider(WrappedResourceProvider provider) {
         synchronized (providers) {
             int before = providers.length;
             Set<WrappedResourceProvider> set = new HashSet<WrappedResourceProvider>();
             if (providers != null) {
                 set.addAll(Arrays.asList(providers));
             }
             set.remove(provider);
             providers = conditionalSort(set);
             return providers.length < before;
         }
     }
 
     /**
      * @param set
      * @return
      */
     private WrappedResourceProvider[] conditionalSort(Set<WrappedResourceProvider> set) {
 
         List<WrappedResourceProvider> providerList = new ArrayList<WrappedResourceProvider>(
                 set);
 
         Collections.sort(providerList, new Comparator<WrappedResourceProvider>() {
 
             @SuppressWarnings("unchecked")
             public int compare(WrappedResourceProvider o1, WrappedResourceProvider o2) {
                 Comparable c1 = o1.getComparable();
                 Comparable c2 = o2.getComparable();
                 if ( c1 == null && c2 == null ) {
                   return 0;
                 } 
                 if ( c1 == null ) {
                   return -1;
                 }
                 if ( c2 == null ) {
                   return 1;
                 }
                 return c1.compareTo(c2);
             }
         });
 
         return set.toArray(new WrappedResourceProvider[set.size()]);
     }
 
     /**
      * Get a of ResourceProvidersEntries leading to the fullPath in reverse
      * order.
      * 
      * @param fullPath
      *            the full path
      * @return a reverse order list of ProviderEntries to the path.
      */
     private void populateProviderPath(
         List<ResourceProviderEntry2> providerEntryPath, String[] elements) {
         ResourceProviderEntry2 base = this;
         if (elements != null) {
             for (String element : elements) {
                 if (element != null) {
                     if (base.containsKey(element)) {
                         base = base.get(element);
                         providerEntryPath.add(base);
                     } else {
                         break;
                     }
                 }
             }
         }
     }
 
 
     /**
      * Resolve a resource from a path into a Resource
      * 
      * @param resolver
      *            the ResourceResolver.
      * @param fullPath
      *            the Full path
      * @return null if no resource was found, a resource if one was found.
      */
     private Resource getInternalResource(ResourceResolver resourceResolver,
             String fullPath) {
         long start = System.currentTimeMillis();
         try {
 
             if (fullPath == null || fullPath.length() == 0
                     || fullPath.charAt(0) != '/') {
                 nmiss++;
                 LOGGER.debug("Not absolute {} :{}",fullPath,(System.currentTimeMillis() - start));
                 return null; // fullpath must be absolute
             }
             String[] elements = split(fullPath, '/'); 
             
             List<ResourceProviderEntry2> list = new ArrayList<ResourceProviderEntry2>();
             populateProviderPath(list, elements);
             // the path is in reverse order end first
 
             for(int i = list.size()-1; i >= 0; i--) {
                 ResourceProvider[] rps = list.get(i).getResourceProviders();
                 for (ResourceProvider rp : rps) {
 
                     Resource resource = rp.getResource(resourceResolver,
                             fullPath);
                     if (resource != null) {
                         nreal++;
                         LOGGER.debug("Resolved Full {} using {} from {} ",new Object[]{
                                 fullPath, rp, Arrays.toString(rps)});
                         return resource;
                     }
                 }
             }
             
             // resolve against this one
             ResourceProvider[] rps = getResourceProviders();
             for (ResourceProvider rp : rps) {
                 Resource resource = rp.getResource(resourceResolver, fullPath);
                 if (resource != null) {
                     nreal++;
                     LOGGER.debug("Resolved Base {} using {} ", fullPath, rp);
                     return resource;
                 }
             }
             
             // query: /libs/sling/servlet/default
             // resource Provider: libs/sling/servlet/default/GET.servlet
             // list will match libs, sling, servlet, default 
             // and there will be no resource provider at the end
             if (list.size() > 0 && list.size() == elements.length ) {
                 if ( list.get(list.size()-1).getResourceProviders().length == 0 ) {
                     nsynthetic++;
                     LOGGER.debug("Resolved Synthetic {}", fullPath);
                     return new SyntheticResource(resourceResolver,
                             fullPath,
                             ResourceProvider.RESOURCE_TYPE_SYNTHETIC);
                 }
             }
 
 
 
             LOGGER.debug("Resource null {} ", fullPath);
             nmiss++;
             return null;
         } catch (Exception ex) {
             LOGGER.debug("Failed! ",ex);
             return null;
         } finally {
             ttime += System.currentTimeMillis() - start;
         }
     }
 
 
     /**
      * Returns all resource providers which provider resources whose prefix is
      * the given path.
      * 
      * @param path
      *            The prefix path to match the resource provider roots against
      * @param providers
      *            The set of already found resource providers to which any
      *            additional resource providers are added.
      */
     private void getResourceProviders(String path,
             Set<ResourceProvider> providers) {
         String[] elements = split(path, '/');
         ResourceProviderEntry2 base = this;
         for (String element : elements ) {
             if ( base.containsKey(element)) {
                 base = base.get(element);
             } else {
                 base = null;
                 break;
             }
         }
         // the path has been exausted and there is a subtree to be collected, so go and collect it.
         if ( base != null ) {
             getResourceProviders(base, providers);
         }
         // add in providers at this node in the tree, ie the root provider
         for ( ResourceProvider rp : getResourceProviders() ) {
             providers.add(rp);
         }
 
     }
 
     /**
      * @param base
      * @param providers2
      */
     private void getResourceProviders(ResourceProviderEntry2 entry,
             Set<ResourceProvider> providers) {
         // recurse down the tree
         LOGGER.debug(" Gathering For {} ",entry.prefix);
         for ( ResourceProviderEntry2 e : entry.values() ) {
             getResourceProviders(e, providers);
         }
         // add in providers at this node in the tree.
         for ( ResourceProvider rp : entry.getResourceProviders() ) {
             providers.add(rp);
         }
     }
 
     /**
      * @param st
      * @param sep
      * @return an array of the strings between the separator
      */
     private String[] split(String st, char sep) {
 
         if (st == null) {
             return new String[0];
         }
         char[] pn = st.toCharArray();
         if (pn.length == 0) {
             return new String[0];
         }
         if (pn.length == 1 && pn[0] == sep) {
             return new String[0];
         }
         int n = 1;
         int start = 0;
         int end = pn.length;
         while (start < end && sep == pn[start])
             start++;
         while (start < end && sep == pn[end - 1])
             end--;
         for (int i = start; i < end; i++) {
             if (sep == pn[i]) {
                 n++;
             }
         }
         String[] e = new String[n];
         int s = start;
         int j = 0;
         for (int i = start; i < end; i++) {
             if (pn[i] == sep) {
                 e[j++] = new String(pn, s, i - s);
                 s = i + 1;
             }
         }
         if (s < end) {
             e[j++] = new String(pn, s, end - s);
         }
         return e;
     }
 
     /**
      * @return
      */
     public String getResolutionStats() {
         long tot = nreal + nsynthetic + nmiss;
         if (tot == 0) {
             return null;
         }
         float n = tot;
         float t = ttime;
         float persec = 1000 * n / t;
         float avgtime = t / n;
 
         String stat = "Resolved: Real(" + nreal + ") Synthetic(" + nsynthetic
                 + ") Missing(" + nmiss + ") Total(" + tot + ") at " + persec
                 + " ops/sec avg " + avgtime + " ms";
         ttime = nmiss = nsynthetic = nreal = 0L;
         return stat;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see java.util.AbstractMap#toString()
      */
     @Override
     public String toString() {
         return this.path;
         //"{path:\"" + this.path + "\", providers:"+Arrays.toString(getResourceProviders())+", map:" + storageMap.toString() + "}";
     }
 
 }
