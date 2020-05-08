 /*
  * ServletClassLoader.java
  *
  * Created on April 18, 2005, 1:32 PM
  */
 
 package org.xins.common.servlet.container;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 
 //import org.apache.tools.ant.loader.AntClassLoader2;
 //import org.apache.tools.ant.BuildException;
 
 /**
  *
  * @author anthonyg
  */
 public class ServletClassLoader {
    
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Get the class loader that will loader the servlet.
     *
     * @param warFile
     *    The WAR file containing the Servlet.
     *
     * @param mode
     *    the mode in which the servlet should be loaded. The possible values are
     *    <code>USE_CURRENT_CLASSPATH</code>, <code>USE_CLASSPATH_LIB</code>,
     *    <code>USE_XINS_LIB</code>, <code>USE_WAR_LIB</code>,
     *    <code>USE_WAR_EXTERNAL_LIB</code>.
     *
     * @return
     *    The Class loader to use to load the Servlet.
     *
     * @throws IOException
     *    if the file cannot be read or is incorrect.
     */
    public static ClassLoader getServletClassLoader(File warFile, int mode) throws IOException {
       if (mode == USE_CURRENT_CLASSPATH) {
          return ServletClassLoader.class.getClassLoader();
       }
       List urlList = new ArrayList();
       if (mode != USE_WAR_EXTERNAL_LIB) {
          URL classesURL = new URL("jar:file:" + warFile.getAbsolutePath().replace(File.separatorChar, '/') + "!/WEB-INF/classes/");
          urlList.add(classesURL);
       }
       
       List standardLibs = new ArrayList();
       if (mode == USE_XINS_LIB) {
          String classLocation = ServletClassLoader.class.getProtectionDomain().getCodeSource().getLocation().toString();
          String commonJar = classLocation.substring(6).replace('/', File.separatorChar);
          File baseDir = new File(commonJar).getParentFile();
          File[] xinsFiles = baseDir.listFiles(); 
          for (int i = 0; i < xinsFiles.length; i++) {
             if (xinsFiles[i].getName().endsWith(".jar")) {
                urlList.add(xinsFiles[i].toURL());
             }
          }
          File libDir = new File(baseDir, ".." + File.separator + "lib");
          File[] libFiles = libDir.listFiles(); 
          for (int i = 0; i < libFiles.length; i++) {
             if (libFiles[i].getName().endsWith(".jar")) {
                urlList.add(libFiles[i].toURL());
             }
          }
       }
       if (mode == USE_CLASSPATH_LIB || mode == USE_WAR_EXTERNAL_LIB) {
          String classPath = System.getProperty("java.class.path");
          StringTokenizer stClassPath = new StringTokenizer(classPath, File.pathSeparator);
          while (stClassPath.hasMoreTokens()) {
             String nextPath = stClassPath.nextToken();
             if (nextPath.toLowerCase().endsWith(".jar")) {
                standardLibs.add(nextPath.substring(nextPath.lastIndexOf(File.separatorChar) + 1));
             }
             urlList.add(new File(nextPath).toURL());
          }
       }
       if (mode == USE_WAR_LIB || mode == USE_WAR_EXTERNAL_LIB) {
          JarInputStream jarStream = new JarInputStream(new FileInputStream(warFile));
          JarEntry entry = jarStream.getNextJarEntry();
          while(entry != null) {
             String entryName = entry.getName();
             if (entryName.startsWith("WEB-INF/lib/") && entryName.endsWith(".jar") && !standardLibs.contains(entryName.substring(12))) {
                File tempJarFile = unpack(jarStream, entryName);
               System.err.println(":"+tempJarFile.toURL());
                urlList.add(tempJarFile.toURL());
             }
             entry = jarStream.getNextJarEntry();
          }
          jarStream.close();
       }
       /*AntClassLoader2 loader = new AntClassLoader2();
       loader.setParentFirst(false);
       loader.setParent(null);
       for (int i=0; i<urlList.size(); i++) {
          try {
             loader.addPathElement((String) urlList.get(i));
          } catch (BuildException bex) {
             bex.printStackTrace();
          }
       }*/
       URL[] urls = new URL[urlList.size()];
       for (int i=0; i<urlList.size(); i++) {
          urls[i] = (URL) urlList.get(i);
         //System.err.println("adding " + urlList.get(i));
       }
       ClassLoader loader = new ChildFirstClassLoader(urls, ServletClassLoader.class.getClassLoader());
       Thread.currentThread().setContextClassLoader(loader);
       return loader;
    }
 
    /**
     * Unpack the specified entry from the JAR file.
     *
     * @param jarStream
     *    The input stream of the JAR file positioned at the entry.
     * @param entryName
     *    The name of the entry to extract.
     *
     * @return
     *    The extracted file. The created file is a temporary file in the 
     *    temporary directory.
     *
     * @throws IOException
     *    if the JAR file cannot be read or is incorrect.
     */
    private static File unpack(JarInputStream jarStream, String entryName) throws IOException {
       String libName = entryName.substring(entryName.lastIndexOf('/') + 1, entryName.length() - 4);
       File tempJarFile = File.createTempFile(libName, ".jar");
       FileOutputStream out = new FileOutputStream(tempJarFile);
     
       // Transfer bytes from the JAR file to the output file
       byte[] buf = new byte[8192];
       int len;
       while ((len = jarStream.read(buf)) > 0) {
          out.write(buf, 0, len);
       }
       out.close();
       return tempJarFile;
    }
    
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Use the current class loader to load the servlet and the libraries.
     */
    public final static int USE_CURRENT_CLASSPATH = 1;
    
    /**
     * Load the Servlet code from the WAR file and use the current
     * classpath for the libraries.
     */
    public final static int USE_CLASSPATH_LIB = 2;
 
    /**
     * Load the servlet code from the WAR file and try to find the libraries
     * in the same directory as this xins-common.jar and &lt:parent&gt;/lib
     * directory.
     */
    public final static int USE_XINS_LIB = 3;
 
    /**
     * Load the servlet code and the libraries from the WAR file.
     * This may take some time as the libraries need to be extracted from the 
     * WAR file.
     */
    public final static int USE_WAR_LIB = 4;
 
    /**
     * Load the servlet code and the standard libraries from the CLASSPATH.
     * Load the included external libraries from the WAR file.
     */
    public final static int USE_WAR_EXTERNAL_LIB = 5;
    
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * An almost trivial no-fuss implementation of a class loader 
     * following the child-first delegation model.
     * 
     * @author <a href="http://www.qos.ch/log4j/">Ceki Gulcu</a>
     */
    private static class ChildFirstClassLoader extends URLClassLoader {
 
       public ChildFirstClassLoader(URL[] urls) {
          super(urls);
       }
 
       public ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
          super(urls, parent);
       }
 
       public void addURL(URL url) {
          super.addURL(url);
       }
 
       public Class loadClass(String name) throws ClassNotFoundException {
          return loadClass(name, false);
       }
 
       /**
        * We override the parent-first behavior established by 
        * java.land.Classloader.
        * <p>
        * The implementation is surprisingly straightforward.
        */
       protected Class loadClass(String name, boolean resolve)
       throws ClassNotFoundException {
 
          //System.out.println("ChildFirstClassLoader("+name+", "+resolve+")");
 
          // First, check if the class has already been loaded
          Class c = findLoadedClass(name);
 
          // if not loaded, search the local (child) resources
          if (c == null) {
             try {
                c = findClass(name);
             } catch(ClassNotFoundException cnfe) {
                // ignore
             }
          }
 
          // if we could not find it, delegate to parent
          // Note that we don't attempt to catch any ClassNotFoundException
          if (c == null) {
             if (getParent() != null) {
                c = getParent().loadClass(name);
             } else {
                c = getSystemClassLoader().loadClass(name);
             }
          }
 
          if (resolve) {
             resolveClass(c);
          }
 
          return c;
       }
 
       /**
        * Override the parent-first resource loading model established by
        * java.land.Classloader with child-first behavior.
        */
       public URL getResource(String name) {
          URL url = findResource(name);
 
          // if local search failed, delegate to parent
          if(url == null) {
             url = getParent().getResource(name);
          }
          return url;
      }
    }
 }
