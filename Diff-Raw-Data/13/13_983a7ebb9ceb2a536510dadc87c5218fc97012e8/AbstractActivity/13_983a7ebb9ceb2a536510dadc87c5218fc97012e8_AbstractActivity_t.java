 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.beanflow;
 
 import org.apache.servicemix.beanflow.support.FieldIntrospector;
 import org.apache.servicemix.beanflow.support.Introspector;
 
 import java.util.Iterator;
 import java.util.concurrent.CountDownLatch;
 
 /**
  * A useful base class which allows simple bean activities to be written easily.
  * When this activity is started it will listen to all the state values which
  * can be found by the introspector (such as all the fields by default) calling
  * the {@link run} method when the state changes so that the activity can be
  * evaluted.
  * 
  * @version $Revision: $
  */
 public abstract class AbstractActivity implements Runnable, Activity {
 
     private State<Transitions> state = new DefaultState<Transitions>(Transitions.Initialised);
     private Introspector introspector = new FieldIntrospector();
     private String failedReason;
     private Throwable failedException;
 
     /**
      * Starts the activity
      */
     public void start() {
         if (state.compareAndSet(Transitions.Initialised, Transitions.Starting)) {
             doStart();
             state.set(Transitions.Started);
         }
     }
 
     /**
      * Stops the activity
      */
     public void stop() {
         if (! state.isAny(Transitions.Stopped, Transitions.Failed)) {
             state.set(Transitions.Stopped);
             doStop();
         }
     }
 
     /**
      * Stops the activity with a failed state, giving the reason for the failure
      */
     public void fail(String reason) {
         this.failedReason = reason;
         state.set(Transitions.Failed);
         doStop();
     }
 
     /**
      * Stops the activity with a failed state with the given reason and
      * exception.
      */
     public void fail(String message, Throwable e) {
         fail(message);
         this.failedException = e;
     }
 
     /**
      * Returns the current running state of this activity
      */
     public State<Transitions> getState() {
         return state;
     }
 
     public boolean isStopped() {
         return state.isAny(Transitions.Stopped, Transitions.Failed);
     }
 
     public boolean isFailed() {
         return state.is(Transitions.Failed);
     }
 
     /**
      * Returns the reason for the failure
      */
     public String getFailedReason() {
         return failedReason;
     }
 
     /**
      * Returns the exception which caused the failure
      */
     public Throwable getFailedException() {
         return failedException;
     }
 
     /**
      * A helper method to add a task to fire when the activity is completed
      */
     public void onStop(final Runnable runnable) {
         getState().addRunnable(new Runnable() {
 
             public void run() {
                 if (isStopped()) {
                     runnable.run();
                 }
             }
         });
     }
 
     /**
      * A helper method to add a task to fire when the activity fails
      */
     public void onFailure(final Runnable runnable) {
         getState().addRunnable(new Runnable() {
 
             public void run() {
                 if (isFailed()) {
                     runnable.run();
                 }
             }
         });
     }
 
     /**
      * A helper method to block the calling thread until the activity completes
      */
     public void join() {
        final CountDownLatch latch = new CountDownLatch(1);
        onStop(new Runnable() {
            public void run() {
                latch.countDown();
            }
        });
         while (!isStopped()) {
             try {
                 latch.await();
             }
             catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
             }
         }
     }
 
     // Implementation methods
     // -------------------------------------------------------------------------
     protected void doStart() {
         addListeners(this);
     }
 
     protected void doStop() {
         removeListeners(this);
     }
 
     protected Introspector getIntrospector() {
         return introspector;
     }
 
     protected void setIntrospector(Introspector introspector) {
         this.introspector = introspector;
     }
 
     protected void addListeners(Runnable listener) {
         if (introspector != null) {
             Iterator<State> iter = introspector.iterator(this);
             while (iter.hasNext()) {
                 iter.next().addRunnable(listener);
             }
         }
     }
 
     protected void removeListeners(Runnable listener) {
         if (introspector != null) {
             Iterator<State> iter = introspector.iterator(this);
             while (iter.hasNext()) {
                 iter.next().removeRunnable(listener);
             }
         }
     }
 }
