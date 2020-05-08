 /*
  * Copyright (C) 2009 eXo Platform SAS.
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
 package org.exoplatform.services.database.impl;
 
 import org.exoplatform.services.cache.CacheService;
 import org.exoplatform.services.cache.ExoCache;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.hibernate.cache.Cache;
 import org.hibernate.cache.CacheException;
 import org.hibernate.cache.CacheProvider;
 import org.hibernate.cache.Timestamper;
 
 import java.io.Serializable;
 import java.util.Properties;
 
 /**
  * Created by The eXo Platform SAS .
  * 
  * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov
  *         </a>
  * @version $Id: ExoCacheProvider.java 34415 2009-07-23 14:33:34Z dkatayev $
  */
 @Deprecated
 public class ExoCacheProvider implements CacheProvider
 {
 
    private static final Log LOG = ExoLogger.getLogger("exo.core.component.database.HibernateServiceImpl");
 
    private CacheService cacheService;
 
    public ExoCacheProvider(CacheService cacheService)
    {
       this.cacheService = cacheService;
 
    }
 
    public Cache buildCache(String name, Properties properties) throws CacheException
    {
       try
       {
          ExoCache<Serializable, Object> cache = cacheService.getCacheInstance(name);
          cache.setMaxSize(5000);
 
          return new ExoCachePlugin(cache);
       }
       catch (Exception ex)
       {
          LOG.error(ex.getLocalizedMessage(), ex);
         throw new CacheException("Cannot instanstiate cache provider");
       }
    }
 
    public long nextTimestamp()
    {
       return Timestamper.next();
    }
 
    /**
     * Callback to perform any necessary initialization of the underlying cache
     * implementation during SessionFactory construction.
     * 
     * @param properties current configuration settings.
     */
    public void start(Properties properties) throws CacheException
    {
 
    }
 
    /**
     * Callback to perform any necessary cleanup of the underlying cache
     * implementation during SessionFactory.close().
     */
    public void stop()
    {
 
    }
 
    public boolean isMinimalPutsEnabledByDefault()
    {
       return true;
    }
 
 }
