 /**
  *  Copyright 2008 ThimbleWare Inc.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.thimbleware.jmemcached;
 
 import com.thimbleware.jmemcached.storage.CacheStorage;
 
 import java.io.IOException;
 
 import java.util.Set;
 import java.util.concurrent.*;
 
 /**
  * Default implementation of the cache handler, supporting local memory cache elements.
  */
 public final class CacheImpl extends AbstractCache<LocalCacheElement> implements Cache<LocalCacheElement> {
 
     final CacheStorage<Key, LocalCacheElement> storage;
     final DelayQueue<DelayedMCElement> deleteQueue;
     private final ScheduledExecutorService scavenger;
 
     /**
      * @inheritDoc
      */
     public CacheImpl(CacheStorage<Key, LocalCacheElement> storage) {
         super();
         this.storage = storage;
         deleteQueue = new DelayQueue<DelayedMCElement>();
 
         scavenger = Executors.newScheduledThreadPool(1);
         scavenger.scheduleAtFixedRate(new Runnable(){
             public void run() {
                 asyncEventPing();
             }
         }, 10, 2, TimeUnit.SECONDS);
     }
 
     /**
      * @inheritDoc
      */
     public DeleteResponse delete(Key key, int time) {
         boolean removed = false;
 
         // delayed remove
         if (time != 0) {
             // block the element and schedule a delete; replace its entry with a blocked element
             LocalCacheElement placeHolder = new LocalCacheElement(key, 0, 0, 0L);
             placeHolder.setData(new byte[]{});
             placeHolder.block(Now() + (long)time);
 
             storage.replace(key, placeHolder);
 
             // this must go on a queue for processing later...
             deleteQueue.add(new DelayedMCElement(placeHolder));
         } else
             removed = storage.remove(key) != null;
 
         if (removed) return DeleteResponse.DELETED;
         else return DeleteResponse.NOT_FOUND;
 
     }
 
     /**
      * @inheritDoc
      */
     public StoreResponse add(LocalCacheElement e) {
         final long origCasUnique = e.getCasUnique();
         e.setCasUnique(casCounter.getAndIncrement());
         final boolean stored = storage.putIfAbsent(e.getKey(), e) == null;
         // we should restore the former cas so that the object isn't left dirty
         if (!stored) {
             e.setCasUnique(origCasUnique);
         }
         return stored ? StoreResponse.STORED : StoreResponse.NOT_STORED;
     }
 
     /**
      * @inheritDoc
      */
     public StoreResponse replace(LocalCacheElement e) {
         return storage.replace(e.getKey(), e) != null ? StoreResponse.STORED : StoreResponse.NOT_STORED;
     }
 
     /**
      * @inheritDoc
      */
     public StoreResponse append(LocalCacheElement element) {
         LocalCacheElement old = storage.get(element.getKey());
         if (old == null || isBlocked(old) || isExpired(old)) {
             getMisses.incrementAndGet();
             return StoreResponse.NOT_FOUND;
         }
         else {
             return storage.replace(old.getKey(), old, old.append(element)) ? StoreResponse.STORED : StoreResponse.NOT_STORED;
         }
     }
 
     /**
      * @inheritDoc
      */
     public StoreResponse prepend(LocalCacheElement element) {
         LocalCacheElement old = storage.get(element.getKey());
         if (old == null || isBlocked(old) || isExpired(old)) {
             getMisses.incrementAndGet();
             return StoreResponse.NOT_FOUND;
         }
         else {
             return storage.replace(old.getKey(), old, old.prepend(element)) ? StoreResponse.STORED : StoreResponse.NOT_STORED;
         }
     }
 
     /**
      * @inheritDoc
      */
     public StoreResponse set(LocalCacheElement e) {
         setCmds.incrementAndGet();//update stats
 
         e.setCasUnique(casCounter.getAndIncrement());
 
         storage.put(e.getKey(), e);
 
         return StoreResponse.STORED;
     }
 
     /**
      * @inheritDoc
      */
     public StoreResponse cas(Long cas_key, LocalCacheElement e) {
         // have to get the element
         LocalCacheElement element = storage.get(e.getKey());
         if (element == null || isBlocked(element)) {
             getMisses.incrementAndGet();
             return StoreResponse.NOT_FOUND;
         }
 
         if (element.getCasUnique() == cas_key) {
             // casUnique matches, now set the element
             if (storage.replace(e.getKey(), element, e)) return StoreResponse.STORED;
             else {
                 getMisses.incrementAndGet();
                 return StoreResponse.NOT_FOUND;
             }
         } else {
             // cas didn't match; someone else beat us to it
             return StoreResponse.EXISTS;
         }
     }
 
     /**
      * @inheritDoc
      */
     public Integer get_add(Key key, int mod) {
         LocalCacheElement old = storage.get(key);
         if (old == null || isBlocked(old) || isExpired(old)) {
             getMisses.incrementAndGet();
             return null;
         } else {
             LocalCacheElement.IncrDecrResult result = old.add(mod);
             return storage.replace(old.getKey(), old, result.replace) ? result.oldValue : null;
         }
     }
 
 
     protected boolean isBlocked(CacheElement e) {
         return e.isBlocked() && e.getBlockedUntil() > Now();
     }
 
     protected boolean isExpired(CacheElement e) {
         return e.getExpire() != 0 && e.getExpire() < Now();
     }
 
     /**
      * @inheritDoc
      */
     public LocalCacheElement[] get(Key ... keys) {
         getCmds.incrementAndGet();//updates stats
 
         LocalCacheElement[] elements = new LocalCacheElement[keys.length];
         int x = 0;
         int hits = 0;
         int misses = 0;
         for (Key key : keys) {
             LocalCacheElement e = storage.get(key);
             if (e == null || isExpired(e) || e.isBlocked()) {
                 misses++;
 
                 elements[x] = null;
             } else {
                 hits++;
 
                 elements[x] = e;
             }
             x++;
 
         }
         getMisses.addAndGet(misses);
         getHits.addAndGet(hits);
 
         return elements;
 
     }
 
     /**
      * @inheritDoc
      */
     public boolean flush_all() {
         return flush_all(0);
     }
 
     /**
      * @inheritDoc
      */
     public boolean flush_all(int expire) {
         // TODO implement this, it isn't right... but how to handle efficiently? (don't want to linear scan entire cacheStorage)
         storage.clear();
         return true;
     }
 
     /**
      * @inheritDoc
      */
     public void close() throws IOException {
         scavenger.shutdown();;
         storage.close();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     protected Set<Key> keys() {
         return storage.keySet();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public long getCurrentItems() {
         return storage.size();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public long getLimitMaxBytes() {
         return storage.getMemoryCapacity();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public long getCurrentBytes() {
         return storage.getMemoryUsed();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void asyncEventPing() {
         DelayedMCElement toDelete = deleteQueue.poll();
         if (toDelete != null) {
             storage.remove(toDelete.element.getKey());
         }
     }
 
 
     /**
      * Delayed key blocks get processed occasionally.
      */
     protected static class DelayedMCElement implements Delayed {
         private CacheElement element;
 
         public DelayedMCElement(CacheElement element) {
             this.element = element;
         }
 
         public long getDelay(TimeUnit timeUnit) {
             return timeUnit.convert(element.getBlockedUntil() - Now(), TimeUnit.MILLISECONDS);
         }
 
         public int compareTo(Delayed delayed) {
             if (!(delayed instanceof CacheImpl.DelayedMCElement))
                 return -1;
             else
                 return new String(element.getKey().bytes).compareTo(new String(((DelayedMCElement)delayed).element.getKey().bytes));
         }
     }
 }
