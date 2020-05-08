 package com.atlassian.plugin.webresource;
 
 import com.atlassian.plugin.ModuleDescriptor;
 import com.atlassian.plugin.Plugin;
 import org.apache.commons.collections.set.ListOrderedSet;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.*;
 
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
     private static final Log log = LogFactory.getLog(WebResourceManagerImpl.class);
 
     static final String STATIC_RESOURCE_PREFIX = "s";
     static final String STATIC_RESOURCE_SUFFIX = "_";
 
     static final String REQUEST_CACHE_RESOURCE_KEY = "plugin.webresource.names";
 
     protected final WebResourceIntegration webResourceIntegration;
     protected final PluginResourceLocator pluginResourceLocator;
     protected static final List<WebResourceFormatter> webResourceFormatters = Arrays.< WebResourceFormatter>asList(
         new CssWebResourceFormatter(),
         new JavascriptWebResourceFormatter()
     );
 
     public WebResourceManagerImpl(PluginResourceLocator pluginResourceLocator, WebResourceIntegration webResourceIntegration)
     {
         this.pluginResourceLocator = pluginResourceLocator;
         this.webResourceIntegration = webResourceIntegration;
     }
 
     public void requireResource(String moduleCompleteKey)
     {
         log.info("Requiring resource: " + moduleCompleteKey);
         Map cache = webResourceIntegration.getRequestCache();
         Collection webResourceNames = (Collection) cache.get(REQUEST_CACHE_RESOURCE_KEY);
         if (webResourceNames == null)
         {
             webResourceNames = new ListOrderedSet();
         }
 
         ListOrderedSet resources = new ListOrderedSet();
         addResourceWithDependencies(moduleCompleteKey, resources, new Stack<String>());
         webResourceNames.addAll(resources);
         cache.put(REQUEST_CACHE_RESOURCE_KEY, webResourceNames);
     }
 
     /**
      * Adds the resources as well as its dependencies in order to the given set. This method uses recursion
      * to add a resouce's dependent resources also to the set. You should call this method with a new stack
      * passed to the last parameter.
      *
      * @param moduleKey the module complete key to add as well as its dependencies
      * @param orderedResourceKeys an ordered list set where the resources are added in order
      * @param stack where we are in the dependency tree
      */
     private void addResourceWithDependencies(String moduleKey, ListOrderedSet orderedResourceKeys, Stack<String> stack)
     {
         if (stack.contains(moduleKey))
         {
             log.warn("Cyclic plugin resource dependency has been detected with: " + moduleKey + "\n" +
                 "Stack trace: " + stack);
             return;
         }
 
         ModuleDescriptor moduleDescriptor = webResourceIntegration.getPluginAccessor().getEnabledPluginModule(moduleKey);
         if (!(moduleDescriptor instanceof WebResourceModuleDescriptor))
         {
             log.warn("Cannot find web resource module for: " + moduleKey);
             return;
         }
 
         List<String> dependencies = ((WebResourceModuleDescriptor) moduleDescriptor).getDependencies();
         log.info("About to add resource [" + moduleKey + "] and its dependencies: " + dependencies);
 
         stack.push(moduleKey);
         try
         {
             for (String dependency : dependencies)
             {
                 if (!orderedResourceKeys.contains(dependency))
                     addResourceWithDependencies(dependency, orderedResourceKeys, stack);
             }
         }
         finally
         {
             stack.pop();
         }
         orderedResourceKeys.add(moduleKey);
     }
 
     public void includeResources(Writer writer)
     {
         Collection webResourceNames = (Collection) webResourceIntegration.getRequestCache().get(REQUEST_CACHE_RESOURCE_KEY);
         if (webResourceNames == null || webResourceNames.isEmpty())
         {
             log.info("No resources required to write");
             return;
         }
 
         for (Object webResourceName : webResourceNames)
         {
             String resourceName = (String) webResourceName;
             writeResourceTag(resourceName, writer);
         }
     }
 
     public String getRequiredResources()
     {
         StringWriter writer = new StringWriter();
         includeResources(writer);
         return writer.toString();
     }
 
     public void requireResource(String moduleCompleteKey, Writer writer)
     {
         ListOrderedSet resourcesWithDeps = new ListOrderedSet();
         addResourceWithDependencies(moduleCompleteKey, resourcesWithDeps, new Stack<String>());
 
         for (Object resource : resourcesWithDeps)
         {
             writeResourceTag((String) resource, writer);
         }
     }
 
     private void writeResourceTag(String moduleCompleteKey, Writer writer)
     {
         List<PluginResource> resources = pluginResourceLocator.getPluginResources(moduleCompleteKey);
 
         if (resources == null)
         {
             writeContentAndSwallowErrors("<!-- Error loading resource \"" + moduleCompleteKey + "\".  Resource not found -->\n", writer);
             return;
         }
 
         for (PluginResource resource : resources)
         {
             WebResourceFormatter formatter = getWebResourceFormatter(resource.getResourceName());
             if (formatter == null)
             {
                 writeContentAndSwallowErrors("<!-- Error loading resource \"" + moduleCompleteKey + "\".  Resource formatter not found -->\n", writer);
                 continue;
             }
 
             String url = resource.getUrl();
             if (resource.isCacheSupported())
             {
                 Plugin plugin = webResourceIntegration.getPluginAccessor().getEnabledPluginModule(resource.getModuleCompleteKey()).getPlugin();
                 url = getStaticResourcePrefix(plugin.getPluginInformation().getVersion()) + url;
             }
            else
            {
                url = webResourceIntegration.getBaseUrl() + url;
            }
             writeContentAndSwallowErrors(formatter.formatResource(url, resource.getParams()), writer);
         }
     }
 
     public String getResourceTags(String moduleCompleteKey)
     {
         StringWriter writer = new StringWriter();
         requireResource(moduleCompleteKey, writer);
         return writer.toString();
     }
 
     private void writeContentAndSwallowErrors(String content, Writer writer)
     {
         try
         {
             writer.write(content);
         }
         catch (IOException e)
         {
             log.error(e);
         }
     }
 
     private WebResourceFormatter getWebResourceFormatter(String resourceName)
     {
         for (WebResourceFormatter webResourceFormatter : webResourceFormatters)
         {
             if (webResourceFormatter.matches(resourceName))
                 return webResourceFormatter;
         }
         return null;
     }
 
     public String getStaticResourcePrefix()
     {
         // "{base url}/s/{build num}/{system counter}/_"
         return webResourceIntegration.getBaseUrl() + "/" +
                 STATIC_RESOURCE_PREFIX + "/" +
                 webResourceIntegration.getSystemBuildNumber() + "/" +
                 webResourceIntegration.getSystemCounter() + "/" +
                 STATIC_RESOURCE_SUFFIX;
     }
 
     public String getStaticResourcePrefix(String resourceCounter)
     {
         // "{base url}/s/{build num}/{system counter}/{resource counter}/_"
         return webResourceIntegration.getBaseUrl() + "/" +
                 STATIC_RESOURCE_PREFIX + "/" +
                 webResourceIntegration.getSystemBuildNumber() + "/" +
                 webResourceIntegration.getSystemCounter() + "/" +
                 resourceCounter + "/" +
                 STATIC_RESOURCE_SUFFIX;
     }
 
     public String getStaticPluginResource(String moduleCompleteKey, String resourceName)
     {
         ModuleDescriptor moduleDescriptor = webResourceIntegration.getPluginAccessor().getEnabledPluginModule(moduleCompleteKey);
         if(moduleDescriptor == null)
             return null;
 
         return getStaticPluginResource(moduleDescriptor, resourceName);
     }
 
     /**
      * @return "{base url}/s/{build num}/{system counter}/{plugin version}/_/download/resources/{plugin.key:module.key}/{resource.name}"
      */
     public String getStaticPluginResource(ModuleDescriptor moduleDescriptor, String resourceName)
     {
         // "{base url}/s/{build num}/{system counter}/{plugin version}/_"
         String staticUrlPrefix = getStaticResourcePrefix(String.valueOf(moduleDescriptor.getPlugin().getPluginsVersion()));
         // "/download/resources/plugin.key:module.key/resource.name"
         return staticUrlPrefix + pluginResourceLocator.getResourceUrl(moduleDescriptor.getCompleteKey(), resourceName);
     }
 
     /* Deprecated methods */
 
     /**
      * @deprecated Use {@link #getStaticPluginResource(com.atlassian.plugin.ModuleDescriptor, String)} instead
      */
     public String getStaticPluginResourcePrefix(ModuleDescriptor moduleDescriptor, String resourceName)
     {
         return getStaticPluginResource(moduleDescriptor, resourceName);
     }
 
     /**
      * @deprecated Since 2.2
      */
     private static final String REQUEST_CACHE_MODE_KEY = "plugin.webresource.mode";
 
     /**
      * @deprecated Since 2.2
      */
     private static final IncludeMode DEFAULT_INCLUDE_MODE = WebResourceManager.DELAYED_INCLUDE_MODE;
 
     /**
      * @deprecated Since 2.2.
      */
     public void setIncludeMode(IncludeMode includeMode)
     {
         webResourceIntegration.getRequestCache().put(REQUEST_CACHE_MODE_KEY, includeMode);
     }
 
     IncludeMode getIncludeMode()
     {
         IncludeMode includeMode = (IncludeMode) webResourceIntegration.getRequestCache().get(REQUEST_CACHE_MODE_KEY);
         if (includeMode == null)
         {
             includeMode = DEFAULT_INCLUDE_MODE;
         }
         return includeMode;
     }
 }
