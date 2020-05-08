 package com.atlassian.plugin.refimpl;
 
 import com.atlassian.plugin.*;
 import com.atlassian.plugin.event.PluginEventManager;
 import com.atlassian.plugin.main.AtlassianPlugins;
 import com.atlassian.plugin.main.PluginsConfiguration;
 import com.atlassian.plugin.main.PluginsConfigurationBuilder;
 import com.atlassian.plugin.osgi.container.OsgiContainerManager;
 import com.atlassian.plugin.osgi.container.impl.DefaultPackageScannerConfiguration;
 import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
 import com.atlassian.plugin.refimpl.servlet.SimpleContextListenerModuleDescriptor;
 import com.atlassian.plugin.refimpl.servlet.SimpleFilterModuleDescriptor;
 import com.atlassian.plugin.refimpl.servlet.SimpleServletModuleDescriptor;
 import com.atlassian.plugin.refimpl.webresource.SimpleWebResourceIntegration;
 import com.atlassian.plugin.servlet.*;
 import com.atlassian.plugin.servlet.descriptors.ServletContextParamModuleDescriptor;
 import com.atlassian.plugin.webresource.WebResourceIntegration;
 import com.atlassian.plugin.webresource.WebResourceManager;
 import com.atlassian.plugin.webresource.WebResourceManagerImpl;
 import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
 
 import javax.servlet.ServletContext;
 import java.io.File;
 import java.util.*;
 
 /**
  * A simple class that behaves like Spring's ContianerManager class.
  */
 public class ContainerManager
 {
     private final ServletModuleManager servletModuleManager;
     private final SimpleWebResourceIntegration webResourceIntegration;
     private final WebResourceManager webResourceManager;
     private final OsgiContainerManager osgiContainerManager;
     private final PluginAccessor pluginAccessor;
     private final HostComponentProvider hostComponentProvider;
     private final DefaultModuleDescriptorFactory moduleDescriptorFactory;
     private final Map<Class,Object> publicContainer;
     private final AtlassianPlugins plugins;
 
     private static ContainerManager instance;
     private List<DownloadStrategy> downloadStrategies;
 
     public ContainerManager(ServletContext servletContext)
     {
         instance = this;
         webResourceIntegration = new SimpleWebResourceIntegration(servletContext);
         webResourceManager = new WebResourceManagerImpl(webResourceIntegration);
 
         File pluginDir = new File(servletContext.getRealPath("/WEB-INF/plugins"));
         if (!pluginDir.exists())
         {
             pluginDir.mkdirs();
         }
         File bundlesDir = new File(servletContext.getRealPath("/WEB-INF/framework-bundles"));
         if (!bundlesDir.exists())
         {
             bundlesDir.mkdirs();
         }
 
         moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("servlet", SimpleServletModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("servlet-filter", SimpleFilterModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("servlet-context-param", ServletContextParamModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("servlet-context-listener", SimpleContextListenerModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("web-resource", WebResourceModuleDescriptor.class);
 
         DefaultPackageScannerConfiguration scannerConfig = new DefaultPackageScannerConfiguration();
         List<String> packageIncludes = new ArrayList<String>(scannerConfig.getPackageIncludes());
         packageIncludes.add("org.bouncycastle*");
         scannerConfig.setPackageIncludes(packageIncludes);
         hostComponentProvider = new SimpleHostComponentProvider();
 
         PluginsConfiguration config = new PluginsConfigurationBuilder()
                 .setPluginDirectory(pluginDir)
                 .setModuleDescriptorFactory(moduleDescriptorFactory)
                 .setPackageScannerConfiguration(scannerConfig)
                 .setHostComponentProvider(hostComponentProvider)
                .setFrameworkBundlesDirectory(bundlesDir)
                 .build();
         plugins = new AtlassianPlugins(config);
 
         PluginEventManager pluginEventManager = plugins.getPluginEventManager();
         osgiContainerManager = plugins.getOsgiContainerManager();
 
         servletModuleManager = new DefaultServletModuleManager(pluginEventManager);
 
         publicContainer = new HashMap<Class,Object>();
 
         pluginAccessor = plugins.getPluginAccessor();
         publicContainer.put(PluginController.class, plugins.getPluginController());
         publicContainer.put(PluginAccessor.class, pluginAccessor);
         publicContainer.put(PluginEventManager.class, pluginEventManager);
         publicContainer.put(Map.class, publicContainer);
 
 
         try {
             plugins.start();
         }
         catch (PluginParseException e)
         {
             e.printStackTrace();
         }
 
         downloadStrategies = new ArrayList<DownloadStrategy>();
         PluginResourceDownload pluginDownloadStrategy = new PluginResourceDownload();
         pluginDownloadStrategy.setPluginAccessor(pluginAccessor);
         pluginDownloadStrategy.setContentTypeResolver(new SimpleContentTypeResolver());
         pluginDownloadStrategy.setCharacterEncoding("UTF-8");
         downloadStrategies.add(pluginDownloadStrategy);
     }
 
     public static synchronized void setInstance(ContainerManager mgr)
     {
         instance = mgr;
     }
 
     public static synchronized ContainerManager getInstance()
     {
         return instance;
     }
 
     public ServletModuleManager getServletModuleManager()
     {
         return servletModuleManager;
     }
 
     public OsgiContainerManager getOsgiContainerManager()
     {
         return osgiContainerManager;
     }
 
     public PluginAccessor getPluginAccessor()
     {
         return pluginAccessor;
     }
 
     public HostComponentProvider getHostComponentProvider()
     {
         return hostComponentProvider;
     }
 
     public ModuleDescriptorFactory getModuleDescriptorFactory()
     {
         return moduleDescriptorFactory;
     }
 
     public List<DownloadStrategy> getDownloadStrategies()
     {
         return downloadStrategies;
     }
     
     public WebResourceManager getWebResourceManager()
     {
         return webResourceManager;
     }
 
     public WebResourceIntegration getWebResourceIntegration()
     {
         return webResourceIntegration;
     }
 
     void shutdown()
     {
         plugins.stop();
     }
 
     /**
      * A simple content type resolver that can identify css and js resources.
      */
     private class SimpleContentTypeResolver implements ContentTypeResolver
     {
         private final Map<String, String> mimeTypes;
 
         SimpleContentTypeResolver()
         {
             Map<String, String> types = new HashMap<String, String>();
             types.put("js", "application/x-javascript");
             types.put("css", "text/css");
             mimeTypes = Collections.unmodifiableMap(types);
         }
 
         public String getContentType(String requestUrl)
         {
             String extension = requestUrl.substring(requestUrl.lastIndexOf('.'));
             return mimeTypes.get(extension);
         }
     }
 
     private class SimpleHostComponentProvider implements HostComponentProvider
     {
         public void provide(ComponentRegistrar componentRegistrar) {
             for (Map.Entry<Class,Object> entry : publicContainer.entrySet())
             {
                 String name = entry.getKey().getSimpleName();
                 name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                 componentRegistrar.register(entry.getKey()).forInstance(entry.getValue()).withName(name);
             }
             componentRegistrar.register(WebResourceManager.class).forInstance(webResourceManager).withName("webResourceManager");
         }
     }
 }
