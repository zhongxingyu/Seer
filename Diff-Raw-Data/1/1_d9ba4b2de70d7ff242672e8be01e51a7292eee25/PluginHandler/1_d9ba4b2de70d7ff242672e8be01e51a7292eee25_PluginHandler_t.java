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
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import net.mcforge.API.ClassicExtension;
 import net.mcforge.API.Dependency;
 import net.mcforge.API.ManualLoad;
 import net.mcforge.server.Server;
 import net.mcforge.system.Serializer;
 import net.mcforge.system.updater.Updatable;
 
 public class PluginHandler {
     private ArrayList<Plugin> plugins = new ArrayList<Plugin>();
     
     private ArrayList<ClassicExtension> ext = new ArrayList<ClassicExtension>();
     
     private ArrayList<URL> urls = new ArrayList<URL>();
     
     private ClassLoader loader = URLClassLoader.newInstance(new URL[] {}, getClass().getClassLoader());
     
     private Server server;
     
     public PluginHandler(Server server) { this.server = server; }
     
     /**
      * Unload a plugin from memory.
      * @param p
      *         The plugin to unload
      */
     public void unload(Plugin p) {
         if (plugins.contains(p)) {
             p.onUnload();
             plugins.remove(p);
             if (p instanceof Updatable) {
                 try {
                     server.getUpdateService().getUpdateManager().remove((Updatable)p);
                 } catch (IllegalAccessException e) {
                     server.logError(e);
                 }
             }
         }
     }
     
     /**
      * Gets the currently loaded MCForge plugins
      * 
      * @return An ArrayList containing all the MCForge plugins currently loaded
      */
     public ArrayList<Plugin> getLoadedPlugins() {
     	return plugins;
     }
     
     /**
      * Look for a loaded plugin using part of/the full name of the plugin
      * @param name
      *            part of or the full name of the plugin
      * @return
      *        If more than 1 plugin is found, then null is returned.
      *        If no plugin is found, then null is returned.
      *        If a plugin is found, then the loaded plugin is returned.
      */
     public Plugin findPlugin(String name) {
         Plugin toreturn = null;
         for (Plugin p : getLoadedPlugins()) {
             if (p.getName().equals(name))
                 return p;
             if (p.getName().indexOf(name) != -1 && toreturn == null)
                 toreturn = p;
             else if (p.getName().indexOf(name) != -1 && toreturn != null)
                 return null;
         }
         return toreturn;
     }
     
     /**
      * Gets the currently loaded Classic extensions
      * 
      * @return An ArrayList containing all the Classic extensions currently loaded
      */
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
     
     public void loadFile(File file) {
         loadFile(file, false);
     }
 
     public Map<Plugin, String> loadFile(File arg0, boolean update) {
         Map<Plugin, String> required = new HashMap<Plugin, String>();
         JarFile file = null;
         try {
             file = new JarFile(arg0);
         } catch (IOException e) {
             server.logError(e);
         }
         if (file != null) {
             Enumeration<JarEntry> entries = file.entries();
             if (update)
                 removePath(arg0);
             addPath(arg0);
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
                                 plugin.filename = arg0.getName();
                                 plugin.filepath = arg0.getAbsolutePath();
                                 boolean canload = true;
                                 if (class_.getAnnotation(Dependency.class) != null) {
                                     Dependency requirements = class_.getAnnotation(Dependency.class);
                                     String[] plugins = requirements.plugins();
                                     for (String s : plugins) {
                                         if (findPlugin(s) == null) {
                                             canload = false;
                                             required.put(plugin, s);
                                         }
                                     }
                                 }
                                 if (!canload)
                                     continue;
                                 loadPlugin(plugin);
                                 
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
                                     
                                 } catch (Exception ex) {
                                     ex.printStackTrace();
                                 }
                             }
 
                         } catch (ClassNotFoundException ex) {
                             ex.printStackTrace();
                         } catch (Exception e) {
                             e.printStackTrace();
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
         return required;
     }
     
     public void loadPlugin(Plugin plugin) {
         plugins.add(plugin);
         plugin.onLoad(new String[]{"-normal"}); //Load called after added so plugins can disable/unload in the load method.
         PluginLoadEvent ple = new PluginLoadEvent(plugin, server);
         server.getEventSystem().callEvent(ple);
         server.Log(plugin.getName() + " v" + plugin.getVersion() + " was loaded.");
         if (plugin instanceof Updatable)
             server.getUpdateService().getUpdateManager().add((Updatable)plugin);
     }
 
    
     public void loadplugins() {
         Map<Plugin, String> require = new HashMap<Plugin, String>();
         File pluginFolder = new File("plugins/");
         if (!pluginFolder.exists()) {
             pluginFolder.mkdir();
             return;
         }
         File[] pluginFiles = pluginFolder.listFiles();
         for (int i = 0; i < pluginFiles.length; i++) {
             if (pluginFiles[i].isFile() && pluginFiles[i].getName().endsWith(".jar")) {
                 try {
                     Map<Plugin, String> temp = loadFile(pluginFiles[i], false);
                     for (Entry<Plugin, String> p : temp.entrySet()) {
                         require.put(p.getKey(), p.getValue());
                     }
                     for (Entry<Plugin, String> p : require.entrySet()) {
                         if (findPlugin(p.getValue()) != null) {
                             loadPlugin(p.getKey());
                             require.remove(p.getKey());
                         }
                     }
                 } catch (Exception e) {
                     server.logError(e);
                 } catch (NoClassDefFoundError e) {  
                     server.logError(e);
                     server.Log("The plugin " + pluginFiles[i] + " failed to load! Try contacting the author of the plugin for a fix.");
                 }  
             }
         }
         if (require.size() > 0) {
             server.Log("The following plugins could not be loaded due to dependency issues:");
             for (Entry<Plugin, String> p : require.entrySet()) {
                 server.Log(p.getKey().getFileName() + " requires " + p.getValue());
             }
             require.clear();
         }
     }
     
     @SuppressWarnings({ "deprecation" })
     private void removePath(File f) {
         try {
             urls.remove(f.toURL());
         } catch (MalformedURLException e) {
             e.printStackTrace();
         }
         loader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());
         Serializer.getKryo().setClassLoader(loader);
     }
     
     @SuppressWarnings("deprecation")
     private void addPath(File f) {
         try {
             URL u = f.toURL();
             Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] {URL.class });
             method.setAccessible(true);
             method.invoke(loader, u);
             urls.add(u);
         } catch (MalformedURLException e) {
             server.logError(e);
         } catch (NoSuchMethodException e) {
             server.logError(e);
         } catch (SecurityException e) {
             server.logError(e);
         } catch (IllegalAccessException e) {
             server.logError(e);
         } catch (IllegalArgumentException e) {
             server.logError(e);
         } catch (InvocationTargetException e) {
             server.logError(e);
         }
     }
 }
 
