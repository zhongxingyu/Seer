 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.core.structure;
 
 import com.flexive.core.Database;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.cache.FxCacheException;
 import com.flexive.shared.configuration.DivisionData;
 import com.flexive.shared.exceptions.FxApplicationException;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.List;
 
 /**
  * Environment helper functions (core)
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 final public class FxEnvironmentUtils {
 //    private static final Log LOG = LogFactory.getLog(FxEnvironmentUtils.class);
 
     private final static String ATTR_ENV_REQUEST_ONLY = "$flexive.env.req$";
 
     /**
      * Includes the division id into the path.
      *
      * @param divisionId the division
      * @param path       the path to encode
      * @return the encoded path
      * @throws FxCacheException if the division id could not be resolved
      */
     private static String divisionEncodePath(int divisionId, final String path) throws FxCacheException {
         if (!DivisionData.isValidDivisionId(divisionId)) {
             throw new FxCacheException("Division ID missing");
         }
         return "#" + divisionId + (path.startsWith("/") ? "" : "/") + path;
 //        return "/Division" + divisionId + (path.startsWith("/") ? "" : "/") + path;
     }
 
     /**
      * Get an object from the cache
      *
      * @param divisionId division
      * @param path       path
      * @param key        key
      * @return requested object or <code>null</code> if not found
      * @throws FxCacheException on errors
      */
     protected static Object cacheGet(int divisionId, String path, Object key) throws FxCacheException {
         return CacheAdmin.getInstance().get(divisionEncodePath(divisionId, path), key);
     }
 
     /**
      * Put an object into the cache
      *
      * @param divisionId division
      * @param path       path
      * @param key        key
      * @param value      value
      * @throws FxCacheException on errors
      */
     protected static void cachePut(int divisionId, String path, Object key, Object value) throws FxCacheException {
         if (CacheAdmin.ENVIRONMENT_RUNTIME.equals(key) && CacheAdmin.ENVIRONMENT_BASE.equals(path)) {
             if (isCacheEnvironmentRequestOnly()) {
                 FxContext.get().setAttribute(CacheAdmin.ATTR_ENVIRONMENT, value);
                 return;
             }
             CacheAdmin.environmentChanged();
         }
         CacheAdmin.getInstance().put(divisionEncodePath(divisionId, path), key, value);
     }
 
     /**
      * Remove a path from the cache
      *
      * @param divisionId division
      * @param path       path
      * @throws FxCacheException on errors
      */
     protected static void cacheRemove(int divisionId, String path) throws FxCacheException {
         CacheAdmin.getInstance().remove(divisionEncodePath(divisionId, path));
     }
 
     /**
      * Remove an object from the cache
      *
      * @param divisionId the division
      * @param path       path in cache
      * @param key        key
      * @throws FxCacheException on errors
      */
     protected static void cacheRemove(int divisionId, String path, String key) throws FxCacheException {
         CacheAdmin.getInstance().remove(divisionEncodePath(divisionId, path), key);
     }
 
     /**
      * Cache the environment for the current request only?
      *
      * @param requestOnly cache the environment for the current request only?
      * @since 3.1.4
      */
     public static void setCacheEnvironmentRequestOnly(boolean requestOnly) {
         if (requestOnly)
             FxContext.get().setAttribute(ATTR_ENV_REQUEST_ONLY, Boolean.TRUE);
         else {
             boolean isSet = isCacheEnvironmentRequestOnly();
             FxContext.get().setAttribute(ATTR_ENV_REQUEST_ONLY, Boolean.FALSE);
             try {
                 final Object env = FxContext.get().getAttribute(CacheAdmin.ATTR_ENVIRONMENT);
                 if (isSet && env != null) {
                     cachePut(FxContext.get().getDivisionId(), CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, env);
                 }
             } catch (FxCacheException e) {
                 //noinspection ThrowableInstanceNeverThrown
                 throw new FxApplicationException(e).asRuntimeException();
             }
         }
     }
 
     /**
      * Is the environment cached for the current request only?
      *
      * @return environment cached for the current request only?
      * @since 3.1.4
      */
     public static boolean isCacheEnvironmentRequestOnly() {
         Object o = FxContext.get().getAttribute(ATTR_ENV_REQUEST_ONLY);
         return o != null && (o instanceof Boolean) && ((Boolean) o);
     }
 
     /**
      * Replace the environments languages
      *
      * @param languages new active languages
      * @throws FxCacheException on errors
      * @since 3.1.4
      */
     public static void replaceEnvironmentLanguages(List<FxLanguage> languages) throws FxCacheException {
        if (CacheAdmin.isNewInstallation() || !CacheAdmin.isEnvironmentLoaded())
             return;
         int divisionId = FxContext.get().getDivisionId();
         FxEnvironmentImpl structure = ((FxEnvironmentImpl) FxEnvironmentUtils.cacheGet(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME)).deepClone();
         structure.setLanguages(languages);
         FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, structure);
     }
 
     /**
      * Get a DB Connection for the given division
      *
      * @param divisionId the division
      * @return connection
      * @throws SQLException on errors
      */
     protected static Connection getDbConnection(int divisionId) throws SQLException {
         // Check division
         if (!DivisionData.isValidDivisionId(divisionId)) {
             throw new SQLException("Unable to obtain connection: Division not defined");
         }
         return Database.getDbConnection(divisionId);
     }
 }
