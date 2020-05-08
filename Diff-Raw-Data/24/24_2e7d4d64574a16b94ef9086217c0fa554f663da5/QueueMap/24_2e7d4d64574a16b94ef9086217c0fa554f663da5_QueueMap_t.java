 package org.openengsb.loom.java.impl;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.LinkedBlockingQueue;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 public class QueueMap<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueMap.class);

     private ConcurrentMap<K, BlockingQueue<V>> map = new ConcurrentHashMap<K, BlockingQueue<V>>();
 
     public void put(K key, V value) {
         synchronized (map) {
             if (!map.containsKey(key)) {
                 map.put(key, new LinkedBlockingQueue<V>());
                 map.notifyAll();
             }
         }
        LOGGER.info("putting message in queue {}", key);
        map.get(key).add(value);
     }
 
     public V poll(K key) throws InterruptedException {
         BlockingQueue<V> queue;
        LOGGER.info("looking for message for corr-id: {}", key);
         synchronized (map) {
             while (!map.containsKey(key)) {
                LOGGER.info("waiting for corr-id: {}", key);
                 map.wait();
             }
            LOGGER.info("queue for corr-id found: {}", key);
             queue = map.get(key);
            LOGGER.info("-SYNC: poll");
         }
        LOGGER.info("polling queue for corr-id: {}", key);
        return queue.take();
     }
 }
