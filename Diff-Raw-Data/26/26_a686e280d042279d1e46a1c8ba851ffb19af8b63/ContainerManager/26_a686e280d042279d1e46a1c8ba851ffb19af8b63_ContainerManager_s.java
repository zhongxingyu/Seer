 package com.atlassian.plugin.refimpl;
 
 import com.atlassian.event.api.EventPublisher;
 import com.atlassian.plugin.DefaultModuleDescriptorFactory;
 import com.atlassian.plugin.ModuleDescriptorFactory;
 import com.atlassian.plugin.PluginAccessor;
 import com.atlassian.plugin.PluginController;
 import com.atlassian.plugin.PluginParseException;
 import com.atlassian.plugin.event.PluginEventManager;
 import com.atlassian.plugin.hostcontainer.HostContainer;
 import com.atlassian.plugin.hostcontainer.SimpleConstructorHostContainer;
 import com.atlassian.plugin.main.AtlassianPlugins;
 import com.atlassian.plugin.main.PluginsConfiguration;
 import com.atlassian.plugin.main.PluginsConfigurationBuilder;
 import com.atlassian.plugin.module.ClassPrefixModuleFactory;
 import com.atlassian.plugin.module.PrefixDelegatingModuleFactory;
 import com.atlassian.plugin.module.ModuleFactory;
 import com.atlassian.plugin.module.PrefixModuleFactory;
 import com.atlassian.plugin.osgi.container.OsgiContainerManager;
 import com.atlassian.plugin.osgi.container.impl.DefaultPackageScannerConfiguration;
 import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
 import com.atlassian.plugin.osgi.module.BeanPrefixModuleFactory;
 import com.atlassian.plugin.refimpl.servlet.SimpleContentTypeResolver;
 import com.atlassian.plugin.refimpl.servlet.SimpleServletContextFactory;
 import com.atlassian.plugin.refimpl.webresource.SimpleWebResourceIntegration;
 import com.atlassian.plugin.servlet.DefaultServletModuleManager;
 import com.atlassian.plugin.servlet.DownloadStrategy;
 import com.atlassian.plugin.servlet.PluginResourceDownload;
 import com.atlassian.plugin.servlet.ServletModuleManager;
 import com.atlassian.plugin.servlet.descriptors.ServletContextListenerModuleDescriptor;
 import com.atlassian.plugin.servlet.descriptors.ServletContextParamModuleDescriptor;
 import com.atlassian.plugin.servlet.descriptors.ServletFilterModuleDescriptor;
 import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
 import com.atlassian.plugin.util.Assertions;
 import com.atlassian.plugin.webresource.DefaultResourceBatchingConfiguration;
 import com.atlassian.plugin.webresource.PluginResourceLocator;
 import com.atlassian.plugin.webresource.PluginResourceLocatorImpl;
 import com.atlassian.plugin.webresource.WebResourceIntegration;
 import com.atlassian.plugin.webresource.WebResourceManager;
 import com.atlassian.plugin.webresource.WebResourceManagerImpl;
 import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
 import com.atlassian.plugin.webresource.transformer.WebResourceTransformerModuleDescriptor;
 import com.google.common.collect.ImmutableSet;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 
 import javax.servlet.ServletContext;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 
 /**
  * A simple class that behaves like Spring's ContainerManager class.
  */
 public class ContainerManager
 {
     /**
      * the name of the bundled plugins zip file to use
      */
     private static final String BUNDLED_PLUGINS_ZIP = "/atlassian-bundled-plugins.zip";
 
     private final ServletModuleManager servletModuleManager;
     private final SimpleWebResourceIntegration webResourceIntegration;
     private final WebResourceManager webResourceManager;
     private final OsgiContainerManager osgiContainerManager;
     private final PluginAccessor pluginAccessor;
     private final HostComponentProvider hostComponentProvider;
     private final DefaultModuleDescriptorFactory moduleDescriptorFactory;
     private final Map<Class<?>, Object> publicContainer;
     private final AtlassianPlugins plugins;
 
     private final HostContainer hostContainer;
     private static ContainerManager instance;
     private final List<DownloadStrategy> downloadStrategies;
 
     public ContainerManager(final ServletContext servletContext)
     {
         instance = this;
         webResourceIntegration = new SimpleWebResourceIntegration(servletContext);
 
         // Delegating host container since the real one requires the created object map, which won't be available until later
         final HostContainer delegatingHostContainer = new HostContainer()
         {
             public <T> T create(final Class<T> moduleClass) throws IllegalArgumentException
             {
                 return hostContainer.create(moduleClass);
             }
         };
 
         moduleDescriptorFactory = new DefaultModuleDescriptorFactory(delegatingHostContainer);
 
         moduleDescriptorFactory.addModuleDescriptor("servlet", ServletModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("servlet-filter", ServletFilterModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("servlet-context-param", ServletContextParamModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("servlet-context-listener", ServletContextListenerModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("web-resource", WebResourceModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("web-resource-transformer", WebResourceTransformerModuleDescriptor.class);
 
         final DefaultPackageScannerConfiguration scannerConfig = new DefaultPackageScannerConfiguration(determineVersion());
         scannerConfig.setServletContext(servletContext);
         
         final List<String> packageIncludes = new ArrayList<String>(scannerConfig.getPackageIncludes());
         packageIncludes.add("org.bouncycastle*");
         packageIncludes.add("org.dom4j*");
         packageIncludes.add("javax.servlet*");
         packageIncludes.add("com.google*");
         packageIncludes.add("com.opensymphony.module.sitemesh*");
 
         scannerConfig.setPackageIncludes(packageIncludes);
         scannerConfig.setPackageVersions(new HashMap<String,String>() {{
             put("javax.servlet", "2.5");
             put("javax.servlet.http", "2.5");
             put("org.apache.commons.logging", "1.1.1");
         }});
         hostComponentProvider = new SimpleHostComponentProvider();
 
         File osgiCache = findAndCreateDirectory(servletContext, "osgi.cache", "WEB-INF/osgi-cache");
         File bundledPluginDir = findAndCreateDirectory(servletContext, "bundledplugins.cache", "WEB-INF/bundled-plugins");
 
         final PluginsConfiguration config = new PluginsConfigurationBuilder()
                 .useLegacyDynamicPluginDeployer(true)
                 .bundledPluginUrl(this.getClass().getResource(BUNDLED_PLUGINS_ZIP))
                 .bundledPluginCacheDirectory(bundledPluginDir)
                 .pluginDirectory(makeSureDirectoryExists(servletContext, "/WEB-INF/plugins"))
                 .moduleDescriptorFactory(moduleDescriptorFactory)
                 .packageScannerConfiguration(scannerConfig)
                 .hostComponentProvider(hostComponentProvider)
                 .osgiPersistentCache(osgiCache)
                 .pluginStateStore(new DefaultPluginPersistentStateStore(osgiCache))
                 .applicationKey("refapp")
                 .build();
 
         PrefixDelegatingModuleFactory moduleFactory = new PrefixDelegatingModuleFactory(ImmutableSet.<PrefixModuleFactory>of(new BeanPrefixModuleFactory()));
         plugins = new AtlassianPlugins(config);
 
         final PluginEventManager pluginEventManager = plugins.getPluginEventManager();
         osgiContainerManager = plugins.getOsgiContainerManager();
 
         servletModuleManager = new DefaultServletModuleManager(servletContext, pluginEventManager);
         pluginAccessor = plugins.getPluginAccessor();
 
         final PluginResourceLocator pluginResourceLocator = new PluginResourceLocatorImpl(webResourceIntegration, new SimpleServletContextFactory(servletContext));
         final PluginResourceDownload pluginDownloadStrategy = new PluginResourceDownload(pluginResourceLocator, new SimpleContentTypeResolver(), "UTF-8");
 
         webResourceManager = new WebResourceManagerImpl(pluginResourceLocator, webResourceIntegration, new DefaultResourceBatchingConfiguration());
 
         publicContainer = new HashMap<Class<?>, Object>();
         publicContainer.put(PluginController.class, plugins.getPluginController());
         publicContainer.put(PluginAccessor.class, pluginAccessor);
        publicContainer.put(EventPublisher.class, pluginEventManager);
         publicContainer.put(ServletModuleManager.class, servletModuleManager);
         publicContainer.put(WebResourceManager.class, webResourceManager);
         publicContainer.put(Map.class, publicContainer);
         publicContainer.put(ModuleFactory.class, moduleFactory);
 
         hostContainer = new SimpleConstructorHostContainer(publicContainer);
         moduleFactory.addPrefixModuleFactory(new ClassPrefixModuleFactory(hostContainer));
 
         try
         {
             plugins.start();
         }
         catch (final PluginParseException e)
         {
             throw new RuntimeException(e);
         }
 
         downloadStrategies = new ArrayList<DownloadStrategy>();
         downloadStrategies.add(pluginDownloadStrategy);
     }
 
     private File findAndCreateDirectory(ServletContext servletContext, String sysPropName, String defaultPath)
     {
         File dir;
         if (System.getProperty(sysPropName) != null)
         {
             dir = makeSureDirectoryExists(System.getProperty(sysPropName));
         }
         else
         {
             dir = makeSureDirectoryExists(servletContext, defaultPath);
         }
         return dir;
     }
 
     private String determineVersion()
     {
         InputStream in = null;
         final Properties props = new Properties();
         try
         {
             in = getClass().getClassLoader().getResourceAsStream("META-INF/maven/com.atlassian.plugins/atlassian-plugins-core/pom.properties");
             if (in != null)
             {
                 props.load(in);
                 return props.getProperty("version");
             }
         } catch (final IOException e) {
             e.printStackTrace();
             return null;
         }
         finally
         {
             IOUtils.closeQuietly(in);
         }
         return null;
     }
 
     private File makeSureDirectoryExists(final ServletContext servletContext, final String relativePath)
     {
         return makeSureDirectoryExists(servletContext.getRealPath(relativePath));
     }
 
     private File makeSureDirectoryExists(final String path)
     {
         final File dir = new File(path);
         if (!dir.exists() && !dir.mkdirs())
         {
             throw new RuntimeException("Could not create directory <" + dir + ">");
         }
         return dir;
     }
 
     public static synchronized void setInstance(final ContainerManager mgr)
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
 
     private class SimpleHostComponentProvider implements HostComponentProvider
     {
         public void provide(final ComponentRegistrar componentRegistrar)
         {
             Assertions.notNull("publicContainer", publicContainer);
             for (final Map.Entry<Class<?>, Object> entry : publicContainer.entrySet())
             {
                 final String name = StringUtils.uncapitalize(entry.getKey().getSimpleName());
                 componentRegistrar.register(entry.getKey()).forInstance(entry.getValue()).withName(name);
             }
 
         }
     }
 }
