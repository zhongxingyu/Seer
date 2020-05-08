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
 package com.blockwithme.pingpong.latency;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.agilewiki.jactor.JAMailboxFactory;
 import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.pactor.impl.DefaultMailboxFactoryImpl;
 import org.jetlang.fibers.PoolFiberFactory;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import scala.concurrent.Await;
 import scala.concurrent.Future;
 import scala.concurrent.duration.Duration;
 import akka.actor.ActorRef;
 import akka.actor.ActorSystem;
 import akka.actor.Props;
 import akka.pattern.Patterns;
 import akka.util.Timeout;
 
 import com.blockwithme.pingpong.latency.impl.AkkaBlockingPinger;
 import com.blockwithme.pingpong.latency.impl.AkkaBlockingPonger;
 import com.blockwithme.pingpong.latency.impl.AkkaNonBlockingPinger;
 import com.blockwithme.pingpong.latency.impl.AkkaNonBlockingPonger;
 import com.blockwithme.pingpong.latency.impl.DirectPinger;
 import com.blockwithme.pingpong.latency.impl.DirectPonger;
 import com.blockwithme.pingpong.latency.impl.ExecutorServicePinger;
 import com.blockwithme.pingpong.latency.impl.ExecutorServicePonger;
 import com.blockwithme.pingpong.latency.impl.JActorBlockingPinger;
 import com.blockwithme.pingpong.latency.impl.JActorBlockingPonger;
 import com.blockwithme.pingpong.latency.impl.JActorIteratorPinger;
 import com.blockwithme.pingpong.latency.impl.JActorIteratorPonger;
 import com.blockwithme.pingpong.latency.impl.JActorStackOverflowPinger;
 import com.blockwithme.pingpong.latency.impl.JActorStackOverflowPonger;
 import com.blockwithme.pingpong.latency.impl.JetlangPinger;
 import com.blockwithme.pingpong.latency.impl.JetlangPonger;
 import com.blockwithme.pingpong.latency.impl.PActorBlockingPinger;
 import com.blockwithme.pingpong.latency.impl.PActorBlockingPonger;
 import com.blockwithme.pingpong.latency.impl.PActorNonBlockingPinger;
 import com.blockwithme.pingpong.latency.impl.PActorNonBlockingPonger;
 import com.blockwithme.pingpong.latency.impl.ThreadWithBlockingQueuePinger;
 import com.blockwithme.pingpong.latency.impl.ThreadWithBlockingQueuePonger;
 import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
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
 @BenchmarkMethodChart(filePrefix = "PingPongBenchmarks")
 public class LatencyBenchmark extends AbstractBenchmark {
 
     /** Sets the benchmark properties, for stats and graphics generation. */
     static {
         System.setProperty("jub.consumers", "CONSOLE,H2");
         System.setProperty("jub.db.file", "benchmarks");
         System.setProperty("jub.charts.dir", "charts");
     }
 
     /**
      * How many messages to send per test?
      *
      * It must be big enough, that the direct impl takes a measurable amount
      * of time. This means that the slower Actor impl will take each several minutes to test.
      */
     private static final int MESSAGES = 1000000;
 
     /** The Akka ActorSystem */
     private ActorSystem system;
 
     /** The JActor MailboxFactory */
     private MailboxFactory jaMailboxFactory;
 
     /** The PActor Default MailboxFactory */
     private DefaultMailboxFactoryImpl paMailboxFactory;
 
     /** The ExecutorService */
     private ExecutorService executorService;
 
     /** Factory for JetLang fibers. */
     private PoolFiberFactory fiberPool;
 
     /** Setup all "services" for all test methods. */
     @Before
     public void setup() {
         executorService = Executors.newCachedThreadPool();
         system = ActorSystem.create("AkkaTest");
         jaMailboxFactory = JAMailboxFactory.newMailboxFactory(2);
         paMailboxFactory = new DefaultMailboxFactoryImpl(executorService, false);
         fiberPool = new PoolFiberFactory(executorService);
     }
 
     /** Shuts down all "services" for all test methods.
      * @throws Exception */
     @After
     public void teardown() throws Exception {
         system.shutdown();
         system = null;
         jaMailboxFactory.close();
         jaMailboxFactory = null;
         paMailboxFactory.close();
         paMailboxFactory = null;
         fiberPool.dispose();
         fiberPool = null;
         if (!executorService.isShutdown()) {
             executorService.shutdownNow();
         }
         executorService = null;
     }
 
     /** Baseline test: How fast would it go in a single thread? */
     @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
     @Test
     public void testDirect() throws Exception {
         final DirectPinger pinger = new DirectPinger();
         final DirectPonger ponger = new DirectPonger();
         final int result = pinger.hammer(ponger, MESSAGES);
         if (result != MESSAGES) {
             throw new IllegalStateException("Expected " + MESSAGES
                     + " but got " + result);
         }
     }
 
     /** Tests using an ExecutorService. */
     @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
     @Test
     public void testExecutorService() throws Exception {
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
 
     /** Test using Threads and blocking queues. */
     @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
     @Test
     public void testThreadWithBlockingQueue() throws Exception {
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
 
     /** Test in Akka, using blocking Futures. */
     @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
     @Test
     public void testAkkaBlocking() throws Exception {
         final ActorRef pinger = system.actorOf(new Props(
                 AkkaBlockingPinger.class), "blockingPinger");
         final ActorRef ponger = system.actorOf(new Props(
                 AkkaBlockingPonger.class), "blockingPonger");
 
         final Timeout timeout = new Timeout(Duration.create(60, "seconds"));
         final Future<Object> future = Patterns.ask(pinger,
                 AkkaBlockingPinger.hammer(ponger, MESSAGES), timeout);
         final Integer result = (Integer) Await.result(future,
                 timeout.duration());
         if (result.intValue() != MESSAGES) {
             throw new IllegalStateException("Expected " + MESSAGES
                     + " but got " + result);
         }
     }
 
     /** Test in Akka, by having a reply generate the next request, to eliminate blocking. */
     @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
     @Test
     public void testAkkaNonBlocking() throws Exception {
         final ActorRef pinger = system.actorOf(new Props(
                 AkkaNonBlockingPinger.class), "nonBlockingPinger");
         final ActorRef ponger = system.actorOf(new Props(
                 AkkaNonBlockingPonger.class), "nonBlockingPonger");
 
         final Timeout timeout = new Timeout(Duration.create(60, "seconds"));
         final Future<Object> future = Patterns.ask(pinger,
                 AkkaNonBlockingPinger.hammer(ponger, MESSAGES), timeout);
         final Integer result = (Integer) Await.result(future,
                 timeout.duration());
         if (result.intValue() != MESSAGES) {
             throw new IllegalStateException("Expected " + MESSAGES
                     + " but got " + result);
         }
     }
 
     /** Test in JActors, using blocking Futures. */
     @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
     @Test
     public void testJActorBlocking() throws Exception {
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
 
     /** Test in JActors, using the Iterator helper class. */
     @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
     @Test
     public void testJActorIterator() throws Exception {
         final JActorIteratorPinger pinger = new JActorIteratorPinger(
                 jaMailboxFactory.createMailbox());
         final JActorIteratorPonger ponger = new JActorIteratorPonger(
                 pinger.getMailbox());
         final Integer result = pinger.hammer(ponger, MESSAGES);
         if (result.intValue() != MESSAGES) {
             throw new IllegalStateException("Expected " + MESSAGES
                     + " but got " + result);
         }
     }
 
     /** Test non-blocking JActor simplistic impl, which causes occasional Stack-Overflow! */
     @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
     @Test
     public void testJActorStackOverflow() throws Exception {
         final JActorStackOverflowPinger pinger = new JActorStackOverflowPinger(
                 jaMailboxFactory.createMailbox());
         final JActorStackOverflowPonger ponger = new JActorStackOverflowPonger(
                 jaMailboxFactory.createMailbox());
         final Integer result = pinger.hammer(ponger, MESSAGES);
         if (result.intValue() != MESSAGES) {
             throw new IllegalStateException("Expected " + MESSAGES
                     + " but got " + result);
         }
     }
 
     /** Test with PActors, using the pend() method to block. */
     @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
     @Test
     public void testPActorBlocking() throws Exception {
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
 
     /** Test with PActors, by having a reply generate the next request, to eliminate blocking. */
     @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
     @Test
     public void testPActorNonBlocking() throws Exception {
         final PActorNonBlockingPinger pinger = new PActorNonBlockingPinger(
                 paMailboxFactory.createMailbox());
         final PActorNonBlockingPonger ponger = new PActorNonBlockingPonger(
                 pinger.getMailbox());
         final Integer result = pinger.hammer(ponger, MESSAGES);
         if (result.intValue() != MESSAGES) {
             throw new IllegalStateException("Expected " + MESSAGES
                     + " but got " + result);
         }
     }
 
     /** Test with Kilim. */
 /*
     @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
     @Test
     public void testKilim() throws Exception {
         final kilim.Mailbox<Object> pingerMB = new kilim.Mailbox<Object>();
         final kilim.Mailbox<Object> pongerMB = new kilim.Mailbox<Object>();
         final KilimPonger ponger = new KilimPonger(pingerMB, pongerMB);
         final KilimPinger pinger = new KilimPinger(pingerMB, ponger);
         pingerMB.start();
         pongerMB.start();
         final Integer result = pinger.hammer(MESSAGES);
         if (result.intValue() != MESSAGES) {
             throw new IllegalStateException("Expected " + MESSAGES
                     + " but got " + result);
         }
     }
 */
     /** Test with JetLang. */
     @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
     @Test
     public void testJetLang() throws Exception {
         final JetlangPinger pinger = new JetlangPinger(fiberPool.create());
         final JetlangPonger ponger = new JetlangPonger(fiberPool.create());
         final Integer result = pinger.hammer(ponger, MESSAGES);
         if (result.intValue() != MESSAGES) {
             throw new IllegalStateException("Expected " + MESSAGES
                     + " but got " + result);
         }
     }
 }
