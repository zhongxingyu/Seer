 /* Copyright (c) 2013, Dźmitry Laŭčuk
    All rights reserved.
 
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met: 
 
    1. Redistributions of source code must retain the above copyright notice, this
       list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
       this list of conditions and the following disclaimer in the documentation
       and/or other materials provided with the distribution.
 
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
 package afc.ant.modular;
 
 import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.CyclicBarrier;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 
 import junit.framework.TestCase;
 
 /**
 * <p>Tests cases when {@link ParallelDependencyResolver} is used by a single thread but
 * multiple modules are acquired simultaneously.</p>
  *
  * @author @author D&#378;mitry La&#365;&#269;uk
  */
 public class ParallelDependencyResolver_ParalleUseTest extends TestCase
 {
     private ParallelDependencyResolver resolver;
     
     @Override
     protected void setUp()
     {
         resolver = new ParallelDependencyResolver();
     }
     
     @Override
     protected void tearDown()
     {
         resolver = null;
     }
     
     /**
      * <p>The module depend upon others in such a way that at the moment there is only
      * a single module that could be processed.</p>
      */
     public void testManyModulesAndThreads_DeterministicResult() throws Exception
     {
         final int n = 100;
         final ArrayList<Module> modules = new ArrayList<Module>(n);
         
         for (int i = 0; i < n; ++i) {
             final Module m = new Module("does_not_matter");
             m.dependencies.addAll(modules);
             modules.add(m);
         }
         
         resolver.init(modules);
         
         final Queue<Module> order = executeConcurrently(resolver, 10);
         
         assertNotNull(order);
         assertEquals(modules, new ArrayList<Module>(order));
     }
     
     private static Queue<Module> executeConcurrently(final ParallelDependencyResolver resolver, final int threadCount)
             throws Exception
     {
         final ConcurrentLinkedQueue<Module> order = new ConcurrentLinkedQueue<Module>();
         final Thread[] threads = new Thread[threadCount];
         final CyclicBarrier barrier = new CyclicBarrier(threadCount);
         final AtomicBoolean failure = new AtomicBoolean();
         final AtomicReference<Throwable> failureCause = new AtomicReference<Throwable>();
         
         for (int i = 0; i < threadCount; ++i) {
             final Thread t = new Thread() {
                 @Override
                 public void run()
                 {
                     try {
                         barrier.await();
                         
                         Module m;
                         while (!failure.get() && (m = resolver.getFreeModule()) != null) {
                             order.add(m);
                             resolver.moduleProcessed(m);
                         }
                     }
                     catch (Throwable ex) {
                         failure.set(true);
                         failureCause.set(ex);
                     }
                 }
             };
             threads[i] = t;
             t.start();
         }
         
         for (int i = 0; i < threadCount; ++i) {
             threads[i].join();
         }
         
         return order;
     }
 }
