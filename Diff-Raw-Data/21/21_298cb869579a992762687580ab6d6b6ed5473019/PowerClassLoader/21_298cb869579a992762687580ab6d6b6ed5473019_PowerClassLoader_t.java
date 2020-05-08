 package jpower.core;
 
import jpower.core.internal.PowerInternalSystem;

 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.function.Consumer;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 
 @SuppressWarnings("ZeroLengthArrayAllocation")
 public class PowerClassLoader extends URLClassLoader {
 
     private boolean autoload = false;
     private Consumer<Class<?>> onClassLoaded;
     private Consumer<String> onClassFound;
 
     public PowerClassLoader() {
         this(new URL[0]);
     }
 
     public PowerClassLoader(URL[] urls) {
         super(urls);
     }
 
     @Override
     public Class<?> loadClass(String name) throws ClassNotFoundException {
         Class<?> clazz = loadClass(name, true);
 
         if (onClassLoaded != null) {
             onClassLoaded.accept(clazz);
         }
 
         return clazz;
     }
 
     @Override
     public void addURL(URL url) {
         super.addURL(url);
         if (autoload) {
             try {
                if (!url.toString().endsWith(".jar")) {
                     return;
                 }
                 JarInputStream stream = new JarInputStream(url.openStream());
                 JarEntry entry;
                 while ((entry = stream.getNextJarEntry()) != null) {
                     String entryName = entry.getName();
                     if (entryName.endsWith(".class")) {
                         String className = entryName.replace("/", ".").substring(0, entryName.length() - 6);
                         if (onClassFound != null) {
                             onClassFound.accept(className);
                         }
                         loadClass(className);
                     }
                 }
             } catch (IOException | ClassNotFoundException ignored) {
                 System.out.println("Failed to autoload!");
             }
         }
     }
 
     public void enableAutoLoading() {
         autoload = true;
     }
 
    public Class<?>[] getLoadedClasses() {
        return PowerInternalSystem.getLoadedClasses(this);
    }

     public Collection<URL> getURLS() {
         return Arrays.asList(getURLs());
     }
 
     public void onClassLoaded(Consumer<Class<?>> onClassLoaded) {
         this.onClassLoaded = onClassLoaded;
     }
 
     public void onClassFound(Consumer<String> onClassFound) {
         this.onClassFound = onClassFound;
     }
 }
