 /*
  * Copyright 2009 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class BrowserShutdownAction implements Action {
   private static final Logger LOGGER = LoggerFactory.getLogger(BrowserShutdownAction.class);
 
   private final BrowserStartupAction browserStartupAction;
 
   public BrowserShutdownAction(BrowserStartupAction browserStartupAction) {
     this.browserStartupAction = browserStartupAction;
   }
 
   public void run() {
     List<Process> processes = browserStartupAction.getProcesses();
 
     for (Process process : processes) {
       try {
        process.destroy();
        if (process.exitValue() != 0) {
          LOGGER.warn("Unexpected shutdown " + process + " " + process.exitValue());
        }
       } catch (IllegalThreadStateException e) {
         LOGGER.warn("Process refused to exit" + process);
       }
     } 
   }
 }
