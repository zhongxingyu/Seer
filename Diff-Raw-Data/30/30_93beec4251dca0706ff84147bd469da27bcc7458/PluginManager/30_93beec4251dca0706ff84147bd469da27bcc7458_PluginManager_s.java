 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2011 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.app.plugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.ClassUtils;
 import org.rapidcontext.core.data.Binary;
 import org.rapidcontext.core.data.Dict;
 import org.rapidcontext.core.storage.FileStorage;
 import org.rapidcontext.core.storage.MemoryStorage;
 import org.rapidcontext.core.storage.Metadata;
 import org.rapidcontext.core.storage.Path;
 import org.rapidcontext.core.storage.Storage;
 import org.rapidcontext.core.storage.StorageException;
 import org.rapidcontext.core.storage.RootStorage;
 import org.rapidcontext.core.storage.ZipFileStorage;
 import org.rapidcontext.core.type.Type;
 import org.rapidcontext.util.FileUtil;
 
 /**
  * A plug-in manager. This singleton class contains the utility
  * functions for managing the plug-in loading and unloading.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class PluginManager {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(PluginManager.class.getName());
 
     /**
      * The storage path to the in-memory storage.
      */
     public static final Path PATH_STORAGE_MEMORY = new Path("/storage/memory/");
 
     /**
      * The storage path to the mounted plug-in file storages.
      */
     public static final Path PATH_STORAGE_PLUGIN = new Path("/storage/plugin/");
 
     /**
      * The storage path to the loaded plug-in objects.
      */
     public static final Path PATH_PLUGIN = new Path("/plugin/");
 
     /**
      * The storage path to the JAR library files.
      */
     private static final Path PATH_LIB = new Path("/lib/");
 
     /**
      * The identifier of the system plug-in.
      */
     public static final String SYSTEM_PLUGIN = "system";
 
     /**
      * The identifier of the local plug-in.
      */
     public static final String LOCAL_PLUGIN = "local";
 
     /**
      * The built-in plug-in directory. This is the base directory from
      * which built-in plug-ins are loaded.
      */
     public File builtinDir = null;
 
     /**
      * The plug-in directory. This is the base directory from which
      * plug-ins are loaded.
      */
     public File pluginDir = null;
 
     /**
      * The storage to use when loading and unloading plug-ins.
      */
     public RootStorage storage;
 
     /**
      * The plug-in class loader.
      */
     public PluginClassLoader classLoader = new PluginClassLoader();
 
     /**
      * A list of all temporary files created. When destroying all
      * plug-ins, all these files are deleted.
      */
     private ArrayList tempFiles = new ArrayList();
 
     /**
      * Returns the plug-in storage path for a specified plug-in id.
      *
      * @param pluginId       the unique plug-in id
      *
      * @return the plug-in storage path
      */
     public static Path storagePath(String pluginId) {
         return PATH_STORAGE_PLUGIN.child(pluginId, true);
     }
 
     /**
      * Returns the plug-in configuration object path for a specified
      * plug-in id.
      *
      * @param pluginId       the unique plug-in id
      *
      * @return the plug-in configuration storage path
      */
     public static Path configPath(String pluginId) {
         return storagePath(pluginId).child("plugin", false);
     }
 
     /**
      * Returns the plug-in instance path for a specified plug-in id.
      *
      * @param pluginId       the unique plug-in id
      *
      * @return the plug-in instance path
      */
     public static Path pluginPath(String pluginId) {
         Path rootRelative = PATH_PLUGIN.child(pluginId, false);
         return PATH_STORAGE_MEMORY.descendant(rootRelative);
     }
 
     /**
      * Creates a new plug-in storage.
      *
      * @param builtinDir     the built-in plug-in directory
      * @param pluginDir      the base plug-in directory
      * @param storage        the storage to use for plug-ins
      */
     public PluginManager(File builtinDir, File pluginDir, RootStorage storage) {
         this.builtinDir = builtinDir;
         this.pluginDir = pluginDir;
         this.storage = storage;
         try {
             MemoryStorage memory = new MemoryStorage(true);
             storage.mount(memory, PATH_STORAGE_MEMORY, true, true, 50);
         } catch (StorageException e) {
             LOG.log(Level.SEVERE, "failed to create memory storage", e);
         }
         initStorages(pluginDir);
         initStorages(builtinDir);
         try {
             loadOverlay(SYSTEM_PLUGIN);
         } catch (PluginException ignore) {
             // Error already logged, ignored here
         }
         try {
             loadOverlay(LOCAL_PLUGIN);
         } catch (PluginException ignore) {
             // Error already logged, ignored here
         }
     }
 
     /**
      * Initializes the plug-in storages found in a base plug-in
      * directory. Any errors will be logged and ignored. If a storage
      * has already been mounted (from another base directory), it
      * will be omitted.
      *
      * @param baseDir        the base plug-in directory
      */
     private void initStorages(File baseDir) {
         File[] files = baseDir.listFiles();
         for (int i = 0; i < files.length; i++) {
            String pluginId = files[i].getName();
            if (files[i].isDirectory() && !isAvailable(pluginId)) {
                 try {
                    createStorage(pluginId);
                 } catch (PluginException ignore) {
                     // Error already logged, ignored here
                 }
             }
         }
     }
 
     /**
      * Checks if the specified plug-in is currently available, i.e.
      * if it has been mounted to the plug-in storage.
      *
      * @param pluginId       the unique plug-in id
      *
      * @return true if the plug-in was available, or
      *         false otherwise
      */
     public boolean isAvailable(String pluginId) {
         return storage.lookup(storagePath(pluginId)) != null;
     }
 
     /**
      * Checks if the specified plug-in is currently loaded.
      *
      * @param pluginId       the unique plug-in id
      *
      * @return true if the plug-in was loaded, or
      *         false otherwise
      */
     public boolean isLoaded(String pluginId) {
         return storage.lookup(pluginPath(pluginId)) != null ||
                SYSTEM_PLUGIN.equals(pluginId) ||
                LOCAL_PLUGIN.equals(pluginId);
     }
 
     /**
      * Returns the specified plug-in configuration dictionary.
      *
      * @param pluginId       the unique plug-in id
      *
      * @return the plug-in configuration dictionary, or
      *         null if not found
      */
     public Dict config(String pluginId) {
         return (Dict) storage.load(configPath(pluginId));
     }
 
     /**
      * Finds the best matching plug-in storage file or directory for
      * a plug-in identifier.
      *
      * @param pluginId       the unique plug-in id
      *
      * @return the readable plug-in file or directory, or
      *         null if not found
      */
     private File storageFile(String pluginId) {
         File[] files = {
             new File(pluginDir, pluginId + ".zip"),
             new File(pluginDir, pluginId),
             new File(builtinDir, pluginId + ".zip"),
             new File(builtinDir, pluginId)
         };
         for (int i = 0; i < files.length; i++) {
             if (files[i].canRead()) {
                 return files[i];
             }
         }
         return null;
     }
 
     /**
      * Creates and mounts a plug-in file storage. This is the first
      * step when installing a plug-in, allowing access to the plug-in
      * files without overlaying then on the root index.
      *
      * @param pluginId       the unique plug-in id
      *
      * @return the plug-in file storage created
      *
      * @throws PluginException if the plug-in had already been mounted
      */
     private Storage createStorage(String pluginId) throws PluginException {
         File     file = storageFile(pluginId);
         Storage  ps;
         String   msg;
 
         if (file == null) {
             msg = "couldn't find " + pluginId + " plug-in storage";
             LOG.log(Level.SEVERE, msg);
             throw new PluginException(msg);
         }
         try {
             if (file.isDirectory()) {
                 ps = new FileStorage(file, false);
             } else {
                 ps = new ZipFileStorage(file);
             }
             storage.mount(ps, storagePath(pluginId), false, false, 0);
         } catch (Exception e) {
             msg = "failed to create " + pluginId + " plug-in storage";
             LOG.log(Level.SEVERE, msg, e);
             throw new PluginException(msg + ": " + e.getMessage());
         }
         return ps;
     }
 
     /**
      * Destroys a plug-in file storage. This is only needed when a
      * new plug-in will be installed over a previous one, otherwise
      * the unload() method is sufficient.
      *
      * @param pluginId       the unique plug-in id
      *
      * @throws PluginException if the plug-in hadn't been mounted
      */
     private void destroyStorage(String pluginId) throws PluginException {
         try {
             storage.unmount(storagePath(pluginId));
         } catch (StorageException e) {
             String msg = "failed to remove " + pluginId + " plug-in storage";
             LOG.log(Level.SEVERE, msg, e);
             throw new PluginException(msg + ": " + e.getMessage());
         }
     }
 
     /**
      * Installs a plug-in from the specified file. If an existing
      * plug-in with the same id already exists, it will be replaced
      * without warning. Note that the new plug-in will NOT be loaded.
      *
      * @param file           the plug-in ZIP file
      *
      * @return the unique plug-in id
      *
      * @throws PluginException if the plug-in couldn't be installed
      *             correctly
      */
     public String install(File file) throws PluginException {
         ZipFileStorage  ps = null;
         Dict            dict;
         String          pluginId;
         File            dst;
         String          msg;
 
         try {
             ps = new ZipFileStorage(file);
             dict = (Dict) ps.load(new Path("/plugin"));
             if (dict == null) {
                 throw new PluginException("missing plugin.properties");
             }
             pluginId = dict.getString(Plugin.KEY_ID, null);
             if (pluginId == null || pluginId.trim().length() < 0) {
                 msg = "missing plug-in identifier in plugin.properties";
                 throw new PluginException(msg);
             }
             if (isAvailable(pluginId)) {
                 unload(pluginId);
                 destroyStorage(pluginId);
             }
             dst = new File(pluginDir, pluginId);
             if (dst.exists()) {
                 FileUtil.delete(dst);
             }
             dst = new File(pluginDir, pluginId + ".zip");
             if (dst.exists()) {
                 FileUtil.delete(dst);
             }
             FileUtil.copy(file, dst);
         } catch (Exception e) {
             msg = "invalid plug-in file " + file.getName();
             LOG.log(Level.WARNING, msg, e);
             throw new PluginException(msg + ": " + e.getMessage());
         } finally {
             if (ps != null) {
                 try {
                     ps.destroy();
                 } catch (Exception ignore) {
                     // Do nothing
                 }
             }
         }
         createStorage(pluginId);
         return pluginId;
     }
 
     /**
      * Loads a plug-in. The plug-in file storage will be added to the
      * root overlay and the plug-in configuration file will be used
      * to initialize the plug-in Java class.
      *
      * @param pluginId       the unique plug-in id
      *
      * @throws PluginException if the plug-in loading failed
      */
     public void load(String pluginId) throws PluginException {
         Plugin       plugin;
         Dict         dict;
         String       className;
         Class        cls;
         Constructor  constr;
         String       msg;
 
         // Load plug-in configuration
         if (SYSTEM_PLUGIN.equals(pluginId) || LOCAL_PLUGIN.equals(pluginId)) {
             msg = "cannot force loading of system or local plug-ins";
             throw new PluginException(msg);
         }
         dict = config(pluginId);
         if (dict == null) {
             msg = "couldn't load " + pluginId + " plugin config file";
             LOG.log(Level.WARNING, msg);
             throw new PluginException(msg);
         }
 
         // Create overlay & load JAR files
         loadOverlay(pluginId);
         loadJarFiles(pluginId);
 
         // Create plug-in instance
         className = dict.getString(Plugin.KEY_CLASSNAME, null);
         if (className == null || className.trim().length() <= 0) {
             plugin = new Plugin(dict);
         } else {
             try {
                 cls = classLoader.loadClass(className);
             } catch (Throwable e) {
                 msg = "couldn't load " + pluginId + " plugin class " +
                       className;
                 LOG.log(Level.WARNING, msg, e);
                 throw new PluginException(msg + ": " + e.getMessage());
             }
             if (!ClassUtils.getAllSuperclasses(cls).contains(Plugin.class)) {
                 msg = pluginId + " plugin class " + className +
                       " isn't a subclass of the Plugin class";
                 LOG.warning(msg);
                 throw new PluginException(msg);
             }
             try {
                 constr = cls.getConstructor(new Class[] { Dict.class});
             } catch (Throwable e) {
                 msg = pluginId + " plugin class " + className +
                       " missing constructor with valid signature";
                 LOG.log(Level.WARNING, msg, e);
                 throw new PluginException(msg + ": " + e.getMessage());
             }
             try {
                 plugin = (Plugin) constr.newInstance(new Object[] { dict });
             } catch (Throwable e) {
                 msg = "couldn't create " + pluginId + " plugin instance for " +
                       className;
                 LOG.log(Level.WARNING, msg, e);
                 throw new PluginException(msg + ": " + e.getMessage());
             }
         }
 
         // Initialize plug-in instance
         try {
             storage.loadAll(storagePath(pluginId).descendant(Type.PATH));
             // TODO: plug-in initialization should be handled by storage
             plugin.init();
             storage.store(pluginPath(pluginId), plugin);
         } catch (Throwable e) {
             msg = "plugin class " + className + " threw exception on init";
             LOG.log(Level.WARNING, msg, e);
             throw new PluginException(msg + ": " + e.getMessage());
         }
     }
 
     /**
      * Loads a plug-in storage to the root overlay. The plug-in
      * storage must already have been mounted.
      *
      * @param pluginId       the unique plug-in id
      *
      * @throws PluginException if the plug-in storage couldn't be
      *             overlaid on the root
      */
     private void loadOverlay(String pluginId) throws PluginException {
         boolean  readWrite = LOCAL_PLUGIN.equals(pluginId);
         int      prio = SYSTEM_PLUGIN.equals(pluginId) ? 0 : 100;
         String   msg;
 
         try {
             storage.remount(storagePath(pluginId), readWrite, true, prio);
         } catch (StorageException e) {
             msg = "failed to overlay " + pluginId + " plug-in storage";
             LOG.log(Level.SEVERE, msg, e);
             throw new PluginException(msg + ": " + e.getMessage());
         }
     }
 
     /**
      * Unloads a plug-in. All plug-in classes will be destroyed and
      * the plug-in file storage will be hidden from the root overlay.
      *
      * @param pluginId       the unique plug-in id
      *
      * @throws PluginException if the plug-in unloading failed
      */
     public void unload(String pluginId) throws PluginException {
         Path    path = pluginPath(pluginId);
         String  msg;
 
         if (SYSTEM_PLUGIN.equals(pluginId) || LOCAL_PLUGIN.equals(pluginId)) {
             msg = "cannot unload system or local plug-ins";
             throw new PluginException(msg);
         }
         try {
             storage.remove(path);
         } catch (StorageException e) {
             msg = "failed destroy call on " + pluginId + " plugin";
             LOG.log(Level.SEVERE, msg, e);
         }
         try {
             storage.remount(storagePath(pluginId), false, false, 0);
         } catch (StorageException e) {
             msg = "plugin " + pluginId + " storage remount failed";
             LOG.log(Level.WARNING, msg, e);
             throw new PluginException(msg + ": " + e.getMessage());
         }
     }
 
     /**
      * Unloads all plug-ins. All plug-in classes will be destroyed
      * and the plug-in file storages will be hidden from the root
      * overlay. Note that the built-in plug-ins will be unaffected by
      * this.
      */
     public void unloadAll() {
         Object[] objs = storage.loadAll(PATH_PLUGIN);
         for (int i = 0; i < objs.length; i++) {
             if (objs[i] instanceof Plugin) {
                 String pluginId = ((Plugin) objs[i]).id();
                 try {
                     unload(pluginId);
                 } catch (PluginException e) {
                     LOG.warning("failed to unload " + pluginId + " plugin");
                 }
             }
         }
         storage.flush(null);
         classLoader = new PluginClassLoader();
         while (tempFiles.size() > 0) {
             File file = (File) tempFiles.remove(tempFiles.size() - 1);
             try {
                 file.delete();
             } catch (Exception ignore) {
                 // File will be deleted on exit instead
             }
         }
     }
 
     /**
      * Loads all JAR files found in the specified plug-in library
      * path. All the files found will be copied to a temporary
      * directory before loading in order to avoid file locking and
      * other issues.
      *
      * @param pluginId       the unique plug-in id
      */
     private void loadJarFiles(String pluginId) {
         Metadata[]  meta;
         String      name;
 
         meta = storage.lookupAll(storagePath(pluginId).descendant(PATH_LIB));
         for (int i = 0; i < meta.length; i++) {
             name = meta[i].path().name();
             if (meta[i].isBinary() && name.toLowerCase().endsWith(".jar")) {
                 loadJarFile(meta[i].path());
             }
         }
     }
 
     /**
      * Adds a JAR file to the plug-in class loader. The JAR file will
      * be copied from storage to a temporary directory to avoid file
      * locking and other issues. This method will only log errors on
      * failure and no error will be thrown.
      *
      * @param path           the storage path to the JAR file
      */
     private void loadJarFile(Path path) {
         try {
             LOG.fine("adding JAR to class loader: " + path);
             Binary data = (Binary) storage.load(path);
             File tmpFile = FileUtil.tempFile(path.name());
             tempFiles.add(tmpFile);
             FileUtil.copy(data.openStream(), tmpFile);
             classLoader.addJar(tmpFile);
         } catch (Exception e) {
             LOG.log(Level.SEVERE, "failed to load JAR file: " + path, e);
         }
     }
 
     /**
      * Simple extension of URLClassLoader to be able to add new URL:s
      * after creation.
      */
     public static class PluginClassLoader extends URLClassLoader {
 
         /**
          * Creates a new empty class loader.
          */
         public PluginClassLoader() {
             super(new URL[0]);
         }
 
         /**
          * Adds the specified JAR file to the class loader.
          *
          * @param file       the JAR file to add
          *
          * @throws IOException if the file couldn't be located
          */
         public void addJar(File file) throws IOException {
             addURL(file.toURI().toURL());
         }
     }
 }
