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
 
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class ThreadedActionsRunner implements Action {
 
   private final JsTestDriverClient client;
   private final List<ThreadedAction> actions;
   private final ExecutorService executor;
 
   public ThreadedActionsRunner(JsTestDriverClient client, List<ThreadedAction> actions,
       ExecutorService executor) {
     this.client = client;
     this.actions = actions;
     this.executor = executor;
   }
 
   public void run() {
    
     Collection<BrowserInfo> browsers = client.listBrowsers();
     int browsersNumber = browsers.size();
 
     if (browsersNumber == 0) {
       System.err.println("No browsers were captured, nothing to run...");
     }
     CountDownLatch latch = new CountDownLatch(browsersNumber);
 
     for (BrowserInfo browserInfo : browsers) {
       executor.submit(new ThreadedActionRunner(browserInfo.getId().toString(),
           client, latch, actions));
     }
     executor.shutdown();
     try {
       latch.await();
     } catch (InterruptedException e) {
      System.err.println(e);
      System.exit(1);
     }
   }
   
   public List<ThreadedAction> getActions() {
     return actions;
   }
   
   public RunTestsAction getRunTestsAction() {
     for (ThreadedAction action : actions) {
       if (action instanceof RunTestsAction) {
         return (RunTestsAction) action;
       }
     }
     return null;
   }
   
   public JsTestDriverClient getClient() {
     return client;
   }
 }
