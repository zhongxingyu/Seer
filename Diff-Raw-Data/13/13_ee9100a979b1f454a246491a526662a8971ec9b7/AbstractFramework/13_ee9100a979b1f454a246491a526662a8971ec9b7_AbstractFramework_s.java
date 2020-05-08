 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.palava.core;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.cosmocode.commons.State;
 
 /**
  * Abstract base implementation of the {@link Framework} interface.
  *
  * @since 2.3
  * @author Willi Schoenborn
  */
 abstract class AbstractFramework implements Framework {
 
     private final Logger log = LoggerFactory.getLogger(getClass());
     
     private State state = State.NEW;
     
     @Override
     public final State currentState() {
         return state;
     };
     
     /**
      * Sets the current state to {@link State#FAILED}.
      */
     protected final void fail() {
         this.state = State.FAILED;
     }
     
     @Override
     public final boolean isRunning() {
         return state == State.RUNNING;
     }
     
     @Override
     public final void start() {
         if (state == State.NEW) {
             state = State.STARTING;
             try {
                 log.info("Starting framework");
                 doStart();
                 state = State.RUNNING;
                 log.info("Framework is running");
                 /* CHECKSTYLE:OFF */
             } catch (Exception e) {
                 /* CHECKSTYLE:ON */
                 state = State.FAILED;
                log.error("Starting framework failed", e);
             }
         }
     }
     
     /**
      * Template method for {@link Framework#start()}.
      */
     protected abstract void doStart();
     
     @Override
     public final void stop() {
         if (state == State.RUNNING || state == State.FAILED) {
             final State oldState = state;
             state = State.STOPPING;
             try {
                 log.info("Stopping framework");
                 doStop();
                 state = oldState == State.FAILED ? State.FAILED : State.TERMINATED;
                 log.info("Framework stopped");
                 /* CHECKSTYLE:OFF */
             } catch (Exception e) {
                 /* CHECKSTYLE:ON */
                 state = State.FAILED;
                 log.warn("Stopping framework failed", e);
             }
         }
     }
     
     /**
      * Template method for {@link Framework#stop()}.
      */
     protected abstract void doStop();
     
 
 }
