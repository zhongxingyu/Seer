 package com.github.tachesimazzoca.java.example;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.concurrent.ConcurrentHashMap;
 
 public final class ConcurrentHashMapExample {
     private ConcurrentHashMapExample() {
     }
 
     public static void main(String[] args) throws InterruptedException {
         for (int i = 0; i < 100; i++) {
             System.out.println("ConcurrentHashMap is Thread safe.");
             ConcurrentHashMap<Integer, Integer> cmap = new ConcurrentHashMap<Integer, Integer>();
             Thread ct1 = new Thread(new CreateMapTask(cmap));
             Thread ct2 = new Thread(new CreateMapTask(cmap));
             ct1.start();
             ct2.start();
             ct1.join();
             ct2.join();
 
             System.out.println("HashMap might cause an infinite loop.");
             HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
             Thread t1 = new Thread(new CreateMapTask(map));
             Thread t2 = new Thread(new CreateMapTask(map));
             t1.start();
             t2.start();
             t1.join();
             t2.join();
         }
     }
 
     private static class CreateMapTask implements Runnable {
         private Map<Integer, Integer> map;
 
        protected CreateMapTask() {
        }

         public CreateMapTask(Map<Integer, Integer> map) {
             this.map = map;
         }
 
         public void run() {
             for (int i = 0; i < 10000000; i++) {
                 int k = i % 10000;
                 if (map.containsKey(k)) {
                     map.remove(k);
                 } else {
                     map.put(k, i);
                 }
             }
         }
     }
 }
