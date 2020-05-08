 /*
  * Copyright (C) 2013 Sebastien Diot.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.blockwithme.pingpong;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import scala.concurrent.Await;
 import scala.concurrent.Future;
 import scala.concurrent.duration.Duration;
 import akka.actor.ActorRef;
 import akka.actor.Props;
 import akka.pattern.Patterns;
 import akka.util.Timeout;
 
 import com.blockwithme.pingpong.latency.impl.AkkaBlockingPinger;
 import com.blockwithme.pingpong.latency.impl.AkkaBlockingPonger;
 import com.blockwithme.pingpong.latency.impl.AkkaNonBlockingPinger;
 import com.blockwithme.pingpong.latency.impl.AkkaNonBlockingPonger;
 import com.blockwithme.pingpong.latency.impl.ExecutorServicePinger;
 import com.blockwithme.pingpong.latency.impl.ExecutorServicePonger;
 import com.blockwithme.pingpong.latency.impl.JActorBlockingPinger;
 import com.blockwithme.pingpong.latency.impl.JActorBlockingPonger;
 import com.blockwithme.pingpong.latency.impl.PActorBlockingPinger;
 import com.blockwithme.pingpong.latency.impl.PActorBlockingPonger;
 import com.blockwithme.pingpong.latency.impl.PActorNonBlockingPinger;
 import com.blockwithme.pingpong.latency.impl.PActorNonBlockingPonger;
 import com.blockwithme.pingpong.latency.impl.ThreadWithBlockingQueuePinger;
 import com.blockwithme.pingpong.latency.impl.ThreadWithBlockingQueuePonger;
 import com.blockwithme.pingpong.latency.impl.kilim.KilimTask;
 import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
 import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
 import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
 
 /**
  * Tests the number of seconds required to do sequential request/reply cycles,
  * for different possible Actor implementations.
  *
  * It is in essence a latency test, not a throughput test.
  */
 @AxisRange(min = 0, max = 3)
 @BenchmarkMethodChart(filePrefix = "Benchmark10M")
 public class Benchmark10MTest extends Benchmark100MTest {
 
     /** Allows disabling the tests easily. */
     private static final boolean RUN = true;
 
     /** Allows disabling the testPActorNonBlocking method easily. */
     private static final boolean testPActorNonBlocking = RUN;
 
     /** Allows disabling the testThreadWithBlockingQueue method easily. */
     private static final boolean testExecutorService = RUN;
 
     /** Allows disabling the  method easily. */
     private static final boolean testThreadWithBlockingQueue = RUN;
 
     /** Allows disabling the testAkkaBlocking method easily. */
     private static final boolean testAkkaBlocking = RUN;
 
     /** Allows disabling the testAkkaNonBlocking method easily. */
     private static final boolean testAkkaNonBlocking = RUN;
 
     /** Allows disabling the testJActorBlocking method easily. */
     private static final boolean testJActorBlocking = RUN;
 
     /** Allows disabling the testPActorBlocking method easily. */
     private static final boolean testPActorBlocking = RUN;
 
     /** Allows disabling the testKilimDirectTask method easily. */
     private static final boolean testKilimDirectTask = RUN;
 
     /** Setup all "services" for all test methods. */
     @Override
     @Before
     public void setup() {
         super.setup();
         MESSAGES = 10000000;
     }
 
     /** Test with PActors, by having a reply generate the next request, to eliminate blocking. */
     @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
     @Test
     public void testPActorNonBlocking() throws Exception {
         if (testPActorNonBlocking) {
             final PActorNonBlockingPinger pinger = new PActorNonBlockingPinger(
                    paMailboxFactory.createMailbox(false));
             final PActorNonBlockingPonger ponger = new PActorNonBlockingPonger(
                    paMailboxFactory.createMailbox(false));
             final Integer result = pinger.hammer(ponger, MESSAGES);
             if (result.intValue() != MESSAGES) {
                 throw new IllegalStateException("Expected " + MESSAGES
                         + " but got " + result);
             }
         }
     }
 
     /** Tests using an ExecutorService. */
     @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
     @Test
     public void testExecutorService() throws Exception {
         if (testExecutorService) {
             final ExecutorServicePinger pinger = new ExecutorServicePinger(
                     executorService);
             final ExecutorServicePonger ponger = new ExecutorServicePonger(
                     executorService);
             try {
                 try {
                     final Integer result = pinger.hammer(ponger, MESSAGES);
                     if (result != MESSAGES) {
                         throw new IllegalStateException("Expected " + MESSAGES
                                 + " but got " + result);
                     }
                 } finally {
                     ponger.kill();
                 }
             } finally {
                 pinger.kill();
             }
         }
     }
 
     /** Test using Threads and blocking queues. */
     @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
     @Test
     public void testThreadWithBlockingQueue() throws Exception {
         if (testThreadWithBlockingQueue) {
             final ThreadWithBlockingQueuePinger pinger = new ThreadWithBlockingQueuePinger();
             final ThreadWithBlockingQueuePonger ponger = new ThreadWithBlockingQueuePonger();
             pinger.start();
             try {
                 ponger.start();
                 try {
                     final Integer result = pinger.hammer(ponger, MESSAGES);
                     if (result != MESSAGES) {
                         throw new IllegalStateException("Expected " + MESSAGES
                                 + " but got " + result);
                     }
                 } finally {
                     ponger.kill();
                 }
             } finally {
                 pinger.kill();
             }
         }
     }
 
     /** Test in Akka, using blocking Futures. */
     @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
     @Test
     public void testAkkaBlocking() throws Exception {
         if (testAkkaBlocking) {
             final ActorRef pinger = system.actorOf(new Props(
                     AkkaBlockingPinger.class), "blockingPinger");
             final ActorRef ponger = system.actorOf(new Props(
                     AkkaBlockingPonger.class), "blockingPonger");
 
             final Timeout timeout = new Timeout(Duration.create(600, "seconds"));
             final Future<Object> future = Patterns.ask(pinger,
                     AkkaBlockingPinger.hammer(ponger, MESSAGES), timeout);
             final Integer result = (Integer) Await.result(future,
                     timeout.duration());
             if (result.intValue() != MESSAGES) {
                 throw new IllegalStateException("Expected " + MESSAGES
                         + " but got " + result);
             }
         }
     }
 
     /** Test in Akka, by having a reply generate the next request, to eliminate blocking. */
     @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
     @Test
     public void testAkkaNonBlocking() throws Exception {
         if (testAkkaNonBlocking) {
             final ActorRef pinger = system.actorOf(new Props(
                     AkkaNonBlockingPinger.class), "nonBlockingPinger");
             final ActorRef ponger = system.actorOf(new Props(
                     AkkaNonBlockingPonger.class), "nonBlockingPonger");
 
             final Timeout timeout = new Timeout(Duration.create(600, "seconds"));
             final Future<Object> future = Patterns.ask(pinger,
                     AkkaNonBlockingPinger.hammer(ponger, MESSAGES), timeout);
             final Integer result = (Integer) Await.result(future,
                     timeout.duration());
             if (result.intValue() != MESSAGES) {
                 throw new IllegalStateException("Expected " + MESSAGES
                         + " but got " + result);
             }
         }
     }
 
     /** Test in JActors, using blocking Futures. */
     @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
     @Test
     public void testJActorBlocking() throws Exception {
         if (testJActorBlocking) {
             final JActorBlockingPinger pinger = new JActorBlockingPinger(
                     jaMailboxFactory.createMailbox());
             final JActorBlockingPonger ponger = new JActorBlockingPonger(
                     jaMailboxFactory.createMailbox());
             final Integer result = pinger.hammer(ponger, MESSAGES);
             if (result.intValue() != MESSAGES) {
                 throw new IllegalStateException("Expected " + MESSAGES
                         + " but got " + result);
             }
         }
     }
 
     /** Test with PActors, using the pend() method to block. */
     @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
     @Test
     public void testPActorBlocking() throws Exception {
         if (testPActorBlocking) {
             final PActorBlockingPinger pinger = new PActorBlockingPinger(
                     paMailboxFactory.createMailbox());
             final PActorBlockingPonger ponger = new PActorBlockingPonger(
                     paMailboxFactory.createMailbox());
             final Integer result = pinger.hammer(ponger, MESSAGES);
             if (result.intValue() != MESSAGES) {
                 throw new IllegalStateException("Expected " + MESSAGES
                         + " but got " + result);
             }
         }
     }
 
     /** Test with Kilim */
     @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
     @Test
     public void testKilimDirectTask() throws Exception {
         if (testKilimDirectTask) {
             final int result = KilimTask.test(MESSAGES);
             if (result != MESSAGES) {
                 throw new IllegalStateException("Expected " + MESSAGES
                         + " but got " + result);
             }
         }
     }
 }
