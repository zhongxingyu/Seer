 package net.sparkmuse.common;
 
 import net.sparkmuse.common.Cache;
 import net.sparkmuse.data.Cacheable;
 import net.sparkmuse.common.CacheKey;
 import com.google.common.base.Preconditions;
 
 import java.io.Serializable;
 
 import org.joda.time.Days;
 import org.joda.time.DurationFieldType;
 
 /**
  * Stupid impl is because Play cache can only be accessed statically :(
  *
  * @author neteller
  * @created: Jul 18, 2010
  */
 public class PlayCache implements Cache {
 
   private final play.cache.CacheImpl cache;
 
   public PlayCache(play.cache.CacheImpl cache) {
     this.cache = cache;
   }
 
   public PlayCache(){
     this(null);
   }
 
   public void clear() {
     if (cache != null) {
       cache.clear();
     }
     else {
       play.cache.Cache.clear();
     }
   }
 
   public void set(String key, Object o, String timeout) {
     if (cache != null) {
       cache.set(key, o, Days.days(30).get(DurationFieldType.millis()));
     }
     else {
       play.cache.Cache.set(key, o, timeout);
     }
   }
 
   public <T> T get(String key, Class<T> clazz) {
     if (cache != null) {
       return clazz.cast(cache.get(key));
     }
     else {
       return play.cache.Cache.get(key, clazz);
     }
   }
 
   public void add(String key, Object o, String timeout) {
     if (cache != null) {
       cache.add(key, o, Days.days(30).get(DurationFieldType.millis()));
     }
     else {
       play.cache.Cache.add(key, o, timeout);
     }
   }
 
   public Object get(String key) {
     Preconditions.checkNotNull(key);
     if (cache != null) {
       return cache.get(key);
     }
     else {
       return play.cache.Cache.get(key);
     }
   }
 
   public <T> T get(final CacheKey<T> key) {
     Preconditions.checkNotNull(key);
     Cacheable<T> cacheable = get(key.toString(), Cacheable.class);
     if (null != cacheable) return cacheable.getInstance();
     else return null;
   }
 
   public <T> T put(String key, T value) {
     Preconditions.checkNotNull(key);
     Preconditions.checkNotNull(value);
     checkSerializable(value);
     set(key, value, "30d");
     return value;
   }
 
   public <T> T put(final Cacheable<T> cacheable) {
     Preconditions.checkNotNull(cacheable);
     Preconditions.checkNotNull(cacheable.getKey(), "Could not get a unique cache key for [" + cacheable.getClass() + "]");
     Preconditions.checkNotNull(cacheable.getInstance());
     checkSerializable(cacheable.getInstance());
     set(cacheable.getKey().toString(), cacheable, "30d");
     return cacheable.getInstance();
   }
 
   private <T> void checkSerializable(Object o) {
     if (!(o instanceof Serializable)) {
       throw new IllegalArgumentException(o + " is not serializable, cannot be cached.");
     }
   }
 }
