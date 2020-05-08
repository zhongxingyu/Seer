 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2010, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.jboss.weld.shared.plugins.cache;
 
 import org.infinispan.Cache;
 import org.infinispan.config.GlobalConfiguration;
 
 /**
  * Infinispan cache builder SPI.
  *
  * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
  */
 public interface CacheBuilder
 {
    /**
    * Session externalizer id.
    */
   static Integer SESSION_EXTERNALIZER_ID = Integer.MAX_VALUE - 1;

   /**
     * Start.
     */
    void start();
 
    /**
     * Stop.
     */
    void stop();
 
    /**
     * Get global configuration.
     *
     * @return the global configuration
     */
    GlobalConfiguration config();
 
    /**
     * Get Infinispan cache.
     *
     * @param cacheName the cache name
     * @param templateCacheName the template cache name
     * @return named cache instance
     */
    <V> Cache<String, V> getCache(String cacheName, String templateCacheName);
 }
