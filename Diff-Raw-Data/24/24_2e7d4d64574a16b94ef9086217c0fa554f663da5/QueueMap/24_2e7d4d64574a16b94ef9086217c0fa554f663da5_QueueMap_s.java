 package org.openengsb.loom.java.impl;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.LinkedBlockingQueue;
 
 public class QueueMap<K, V> {
     private ConcurrentMap<K, BlockingQueue<V>> map = new ConcurrentHashMap<K, BlockingQueue<V>>();
 
     public void put(K key, V value) {
         synchronized (map) {
             if (!map.containsKey(key)) {
                 map.put(key, new LinkedBlockingQueue<V>());
                 map.notifyAll();
             }
            map.get(key).add(value);
         }
     }
 
     public V poll(K key) throws InterruptedException {
         BlockingQueue<V> queue;
         synchronized (map) {
             while (!map.containsKey(key)) {
                 map.wait();
             }
             queue = map.get(key);
         }
        return queue.poll();
     }
 }
