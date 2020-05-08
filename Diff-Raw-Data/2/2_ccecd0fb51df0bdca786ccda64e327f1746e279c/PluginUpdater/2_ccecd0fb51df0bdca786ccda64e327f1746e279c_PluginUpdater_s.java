 package name.richardson.james.bukkit.utilities.updater;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
 import org.xml.sax.SAXException;
 
 import name.richardson.james.bukkit.utilities.internals.Logger;
 import name.richardson.james.bukkit.utilities.plugin.SimplePlugin;
 
 public class PluginUpdater implements Runnable {
 
   private final Logger logger = new Logger(PluginUpdater.class);
   
   private final SimplePlugin plugin;
   
   private MavenManifest manifest;
 
   private boolean installUpdates;
   
   public PluginUpdater(SimplePlugin plugin, boolean install) {
     this.plugin = plugin;
     this.installUpdates = install;
     if (plugin.isDebugging()) logger.setDebugging(true);
   }
 
   private boolean isNewVersionAvailable() {
     DefaultArtifactVersion current = new DefaultArtifactVersion(plugin.getDescription().getVersion());
     logger.debug("Current local version: " + current.toString());
     DefaultArtifactVersion target = new DefaultArtifactVersion(manifest.getCurrentVersion());
     logger.debug("Latest remote version: " + target.toString());
     if (current.compareTo(target) == -1) {
       return true;
     } else {
       return false;
     }
   }
   
   private URL getMavenMetaDataURL() throws MalformedURLException {
     StringBuilder path = new StringBuilder();
     path.append(this.plugin.getRepositoryURL());
     path.append("/");
     path.append(this.plugin.getGroupID().replace(".", "/"));
     path.append("/");
     path.append(this.plugin.getArtifactID());
     path.append("/maven-metadata.xml");
     return new URL(path.toString());
   }
   
   private URL getPluginURL() throws MalformedURLException {
     String version = manifest.getCurrentVersion();
     StringBuilder path = new StringBuilder();
     path.append(this.plugin.getRepositoryURL());
     path.append("/");
     path.append(this.plugin.getGroupID().replace(".", "/"));
     path.append("/");
     path.append(this.plugin.getArtifactID());
     path.append("/");
     path.append(version);
     path.append("/");
     path.append(this.plugin.getArtifactID());
     path.append("-");
     path.append(version);
     path.append(".jar");
     return new URL(path.toString());
   }
 
   public void run() {
     
     this.logger.setPrefix("[" + plugin.getName() + "] ");
     logger.debug(this.plugin.getMessage("updater-checking-for-new-version"));
     
     try {
       this.parseMavenMetaData();
     } catch (IOException e) {
       logger.warning(this.plugin.getMessage("updater-unable-to-save-file"));
       e.printStackTrace();
     } catch (SAXException e) {
       logger.warning(this.plugin.getMessage("updater-unable-to-read-metadata"));
       e.printStackTrace();
     } catch (ParserConfigurationException e) {
       logger.warning(this.plugin.getMessage("updater-unable-to-read-metadata"));
       e.printStackTrace();
     }
     
     if (this.isNewVersionAvailable()) {
       if (this.installUpdates) {
         try {
           // create the path for the updated plugin
           StringBuilder path = new StringBuilder();
           path.append(this.plugin.getServer().getUpdateFolderFile().getAbsolutePath());
           path.append(File.separatorChar);
           path.append(this.plugin.getDescription().getName());
           path.append(".jar");
           // create the URL of the updated plugin
           // download the update to the update folder
           logger.debug("Path to save updated plugin: " + path);
           File storage = new File(path.toString());
           storage.createNewFile();
           // normalise the plugin name as necessary
           this.normalisePluginFileName();
           this.fetchFile(this.getPluginURL(), storage);
          logger.info(this.plugin.getMessage("updater-plugin-updated"));
         } catch (MalformedURLException e) {
           logger.warning(this.plugin.getMessage("updater-unable-to-get-plugin"));
           e.printStackTrace();
         } catch (IOException e) {
           logger.warning(this.plugin.getMessage("updater-unable-to-save-file"));
           e.printStackTrace();
         }
       } else {
         logger.info(this.plugin.getSimpleFormattedMessage("updater-newer-version-available", this.manifest.getCurrentVersion()));
       }
     } 
   }
   
   // This is used to search the plugin directory and then change the name of the plugin
   // if necessary. The .jar should match the name of the plugin as defined in plugin.yml.
   // This is necessary otherwise the updater in Bukkit will not work.
   private File getPluginFile() {
     File plugins = plugin.getDataFolder().getParentFile();
     String[] files = plugins.list(new PluginFilter(this.plugin));
     logger.debug(files.toString());
     return new File(plugin.getDataFolder().getParentFile().toString() + File.separatorChar + files[0]);
   }
   
   private void normalisePluginFileName() {
     String name = plugin.getName() + ".jar";
     File plugin = this.getPluginFile();
     if (!plugin.getName().equals(name)) {
       logger.debug("Plugin file name is inconsistent. Renaming to " + name + ".");
       File file = new File(plugin.getParentFile().toString() + File.separatorChar + name);
       plugin.renameTo(file);
     }
   }
   
   private void parseMavenMetaData() throws IOException, SAXException, ParserConfigurationException {
     File temp = File.createTempFile(this.plugin.getClass().getSimpleName() + "-", null);
     this.fetchFile(this.getMavenMetaDataURL(), temp);
     this.manifest = new MavenManifest(temp);
   }
   
   private void fetchFile(URL url, File storage) throws IOException {
     logger.debug("Fetching resource from " + url.toString());
     ReadableByteChannel rbc = Channels.newChannel(url.openStream());
     logger.debug("Saving resources to " + storage.getPath());
     FileOutputStream fos = new FileOutputStream(storage);
     fos.getChannel().transferFrom(rbc,  0, 1 << 24);
     rbc.close();
     fos.close();
   }
   
 }
