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
 package twigkit.klustr.strategy;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  *
  */
 public class RoundRobinTest {
 
     @Test
     public void testCreateCluster() throws Exception {
         String s0 = "cluster_0";
         String s1 = "cluster_1";
         String s2 = "cluster_2";
 
         Strategy<String> strategy = new RoundRobin<String>();
         strategy.setResources(s0, s1, s2);
 
         for (int i = 0; i < 2; i++) {
             assertEquals(s0, strategy.next());
             assertEquals(s1, strategy.next());
             assertEquals(s2, strategy.next());
         }
     }
 }
