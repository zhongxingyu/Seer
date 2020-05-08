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
 
 import java.io.File;
 import java.util.Date;
 
 /**
  * An object metadata container. Used for basic introspection of
  * objects inside storages.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class Metadata extends StorableObject {
 
     /**
      * The dictionary key for the object category, i.e. the type of
      * object being described. The value stored is a string, using
      * one of the predefined constant type values.
      *
      * @see #CATEGORY_INDEX
      * @see #CATEGORY_OBJECT
      * @see #CATEGORY_FILE
      */
     public static final String KEY_CATEGORY = "category";
 
     /**
      * The dictionary key for the Java class of the object. The value
      * stored is the actual Class of the object.
      */
     public static final String KEY_CLASS = "class";
 
     /**
      * The dictionary key for the absolute object path. The value
      * stored is the path object.
      */
     public static final String KEY_PATH = "path";
 
     /**
      * The dictionary key for the absolute storage path. This is the
      * path to the storage containing the object.
      */
     public static final String KEY_STORAGEPATH = "storagePath";
 
     /**
      * The dictionary key for the last modified date. The value stored
      * is a Date object.
      */
     public static final String KEY_MODIFIED = "lastModified";
 
     /**
      * The index category value.
      */
     public static final String CATEGORY_INDEX = "index";
 
     /**
      * The object category value.
      */
     public static final String CATEGORY_OBJECT = "object";
 
     /**
      * The file category value.
      */
     public static final String CATEGORY_FILE = "file";
 
     /**
      * Returns the last modified date of two metadata objects.
      *
      * @param meta1          the first metadata object
      * @param meta2          the second metadata object
      *
      * @return the last modified date of the two objects
      */
     public static Date lastModified(Metadata meta1, Metadata meta2) {
         if (meta1 == null) {
             return meta2.lastModified();
         } else if (meta2 == null) {
             return meta1.lastModified();
         } else if (meta2.lastModified().after(meta1.lastModified())) {
             return meta2.lastModified();
         } else {
             return meta1.lastModified();
         }
     }
 
     /**
      * Creates a new metadata container for an index.
      *
      * @param path           the absolute object path
      * @param storagePath    the absolute storage path
      * @param idx            the index to inspect
      */
     public Metadata(Path path, Path storagePath, Index idx) {
         this(CATEGORY_INDEX, Index.class, path, storagePath, idx.lastModified());
     }
 
     /**
      * Creates a new metadata container for a file.
      *
      * @param path           the absolute object path
      * @param storagePath    the absolute storage path
      * @param file           the file to inspect
      */
     public Metadata(Path path, Path storagePath, File file) {
         this(CATEGORY_FILE, File.class, path, storagePath, new Date(file.lastModified()));
     }
 
     /**
      * Creates a new metadata container for a generic object.
      *
      * @param path           the absolute object path
      * @param storagePath    the absolute storage path
      * @param obj            the object to inspect
      */
     public Metadata(Path path, Path storagePath, Object obj) {
         this(CATEGORY_OBJECT, obj.getClass(), path, storagePath, new Date());
     }
 
     /**
      * Creates a new metadata container for an index.
      *
      * @param path           the absolute object path
      * @param meta           the metadata container to copy
      */
     public Metadata(Path path, Metadata meta) {
         super(null, "metadata");
        dict.addAll(meta.dict);
         dict.set(KEY_PATH, path);
     }
 
     /**
      * Creates a new metadata container.
      *
      * @param category       the object category
      * @param clazz          the object class
      * @param path           the absolute object path
      * @param storagePath    the absolute storage path
      * @param modified       the last modified date, or null for now
      */
     public Metadata(String category, Class clazz, Path path, Path storagePath, Object modified) {
         super(null, "metadata");
         dict.remove(KEY_ID);
         dict.set(KEY_CATEGORY, category);
         dict.set(KEY_CLASS, clazz);
         dict.set(KEY_PATH, path);
         dict.set(KEY_STORAGEPATH, storagePath);
         if (modified instanceof Date) {
             dict.set(KEY_MODIFIED, modified);
         } else if (modified instanceof Long) {
             dict.set(KEY_MODIFIED, new Date(((Long) modified).longValue()));
         } else {
             dict.set(KEY_MODIFIED, new Date());
         }
     }
 
     /**
      * Checks if the object category is an index.
      *
      * @return true if the object category is an index, or
      *         false otherwise
      */
     public boolean isIndex() {
         return CATEGORY_INDEX.equals(category());
     }
 
     /**
      * Checks if the object category is an object.
      *
      * @return true if the object category is an object, or
      *         false otherwise
      */
     public boolean isObject() {
         return CATEGORY_OBJECT.equals(category());
     }
 
     /**
      * Checks if the object category is a file.
      *
      * @return true if the object category is a file, or
      *         false otherwise
      */
     public boolean isFile() {
         return CATEGORY_FILE.equals(category());
     }
 
     /**
      * Returns the category for the object.
      *
      * @return the category for the object
      *
      * @see #CATEGORY_INDEX
      * @see #CATEGORY_OBJECT
      * @see #CATEGORY_FILE
      */
     public String category() {
         return dict.getString(KEY_CATEGORY, null);
     }
 
     /**
      * Returns the class for the object.
      *
      * @return the class for the object
      */
     public Class classInstance() {
         return (Class) dict.get(KEY_CLASS);
     }
 
     /**
      * Returns the class name for the object.
      *
      * @return the class name for the object
      */
     public String className() {
         return classInstance().getName();
     }
 
     /**
      * Returns the absolute object path.
      *
      * @return the absolute object path
      */
     public Path path() {
         return (Path) dict.get(KEY_PATH);
     }
 
     /**
      * Returns the absolute storage path. This is the path to the
      * storage containing the object.
      *
      * @return the absolute storage path
      */
     public Path storagePath() {
         return (Path) dict.get(KEY_STORAGEPATH);
     }
 
     /**
      * Returns the last modified date for the object.
      *
      * @return the last modified date for the object
      */
     public Date lastModified() {
         return (Date) dict.get(KEY_MODIFIED);
     }
 }
