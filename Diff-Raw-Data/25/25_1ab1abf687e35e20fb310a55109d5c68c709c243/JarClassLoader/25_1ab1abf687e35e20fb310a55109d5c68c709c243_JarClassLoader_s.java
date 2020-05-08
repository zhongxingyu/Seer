 package com.epita.mti.plic.opensource.controlibserversample.jarloader;
 
 import com.epita.mti.plic.opensource.controlibserversample.ServerSample;
 import java.io.File;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.jar.JarFile;
 
 public class JarClassLoader
 {
 
   private JarFinder finder;
   private ArrayList<Class<?>> plugins;
 
   public JarClassLoader()
   {
     // TODO Auto-generated constructor stub
     finder = new JarFinder();
   }
 
   public void initializeLoader() throws Exception
   {
    File[] f = finder.listFiles("pugins/");
     URLClassLoader loader;
     Enumeration enumeration;
     int length = f == null ? 0 : f.length;
     
     plugins = new ArrayList<Class<?>>();
     
     for (int i = 0; i < length; i++)
     {
       URL u = new URL("file://" + f[i].getAbsolutePath());
       loader = new URLClassLoader(new URL[]
               {
                 u
               });
       JarFile jar = new JarFile(f[i].getAbsolutePath());
       enumeration = jar.entries();
 
       while (enumeration.hasMoreElements())
       {
 
         String tmp = enumeration.nextElement().toString();
         if (tmp.length() > 6 && tmp.substring(tmp.length() - 6).compareTo(".class") == 0)
         {
           tmp = tmp.substring(0, tmp.length() - 6);
           tmp = tmp.replaceAll("/", ".");
           plugins.add(Class.forName(tmp, true, loader));
         }
       }
       ServerSample.updatePlugins();
     }
   }
 
   public void addPlugins(String jarFile) throws Exception
   {
    File file = new File(jarFile);
     URL u = new URL("file://" + file.getAbsolutePath());
     URLClassLoader loader = new URLClassLoader(new URL[]
             {
               u
             });
     JarFile jar = new JarFile(file.getAbsolutePath());
     Enumeration enumeration = jar.entries();
     while (enumeration.hasMoreElements())
     {
       String tmp = enumeration.nextElement().toString();
       if (tmp.length() > 6 && tmp.substring(tmp.length() - 6).compareTo(".class") == 0)
       {
         tmp = tmp.substring(0, tmp.length() - 6);
         tmp = tmp.replaceAll("/", ".");
         plugins.add(Class.forName(tmp, true, loader));
       }
     }
     ServerSample.updatePlugins();
   }
 
   public ArrayList<Class<?>> getPlugins()
   {
     return plugins;
   }
 
   public JarFinder getFinder()
   {
     return finder;
   }
 
   public void setFinder(JarFinder finder)
   {
     this.finder = finder;
   }
 }
