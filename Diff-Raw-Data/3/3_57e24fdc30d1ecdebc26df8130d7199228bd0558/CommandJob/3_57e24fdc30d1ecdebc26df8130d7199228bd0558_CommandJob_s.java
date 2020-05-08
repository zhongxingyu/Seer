 /*
  * Copyright 2008 FatWire Corporation. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.fatwire.dta.sscrawler.jobs;
 
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 public class CommandJob implements Job {
 
     private final Set<JobChangeListener> listeners = new CopyOnWriteArraySet<JobChangeListener>();
 
     private final Command command;
 
     /**
     * @param hostConfig
     * @param maxPages
      */
     public CommandJob(final Command command) {
         super();
         this.command = command;
     }
 
     public void schedule() {
     }
 
     public void run(ProgressMonitor monitor) {
         final JobStartedEvent event = new JobStartedEvent(this);
         for (final JobChangeListener listener : listeners) {
             listener.jobStarted(event);
         }
 
         command.execute(monitor);
         final JobFinishedEvent finishedEvent = new JobFinishedEvent(this);
         for (final JobChangeListener listener : listeners) {
 
             listener.jobFinished(finishedEvent);
         }
     }
 
     public void addListener(final JobChangeListener listener) {
         listeners.add(listener);
     }
 
     public void removeListener(final JobChangeListener listener) {
         listeners.remove(listener);
     }
 
 }
