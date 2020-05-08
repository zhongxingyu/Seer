 package com.atlassian.plugin.factories;
 
 import com.atlassian.plugin.*;
 import com.atlassian.plugin.factories.PluginFactory;
 import com.atlassian.plugin.impl.DefaultDynamicPlugin;
 import com.atlassian.plugin.impl.DynamicPlugin;
 import com.atlassian.plugin.classloader.PluginClassLoader;
 import com.atlassian.plugin.loaders.classloading.DeploymentUnit;
 import com.atlassian.plugin.parsers.DescriptorParser;
 import com.atlassian.plugin.parsers.DescriptorParserFactory;
 import com.atlassian.plugin.parsers.XmlDescriptorParserFactory;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.Validate;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.net.URL;
 
 /**
  * Deploys version 1.0 plugins into the legacy custom classloader structure that gives each plugin its own classloader.
  *
  * @since 2.0.0
  */
 public class LegacyDynamicPluginFactory implements PluginFactory
 {
     private DescriptorParserFactory descriptorParserFactory;
     private String pluginDescriptorFileName;
 
     public LegacyDynamicPluginFactory(String pluginDescriptorFileName)
     {
         Validate.notEmpty(pluginDescriptorFileName, "Plugin descriptor name cannot be null or blank");
         this.descriptorParserFactory = new XmlDescriptorParserFactory();
         this.pluginDescriptorFileName = pluginDescriptorFileName;
     }
 
     /**
      * Deploys the plugin jar
      * @param deploymentUnit the jar to deploy
      * @param moduleDescriptorFactory The factory for plugin modules
      * @return The instantiated and populated plugin
      * @throws PluginParseException If the descriptor cannot be parsed
      */
     public Plugin create(DeploymentUnit deploymentUnit, ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
     {
         Validate.notNull(deploymentUnit, "The deployment unit must not be null");
         Validate.notNull(moduleDescriptorFactory, "The module descriptor factory must not be null");
 
         Plugin plugin = null;
         InputStream pluginDescriptor = null;
        PluginClassLoader loader = new PluginClassLoader(deploymentUnit.getPath(), Thread.currentThread().getContextClassLoader());
         try
         {
             URL pluginDescriptorUrl = loader.getLocalResource(pluginDescriptorFileName);
             if (pluginDescriptorUrl == null)
                 throw new PluginParseException("No descriptor found in classloader for : " + deploymentUnit);
 
             pluginDescriptor = pluginDescriptorUrl.openStream();
             // The plugin we get back may not be the same (in the case of an UnloadablePlugin), so add what gets returned, rather than the original
             DescriptorParser parser = descriptorParserFactory.getInstance(pluginDescriptor);
             plugin = parser.configurePlugin(moduleDescriptorFactory, createPlugin(deploymentUnit, loader));
         }
         // Under normal conditions, the deployer would be closed when the plugins are undeployed. However,
         // these are not normal conditions, so we need to make sure that we close them explicitly.
         catch (PluginParseException e)
         {
            loader.close();
             throw e;
         }
         catch (RuntimeException e)
         {
            loader.close();
             throw e;
         }
         catch (Error e)
         {
            loader.close();
             throw e;
         }
         catch (IOException e)
         {
            loader.close();
             throw new PluginParseException();
         } finally
         {
             IOUtils.closeQuietly(pluginDescriptor);
         }
         return plugin;
     }
 
     /**
      * Creates the plugin.  Override to use a different Plugin class
      * @param deploymentUnit The deployment unit
      * @param loader The plugin loader
      * @return The plugin instance
      */
     protected DynamicPlugin createPlugin(DeploymentUnit deploymentUnit, PluginClassLoader loader)
     {
         return new DefaultDynamicPlugin(deploymentUnit, loader);
     }
 
     /**
      * Determines if this deployer can handle this artifact by looking for the plugin descriptor
      *
      * @param pluginArtifact The artifact to test
      * @return The plugin key, null if it cannot load the plugin
      * @throws com.atlassian.plugin.PluginParseException If there are exceptions parsing the plugin configuration
      */
     public String canCreate(PluginArtifact pluginArtifact) throws PluginParseException
     {
         Validate.notNull(pluginArtifact, "The plugin artifact must not be null");
         String pluginKey = null;
         final InputStream descriptorStream = pluginArtifact.getResourceAsStream(pluginDescriptorFileName);
         if (descriptorStream != null)
         {
             final DescriptorParser descriptorParser = descriptorParserFactory.getInstance(descriptorStream);
 
             // Only recognize version 1 plugins
             if (descriptorParser.getPluginsVersion() <= 1)
                 pluginKey = descriptorParser.getKey();
         }
         return pluginKey;
     }
 }
