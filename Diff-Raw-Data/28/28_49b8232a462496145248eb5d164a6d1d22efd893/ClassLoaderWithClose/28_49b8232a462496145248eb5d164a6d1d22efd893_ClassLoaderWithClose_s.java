 package pimp.classloader;
 
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Collection;
 import java.util.jar.JarFile;
 
 /**
  * Custom URLClassLoader to fix the .jar file locking bug, see:
  * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5041014
  * 
  * Source code from StackOverflow.com:
  * http://stackoverflow.com/questions/3216780/problem-reloading-a-jar-using-urlclassloader
  */
 public class ClassLoaderWithClose extends URLClassLoader {
 
 	public ClassLoaderWithClose(URL[] urls) {
 	    super(urls);
 	}
 
     /**
      * Closes all open jar files
      */
     public void close() {
         try {
             Class<?> c = java.net.URLClassLoader.class;
             Field ucp = c.getDeclaredField("ucp");
             ucp.setAccessible(true);
             Object sunMiscURLClassPath = ucp.get(this);
             Field loaders = sunMiscURLClassPath.getClass().getDeclaredField("loaders");
             loaders.setAccessible(true);
            Object collection = loaders.get(sunMiscURLClassPath);
            for (Object sunMiscURLClassPathJarLoader : ((Collection<?>) collection).toArray()) {
                 try {
                     Field loader = sunMiscURLClassPathJarLoader.getClass().getDeclaredField("jar");
                     loader.setAccessible(true);
                     Object jarFile = loader.get(sunMiscURLClassPathJarLoader);
                     System.out.println("Closing jar file: " + jarFile);
                     ((JarFile) jarFile).close();
                 } catch (Throwable t) {
                     // if we got this far, this is probably not a JAR loader so skip it
                 }
             }
         } catch (Throwable t) {
             // probably not a SUN VM
         }
         return;
     }
 }
