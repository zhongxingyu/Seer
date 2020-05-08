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
 
 package org.rapidcontext.core.storage;
 
 import java.lang.reflect.Constructor;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringUtils;
 import org.rapidcontext.core.data.Array;
 import org.rapidcontext.core.data.Dict;
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
  *       storage. If a matching type handler or class can be located,
  *       the corresponding object will be created, initialized and
  *       cached for future references.
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
      * The system time of the last mount or remount operation.
      */
     private static long lastMountTime = 0L;
 
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
      * Updates the mount information in a storage object.
      *
      * @param storage        the storage to update
      * @param readWrite      the read write flag
      * @param overlay        the root overlay flag
      * @param prio           the root overlay search priority (higher numbers
      *                       are searched before lower numbers)
      */
     private void updateMountInfo(Storage storage,
                                  boolean readWrite,
                                  boolean overlay,
                                  int prio) {
 
         lastMountTime = Math.max(System.currentTimeMillis(), lastMountTime + 1);
         storage.dict.set(KEY_MOUNT_TIME, new Date(lastMountTime));
         storage.dict.setBoolean(KEY_READWRITE, readWrite);
         storage.dict.setBoolean(KEY_MOUNT_OVERLAY, overlay);
         storage.dict.setInt(KEY_MOUNT_OVERLAY_PRIO, overlay ? prio : -1);
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
             cache.dict.set(KEY_MOUNT_PATH, path);
             cacheStorages.put(path, cache);
         } else if (!exist && cacheStorages.containsKey(path)) {
             cache = (MemoryStorage) cacheStorages.get(path);
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
         storage.dict.set(KEY_MOUNT_PATH, path);
         updateMountInfo(storage, readWrite, overlay, prio);
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
         updateMountInfo(storage, readWrite, overlay, prio);
         mountedStorages.sort();
         updateStorageCache(path, overlay);
     }
 
     /**
      * Unmounts a storage from the specified path. The path must have
      * previously been used to mount a storage.
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
         setMountedStorage(path, null);
         storage.dict.set(KEY_MOUNT_PATH, Path.ROOT);
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
         Metadata  idx = null;
 
         if (storage != null) {
             meta = lookupObject(storage, storage.localPath(path));
            return new Metadata(path, meta);
         } else {
             meta = metadata.lookup(path);
             if (meta != null && meta.isIndex()) {
                 idx = meta;
             } else if (meta != null) {
                 return meta;
             }
             for (int i = 0; i < mountedStorages.size(); i++) {
                 storage = (Storage) mountedStorages.get(i);
                 if (storage.mountOverlay()) {
                     meta = lookupObject(storage, path);
                     if (meta != null && meta.isIndex()) {
                         idx = new Metadata(Metadata.CATEGORY_INDEX,
                                            Index.class,
                                            path,
                                            path(),
                                            Metadata.lastModified(idx, meta));
                     } else if (meta != null) {
                         return meta;
                     }
                 }
             }
         }
         return idx;
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
         // FIXME: The storage metadata will report Dict as the class
         //        for some objects. Should we load these objects in
         //        order to surely return the correct class?
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
             res = loadObject(storage, storage.localPath(path));
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
                 return res;
             }
         }
         res = storage.load(path);
         LOG.fine("loaded " + path + " value: " + res);
         if (cache != null && res instanceof Dict) {
             id = StringUtils.removeStart(path.subPath(1).toString(), "/");
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
                 return obj;
             } catch (Exception e) {
                 msg = "failed to create instance of " + constr.getClass().getName() +
                       " for object " + id + " of type " + typeId;
                 LOG.log(Level.WARNING, msg, e);
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
         Storage  storage = getMountedStorage(path, false);
 
         if (storage != null) {
             flushSingle(storage.path(), storage.localPath(path));
             storeObject(storage, storage.localPath(path), data);
         } else {
             flushAll(path);
             for (int i = 0; i < mountedStorages.size(); i++) {
                 storage = (Storage) mountedStorages.get(i);
                 if (storage.isReadWrite() && storage.mountOverlay()) {
                     storeObject(storage, path, data);
                     return;
                 }
             }
             throw new StorageException("no writable storage found for " + path);
         }
     }
 
     /**
      * Stores an object in a specified storage. If the object is a
      * StorableObject, it will also be stored in the memory cache for
      * the specified storage.
      *
      * @param storage        the storage to use
      * @param path           the storage location
      * @param data           the data to store
      *
      * @throws StorageException if the data couldn't be written
      */
     private void storeObject(Storage storage, Path path, Object data)
         throws StorageException {
 
         MemoryStorage  cache;
 
         storage.store(path, data);
         cache = (MemoryStorage) cacheStorages.get(storage.path());
         if (cache != null && data instanceof StorableObject) {
             cache.store(path, data);
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
         Storage  storage = getMountedStorage(path, false);
 
         if (storage != null) {
             flushSingle(storage.path(), storage.localPath(path));
             storage.remove(storage.localPath(path));
         } else {
             flushAll(path);
             for (int i = 0; i < mountedStorages.size(); i++) {
                 storage = (Storage) mountedStorages.get(i);
                 if (storage.isReadWrite() && storage.mountOverlay()) {
                     storage.remove(path);
                 }
             }
         }
     }
 
     /**
      * Destroys any cached objects for the specified location.
      *
      * @param path           the storage location
      */
     public void flush(Path path) {
         Storage  storage = getMountedStorage(path, false);
 
         if (storage != null) {
             flushSingle(storage.path(), storage.localPath(path));
         } else {
             flushAll(path);
         }
     }
 
     /**
      * Destroys any cached objects for the specified location in a
      * specific memory storage.
      *
      * @param storagePath    the mounted storage path
      * @param path           the storage location to clear
      */
     private void flushSingle(Path storagePath, Path path) {
         MemoryStorage  cache;
 
         cache = (MemoryStorage) cacheStorages.get(storagePath);
         if (cache != null) {
             try {
                 if (path == null) {
                     cache.remove(Path.ROOT);
                 } else {
                     cache.remove(path);
                 }
             } catch (StorageException e) {
                 LOG.log(Level.WARNING, "failed to flush object", e);
             }
         }
     }
 
     /**
      * Destroys any cached objects for the specified location in all
      * of the memory storages.
      *
      * @param path           the storage location
      */
     private void flushAll(Path path) {
         Iterator  iter;
         Path      storagePath;
 
         iter = cacheStorages.keySet().iterator();
         while (iter.hasNext()) {
             storagePath = (Path) iter.next();
             flushSingle(storagePath, path);
         }
     }
 }
