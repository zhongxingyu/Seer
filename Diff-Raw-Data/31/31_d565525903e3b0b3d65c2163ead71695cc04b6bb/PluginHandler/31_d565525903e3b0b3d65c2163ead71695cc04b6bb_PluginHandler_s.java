 /*******************************************************************************
  * Copyright (c) 2012 MCForge.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package net.mcforge.API.plugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import net.mcforge.API.ClassicExtension;
 import net.mcforge.API.ManualLoad;
 import net.mcforge.server.Server;
 import net.mcforge.system.updater.Updatable;
 
 public class PluginHandler {
     private ArrayList<Plugin> plugins = new ArrayList<Plugin>();
     
     private ArrayList<ClassicExtension> ext = new ArrayList<ClassicExtension>();
     
     private final ClassLoader loader = URLClassLoader.newInstance(new URL[] {}, getClass().getClassLoader());
     
     /**
      * Unload a plugin from memory.
      * @param p
      *         The plugin to unload
      */
     public void unload(Plugin p) {
         if (plugins.contains(p)) {
             p.onUnload();
             plugins.remove(p);
         }
     }
     
     public ArrayList<ClassicExtension> getExtensions() {
         return ext;
     }
     
     public ClassLoader getClassLoader() {
         return loader;
     }
     
     public void addExtension(ClassicExtension ce) {
         ext.add(ce);
     }
     
     public void addExtension(Object o) {
         Class<?> class_ = o.getClass();
         ClassicExtension ce = null;
         if ((ce = class_.getAnnotation(ClassicExtension.class)) != null)
             addExtension(ce);
     }
 
     public void loadFile(Server server, File arg0) {
         JarFile file = null;
         try {
             file = new JarFile(arg0);
         } catch (IOException e) {
             e.printStackTrace();
         }
         if (file != null) {
             Enumeration<JarEntry> entries = file.entries();
             addPath(arg0, server);
             if (entries != null) {
                 while (entries.hasMoreElements()) {
                     JarEntry fileName = entries.nextElement();
                     if (fileName.getName().endsWith(".class")) {
                         try {
                             String fullName = fileName.getName();
                             int lastSlash = fullName.lastIndexOf('/');
                             String path = fullName.substring(0, lastSlash + 1);
                             //System.out.println(fullName.length() + "-" + path.length() + "-" + ".class".length());
                             String name = fullName.substring(path.length(), fullName.length() - ".class".length());
 
                             Class<?> class_ = Class.forName(path.replace('/', '.') + name, true, loader);
                             if (class_.getAnnotation(ManualLoad.class) != null)
                                 continue;
                             if (class_.getAnnotation(ClassicExtension.class) != null)
                                 ext.add(class_.getAnnotation(ClassicExtension.class));
                             if (Modifier.isAbstract(class_.getModifiers()))
                                 continue;
                             if (Plugin.class.isAssignableFrom(class_)) {
                                 Class<? extends Plugin> pluginClass = class_.asSubclass(Plugin.class);
                                 Constructor<? extends Plugin> constructByServer = null;
                                 Exception tmpEx = null;
                                 try {
                                     constructByServer = pluginClass.getConstructor(Server.class);
                                 } catch (Exception ex) {
                                     tmpEx = ex;
                                 }
                                 Constructor<? extends Plugin> constructByServerProperties = null;
                                 try {
                                     constructByServerProperties = pluginClass.getConstructor(Server.class, Properties.class);
                                 } catch (Exception ex) {
                                     if (tmpEx != null) {
                                         tmpEx.printStackTrace();
                                         ex.printStackTrace();
                                     }
                                 }
 
                                 JarEntry propFile = file.getJarEntry(path + name + ".config");
                                 Properties properties = new Properties();
                                 if (propFile != null) {
                                     try {
                                         properties.load(file.getInputStream(propFile));
                                     } catch (Exception ex) {
                                         ex.printStackTrace();
                                     }
                                 }
 
                                 Plugin plugin = null;
                                 if (constructByServerProperties != null) {
                                     try {
                                         plugin = constructByServerProperties.newInstance(server, properties);
                                     } catch (Exception ex) {
                                         ex.printStackTrace();
                                     }
                                 }
                                 if (constructByServer != null && plugin == null) {
                                     try {
                                         plugin = constructByServer.newInstance(server);
                                     } catch (Exception ex) {
                                         ex.printStackTrace();
                                     }
                                     if(plugin != null) plugin.setProperties(properties);
                                 }
                                 if (plugin == null) {
                                     System.out.println("Plugin could not be load: " + name);
                                     continue;
                                 }
                                 loadPlugin(plugin, server);
                                 if (plugin instanceof Updatable)
                                     server.getUpdateService().getUpdateManager().add((Updatable)plugin);
                                 plugin.filename = arg0.getName();
                                 plugin.filepath = arg0.getAbsolutePath();
                             } else {
                                 if (!Command.class.isAssignableFrom(class_)) {
                                     continue;
                                 }
                                 try {
                                     Class<? extends Command> commandClass = class_.asSubclass(Command.class);
                                     Constructor<? extends Command> construct = commandClass.getConstructor();
                                     Command c = construct.newInstance();
                                     server.getCommandHandler().addCommand(c);
                                     if (c instanceof Updatable)
                                         server.getUpdateService().getUpdateManager().add((Updatable)c);
                                     CommandLoadEvent cle = new CommandLoadEvent(c, server);
                                     server.getEventSystem().callEvent(cle);
                                 } catch (Exception ex) {
                                     ex.printStackTrace();
                                 }
                             }
 
                         } catch (ClassNotFoundException ex) {
                             ex.printStackTrace();
                         } 
                     }
                 }
                 try {
                     file.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
     
     public void loadPlugin(Plugin plugin, Server server) {
         plugins.add(plugin);
         plugin.onLoad(new String[]{"-normal"}); //Load called after added so plugins can disable/unload in the load method.
         PluginLoadEvent ple = new PluginLoadEvent(plugin, server);
         server.getEventSystem().callEvent(ple);
     }
 
     public void loadplugins(Server server) {
         File pluginFolder = new File("plugins/");
         if (!pluginFolder.exists()) {
             pluginFolder.mkdir();
             return;
         }
         File[] pluginFiles = pluginFolder.listFiles();
         for (int i = 0; i < pluginFiles.length; i++) {
             if (pluginFiles[i].isFile() && pluginFiles[i].getName().endsWith(".jar")) {
                 loadFile(server, pluginFiles[i]);
             }
         }
     }
     
     @SuppressWarnings("deprecation")
     private void addPath(File f, Server server) {
         try {
             URL u = f.toURL();
             Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] {URL.class });
             method.setAccessible(true);
             method.invoke(loader, u);
         } catch (MalformedURLException e) {
             server.Log(e.toString());
             e.printStackTrace();
         } catch (NoSuchMethodException e) {
             server.Log(e.toString());
             e.printStackTrace();
         } catch (SecurityException e) {
             server.Log(e.toString());
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             server.Log(e.toString());
             e.printStackTrace();
         } catch (IllegalArgumentException e) {
             server.Log(e.toString());
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             server.Log(e.toString());
             e.printStackTrace();
         }
     }
 }
 
