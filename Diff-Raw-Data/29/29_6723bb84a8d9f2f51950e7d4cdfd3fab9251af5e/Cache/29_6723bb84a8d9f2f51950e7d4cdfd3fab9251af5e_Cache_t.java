 package net.sparkmuse.common;
 
 import net.sparkmuse.common.CacheKey;
 import net.sparkmuse.data.Cacheable;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author neteller
  * @created: Nov 28, 2010
  */
 public interface Cache {
 
   /**
    * Cannot cache a non-inserted entity, these do not have a unique pk yet.
    *
    * @param cacheable
    * @param <T>
    * @return
    */
   <T> T put(Cacheable<T> cacheable);
 
   <T> T get(CacheKey<T> key);
 
   Object get(String key);
 
   <T> T get(String key, Class<T> type);
 
   <T> T put(String key, T value);
 
   void clear();
 
  void delete(String key);

  void delete(CacheKey key);

 }
