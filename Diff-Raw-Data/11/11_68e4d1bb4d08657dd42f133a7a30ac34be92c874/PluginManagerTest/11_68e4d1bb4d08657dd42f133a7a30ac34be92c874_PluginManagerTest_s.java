 package ch9k.plugins;
 
 import ch9k.configuration.Storage;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class PluginManagerTest {    
     /**
      * Test plugin installation.
      */
     @Test
     public void testInstallPlugin() throws MalformedURLException {
         /* Remove the original plugin. */
         File file = new File(Storage.getStorageDirectory(),
                 "plugins/DummyPlugin.jar");
         file.delete();
 
         /* Obtain the plugin manager. */
         PluginManager manager = new PluginManager();
         PluginInstaller installer = manager.getPluginInstaller();
 
         /* Install the plugin from the web. */
         String url = "http://zeus.ugent.be/~jasper/DummyPlugin.jar";
         installer.installPlugin(new URL(url));
 
         /* The plugin class should now be available. */
        String expectedPlugin = "be.ugent.zeus.DummyPlugin";
        assertTrue(manager.getAvailablePlugins().contains(expectedPlugin));
     }
 }
