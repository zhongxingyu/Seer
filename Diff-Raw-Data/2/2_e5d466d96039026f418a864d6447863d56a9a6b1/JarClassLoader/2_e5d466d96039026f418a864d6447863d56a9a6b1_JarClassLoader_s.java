 package com.epita.mti.plic.opensource.controlibserver.jarloader;
 
 import com.epita.mti.plic.opensource.controlibserver.plugin.PluginVersionControl;
 import com.epita.mti.plic.opensource.controlibserver.server.CLServer;
 import com.epita.mti.plic.opensource.controlibutility.plugins.CLObserver;
 import com.epita.mti.plic.opensource.controlibutility.plugins.CLObserverSend;
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.jar.JarFile;
 
 /**
  *
  * @author Benoit "KIDdAe" Vasseur This class is used to get all plugins
  * contained in a jar file sent by the client.
  */
 public class JarClassLoader {
 
     private JarFinder finder;
     private ArrayList<Class<?>> plugins;
     private CLServer server;
 
     public JarClassLoader(CLServer theServer) {
         // TODO Auto-generated constructor stub
         finder = new JarFinder();
         this.server = theServer;
         plugins = new ArrayList<Class<?>>();
     }
 
     public JarClassLoader() {
         // TODO Auto-generated constructor stub
         finder = new JarFinder();
         plugins = new ArrayList<Class<?>>();
     }
 
     /*
      * Find all existing plugins
      */
     public void initializeLoader() throws Exception {
         File[] f = finder.listFiles("plugins/");
         URLClassLoader loader;
         Enumeration enumeration;
         int length = f == null ? 0 : f.length;
 
         for (int i = 0; i < length; i++) {
             URL u = new URL("file://" + f[i].getAbsolutePath());
             loader = new URLClassLoader(new URL[]{
                         u
                     });
             JarFile jar = new JarFile(f[i].getAbsolutePath());
             enumeration = jar.entries();
 
             while (enumeration.hasMoreElements()) {
 
                 String tmp = enumeration.nextElement().toString();
                 if (tmp.length() > 6 && tmp.substring(tmp.length() - 6).compareTo(".class") == 0) {
                     tmp = tmp.substring(0, tmp.length() - 6);
                     tmp = tmp.replaceAll("/", ".");
                     plugins.add(Class.forName(tmp, true, loader));
                 }
             }
             // server.updatePlugins();
         }
     }
 
     public String getVersionForPlugin(Class<?> c) throws Exception
     {
         Class[] interfaces = c.getInterfaces();
         for (Class inter : interfaces) {
             if (c == CLObserver.class) {
                 Constructor<?> constructor = inter.getConstructor();
                 CLObserver observer = (CLObserver) constructor.newInstance();
                 return observer.getVersion();
             } else if (c == CLObserverSend.class) {
                 Constructor<?> constructor = inter.getConstructor();
                 CLObserverSend observer = (CLObserverSend) constructor.newInstance();
                 return observer.getVersion();
             }
         }
         return "";
     }
     
     public boolean testPlugins(String jarFile, int index) throws Exception
     {
         // tmp loader to test plugin version
        File tmpFile = new File("tmp/" + jarFile);
         URL uTmp = new URL("file://" + tmpFile.getAbsolutePath());
         URLClassLoader loaderTmp = new URLClassLoader(new URL[]{
                     uTmp
                 });
         JarFile jar = new JarFile(tmpFile.getAbsolutePath());
         Enumeration enumeration = jar.entries();
         PluginVersionControl pvc = new PluginVersionControl();
         while (enumeration.hasMoreElements()) {
             String tmp = enumeration.nextElement().toString();
             if (tmp.length() > 6 && tmp.substring(tmp.length() - 6).compareTo(".class") == 0) {
                 tmp = tmp.substring(0, tmp.length() - 6);
                 tmp = tmp.replaceAll("/", ".");
                 
                 Class<?> newClass = Class.forName(tmp, true, loaderTmp);
                 if (pvc.testPluginVersion(newClass,getVersionForPlugin(newClass))) {
                     return true;
                 }
             }
         }
         return false;
     }
     
     /*
      * Adds a new plugins to the already known plugins.
      * This method should be used when the server receive a new plugin definition.
      */
     public void addPlugins(String jarFile, int index) throws Exception {
         File file = new File(jarFile);
         URL u = new URL("file://" + file.getAbsolutePath());
         URLClassLoader loader = new URLClassLoader(new URL[]{
                     u
                 });
 
         JarFile jar = new JarFile(file.getAbsolutePath());
         Enumeration enumeration = jar.entries();
         PluginVersionControl pvc = new PluginVersionControl();
         while (enumeration.hasMoreElements()) {
             String tmp = enumeration.nextElement().toString();
             if (tmp.length() > 6 && tmp.substring(tmp.length() - 6).compareTo(".class") == 0) {
                 tmp = tmp.substring(0, tmp.length() - 6);
                 tmp = tmp.replaceAll("/", ".");
                 
                 plugins.add(Class.forName(tmp, true, loader));
             }
         }
         server.updatePlugins(index);
     }
 
     public ArrayList<Class<?>> getPlugins() {
         return plugins;
     }
 
     public JarFinder getFinder() {
         return finder;
     }
 
     public void setFinder(JarFinder finder) {
         this.finder = finder;
     }
 
     public CLServer getServer() {
         return server;
     }
 
     public void setServer(CLServer server) {
         this.server = server;
     }
 }
