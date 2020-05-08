 package com.atlassian.plugin.loaders.classloading;
 
 import com.atlassian.plugin.mock.MockBear;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Enumeration;
 
 public class TestPluginsClassLoader extends AbstractTestClassLoader
 {
     public void testLoaderWithDirectory() throws URISyntaxException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
     {
         File pluginsDirectory = getPluginsDirectory();
 
        System.out.println("pluginsDirectory = " + pluginsDirectory);

         // first make a classloader of the entire directory.
         ClassLoader loader = PluginsClassLoader.getInstance(pluginsDirectory.toURL());
 
         // check we got the right one back
         assertEquals(URLClassLoader.class, loader.getClass());
 
         // we should get one descriptor back for each JAR in the directory (2)
         Enumeration descriptors = loader.getResources("paddington-test-plugin.jar");
         assertTrue(descriptors.hasMoreElements());
         URL descriptor = (URL) descriptors.nextElement();
         assertTrue(descriptor.toExternalForm().endsWith("plugins/paddington-test-plugin.jar"));
         assertFalse(descriptors.hasMoreElements());
 
         descriptors = loader.getResources("pooh-test-plugin.jar");
         assertTrue(descriptors.hasMoreElements());
         descriptor = (URL) descriptors.nextElement();
         assertTrue(descriptor.toExternalForm().endsWith("plugins/pooh-test-plugin.jar"));
         assertFalse(descriptors.hasMoreElements());
     }
 }
