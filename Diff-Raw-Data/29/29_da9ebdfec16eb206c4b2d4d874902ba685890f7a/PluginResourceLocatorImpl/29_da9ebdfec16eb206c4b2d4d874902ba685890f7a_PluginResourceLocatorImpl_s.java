 package com.atlassian.plugin.webresource;
 
 import com.atlassian.plugin.PluginAccessor;
 import com.atlassian.plugin.ModuleDescriptor;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.elements.ResourceDescriptor;
 import com.atlassian.plugin.elements.ResourceLocation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.*;
 
 import com.atlassian.plugin.servlet.ServletContextFactory;
 import com.atlassian.plugin.servlet.DownloadableResource;
 import com.atlassian.plugin.servlet.DownloadableWebResource;
 import com.atlassian.plugin.servlet.DownloadableClasspathResource;
 import com.atlassian.plugin.servlet.ForwardableResource;
 
 /**
  * Default implementation of {@link PluginResourceLocator}.
  * @since 2.2
  */
 public class PluginResourceLocatorImpl implements PluginResourceLocator
 {
     private static final Log log = LogFactory.getLog(PluginResourceLocatorImpl.class);
 
     public static final String PLUGIN_WEBRESOURCE_BATCHING_OFF = "plugin.webresource.batching.off";
 
     private static final String DOWNLOAD_TYPE = "download";
 
     private static String[] BATCH_PARAMS = new String[] { "ieonly", "media", "content-type", "cache" };
 
     final private PluginAccessor pluginAccessor;
     final private ServletContextFactory servletContextFactory;
     private static final String RESOURCE_SOURCE_PARAM = "source";
     private static final String RESOURCE_BATCH_PARAM = "batch";
 
     public PluginResourceLocatorImpl(PluginAccessor pluginAccessor, ServletContextFactory servletContextFactory)
     {
         this.pluginAccessor = pluginAccessor;
         this.servletContextFactory = servletContextFactory;
     }
 
     public boolean matches(String url)
     {
         return SinglePluginResource.matches(url) || BatchPluginResource.matches(url);
     }
 
     public DownloadableResource getDownloadableResource(String url, Map<String, String> queryParams)
     {
         if (BatchPluginResource.matches(url))
         {
             BatchPluginResource batchResource = BatchPluginResource.parse(url, queryParams);
             return locateBatchPluginResource(batchResource);
         }
 
         if (SinglePluginResource.matches(url))
         {
             SinglePluginResource resource = SinglePluginResource.parse(url);
             return locatePluginResource(resource.getModuleCompleteKey(), resource.getResourceName());
         }
 
         log.error("Cannot locate resource for unknown url: " + url);
         return null;
     }
 
     private DownloadableResource locateBatchPluginResource(BatchPluginResource batchResource)
     {
         ModuleDescriptor moduleDescriptor = pluginAccessor.getEnabledPluginModule(batchResource.getModuleCompleteKey());
         for (ResourceDescriptor resourceDescriptor : moduleDescriptor.getResourceDescriptors(DOWNLOAD_TYPE))
         {
             if (isResourceInBatch(resourceDescriptor, batchResource))
                 batchResource.add(locatePluginResource(moduleDescriptor.getCompleteKey(), resourceDescriptor.getName()));
         }
 
         // if batch is empty, check if we can locate a plugin resource
         if (batchResource.isEmpty())
         {
             DownloadableResource resource = locatePluginResource(batchResource.getModuleCompleteKey(), batchResource.getResourceName());
             if (resource != null)
                 return resource;
         }
 
         return batchResource;
     }
 
     private boolean isResourceInBatch(ResourceDescriptor resourceDescriptor, BatchPluginResource batchResource)
     {
         if (!resourceDescriptor.getName().endsWith("." + batchResource.getType()))
             return false;
 
         if (skipBatch(resourceDescriptor))
             return false;
 
         for (String param : BATCH_PARAMS)
         {
             String batchValue = batchResource.getParams().get(param);
             String resourceValue = resourceDescriptor.getParameter(param);
 
             if (batchValue == null && resourceValue != null)
                 return false;
 
             if(batchValue != null && !batchValue.equals(resourceValue))
                 return false;
         }
 
         return true;
     }
 
     private DownloadableResource locatePluginResource(String moduleCompleteKey, String resourceName)
     {
         DownloadableResource resource;
 
         // resource from the module
         if (moduleCompleteKey.indexOf(":") > -1)
         {
             ModuleDescriptor moduleDescriptor = pluginAccessor.getPluginModule(moduleCompleteKey);
             if (moduleDescriptor != null && pluginAccessor.isPluginModuleEnabled(moduleCompleteKey))
             {
                 resource = getResourceFromModule(moduleDescriptor, resourceName, "");
             }
             else
             {
                 log.info("Module not found: " + moduleCompleteKey);
                 return null;
             }
         }
         else // resource from plugin
         {
             Plugin plugin = pluginAccessor.getPlugin(moduleCompleteKey);
             resource = getResourceFromPlugin(plugin, resourceName, "");
         }
 
         if (resource == null)
             resource = getResourceFromPlugin(getPlugin(moduleCompleteKey), resourceName, "");
 
         if (resource == null)
         {
             log.info("Unable to find resource for plugin: " + moduleCompleteKey + " and path: " + resourceName);
             return null;
         }
 
         return resource;
     }
 
     private Plugin getPlugin(String moduleKey)
     {
         if (moduleKey.indexOf(':') < 0 || moduleKey.indexOf(':') == moduleKey.length() - 1)
             return null;
 
         return pluginAccessor.getPlugin(moduleKey.substring(0, moduleKey.indexOf(':')));
     }
 
     private DownloadableResource getResourceFromModule(ModuleDescriptor moduleDescriptor, String resourcePath, String filePath)
     {
         Plugin plugin = pluginAccessor.getPlugin(moduleDescriptor.getPluginKey());
         ResourceLocation resourceLocation = moduleDescriptor.getResourceLocation(DOWNLOAD_TYPE, resourcePath);
 
         if (resourceLocation != null)
         {
             return getDownloadablePluginResource(plugin, resourceLocation, filePath);
         }
 
         String[] nextParts = splitLastPathPart(resourcePath);
         if (nextParts == null)
             return null;
 
         return getResourceFromModule(moduleDescriptor, nextParts[0], nextParts[1] + filePath);
     }
 
     private DownloadableResource getResourceFromPlugin(Plugin plugin, String resourcePath, String filePath)
     {
         if (plugin == null)
             return null;
 
         ResourceLocation resourceLocation = plugin.getResourceLocation(DOWNLOAD_TYPE, resourcePath);
         if (resourceLocation != null)
         {
             return getDownloadablePluginResource(plugin, resourceLocation, filePath);
         }
 
         String[] nextParts = splitLastPathPart(resourcePath);
         if (nextParts == null)
             return null;
 
         return getResourceFromPlugin(plugin, nextParts[0], nextParts[1] + filePath);
     }
 
     // pacakge protected so we can test it
     String[] splitLastPathPart(String resourcePath)
     {
         int indexOfSlash = resourcePath.lastIndexOf('/');
         if (resourcePath.endsWith("/")) // skip over the trailing slash
         {
             indexOfSlash = resourcePath.lastIndexOf('/', indexOfSlash - 1);
         }
 
         if (indexOfSlash < 0) return null;
 
         return new String[] {
             resourcePath.substring(0, indexOfSlash + 1),
             resourcePath.substring(indexOfSlash + 1)
         };
     }
 
     private DownloadableResource getDownloadablePluginResource(Plugin plugin, ResourceLocation resourceLocation, String filePath)
     {
         String sourceParam = resourceLocation.getParameter(RESOURCE_SOURCE_PARAM);
 
        // allows for the resource to be served by forwarding the request to the location
        if("forward".equalsIgnoreCase(sourceParam))
             return new ForwardableResource(resourceLocation);
 
        // this allows plugins that are loaded from the web to be served
        if ("webContext".equalsIgnoreCase(sourceParam))
             return new DownloadableWebResource(plugin, resourceLocation, filePath, servletContextFactory.getServletContext());
 
         return new DownloadableClasspathResource(plugin, resourceLocation, filePath);
     }
 
     public List<PluginResource> getPluginResources(String moduleCompleteKey)
     {
         ModuleDescriptor moduleDescriptor = pluginAccessor.getEnabledPluginModule(moduleCompleteKey);
         if (moduleDescriptor == null || !(moduleDescriptor instanceof WebResourceModuleDescriptor))
         {
             log.error("Error loading resource \"" + moduleCompleteKey + "\". Resource is not a Web Resource Module");
             return Collections.EMPTY_LIST;
         }
 
         boolean singleMode = Boolean.valueOf(System.getProperty(PLUGIN_WEBRESOURCE_BATCHING_OFF));
         List<PluginResource> resources = new ArrayList<PluginResource>();
 
         for (ResourceDescriptor resourceDescriptor : moduleDescriptor.getResourceDescriptors())
         {
             if (singleMode || skipBatch(resourceDescriptor))
             {
                 boolean cache = !"false".equalsIgnoreCase(resourceDescriptor.getParameter("cache"));
                 resources.add(new SinglePluginResource(resourceDescriptor.getName(), moduleDescriptor.getCompleteKey(), cache));
             }
             else
             {
                 BatchPluginResource batchResource = createBatchResource(moduleDescriptor.getCompleteKey(),  resourceDescriptor);
                 if (!resources.contains(batchResource))
                     resources.add(batchResource);
             }
         }
         return resources;
     }
 
     private boolean skipBatch(ResourceDescriptor resourceDescriptor)
     {
         return "false".equalsIgnoreCase(resourceDescriptor.getParameter(RESOURCE_BATCH_PARAM)) ||
            "forward".equalsIgnoreCase(resourceDescriptor.getParameter(RESOURCE_SOURCE_PARAM)); // you can't batch forwarded requests
     }
 
     private BatchPluginResource createBatchResource(String moduleCompleteKey, ResourceDescriptor resourceDescriptor)
     {
         String name = resourceDescriptor.getName();
         String type = name.substring(name.lastIndexOf(".") + 1);
         Map<String, String> params = new TreeMap<String, String>();
         for (String param : BATCH_PARAMS)
         {
             String value = resourceDescriptor.getParameter(param);
             if (StringUtils.isNotEmpty(value))
                 params.put(param, value);
         }
 
         return new BatchPluginResource(moduleCompleteKey, type, params);
     }
 
     public String getResourceUrl(String moduleCompleteKey, String resourceName)
     {
         PluginResource pluginResource = new SinglePluginResource(resourceName, moduleCompleteKey, false);
         return pluginResource.getUrl();
     }
 }
