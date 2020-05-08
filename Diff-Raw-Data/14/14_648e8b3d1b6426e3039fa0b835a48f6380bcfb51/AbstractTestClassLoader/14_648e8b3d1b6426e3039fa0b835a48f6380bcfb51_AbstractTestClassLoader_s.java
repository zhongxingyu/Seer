 package com.atlassian.plugin.loaders.classloading;
 
 import com.atlassian.plugin.util.ClassLoaderUtils;
 import com.atlassian.plugin.util.FileUtils;
 import com.atlassian.plugin.loaders.TestClassPathPluginLoader;
 import junit.framework.TestCase;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 public abstract class AbstractTestClassLoader extends TestCase
 {
     public static final String PADDINGTON_JAR = "paddington-test-plugin.jar";
     public static final String POOH_JAR = "pooh-test-plugin.jar";
 
     protected File pluginsDirectory;
     protected File tempDir;
     protected File pluginsTestDir;
 
     protected File getPluginsDirectory()
     {
         URL url = ClassLoaderUtils.getResource("plugins", TestClassPathPluginLoader.class);
         String path = url.toExternalForm().substring(5);
     	path = path.replace('/', File.separatorChar);
         pluginsDirectory = new File(path);
         return pluginsDirectory;
     }
 
     /**
      * Generate a random string of characters - including numbers
      */
     public static String randomString(int length)
     {
         StringBuffer b = new StringBuffer(length);
 
         for (int i = 0; i < length; i++)
         {
             b.append(randomAlpha());
         }
 
         return b.toString();
     }
 
     /**
      * Generate a random character from the alphabet - either a-z or A-Z
      */
     public static char randomAlpha()
     {
         int i = (int) (Math.random() * 52);
 
         if (i > 25)
             return (char) (97 + i - 26);
         else
             return (char) (65 + i);
     }
 
     protected void createFillAndCleanTempPluginDirectory() throws IOException
     {
         pluginsDirectory = getPluginsDirectory(); // hacky way of getting to the directoryPluginLoaderFiles classloading
         tempDir = new File(System.getProperty("java.io.tmpdir"));
 
         File pluginsDir = new File(tempDir.toString() + File.separator +  "plugins");
         pluginsTestDir = new File(pluginsDir, randomString(6));
 
         if (pluginsDir.exists() && pluginsDir.isDirectory())
             assertTrue(FileUtils.deleteDir(pluginsDir));
 
         pluginsTestDir.mkdirs();
 
         FileUtils.copyDirectory(pluginsDirectory, pluginsTestDir);
 
         // Clean up CVS directory in case we copied it over by mistake.
         File cvsDir = new File(pluginsTestDir, "CVS");
         if (cvsDir.exists())
             assertTrue(FileUtils.deleteDir(cvsDir));
     }
 }
