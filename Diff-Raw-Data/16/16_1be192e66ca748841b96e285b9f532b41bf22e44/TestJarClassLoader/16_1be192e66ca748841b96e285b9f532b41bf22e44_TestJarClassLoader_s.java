 package com.atlassian.plugin.loaders.classloading;
 
 import com.atlassian.plugin.mock.MockBear;
 import com.atlassian.plugin.util.ClassLoaderUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Enumeration;
 
 public class TestJarClassLoader extends AbstractTestClassLoader
 {
     /**
      * Here we try to load a specific JAR (paddington-test-plugin.jar) and get the descriptor out of it.
      */
     public void testLoader() throws URISyntaxException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
     {
         // hacky way of getting to the paddington-test-plugin.jar (assume it's two levels below src/test/etc/test-disabled-plugin.xml)
         File pluginsDirectory = getPluginsDirectory();
         File pluginJar = new File(pluginsDirectory, "paddington-test-plugin.jar");
 
         // make the JAR loader
        JarClassLoader loader = new JarClassLoader(pluginJar, ClassLoader.getSystemClassLoader());
 
         // now make sure we only got one descriptor back
         Enumeration descriptors = loader.findResources("atlassian-plugin.xml");
         assertTrue(descriptors.hasMoreElements());
         URL descriptor = (URL) descriptors.nextElement();
         assertTrue(descriptor.toExternalForm().endsWith("paddington-test-plugin.jar!atlassian-plugin.xml"));
         assertFalse(descriptors.hasMoreElements());
 
         // now try to load the plugin class and cast it to something in the global class path
         Class paddingtonClass = loader.findClass("com.atlassian.plugin.mock.MockPaddington");
         MockBear paddington = (MockBear) paddingtonClass.newInstance();
 
         // check we really did get the right class
         assertEquals("com.atlassian.plugin.mock.MockPaddington", paddington.getClass().getName());
     }
 }
