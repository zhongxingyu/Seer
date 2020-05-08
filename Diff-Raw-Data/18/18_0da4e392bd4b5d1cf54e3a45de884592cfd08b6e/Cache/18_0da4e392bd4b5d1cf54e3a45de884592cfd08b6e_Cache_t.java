 // Copyright 2002-2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.cache;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.lang.ref.ReferenceQueue;
 import java.util.HashMap;
 
 /**
  * A memory sensitive cache.
  */
 public final class
 Cache<K,V> implements Serializable {
     static private final long serialVersionUID = 1L;
 
     /**
      * cache exit queue
      */
     private transient ReferenceQueue<V> wiped;
 
     /**
      * object cache: [ id =&gt; soft value ]
      */
     private transient HashMap<K,CacheReference<K,V>> entries;
 
     private
     Cache() {
         wiped = new ReferenceQueue<V>();
         entries = new HashMap<K,CacheReference<K,V>>();
     }
     
     /**
      * Constructs an instance.
      */
     static public <K,V> Cache<K,V>
     make() { return new Cache<K,V>(); }
     
     // java.io.Serializable interface
 
     /**
      * Restores the transient data structures.
      */
     private void
     readObject(final ObjectInputStream in) throws IOException,
                                                   ClassNotFoundException {
         // Read in the attributes.
         in.defaultReadObject();
 
         // Setup the cache structures.
         entries = new HashMap<K,CacheReference<K,V>>();
         wiped = new ReferenceQueue<V>();
     }
     
     // org.waterken.cache.Cache interface
 
     /**
      * Fetches a value.
      * @param otherwise default value
      * @param key       cache entry key
      * @return cached value, or <code>otherwise</code>
      */
     public V
     fetch(final V otherwise, final K key) {
         final CacheReference<K,V> rv = entries.get(key);
         final V v = null != rv ? rv.get() : null;
         return null != v ? (v instanceof Null ? null : v) : otherwise;
     }
 
     /**
      * Caches a value.
      * @param key   cache entry key
      * @param value cached value
      */
     public void
     put(final K key, final V value) {
 
        // remove dead cache entries
         while (true) {
            final CacheReference<?,?> r = (CacheReference<?,?>)wiped.poll();
            if (null == r) { break; }
            final CacheReference<K,V> x = entries.remove(r.key);
            if (x != r) {
                /*
                 * The entry was overwritten before the soft reference to the
                 * previous value was dequeued. Just put it back in the cache.
                 */
                entries.put(x.key, x);
            }
         }
 
         entries.put(key, new CacheReference<K,V>(key, nonNull(value), wiped));
     }
     
 	static private @SuppressWarnings("unchecked") <V> V
     nonNull(final V value) { return null != value ? value : (V)new Null(); }
     
     static private final class
     Null {}
 }
