 package com.atlassian.maven.plugins.amps.product;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 import com.atlassian.maven.plugins.amps.MavenGoals;
 import com.atlassian.maven.plugins.amps.Product;
 import com.atlassian.maven.plugins.amps.ProductArtifact;
 
 public abstract class AbstractWebappProductHandler implements ProductHandler
 {
     protected final MavenGoals goals;
     protected final MavenProject project;
 
     public AbstractWebappProductHandler(final MavenProject project, final MavenGoals goals)
     {
         this.project = project;
         this.goals = goals;
     }
 
     public int start(final Product ctx) throws MojoExecutionException
     {
         // Copy the webapp war to target
         final File webappWar = goals.copyWebappWar(getId(), getBaseDirectory(),
                 new ProductArtifact(getArtifact().getGroupId(), getArtifact().getArtifactId(), ctx.getVersion()));
 
         final File homeDir = extractAndProcessHomeDirectory(ctx);
 
         final File combinedWebappWar = addArtifacts(ctx, homeDir, webappWar);
 
         return goals.startWebapp(getId(), combinedWebappWar, getSystemProperties(), getExtraContainerDependencies(), ctx);
     }
 
     public void stop(final Product ctx) throws MojoExecutionException
     {
         goals.stopWebapp(getId(), ctx.getContainerId());
     }
 
     private List<ProductArtifact> getPluginsArtifacts(final Product ctx)
     {
         final List<ProductArtifact> artifacts = new ArrayList<ProductArtifact>();
         artifacts.addAll(getDefaultPlugins());
         artifacts.addAll(ctx.getPluginArtifacts());
 
         if (ctx.getSalVersion() != null)
         {
             artifacts.addAll(getSalArtifacts(ctx.getSalVersion()));
         }
 
         if (ctx.getPdkVersion() != null)
         {
             artifacts.add(new ProductArtifact("com.atlassian.pdkinstall", "pdkinstall-plugin", ctx.getPdkVersion()));
         }
 
         if (ctx.getRestVersion() != null)
         {
             artifacts.add(new ProductArtifact("com.atlassian.plugins.rest", "atlassian-rest-module", ctx.getRestVersion()));
         }
 
         return artifacts;
     }
 
     private File addArtifacts(final Product ctx, final File homeDir, final File webappWar) throws MojoExecutionException
     {
         try
         {
             final String webappDir = new File(getBaseDirectory(), "webapp").getAbsolutePath();
             if (!new File(webappDir).exists())
             {
                 unzip(webappWar, webappDir);
             }
 
             File pluginsDir = getPluginsDirectory(webappDir, homeDir);
             final File bundledPluginsDir = new File(getBaseDirectory(), "bundled-plugins");
 
             bundledPluginsDir.mkdir();
             // add bundled plugins
             final File bundledPluginsZip = new File(webappDir, getBundledPluginPath());
             if (bundledPluginsZip.exists())
             {
                 unzip(bundledPluginsZip, bundledPluginsDir.getPath());
             }
 
             if (isStaticPlugin())
             {
                 pluginsDir = new File(webappDir, "WEB-INF/lib");
             }
 
             if (pluginsDir==null)
             {
                 pluginsDir = bundledPluginsDir;
             }
 
            pluginsDir.mkdirs();
             // add this plugin itself
             addThisPluginToDirectory(pluginsDir);
             // add plugins2 plugins
             addArtifactsToDirectory(goals, getPluginsArtifacts(ctx), pluginsDir);
 
             // add plugins1 plugins
             List<ProductArtifact> artifacts = new ArrayList<ProductArtifact>();
             artifacts.addAll(getDefaultLibPlugins());
             artifacts.addAll(ctx.getLibArtifacts());
             addArtifactsToDirectory(goals, artifacts, new File(webappDir, "WEB-INF/lib"));
 
             artifacts = new ArrayList<ProductArtifact>();
             artifacts.addAll(getDefaultBundledPlugins());
             artifacts.addAll(ctx.getBundledArtifacts());
 
             addArtifactsToDirectory(goals, artifacts, bundledPluginsDir);
 
             if (bundledPluginsDir.list().length > 0)
             {
                 com.atlassian.core.util.FileUtils.createZipFile(bundledPluginsDir, bundledPluginsZip);
             }
 
             // add log4j.properties file if specified
             if (ctx.getLog4jProperties() != null)
             {
                 FileUtils.copyFile(ctx.getLog4jProperties(), new File(webappDir, "WEB-INF/classes/log4j.properties"));
             }
 
             final File warFile = new File(webappWar.getParentFile(), getId() + ".war");
             com.atlassian.core.util.FileUtils.createZipFile(new File(webappDir), warFile);
             return warFile;
 
         }
         catch (final Exception e)
         {
             e.printStackTrace();
             throw new MojoExecutionException(e.getMessage());
         }
     }
 
     private void unzip(final File zipFile, final String destDir) throws IOException
     {
         final ZipFile zip = new ZipFile(zipFile);
         final Enumeration<? extends ZipEntry> entries = zip.entries();
         while (entries.hasMoreElements())
         {
             final ZipEntry zipEntry = entries.nextElement();
             final File file = new File(destDir + "/" + zipEntry.getName());
             if (zipEntry.isDirectory())
             {
                 file.mkdirs();
                 continue;
             }
             InputStream is = null;
             OutputStream fos = null;
             try
             {
                 is = zip.getInputStream(zipEntry);
                 fos = new FileOutputStream(file);
                 IOUtils.copy(is, fos);
             }
             finally
             {
                 IOUtils.closeQuietly(is);
                 IOUtils.closeQuietly(fos);
             }
         }
     }
 
     private File getBaseDirectory()
     {
         final File dir = new File(project.getBuild().getDirectory(), getId());
         dir.mkdir();
         return dir;
     }
 
     private File extractAndProcessHomeDirectory(final Product ctx) throws MojoExecutionException
     {
         if (getTestResourcesArtifact() != null)
         {
 
             final File outputDir = getBaseDirectory();
             final File confHomeZip = goals.copyHome(outputDir,
                     new ProductArtifact(
                             getTestResourcesArtifact().getGroupId(),
                             getTestResourcesArtifact().getArtifactId(),
                             ctx.getTestResourcesVersion()));
             final File tmpDir = new File(getBaseDirectory(), "tmp-resources");
             tmpDir.mkdir();
 
             File homeDir;
             try
             {
                 unzip(confHomeZip, tmpDir.getPath());
                 FileUtils.copyDirectory(tmpDir.listFiles()[0], outputDir);
                 final String homeDirName = tmpDir.listFiles()[0].listFiles()[0].getName();
                 homeDir = new File(outputDir, homeDirName);
                 overrideAndPatchHomeDir(homeDirName);
             }
             catch (final IOException ex)
             {
                 throw new MojoExecutionException("Unable to copy home directory", ex);
             }
             processHomeDirectory(ctx, homeDir);
             return homeDir;
         }
         else
         {
             return getHomeDirectory();
         }
     }
 
     private void overrideAndPatchHomeDir(final String homeDirName) throws IOException
     {
         final File srcDir = new File(project.getBasedir(), "src/test/resources/"+homeDirName);
         final File outputDir = new File(getBaseDirectory(), homeDirName);
         if (srcDir.exists() && outputDir.exists())
             FileUtils.copyDirectory(srcDir, outputDir);
     }
 
     private boolean isStaticPlugin() throws IOException
     {
         final File atlassianPluginXml = new File(project.getBasedir(), "src/main/resources/atlassian-plugin.xml");
         if (atlassianPluginXml.exists())
         {
             String text = FileUtils.readFileToString(atlassianPluginXml);
             return !text.contains("pluginsVersion=\"2\"") && !text.contains("plugins-version=\"2\"");
         }
         else
         {
             // probably an osgi bundle
             return false;
         }
     }
 
     private void addThisPluginToDirectory(final File pluginsDir) throws IOException
     {
         final File thisPlugin = getPluginFile();
 
         // remove any existing version
         for (final Iterator<?> iterateFiles = FileUtils.iterateFiles(pluginsDir, null, false); iterateFiles.hasNext();)
         {
             final File file = (File) iterateFiles.next();
             if (file.getName().contains(project.getArtifactId()))
             {
                 file.delete();
             }
         }
 
         // add the plugin jar to the directory
         FileUtils.copyFile(thisPlugin, new File(pluginsDir, thisPlugin.getName()));
 
     }
 
     private File getPluginFile()
     {
         return new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".jar");
     }
 
     private void addArtifactsToDirectory(final MavenGoals goals, final List<ProductArtifact> artifacts, final File pluginsDir) throws MojoExecutionException
     {
         // first remove plugins from the webapp that we want to update
         if (pluginsDir.isDirectory() && pluginsDir.exists())
         {
             for (final Iterator<?> iterateFiles = FileUtils.iterateFiles(pluginsDir, null, false); iterateFiles.hasNext();)
             {
                 final File file = (File) iterateFiles.next();
                 for (final ProductArtifact webappArtifact : artifacts)
                 {
                     if (!file.isDirectory() && file.getName().contains(webappArtifact.getArtifactId()))
                     {
                         file.delete();
                     }
                 }
             }
         }
         // copy the all the plugins we want in the webapp
         if (!artifacts.isEmpty())
         {
             goals.copyPlugins(pluginsDir, artifacts);
         }
     }
 
     protected abstract File getHomeDirectory();
 
     protected abstract void processHomeDirectory(Product ctx, File homeDir) throws MojoExecutionException;
 
     protected abstract ProductArtifact getTestResourcesArtifact();
 
     protected abstract Collection<ProductArtifact> getDefaultPlugins();
 
     protected abstract Collection<ProductArtifact> getDefaultBundledPlugins();
 
     protected abstract Collection<ProductArtifact> getDefaultLibPlugins();
 
     protected abstract String getBundledPluginPath();
 
     protected abstract File getPluginsDirectory(String webappDir, File homeDir);
 
     protected abstract List<ProductArtifact> getExtraContainerDependencies();
 
     protected abstract Map<String, String> getSystemProperties();
 
     protected abstract ProductArtifact getArtifact();
 
     protected abstract Collection<ProductArtifact> getSalArtifacts(String salVersion);
 
 }
