 package name.richardson.james.bukkit.utilities.updater;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.bukkit.Bukkit;
 import org.xml.sax.SAXException;
 
 import name.richardson.james.bukkit.utilities.internals.Logger;
 import name.richardson.james.bukkit.utilities.plugin.SimplePlugin;
 
 public class PluginUpdater implements Runnable {
 
   private final Logger logger = new Logger(PluginUpdater.class);
   
   private final SimplePlugin plugin;
   
   private MavenManifest manifest;
   
   public PluginUpdater(SimplePlugin plugin) {
     this.plugin = plugin;
   }
 
   private boolean isNewVersionAvailable() {
     return !plugin.getDescription().getVersion().equalsIgnoreCase(manifest.getCurrentVersion());
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
     logger.info(this.plugin.getMessage("updater-checking-for-new-version"));
     
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
       logger.info(this.plugin.getSimpleFormattedMessage("updater-newer-version-available", this.manifest.getCurrentVersion()));
       try {
         // create the path for the updated plugin
         StringBuilder path = new StringBuilder();
         path.append(this.plugin.getServer().getUpdateFolderFile());
         path.append(File.separatorChar);
         path.append(this.plugin.getDescription().getName());
         path.append(".jar");
         // create the URL of the updated plugin
         // download the update to the update folder
         this.fetchFile(this.getPluginURL(), new File(path.toString()));
       } catch (MalformedURLException e) {
         logger.warning(this.plugin.getMessage("updater-unable-to-get-plugin"));
        e.printStackTrace();
       } catch (IOException e) {
         logger.warning(this.plugin.getMessage("updater-unable-to-save-file"));
        e.printStackTrace();
       }
     }
     
     logger.info(this.plugin.getMessage("updater-plugin-updated"));
     
   }
   
   private void parseMavenMetaData() throws IOException, SAXException, ParserConfigurationException {
     File temp = File.createTempFile(this.plugin.getArtifactID(), null);
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
