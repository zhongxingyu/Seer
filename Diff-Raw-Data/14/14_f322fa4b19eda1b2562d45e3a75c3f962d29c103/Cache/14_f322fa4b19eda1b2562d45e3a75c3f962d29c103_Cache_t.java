 package org.narwhal.util;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * The <code>Cache</code> class is an implementation of the thread-safe cache that
  * keeps pair of mapped class and mapped class information correspondingly.
  * Actually, this cache is an implementation of the thread safe hash map.
  * */
 public class Cache {
 
     private Lock readLock;
     private Lock writeLock;
     private Map<Class, MappedClassInformation> entityCache;
 
     /**
      * Initializes a new instance of the Cache class.
      * */
     public Cache() {
         ReadWriteLock lock = new ReentrantReadWriteLock();
         readLock = lock.readLock();
         writeLock = lock.writeLock();
         entityCache = new HashMap<>();
     }
 
     /**
      * Tests whether cache contains a particular key or not.
      *
     * @param key Key whose presence in this map is tested
      * @return True if cache contains the key. False otherwise.
      * */
     public boolean containsKey(Class key) {
         readLock.lock();
         try {
             return entityCache.containsKey(key);
         } finally {
             readLock.unlock();
         }
     }
 
     /**
     * Maps the value with the key. If the map already has the key
     * then the old value will be replaced by the new one.
      *
     * @param mappedClass The value of the Class which is used as a key.
     * @param classInformation The instance of the MappedClassInformation which is used as a value.
     * @return The mapped value which is mapped with a particular key.
      * */
     public MappedClassInformation put(Class mappedClass, MappedClassInformation classInformation) {
         writeLock.lock();
         try {
             return entityCache.put(mappedClass, classInformation);
         } finally {
             writeLock.unlock();
         }
     }
 
     /**
      * Returns the value to which the specified key is mapped.
      *
      * @param key Key whose presence in this map is to be tested
      * @return Instance of the MappedClassInformation if the key is presence in the map. Null otherwise.
      * */
     public MappedClassInformation get(Class key) {
         readLock.lock();
         try {
             return entityCache.get(key);
         } finally {
             readLock.unlock();
         }
     }
 }
