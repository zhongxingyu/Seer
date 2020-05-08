 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2013 Per Cederberg. All rights reserved.
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
 
 package org.rapidcontext.core.type;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.rapidcontext.core.data.Array;
 import org.rapidcontext.core.data.Dict;
 import org.rapidcontext.core.storage.Path;
 import org.rapidcontext.core.storage.StorableObject;
 import org.rapidcontext.core.storage.Storage;
 
 /**
  * A user access role. Each role may contain an access rule list for
  * declaring which objects that the role provides access to.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class Role extends StorableObject {
 
     /**
      * The class logger.
      */
     private static final Logger LOG = Logger.getLogger(Role.class.getName());
 
     /**
      * The dictionary key for the role name.
      */
     public static final String KEY_NAME = "name";
 
     /**
      * The dictionary key for the role description.
      */
     public static final String KEY_DESCRIPTION = "description";
 
     /**
      * The dictionary key for automatic user match.
      */
     public static final String KEY_AUTO = "auto";
 
     /**
      * The dictionary key for the role access array. The value stored
      * is an array of access rules.
      */
     public static final String KEY_ACCESS = "access";
 
     /**
      * The dictionary key for the path in the access dictionary. The
      * value stored is an absolute path to an object, with optional
      * glob characters ('*', '**' or '?').
      */
     public static final String ACCESS_PATH = "path";
 
     /**
      * The dictionary key for the regex path in the access
      * dictionary. The value stored is a regular expression matching
      * an absolute path to an object (without leading '/' chars).
      */
     public static final String ACCESS_REGEX = "regex";
 
     /**
      * The dictionary key for the permission list in the access
      * dictionary. The value stored is a string with permissions
      * separated by comma (',').
      *
      * @see #PERM_NONE
      * @see #PERM_INTERNAL
      * @see #PERM_READ
      * @see #PERM_SEARCH
      * @see #PERM_WRITE
      * @see #PERM_ALL
      */
     public static final String ACCESS_PERMISSION = "permission";
 
     /**
      * The permission key for no access.
      */
     public static final String PERM_NONE = "none";
 
     /**
      * The permission key for internal access.
      */
     public static final String PERM_INTERNAL = "internal";
 
     /**
      * The permission key for read access.
      */
     public static final String PERM_READ = "read";
 
     /**
      * The permission key for search access.
      */
     public static final String PERM_SEARCH = "search";
 
     /**
      * The permission key for write access.
      */
     public static final String PERM_WRITE = "write";
 
     /**
      * The permission key for full access.
      */
     public static final String PERM_ALL = "all";
 
     /**
      * The role object storage path.
      */
     public static final Path PATH = new Path("/role/");
 
     /**
      * Searches for all roles in the storage.
      *
      * @param storage        the storage to search in
      *
      * @return an array of all roles found
      */
     public static Role[] findAll(Storage storage) {
         Object[]   objs = storage.loadAll(PATH);
         ArrayList  list = new ArrayList(objs.length);
 
         for (int i = 0; i < objs.length; i++) {
             if (objs[i] instanceof Role) {
                 list.add(objs[i]);
             }
         }
         return (Role[]) list.toArray(new Role[list.size()]);
     }
 
     /**
      * Creates a new role from a serialized representation.
      *
      * @param id             the object identifier
      * @param type           the object type name
      * @param dict           the serialized representation
      */
     public Role(String id, String type, Dict dict) {
         super(id, type, dict);
         dict.set(KEY_NAME, name());
         dict.set(KEY_DESCRIPTION, description());
     }
 
     /**
      * Returns the role name.
      *
      * @return the role name.
      */
     public String name() {
         return dict.getString(KEY_NAME, "");
     }
 
     /**
      * Returns the role description.
      *
      * @return the role description.
      */
     public String description() {
         return dict.getString(KEY_DESCRIPTION, "");
     }
 
     /**
      * Returns the automatic role attachment type. The values "all"
      * and "auth" are the only ones with defined meaning.
      *
      * @return the automatic role attachment type
      */
     public String auto() {
         return dict.getString(KEY_AUTO, "none");
     }
 
     /**
      * Checks if the specified user has this role. The user may be
      * null, in which case only automatic roles for "all" will be
      * considered a match.
      *
      * @param user           the user to check, or null
      *
      * @return true if the user has this role, or
      *         false otherwise
      */
     public boolean hasUser(User user) {
         boolean matchAll = auto().equalsIgnoreCase("all");
         boolean matchAuth = auto().equalsIgnoreCase("auth");
         if (user == null) {
             return matchAll;
         } else  {
             return matchAll || matchAuth || user.hasRole(id());
         }
     }
 
     /**
      * Checks if the role has access permission for a storage path.
      * The access list is processed from top to bottom to find a
      * matching path entry. If a matching path with the PERM_NONE
      * permission is encountered, false will be returned. Otherwise
      * true will be returned only if the permission matches the
      * requested one.
      *
      * @param path           the object storage path
      * @param permission     the requested permission
      *
      * @return true if the role provides access, or
      *         false otherwise
      */
     public boolean hasAccess(String path, String permission) {
         Array arr = dict.getArray(KEY_ACCESS);
         for (int i = 0; arr != null && i < arr.size(); i++) {
             Dict dict = arr.getDict(i);
             if (matchPath(dict, path)) {
                 String perms = dict.getString(ACCESS_PERMISSION, "").trim();
                 HashMap map = (HashMap) dict.get("_" + ACCESS_PERMISSION);
                 if (map == null) {
                     map = new HashMap();
                     String[] list = perms.split("[,;\\s]+");
                     for (int j = 0; j < list.length; j++) {
                         if (list[j].equalsIgnoreCase(PERM_NONE)) {
                             map.put(PERM_NONE, Boolean.TRUE);
                             map.put(PERM_INTERNAL, Boolean.FALSE);
                             map.put(PERM_READ, Boolean.FALSE);
                             map.put(PERM_SEARCH, Boolean.FALSE);
                             map.put(PERM_WRITE, Boolean.FALSE);
                             map.put(PERM_ALL, Boolean.FALSE);
                         } else if (list[j].equalsIgnoreCase(PERM_ALL)) {
                             map.put(PERM_NONE, Boolean.FALSE);
                             map.put(PERM_INTERNAL, Boolean.TRUE);
                             map.put(PERM_READ, Boolean.TRUE);
                             map.put(PERM_SEARCH, Boolean.TRUE);
                             map.put(PERM_WRITE, Boolean.TRUE);
                             map.put(PERM_ALL, Boolean.TRUE);
                         } else if (list[j].equalsIgnoreCase(PERM_WRITE)) {
                             map.put(PERM_INTERNAL, Boolean.TRUE);
                             map.put(PERM_READ, Boolean.TRUE);
                             map.put(PERM_SEARCH, Boolean.TRUE);
                             map.put(PERM_WRITE, Boolean.TRUE);
                         } else if (list[j].equalsIgnoreCase(PERM_SEARCH)) {
                             map.put(PERM_INTERNAL, Boolean.TRUE);
                             map.put(PERM_SEARCH, Boolean.TRUE);
                             map.put(PERM_READ, Boolean.TRUE);
                         } else if (list[j].equalsIgnoreCase(PERM_READ)) {
                             map.put(PERM_INTERNAL, Boolean.TRUE);
                             map.put(PERM_READ, Boolean.TRUE);
                         } else if (list[j].length() <= 0) {
                             map.put(PERM_INTERNAL, Boolean.TRUE);
                             map.put(PERM_READ, Boolean.TRUE);
                         } else {
                             map.put(list[j].toLowerCase(), Boolean.TRUE);
                         }
                     }
                     dict.set("_" + ACCESS_PERMISSION, map);
                 }
                 Boolean bool = (Boolean) map.get(permission.toLowerCase());
                 if (bool != null) {
                     return bool.booleanValue();
                 }
             }
         }
         return false;
     }
 
     /**
      * Checks if the access data matches the specified values.
      *
      * @param dict           the access data
      * @param path           the object storage path
      *
      * @return true if the access path matches, or
      *         false otherwise
      */
     private boolean matchPath(Dict dict, String path) {
         String glob = dict.getString(ACCESS_PATH, null);
         String regex = dict.getString(ACCESS_REGEX, null);
         Pattern m = (Pattern) dict.get("_" + ACCESS_REGEX);
         if (m == null && glob != null) {
             glob = glob.replace("\\", "\\\\").replace(".", "\\.");
             glob = glob.replace("+", "\\+").replace("|", "\\|");
             glob = glob.replace("^", "\\^").replace("$", "\\$");
             glob = glob.replace("(", "\\(").replace(")", "\\)");
             glob = glob.replace("[", "\\[").replace("]", "\\]");
             glob = glob.replace("{", "\\{").replace("}", "\\}");
             glob = glob.replace("**", ".+");
            glob = glob.replace("*", "[^/]+");
             glob = glob.replace("?", ".");
             try {
                 m = Pattern.compile("^" + glob + "$");
             } catch (Exception e) {
                 LOG.log(Level.WARNING, "invalid pattern in role " + id(), e);
                 m = Pattern.compile("^invalid-glob-pattern$");
             }
             dict.set("_" + ACCESS_REGEX, m);
         } else if (m == null && regex != null) {
             regex = StringUtils.removeStart(regex, "^");
             regex = StringUtils.removeStart(regex, "/");
             regex = StringUtils.removeEnd(regex, "$");
             try {
                 m = Pattern.compile("^" + regex + "$");
             } catch (Exception e) {
                 LOG.log(Level.WARNING, "invalid pattern in role " + id(), e);
                 m = Pattern.compile("^invalid-regex-pattern$");
             }
             dict.set("_" + ACCESS_REGEX, m);
         }
         return m.matcher(path).matches();
     }
 }
