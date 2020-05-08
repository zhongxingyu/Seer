 package org.daum.library.ormH.store;
 
 import org.daum.library.ormH.api.PersistenceSessionStore;
 import org.daum.library.ormH.persistence.Orhm;
 import org.daum.library.ormH.utils.PersistenceException;
 import org.daum.library.replica.cache.Cache;
 import org.daum.library.replica.cache.ReplicaService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jed
  * Date: 22/06/12
  * Time: 10:35
  * To change this template use File | Settings | File Templates.
  */
 public class ReplicaStore  implements PersistenceSessionStore {
 
     private ReplicaService replicatingService=null;
     private Logger logger = LoggerFactory.getLogger(this.getClass());
 
     public ReplicaStore(ReplicaService cache)throws PersistenceException {
         this.replicatingService = cache;
         if(replicatingService == null)
         {
             throw new PersistenceException("service is null");
         }
     }
 
 
     @Override
     public void save(Orhm orhm, Object bean) throws PersistenceException {
         if(bean == null)
         {
             throw new PersistenceException("The bean  is null");
         } else  if(replicatingService == null){
             throw new PersistenceException("service is null");
         } else if(orhm == null)
         {
             throw new PersistenceException("Orhm is null");
         }
         logger.debug("Saving "+orhm.getCacheName()+" "+orhm.getId()+" "+orhm.getGeneratedType());
         Cache cache = replicatingService.getCache(orhm.getCacheName());
         if(cache == null)
         {
             throw new PersistenceException("The cache "+orhm.getCacheName()+" is null ");
         }
        cache.put(orhm.getId(),bean);
     }
 
     @Override
     public void update(Orhm orhm, Object bean) throws PersistenceException {
         if(bean == null)
         {
             throw new PersistenceException("The bean  is null");
         } else  if(replicatingService == null){
             throw new PersistenceException("service is null");
         } else if(orhm == null)
         {
             throw new PersistenceException("Orhm is null");
         }
         Cache cache = replicatingService.getCache(orhm.getCacheName());
         if(cache == null)
         {
             throw new PersistenceException("The cache "+orhm.getCacheName()+" is null ");
         }
        cache.put(orhm.getId(),bean);
     }
 
     @Override
     public void delete(Orhm orhm) throws PersistenceException {
         if(replicatingService == null){
             throw new PersistenceException("service is null");
         } else if(orhm == null)
         {
             throw new PersistenceException("Orhm is null");
         }
         Cache cache = replicatingService.getCache(orhm.getCacheName());
         if(cache == null)
         {
             throw new PersistenceException("The cache "+orhm.getCacheName()+" is null ");
         }
         cache.remove(orhm.getId());
     }
 
     @Override
     public Object get(Orhm orhm) throws PersistenceException {
         if(replicatingService == null){
             throw new PersistenceException("service is null");
         }
         Cache cache = replicatingService.getCache(orhm.getCacheName());
         if(cache == null)
         {
             throw new PersistenceException("The cache "+orhm.getCacheName()+" is null ");
         }
         return cache.get(orhm.getId()).getValue();
     }
 
     @Override
     public Map<Object, Object> getAll(Orhm id) throws PersistenceException
     {
         if(replicatingService == null)
         {
             throw new PersistenceException("service is null");
         }
         HashMap<Object,Object> result = new HashMap<Object, Object>();
         Cache cache = replicatingService.getCache(id.getCacheName());
         if(cache != null)
         {
             for( Object key : cache.keySet())
             {
                 result.put(key,cache.get(key).getValue());
             }       return result;
         }
         return  result;
     }
 
     @Override
     public Object lock(Orhm id) throws PersistenceException {
 
         return null;
     }
 
     @Override
     public void unlock(Orhm id) throws PersistenceException {
 
     }
 
 }
