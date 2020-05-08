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
 
 import java.util.LinkedHashMap;
import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.rapidcontext.core.data.Array;
 import org.rapidcontext.core.data.Dict;
 
 /**
  * A persistent data storage and retrieval handler based on an
  * in-memory hash table. Naturally, this is not really persistent
  * in case of server shutdown, so should normally be used only for
  * run-time objects that need to be available. An advantage of the
  * memory storage compared to other implementations is that no
  * object serialization is performed, so any type of objects may
  * be stored and retrieved.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class MemoryStorage extends Storage {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(MemoryStorage.class.getName());
 
     /**
      * The data storage map. Indexed by the storage path.
      */
     private LinkedHashMap objects = new LinkedHashMap();
 
     /**
      * The metadata storage map. Indexed by the storage path. This
      * map contains metadata objects corresponding to each data
      * object. It will also contain all the parent indices, all the
      * way back to the root index.
      */
     private LinkedHashMap meta = new LinkedHashMap();
 
     /**
      * The show storage info flag. When set to true, the
      * "/storageinfo" path will be enabled for this storage.
      */
     private boolean storageInfo;
 
     /**
      * Creates a new memory storage.
      *
      * @param readWrite      the read write flag
      * @param storageInfo    the show storage info flag
      */
     public MemoryStorage(boolean readWrite, boolean storageInfo) {
         super("memory", readWrite);
         this.storageInfo = storageInfo;
        if (storageInfo) {
            try {
                store(PATH_STORAGEINFO, dict);
            } catch (StorageException e) {
                LOG.log(Level.WARNING, "internal error in memory storage", e);
            }
        }
     }
 
     /**
      * Destroys this storage. Note that the objects in the storage
      * will NOT be destroyed by this method.
      */
     public void destroy() {
         objects.clear();
         meta.clear();
         objects = null;
         meta = null;
     }
 
     /**
      * Returns a serialized representation of this object. Used when
      * accessing the object from outside pure Java.
      *
      * @return the serialized representation of this object
      */
     public Dict serialize() {
         dict.setInt("_objectCount", objects.size());
         dict.setInt("_metadataCount", meta.size());
         return dict;
     }
 
     /**
      * Returns the number of objects currently in this storage. This
      * number does not include indexes or metadata objects, since
      * those are dynamically inserted and removed.
      *
      * @return the number of objects in the storage
      */
     public int count() {
         return objects.size();
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
         Object obj = meta.get(path);
         if (obj instanceof Index) {
             Index idx = (Index) obj;
             return new Metadata(Index.class, path, path(), idx.lastModified());
         } else {
             return (Metadata) obj;
         }
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
         if (storageInfo && PATH_STORAGEINFO.equals(path)) {
             return serialize();
         }
         if (path.isIndex()) {
             Object obj = meta.get(path);
             return (obj instanceof Index) ? obj : null;
         } else {
             return objects.get(path);
         }
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
         String  msg;
 
         if (path.isIndex()) {
             msg = "cannot write to index " + path;
             LOG.warning(msg);
             throw new StorageException(msg);
         } else if (data == null) {
             msg = "cannot store null data, use remove() instead: " + path;
             LOG.warning(msg);
             throw new StorageException(msg);
         } else if (!isReadWrite()) {
             msg = "cannot store to read-only storage at " + path();
             LOG.warning(msg);
             throw new StorageException(msg);
         }
         if (objects.containsKey(path)) {
             remove(path);
         }
         objects.put(path, data);
         meta.put(path, new Metadata(data.getClass(), path, path(), -1));
         indexInsert(path);
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
         if (!isReadWrite()) {
             String msg = "cannot remove from read-only storage at " + path();
             LOG.warning(msg);
             throw new StorageException(msg);
         }
        if (!storageInfo || !PATH_STORAGEINFO.equals(path)) {
            remove(path, true);
        }
     }
 
     /**
      * Removes an object or an index at the specified location. If
      * the path refers to an index, all contained objects and indices
      * will be removed recursively.
      *
      * @param path           the storage location
      * @param updateParent   the parent index update flag
      */
     private void remove(Path path, boolean updateParent) {
         Object  obj = meta.get(path);
         Index   idx;
         Array   arr;
 
         if (path.isIndex() && obj instanceof Index) {
             idx = (Index) obj;
             arr = idx.indices();
             for (int i = 0; i < arr.size(); i++) {
                 remove(path.child(arr.getString(i, null), true), false);
             }
             arr = idx.objects();
             for (int i = 0; i < arr.size(); i++) {
                 remove(path.child(arr.getString(i, null), false), false);
             }
         }
         objects.remove(path);
         meta.remove(path);
         if (updateParent) {
             indexRemove(path);
         }
     }
 
     /**
      * Inserts a path into the meta-data index structure. Each of the
      * parent indices in the path will be updated until either the
      * root index is reached or no changes are required to the index.
      * The meta-data for the specified path itself is not modified,
      * only the parent indices are changed.
      *
      * @param path           the path previously added
      */
     private void indexInsert(Path path) {
         Path     parent = path.parent();
         Index    idx = (Index) meta.get(parent);
         boolean  modified = false;
 
         if (idx == null) {
             idx = new Index(parent);
         }
         if (path.isIndex()) {
             modified = idx.addIndex(path.name());
         } else {
             modified = idx.addObject(path.name());
         }
         if (modified) {
             idx.updateLastModified(null);
             if (!meta.containsKey(parent)) {
                 meta.put(parent, idx);
                 if (!parent.isRoot()) {
                     indexInsert(parent);
                 }
             }
         }
     }
 
     /**
      * Removes a path from the meta-data index structure. Each of the
      * parent indices in the path will be updated until either the
      * root index is reached or no changes are required to the index.
      * The meta-data for the specified path itself is not modified,
      * only the parent indices are changed.
      *
      * @param path           the path previously removed
      */
     private void indexRemove(Path path) {
         Path     parent = path.parent();
         Index    idx = (Index) meta.get(parent);
         boolean  modified = false;
 
         if (idx != null) {
             if (path.isIndex()) {
                 modified = idx.removeIndex(path.name());
             } else {
                 modified = idx.removeObject(path.name());
             }
             if (modified) {
                 idx.updateLastModified(null);
                 if (idx.indices().size() <= 0 && idx.objects().size() <= 0) {
                     meta.remove(parent);
                     if (!parent.isRoot()) {
                         indexRemove(parent);
                     }
                 }
             }
         }
     }
 }
