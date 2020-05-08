 package org.tpi.pso.gemfire.dsl.loader;
 
 import com.gemstone.gemfire.cache.Region;
 
 public class DistributedClassLoader extends ClassLoader {
 
     private final Region<String, byte[]> region;
 
     public DistributedClassLoader(Region<String, byte[]> region,
             ClassLoader parent) {
         super(parent);
         this.region = region;
     }
 
     @Override
     protected Class<?> loadClass(String name, boolean resolve)
             throws ClassNotFoundException {
         return super.loadClass(name, resolve);
     }
 
     @Override
     protected Class<?> findClass(String className)
             throws ClassNotFoundException {
 
         Class<?> c = super.findLoadedClass(className);
         if (c != null)
             return c;
 
         try {
             return super.findSystemClass(className);
         } catch (ClassNotFoundException e) {
             //
         }
 
         if (className.startsWith("java."))
             throw new ClassNotFoundException();
 
         try {
             return super.findClass(className);
         } catch (ClassNotFoundException e) {
             //
         }
 
         if (region.containsKey(className)) {
             byte[] raw = region.get(className);
             Class<?> found = defineClass(className, raw, 0, raw.length);
             resolveClass(found);
             return found;
         }
 
         throw new ClassNotFoundException("Not found in classloader region: "
                 + className);
     }
 
 }
