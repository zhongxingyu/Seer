 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2012 Per Cederberg. All rights reserved.
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
 
 package org.rapidcontext.core.storage;
 
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.rapidcontext.core.data.Array;
 import org.rapidcontext.core.data.Dict;
 import org.rapidcontext.core.task.Scheduler;
 import org.rapidcontext.core.task.Task;
 import org.rapidcontext.core.type.Type;
 
 /**
  * The root storage that provides a unified view of other storages.
  * This class provides a number of unique storage services:
  *
  * <ul>
  *   <li><strong>Mounting</strong> -- Sub-storages can be mounted
  *       on a "storage/..." subpath, providing a global namespace
  *       for objects. Storage paths are automatically converted to
  *       local paths for all storage operations.
  *   <li><strong>Unification</strong> -- Mounted storages can also
  *       be overlaid or unified with the root path, providing a
  *       storage view where objects from all storages are mixed. The
  *       mount order defines which object names take priority, in
  *       case several objects have the same paths.
  *   <li><strong>Object Initialization</strong> -- Dictionary objects
  *       will be inspected upon retrieval from a mounted and unified
  *       path (i.e. not when retrieved directly from the storage).
  *       If a matching type handler or class can be located, the
  *       corresponding object will be created, initialized and cached
  *       for future references.
  * </ul>
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class RootStorage extends Storage {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(RootStorage.class.getName());
 
     /**
      * The number of seconds between each run of the object cache
      * cleaner job.
      */
     private static final int PASSIVATE_INTERVAL_SECS = 30;
 
     /**
      * The meta-data storage for mount points and parent indices.
      * The mounted storages will be added to this storage under
      * their corresponding mount path (appended to form an object
      * path instead of an index path).
      */
     private MemoryStorage metadata = new MemoryStorage(true);
 
     /**
      * The sorted array of mounted storages. This array is sorted
      * every time a mount point is added or modified.
      */
     private Array mountedStorages = new Array();
 
     /**
      * The map of cache memory storages. For each mounted and
      * overlaid storage, a corresponding cache storage is created to
      * contain any StorableObject instances.
      */
     private HashMap cacheStorages = new HashMap();
 
     /**
      * Creates a new root storage.
      *
      * @param readWrite      the read write flag
      */
     public RootStorage(boolean readWrite) {
         super("root", readWrite);
         dict.set("storages", mountedStorages);
         try {
             metadata.store(PATH_STORAGEINFO, dict);
         } catch (StorageException e) {
             LOG.severe("error while initializing virtual storage: " +
                        e.getMessage());
         }
         Task cacheCleaner = new Task("storage cache cleaner") {
             public void execute() {
                 cacheClean(false);
             }
         };
         long delay = PASSIVATE_INTERVAL_SECS * 1000L;
         Scheduler.schedule(cacheCleaner, delay, delay);
     }
 
     /**
      * Returns the storage at a specific storage location. If the
      * exact match flag is set, the path must exactly match the mount
      * point of a storage. If exact matching is not required, the
      * parent storage for the path will be returned.
      *
      * @param path           the storage location
      * @param exact          the exact match flag
      *
      * @return the storage found, or
      *         null if not found
      */
     private Storage getMountedStorage(Path path, boolean exact) {
         if (path == null) {
             return null;
         } else if (exact) {
             return (Storage) metadata.load(path.child("storage", false));
         } else {
             for (int i = 0; i < mountedStorages.size(); i++) {
                 Storage storage = (Storage) mountedStorages.get(i);
                 if (path.startsWith(storage.path())) {
                     return storage;
                 }
             }
             return null;
         }
     }
 
     /**
      * Sets or removes a storage at a specific storage location.
      *
      * @param path           the storage mount path
      * @param storage        the storage to add, or null to remove
      *
      * @throws StorageException if the data couldn't be written
      */
     private void setMountedStorage(Path path, Storage storage)
     throws StorageException {
 
         path = path.child("storage", false);
         if (storage == null) {
             metadata.remove(path);
         } else {
             metadata.store(path, storage);
         }
     }
 
     /**
      * Creates or removes a cache memory storage for the specified
      * path.
      *
      * @param path           the storage mount path
      * @param exist          the create or remove flag
      */
     private void updateStorageCache(Path path, boolean exist) {
         MemoryStorage  cache;
 
         if (exist && !cacheStorages.containsKey(path)) {
             cache = new MemoryStorage(true);
             cache.setMountInfo(path, true, true, 1);
             cacheStorages.put(path, cache);
         } else if (!exist && cacheStorages.containsKey(path)) {
             cache = (MemoryStorage) cacheStorages.get(path);
             cacheRemove(cache, Path.ROOT, false, true);
             cache.destroy();
             cacheStorages.remove(path);
         }
     }
 
     /**
      * Mounts a storage to a unique path. The path may not collide
      * with a previously mounted storage, such that it would hide or
      * be hidden by the other storage. Overlapping parent indices
      * will be merged automatically. In addition to adding the
      * storage to the specified path, it's contents may also be
      * overlaid directly on the root path.
      *
      * @param storage        the storage to mount
      * @param path           the mount path
      * @param readWrite      the read write flag
      * @param overlay        the root overlay flag
      * @param prio           the root overlay search priority (higher numbers
      *                       are searched before lower numbers)
      *
      * @throws StorageException if the storage couldn't be mounted
      */
     public void mount(Storage storage,
                       Path path,
                       boolean readWrite,
                       boolean overlay,
                       int prio)
     throws StorageException {
 
         String  msg;
 
         if (!path.isIndex()) {
             msg = "cannot mount storage to a non-index path: " + path;
             LOG.warning(msg);
             throw new StorageException(msg);
         } else if (metadata.lookup(path) != null) {
             msg = "storage mount path conflicts with another mount: " + path;
             LOG.warning(msg);
             throw new StorageException(msg);
         }
         storage.setMountInfo(path, readWrite, overlay, prio);
         setMountedStorage(path, storage);
         mountedStorages.add(storage);
         mountedStorages.sort();
         updateStorageCache(path, overlay);
     }
 
     /**
      * Remounts a storage for a unique path. The path or the storage
      * are not modified, but only the mounting options.
      *
      * @param path           the mount path
      * @param readWrite      the read write flag
      * @param overlay        the root overlay flag
      * @param prio           the root overlay search priority (higher numbers
      *                       are searched before lower numbers)
      *
      * @throws StorageException if the storage couldn't be remounted
      */
     public void remount(Path path, boolean readWrite, boolean overlay, int prio)
     throws StorageException {
 
         Storage  storage = getMountedStorage(path, true);
         String   msg;
 
         if (storage == null) {
             msg = "no mounted storage found matching path: " + path;
             LOG.warning(msg);
             throw new StorageException(msg);
         }
         storage.setMountInfo(storage.path(), readWrite, overlay, prio);
         mountedStorages.sort();
         updateStorageCache(path, overlay);
     }
 
     /**
      * Unmounts a storage from the specified path. The path must have
      * previously been used to mount a storage, which will also be
      * destroyed by this operation.
      *
      * @param path           the mount path
      *
      * @throws StorageException if the storage couldn't be unmounted
      */
     public void unmount(Path path) throws StorageException {
         Storage  storage = getMountedStorage(path, true);
         String   msg;
 
         if (storage == null) {
             msg = "no mounted storage found matching path: " + path;
             LOG.warning(msg);
             throw new StorageException(msg);
         }
         updateStorageCache(path, false);
         mountedStorages.remove(mountedStorages.indexOf(storage));
         // Removal of storage from metadata also cause its destruction
         setMountedStorage(path, null);
     }
 
     /**
      * Unmounts and destroys all mounted storages.
      */
     public void unmountAll() {
         Storage  storage;
         String   msg;
 
         while (mountedStorages.size() > 0) {
             storage = (Storage) mountedStorages.get(mountedStorages.size() - 1);
             try {
                 unmount(storage.path());
             } catch (Exception e) {
                 msg = "failed to unmount storage at " + storage.path();
                 LOG.log(Level.WARNING, msg, e);
             }
         }
     }
 
     /**
      * Searches for an object at the specified location and returns
      * metadata about the object if found. The path may locate either
      * an index or a specific object.
      *
      * @param path           the storage location
      *
      * @return the metadata for the object, or
      *         null if not found
      */
     public Metadata lookup(Path path) {
         Storage   storage = getMountedStorage(path, false);
         Metadata  meta = null;
 
         if (storage != null) {
             meta = storage.lookup(storage.localPath(path));
             return (meta == null) ? null : new Metadata(path, meta);
         } else {
             meta = metadata.lookup(path);
             for (int i = 0; i < mountedStorages.size(); i++) {
                 storage = (Storage) mountedStorages.get(i);
                 if (storage.mountOverlay()) {
                     meta = Metadata.merge(meta, lookupObject(storage, path));
                 }
             }
             return meta;
         }
     }
 
     /**
      * Searches for an object in a specified storage. The object will
      * be looked up primarily in the cache and thereafter in the
      * actual storage.
      *
      * @param storage        the storage to search in
      * @param path           the storage location
      *
      * @return the metadata for the object, or
      *         null if not found
      */
     private Metadata lookupObject(Storage storage, Path path) {
         MemoryStorage  cache;
         Metadata       meta = null;
 
         cache = (MemoryStorage) cacheStorages.get(storage.path());
         if (cache != null) {
             meta = cache.lookup(path);
             if (meta != null && meta.isObject()) {
                 return meta;
             }
         }
         return storage.lookup(path);
     }
 
     /**
      * Loads an object from the specified location. The path may
      * locate either an index or a specific object. In case of an
      * index, the data returned is an index dictionary listing of
      * all objects in it.
      *
      * @param path           the storage location
      *
      * @return the data read, or
      *         null if not found
      */
     public Object load(Path path) {
         Storage  storage = getMountedStorage(path, false);
         Object   res;
         Index    idx = null;
 
         if (storage != null) {
             res = storage.load(storage.localPath(path));
             if (res instanceof Index) {
                 idx = (Index) res;
                 return new Index(path, idx.indices(), idx.objects());
             } else {
                 return res;
             }
         } else {
             res = metadata.load(path);
             if (res instanceof Index) {
                 idx = (Index) res;
             } else if (res != null) {
                 return res;
             }
             for (int i = 0; i < mountedStorages.size(); i++) {
                 storage = (Storage) mountedStorages.get(i);
                 if (storage.mountOverlay()) {
                     res = loadObject(storage, path);
                     if (res instanceof Index) {
                         idx = Index.merge(idx, (Index) res);
                     } else if (res != null) {
                         return res;
                     }
                 }
             }
         }
         return idx;
     }
 
     /**
      * Loads an object from the specified storage. The storage cache
      * will be used primarily, if it exists. If an object is found in
      * the storage that can be cached, it will be initialized and
      * cached by this method.
      *
      * @param storage        the storage to load from
      * @param path           the storage location
      *
      * @return the data read, or
      *         null if not found
      */
     private Object loadObject(Storage storage, Path path) {
         MemoryStorage  cache;
         Object         res = null;
         String         id;
         String         msg;
 
         LOG.fine("loading " + path + " from " + storage.path());
         cache = (MemoryStorage) cacheStorages.get(storage.path());
         if (cache != null) {
             res = cache.load(path);
             if (res instanceof StorableObject && !(res instanceof Index)) {
                 LOG.fine("loaded " + path + " value from cache: " + res);
                 ((StorableObject) res).activate();
                 return res;
             }
         }
         res = storage.load(path);
         LOG.fine("loaded " + path + " value: " + res);
         if (cache != null && res instanceof Dict) {
             id = path.toIdent(1);
             res = initObject(id, (Dict) res);
             if (res instanceof StorableObject) {
                 try {
                     cache.store(path, res);
                     LOG.fine("initialized object for " + path + ": " + res);
                 } catch (StorageException e) {
                     msg = "failed to store object in storage cache " +
                           cache.path();
                     LOG.log(Level.WARNING, msg, e);
                 }
             }
         }
         return res;
     }
 
     /**
      * Initializes an object with the corresponding object type (if
      * found).
      *
      * @param id             the object id
      * @param dict           the dictionary data
      *
      * @return the StorableObject instance created, or
      *         the input dictionary if no type matched
      */
     private Object initObject(String id, Dict dict) {
         String          typeId = dict.getString(KEY_TYPE, null);
         Constructor     constr = null;
         Object[]        args;
         StorableObject  obj;
         String          msg;
 
         if (typeId != null) {
             constr = Type.constructor(this, dict);
         }
         if (constr != null) {
             args = new Object[] { id, typeId, dict };
             try {
                 obj = (StorableObject) constr.newInstance(args);
                 obj.init();
                 obj.activate();
                 return obj;
             } catch (Exception e) {
                 msg = "failed to create instance of " + constr.getClass().getName() +
                       " for object " + id + " of type " + typeId;
                 LOG.log(Level.WARNING, msg, e);
                dict.add("_error", msg);
                return dict;
             }
         }
         return dict;
     }
 
     /**
      * Stores an object at the specified location. The path must
      * locate a particular object or file, since direct manipulation
      * of indices is not supported. Any previous data at the
      * specified path will be overwritten or removed.
      *
      * @param path           the storage location
      * @param data           the data to store
      *
      * @throws StorageException if the data couldn't be written
      */
     public void store(Path path, Object data) throws StorageException {
         store(getMountedStorage(path, false), path, data, true);
     }
 
     /**
      * Stores an object at the specified location. The path must
      * locate a particular object or file, since direct manipulation
      * of indices is not supported. Any previous data at the
      * specified path will be overwritten or removed. If the caching
      * flag is not set, no updates will be made to the storage cache.
      *
      * @param storage        the storage to write to (or null)
      * @param path           the storage location
      * @param data           the data to store
      * @param caching        the caching update flag
      *
      * @throws StorageException if the data couldn't be written
      */
     private void store(Storage storage, Path path, Object data, boolean caching)
         throws StorageException {
 
         if (storage != null) {
             if (caching) {
                 cacheRemove(storage.path(), storage.localPath(path));
             }
             storage.store(storage.localPath(path), data);
             if (caching) {
                 cacheAdd(storage.path(), storage.localPath(path), data);
             }
         } else {
             if (caching) {
                 cacheRemove(null, path);
             }
             for (int i = 0; i < mountedStorages.size(); i++) {
                 storage = (Storage) mountedStorages.get(i);
                 if (storage.isReadWrite() && storage.mountOverlay()) {
                     storage.store(path, data);
                     if (caching) {
                         cacheAdd(storage.path(), path, data);
                     }
                     return;
                 }
             }
             throw new StorageException("no writable storage found for " + path);
         }
     }
 
     /**
      * Removes an object or an index at the specified location. If
      * the path refers to an index, all contained objects and indices
      * will be removed recursively.
      *
      * @param path           the storage location
      *
      * @throws StorageException if the data couldn't be removed
      */
     public void remove(Path path) throws StorageException {
         remove(getMountedStorage(path, false), path, true);
     }
 
     /**
      * Removes an object or an index at the specified location. If
      * the path refers to an index, all contained objects and indices
      * will be removed recursively. If the caching flag is not set,
      * no updates will be made to the storage cache.
      *
      * @param storage        the storage to write to (or null)
      * @param path           the storage location
      * @param caching        the caching update flag
      *
      * @throws StorageException if the data couldn't be removed
      */
     private void remove(Storage storage, Path path, boolean caching)
         throws StorageException {
 
         if (storage != null) {
             if (caching) {
                 cacheRemove(storage.path(), storage.localPath(path));
             }
             storage.remove(storage.localPath(path));
         } else {
             if (caching) {
                 cacheRemove(null, path);
             }
             for (int i = 0; i < mountedStorages.size(); i++) {
                 storage = (Storage) mountedStorages.get(i);
                 if (storage.isReadWrite() && storage.mountOverlay()) {
                     storage.remove(path);
                 }
             }
         }
     }
 
     /**
      * Adds an object to a storage cache (if possible). The object
      * will only be added if it is an instance of  StorableObject and
      * a memory cache exists for the specified storage path.
      *
      * @param storagePath    the storage path to cache for
      * @param path           the object location
      * @param data           the object to store
      */
     private void cacheAdd(Path storagePath, Path path, Object data) {
         MemoryStorage cache = (MemoryStorage) cacheStorages.get(storagePath);
         if (cache != null && data instanceof StorableObject) {
             try {
                 cache.store(path, data);
             } catch (StorageException e) {
                 LOG.log(Level.WARNING, "failed to cache object", e);
             }
         }
     }
 
     /**
      * Removes one or more objects from the storage cache. All the
      * objects removed will be destroyed, but not persisted if
      * modified.
      *
      * @param storagePath    the storage path or null for all
      * @param path           the path to remove
      */
     private void cacheRemove(Path storagePath, Path path) {
         if (storagePath == null) {
             Iterator iter = cacheStorages.keySet().iterator();
             while (iter.hasNext()) {
                 cacheRemove((Path) iter.next(), path);
             }
         } else {
             MemoryStorage cache = (MemoryStorage) cacheStorages.get(storagePath);
             if (cache != null) {
                 cacheRemove(cache, path, false, true);
             }
         }
     }
 
     /**
      * Removes one or more objects from a storage cache. All objects
      * removed will be either persisted (if modified) and/or removed
      * depending on the store and force removal flags.
      *
      * @param cache          the storage cache to modify
      * @param basePath       the object path to remove
      * @param store          the persist modified objects flag
      * @param force          the forced removal flag
      */
     private void cacheRemove(MemoryStorage cache,
                              Path basePath,
                              boolean store,
                              boolean force) {
 
         Metadata[] metas = cache.lookupAll(basePath);
         for (int i = 0; i < metas.length; i++) {
             Path path = metas[i].path();
             Object obj = cache.load(path);
             if (obj instanceof StorableObject) {
                 StorableObject storable = (StorableObject) obj;
                 if (store && storable.isModified()) {
                     try {
                         store(null, path, storable, false);
                         LOG.fine("persisted cached object: " + path);
                     } catch (StorageException e) {
                         LOG.log(Level.WARNING, "failed to persist cached object", e);
                     }
                 }
                 storable.passivate();
                 if (force || !storable.isActive()) {
                     try {
                         storable.destroy();
                     } catch (StorageException e) {
                         LOG.log(Level.WARNING, "failed to destroy cached object", e);
                     }
                     try {
                         cache.remove(path);
                         LOG.fine("removed cached object: " + path);
                     } catch (StorageException e) {
                         LOG.log(Level.WARNING, "failed to remove cached object", e);
                     }
                 }
             } else {
                 try {
                     cache.remove(path);
                     LOG.fine("removed cached data: " + path);
                 } catch (StorageException e) {
                     LOG.log(Level.WARNING, "failed to remove cached data", e);
                 }
             }
         }
     }
 
     /**
      * Destroys all cached objects. The objects will first be
      * passivated and thereafter queried for their status. All
      * modified objects will be stored persistently if possible, but
      * errors will only be logged. If the force clean flag is set,
      * all objects in the cache will be destroyed. Otherwise only
      * inactive objects.<p>
      *
      * This method is called regularly from a background job in
      * order to destroy inactive objects.
      *
      * @param force          the forced clean flag
      */
     public void cacheClean(boolean force) {
         Iterator iter = cacheStorages.values().iterator();
         while (iter.hasNext()) {
             cacheRemove((MemoryStorage) iter.next(), Path.ROOT, true, force);
         }
     }
 }
