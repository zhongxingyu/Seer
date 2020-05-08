 package com.atlassian.maven.plugins.amps.product;
 
 import java.io.File;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.atlassian.maven.plugins.amps.AbstractProductHandlerMojo;
 import com.atlassian.maven.plugins.amps.MavenContext;
 import com.atlassian.maven.plugins.amps.MavenGoals;
 import com.atlassian.maven.plugins.amps.Product;
 import com.atlassian.maven.plugins.amps.ProductArtifact;
 import com.atlassian.maven.plugins.amps.util.ConfigFileUtils;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Iterables;
 
 import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;
 
 import static com.atlassian.maven.plugins.amps.util.FileUtils.doesFileNameMatchArtifact;
 import static com.atlassian.maven.plugins.amps.util.ZipUtils.unzip;
 import static org.apache.commons.io.FileUtils.copyDirectory;
 import static org.apache.commons.io.FileUtils.copyFile;
 import static org.apache.commons.io.FileUtils.iterateFiles;
 import static org.apache.commons.io.FileUtils.moveDirectory;
 import static org.apache.commons.io.FileUtils.readFileToString;
 import static com.atlassian.maven.plugins.amps.util.ProjectUtils.createDirectory;
 
 public abstract class AbstractProductHandler extends AmpsProductHandler
 {
     private final PluginProvider pluginProvider;
 
     protected AbstractProductHandler(MavenContext context, MavenGoals goals, PluginProvider pluginProvider)
     {
         super(context, goals);
         this.pluginProvider = pluginProvider;
     }
 
     /**
      * Extracts the product and its home, prepares both and starts the product
      * @return the port
      */
     public final int start(final Product ctx) throws MojoExecutionException
     {
         final File homeDir = extractAndProcessHomeDirectory(ctx);
 
         final File extractedApp = extractApplication(ctx, homeDir);
 
         final File finalApp = addArtifactsAndOverrides(ctx, homeDir, extractedApp);
 
         // Ask for the system properties (from the ProductHandler and from the pom.xml)
         Map<String, String> systemProperties = mergeSystemProperties(ctx);
 
         return startApplication(ctx, finalApp, homeDir, systemProperties);
     }
 
     protected final File extractAndProcessHomeDirectory(final Product ctx) throws MojoExecutionException
     {
         final File homeDir = getHomeDirectory(ctx);
 
         // Check if home directory was provided by the user
         if (StringUtils.isNotBlank(ctx.getDataHome()))
         {
             // Don't modify the home. Just use it.
             return homeDir;
         }
 
         // Create a home dir for the product in target
         final File productHomeData = getProductHomeData(ctx);
         if (productHomeData != null)
         {
 
             // Only create the home dir if it doesn't exist
             if (!homeDir.exists())
             {
                 extractProductHomeData(productHomeData, homeDir, ctx);
 
                 // just in case
                 homeDir.mkdir();
                 processHomeDirectory(ctx, homeDir);
             }
 
             // Always override files regardless of home directory existing or not
             overrideAndPatchHomeDir(homeDir, ctx);
         }
         return homeDir;
     }
 
     protected void extractProductHomeData(File productHomeData, File homeDir, Product ctx)
             throws MojoExecutionException
     {
         final File tmpDir = new File(getBaseDirectory(ctx), "tmp-resources");
         tmpDir.mkdir();
 
         try
         {
             if (productHomeData.isFile())
             {
                 File tmp = new File(getBaseDirectory(ctx), ctx.getId() + "-home");
 
                 unzip(productHomeData, tmpDir.getPath());
 
                 File[] topLevelFiles = tmpDir.listFiles();
                 if (topLevelFiles.length != 1)
                 {
                     Iterable<String> filenames = Iterables.transform(Arrays.asList(topLevelFiles), new Function<File, String>(){
                         @Override
                         public String apply(File from)
                         {
                             return from.getName();
                         }
                     });
                     throw new MojoExecutionException("Expected a single top-level directory in test resources. Got: "
                             + Joiner.on(", ").join(filenames));
                 }
 
                 copyDirectory(topLevelFiles[0], getBaseDirectory(ctx), true);
                 moveDirectory(tmp, homeDir);
             }
             else if (productHomeData.isDirectory())
             {
                 copyDirectory(productHomeData, homeDir);
             }
         }
         catch (final IOException ex)
         {
             throw new MojoExecutionException("Unable to copy home directory", ex);
         }
     }
 
     /**
      * Takes 'app' (the file of the application - either .war or the exploded directory),
      * adds the artifacts, then returns the 'app'.
      * @return if {@code app} was a dir, returns a dir; if {@code app} was a war, returns a war.
      */
     private final File addArtifactsAndOverrides(final Product ctx, final File homeDir, final File app) throws MojoExecutionException
     {
         try
         {
             final File appDir;
             if (app.isFile())
             {
                 appDir = new File(getBaseDirectory(ctx), "webapp");
                 if (!appDir.exists())
                 {
                     unzip(app, appDir.getAbsolutePath());
                 }
             }
             else
             {
                 appDir = app;
             }
 
             addArtifacts(ctx, homeDir, appDir);
 
             // override war files
             try
             {
                 addOverrides(appDir, ctx);
                 customiseInstance(ctx, homeDir, appDir);
             }
             catch (IOException e)
             {
                 throw new MojoExecutionException("Unable to override WAR files using src/test/resources/" + ctx.getInstanceId() + "-app", e);
             }
 
             if (app.isFile())
             {
                 final File warFile = new File(app.getParentFile(), getId() + ".war");
                 com.atlassian.core.util.FileUtils.createZipFile(appDir, warFile);
                 return warFile;
             }
             else
             {
                 return appDir;
             }
 
         }
         catch (final Exception e)
         {
             throw new MojoExecutionException(e.getMessage(), e);
         }
     }
 
     /**
      * Each product handler can add specific operations on the application's home and war.
      * By default no operation is performed in this hook.
      *
      * <p>Example: StudioXXXProductHandlers can change the webapp to be studio-ready.</p>
      * @param ctx the product's details
      * @param homeDir the home directory
      * @param explodedWarDir the directory containing the exploded WAR of the application
      * @throws MojoExecutionException
      */
     protected void customiseInstance(Product ctx, File homeDir, File explodedWarDir) throws MojoExecutionException
     {
         // No operation by default
     }
 
     private void addArtifacts(final Product ctx, final File homeDir, final File appDir)
             throws IOException, MojoExecutionException, Exception
     {
         File pluginsDir = getUserInstalledPluginsDirectory(appDir, homeDir);
         final File bundledPluginsDir = new File(getBaseDirectory(ctx), "bundled-plugins");
 
         bundledPluginsDir.mkdir();
         // add bundled plugins
         final File bundledPluginsZip = new File(appDir, getBundledPluginPath(ctx));
         if (bundledPluginsZip.exists())
         {
             unzip(bundledPluginsZip, bundledPluginsDir.getPath());
         }
 
         if (isStaticPlugin())
         {
             if (!supportsStaticPlugins())
             {
                   throw new MojoExecutionException("According to your atlassian-plugin.xml file, this plugin is not " +
                           "atlassian-plugins version 2. This app currently only supports atlassian-plugins " +
                           "version 2.");
             }
             pluginsDir = new File(appDir, "WEB-INF/lib");
         }
 
         if (pluginsDir == null)
         {
             pluginsDir = bundledPluginsDir;
         }
 
         createDirectory(pluginsDir);
 
         // add this plugin itself if enabled
         if (ctx.isInstallPlugin())
         {
             addThisPluginToDirectory(pluginsDir);
             addTestPluginToDirectory(pluginsDir);
         }
 
         // add plugins2 plugins if necessary
         if (!isStaticPlugin())
         {
             addArtifactsToDirectory(pluginProvider.provide(ctx), pluginsDir);
         }
 
         // add plugins1 plugins
         List<ProductArtifact> artifacts = new ArrayList<ProductArtifact>();
         artifacts.addAll(getDefaultLibPlugins());
         artifacts.addAll(ctx.getLibArtifacts());
         addArtifactsToDirectory(artifacts, new File(appDir, "WEB-INF/lib"));
 
         artifacts = new ArrayList<ProductArtifact>();
         artifacts.addAll(getDefaultBundledPlugins());
         artifacts.addAll(ctx.getBundledArtifacts());
 
         addArtifactsToDirectory(artifacts, bundledPluginsDir);
 
         if (bundledPluginsDir.list().length > 0)
         {
             com.atlassian.core.util.FileUtils.createZipFile(bundledPluginsDir, bundledPluginsZip);
         }
 
         if (ctx.getLog4jProperties() != null && getLog4jPropertiesPath() != null)
         {
             copyFile(ctx.getLog4jProperties(), new File(appDir, getLog4jPropertiesPath()));
         }
     }
 
     /**
      * Processes standard replacement of configuration placeholders in the home directory.
      */
     protected void processHomeDirectory(Product ctx, File snapshotDir) throws MojoExecutionException
     {
         ConfigFileUtils.replace(getConfigFiles(ctx, snapshotDir), getReplacements(ctx), false, log);
     }
 
     abstract protected File extractApplication(Product ctx, File homeDir) throws MojoExecutionException;
     abstract protected int startApplication(Product ctx, File app, File homeDir, Map<String, String> properties) throws MojoExecutionException;
     abstract protected boolean supportsStaticPlugins();
     abstract protected Collection<? extends ProductArtifact> getDefaultBundledPlugins();
     abstract protected Collection<? extends ProductArtifact> getDefaultLibPlugins();
     abstract protected String getBundledPluginPath(Product ctx);
     abstract protected File getUserInstalledPluginsDirectory(File webappDir, File homeDir);
 
     protected String getLog4jPropertiesPath()
     {
         return null;
     }
 
     protected boolean isStaticPlugin() throws IOException
     {
         final File atlassianPluginXml = new File(project.getBasedir(), "src/main/resources/atlassian-plugin.xml");
         if (atlassianPluginXml.exists())
         {
             String text = readFileToString(atlassianPluginXml);
             return !text.contains("pluginsVersion=\"2\"") && !text.contains("plugins-version=\"2\"");
         }
         else
         {
             // probably an osgi bundle
             return false;
         }
     }
 
     protected final void addThisPluginToDirectory(final File targetDir) throws IOException
     {
         final File thisPlugin = getPluginFile();
 
         if (thisPlugin.exists())
         {
             // remove any existing version
             for (final Iterator<?> iterateFiles = iterateFiles(targetDir, null, false); iterateFiles.hasNext();)
             {
                 final File file = (File) iterateFiles.next();
                 if (doesFileNameMatchArtifact(file.getName(), project.getArtifactId()))
                 {
                     file.delete();
                 }
             }
 
             // add the plugin jar to the directory
             copyFile(thisPlugin, new File(targetDir, thisPlugin.getName()));
         }
         else
         {
             log.info("No plugin in the current project - " + thisPlugin.getAbsolutePath());
         }
     }
 
     protected void addTestPluginToDirectory(final File targetDir) throws IOException
     {
         final File testPluginFile = getTestPluginFile();
         if (testPluginFile.exists())
         {
             // add the test plugin jar to the directory
             copyFile(testPluginFile, new File(targetDir, testPluginFile.getName()));
         }
 
     }
 
     protected final File getPluginFile()
     {
         return new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".jar");
     }
 
     protected File getTestPluginFile()
     {
         return new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + "-tests.jar");
     }
 
     protected final void addArtifactsToDirectory(final List<ProductArtifact> artifacts, final File pluginsDir) throws MojoExecutionException
     {
         // copy the all the plugins we want in the webapp
         if (!artifacts.isEmpty())
         {
             // first remove plugins from the webapp that we want to update
             if (pluginsDir.isDirectory() && pluginsDir.exists())
             {
                 for (final Iterator<?> iterateFiles = iterateFiles(pluginsDir, null, false); iterateFiles.hasNext();)
                 {
                     final File file = (File) iterateFiles.next();
                     for (final ProductArtifact webappArtifact : artifacts)
                     {
                         if (!file.isDirectory() && doesFileNameMatchArtifact(file.getName(), webappArtifact.getArtifactId()))
                         {
                             file.delete();
                         }
                     }
                 }
             }
             goals.copyPlugins(pluginsDir, artifacts);
         }
     }
 
     protected final void addOverrides(File appDir, final Product ctx) throws IOException
     {
         final File srcDir = new File(project.getBasedir(), "src/test/resources/" + ctx.getInstanceId() + "-app");
         if (srcDir.exists() && appDir.exists())
         {
             copyDirectory(srcDir, appDir);
         }
     }
 
     /**
      * Merges the properties: pom.xml overrides {@link AbstractProductHandlerMojo#setDefaultValues} overrides the Product Handler.
      * @param ctx the Product
      * @return the complete list of system properties
      */
     protected final Map<String, String> mergeSystemProperties(Product ctx)
     {
         final Map<String, String> properties = new HashMap<String, String>();
 
         properties.putAll(getSystemProperties(ctx));
         for (Map.Entry<String, Object> entry : ctx.getSystemPropertyVariables().entrySet())
         {
             properties.put(entry.getKey(), (String) entry.getValue());
         }
         return properties;
     }
 
     /**
      * System properties which are specific to the Product Handler
      */
     protected abstract Map<String, String> getSystemProperties(Product ctx);
 
     /**
      * The artifact of the product (a war, a jar, a binary...)
      */
     protected abstract ProductArtifact getArtifact();
 
 }
