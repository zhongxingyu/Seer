 // ============================================================================
 //   Copyright 2006-2009 Daniel W. Dyer
 //
 //   Licensed under the Apache License, Version 2.0 (the "License");
 //   you may not use this file except in compliance with the License.
 //   You may obtain a copy of the License at
 //
 //       http://www.apache.org/licenses/LICENSE-2.0
 //
 //   Unless required by applicable law or agreed to in writing, software
 //   distributed under the License is distributed on an "AS IS" BASIS,
 //   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //   See the License for the specific language governing permissions and
 //   limitations under the License.
 // ============================================================================
 package org.uncommons.watchmaker.framework;
 
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import org.uncommons.util.concurrent.ConfigurableThreadFactory;
 
 /**
  * This is the class that actually runs the fitness evaluation tasks created by a
  * {@link ConcurrentEvolutionEngine}.  This responsibility is abstracted away from
  * the evolution engine to permit the possibility of creating multiple instances
  * across several machines, all fed by a single shared work queue, using Terracotta
  * (http://www.terracotta.org) or similar.
  * @author Daniel Dyer
  */
 public class FitnessEvaluationWorker
 {
     /**
      * Share this field to use Terracotta to distribute fitness evaluations.
      */
     private final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
 
     
     /**
      * Thread pool that performs concurrent fitness evaluations.
      */
     private final ThreadPoolExecutor executor;
 
     
     FitnessEvaluationWorker()
     {
         ConfigurableThreadFactory threadFactory = new ConfigurableThreadFactory("FitnessEvaluationWorker",
                                                                                 Thread.NORM_PRIORITY,
                                                                                 false);
         this.executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                                                Runtime.getRuntime().availableProcessors(),
                                                60,
                                                TimeUnit.SECONDS,
                                                workQueue,
                                                threadFactory);
         executor.prestartAllCoreThreads();
     }
 
 
     public <T> Future<EvaluatedCandidate<T>> submit(FitnessEvalutationTask<T> task)
     {
         return executor.submit(task);
     }
 
 
     /**
      * Entry-point for running this class standalone, as an additional node for fitness evaluations.
      * If this method is invoked without using Terracotta (or similar) to share the work queue, the
      * program will do nothing.
      */
     public static void main(String[] args)
     {
         // The program will not exit immediately upon completion of the main method because
         // the worker creates non-daemon threads that keep the JVM alive.
         new FitnessEvaluationWorker();
     }
 }
