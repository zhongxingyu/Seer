 /**
  * TwigKit
  *
  * Copyright (c) twigkit.com
  *
  * NOTICE: All information contained herein is, and remains the property
  * of TwigKit and its suppliers, if any.
  * The intellectual and technical concepts contained herein are PROPRIETARY
  * to TwigKit and its suppliers and may be covered by U.S. and Foreign Patents,
  * patents in process, and are protected by trade secret or COPYRIGHT LAW.
  * Dissemination of this information or reproduction of this material
  * is strictly FORBIDDEN unless prior written permission is obtained
  * from TwigKit.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * All Rights Reserved.
  *
  */
 package twigkit.klustr;
 
 import org.junit.Ignore;
 import org.junit.Test;
 import twigkit.klustr.Cluster;
 import twigkit.klustr.strategy.RoundRobin;
 import twigkit.klustr.strategy.Strategy;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  *
  */
 public class ClusterTest {
 
     @Test
     public void testNext() throws Exception {
         String s0 = "cluster_0";
         String s1 = "cluster_1";
         String s2 = "cluster_2";
 
         Cluster<String> cluster = new Cluster<String>(s0, s1, s2);
 
         for (int i = 0; i < 2; i++) {
             assertEquals(s0, cluster.next());
             assertEquals(s1, cluster.next());
             assertEquals(s2, cluster.next());
         }
     }
 
     @Test
     public void testReset() throws Exception {
         String s0 = "cluster_0";
         String s1 = "cluster_1";
         String s2 = "cluster_2";
 
         Cluster<String> cluster = new Cluster<String>(s0, s1, s2);
 
         assertEquals(s0, cluster.next());
         assertEquals(s1, cluster.next());
 
         cluster.reset();
 
         assertEquals(s0, cluster.next());
         assertEquals(s1, cluster.next());
         assertEquals(s2, cluster.next());
     }
 
     @Test
     public void testMultithreaded() throws Exception {
         for (int p = 0; p < 50; p++) {
             String s0 = "cluster_0";
             String s1 = "cluster_1";
             String s2 = "cluster_2";
 
             final Cluster<String> cluster = new Cluster<String>(s0, s1, s2);
 
             Collection<Callable<String>> tasks = new ArrayList<Callable<String>>();
             for (int i = 0; i < 10000; i++) {
                 tasks.add(new Callable<String>() {
                     public String call() throws Exception {
                         return cluster.next();
                     }
                 });
             }
 
             ExecutorService executorService = Executors.newFixedThreadPool(10);
             List<Future<String>> futures = executorService.invokeAll(tasks);
             assertEquals(10000, futures.size());
             for (Future<String> f : futures) {
                 f.get();
             }
            executorService.shutdown();
         }
     }
 }
