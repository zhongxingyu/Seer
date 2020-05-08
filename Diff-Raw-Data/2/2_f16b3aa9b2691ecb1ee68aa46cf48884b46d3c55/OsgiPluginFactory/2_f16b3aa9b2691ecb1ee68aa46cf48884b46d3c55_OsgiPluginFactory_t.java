 package com.atlassian.plugin.osgi.factory;
 
 import com.atlassian.plugin.JarPluginArtifact;
 import com.atlassian.plugin.ModuleDescriptorFactory;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.PluginArtifact;
 import com.atlassian.plugin.PluginInformation;
 import com.atlassian.plugin.PluginParseException;
 import com.atlassian.plugin.event.PluginEventManager;
 import com.atlassian.plugin.factories.PluginFactory;
 import com.atlassian.plugin.impl.UnloadablePlugin;
 import com.atlassian.plugin.loaders.classloading.DeploymentUnit;
 import com.atlassian.plugin.osgi.container.OsgiContainerManager;
 import com.atlassian.plugin.osgi.container.OsgiPersistentCache;
 import com.atlassian.plugin.osgi.factory.transform.DefaultPluginTransformer;
 import com.atlassian.plugin.osgi.factory.transform.PluginTransformationException;
 import com.atlassian.plugin.osgi.factory.transform.PluginTransformer;
 import com.atlassian.plugin.osgi.factory.transform.model.SystemExports;
 import com.atlassian.plugin.parsers.DescriptorParser;
 import com.atlassian.plugin.parsers.DescriptorParserFactory;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.Validate;
 import org.osgi.framework.Constants;
 import org.osgi.util.tracker.ServiceTracker;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.jar.Manifest;
 
 /**
  * Plugin loader that starts an OSGi container and loads plugins into it, wrapped as OSGi bundles.  Supports
  * <ul>
  *  <li>Dynamic loading of module descriptors via OSGi services</li>
  *  <li>Delayed enabling until the Spring container is active</li>
  *  <li>XML or Jar manifest configuration</li>
  * </ul>
  */
 public class OsgiPluginFactory implements PluginFactory
 {
     private static final Logger log = LoggerFactory.getLogger(OsgiPluginFactory.class);
 
     public interface PluginTransformerFactory
     {
         PluginTransformer newPluginTransformer(OsgiPersistentCache cache, SystemExports systemExports, Set<String> applicationKeys, String pluginDescriptorPath, OsgiContainerManager osgi);
     }
 
     public static class DefaultPluginTransformerFactory implements PluginTransformerFactory
     {
         public PluginTransformer newPluginTransformer(OsgiPersistentCache cache, SystemExports systemExports, Set<String> applicationKeys, String pluginDescriptorPath, OsgiContainerManager osgi)
         {
             return new DefaultPluginTransformer(cache, systemExports, applicationKeys, pluginDescriptorPath, osgi);
         }
     }
 
     private final OsgiContainerManager osgi;
     private final String pluginDescriptorFileName;
     private final DescriptorParserFactory descriptorParserFactory;
     private final PluginEventManager pluginEventManager;
     private final Set<String> applicationKeys;
     private final OsgiPersistentCache persistentCache;
     private final PluginTransformerFactory pluginTransformerFactory;
 
     private volatile PluginTransformer pluginTransformer;
 
     private ServiceTracker moduleDescriptorFactoryTracker;
     private final OsgiChainedModuleDescriptorFactoryCreator osgiChainedModuleDescriptorFactoryCreator;
 
     /**
      * Old constructor retained for backwards compatibility
      * @deprecated
      */
     public OsgiPluginFactory(String pluginDescriptorFileName, String applicationKey, OsgiPersistentCache persistentCache, OsgiContainerManager osgi, PluginEventManager pluginEventManager)
     {
         this(pluginDescriptorFileName, new HashSet<String>(Arrays.asList(applicationKey)), persistentCache, osgi, pluginEventManager);
     }
 
     /**
      * Default constructor
      */
     public OsgiPluginFactory(String pluginDescriptorFileName, Set<String> applicationKeys, OsgiPersistentCache persistentCache, final OsgiContainerManager osgi, PluginEventManager pluginEventManager)
     {
         this(pluginDescriptorFileName, applicationKeys, persistentCache, osgi, pluginEventManager, new DefaultPluginTransformerFactory());
     }
 
     /**
      * Constructor for implementations that want to override the DefaultPluginTransformer with a custom implementation
      */
     public OsgiPluginFactory(String pluginDescriptorFileName, Set<String> applicationKeys, OsgiPersistentCache persistentCache, final OsgiContainerManager osgi, PluginEventManager pluginEventManager, PluginTransformerFactory pluginTransformerFactory)
     {
         Validate.notNull(pluginDescriptorFileName, "Plugin descriptor is required");
         Validate.notNull(osgi, "The OSGi container is required");
         Validate.notNull(applicationKeys, "The application keys are required");
         Validate.notNull(persistentCache, "The osgi persistent cache is required");
         Validate.notNull(persistentCache, "The plugin event manager is required");
        Validate.notNull(pluginTransformerFactory, "The plugin transformer factory is required");
 
         this.osgi = osgi;
         this.pluginDescriptorFileName = pluginDescriptorFileName;
         this.descriptorParserFactory = new OsgiPluginXmlDescriptorParserFactory();
         this.pluginEventManager = pluginEventManager;
         this.applicationKeys = applicationKeys;
         this.persistentCache = persistentCache;
         this.osgiChainedModuleDescriptorFactoryCreator = new OsgiChainedModuleDescriptorFactoryCreator(new OsgiChainedModuleDescriptorFactoryCreator.ServiceTrackerFactory()
         {
             public ServiceTracker create(String className)
             {
                 return osgi.getServiceTracker(className);
             }
         });
         this.pluginTransformerFactory = pluginTransformerFactory;
     }
 
 
     private PluginTransformer getPluginTransformer()
     {
         if (pluginTransformer == null)
         {
             String exportString = (String) osgi.getBundles()[0].getHeaders()
                     .get(Constants.EXPORT_PACKAGE);
             SystemExports exports = new SystemExports(exportString);
             pluginTransformer = pluginTransformerFactory.newPluginTransformer(persistentCache, exports, applicationKeys, pluginDescriptorFileName, osgi);
         }
         return pluginTransformer;
     }
 
     public String canCreate(PluginArtifact pluginArtifact) throws PluginParseException
     {
         Validate.notNull(pluginArtifact, "The plugin artifact is required");
 
         String pluginKey = getPluginKeyFromDescriptor(pluginArtifact);
         if (pluginKey == null)
         {
             pluginKey = getPluginKeyFromManifest(pluginArtifact);
         }
         return pluginKey;
     }
 
     /**
      * @param pluginArtifact The plugin artifact
      * @return The plugin key if a manifest is present and contains {@link OsgiPlugin.ATLASSIAN_PLUGIN_KEY} and
      * {@link Constants.BUNDLE_VERSION}
      */
     private String getPluginKeyFromManifest(PluginArtifact pluginArtifact)
     {
         Manifest mf = getManifest(pluginArtifact);
         if (mf != null)
         {
             String key = mf.getMainAttributes().getValue(OsgiPlugin.ATLASSIAN_PLUGIN_KEY);
             String version = mf.getMainAttributes().getValue(Constants.BUNDLE_VERSION);
             if (key != null)
             {
                 if (version != null)
                 {
                     return key;
                 }
                 else
                 {
                     log.warn("Found plugin key '" + key + "' in the manifest but no bundle version, so it can't be loaded as an OsgiPlugin");
                 }
             }
         }
         return null;
     }
 
     private Manifest getManifest(PluginArtifact pluginArtifact)
     {
         InputStream descriptorClassStream = pluginArtifact.getResourceAsStream("META-INF/MANIFEST.MF");
         if (descriptorClassStream != null)
         {
             try
             {
                 return new Manifest(descriptorClassStream);
             }
             catch (IOException e)
             {
                 log.error("Cannot read manifest from plugin artifact " + pluginArtifact.getName(), e);
             }
             finally
             {
                 IOUtils.closeQuietly(descriptorClassStream);
             }
         }
         return null;
     }
 
     private String getPluginKeyFromDescriptor(PluginArtifact pluginArtifact)
     {
         String pluginKey = null;
         InputStream descriptorStream = null;
         try
         {
             descriptorStream = pluginArtifact.getResourceAsStream(pluginDescriptorFileName);
 
             if (descriptorStream != null)
             {
                 final DescriptorParser descriptorParser = descriptorParserFactory.getInstance(descriptorStream, applicationKeys.toArray(new String[applicationKeys.size()]));
                 if (descriptorParser.getPluginsVersion() == 2)
                 {
                     pluginKey = descriptorParser.getKey();
                 }
             }
         }
         finally
         {
             IOUtils.closeQuietly(descriptorStream);
         }
         return pluginKey;
     }
 
     /**
      * @deprecated Since 2.2.0, use {@link #create(PluginArtifact,ModuleDescriptorFactory)} instead
      */
     public Plugin create(DeploymentUnit deploymentUnit, ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
     {
         Validate.notNull(deploymentUnit, "The deployment unit is required");
         return create(new JarPluginArtifact(deploymentUnit.getPath()), moduleDescriptorFactory);
     }
 
     /**
      * Deploys the plugin artifact
      *
      * @param pluginArtifact          the plugin artifact to deploy
      * @param moduleDescriptorFactory The factory for plugin modules
      * @return The instantiated and populated plugin
      * @throws PluginParseException If the descriptor cannot be parsed
      * @throws IllegalArgumentException If the plugin descriptor isn't found, and the plugin key and bundle version aren't
      * specified in the manifest
      * @since 2.2.0
      */
     public Plugin create(PluginArtifact pluginArtifact, ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
     {
         Validate.notNull(pluginArtifact, "The plugin deployment unit is required");
         Validate.notNull(moduleDescriptorFactory, "The module descriptor factory is required");
 
         Plugin plugin = null;
         InputStream pluginDescriptor = null;
         try
         {
             pluginDescriptor = pluginArtifact.getResourceAsStream(pluginDescriptorFileName);
             if (pluginDescriptor != null)
             {
                 ModuleDescriptorFactory combinedFactory = getChainedModuleDescriptorFactory(moduleDescriptorFactory, pluginArtifact);
                 DescriptorParser parser = descriptorParserFactory.getInstance(pluginDescriptor, applicationKeys.toArray(new String[applicationKeys.size()]));
                 final Plugin osgiPlugin = new OsgiPlugin(parser.getKey(), osgi, createOsgiPluginJar(pluginArtifact), pluginEventManager);
 
                 // Temporarily configure plugin until it can be properly installed
                 plugin = parser.configurePlugin(combinedFactory, osgiPlugin);
             }
             else
             {
                 Manifest mf = getManifest(pluginArtifact);
                 String pluginKey = mf.getMainAttributes().getValue(OsgiPlugin.ATLASSIAN_PLUGIN_KEY);
                 String pluginVersion = mf.getMainAttributes().getValue(Constants.BUNDLE_VERSION);
                 Validate.notEmpty(pluginKey);
                 Validate.notEmpty(pluginVersion);
 
                 plugin = new OsgiPlugin(pluginKey, osgi, pluginArtifact, pluginEventManager);
                 plugin.setKey(pluginKey);
                 plugin.setPluginsVersion(2);
                 PluginInformation info = new PluginInformation();
                 info.setVersion(pluginVersion);
                 plugin.setPluginInformation(info);
             }
         }
         catch (PluginTransformationException ex)
         {
             return reportUnloadablePlugin(pluginArtifact.toFile(), ex);
         }
         finally
         {
             IOUtils.closeQuietly(pluginDescriptor);
         }
         return plugin;
     }
 
     /**
      * Get a chained module descriptor factory that includes any dynamically available descriptor factories
      *
      * @param originalFactory The factory provided by the host application
      * @param pluginArtifact
      * @return The composite factory
      */
     private ModuleDescriptorFactory getChainedModuleDescriptorFactory(ModuleDescriptorFactory originalFactory, final PluginArtifact pluginArtifact)
     {
         return osgiChainedModuleDescriptorFactoryCreator.create(new OsgiChainedModuleDescriptorFactoryCreator.ResourceLocator()
         {
             public boolean doesResourceExist(String name)
             {
                 return pluginArtifact.doesResourceExist(name);
             }
         }, originalFactory);
     }
 
     private PluginArtifact createOsgiPluginJar(PluginArtifact pluginArtifact)
     {
         File transformedFile = getPluginTransformer().transform(pluginArtifact, osgi.getHostComponentRegistrations());
         return new JarPluginArtifact(transformedFile);
     }
 
     private Plugin reportUnloadablePlugin(File file, Exception e)
     {
         log.error("Unable to load plugin: " + file, e);
 
         UnloadablePlugin plugin = new UnloadablePlugin();
         plugin.setErrorText("Unable to load plugin: " + e.getMessage());
         return plugin;
     }
 }
