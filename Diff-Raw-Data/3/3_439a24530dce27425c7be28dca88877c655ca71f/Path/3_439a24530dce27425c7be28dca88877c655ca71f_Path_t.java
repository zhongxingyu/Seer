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
 
 package org.rapidcontext.core.data;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * A data storage path. This class encapsulates the path (directory
  * plus name) of an object, a file or an index. It also provides
  * some simple help methods to access and work with the path and to
  * locate the object addressed by it.
  *
  * @author Per Cederberg
  * @version 1.0
  */
 public class Path {
 
     /**
      * The index flag. This flag is set if the path corresponds
      * to an index (a directory, a list of objects and files).
      */
     private boolean index = false;
 
     /**
      * The path components. The last element in this array is the
      * object name, and any previous elements correspond to parent
      * indices (i.e. the parent path). The root index has a zero
      * length array.
      */
     private String[] parts = null;
 
     /**
      * Creates a new data selector from a query string (similar to
      * an file system path for example).
      *
      * @param query          the query string to parse
      */
     public Path(String query) {
         query = StringUtils.stripStart(query, "/");
         this.index = query.equals("") || query.endsWith("/");
         query = StringUtils.stripEnd(query, "/");
         if (query.equals("")) {
             this.parts = ArrayUtils.EMPTY_STRING_ARRAY;
         } else {
             this.parts = query.split("/");
         }
     }
 
     /**
      * Returns a string representation of this object.
      *
      * @return a string representation of this object
      */
     public String toString() {
         StringBuilder  buffer = new StringBuilder();
 
         for (int i = 0; i < parts.length; i++) {
             buffer.append("/");
             buffer.append(parts[i]);
         }
         if (index) {
             buffer.append("/");
         }
         return buffer.toString();
     }
 
     /**
      * Checks if this path corresponds to the root index.
      *
      * @return true if the path is for the root index, or
      *         false otherwise
      */
     public boolean isRoot() {
         return isIndex() && parts.length == 0;
     }
 
     /**
      * Checks if this path corresponds to an index.
      *
      * @return true if the path is an index, or
      *         false otherwise
      */
     public boolean isIndex() {
         return index;
     }
 
     /**
      * Returns the directory depth. The root index, and any objects
      * located directly there, have depth zero (0). For each
      * additional sub-level traversed, the depth is increased by
      * one (1). Objects and files in the storage tree will not
      * affect the depth.
      *
      * @return the path directory depth
      */
     public int depth() {
         return parts.length - (isIndex() ? 0 : 1);
     }
 
     /**
      * Returns the path length. The length contains the number of
      * elements in the path, counting both indices and any named
      * object or file. The length is always greater or equal to
      * the depth.
      *
      * @return the path length
      */
     public int length() {
         return parts.length;
     }
 
     /**
      * Returns the name of the last element in the path. This is
      * normally the object name.
      *
      * @return the object or index name, or
      *         null for the root index
      */
     public String name() {
         return (parts.length > 0) ? parts[parts.length - 1] : null;
     }
 
     /**
      * Returns the name of the path element at the specified position.
      * A zero position will return the first element traversed, i.e.
      * the one located in the root.
      *
      * @param pos            the position, 0 <= pos < length()
      *
      * @return the name of the element at the specified position, or
      *         null if the position is out of range
      */
     public String name(int pos) {
         if (0 <= pos && pos < parts.length) {
             return parts[pos];
         } else {
             return null;
         }
     }
 }
