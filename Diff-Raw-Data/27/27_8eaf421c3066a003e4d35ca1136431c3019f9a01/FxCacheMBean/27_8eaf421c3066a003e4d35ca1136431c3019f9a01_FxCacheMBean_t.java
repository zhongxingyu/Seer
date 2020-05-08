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
 package com.flexive.shared.mbeans;
 
 import com.flexive.shared.cache.FxBackingCache;
 import com.flexive.shared.cache.FxCacheException;
 
 import java.util.Set;
 
 /**
  * Tree cache interface
  *
  * @author UCS
  */
 //@Management
 public interface FxCacheMBean extends FxBackingCache {

    /**
     * System property to override the streaming server port
     * @since 3.1
     */
    final static String STREAMING_PORT_PROPERTY = "fxstreaming.port";

     /**
      * Port of the streaming server
      */
    int DEFAULT_STREAMING_PORT = 6241;
 
     // Life cycle methods
     /**
      * Creates and starts up the cache
      *
      * @throws Exception on errors
      */
     void create() throws Exception;
 
     /**
      * Shutdown
      *
      * @throws Exception on errors
      */
     void destroy() throws Exception;
 
     /**
      * Returns the time the system was started up.
      *
      * @return the time the system was started up.
      */
     long getSystemStartTime();
 
     /**
      * Returns the time this node was started up.
      *
      * @return the time this node was started
      */
     long getNodeStartTime();
 
     /**
      * Return the Id of this deployment of flexive (used to check existance of running mbean server on redeployment)
      *
      * @return deployment id
      */
     String getDeploymentId();
 
     /**
      * A global getKeys
      *
      * @param path path
      * @return key set
      * @throws FxCacheException on errors
      * @see com.flexive.shared.cache.FxBackingCache#getKeys(String)
      */
     public Set globalGetKeys(String path) throws FxCacheException;
 
 
     /**
      * A global get
      *
      * @param path path
      * @param key  key
      * @return value
      * @throws FxCacheException on errors
      * @see com.flexive.shared.cache.FxBackingCache#get(String,Object)
      */
     public Object globalGet(String path, Object key) throws FxCacheException;
 
     /**
      * A global exists
      *
      * @param path path
      * @param key  key
      * @return exists
      * @throws FxCacheException on errors
      * @see com.flexive.shared.cache.FxBackingCache#exists(String,Object)
      */
     public boolean globalExists(String path, Object key) throws FxCacheException;
 
     /**
      * A global put
      *
      * @param path  path
      * @param key   key
      * @param value value
      * @throws FxCacheException on errors
      * @see com.flexive.shared.cache.FxBackingCache#put(String,Object,Object)
      */
     public void globalPut(String path, Object key, Object value) throws FxCacheException;
 
     /**
      * A global remove
      *
      * @param path path
      * @throws FxCacheException on errors
      * @see com.flexive.shared.cache.FxBackingCache#remove(String)
      */
     public void globalRemove(String path) throws FxCacheException;
 
     /**
      * A global remove
      *
      * @param path path
      * @param key  key
      * @throws FxCacheException on errors
      * @see com.flexive.shared.cache.FxBackingCache#remove(String,Object)
      */
     public void globalRemove(String path, Object key) throws FxCacheException;
 
 
     /**
      * Force a reload for given division
      *
      * @param divisionId division
      * @throws Exception on errors
      */
     void reloadEnvironment(Integer divisionId) throws Exception;
 
     /**
      * Set the eviction strategy for a path (if the backing cache supports this)
      *
      * @param divisionId  division
      * @param path        path
      * @param maxContents max. number of entries to allow (0=unlimited)
      * @param timeToIdle  time a value has to be idle to be evicted (0=forever)
      * @param timeToLive  time to live (0=forever)
      * @throws com.flexive.shared.cache.FxCacheException    on cache errors
      */
     void setEvictionStrategy(Integer divisionId, String path, Integer maxContents, Integer timeToIdle, Integer timeToLive) throws FxCacheException;
 
     /**
      * Set the eviction strategy for a path (if the backing cache supports this)
      *
      * @param divisionId  division
      * @param path        path
      * @param maxContents max. number of entries to allow (0=unlimited)
      * @param timeToIdle  time a value has to be idle to be evicted (0=forever)
      * @param timeToLive  time to live (0=forever)
      * @param overwrite   if an existing policy should be overwritten
      * @throws com.flexive.shared.cache.FxCacheException    on cache errors
      * @since 3.0.2
      */
     void setEvictionStrategy(Integer divisionId, String path, Integer maxContents, Integer timeToIdle, Integer timeToLive, Boolean overwrite) throws FxCacheException;
 }
