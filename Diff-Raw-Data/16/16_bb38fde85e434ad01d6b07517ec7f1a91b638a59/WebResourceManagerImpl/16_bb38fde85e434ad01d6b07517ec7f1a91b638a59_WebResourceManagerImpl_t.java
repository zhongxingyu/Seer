 package com.atlassian.plugin.webresource;
 
 import com.atlassian.plugin.FileCacheService;
 import com.atlassian.plugin.ModuleDescriptor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
 import org.apache.commons.codec.binary.Hex;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static com.google.common.collect.Iterables.addAll;
 import static com.google.common.collect.Iterables.concat;
 import static com.google.common.collect.Iterables.contains;
 import static com.google.common.collect.Iterables.transform;
 
 /**
  * A handy super-class that handles most of the resource management.
  * <p/>
  * To use this manager, you need to have the following UrlRewriteFilter code:
  * <pre>
  * &lt;rule>
  * &lt;from>^/s/(.*)/_/(.*)&lt;/from>
  * &lt;run class="com.atlassian.plugin.servlet.ResourceDownloadUtils" method="addCachingHeaders" />
  * &lt;to type="forward">/$2&lt;/to>
  * &lt;/rule>
  * </pre>
  * <p/>
  * Sub-classes should implement the abstract methods
  */
 public class WebResourceManagerImpl implements WebResourceManager
 {
     private static final Logger log = LoggerFactory.getLogger(WebResourceManagerImpl.class);
 
     static final String STATIC_RESOURCE_PREFIX = "s";
     static final String STATIC_RESOURCE_SUFFIX = "_";
 
     static final String REQUEST_CACHE_RESOURCE_KEY = "plugin.webresource.names";
     static final String REQUEST_CACHE_CONTEXT_KEY = "plugin.webresource.contexts";
 
     protected final WebResourceIntegration webResourceIntegration;
     protected final PluginResourceLocator pluginResourceLocator;
     private final WebResourceUrlProvider webResourceUrlProvider;
     protected final ResourceBatchingConfiguration batchingConfiguration;
     protected final ResourceDependencyResolver dependencyResolver;
     protected static final List<WebResourceFormatter> webResourceFormatters = Arrays.asList(CssWebResource.FORMATTER, JavascriptWebResource.FORMATTER);
     private final FileCacheService fileCacheService;
 
     private static final boolean IGNORE_SUPERBATCHING = false;
 
     public WebResourceManagerImpl(final PluginResourceLocator pluginResourceLocator, final WebResourceIntegration webResourceIntegration, final WebResourceUrlProvider webResourceUrlProvider)
     {
         this(pluginResourceLocator, webResourceIntegration, webResourceUrlProvider,
                 new DefaultResourceBatchingConfiguration());
     }
 
     public WebResourceManagerImpl(final PluginResourceLocator pluginResourceLocator, final WebResourceIntegration webResourceIntegration, final WebResourceUrlProvider webResourceUrlProvider, final ResourceBatchingConfiguration batchingConfiguration)
     {
         this(pluginResourceLocator, webResourceIntegration, webResourceUrlProvider, batchingConfiguration, new DefaultResourceDependencyResolver(
                 webResourceIntegration, batchingConfiguration));
     }
 
     public WebResourceManagerImpl(final PluginResourceLocator pluginResourceLocator, final WebResourceIntegration webResourceIntegration, final WebResourceUrlProvider webResourceUrlProvider, final ResourceBatchingConfiguration batchingConfiguration, final ResourceDependencyResolver dependencyResolver)
     {
         this(pluginResourceLocator,webResourceIntegration,webResourceUrlProvider,batchingConfiguration,dependencyResolver,null);
     }
 
     public WebResourceManagerImpl(final PluginResourceLocator pluginResourceLocator, final WebResourceIntegration webResourceIntegration, final WebResourceUrlProvider webResourceUrlProvider, final ResourceBatchingConfiguration batchingConfiguration, final ResourceDependencyResolver dependencyResolver, final FileCacheService temp)
     {
         this.pluginResourceLocator = pluginResourceLocator;
         this.webResourceIntegration = webResourceIntegration;
         this.webResourceUrlProvider = webResourceUrlProvider;
         this.batchingConfiguration = batchingConfiguration;
         this.dependencyResolver = dependencyResolver;
         this.fileCacheService = temp;
     }
 
     public void requireResource(final String moduleCompleteKey)
     {
         log.debug("Requiring resource: " + moduleCompleteKey);
         Iterable<String> dependencies = transformModuleDescriptorsToModuleKeys(dependencyResolver.getDependencies(moduleCompleteKey, batchingConfiguration.isSuperBatchingEnabled()));
         addAll(getIncludedResourceNames(), dependencies);
     }
 
     public void requireResourcesForContext(final String context)
     {
         getIncludedContexts().add(context);
     }
 
     private Set<String> getIncludedContexts()
     {
         return getOrCreateFromRequestCache(REQUEST_CACHE_CONTEXT_KEY);
     }
 
     private Set<String> getIncludedResourceNames()
     {
         return getOrCreateFromRequestCache(REQUEST_CACHE_RESOURCE_KEY);
     }
 
     private Set<String> getOrCreateFromRequestCache(final String key)
     {
         final Map<String, Object> cache = webResourceIntegration.getRequestCache();
         @SuppressWarnings("unchecked")
         Set<String> set = (Set<String>) cache.get(key);
         if (set == null)
         {
             set = new LinkedHashSet<String>();
             cache.put(key, set);
         }
         return set;
     }
 
     private void clear()
     {
         log.debug("Clearing included resources and contexts");
         getIncludedResourceNames().clear();
         getIncludedContexts().clear();
     }
 
     /**
      * This is the equivalent of of calling {@link #includeResources(Writer, UrlMode, WebResourceFilter)} with
      * {@link UrlMode#AUTO} and a {@link DefaultWebResourceFilter}.
      *
      * @see #includeResources(Writer, UrlMode, WebResourceFilter)
      */
     public void includeResources(final Writer writer)
     {
         includeResources(writer, UrlMode.AUTO);
     }
 
     public void includeResources(final Iterable<String> moduleCompleteKeys, final Writer writer, final UrlMode urlMode)
     {
        Iterable<String> resources = Lists.newArrayList();
         for (final String moduleCompleteKey : moduleCompleteKeys)
         {
             // Include resources from the super batch as we don't include the super batch itself
             final Iterable<String> dependencies = transformModuleDescriptorsToModuleKeys(dependencyResolver.getDependencies(moduleCompleteKey, false));
            resources = concat(resources, dependencies);
         }

        // Resolve duplicates
        resources = ImmutableSet.copyOf(resources);
         writeResourceTags(getModuleResources(resources, Collections.<String> emptyList(), DefaultWebResourceFilter.INSTANCE), writer, urlMode);
     }
 
     /**
      * This is the equivalent of of calling {@link #includeResources(Writer, UrlMode, WebResourceFilter)} with
      * the given url mode and a {@link DefaultWebResourceFilter}.
      *
      * @see #includeResources(Writer, UrlMode, WebResourceFilter)
      */
     public void includeResources(final Writer writer, final UrlMode urlMode)
     {
         includeResources(writer, urlMode, DefaultWebResourceFilter.INSTANCE);
     }
 
     /**
      * Writes out the resource tags to the previously required resources called via requireResource methods for the
      * specified url mode and resource filter. Note that this method will clear the list of previously required resources.
      *
      * @param writer the writer to write the links to
      * @param urlMode the url mode to write resource url links in
      * @param webResourceFilter the resource filter to filter resources on
      * @since 2.4
      */
     public void includeResources(final Writer writer, final UrlMode urlMode, final WebResourceFilter webResourceFilter)
     {
         writeIncludedResources(writer, urlMode, webResourceFilter);
         clear();
     }
 
     /**
      * This is the equivalent of calling {@link #getRequiredResources(UrlMode, WebResourceFilter)} with
      * {@link UrlMode#AUTO} and a {@link DefaultWebResourceFilter}.
      *
      * @see #getRequiredResources(UrlMode, WebResourceFilter)
      */
     public String getRequiredResources()
     {
         return getRequiredResources(UrlMode.AUTO);
     }
 
     /**
      * This is the equivalent of calling {@link #getRequiredResources(UrlMode, WebResourceFilter)} with the given url
      * mode and a {@link DefaultWebResourceFilter}.
      *
      * @see #getRequiredResources(UrlMode, WebResourceFilter)
      */
     public String getRequiredResources(final UrlMode urlMode)
     {
         return getRequiredResources(urlMode, DefaultWebResourceFilter.INSTANCE);
     }
 
     /**
      * Returns a String of the resources tags to the previously required resources called via requireResource methods
      * for the specified url mode and resource filter. Note that this method will NOT clear the list of previously
      * required resources.
      *
      * @param urlMode the url mode to write out the resource tags
      * @param webResourceFilter the web resource filter to filter resources on
      * @return a String of the resource tags
      * @since 2.4
      */
     public String getRequiredResources(final UrlMode urlMode, final WebResourceFilter webResourceFilter)
     {
         final StringWriter writer = new StringWriter();
         writeIncludedResources(writer, urlMode, webResourceFilter);
         return writer.toString();
     }
 
     /**
      * Write all currently included resources to the given writer.
      */
     private void writeIncludedResources(final Writer writer, final UrlMode urlMode, final WebResourceFilter filter)
     {
         Iterable<PluginResource> resourcesToInclude = getSuperBatchResources(filter);
 
         final ContextBatchBuilder builder = new ContextBatchBuilder(pluginResourceLocator, dependencyResolver, batchingConfiguration, fileCacheService);
         resourcesToInclude = concat(resourcesToInclude, builder.build(getIncludedContexts(), filter));
         for (final String skippedResource : builder.getSkippedResources())
         {
             requireResource(skippedResource);
         }
 
         resourcesToInclude = concat(resourcesToInclude, getModuleResources(getIncludedResourceNames(), builder.getAllIncludedResources(), filter));
 
         writeResourceTags(resourcesToInclude, writer, urlMode);
     }
 
     /**
      * Get all super-batch resources that match the given filter. If superbatching is disabled this will just
      * return the empty list.
      *
      * Package private so it can be tested independently.
      */
     List<PluginResource> getSuperBatchResources(final WebResourceFilter filter)
     {
          if (!batchingConfiguration.isSuperBatchingEnabled())
                 {
                     return Collections.emptyList();
                 }
 
                 final Iterable<WebResourceModuleDescriptor> superBatchModuleKeys = dependencyResolver.getSuperBatchDependencies();
                 final List<PluginResource> resources = new ArrayList<PluginResource>();
                 final List<PluginResource> pluginResources = new ArrayList<PluginResource>();
                 // as we need to sort the plugin resources to create a consistent hash we keep a reference with the order in it
                 final List<PluginResource> orderedPluginResources = new ArrayList<PluginResource>();
 
                 // This is necessarily quite complicated. We need distinct superbatch resources for each combination of
                 // resourceFormatter (i.e. separate CSS or JS resources), and also each unique combination of
                 // BATCH_PARAMS (i.e. separate superbatches for print stylesheets, IE only stylesheets, and IE only print
                 // stylesheets if they ever exist in the future)
                 for (final WebResourceFormatter formatter : webResourceFormatters)
                 {
                     final Set<Map<String, String>> alreadyIncluded = new HashSet<Map<String, String>>();
                     for (final WebResourceModuleDescriptor moduleDescriptor : superBatchModuleKeys)
                     {
                         for (final PluginResource pluginResource : pluginResourceLocator.getPluginResources(moduleDescriptor.getCompleteKey()))
                         {
                             if (formatter.matches(pluginResource.getResourceName()) && filter.matches(pluginResource.getResourceName()))
                             {
                                 final Map<String, String> batchParamsMap = new HashMap<String, String>(PluginResourceLocator.BATCH_PARAMS.length);
                                 for (final String s : PluginResourceLocator.BATCH_PARAMS)
                                 {
                                     batchParamsMap.put(s, pluginResource.getParams().get(s));
                                 }
 
                                 if (!alreadyIncluded.contains(batchParamsMap))
                                 {
                                     pluginResources.add(pluginResource);
                                     alreadyIncluded.add(batchParamsMap);
                                     orderedPluginResources.add(pluginResource);
                                 }
                             }
                         }
                     }
                 }
                 Collections.sort(pluginResources, new Comparator<PluginResource>()
                 {
                     public int compare(PluginResource o1, PluginResource o2)
                     {
                         return o1.getModuleCompleteKey().compareTo(o2.getModuleCompleteKey());
                     }
                 });
                 String hash = createHash(pluginResources);
                 for (PluginResource resource : orderedPluginResources)
                 {
                     resources.add(SuperBatchPluginResource.createBatchFor(resource, hash, fileCacheService));
                 }
                 return resources;
     }
 
 
     private String createHash(List<PluginResource> resources)
     {
         try
         {
             MessageDigest md5 = MessageDigest.getInstance("MD5");
             for (PluginResource moduleDescriptor : resources)
             {
                 String version = moduleDescriptor.getVersion(webResourceIntegration);
                 md5.update(moduleDescriptor.getModuleCompleteKey().getBytes("UTF8"));
                 md5.update(version.getBytes("UTF8"));
                 List<Map.Entry<String, String>> params = new ArrayList<Map.Entry<String, String>>(moduleDescriptor.getParams().entrySet());
                 Collections.sort(params, new Comparator<Map.Entry<String, String>>()
                 {
                     public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2)
                     {
                         int cmpvalue = o1.getKey().compareTo(o2.getKey());
                         if (cmpvalue == 0)
                         {
                             cmpvalue = o1.getValue().compareTo(o2.getValue());
                         }
                         return cmpvalue;
                     }
                 });
                 for (Map.Entry<String, String> entry : params)
                 {
                     md5.update((entry.getKey() + entry.getValue()).getBytes("UTF8"));
                 }
 
             }
 
             return new String(Hex.encodeHex(md5.digest()));
         }
         catch (NoSuchAlgorithmException e)
         {
             throw new AssertionError("MD5 hashing algorithm is not available.");
         }
         catch (UnsupportedEncodingException e)
         {
             throw new AssertionError("UTF-8 encoding is not available.");
         }
     }
 
     private Iterable<PluginResource> getModuleResources(final Iterable<String> webResourcePluginModuleKeys, final Iterable<String> batchedModules, final WebResourceFilter filter)
     {
         final List<PluginResource> includedResources = new ArrayList<PluginResource>();
         for (final String moduleKey : webResourcePluginModuleKeys)
         {
             if (contains(batchedModules, moduleKey))
             {
                 // skip this resource if it is already in a batch
                 continue;
             }
 
             final List<PluginResource> moduleResources = pluginResourceLocator.getPluginResources(moduleKey);
             for (final PluginResource moduleResource : moduleResources)
             {
                 if (filter.matches(moduleResource.getResourceName()))
                 {
                     includedResources.add(moduleResource);
                 }
                 else
                 {
                     log.debug("Resource [" + moduleResource.getResourceName() + "] excluded by filter");
                 }
             }
         }
         return includedResources;
     }
 
     /**
      * Write the tags for the given set of resources to the writer. Writing will be done in order of
      * webResourceFormatters so that all CSS resources will be output before Javascript.
      */
     private void writeResourceTags(final Iterable<PluginResource> resourcesToInclude, final Writer writer, final UrlMode urlMode)
     {
         for (final WebResourceFormatter formatter : webResourceFormatters)
         {
             for (final Iterator<PluginResource> iter = resourcesToInclude.iterator(); iter.hasNext(); )
             {
                 final PluginResource resource = iter.next();
                 if (formatter.matches(resource.getResourceName()))
                 {
                     writeResourceTag(urlMode, resource, formatter, writer);
                     iter.remove();
                 }
             }
         }
 
         for (final PluginResource resource : resourcesToInclude)
         {
             writeContentAndSwallowErrors(
                     "<!-- Error loading resource \"" + resource.getModuleCompleteKey() + "\".  No resource formatter matches \"" + resource.getResourceName() + "\" -->\n",
                     writer);
         }
     }
 
     private void writeResourceTag(final UrlMode urlMode, final PluginResource resource, final WebResourceFormatter formatter, final Writer writer)
     {
         String url = resource.getUrl();
         if (resource.isCacheSupported())
         {
             url = webResourceUrlProvider.getStaticResourcePrefix(resource.getVersion(webResourceIntegration), urlMode) + url;
         }
         else
         {
             url = webResourceUrlProvider.getBaseUrl(urlMode) + url;
         }
         writeContentAndSwallowErrors(formatter.formatResource(url, resource.getParams()), writer);
     }
 
     public void requireResource(final String moduleCompleteKey, final Writer writer)
     {
         requireResource(moduleCompleteKey, writer, UrlMode.AUTO);
     }
 
     public void requireResource(final String moduleCompleteKey, final Writer writer, final UrlMode urlMode)
     {
         final Iterable<String> allDependentModuleKeys = transformModuleDescriptorsToModuleKeys(dependencyResolver.getDependencies(moduleCompleteKey, IGNORE_SUPERBATCHING));
         final Iterable<String> empty = Collections.<String>emptyList();
         final Iterable<PluginResource> resourcesToInclude = getModuleResources(allDependentModuleKeys, empty, DefaultWebResourceFilter.INSTANCE);
         writeResourceTags(resourcesToInclude, writer, urlMode);
     }
 
     public String getResourceTags(final String moduleCompleteKey)
     {
         return getResourceTags(moduleCompleteKey, UrlMode.AUTO);
     }
 
     public String getResourceTags(final String moduleCompleteKey, final UrlMode urlMode)
     {
         final StringWriter writer = new StringWriter();
         requireResource(moduleCompleteKey, writer, urlMode);
         return writer.toString();
     }
 
     private void writeContentAndSwallowErrors(final String content, final Writer writer)
     {
         try
         {
             writer.write(content);
         }
         catch (final IOException e)
         {
             log.debug("Ignoring", e);
         }
     }
 
     public String getStaticResourcePrefix()
     {
         return getStaticResourcePrefix(UrlMode.AUTO);
     }
 
     public String getStaticResourcePrefix(final UrlMode urlMode)
     {
         return webResourceUrlProvider.getStaticResourcePrefix(urlMode);
     }
 
     public String getStaticResourcePrefix(final String resourceCounter)
     {
         return getStaticResourcePrefix(resourceCounter, UrlMode.AUTO);
     }
 
     public String getStaticResourcePrefix(final String resourceCounter, final UrlMode urlMode)
     {
         return webResourceUrlProvider.getStaticResourcePrefix(resourceCounter, urlMode);
     }
 
     public String getStaticPluginResource(final String moduleCompleteKey, final String resourceName)
     {
         return getStaticPluginResource(moduleCompleteKey, resourceName, UrlMode.AUTO);
     }
 
     public String getStaticPluginResource(final String moduleCompleteKey, final String resourceName, final UrlMode urlMode)
     {
         return webResourceUrlProvider.getStaticPluginResourceUrl(moduleCompleteKey, resourceName, urlMode);
     }
 
     /**
      * @return "{base url}/s/{build num}/{system counter}/{plugin version}/_/download/resources/{plugin.key:module.key}/{resource.name}"
      */
     @SuppressWarnings("unchecked")
     public String getStaticPluginResource(final ModuleDescriptor moduleDescriptor, final String resourceName)
     {
         return getStaticPluginResource(moduleDescriptor, resourceName, UrlMode.AUTO);
     }
 
     public String getStaticPluginResource(final ModuleDescriptor moduleDescriptor, final String resourceName, final UrlMode urlMode)
     {
         return webResourceUrlProvider.getStaticPluginResourceUrl(moduleDescriptor, resourceName, urlMode);
     }
 
     private Iterable<String> transformModuleDescriptorsToModuleKeys(Iterable<WebResourceModuleDescriptor> descriptors)
     {
         return transform(descriptors, new TransformDescriptorToKey());
     }
 
     /* Deprecated methods */
 
     /**
      * @deprecated Use {@link #getStaticPluginResource(com.atlassian.plugin.ModuleDescriptor, String)} instead
      */
     @Deprecated
     @SuppressWarnings("unchecked")
     public String getStaticPluginResourcePrefix(final ModuleDescriptor moduleDescriptor, final String resourceName)
     {
         return getStaticPluginResource(moduleDescriptor, resourceName);
     }
 
     /**
      * @deprecated Since 2.2
      */
     @Deprecated
     private static final String REQUEST_CACHE_MODE_KEY = "plugin.webresource.mode";
 
     /**
      * @deprecated Since 2.2.
      */
     @Deprecated
     public void setIncludeMode(final IncludeMode includeMode)
     {
         webResourceIntegration.getRequestCache().put(REQUEST_CACHE_MODE_KEY, includeMode);
     }
 }
