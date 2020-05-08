 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg. All rights reserved.
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
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.apache.commons.lang.ClassUtils;
 import org.rapidcontext.core.data.Dict;
 import org.rapidcontext.core.storage.FileStorage;
 import org.rapidcontext.core.storage.MemoryStorage;
 import org.rapidcontext.core.storage.Path;
 import org.rapidcontext.core.storage.Storage;
 import org.rapidcontext.core.storage.StorageException;
 import org.rapidcontext.core.storage.RootStorage;
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
                     createStorage(baseDir, pluginId);
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
      * Creates and mounts a plug-in file storage. This is the first
      * step when installing a plug-in, allowing access to the plug-in
      * files without overlaying then on the root index.
      *
      * @param baseDir        the base plug-in directory
      * @param pluginId       the unique plug-in id
      *
      * @return the plug-in file storage created
      *
      * @throws PluginException if the plug-in had already been mounted
      */
     private Storage createStorage(File baseDir, String pluginId) throws PluginException {
         File     dir = new File(baseDir, pluginId);
         Storage  fs = new FileStorage(dir, false);
 
         try {
             storage.mount(fs, storagePath(pluginId), false, false, 0);
         } catch (StorageException e) {
             String msg = "failed to create " + pluginId + " plug-in storage";
             LOG.log(Level.SEVERE, msg, e);
             throw new PluginException(msg + ": " + e.getMessage());
         }
         return fs;
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
         ZipFile      zip = null;
         ZipEntry     entry;
         InputStream  is;
         Properties   props;
         String       pluginId;
         File         dir;
         String       msg;
 
         try {
             zip = new ZipFile(file);
             entry = zip.getEntry("plugin.properties");
             if (entry == null) {
                 msg = "missing plugin.properties inside zip file " +
                       file.getName();
                 LOG.warning(msg);
                 throw new PluginException(msg);
             }
             is = zip.getInputStream(entry);
             props = new Properties();
             try {
                 props.load(is);
             } finally {
                 try {
                     is.close();
                 } catch (Exception ignore) {
                     // Ignore exception on closing file
                 }
             }
             pluginId = props.getProperty(Plugin.KEY_ID);
             if (pluginId == null || pluginId.trim().length() < 0) {
                 msg = "missing plug-in identifier in plugin.properties";
                 throw new PluginException(msg);
             }
             if (isAvailable(pluginId)) {
                 unload(pluginId);
                 destroyStorage(pluginId);
             }
             dir = new File(pluginDir, pluginId);
             if (dir.exists()) {
                 // TODO: perhaps backup the old directory instead?
                 FileUtil.delete(dir);
             }
             FileUtil.unpackZip(zip, dir);
         } catch (IOException e) {
             msg = "IO error while reading zip file " + file.getName() + ": " +
                   e.getMessage();
             LOG.warning(msg);
             throw new PluginException(msg);
         } finally {
             if (zip != null) {
                 try {
                     zip.close();
                 } catch (IOException ignore) {
                     // Do nothing
                 }
             }
         }
         createStorage(pluginDir, pluginId);
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
         File         dir;
         String       className;
         Class        cls;
         Constructor  constr;
         String       msg;
 
         // Load plug-in configuration
         if (SYSTEM_PLUGIN.equals(pluginId) || LOCAL_PLUGIN.equals(pluginId)) {
             msg = "cannot force loading of system or local plug-ins";
             throw new PluginException(msg);
         }
         try {
             dict = (Dict) storage.load(configPath(pluginId));
             if (dict == null) {
                 throw new StorageException("file not found");
             }
         } catch (StorageException e) {
             msg = "couldn't load " + pluginId + " plugin config file";
             LOG.log(Level.WARNING, msg, e);
             throw new PluginException(msg + ": " + e.getMessage());
         }
 
         // Add to root overlay
         loadOverlay(pluginId);
 
         // Create plug-in instance
         dir = new File(this.pluginDir, pluginId);
         if (!dir.isDirectory()) {
             dir = new File(this.builtinDir, pluginId);
         }
         classLoader.addPluginJars(dir);
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
         Object[]  objs;
         String    pluginId;
 
         objs = storage.loadAll(PATH_PLUGIN);
         for (int i = 0; i < objs.length; i++) {
             if (objs[i] instanceof Plugin) {
                 pluginId = ((Plugin) objs[i]).id();
                 try {
                     unload(pluginId);
                 } catch (PluginException e) {
                     LOG.warning("failed to unload " + pluginId + " plugin");
                 }
             }
         }
         classLoader = new PluginClassLoader();
     }
 }
