 package org.apache.hadoop.test.runner.server;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.ref.SoftReference;
 import java.net.URL;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FsUrlStreamHandlerFactory;
 import org.apache.hadoop.test.runner.common.ClasspathUtil;
 
 /**
  * A <code>DistributedClassLoader</code> is an object that is responsible for
  * loading classes from <tt>Hadoop Distributed File System (HDFS)</tt>. Given
  * the name of a class, the instance of this class searches HDFS for a file
  * which contains this class, loads it and transforms into an instance of
  * java.lang.Class.
  * 
  * <p>
  * Like {@link java.net.URLClassLoader}, <code>DistributedClassLoader</code>
  * loads classes and resources from a search path of URLs. The search path is
  * acquired from the Hadoop configuration.
  * </p>
  * <p>
  * Note that <code>DistributedClassLoader</code> loads only classes which are on
  * a {@link org.apache.hadoop.mapreduce.Job} classpath or which are JRE-classes.
  * Hadoop dependencies will not be loaded.
  * </p>
  * 
  * @since 0.1
  */
 public class DistributedClassLoader extends ClassLoader {
 
     static {
         URL.setURLStreamHandlerFactory(new FsUrlStreamHandlerFactory());
     }
 
     /**
      * Delegation classloader which loads classes if:
      * <ul>
      * <li>The class exists on</li>
      * </ul>
      * 
      */
     private ClassLoader systemClassLoader;
 
     private Map<String, URL> classpath;
     private Map<String, SoftReference<Class<?>>> classesCache;
 
     private DistributedClassLoader(Map<String, URL> classpath,
             ClassLoader systemClassLoader) {
         this.classpath = classpath;
         this.classesCache = new HashMap<String, SoftReference<Class<?>>>();
         this.systemClassLoader = systemClassLoader;
     }
 
     @Override
     public Class<?> loadClass(final String name) throws ClassNotFoundException {
         Class<?> clazz = null;
         if (isClasspathClass(name)) {
             clazz = AccessController
                     .doPrivileged(new PrivilegedAction<Class<?>>() {
                         public Class<?> run() {
                             return loadClasspathClass(name);
                         }
                     });
         } else {
            systemClassLoader.loadClass(name);
         }
 
         if (clazz != null) {
             return clazz;
         } else {
             throw new ClassNotFoundException(name);
         }
     }
 
     private boolean isClasspathClass(String name) {
         return classpath.containsKey(name);
     }
 
     private Class<?> loadClasspathClass(final String name) {
         try {
             SoftReference<Class<?>> clazzRef = classesCache.get(name);
             if (clazzRef != null && clazzRef.get() != null) {
                 return clazzRef.get();
             }
             URL classURL = classpath.get(name);
             InputStream input = classURL.openStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream();
             byte[] buffer = new byte[256];
             int byteRead = 0;
             int totalBytes = 0;
             while ((byteRead = input.read(buffer)) != -1) {
                 output.write(buffer, 0, byteRead);
                 totalBytes += byteRead;
             }
             Class<?> clazz = defineClass(name, output.toByteArray(), 0,
                     totalBytes);
             if (clazz != null) {
                 classesCache.put(name, new SoftReference<Class<?>>(clazz));
                 return clazz;
             } else {
                 return null;
             }
         } catch (IOException e) {
             return null;
         }
     }
 
     @Override
     protected Class<?> findClass(String name) throws ClassNotFoundException {
         return loadClass(name);
     }
 
     @Override
     protected URL findResource(String name) {
         if (isClasspathClass(name)) {
             return super.findResource(name);
         }
         return null;
     }
 
     @Override
     public InputStream getResourceAsStream(String name) {
         if (isClasspathClass(name)) {
             return super.getResourceAsStream(name);
         }
         return null;
     }
 
     /**
      * Creates an instance of class loader capable of accessing resources from
      * HDFS.
      * 
      * @param configuration
      *            - hadoop job configuration.
      * @return
      */
     public static ClassLoader get(Configuration configuration) {
         try {
             return new DistributedClassLoader(
                     ClasspathUtil.classpathToURL(configuration),
                     getSystemClassLoader());
         } catch (IOException exception) {
             throw new RuntimeException(exception);
         }
     }
 
 }
