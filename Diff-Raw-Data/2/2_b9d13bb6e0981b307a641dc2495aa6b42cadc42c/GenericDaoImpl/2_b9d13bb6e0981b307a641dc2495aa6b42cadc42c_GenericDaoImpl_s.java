 package cz.muni.fi.pv243.mymaps.dao.impl;
 
 import cz.muni.fi.pv243.mymaps.entities.AbstractEntity;
 import cz.muni.fi.pv243.mymaps.caches.CacheContainerProvider;
 import cz.muni.fi.pv243.mymaps.logging.Logged;
 import java.util.Collections;
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.Dependent;
 import javax.enterprise.inject.Model;
 import javax.inject.Inject;
 import org.infinispan.api.BasicCache;
 import org.jboss.logging.Logger;
 
 /**
  * This class avoids redundancy of CRUD operations in every DAO object.
  * Instead it uses generics to operate with every entity in the same way.
  * 
  * @author Jiri Holusa
  * @param <T> 
  */
 @Model
 @Dependent
 public abstract class GenericDaoImpl<T extends AbstractEntity> {
 
     @Inject
     protected CacheContainerProvider provider;
     
     @Inject
     protected Logger log;
     
     protected BasicCache<Long, T> cache;
 
     @PostConstruct
     public void init() {
         cache = provider.getCacheContainer().getCache(cacheName());
     }
 
     protected abstract String cacheName();
 
     @Logged
     public T create(T entity) {        
         if (cache == null) {   
             String msg = "Internal error: cache is null.";
             log.error(msg);
             throw new IllegalStateException(msg);
         }
 
         if (entity == null) {
             String msg = "Entity cannot be null.";
             log.error(msg);
             throw new IllegalArgumentException(msg);
         }
 
         if (entity.getId() != null) {
            String msg = "Enitiy's ID must be null.";
             log.error(msg);
             throw new IllegalArgumentException(msg);
         }
 
         Long id;
         if (!cache.keySet().isEmpty()) {
             id = Collections.max(cache.keySet()) + 1;
         } else {
             id = 1L;
         }
 
         cache.put(id, entity);
         entity.setId(id);
 
         return entity;
     }
 
     @Logged
     public void delete(T entity) {
         if (cache == null) {
             String msg = "Internal error: cache is null.";
             log.error(msg);
             throw new IllegalStateException(msg);
         }
 
         if (entity == null) {
             String msg = "Entity cannot be null.";
             log.error(msg);
             throw new IllegalArgumentException(msg);
         }
 
         if (entity.getId() == null) {
             String msg = "Entity's ID cannot be null.";
             log.error(msg);
             throw new IllegalArgumentException(msg);
         }
 
         cache.remove(entity.getId());
     }
 
     @Logged
     public T update(T entity) {
         if (cache == null) {
             String msg = "Internal error: cache is null.";
             log.error(msg);
             throw new IllegalStateException(msg);
         }
 
         if (entity == null) {
             String msg = "Entity cannot be null.";
             log.error(msg);
             throw new IllegalArgumentException(msg);
         }
 
         if (entity.getId() == null) {
             String msg = "Entity's ID cannot be null.";
             log.error(msg);
             throw new IllegalArgumentException(msg);
         }
 
         cache.put(entity.getId(), entity);
 
         return entity;
     }
 
     @Logged
     public T getById(Long id) {
         if (cache == null) {
             String msg = "Internal error: cache is null.";
             log.error(msg);
             throw new IllegalStateException(msg);
         }
 
         if (id == null) {
             String msg = "ID cannot be null.";
             log.error(msg);
             throw new IllegalArgumentException(msg);
         }
 
         return cache.get(id);
     }
 }
