 package org.kevoree.microsandbox.samples.benchmark.dacapo;
 
 import org.jetbrains.annotations.NotNull;
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.framework.kaspects.TypeDefinitionAspect;
 import org.kevoree.kcl.KevoreeJarClassLoader;
import org.kevoree.log.Log;
 import org.kevoree.microsandbox.api.contract.CPUContracted;
 import org.kevoree.microsandbox.api.contract.MemoryContracted;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Hashtable;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 /**
  * Created with IntelliJ IDEA.
  * User: inti
  * Date: 9/5/13
  * Time: 2:22 PM
  * To change this template use File | Settings | File Templates.
  */
 @DictionaryType({
         @DictionaryAttribute(name = "dacapo_path",dataType = String.class),
         @DictionaryAttribute(name = "dacapo_test", dataType = String.class),
         @DictionaryAttribute(name = "dacapo_n", dataType = Integer.class, defaultValue = "1")
 })
 @ComponentType
 public class RunningDacapoComponent extends AbstractComponentType
                     implements MemoryContracted, CPUContracted {
 
 
     private String path;
     private String test;
     private Integer n;
     private ClassLoader loader;
 
     class MyLoader extends URLClassLoader {
 
         int i = 0;
 
         public MyLoader(URL[] urls, ClassLoader parent) {
             super(urls, parent);
         }
 
         @NotNull
         @Override
         protected Class<?> findClass(String name) throws ClassNotFoundException {
            Log.debug("Finding class {} {}", name, i++);
             return super.findClass(name);
         }
     }
 
     class DacapoExecuter extends Thread {
 
         @Override
         public void run() {
             try {
                 Class<?> cl = loader.loadClass("Harness");
                 Method method = cl.getMethod("main", new Class[]{String[].class});
 
                 method.invoke(null,new Object[]{new String[]{
                         "-noValidation",
                         "-n",
                         n.toString(),
                         test}});
 
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             } catch (NoSuchMethodException e) {
                 e.printStackTrace();
             } catch (InvocationTargetException e) {
//                e.printStackTrace();
                Log.error("Not a big deal if the test has finished");
             }
         }
     }
 
 
     @Start
     public void start() {
         path = getDictionary().get("dacapo_path").toString();
         test = getDictionary().get("dacapo_test").toString();
         n = Integer.parseInt(getDictionary().get("dacapo_n").toString());
         try {
             KevoreeJarClassLoader ll = (KevoreeJarClassLoader)this.getClass().getClassLoader();
             ll.add(new File(path).toURI().toURL());
 
             loader = ll;//new MyLoader(new URL[]{new File(path).toURI().toURL()}, this.getClass().getClassLoader());
         } catch (MalformedURLException e) {
             e.printStackTrace();
         }
         Thread th = new Thread(new DacapoExecuter());
        Log.debug("Within RunningDacapoComponent => TG {}",  th.getThreadGroup().getName());
         th.setContextClassLoader(loader);
         th.start();
     }
 
     @Stop
     public void stop() {
 
     }
 
     @Update
     public void update() {
         stop();
         start();
     }
 }
